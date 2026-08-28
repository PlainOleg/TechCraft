package TechCraft.item;

import TechCraft.TechCraft;
import TechCraft.block.AlloySmelterBlockEntity;
import TechCraft.item.custom.DamageOnCraftUseItem;
import TechCraft.item.custom.DrillItem;
import TechCraft.item.custom.HammerItem;
import TechCraft.item.custom.PlateItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nonnull;

public class ModItems {
    private static final int FORGE_HAMMER_DURABILITY = 250;
    private static final int CUTTER_DURABILITY = 180;
    private static final float FORGE_HAMMER_ATTACK_DAMAGE = 7f;
    private static final float FORGE_HAMMER_ATTACK_DAMAGE_MODIFIER = -3.5f;
    private static final int FORGE_HAMMER_MINING_SIZE = 3;

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    private static DeferredItem<Item> registerMaterialItem(MaterialType material, String itemType) {
        return ITEMS.register(material.getName() + "_" + itemType, () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<Item> registerPlate(MaterialType material) {
        return ITEMS.register(material.getName() + "_plate", () -> new PlateItem(new Item.Properties()));
    }

    private static DeferredItem<Item> registerSimpleItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<DrillItem> registerDrill(String name, Tier tier, int maxDamage, int radius, int speed) {
        return ITEMS.register(name, () -> new DrillItem(tier, maxDamage, radius, speed, new Item.Properties()));
    }

    private static DeferredItem<SmelterUpgradeItem> registerUpgrade(AlloySmelterBlockEntity.UpgradeType type, int tier) {
        return ITEMS.register(type.getName() + "_upgrade_t" + tier, () ->
            new SmelterUpgradeItem(type, tier, new Item.Properties())
        );
    }

    public static final DeferredItem<Item> RAW_TIN;
    public static final DeferredItem<Item> TIN_INGOT;
    public static final DeferredItem<Item> STEEL_INGOT;

    public static final DeferredItem<Item> RAW_RUBBER;
    public static final DeferredItem<Item> RUBBER;

    public static final DeferredItem<Item> COAL_DUST;
    public static final DeferredItem<Item> IRON_DUST;
    public static final DeferredItem<Item> STEEL_DUST;

    public static final DeferredItem<Item> TIN_PLATE;
    public static final DeferredItem<Item> IRON_PLATE;
    public static final DeferredItem<Item> GOLD_PLATE;
    public static final DeferredItem<Item> COPPER_PLATE;
    public static final DeferredItem<Item> STEEL_PLATE;

    public static final DeferredItem<Item> BRONZE_INGOT;
    public static final DeferredItem<Item> NICKEL_INGOT;
    public static final DeferredItem<Item> SILVER_INGOT;
    public static final DeferredItem<Item> PRISMITE_INGOT;
    public static final DeferredItem<Item> QUANTUM_INGOT;

    public static final DeferredItem<Item> BRONZE_CABLE;
    public static final DeferredItem<Item> COPPER_CABLE;
    public static final DeferredItem<Item> GOLD_CABLE;
    public static final DeferredItem<Item> IRON_CABLE;
    public static final DeferredItem<Item> NICKEL_CABLE;
    public static final DeferredItem<Item> SILVER_CABLE;
    public static final DeferredItem<Item> STEEL_CABLE;
    public static final DeferredItem<Item> TIN_CABLE;
    public static final DeferredItem<Item> PRISMITE_CABLE;
    public static final DeferredItem<Item> QUANTUM_CABLE;

    public static final DeferredItem<Item> CIRCUIT;
    public static final DeferredItem<Item> RESISTOR;
    public static final DeferredItem<Item> TRANSISTOR;
    public static final DeferredItem<Item> MAGNET;
    public static final DeferredItem<Item> MOTOR;

    public static final DeferredItem<Item> BATTERY;
    public static final DeferredItem<Item> ACCUMULATOR;
    public static final DeferredItem<Item> QUANTUM_BATTERY;
    public static final DeferredItem<Item> ENERGY_CRYSTAL;

    public static final DeferredItem<Item> CUT_RUBY;
    public static final DeferredItem<Item> FLAWLESS_RUBY;
    public static final DeferredItem<Item> PERFECT_RUBY;
    public static final DeferredItem<Item> POLISHED_RUBY;
    public static final DeferredItem<Item> RUBY_SHARD;

    public static final DeferredItem<Item> BLUE_PLASMA_CORE;
    public static final DeferredItem<Item> VIOLET_PLASMA_CORE;
    public static final DeferredItem<Item> RAW_BLUE_CORE;
    public static final DeferredItem<Item> RAW_VIOLET_CORE;

    public static final DeferredItem<Item> FORGE_BOOK;
    public static final DeferredItem<HammerItem> FORGE_HAMMER;
    public static final DeferredItem<Item> CUTTER;

    public static final DeferredItem<DrillItem> IRON_DRILL;
    public static final DeferredItem<DrillItem> DIAMOND_DRILL;
    public static final DeferredItem<DrillItem> NETHERITE_DRILL;
    public static final DeferredItem<DrillItem> QUANTUM_DRILL;

    public static final DeferredItem<SmelterUpgradeItem> CAPACITY_UPGRADE_T2;
    public static final DeferredItem<SmelterUpgradeItem> CAPACITY_UPGRADE_T3;
    public static final DeferredItem<SmelterUpgradeItem> TEMP_UPGRADE_T2;
    public static final DeferredItem<SmelterUpgradeItem> TEMP_UPGRADE_T3;
    public static final DeferredItem<SmelterUpgradeItem> EFFICIENCY_UPGRADE_T2;
    public static final DeferredItem<SmelterUpgradeItem> EFFICIENCY_UPGRADE_T3;

    public static final DeferredItem<Item> PRISMITE_HELMET;
    public static final DeferredItem<Item> PRISMITE_CHESTPLATE;
    public static final DeferredItem<Item> PRISMITE_LEGGINGS;
    public static final DeferredItem<Item> PRISMITE_BOOTS;

    public static final DeferredItem<Item> QUANTUM_HELMET;
    public static final DeferredItem<Item> QUANTUM_CHESTPLATE;
    public static final DeferredItem<Item> QUANTUM_LEGGINGS;
    public static final DeferredItem<Item> QUANTUM_BOOTS;

    static {
        RAW_TIN = registerMaterialItem(MaterialType.TIN, "raw");
        TIN_INGOT = registerMaterialItem(MaterialType.TIN, "ingot");
        TIN_PLATE = registerPlate(MaterialType.TIN);

        IRON_PLATE = registerPlate(MaterialType.IRON);
        GOLD_PLATE = registerPlate(MaterialType.GOLD);
        COPPER_PLATE = registerPlate(MaterialType.COPPER);

        STEEL_INGOT = registerMaterialItem(MaterialType.STEEL, "ingot");
        STEEL_PLATE = registerPlate(MaterialType.STEEL);

        COAL_DUST = registerSimpleItem("coal_dust");
        IRON_DUST = registerSimpleItem("iron_dust");
        STEEL_DUST = registerSimpleItem("steel_dust");

        RAW_RUBBER = registerSimpleItem("raw_rubber");
        RUBBER = registerSimpleItem("rubber");

        BRONZE_INGOT = registerSimpleItem("bronze_ingot");
        NICKEL_INGOT = registerSimpleItem("nickel_ingot");
        SILVER_INGOT = registerSimpleItem("silver_ingot");
        PRISMITE_INGOT = registerSimpleItem("prismite_ingot");
        QUANTUM_INGOT = registerSimpleItem("quantum_ingot");

        BRONZE_CABLE = registerSimpleItem("bronze_cable");
        COPPER_CABLE = registerSimpleItem("copper_cable");
        GOLD_CABLE = registerSimpleItem("gold_cable");
        IRON_CABLE = registerSimpleItem("iron_cable");
        NICKEL_CABLE = registerSimpleItem("nickel_cable");
        SILVER_CABLE = registerSimpleItem("silver_cable");
        STEEL_CABLE = registerSimpleItem("steel_cable");
        TIN_CABLE = registerSimpleItem("tin_cable");
        PRISMITE_CABLE = registerSimpleItem("prismite_cable");
        QUANTUM_CABLE = registerSimpleItem("quantum_cable");

        CIRCUIT = registerSimpleItem("circuit");
        RESISTOR = registerSimpleItem("resistor");
        TRANSISTOR = registerSimpleItem("transistor");
        MAGNET = registerSimpleItem("magnet");
        MOTOR = registerSimpleItem("motor");

        BATTERY = registerSimpleItem("battery");
        ACCUMULATOR = registerSimpleItem("accumulator");
        QUANTUM_BATTERY = registerSimpleItem("quantum_battery");
        ENERGY_CRYSTAL = registerSimpleItem("energy_crystal");

        CUT_RUBY = registerSimpleItem("cut_ruby");
        FLAWLESS_RUBY = registerSimpleItem("flawless_ruby");
        PERFECT_RUBY = registerSimpleItem("perfect_ruby");
        POLISHED_RUBY = registerSimpleItem("polished_ruby");
        RUBY_SHARD = registerSimpleItem("ruby_shard");

        BLUE_PLASMA_CORE = registerSimpleItem("blue_plasma_core");
        VIOLET_PLASMA_CORE = registerSimpleItem("violet_plasma_core");
        RAW_BLUE_CORE = registerSimpleItem("raw_blue_core");
        RAW_VIOLET_CORE = registerSimpleItem("raw_violet_core");

        FORGE_BOOK = ITEMS.register("forge_book", () -> new ForgeBook(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).setNoRepair()));

        FORGE_HAMMER = ITEMS.register("forge_hammer", () ->
            new HammerItem(Tiers.IRON, FORGE_HAMMER_MINING_SIZE, new Item.Properties()
                .component(DataComponents.MAX_DAMAGE, FORGE_HAMMER_DURABILITY)
                .attributes(DiggerItem.createAttributes(Tiers.IRON, FORGE_HAMMER_ATTACK_DAMAGE, FORGE_HAMMER_ATTACK_DAMAGE_MODIFIER))
            )
        );

        CUTTER = ITEMS.register("cutter", () ->
            new DamageOnCraftUseItem(new Item.Properties().component(DataComponents.MAX_DAMAGE, CUTTER_DURABILITY)) {
                @Override
                public boolean isRepairable(@Nonnull ItemStack stack) {
                    return false;
                }
            }
        );

        IRON_DRILL = registerDrill("iron_drill", Tiers.IRON, 1700, 3, 10);
        DIAMOND_DRILL = registerDrill("diamond_drill", Tiers.DIAMOND, 5000, 3, 8);
        NETHERITE_DRILL = registerDrill("netherite_drill", Tiers.NETHERITE, 10000, 5, 6);
        QUANTUM_DRILL = registerDrill("quantum_drill", Tiers.NETHERITE, 35000, 7, 4);

        CAPACITY_UPGRADE_T2 = registerUpgrade(AlloySmelterBlockEntity.UpgradeType.CAPACITY_BOOST, 2);
        CAPACITY_UPGRADE_T3 = registerUpgrade(AlloySmelterBlockEntity.UpgradeType.CAPACITY_BOOST, 3);
        TEMP_UPGRADE_T2 = registerUpgrade(AlloySmelterBlockEntity.UpgradeType.TEMP_BOOST, 2);
        TEMP_UPGRADE_T3 = registerUpgrade(AlloySmelterBlockEntity.UpgradeType.TEMP_BOOST, 3);
        EFFICIENCY_UPGRADE_T2 = registerUpgrade(AlloySmelterBlockEntity.UpgradeType.EFFICIENCY, 2);
        EFFICIENCY_UPGRADE_T3 = registerUpgrade(AlloySmelterBlockEntity.UpgradeType.EFFICIENCY, 3);

        PRISMITE_HELMET = ITEMS.register("prismite_helmet", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.HELMET, new Item.Properties()));
        PRISMITE_CHESTPLATE = ITEMS.register("prismite_chestplate", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
        PRISMITE_LEGGINGS = ITEMS.register("prismite_leggings", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
        PRISMITE_BOOTS = ITEMS.register("prismite_boots", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.BOOTS, new Item.Properties()));

        QUANTUM_HELMET = ITEMS.register("quantum_helmet", () -> new ArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC)));
        QUANTUM_CHESTPLATE = ITEMS.register("quantum_chestplate", () -> new ArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC)));
        QUANTUM_LEGGINGS = ITEMS.register("quantum_leggings", () -> new ArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC)));
        QUANTUM_BOOTS = ITEMS.register("quantum_boots", () -> new ArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.EPIC)));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}