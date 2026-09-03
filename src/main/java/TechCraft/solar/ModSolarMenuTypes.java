package TechCraft.solar;

import TechCraft.TechCraft;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration class for solar panel menu types.
 */
public class ModSolarMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, TechCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SolarPanelBankMenu>> SOLAR_PANEL_BANK = 
        MENU_TYPES.register("solar_panel_bank", () -> 
            IMenuTypeExtension.create(SolarPanelBankMenu::new)
        );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
