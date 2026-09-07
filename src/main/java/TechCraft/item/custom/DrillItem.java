package TechCraft.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Дрель - электрический инструмент для разрушения блоков в области.
 * Работает от энергии, без энергии не копает.
 * При приседании разрушает только один блок.
 */
public class DrillItem extends DiggerItem {

    private static final String ENERGY_KEY = "Energy";

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
        return tag.contains(ENERGY_KEY) ? Math.clamp(tag.getInt(ENERGY_KEY), 0, maxEnergy) : maxEnergy;
    }

    /**
     * Устанавливает энергию дрели.
     */
    public void setEnergy(ItemStack stack, int energy) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        tag.putInt(ENERGY_KEY, Math.clamp(energy, 0, maxEnergy));
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
     *
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
        if (amount > 0) setEnergy(stack, (int) Math.min(maxEnergy, (long) getEnergy(stack) + amount));
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
