package TechCraft.util;

import net.minecraft.client.gui.screens.Screen;

/**
 * Утилитарный класс для форматирования чисел с сокращениями.
 * Пример: 10000 → 10K, 1500000 → 1.5M
 * При зажатом Shift показывает полное число с разделителями.
 */
public class NumberFormat {

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "Oc", "No", "Dc"};

    /**
     * Форматирует число с сокращением.
     * При зажатом Shift показывает полное число с разделителями.
     *
     * @param number число для форматирования
     * @return отформатированная строка
     */
    public static String format(long number) {
        if (Screen.hasShiftDown()) {
            return formatFull(number);
        } else {
            return formatCompact(number);
        }
    }

    /**
     * Форматирует число с сокращением (без Shift).
     *
     * @param number число для форматирования
     * @return сокращенная строка (например, "10K")
     */
    public static String formatCompact(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        }

        int suffixIndex = (int) (Math.log10(number) / 3);
        suffixIndex = Math.min(suffixIndex, SUFFIXES.length - 1);

        double scaled = number / Math.pow(1000, suffixIndex);
        
        if (scaled < 100) {
            return String.format("%.1f%s", scaled, SUFFIXES[suffixIndex]);
        } else {
            return String.format("%.0f%s", scaled, SUFFIXES[suffixIndex]);
        }
    }

    /**
     * Форматирует полное число с разделителями (при Shift).
     *
     * @param number число для форматирования
     * @return полная строка с разделителями (например, "10,000")
     */
    public static String formatFull(long number) {
        return String.format("%,d", number);
    }

    /**
     * Форматирует число с сокращением, игнорируя состояние Shift.
     *
     * @param number число для форматирования
     * @return сокращенная строка
     */
    public static String formatAlwaysCompact(long number) {
        return formatCompact(number);
    }

    /**
     * Форматирует полное число с разделителями, игнорируя состояние Shift.
     *
     * @param number число для форматирования
     * @return полная строка с разделителями
     */
    public static String formatAlwaysFull(long number) {
        return formatFull(number);
    }
}
