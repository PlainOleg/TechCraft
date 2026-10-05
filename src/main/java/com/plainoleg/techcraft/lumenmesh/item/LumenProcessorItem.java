package com.plainoleg.techcraft.lumenmesh.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A processing component used by Lumen Mesh machines. */
public class LumenProcessorItem extends Item {
    private final int operationsPerSecond;
    private final int tier;

    public LumenProcessorItem(Properties properties, int operationsPerSecond, int tier) {
        super(properties);
        this.operationsPerSecond = operationsPerSecond;
        this.tier = tier;
    }

    public int operationsPerSecond() {
        return operationsPerSecond;
    }

    public int tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ChatFormatting powerColor = tier <= 3
                ? ChatFormatting.AQUA
                : tier <= 6 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GOLD;

        tooltip.add(Component.translatable("tooltip.techcraft.lumen_processor.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("◆ ").withStyle(powerColor)
                .append(Component.translatable("tooltip.techcraft.lumen_processor.operations", operationsPerSecond)
                        .withStyle(powerColor, ChatFormatting.BOLD)));
    }
}
