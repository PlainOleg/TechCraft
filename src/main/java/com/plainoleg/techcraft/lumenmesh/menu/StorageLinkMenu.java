package com.plainoleg.techcraft.lumenmesh.menu;

import com.plainoleg.techcraft.lumenmesh.block.storage.StorageLinkBlockEntity;
import com.plainoleg.techcraft.lumenmesh.registry.LumenMenuTypes;
import com.plainoleg.techcraft.util.MenuAccess;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class StorageLinkMenu extends AbstractContainerMenu {
    private final StorageLinkBlockEntity link;
    private int connected, networked, blacklist, autoIn, autoOut, lastMoved;

    public StorageLinkMenu(int id, Inventory inv, StorageLinkBlockEntity link) {
        super(LumenMenuTypes.STORAGE_LINK.get(), id);
        this.link = link;
        for (int i = 0; i < StorageLinkBlockEntity.FILTERS; i++)
            addSlot(new SlotItemHandler(link.filters(), i, 52 + i * 24, 51) {
                @Override
                public boolean mayPickup(Player p) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack s) {
                    return false;
                }
            });
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 31 + c * 18, 128 + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 31 + c * 18, 186));
        addDataSlot(data(() -> link.hasExternalInventory() ? 1 : 0, v -> connected = v));
        addDataSlot(data(() -> link.getNetworkId() != null ? 1 : 0, v -> networked = v));
        addDataSlot(data(() -> link.isBlacklist() ? 1 : 0, v -> blacklist = v));
        addDataSlot(data(() -> link.isAutoToNetwork() ? 1 : 0, v -> autoIn = v));
        addDataSlot(data(() -> link.isAutoToExternal() ? 1 : 0, v -> autoOut = v));
        addDataSlot(data(link::getLastMoved, v -> lastMoved = v));
    }

    public StorageLinkMenu(int id, Inventory inv, RegistryFriendlyByteBuf data) {
        this(id, inv, (StorageLinkBlockEntity) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    private static DataSlot data(java.util.function.IntSupplier get, java.util.function.IntConsumer set) {
        return new DataSlot() {
            public int get() {
                return get.getAsInt();
            }

            public void set(int v) {
                set.accept(v);
            }
        };
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId >= 0 && slotId < StorageLinkBlockEntity.FILTERS) {
            if (!player.level().isClientSide) {
                ItemStack held = getCarried();
                link.filters().setStackInSlot(slotId, button == 1 || held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, type, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level().isClientSide) return true;
        switch (id) {
            case 0 -> link.moveToNetwork();
            case 1 -> link.moveToExternal();
            case 2 -> link.toggleAutoToNetwork();
            case 3 -> link.toggleAutoToExternal();
            case 4 -> link.toggleBlacklist();
            default -> {
                return false;
            }
        }
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        if (index < StorageLinkBlockEntity.FILTERS || index >= slots.size()) return ItemStack.EMPTY;
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player p) {
        return MenuAccess.stillValid(p, link);
    }

    public boolean connected() {
        return connected != 0;
    }

    public boolean networked() {
        return networked != 0;
    }

    public boolean blacklist() {
        return blacklist != 0;
    }

    public boolean autoIn() {
        return autoIn != 0;
    }

    public boolean autoOut() {
        return autoOut != 0;
    }

    public int lastMoved() {
        return lastMoved;
    }
}
