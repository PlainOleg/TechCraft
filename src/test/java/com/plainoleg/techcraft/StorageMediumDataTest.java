package com.plainoleg.techcraft;

import com.plainoleg.techcraft.block.alloysmelter.AlloySmelterBlockEntity;
import com.plainoleg.techcraft.item.armor.QuantumArmorItem;
import com.plainoleg.techcraft.lumenmesh.block.storage.PrismDriveBlockEntity;
import com.plainoleg.techcraft.lumenmesh.item.StorageMediumItem;
import com.plainoleg.techcraft.lumenmesh.network.LumenNetworkManager;
import com.plainoleg.techcraft.lumenmesh.registry.LumenBlocks;
import com.plainoleg.techcraft.lumenmesh.registry.LumenItems;
import com.plainoleg.techcraft.lumenmesh.storage.NetworkStorageService;
import com.plainoleg.techcraft.lumenmesh.storage.StorageMediumData;
import com.plainoleg.techcraft.registry.ModBlocks;
import com.plainoleg.techcraft.registry.ModItems;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

final class StorageMediumDataTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @org.junit.jupiter.api.Test
    void simulationAndOverflow() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        ItemStack medium = new ItemStack(LumenItems.STORAGE_PRISM_1K.get());
        StorageMediumItem mediumType = (StorageMediumItem) medium.getItem();
        ItemStack offered = new ItemStack(Items.STONE, 64);
        for (int i = 0; i < 15; i++) StorageMediumData.insert(medium, offered, false, registries);
        StorageMediumData.insert(medium, offered.copyWithCount(54), false, registries);
        check(StorageMediumData.insert(medium, offered, true, registries) == 10, "Simulated capacity");
        check(StorageMediumData.used(medium, registries) == 1014, "Simulation mutated medium");
        check(StorageMediumData.insert(medium, offered, false, registries) == 10, "Actual capacity");
        check(offered.getCount() == 64, "Insert mutated caller stack");
        check(StorageMediumData.extract(medium, offered, 4, true, registries).getCount() == 4, "Simulated extraction");
        check(StorageMediumData.used(medium, registries) == 1024, "Simulated extraction mutated medium");
        check(StorageMediumData.extract(medium, offered, 4, false, registries).getCount() == 4, "Actual extraction");
        ItemStack renamed = offered.copyWithCount(1);
        renamed.set(DataComponents.CUSTOM_NAME, Component.literal("different"));
        check(StorageMediumData.extract(medium, renamed, 1, false, registries).isEmpty(), "Component identity ignored");
        check(StorageMediumData.insert(medium, offered, false, registries) == 4, "Existing item capacity");

        ItemStack typed = new ItemStack(LumenItems.STORAGE_PRISM_1K.get());
        for (int i = 0; i < mediumType.typeLimit(); i++) {
            ItemStack named = offered.copyWithCount(1);
            named.set(DataComponents.CUSTOM_NAME, Component.literal("type-" + i));
            check(StorageMediumData.insert(typed, named, false, registries) == 1, "Distinct components merged");
        }
        check(StorageMediumData.insert(typed, offered, false, registries) == 0, "Type limit ignored");

        ItemStack infinite = new ItemStack(LumenItems.QUANTUM_STORAGE_PRISM.get());
        CompoundTag entry = new CompoundTag();
        entry.put("stack", offered.copyWithCount(1).save(registries));
        entry.putLong("count", Long.MAX_VALUE - 1);
        ListTag entries = new ListTag();
        entries.add(entry);
        CompoundTag root = new CompoundTag();
        root.put("lumen_storage", entries);
        infinite.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        check(StorageMediumData.insert(infinite, offered, true, registries) == 1, "Simulated overflow limit");
        check(StorageMediumData.insert(infinite, offered, false, registries) == 1, "Actual overflow limit");
        check(StorageMediumData.used(infinite, registries) == Long.MAX_VALUE, "Overflow destroyed stored items");
        check(StorageMediumData.insert(infinite, offered, false, registries) == 0, "Accepted beyond long range");
        check(StorageMediumData.extract(infinite, offered, Integer.MAX_VALUE, false, registries).getCount() == 64,
                "Extraction exceeded physical stack size");
    }

    @org.junit.jupiter.api.Test
    void networkAggregationAndPhysicalStackLimit() {
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var manager = new LumenNetworkManager();
        var networkId = manager.createNetwork(null);
        var network = manager.getNetwork(networkId);
        network.setEnergyStored(20);
        ItemStack stone = new ItemStack(Items.STONE, 64);
        for (int index = 0; index < 2; index++) {
            var drive = new PrismDriveBlockEntity(
                    new net.minecraft.core.BlockPos(index, 64, 0),
                    LumenBlocks.PRISM_DRIVE.get().defaultBlockState());
            ItemStack medium = new ItemStack(LumenItems.STORAGE_PRISM_1K.get());
            StorageMediumData.insert(medium, stone, false, registries);
            drive.getMedia().setStackInSlot(0, medium);
            manager.registerNode(drive);
            manager.addToNetwork(drive.getNodeId(), networkId);
        }
        var snapshot = NetworkStorageService.snapshot(manager, networkId, registries);
        check(snapshot.size() == 1 && snapshot.getFirst().count() == 128, "Drive aggregation lost or duplicated items");
        ItemStack simulated = NetworkStorageService.extract(
                manager, networkId, stone, Integer.MAX_VALUE, true, registries);
        check(simulated.getCount() == 64 && network.getEnergyStored() == 20, "Simulation changed energy or exceeded stack limit");
        ItemStack extracted = NetworkStorageService.extract(
                manager, networkId, stone, Integer.MAX_VALUE, false, registries);
        check(extracted.getCount() == 64 && network.getEnergyStored() == 18, "Extraction limit or operation cost");
        check(NetworkStorageService.snapshot(manager, networkId, registries).getFirst().count() == 64,
                "Actual extraction did not update aggregate");
    }

    @org.junit.jupiter.api.Test
    void armorEnergyBounds() {
        ItemStack stack = new ItemStack(ModItems.QUANTUM_CHESTPLATE.get());
        var armor = (QuantumArmorItem) stack.getItem();
        armor.setEnergy(stack, -10);
        check(armor.getEnergy(stack) == 0, "Negative armor energy");
        check(!armor.consumeEnergy(stack, -1), "Negative cost creates armor energy");
        check(armor.getEnergyStorage(stack).receiveEnergy(-1, false) == 0, "Negative capability transfer");
        armor.addEnergy(stack, Integer.MAX_VALUE);
        check(armor.getEnergy(stack) == armor.getMaxEnergy(), "Armor charging overflow");
        int before = armor.getEnergy(stack);
        check(armor.absorbDamage(stack, 2.5F) == 0 && armor.getEnergy(stack) == before - 250, "Fractional shield cost");
    }

    @org.junit.jupiter.api.Test
    void fluidDrainPreservesComponentsAndSimulation() {
        var smelter = new AlloySmelterBlockEntity(net.minecraft.core.BlockPos.ZERO,
                ModBlocks.ALLOY_SMELTER.get().defaultBlockState());
        var tank = smelter.getFluidHandler();
        var fluid = new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        fluid.set(DataComponents.CUSTOM_NAME, Component.literal("test fluid"));
        var execute = net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
        var simulate = net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;
        check(tank.fill(fluid, execute) == 1000, "Fluid fill");
        check(tank.drain(fluid.copyWithAmount(400), simulate).getAmount() == 400, "Typed fluid simulation");
        check(tank.getFluidInTank(0).getAmount() == 1000, "Simulation drained fluid");
        var drained = tank.drain(fluid.copyWithAmount(400), execute);
        check(drained.getAmount() == 400 && net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(fluid, drained),
                "Fluid components lost during drain");
        check(tank.getFluidInTank(0).getAmount() == 600, "Wrong remainder in fluid tank");
    }
}
