package TechCraft.lumenmesh.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Граф сети Lumen Mesh.
 * Отвечает за топологию сети, поиск путей и определение компонентов связности.
 * <p>
 * Узлы индексируются по измерению и упакованной позиции ({@link BlockPos#asLong()}),
 * поэтому поиск соседа — это один lookup без создания объектов.
 */
public class LumenGraph {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Map<UUID, LumenNode> nodes;
    private final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<LumenNode>> nodesByPosition;

    public LumenGraph() {
        this.nodes = new HashMap<>();
        this.nodesByPosition = new HashMap<>();
    }

    /**
     * Добавляет узел в граф.
     */
    public void addNode(LumenNode node) {
        LumenNode occupant = getNodeAt(node.getDimension(), node.getPosition());
        if (occupant != null && !occupant.getNodeId().equals(node.getNodeId())) {
            removeNode(occupant.getNodeId());
        }
        LumenNode previous = nodes.put(node.getNodeId(), node);
        if (previous != null) {
            unindex(previous);
        }
        nodesByPosition
                .computeIfAbsent(node.getDimension(), k -> new Long2ObjectOpenHashMap<>())
                .put(node.getPosition().asLong(), node);
    }

    /**
     * Удаляет узел из графа.
     */
    public void removeNode(UUID nodeId) {
        LumenNode node = nodes.remove(nodeId);
        if (node != null) {
            unindex(node);
        }
    }

    private void unindex(LumenNode node) {
        Long2ObjectOpenHashMap<LumenNode> dimensionNodes = nodesByPosition.get(node.getDimension());
        if (dimensionNodes == null) return;
        dimensionNodes.remove(node.getPosition().asLong(), node);
        if (dimensionNodes.isEmpty()) {
            nodesByPosition.remove(node.getDimension());
        }
    }

    /**
     * Получает узел по ID.
     */
    public LumenNode getNode(UUID nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Получает узел по позиции.
     */
    public LumenNode getNodeAt(ResourceKey<Level> dimension, BlockPos position) {
        Long2ObjectOpenHashMap<LumenNode> dimensionNodes = nodesByPosition.get(dimension);
        return dimensionNodes != null ? dimensionNodes.get(position.asLong()) : null;
    }

    /**
     * Получает все узлы в измерении.
     */
    public Set<UUID> getNodesInDimension(ResourceKey<Level> dimension) {
        Long2ObjectOpenHashMap<LumenNode> dimensionNodes = nodesByPosition.get(dimension);
        if (dimensionNodes == null) return Set.of();
        Set<UUID> ids = new HashSet<>();
        for (LumenNode node : dimensionNodes.values()) ids.add(node.getNodeId());
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Получает все узлы.
     */
    public Collection<LumenNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Находит соседние узлы для заданного узла.
     * Проверяет все стороны соединения и возвращает подключённые узлы.
     */
    public List<UUID> getNeighbors(UUID nodeId) {
        LumenNode node = nodes.get(nodeId);
        if (node == null || !node.isEnabled()) {
            return List.of();
        }

        Long2ObjectOpenHashMap<LumenNode> dimensionNodes = nodesByPosition.get(node.getDimension());
        List<UUID> neighbors = new ArrayList<>();
        long origin = node.getPosition().asLong();
        for (Direction side : DIRECTIONS) {
            LumenNode neighbor = connectedNeighbor(dimensionNodes, node, origin, side);
            if (neighbor != null) neighbors.add(neighbor.getNodeId());
        }
        return neighbors;
    }

    /**
     * Узлы в шести соседних позициях — без учёта сторон подключения и включённости.
     * Нужны при изменении узла: связь с ними могла как появиться, так и пропасть.
     */
    List<UUID> getAdjacentNodeIds(ResourceKey<Level> dimension, BlockPos position) {
        Long2ObjectOpenHashMap<LumenNode> dimensionNodes = nodesByPosition.get(dimension);
        if (dimensionNodes == null) return List.of();
        List<UUID> adjacent = new ArrayList<>(DIRECTIONS.length);
        long origin = position.asLong();
        for (Direction side : DIRECTIONS) {
            LumenNode neighbor = dimensionNodes.get(BlockPos.offset(origin, side));
            if (neighbor != null) adjacent.add(neighbor.getNodeId());
        }
        return adjacent;
    }

    /**
     * Выполняет обход графа (BFS) от начального узла.
     * Возвращает все достижимые узлы.
     */
    public Set<UUID> findConnectedComponent(UUID startNodeId) {
        LumenNode start = nodes.get(startNodeId);
        return start == null ? new HashSet<>() : collectComponent(start, new HashSet<>());
    }

    /**
     * Находит все компоненты связности в графе.
     * Используется при разделении сети.
     */
    public List<Set<UUID>> findConnectedComponents() {
        List<Set<UUID>> components = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        for (LumenNode node : nodes.values()) {
            if (!visited.contains(node.getNodeId())) {
                components.add(collectComponent(node, visited));
            }
        }
        return components;
    }

    /**
     * BFS от {@code start}. Все найденные узлы добавляются и в результат, и в {@code visited}:
     * компоненты не пересекаются, поэтому общий {@code visited} позволяет обходить граф по частям.
     */
    Set<UUID> collectComponent(LumenNode start, Set<UUID> visited) {
        Set<UUID> component = new HashSet<>();
        ArrayDeque<LumenNode> queue = new ArrayDeque<>();
        visited.add(start.getNodeId());
        component.add(start.getNodeId());
        queue.add(start);

        LumenNode current;
        while ((current = queue.poll()) != null) {
            if (!current.isEnabled()) continue;
            Long2ObjectOpenHashMap<LumenNode> dimensionNodes = nodesByPosition.get(current.getDimension());
            long origin = current.getPosition().asLong();
            for (Direction side : DIRECTIONS) {
                LumenNode neighbor = connectedNeighbor(dimensionNodes, current, origin, side);
                if (neighbor != null && visited.add(neighbor.getNodeId())) {
                    component.add(neighbor.getNodeId());
                    queue.add(neighbor);
                }
            }
        }
        return component;
    }

    /** Сосед со стороны {@code side}, если оба узла включены и смотрят друг на друга. */
    private static LumenNode connectedNeighbor(Long2ObjectOpenHashMap<LumenNode> dimensionNodes,
                                               LumenNode node, long origin, Direction side) {
        if (dimensionNodes == null || !node.connectsTo(side)) return null;
        LumenNode neighbor = dimensionNodes.get(BlockPos.offset(origin, side));
        if (neighbor == null || !neighbor.isEnabled() || !neighbor.connectsTo(side.getOpposite())) return null;
        return neighbor;
    }

    /**
     * Очищает граф.
     */
    public void clear() {
        nodes.clear();
        nodesByPosition.clear();
    }

    /**
     * @return количество узлов в графе
     */
    public int size() {
        return nodes.size();
    }
}
