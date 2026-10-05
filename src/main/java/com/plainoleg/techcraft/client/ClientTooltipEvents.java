package com.plainoleg.techcraft.client;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.item.armor.QuantumArmorItem;
import com.plainoleg.techcraft.item.tool.DamageOnCraftUseItem;
import com.plainoleg.techcraft.item.tool.DrillItem;
import com.plainoleg.techcraft.item.tool.HammerItem;
import com.plainoleg.techcraft.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Client-only tooltip presentation; never loaded by a dedicated server.
 */
@EventBusSubscriber(modid = TechCraft.MOD_ID, value = Dist.CLIENT)
public final class ClientTooltipEvents {
    // ToolTips
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();

        // === Hammers ===
        if (stack.getItem() instanceof HammerItem hammer) {
            int size = hammer.getMiningSize();

            if (Screen.hasShiftDown()) {
                if (size > 1) {
                    lines.add(Component.translatable("tooltip.techcraft.hammer.area")
                            .withStyle(ChatFormatting.GRAY));

                    lines.add(Component.literal("  " + size + "×" + size + " ")
                            .append(Component.translatable("tooltip.techcraft.hammer.area.normal"))
                            .withStyle(ChatFormatting.AQUA));
                    lines.add(Component.literal("  1×1 ")
                            .append(Component.translatable("tooltip.techcraft.hammer.area.sneak"))
                            .withStyle(ChatFormatting.YELLOW));

                    lines.add(Component.translatable("tooltip.techcraft.for_craft")
                            .withStyle(ChatFormatting.GRAY));
                    lines.add(Component.translatable("tooltip.techcraft.damages_on_craft")
                            .withStyle(ChatFormatting.RED));
                }


            } else {
                lines.add(Component.translatable("tooltip.techcraft.hammer.area")
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable("tooltip.techcraft.hold_shift")
                        .withStyle(ChatFormatting.DARK_GRAY));

                lines.add(Component.translatable("tooltip.techcraft.for_craft")
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable("tooltip.techcraft.damages_on_craft")
                        .withStyle(ChatFormatting.RED));
            }
        }

        // === Damage On Craft Items ===
        else if (stack.getItem() instanceof DamageOnCraftUseItem) {
            lines.add(Component.translatable("tooltip.techcraft.for_craft")
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.techcraft.damages_on_craft")
                    .withStyle(ChatFormatting.RED));
        }

        // === Drills ===
        else if (stack.getItem() instanceof DrillItem drill) {
            int currentEnergy = drill.getEnergy(stack);
            int maxEnergy = drill.getMaxEnergy();
            int energyCost = drill.getEnergyCostPerBlock();
            int size = drill.getMiningSize();

            if (Screen.hasShiftDown()) {
                // Энергия
                lines.add(Component.literal("⚡ ")
                        .append(Component.translatable("tooltip.techcraft.drill.energy", NumberFormat.format(currentEnergy), NumberFormat.format(maxEnergy)))
                        .withStyle(ChatFormatting.GOLD));

                // Потребление
                lines.add(Component.literal("⚡ ")
                        .append(Component.translatable("tooltip.techcraft.drill.cost", NumberFormat.format(energyCost)))
                        .withStyle(ChatFormatting.GOLD));

                // Режимы добычи
                if (size > 1) {
                    lines.add(Component.translatable("tooltip.techcraft.drill.area")
                            .withStyle(ChatFormatting.GRAY));

                    lines.add(Component.literal("  " + size + "×" + size + " ")
                            .append(Component.translatable("tooltip.techcraft.hammer.area.normal"))
                            .withStyle(ChatFormatting.AQUA));
                    lines.add(Component.literal("  1×1 ")
                            .append(Component.translatable("tooltip.techcraft.hammer.area.sneak"))
                            .withStyle(ChatFormatting.YELLOW));
                }

            } else {
                // Краткая информация без Shift
                lines.add(Component.literal("⚡ ")
                        .append(Component.translatable("tooltip.techcraft.drill.energy", NumberFormat.format(currentEnergy), NumberFormat.format(maxEnergy)))
                        .withStyle(ChatFormatting.GOLD));

                lines.add(Component.translatable("tooltip.techcraft.hold_shift")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        // === Quantum Armor ===
        else if (stack.getItem() instanceof QuantumArmorItem armor) {
            int currentEnergy = armor.getEnergy(stack);
            int maxEnergy = armor.getMaxEnergy();
            float energyRatio = Math.clamp(currentEnergy / (float) maxEnergy, 0.0F, 1.0F);
            int energyPercent = Math.round(energyRatio * 100.0F);
            ChatFormatting energyColor = energyRatio > 0.5F
                    ? ChatFormatting.GREEN
                    : energyRatio > 0.25F ? ChatFormatting.YELLOW : ChatFormatting.RED;

            if (Screen.hasShiftDown()) {
                lines.add(Component.empty());
                lines.add(Component.literal("✦ ")
                        .append(Component.translatable("tooltip.techcraft.quantum_armor.system"))
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

                int filledSegments = Math.round(energyRatio * 10.0F);
                MutableComponent energyBar = Component.literal("  ⚡ ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("▰".repeat(filledSegments)).withStyle(energyColor))
                        .append(Component.literal("▱".repeat(10 - filledSegments)).withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("  " + energyPercent + "%").withStyle(energyColor));
                lines.add(energyBar);
                lines.add(Component.literal("     " + NumberFormat.format(currentEnergy) + " / "
                        + NumberFormat.format(maxEnergy) + " FE").withStyle(ChatFormatting.GRAY));

                lines.add(Component.empty());
                lines.add(Component.literal("◆ ").withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable("tooltip.techcraft.quantum_armor.protection")
                                .withStyle(ChatFormatting.AQUA)));
                lines.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.techcraft.quantum_armor.protection.detail"))
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.techcraft.quantum_armor.protection.cost"))
                        .withStyle(ChatFormatting.DARK_GRAY));

                String abilityKey = stack.getItem() == ModItems.QUANTUM_HELMET.get()
                        ? "tooltip.techcraft.quantum_armor.helmet"
                        : stack.getItem() == ModItems.QUANTUM_CHESTPLATE.get()
                        ? "tooltip.techcraft.quantum_armor.chestplate"
                        : stack.getItem() == ModItems.QUANTUM_LEGGINGS.get()
                        ? "tooltip.techcraft.quantum_armor.leggings"
                        : "tooltip.techcraft.quantum_armor.boots";
                lines.add(Component.literal("◆ ").withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.translatable("tooltip.techcraft.quantum_armor.module")
                                .withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.translatable(abilityKey).withStyle(ChatFormatting.GREEN)));

            } else {
                lines.add(Component.literal("⚡ ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(NumberFormat.format(currentEnergy) + " / "
                                + NumberFormat.format(maxEnergy) + " FE ").withStyle(energyColor))
                        .append(Component.literal("[" + energyPercent + "%]").withStyle(ChatFormatting.DARK_GRAY)));

                lines.add(Component.translatable("tooltip.techcraft.hold_shift")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

}
