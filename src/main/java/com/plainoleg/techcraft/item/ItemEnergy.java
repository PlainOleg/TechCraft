package com.plainoleg.techcraft.item;

import com.plainoleg.techcraft.registry.ModDataComponents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Общая логика энергии предметов: хранение, полоса заряда, {@link IEnergyStorage} для зарядки.
 * <p>
 * Энергия лежит в компоненте {@link ModDataComponents#ENERGY}: чтение не копирует NBT,
 * а полоса заряда отрисовывается каждый кадр. Предметы, сохранённые до появления компонента
 * (ключ {@code Energy} в CUSTOM_DATA), читаются как раньше и переносятся при первой записи.
 */
public final class ItemEnergy {
    private static final String LEGACY_KEY = "Energy";
    private static final int BAR_WIDTH = 13;

    private ItemEnergy() {
    }

    /** Текущая энергия; предмет без сохранённого значения считается полностью заряженным. */
    public static int get(ItemStack stack, int maxEnergy) {
        Integer energy = stack.get(ModDataComponents.ENERGY.get());
        if (energy == null) energy = readLegacy(stack);
        return energy == null ? maxEnergy : Math.clamp(energy, 0, maxEnergy);
    }

    public static void set(ItemStack stack, int energy, int maxEnergy) {
        stack.set(ModDataComponents.ENERGY.get(), Math.clamp(energy, 0, maxEnergy));
        if (legacyTag(stack) != null) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(LEGACY_KEY));
        }
    }

    /** Списывает {@code amount}, если энергии хватает. */
    public static boolean consume(ItemStack stack, int amount, int maxEnergy) {
        int current = get(stack, maxEnergy);
        if (amount < 0 || current < amount) return false;
        set(stack, current - amount, maxEnergy);
        return true;
    }

    public static void add(ItemStack stack, int amount, int maxEnergy) {
        if (amount > 0) set(stack, (int) Math.min(maxEnergy, (long) get(stack, maxEnergy) + amount), maxEnergy);
    }

    public static int barWidth(ItemStack stack, int maxEnergy) {
        return maxEnergy <= 0 ? 0 : (int) (get(stack, maxEnergy) / (float) maxEnergy * BAR_WIDTH);
    }

    /** Зелёный выше 50%, жёлтый выше 25%, иначе красный. */
    public static int barColor(ItemStack stack, int maxEnergy) {
        float ratio = maxEnergy <= 0 ? 0 : get(stack, maxEnergy) / (float) maxEnergy;
        if (ratio > 0.5f) return 0x00FF00;
        if (ratio > 0.25f) return 0xFFFF00;
        return 0xFF0000;
    }

    /** Хранилище только на приём: предмет можно зарядить, но не разрядить извне. */
    public static IEnergyStorage receiveOnlyStorage(ItemStack stack, int maxEnergy) {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int amount, boolean simulate) {
                int current = get(stack, maxEnergy);
                int accepted = Math.min(Math.max(0, amount), maxEnergy - current);
                if (!simulate && accepted > 0) set(stack, current + accepted, maxEnergy);
                return accepted;
            }

            @Override
            public int extractEnergy(int amount, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return get(stack, maxEnergy);
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

    private static Integer readLegacy(ItemStack stack) {
        CompoundTag tag = legacyTag(stack);
        return tag == null ? null : tag.getInt(LEGACY_KEY);
    }

    /** Тег CUSTOM_DATA, если в нём осталась энергия в старом формате, иначе null. */
    @SuppressWarnings("deprecation") // getUnsafe — только чтение, без копирования тега
    private static CompoundTag legacyTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag tag = customData.getUnsafe();
        return tag.contains(LEGACY_KEY) ? tag : null;
    }
}
