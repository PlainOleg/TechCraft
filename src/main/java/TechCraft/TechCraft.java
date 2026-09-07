package TechCraft;

import TechCraft.block.ModBlockEntities;
import TechCraft.block.ModBlocks;
import TechCraft.block.ModMenuTypes;
import TechCraft.block.ModRecipeTypes;
import TechCraft.item.ModArmorMaterials;
import TechCraft.item.ModCreativeModTabs;
import TechCraft.item.ModItems;
import TechCraft.lumenmesh.LumenMesh;
import TechCraft.lumenmesh.ModLumenMenuTypes;
import TechCraft.lumenmesh.LumenCapabilities;
import TechCraft.lumenmesh.item.LumenItems;
import TechCraft.solar.ModSolarBlockEntities;
import TechCraft.solar.ModSolarBlocks;
import TechCraft.solar.ModSolarMenuTypes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Главный класс мода TechCraft.
 * Технологический мод добавляющий новые материалы, инструменты и механизмы.
 */
@Mod(TechCraft.MOD_ID)
public class TechCraft {
    public static final String MOD_ID = "techcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Конструктор мода. Инициализирует все системы регистрации.
     * @param modEventBus шина событий мода
     * @param modContainer контейнер мода
     */
    public TechCraft(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        // Ensure Lumen Mesh items are registered before creative tabs use them
        LumenItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);
        ModArmorMaterials.register(modEventBus);

        // Register solar panel system
        ModSolarBlocks.register(modEventBus);
        ModSolarBlockEntities.register(modEventBus);
        ModSolarMenuTypes.register(modEventBus);

        // Register LumenMesh menu types
        ModLumenMenuTypes.register(modEventBus);
        modEventBus.addListener(LumenCapabilities::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Инициализация системы Lumen Mesh
        new LumenMesh(modEventBus, modContainer);
    }

    /**
     * Общий этап инициализации мода.
     * @param event событие инициализации
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        // Populate solar panel type registry after all registrations are complete
        event.enqueueWork(() -> {
            ModSolarBlocks.populateTypeRegistry();
            LOGGER.info("Solar panel type registry populated");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}
