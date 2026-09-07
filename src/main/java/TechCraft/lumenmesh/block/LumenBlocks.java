package TechCraft.lumenmesh.block;

import TechCraft.TechCraft;
import TechCraft.lumenmesh.block.cable.MeshCableBlock;
import TechCraft.lumenmesh.block.core.MeshCoreBlock;
import TechCraft.lumenmesh.block.energy.EnergyBridgeBlock;
import TechCraft.lumenmesh.block.storage.PrismDriveBlock;
import TechCraft.lumenmesh.block.storage.StorageLinkBlock;
import TechCraft.lumenmesh.block.terminal.ItemTerminalBlock;
import TechCraft.lumenmesh.item.LumenBlockItem;
import TechCraft.lumenmesh.item.LumenItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Регистрация блоков Lumen Mesh.
 */
public class LumenBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechCraft.MOD_ID);

    // Сеть
    public static final DeferredBlock<Block> MESH_CABLE;
    public static final DeferredBlock<Block> SMART_CABLE;
    public static final DeferredBlock<Block> DENSE_TRUNK;
    public static final DeferredBlock<Block> CABLE_JUNCTION;

    // Ядро и энергия
    public static final DeferredBlock<Block> MESH_CORE;
    public static final DeferredBlock<Block> ENERGY_BRIDGE;
    public static final DeferredBlock<Block> PULSE_BUFFER;
    public static final DeferredBlock<Block> COHERENCE_STABILIZER;

    // Хранение и терминалы
    public static final DeferredBlock<Block> PRISM_DRIVE;
    public static final DeferredBlock<Block> ITEM_TERMINAL;
    public static final DeferredBlock<Block> CRAFTING_TERMINAL;

    // Автокрафт
    public static final DeferredBlock<Block> BLUEPRINT_ENCODER;
    public static final DeferredBlock<Block> FABRICATOR;
    public static final DeferredBlock<Block> CRAFTING_PROCESSOR;

    // Логистика
    public static final DeferredBlock<Block> IMPORT_NODE;
    public static final DeferredBlock<Block> EXPORT_NODE;
    public static final DeferredBlock<Block> STORAGE_LINK;
    public static final DeferredBlock<Block> MACHINE_INTERFACE;
    public static final DeferredBlock<Block> LEVEL_KEEPER;
    public static final DeferredBlock<Block> STOCK_MONITOR;

    // Удалённый доступ
    public static final DeferredBlock<Block> WIRELESS_RELAY;
    public static final DeferredBlock<Block> QUANTUM_BRIDGE;
    public static final DeferredBlock<Block> SECURITY_CONSOLE;

    // Утилита
    public static final DeferredBlock<Block> MATTER_CONDENSER;

    static {
        // Сеть
        MESH_CABLE = registerBlock("mesh_cable", () -> new MeshCableBlock(cableProperties()));
        SMART_CABLE = registerBlock("smart_cable", () -> new MeshCableBlock(cableProperties()));
        DENSE_TRUNK = registerBlock("dense_trunk", () -> new MeshCableBlock(cableProperties().strength(4.0F, 4.0F)));
        CABLE_JUNCTION = registerBlock("cable_junction", () -> new MeshCableBlock(cableProperties()));

        // Ядро и энергия
        MESH_CORE = registerBlock("mesh_core", () -> new MeshCoreBlock(machineProperties()));
        ENERGY_BRIDGE = registerBlock("energy_bridge", () -> new EnergyBridgeBlock(machineProperties()));
        PULSE_BUFFER = registerBlock("pulse_buffer", () -> new LumenFacingBlock(machineProperties()));
        COHERENCE_STABILIZER = registerBlock("coherence_stabilizer", () -> new LumenFacingBlock(machineProperties()));

        // Хранение и терминалы
        PRISM_DRIVE = registerBlock("prism_drive", () -> new PrismDriveBlock(machineProperties()));
        ITEM_TERMINAL = registerBlock("item_terminal", () -> new ItemTerminalBlock(machineProperties()));
        CRAFTING_TERMINAL = registerBlock("crafting_terminal", () -> new LumenFacingBlock(machineProperties()));

        // Автокрафт
        BLUEPRINT_ENCODER = registerBlock("blueprint_encoder", () -> new LumenFacingBlock(machineProperties()));
        FABRICATOR = registerBlock("fabricator", () -> new LumenFacingBlock(machineProperties()));
        CRAFTING_PROCESSOR = registerBlock("crafting_processor", () -> new LumenFacingBlock(machineProperties()));

        // Логистика
        IMPORT_NODE = registerBlock("import_node", () -> new LumenFacingBlock(machineProperties()));
        EXPORT_NODE = registerBlock("export_node", () -> new LumenFacingBlock(machineProperties()));
        STORAGE_LINK = registerBlock("storage_link", () -> new StorageLinkBlock(machineProperties()));
        MACHINE_INTERFACE = registerBlock("machine_interface", () -> new LumenFacingBlock(machineProperties()));
        LEVEL_KEEPER = registerBlock("level_keeper", () -> new LumenFacingBlock(machineProperties()));
        STOCK_MONITOR = registerBlock("stock_monitor", () -> new LumenFacingBlock(machineProperties()));

        // Удалённый доступ
        WIRELESS_RELAY = registerBlock("wireless_relay", () -> new LumenFacingBlock(machineProperties()));
        QUANTUM_BRIDGE = registerBlock("quantum_bridge", () -> new LumenFacingBlock(machineProperties()));
        SECURITY_CONSOLE = registerBlock("security_console", () -> new LumenFacingBlock(machineProperties()));

        // Утилита
        MATTER_CONDENSER = registerBlock("matter_condenser", () -> new LumenFacingBlock(machineProperties()));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.5F, 3.5F)
            .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties cableProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .strength(0.5F, 0.5F);
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registeredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, registeredBlock);
        return registeredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        LumenItems.ITEMS.register(name, () -> new LumenBlockItem(name, block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
