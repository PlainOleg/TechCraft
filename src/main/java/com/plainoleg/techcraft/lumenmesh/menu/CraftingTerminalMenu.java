package com.plainoleg.techcraft.lumenmesh.menu;

import com.plainoleg.techcraft.lumenmesh.block.terminal.CraftingTerminalBlockEntity;
import com.plainoleg.techcraft.lumenmesh.registry.LumenMenuTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftingTerminalMenu extends ItemTerminalMenu {
    public static final int CRAFTING_START = 81;
    private final SimpleContainer craftingTemplate = new SimpleContainer(10);
    private int craftingGridSize = 3;

    public CraftingTerminalMenu(int id, Inventory inventory, CraftingTerminalBlockEntity terminal) {
        super(LumenMenuTypes.CRAFTING_TERMINAL.get(), id, inventory, terminal, 15, 3, 17, 52);
        craftingGridSize = terminal == null ? 3 : terminal.getCraftingGridSize();
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                addSlot(new Slot(craftingTemplate, col + row * 3, 218 + col * 18, 157 + row * 18));
        addSlot(new Slot(craftingTemplate, 9, 288, 175) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return terminal == null ? 3 : terminal.getCraftingGridSize(); }
            @Override public void set(int value) { craftingGridSize = Math.clamp(value, 3, 9); }
        });
    }

    public CraftingTerminalMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, (CraftingTerminalBlockEntity) inventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    public int getCraftingGridSize() { return craftingGridSize; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= CRAFTING_START) return ItemStack.EMPTY;
        return super.quickMoveStack(player, index);
    }

}
