package TechCraft.event;

import TechCraft.TechCraft;
import TechCraft.item.custom.DamageOnCraftUseItem;
import TechCraft.item.custom.HammerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс обработки событий мода TechCraft.
 * Содержит обработчики для инструментов, тултипов и других игровых событий.
 */
@EventBusSubscriber(modid = TechCraft.MOD_ID)
public class ModEvents {
    // Hammer
    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();
    @SubscribeEvent
    public static void onHammerUsage(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level() == null) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand == null || mainHand.isEmpty()) {
            return;
        }

        if (!(mainHand.getItem() instanceof HammerItem hammer) || !(player instanceof ServerPlayer sp)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (pos == null || HARVESTED_BLOCKS.contains(pos)) {
            return;
        }

        try {
            for (BlockPos extraPos : HammerItem.getBlocksToBeDestroyed(pos, sp, mainHand)) {
                if (extraPos == null || HARVESTED_BLOCKS.contains(extraPos)) {
                    continue;
                }

                var state = event.getLevel().getBlockState(extraPos);
                if (state != null && hammer.isCorrectToolForDrops(mainHand, state)) {
                    HARVESTED_BLOCKS.add(extraPos);
                    sp.gameMode.destroyBlock(extraPos);
                    HARVESTED_BLOCKS.remove(extraPos);
                }
            }
        } catch (Exception e) {
            TechCraft.LOGGER.error("Error during hammer usage at position {}: {}", pos, e.getMessage());
        } finally {
            HARVESTED_BLOCKS.clear();
        }
    }

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
    }

    // After Craft
    // public static void onCraftEvent(PlayerEvent.ItemCraftedEvent event) {}
}