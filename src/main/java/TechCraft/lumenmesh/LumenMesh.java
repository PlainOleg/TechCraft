package TechCraft.lumenmesh;

import TechCraft.TechCraft;
import TechCraft.lumenmesh.block.LumenBlocks;
import TechCraft.lumenmesh.block.core.LumenMeshIntegration;
import TechCraft.lumenmesh.blockentity.LumenBlockEntities;
import TechCraft.lumenmesh.config.LumenMeshConfig;
import TechCraft.lumenmesh.item.LumenItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * Главный класс системы Lumen Mesh.
 * Инициализирует все подсистемы цифровой сети.
 */
public class LumenMesh {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LumenMesh.class);

    public LumenMesh(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        // Регистрация конфигурации
        modContainer.registerConfig(ModConfig.Type.SERVER, LumenMeshConfig.SPEC);

        // Регистрация блоков
        LumenBlocks.register(modEventBus);
        
        // Регистрация блок-ентити
        LumenBlockEntities.register(modEventBus);
        
        LOGGER.info("Lumen Mesh инициализирован");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LumenMeshIntegration.init();
            LOGGER.info("Менеджер сетей Lumen Mesh создан");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LumenMeshIntegration.start(event.getServer());
        LOGGER.info("Сервер запускается, загрузка данных сетей Lumen Mesh");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var manager = LumenMeshIntegration.getNetworkManager(event.getServer());
        if (manager != null) manager.tick(event.getServer().overworld());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LumenMeshIntegration.save(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        LumenMeshIntegration.shutdown();
        LOGGER.info("Сервер остановлен, данные сетей Lumen Mesh сохранены");
    }
}
