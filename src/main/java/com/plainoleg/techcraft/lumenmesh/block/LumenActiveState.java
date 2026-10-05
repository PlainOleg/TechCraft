package com.plainoleg.techcraft.lumenmesh.block;

import net.minecraft.world.level.block.entity.BlockEntity;

/** Keeps the visual lit model synchronized with real server-side operation. */
public final class LumenActiveState {
    private LumenActiveState() {}
    public static void set(BlockEntity entity, boolean active) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide) return;
        var state = entity.getBlockState();
        if (state.hasProperty(LumenFacingEntityBlock.ACTIVE) && state.getValue(LumenFacingEntityBlock.ACTIVE) != active)
            entity.getLevel().setBlock(entity.getBlockPos(), state.setValue(LumenFacingEntityBlock.ACTIVE, active), 3);
    }
}
