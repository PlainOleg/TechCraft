package TechCraft.item;

import TechCraft.TechCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> TechCraft_Tab = CREATIVE_MODE_TAB.register("techcraft_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.Raw_Tin.get()))
                    .title(Component.translatable("creativetab.techcraft.items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.Raw_Tin);
                        output.accept(ModItems.Tin_Ingot);
                    }).build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TAB.register(bus);
    }
}
