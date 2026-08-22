
package TechCraft.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Базовый класс для предметов, которые получают урон при использовании в крафте.
 * Примеры: резак, ножницы и другие инструменты.
 */
public class DamageOnCraftUseItem extends Item {

    /**
     * Создает новый предмет, который получает урон при крафте.
     * @param properties свойства предмета
     */
    public DamageOnCraftUseItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack) {
        return true;
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
}