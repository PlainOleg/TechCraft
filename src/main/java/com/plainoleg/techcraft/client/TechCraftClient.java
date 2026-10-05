package com.plainoleg.techcraft.client;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.lumenmesh.client.EnergyBridgeScreen;
import com.plainoleg.techcraft.lumenmesh.client.ItemTerminalScreen;
import com.plainoleg.techcraft.lumenmesh.client.MeshCoreScreen;
import com.plainoleg.techcraft.lumenmesh.client.PrismDriveScreen;
import com.plainoleg.techcraft.lumenmesh.client.StorageLinkScreen;
import com.plainoleg.techcraft.lumenmesh.registry.LumenMenuTypes;
import com.plainoleg.techcraft.registry.ModMenuTypes;
import com.plainoleg.techcraft.solar.client.SolarPanelBankScreen;
import com.plainoleg.techcraft.solar.registry.SolarMenuTypes;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TechCraft.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TechCraft.MOD_ID, value = Dist.CLIENT)
public class TechCraftClient {
    public TechCraftClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ALLOY_SMELTER.get(), AlloySmelterScreen::new);
        event.register(SolarMenuTypes.SOLAR_PANEL_BANK.get(), SolarPanelBankScreen::new);
        event.register(LumenMenuTypes.MESH_CORE.get(), MeshCoreScreen::new);
        event.register(LumenMenuTypes.ENERGY_BRIDGE.get(), EnergyBridgeScreen::new);
        event.register(LumenMenuTypes.PRISM_DRIVE.get(), PrismDriveScreen::new);
        event.register(LumenMenuTypes.ITEM_TERMINAL.get(), ItemTerminalScreen::new);
        event.register(LumenMenuTypes.STORAGE_LINK.get(), StorageLinkScreen::new);
    }
}
