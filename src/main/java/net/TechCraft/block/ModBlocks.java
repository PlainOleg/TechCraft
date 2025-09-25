package net.TechCraft.block;

import net.TechCraft.TechCraft;
import net.TechCraft.item.ModItems;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TechCraft.MOD_ID);

    public static final DeferredBlock<Block> Raw_Tin_Block;
    public static final DeferredBlock<Block> Tin_Block;
    public static final DeferredBlock<Block> Tin_Ore;
    public static final DeferredBlock<Block> Deepslate_Tin_Ore;

    static {
        Raw_Tin_Block = registerBlock("raw_tin_block", () -> new Block(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
        Tin_Block = registerBlock("tin_block", () -> new Block(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.COPPER)));
        Tin_Ore = registerBlock("tin_ore", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
        Deepslate_Tin_Ore = registerBlock("deepslate_tin_ore", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(ModBlocks.Tin_Ore.get()).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
     DeferredBlock<T> toReturn = BLOCKS.register(name, block);
     registerBlockItem(name, toReturn);
     return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
