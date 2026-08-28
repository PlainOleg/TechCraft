package TechCraft.item.custom;

import net.minecraft.world.item.Item;

/**
 * Базовый класс для всех пластин в моде TechCraft.
 * Обеспечивает общую логику и свойства для всех типов пластин.
 */
public class PlateItem extends Item {

    /**
     * Создает новый предмет-пластину.
     * @param properties свойства предмета
     */
    public PlateItem(Properties properties) {
        super(properties);
    }
}
