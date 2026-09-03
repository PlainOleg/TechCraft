package TechCraft.lumenmesh.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Базовый интерфейс для всех узлов сети Lumen Mesh.
 * Каждый блок, подключаемый к сети, должен реализовать этот интерфейс.
 */
public interface LumenNetworkNode {
    /**
     * @return уникальный идентификатор узла
     */
    UUID getNodeId();

    /**
     * @return позиция блока в мире
     */
    BlockPos getNodePosition();

    /**
     * @return измерение, в котором находится узел
     */
    ResourceKey<Level> getDimension();

    /**
     * @return множество сторон, к которым можно подключиться
     */
    Set<Direction> getConnectionSides();

    /**
     * @return тип узла (кабель, ядро, накопитель и т.д.)
     */
    NodeType getNodeType();

    /**
     * @return true, если узел включён и функционирует
     */
    boolean isEnabled();

    /**
     * Вызывается при изменении сети (объединение, разделение, перестройка графа).
     * @param networkId UUID новой сети, или null если узел отключён от сети
     */
    void onNetworkChanged(@Nullable UUID networkId);

    /**
     * Типы узлов сети.
     */
    enum NodeType {
        CABLE,              // Обычный кабель
        SMART_CABLE,        // Умный кабель (показывает загрузку)
        DENSE_TRUNK,        // Магистраль высокой пропускной способности
        CABLE_JUNCTION,     // Разветвитель кабелей
        MESH_CORE,          // Ядро сети (координатор)
        ENERGY_BRIDGE,      // Энергомост
        PULSE_BUFFER,       // Буфер энергии
        COHERENCE_STABILIZER, // Стабилизатор когерентности
        PRISM_DRIVE,        // Накопитель призм
        ITEM_TERMINAL,      // Предметный терминал
        CRAFTING_TERMINAL,  // Крафтовый терминал
        BLUEPRINT_ENCODER,  // Кодировщик чертежей
        FABRICATOR,         // Фабрикатор
        CRAFTING_PROCESSOR, // Процессор автокрафта
        IMPORT_NODE,        // Узел импорта
        EXPORT_NODE,        // Узел экспорта
        STORAGE_LINK,       // Связь с внешним инвентарём
        MACHINE_INTERFACE,  // Интерфейс механизмов
        LEVEL_KEEPER,       // Поддержание запасов
        STOCK_MONITOR,      // Монитор запасов
        WIRELESS_RELAY,     // Беспроводное реле
        QUANTUM_BRIDGE,     // Квантовый мост
        SECURITY_CONSOLE,   // Консоль безопасности
        MATTER_CONDENSER    // Конденсатор материи
    }
}
