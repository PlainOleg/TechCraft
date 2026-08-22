package TechCraft;

import TechCraft.block.ModBlockEntities;
import TechCraft.block.ModBlocks;
import TechCraft.block.ModMenuTypes;
import TechCraft.block.ModRecipeTypes;
import TechCraft.item.ModCreativeModTabs;
import TechCraft.item.ModItems;
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
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Общий этап инициализации мода.
     * @param event событие инициализации
     */
    private void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}