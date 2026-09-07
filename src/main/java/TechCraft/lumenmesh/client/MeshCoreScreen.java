package TechCraft.lumenmesh.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import TechCraft.lumenmesh.menu.MeshCoreMenu;
import TechCraft.TechCraft;

/**
 * Minimal screen for Mesh Core.
 */
public class MeshCoreScreen extends AbstractContainerScreen<MeshCoreMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, "textures/gui/container/lumen_mesh/mesh_core.png");
    private static final int BAR_X = 70;
    private static final int BAR_WIDTH = 88;
    private static final int ENERGY_BAR_Y = 37;
    private static final int BANDWIDTH_BAR_Y = 54;
    private static final int COHERENCE_BAR_Y = 71;

    public MeshCoreScreen(MeshCoreMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        LumenGuiStyle.bar(guiGraphics, leftPos + BAR_X, topPos + ENERGY_BAR_Y, BAR_WIDTH, 6,
            menu.getEnergyCapacity() == 0 ? 0 : (double) menu.getEnergyStored() / menu.getEnergyCapacity(), LumenGuiStyle.ENERGY);
        LumenGuiStyle.bar(guiGraphics, leftPos + BAR_X, topPos + BANDWIDTH_BAR_Y, BAR_WIDTH, 6,
            menu.getBandwidth() == 0 ? 0 : (double) menu.getUsedBandwidth() / menu.getBandwidth(), LumenGuiStyle.BANDWIDTH);
        LumenGuiStyle.bar(guiGraphics, leftPos + BAR_X, topPos + COHERENCE_BAR_Y, BAR_WIDTH, 6,
            menu.getCoherence(), LumenGuiStyle.COHERENCE);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LumenGuiStyle.title(graphics, font, title, imageWidth);
        Component status = Component.translatable(menu.hasNetwork()
            ? menu.getEnergyStored() > 0
                ? "gui.techcraft.mesh_core.status.active"
                : "gui.techcraft.mesh_core.status.no_energy"
            : "gui.techcraft.mesh_core.status.no_network");
        int statusColor = !menu.hasNetwork() ? 0xA03030 : menu.getEnergyStored() > 0 ? 0x287A38 : 0xB07818;
        graphics.drawString(font, status, 18, 16, statusColor, false);

        Component nodes = Component.translatable("gui.techcraft.mesh_core.nodes", menu.getNodeCount());
        graphics.drawString(font, nodes, 158 - font.width(nodes), 16, 0x404040, false);

        drawMetric(graphics, "gui.techcraft.mesh_core.energy", 27,
            LumenGuiStyle.number(menu.getEnergyStored()) + "/" + LumenGuiStyle.number(menu.getEnergyCapacity()) + " FE");
        drawMetric(graphics, "gui.techcraft.mesh_core.bandwidth", 44,
            menu.getUsedBandwidth() + "/" + menu.getBandwidth());
        drawMetric(graphics, "gui.techcraft.mesh_core.coherence", 61,
            Math.round(menu.getCoherence() * 100.0) + "%");
    }

    private void drawMetric(GuiGraphics graphics, String labelKey, int y, String value) {
        float scale = 0.75F;
        int scaledX = Math.round(55 / scale);
        int scaledRight = Math.round(158 / scale);
        int scaledY = Math.round(y / scale);
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, Component.translatable(labelKey), scaledX, scaledY, 0x404040, false);
        graphics.drawString(font, value, scaledRight - font.width(value), scaledY, 0x404040, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (isHovering(BAR_X, ENERGY_BAR_Y, BAR_WIDTH, 6, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.techcraft.mesh_core.energy.tooltip")
                .withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        } else if (isHovering(BAR_X, BANDWIDTH_BAR_Y, BAR_WIDTH, 6, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.techcraft.mesh_core.bandwidth.tooltip")
                .withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        } else if (isHovering(BAR_X, COHERENCE_BAR_Y, BAR_WIDTH, 6, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.techcraft.mesh_core.coherence.tooltip")
                .withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
