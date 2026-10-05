package com.plainoleg.techcraft.event;

import com.plainoleg.techcraft.TechCraft;
import com.plainoleg.techcraft.item.tool.DrillItem;
import com.plainoleg.techcraft.item.tool.HammerItem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared area-mining lifecycle, including reentrant BreakEvents for extra blocks.
 */
@EventBusSubscriber(modid = TechCraft.MOD_ID)
public final class ToolMiningEvents {
    private static final Set<UUID> MINING_PLAYERS = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack tool = player.getMainHandItem();
        DrillItem drill = tool.getItem() instanceof DrillItem item ? item : null;
        HammerItem hammer = tool.getItem() instanceof HammerItem item ? item : null;
        if (drill == null && hammer == null) return;

        // Charge every actual block event, including recursive area breaks.
        if (drill != null && !drill.consumeEnergy(tool)) {
            event.setCanceled(true);
            return;
        }
        if (!MINING_PLAYERS.add(player.getUUID())) return;

        try {
            List<BlockPos> positions = drill != null
                    ? DrillItem.getBlocksToBeDestroyed(event.getPos(), player, tool)
                    : HammerItem.getBlocksToBeDestroyed(event.getPos(), player, tool);
            for (BlockPos position : positions) {
                if (tool.isEmpty() || player.getMainHandItem() != tool) break;
                if (drill != null && drill.getEnergy(tool) < drill.getEnergyCostPerBlock()) break;
                var state = event.getLevel().getBlockState(position);
                if (state.isAir() || state.getDestroySpeed(event.getLevel(), position) < 0) continue;
                if (hammer == null || hammer.isCorrectToolForDrops(tool, state)) {
                    player.gameMode.destroyBlock(position);
                }
            }
        } finally {
            MINING_PLAYERS.remove(player.getUUID());
        }
    }
}
