package TechCraft.lumenmesh.block.core;

import TechCraft.lumenmesh.blockentity.LumenBlockEntities;
import TechCraft.lumenmesh.network.LumenNetworkManager;
import TechCraft.lumenmesh.network.LumenNetwork;
import TechCraft.lumenmesh.network.LumenNetworkNode;
import TechCraft.lumenmesh.block.LumenActiveState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Block Entity ядра сети Lumen Mesh.
 * Реализует интерфейс LumenNetworkNode и координирует сеть.
 */
public class MeshCoreBlockEntity extends BlockEntity implements LumenNetworkNode, MenuProvider {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MeshCoreBlockEntity.class);

    private UUID nodeId;
    private UUID networkId;
    private boolean enabled;
    private boolean registered;

    public MeshCoreBlockEntity(BlockPos pos, BlockState state) {
        super(LumenBlockEntities.MESH_CORE.get(), pos, state);
        this.nodeId = UUID.randomUUID();
        this.networkId = null;
        this.enabled = true;
    }

    @Override
    public UUID getNodeId() {
        return nodeId;
    }

    @Override
    public UUID getNetworkId() {
        return networkId;
    }

    @Nullable
    public LumenNetwork getNetwork() {
        if (level == null || networkId == null) return null;
        LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
        return manager == null ? null : manager.getNetwork(networkId);
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
        // Ядро соединяется со всех сторон
        return Set.of(Direction.values());
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MESH_CORE;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onNetworkChanged(@Nullable UUID networkId) {
        this.networkId = networkId;
        LumenActiveState.set(this, getNetwork() != null && getNetwork().getEnergyStored() > 0);
        setChanged();
    }

    /**
     * Вызывается при установке блока.
     */
    public void onPlaced() {
        registerNode();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerNode();
    }

    private void registerNode() {
        if (!registered && level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) {
                manager.registerNode(this);
                registered = true;
            }
        }
    }

    /**
     * Вызывается при удалении блока.
     */
    public void onRemoved() {
        if (registered && level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) {
                manager.unregisterNode(nodeId);
            }
            registered = false;
        }
    }

    @Override
    public void setRemoved() {
        onRemoved();
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
    public Component getDisplayName() {
        return Component.translatable("block.techcraft.mesh_core");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TechCraft.lumenmesh.menu.MeshCoreMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("node_id", nodeId);
        if (networkId != null) {
            tag.putUUID("network_id", networkId);
        }
        tag.putBoolean("enabled", enabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("node_id")) {
            nodeId = tag.getUUID("node_id");
        }
        if (tag.hasUUID("network_id")) {
            networkId = tag.getUUID("network_id");
        }
        enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
    }

    /**
     * Статический метод для тика блока.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, MeshCoreBlockEntity entity) {
        if (entity.level != null && !entity.level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) {
                LumenActiveState.set(entity, entity.getNetwork() != null && entity.getNetwork().getEnergyStored() > 0);
            }
        }
    }
}
