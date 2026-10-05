package com.plainoleg.techcraft.registry;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.block.alloysmelter.AlloySmelterBlockEntity;
import com.plainoleg.techcraft.item.ForgeBook;
import com.plainoleg.techcraft.item.PlateItem;
import com.plainoleg.techcraft.item.SmelterUpgradeItem;
import com.plainoleg.techcraft.item.armor.QuantumArmorItem;
import com.plainoleg.techcraft.item.tool.DamageOnCraftUseItem;
import com.plainoleg.techcraft.item.tool.DrillItem;
import com.plainoleg.techcraft.item.tool.HammerItem;
import com.plainoleg.techcraft.lumenmesh.registry.LumenItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    /** Обычный стакающийся материал (до 64 в стаке). */
    private static DeferredItem<Item> registerMaterial(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    /** Предмет, который не стакается (батареи, инструменты-заглушки). */
    private static DeferredItem<Item> registerUnstackable(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(1)));
    }

    private static DeferredItem<DrillItem> registerDrill(String name, Tier tier, int maxDamage, int radius, int speed) {
        return ITEMS.register(name, () -> new DrillItem(tier, maxDamage, radius, speed, new Item.Properties().stacksTo(1)));
    }

    private static DeferredItem<SmelterUpgradeItem> registerUpgrade(AlloySmelterBlockEntity.UpgradeType type, int tier) {
        return ITEMS.register(type.getName() + "_upgrade_t" + tier, () ->
            new SmelterUpgradeItem(type, tier, new Item.Properties())
        );
    }

    public static final DeferredItem<Item> RAW_TIN;
    public static final DeferredItem<Item> TIN_INGOT;
    public static final DeferredItem<Item> STEEL_INGOT;
    public static final DeferredItem<Item> RAW_URANIUM;
    public static final DeferredItem<Item> ENRICHED_URANIUM;
    public static final DeferredItem<Item> ENRICHED_URANIUM_INGOT;
    public static final DeferredItem<Item> RAW_COBALT;
    public static final DeferredItem<Item> COBALT_INGOT;
    public static final DeferredItem<Item> RAW_TITANIUM;
    public static final DeferredItem<Item> TITANIUM_INGOT;
    public static final DeferredItem<Item> AETHERIUM_INGOT;
    public static final DeferredItem<Item> SOLARITE_INGOT;
    public static final DeferredItem<Item> RAW_ORICHALCUM;
    public static final DeferredItem<Item> ORICHALCUM_INGOT;

    public static final DeferredItem<Item> RAW_RUBBER;
    public static final DeferredItem<Item> RUBBER;

    public static final DeferredItem<Item> COAL_DUST;
    public static final DeferredItem<Item> IRON_DUST;
    public static final DeferredItem<Item> NICKEL_DUST;
    public static final DeferredItem<Item> STEEL_DUST;

    public static final DeferredItem<Item> TIN_PLATE;
    public static final DeferredItem<Item> IRON_PLATE;
    public static final DeferredItem<Item> GOLD_PLATE;
    public static final DeferredItem<Item> COPPER_PLATE;
    public static final DeferredItem<Item> STEEL_PLATE;
    public static final DeferredItem<Item> BRONZE_PLATE;
    public static final DeferredItem<Item> NICKEL_PLATE;
    public static final DeferredItem<Item> SILVER_PLATE;
    public static final DeferredItem<Item> PRISMITE_PLATE;
    public static final DeferredItem<Item> QUANTUM_PLATE;

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

    /** @deprecated предмет регистрируется в {@link LumenItems}; оставлено для совместимости. */
    @Deprecated
    public static final DeferredItem<Item> REFINED_PHASE_QUARTZ = LumenItems.REFINED_PHASE_QUARTZ;

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

    public static final DeferredItem<Item> PRISMITE_AXE;
    public static final DeferredItem<Item> PRISMITE_BOW;
    public static final DeferredItem<Item> PRISMITE_HOE;
    public static final DeferredItem<Item> PRISMITE_PICKAXE;
    public static final DeferredItem<Item> PRISMITE_SHOVEL;
    public static final DeferredItem<Item> PRISMITE_SWORD;
    public static final DeferredItem<Item> QUANTUM_AXE;
    public static final DeferredItem<Item> QUANTUM_BOW;
    public static final DeferredItem<Item> QUANTUM_HOE;
    public static final DeferredItem<Item> QUANTUM_PICKAXE;
    public static final DeferredItem<Item> QUANTUM_SHOVEL;
    public static final DeferredItem<Item> QUANTUM_SWORD;
    public static final DeferredItem<Item> QUANTUM_TRUE_SWORD;

    public static final DeferredItem<Item> PRISMITE_HELMET;
    public static final DeferredItem<Item> PRISMITE_CHESTPLATE;
    public static final DeferredItem<Item> PRISMITE_LEGGINGS;
    public static final DeferredItem<Item> PRISMITE_BOOTS;

    public static final DeferredItem<Item> QUANTUM_HELMET;
    public static final DeferredItem<Item> QUANTUM_CHESTPLATE;
    public static final DeferredItem<Item> QUANTUM_LEGGINGS;
    public static final DeferredItem<Item> QUANTUM_BOOTS;

    static {
        RAW_TIN = registerMaterial("raw_tin");
        TIN_INGOT = registerMaterialItem(MaterialType.TIN, "ingot");
        TIN_PLATE = registerPlate(MaterialType.TIN);
        RAW_URANIUM = registerMaterial("raw_uranium");
        ENRICHED_URANIUM = registerMaterial("enriched_uranium");
        ENRICHED_URANIUM_INGOT = registerMaterial("enriched_uranium_ingot");
        RAW_COBALT = registerMaterial("raw_cobalt");
        COBALT_INGOT = registerMaterial("cobalt_ingot");
        RAW_TITANIUM = registerMaterial("raw_titanium");
        TITANIUM_INGOT = registerMaterial("titanium_ingot");
        AETHERIUM_INGOT = ITEMS.register("aetherium_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
        SOLARITE_INGOT = ITEMS.register("solarite_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
        RAW_ORICHALCUM = ITEMS.register("raw_orichalcum", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
        ORICHALCUM_INGOT = ITEMS.register("orichalcum_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

        IRON_PLATE = registerPlate(MaterialType.IRON);
        GOLD_PLATE = registerPlate(MaterialType.GOLD);
        COPPER_PLATE = registerPlate(MaterialType.COPPER);
        BRONZE_PLATE = registerPlate(MaterialType.BRONZE);
        NICKEL_PLATE = registerPlate(MaterialType.NICKEL);
        SILVER_PLATE = registerPlate(MaterialType.SILVER);
        PRISMITE_PLATE = registerPlate(MaterialType.PRISMITE);
        QUANTUM_PLATE = registerPlate(MaterialType.QUANTUM);

        STEEL_INGOT = registerMaterialItem(MaterialType.STEEL, "ingot");
        STEEL_PLATE = registerPlate(MaterialType.STEEL);

        COAL_DUST = registerMaterial("coal_dust");
        IRON_DUST = registerMaterial("iron_dust");
        NICKEL_DUST = registerMaterial("nickel_dust");
        STEEL_DUST = registerMaterial("steel_dust");

        RAW_RUBBER = registerMaterial("raw_rubber");
        RUBBER = registerMaterial("rubber");

        BRONZE_INGOT = registerMaterial("bronze_ingot");
        NICKEL_INGOT = registerMaterial("nickel_ingot");
        SILVER_INGOT = registerMaterial("silver_ingot");
        PRISMITE_INGOT = registerMaterial("prismite_ingot");
        QUANTUM_INGOT = registerMaterial("quantum_ingot");

        BRONZE_CABLE = registerMaterial("bronze_cable");
        COPPER_CABLE = registerMaterial("copper_cable");
        GOLD_CABLE = registerMaterial("gold_cable");
        IRON_CABLE = registerMaterial("iron_cable");
        NICKEL_CABLE = registerMaterial("nickel_cable");
        SILVER_CABLE = registerMaterial("silver_cable");
        STEEL_CABLE = registerMaterial("steel_cable");
        TIN_CABLE = registerMaterial("tin_cable");
        PRISMITE_CABLE = registerMaterial("prismite_cable");
        QUANTUM_CABLE = registerMaterial("quantum_cable");

        CIRCUIT = registerMaterial("circuit");
        RESISTOR = registerMaterial("resistor");
        TRANSISTOR = registerMaterial("transistor");
        MAGNET = registerMaterial("magnet");
        MOTOR = registerMaterial("motor");

        BATTERY = registerUnstackable("battery");
        ACCUMULATOR = registerUnstackable("accumulator");
        QUANTUM_BATTERY = registerUnstackable("quantum_battery");
        ENERGY_CRYSTAL = registerUnstackable("energy_crystal");

        CUT_RUBY = registerMaterial("cut_ruby");
        FLAWLESS_RUBY = registerMaterial("flawless_ruby");
        PERFECT_RUBY = registerMaterial("perfect_ruby");
        POLISHED_RUBY = registerMaterial("polished_ruby");
        RUBY_SHARD = registerMaterial("ruby_shard");

        BLUE_PLASMA_CORE = registerMaterial("blue_plasma_core");
        VIOLET_PLASMA_CORE = registerMaterial("violet_plasma_core");
        RAW_BLUE_CORE = registerMaterial("raw_blue_core");
        RAW_VIOLET_CORE = registerMaterial("raw_violet_core");

        FORGE_BOOK = ITEMS.register("forge_book", () -> new ForgeBook(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).setNoRepair()));

        FORGE_HAMMER = ITEMS.register("forge_hammer", () ->
            new HammerItem(Tiers.IRON, FORGE_HAMMER_MINING_SIZE, new Item.Properties()
                .stacksTo(1)
                .component(DataComponents.MAX_DAMAGE, FORGE_HAMMER_DURABILITY)
                .attributes(DiggerItem.createAttributes(Tiers.IRON, FORGE_HAMMER_ATTACK_DAMAGE, FORGE_HAMMER_ATTACK_DAMAGE_MODIFIER))
            )
        );

        PRISMITE_AXE = registerUnstackable("prismite_axe");
        PRISMITE_BOW = registerUnstackable("prismite_bow");
        PRISMITE_HOE = registerUnstackable("prismite_hoe");
        PRISMITE_PICKAXE = registerUnstackable("prismite_pickaxe");
        PRISMITE_SHOVEL = registerUnstackable("prismite_shovel");
        PRISMITE_SWORD = registerUnstackable("prismite_sword");
        QUANTUM_AXE = registerUnstackable("quantum_axe");
        QUANTUM_BOW = registerUnstackable("quantum_bow");
        QUANTUM_HOE = registerUnstackable("quantum_hoe");
        QUANTUM_PICKAXE = registerUnstackable("quantum_pickaxe");
        QUANTUM_SHOVEL = registerUnstackable("quantum_shovel");
        QUANTUM_SWORD = registerUnstackable("quantum_sword");
        QUANTUM_TRUE_SWORD = registerUnstackable("quantum_true_sword");

        CUTTER = ITEMS.register("cutter", () ->
            new DamageOnCraftUseItem(new Item.Properties().stacksTo(1).component(DataComponents.MAX_DAMAGE, CUTTER_DURABILITY)) {
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

        PRISMITE_HELMET = ITEMS.register("prismite_helmet", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
        PRISMITE_CHESTPLATE = ITEMS.register("prismite_chestplate", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
        PRISMITE_LEGGINGS = ITEMS.register("prismite_leggings", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
        PRISMITE_BOOTS = ITEMS.register("prismite_boots", () -> new ArmorItem(ModArmorMaterials.PRISMITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

        QUANTUM_HELMET = ITEMS.register("quantum_helmet", () -> new QuantumArmorItem(ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        QUANTUM_CHESTPLATE = ITEMS.register("quantum_chestplate", () -> new QuantumArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        QUANTUM_LEGGINGS = ITEMS.register("quantum_leggings", () -> new QuantumArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        QUANTUM_BOOTS = ITEMS.register("quantum_boots", () -> new QuantumArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
