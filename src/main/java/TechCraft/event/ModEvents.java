package TechCraft.event;

import TechCraft.TechCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TechCraft.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onCraftEvent(PlayerEvent.ItemCraftedEvent event) {
        var player = event.getEntity();

        if (player.level().isClientSide) {return;}

        var inv = event.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            ResourceLocation myTag = ResourceLocation.fromNamespaceAndPath("techcraft", "forge_tools");
            if (!stack.isEmpty() && stack.is(ItemTags.create(myTag))) {
                // создаём копию с увеличенным уроном
                ItemStack newStack = stack.copy();
                newStack.setDamageValue(stack.getDamageValue() + 1);

                // пробуем вернуть в слот того же индекса в основном инвентаре
                if (i < player.getInventory().getContainerSize()) {
                    player.getInventory().setItem(i, newStack);
                } else {
                    // если слот недоступен — выдаём в инвентарь или кидаем на землю
                    if (!player.addItem(newStack)) {
                        player.drop(newStack, false);
                    }
                }

                // сообщение игроку
                player.sendSystemMessage(Component.literal(
                        "Инструмент получил урон! Прочность: " +
                                (stack.getMaxDamage() - stack.getDamageValue())
                ));
            }
        }
    }
}