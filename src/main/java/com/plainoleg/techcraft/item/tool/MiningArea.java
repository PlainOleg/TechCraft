package com.plainoleg.techcraft.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometry shared by area tools. The initial block is always left to vanilla.
 */
public final class MiningArea {
    private static final double REACH = 6.0;

    private MiningArea() {
    }

    public static List<BlockPos> aroundHit(BlockPos origin, ServerPlayer player, int size, boolean raiseLargeArea) {
        if (player.isShiftKeyDown() || size <= 1) return List.of();
        var eye = player.getEyePosition(1.0F);
        var hit = player.level().clip(new ClipContext(eye,
                eye.add(player.getViewVector(1.0F).scale(REACH)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.MISS) return List.of();
        int rise = raiseLargeArea ? (size == 5 ? 1 : size == 7 ? 2 : 0) : 0;
        return positions(origin, hit.getDirection().getAxis(), size, rise);
    }

    public static List<BlockPos> positions(BlockPos origin, Direction.Axis axis, int size, int rise) {
        if (size <= 1) return List.of();
        BlockPos center = axis == Direction.Axis.Y ? origin : origin.above(rise);
        List<BlockPos> positions = new ArrayList<>(size * size - 1);
        int offset = size / 2;
        for (int first = -offset; first < size - offset; first++) {
            for (int second = -offset; second < size - offset; second++) {
                BlockPos position = switch (axis) {
                    case X -> center.offset(0, second, first);
                    case Y -> center.offset(first, 0, second);
                    case Z -> center.offset(first, second, 0);
                };
                if (!position.equals(origin)) positions.add(position);
            }
        }
        return positions;
    }
}
