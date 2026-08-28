package TechCraft.item;

import TechCraft.block.AlloySmelterBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

public class SmelterUpgradeItem extends Item {

    private final AlloySmelterBlockEntity.UpgradeType upgradeType;
    private final int tier;

    public SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType upgradeType, int tier, Properties properties) {
        super(properties);
        this.upgradeType = upgradeType;
        this.tier = tier;
    }

    public AlloySmelterBlockEntity.UpgradeType getUpgradeType() {
        return upgradeType;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // Записываем NBT данные, которые читает AlloySmelterBlock при ПКМ
        CompoundTag tag = new CompoundTag();
        tag.putString("upgrade_type", upgradeType.getName());
        tag.putInt("tier", tier);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
}