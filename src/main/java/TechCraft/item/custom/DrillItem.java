package TechCraft.item.custom;

import TechCraft.TechCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Дрель - электрический инструмент для разрушения блоков в области.
 * Работает от энергии, без энергии не копает.
 * При приседании разрушает только один блок.
 */
public class DrillItem extends DiggerItem {

    private static final String ENERGY_KEY = "Energy";
    private static final float RAYTRACE_DISTANCE = 6f;

    private final int maxEnergy;
    private final int miningSize;
    private final int energyCostPerBlock;

    /**
     * Создает новую дрель с указанными параметрами.
     * @param tier уровень материала инструмента
     * @param maxEnergy максимальная вместимость энергии
     * @param miningSize размер области разрушения (3 для 3x3)
     * @param energyCostPerBlock стоимость энергии за блок
     * @param properties свойства предмета
     */
    public DrillItem(Tier tier, int maxEnergy, int miningSize, int energyCostPerBlock, Properties properties) {
        super(tier, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, properties.stacksTo(1));
        this.maxEnergy = maxEnergy;
        this.miningSize = miningSize;
        this.energyCostPerBlock = energyCostPerBlock;
    }

    @Override
    public boolean isRepairable(@Nonnull ItemStack stack) {
        return true; // дрели можно чинить (зачарование на починку)
    }

    @Override
    public boolean isDamageable(@Nonnull ItemStack stack) {
        return false; // дрели не теряют прочность (работают от энергии)
    }

    @Override
    public boolean mineBlock(ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.level.block.state.BlockState state, BlockPos pos, net.minecraft.world.entity.LivingEntity miner) {
        // Не уменьшаем прочность при копании - дрель работает от энергии
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.LivingEntity attacker) {
        // Не уменьшаем прочность при атаке - дрель работает от энергии
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
        // Проверяем энергию
        if (getEnergy(stack) < energyCostPerBlock) {
            return 0f; // Без энергии не копаем
        }

        // Получаем базовую скорость от родительского класса (для блоков которые киркой)
        float baseSpeed = super.getDestroySpeed(stack, state);

        // Если блок копается киркой - возвращаем базовую скорость
        if (baseSpeed > 1f) {
            return baseSpeed;
        }

        // Для мягких блоков (земля, песок, гравий) возвращаем высокую скорость
        if (state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) ||
            state.is(net.minecraft.tags.BlockTags.DIRT) ||
            state.is(net.minecraft.tags.BlockTags.SAND)) {
            return getTier().getSpeed();
        }

        // Для обычного камня и руд (но не бедрок и не обсидиан)
        if (state.is(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES) ||
            state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)) {
            // Проверяем что это не бедрок и не обсидиан
            float hardness = state.getDestroySpeed(null, null);
            if (hardness < 50f) { // Бедрок имеет прочность -1 (не копается), обсидиан ~50
                return getTier().getSpeed();
            }
        }

        return 1f;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // Показываем полосу прочности для отображения энергии
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int currentEnergy = getEnergy(stack);
        // Маппим энергию на полосу прочности (0-13)
        // Полная энергия = полная полоса, пустая энергия = пустая полоса
        return (int) ((currentEnergy / (float) maxEnergy) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int currentEnergy = getEnergy(stack);
        float ratio = currentEnergy / (float) maxEnergy;
        
        // Цвет меняется от красного (низкая энергия) к зеленому (высокая энергия)
        // Как в IC2Classic
        if (ratio > 0.5f) {
            return 0x00FF00; // Зеленый
        } else if (ratio > 0.25f) {
            return 0xFFFF00; // Желтый
        } else {
            return 0xFF0000; // Красный
        }
    }

    @Override
    @Nonnull
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        CompoundTag tag = new CompoundTag();
        tag.putInt(ENERGY_KEY, maxEnergy);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    /**
     * Получает текущую энергию дрели.
     */
    public int getEnergy(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return maxEnergy; // Если NBT нет - считаем что полная энергия (новый предмет)
        }
        CompoundTag tag = customData.copyTag();
        return tag.getInt(ENERGY_KEY);
    }

    /**
     * Устанавливает энергию дрели.
     */
    public void setEnergy(ItemStack stack, int energy) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        tag.putInt(ENERGY_KEY, Math.min(energy, maxEnergy));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Получает максимальную энергию дрели.
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * Потребляет энергию при копании.
     * @return true если энергии хватило, false если нет
     */
    public boolean consumeEnergy(ItemStack stack) {
        int currentEnergy = getEnergy(stack);
        if (currentEnergy < energyCostPerBlock) {
            return false;
        }
        setEnergy(stack, currentEnergy - energyCostPerBlock);
        return true;
    }

    /**
     * Добавляет энергию в дрель.
     */
    public void addEnergy(ItemStack stack, int amount) {
        setEnergy(stack, getEnergy(stack) + amount);
    }


    /**
     * Возвращает размер области разрушения дрели.
     */
    public int getMiningSize() {
        return miningSize;
    }

    /**
     * Возвращает стоимость энергии за блок.
     */
    public int getEnergyCostPerBlock() {
        return energyCostPerBlock;
    }

    /**
     * Получает список блоков для разрушения дрелью.
     * @param initialBlockPos начальная позиция блока
     * @param player игрок использующий дрель
     * @param drillStack стак с дрелью
     * @return список позиций соседних блоков
     */
    public static List<BlockPos> getBlocksToBeDestroyed(BlockPos initialBlockPos, ServerPlayer player, ItemStack drillStack) {
        if (initialBlockPos == null || player == null || drillStack == null || drillStack.isEmpty()) {
            TechCraft.LOGGER.debug("Invalid parameters for drill block calculation");
            return List.of();
        }

        if (!(drillStack.getItem() instanceof DrillItem drill)) {
            return List.of();
        }

        // Проверяем энергию
        int currentEnergy = drill.getEnergy(drillStack);

        // Если энергии недостаточно даже для центрального блока
        if (currentEnergy < drill.energyCostPerBlock) {
            return List.of();
        }

        // Если игрок приседает — всегда 1x1
        int size = player.isShiftKeyDown() ? 1 : drill.getMiningSize();
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
                TechCraft.LOGGER.debug("Raytrace missed for drill at position {}", initialBlockPos);
                return positions;
            }

            Direction dir = result.getDirection();
            int offset = size / 2;

            // Для 5x5 смещаем область вверх на 1 блок, для 7x7 - на 2 блока
            BlockPos centerPos = initialBlockPos;
            if (size == 5 && dir.getAxis() != Direction.Axis.Y) {
                centerPos = initialBlockPos.above(1);
            } else if (size == 7 && dir.getAxis() != Direction.Axis.Y) {
                centerPos = initialBlockPos.above(2);
            }

            // Генерируем координаты от -offset до (size - offset - 1)
            if (dir.getAxis() == Direction.Axis.Y) { // вверх/вниз → X и Z
                for (int x = -offset; x < size - offset; x++) {
                    for (int z = -offset; z < size - offset; z++) {
                        positions.add(centerPos.offset(x, 0, z));
                    }
                }
            }
            else if (dir.getAxis() == Direction.Axis.Z) { // север/юг → X и Y
                for (int x = -offset; x < size - offset; x++) {
                    for (int y = -offset; y < size - offset; y++) {
                        positions.add(centerPos.offset(x, y, 0));
                    }
                }
            }
            else if (dir.getAxis() == Direction.Axis.X) { // восток/запад → Z и Y
                for (int z = -offset; z < size - offset; z++) {
                    for (int y = -offset; y < size - offset; y++) {
                        positions.add(centerPos.offset(0, y, z));
                    }
                }
            }

            TechCraft.LOGGER.debug("Drill calculated {} blocks to destroy at {}", positions.size(), initialBlockPos);
        } catch (Exception e) {
            TechCraft.LOGGER.error("Error calculating drill blocks at position {}: {}", initialBlockPos, e.getMessage());
        }

        return positions;
    }
}
