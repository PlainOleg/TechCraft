package TechCraft.solar;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Common solar panel block class for all tiers.
 * Each registered block gets its own SolarPanelType via supplier.
 */
public class SolarPanelBlock extends BaseEntityBlock {
    private static final MapCodec<SolarPanelBlock> CODEC = Block.simpleCodec(properties -> new SolarPanelBlock(properties, () -> new SolarPanelType(1, 4, 20000, "copper")));
    private final Supplier<SolarPanelType> panelType;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public SolarPanelBlock(Properties properties, Supplier<SolarPanelType> panelType) {
        super(properties);
        this.panelType = panelType;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * Gets the solar panel type for this block.
     */
    public SolarPanelType getPanelType() {
        return panelType.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModSolarBlockEntities.SOLAR_PANEL.get(), SolarPanelBlockEntity::serverTick);
    }
}
