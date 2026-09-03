package TechCraft.solar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration class for solar panel block entities.
 */
public class ModSolarBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TechCraft.TechCraft.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = 
        BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder.of(
            SolarPanelBlockEntity::new,
            // Block instances will be added during registration
            ModSolarBlocks.COPPER_SOLAR_PANEL.get(),
            ModSolarBlocks.SILICON_SOLAR_PANEL.get(),
            ModSolarBlocks.REINFORCED_SOLAR_PANEL.get(),
            ModSolarBlocks.PRISMATIC_SOLAR_PANEL.get(),
            ModSolarBlocks.RESONANT_SOLAR_PANEL.get(),
            ModSolarBlocks.FLUX_SOLAR_PANEL.get(),
            ModSolarBlocks.STELLAR_SOLAR_PANEL.get(),
            ModSolarBlocks.HELIOS_SOLAR_PANEL.get()
        ).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBankBlockEntity>> SOLAR_PANEL_BANK = 
        BLOCK_ENTITIES.register("solar_panel_bank", () -> BlockEntityType.Builder.of(
            SolarPanelBankBlockEntity::new,
            ModSolarBlocks.SOLAR_PANEL_BANK.get()
        ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
