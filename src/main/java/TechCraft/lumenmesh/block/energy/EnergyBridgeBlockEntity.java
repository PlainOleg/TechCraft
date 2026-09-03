package TechCraft.lumenmesh.block.energy;

import TechCraft.lumenmesh.block.core.LumenMeshIntegration;
import TechCraft.lumenmesh.blockentity.LumenBlockEntities;
import TechCraft.lumenmesh.energy.LumenEnergyService;
import TechCraft.lumenmesh.network.LumenNetworkManager;
import TechCraft.lumenmesh.network.LumenNetworkNode;
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

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Block Entity энергомоста Lumen Mesh.
 * Принимает энергию и передаёт её в сеть.
 */
public class EnergyBridgeBlockEntity extends BlockEntity implements LumenNetworkNode, MenuProvider {
    private final UUID nodeId;
    private UUID networkId;
    private boolean enabled;
    private long energyBuffer;
    private long maxEnergyBuffer;

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
        setChanged();
    }

    public void onPlaced() {
        if (level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) {
                manager.registerNode(this);
            }
        }
    }

    public void onRemoved() {
        if (level != null && !level.isClientSide) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null) {
                manager.unregisterNode(nodeId);
            }
        }
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

    /**
     * Добавляет энергию в буфер энергомоста.
     */
    public long receiveEnergy(long amount) {
        long space = maxEnergyBuffer - energyBuffer;
        long received = Math.min(amount, space);
        energyBuffer += received;
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
        // TODO: Создать меню для энергомоста
        return null;
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
        if (tag.hasUUID("network_id")) {
            networkId = tag.getUUID("network_id");
        }
        enabled = tag.getBoolean("enabled");
        energyBuffer = tag.getLong("energy_buffer");
        maxEnergyBuffer = tag.getLong("max_energy_buffer");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyBridgeBlockEntity entity) {
        if (entity.level != null && !entity.level.isClientSide && entity.energyBuffer > 0) {
            LumenNetworkManager manager = LumenMeshIntegration.getNetworkManager(level);
            if (manager != null && entity.networkId != null) {
                var network = manager.getNetwork(entity.networkId);
                if (network != null) {
                    // Передаём энергию в сеть
                    long transferAmount = Math.min(entity.energyBuffer, 100L); // TODO: использовать конфиг
                    long transferred = LumenEnergyService.addEnergy(network, transferAmount);
                    entity.energyBuffer -= transferred;
                    if (transferred > 0) {
                        entity.setChanged();
                    }
                }
            }
        }
    }
}
