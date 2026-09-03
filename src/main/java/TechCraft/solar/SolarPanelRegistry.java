package TechCraft.solar;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry mapping solar panel blocks and items to their types.
 * This provides type-safe lookup without relying on string IDs.
 */
public class SolarPanelRegistry {
    private static final Map<Block, SolarPanelType> BLOCK_TO_TYPE = new HashMap<>();
    private static final Map<Item, SolarPanelType> ITEM_TO_TYPE = new HashMap<>();

    /**
     * Registers a solar panel block and its item with their type.
     */
    public static void register(Block block, Item item, SolarPanelType type) {
        BLOCK_TO_TYPE.put(block, type);
        ITEM_TO_TYPE.put(item, type);
    }

    /**
     * Gets the solar panel type for a block.
     * @return the type, or null if the block is not a solar panel
     */
    @Nullable
    public static SolarPanelType getType(Block block) {
        return BLOCK_TO_TYPE.get(block);
    }

    /**
     * Gets the solar panel type for an item.
     * @return the type, or null if the item is not a solar panel block item
     */
    @Nullable
    public static SolarPanelType getType(Item item) {
        return ITEM_TO_TYPE.get(item);
    }

    /**
     * Checks if a block is a solar panel.
     */
    public static boolean isSolarPanel(Block block) {
        return BLOCK_TO_TYPE.containsKey(block);
    }

    /**
     * Checks if an item is a solar panel block item.
     */
    public static boolean isSolarPanelItem(Item item) {
        return ITEM_TO_TYPE.containsKey(item);
    }
}
