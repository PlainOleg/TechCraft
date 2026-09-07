package TechCraft.event;

import TechCraft.TechCraft;
import TechCraft.item.custom.QuantumArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Server-side combat events. Client presentation lives in ClientTooltipEvents.
 */
@EventBusSubscriber(modid = TechCraft.MOD_ID)
public final class ModEvents {
    private static final float QUANTUM_ARMOR_MAX_ABSORBABLE_DAMAGE = 1000.0F;

    // Quantum Armor Damage Absorption
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // Extremely large hits bypass the quantum shield entirely.
        if (event.getOriginalDamage() >= QUANTUM_ARMOR_MAX_ABSORBABLE_DAMAGE) {
            return;
        }

        float remainingDamage = event.getNewDamage();
        if (remainingDamage <= 0.0F) {
            return;
        }

        for (ItemStack armorStack : event.getEntity().getArmorSlots()) {
            if (armorStack.getItem() instanceof QuantumArmorItem quantumArmor) {
                remainingDamage = quantumArmor.absorbDamage(armorStack, remainingDamage);
            }
        }

        event.setNewDamage(remainingDamage);
    }
}
