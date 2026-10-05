package com.plainoleg.techcraft.lumenmesh.registry;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.lumenmesh.block.cable.MeshCableBlockEntity;
import com.plainoleg.techcraft.lumenmesh.block.core.MeshCoreBlockEntity;
import com.plainoleg.techcraft.lumenmesh.block.energy.EnergyBridgeBlockEntity;
import com.plainoleg.techcraft.lumenmesh.block.storage.PrismDriveBlockEntity;
import com.plainoleg.techcraft.lumenmesh.block.storage.StorageLinkBlockEntity;
import com.plainoleg.techcraft.lumenmesh.block.terminal.CraftingTerminalBlockEntity;
import com.plainoleg.techcraft.lumenmesh.block.terminal.ItemTerminalBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Регистрация Block Entities Lumen Mesh.
 */
public class LumenBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, TechCraft.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeshCoreBlockEntity>> MESH_CORE =
        BLOCK_ENTITIES.register("mesh_core", () -> 
            BlockEntityType.Builder.of(MeshCoreBlockEntity::new, LumenBlocks.MESH_CORE.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBridgeBlockEntity>> ENERGY_BRIDGE =
        BLOCK_ENTITIES.register("energy_bridge", () -> 
            BlockEntityType.Builder.of(EnergyBridgeBlockEntity::new, LumenBlocks.ENERGY_BRIDGE.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeshCableBlockEntity>> MESH_CABLE =
        BLOCK_ENTITIES.register("mesh_cable", () ->
            BlockEntityType.Builder.of(MeshCableBlockEntity::new,
                    LumenBlocks.MESH_CABLE.get(),
                    LumenBlocks.SMART_CABLE.get(),
                    LumenBlocks.DENSE_TRUNK.get(),
                    LumenBlocks.CABLE_JUNCTION.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrismDriveBlockEntity>> PRISM_DRIVE =
        BLOCK_ENTITIES.register("prism_drive", () ->
            BlockEntityType.Builder.of(PrismDriveBlockEntity::new, LumenBlocks.PRISM_DRIVE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemTerminalBlockEntity>> ITEM_TERMINAL =
        BLOCK_ENTITIES.register("item_terminal", () ->
            BlockEntityType.Builder.of(ItemTerminalBlockEntity::new, LumenBlocks.ITEM_TERMINAL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingTerminalBlockEntity>> CRAFTING_TERMINAL =
        BLOCK_ENTITIES.register("crafting_terminal", () ->
            BlockEntityType.Builder.of(CraftingTerminalBlockEntity::new, LumenBlocks.CRAFTING_TERMINAL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageLinkBlockEntity>> STORAGE_LINK =
        BLOCK_ENTITIES.register("storage_link", () -> BlockEntityType.Builder.of(StorageLinkBlockEntity::new, LumenBlocks.STORAGE_LINK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
