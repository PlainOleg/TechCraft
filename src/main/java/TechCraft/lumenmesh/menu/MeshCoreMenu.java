package TechCraft.lumenmesh.menu;

import TechCraft.lumenmesh.block.core.MeshCoreBlockEntity;
import TechCraft.lumenmesh.ModLumenMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MeshCoreMenu extends AbstractContainerMenu {
    private final MeshCoreBlockEntity blockEntity;
    private long energyStored;
    private long energyCapacity;
    private int bandwidth;
    private int usedBandwidth;
    private int coherence;
    private int nodeCount;

    public MeshCoreMenu(int containerId, Inventory playerInventory, MeshCoreBlockEntity blockEntity) {
        super(ModLumenMenuTypes.MESH_CORE.get(), containerId);
        this.blockEntity = blockEntity;
        addPlayerSlots(playerInventory);
        addDataSlots(createData());
    }

    // Client factory
    public MeshCoreMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, (MeshCoreBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return TechCraft.util.MenuAccess.stillValid(player, blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No inventory slots exposed by this menu yet
        return ItemStack.EMPTY;
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inventory, 9 + row * 9 + col, 7 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 7 + col * 18, 142));
    }

    private ContainerData createData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                var network = blockEntity.getNetwork();
                if (network == null) return 0;
                return switch (index) {
                    case 0 -> (int) network.getEnergyStored();
                    case 1 -> (int) (network.getEnergyStored() >>> 32);
                    case 2 -> (int) network.getEnergyCapacity();
                    case 3 -> (int) (network.getEnergyCapacity() >>> 32);
                    case 4 -> network.getBaseBandwidth();
                    case 5 -> network.getUsedBandwidth();
                    case 6 -> (int) Math.round(network.getCoherence() * 1000.0);
                    case 7 -> network.getNodeIds().size();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> energyStored = withLow(energyStored, value);
                    case 1 -> energyStored = withHigh(energyStored, value);
                    case 2 -> energyCapacity = withLow(energyCapacity, value);
                    case 3 -> energyCapacity = withHigh(energyCapacity, value);
                    case 4 -> bandwidth = value;
                    case 5 -> usedBandwidth = value;
                    case 6 -> coherence = value;
                    case 7 -> nodeCount = value;
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    private static long withLow(long current, int value) {
        return current & 0xFFFFFFFF00000000L | Integer.toUnsignedLong(value);
    }

    private static long withHigh(long current, int value) {
        return current & 0xFFFFFFFFL | (long) value << 32;
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getEnergyCapacity() {
        return energyCapacity;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public int getUsedBandwidth() {
        return usedBandwidth;
    }

    public double getCoherence() {
        return coherence / 1000.0;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public boolean hasNetwork() {
        return nodeCount > 0;
    }
}
