package TechCraft.block;

import TechCraft.item.SmelterUpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class AlloySmelterBlock extends BaseEntityBlock {

    public static final MapCodec<AlloySmelterBlock> CODEC = simpleCodec(AlloySmelterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AlloySmelterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Применение улучшений через Shift + ПКМ
        if (player.isShiftKeyDown() && !stack.isEmpty() && stack.getItem() instanceof SmelterUpgradeItem upgradeItem) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AlloySmelterBlockEntity alloySmelter) {
                    AlloySmelterBlockEntity.UpgradeType type = upgradeItem.getUpgradeType();
                    int tier = upgradeItem.getTier();

                    if (alloySmelter.applyUpgrade(type, tier)) {
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        player.displayClientMessage(Component.literal("Улучшение применено! (" + type.name() + " T" + tier + ")"), true);
                        return ItemInteractionResult.SUCCESS;
                    } else {
                        player.displayClientMessage(Component.literal("Улучшение этого уровня или выше уже установлено!"), true);
                        return ItemInteractionResult.FAIL;
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        // Обычное открытие GUI
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AlloySmelterBlockEntity alloySmelter) {
                serverPlayer.openMenu(alloySmelter, buf -> buf.writeBlockPos(pos));
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlloySmelterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ALLOY_SMELTER.get(),
                level.isClientSide ? AlloySmelterBlockEntity::clientTick : AlloySmelterBlockEntity::serverTick);
    }
}