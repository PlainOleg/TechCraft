package TechCraft.lumenmesh.network;

import TechCraft.util.NonNegativeMath;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.*;

/**
 * Менеджер сетей Lumen Mesh.
 * Управляет созданием, объединением и разделением сетей.
 * <p>
 * Перестройка инкрементальная: изменение узла помечает его и соседей, и через
 * {@link #REBUILD_DELAY_TICKS} тиков пересчитываются только компоненты, в которые они входят
 * (плюс все узлы затронутых сетей). Остальные сети мира не обходятся.
 */
public class LumenNetworkManager {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LumenNetworkManager.class);

    private static final int REBUILD_DELAY_TICKS = 5;
    private static final int SAVE_CHECK_INTERVAL_TICKS = 20;

    private final Map<UUID, LumenNetwork> networks;
    private final LumenGraph graph;
    private final Map<UUID, LumenNetworkNode> nodeImplementations;
    /** Узлы, от которых нужно заново найти компоненты связности. */
    private final Set<UUID> pendingRebuildNodes;
    private boolean fullRebuildPending;
    private int rebuildTimer;
    private long lastTickTime = Long.MIN_VALUE;
    private LumenNetworkSavedData savedData;
    private int saveTimer;
    /** Изменилась топология (узлы, принадлежность сетям) с последней пометки SavedData. */
    private boolean topologyDirty;

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
        if (persisted != null && !isSamePlace(persisted, node.getDimension(), node.getNodePosition())) {
            // Узел с тем же ID переехал: у старых соседей связь пропала.
            scheduleAdjacent(persisted.getDimension(), persisted.getPosition());
        }
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

    private static boolean isSamePlace(LumenNode node, ResourceKey<Level> dimension, BlockPos position) {
        return node.getDimension().equals(dimension) && node.getPosition().equals(position);
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
            LumenNetwork network = getNetwork(graphNode.getNetworkId());
            if (network != null) {
                network.removeNode(nodeId);
            }
            // Соседей запоминаем до удаления: после него их уже не найти по позиции узла.
            scheduleAdjacent(graphNode.getDimension(), graphNode.getPosition());
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
            topologyDirty = true;
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
        topologyDirty = true;
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
        return node != null ? getNetwork(node.getNetworkId()) : null;
    }

    /**
     * Получает все сети.
     */
    public Collection<LumenNetwork> getAllNetworks() {
        return Collections.unmodifiableCollection(networks.values());
    }

    /** Узлы графа (включая выгруженные чанки) — для сохранения. */
    Collection<LumenNode> getGraphNodes() {
        return graph.getAllNodes();
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
     * Вместе с узлом помечаются его соседи: связь с ними могла как появиться, так и пропасть.
     */
    public void scheduleRebuild(UUID nodeId) {
        pendingRebuildNodes.add(nodeId);
        LumenNode node = graph.getNode(nodeId);
        if (node != null) scheduleAdjacent(node.getDimension(), node.getPosition());
        startRebuildTimer();
    }

    private void scheduleAdjacent(ResourceKey<Level> dimension, BlockPos position) {
        pendingRebuildNodes.addAll(graph.getAdjacentNodeIds(dimension, position));
        startRebuildTimer();
    }

    private void scheduleFullRebuild() {
        fullRebuildPending = true;
        startRebuildTimer();
    }

    private void startRebuildTimer() {
        topologyDirty = true;
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

        if (savedData != null && ++saveTimer >= SAVE_CHECK_INTERVAL_TICKS) {
            saveTimer = 0;
            markSavedDataDirtyIfChanged();
        }

        if (rebuildTimer > 0) {
            rebuildTimer--;
            if (rebuildTimer == 0 && (fullRebuildPending || !pendingRebuildNodes.isEmpty())) {
                rebuildNetworks();
            }
        }
    }

    /**
     * SavedData сериализует живое состояние при сохранении мира, поэтому здесь не копируем
     * данные, а только помечаем их изменёнными — и только если что-то действительно изменилось.
     */
    private void markSavedDataDirtyIfChanged() {
        boolean changed = topologyDirty;
        topologyDirty = false;
        for (LumenNetwork network : networks.values()) {
            changed |= network.consumeDirty();
        }
        if (changed) savedData.setDirty();
    }

    /**
     * Перестраивает сети, затронутые изменениями с прошлой перестройки.
     * Объединяет и разделяет сети при необходимости.
     */
    private void rebuildNetworks() {
        Collection<UUID> seeds = fullRebuildPending
                ? graph.getAllNodes().stream().map(LumenNode::getNodeId).toList()
                : List.copyOf(pendingRebuildNodes);
        pendingRebuildNodes.clear();
        fullRebuildPending = false;

        List<Component> components = collectAffectedComponents(seeds);
        // Детерминированный порядок, чтобы при разделении сеть всегда оставалась за одной и той же частью.
        components.sort(Comparator.comparing(Component::minNodeId));

        // Assign each old network to exactly one component before changing any
        // membership. Splitting and merging in the same rebuild must not move
        // nodes from an already processed component or discard a third buffer.
        Map<UUID, Integer> retainedBy = new HashMap<>();
        List<UUID> owners = new ArrayList<>(components.size());
        for (int index = 0; index < components.size(); index++) {
            owners.add(determineOwner(components.get(index).nodeIds()));
            for (UUID nodeId : components.get(index).nodeIds()) {
                UUID networkId = graph.getNode(nodeId).getNetworkId();
                if (networkId != null && networks.containsKey(networkId)) {
                    retainedBy.putIfAbsent(networkId, index);
                }
            }
        }
        Map<Integer, SortedSet<UUID>> candidates = new HashMap<>();
        retainedBy.forEach((networkId, index) ->
                candidates.computeIfAbsent(index, ignored -> new TreeSet<>()).add(networkId));
        for (UUID networkId : retainedBy.keySet()) {
            networks.get(networkId).clearNodes();
        }

        for (int index = 0; index < components.size(); index++) {
            SortedSet<UUID> existing = candidates.getOrDefault(index, Collections.emptySortedSet());
            UUID target = existing.isEmpty() ? createNetwork(owners.get(index)) : existing.first();
            for (UUID other : existing) {
                if (!other.equals(target)) target = mergeNetworks(target, other);
            }
            LumenNetwork network = networks.get(target);
            for (UUID nodeId : components.get(index).nodeIds()) {
                graph.getNode(nodeId).setNetworkId(target);
                network.addNode(nodeId);
            }
        }

        networks.values().removeIf(network -> network.getNodeIds().isEmpty());
        topologyDirty = true;

        // Notify after the complete topology is consistent. Уведомления собираем заранее:
        // обработчик узла может снова обратиться к менеджеру.
        List<Map.Entry<LumenNetworkNode, UUID>> notifications = new ArrayList<>();
        for (Component component : components) {
            for (UUID nodeId : component.nodeIds()) {
                LumenNetworkNode impl = nodeImplementations.get(nodeId);
                UUID networkId = graph.getNode(nodeId).getNetworkId();
                if (impl != null && !Objects.equals(impl.getNetworkId(), networkId)) {
                    notifications.add(new AbstractMap.SimpleImmutableEntry<>(impl, networkId));
                }
            }
        }
        notifications.forEach(entry -> entry.getKey().onNetworkChanged(entry.getValue()));
        LOGGER.debug("Перестройка завершена: компонентов пересчитано {}, всего сетей {}", components.size(), networks.size());
    }

    /**
     * Находит компоненты связности, содержащие узлы-затравки. Если в компонент попал узел
     * какой-то сети, пересматриваются и все остальные узлы этой сети — иначе при разделении
     * одна сеть могла бы остаться сразу в двух несвязанных частях.
     */
    private List<Component> collectAffectedComponents(Collection<UUID> seeds) {
        List<Component> components = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Set<UUID> touchedNetworks = new HashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>(seeds);
        UUID seed;
        while ((seed = queue.poll()) != null) {
            if (visited.contains(seed)) continue;
            LumenNode start = graph.getNode(seed);
            if (start == null) continue;

            Set<UUID> nodeIds = graph.collectComponent(start, visited);
            UUID minNodeId = null;
            for (UUID nodeId : nodeIds) {
                if (minNodeId == null || nodeId.compareTo(minNodeId) < 0) minNodeId = nodeId;
                UUID networkId = graph.getNode(nodeId).getNetworkId();
                LumenNetwork network = getNetwork(networkId);
                if (network != null && touchedNetworks.add(networkId)) {
                    queue.addAll(network.getNodeIds());
                }
            }
            components.add(new Component(minNodeId, nodeIds));
        }
        return components;
    }

    private record Component(UUID minNodeId, Set<UUID> nodeIds) {
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

        // Ёмкость и пропускная способность — свойства сети, а не сумма истории слияний.
        // Раньше они складывались, и каждый разрыв и восстановление кабеля добавляли
        // базовую ёмкость новой сети. Энергия сохраняется в пределах ёмкости.
        primary.setEnergyCapacity(Math.max(primary.getEnergyCapacity(), secondary.getEnergyCapacity()));
        primary.setEnergyStored(NonNegativeMath.add(primary.getEnergyStored(), secondary.getEnergyStored()));
        primary.setBaseBandwidth(Math.max(primary.getBaseBandwidth(), secondary.getBaseBandwidth()));

        // Переносим задания автокрафта
        for (LumenNetwork.CraftingJob job : secondary.getCraftingJobs()) {
            primary.addCraftingJob(job);
        }

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
            if (node != null) {
                LumenNetwork network = getNetwork(node.getNetworkId());
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
        // Если данные уже привязаны к этому менеджеру, сначала снимаем снимок — ниже менеджер очищается.
        savedData.detach();
        this.savedData = savedData;
        networks.clear();
        graph.clear();
        pendingRebuildNodes.clear();
        fullRebuildPending = false;
        rebuildTimer = 0;
        lastTickTime = Long.MIN_VALUE;
        saveTimer = 0;

        for (LumenNetwork network : savedData.getNetworks()) {
            networks.put(network.getNetworkId(), network);
        }

        for (LumenNode node : savedData.getNodes()) {
            graph.addNode(node);
        }
        savedData.attach(this);

        // Block entities may have loaded before ServerStartingEvent.
        for (LumenNetworkNode node : List.copyOf(nodeImplementations.values())) registerNode(node);
        scheduleFullRebuild();
        LOGGER.debug("Загружено {} сетей и {} узлов", networks.size(), graph.size());
    }

    /**
     * Сохраняет данные сетей в SavedData. Свои данные мира менеджер не копирует — они читают
     * его живое состояние при записи; в любой другой SavedData кладётся снимок, как и раньше.
     */
    public void saveToSavedData(LumenNetworkSavedData savedData) {
        if (savedData == this.savedData) {
            savedData.attach(this);
            savedData.setDirty();
        } else {
            savedData.setNetworks(new ArrayList<>(networks.values()));
            savedData.setNodes(new ArrayList<>(graph.getAllNodes()));
        }
    }

    /**
     * Очищает все данные (используется при перезагрузке).
     */
    public void clear() {
        if (savedData != null) savedData.detach();
        networks.clear();
        graph.clear();
        nodeImplementations.clear();
        pendingRebuildNodes.clear();
        fullRebuildPending = false;
        rebuildTimer = 0;
        lastTickTime = Long.MIN_VALUE;
        savedData = null;
        saveTimer = 0;
        topologyDirty = false;
    }
}
