package TechCraft.block;

import TechCraft.TechCraft;
import TechCraft.item.ModItems;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechCraft.MOD_ID);

    public static final DeferredBlock<Block> TIN_ORE;
    public static final DeferredBlock<Block> NICKEL_ORE;
    public static final DeferredBlock<Block> COBALT_ORE;
    public static final DeferredBlock<Block> DEEPSLATE_COBALT_ORE;
    public static final DeferredBlock<Block> TITANIUM_ORE;
    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE;
    public static final DeferredBlock<Block> URANIUM_ORE;
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE;
    public static final DeferredBlock<Block> AETHERIUM_ORE;
    public static final DeferredBlock<Block> SOLARITE_ORE;
    public static final DeferredBlock<Block> ORICHALCUM_ORE;
    public static final DeferredBlock<Block> DEEPSLATE_ORICHALCUM_ORE;
    public static final DeferredBlock<Block> TIN_BLOCK;
    public static final DeferredBlock<Block> COBALT_BLOCK;
    public static final DeferredBlock<Block> TITANIUM_BLOCK;
    public static final DeferredBlock<Block> NICKEL_BLOCK;
    public static final DeferredBlock<Block> ENRICHED_URANIUM_BLOCK;
    public static final DeferredBlock<Block> AETHERIUM_BLOCK;
    public static final DeferredBlock<Block> SOLARITE_BLOCK;
    public static final DeferredBlock<Block> CRYOGENIC_CASING;
    public static final DeferredBlock<Block> ORICHALCUM_BLOCK;
    public static final DeferredBlock<Block> ALLOY_SMELTER;
    public static final DeferredBlock<Block> BRONZE_BLOCK;
    public static final DeferredBlock<Block> PRISMITE_BLOCK;
    public static final DeferredBlock<Block> QUANTUM_BLOCK;
    public static final DeferredBlock<Block> RUBY_BLOCK;
    public static final DeferredBlock<Block> SILVER_BLOCK;
    public static final DeferredBlock<Block> STEEL_BLOCK;
    public static final DeferredBlock<Block> RAW_REFACTORY_BRICK;
    public static final DeferredBlock<Block> REFRACTORY_BRICK;

    static {
        TIN_ORE = registerBlock("tin_ore", () -> new DropExperienceBlock(
            ConstantInt.of(0),
            metalOreProperties(MapColor.STONE, 3.0F)
        ));
        NICKEL_ORE = registerOre("nickel_ore", MapColor.STONE, 3.0F, 0);
        COBALT_ORE = registerOre("cobalt_ore", MapColor.STONE, 3.0F, 0);
        DEEPSLATE_COBALT_ORE = registerOre("deepslate_cobalt_ore", MapColor.DEEPSLATE, 4.5F, 0);
        TITANIUM_ORE = registerOre("titanium_ore", MapColor.STONE, 3.5F, 1);
        DEEPSLATE_TITANIUM_ORE = registerOre("deepslate_titanium_ore", MapColor.DEEPSLATE, 5.0F, 1);
        URANIUM_ORE = registerOre("uranium_ore", MapColor.STONE, 3.5F, 2);
        DEEPSLATE_URANIUM_ORE = registerOre("deepslate_uranium_ore", MapColor.DEEPSLATE, 5.0F, 2);
        AETHERIUM_ORE = registerOre("aetherium_ore", MapColor.STONE, 4.0F, 3);
        SOLARITE_ORE = registerOre("solarite_ore", MapColor.STONE, 4.0F, 3);
        ORICHALCUM_ORE = registerOre("orichalcum_ore", MapColor.STONE, 4.0F, 2);
        DEEPSLATE_ORICHALCUM_ORE = registerOre("deepslate_orichalcum_ore", MapColor.DEEPSLATE, 5.5F, 2);
        TIN_BLOCK = registerBlock("tin_block", () -> new Block(metalProperties()));
        COBALT_BLOCK = registerBlock("cobalt_block", () -> new Block(metalProperties()));
        TITANIUM_BLOCK = registerBlock("titanium_block", () -> new Block(metalProperties()));
        NICKEL_BLOCK = registerBlock("nickel_block", () -> new Block(metalProperties()));
        ENRICHED_URANIUM_BLOCK = registerBlock("enriched_uranium_block", () -> new Block(metalProperties().lightLevel(state -> 8)));
        AETHERIUM_BLOCK = registerBlock("aetherium_block", () -> new Block(metalProperties().lightLevel(state -> 10)));
        SOLARITE_BLOCK = registerBlock("solarite_block", () -> new Block(metalProperties().lightLevel(state -> 10)));
        CRYOGENIC_CASING = registerBlock("cryogenic_casing", () -> new Block(metalProperties().strength(4.0F, 6.0F)));
        ORICHALCUM_BLOCK = registerBlock("orichalcum_block", () -> new Block(metalProperties().strength(5.0F, 8.0F)));
        ALLOY_SMELTER = registerBlock("alloy_smelter", () -> new AlloySmelterBlock(metalProperties().strength(3.5F, 3.5F)));
        BRONZE_BLOCK = registerBlock("bronze_block", () -> new Block(metalProperties()));
        PRISMITE_BLOCK = registerBlock("prismite_block", () -> new Block(metalProperties()));
        QUANTUM_BLOCK = registerBlock("quantum_block", () -> new Block(metalProperties()));
        RUBY_BLOCK = registerBlock("ruby_block", () -> new Block(metalProperties()));
        SILVER_BLOCK = registerBlock("silver_block", () -> new Block(metalProperties()));
        STEEL_BLOCK = registerBlock("steel_block", () -> new Block(metalProperties()));
        RAW_REFACTORY_BRICK = registerBlock("raw_refractory_brick", () -> new Block(refractoryProperties()));
        REFRACTORY_BRICK = registerBlock("refractory_brick", () -> new Block(refractoryProperties().strength(2.5F, 6.0F)));
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(3.0F, 3.0F);
    }

    private static BlockBehaviour.Properties refractoryProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(1.8F, 4.5F);
    }

    private static BlockBehaviour.Properties metalOreProperties(MapColor color, float strength) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(strength, strength);
    }

    private static DeferredBlock<Block> registerOre(String name, MapColor color, float strength, int experience) {
        return registerBlock(name, () -> new DropExperienceBlock(
            ConstantInt.of(experience), metalOreProperties(color, strength)
        ));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registeredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, registeredBlock);
        return registeredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
