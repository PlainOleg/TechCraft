package TechCraft.item;

import TechCraft.TechCraft;
import TechCraft.block.AlloySmelterBlockEntity;
import TechCraft.item.custom.CoilItem;
import TechCraft.item.custom.DamageOnCraftUseItem;
import TechCraft.item.custom.HammerItem;
import TechCraft.item.custom.PlateItem;
import TechCraft.item.custom.WireItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import  net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nonnull;

/**
 * Центральный класс для регистрации всех предметов мода TechCraft.
 * Содержит DeferredRegister и фабричные методы для упрощенной регистрации.
 */
public class ModItems {
    private static final int FORGE_HAMMER_DURABILITY = 250;
    private static final int CUTTER_DURABILITY = 180;
    private static final float FORGE_HAMMER_ATTACK_SPEED = 3f;
    private static final float FORGE_HAMMER_ATTACK_DAMAGE = 7f;
    private static final float FORGE_HAMMER_ATTACK_DAMAGE_MODIFIER = -3.5f;
    private static final int FORGE_HAMMER_MINING_SIZE = 3;

    public  static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    /**
     * Регистрирует обычный предмет для указанного материала.
     * @param material тип материала
     * @param itemType тип предмета (ingot, raw, etc.)
     * @return зарегистрированный предмет
     */
    private static DeferredItem<Item> registerMaterialItem(MaterialType material, String itemType) {
        return ITEMS.register(material.getName() + "_" + itemType, () -> new Item(new Item.Properties()));
    }

    /**
     * Регистрирует пластину для указанного материала.
     * @param material тип материала
     * @return зарегистрированная пластина
     */
    private static DeferredItem<Item> registerPlate(MaterialType material) {
        return ITEMS.register(material.getName() + "_plate", () -> new PlateItem(new Item.Properties()));
    }

    /**
     * Регистрирует проволоку для указанного материала.
     * @param material тип материала
     * @return зарегистрированная проволока
     */
    private static DeferredItem<Item> registerWire(MaterialType material) {
        return ITEMS.register(material.getName() + "_wire", () -> new WireItem(new Item.Properties()));
    }

    /**
     * Регистрирует катушку для указанного материала.
     * @param material тип материала
     * @return зарегистрированная катушка
     */
    private static DeferredItem<Item> registerCoil(MaterialType material) {
        return ITEMS.register(material.getName() + "_coil", () -> new CoilItem(new Item.Properties()));
    }

    public static final DeferredItem<Item> Raw_Tin;
    public static final DeferredItem<Item> Tin_Ingot;
    public static final DeferredItem<Item> Steel_Ingot;

    public static final DeferredItem<Item> Coal_Dust;
    public static final DeferredItem<Item> Iron_Dust;
    public static final DeferredItem<Item> Steel_Dust;

    public static final DeferredItem<Item> Tin_Plate;
    public static final DeferredItem<Item> Iron_Plate;
    public static final DeferredItem<Item> Gold_Plate;
    public static final DeferredItem<Item> Copper_Plate;
    public static final DeferredItem<Item> Steel_Plate;

    public static final DeferredItem<Item> Copper_Wire;
    public static final DeferredItem<Item> Tin_Wire;
    public static final DeferredItem<Item> Iron_Wire;
    public static final DeferredItem<Item> Gold_Wire;
    public static final DeferredItem<Item> Steel_Wire;

    public static final DeferredItem<Item> Copper_Coil;
    public static final DeferredItem<Item> Tin_Coil;
    public static final DeferredItem<Item> Iron_Coil;
    public static final DeferredItem<Item> Gold_Coil;
    public static final DeferredItem<Item> Steel_Coil;

    public static final DeferredItem<Item> Forge_Book;

    public static final DeferredItem<HammerItem> Forge_Hammer;
    public static final DeferredItem<Item> Cutter;

    // Smelter upgrades
    public static final DeferredItem<SmelterUpgradeItem> Capacity_Upgrade_T1;
    public static final DeferredItem<SmelterUpgradeItem> Capacity_Upgrade_T2;
    public static final DeferredItem<SmelterUpgradeItem> Capacity_Upgrade_T3;
    public static final DeferredItem<SmelterUpgradeItem> Temp_Upgrade_T1;
    public static final DeferredItem<SmelterUpgradeItem> Temp_Upgrade_T2;
    public static final DeferredItem<SmelterUpgradeItem> Temp_Upgrade_T3;
    public static final DeferredItem<SmelterUpgradeItem> Efficiency_Upgrade_T1;
    public static final DeferredItem<SmelterUpgradeItem> Efficiency_Upgrade_T2;
    public static final DeferredItem<SmelterUpgradeItem> Efficiency_Upgrade_T3;

    static {
        // Tin
        Raw_Tin = registerMaterialItem(MaterialType.TIN, "raw");
        Tin_Ingot = registerMaterialItem(MaterialType.TIN, "ingot");
        Tin_Plate = registerPlate(MaterialType.TIN);
        Tin_Wire = registerWire(MaterialType.TIN);
        Tin_Coil = registerCoil(MaterialType.TIN);

        // Iron
        Iron_Plate = registerPlate(MaterialType.IRON);
        Iron_Wire = registerWire(MaterialType.IRON);
        Iron_Coil = registerCoil(MaterialType.IRON);

        // Gold
        Gold_Plate = registerPlate(MaterialType.GOLD);
        Gold_Wire = registerWire(MaterialType.GOLD);
        Gold_Coil = registerCoil(MaterialType.GOLD);

        // Copper
        Copper_Plate = registerPlate(MaterialType.COPPER);
        Copper_Wire = registerWire(MaterialType.COPPER);
        Copper_Coil = registerCoil(MaterialType.COPPER);

        // Steel
        Steel_Ingot = registerMaterialItem(MaterialType.STEEL, "ingot");
        Steel_Plate = registerPlate(MaterialType.STEEL);
        Steel_Wire = registerWire(MaterialType.STEEL);
        Steel_Coil = registerCoil(MaterialType.STEEL);

        // Dusts
        Coal_Dust = ITEMS.register("coal_dust", () -> new Item(new Item.Properties()));
        Iron_Dust = ITEMS.register("iron_dust", () -> new Item(new Item.Properties()));
        Steel_Dust = ITEMS.register("steel_dust", () -> new Item(new Item.Properties()));

        Forge_Book = ITEMS.register("forge_book", () -> new ForgeBook(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).setNoRepair()));

        Forge_Hammer = ITEMS.register("forge_hammer", () ->
                new HammerItem(Tiers.IRON, FORGE_HAMMER_MINING_SIZE, new Item.Properties()
                        .component(DataComponents.MAX_DAMAGE, FORGE_HAMMER_DURABILITY)
                        .attributes(DiggerItem.createAttributes(Tiers.IRON, FORGE_HAMMER_ATTACK_DAMAGE, FORGE_HAMMER_ATTACK_DAMAGE_MODIFIER))
                )
        );

        Cutter = ITEMS.register("cutter", () ->
                new DamageOnCraftUseItem(new Item.Properties()
                        .component(DataComponents.MAX_DAMAGE, CUTTER_DURABILITY)
                ){
                    @Override
                    public boolean isRepairable(@Nonnull ItemStack stack) {
                        return false; // запрещаем ремонт, чар на починку не отключает
                    }
                }
        );

        // Smelter upgrades
        Capacity_Upgrade_T1 = ITEMS.register("capacity_upgrade_t1", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.CAPACITY_BOOST, 1, new Item.Properties()));
        Capacity_Upgrade_T2 = ITEMS.register("capacity_upgrade_t2", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.CAPACITY_BOOST, 2, new Item.Properties()));
        Capacity_Upgrade_T3 = ITEMS.register("capacity_upgrade_t3", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.CAPACITY_BOOST, 3, new Item.Properties()));
        
        Temp_Upgrade_T1 = ITEMS.register("temp_upgrade_t1", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.TEMP_BOOST, 1, new Item.Properties()));
        Temp_Upgrade_T2 = ITEMS.register("temp_upgrade_t2", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.TEMP_BOOST, 2, new Item.Properties()));
        Temp_Upgrade_T3 = ITEMS.register("temp_upgrade_t3", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.TEMP_BOOST, 3, new Item.Properties()));
        
        Efficiency_Upgrade_T1 = ITEMS.register("efficiency_upgrade_t1", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.EFFICIENCY, 1, new Item.Properties()));
        Efficiency_Upgrade_T2 = ITEMS.register("efficiency_upgrade_t2", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.EFFICIENCY, 2, new Item.Properties()));
        Efficiency_Upgrade_T3 = ITEMS.register("efficiency_upgrade_t3", () -> 
            new SmelterUpgradeItem(AlloySmelterBlockEntity.UpgradeType.EFFICIENCY, 3, new Item.Properties()));
    }

    /**
     * Регистрирует все предметы в моде на шине событий.
     * @param bus шина событий NeoForge
     */
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}