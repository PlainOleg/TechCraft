package TechCraft.lumenmesh.block.energy;

import com.mojang.serialization.MapCodec;
import TechCraft.lumenmesh.block.LumenFacingEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Энергомост Lumen Mesh.
 * Принимает энергию из внешних источников и передаёт в сеть.
 */
public class EnergyBridgeBlock extends LumenFacingEntityBlock {
    public static final MapCodec<EnergyBridgeBlock> CODEC = simpleCodec(EnergyBridgeBlock::new);

    public EnergyBridgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof EnergyBridgeBlockEntity bridge) {
            serverPlayer.openMenu(bridge, data -> data.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyBridgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, TechCraft.lumenmesh.blockentity.LumenBlockEntities.ENERGY_BRIDGE.get(), EnergyBridgeBlockEntity::serverTick);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnergyBridgeBlockEntity bridge) {
                bridge.onPlaced();
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof EnergyBridgeBlockEntity bridge) {
                    bridge.onRemoved();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
