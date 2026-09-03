package TechCraft.lumenmesh.menu;

import TechCraft.lumenmesh.block.energy.EnergyBridgeBlockEntity;
import TechCraft.lumenmesh.ModLumenMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class EnergyBridgeMenu extends AbstractContainerMenu {
    private final EnergyBridgeBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public EnergyBridgeMenu(int containerId, Inventory playerInventory, EnergyBridgeBlockEntity blockEntity) {
        super(ModLumenMenuTypes.ENERGY_BRIDGE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    // Client factory
    public EnergyBridgeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, (EnergyBridgeBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No slots for now
        return ItemStack.EMPTY;
    }
}
