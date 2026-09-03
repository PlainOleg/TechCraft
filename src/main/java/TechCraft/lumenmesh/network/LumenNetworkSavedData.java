package TechCraft.lumenmesh.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Сохраняемые данные сетей Lumen Mesh.
 * Хранит состояние всех сетей между перезапусками мира.
 */
public class LumenNetworkSavedData extends SavedData {
    private static final String DATA_NAME = "lumen_mesh_networks";
    
    private List<LumenNetwork> networks;
    private List<LumenNode> nodes;

    public LumenNetworkSavedData() {
        this.networks = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public List<LumenNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<LumenNetwork> networks) {
        this.networks = networks;
        setDirty();
    }

    public List<LumenNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<LumenNode> nodes) {
        this.nodes = nodes;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag networksTag = new CompoundTag();
        for (int i = 0; i < networks.size(); i++) {
            networksTag.put("network_" + i, networks.get(i).save(registries));
        }
        tag.put("networks", networksTag);
        
        CompoundTag nodesTag = new CompoundTag();
        for (int i = 0; i < nodes.size(); i++) {
            nodesTag.put("node_" + i, nodes.get(i).save(registries));
        }
        tag.put("nodes", nodesTag);
        
        tag.putInt("version", 1);
        
        return tag;
    }

    public static LumenNetworkSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        LumenNetworkSavedData data = new LumenNetworkSavedData();
        
        int version = tag.getInt("version");
        
        CompoundTag networksTag = tag.getCompound("networks");
        for (String key : networksTag.getAllKeys()) {
            data.networks.add(LumenNetwork.load(networksTag.getCompound(key), registries));
        }
        
        CompoundTag nodesTag = tag.getCompound("nodes");
        for (String key : nodesTag.getAllKeys()) {
            data.nodes.add(LumenNode.load(nodesTag.getCompound(key), registries));
        }
        
        return data;
    }

    /**
     * Получает или создаёт экземпляр SavedData для сервера.
     */
    public static LumenNetworkSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        // Временное упрощение - будет доработано при интеграции с SavedData
        LumenNetworkSavedData data = new LumenNetworkSavedData();
        // TODO: Реализовать правильную загрузку/сохранение через SavedData API
        return data;
    }
}
