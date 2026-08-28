package TechCraft.item;

import TechCraft.TechCraft;
import TechCraft.block.ModBlocks;
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

    private static final List<Supplier<? extends ItemLike>> GENERAL_ITEMS = List.of(
            ModItems.FORGE_BOOK,
            ModItems.CUTTER,
            ModItems.FORGE_HAMMER,
            ModItems.IRON_DRILL,
            ModItems.DIAMOND_DRILL,
            ModItems.NETHERITE_DRILL,
            ModItems.QUANTUM_DRILL,

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

            ModItems.COAL_DUST,
            ModItems.IRON_DUST,
            ModItems.STEEL_DUST,

            ModItems.TIN_PLATE,
            ModItems.COPPER_PLATE,
            ModItems.IRON_PLATE,
            ModItems.GOLD_PLATE,
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
            ModBlocks.TIN_BLOCK,
            ModBlocks.BRONZE_BLOCK,
            ModBlocks.PRISMITE_BLOCK,
            ModBlocks.QUANTUM_BLOCK,
            ModBlocks.RUBY_BLOCK,
            ModBlocks.SILVER_BLOCK,
            ModBlocks.STEEL_BLOCK,
            ModBlocks.ALLOY_SMELTER
    );

    private static void addGeneralItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        GENERAL_ITEMS.forEach(item -> output.accept(item.get()));
    }

    private static void addMaterials(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        MATERIAL_ITEMS.forEach(item -> output.accept(item.get()));
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TAB.register(bus);
    }
}