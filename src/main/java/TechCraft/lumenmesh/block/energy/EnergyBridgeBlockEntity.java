package TechCraft.lumenmesh.block.energy;

import TechCraft.lumenmesh.block.core.LumenMeshIntegration;
import TechCraft.lumenmesh.blockentity.LumenBlockEntities;
import TechCraft.lumenmesh.energy.LumenEnergyService;
import TechCraft.lumenmesh.config.LumenMeshConfig;
import TechCraft.lumenmesh.network.LumenNetworkManager;
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
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Block Entity энергомоста Lumen Mesh.
 * Принимает энергию и передаёт её в сеть.
 */
public class EnergyBridgeBlockEntity extends BlockEntity implements LumenNetworkNode, MenuProvider {
    private UUID nodeId;
    private UUID networkId;
    private boolean enabled;
    private long energyBuffer;
    private long maxEnergyBuffer;
    private boolean registered;
    private int activityTicks;
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            int accepted = (int) Math.min(Math.max(amount, 0), maxEnergyBuffer - energyBuffer);
            if (!simulate && accepted > 0) EnergyBridgeBlockEntity.this.receiveEnergy(accepted);
            return accepted;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, energyBuffer);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, maxEnergyBuffer);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public EnergyBridgeBlockEntity(BlockPos pos, BlockState state) {
        super(LumenBlockEntities.ENERGY_BRIDGE.get(), pos, state);
        this.nodeId = UUID.randomUUID();
        this.networkId = null;
        this.enabled = true;
        this.energyBuffer = 0;
        this.maxEnergyBuffer = 10000;
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
        return NodeType.ENERGY_BRIDGE;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onNetworkChanged(@Nullable UUID networkId) {
        this.networkId = networkId;
        LumenActiveState.set(this, enabled && (energyBuffer > 0 || activityTicks > 0));
        setChanged();
    }

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

    public long getEnergyBuffer() {
        return energyBuffer;
    }

    public void setEnergyBuffer(long energyBuffer) {
        this.energyBuffer = Math.clamp(energyBuffer, 0, maxEnergyBuffer);
        setChanged();
    }

    public long getMaxEnergyBuffer() {
        return maxEnergyBuffer;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    /**
     * Добавляет энергию в буфер энергомоста.
     */
    public long receiveEnergy(long amount) {
        if (amount <= 0) return 0;
        long space = maxEnergyBuffer - energyBuffer;
        long received = Math.min(amount, space);
        energyBuffer += received;
        if (received > 0) activityTicks = 10;
        setChanged();
        return received;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.techcraft.energy_bridge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TechCraft.lumenmesh.menu.EnergyBridgeMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("node_id", nodeId);
        if (networkId != null) {
            tag.putUUID("network_id", networkId);
        }
        tag.putBoolean("enabled", enabled);
        tag.putLong("energy_buffer", energyBuffer);
        tag.putLong("max_energy_buffer", maxEnergyBuffer);
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
        energyBuffer = tag.getLong("energy_buffer");
        maxEnergyBuffer = tag.contains("max_energy_buffer") ? Math.max(0, tag.getLong("max_energy_buffer")) : 10000;
        energyBuffer = Math.clamp(energyBuffer, 0, maxEnergyBuffer);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyBridgeBlockEntity entity) {
        if (entity.level != null && !entity.level.isClientSide && entity.energyBuffer > 0) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null && entity.networkId != null) {
                var network = manager.getNetwork(entity.networkId);
                if (network != null) {
                    // Передаём энергию в сеть
                    long transferAmount = Math.min(entity.energyBuffer, LumenMeshConfig.ENERGY_BRIDGE_TRANSFER_RATE.getAsInt());
                    long transferred = LumenEnergyService.addEnergy(network, transferAmount);
                    entity.energyBuffer -= transferred;
                    if (transferred > 0) {
                        entity.activityTicks = 10;
                        entity.setChanged();
                    }
                }
            }
        }
        LumenActiveState.set(entity, entity.enabled && (entity.energyBuffer > 0 || entity.activityTicks > 0));
        if (entity.activityTicks > 0) entity.activityTicks--;
    }
}
