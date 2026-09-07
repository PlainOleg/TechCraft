package TechCraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.IMenuProviderExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class AlloySmelterBlockEntity extends BlockEntity implements MenuProvider, IMenuProviderExtension {

    // Enum для типов улучшений (используется в SmelterUpgradeItem)
    public enum UpgradeType {
        CAPACITY_BOOST("capacity"),
        TEMP_BOOST("heat"),
        EFFICIENCY("efficiency");

        private final String name;

        UpgradeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // Базовые характеристики
    private static final int BASE_CAPACITY = 8000; // mB
    private static final int HEATING_RATE = 2; // °C per tick
    private static final float COOLING_RATE = 0.1f; // °C per tick

    // Слоты: 0-2 = Входные предметы, 3 = Топливо
    private static final int INPUT_SLOTS_COUNT = 3;
    private static final int FUEL_SLOT = 3;
    private static final int TOTAL_SLOTS = 4;

    // Установленные тиры улучшений (1 - базовое состояние)
    private int capacityTier = 1;
    private int heatTier = 1;
    private int efficiencyTier = 1;

    // Инвентарь
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final AlloyFluidHandler fluidHandler = new AlloyFluidHandler();

    // Текущее состояние
    private float currentTemperature = 0.0f; // Базовая температура
    private int targetTemperature = 0;
    private int fuelBurnTime = 0;
    private int maxFuelBurnTime = 0;

    public AlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_SMELTER.get(), pos, state);
    }

    // Метод применения улучшения с защитой от понижения тира
    public boolean applyUpgrade(UpgradeType type, int tier) {
        if (tier < 1 || tier > 3) {
            return false;
        }
        switch (type) {
            case CAPACITY_BOOST:
                if (tier <= capacityTier) return false;
                capacityTier = tier;
                break;
            case TEMP_BOOST:
                if (tier <= heatTier) return false;
                heatTier = tier;
                break;
            case EFFICIENCY:
                if (tier <= efficiencyTier) return false;
                efficiencyTier = tier;
                break;
            default:
                return false;
        }
        setChanged();
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.techcraft.alloy_smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AlloySmelterMenu(containerId, playerInventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public float getCurrentTemperature() {
        return currentTemperature;
    }

    public int getTargetTemperature() {
        return targetTemperature;
    }

    public int getFuelBurnTime() {
        return fuelBurnTime;
    }

    public void setFuelBurnTime(int fuelBurnTime) {
        this.fuelBurnTime = fuelBurnTime;
    }

    public int getMaxFuelBurnTime() {
        return maxFuelBurnTime;
    }

    public void setMaxFuelBurnTime(int maxFuelBurnTime) {
        this.maxFuelBurnTime = maxFuelBurnTime;
    }

    public int getMaxTemperature() {
        // Зависимость максимальной температуры от тира нагрева
        return switch (heatTier) {
            case 2 -> 1000;
            case 3 -> 2000;
            default -> 400; // Базовый T1
        };
    }

    public int getCurrentCapacity() {
        return BASE_CAPACITY * capacityTier;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlloySmelterBlockEntity entity) {
        entity.tick();
    }

    private void tick() {
        int previousBurnTime = fuelBurnTime;
        float previousTemperature = currentTemperature;
        handleFuel();
        handleTemperature();
        handleMeltingAndAlloying();

        // Keep active progress durable without dirtying the chunk on every server tick.
        if ((fuelBurnTime != previousBurnTime || currentTemperature != previousTemperature)
                && level != null && level.getGameTime() % 20 == 0) {
            setChanged();
        }
    }

    private void handleFuel() {
        if (fuelBurnTime > 0) {
            fuelBurnTime--;
        } else {
            ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
            if (!fuelStack.isEmpty()) {
                // Определение категории топлива и целевой температуры
                if (fuelStack.is(Items.LAVA_BUCKET)) {
                    targetTemperature = 1600;
                    fuelBurnTime = maxFuelBurnTime = (int) (2000 * getEfficiencyMultiplier());
                    itemHandler.setStackInSlot(FUEL_SLOT, new ItemStack(Items.BUCKET));
                } else if (fuelStack.is(Items.COAL) || fuelStack.is(Items.CHARCOAL) || fuelStack.is(Items.COAL_BLOCK)) {
                    targetTemperature = 800;
                    fuelBurnTime = maxFuelBurnTime = (int) (800 * getEfficiencyMultiplier());
                    fuelStack.shrink(1);
                } else if (fuelStack.is(ItemTags.PLANKS) || fuelStack.is(ItemTags.LOGS) || fuelStack.is(Items.STICK)) {
                    targetTemperature = 200;
                    fuelBurnTime = maxFuelBurnTime = (int) (300 * getEfficiencyMultiplier());
                    fuelStack.shrink(1);
                } else {
                    targetTemperature = 0;
                }
                setChanged();
            } else {
                targetTemperature = 0;
            }
        }
    }

    private float getEfficiencyMultiplier() {
        return switch (efficiencyTier) {
            case 2 -> 1.5f;
            case 3 -> 2.5f;
            default -> 1.0f;
        };
    }

    private void handleTemperature() {
        int allowedMax = getMaxTemperature();
        int realTarget = Math.min(targetTemperature, allowedMax);

        if (currentTemperature < realTarget) {
            currentTemperature = Math.min(realTarget, currentTemperature + HEATING_RATE);
        } else if (currentTemperature > realTarget) {
            currentTemperature = Math.max(realTarget, currentTemperature - COOLING_RATE);
        }
    }

    private void handleMeltingAndAlloying() {
        // Проверка плавления руд/слитков во входных слотах
        for (int i = 0; i < INPUT_SLOTS_COUNT; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int requiredTemp = getRequiredMeltingTemp(stack);
                if (currentTemperature >= requiredTemp) {
                    FluidStack meltedFluid = getMeltResult(stack);
                    if (!meltedFluid.isEmpty() && fluidHandler.fill(meltedFluid, IFluidHandler.FluidAction.SIMULATE) == meltedFluid.getAmount()) {
                        fluidHandler.fill(meltedFluid, IFluidHandler.FluidAction.EXECUTE);
                        stack.shrink(1);
                        setChanged();
                    }
                }
            }
        }
    }

    private int getRequiredMeltingTemp(ItemStack stack) {
        if (stack.is(Items.RAW_IRON) || stack.is(Items.IRON_INGOT)) return 1200;
        if (stack.is(Items.RAW_COPPER) || stack.is(Items.COPPER_INGOT)) return 600;
        if (stack.is(Items.RAW_GOLD) || stack.is(Items.GOLD_INGOT)) return 400;
        return 9999;
    }

    private FluidStack getMeltResult(ItemStack stack) {
        return FluidStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("fluids", fluidHandler.serializeNBT(registries));
        tag.putFloat("temperature", currentTemperature);
        tag.putInt("target_temp", targetTemperature);
        tag.putInt("fuel_burn", fuelBurnTime);
        tag.putInt("max_fuel_burn", maxFuelBurnTime);
        tag.putInt("cap_tier", capacityTier);
        tag.putInt("heat_tier", heatTier);
        tag.putInt("eff_tier", efficiencyTier);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        fluidHandler.deserializeNBT(registries, tag.getCompound("fluids"));
        currentTemperature = tag.getFloat("temperature");
        targetTemperature = tag.getInt("target_temp");
        fuelBurnTime = tag.getInt("fuel_burn");
        maxFuelBurnTime = tag.getInt("max_fuel_burn");
        capacityTier = tag.contains("cap_tier") ? Math.clamp(tag.getInt("cap_tier"), 1, 3) : 1;
        heatTier = tag.contains("heat_tier") ? Math.clamp(tag.getInt("heat_tier"), 1, 3) : 1;
        efficiencyTier = tag.contains("eff_tier") ? Math.clamp(tag.getInt("eff_tier"), 1, 3) : 1;
    }

    private class AlloyFluidHandler implements IFluidHandler {
        private final FluidStack[] tanks = new FluidStack[1];

        public AlloyFluidHandler() {
            tanks[0] = FluidStack.EMPTY;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? tanks[0] : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? getCurrentCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            if (tanks[0].isEmpty() || FluidStack.isSameFluidSameComponents(tanks[0], resource)) {
                int space = Math.max(0, getCurrentCapacity() - tanks[0].getAmount());
                int filled = Math.min(space, resource.getAmount());
                if (action.execute() && filled > 0) {
                    if (tanks[0].isEmpty()) {
                        tanks[0] = resource.copy();
                        tanks[0].setAmount(filled);
                    } else {
                        tanks[0].grow(filled);
                    }
                    setChanged();
                }
                return filled;
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(tanks[0], resource))
                return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || tanks[0].isEmpty()) return FluidStack.EMPTY;
            int drained = Math.min(maxDrain, tanks[0].getAmount());
            FluidStack result = tanks[0].copyWithAmount(drained);
            if (action.execute()) {
                tanks[0].shrink(drained);
                setChanged();
            }
            return result;
        }

        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            if (!tanks[0].isEmpty()) tag.put("tank_0", tanks[0].save(registries));
            return tag;
        }

        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
            tanks[0] = FluidStack.EMPTY;
            if (tag.contains("tank_0")) {
                tanks[0] = FluidStack.parse(registries, tag.getCompound("tank_0")).orElse(FluidStack.EMPTY);
            }
        }
    }
}
