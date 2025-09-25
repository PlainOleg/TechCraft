package net.TechCraft;

import net.TechCraft.block.ModBlocks;
import net.TechCraft.item.ModCreativeModTabs;
import net.TechCraft.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(TechCraft.MOD_ID)
public class TechCraft {
    public static final String MOD_ID = "techcraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    public TechCraft(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.Raw_Tin);
            event.accept(ModItems.Tin_Ingot);
        }

        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS){
           event.accept(ModBlocks.Raw_Tin_Block);
           event.accept(ModBlocks.Tin_Ore);
           event.accept(ModBlocks.Deepslate_Tin_Ore);
        }

        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){
            event.accept(ModBlocks.Tin_Block);

        }

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}