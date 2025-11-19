package TechCraft.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class HammerItem extends DiggerItem {

    private final int miningSize;

    public HammerItem(Tier tier, int miningSize, Properties properties) {
        super(tier, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, properties
                .stacksTo(1)
                .component(net.minecraft.core.component.DataComponents.MAX_DAMAGE, 250)
        );
        this.miningSize = miningSize;
    }

    @Override
    public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public boolean isRepairable(@Nonnull ItemStack stack) {
        return false; // запрещаем ремонт в верстаке
    }

    @Override
    @Nonnull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        @Nonnull ItemStack copy = stack.copy();
        copy.applyComponents(stack.getComponents());

        int newDamage = stack.getDamageValue() + 1;
        if (newDamage >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setDamageValue(newDamage);
        return copy;
    }

    // ЛОГИКА 3×3 ЛОМАНИЯ
    public int getMiningSize() { return miningSize; }

    public static List<BlockPos> getBlocksToBeDestroyed(BlockPos initialBlockPos, ServerPlayer player, ItemStack hammerStack) {
        if (!(hammerStack.getItem() instanceof HammerItem hammer)) {
            return List.of();
        }

        // Если игрок приседает — всегда 1x1
        int size = player.isShiftKeyDown() ? 1 : hammer.getMiningSize();
        if (size <= 1) {
            return List.of(); // центральный блок ломает ванилла
        }

        List<BlockPos> positions = new ArrayList<>();

        // Определяем сторону, в которую смотрит игрок
        BlockHitResult result = player.level().clip(new ClipContext(
                player.getEyePosition(1f),
                player.getEyePosition(1f).add(player.getViewVector(1f).scale(6f)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (result.getType() == HitResult.Type.MISS) {
            return positions;
        }

        Direction dir = result.getDirection();
        int offset = size / 2;

        // Генерируем координаты от -offset до (size - offset - 1)
        if (dir.getAxis() == Direction.Axis.Y) { // вверх/вниз → X и Z
            for (int x = -offset; x < size - offset; x++) {
                for (int z = -offset; z < size - offset; z++) {
                    if (x != 0 || z != 0) {
                        positions.add(initialBlockPos.offset(x, 0, z));
                    }
                }
            }
        }
        else if (dir.getAxis() == Direction.Axis.Z) { // север/юг → X и Y
            for (int x = -offset; x < size - offset; x++) {
                for (int y = -offset; y < size - offset; y++) {
                    if (x != 0 || y != 0) {
                        positions.add(initialBlockPos.offset(x, y, 0));
                    }
                }
            }
        }
        else if (dir.getAxis() == Direction.Axis.X) { // восток/запад → Z и Y
            for (int z = -offset; z < size - offset; z++) {
                for (int y = -offset; y < size - offset; y++) {
                    if (z != 0 || y != 0) {
                        positions.add(initialBlockPos.offset(0, y, z));
                    }
                }
            }
        }

        return positions;
    }
}