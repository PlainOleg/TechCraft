package TechCraft.lumenmesh.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Конфигурация системы Lumen Mesh.
 * Все параметры настраиваются на стороне сервера.
 */
public class LumenMeshConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Ёмкость призм хранения
    public static final ModConfigSpec.IntValue PRISM_1K_CAPACITY;
    public static final ModConfigSpec.IntValue PRISM_4K_CAPACITY;
    public static final ModConfigSpec.IntValue PRISM_16K_CAPACITY;
    public static final ModConfigSpec.IntValue PRISM_64K_CAPACITY;

    // Лимиты типов предметов в призмах
    public static final ModConfigSpec.IntValue PRISM_1K_TYPE_LIMIT;
    public static final ModConfigSpec.IntValue PRISM_4K_TYPE_LIMIT;
    public static final ModConfigSpec.IntValue PRISM_16K_TYPE_LIMIT;
    public static final ModConfigSpec.IntValue PRISM_64K_TYPE_LIMIT;

    // Энергия
    public static final ModConfigSpec.IntValue BASE_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue MESH_CORE_BANDWIDTH;
    public static final ModConfigSpec.IntValue NETWORK_PROCESSOR_BANDWIDTH;
    public static final ModConfigSpec.IntValue ENERGY_BRIDGE_TRANSFER_RATE;

    // Пропускная способность
    public static final ModConfigSpec.IntValue OPERATION_QUEUE_SIZE;
    public static final ModConfigSpec.IntValue TRAFFIC_OVERLOAD_DELAY_TICKS;

    // Когерентность
    public static final ModConfigSpec.IntValue COHERENCE_NO_PENALTY_DISTANCE;
    public static final ModConfigSpec.IntValue COHERENCE_PENALLY_SEGMENT_SIZE;
    public static final ModConfigSpec.IntValue COHERENCE_PENALLY_DELAY_TICKS;
    public static final ModConfigSpec.IntValue DIMENSIONAL_CROSSING_PENALTY;
    public static final ModConfigSpec.IntValue STABILIZER_BONUS;

    // Беспроводная связь
    public static final ModConfigSpec.IntValue WIRELESS_BASE_RANGE;
    public static final ModConfigSpec.IntValue WIRELESS_TERMINAL_RANGE;

    // Логистика
    public static final ModConfigSpec.IntValue IMPORT_EXPORT_SPEED;
    public static final ModConfigSpec.IntValue IMPORT_EXPORT_BATCH_SIZE;

    // Автокрафт
    public static final ModConfigSpec.IntValue AUTOCRAFT_MAX_DEPTH;
    public static final ModConfigSpec.IntValue AUTOCRAFT_MAX_JOBS;
    public static final ModConfigSpec.IntValue FABRICATOR_SPEED;

    // Квантовая связь
    public static final ModConfigSpec.IntValue QUANTUM_ENERGY_COST;
    public static final ModConfigSpec.IntValue QUANTUM_TRAFFIC_COST;
    public static final ModConfigSpec.BooleanValue QUANTUM_CHUNK_LOADING;

    // Конденсация материи
    public static final ModConfigSpec.IntValue MATTER_CONDENSATION_RATIO;

    static {
        BUILDER.push("Prism Storage");
        PRISM_1K_CAPACITY = BUILDER
            .comment("Ёмкость призмы 1K")
            .defineInRange("prism_1k_capacity", 1024, 1, Integer.MAX_VALUE);
        PRISM_4K_CAPACITY = BUILDER
            .comment("Ёмкость призмы 4K")
            .defineInRange("prism_4k_capacity", 4096, 1, Integer.MAX_VALUE);
        PRISM_16K_CAPACITY = BUILDER
            .comment("Ёмкость призмы 16K")
            .defineInRange("prism_16k_capacity", 16384, 1, Integer.MAX_VALUE);
        PRISM_64K_CAPACITY = BUILDER
            .comment("Ёмкость призмы 64K")
            .defineInRange("prism_64k_capacity", 65536, 1, Integer.MAX_VALUE);

        PRISM_1K_TYPE_LIMIT = BUILDER
            .comment("Лимит типов предметов в призме 1K")
            .defineInRange("prism_1k_type_limit", 32, 1, Integer.MAX_VALUE);
        PRISM_4K_TYPE_LIMIT = BUILDER
            .comment("Лимит типов предметов в призме 4K")
            .defineInRange("prism_4k_type_limit", 64, 1, Integer.MAX_VALUE);
        PRISM_16K_TYPE_LIMIT = BUILDER
            .comment("Лимит типов предметов в призме 16K")
            .defineInRange("prism_16k_type_limit", 128, 1, Integer.MAX_VALUE);
        PRISM_64K_TYPE_LIMIT = BUILDER
            .comment("Лимит типов предметов в призме 64K")
            .defineInRange("prism_64k_type_limit", 256, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Energy");
        BASE_ENERGY_CAPACITY = BUILDER
            .comment("Базовая ёмкость энергетического буфера")
            .defineInRange("base_energy_capacity", 10000, 1, Integer.MAX_VALUE);
        MESH_CORE_BANDWIDTH = BUILDER
            .comment("Базовая пропускная способность ядра (импульсов/тик)")
            .defineInRange("mesh_core_bandwidth", 32, 1, Integer.MAX_VALUE);
        NETWORK_PROCESSOR_BANDWIDTH = BUILDER
            .comment("Добавочная пропускная способность сетевого процессора (импульсов/тик)")
            .defineInRange("network_processor_bandwidth", 16, 0, Integer.MAX_VALUE);
        ENERGY_BRIDGE_TRANSFER_RATE = BUILDER
            .comment("Скорость передачи энергии через энергомост (ед./тик)")
            .defineInRange("energy_bridge_transfer_rate", 100, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Traffic");
        OPERATION_QUEUE_SIZE = BUILDER
            .comment("Максимальный размер очереди операций")
            .defineInRange("operation_queue_size", 100, 1, Integer.MAX_VALUE);
        TRAFFIC_OVERLOAD_DELAY_TICKS = BUILDER
            .comment("Задержка при перегрузке (тиков)")
            .defineInRange("traffic_overload_delay_ticks", 5, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Coherence");
        COHERENCE_NO_PENALTY_DISTANCE = BUILDER
            .comment("Расстояние без штрафа когерентности (сегментов кабеля)")
            .defineInRange("coherence_no_penalty_distance", 24, 0, Integer.MAX_VALUE);
        COHERENCE_PENALLY_SEGMENT_SIZE = BUILDER
            .comment("Размер сегмента для расчёта штрафа")
            .defineInRange("coherence_penalty_segment_size", 16, 1, Integer.MAX_VALUE);
        COHERENCE_PENALLY_DELAY_TICKS = BUILDER
            .comment("Задержка на сегмент (тиков)")
            .defineInRange("coherence_penalty_delay_ticks", 1, 0, Integer.MAX_VALUE);
        DIMENSIONAL_CROSSING_PENALTY = BUILDER
            .comment("Штраф за межпространственное соединение (тиков)")
            .defineInRange("dimensional_crossing_penalty", 50, 0, Integer.MAX_VALUE);
        STABILIZER_BONUS = BUILDER
            .comment("Бонус стабилизатора к эффективному расстоянию")
            .defineInRange("stabilizer_bonus", 20, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Wireless");
        WIRELESS_BASE_RANGE = BUILDER
            .comment("Базовый радиус беспроводного реле (блоков)")
            .defineInRange("wireless_base_range", 16, 1, Integer.MAX_VALUE);
        WIRELESS_TERMINAL_RANGE = BUILDER
            .comment("Радиус действия беспроводного терминала (блоков)")
            .defineInRange("wireless_terminal_range", 32, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Logistics");
        IMPORT_EXPORT_SPEED = BUILDER
            .comment("Скорость импорта/экспорта (предметов/тик)")
            .defineInRange("import_export_speed", 1, 1, Integer.MAX_VALUE);
        IMPORT_EXPORT_BATCH_SIZE = BUILDER
            .comment("Размер партии импорта/экспорта")
            .defineInRange("import_export_batch_size", 64, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Autocraft");
        AUTOCRAFT_MAX_DEPTH = BUILDER
            .comment("Максимальная глубина рекурсии автокрафта")
            .defineInRange("autocraft_max_depth", 10, 1, Integer.MAX_VALUE);
        AUTOCRAFT_MAX_JOBS = BUILDER
            .comment("Максимальное количество одновременных заданий")
            .defineInRange("autocraft_max_jobs", 50, 1, Integer.MAX_VALUE);
        FABRICATOR_SPEED = BUILDER
            .comment("Скорость работы фабрикатора (тиков на операцию)")
            .defineInRange("fabricator_speed", 20, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Quantum");
        QUANTUM_ENERGY_COST = BUILDER
            .comment("Стоимость квантовой передачи (энергии)")
            .defineInRange("quantum_energy_cost", 100, 0, Integer.MAX_VALUE);
        QUANTUM_TRAFFIC_COST = BUILDER
            .comment("Стоимость квантовой передачи (импульсов)")
            .defineInRange("quantum_traffic_cost", 16, 0, Integer.MAX_VALUE);
        QUANTUM_CHUNK_LOADING = BUILDER
            .comment("Разрешить загрузку чанков квантовым мостом")
            .define("quantum_chunk_loading", false);
        BUILDER.pop();

        BUILDER.push("Matter Condenser");
        MATTER_CONDENSATION_RATIO = BUILDER
            .comment("Коэффициент конденсации материи (предметов -> единица фазовой материи)")
            .defineInRange("matter_condensation_ratio", 64, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static final ModConfigSpec SPEC;
}
