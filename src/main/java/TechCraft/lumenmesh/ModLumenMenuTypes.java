package TechCraft.lumenmesh;

import TechCraft.TechCraft;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import TechCraft.lumenmesh.menu.MeshCoreMenu;
import TechCraft.lumenmesh.menu.EnergyBridgeMenu;
import TechCraft.lumenmesh.menu.PrismDriveMenu;
import TechCraft.lumenmesh.menu.ItemTerminalMenu;
import TechCraft.lumenmesh.menu.StorageLinkMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<PrismDriveMenu>> PRISM_DRIVE =
        MENU_TYPES.register("prism_drive", () -> IMenuTypeExtension.create(PrismDriveMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ItemTerminalMenu>> ITEM_TERMINAL =
        MENU_TYPES.register("item_terminal", () -> IMenuTypeExtension.create(ItemTerminalMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<StorageLinkMenu>> STORAGE_LINK =
        MENU_TYPES.register("storage_link", () -> IMenuTypeExtension.create(StorageLinkMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
