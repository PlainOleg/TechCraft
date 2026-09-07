package TechCraft.lumenmesh.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import TechCraft.lumenmesh.menu.EnergyBridgeMenu;
import TechCraft.TechCraft;

/**
 * Minimal screen for Energy Bridge.
 */
public class EnergyBridgeScreen extends AbstractContainerScreen<EnergyBridgeMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, "textures/gui/container/lumen_mesh/energy_bridge.png");

    public EnergyBridgeScreen(EnergyBridgeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        LumenGuiStyle.segmentedBar(guiGraphics, leftPos + 43, topPos + 59, 118, 6, 10,
            menu.getEnergyCapacity() == 0 ? 0 : (double) menu.getEnergyStored() / menu.getEnergyCapacity(), LumenGuiStyle.ENERGY);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LumenGuiStyle.title(graphics, font, title, imageWidth);
        graphics.drawString(font, "Энергия", 42, 27, 0x404040, false);
        graphics.drawString(font, LumenGuiStyle.number(menu.getEnergyStored()) + " / " + LumenGuiStyle.number(menu.getEnergyCapacity()) + " FE", 42, 43, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 7, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
