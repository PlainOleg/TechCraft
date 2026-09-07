package TechCraft.solar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Screen for Solar Panel Bank.
 * Renders the GUI with energy bars, generation display, and tooltips.
 */
public class SolarPanelBankScreen extends AbstractContainerScreen<SolarPanelBankMenu> {
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(TechCraft.TechCraft.MOD_ID, "textures/gui/container/solar/solar_panel_bank.png");
    
    private static final int IMAGE_WIDTH = 194;
    private static final int IMAGE_HEIGHT = 242;
    private static final int TEXTURE_SIZE = 256;
    private static final int BAR_BACKGROUND_COLOR = 0xFF8C8C8C;
    private static final int BAR_FILL_COLOR = 0xFFFFC33A;
    private static final float TITLE_SCALE = 0.75f;
    private static final float TITLE_Y = 5.0f;

    public SolarPanelBankScreen(SolarPanelBankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);

        // The reference layout uses one common energy bar below the info panel.
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        long maxCapacity = menu.getMaxEnergyCapacity();
        long energyStored = menu.getEnergyStored();
        int barX = x + 25;
        int barY = y + 124;

        guiGraphics.fill(barX, barY, barX + 144, barY + 6, BAR_BACKGROUND_COLOR);

        if (maxCapacity > 0) {
            double ratio = Math.clamp((double) energyStored / maxCapacity, 0.0, 1.0);
            int barWidth = (int) (144 * ratio);
            guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, BAR_FILL_COLOR);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(TITLE_SCALE, TITLE_SCALE, 1.0f);
        int scaledWidth = Math.round(imageWidth / TITLE_SCALE);
        int titleX = (scaledWidth - font.width(title)) / 2;
        guiGraphics.drawString(
            font,
            title,
            titleX,
            Math.round(TITLE_Y / TITLE_SCALE),
            0xF0F0F0,
            true
        );
        guiGraphics.pose().popPose();

        Component energy = Component.literal("Энергия: ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(formatNumber(menu.getEnergyStored()) + " / "
                + formatNumber(menu.getMaxEnergyCapacity()) + " FE").withStyle(ChatFormatting.GOLD));
        Component generation = Component.literal("Генерация: ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(formatNumber(menu.getCurrentGeneration()) + " FE/t").withStyle(ChatFormatting.GOLD));
        Component status = Component.literal(menu.isSkyVisible() ? "☀" : "☁")
            .withStyle(menu.isSkyVisible() ? ChatFormatting.YELLOW : ChatFormatting.GRAY);

        guiGraphics.drawString(font, energy, 20, 94, 0xFFFFFF, false);
        guiGraphics.drawString(font, generation, 20, 106, 0xFFFFFF, false);
        guiGraphics.drawString(font, status, 164, 106, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        if (isMouseOver(x, y, guiX + 16, guiY + 89, 162, 29)) {
            renderGenerationTooltip(guiGraphics, x, y);
        }

        if (isMouseOver(x, y, guiX + 25, guiY + 124, 144, 6)) {
            renderEnergyTooltip(guiGraphics, x, y);
        }
    }

    private void renderGenerationTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        long current = menu.getCurrentGeneration();
        long peak = menu.getPeakGeneration();
        int panelCount = menu.getPanelCount();
        boolean skyVisible = menu.isSkyVisible();

        double solarFactor = peak > 0 ? (double) current / peak : 0.0;

        Component heading = Component.literal("Солнечная генерация").withStyle(ChatFormatting.GOLD);
        Component currentLine = tooltipLine("Сейчас: ", formatNumber(current) + " FE/t", ChatFormatting.YELLOW);
        Component peakLine = tooltipLine("Максимум: ", formatNumber(peak) + " FE/t", ChatFormatting.YELLOW);
        Component efficiencyLine = tooltipLine(
            "Эффективность: ", String.format("%.0f%%", solarFactor * 100), ChatFormatting.AQUA);
        Component panelsLine = tooltipLine("Установлено панелей: ", Integer.toString(panelCount), ChatFormatting.WHITE);
        Component skyLine = tooltipLine(
            "Доступ к небу: ", skyVisible ? "есть" : "нет",
            skyVisible ? ChatFormatting.GREEN : ChatFormatting.RED);

        guiGraphics.renderTooltip(font, List.of(
            heading.getVisualOrderText(),
            currentLine.getVisualOrderText(),
            peakLine.getVisualOrderText(),
            efficiencyLine.getVisualOrderText(),
            panelsLine.getVisualOrderText(),
            skyLine.getVisualOrderText()
        ), mouseX, mouseY);
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        long stored = menu.getEnergyStored();
        long max = menu.getMaxEnergyCapacity();
        double ratio = max > 0 ? (double) stored / max : 0.0;

        Component heading = Component.literal("Энергия банка").withStyle(ChatFormatting.GOLD);
        Component storedLine = tooltipLine("Накоплено: ", formatNumber(stored) + " FE", ChatFormatting.YELLOW);
        Component capacityLine = tooltipLine("Ёмкость: ", formatNumber(max) + " FE", ChatFormatting.YELLOW);
        Component fillLine = tooltipLine(
            "Заполнение: ", String.format("%.1f%%", ratio * 100), ChatFormatting.AQUA);

        guiGraphics.renderTooltip(font, List.of(
            heading.getVisualOrderText(),
            storedLine.getVisualOrderText(),
            capacityLine.getVisualOrderText(),
            fillLine.getVisualOrderText()
        ), mouseX, mouseY);
    }

    private static Component tooltipLine(String label, String value, ChatFormatting valueColor) {
        return Component.literal(label).withStyle(ChatFormatting.GRAY)
            .append(Component.literal(value).withStyle(valueColor));
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.2fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }
}
