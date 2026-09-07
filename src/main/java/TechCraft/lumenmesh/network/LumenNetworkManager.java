package TechCraft.lumenmesh.network;

import TechCraft.util.NonNegativeMath;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.*;

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
    private long lastTickTime = Long.MIN_VALUE;
    private LumenNetworkSavedData savedData;
    private int saveTimer;

    public LumenNetworkManager() {
        this.networks = new HashMap<>();
        this.graph = new LumenGraph();
        this.nodeImplementations = new HashMap<>();
        this.pendingRebuildNodes = new HashSet<>();
        this.rebuildTimer = 0;
    }

    /**
     * Регистрирует узел в системе.
     */
    public void registerNode(LumenNetworkNode node) {
        UUID nodeId = node.getNodeId();
        LumenNode occupant = graph.getNodeAt(node.getDimension(), node.getNodePosition());
        if (occupant != null && !occupant.getNodeId().equals(nodeId)) unregisterNode(occupant.getNodeId());
        nodeImplementations.put(nodeId, node);
        LumenNode persisted = graph.getNode(nodeId);
        LumenNode graphNode = new LumenNode(
                nodeId,
                node.getNodePosition(),
                node.getDimension(),
                node.getConnectionSides(),
                node.getNodeType()
        );
        graphNode.setNetworkId(persisted != null ? persisted.getNetworkId() : node.getNetworkId());
        graphNode.setEnabled(node.isEnabled());
        graph.addNode(graphNode);
        node.onNetworkChanged(graphNode.getNetworkId());

        scheduleRebuild(nodeId);
    }

    /**
     * Удаляет узел из системы.
     */
    public void unloadNode(UUID nodeId) {
        // Chunk unload is not block destruction. Keep the persisted topology and
        // buffers, but release the live block entity so storage cannot use it.
        nodeImplementations.remove(nodeId);
    }

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
        if (graphNode != null && networks.containsKey(networkId)) {
            LumenNetwork old = getNetwork(graphNode.getNetworkId());
            if (old != null) old.removeNode(nodeId);
            graphNode.setNetworkId(networkId);
            LumenNetwork network = networks.get(networkId);
            if (network != null) {
                network.addNode(nodeId);
            }
            LumenNetworkNode impl = nodeImplementations.get(nodeId);
            if (impl != null) impl.onNetworkChanged(networkId);
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
        return networkId == null ? null : networks.get(networkId);
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

    public <T extends LumenNetworkNode> List<T> getNodes(UUID networkId, Class<T> type) {
        LumenNetwork network = networks.get(networkId);
        if (network == null) return List.of();
        List<T> result = new ArrayList<>();
        for (UUID nodeId : network.getNodeIds()) {
            LumenNetworkNode node = nodeImplementations.get(nodeId);
            if (type.isInstance(node)) result.add(type.cast(node));
        }
        return result;
    }

    /**
     * Запланировать перестройку графа для узла.
     */
    public void scheduleRebuild(UUID nodeId) {
        pendingRebuildNodes.add(nodeId);
        if (rebuildTimer == 0) rebuildTimer = REBUILD_DELAY_TICKS;
    }

    /**
     * Тиковый метод для обработки отложенных перестроек.
     * Вызывается каждый тик сервера.
     */
    public void tick(Level level) {
        tick(level.getGameTime());
    }

    public void tick(long gameTime) {
        if (lastTickTime == gameTime) {
            return;
        }
        lastTickTime = gameTime;

        if (savedData != null && ++saveTimer >= 20) {
            saveTimer = 0;
            saveToSavedData(savedData);
        }

        if (rebuildTimer > 0) {
            rebuildTimer--;
            if (rebuildTimer == 0 && !pendingRebuildNodes.isEmpty()) {
                rebuildNetworks();
                pendingRebuildNodes.clear();
            }
        }
    }

    /**
     * Перестраивает сети на основе текущего состояния графа.
     * Объединяет и разделяет сети при необходимости.
     */
    private void rebuildNetworks() {
        List<Set<UUID>> components = graph.findConnectedComponents();
        components.sort(Comparator.comparing(component -> Collections.min(component)));

        // Assign each old network to exactly one component before changing any
        // membership. Splitting and merging in the same rebuild must not move
        // nodes from an already processed component or discard a third buffer.
        Map<UUID, Integer> retainedBy = new HashMap<>();
        List<UUID> owners = new ArrayList<>();
        for (int index = 0; index < components.size(); index++) {
            owners.add(determineOwner(components.get(index)));
            for (UUID nodeId : components.get(index)) {
                UUID networkId = graph.getNode(nodeId).getNetworkId();
                if (networkId != null && networks.containsKey(networkId)) {
                    retainedBy.putIfAbsent(networkId, index);
                }
            }
        }
        Map<Integer, SortedSet<UUID>> candidates = new HashMap<>();
        retainedBy.forEach((networkId, index) ->
                candidates.computeIfAbsent(index, ignored -> new TreeSet<>()).add(networkId));
        networks.values().forEach(LumenNetwork::clearNodes);

        for (int index = 0; index < components.size(); index++) {
            SortedSet<UUID> existing = candidates.getOrDefault(index, Collections.emptySortedSet());
            UUID target = existing.isEmpty() ? createNetwork(owners.get(index)) : existing.first();
            for (UUID other : existing) {
                if (!other.equals(target)) target = mergeNetworks(target, other);
            }
            LumenNetwork network = networks.get(target);
            for (UUID nodeId : components.get(index)) {
                graph.getNode(nodeId).setNetworkId(target);
                network.addNode(nodeId);
            }
        }

        networks.entrySet().removeIf(entry -> entry.getValue().getNodeIds().isEmpty());
        // Notify after the complete topology is consistent.
        for (LumenNode node : graph.getAllNodes()) {
            LumenNetworkNode impl = nodeImplementations.get(node.getNodeId());
            if (impl != null && !Objects.equals(impl.getNetworkId(), node.getNetworkId())) {
                impl.onNetworkChanged(node.getNetworkId());
            }
        }
        LOGGER.debug("Перестройка завершена. Всего сетей: {}", networks.size());
        if (savedData != null) saveToSavedData(savedData);
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
        long totalEnergy = NonNegativeMath.add(primary.getEnergyStored(), secondary.getEnergyStored());
        long totalCapacity = NonNegativeMath.add(primary.getEnergyCapacity(), secondary.getEnergyCapacity());
        primary.setEnergyCapacity(totalCapacity);
        primary.setEnergyStored(totalEnergy);

        // Переносим задания автокрафта
        for (LumenNetwork.CraftingJob job : secondary.getCraftingJobs()) {
            primary.addCraftingJob(job);
        }

        // Объединяем пропускную способность
        primary.setBaseBandwidth((int) Math.min(Integer.MAX_VALUE, (long) primary.getBaseBandwidth() + secondary.getBaseBandwidth()));

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
        this.savedData = savedData;
        networks.clear();
        graph.clear();
        pendingRebuildNodes.clear();
        rebuildTimer = 0;
        lastTickTime = Long.MIN_VALUE;
        saveTimer = 0;

        for (LumenNetwork network : savedData.getNetworks()) {
            networks.put(network.getNetworkId(), network);
        }

        for (LumenNode node : savedData.getNodes()) {
            graph.addNode(node);
        }

        // Block entities may have loaded before ServerStartingEvent.
        for (LumenNetworkNode node : List.copyOf(nodeImplementations.values())) registerNode(node);
        for (LumenNode node : graph.getAllNodes()) scheduleRebuild(node.getNodeId());
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
        lastTickTime = Long.MIN_VALUE;
        savedData = null;
        saveTimer = 0;
    }
}
