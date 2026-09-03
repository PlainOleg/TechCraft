package TechCraft.lumenmesh.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.UUID;

/**
 * Базовая реализация узла сети.
 * Хранит основную информацию о подключении к сети.
 */
public class LumenNode {
    private final UUID nodeId;
    private final BlockPos position;
    private final ResourceKey<Level> dimension;
    private final Set<Direction> connectionSides;
    private final LumenNetworkNode.NodeType nodeType;
    private UUID networkId;
    private boolean enabled;

    public LumenNode(
        UUID nodeId,
        BlockPos position,
        ResourceKey<Level> dimension,
        Set<Direction> connectionSides,
        LumenNetworkNode.NodeType nodeType
    ) {
        this.nodeId = nodeId;
        this.position = position;
        this.dimension = dimension;
        this.connectionSides = connectionSides;
        this.nodeType = nodeType;
        this.networkId = null;
        this.enabled = true;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public BlockPos getPosition() {
        return position;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public Set<Direction> getConnectionSides() {
        return connectionSides;
    }

    public LumenNetworkNode.NodeType getNodeType() {
        return nodeType;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Сериализация узла для сохранения в NBT.
     */
    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("node_id", nodeId);
        tag.putInt("x", position.getX());
        tag.putInt("y", position.getY());
        tag.putInt("z", position.getZ());
        tag.putString("dimension", dimension.location().toString());
        tag.putInt("node_type", nodeType.ordinal());
        if (networkId != null) {
            tag.putUUID("network_id", networkId);
        }
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    /**
     * Десериализация узла из NBT.
     */
    public static LumenNode load(CompoundTag tag, HolderLookup.Provider registries) {
        UUID nodeId = tag.getUUID("node_id");
        BlockPos position = new BlockPos(
            tag.getInt("x"),
            tag.getInt("y"),
            tag.getInt("z")
        );
        ResourceKey<Level> dimension = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.parse(tag.getString("dimension"))
        );
        LumenNetworkNode.NodeType nodeType = LumenNetworkNode.NodeType.values()[tag.getInt("node_type")];
        
        LumenNode node = new LumenNode(nodeId, position, dimension, Set.of(), nodeType);
        if (tag.hasUUID("network_id")) {
            node.setNetworkId(tag.getUUID("network_id"));
        }
        node.setEnabled(tag.getBoolean("enabled"));
        return node;
    }
}
