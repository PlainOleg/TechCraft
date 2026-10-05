package com.plainoleg.techcraft.lumenmesh.client;

import com.plainoleg.techcraft.client.NumberFormat;
import com.plainoleg.techcraft.lumenmesh.menu.StorageLinkMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StorageLinkScreen extends AbstractContainerScreen<StorageLinkMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techcraft","textures/gui/container/lumen_mesh/storage_link.png");
    private Button autoNetwork, autoExternal, filterMode;
    public StorageLinkScreen(StorageLinkMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=224;imageHeight=210;inventoryLabelY=117;}
    @Override protected void init(){super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.techcraft.storage_link.to_network"),b->send(0)).bounds(leftPos+16,topPos+78,92,18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.techcraft.storage_link.to_external"),b->send(1)).bounds(leftPos+116,topPos+78,92,18).build());
        autoNetwork=addRenderableWidget(Button.builder(Component.empty(),b->send(2)).bounds(leftPos+16,topPos+100,72,14).build());
        filterMode=addRenderableWidget(Button.builder(Component.empty(),b->send(4)).bounds(leftPos+84,topPos+100,56,14).build());
        autoExternal=addRenderableWidget(Button.builder(Component.empty(),b->send(3)).bounds(leftPos+136,topPos+100,72,14).build());
        refreshButtons();
    }
    @Override protected void containerTick(){super.containerTick();refreshButtons();}
    private void refreshButtons(){if(autoNetwork==null)return;autoNetwork.setMessage(Component.translatable("gui.techcraft.storage_link.auto_in_short",on(menu.autoIn())));autoExternal.setMessage(Component.translatable("gui.techcraft.storage_link.auto_out_short",on(menu.autoOut())));filterMode.setMessage(Component.translatable(menu.blacklist()?"gui.techcraft.storage_link.blacklist_short":"gui.techcraft.storage_link.whitelist_short"));}
    private Component on(boolean value){return Component.translatable(value?"gui.techcraft.storage_link.on":"gui.techcraft.storage_link.off");}
    private void send(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight,256,256);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){LumenGuiStyle.title(g,font,title,imageWidth);g.drawString(font,Component.translatable(menu.connected()?"gui.techcraft.storage_link.external_ok":"gui.techcraft.storage_link.external_missing"),16,27,menu.connected()?0x287A38:0xA03030,false);g.drawString(font,Component.translatable(menu.networked()?"gui.techcraft.storage_link.network_ok":"gui.techcraft.storage_link.network_missing"),16,37,menu.networked()?0x287A38:0xA03030,false);g.drawString(font,Component.translatable("gui.techcraft.storage_link.moved",NumberFormat.format(menu.lastMoved())),139,32,0x404040,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g,mx,my,pt);super.render(g,mx,my,pt);renderTooltip(g,mx,my);}
}
