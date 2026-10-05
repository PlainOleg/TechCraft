package com.plainoleg.techcraft.lumenmesh.menu;

import com.plainoleg.techcraft.lumenmesh.block.LumenFacingEntityBlock;
import com.plainoleg.techcraft.lumenmesh.block.storage.PrismDriveBlockEntity;
import com.plainoleg.techcraft.lumenmesh.registry.LumenMenuTypes;
import com.plainoleg.techcraft.util.MenuAccess;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PrismDriveMenu extends AbstractContainerMenu {
    private final PrismDriveBlockEntity drive;
    private int networked;
    private int active;

    public PrismDriveMenu(int id, Inventory inventory, PrismDriveBlockEntity drive) {
        super(LumenMenuTypes.PRISM_DRIVE.get(), id);
        this.drive = drive;
        for (int i = 0; i < PrismDriveBlockEntity.MEDIA_SLOTS; i++) {
            addSlot(new SlotItemHandler(drive.getMedia(), i, 44 + i * 22, 36));
        }
        addPlayerInventory(inventory);
        addDataSlots(createData());
    }

    public PrismDriveMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, (PrismDriveBlockEntity) inventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new net.minecraft.world.inventory.Slot(inventory, col + row * 9 + 9, 7 + col * 18, 90 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new net.minecraft.world.inventory.Slot(inventory, col, 7 + col * 18, 148));
    }

    private ContainerData createData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> drive.getNetworkId() == null ? 0 : 1;
                    case 1 ->
                            drive.getBlockState().getValue(LumenFacingEntityBlock.ACTIVE) ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) networked = value;
                if (index == 1) active = value;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public boolean isNetworked() {
        return networked != 0;
    }

    public boolean isActive() {
        return active != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return MenuAccess.stillValid(player, drive);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        ItemStack result = ItemStack.EMPTY;
        var slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < PrismDriveBlockEntity.MEDIA_SLOTS) {
                if (!moveItemStackTo(stack, PrismDriveBlockEntity.MEDIA_SLOTS, slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, PrismDriveBlockEntity.MEDIA_SLOTS, false)) return ItemStack.EMPTY;
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }
}
