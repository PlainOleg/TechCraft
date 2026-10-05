package com.plainoleg.techcraft.lumenmesh.block.storage;

import com.plainoleg.techcraft.lumenmesh.block.LumenFacingEntityBlock;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PrismDriveBlock extends LumenFacingEntityBlock {
    public static final MapCodec<PrismDriveBlock> CODEC = simpleCodec(PrismDriveBlock::new);
    public PrismDriveBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PrismDriveBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l,p,s,be) -> { if (be instanceof PrismDriveBlockEntity drive) drive.serverTick(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof PrismDriveBlockEntity drive) {
            serverPlayer.openMenu(drive, data -> data.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PrismDriveBlockEntity drive) {
            for (int i = 0; i < drive.getMedia().getSlots(); i++) {
                ItemStack stack = drive.getMedia().getStackInSlot(i);
                if (!stack.isEmpty()) Block.popResource(level, pos, stack.copy());
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
