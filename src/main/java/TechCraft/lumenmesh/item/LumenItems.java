package TechCraft.lumenmesh.item;

import TechCraft.TechCraft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Регистрация предметов Lumen Mesh.
 */
public class LumenItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    // Материалы
    public static final DeferredItem<Item> PHASE_QUARTZ;
    public static final DeferredItem<Item> REFINED_PHASE_QUARTZ;
    public static final DeferredItem<Item> ETCHED_SILICON;
    public static final DeferredItem<Item> LOGIC_WAFER;
    public static final DeferredItem<Item> CALCULATION_WAFER;
    public static final DeferredItem<Item> ENGINEERING_WAFER;
    public static final DeferredItem<Item> COHERENCE_CORE;
    public static final DeferredItem<Item> NETWORK_PROCESSOR;

    // Призмы хранения
    public static final DeferredItem<Item> BLANK_PRISM;
    public static final DeferredItem<Item> STORAGE_PRISM_1K;
    public static final DeferredItem<Item> STORAGE_PRISM_4K;
    public static final DeferredItem<Item> STORAGE_PRISM_16K;
    public static final DeferredItem<Item> STORAGE_PRISM_64K;

    // Чертежи
    public static final DeferredItem<Item> BLANK_BLUEPRINT;
    public static final DeferredItem<Item> ENCODED_BLUEPRINT;

    // Утилиты
    public static final DeferredItem<Item> WIRELESS_TERMINAL;
    public static final DeferredItem<Item> NETWORK_PROBE;
    public static final DeferredItem<Item> LINK_CARD;
    public static final DeferredItem<Item> IDENTITY_KEY;
    public static final DeferredItem<Item> ACCELERATION_MODULE;

    static {
        // Материалы
        PHASE_QUARTZ = ITEMS.register("phase_quartz", () -> new Item(new Item.Properties()));
        REFINED_PHASE_QUARTZ = ITEMS.register("refined_phase_quartz", () -> new Item(new Item.Properties()));
        ETCHED_SILICON = ITEMS.register("etched_silicon", () -> new Item(new Item.Properties()));
        LOGIC_WAFER = ITEMS.register("logic_wafer", () -> new Item(new Item.Properties()));
        CALCULATION_WAFER = ITEMS.register("calculation_wafer", () -> new Item(new Item.Properties()));
        ENGINEERING_WAFER = ITEMS.register("engineering_wafer", () -> new Item(new Item.Properties()));
        COHERENCE_CORE = ITEMS.register("coherence_core", () -> new Item(new Item.Properties()));
        NETWORK_PROCESSOR = ITEMS.register("network_processor", () -> new Item(new Item.Properties()));

        // Призмы хранения
        BLANK_PRISM = ITEMS.register("blank_prism", () -> new Item(new Item.Properties().stacksTo(1)));
        STORAGE_PRISM_1K = ITEMS.register("storage_prism_1k", () -> new Item(new Item.Properties().stacksTo(1)));
        STORAGE_PRISM_4K = ITEMS.register("storage_prism_4k", () -> new Item(new Item.Properties().stacksTo(1)));
        STORAGE_PRISM_16K = ITEMS.register("storage_prism_16k", () -> new Item(new Item.Properties().stacksTo(1)));
        STORAGE_PRISM_64K = ITEMS.register("storage_prism_64k", () -> new Item(new Item.Properties().stacksTo(1)));

        // Чертежи
        BLANK_BLUEPRINT = ITEMS.register("blank_blueprint", () -> new Item(new Item.Properties().stacksTo(1)));
        ENCODED_BLUEPRINT = ITEMS.register("encoded_blueprint", () -> new Item(new Item.Properties().stacksTo(1)));

        // Утилиты
        WIRELESS_TERMINAL = ITEMS.register("wireless_terminal", () -> new Item(new Item.Properties().stacksTo(1)));
        NETWORK_PROBE = ITEMS.register("network_probe", () -> new Item(new Item.Properties().stacksTo(1)));
        LINK_CARD = ITEMS.register("link_card", () -> new Item(new Item.Properties().stacksTo(1)));
        IDENTITY_KEY = ITEMS.register("identity_key", () -> new Item(new Item.Properties().stacksTo(1)));
        ACCELERATION_MODULE = ITEMS.register("acceleration_module", () -> new Item(new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
