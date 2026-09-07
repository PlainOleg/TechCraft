package TechCraft;

import TechCraft.block.AlloySmelterScreen;
import TechCraft.block.ModMenuTypes;
import TechCraft.solar.ModSolarMenuTypes;
import TechCraft.solar.SolarPanelBankScreen;
import TechCraft.lumenmesh.ModLumenMenuTypes;
import TechCraft.lumenmesh.client.MeshCoreScreen;
import TechCraft.lumenmesh.client.EnergyBridgeScreen;
import TechCraft.lumenmesh.client.PrismDriveScreen;
import TechCraft.lumenmesh.client.ItemTerminalScreen;
import TechCraft.lumenmesh.client.StorageLinkScreen;
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
        event.register(ModSolarMenuTypes.SOLAR_PANEL_BANK.get(), SolarPanelBankScreen::new);
        event.register(ModLumenMenuTypes.MESH_CORE.get(), MeshCoreScreen::new);
        event.register(ModLumenMenuTypes.ENERGY_BRIDGE.get(), EnergyBridgeScreen::new);
        event.register(ModLumenMenuTypes.PRISM_DRIVE.get(), PrismDriveScreen::new);
        event.register(ModLumenMenuTypes.ITEM_TERMINAL.get(), ItemTerminalScreen::new);
        event.register(ModLumenMenuTypes.STORAGE_LINK.get(), StorageLinkScreen::new);
    }
}
