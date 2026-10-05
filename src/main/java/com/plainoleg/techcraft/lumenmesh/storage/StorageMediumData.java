package com.plainoleg.techcraft.lumenmesh.storage;

import com.plainoleg.techcraft.lumenmesh.item.StorageMediumItem;
import com.plainoleg.techcraft.util.NonNegativeMath;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialization and mutation of item counts stored directly on a storage medium.
 */
public final class StorageMediumData {
    private static final String STORAGE_KEY = "lumen_storage";
    private static final String STACK_KEY = "stack";
    private static final String COUNT_KEY = "count";

    private StorageMediumData() {
    }

    public record StoredStack(ItemStack prototype, long count) {
    }

    public record Statistics(long used, int types) {
    }

    public static List<StoredStack> read(ItemStack medium, HolderLookup.Provider registries) {
        List<StoredStack> result = new ArrayList<>();
        CustomData data = medium.get(DataComponents.CUSTOM_DATA);
        if (data == null) return result;
        ListTag list = data.copyTag().getList(STORAGE_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack.parse(registries, entry.getCompound(STACK_KEY)).ifPresent(stack -> {
                long count = entry.getLong(COUNT_KEY);
                if (!stack.isEmpty() && count > 0) result.add(new StoredStack(stack.copyWithCount(1), count));
            });
        }
        return result;
    }

    private static void write(ItemStack medium, List<StoredStack> entries, HolderLookup.Provider registries) {
        CustomData current = medium.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = current == null ? new CompoundTag() : current.copyTag();
        ListTag list = new ListTag();
        for (StoredStack entry : entries) {
            if (entry.count <= 0 || entry.prototype.isEmpty()) continue;
            CompoundTag value = new CompoundTag();
            // Since 1.21 ItemStack#save returns the encoded tag. It is not
            // guaranteed to mutate the supplied empty CompoundTag; ignoring
            // the return value produced entries with a count but no item id.
            value.put(STACK_KEY, entry.prototype.copyWithCount(1).save(registries));
            value.putLong(COUNT_KEY, entry.count);
            list.add(value);
        }
        root.put(STORAGE_KEY, list);
        medium.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    public static long used(ItemStack medium, HolderLookup.Provider registries) {
        return statistics(medium, registries).used();
    }

    public static Statistics statistics(ItemStack medium, HolderLookup.Provider registries) {
        long total = 0;
        List<StoredStack> entries = read(medium, registries);
        for (StoredStack entry : entries) {
            total = NonNegativeMath.add(total, entry.count);
        }
        return new Statistics(total, entries.size());
    }

    public static int typeCount(ItemStack medium, HolderLookup.Provider registries) {
        return read(medium, registries).size();
    }

    public static int insert(ItemStack medium, ItemStack stack, boolean simulate, HolderLookup.Provider registries) {
        if (!(medium.getItem() instanceof StorageMediumItem type) || stack.isEmpty()
                || stack.getItem() instanceof StorageMediumItem) return 0;

        List<StoredStack> entries = read(medium, registries);
        int matching = -1;
        long used = 0;
        for (int i = 0; i < entries.size(); i++) {
            StoredStack entry = entries.get(i);
            used = used > Long.MAX_VALUE - entry.count ? Long.MAX_VALUE : used + entry.count;
            if (ItemStack.isSameItemSameComponents(entry.prototype, stack)) matching = i;
        }
        if (matching < 0 && entries.size() >= type.typeLimit()) return 0;

        long room = type.isInfinite() ? Long.MAX_VALUE : Math.max(0, type.capacity() - used);
        // Infinite media still use a signed long for each individual item count.
        if (matching >= 0) room = Math.min(room, Long.MAX_VALUE - entries.get(matching).count);
        int accepted = (int) Math.min(stack.getCount(), room);
        if (!simulate && accepted > 0) {
            if (matching >= 0) {
                StoredStack old = entries.get(matching);
                entries.set(matching, new StoredStack(old.prototype, old.count + accepted));
            } else {
                entries.add(new StoredStack(stack.copyWithCount(1), accepted));
            }
            write(medium, entries, registries);
        }
        return accepted;
    }

    public static ItemStack extract(ItemStack medium, ItemStack requested, int amount, boolean simulate, HolderLookup.Provider registries) {
        if (!(medium.getItem() instanceof StorageMediumItem) || requested.isEmpty() || amount <= 0)
            return ItemStack.EMPTY;
        List<StoredStack> entries = read(medium, registries);
        for (int i = 0; i < entries.size(); i++) {
            StoredStack entry = entries.get(i);
            if (!ItemStack.isSameItemSameComponents(entry.prototype, requested)) continue;
            int extracted = (int) Math.min(Math.min(entry.count, amount), requested.getMaxStackSize());
            if (!simulate && extracted > 0) {
                long remaining = entry.count - extracted;
                if (remaining == 0) entries.remove(i);
                else entries.set(i, new StoredStack(entry.prototype, remaining));
                write(medium, entries, registries);
            }
            return entry.prototype.copyWithCount(extracted);
        }
        return ItemStack.EMPTY;
    }
}
