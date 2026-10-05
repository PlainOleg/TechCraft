package com.plainoleg.techcraft.lumenmesh.block.core;

import com.plainoleg.techcraft.lumenmesh.LumenMeshIntegration;
import com.plainoleg.techcraft.lumenmesh.block.LumenActiveState;
import com.plainoleg.techcraft.lumenmesh.menu.MeshCoreMenu;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetwork;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkNode;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;
import com.plainoleg.techcraft.lumenmesh.registry.LumenItems;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

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
    private final ItemStackHandler craftingUpgrades = new ItemStackHandler(2) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 ? stack.is(LumenItems.PROCESSOR_64.get()) : stack.is(LumenItems.PROCESSOR_256.get());
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

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

    public ItemStackHandler getCraftingUpgrades() { return craftingUpgrades; }

    public int getCraftingGridSize() {
        if (!craftingUpgrades.getStackInSlot(1).isEmpty()) return 9;
        if (!craftingUpgrades.getStackInSlot(0).isEmpty()) return 6;
        return 3;
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
        return new MeshCoreMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("node_id", nodeId);
        if (networkId != null) {
            tag.putUUID("network_id", networkId);
        }
        tag.putBoolean("enabled", enabled);
        tag.put("crafting_upgrades", craftingUpgrades.serializeNBT(registries));
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
        if (tag.contains("crafting_upgrades"))
            craftingUpgrades.deserializeNBT(registries, tag.getCompound("crafting_upgrades"));
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
