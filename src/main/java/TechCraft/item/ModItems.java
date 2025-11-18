package TechCraft.item;

import TechCraft.TechCraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import  net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItems {
    public  static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    public static final DeferredItem<Item> Raw_Tin;
    public static final DeferredItem<Item> Tin_Ingot;
    public static final DeferredItem<Item> Tin_Plate;
    public static final DeferredItem<Item> Forge_Book;
    public static final DeferredItem<Item> Forge_Hammer;
    public static final DeferredItem<Item> Cutter;

    static {
        Raw_Tin = ITEMS.register("raw_tin", () -> new Item(new Item.Properties()));
        Tin_Ingot = ITEMS.register("tin_ingot", () -> new Item(new Item.Properties()));
        Tin_Plate = ITEMS.register("tin_plate", () -> new Item(new Item.Properties()));

        Forge_Book = ITEMS.register("forge_book", () -> new ForgeBook(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).setNoRepair()));
        Forge_Hammer = ITEMS.register("forge_hammer", () -> new Item(new Item.Properties().durability(250).stacksTo(1)));
        Cutter = ITEMS.register("cutter", () -> new Item(new Item.Properties().durability(250).stacksTo(1)));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}