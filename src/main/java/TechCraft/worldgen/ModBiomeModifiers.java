package TechCraft.worldgen;

import TechCraft.TechCraft;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_Tin_ORE = registerKey("add_tin_ore");
    public static final ResourceKey<BiomeModifier> ADD_NICKEL_ORE = registerKey("add_nickel_ore");
    public static final ResourceKey<BiomeModifier> ADD_COBALT_ORE = registerKey("add_cobalt_ore");
    public static final ResourceKey<BiomeModifier> ADD_TITANIUM_ORE = registerKey("add_titanium_ore");
    public static final ResourceKey<BiomeModifier> ADD_URANIUM_ORE = registerKey("add_uranium_ore");
    public static final ResourceKey<BiomeModifier> ADD_AETHERIUM_ORE = registerKey("add_aetherium_ore");
    public static final ResourceKey<BiomeModifier> ADD_SOLARITE_ORE = registerKey("add_solarite_ore");
    public static final ResourceKey<BiomeModifier> ADD_ORICHALCUM_ORE = registerKey("add_orichalcum_ore");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placeFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes =  context.lookup(Registries.BIOME);

        context.register(ADD_Tin_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placeFeatures.getOrThrow(ModPlacedFeatures.Tin_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        addOverworldOre(context, ADD_NICKEL_ORE, ModPlacedFeatures.NICKEL_ORE_PLACED_KEY);
        addOverworldOre(context, ADD_COBALT_ORE, ModPlacedFeatures.COBALT_ORE_PLACED_KEY);
        addOverworldOre(context, ADD_TITANIUM_ORE, ModPlacedFeatures.TITANIUM_ORE_PLACED_KEY);
        addOverworldOre(context, ADD_URANIUM_ORE, ModPlacedFeatures.URANIUM_ORE_PLACED_KEY);
        addOverworldOre(context, ADD_AETHERIUM_ORE, ModPlacedFeatures.AETHERIUM_ORE_PLACED_KEY);
        addOverworldOre(context, ADD_SOLARITE_ORE, ModPlacedFeatures.SOLARITE_ORE_PLACED_KEY);
        addOverworldOre(context, ADD_ORICHALCUM_ORE, ModPlacedFeatures.ORICHALCUM_ORE_PLACED_KEY);
    }

    private static void addOverworldOre(BootstrapContext<BiomeModifier> context,
                                        ResourceKey<BiomeModifier> modifierKey,
                                        ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> featureKey) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);
        context.register(modifierKey, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(featureKey)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name){
     return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));
    }
}
