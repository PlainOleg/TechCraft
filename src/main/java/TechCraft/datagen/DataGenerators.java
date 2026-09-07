package TechCraft.datagen;

import TechCraft.TechCraft;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TechCraft.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        ModDatapackProvider datapackProvider = new ModDatapackProvider(
                generator.getPackOutput(),
                event.getLookupProvider()
        );

        generator.addProvider(event.includeServer(), datapackProvider);
    }
}
