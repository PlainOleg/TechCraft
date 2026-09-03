package TechCraft.solar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Solar Panel Bank.
 * Renders the GUI with energy bars, generation display, and tooltips.
 */
public class SolarPanelBankScreen extends AbstractContainerScreen<SolarPanelBankMenu> {
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(TechCraft.TechCraft.MOD_ID, "textures/gui/container/solar_panel_bank.png");
    
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 188;

    public SolarPanelBankScreen(SolarPanelBankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render generation bar (horizontal)
        renderGenerationBar(guiGraphics, x, y);

        // Render energy bar (vertical)
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderGenerationBar(GuiGraphics guiGraphics, int x, int y) {
        SolarPanelBankBlockEntity blockEntity = menu.blockEntity;
        long peakGeneration = blockEntity.getPeakGeneration();
        long currentGeneration = blockEntity.getCurrentGeneration();

        if (peakGeneration > 0) {
            double ratio = (double) currentGeneration / peakGeneration;
            int barWidth = (int) (92 * ratio);
            int barX = x + 17;
            int barY = y + 78;
            
            // Draw filled portion
            guiGraphics.blit(TEXTURE, barX, barY, 0, 188, barWidth, 10);
        }
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        SolarPanelBankBlockEntity blockEntity = menu.blockEntity;
        long maxCapacity = blockEntity.getMaxEnergyCapacity();
        long energyStored = blockEntity.getEnergyStored();

        if (maxCapacity > 0) {
            double ratio = (double) energyStored / maxCapacity;
            int barHeight = (int) (64 * ratio);
            int barX = x + 151;
            int barY = y + 26 + (64 - barHeight);
            
            // Draw filled portion from bottom
            guiGraphics.blit(TEXTURE, barX, barY, 92, 188 + (64 - barHeight), 11, barHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title is rendered by super, but we can add additional labels here
        // For now, we'll rely on tooltips for information display
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // Generation bar tooltip
        if (isMouseOver(x, y, guiX + 17, guiY + 78, 92, 10)) {
            renderGenerationTooltip(guiGraphics, x, y);
        }

        // Energy bar tooltip
        if (isMouseOver(x, y, guiX + 151, guiY + 26, 11, 64)) {
            renderEnergyTooltip(guiGraphics, x, y);
        }
    }

    private void renderGenerationTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        SolarPanelBankBlockEntity blockEntity = menu.blockEntity;
        long current = blockEntity.getCurrentGeneration();
        long peak = blockEntity.getPeakGeneration();
        int panelCount = blockEntity.getTotalPanelCount();
        boolean skyVisible = blockEntity.isSkyVisible();

        double solarFactor = peak > 0 ? (double) current / peak : 0.0;

        guiGraphics.renderTooltip(this.font, 
            Component.translatable("tooltip.techcraft.solar_panel_bank.generation",
                formatNumber(current),
                formatNumber(peak),
                String.format("%.0f%%", solarFactor * 100),
                panelCount,
                skyVisible ? "✓" : "✗"
            ),
            mouseX, mouseY
        );
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        SolarPanelBankBlockEntity blockEntity = menu.blockEntity;
        long stored = blockEntity.getEnergyStored();
        long max = blockEntity.getMaxEnergyCapacity();
        double ratio = max > 0 ? (double) stored / max : 0.0;

        guiGraphics.renderTooltip(this.font, 
            Component.translatable("tooltip.techcraft.solar_panel_bank.energy",
                formatNumber(stored),
                formatNumber(max),
                String.format("%.1f%%", ratio * 100)
            ),
            mouseX, mouseY
        );
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
