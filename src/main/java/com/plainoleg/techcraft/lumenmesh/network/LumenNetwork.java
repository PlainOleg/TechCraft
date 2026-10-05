package com.plainoleg.techcraft.lumenmesh.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.*;

/**
 * Представляет отдельную сеть Lumen Mesh.
 * Хранит состояние сети: узлы, энергию, безопасность, задания автокрафта.
 */
public class LumenNetwork {
    private final UUID networkId;
    private UUID ownerId;
    private SecurityMode securityMode;
    private final Set<UUID> nodeIds;
    private long energyStored;
    private long energyCapacity;
    private int baseBandwidth;
    private int usedBandwidth;
    private double coherence;
    private long storageIndexVersion;
    private final List<CraftingJob> craftingJobs;
    private final Map<UUID, Set<Permission>> permissions;
    /** Изменилось ли состояние с последнего сохранения; читает {@link LumenNetworkManager}. */
    private boolean dirty = true;

    public LumenNetwork(UUID networkId) {
        this.networkId = networkId;
        this.securityMode = SecurityMode.PRIVATE;
        this.nodeIds = new HashSet<>();
        this.energyStored = 0;
        this.energyCapacity = 10000;
        this.baseBandwidth = 32;
        this.usedBandwidth = 0;
        this.coherence = 1.0;
        this.storageIndexVersion = 0;
        this.craftingJobs = new ArrayList<>();
        this.permissions = new HashMap<>();
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        dirty = true;
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
        dirty = true;
    }

    public Set<UUID> getNodeIds() {
        return Collections.unmodifiableSet(nodeIds);
    }

    public void addNode(UUID nodeId) {
        if (nodeIds.add(nodeId)) dirty = true;
    }

    public void removeNode(UUID nodeId) {
        if (nodeIds.remove(nodeId)) dirty = true;
    }

    void clearNodes() {
        if (!nodeIds.isEmpty()) dirty = true;
        nodeIds.clear();
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(long energyStored) {
        long clamped = Math.clamp(energyStored, 0, energyCapacity);
        if (clamped != this.energyStored) {
            this.energyStored = clamped;
            dirty = true;
        }
    }

    public long getEnergyCapacity() {
        return energyCapacity;
    }

    public void setEnergyCapacity(long energyCapacity) {
        this.energyCapacity = Math.max(0, energyCapacity);
        this.energyStored = Math.min(energyStored, this.energyCapacity);
        dirty = true;
    }

    public int getBaseBandwidth() {
        return baseBandwidth;
    }

    public void setBaseBandwidth(int baseBandwidth) {
        this.baseBandwidth = Math.max(0, baseBandwidth);
        dirty = true;
    }

    public int getUsedBandwidth() {
        return usedBandwidth;
    }

    public void setUsedBandwidth(int usedBandwidth) {
        int clamped = Math.max(0, usedBandwidth);
        if (clamped != this.usedBandwidth) {
            this.usedBandwidth = clamped;
            dirty = true;
        }
    }

    public double getCoherence() {
        return coherence;
    }

    public void setCoherence(double coherence) {
        double clamped = Math.clamp(coherence, 0.0, 1.0);
        if (clamped != this.coherence) {
            this.coherence = clamped;
            dirty = true;
        }
    }

    public long getStorageIndexVersion() {
        return storageIndexVersion;
    }

    public void incrementStorageIndexVersion() {
        storageIndexVersion++;
        dirty = true;
    }

    public List<CraftingJob> getCraftingJobs() {
        return Collections.unmodifiableList(craftingJobs);
    }

    public void addCraftingJob(CraftingJob job) {
        craftingJobs.add(job);
        job.owner = this;
        dirty = true;
    }

    public void removeCraftingJob(UUID jobId) {
        if (craftingJobs.removeIf(job -> job.getJobId().equals(jobId))) dirty = true;
    }

    public Set<Permission> getPermissions(UUID playerId) {
        return Collections.unmodifiableSet(permissions.getOrDefault(playerId, Set.of()));
    }

    public void setPermissions(UUID playerId, Set<Permission> permissions) {
        this.permissions.put(playerId, new HashSet<>(permissions));
        dirty = true;
    }

    /** Возвращает признак изменения и сбрасывает его. */
    boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }

    public boolean hasPermission(UUID playerId, Permission permission) {
        if (playerId.equals(ownerId)) {
            return true;
        }
        return getPermissions(playerId).contains(permission);
    }

    /**
     * Сериализация сети для сохранения.
     */
    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("network_id", networkId);
        if (ownerId != null) {
            tag.putUUID("owner_id", ownerId);
        }
        tag.putInt("security_mode", securityMode.ordinal());

        tag.put("nodes", NbtCompat.writeUuids(nodeIds));

        tag.putLong("energy_stored", energyStored);
        tag.putLong("energy_capacity", energyCapacity);
        tag.putInt("base_bandwidth", baseBandwidth);
        tag.putInt("used_bandwidth", usedBandwidth);
        tag.putDouble("coherence", coherence);
        tag.putLong("storage_index_version", storageIndexVersion);

        ListTag jobsTag = new ListTag();
        for (CraftingJob job : craftingJobs) {
            jobsTag.add(job.save(registries));
        }
        tag.put("crafting_jobs", jobsTag);

        // Сохранение разрешений
        ListTag permissionsTag = new ListTag();
        for (Map.Entry<UUID, Set<Permission>> entry : permissions.entrySet()) {
            CompoundTag permissionTag = new CompoundTag();
            permissionTag.putUUID("player", entry.getKey());
            permissionTag.putIntArray("perms", entry.getValue().stream().mapToInt(Permission::ordinal).toArray());
            permissionsTag.add(permissionTag);
        }
        tag.put("permissions", permissionsTag);

        return tag;
    }

    /**
     * Десериализация сети из сохранения.
     */
    public static LumenNetwork load(CompoundTag tag, HolderLookup.Provider registries) {
        UUID networkId = tag.getUUID("network_id");
        LumenNetwork network = new LumenNetwork(networkId);

        if (tag.hasUUID("owner_id")) {
            network.setOwnerId(tag.getUUID("owner_id"));
        }
        SecurityMode[] modes = SecurityMode.values();
        network.setSecurityMode(modes[Math.clamp(tag.getInt("security_mode"), 0, modes.length - 1)]);

        for (UUID nodeId : NbtCompat.readUuids(tag, "nodes")) {
            network.addNode(nodeId);
        }

        network.setEnergyCapacity(tag.getLong("energy_capacity"));
        network.setEnergyStored(tag.getLong("energy_stored"));
        network.setBaseBandwidth(tag.getInt("base_bandwidth"));
        network.setUsedBandwidth(tag.getInt("used_bandwidth"));
        network.setCoherence(tag.getDouble("coherence"));
        network.storageIndexVersion = tag.getLong("storage_index_version");

        for (CompoundTag jobTag : NbtCompat.readCompounds(tag, "crafting_jobs")) {
            network.addCraftingJob(CraftingJob.load(jobTag, registries));
        }

        if (tag.get("permissions") instanceof ListTag permissionsTag) {
            for (int index = 0; index < permissionsTag.size(); index++) {
                CompoundTag permissionTag = permissionsTag.getCompound(index);
                if (permissionTag.hasUUID("player")) {
                    network.setPermissions(permissionTag.getUUID("player"), readPermissions(permissionTag.getIntArray("perms")));
                }
            }
        } else {
            // Формат до версии 2: ключи "perms_<uuid>" внутри CompoundTag.
            CompoundTag legacyPermissions = tag.getCompound("permissions");
            for (String key : legacyPermissions.getAllKeys()) {
                if (key.startsWith("perms_")) {
                    UUID playerId = UUID.fromString(key.substring(6));
                    network.setPermissions(playerId, readPermissions(legacyPermissions.getIntArray(key)));
                }
            }
        }

        network.dirty = false;
        return network;
    }

    private static Set<Permission> readPermissions(int[] ordinals) {
        Permission[] values = Permission.values();
        Set<Permission> perms = EnumSet.noneOf(Permission.class);
        for (int ordinal : ordinals) {
            if (ordinal >= 0 && ordinal < values.length) perms.add(values[ordinal]);
        }
        return perms;
    }

    /**
     * Режимы безопасности сети.
     */
    public enum SecurityMode {
        PRIVATE,        // Только владелец
        TRUSTED,        // Владелец + доверенные игроки
        PUBLIC_READ,   // Все могут смотреть, только владелец изменять
        PUBLIC          // Полный доступ всем
    }

    /**
     * Разрешения доступа к сети.
     */
    public enum Permission {
        VIEW,           // Просмотр содержимого
        INSERT,         // Вставка предметов
        EXTRACT,        // Извлечение предметов
        CRAFT,          // Автокрафт
        CONFIGURE,      // Конфигурация устройств
        SECURITY,       // Управление правами
        LINK_REMOTE     // Подключение удалённых устройств
    }

    /**
     * Задание автокрафта.
     */
    public static class CraftingJob {
        /** Сеть, которой принадлежит задание; её помечаем изменённой при правках задания. */
        private LumenNetwork owner;
        private final UUID jobId;
        private JobState state;
        private final UUID recipeId;
        private long targetAmount;
        private long craftedAmount;
        private final long startTime;

        public static CraftingJob create(UUID recipeId, long targetAmount) {
            return new CraftingJob(recipeId, targetAmount);
        }

        private CraftingJob(UUID recipeId, long targetAmount) {
            this(UUID.randomUUID(), recipeId, targetAmount, System.currentTimeMillis());
        }

        private CraftingJob(UUID jobId, UUID recipeId, long targetAmount, long startTime) {
            this.jobId = jobId;
            this.state = JobState.PLANNING;
            this.recipeId = recipeId;
            this.targetAmount = targetAmount;
            this.craftedAmount = 0;
            this.startTime = startTime;
        }

        public UUID getJobId() {
            return jobId;
        }

        public JobState getState() {
            return state;
        }

        public void setState(JobState state) {
            this.state = state;
            markOwnerDirty();
        }

        public UUID getRecipeId() {
            return recipeId;
        }

        public long getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(long targetAmount) {
            this.targetAmount = targetAmount;
            markOwnerDirty();
        }

        public long getCraftedAmount() {
            return craftedAmount;
        }

        public void setCraftedAmount(long craftedAmount) {
            this.craftedAmount = craftedAmount;
            markOwnerDirty();
        }

        private void markOwnerDirty() {
            if (owner != null) owner.dirty = true;
        }

        public long getStartTime() {
            return startTime;
        }

        public CompoundTag save(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("job_id", jobId);
            tag.putInt("state", state.ordinal());
            tag.putUUID("recipe_id", recipeId);
            tag.putLong("target_amount", targetAmount);
            tag.putLong("crafted_amount", craftedAmount);
            tag.putLong("start_time", startTime);
            return tag;
        }

        public static CraftingJob load(CompoundTag tag, HolderLookup.Provider registries) {
            CraftingJob job = new CraftingJob(
                    tag.getUUID("job_id"),
                    tag.getUUID("recipe_id"),
                    tag.getLong("target_amount"),
                    tag.getLong("start_time")
            );
            JobState[] states = JobState.values();
            job.state = states[Math.clamp(tag.getInt("state"), 0, states.length - 1)];
            job.craftedAmount = tag.getLong("crafted_amount");
            return job;
        }

        public enum JobState {
            PLANNING,
            WAITING_FOR_ITEMS,
            QUEUED,
            RUNNING,
            BLOCKED,
            COMPLETED,
            CANCELLED,
            FAILED
        }
    }
}
