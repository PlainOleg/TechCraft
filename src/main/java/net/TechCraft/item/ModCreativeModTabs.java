package net.TechCraft.item;

import net.TechCraft.TechCraft;
import net.TechCraft.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> TechCraft_Tab;

    static {
        TechCraft_Tab = CREATIVE_MODE_TAB.register(
                "techcraft_tab",
                () -> CreativeModeTab.builder()
                        .icon(() -> new ItemStack(ModItems.Raw_Tin.get()))
                        .title(Component.translatable("creativetab.techcraft.items"))
                        .displayItems(ModCreativeModTabs::addItems)
                        .build()
        );
    }

    private static final List<Supplier<? extends ItemLike>> TAB_ITEMS = List.of(
            ModItems.Raw_Tin,
            ModItems.Tin_Ingot,
            ModBlocks.Raw_Tin_Block,
            ModBlocks.Tin_Ore,
            ModBlocks.Deepslate_Tin_Ore,
            ModBlocks.Tin_Block
    );

    private static void addItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        TAB_ITEMS.forEach(item -> output.accept(item.get()));
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TAB.register(bus);
    }
}
