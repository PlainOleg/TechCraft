package TechCraft.lumenmesh.block.core;

import TechCraft.lumenmesh.network.LumenNetworkManager;
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

    /**
     * Получает менеджер сетей для уровня.
     */
    @Nullable
    public static LumenNetworkManager getNetworkManager(Level level) {
        if (networkManager == null) {
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
        }
    }
}
