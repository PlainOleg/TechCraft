package TechCraft.solar;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menu for Solar Panel Bank.
 * Handles 36 panel slots, 6 charging slots, and player inventory.
 * Implements safe extraction logic to prevent energy loss.
 */
public class SolarPanelBankMenu extends AbstractContainerMenu {
    private static final int PANEL_SLOT_COUNT = SolarPanelBankBlockEntity.PANEL_SLOT_COUNT;
    private static final int CHARGE_SLOT_START = SolarPanelBankBlockEntity.CHARGE_SLOT_START;
    private static final int CHARGE_SLOT_COUNT = SolarPanelBankBlockEntity.CHARGE_SLOT_COUNT;
    private static final int MACHINE_SLOT_COUNT = PANEL_SLOT_COUNT + CHARGE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;

    public final SolarPanelBankBlockEntity blockEntity;
    private long energyStored;
    private long maxEnergyCapacity;
    private long currentGeneration;
    private long peakGeneration;
    private int panelCount;
    private boolean skyVisible;

    public SolarPanelBankMenu(int containerId, Inventory playerInventory, SolarPanelBankBlockEntity blockEntity) {
        super(ModSolarMenuTypes.SOLAR_PANEL_BANK.get(), containerId);
        this.blockEntity = blockEntity;

        IItemHandler handler = blockEntity.getItemHandler();

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                addPanelSlot(handler, slot, 16 + column * 18, 14 + row * 18);
            }
        }

        for (int slot = 0; slot < CHARGE_SLOT_COUNT; slot++) {
            addSlot(new EnergyChargeSlot(handler, CHARGE_SLOT_START + slot, 43 + slot * 18, 136));
        }

        addPlayerInventorySlots(playerInventory, 16, 161);
        addPlayerHotbarSlots(playerInventory, 16, 219);
        addDataSlots(createData());
    }

    public SolarPanelBankMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, (SolarPanelBankBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    private void addPanelSlot(IItemHandler handler, int index, int x, int y) {
        addSlot(new SolarPanelStackSlot(handler, index, x, y));
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int xOffset, int yOffset) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = 9 + row * 9 + col;
                int x = xOffset + col * 18;
                int y = yOffset + row * 18;
                addSlot(new Slot(playerInventory, index, x, y));
            }
        }
    }

    private void addPlayerHotbarSlots(Inventory playerInventory, int xOffset, int yOffset) {
        for (int col = 0; col < 9; col++) {
            int index = col;
            int x = xOffset + col * 18;
            addSlot(new Slot(playerInventory, index, x, yOffset));
        }
    }

    private ContainerData createData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) blockEntity.getEnergyStored();
                    case 1 -> (int) (blockEntity.getEnergyStored() >>> 32);
                    case 2 -> (int) blockEntity.getMaxEnergyCapacity();
                    case 3 -> (int) (blockEntity.getMaxEnergyCapacity() >>> 32);
                    case 4 -> (int) blockEntity.getCurrentGeneration();
                    case 5 -> (int) (blockEntity.getCurrentGeneration() >>> 32);
                    case 6 -> (int) blockEntity.getPeakGeneration();
                    case 7 -> (int) (blockEntity.getPeakGeneration() >>> 32);
                    case 8 -> blockEntity.getTotalPanelCount();
                    case 9 -> blockEntity.isSkyVisible() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> energyStored = withLow(energyStored, value);
                    case 1 -> energyStored = withHigh(energyStored, value);
                    case 2 -> maxEnergyCapacity = withLow(maxEnergyCapacity, value);
                    case 3 -> maxEnergyCapacity = withHigh(maxEnergyCapacity, value);
                    case 4 -> currentGeneration = withLow(currentGeneration, value);
                    case 5 -> currentGeneration = withHigh(currentGeneration, value);
                    case 6 -> peakGeneration = withLow(peakGeneration, value);
                    case 7 -> peakGeneration = withHigh(peakGeneration, value);
                    case 8 -> panelCount = value;
                    case 9 -> skyVisible = value != 0;
                }
            }

            @Override
            public int getCount() {
                return 10;
            }
        };
    }

    private static long withLow(long current, int low) {
        return current & 0xFFFFFFFF00000000L | Integer.toUnsignedLong(low);
    }

    private static long withHigh(long current, int high) {
        return current & 0xFFFFFFFFL | (long) high << 32;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        ItemStack clickedStack = ItemStack.EMPTY;
        Slot clickedSlot = slots.get(index);

        if (clickedSlot.hasItem()) {
            ItemStack originalStack = clickedSlot.getItem();
            clickedStack = originalStack.copy();

            if (index < MACHINE_SLOT_COUNT) {
                if (!moveItemStackTo(originalStack, PLAYER_INVENTORY_START, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Player inventory/hotbar
            else {
                // Try to move to panel slots first
                if (SolarPanelRegistry.isSolarPanelItem(originalStack.getItem())) {
                    if (!moveItemStackTo(originalStack, 0, PANEL_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Then try charge slot
                else if (SolarPanelBankBlockEntity.isChargeable(originalStack)) {
                    if (!moveItemStackTo(originalStack, CHARGE_SLOT_START, MACHINE_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < PLAYER_HOTBAR_START) {
                    if (!moveItemStackTo(originalStack, PLAYER_HOTBAR_START, slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!moveItemStackTo(originalStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            clickedSlot.setChanged();
            if (originalStack.isEmpty()) {
                clickedSlot.setByPlayer(ItemStack.EMPTY);
            } else {
                clickedSlot.onTake(player, originalStack);
            }
        }

        return clickedStack;
    }

    /**
     * Checks if an item is an energy item (can be placed in charge slot).
     * This is a simplified check - in a full implementation, this would check
     * against the energy API.
     */
    @Override
    public boolean stillValid(Player player) {
        return TechCraft.util.MenuAccess.stillValid(player, blockEntity);
    }

    /**
     * Slot for solar panel stacks.
     * Only accepts solar panel block items.
     */
    public static class SolarPanelStackSlot extends SlotItemHandler {
        public SolarPanelStackSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return SolarPanelRegistry.isSolarPanelItem(stack.getItem());
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 64;
        }
    }

    /**
     * Slot for charging energy items.
     * Only accepts items that can store energy.
     */
    public static class EnergyChargeSlot extends SlotItemHandler {
        public EnergyChargeSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return SolarPanelBankBlockEntity.isChargeable(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getMaxEnergyCapacity() {
        return maxEnergyCapacity;
    }

    public long getCurrentGeneration() {
        return currentGeneration;
    }

    public long getPeakGeneration() {
        return peakGeneration;
    }

    public int getPanelCount() {
        return panelCount;
    }

    public boolean isSkyVisible() {
        return skyVisible;
    }
}
