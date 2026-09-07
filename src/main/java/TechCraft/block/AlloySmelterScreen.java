package TechCraft.block;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class AlloySmelterScreen extends AbstractContainerScreen<AlloySmelterMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechCraft.TechCraft.MOD_ID, "textures/gui/container/alloy_smelter.png");

    private final AlloySmelterBlockEntity blockEntity;

    // Размеры и координаты иконки жара в GUI
    private static final int HEAT_ICON_X = 150;
    private static final int HEAT_ICON_Y = 13;
    private static final int HEAT_ICON_WIDTH = 15;
    private static final int HEAT_ICON_HEIGHT = 14;

    // Координаты незакрашенной иконки жара на текстуре
    private static final int HEAT_ICON_UV_X = 176;
    private static final int HEAT_ICON_UV_Y = 34;

    // Координаты закрашенной иконки жара на текстуре
    private static final int HEAT_FILLED_UV_X = 176;
    private static final int HEAT_FILLED_UV_Y = 47;

    public AlloySmelterScreen(AlloySmelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.blockEntity = menu.getBlockEntity();
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        renderTemperatureIcon(guiGraphics);
    }

    private void renderTemperatureIcon(GuiGraphics guiGraphics) {
        if (blockEntity == null) return;

        int fuelBurnTime = blockEntity.getFuelBurnTime();
        int maxFuelBurnTime = blockEntity.getMaxFuelBurnTime();

        // Отрисовка незакрашенной иконки (всегда)
        guiGraphics.blit(
                TEXTURE,
                this.leftPos + HEAT_ICON_X, this.topPos + HEAT_ICON_Y,
                HEAT_ICON_UV_X, HEAT_ICON_UV_Y,
                HEAT_ICON_WIDTH, HEAT_ICON_HEIGHT
        );

        // Если есть топливо, отрисовываем закрашенную часть по уровню топлива
        if (maxFuelBurnTime > 0 && fuelBurnTime > 0) {
            float ratio = (float) fuelBurnTime / maxFuelBurnTime;
            int filledHeight = Math.round(ratio * HEAT_ICON_HEIGHT);

            if (filledHeight > 0) {
                int drawY = this.topPos + HEAT_ICON_Y + (HEAT_ICON_HEIGHT - filledHeight);

                guiGraphics.blit(
                        TEXTURE,
                        this.leftPos + HEAT_ICON_X, drawY,
                        HEAT_FILLED_UV_X, HEAT_FILLED_UV_Y + HEAT_ICON_HEIGHT - filledHeight,
                        HEAT_ICON_WIDTH, filledHeight
                );
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Заголовок "Плавильная печь"
        guiGraphics.drawCenteredString(this.font, Component.literal("Плавильная печь"), this.imageWidth / 2, 6, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        // Проверка наведения на иконку жара
        int heatIconScreenX = this.leftPos + HEAT_ICON_X;
        int heatIconScreenY = this.topPos + HEAT_ICON_Y;

        if (mouseX >= heatIconScreenX && mouseX <= heatIconScreenX + HEAT_ICON_WIDTH &&
            mouseY >= heatIconScreenY && mouseY <= heatIconScreenY + HEAT_ICON_HEIGHT) {
            if (blockEntity != null) {
                float currentTemp = menu.getCurrentTemperature();
                int maxTemp = menu.getMaxTemperature();

                // Градиент цвета от желтого к красно-оранжевому для текущей температуры
                float ratio = maxTemp > 0 ? Math.min(1.0f, currentTemp / maxTemp) : 0;
                int currentColor = getTemperatureColor(ratio);
                int maxColor = 0xFF5555; // Светло-красный для максимальной температуры

                // Две строки с подписями
                Component line1 = Component.literal("Текущая: ")
                        .append(Component.literal(String.format("%.0f°C", currentTemp)).withStyle(s -> s.withColor(currentColor)));
                Component line2 = Component.literal("Максимальная: ")
                        .append(Component.literal(String.format("%d°C", maxTemp)).withStyle(s -> s.withColor(maxColor)));

                guiGraphics.renderTooltip(this.font, List.of(line1.getVisualOrderText(), line2.getVisualOrderText()), mouseX, mouseY);
            }
        }
    }

    private int getTemperatureColor(float ratio) {
        // Градиент от желтого (255, 255, 0) к красно-оранжевому (255, 69, 0)
        int red = 255;
        int green = Math.round(255 * (1 - ratio * 0.73f)); // 255 -> 69
        int blue = 0;
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }
}
