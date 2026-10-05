package com.plainoleg.techcraft.lumenmesh.block.device;

import com.plainoleg.techcraft.lumenmesh.LumenMeshIntegration;
import com.plainoleg.techcraft.lumenmesh.block.LumenActiveState;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Persistent common implementation for blocks that participate in a Lumen network.
 */
public abstract class LumenDeviceBlockEntity extends BlockEntity implements LumenNetworkNode {
    private UUID nodeId = UUID.randomUUID();
    private UUID networkId;
    private boolean registered;

    protected LumenDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!registered && level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) {
                manager.registerNode(this);
                registered = true;
            }
        }
    }

    @Override
    public void setRemoved() {
        if (registered && level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) manager.unregisterNode(nodeId);
            registered = false;
        }
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

    @Override
    public UUID getNodeId() {
        return nodeId;
    }

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
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onNetworkChanged(@Nullable UUID networkId) {
        this.networkId = networkId;
        refreshActiveState();
        setChanged();
    }

    protected boolean isOperational() {
        if (networkId == null || level == null) return false;
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        var network = manager == null ? null : manager.getNetwork(networkId);
        return network != null && network.getEnergyStored() > 0;
    }

    protected final void refreshActiveState() {
        LumenActiveState.set(this, isOperational());
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
