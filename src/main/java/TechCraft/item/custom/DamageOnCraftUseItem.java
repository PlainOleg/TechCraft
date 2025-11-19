
package TechCraft.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class DamageOnCraftUseItem extends Item {

    public DamageOnCraftUseItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.applyComponents(stack.getComponents());

        int newDamage = stack.getDamageValue() + 1;
        if (newDamage >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setDamageValue(newDamage);
        return copy;
    }
}