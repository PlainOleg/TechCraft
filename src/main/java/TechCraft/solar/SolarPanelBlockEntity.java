package TechCraft.solar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;

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
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            int extracted = (int) Math.min(Math.max(amount, 0), energyStored);
            if (!simulate && extracted > 0) {
                energyStored -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, energyStored);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, SolarPanelBlockEntity.this.getMaxEnergyStored());
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

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

        long previousEnergy = entity.energyStored;
        long previousRemainder = entity.scaledRemainder;
        boolean previousSky = entity.skyVisible;
        boolean previousActive = entity.active;

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
            IEnergyStorage receiver = level.getCapability(Capabilities.EnergyStorage.BLOCK, belowPos, Direction.UP);
            if (receiver != null && receiver.canReceive()) {
                int offered = (int) Math.min(entity.energyStored, type.generationPerTick());
                int accepted = receiver.receiveEnergy(offered, false);
                if (accepted > 0) entity.energyStored -= accepted;
            }
        }

        entity.active = generated > 0 && entity.skyVisible;
        if (entity.energyStored != previousEnergy || entity.scaledRemainder != previousRemainder
                || entity.skyVisible != previousSky || entity.active != previousActive) {
            entity.setChanged();
        }
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

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
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
        long capacity = getBlockState().getBlock() instanceof SolarPanelBlock panel
                ? panel.getPanelType().energyCapacity() : 0;
        energyStored = Math.clamp(tag.getLong("energy_stored"), 0, capacity);
        scaledRemainder = Math.clamp(tag.getLong("scaled_remainder"), 0, 999);
        skyVisible = tag.getBoolean("sky_visible");
        active = tag.getBoolean("active");
        dataVersion = tag.getInt("data_version");
    }
}
