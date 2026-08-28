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
    public static final DeferredBlock<Block> TIN_BLOCK;
    public static final DeferredBlock<Block> ALLOY_SMELTER;
    public static final DeferredBlock<Block> BRONZE_BLOCK;
    public static final DeferredBlock<Block> PRISMITE_BLOCK;
    public static final DeferredBlock<Block> QUANTUM_BLOCK;
    public static final DeferredBlock<Block> RUBY_BLOCK;
    public static final DeferredBlock<Block> SILVER_BLOCK;
    public static final DeferredBlock<Block> STEEL_BLOCK;

    static {
        TIN_ORE = registerBlock("tin_ore", () -> new DropExperienceBlock(
            ConstantInt.of(0),
            metalOreProperties(MapColor.STONE, 3.0F)
        ));
        TIN_BLOCK = registerBlock("tin_block", () -> new Block(metalProperties()));
        ALLOY_SMELTER = registerBlock("alloy_smelter", () -> new AlloySmelterBlock(metalProperties().strength(3.5F, 3.5F)));
        BRONZE_BLOCK = registerBlock("bronze_block", () -> new Block(metalProperties()));
        PRISMITE_BLOCK = registerBlock("prismite_block", () -> new Block(metalProperties()));
        QUANTUM_BLOCK = registerBlock("quantum_block", () -> new Block(metalProperties()));
        RUBY_BLOCK = registerBlock("ruby_block", () -> new Block(metalProperties()));
        SILVER_BLOCK = registerBlock("silver_block", () -> new Block(metalProperties()));
        STEEL_BLOCK = registerBlock("steel_block", () -> new Block(metalProperties()));
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(3.0F, 3.0F);
    }

    private static BlockBehaviour.Properties metalOreProperties(MapColor color, float strength) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(strength, strength);
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