package com.plainoleg.techcraft.lumenmesh.menu;

import com.plainoleg.techcraft.lumenmesh.block.terminal.ItemTerminalBlockEntity;
import com.plainoleg.techcraft.lumenmesh.registry.LumenMenuTypes;
import com.plainoleg.techcraft.lumenmesh.storage.NetworkStorageService;
import com.plainoleg.techcraft.util.MenuAccess;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class ItemTerminalMenu extends AbstractContainerMenu {
    private static final int NETWORK_SLOTS = ItemTerminalBlockEntity.VISIBLE_SLOTS;
    private final ItemTerminalBlockEntity terminal;
    private int page;
    private int maxPage;
    private int sortDescending = 1;
    private int modIndex;
    private String search = "";
    // Used only to encode the selected namespace into a DataSlot. The actual
    // filter choices are rebuilt from the current network storage snapshot.
    private final List<String> registeredMods = collectRegisteredMods();
    private List<NetworkStorageService.NetworkStack> broadcastView;

    public ItemTerminalMenu(int id, Inventory inventory, ItemTerminalBlockEntity terminal) {
        this(LumenMenuTypes.ITEM_TERMINAL.get(), id, inventory, terminal, 9, 5, 28, 43);
    }

    protected ItemTerminalMenu(MenuType<?> type, int id, Inventory inventory, ItemTerminalBlockEntity terminal) {
        this(type, id, inventory, terminal, 9, 5, 28, 43);
    }

    protected ItemTerminalMenu(MenuType<?> type, int id, Inventory inventory, ItemTerminalBlockEntity terminal,
                               int networkColumns, int networkRows, int networkX, int networkY) {
        super(type, id);
        this.terminal = terminal;
        this.maxPage = calculateMaxPage();
        IItemHandler handler = terminal == null ? new ItemStackHandler(NETWORK_SLOTS) : terminal.createNetworkHandler(() -> page, this::filteredStacks);
        int networkSlot = 0;
        for (int row = 0; row < networkRows && networkSlot < NETWORK_SLOTS; row++)
            for (int col = 0; col < networkColumns && networkSlot < NETWORK_SLOTS; col++)
                addSlot(new NetworkSlot(handler, networkSlot++, networkX + col * 18, networkY + row * 18));
        addPlayerInventory(inventory);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return page;
            }

            @Override
            public void set(int value) {
                page = Math.max(0, value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return sortDescending;
            }

            @Override
            public void set(int value) {
                sortDescending = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return modIndex;
            }

            @Override
            public void set(int value) {
                modIndex = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                maxPage = calculateMaxPage();
                page = Math.min(page, maxPage);
                return maxPage;
            }

            @Override
            public void set(int value) {
                maxPage = Math.max(0, value);
                page = Math.min(page, maxPage);
            }
        });
    }

    public ItemTerminalMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, (ItemTerminalBlockEntity) inventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new net.minecraft.world.inventory.Slot(inventory, col + row * 9 + 9, 28 + col * 18, 151 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new net.minecraft.world.inventory.Slot(inventory, col, 28 + col * 18, 209));
    }

    @Override
    public boolean stillValid(Player player) {
        return MenuAccess.stillValid(player, terminal);
    }

    public int getPage() {
        return page;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public boolean isSortDescending() {
        return sortDescending != 0;
    }

    public int getModIndex() {
        return modIndex;
    }

    public String getSearch() {
        return search;
    }

    private static List<String> collectRegisteredMods() {
        return BuiltInRegistries.ITEM.keySet().stream().map(id -> id.getNamespace()).distinct()
                .sorted(Comparator.comparingInt((String id) -> id.equals("minecraft") ? 0 : id.equals("techcraft") ? 1 : 2).thenComparing(id -> id)).toList();
    }

    static List<String> collectStoredMods(List<NetworkStorageService.NetworkStack> stacks) {
        var namespaces = new LinkedHashSet<String>();
        for (var stack : stacks) {
            namespaces.add(BuiltInRegistries.ITEM.getKey(stack.prototype().getItem()).getNamespace());
        }
        return namespaces.stream()
                .sorted(Comparator.comparingInt((String id) -> id.equals("minecraft") ? 0 : id.equals("techcraft") ? 1 : 2).thenComparing(id -> id))
                .toList();
    }

    private List<NetworkStorageService.NetworkStack> filteredStacks() {
        if (broadcastView != null) return broadcastView;
        if (terminal == null || terminal.getLevel() == null || terminal.getLevel().isClientSide) return List.of();
        List<NetworkStorageService.NetworkStack> source = terminal.networkStacks();
        List<String> storedMods = collectStoredMods(source);
        String selectedMod = getSelectedModNamespace();
        if (!selectedMod.isEmpty() && !storedMods.contains(selectedMod)) {
            modIndex = 0;
            selectedMod = "";
        }
        String needle = search.toLowerCase(Locale.ROOT).trim();
        List<NetworkStorageService.NetworkStack> result = new ArrayList<>();
        for (var stack : source) {
            String id = BuiltInRegistries.ITEM.getKey(stack.prototype().getItem()).toString();
            if (!selectedMod.isEmpty() && !id.startsWith(selectedMod + ":")) continue;
            if (!needle.isEmpty() && !stack.prototype().getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)
                    && !id.toLowerCase(Locale.ROOT).contains(needle)) continue;
            result.add(stack);
        }
        Comparator<NetworkStorageService.NetworkStack> order = Comparator.comparingLong(NetworkStorageService.NetworkStack::count);
        if (sortDescending != 0) order = order.reversed();
        result.sort(order.thenComparing(s -> BuiltInRegistries.ITEM.getKey(s.prototype().getItem()).toString()));
        return result;
    }

    private int calculateMaxPage() {
        if (terminal == null || terminal.getLevel() == null || terminal.getLevel().isClientSide) return maxPage;
        return Math.max(0, (filteredStacks().size() - 1) / NETWORK_SLOTS);
    }

    @Override
    public void broadcastChanges() {
        // One consistent view for all 45 slots and data fields in this broadcast.
        // Do not keep it between broadcasts: media can also change via automation.
        broadcastView = filteredStacks();
        try {
            maxPage = calculateMaxPage();
            page = Math.min(page, maxPage);
            super.broadcastChanges();
        } finally {
            broadcastView = null;
        }
    }

    public String getSelectedModNamespace() {
        return modIndex > 0 && modIndex <= registeredMods.size() ? registeredMods.get(modIndex - 1) : "";
    }

    private void filtersChanged() {
        page = 0;
        maxPage = calculateMaxPage();
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && page > 0) page--;
        else if (id == 1 && page < maxPage) page++;
        else if (id == 2) {
            sortDescending = sortDescending == 0 ? 1 : 0;
            filtersChanged();
            return true;
        } else if (id == 3) {
            List<String> storedMods = terminal == null ? List.of() : collectStoredMods(terminal.networkStacks());
            String selectedMod = getSelectedModNamespace();
            int selectedPosition = storedMods.indexOf(selectedMod);
            String nextMod = selectedPosition + 1 < storedMods.size() ? storedMods.get(selectedPosition + 1) : "";
            modIndex = nextMod.isEmpty() ? 0 : registeredMods.indexOf(nextMod) + 1;
            filtersChanged();
            return true;
        } else if (id == 1000) {
            search = "";
            filtersChanged();
            return true;
        } else if ((id & 0xF0000) == 0x10000 && search.length() < 40) {
            search += (char) (id & 0xFFFF);
            filtersChanged();
            return true;
        } else if (id == 0x20000 && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            filtersChanged();
            return true;
        } else return false;
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        if (index < NETWORK_SLOTS) {
            var slot = slots.get(index);
            if (!slot.hasItem()) return ItemStack.EMPTY;
            ItemStack shown = slot.getItem();
            int amount = Math.min(shown.getMaxStackSize(), playerInventorySpace(shown));
            if (amount == 0) return ItemStack.EMPTY;
            ItemStack extracted = slot.remove(amount);
            if (extracted.isEmpty()) return ItemStack.EMPTY;
            ItemStack original = extracted.copy();
            moveItemStackTo(extracted, NETWORK_SLOTS, slots.size(), true);
            // If inventory rules changed during transfer, keep the remainder
            // with the player. Returning it to the network may require unavailable energy.
            if (!extracted.isEmpty()) player.getInventory().placeItemBackInInventory(extracted);
            slot.onTake(player, original);
            return original;
        }
        var playerSlot = slots.get(index);
        if (!playerSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = playerSlot.getItem();
        ItemStack original = source.copy();
        ItemStack remainder = ((NetworkSlot) slots.get(0)).getItemHandler().insertItem(0, source, false);
        if (remainder.getCount() == source.getCount()) return ItemStack.EMPTY;
        source.setCount(remainder.getCount());
        if (source.isEmpty()) playerSlot.setByPlayer(ItemStack.EMPTY);
        else playerSlot.setChanged();
        return original;
    }

    private int playerInventorySpace(ItemStack stack) {
        int space = 0;
        for (int index = NETWORK_SLOTS; index < slots.size(); index++) {
            var slot = slots.get(index);
            if (!slot.mayPlace(stack)) continue;
            ItemStack current = slot.getItem();
            if (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack)) {
                space += Math.max(0, slot.getMaxStackSize(stack) - current.getCount());
                if (space >= stack.getMaxStackSize()) return stack.getMaxStackSize();
            }
        }
        return space;
    }

    @Override
    public boolean canDragTo(net.minecraft.world.inventory.Slot slot) {
        // Virtual aggregate slots cannot participate in vanilla QUICK_CRAFT.
        // MouseTweaks still works with the real player inventory slots.
        return !(slot instanceof NetworkSlot);
    }

    private static class NetworkSlot extends SlotItemHandler {
        NetworkSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public void set(ItemStack stack) {
            if (getItemHandler() instanceof IItemHandlerModifiable modifiable)
                modifiable.setStackInSlot(getSlotIndex(), stack);
        }

        @Override
        public boolean isFake() {
            return true;
        }

        @Override
        public ItemStack safeInsert(ItemStack stack, int increment) {
            int offered = Math.min(stack.getCount(), increment);
            ItemStack part = stack.copyWithCount(offered);
            ItemStack remainder = getItemHandler().insertItem(getSlotIndex(), part, false);
            int inserted = offered - remainder.getCount();
            stack.shrink(inserted);
            // Slot.safeInsert returns the remainder, not the inserted portion.
            return stack;
        }
    }
}
