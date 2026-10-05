package com.plainoleg.techcraft;

import com.plainoleg.techcraft.item.tool.MiningArea;
import com.plainoleg.techcraft.lumenmesh.energy.LumenEnergyService;
import com.plainoleg.techcraft.lumenmesh.network.*;
import com.plainoleg.techcraft.solar.SolarGenerationService;
import com.plainoleg.techcraft.util.NonNegativeMath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Run with ./gradlew regressionTest. Throws on failure even without JVM assertions enabled.
 */
public final class RegressionSuite {
    private static int passed;

    public static void main(String[] args) {
        run("saturating arithmetic and energy boundaries", RegressionSuite::energyBoundaries);
        run("graph partitions, disabled nodes and dimensions", RegressionSuite::graphPartitions);
        run("position replacement and immutable coordinates", RegressionSuite::positionReplacement);
        run("three-way merge preserves all buffers and jobs", RegressionSuite::mergeThreeNetworks);
        run("simultaneous split and merge keeps membership disjoint", RegressionSuite::splitAndMerge);
        run("continuous changes cannot postpone rebuilding forever", RegressionSuite::rebuildDeadline);
        run("saved-data reload preserves registered devices", RegressionSuite::reloadRegisteredDevices);
        run("chunk unload preserves topology and energy", RegressionSuite::chunkUnload);
        run("mining geometry excludes the original block", RegressionSuite::miningGeometry);
        run("fractional solar generation accumulates across ticks", RegressionSuite::solarGeneration);
        System.out.println("Passed " + passed + " regression scenarios");
    }

    private static void run(String name, Runnable test) {
        test.run();
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void energyBoundaries() {
        check(NonNegativeMath.add(Long.MAX_VALUE, 1) == Long.MAX_VALUE, "Addition overflow");
        check(NonNegativeMath.multiply(Long.MAX_VALUE, 2) == Long.MAX_VALUE, "Multiplication overflow");
        check(NonNegativeMath.multiply(Long.MAX_VALUE, 0) == 0, "Zero multiplication");
        LumenNetwork network = new LumenNetwork(UUID.randomUUID());
        network.setEnergyCapacity(-1);
        network.setEnergyStored(10);
        check(network.getEnergyStored() == 0 && network.getEnergyCapacity() == 0, "Negative capacity");
        network.setEnergyCapacity(Long.MAX_VALUE);
        network.setEnergyStored(Long.MAX_VALUE - 2);
        check(LumenEnergyService.addEnergy(network, 10) == 2, "Capacity overflow");
        check(!LumenEnergyService.consumeEnergy(network, -1), "Negative cost creates energy");
        check(LumenEnergyService.extractEnergy(network, -1) == 0, "Negative extraction");
    }

    private static LumenNode graphNode(int id, int x, boolean enabled) {
        LumenNode node = new LumenNode(new UUID(0, id), new BlockPos(x, 64, 0), Level.OVERWORLD,
                EnumSet.allOf(Direction.class), LumenNetworkNode.NodeType.CABLE);
        node.setEnabled(enabled);
        return node;
    }

    private static void graphPartitions() {
        LumenGraph graph = new LumenGraph();
        graph.addNode(graphNode(1, 0, true));
        graph.addNode(graphNode(2, 1, false));
        graph.addNode(graphNode(3, 2, true));
        graph.addNode(new LumenNode(new UUID(0, 4), new BlockPos(0, 64, 0), Level.NETHER,
                EnumSet.allOf(Direction.class), LumenNetworkNode.NodeType.CABLE));
        List<Set<UUID>> components = graph.findConnectedComponents();
        check(components.size() == 4, "Disabled node or dimension bridges components");
        Set<UUID> seen = new HashSet<>();
        for (Set<UUID> component : components) {
            for (UUID id : component) check(seen.add(id), "Overlapping components");
        }
        check(seen.size() == graph.size(), "Missing graph nodes");
    }

    private static void positionReplacement() {
        LumenGraph graph = new LumenGraph();
        graph.addNode(graphNode(1, 0, true));
        graph.addNode(graphNode(2, 0, true));
        graph.removeNode(new UUID(0, 1));
        check(graph.size() == 1 && graph.getNodeAt(Level.OVERWORLD, new BlockPos(0, 64, 0)) != null,
                "Removing stale UUID clears replacement position");
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(10, 64, 0);
        graph.addNode(new LumenNode(new UUID(0, 3), mutable, Level.OVERWORLD,
                Set.of(Direction.EAST), LumenNetworkNode.NodeType.CABLE));
        mutable.setX(100);
        check(graph.getNodeAt(Level.OVERWORLD, new BlockPos(10, 64, 0)).getPosition().getX() == 10,
                "Mutable input corrupts position index");
    }

    private static void mergeThreeNetworks() {
        LumenNetworkManager manager = new LumenNetworkManager();
        List<TestNode> nodes = new ArrayList<>();
        Set<UUID> jobs = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            UUID id = manager.createNetwork(null);
            LumenNetwork network = manager.getNetwork(id);
            network.setEnergyStored((i + 1) * 100);
            var job = LumenNetwork.CraftingJob.create(UUID.randomUUID(), 1);
            network.addCraftingJob(job);
            jobs.add(job.getJobId());
            TestNode node = new TestNode(i + 1, i, id);
            nodes.add(node);
            manager.registerNode(node);
        }
        tick(manager, 0, 5);
        check(manager.getAllNetworks().size() == 1, "Three networks did not merge");
        LumenNetwork merged = manager.getNetwork(nodes.getFirst().network);
        check(merged.getEnergyStored() == 600, "Energy lost during three-way merge");
        // Capacity is the largest of the merged networks, not their sum: otherwise every
        // cable break and repair would add another base capacity to the network.
        check(merged.getEnergyCapacity() == 10000, "Capacity changed during merge");
        check(merged.getCraftingJobs().stream().map(LumenNetwork.CraftingJob::getJobId).collect(java.util.stream.Collectors.toSet()).equals(jobs), "Crafting jobs lost");
        for (TestNode node : nodes) check(node.network.equals(merged.getNetworkId()), "Stale device network ID");
    }

    private static void splitAndMerge() {
        LumenNetworkManager manager = new LumenNetworkManager();
        UUID old = manager.createNetwork(null);
        UUID other = manager.createNetwork(null);
        manager.getNetwork(old).setEnergyStored(400);
        manager.getNetwork(other).setEnergyStored(600);
        TestNode left = new TestNode(1, 0, old);
        TestNode right = new TestNode(2, 10, old);
        TestNode adjacent = new TestNode(3, 11, other);
        manager.registerNode(left);
        manager.registerNode(right);
        manager.registerNode(adjacent);
        tick(manager, 0, 5);
        check(!left.network.equals(right.network) && right.network.equals(adjacent.network), "Invalid split/merge assignments");
        check(manager.getAllNetworks().stream().mapToLong(LumenNetwork::getEnergyStored).sum() == 1000, "Split/merge lost or duplicated energy");
        for (TestNode node : List.of(left, right, adjacent)) {
            long memberships = manager.getAllNetworks().stream().filter(network -> network.getNodeIds().contains(node.id)).count();
            check(memberships == 1, "Node belongs to multiple networks");
        }
    }

    private static void rebuildDeadline() {
        LumenNetworkManager manager = new LumenNetworkManager();
        TestNode node = new TestNode(1, 0, null);
        manager.registerNode(node);
        for (int tick = 0; tick < 5; tick++) {
            manager.scheduleRebuild(node.id);
            manager.tick(tick);
        }
        check(node.network != null, "Rebuild starved by continuous changes");
    }

    private static void reloadRegisteredDevices() {
        LumenNetworkManager manager = new LumenNetworkManager();
        TestNode node = new TestNode(1, 0, null);
        manager.registerNode(node);
        manager.loadFromSavedData(new LumenNetworkSavedData());
        tick(manager, 0, 5);
        check(node.network != null && manager.getNodes(node.network, TestNode.class).size() == 1,
                "Reload forgot a device registered before saved data loaded");
    }

    private static void miningGeometry() {
        BlockPos origin = new BlockPos(5, 50, 5);
        for (Direction.Axis axis : Direction.Axis.values()) {
            for (int size : new int[]{3, 5, 7}) {
                var positions = MiningArea.positions(origin, axis, size, (size - 3) / 2);
                check(positions.size() == size * size - 1, "Wrong area size");
                check(!positions.contains(origin), "Original block destroyed recursively");
                check(new HashSet<>(positions).size() == positions.size(), "Duplicate blocks");
            }
        }
    }

    private static void chunkUnload() {
        LumenNetworkManager manager = new LumenNetworkManager();
        TestNode node = new TestNode(1, 0, null);
        manager.registerNode(node);
        tick(manager, 0, 5);
        UUID networkId = node.network;
        manager.getNetwork(networkId).setEnergyStored(1234);
        manager.unloadNode(node.id);
        tick(manager, 5, 10);
        check(manager.getNodes(networkId, TestNode.class).isEmpty(), "Unloaded device remains accessible");
        check(manager.getNetwork(networkId).getEnergyStored() == 1234, "Unloading destroyed energy");
        LumenNetworkSavedData data = new LumenNetworkSavedData();
        manager.saveToSavedData(data);
        LumenNetworkManager restored = new LumenNetworkManager();
        restored.loadFromSavedData(data);
        restored.registerNode(node);
        tick(restored, 0, 5);
        check(networkId.equals(node.network), "Loading changed network identity");
        check(restored.getNetwork(node.network).getEnergyStored() == 1234, "Reloading lost energy");
    }

    private static void solarGeneration() {
        long generated = 0;
        long remainder = 0;
        for (int tick = 0; tick < 100; tick++) {
            long[] result = SolarGenerationService.calculateGeneration(1, 0.25,
                    SolarGenerationService.unscaleRemainder(remainder));
            generated += result[0];
            remainder = result[1];
        }
        check(generated == 25 && remainder == 0, "Fractional generation drift");
    }

    private static void tick(LumenNetworkManager manager, int start, int count) {
        for (int tick = start; tick < start + count; tick++) manager.tick(tick);
    }

    private static final class TestNode implements LumenNetworkNode {
        private final UUID id;
        private final BlockPos position;
        private UUID network;

        private TestNode(int id, int x, UUID network) {
            this.id = new UUID(0, id);
            this.position = new BlockPos(x, 64, 0);
            this.network = network;
        }

        @Override
        public UUID getNodeId() {
            return id;
        }

        @Override
        public UUID getNetworkId() {
            return network;
        }

        @Override
        public BlockPos getNodePosition() {
            return position;
        }

        @Override
        public ResourceKey<Level> getDimension() {
            return Level.OVERWORLD;
        }

        @Override
        public Set<Direction> getConnectionSides() {
            return EnumSet.allOf(Direction.class);
        }

        @Override
        public NodeType getNodeType() {
            return NodeType.CABLE;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onNetworkChanged(UUID network) {
            this.network = network;
        }
    }
}
