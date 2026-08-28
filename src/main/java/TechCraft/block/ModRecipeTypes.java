package TechCraft.block;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {
    // Temporarily disabled due to API compatibility issues
    // public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.createRecipeTypes(TechCraft.TechCraft.MOD_ID);
    // public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.createRecipeSerializers(TechCraft.TechCraft.MOD_ID);
    // public static final DeferredHolder<RecipeType<AlloyRecipe>> ALLOY_TYPE =
    //     RECIPE_TYPES.register("alloy", () -> new RecipeType<>(AlloyRecipe::new));
    // public static final DeferredHolder<RecipeSerializer<AlloyRecipe>> ALLOY_SERIALIZER =
    //     RECIPE_SERIALIZERS.register("alloy", AlloyRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        // RECIPE_TYPES.register(eventBus);
        // RECIPE_SERIALIZERS.register(eventBus);
    }
}
