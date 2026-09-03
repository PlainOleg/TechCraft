package TechCraft.solar;

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

/**
 * Block entity for Solar Panel Bank.
 * Stores stacks of solar panels and generates energy based on their combined stats.
 */
public class SolarPanelBankBlockEntity extends BlockEntity implements MenuProvider {
    // Inventory: 8 slots for panels (0-7), 1 charge slot (8)
    private static final int PANEL_SLOT_COUNT = 8;
    private static final int CHARGE_SLOT = 8;
    private static final int TOTAL_SLOTS = 9;

    // Use ItemStackHandler for inventory management
    private final net.neoforged.neoforge.items.ItemStackHandler itemHandler = new net.neoforged.neoforge.items.ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // Energy storage
    private long energyStored;
    private long maxEnergyCapacity;
    private long peakGeneration;
    private long currentGeneration;
    
    // Solar conditions
    private double scaledRemainder; // Scaled by 1000
    private boolean skyVisible;
    private int skyCheckTimer;
    
    // State
    private boolean active;
    private int totalPanelCount;
    
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

        entity.skyCheckTimer++;
        boolean shouldCheckSky = entity.skyCheckTimer >= SolarGenerationService.getSkyCheckInterval();

        // Check sky visibility periodically
        if (shouldCheckSky) {
            entity.skyVisible = SolarGenerationService.isSkyVisible(level, pos.getX(), pos.getY() + 1, pos.getZ());
            entity.skyCheckTimer = 0;
        }

        // Recalculate generation and capacity
        entity.recalculateStats();

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

        entity.active = generated > 0 && entity.skyVisible && entity.peakGeneration > 0;
        entity.setChanged();
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
                    totalGeneration = saturatedAdd(totalGeneration, saturatedMultiply(type.generationPerTick(), count));
                    totalCapacity = saturatedAdd(totalCapacity, saturatedMultiply(type.energyCapacity(), count));
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
     * Safe multiplication that prevents overflow.
     */
    private static long saturatedMultiply(long a, long b) {
        if (a == 0 || b == 0) return 0;
        if (a == Long.MIN_VALUE || b == Long.MIN_VALUE) return Long.MIN_VALUE;
        
        long result = a * b;
        if (a == result / b) {
            return result;
        }
        return Long.MAX_VALUE;
    }

    /**
     * Safe addition that prevents overflow.
     */
    private static long saturatedAdd(long a, long b) {
        long result = a + b;
        if ((a ^ b) < 0 || (a ^ result) >= 0) {
            return result;
        }
        return result > 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
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
        long capacityToRemove = saturatedMultiply(type.energyCapacity(), count);
        long futureCapacity = maxEnergyCapacity - capacityToRemove;

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

    public net.neoforged.neoforge.items.IItemHandler getItemHandler() { return itemHandler; }

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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // Save inventory via ItemStackHandler
        tag.put("inventory", itemHandler.serializeNBT(registries));

        tag.putLong("energy_stored", energyStored);
        tag.putLong("max_energy_capacity", maxEnergyCapacity);
        tag.putLong("peak_generation", peakGeneration);
        tag.putLong("scaled_remainder", (long) (scaledRemainder * 1000));
        tag.putBoolean("sky_visible", skyVisible);
        tag.putBoolean("active", active);
        tag.putInt("total_panel_count", totalPanelCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Load inventory via ItemStackHandler
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }

        energyStored = tag.getLong("energy_stored");
        maxEnergyCapacity = tag.getLong("max_energy_capacity");
        peakGeneration = tag.getLong("peak_generation");
        scaledRemainder = tag.getLong("scaled_remainder") / 1000.0;
        skyVisible = tag.getBoolean("sky_visible");
        active = tag.getBoolean("active");
        totalPanelCount = tag.getInt("total_panel_count");
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
