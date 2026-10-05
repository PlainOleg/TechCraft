package TechCraft.lumenmesh.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Сохраняемые данные сетей Lumen Mesh.
 * Хранит состояние всех сетей между перезапусками мира.
 * <p>
 * Пока к данным привязан {@link LumenNetworkManager}, {@link #save} читает его живое состояние:
 * менеджеру не нужно копировать все сети и узлы при каждом изменении, достаточно вызвать {@link #setDirty()}.
 */
public class LumenNetworkSavedData extends SavedData {
    private static final String DATA_NAME = "lumen_mesh_networks";
    /** 1 — списки в CompoundTag с ключами node_N; 2 — ListTag. Читаются оба. */
    private static final int FORMAT_VERSION = 2;
    private static final Factory<LumenNetworkSavedData> FACTORY =
        new Factory<>(LumenNetworkSavedData::new, LumenNetworkSavedData::load);

    private List<LumenNetwork> networks;
    private List<LumenNode> nodes;
    @Nullable
    private LumenNetworkManager source;

    public LumenNetworkSavedData() {
        this.networks = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public List<LumenNetwork> getNetworks() {
        return source != null ? new ArrayList<>(source.getAllNetworks()) : networks;
    }

    public void setNetworks(List<LumenNetwork> networks) {
        detach();
        this.networks = networks;
        setDirty();
    }

    public List<LumenNode> getNodes() {
        return source != null ? new ArrayList<>(source.getGraphNodes()) : nodes;
    }

    public void setNodes(List<LumenNode> nodes) {
        detach();
        this.nodes = nodes;
        setDirty();
    }

    /** Привязывает живое состояние менеджера: дальше {@link #save} читает данные прямо из него. */
    void attach(LumenNetworkManager manager) {
        this.source = manager;
    }

    /** Отвязывает менеджер, сохранив снимок его текущего состояния (вызывается перед очисткой менеджера). */
    void detach() {
        if (source != null) {
            networks = new ArrayList<>(source.getAllNetworks());
            nodes = new ArrayList<>(source.getGraphNodes());
            source = null;
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        Collection<LumenNetwork> networksToSave = source != null ? source.getAllNetworks() : networks;
        Collection<LumenNode> nodesToSave = source != null ? source.getGraphNodes() : nodes;

        ListTag networksTag = new ListTag();
        for (LumenNetwork network : networksToSave) {
            networksTag.add(network.save(registries));
        }
        tag.put("networks", networksTag);

        ListTag nodesTag = new ListTag();
        for (LumenNode node : nodesToSave) {
            nodesTag.add(node.save(registries));
        }
        tag.put("nodes", nodesTag);

        tag.putInt("version", FORMAT_VERSION);

        return tag;
    }

    public static LumenNetworkSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        LumenNetworkSavedData data = new LumenNetworkSavedData();

        for (CompoundTag networkTag : NbtCompat.readCompounds(tag, "networks")) {
            data.networks.add(LumenNetwork.load(networkTag, registries));
        }

        for (CompoundTag nodeTag : NbtCompat.readCompounds(tag, "nodes")) {
            data.nodes.add(LumenNode.load(nodeTag, registries));
        }

        return data;
    }

    /**
     * Получает или создаёт экземпляр SavedData для сервера.
     */
    public static LumenNetworkSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, DATA_NAME);
    }
}
