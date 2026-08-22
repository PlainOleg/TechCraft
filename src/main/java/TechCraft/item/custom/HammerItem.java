package TechCraft.item.custom;

import TechCraft.TechCraft;
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

/**
 * Молот - инструмент для разрушения блоков в области 3x3.
 * При использовании в крафте наносит урон инструменту.
 * При приседании разрушает только один блок.
 */
public class HammerItem extends DiggerItem {

    private static final int DEFAULT_DURABILITY = 250;
    private static final float DEFAULT_ATTACK_SPEED = 3f;
    private static final float DEFAULT_ATTACK_DAMAGE = 7f;
    private static final float DEFAULT_ATTACK_DAMAGE_MODIFIER = -3.5f;
    private static final int DEFAULT_MINING_SIZE = 3;
    private static final float RAYTRACE_DISTANCE = 6f;

    private final int miningSize;

    /**
     * Создает новый молот с указанными параметрами.
     * @param tier уровень материала инструмента
     * @param miningSize размер области разрушения (3 для 3x3)
     * @param properties свойства предмета
     */
    public HammerItem(Tier tier, int miningSize, Properties properties) {
        super(tier, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, properties
                .stacksTo(1)
                .component(net.minecraft.core.component.DataComponents.MAX_DAMAGE, DEFAULT_DURABILITY)
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
        ItemStack copy = stack.copy();

        int newDamage = stack.getDamageValue() + 1;
        if (newDamage >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setDamageValue(newDamage);
        return copy;
    }

    /**
     * Возвращает размер области разрушения молота.
     * @return размер области (обычно 3 для 3x3)
     */
    public int getMiningSize() { return miningSize; }

    /**
     * Получает список блоков для разрушения молотом.
     * @param initialBlockPos начальная позиция блока
     * @param player игрок использующий молот
     * @param hammerStack стак с молотом
     * @return список позиций соседних блоков
     */
    public static List<BlockPos> getBlocksToBeDestroyed(BlockPos initialBlockPos, ServerPlayer player, ItemStack hammerStack) {
        if (initialBlockPos == null || player == null || hammerStack == null || hammerStack.isEmpty()) {
            TechCraft.LOGGER.debug("Invalid parameters for hammer block calculation");
            return List.of();
        }

        if (!(hammerStack.getItem() instanceof HammerItem hammer)) {
            return List.of();
        }

        // Если игрок приседает — всегда 1x1
        int size = player.isShiftKeyDown() ? 1 : hammer.getMiningSize();
        if (size <= 1) {
            return List.of(); // центральный блок ломает ванилла
        }

        List<BlockPos> positions = new ArrayList<>();

        try {
            // Определяем сторону, в которую смотрит игрок
            BlockHitResult result = player.level().clip(new ClipContext(
                    player.getEyePosition(1f),
                    player.getEyePosition(1f).add(player.getViewVector(1f).scale(RAYTRACE_DISTANCE)),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (result.getType() == HitResult.Type.MISS) {
                TechCraft.LOGGER.debug("Raytrace missed for hammer at position {}", initialBlockPos);
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

            TechCraft.LOGGER.debug("Hammer calculated {} blocks to destroy at {}", positions.size(), initialBlockPos);
        } catch (Exception e) {
            TechCraft.LOGGER.error("Error calculating hammer blocks at position {}: {}", initialBlockPos, e.getMessage());
        }

        return positions;
    }
}