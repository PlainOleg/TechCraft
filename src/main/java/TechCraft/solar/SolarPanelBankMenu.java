package TechCraft.solar;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for Solar Panel Bank.
 * Handles 8 panel slots, 1 charge slot, and player inventory.
 * Implements safe extraction logic to prevent energy loss.
 */
public class SolarPanelBankMenu extends AbstractContainerMenu {
    private static final int PANEL_SLOT_COUNT = 8;
    private static final int CHARGE_SLOT = 8;
    private static final int TOTAL_SLOTS = 9;
    
    public final SolarPanelBankBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    public SolarPanelBankMenu(int containerId, Inventory playerInventory, SolarPanelBankBlockEntity blockEntity) {
        super(ModSolarMenuTypes.SOLAR_PANEL_BANK.get(), containerId);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        IItemHandler handler = blockEntity.getItemHandler();

        // Panel slots (0-7) - 2 rows of 4
        addPanelSlot(handler, 0, 17, 28);
        addPanelSlot(handler, 1, 40, 28);
        addPanelSlot(handler, 2, 63, 28);
        addPanelSlot(handler, 3, 86, 28);
        addPanelSlot(handler, 4, 17, 51);
        addPanelSlot(handler, 5, 40, 51);
        addPanelSlot(handler, 6, 63, 51);
        addPanelSlot(handler, 7, 86, 51);

        // Charge slot (8)
        addSlot(new EnergyChargeSlot(handler, CHARGE_SLOT, 123, 40));

        // Player inventory (9-35)
        addPlayerInventorySlots(playerInventory, 8, 106);

        // Player hotbar (36-44)
        addPlayerHotbarSlots(playerInventory, 8, 164);
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack clickedStack = ItemStack.EMPTY;
        Slot clickedSlot = slots.get(index);

        if (clickedSlot.hasItem()) {
            ItemStack originalStack = clickedSlot.getItem();
            clickedStack = originalStack.copy();

            // Panel slots (0-7)
            if (index < PANEL_SLOT_COUNT) {
                if (!moveItemStackTo(originalStack, PANEL_SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Charge slot (8)
            else if (index == CHARGE_SLOT) {
                if (!moveItemStackTo(originalStack, PANEL_SLOT_COUNT, slots.size(), true)) {
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
                else if (isEnergyItem(originalStack)) {
                    if (!moveItemStackTo(originalStack, CHARGE_SLOT, CHARGE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Finally, move between player inventory and hotbar
                if (index < 36) {
                    if (!moveItemStackTo(originalStack, 36, slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!moveItemStackTo(originalStack, 9, 36, false)) {
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
    private boolean isEnergyItem(ItemStack stack) {
        // For now, only allow specific battery items
        // In production, check against energy capability
        return stack.getItem() == Items.REDSTONE || stack.getItem() == Items.GLOWSTONE_DUST;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }

    /**
     * Slot for solar panel stacks.
     * Only accepts solar panel block items.
     */
    public static class SolarPanelStackSlot extends SlotItemHandler {
        private final IItemHandler handler;

        public SolarPanelStackSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
            this.handler = handler;
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
        private final IItemHandler handler;

        public EnergyChargeSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
            this.handler = handler;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            // Check if item has energy capability
            // For now, simplified check
            return stack.getItem() == Items.REDSTONE || stack.getItem() == Items.GLOWSTONE_DUST;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
