package TechCraft.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import javax.annotation.Nonnull;
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

    private final int miningSize;

    /**
     * Создает новый молот с указанными параметрами.
     *
     * @param tier       уровень материала инструмента
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
     *
     * @return размер области (обычно 3 для 3x3)
     */
    public int getMiningSize() {
        return miningSize;
    }

    /**
     * Получает список блоков для разрушения молотом.
     *
     * @param initialBlockPos начальная позиция блока
     * @param player          игрок использующий молот
     * @param hammerStack     стак с молотом
     * @return список позиций соседних блоков
     */
    public static List<BlockPos> getBlocksToBeDestroyed(BlockPos initialBlockPos, ServerPlayer player, ItemStack hammerStack) {
        if (initialBlockPos == null || player == null || hammerStack == null
                || !(hammerStack.getItem() instanceof HammerItem tool)) return List.of();
        return MiningArea.aroundHit(initialBlockPos, player, tool.getMiningSize(), false);
    }
}
