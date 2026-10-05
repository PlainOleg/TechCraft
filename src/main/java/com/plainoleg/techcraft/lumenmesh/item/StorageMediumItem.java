package com.plainoleg.techcraft.lumenmesh.item;

import com.plainoleg.techcraft.client.NumberFormat;
import com.plainoleg.techcraft.lumenmesh.storage.StorageMediumData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Physical storage medium installed into a Prism Drive.
 */
public class StorageMediumItem extends Item {
    private static final int TICKS_PER_SECOND = 20;

    private final long capacity;
    private final int typeLimit;
    private final int operationsPerTick;
    private final int craftingJobs;

    public StorageMediumItem(Properties properties, long capacity, int typeLimit, int operationsPerTick, int craftingJobs) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
        this.typeLimit = typeLimit;
        this.operationsPerTick = operationsPerTick;
        this.craftingJobs = craftingJobs;
    }

    public long capacity() {
        return capacity;
    }

    public int typeLimit() {
        return typeLimit;
    }

    public int operationsPerTick() {
        return operationsPerTick;
    }

    public int operationsPerSecond() {
        return operationsPerTick * TICKS_PER_SECOND;
    }

    public int craftingJobs() {
        return craftingJobs;
    }

    public boolean isInfinite() {
        return capacity == Long.MAX_VALUE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var statistics = StorageMediumData.statistics(stack, context.registries());
        long used = statistics.used();
        int types = statistics.types();
        Component capacityText = isInfinite()
                ? Component.translatable("tooltip.techcraft.lumen_storage.infinite")
                : Component.translatable("tooltip.techcraft.lumen_storage.item_count", NumberFormat.format(capacity));

        tooltip.add(Component.translatable("tooltip.techcraft.lumen_storage.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.techcraft.lumen_storage.capacity", capacityText)
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.techcraft.lumen_storage.stored", NumberFormat.format(used))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.techcraft.lumen_storage.types", types, typeLimit)
                .withStyle(ChatFormatting.AQUA));
        if (isInfinite()) {
            tooltip.add(Component.translatable("tooltip.techcraft.lumen_storage.speed", operationsPerSecond())
                    .withStyle(ChatFormatting.GREEN));
        }
    }
}
