package com.plainoleg.techcraft.lumenmesh.block.terminal;

import com.plainoleg.techcraft.lumenmesh.LumenMeshIntegration;
import com.plainoleg.techcraft.lumenmesh.block.device.LumenDeviceBlockEntity;
import com.plainoleg.techcraft.lumenmesh.menu.ItemTerminalMenu;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkNode;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;
import com.plainoleg.techcraft.lumenmesh.storage.NetworkStorageService;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ItemTerminalBlockEntity extends LumenDeviceBlockEntity implements MenuProvider {
    public static final int VISIBLE_SLOTS = 45;

    public ItemTerminalBlockEntity(BlockPos pos, BlockState state) {
        this(LumenBlockEntities.ITEM_TERMINAL.get(), pos, state);
    }

    protected ItemTerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public NodeType getNodeType() { return LumenNetworkNode.NodeType.ITEM_TERMINAL; }
    public void serverTick() { refreshActiveState(); }
    @Override public Component getDisplayName() { return Component.translatable("block.techcraft.item_terminal"); }

    public int getMaxPage() {
        if (level == null || getNetworkId() == null) return 0;
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        if (manager == null) return 0;
        int size = NetworkStorageService.snapshot(manager, getNetworkId(), level.registryAccess()).size();
        return Math.max(0, (size - 1) / VISIBLE_SLOTS);
    }

    public List<NetworkStorageService.NetworkStack> networkStacks() {
        if (level == null || getNetworkId() == null) return List.of();
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        return manager == null ? List.of() : NetworkStorageService.snapshot(manager, getNetworkId(), level.registryAccess());
    }

    public IItemHandler createNetworkHandler(IntSupplier page, Supplier<List<NetworkStorageService.NetworkStack>> view) {
        return new IItemHandlerModifiable() {
            private final ItemStack[] clientStacks = new ItemStack[VISIBLE_SLOTS];
            {
                java.util.Arrays.fill(clientStacks, ItemStack.EMPTY);
            }

            private boolean clientSide() {
                return level == null || level.isClientSide;
            }

            private List<NetworkStorageService.NetworkStack> stacks() {
                return view.get();
            }
            @Override public int getSlots() { return VISIBLE_SLOTS; }
            @Override public ItemStack getStackInSlot(int slot) {
                if (slot < 0 || slot >= VISIBLE_SLOTS) return ItemStack.EMPTY;
                // AbstractContainerMenu synchronizes remote slot contents through
                // Slot#set. Keep that server-sent value on the client instead of
                // trying to query the server-only network manager locally.
                if (clientSide()) return clientStacks[slot];
                List<NetworkStorageService.NetworkStack> stacks = stacks();
                int index = slot + page.getAsInt() * VISIBLE_SLOTS;
                if (index >= stacks.size()) return ItemStack.EMPTY;
                var stored = stacks.get(index);
                // This is a virtual display stack, so its count represents the
                // network total rather than a physical stack limit.
                return stored.prototype().copyWithCount((int) Math.min(stored.count(), Integer.MAX_VALUE));
            }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (clientSide() || getNetworkId() == null) return stack;
                LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
                return manager == null ? stack : NetworkStorageService.insert(manager, getNetworkId(), stack, simulate, level.registryAccess());
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack shown = getStackInSlot(slot);
                if (shown.isEmpty() || clientSide() || getNetworkId() == null) return ItemStack.EMPTY;
                LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
                return manager == null ? ItemStack.EMPTY : NetworkStorageService.extract(manager, getNetworkId(), shown, amount, simulate, level.registryAccess());
            }
            @Override public int getSlotLimit(int slot) { return 99; }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
            @Override public void setStackInSlot(int slot, ItemStack stack) {
                if (clientSide() && slot >= 0 && slot < VISIBLE_SLOTS) {
                    clientStacks[slot] = stack.copy();
                }
            }
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ItemTerminalMenu(id, inventory, this);
    }
}
