package com.plainoleg.techcraft.lumenmesh.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

/** Block item with a concise, capability-accurate Lumen Mesh tooltip. */
public final class LumenBlockItem extends BlockItem {
    private static final Set<String> IMPLEMENTED_BLOCKS = Set.of(
        "mesh_cable",
        "smart_cable",
        "dense_trunk",
        "cable_junction",
        "mesh_core",
        "energy_bridge",
        "prism_drive",
        "item_terminal",
        "storage_link"
    );

    private final String descriptionKey;
    private final String detailsKey;
    private final boolean implemented;

    public LumenBlockItem(String name, Block block, Item.Properties properties) {
        super(block, properties);
        descriptionKey = "tooltip.techcraft.lumen_block." + name + ".description";
        detailsKey = "tooltip.techcraft.lumen_block." + name + ".details";
        implemented = IMPLEMENTED_BLOCKS.contains(name);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
            implemented ? detailsKey : "tooltip.techcraft.lumen_block.not_implemented"
        ).withStyle(implemented ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
    }
}
