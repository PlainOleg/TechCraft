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
     * Used for deferred registration to avoid circular dependencies.
     */
    public static Supplier<SolarPanelType> supplier(int tier, long generation, long capacity, String frameColor) {
        return () -> new SolarPanelType(tier, generation, capacity, frameColor);
    }
}
