package TechCraft.solar;

import TechCraft.item.custom.DrillItem;
import TechCraft.util.NonNegativeMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Block entity for Solar Panel Bank.
 * Stores stacks of solar panels and generates energy based on their combined stats.
 */
public class SolarPanelBankBlockEntity extends BlockEntity implements MenuProvider {
    // Inventory: 36 panel slots (0-35), 6 charging slots (36-41)
    static final int PANEL_SLOT_COUNT = 36;
    static final int CHARGE_SLOT_START = PANEL_SLOT_COUNT;
    static final int CHARGE_SLOT_COUNT = 6;
    private static final int TOTAL_SLOTS = PANEL_SLOT_COUNT + CHARGE_SLOT_COUNT;
    private static final int CHARGE_RATE_PER_SLOT = 1_000;

    // Use ItemStackHandler for inventory management
    private final net.neoforged.neoforge.items.ItemStackHandler itemHandler = new net.neoforged.neoforge.items.ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot < PANEL_SLOT_COUNT) {
                inventoryStatsDirty = true;
            }
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot < PANEL_SLOT_COUNT) {
                return SolarPanelRegistry.isSolarPanelItem(stack.getItem());
            }
            return slot < TOTAL_SLOTS && isChargeable(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= CHARGE_SLOT_START ? 1 : SLOT_STACK_LIMIT;
        }
    };

    // Energy storage
    private long energyStored;
    private long maxEnergyCapacity;
    private long peakGeneration;
    private long currentGeneration;
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            int extracted = (int) Math.min(Math.max(amount, 0), energyStored);
            if (!simulate && extracted > 0) {
                energyStored -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, energyStored);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, maxEnergyCapacity);
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    // Solar conditions
    private long scaledRemainder; // Scaled by 1000
    private boolean skyVisible;
    private int skyCheckTimer;

    // State
    private boolean active;
    private int totalPanelCount;
    private boolean inventoryStatsDirty = true;

    // Configuration
    private static final int SLOT_STACK_LIMIT = 64;
    private static final long ABSOLUTE_MAX_GENERATION = 1179648L;
    private static final long ABSOLUTE_MAX_CAPACITY = 5898240000L;

    public SolarPanelBankBlockEntity(BlockPos pos, BlockState state) {
        super(ModSolarBlockEntities.SOLAR_PANEL_BANK.get(), pos, state);

        this.energyStored = 0;
        this.maxEnergyCapacity = 0;
        this.peakGeneration = 0;
        this.currentGeneration = 0;
        this.scaledRemainder = 0;
        this.skyVisible = false;
        this.skyCheckTimer = 0;
        this.active = false;
        this.totalPanelCount = 0;
    }

    /**
     * Server tick handler for solar panel bank.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBankBlockEntity entity) {
        if (level.isClientSide) {
            return;
        }

        long previousEnergy = entity.energyStored;
        long previousRemainder = entity.scaledRemainder;
        boolean previousSky = entity.skyVisible;
        boolean previousActive = entity.active;
        entity.skyCheckTimer++;
        boolean shouldCheckSky = entity.skyCheckTimer >= SolarGenerationService.getSkyCheckInterval();

        // Check sky visibility periodically
        if (shouldCheckSky) {
            entity.skyVisible = SolarGenerationService.isSkyVisible(level, pos.getX(), pos.getY() + 1, pos.getZ());
            entity.skyCheckTimer = 0;
        }

        // Panel stats only change when one of the panel slots changes.
        if (entity.inventoryStatsDirty) {
            entity.recalculateStats();
            entity.inventoryStatsDirty = false;
        }

        // Calculate generation
        double solarFactor = entity.skyVisible ? SolarGenerationService.calculateSolarFactor(level) : 0.0;
        double remainder = entity.scaledRemainder / 1000.0;

        long[] result = SolarGenerationService.calculateGeneration(
                entity.peakGeneration,
                solarFactor,
                remainder
        );

        long generated = result[0];
        entity.scaledRemainder = result[1];
        entity.currentGeneration = generated;

        // Store generated energy
        if (generated > 0 && entity.maxEnergyCapacity > 0) {
            long space = entity.maxEnergyCapacity - entity.energyStored;
            long toStore = Math.min(generated, space);
            entity.energyStored += toStore;
        }

        entity.chargeItems();

        // Output energy to block below
        if (entity.energyStored > 0) {
            BlockPos belowPos = pos.below();
            IEnergyStorage receiver = level.getCapability(Capabilities.EnergyStorage.BLOCK, belowPos, Direction.UP);
            if (receiver != null && receiver.canReceive()) {
                int offered = (int) Math.min(entity.energyStored, entity.peakGeneration);
                int accepted = receiver.receiveEnergy(offered, false);
                if (accepted > 0) entity.energyStored -= accepted;
            }
        }

        entity.active = generated > 0 && entity.skyVisible && entity.peakGeneration > 0;
        if (entity.energyStored != previousEnergy || entity.scaledRemainder != previousRemainder
                || entity.skyVisible != previousSky || entity.active != previousActive) {
            entity.setChanged();
        }
    }

    private void chargeItems() {
        for (int slot = CHARGE_SLOT_START; slot < TOTAL_SLOTS && energyStored > 0; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int available = (int) Math.min(energyStored, CHARGE_RATE_PER_SLOT);
            IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            int transferred = 0;
            if (storage != null && storage.canReceive()) {
                transferred = storage.receiveEnergy(available, false);
            } else if (stack.getItem() instanceof DrillItem drill) {
                int missing = Math.max(0, drill.getMaxEnergy() - drill.getEnergy(stack));
                transferred = Math.min(available, missing);
                if (transferred > 0) {
                    drill.setEnergy(stack, drill.getEnergy(stack) + transferred);
                }
            }

            if (transferred > 0) {
                energyStored -= transferred;
                itemHandler.setStackInSlot(slot, stack);
            }
        }
    }

    static boolean isChargeable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage != null && storage.canReceive() || stack.getItem() instanceof DrillItem;
    }

    /**
     * Recalculates total generation and capacity based on inventory contents.
     */
    private void recalculateStats() {
        long totalGeneration = 0;
        long totalCapacity = 0;
        int panelCount = 0;

        for (int slot = 0; slot < PANEL_SLOT_COUNT; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                SolarPanelType type = SolarPanelRegistry.getType(stack.getItem());
                if (type != null) {
                    int count = stack.getCount();
                    panelCount += count;

                    // Safe multiplication to prevent overflow
                    totalGeneration = NonNegativeMath.add(totalGeneration, NonNegativeMath.multiply(type.generationPerTick(), count));
                    totalCapacity = NonNegativeMath.add(totalCapacity, NonNegativeMath.multiply(type.energyCapacity(), count));
                }
            }
        }

        // Clamp to absolute maximums
        this.peakGeneration = Math.min(totalGeneration, ABSOLUTE_MAX_GENERATION);
        this.maxEnergyCapacity = Math.min(totalCapacity, ABSOLUTE_MAX_CAPACITY);
        this.totalPanelCount = panelCount;

        // Ensure stored energy doesn't exceed new capacity
        if (this.energyStored > this.maxEnergyCapacity) {
            this.energyStored = this.maxEnergyCapacity;
        }
    }

    /**
     * Checks if removing panels would be safe (energy won't exceed future capacity).
     */
    public boolean isRemovalSafe(int slot, int count) {
        if (slot < 0 || slot >= PANEL_SLOT_COUNT) {
            return true; // Not a panel slot
        }

        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return true;
        }

        SolarPanelType type = SolarPanelRegistry.getType(stack.getItem());
        if (type == null) {
            return true;
        }

        // Calculate future capacity
        long futureCapacity = 0;
        for (int index = 0; index < PANEL_SLOT_COUNT; index++) {
            ItemStack panelStack = itemHandler.getStackInSlot(index);
            SolarPanelType panelType = SolarPanelRegistry.getType(panelStack.getItem());
            if (panelType == null) continue;
            int remaining = panelStack.getCount() - (index == slot ? Math.clamp(count, 0, panelStack.getCount()) : 0);
            futureCapacity = NonNegativeMath.add(futureCapacity,
                    NonNegativeMath.multiply(panelType.energyCapacity(), remaining));
        }
        futureCapacity = Math.min(futureCapacity, ABSOLUTE_MAX_CAPACITY);

        // Check if stored energy would exceed future capacity
        return energyStored <= futureCapacity;
    }

    /**
     * Gets a copy of the inventory as an array.
     */
    public ItemStack[] getInventory() {
        ItemStack[] arr = new ItemStack[TOTAL_SLOTS];
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            arr[i] = itemHandler.getStackInSlot(i);
        }
        return arr;
    }

    /**
     * Gets the item stack at a specific slot.
     */
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= TOTAL_SLOTS) {
            return ItemStack.EMPTY;
        }
        return itemHandler.getStackInSlot(slot);
    }

    /**
     * Sets the item stack at a specific slot.
     */
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= TOTAL_SLOTS) {
            return;
        }
        itemHandler.setStackInSlot(slot, stack);
        setChanged();
    }

    public net.neoforged.neoforge.items.IItemHandler getItemHandler() {
        return itemHandler;
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(long energy) {
        this.energyStored = Math.clamp(energy, 0, maxEnergyCapacity);
        setChanged();
    }

    public long getMaxEnergyCapacity() {
        return maxEnergyCapacity;
    }

    public long getPeakGeneration() {
        return peakGeneration;
    }

    public long getCurrentGeneration() {
        return currentGeneration;
    }

    public int getTotalPanelCount() {
        return totalPanelCount;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSkyVisible() {
        return skyVisible;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // Save inventory via ItemStackHandler
        tag.put("inventory", itemHandler.serializeNBT(registries));

        tag.putLong("energy_stored", energyStored);
        tag.putLong("max_energy_capacity", maxEnergyCapacity);
        tag.putLong("peak_generation", peakGeneration);
        // Preserve the historical bank NBT scale (1000 times the in-memory scale).
        tag.putLong("scaled_remainder", scaledRemainder * 1000);
        tag.putBoolean("sky_visible", skyVisible);
        tag.putBoolean("active", active);
        tag.putInt("total_panel_count", totalPanelCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Load inventory via ItemStackHandler
        if (tag.contains("inventory")) {
            net.neoforged.neoforge.items.ItemStackHandler loaded = new net.neoforged.neoforge.items.ItemStackHandler();
            loaded.deserializeNBT(registries, tag.getCompound("inventory"));
            if (loaded.getSlots() == 9) {
                // Migrate the former 8-panel + 1-charge layout.
                for (int slot = 0; slot < 8; slot++) {
                    itemHandler.setStackInSlot(slot, loaded.getStackInSlot(slot));
                }
                itemHandler.setStackInSlot(CHARGE_SLOT_START, loaded.getStackInSlot(8));
            } else {
                for (int slot = 0; slot < Math.min(loaded.getSlots(), TOTAL_SLOTS); slot++) {
                    itemHandler.setStackInSlot(slot, loaded.getStackInSlot(slot));
                }
            }
        }

        energyStored = Math.max(0, tag.getLong("energy_stored"));
        maxEnergyCapacity = Math.clamp(tag.getLong("max_energy_capacity"), 0, ABSOLUTE_MAX_CAPACITY);
        peakGeneration = Math.clamp(tag.getLong("peak_generation"), 0, ABSOLUTE_MAX_GENERATION);
        scaledRemainder = Math.clamp(tag.getLong("scaled_remainder") / 1000, 0, 999);
        skyVisible = tag.getBoolean("sky_visible");
        active = tag.getBoolean("active");
        totalPanelCount = tag.getInt("total_panel_count");
        inventoryStatsDirty = true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.techcraft.solar_panel_bank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SolarPanelBankMenu(containerId, playerInventory, this);
    }
}
