package TechCraft.item;

import TechCraft.TechCraft;
import TechCraft.block.ModBlocks;
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

    public static final Supplier<CreativeModeTab> TECHCRAFT_TAB = CREATIVE_MODE_TAB.register(
            "techcraft_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.Forge_Book.get()))
                    .title(Component.translatable("creativetab.techcraft.items"))
                    .displayItems(ModCreativeModTabs::addGeneralItems)
                    .build()
    );

    public static final Supplier<CreativeModeTab> TECHCRAFT_MATERIALS_TAB = CREATIVE_MODE_TAB.register(
            "techcraft_materials",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.Tin_Ingot.get()))
                    .title(Component.translatable("creativetab.techcraft.materials"))
                    .displayItems(ModCreativeModTabs::addMaterials)
                    .build()
    );

    private static final List<Supplier<? extends ItemLike>> GENERAL_ITEMS = List.of(
            ModItems.Forge_Book,
            ModItems.Cutter,
            ModItems.Forge_Hammer,
            ModItems.Tin_Plate
    );

    private static final List<Supplier<? extends ItemLike>> MATERIAL_ITEMS = List.of(
            ModItems.Raw_Tin,
            ModItems.Tin_Ingot,
            ModBlocks.Raw_Tin_Block,
            ModBlocks.Tin_Ore,
            ModBlocks.Deepslate_Tin_Ore,
            ModBlocks.Tin_Block
    );

    private static void addGeneralItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        GENERAL_ITEMS.forEach(item -> output.accept(item.get()));
    }

    private static void addMaterials(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        MATERIAL_ITEMS.forEach(item -> output.accept(item.get()));
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TAB.register(bus);
    }
}