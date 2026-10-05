package com.plainoleg.techcraft.lumenmesh.block.storage;

import com.plainoleg.techcraft.lumenmesh.LumenMeshIntegration;
import com.plainoleg.techcraft.lumenmesh.block.LumenFacingEntityBlock;
import com.plainoleg.techcraft.lumenmesh.block.device.LumenDeviceBlockEntity;
import com.plainoleg.techcraft.lumenmesh.menu.StorageLinkMenu;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;
import com.plainoleg.techcraft.lumenmesh.storage.NetworkStorageService;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class StorageLinkBlockEntity extends LumenDeviceBlockEntity implements MenuProvider {
    public static final int FILTERS = 5;
    private final ItemStackHandler filters = new ItemStackHandler(FILTERS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private boolean blacklist;
    private boolean autoToNetwork;
    private boolean autoToExternal;
    private int cooldown;
    private int lastMoved;

    public StorageLinkBlockEntity(BlockPos pos, BlockState state) {
        super(LumenBlockEntities.STORAGE_LINK.get(), pos, state);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.STORAGE_LINK;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.techcraft.storage_link");
    }

    public ItemStackHandler filters() {
        return filters;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public boolean isAutoToNetwork() {
        return autoToNetwork;
    }

    public boolean isAutoToExternal() {
        return autoToExternal;
    }

    public int getLastMoved() {
        return lastMoved;
    }

    public boolean hasExternalInventory() {
        return external() != null;
    }

    private IItemHandler external() {
        if (level == null) return null;
        var facing = getBlockState().getValue(LumenFacingEntityBlock.FACING);
        BlockPos target = worldPosition.relative(facing.getOpposite());
        return level.getCapability(Capabilities.ItemHandler.BLOCK, target, facing);
    }

    private boolean allowed(ItemStack stack) {
        boolean hasFilters = false;
        for (int slot = 0; slot < FILTERS; slot++) {
            ItemStack filter = filters.getStackInSlot(slot);
            if (filter.isEmpty()) continue;
            hasFilters = true;
            if (ItemStack.isSameItemSameComponents(filter, stack)) return !blacklist;
        }
        return !hasFilters || blacklist;
    }

    public int moveToNetwork() {
        if (level == null || level.isClientSide) return lastMoved = 0;
        IItemHandler external = external();
        LumenNetworkManager manager = manager();
        if (external == null || manager == null || getNetworkId() == null) return lastMoved = 0;

        int moved = 0;
        for (int slot = 0; slot < external.getSlots(); slot++) {
            ItemStack offered = external.extractItem(slot, 64, true);
            if (offered.isEmpty() || !allowed(offered)) continue;
            ItemStack simulatedRemainder = NetworkStorageService.insert(
                    manager, getNetworkId(), offered, true, level.registryAccess());
            int accepted = offered.getCount() - simulatedRemainder.getCount();
            if (accepted <= 0) continue;

            ItemStack extracted = external.extractItem(slot, accepted, false);
            if (extracted.isEmpty()) continue;
            ItemStack remainder = NetworkStorageService.insert(
                    manager, getNetworkId(), extracted, false, level.registryAccess());
            moved += extracted.getCount() - remainder.getCount();
            if (!remainder.isEmpty()) {
                remainder = external.insertItem(slot, remainder, false);
                dropRemainder(ItemHandlerHelper.insertItemStacked(external, remainder, false));
            }
        }
        return lastMoved = moved;
    }

    public int moveToExternal() {
        if (level == null || level.isClientSide) return lastMoved = 0;
        IItemHandler external = external();
        LumenNetworkManager manager = manager();
        if (external == null || manager == null || getNetworkId() == null) return lastMoved = 0;

        int moved = 0;
        for (var stored : NetworkStorageService.snapshot(manager, getNetworkId(), level.registryAccess())) {
            if (!allowed(stored.prototype())) continue;
            int request = (int) Math.min(stored.count(), stored.prototype().getMaxStackSize());
            ItemStack offered = stored.prototype().copyWithCount(request);
            ItemStack simulatedRemainder = ItemHandlerHelper.insertItemStacked(external, offered, true);
            int accepted = request - simulatedRemainder.getCount();
            if (accepted <= 0) continue;

            ItemStack extracted = NetworkStorageService.extract(
                    manager, getNetworkId(), offered, accepted, false, level.registryAccess());
            int extractedCount = extracted.getCount();
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(external, extracted, false);
            moved += extractedCount - remainder.getCount();
            if (!remainder.isEmpty()) {
                dropRemainder(NetworkStorageService.insert(
                        manager, getNetworkId(), remainder, false, level.registryAccess()));
            }
        }
        return lastMoved = moved;
    }

    private void dropRemainder(ItemStack remainder) {
        // External handlers may accept less than they simulated. Never discard
        // items if rollback is rejected or the network has exhausted its energy.
        if (!remainder.isEmpty() && level != null && !level.isClientSide) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, remainder);
        }
    }

    private LumenNetworkManager manager() {
        return level == null ? null : LumenMeshIntegration.getNetworkManager(level);
    }

    public void toggleBlacklist() {
        blacklist = !blacklist;
        setChanged();
    }

    public void toggleAutoToNetwork() {
        autoToNetwork = !autoToNetwork;
        if (autoToNetwork) autoToExternal = false;
        setChanged();
    }

    public void toggleAutoToExternal() {
        autoToExternal = !autoToExternal;
        if (autoToExternal) autoToNetwork = false;
        setChanged();
    }

    @Override
    protected boolean isOperational() {
        return super.isOperational() && external() != null;
    }

    public void serverTick() {
        refreshActiveState();
        if (++cooldown < 20) return;
        cooldown = 0;
        if (autoToNetwork) moveToNetwork();
        else if (autoToExternal) moveToExternal();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new StorageLinkMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("filters", filters.serializeNBT(registries));
        tag.putBoolean("blacklist", blacklist);
        tag.putBoolean("auto_in", autoToNetwork);
        tag.putBoolean("auto_out", autoToExternal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("filters")) filters.deserializeNBT(registries, tag.getCompound("filters"));
        blacklist = tag.getBoolean("blacklist");
        autoToNetwork = tag.getBoolean("auto_in");
        autoToExternal = tag.getBoolean("auto_out");
    }
}
