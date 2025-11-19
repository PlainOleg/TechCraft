package TechCraft.item;

import TechCraft.TechCraft;
import TechCraft.item.custom.DamageOnCraftUseItem;
import TechCraft.item.custom.HammerItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import  net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nonnull;

public class ModItems {
    public  static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    public static final DeferredItem<Item> Raw_Tin;
    public static final DeferredItem<Item> Tin_Ingot;
    public static final DeferredItem<Item> Tin_Plate;
    public static final DeferredItem<Item> Forge_Book;
    public static final DeferredItem<Item> Cutter;

    public static final DeferredItem<HammerItem> Forge_Hammer;

    static {
        Raw_Tin = ITEMS.register("raw_tin", () -> new Item(new Item.Properties()));
        Tin_Ingot = ITEMS.register("tin_ingot", () -> new Item(new Item.Properties()));
        Tin_Plate = ITEMS.register("tin_plate", () -> new Item(new Item.Properties()));

        Forge_Book = ITEMS.register("forge_book", () -> new ForgeBook(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).setNoRepair()));
        Forge_Hammer = ITEMS.register("forge_hammer", () ->
                new HammerItem(Tiers.IRON, 3, new Item.Properties()
                        .component(DataComponents.MAX_DAMAGE, 250)
                        .attributes(DiggerItem.createAttributes(Tiers.IRON, 7f, -3.5f))
                )
        );

        Cutter = ITEMS.register("cutter", () ->
                new DamageOnCraftUseItem(new Item.Properties()
                        .component(DataComponents.MAX_DAMAGE, 180)
                ){
                    @Override
                    public boolean isRepairable(@Nonnull ItemStack stack) {
                        return false; // запрещаем ремонт, чар на починку не отключает
                    }
                }
        );
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}