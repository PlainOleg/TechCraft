package com.plainoleg.techcraft.lumenmesh.client;

import com.plainoleg.techcraft.client.NumberFormat;
import com.plainoleg.techcraft.lumenmesh.item.StorageMediumItem;
import com.plainoleg.techcraft.lumenmesh.menu.PrismDriveMenu;
import com.plainoleg.techcraft.lumenmesh.storage.StorageMediumData;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PrismDriveScreen extends AbstractContainerScreen<PrismDriveMenu> {
    private StorageSummary summary = new StorageSummary(0, 0, 0, 0, 0, false);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("techcraft", "textures/gui/container/lumen_mesh/prism_drive.png");

    public PrismDriveScreen(PrismDriveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 172;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LumenGuiStyle.title(graphics, font, title, imageWidth);
        Component status = Component.translatable(!menu.isNetworked()
                ? "gui.techcraft.prism_drive.status.no_network"
                : summary.installed() == 0
                ? "gui.techcraft.prism_drive.status.no_prisms"
                : menu.isActive()
                ? "gui.techcraft.prism_drive.status.active"
                : "gui.techcraft.prism_drive.status.no_energy");
        int color = !menu.isNetworked() ? 0xA03030 : menu.isActive() ? 0x287A38 : 0xB07818;
        graphics.drawString(font, status, 16, 16, color, false);

        Component installed = Component.translatable("gui.techcraft.prism_drive.installed", summary.installed(), 4);
        graphics.drawString(font, installed, 160 - font.width(installed), 16, 0x404040, false);

        Component capacity = Component.translatable("gui.techcraft.prism_drive.capacity",
                NumberFormat.format(summary.used()), summary.infinite() ? "∞" : NumberFormat.format(summary.capacity()));
        drawScaled(graphics, capacity, 16, 57);

        Component types = Component.translatable("gui.techcraft.prism_drive.types", summary.types(), summary.typeLimit());
        drawScaled(graphics, types, (imageWidth - Math.round(font.width(types) * 0.75F)) / 2, 77);
    }

    private void drawScaled(GuiGraphics graphics, Component text, int x, int y) {
        float scale = 0.75F;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, Math.round(x / scale), Math.round(y / scale), 0x404040, false);
        graphics.pose().popPose();
    }

    private StorageSummary storageSummary() {
        if (minecraft == null || minecraft.level == null) return new StorageSummary(0, 0, 0, 0, 0, false);
        int installed = 0;
        long used = 0;
        long capacity = 0;
        int types = 0;
        int typeLimit = 0;
        boolean infinite = false;
        for (int index = 0; index < 4; index++) {
            var stack = menu.getSlot(index).getItem();
            if (!(stack.getItem() instanceof StorageMediumItem medium)) continue;
            installed++;
            var statistics = StorageMediumData.statistics(stack, minecraft.level.registryAccess());
            used = saturatingAdd(used, statistics.used());
            infinite |= medium.isInfinite();
            if (!infinite) capacity = saturatingAdd(capacity, medium.capacity());
            types += statistics.types();
            typeLimit += medium.typeLimit();
        }
        return new StorageSummary(installed, used, capacity, types, typeLimit, infinite);
    }

    private static long saturatingAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    private record StorageSummary(int installed, long used, long capacity, int types,
                                  int typeLimit, boolean infinite) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        summary = storageSummary();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        double fill = summary.infinite() || summary.capacity() == 0
                ? 0
                : (double) summary.used() / summary.capacity();
        LumenGuiStyle.bar(graphics, leftPos + 17, topPos + 68, 142, 6, fill, LumenGuiStyle.BANDWIDTH);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
