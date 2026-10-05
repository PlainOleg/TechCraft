package com.plainoleg.techcraft.item;

import com.plainoleg.techcraft.TechCraft;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;

/**
 * Книга-справочник. Отрисовкой занимается Patchouli; мод объявлен как необязательная зависимость,
 * поэтому без него книга сообщает игроку, что нужен Patchouli, а не роняет игру.
 */
public class ForgeBook extends Item {
    private static final String PATCHOULI_MOD_ID = "patchouli";
    private static final ResourceLocation BOOK_ID = ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, "forge_book");

    public ForgeBook(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (ModList.get().isLoaded(PATCHOULI_MOD_ID)) {
                PatchouliCompat.openBook(serverPlayer);
            } else {
                serverPlayer.displayClientMessage(Component.translatable("message.techcraft.forge_book.patchouli_missing"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    /** Классы Patchouli загружаются только при вызове, то есть только когда мод установлен. */
    private static final class PatchouliCompat {
        static void openBook(ServerPlayer player) {
            PatchouliAPI.get().openBookGUI(player, BOOK_ID);
        }
    }
}
