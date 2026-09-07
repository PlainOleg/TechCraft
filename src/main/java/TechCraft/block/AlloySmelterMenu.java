package TechCraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AlloySmelterMenu extends AbstractContainerMenu {

    private final AlloySmelterBlockEntity blockEntity;
    private int currentTemperature;
    private int maxTemperature;

    private static final int INPUT_START = 0;
    private static final int INPUT_COUNT = 3;
    private static final int FUEL_SLOT = 3;
    private static final int PLAYER_INVENTORY_START = 4;
    private static final int PLAYER_HOTBAR_END = 40;

    public AlloySmelterMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, (AlloySmelterBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    public AlloySmelterMenu(int containerId, Inventory playerInventory, AlloySmelterBlockEntity blockEntity) {
        super(ModMenuTypes.ALLOY_SMELTER.get(), containerId);
        this.blockEntity = blockEntity;

        if (this.blockEntity != null) {
            IItemHandler itemHandler = blockEntity.getItemHandler();

            // 3 вертикальных слота плавления слева
            for (int i = 0; i < INPUT_COUNT; i++) {
                this.addSlot(new SlotItemHandler(itemHandler, INPUT_START + i, 40, 16 + i * 18));
            }

            // Слот топлива
            this.addSlot(new SlotItemHandler(itemHandler, FUEL_SLOT, 149, 30));

            // Data slots для синхронизации данных топлива
            this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return blockEntity.getFuelBurnTime();
                }
                @Override
                public void set(int value) {
                    blockEntity.setFuelBurnTime(value);
                }
            });
            this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return Math.round(blockEntity.getCurrentTemperature());
                }
                @Override
                public void set(int value) {
                    currentTemperature = value;
                }
            });
            this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return blockEntity.getMaxTemperature();
                }
                @Override
                public void set(int value) {
                    maxTemperature = value;
                }
            });

            currentTemperature = Math.round(blockEntity.getCurrentTemperature());
            maxTemperature = blockEntity.getMaxTemperature();
            this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return blockEntity.getMaxFuelBurnTime();
                }
                @Override
                public void set(int value) {
                    blockEntity.setMaxFuelBurnTime(value);
                }
            });
        }

        // Инвентарь игрока
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Хотбар игрока
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            stack = original.copy();

            if (index < PLAYER_INVENTORY_START) {
                if (!this.moveItemStackTo(original, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(original, 0, PLAYER_INVENTORY_START, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (original.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }

    public AlloySmelterBlockEntity getBlockEntity() { return blockEntity; }
    public int getCurrentTemperature() { return currentTemperature; }
    public int getMaxTemperature() { return maxTemperature; }
}
