package com.plainoleg.techcraft.lumenmesh.block.cable;

import com.plainoleg.techcraft.lumenmesh.LumenMeshIntegration;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkNode;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * A cable participates in the same topology graph as machines.
 */
public class MeshCableBlockEntity extends BlockEntity implements LumenNetworkNode {
    private UUID nodeId = UUID.randomUUID();
    private UUID networkId;
    private boolean registered;

    public MeshCableBlockEntity(BlockPos pos, BlockState state) {
        super(LumenBlockEntities.MESH_CABLE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerNode();
    }

    @Override
    public void setRemoved() {
        unregisterNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (registered && level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) manager.unloadNode(nodeId);
            registered = false;
        }
        super.onChunkUnloaded();
    }

    public void topologyChanged() {
        if (level == null || level.isClientSide) return;
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        if (manager != null) manager.scheduleRebuild(nodeId);
    }

    private void registerNode() {
        if (registered || level == null || level.isClientSide) return;
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        if (manager != null) {
            manager.registerNode(this);
            registered = true;
        }
    }

    private void unregisterNode() {
        if (!registered || level == null || level.isClientSide) return;
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        if (manager != null) manager.unregisterNode(nodeId);
        registered = false;
    }

    @Override
    public UUID getNodeId() {
        return nodeId;
    }

    @Override
    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public BlockPos getNodePosition() {
        return getBlockPos();
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return level == null ? Level.OVERWORLD : level.dimension();
    }

    @Override
    public Set<Direction> getConnectionSides() {
        return Set.of(Direction.values());
    }

    @Override
    public NodeType getNodeType() {
        if (getBlockState().is(LumenBlocks.SMART_CABLE)) return NodeType.SMART_CABLE;
        if (getBlockState().is(LumenBlocks.DENSE_TRUNK)) return NodeType.DENSE_TRUNK;
        if (getBlockState().is(LumenBlocks.CABLE_JUNCTION)) return NodeType.CABLE_JUNCTION;
        return NodeType.CABLE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onNetworkChanged(@Nullable UUID networkId) {
        this.networkId = networkId;
        if (level != null && !level.isClientSide && getBlockState().getValue(MeshCableBlock.ACTIVE) != (networkId != null))
            level.setBlock(worldPosition, getBlockState().setValue(MeshCableBlock.ACTIVE, networkId != null), 3);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("node_id", nodeId);
        if (networkId != null) tag.putUUID("network_id", networkId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("node_id")) nodeId = tag.getUUID("node_id");
        networkId = tag.hasUUID("network_id") ? tag.getUUID("network_id") : null;
    }
}
