package com.plainoleg.techcraft.event;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.item.armor.QuantumArmorItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Flight is coordinated once per player, including ticks after the armor is removed.
 */
@EventBusSubscriber(modid = TechCraft.MOD_ID)
public final class QuantumArmorEvents {
    private static final String GRANTED_FLIGHT = "techcraft_quantum_flight";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        if (player.isCreative() || player.isSpectator()) {
            data.remove(GRANTED_FLIGHT);
            return;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean powered = false;
        if (chest.getItem() instanceof QuantumArmorItem armor) {
            powered = armor.getEnergy(chest) > 0;
            if (powered && player.getAbilities().flying) {
                powered = armor.consumeEnergy(chest, armor.getFlightEnergyCostPerTick())
                        && armor.getEnergy(chest) > 0;
            }
        }

        if (powered && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            data.putBoolean(GRANTED_FLIGHT, true);
            player.onUpdateAbilities();
        } else if (!powered && data.getBoolean(GRANTED_FLIGHT)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            data.remove(GRANTED_FLIGHT);
            player.onUpdateAbilities();
        }
    }
}
