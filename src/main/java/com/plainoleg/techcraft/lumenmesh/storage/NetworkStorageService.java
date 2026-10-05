package com.plainoleg.techcraft.lumenmesh.storage;

import com.plainoleg.techcraft.lumenmesh.block.storage.PrismDriveBlockEntity;
import com.plainoleg.techcraft.lumenmesh.energy.LumenEnergyService;
import com.plainoleg.techcraft.lumenmesh.item.StorageMediumItem;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.registry.LumenItems;
import com.plainoleg.techcraft.util.NonNegativeMath;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregates every storage medium attached to a single network.
 */
public final class NetworkStorageService {
    private NetworkStorageService() {
    }

    public record NetworkStack(ItemStack prototype, long count) {
    }

    public static List<NetworkStack> snapshot(LumenNetworkManager manager, UUID networkId, HolderLookup.Provider registries) {
        Map<StackKey, NetworkStack> totals = new LinkedHashMap<>();
        // Viewing the index must never hide already stored items. Requiring one
        // last unit of energy here made a successful insert consume the remaining
        // power and immediately made the inserted stack disappear from the GUI.
        if (manager.getNetwork(networkId) == null) return List.of();
        boolean quantumSeen = false;
        for (PrismDriveBlockEntity drive : manager.getNodes(networkId, PrismDriveBlockEntity.class)) {
            for (int slot = 0; slot < drive.getMedia().getSlots(); slot++) {
                ItemStack medium = drive.getMedia().getStackInSlot(slot);
                if (!(medium.getItem() instanceof StorageMediumItem)) continue;
                boolean quantum = medium.is(LumenItems.QUANTUM_STORAGE_PRISM.get());
                if (quantum && quantumSeen) continue;
                quantumSeen |= quantum;
                for (StorageMediumData.StoredStack stored : StorageMediumData.read(medium, registries)) {
                    totals.merge(new StackKey(stored.prototype()), new NetworkStack(stored.prototype(), stored.count()),
                            (left, right) -> new NetworkStack(left.prototype(), NonNegativeMath.add(left.count(), right.count())));
                }
            }
        }
        List<NetworkStack> result = new ArrayList<>(totals.values());
        result.sort(Comparator.comparing(stack -> stack.prototype().getHoverName().getString()));
        return result;
    }

    public static ItemStack insert(LumenNetworkManager manager, UUID networkId, ItemStack input, boolean simulate, HolderLookup.Provider registries) {
        if (!LumenEnergyService.hasEnergy(manager.getNetwork(networkId), LumenEnergyService.STACK_INSERT_COST))
            return input;
        ItemStack remainder = input.copy();
        boolean quantumSeen = false;
        for (PrismDriveBlockEntity drive : manager.getNodes(networkId, PrismDriveBlockEntity.class)) {
            for (int slot = 0; slot < drive.getMedia().getSlots() && !remainder.isEmpty(); slot++) {
                ItemStack medium = drive.getMedia().getStackInSlot(slot);
                boolean quantum = medium.is(LumenItems.QUANTUM_STORAGE_PRISM.get());
                if (quantum && quantumSeen) continue;
                quantumSeen |= quantum;
                int accepted = StorageMediumData.insert(medium, remainder, simulate, registries);
                if (accepted > 0) {
                    remainder.shrink(accepted);
                    if (!simulate) drive.setChanged();
                }
            }
        }
        if (!simulate && remainder.getCount() < input.getCount())
            LumenEnergyService.consumeEnergy(manager.getNetwork(networkId), LumenEnergyService.STACK_INSERT_COST);
        return remainder;
    }

    public static ItemStack extract(LumenNetworkManager manager, UUID networkId, ItemStack requested, int amount, boolean simulate, HolderLookup.Provider registries) {
        if (requested.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        if (!LumenEnergyService.hasEnergy(manager.getNetwork(networkId), LumenEnergyService.STACK_EXTRACT_COST))
            return ItemStack.EMPTY;
        int remaining = Math.min(amount, requested.getMaxStackSize());
        ItemStack result = ItemStack.EMPTY;
        boolean quantumSeen = false;
        for (PrismDriveBlockEntity drive : manager.getNodes(networkId, PrismDriveBlockEntity.class)) {
            for (int slot = 0; slot < drive.getMedia().getSlots() && remaining > 0; slot++) {
                ItemStack medium = drive.getMedia().getStackInSlot(slot);
                boolean quantum = medium.is(LumenItems.QUANTUM_STORAGE_PRISM.get());
                if (quantum && quantumSeen) continue;
                quantumSeen |= quantum;
                ItemStack part = StorageMediumData.extract(medium, requested, remaining, simulate, registries);
                if (!part.isEmpty()) {
                    if (result.isEmpty()) result = part;
                    else result.grow(part.getCount());
                    remaining -= part.getCount();
                    if (!simulate) drive.setChanged();
                }
            }
        }
        if (!simulate && !result.isEmpty())
            LumenEnergyService.consumeEnergy(manager.getNetwork(networkId), LumenEnergyService.STACK_EXTRACT_COST);
        return result;
    }

    /**
     * Private immutable key: count does not participate in stack identity.
     */
    private static final class StackKey {
        private final ItemStack stack;
        private final int hash;

        private StackKey(ItemStack stack) {
            this.stack = stack.copyWithCount(1);
            this.hash = ItemStack.hashItemAndComponents(this.stack);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof StackKey key && ItemStack.isSameItemSameComponents(stack, key.stack);
        }
    }
}
