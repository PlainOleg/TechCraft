package TechCraft.item;

import TechCraft.TechCraft;
import TechCraft.block.ModBlocks;
import TechCraft.solar.ModSolarBlocks;
import TechCraft.lumenmesh.block.LumenBlocks;
import TechCraft.lumenmesh.item.LumenItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> TECHCRAFT_TAB = CREATIVE_MODE_TAB.register(
            "techcraft_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.FORGE_BOOK.get()))
                    .title(Component.translatable("creativetab.techcraft.items"))
                    .displayItems(ModCreativeModTabs::addGeneralItems)
                    .build()
    );

    public static final Supplier<CreativeModeTab> TECHCRAFT_MATERIALS_TAB = CREATIVE_MODE_TAB.register(
            "techcraft_materials",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.TIN_INGOT.get()))
                    .title(Component.translatable("creativetab.techcraft.materials"))
                    .displayItems(ModCreativeModTabs::addMaterials)
                    .build()
    );

    public static final Supplier<CreativeModeTab> LUMEN_MESH_TAB = CREATIVE_MODE_TAB.register(
            "lumen_mesh",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(LumenBlocks.MESH_CORE.get()))
                    .title(Component.translatable("creativetab.techcraft.lumen_mesh"))
                    .displayItems(ModCreativeModTabs::addLumenMeshItems)
                    .build()
    );

    private static final List<Supplier<? extends ItemLike>> GENERAL_ITEMS = List.of(
            ModItems.FORGE_BOOK,
            ModItems.CUTTER,
            ModItems.FORGE_HAMMER,
            ModItems.IRON_DRILL,
            ModItems.DIAMOND_DRILL,
            ModItems.NETHERITE_DRILL,
            ModItems.QUANTUM_DRILL,
            ModItems.PRISMITE_AXE,
            ModItems.PRISMITE_BOW,
            ModItems.PRISMITE_HOE,
            ModItems.PRISMITE_PICKAXE,
            ModItems.PRISMITE_SHOWER,
            ModItems.PRISMITE_SWORD,
            ModItems.QUANTUM_AXE,
            ModItems.QUANTUM_BOW,
            ModItems.QUANTUM_HOE,
            ModItems.QUANTUM_PICKAXE,
            ModItems.QUANTUM_SHOWER,
            ModItems.QUANTUM_SWORD,
            ModItems.QUANTUM_TRUE_SWORD,

            ModItems.PRISMITE_HELMET,
            ModItems.PRISMITE_CHESTPLATE,
            ModItems.PRISMITE_LEGGINGS,
            ModItems.PRISMITE_BOOTS,

            ModItems.QUANTUM_HELMET,
            ModItems.QUANTUM_CHESTPLATE,
            ModItems.QUANTUM_LEGGINGS,
            ModItems.QUANTUM_BOOTS
    );

    private static final List<Supplier<? extends ItemLike>> MATERIAL_ITEMS = List.of(
            ModItems.RAW_TIN,
            ModItems.TIN_INGOT,
            ModItems.STEEL_INGOT,
            ModItems.RAW_URANIUM,
            ModItems.ENRICHED_URANIUM,
            ModItems.ENRICHED_URANIUM_INGOT,
            ModItems.RAW_COBALT,
            ModItems.COBALT_INGOT,
            ModItems.RAW_TITANIUM,
            ModItems.TITANIUM_INGOT,
            ModItems.AETHERIUM_INGOT,
            ModItems.SOLARITE_INGOT,
            ModItems.RAW_ORICHALCUM,
            ModItems.ORICHALCUM_INGOT,

            ModItems.COAL_DUST,
            ModItems.IRON_DUST,
            ModItems.NICKEL_DUST,
            ModItems.STEEL_DUST,

            ModItems.TIN_PLATE,
            ModItems.COPPER_PLATE,
            ModItems.IRON_PLATE,
            ModItems.GOLD_PLATE,
            ModItems.BRONZE_PLATE,
            ModItems.NICKEL_PLATE,
            ModItems.SILVER_PLATE,
            ModItems.PRISMITE_PLATE,
            ModItems.QUANTUM_PLATE,
            ModItems.STEEL_PLATE,

            ModItems.RAW_RUBBER,
            ModItems.RUBBER,

            ModItems.BRONZE_INGOT,
            ModItems.NICKEL_INGOT,
            ModItems.SILVER_INGOT,
            ModItems.PRISMITE_INGOT,
            ModItems.QUANTUM_INGOT,

            ModItems.BRONZE_CABLE,
            ModItems.COPPER_CABLE,
            ModItems.GOLD_CABLE,
            ModItems.IRON_CABLE,
            ModItems.NICKEL_CABLE,
            ModItems.SILVER_CABLE,
            ModItems.STEEL_CABLE,
            ModItems.TIN_CABLE,
            ModItems.PRISMITE_CABLE,
            ModItems.QUANTUM_CABLE,

            ModItems.CIRCUIT,
            ModItems.RESISTOR,
            ModItems.TRANSISTOR,
            ModItems.MAGNET,
            ModItems.MOTOR,

            ModItems.BATTERY,
            ModItems.ACCUMULATOR,
            ModItems.QUANTUM_BATTERY,
            ModItems.ENERGY_CRYSTAL,

            ModItems.CUT_RUBY,
            ModItems.FLAWLESS_RUBY,
            ModItems.PERFECT_RUBY,
            ModItems.POLISHED_RUBY,
            ModItems.RUBY_SHARD,

            ModItems.BLUE_PLASMA_CORE,
            ModItems.VIOLET_PLASMA_CORE,
            ModItems.RAW_BLUE_CORE,
            ModItems.RAW_VIOLET_CORE,

            ModBlocks.TIN_ORE,
            ModBlocks.NICKEL_ORE,
            ModBlocks.COBALT_ORE,
            ModBlocks.DEEPSLATE_COBALT_ORE,
            ModBlocks.TITANIUM_ORE,
            ModBlocks.DEEPSLATE_TITANIUM_ORE,
            ModBlocks.URANIUM_ORE,
            ModBlocks.DEEPSLATE_URANIUM_ORE,
            ModBlocks.AETHERIUM_ORE,
            ModBlocks.SOLARITE_ORE,
            ModBlocks.ORICHALCUM_ORE,
            ModBlocks.DEEPSLATE_ORICHALCUM_ORE,
            ModBlocks.TIN_BLOCK,
            ModBlocks.COBALT_BLOCK,
            ModBlocks.TITANIUM_BLOCK,
            ModBlocks.NICKEL_BLOCK,
            ModBlocks.ENRICHED_URANIUM_BLOCK,
            ModBlocks.AETHERIUM_BLOCK,
            ModBlocks.SOLARITE_BLOCK,
            ModBlocks.CRYOGENIC_CASING,
            ModBlocks.ORICHALCUM_BLOCK,
            ModBlocks.BRONZE_BLOCK,

            ModBlocks.PRISMITE_BLOCK,
            ModBlocks.QUANTUM_BLOCK,
            ModBlocks.RUBY_BLOCK,
            ModBlocks.SILVER_BLOCK,
            ModBlocks.STEEL_BLOCK,
            ModBlocks.RAW_REFACTORY_BRICK,
            ModBlocks.REFRACTORY_BRICK,
            ModBlocks.ALLOY_SMELTER,

            // Solar panels
            ModSolarBlocks.COPPER_SOLAR_PANEL,
            ModSolarBlocks.SILICON_SOLAR_PANEL,
            ModSolarBlocks.REINFORCED_SOLAR_PANEL,
            ModSolarBlocks.PRISMATIC_SOLAR_PANEL,
            ModSolarBlocks.RESONANT_SOLAR_PANEL,
            ModSolarBlocks.FLUX_SOLAR_PANEL,
            ModSolarBlocks.STELLAR_SOLAR_PANEL,
            ModSolarBlocks.HELIOS_SOLAR_PANEL,
            ModSolarBlocks.SOLAR_PANEL_BANK
    );

    private static final List<Supplier<? extends ItemLike>> LUMEN_MESH_ITEMS = List.of(
            LumenBlocks.MESH_CABLE,
            LumenBlocks.SMART_CABLE,
            LumenBlocks.DENSE_TRUNK,
            LumenBlocks.CABLE_JUNCTION,
            LumenBlocks.MESH_CORE,
            LumenBlocks.ENERGY_BRIDGE,
            LumenBlocks.PULSE_BUFFER,
            LumenBlocks.COHERENCE_STABILIZER,
            LumenBlocks.PRISM_DRIVE,
            LumenBlocks.ITEM_TERMINAL,
            LumenBlocks.CRAFTING_TERMINAL,
            LumenBlocks.BLUEPRINT_ENCODER,
            LumenBlocks.FABRICATOR,
            LumenBlocks.CRAFTING_PROCESSOR,
            LumenBlocks.IMPORT_NODE,
            LumenBlocks.EXPORT_NODE,
            LumenBlocks.STORAGE_LINK,
            LumenBlocks.MACHINE_INTERFACE,
            LumenBlocks.LEVEL_KEEPER,
            LumenBlocks.STOCK_MONITOR,
            LumenBlocks.WIRELESS_RELAY,
            LumenBlocks.QUANTUM_BRIDGE,
            LumenBlocks.SECURITY_CONSOLE,
            LumenBlocks.MATTER_CONDENSER,

            LumenItems.PHASE_QUARTZ,
            LumenItems.REFINED_PHASE_QUARTZ,
            LumenItems.ETCHED_SILICON,
            LumenItems.LOGIC_WAFER,
            LumenItems.CALCULATION_WAFER,
            LumenItems.ENGINEERING_WAFER,
            LumenItems.COHERENCE_CORE,
            LumenItems.PROCESSOR_1,
            LumenItems.PROCESSOR_4,
            LumenItems.PROCESSOR_8,
            LumenItems.PROCESSOR_16,
            LumenItems.PROCESSOR_32,
            LumenItems.PROCESSOR_64,
            LumenItems.PROCESSOR_128,
            LumenItems.PROCESSOR_256,
            LumenItems.PROCESSOR_512,
            LumenItems.STORAGE_PRISM_1K,
            LumenItems.STORAGE_PRISM_2K,
            LumenItems.STORAGE_PRISM_4K,
            LumenItems.STORAGE_PRISM_8K,
            LumenItems.STORAGE_PRISM_16K,
            LumenItems.STORAGE_PRISM_32K,
            LumenItems.STORAGE_PRISM_64K,
            LumenItems.STORAGE_PRISM_128K,
            LumenItems.STORAGE_PRISM_256K,
            LumenItems.STORAGE_PRISM_512K,
            LumenItems.STORAGE_PRISM_1024K,
            LumenItems.STORAGE_PRISM_2048K,
            LumenItems.STORAGE_PRISM_4096K,
            LumenItems.STORAGE_PRISM_8192K,
            LumenItems.STORAGE_PRISM_16384K,
            LumenItems.QUANTUM_STORAGE_PRISM,
            LumenItems.BLANK_BLUEPRINT,
            LumenItems.ENCODED_BLUEPRINT,
            LumenItems.WIRELESS_TERMINAL,
            LumenItems.NETWORK_PROBE,
            LumenItems.LINK_CARD,
            LumenItems.IDENTITY_KEY,
            LumenItems.ACCELERATION_MODULE
    );

    private static void addGeneralItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        GENERAL_ITEMS.forEach(item -> output.accept(item.get()));
    }

    private static void addMaterials(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        MATERIAL_ITEMS.forEach(item -> output.accept(item.get()));
    }

    private static void addLumenMeshItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        LUMEN_MESH_ITEMS.forEach(item -> output.accept(item.get()));
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TAB.register(bus);
    }
}
