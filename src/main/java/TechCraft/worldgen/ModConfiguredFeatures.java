package TechCraft.worldgen;

import TechCraft.TechCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_Tin_ORE_KEY = registerKey("tin_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NICKEL_ORE_KEY = registerKey("nickel_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> COBALT_ORE_KEY = registerKey("cobalt_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TITANIUM_ORE_KEY = registerKey("titanium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> URANIUM_ORE_KEY = registerKey("uranium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AETHERIUM_ORE_KEY = registerKey("aetherium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SOLARITE_ORE_KEY = registerKey("solarite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORICHALCUM_ORE_KEY = registerKey("orichalcum_ore");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var blocks = context.lookup(Registries.BLOCK);
        
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        registerStoneOre(context, blocks, stoneReplaceables, OVERWORLD_Tin_ORE_KEY, "tin_ore", 10);
        registerStoneOre(context, blocks, stoneReplaceables, NICKEL_ORE_KEY, "nickel_ore", 8);

        registerOre(context, blocks, stoneReplaceables, deepslateReplaceables,
                COBALT_ORE_KEY, "cobalt_ore", "deepslate_cobalt_ore", 8);
        registerOre(context, blocks, stoneReplaceables, deepslateReplaceables,
                TITANIUM_ORE_KEY, "titanium_ore", "deepslate_titanium_ore", 6);
        registerOre(context, blocks, stoneReplaceables, deepslateReplaceables,
                URANIUM_ORE_KEY, "uranium_ore", "deepslate_uranium_ore", 4);
        registerStoneOre(context, blocks, stoneReplaceables, AETHERIUM_ORE_KEY, "aetherium_ore", 5);
        registerStoneOre(context, blocks, stoneReplaceables, SOLARITE_ORE_KEY, "solarite_ore", 5);
        registerOre(context, blocks, stoneReplaceables, deepslateReplaceables,
                ORICHALCUM_ORE_KEY, "orichalcum_ore", "deepslate_orichalcum_ore", 6);

    }

    private static void registerOre(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                    net.minecraft.core.HolderGetter<Block> blocks,
                                    RuleTest stoneReplaceables, RuleTest deepslateReplaceables,
                                    ResourceKey<ConfiguredFeature<?, ?>> key,
                                    String stoneOre, String deepslateOre, int veinSize) {
        var stone = blocks.getOrThrow(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, stoneOre))).value().defaultBlockState();
        var deepslate = blocks.getOrThrow(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, deepslateOre))).value().defaultBlockState();
        register(context, key, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stoneReplaceables, stone),
                OreConfiguration.target(deepslateReplaceables, deepslate)
        ), veinSize));
    }

    private static void registerStoneOre(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                         net.minecraft.core.HolderGetter<Block> blocks,
                                         RuleTest stoneReplaceables,
                                         ResourceKey<ConfiguredFeature<?, ?>> key,
                                         String ore, int veinSize) {
        var state = blocks.getOrThrow(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, ore))).value().defaultBlockState();
        register(context, key, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stoneReplaceables, state)
        ), veinSize));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name){
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
