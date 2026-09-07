package TechCraft.item.custom;

import TechCraft.item.ModArmorMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

/**
 * Квантовая броня — каждый предмет даёт отдельный бонус:
 * - Шлем: ночное зрение
 * - Нагрудник: скорость бега + разрешение полёта (mayfly)
 * - Поножи: повышенный прыжок
 * - Ботинки: медленное падение (игнор урона при падении)
 * <p>
 * Все части брони используют энергию для защиты от урона и специальных функций.
 */
public class QuantumArmorItem extends ArmorItem {
    private static final String ENERGY_KEY = "Energy";
    private final int maxEnergy;
    private final int energyPerDamagePoint;
    private final int flightEnergyCostPerTick;

    public QuantumArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.QUANTUM, type, properties);
        this.maxEnergy = getMaxEnergyForSlot(type);
        this.energyPerDamagePoint = 100; // 100 энергии за 1 очко защиты
        this.flightEnergyCostPerTick = 10; // 10 энергии за тик полёта
    }

    private int getMaxEnergyForSlot(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 100000;
            case CHESTPLATE -> 200000;
            case LEGGINGS -> 150000;
            case BOOTS -> 100000;
            default -> 100000;
        };
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // Показываем полосу прочности для отображения энергии
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int currentEnergy = getEnergy(stack);
        return (int) ((currentEnergy / (float) maxEnergy) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int currentEnergy = getEnergy(stack);
        float ratio = currentEnergy / (float) maxEnergy;

        if (ratio > 0.5f) {
            return 0x00FF00; // Зеленый
        } else if (ratio > 0.25f) {
            return 0xFFFF00; // Желтый
        } else {
            return 0xFF0000; // Красный
        }
    }

    @Override
    @Nonnull
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        CompoundTag tag = new CompoundTag();
        tag.putInt(ENERGY_KEY, maxEnergy);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClientSide || !(entity instanceof net.minecraft.world.entity.LivingEntity living)
                || living.getItemBySlot(getEquipmentSlot()) != stack) return;

        switch (getType()) {
            case HELMET -> refreshEffect(living, MobEffects.NIGHT_VISION, 0);
            case CHESTPLATE -> refreshEffect(living, MobEffects.MOVEMENT_SPEED, 1);
            case LEGGINGS -> refreshEffect(living, MobEffects.JUMP, 2);
            case BOOTS -> refreshEffect(living, MobEffects.SLOW_FALLING, 0);
            default -> {
            }
        }
    }

    private static void refreshEffect(net.minecraft.world.entity.LivingEntity entity,
                                      net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        MobEffectInstance current = entity.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier
                || current.getAmplifier() == amplifier && !current.isInfiniteDuration() && current.getDuration() <= 200) {
            entity.addEffect(new MobEffectInstance(effect, 220, amplifier, false, false));
        }
    }

    public int getFlightEnergyCostPerTick() {
        return flightEnergyCostPerTick;
    }

    /**
     * Получает текущую энергию брони.
     */
    public int getEnergy(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return maxEnergy; // Если NBT нет - считаем что полная энергия (новый предмет)
        }
        CompoundTag tag = customData.copyTag();
        return tag.contains(ENERGY_KEY) ? Math.clamp(tag.getInt(ENERGY_KEY), 0, maxEnergy) : maxEnergy;
    }

    /**
     * Устанавливает энергию брони.
     */
    public void setEnergy(ItemStack stack, int energy) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        tag.putInt(ENERGY_KEY, Math.clamp(energy, 0, maxEnergy));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Получает максимальную энергию брони.
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * Потребляет энергию.
     *
     * @return true если энергии хватило, false если нет
     */
    public boolean consumeEnergy(ItemStack stack, int amount) {
        int currentEnergy = getEnergy(stack);
        if (amount < 0 || currentEnergy < amount) {
            return false;
        }
        setEnergy(stack, currentEnergy - amount);
        return true;
    }

    /**
     * Добавляет энергию в броню.
     */
    public void addEnergy(ItemStack stack, int amount) {
        if (amount > 0) setEnergy(stack, (int) Math.min(maxEnergy, (long) getEnergy(stack) + amount));
    }

    /**
     * Проверяет и поглощает урон энергией брони.
     * Вызывается из события LivingDamageEvent.
     */
    public float absorbDamage(ItemStack stack, float damage) {
        int energy = getEnergy(stack);
        if (energy <= 0) {
            return damage; // Энергии нет - урон проходит полностью
        }

        // Каждая единица урона поглощается за счёт энергии.
        // Если энергии хватает, урон поглощается полностью.
        int energyToConsume = (int) Math.ceil(damage * energyPerDamagePoint);
        int actualEnergyConsumed = Math.min(energy, energyToConsume);

        if (actualEnergyConsumed > 0) {
            consumeEnergy(stack, actualEnergyConsumed);
        }

        // Рассчитываем сколько урона поглотилось
        float absorbedDamage = actualEnergyConsumed / (float) energyPerDamagePoint;
        float remainingDamage = Math.max(0, damage - absorbedDamage);

        return remainingDamage;
    }

    /**
     * Создаёт IEnergyStorage для зарядки брони.
     */
    public IEnergyStorage getEnergyStorage(ItemStack stack) {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int amount, boolean simulate) {
                int currentEnergy = getEnergy(stack);
                int space = maxEnergy - currentEnergy;
                int accepted = Math.min(Math.max(0, amount), space);
                if (!simulate && accepted > 0) {
                    setEnergy(stack, currentEnergy + accepted);
                }
                return accepted;
            }

            @Override
            public int extractEnergy(int amount, boolean simulate) {
                return 0; // Броня не отдаёт энергию
            }

            @Override
            public int getEnergyStored() {
                return getEnergy(stack);
            }

            @Override
            public int getMaxEnergyStored() {
                return maxEnergy;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }
}
