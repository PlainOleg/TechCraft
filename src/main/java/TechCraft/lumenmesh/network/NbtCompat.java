package TechCraft.lumenmesh.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Чтение списков из NBT в двух форматах: текущем ({@link ListTag}) и старом
 * (CompoundTag с ключами {@code node_0}, {@code node_1}, ...), чтобы старые миры загружались без потерь.
 */
final class NbtCompat {
    private NbtCompat() {
    }

    static List<CompoundTag> readCompounds(CompoundTag tag, String key) {
        List<CompoundTag> result = new ArrayList<>();
        Tag raw = tag.get(key);
        if (raw instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) result.add(list.getCompound(i));
        } else if (raw instanceof CompoundTag legacy) {
            for (String entryKey : legacy.getAllKeys()) result.add(legacy.getCompound(entryKey));
        }
        return result;
    }

    static List<UUID> readUuids(CompoundTag tag, String key) {
        List<UUID> result = new ArrayList<>();
        Tag raw = tag.get(key);
        if (raw instanceof ListTag list) {
            for (Tag entry : list) result.add(NbtUtils.loadUUID(entry));
        } else if (raw instanceof CompoundTag legacy) {
            for (String entryKey : legacy.getAllKeys()) {
                if (legacy.hasUUID(entryKey)) result.add(legacy.getUUID(entryKey));
            }
        }
        return result;
    }

    static ListTag writeUuids(Iterable<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) list.add(NbtUtils.createUUID(uuid));
        return list;
    }
}
