package TechCraft.lumenmesh.menu;

import TechCraft.lumenmesh.block.energy.EnergyBridgeBlockEntity;
import TechCraft.lumenmesh.ModLumenMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnergyBridgeMenu extends AbstractContainerMenu {
    private final EnergyBridgeBlockEntity blockEntity;
    private long energyStored;
    private long energyCapacity;

    public EnergyBridgeMenu(int containerId, Inventory playerInventory, EnergyBridgeBlockEntity blockEntity) {
        super(ModLumenMenuTypes.ENERGY_BRIDGE.get(), containerId);
        this.blockEntity = blockEntity;
        addPlayerSlots(playerInventory);
        addDataSlots(createData());
    }

    // Client factory
    public EnergyBridgeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, (EnergyBridgeBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return TechCraft.util.MenuAccess.stillValid(player, blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No slots for now
        return ItemStack.EMPTY;
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, 85 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 143));
    }

    private ContainerData createData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) blockEntity.getEnergyBuffer();
                    case 1 -> (int) (blockEntity.getEnergyBuffer() >>> 32);
                    case 2 -> (int) blockEntity.getMaxEnergyBuffer();
                    case 3 -> (int) (blockEntity.getMaxEnergyBuffer() >>> 32);
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
                }
            }

            @Override
            public int getCount() {
                return 4;
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
}
