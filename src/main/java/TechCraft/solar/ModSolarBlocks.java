package TechCraft.solar;

import TechCraft.TechCraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * Registration class for solar panel blocks.
 * Registers 8 tiers of solar panels with their BlockItems.
 */
public class ModSolarBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechCraft.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechCraft.MOD_ID);

    // Solar panel blocks - 8 tiers
    public static final DeferredBlock<Block> COPPER_SOLAR_PANEL;
    public static final DeferredBlock<Block> SILICON_SOLAR_PANEL;
    public static final DeferredBlock<Block> REINFORCED_SOLAR_PANEL;
    public static final DeferredBlock<Block> PRISMATIC_SOLAR_PANEL;
    public static final DeferredBlock<Block> RESONANT_SOLAR_PANEL;
    public static final DeferredBlock<Block> FLUX_SOLAR_PANEL;
    public static final DeferredBlock<Block> STELLAR_SOLAR_PANEL;
    public static final DeferredBlock<Block> HELIOS_SOLAR_PANEL;
    
    // Solar Panel Bank
    public static final DeferredBlock<Block> SOLAR_PANEL_BANK;

    static {
        // Tier 1: Copper Solar Panel - 4/tick, 20,000 capacity
        COPPER_SOLAR_PANEL = registerSolarPanel("copper_solar_panel", 
            SolarPanelType.supplier(1, 4, 20000, "copper"));

        // Tier 2: Silicon Solar Panel - 12/tick, 60,000 capacity
        SILICON_SOLAR_PANEL = registerSolarPanel("silicon_solar_panel", 
            SolarPanelType.supplier(2, 12, 60000, "blue"));

        // Tier 3: Reinforced Solar Panel - 32/tick, 160,000 capacity
        REINFORCED_SOLAR_PANEL = registerSolarPanel("reinforced_solar_panel", 
            SolarPanelType.supplier(3, 32, 160000, "steel"));

        // Tier 4: Prismatic Solar Panel - 80/tick, 400,000 capacity
        PRISMATIC_SOLAR_PANEL = registerSolarPanel("prismatic_solar_panel", 
            SolarPanelType.supplier(4, 80, 400000, "cyan"));

        // Tier 5: Resonant Solar Panel - 192/tick, 960,000 capacity
        RESONANT_SOLAR_PANEL = registerSolarPanel("resonant_solar_panel", 
            SolarPanelType.supplier(5, 192, 960000, "violet"));

        // Tier 6: Flux Solar Panel - 448/tick, 2,240,000 capacity
        FLUX_SOLAR_PANEL = registerSolarPanel("flux_solar_panel", 
            SolarPanelType.supplier(6, 448, 2240000, "green"));

        // Tier 7: Stellar Solar Panel - 1,024/tick, 5,120,000 capacity
        STELLAR_SOLAR_PANEL = registerSolarPanel("stellar_solar_panel", 
            SolarPanelType.supplier(7, 1024, 5120000, "gold"));

        // Tier 8: Helios Solar Panel - 2,304/tick, 11,520,000 capacity
        HELIOS_SOLAR_PANEL = registerSolarPanel("helios_solar_panel", 
            SolarPanelType.supplier(8, 2304, 11520000, "cyan2"));

        // Solar Panel Bank
        SOLAR_PANEL_BANK = BLOCKS.register("solar_panel_bank", () -> 
            new SolarPanelBankBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 3.5F)
                .requiresCorrectToolForDrops())
        );
        
        // Register BlockItem for bank
        ITEMS.register("solar_panel_bank", () -> new BlockItem(SOLAR_PANEL_BANK.get(), new Item.Properties()));
    }

    private static DeferredBlock<Block> registerSolarPanel(String name, Supplier<SolarPanelType> panelType) {
        Supplier<Block> blockSupplier = () -> new SolarPanelBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 3.0F)
                .requiresCorrectToolForDrops(),
            panelType
        );

        DeferredBlock<Block> block = BLOCKS.register(name, blockSupplier);
        
        // Register BlockItem in our own ITEMS register
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));

        return block;
    }

    /**
     * Called after block registration to populate the type registry.
     * This must be called after the DeferredRegister has been processed.
     */
    public static void populateTypeRegistry() {
        for (DeferredBlock<Block> panel : List.of(COPPER_SOLAR_PANEL, SILICON_SOLAR_PANEL, REINFORCED_SOLAR_PANEL,
                PRISMATIC_SOLAR_PANEL, RESONANT_SOLAR_PANEL, FLUX_SOLAR_PANEL, STELLAR_SOLAR_PANEL, HELIOS_SOLAR_PANEL)) {
            registerType(panel);
        }
    }

    /** Characteristics come from the block itself, so each tier is described in exactly one place. */
    private static void registerType(DeferredBlock<Block> block) {
        if (block.get() instanceof SolarPanelBlock panel) {
            SolarPanelRegistry.register(panel, panel.asItem(), panel.getPanelType());
        }
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
