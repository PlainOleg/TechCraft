package TechCraft.item;

import TechCraft.TechCraft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import  net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItems {
    public  static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    public static final DeferredItem<Item> Raw_Tin;
    public static final DeferredItem<Item> Tin_Ingot;

    static {
        Raw_Tin = ITEMS.register("raw_tin", () -> new Item(new Item.Properties()));
        Tin_Ingot = ITEMS.register("tin_ingot", () -> new Item(new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}