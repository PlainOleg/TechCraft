package TechCraft.lumenmesh.block.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Базовый класс для кабелей Lumen Mesh.
 * Поддерживает соединение с соседними блоками.
 */
public class MeshCableBlock extends BaseEntityBlock {
    public static final MapCodec<MeshCableBlock> CODEC = simpleCodec(MeshCableBlock::new);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape CENTER = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_SHAPE = box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SOUTH_SHAPE = box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape EAST_SHAPE = box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape WEST_SHAPE = box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape UP_SHAPE = box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN_SHAPE = box(6, 0, 6, 10, 6, 10);

    public MeshCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(NORTH, false).setValue(SOUTH, false)
            .setValue(EAST, false).setValue(WEST, false)
            .setValue(UP, false).setValue(DOWN, false).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeshCableBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            BlockState neighbor = context.getLevel().getBlockState(pos.relative(direction));
            state = state.setValue(getPropertyForDirection(direction), canConnectTo(neighbor));
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER;
        
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE);
        
        return shape;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = state.setValue(getPropertyForDirection(direction), canConnectTo(neighborState));
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MeshCableBlockEntity cable) cable.topologyChanged();
        return updated;
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, ACTIVE);
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    /**
     * Проверяет, может ли кабель соединиться с соседним блоком.
     */
    private boolean canConnectTo(BlockState neighborState) {
        return neighborState.getBlock() instanceof MeshCableBlock
            || neighborState.getBlock() instanceof TechCraft.lumenmesh.block.LumenFacingBlock
            || neighborState.getBlock() instanceof TechCraft.lumenmesh.block.LumenFacingEntityBlock;
    }
}
