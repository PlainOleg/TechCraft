package TechCraft.lumenmesh.blockentity;

import TechCraft.TechCraft;
import TechCraft.lumenmesh.block.LumenBlocks;
import TechCraft.lumenmesh.block.core.MeshCoreBlockEntity;
import TechCraft.lumenmesh.block.energy.EnergyBridgeBlockEntity;
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

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
