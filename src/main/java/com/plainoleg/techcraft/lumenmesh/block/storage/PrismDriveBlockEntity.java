package com.plainoleg.techcraft.lumenmesh.block.storage;

import com.plainoleg.techcraft.lumenmesh.block.device.LumenDeviceBlockEntity;
import com.plainoleg.techcraft.lumenmesh.item.StorageMediumItem;
import com.plainoleg.techcraft.lumenmesh.menu.PrismDriveMenu;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkNode;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PrismDriveBlockEntity extends LumenDeviceBlockEntity implements MenuProvider {
    public static final int MEDIA_SLOTS = 4;
    private final ItemStackHandler media = new ItemStackHandler(MEDIA_SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof StorageMediumItem; }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) { setChanged(); refreshActiveState(); }
    };

    public PrismDriveBlockEntity(BlockPos pos, BlockState state) {
        super(LumenBlockEntities.PRISM_DRIVE.get(), pos, state);
    }

    @Override public NodeType getNodeType() { return LumenNetworkNode.NodeType.PRISM_DRIVE; }
    public void serverTick() { refreshActiveState(); }
    @Override protected boolean isOperational() {
        if (!super.isOperational()) return false;
        for (int i=0;i<media.getSlots();i++) if (!media.getStackInSlot(i).isEmpty()) return true;
        return false;
    }
    public ItemStackHandler getMedia() { return media; }
    @Override public Component getDisplayName() { return Component.translatable("block.techcraft.prism_drive"); }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PrismDriveMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("media", media.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("media")) media.deserializeNBT(registries, tag.getCompound("media"));
    }
}
