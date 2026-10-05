package com.plainoleg.techcraft.lumenmesh;

import com.plainoleg.techcraft.item.armor.QuantumArmorItem;
import com.plainoleg.techcraft.lumenmesh.block.energy.EnergyBridgeBlockEntity;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlockEntities;
import com.plainoleg.techcraft.registry.ModItems;
import com.plainoleg.techcraft.solar.block.SolarPanelBankBlockEntity;
import com.plainoleg.techcraft.solar.registry.SolarBlockEntities;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** NeoForge capability wiring used by Lumen Mesh energy input. */
public final class LumenCapabilities {
    private LumenCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, LumenBlockEntities.ENERGY_BRIDGE.get(),
            (bridge, side) -> bridge.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, SolarBlockEntities.SOLAR_PANEL.get(),
            (panel, side) -> panel.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, SolarBlockEntities.SOLAR_PANEL_BANK.get(),
            (bank, side) -> bank.getEnergyStorage());
        
        // Register quantum armor items for charging
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, context) -> {
                if (stack.getItem() instanceof QuantumArmorItem armor) {
                    return armor.getEnergyStorage(stack);
                }
                return null;
            },
            ModItems.QUANTUM_HELMET.get(),
            ModItems.QUANTUM_CHESTPLATE.get(),
            ModItems.QUANTUM_LEGGINGS.get(),
            ModItems.QUANTUM_BOOTS.get()
        );
    }
}
