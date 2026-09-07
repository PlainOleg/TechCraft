package TechCraft.lumenmesh.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Граф сети Lumen Mesh.
 * Отвечает за топологию сети, поиск путей и определение компонентов связности.
 */
public class LumenGraph {
    private final Map<UUID, LumenNode> nodes;
    private final Map<NodePosition, UUID> positionToNodeId;
    private final Map<ResourceKey<Level>, Set<UUID>> dimensionToNodes;

    public LumenGraph() {
        this.nodes = new HashMap<>();
        this.positionToNodeId = new HashMap<>();
        this.dimensionToNodes = new HashMap<>();
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
            positionToNodeId.remove(new NodePosition(previous.getDimension(), previous.getPosition()));
            Set<UUID> oldDimension = dimensionToNodes.get(previous.getDimension());
            if (oldDimension != null) oldDimension.remove(previous.getNodeId());
        }
        positionToNodeId.put(new NodePosition(node.getDimension(), node.getPosition()), node.getNodeId());
        dimensionToNodes
                .computeIfAbsent(node.getDimension(), k -> new HashSet<>())
                .add(node.getNodeId());
    }

    /**
     * Удаляет узел из графа.
     */
    public void removeNode(UUID nodeId) {
        LumenNode node = nodes.remove(nodeId);
        if (node != null) {
            positionToNodeId.remove(new NodePosition(node.getDimension(), node.getPosition()));
            Set<UUID> dimNodes = dimensionToNodes.get(node.getDimension());
            if (dimNodes != null) {
                dimNodes.remove(nodeId);
                if (dimNodes.isEmpty()) {
                    dimensionToNodes.remove(node.getDimension());
                }
            }
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
        UUID nodeId = positionToNodeId.get(new NodePosition(dimension, position));
        return nodeId != null ? nodes.get(nodeId) : null;
    }

    /**
     * Получает все узлы в измерении.
     */
    public Set<UUID> getNodesInDimension(ResourceKey<Level> dimension) {
        return Collections.unmodifiableSet(dimensionToNodes.getOrDefault(dimension, Set.of()));
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

        List<UUID> neighbors = new ArrayList<>();
        for (Direction side : node.getConnectionSides()) {
            BlockPos neighborPos = node.getPosition().relative(side);
            LumenNode neighbor = getNodeAt(node.getDimension(), neighborPos);
            if (neighbor != null && neighbor.isEnabled()) {
                // Проверяем, может ли сосед соединиться с этой стороны
                Direction oppositeSide = side.getOpposite();
                if (neighbor.getConnectionSides().contains(oppositeSide)) {
                    neighbors.add(neighbor.getNodeId());
                }
            }
        }
        return neighbors;
    }

    /**
     * Выполняет обход графа (BFS) от начального узла.
     * Возвращает все достижимые узлы.
     */
    public Set<UUID> findConnectedComponent(UUID startNodeId) {
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new ArrayDeque<>();

        if (!nodes.containsKey(startNodeId)) {
            return visited;
        }

        queue.add(startNodeId);
        visited.add(startNodeId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            for (UUID neighbor : getNeighbors(current)) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }

    /**
     * Находит все компоненты связности в графе.
     * Используется при разделении сети.
     */
    public List<Set<UUID>> findConnectedComponents() {
        List<Set<UUID>> components = new ArrayList<>();
        Set<UUID> unvisited = new HashSet<>(nodes.keySet());

        while (!unvisited.isEmpty()) {
            UUID start = unvisited.iterator().next();
            Set<UUID> component = findConnectedComponent(start);
            components.add(component);
            unvisited.removeAll(component);
        }

        return components;
    }

    /**
     * Очищает граф.
     */
    public void clear() {
        nodes.clear();
        positionToNodeId.clear();
        dimensionToNodes.clear();
    }

    /**
     * @return количество узлов в графе
     */
    public int size() {
        return nodes.size();
    }

    private record NodePosition(ResourceKey<Level> dimension, BlockPos position) {
    }
}
