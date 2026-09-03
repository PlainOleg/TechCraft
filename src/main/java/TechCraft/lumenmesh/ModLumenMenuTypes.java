package TechCraft.lumenmesh;

import TechCraft.TechCraft;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import TechCraft.lumenmesh.menu.MeshCoreMenu;
import TechCraft.lumenmesh.menu.EnergyBridgeMenu;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration class for Lumen Mesh menu types.
 */
public class ModLumenMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, TechCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<MeshCoreMenu>> MESH_CORE =
        MENU_TYPES.register("mesh_core", () -> IMenuTypeExtension.create(MeshCoreMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EnergyBridgeMenu>> ENERGY_BRIDGE =
        MENU_TYPES.register("energy_bridge", () -> IMenuTypeExtension.create(EnergyBridgeMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
