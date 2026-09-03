package TechCraft.lumenmesh.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер сетей Lumen Mesh.
 * Управляет созданием, объединением и разделением сетей.
 * Очередь отложенного пересчёта графа для оптимизации производительности.
 */
public class LumenNetworkManager {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LumenNetworkManager.class);
    
    private static final int REBUILD_DELAY_TICKS = 5;
    
    private final Map<UUID, LumenNetwork> networks;
    private final LumenGraph graph;
    private final Map<UUID, LumenNetworkNode> nodeImplementations;
    private final Set<UUID> pendingRebuildNodes;
    private int rebuildTimer;

    public LumenNetworkManager() {
        this.networks = new ConcurrentHashMap<>();
        this.graph = new LumenGraph();
        this.nodeImplementations = new ConcurrentHashMap<>();
        this.pendingRebuildNodes = ConcurrentHashMap.newKeySet();
        this.rebuildTimer = 0;
    }

    /**
     * Регистрирует узел в системе.
     */
    public void registerNode(LumenNetworkNode node) {
        UUID nodeId = node.getNodeId();
        nodeImplementations.put(nodeId, node);
        
        LumenNode graphNode = new LumenNode(
            nodeId,
            node.getNodePosition(),
            node.getDimension(),
            node.getConnectionSides(),
            node.getNodeType()
        );
        graph.addNode(graphNode);
        
        scheduleRebuild(nodeId);
    }

    /**
     * Удаляет узел из системы.
     */
    public void unregisterNode(UUID nodeId) {
        LumenNode graphNode = graph.getNode(nodeId);
        if (graphNode != null) {
            UUID networkId = graphNode.getNetworkId();
            if (networkId != null) {
                LumenNetwork network = networks.get(networkId);
                if (network != null) {
                    network.removeNode(nodeId);
                }
            }
        }
        
        graph.removeNode(nodeId);
        nodeImplementations.remove(nodeId);
        scheduleRebuild(nodeId);
    }

    /**
     * Добавляет узел к существующей сети.
     */
    public void addToNetwork(UUID nodeId, UUID networkId) {
        LumenNode graphNode = graph.getNode(nodeId);
        if (graphNode != null) {
            graphNode.setNetworkId(networkId);
            LumenNetwork network = networks.get(networkId);
            if (network != null) {
                network.addNode(nodeId);
            }
        }
    }

    /**
     * Создаёт новую сеть с указанным владельцем.
     */
    public UUID createNetwork(UUID ownerId) {
        UUID networkId = UUID.randomUUID();
        LumenNetwork network = new LumenNetwork(networkId);
        network.setOwnerId(ownerId);
        networks.put(networkId, network);
        LOGGER.debug("Создана новая сеть Lumen Mesh: {}", networkId);
        return networkId;
    }

    /**
     * Получает сеть по ID.
     */
    public LumenNetwork getNetwork(UUID networkId) {
        return networks.get(networkId);
    }

    /**
     * Получает сеть, к которой принадлежит узел.
     */
    public LumenNetwork getNetworkForNode(UUID nodeId) {
        LumenNode node = graph.getNode(nodeId);
        if (node != null && node.getNetworkId() != null) {
            return networks.get(node.getNetworkId());
        }
        return null;
    }

    /**
     * Получает все сети.
     */
    public Collection<LumenNetwork> getAllNetworks() {
        return Collections.unmodifiableCollection(networks.values());
    }

    /**
     * Запланировать перестройку графа для узла.
     */
    public void scheduleRebuild(UUID nodeId) {
        pendingRebuildNodes.add(nodeId);
        rebuildTimer = REBUILD_DELAY_TICKS;
    }

    /**
     * Тиковый метод для обработки отложенных перестроек.
     * Вызывается каждый тик сервера.
     */
    public void tick(Level level) {
        if (rebuildTimer > 0) {
            rebuildTimer--;
            if (rebuildTimer == 0 && !pendingRebuildNodes.isEmpty()) {
                rebuildNetworks(level);
                pendingRebuildNodes.clear();
            }
        }
    }

    /**
     * Перестраивает сети на основе текущего состояния графа.
     * Объединяет и разделяет сети при необходимости.
     */
    private void rebuildNetworks(Level level) {
        LOGGER.debug("Перестройка сетей Lumen Mesh...");
        
        // Находим все компоненты связности
        List<Set<UUID>> components = graph.findConnectedComponents(level);
        
        // Обновляем принадлежность узлов к сетям
        for (Set<UUID> component : components) {
            if (component.isEmpty()) continue;
            
            // Определяем, к какой сети принадлежит компонент
            UUID targetNetworkId = determineTargetNetwork(component);
            
            if (targetNetworkId == null) {
                // Создаём новую сеть для изолированного компонента
                UUID ownerId = determineOwner(component);
                targetNetworkId = createNetwork(ownerId);
            }
            
            // Обновляем узлы
            for (UUID nodeId : component) {
                LumenNode node = graph.getNode(nodeId);
                if (node != null) {
                    UUID oldNetworkId = node.getNetworkId();
                    if (!Objects.equals(oldNetworkId, targetNetworkId)) {
                        node.setNetworkId(targetNetworkId);
                        
                        // Обновляем сети
                        if (oldNetworkId != null) {
                            LumenNetwork oldNetwork = networks.get(oldNetworkId);
                            if (oldNetwork != null) {
                                oldNetwork.removeNode(nodeId);
                                // Удаляем пустые сети
                                if (oldNetwork.getNodeIds().isEmpty()) {
                                    networks.remove(oldNetworkId);
                                }
                            }
                        }
                        
                        LumenNetwork newNetwork = networks.get(targetNetworkId);
                        if (newNetwork != null) {
                            newNetwork.addNode(nodeId);
                        }
                        
                        // Уведомляем реализацию узла
                        LumenNetworkNode impl = nodeImplementations.get(nodeId);
                        if (impl != null) {
                            impl.onNetworkChanged(targetNetworkId);
                        }
                    }
                }
            }
        }
        
        LOGGER.debug("Перестройка завершена. Всего сетей: {}", networks.size());
    }

    /**
     * Определяет целевую сеть для компонента.
     * Если узлы уже принадлежат к одной сети, возвращает её ID.
     * Если принадлежат к разным сетям, выбирает детерминированно.
     */
    private UUID determineTargetNetwork(Set<UUID> component) {
        UUID firstNetworkId = null;
        
        for (UUID nodeId : component) {
            LumenNode node = graph.getNode(nodeId);
            if (node != null && node.getNetworkId() != null) {
                if (firstNetworkId == null) {
                    firstNetworkId = node.getNetworkId();
                } else if (!firstNetworkId.equals(node.getNetworkId())) {
                    // Обнаружено объединение сетей
                    return mergeNetworks(firstNetworkId, node.getNetworkId());
                }
            }
        }
        
        return firstNetworkId;
    }

    /**
     * Объединяет две сети.
     * Выбирает детерминированно основную сеть и переносит узлы.
     */
    private UUID mergeNetworks(UUID networkId1, UUID networkId2) {
        LumenNetwork network1 = networks.get(networkId1);
        LumenNetwork network2 = networks.get(networkId2);
        
        if (network1 == null || network2 == null) {
            return network1 != null ? networkId1 : networkId2;
        }
        
        // Детерминированный выбор основной сети (по UUID)
        UUID primaryId = networkId1.compareTo(networkId2) < 0 ? networkId1 : networkId2;
        UUID secondaryId = primaryId.equals(networkId1) ? networkId2 : networkId1;
        
        LumenNetwork primary = networks.get(primaryId);
        LumenNetwork secondary = networks.get(secondaryId);
        
        // Проверяем владельцев
        if (!Objects.equals(primary.getOwnerId(), secondary.getOwnerId())) {
            LOGGER.warn("Попытка объединения сетей разных владельцев: {} и {}", primaryId, secondaryId);
            // В реальной реализации здесь должна быть логика проверки прав
            // Пока объединяем, но логируем предупреждение
        }
        
        // Переносим узлы
        for (UUID nodeId : secondary.getNodeIds()) {
            primary.addNode(nodeId);
            LumenNode node = graph.getNode(nodeId);
            if (node != null) {
                node.setNetworkId(primaryId);
            }
        }
        
        // Переносим энергию
        long totalEnergy = primary.getEnergyStored() + secondary.getEnergyStored();
        long totalCapacity = primary.getEnergyCapacity() + secondary.getEnergyCapacity();
        primary.setEnergyCapacity(totalCapacity);
        primary.setEnergyStored(totalEnergy);
        
        // Переносим задания автокрафта
        for (LumenNetwork.CraftingJob job : secondary.getCraftingJobs()) {
            primary.addCraftingJob(job);
        }
        
        // Объединяем пропускную способность
        primary.setBaseBandwidth(primary.getBaseBandwidth() + secondary.getBaseBandwidth());
        
        // Удаляем вторичную сеть
        networks.remove(secondaryId);
        
        LOGGER.debug("Сети объединены: {} + {} -> {}", secondaryId, primaryId, primaryId);
        return primaryId;
    }

    /**
     * Определяет владельца для компонента.
     * Использует владельца первого найденного узла с сетью.
     */
    private UUID determineOwner(Set<UUID> component) {
        for (UUID nodeId : component) {
            LumenNode node = graph.getNode(nodeId);
            if (node != null && node.getNetworkId() != null) {
                LumenNetwork network = networks.get(node.getNetworkId());
                if (network != null && network.getOwnerId() != null) {
                    return network.getOwnerId();
                }
            }
        }
        return null;
    }

    /**
     * Загружает данные сетей из сохранения мира.
     */
    public void loadFromSavedData(LumenNetworkSavedData savedData) {
        networks.clear();
        graph.clear();
        
        for (LumenNetwork network : savedData.getNetworks()) {
            networks.put(network.getNetworkId(), network);
        }
        
        for (LumenNode node : savedData.getNodes()) {
            graph.addNode(node);
        }
        
        LOGGER.debug("Загружено {} сетей и {} узлов", networks.size(), graph.size());
    }

    /**
     * Сохраняет данные сетей в SavedData.
     */
    public void saveToSavedData(LumenNetworkSavedData savedData) {
        savedData.setNetworks(new ArrayList<>(networks.values()));
        savedData.setNodes(new ArrayList<>(graph.getAllNodes()));
    }

    /**
     * Очищает все данные (используется при перезагрузке).
     */
    public void clear() {
        networks.clear();
        graph.clear();
        nodeImplementations.clear();
        pendingRebuildNodes.clear();
        rebuildTimer = 0;
    }
}
