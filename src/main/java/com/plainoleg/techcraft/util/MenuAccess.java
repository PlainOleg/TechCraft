package com.plainoleg.techcraft.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Common lifetime and distance checks for block-entity menus.
 */
public final class MenuAccess {
    private MenuAccess() {
    }

    public static boolean stillValid(Player player, BlockEntity entity) {
        return entity != null && !entity.isRemoved()
                && entity.getLevel() == player.level()
                && player.level().getBlockEntity(entity.getBlockPos()) == entity
                && entity.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }
}
