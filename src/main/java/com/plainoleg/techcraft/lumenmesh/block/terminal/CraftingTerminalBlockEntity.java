package com.plainoleg.techcraft.lumenmesh.block.terminal;

import com.plainoleg.techcraft.lumenmesh.LumenMeshIntegration;
import com.plainoleg.techcraft.lumenmesh.block.core.MeshCoreBlockEntity;
import com.plainoleg.techcraft.lumenmesh.menu.CraftingTerminalMenu;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class CraftingTerminalBlockEntity extends ItemTerminalBlockEntity {
    public CraftingTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(LumenBlockEntities.CRAFTING_TERMINAL.get(), pos, state);
    }

    @Override public NodeType getNodeType() { return NodeType.CRAFTING_TERMINAL; }
    @Override public Component getDisplayName() { return Component.translatable("block.techcraft.crafting_terminal"); }

    public int getCraftingGridSize() {
        if (getLevel() == null || getNetworkId() == null) return 3;
        var manager = LumenMeshIntegration.getNetworkManager(getLevel());
        if (manager == null) return 3;
        return manager.getNodes(getNetworkId(), MeshCoreBlockEntity.class).stream()
                .mapToInt(MeshCoreBlockEntity::getCraftingGridSize).max().orElse(3);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraftingTerminalMenu(id, inventory, this);
    }
}
