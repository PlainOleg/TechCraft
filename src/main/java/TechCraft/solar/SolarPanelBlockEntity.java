package TechCraft.solar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for solar panels.
 * Common implementation for all solar panel tiers.
 * Type-specific data comes from the associated SolarPanelType.
 */
public class SolarPanelBlockEntity extends BlockEntity {
    private long energyStored;
    private long scaledRemainder; // Scaled by 1000 to avoid floating point
    private boolean skyVisible;
    private boolean active;
    private int skyCheckTimer;
    private int dataVersion = 1;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModSolarBlockEntities.SOLAR_PANEL.get(), pos, state);
        this.energyStored = 0;
        this.scaledRemainder = 0;
        this.skyVisible = false;
        this.active = false;
        this.skyCheckTimer = 0;
    }

    /**
     * Server tick handler for solar panels.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity entity) {
        if (level.isClientSide) {
            return;
        }

        entity.skyCheckTimer++;
        boolean shouldCheckSky = entity.skyCheckTimer >= SolarGenerationService.getSkyCheckInterval();

        // Check sky visibility periodically
        if (shouldCheckSky) {
            entity.skyVisible = SolarGenerationService.isSkyVisible(level, pos.getX(), pos.getY() + 1, pos.getZ());
            entity.skyCheckTimer = 0;
        }

        // Get panel type from block state
        if (!(state.getBlock() instanceof SolarPanelBlock solarBlock)) {
            return;
        }

        SolarPanelType type = solarBlock.getPanelType();
        if (type == null) {
            return;
        }

        // Calculate generation
        double solarFactor = entity.skyVisible ? SolarGenerationService.calculateSolarFactor(level) : 0.0;
        double remainder = SolarGenerationService.unscaleRemainder(entity.scaledRemainder);
        
        long[] result = SolarGenerationService.calculateGeneration(
            type.generationPerTick(),
            solarFactor,
            remainder
        );

        long generated = result[0];
        entity.scaledRemainder = result[1];

        // Store generated energy (respect capacity)
        if (generated > 0) {
            long space = type.energyCapacity() - entity.energyStored;
            long toStore = Math.min(generated, space);
            entity.energyStored += toStore;
        }

        // Output energy to block below
        if (entity.energyStored > 0) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            
            // Try to output energy (simplified - would need energy API integration)
            // For now, we'll just mark as active if generating
        }

        entity.active = generated > 0 && entity.skyVisible;
        entity.setChanged();
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getMaxEnergyStored() {
        if (level == null || !(getBlockState().getBlock() instanceof SolarPanelBlock solarBlock)) {
            return 0;
        }
        SolarPanelType type = solarBlock.getPanelType();
        return type != null ? type.energyCapacity() : 0;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSkyVisible() {
        return skyVisible;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("energy_stored", energyStored);
        tag.putLong("scaled_remainder", scaledRemainder);
        tag.putBoolean("sky_visible", skyVisible);
        tag.putBoolean("active", active);
        tag.putInt("data_version", dataVersion);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getLong("energy_stored");
        scaledRemainder = tag.getLong("scaled_remainder");
        skyVisible = tag.getBoolean("sky_visible");
        active = tag.getBoolean("active");
        dataVersion = tag.getInt("data_version");
    }
}
