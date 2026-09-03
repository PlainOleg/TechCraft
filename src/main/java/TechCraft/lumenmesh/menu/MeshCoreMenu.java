package TechCraft.lumenmesh.menu;

import TechCraft.lumenmesh.block.core.MeshCoreBlockEntity;
import TechCraft.lumenmesh.ModLumenMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class MeshCoreMenu extends AbstractContainerMenu {
    private final MeshCoreBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public MeshCoreMenu(int containerId, Inventory playerInventory, MeshCoreBlockEntity blockEntity) {
        super(ModLumenMenuTypes.MESH_CORE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    // Client factory
    public MeshCoreMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, (MeshCoreBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No inventory slots exposed by this menu yet
        return ItemStack.EMPTY;
    }
}
