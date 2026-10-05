package com.plainoleg.techcraft.lumenmesh.client;

import com.plainoleg.techcraft.client.NumberFormat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Shared visual language matching the Solar Panel Bank screens. */
final class LumenGuiStyle {
    static final int BAR_BACKGROUND = 0xFF8C8C8C;
    static final int ENERGY = 0xFFFFC33A;
    static final int BANDWIDTH = 0xFF55D6E8;
    static final int COHERENCE = 0xFFA96CE0;

    private LumenGuiStyle() {}

    static void title(GuiGraphics graphics, Font font, Component title, int imageWidth) {
        title(graphics, font, title, imageWidth, 7);
    }

    static void title(GuiGraphics graphics, Font font, Component title, int imageWidth, int y) {
        float scale = 0.75F;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        int scaledWidth = Math.round(imageWidth / scale);
        graphics.drawString(font, title, (scaledWidth - font.width(title)) / 2, y, 0xF0F0F0, true);
        graphics.pose().popPose();
    }

    static void bar(GuiGraphics graphics, int x, int y, int width, int height, double ratio, int color) {
        graphics.fill(x, y, x + width, y + height, BAR_BACKGROUND);
        int filled = (int) Math.round(width * Math.clamp(ratio, 0.0, 1.0));
        if (filled > 0) graphics.fill(x, y, x + filled, y + height, color);
    }

    static void segmentedBar(GuiGraphics graphics, int x, int y, int width, int height,
                             int segments, double ratio, int color) {
        graphics.fill(x, y, x + width, y + height, BAR_BACKGROUND);
        int filledSegments = (int) Math.ceil(Math.clamp(ratio, 0.0, 1.0) * segments);
        for (int segment = 0; segment < segments; segment++) {
            int start = x + segment * width / segments;
            int end = x + (segment + 1) * width / segments;
            if (segment > 0) graphics.fill(start, y, start + 1, y + height, 0xFF545454);
            if (segment < filledSegments) {
                graphics.fill(start + (segment > 0 ? 1 : 0), y, end, y + height, color);
            }
        }
    }

    static String number(long value) {
        return NumberFormat.format(value);
    }
}
