package TechCraft.item.custom;

import TechCraft.item.ModArmorMaterials;
import TechCraft.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Квантовая броня — каждый предмет даёт отдельный бонус:
 *  - Шлем: ночное зрение
 *  - Нагрудник: скорость бега + разрешение полёта (mayfly)
 *  - Поножи: повышенный прыжок
 *  - Ботинки: медленное падение (игнор урона при падении)
 */
public class QuantumArmorItem extends ArmorItem {
    public QuantumArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.QUANTUM, type, properties);
    }

    public void onArmorTick(ItemStack stack, Level world, net.minecraft.world.entity.LivingEntity entity) {
        // shared logic; may or may not be called depending on mappings
        applyPerPieceEffects(stack, world, entity);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        // inventoryTick is reliably called for items; check if this stack is actually equipped in armor slot
        if (world.isClientSide()) return;
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity le)) return;

        // If the same ItemStack instance is present in the corresponding equipment slot, apply effects
        try {
            // head
            ItemStack headStack = le.getItemBySlot(EquipmentSlot.HEAD);
            if (headStack == stack) applyPerPieceEffects(stack, world, le);
            // chest
            ItemStack chestStack = le.getItemBySlot(EquipmentSlot.CHEST);
            if (chestStack == stack) applyPerPieceEffects(stack, world, le);
            // legs
            ItemStack legsStack = le.getItemBySlot(EquipmentSlot.LEGS);
            if (legsStack == stack) applyPerPieceEffects(stack, world, le);
            // feet
            ItemStack feetStack = le.getItemBySlot(EquipmentSlot.FEET);
            if (feetStack == stack) applyPerPieceEffects(stack, world, le);
        } catch (Throwable ignored) {
            // be defensive — do not break the game if something unexpected happens
        }
    }

    private void applyPerPieceEffects(ItemStack stack, Level world, net.minecraft.world.entity.LivingEntity entity) {
        if (world.isClientSide()) return; // server only

        boolean head = entity.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.QUANTUM_HELMET.get();
        boolean chest = entity.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.QUANTUM_CHESTPLATE.get();
        boolean legs = entity.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.QUANTUM_LEGGINGS.get();
        boolean feet = entity.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.QUANTUM_BOOTS.get();

        final int DURATION = 220;

        if (head) {
            entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, false, false));
        }

        if (chest) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 1, false, false));
            if (entity instanceof Player p) {
                if (!p.getAbilities().mayfly) {
                    p.getAbilities().mayfly = true;
                    if (p instanceof ServerPlayer sp) {
                        try {
                            sp.onUpdateAbilities();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        } else {
            if (entity instanceof Player p) {
                if (!p.isCreative() && !p.isSpectator()) {
                    if (p.getAbilities().mayfly) {
                        p.getAbilities().mayfly = false;
                        p.getAbilities().flying = false;
                        if (p instanceof ServerPlayer sp) {
                            try {
                                sp.onUpdateAbilities();
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            }
        }

        if (legs) {
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, DURATION, 2, false, false));
        }

        if (feet) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, false, false));
        }
    }
}
