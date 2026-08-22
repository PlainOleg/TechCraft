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
    public static final ResourceKey<BiomeModifier> ADD_RAW_Tin_BLOCK = registerKey("add_raw_tin_block");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placeFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes =  context.lookup(Registries.BIOME);

        context.register(ADD_Tin_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placeFeatures.getOrThrow(ModPlacedFeatures.Tin_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_RAW_Tin_BLOCK, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placeFeatures.getOrThrow(ModPlacedFeatures.RAW_Tin_BLOCK_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name){
     return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));
    }
}