package com.plainoleg.techcraft.lumenmesh.menu;

import com.plainoleg.techcraft.lumenmesh.registry.LumenItems;
import com.plainoleg.techcraft.lumenmesh.storage.NetworkStorageService;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ItemTerminalMenuTest {
    @Test
    void storedModsContainOnlyPresentNamespacesInDisplayOrder() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        var stacks = List.of(
                new NetworkStorageService.NetworkStack(new ItemStack(LumenItems.STORAGE_PRISM_1K.get()), 1),
                new NetworkStorageService.NetworkStack(new ItemStack(Items.STONE), 64),
                new NetworkStorageService.NetworkStack(new ItemStack(Items.DIRT), 32));

        assertEquals(List.of("minecraft", "techcraft"), ItemTerminalMenu.collectStoredMods(stacks));
        assertEquals(List.of(), ItemTerminalMenu.collectStoredMods(List.of()));
    }
}
