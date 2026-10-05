package TechCraft.solar;

import java.util.function.Supplier;

/**
 * Record defining the characteristics of a solar panel tier.
 * Each solar panel block has an associated SolarPanelType.
 */
public record SolarPanelType(
    int tier,
    long generationPerTick,
    long energyCapacity,
    String frameColor
) {
    /**
     * Creates a supplier for this solar panel type.
     * The record is created once: panels read their type every tick.
     */
    public static Supplier<SolarPanelType> supplier(int tier, long generation, long capacity, String frameColor) {
        SolarPanelType type = new SolarPanelType(tier, generation, capacity, frameColor);
        return () -> type;
    }
}
