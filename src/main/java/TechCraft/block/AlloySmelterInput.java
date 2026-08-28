package TechCraft.block;

import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class AlloySmelterInput implements RecipeInput {
    private final List<FluidStack> fluids;
    private final float temperature;
    
    public AlloySmelterInput(List<FluidStack> fluids, float temperature) {
        this.fluids = fluids;
        this.temperature = temperature;
    }
    
    public List<FluidStack> getFluids() {
        return fluids;
    }
    
    public float getTemperature() {
        return temperature;
    }
    
    @Override
    public int size() {
        return 0; // No item slots
    }
    
    @Override
    public net.minecraft.world.item.ItemStack getItem(int slot) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
