package TechCraft.lumenmesh.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    public Set<UUID> getNodeIds() {
        return Collections.unmodifiableSet(nodeIds);
    }

    public void addNode(UUID nodeId) {
        nodeIds.add(nodeId);
    }

    public void removeNode(UUID nodeId) {
        nodeIds.remove(nodeId);
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(long energyStored) {
        this.energyStored = Math.clamp(energyStored, 0, energyCapacity);
    }

    public long getEnergyCapacity() {
        return energyCapacity;
    }

    public void setEnergyCapacity(long energyCapacity) {
        this.energyCapacity = energyCapacity;
        this.energyStored = Math.min(energyStored, energyCapacity);
    }

    public int getBaseBandwidth() {
        return baseBandwidth;
    }

    public void setBaseBandwidth(int baseBandwidth) {
        this.baseBandwidth = baseBandwidth;
    }

    public int getUsedBandwidth() {
        return usedBandwidth;
    }

    public void setUsedBandwidth(int usedBandwidth) {
        this.usedBandwidth = usedBandwidth;
    }

    public double getCoherence() {
        return coherence;
    }

    public void setCoherence(double coherence) {
        this.coherence = Math.clamp(coherence, 0.0, 1.0);
    }

    public long getStorageIndexVersion() {
        return storageIndexVersion;
    }

    public void incrementStorageIndexVersion() {
        storageIndexVersion++;
    }

    public List<CraftingJob> getCraftingJobs() {
        return Collections.unmodifiableList(craftingJobs);
    }

    public void addCraftingJob(CraftingJob job) {
        craftingJobs.add(job);
    }

    public void removeCraftingJob(UUID jobId) {
        craftingJobs.removeIf(job -> job.getJobId().equals(jobId));
    }

    public Set<Permission> getPermissions(UUID playerId) {
        return permissions.getOrDefault(playerId, Set.of());
    }

    public void setPermissions(UUID playerId, Set<Permission> permissions) {
        this.permissions.put(playerId, new HashSet<>(permissions));
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
        
        CompoundTag nodesTag = new CompoundTag();
        int i = 0;
        for (UUID nodeId : nodeIds) {
            nodesTag.putUUID("node_" + i, nodeId);
            i++;
        }
        tag.put("nodes", nodesTag);
        
        tag.putLong("energy_stored", energyStored);
        tag.putLong("energy_capacity", energyCapacity);
        tag.putInt("base_bandwidth", baseBandwidth);
        tag.putInt("used_bandwidth", usedBandwidth);
        tag.putDouble("coherence", coherence);
        tag.putLong("storage_index_version", storageIndexVersion);
        
        CompoundTag jobsTag = new CompoundTag();
        for (int j = 0; j < craftingJobs.size(); j++) {
            jobsTag.put("job_" + j, craftingJobs.get(j).save(registries));
        }
        tag.put("crafting_jobs", jobsTag);
        
        // Сохранение разрешений
        CompoundTag permissionsTag = new CompoundTag();
        for (Map.Entry<UUID, Set<Permission>> entry : permissions.entrySet()) {
            permissionsTag.putUUID(entry.getKey().toString(), entry.getKey());
            int[] permOrdinals = entry.getValue().stream().mapToInt(Permission::ordinal).toArray();
            permissionsTag.putIntArray("perms_" + entry.getKey(), permOrdinals);
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
        network.setSecurityMode(SecurityMode.values()[tag.getInt("security_mode")]);
        
        CompoundTag nodesTag = tag.getCompound("nodes");
        for (String key : nodesTag.getAllKeys()) {
            network.addNode(nodesTag.getUUID(key));
        }
        
        network.setEnergyStored(tag.getLong("energy_stored"));
        network.setEnergyCapacity(tag.getLong("energy_capacity"));
        network.setBaseBandwidth(tag.getInt("base_bandwidth"));
        network.setUsedBandwidth(tag.getInt("used_bandwidth"));
        network.setCoherence(tag.getDouble("coherence"));
        network.storageIndexVersion = tag.getLong("storage_index_version");
        
        CompoundTag jobsTag = tag.getCompound("crafting_jobs");
        for (String key : jobsTag.getAllKeys()) {
            network.addCraftingJob(CraftingJob.load(jobsTag.getCompound(key), registries));
        }
        
        CompoundTag permissionsTag = tag.getCompound("permissions");
        for (String key : permissionsTag.getAllKeys()) {
            if (key.startsWith("perms_")) {
                UUID playerId = UUID.fromString(key.substring(6));
                int[] permOrdinals = permissionsTag.getIntArray(key);
                Set<Permission> perms = new HashSet<>();
                for (int ordinal : permOrdinals) {
                    perms.add(Permission.values()[ordinal]);
                }
                network.setPermissions(playerId, perms);
            }
        }
        
        return network;
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
            this.jobId = UUID.randomUUID();
            this.state = JobState.PLANNING;
            this.recipeId = recipeId;
            this.targetAmount = targetAmount;
            this.craftedAmount = 0;
            this.startTime = System.currentTimeMillis();
        }

        public UUID getJobId() {
            return jobId;
        }

        public JobState getState() {
            return state;
        }

        public void setState(JobState state) {
            this.state = state;
        }

        public UUID getRecipeId() {
            return recipeId;
        }

        public long getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(long targetAmount) {
            this.targetAmount = targetAmount;
        }

        public long getCraftedAmount() {
            return craftedAmount;
        }

        public void setCraftedAmount(long craftedAmount) {
            this.craftedAmount = craftedAmount;
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
                tag.getUUID("recipe_id"),
                tag.getLong("target_amount")
            );
            // jobId загружается отдельно, так как он final
            job.state = JobState.values()[tag.getInt("state")];
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
