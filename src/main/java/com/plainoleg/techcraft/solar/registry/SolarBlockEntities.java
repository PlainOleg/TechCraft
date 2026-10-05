package com.plainoleg.techcraft.solar.registry;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.solar.block.SolarPanelBankBlockEntity;
import com.plainoleg.techcraft.solar.block.SolarPanelBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration class for solar panel block entities.
 */
public class SolarBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TechCraft.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = 
        BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder.of(
            SolarPanelBlockEntity::new,
            // Block instances will be added during registration
            SolarBlocks.COPPER_SOLAR_PANEL.get(),
            SolarBlocks.SILICON_SOLAR_PANEL.get(),
            SolarBlocks.REINFORCED_SOLAR_PANEL.get(),
            SolarBlocks.PRISMATIC_SOLAR_PANEL.get(),
            SolarBlocks.RESONANT_SOLAR_PANEL.get(),
            SolarBlocks.FLUX_SOLAR_PANEL.get(),
            SolarBlocks.STELLAR_SOLAR_PANEL.get(),
            SolarBlocks.HELIOS_SOLAR_PANEL.get()
        ).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBankBlockEntity>> SOLAR_PANEL_BANK = 
        BLOCK_ENTITIES.register("solar_panel_bank", () -> BlockEntityType.Builder.of(
            SolarPanelBankBlockEntity::new,
            SolarBlocks.SOLAR_PANEL_BANK.get()
        ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
