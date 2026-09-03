package TechCraft.lumenmesh.energy;

import TechCraft.lumenmesh.network.LumenNetwork;
import org.slf4j.Logger;

/**
 * Сервис управления энергией в сети Lumen Mesh.
 * Распределяет энергию между устройствами и проверяет доступность энергии.
 */
public class LumenEnergyService {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LumenEnergyService.class);
    
    /**
     * Базовые стоимости операций в импульсах энергии.
     */
    public static final int TERMINAL_VIEW_COST = 1;
    public static final int STACK_INSERT_COST = 2;
    public static final int STACK_EXTRACT_COST = 2;
    public static final int IMPORT_NODE_COST = 3;
    public static final int EXPORT_NODE_COST = 3;
    public static final int AUTOCRAFT_REQUEST_COST = 8;
    public static final int FABRICATOR_OPERATION_COST = 5;
    public static final int WIRELESS_ACCESS_COST = 4;
    public static final int QUANTUM_TRANSFER_COST = 16;

    /**
     * Проверяет, достаточно ли энергии для операции.
     * @param network сеть
     * @param cost стоимость операции
     * @return true, если энергии достаточно
     */
    public static boolean hasEnergy(LumenNetwork network, int cost) {
        if (network == null) {
            return false;
        }
        return network.getEnergyStored() >= cost;
    }

    /**
     * Пытается потребить энергию для операции.
     * @param network сеть
     * @param cost стоимость операции
     * @return true, если энергия была потреблена
     */
    public static boolean consumeEnergy(LumenNetwork network, int cost) {
        if (!hasEnergy(network, cost)) {
            return false;
        }
        
        network.setEnergyStored(network.getEnergyStored() - cost);
        return true;
    }

    /**
     * Добавляет энергию в сеть.
     * @param network сеть
     * @param amount количество энергии
     * @return количество фактически добавленной энергии (с учётом ёмкости)
     */
    public static long addEnergy(LumenNetwork network, long amount) {
        if (network == null || amount <= 0) {
            return 0;
        }
        
        long space = network.getEnergyCapacity() - network.getEnergyStored();
        long added = Math.min(amount, space);
        
        if (added > 0) {
            network.setEnergyStored(network.getEnergyStored() + added);
        }
        
        return added;
    }

    /**
     * Извлекает энергию из сети.
     * @param network сеть
     * @param amount количество энергии
     * @return количество фактически извлечённой энергии
     */
    public static long extractEnergy(LumenNetwork network, long amount) {
        if (network == null || amount <= 0) {
            return 0;
        }
        
        long available = network.getEnergyStored();
        long extracted = Math.min(amount, available);
        
        if (extracted > 0) {
            network.setEnergyStored(network.getEnergyStored() - extracted);
        }
        
        return extracted;
    }

    /**
     * Получает процент заполнения энергетического буфера.
     * @param network сеть
     * @return значение от 0.0 до 1.0
     */
    public static double getEnergyRatio(LumenNetwork network) {
        if (network == null || network.getEnergyCapacity() <= 0) {
            return 0.0;
        }
        return (double) network.getEnergyStored() / network.getEnergyCapacity();
    }

    /**
     * Проверяет, находится ли сеть в критическом состоянии по энергии.
     * @param network сеть
     * @param threshold порог в долях (например, 0.1 = 10%)
     * @return true, если энергия ниже порога
     */
    public static boolean isLowEnergy(LumenNetwork network, double threshold) {
        return getEnergyRatio(network) < threshold;
    }
}
