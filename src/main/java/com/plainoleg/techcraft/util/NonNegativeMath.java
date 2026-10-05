package com.plainoleg.techcraft.util;

/**
 * Saturating arithmetic for non-negative energy, capacity and item counts.
 */
public final class NonNegativeMath {
    private NonNegativeMath() {
    }

    public static long add(long left, long right) {
        if (left < 0 || right < 0) throw new IllegalArgumentException("Counts must be non-negative");
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    public static long multiply(long value, long count) {
        if (value < 0 || count < 0) throw new IllegalArgumentException("Counts must be non-negative");
        return count != 0 && value > Long.MAX_VALUE / count ? Long.MAX_VALUE : value * count;
    }
}
