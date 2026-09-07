package TechCraft.lumenmesh.block.core;

import TechCraft.lumenmesh.network.LumenNetworkManager;
import TechCraft.lumenmesh.network.LumenNetworkSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Класс интеграции Lumen Mesh с основным модом.
 * Предоставляет доступ к менеджеру сетей для блоков и блок-ентити.
 */
public class LumenMeshIntegration {
    private static LumenNetworkManager networkManager;

    /**
     * Инициализирует систему Lumen Mesh.
     */
    public static void init() {
        networkManager = new LumenNetworkManager();
    }

    public static void start(MinecraftServer server) {
        if (networkManager == null) init();
        networkManager.loadFromSavedData(LumenNetworkSavedData.get(server));
    }

    public static void save(MinecraftServer server) {
        if (networkManager != null) networkManager.saveToSavedData(LumenNetworkSavedData.get(server));
    }

    /**
     * Получает менеджер сетей для уровня.
     */
    @Nullable
    public static LumenNetworkManager getNetworkManager(Level level) {
        if (level == null || level.isClientSide || networkManager == null) {
            return null;
        }

        // В будущем здесь может быть логика для разных измерений
        return networkManager;
    }

    /**
     * Получает менеджер сетей для сервера.
     */
    @Nullable
    public static LumenNetworkManager getNetworkManager(MinecraftServer server) {
        return networkManager;
    }

    /**
     * Очищает систему (используется при перезагрузке).
     */
    public static void shutdown() {
        if (networkManager != null) {
            networkManager.clear();
            networkManager = null;
        }
    }
}
