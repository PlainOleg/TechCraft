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

    // Процессоры
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_1;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_4;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_8;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_16;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_32;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_64;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_128;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_256;
    public static final DeferredItem<LumenProcessorItem> PROCESSOR_512;

    // Призмы хранения
    public static final DeferredItem<Item> STORAGE_PRISM_1K;
    public static final DeferredItem<Item> STORAGE_PRISM_2K;
    public static final DeferredItem<Item> STORAGE_PRISM_4K;
    public static final DeferredItem<Item> STORAGE_PRISM_8K;
    public static final DeferredItem<Item> STORAGE_PRISM_16K;
    public static final DeferredItem<Item> STORAGE_PRISM_32K;
    public static final DeferredItem<Item> STORAGE_PRISM_64K;
    public static final DeferredItem<Item> STORAGE_PRISM_128K;
    public static final DeferredItem<Item> STORAGE_PRISM_256K;
    public static final DeferredItem<Item> STORAGE_PRISM_512K;
    public static final DeferredItem<Item> STORAGE_PRISM_1024K;
    public static final DeferredItem<Item> STORAGE_PRISM_2048K;
    public static final DeferredItem<Item> STORAGE_PRISM_4096K;
    public static final DeferredItem<Item> STORAGE_PRISM_8192K;
    public static final DeferredItem<Item> STORAGE_PRISM_16384K;
    public static final DeferredItem<Item> QUANTUM_STORAGE_PRISM;

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

        // Процессоры
        PROCESSOR_1 = registerProcessor(1, 1);
        PROCESSOR_4 = registerProcessor(4, 2);
        PROCESSOR_8 = registerProcessor(8, 3);
        PROCESSOR_16 = registerProcessor(16, 4);
        PROCESSOR_32 = registerProcessor(32, 5);
        PROCESSOR_64 = registerProcessor(64, 6);
        PROCESSOR_128 = registerProcessor(128, 7);
        PROCESSOR_256 = registerProcessor(256, 8);
        PROCESSOR_512 = registerProcessor(512, 9);

        // Призмы хранения
        STORAGE_PRISM_1K = ITEMS.register("storage_prism_1k", () -> new StorageMediumItem(new Item.Properties(), 1024, 32, 2, 0));
        STORAGE_PRISM_2K = ITEMS.register("storage_prism_2k", () -> new StorageMediumItem(new Item.Properties(), 2048, 64, 4, 0));
        STORAGE_PRISM_4K = ITEMS.register("storage_prism_4k", () -> new StorageMediumItem(new Item.Properties(), 4096, 64, 4, 0));
        STORAGE_PRISM_8K = ITEMS.register("storage_prism_8k", () -> new StorageMediumItem(new Item.Properties(), 8192, 128, 8, 0));
        STORAGE_PRISM_16K = ITEMS.register("storage_prism_16k", () -> new StorageMediumItem(new Item.Properties(), 16384, 128, 8, 0));
        STORAGE_PRISM_32K = ITEMS.register("storage_prism_32k", () -> new StorageMediumItem(new Item.Properties(), 32768, 256, 16, 0));
        STORAGE_PRISM_64K = ITEMS.register("storage_prism_64k", () -> new StorageMediumItem(new Item.Properties(), 65536, 256, 16, 0));
        STORAGE_PRISM_128K = registerStoragePrism(128);
        STORAGE_PRISM_256K = registerStoragePrism(256);
        STORAGE_PRISM_512K = registerStoragePrism(512);
        STORAGE_PRISM_1024K = registerStoragePrism(1024);
        STORAGE_PRISM_2048K = registerStoragePrism(2048);
        STORAGE_PRISM_4096K = registerStoragePrism(4096);
        STORAGE_PRISM_8192K = registerStoragePrism(8192);
        STORAGE_PRISM_16384K = registerStoragePrism(16384);
        QUANTUM_STORAGE_PRISM = ITEMS.register("quantum_storage_prism", () -> new StorageMediumItem(new Item.Properties(), Long.MAX_VALUE, 256, 50, 16));

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

    private static DeferredItem<LumenProcessorItem> registerProcessor(int power, int tier) {
        return ITEMS.register("processor_" + power,
                () -> new LumenProcessorItem(new Item.Properties(), power, tier));
    }

    private static DeferredItem<Item> registerStoragePrism(int capacityK) {
        return ITEMS.register("storage_prism_" + capacityK + "k",
                () -> new StorageMediumItem(new Item.Properties(), (long) capacityK * 1024, 256, 16, 0));
    }
}
