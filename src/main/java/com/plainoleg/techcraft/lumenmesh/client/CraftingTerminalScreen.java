package com.plainoleg.techcraft.lumenmesh.client;

import com.plainoleg.techcraft.lumenmesh.menu.CraftingTerminalMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftingTerminalScreen extends ItemTerminalScreen<CraftingTerminalMenu> {
    public CraftingTerminalScreen(CraftingTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 312;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        frame(graphics, leftPos, topPos, imageWidth, imageHeight);
        panel(graphics, leftPos + 12, topPos + 20, 288, 121);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 15; col++)
                slot(graphics, leftPos + 17 + col * 18, topPos + 52 + row * 18);
        divider(graphics, leftPos + 7, topPos + 146, imageWidth - 14);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                slot(graphics, leftPos + 28 + col * 18, topPos + 151 + row * 18);
        for (int col = 0; col < 9; col++)
            slot(graphics, leftPos + 28 + col * 18, topPos + 209);
        panel(graphics, leftPos + 212, topPos + 151, 96, 76);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                slot(graphics, leftPos + 218 + col * 18, topPos + 157 + row * 18);
        arrow(graphics, leftPos + 273, topPos + 181);
        slot(graphics, leftPos + 288, topPos + 175);
    }

    private static void frame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 2, y, x + w - 2, y + h, 0xFF373737);
        g.fill(x, y + 2, x + w, y + h - 2, 0xFF373737);
        g.fill(x + 3, y + 2, x + w - 3, y + h - 3, 0xFFC6C6C6);
        g.fill(x + 2, y + 3, x + w - 2, y + h - 3, 0xFFC6C6C6);
        g.fill(x + 3, y + 2, x + w - 3, y + 3, 0xFFFFFFFF);
        g.fill(x + 2, y + 3, x + 3, y + h - 3, 0xFFFFFFFF);
    }

    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        g.fill(x, y, x + w, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF979797);
    }

    private static void slot(GuiGraphics g, int x, int y) { panel(g, x - 1, y - 1, 18, 18); }
    private static void divider(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, 0xFF8B8B8B);
        g.fill(x, y + 1, x + w, y + 2, 0xFFFFFFFF);
    }

    private static void arrow(GuiGraphics g, int x, int y) {
        g.fill(x, y + 2, x + 7, y + 4, 0xFF555555);
        g.fill(x + 6, y, x + 8, y + 6, 0xFF555555);
        g.fill(x + 8, y + 1, x + 10, y + 5, 0xFF555555);
        g.fill(x + 10, y + 2, x + 12, y + 4, 0xFF555555);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LumenGuiStyle.title(graphics, font, title, imageWidth, 9);
    }

    @Override protected int pageLabelRight() { return 299; }

}
