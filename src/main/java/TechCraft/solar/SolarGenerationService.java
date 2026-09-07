package TechCraft.solar;

import net.minecraft.world.level.Level;

/**
 * Service for calculating solar generation based on environmental conditions.
 * Used by both individual solar panels and the solar panel bank.
 */
public class SolarGenerationService {
    // Weather multipliers (configurable)
    private static final double WEATHER_CLEAR = 1.0;
    private static final double WEATHER_RAIN = 0.55;
    private static final double WEATHER_THUNDER = 0.25;

    // Check interval in ticks (20 ticks = 1 second)
    private static final int SKY_CHECK_INTERVAL = 20;

    /**
     * Calculates the solar factor based on time of day and weather.
     *
     * @param level the world level
     * @return a factor from 0.0 to 1.0 representing solar efficiency
     */
    public static double calculateSolarFactor(Level level) {
        if (!level.dimensionType().hasSkyLight()) {
            return 0.0;
        }

        // Check if it's daytime
        long dayTime = level.getDayTime() % 24000;
        double daylightFactor = calculateDaylightFactor(dayTime);

        if (daylightFactor <= 0.0) {
            return 0.0;
        }

        // Apply weather modifier
        double weatherFactor = getWeatherFactor(level);

        return daylightFactor * weatherFactor;
    }

    /**
     * Calculates daylight factor based on time of day.
     * Uses cosine function for smooth transition.
     *
     * @param sunTime time in ticks (0-24000)
     * @return factor from 0.0 to 1.0
     */
    static double calculateDaylightFactor(long sunTime) {
        // Peak at noon (6000), zero at midnight (18000)
        // Formula: max(0, cos(((sunTime - 6000) / 12000) * PI))
        double normalized = ((double) sunTime - 6000.0) / 12000.0;
        double cosine = Math.cos(normalized * Math.PI);
        return Math.max(0.0, cosine);
    }

    /**
     * Gets the weather factor based on current weather conditions.
     *
     * @param level the world level
     * @return factor from 0.0 to 1.0
     */
    private static double getWeatherFactor(Level level) {
        boolean isRaining = level.isRaining();
        boolean isThundering = level.isThundering();

        if (isThundering) {
            return WEATHER_THUNDER;
        } else if (isRaining) {
            return WEATHER_RAIN;
        } else {
            return WEATHER_CLEAR;
        }
    }

    /**
     * Checks if the sky is visible at a given position.
     *
     * @param level the world level
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @return true if sky is visible
     */
    public static boolean isSkyVisible(Level level, int x, int y, int z) {
        return level.canSeeSkyFromBelowWater(new net.minecraft.core.BlockPos(x, y, z));
    }

    /**
     * Gets the recommended interval for sky checks.
     *
     * @return interval in ticks
     */
    public static int getSkyCheckInterval() {
        return SKY_CHECK_INTERVAL;
    }

    /**
     * Calculates actual generation with fractional remainder handling.
     *
     * @param peakGeneration the peak generation rate
     * @param solarFactor    the solar efficiency factor (0.0 to 1.0)
     * @param remainder      the fractional remainder from previous tick
     * @return array containing [wholeGeneration, newRemainder]
     */
    public static long[] calculateGeneration(long peakGeneration, double solarFactor, double remainder) {
        double exact = peakGeneration * solarFactor + remainder;
        long whole = (long) Math.floor(exact);
        double newRemainder = exact - whole;
        return new long[]{whole, (long) (newRemainder * 1000)}; // Store remainder as scaled long
    }

    /**
     * Converts scaled remainder back to double.
     *
     * @param scaledRemainder the scaled remainder
     * @return the actual remainder value
     */
    public static double unscaleRemainder(long scaledRemainder) {
        return scaledRemainder / 1000.0;
    }
}
