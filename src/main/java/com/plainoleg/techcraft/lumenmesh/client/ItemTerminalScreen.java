package com.plainoleg.techcraft.lumenmesh.client;

import com.plainoleg.techcraft.client.NumberFormat;
import com.plainoleg.techcraft.lumenmesh.block.terminal.ItemTerminalBlockEntity;
import com.plainoleg.techcraft.lumenmesh.menu.ItemTerminalMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemTerminalScreen<M extends ItemTerminalMenu> extends AbstractContainerScreen<M> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("techcraft", "textures/gui/container/lumen_mesh/item_terminal.png");
    private EditBox searchBox;
    private Button sortButton;
    private Button modButton;
    private String lastSentSearch = "";
    public ItemTerminalScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 224;
        imageHeight = 235;
        inventoryLabelY = 140;
    }
    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + 19, topPos + 26, 72, 9, Component.translatable("gui.techcraft.item_terminal.search"));
        searchBox.setBordered(false);
        searchBox.setMaxLength(40);
        searchBox.setHint(Component.translatable("gui.techcraft.item_terminal.search"));
        searchBox.setResponder(this::syncSearch);
        addRenderableWidget(searchBox);
        modButton = addRenderableWidget(Button.builder(Component.empty(), button -> sendPage(3))
            .bounds(leftPos + 96, topPos + 23, 38, 14).build());
        sortButton = addRenderableWidget(Button.builder(Component.empty(), button -> sendPage(2))
            .bounds(leftPos + 138, topPos + 23, 30, 14).build());
        addRenderableWidget(Button.builder(Component.literal("‹"), button -> sendPage(0))
            .bounds(leftPos + 172, topPos + 23, 16, 14).build());
        addRenderableWidget(Button.builder(Component.literal("›"), button -> sendPage(1))
            .bounds(leftPos + 192, topPos + 23, 16, 14).build());
        refreshFilterButtons();
    }

    private void syncSearch(String value) {
        if (value.equals(lastSentSearch)) return;
        if (value.length() == lastSentSearch.length() + 1 && value.startsWith(lastSentSearch)) {
            sendPage(0x10000 | value.charAt(value.length() - 1));
        } else if (lastSentSearch.length() == value.length() + 1 && lastSentSearch.startsWith(value)) {
            sendPage(0x20000);
        } else {
            sendPage(1000);
            for (int i = 0; i < value.length(); i++) sendPage(0x10000 | value.charAt(i));
        }
        lastSentSearch = value;
    }

    private void refreshFilterButtons() {
        if (sortButton == null) return;
        sortButton.setMessage(Component.literal(menu.isSortDescending() ? "9↓" : "1↑"));
        String namespace = menu.getSelectedModNamespace();
        modButton.setMessage(Component.literal(menu.getModIndex() == 0 ? "@Все" : "@" + compactNamespace(namespace)));
    }

    private String compactNamespace(String value) { return value.length() <= 4 ? value : value.substring(0, 4); }

    @Override protected void containerTick() { super.containerTick(); refreshFilterButtons(); }

    private void sendPage(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString) {
        if (slot.index < ItemTerminalBlockEntity.VISIBLE_SLOTS && !stack.isEmpty()) {
            String abbreviated = stack.getCount() > 1 ? NumberFormat.format(stack.getCount()) : null;
            super.renderSlotContents(graphics, stack, slot, abbreviated);
            return;
        }
        super.renderSlotContents(graphics, stack, slot, countString);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LumenGuiStyle.title(graphics, font, title, imageWidth, 9);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        String pageLabel = (menu.getPage() + 1) + "/" + (menu.getMaxPage() + 1);
        graphics.drawString(font, pageLabel, leftPos + pageLabelRight() - font.width(pageLabel), topPos + 132, 0xF0F0F0, true);
        if (modButton != null && modButton.isHovered())
            graphics.renderTooltip(font, menu.getModIndex() == 0
                ? Component.translatable("gui.techcraft.item_terminal.all_mods")
                : Component.translatable("gui.techcraft.item_terminal.selected_mod", menu.getSelectedModNamespace()), mouseX, mouseY);
        else if (sortButton != null && sortButton.isHovered())
            graphics.renderTooltip(font, Component.translatable(menu.isSortDescending()
                ? "gui.techcraft.item_terminal.sort_desc" : "gui.techcraft.item_terminal.sort_asc"), mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    protected int pageLabelRight() { return 211; }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) return super.keyPressed(keyCode, scanCode, modifiers);
        if (searchBox != null && (searchBox.keyPressed(keyCode, scanCode, modifiers) || searchBox.canConsumeInput())) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char codePoint, int modifiers) {
        return searchBox != null && searchBox.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }
}
