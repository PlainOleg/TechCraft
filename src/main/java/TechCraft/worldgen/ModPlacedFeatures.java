package TechCraft.worldgen;

import TechCraft.TechCraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> Tin_ORE_PLACED_KEY = registerKey("tin_ore_placed");
    public static final ResourceKey<PlacedFeature> NICKEL_ORE_PLACED_KEY = registerKey("nickel_ore_placed");
    public static final ResourceKey<PlacedFeature> COBALT_ORE_PLACED_KEY = registerKey("cobalt_ore_placed");
    public static final ResourceKey<PlacedFeature> TITANIUM_ORE_PLACED_KEY = registerKey("titanium_ore_placed");
    public static final ResourceKey<PlacedFeature> URANIUM_ORE_PLACED_KEY = registerKey("uranium_ore_placed");
    public static final ResourceKey<PlacedFeature> AETHERIUM_ORE_PLACED_KEY = registerKey("aetherium_ore_placed");
    public static final ResourceKey<PlacedFeature> SOLARITE_ORE_PLACED_KEY = registerKey("solarite_ore_placed");
    public static final ResourceKey<PlacedFeature> ORICHALCUM_ORE_PLACED_KEY = registerKey("orichalcum_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, Tin_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_Tin_ORE_KEY),
                ModOrePlacement.commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(112))));
        register(context, NICKEL_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.NICKEL_ORE_KEY),
                ModOrePlacement.commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(80))));
        register(context, COBALT_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.COBALT_ORE_KEY),
                ModOrePlacement.commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(64))));
        register(context, TITANIUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TITANIUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(5, HeightRangePlacement.triangle(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(32))));
        register(context, URANIUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.URANIUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(3, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16))));
        register(context, AETHERIUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.AETHERIUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(3, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(24))));
        register(context, SOLARITE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.SOLARITE_ORE_KEY),
                ModOrePlacement.commonOrePlacement(3, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(48))));
        register(context, ORICHALCUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.ORICHALCUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(32))));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name){
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
