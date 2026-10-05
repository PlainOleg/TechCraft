package com.plainoleg.techcraft.item.tool;

import com.plainoleg.techcraft.item.ItemEnergy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * Дрель - электрический инструмент для разрушения блоков в области.
 * Работает от энергии, без энергии не копает.
 * При приседании разрушает только один блок.
 */
public class DrillItem extends DiggerItem {

    private final int maxEnergy;
    private final int miningSize;
    private final int energyCostPerBlock;

    /**
     * Создает новую дрель с указанными параметрами.
     *
     * @param tier               уровень материала инструмента
     * @param maxEnergy          максимальная вместимость энергии
     * @param miningSize         размер области разрушения (3 для 3x3)
     * @param energyCostPerBlock стоимость энергии за блок
     * @param properties         свойства предмета
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
        return ItemEnergy.barWidth(stack, maxEnergy);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ItemEnergy.barColor(stack, maxEnergy);
    }

    /**
     * Получает текущую энергию дрели.
     */
    public int getEnergy(ItemStack stack) {
        return ItemEnergy.get(stack, maxEnergy);
    }

    /**
     * Устанавливает энергию дрели.
     */
    public void setEnergy(ItemStack stack, int energy) {
        ItemEnergy.set(stack, energy, maxEnergy);
    }

    /**
     * Хранилище для зарядки дрели (для регистрации capability {@code Capabilities.EnergyStorage.ITEM}).
     */
    public IEnergyStorage getEnergyStorage(ItemStack stack) {
        return ItemEnergy.receiveOnlyStorage(stack, maxEnergy);
    }

    /**
     * Получает максимальную энергию дрели.
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * Потребляет энергию при копании.
     *
     * @return true если энергии хватило, false если нет
     */
    public boolean consumeEnergy(ItemStack stack) {
        return ItemEnergy.consume(stack, energyCostPerBlock, maxEnergy);
    }

    /**
     * Добавляет энергию в дрель.
     */
    public void addEnergy(ItemStack stack, int amount) {
        ItemEnergy.add(stack, amount, maxEnergy);
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
     *
     * @param initialBlockPos начальная позиция блока
     * @param player          игрок использующий дрель
     * @param drillStack      стак с дрелью
     * @return список позиций соседних блоков
     */
    public static List<BlockPos> getBlocksToBeDestroyed(BlockPos initialBlockPos, ServerPlayer player, ItemStack drillStack) {
        if (initialBlockPos == null || player == null || drillStack == null
                || !(drillStack.getItem() instanceof DrillItem tool)) return List.of();
        return MiningArea.aroundHit(initialBlockPos, player, tool.getMiningSize(), true);
    }
}
