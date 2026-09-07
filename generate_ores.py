#!/usr/bin/env python3
"""
Генератор Java кода для руд из JSON файлов.
Читает JSON файлы из папки ores/ и генерирует код регистрации.
"""

import json
import argparse
from pathlib import Path
from typing import Dict, Any, List

# Маппинг уровней кирки на Minecraft Tier
MINING_LEVELS = {
    "wooden": 0,
    "stone": 1,
    "iron": 2,
    "diamond": 3,
    "netherite": 4
}

class OreGenerator:
    def __init__(self, ores_dir: str, output_dir: str):
        self.ores_dir = Path(ores_dir)
        self.output_dir = Path(output_dir)
        self.ores: List[Dict[str, Any]] = []
        self.java_src_dir = Path(output_dir) / "src" / "main" / "java" / "TechCraft"
        
    def load_ores(self):
        """Загружает все JSON файлы из папки ores/"""
        if not self.ores_dir.exists():
            print(f"Папка {self.ores_dir} не найдена. Создаю...")
            self.ores_dir.mkdir(parents=True, exist_ok=True)
            return
            
        self.ores.clear()
        for json_file in sorted(self.ores_dir.glob("*.json")):
            if json_file.name == "schema.json":
                continue
            try:
                with open(json_file, 'r', encoding='utf-8') as f:
                    ore_data = json.load(f)
                    self.ores.append(ore_data)
                    print(f"Загружен: {json_file.name}")
            except (OSError, ValueError) as e:
                raise ValueError(f"Ошибка при загрузке {json_file}: {e}") from e
    
    def generate_mod_blocks(self) -> str:
        """Генерирует код для ModBlocks.java"""
        code = []
        code.append("package TechCraft.block;")
        code.append("")
        code.append("import TechCraft.TechCraft;")
        code.append("import TechCraft.item.ModItems;")
        code.append("import net.minecraft.util.valueproviders.ConstantInt;")
        code.append("import net.minecraft.util.valueproviders.UniformInt;")
        code.append("import net.minecraft.world.item.BlockItem;")
        code.append("import net.minecraft.world.item.Item;")
        code.append("import net.minecraft.world.level.block.Block;")
        code.append("import net.minecraft.world.level.block.DropExperienceBlock;")
        code.append("import net.minecraft.world.level.block.SoundType;")
        code.append("import net.minecraft.world.level.block.state.BlockBehaviour;")
        code.append("import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;")
        code.append("import net.minecraft.world.level.material.MapColor;")
        code.append("import net.neoforged.bus.api.IEventBus;")
        code.append("import net.neoforged.neoforge.registries.DeferredBlock;")
        code.append("import net.neoforged.neoforge.registries.DeferredRegister;")
        code.append("")
        code.append("import java.util.function.Supplier;")
        code.append("")
        code.append("@SuppressWarnings(\"deprecation\")")
        code.append("public class ModBlocks {")
        code.append("    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechCraft.MOD_ID);")
        code.append("")
        
        # Генерируем поля для каждой руды и связанных блоков
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            
            # Руды
            code.append(f"    public static final DeferredBlock<Block> {upper_name}_Ore;")
            if ore.get("generate_deepslate", True):
                code.append(f"    public static final DeferredBlock<Block> Deepslate_{upper_name}_Ore;")
            
            # Блок сырого металла
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                code.append(f"    public static final DeferredBlock<Block> Raw_{upper_name}_Block;")
            
            # Блок хранения
            storage_block = ore.get("storage_block", {})
            if storage_block.get("enabled", False):
                code.append(f"    public static final DeferredBlock<Block> {upper_name}_Block;")
        
        code.append("")
        code.append("    static {")
        
        # Генерируем инициализацию в static блоке
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            
            props = ore.get("properties", {})
            stone_props = props.get("stone", {"hardness": 3.0, "resistance": 3.0})
            deepslate_props = props.get("deepslate", {"hardness": 4.5, "resistance": 3.0})
            
            exp = self._get_experience(ore)
            
            hardness = stone_props.get("hardness", 3.0)
            resistance = stone_props.get("resistance", 3.0)
            
            # Руды
            code.append(f"        {upper_name}_Ore = registerBlock(\"{name}_ore\", () -> new DropExperienceBlock(")
            code.append(f"            {exp},")
            code.append(f"            BlockBehaviour.Properties.of()")
            code.append(f"                .mapColor(MapColor.STONE)")
            code.append(f"                .instrument(NoteBlockInstrument.BASEDRUM)")
            code.append(f"                .requiresCorrectToolForDrops()")
            code.append(f"                .strength({hardness}F, {resistance}F)));")
            
            if ore.get("generate_deepslate", True):
                ds_hardness = deepslate_props.get("hardness", 4.5)
                ds_resistance = deepslate_props.get("resistance", 3.0)
                code.append(f"        Deepslate_{upper_name}_Ore = registerBlock(\"deepslate_{name}_ore\", () -> new DropExperienceBlock(")
                code.append(f"            {exp},")
                code.append(f"            BlockBehaviour.Properties.ofLegacyCopy(ModBlocks.{upper_name}_Ore.get())")
                code.append(f"                .mapColor(MapColor.DEEPSLATE)")
                code.append(f"                .strength({ds_hardness}F, {ds_resistance}F)")
                code.append(f"                .sound(SoundType.DEEPSLATE)));")
            
            # Блок сырого металла
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                raw_props = raw_block.get("properties", {"hardness": 3.0, "resistance": 3.0})
                raw_hardness = raw_props.get("hardness", 3.0)
                raw_resistance = raw_props.get("resistance", 3.0)
                code.append(f"        Raw_{upper_name}_Block = registerBlock(\"raw_{name}_block\", () -> new Block(")
                code.append(f"            BlockBehaviour.Properties.of()")
                code.append(f"                .mapColor(MapColor.METAL)")
                code.append(f"                .instrument(NoteBlockInstrument.BASEDRUM)")
                code.append(f"                .requiresCorrectToolForDrops()")
                code.append(f"                .strength({raw_hardness}F, {raw_resistance}F)));")
            
            # Блок хранения
            storage_block = ore.get("storage_block", {})
            if storage_block.get("enabled", False):
                storage_props = storage_block.get("properties", {"hardness": 3.0, "resistance": 3.0})
                storage_hardness = storage_props.get("hardness", 3.0)
                storage_resistance = storage_props.get("resistance", 3.0)
                code.append(f"        {upper_name}_Block = registerBlock(\"{name}_block\", () -> new Block(")
                code.append(f"            BlockBehaviour.Properties.of()")
                code.append(f"                .mapColor(MapColor.METAL)")
                code.append(f"                .instrument(NoteBlockInstrument.BASEDRUM)")
                code.append(f"                .requiresCorrectToolForDrops()")
                code.append(f"                .strength({storage_hardness}F, {storage_resistance}F)));")
        
        code.append("    }")
        code.append("")
        code.append("    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {")
        code.append("        DeferredBlock<T> toReturn = BLOCKS.register(name, block);")
        code.append("        registerBlockItem(name, toReturn);")
        code.append("        return toReturn;")
        code.append("    }")
        code.append("")
        code.append("    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {")
        code.append("        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));")
        code.append("    }")
        code.append("")
        code.append("    public static void register(IEventBus eventBus) {")
        code.append("        BLOCKS.register(eventBus);")
        code.append("    }")
        code.append("}")
        
        return "\n".join(code)
    
    def generate_configured_features(self) -> str:
        """Генерирует код для ModConfiguredFeatures.java"""
        code = []
        code.append("package TechCraft.worldgen;")
        code.append("")
        code.append("import TechCraft.TechCraft;")
        code.append("import TechCraft.block.ModBlocks;")
        code.append("import net.minecraft.core.registries.Registries;")
        code.append("import net.minecraft.data.worldgen.BootstrapContext;")
        code.append("import net.minecraft.resources.ResourceKey;")
        code.append("import net.minecraft.resources.ResourceLocation;")
        code.append("import net.minecraft.tags.BlockTags;")
        code.append("import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;")
        code.append("import net.minecraft.world.level.levelgen.feature.Feature;")
        code.append("import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;")
        code.append("import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;")
        code.append("import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;")
        code.append("import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;")
        code.append("")
        code.append("import java.util.List;")
        code.append("")
        code.append("public class ModConfiguredFeatures {")
        
        # Генерируем ключи для руд
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            code.append(f"    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_{upper_name}_ORE_KEY = registerKey(\"{name}_ore\");")
            
            # Ключ для блока сырого металла
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                code.append(f"    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_RAW_{upper_name}_BLOCK_KEY = registerKey(\"raw_{name}_block\");")
        
        code.append("")
        code.append("    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {")
        code.append("        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);")
        code.append("        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);")
        code.append("")
        
        # Генерируем конфигурации для руд
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            vein_size = self._get_vein_size(ore)
            
            code.append(f"        List<OreConfiguration.TargetBlockState> overworld{upper_name}Ore = List.of(")
            code.append(f"                OreConfiguration.target(stoneReplaceables, ModBlocks.{upper_name}_Ore.get().defaultBlockState()),")
            
            if ore.get("generate_deepslate", True):
                code.append(f"                OreConfiguration.target(deepslateReplaceables, ModBlocks.Deepslate_{upper_name}_Ore.get().defaultBlockState()));")
            else:
                code[-1] = code[-1].rstrip(",") + ");"
            
            code.append(f"        register(context, OVERWORLD_{upper_name}_ORE_KEY, Feature.ORE, new OreConfiguration(overworld{upper_name}Ore, {vein_size}));")
            code.append("")
        
        # Генерируем конфигурации для блоков сырого металла
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                raw_vein_size = self._get_raw_block_vein_size(ore)
                code.append(f"        List<OreConfiguration.TargetBlockState> overworldRaw{upper_name}Block = List.of(")
                code.append(f"                OreConfiguration.target(stoneReplaceables, ModBlocks.Raw_{upper_name}_Block.get().defaultBlockState()),")
                code.append(f"                OreConfiguration.target(deepslateReplaceables, ModBlocks.Raw_{upper_name}_Block.get().defaultBlockState()));")
                code.append(f"        register(context, OVERWORLD_RAW_{upper_name}_BLOCK_KEY, Feature.ORE, new OreConfiguration(overworldRaw{upper_name}Block, {raw_vein_size}));")
                code.append("")
        
        code.append("    }")
        code.append("")
        code.append("    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name){")
        code.append("        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));")
        code.append("    }")
        code.append("")
        code.append("    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,")
        code.append("                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {")
        code.append("        context.register(key, new ConfiguredFeature<>(feature, configuration));")
        code.append("    }")
        code.append("}")
        
        return "\n".join(code)
    
    def generate_placed_features(self) -> str:
        """Генерирует код для ModPlacedFeatures.java"""
        code = []
        code.append("package TechCraft.worldgen;")
        code.append("")
        code.append("import TechCraft.TechCraft;")
        code.append("import net.minecraft.core.Holder;")
        code.append("import net.minecraft.core.registries.Registries;")
        code.append("import net.minecraft.data.worldgen.BootstrapContext;")
        code.append("import net.minecraft.resources.ResourceKey;")
        code.append("import net.minecraft.resources.ResourceLocation;")
        code.append("import net.minecraft.world.level.levelgen.VerticalAnchor;")
        code.append("import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;")
        code.append("import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;")
        code.append("import net.minecraft.world.level.levelgen.placement.PlacedFeature;")
        code.append("import net.minecraft.world.level.levelgen.placement.PlacementModifier;")
        code.append("")
        code.append("import java.util.List;")
        code.append("")
        code.append("public class ModPlacedFeatures {")
        
        # Генерируем ключи для руд
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            code.append(f"    public static final ResourceKey<PlacedFeature> {upper_name}_ORE_PLACED_KEY = registerKey(\"{name}_ore_placed\");")
            
            # Ключ для блока сырого металла
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                code.append(f"    public static final ResourceKey<PlacedFeature> RAW_{upper_name}_BLOCK_PLACED_KEY = registerKey(\"raw_{name}_block_placed\");")
        
        code.append("")
        code.append("    public static void bootstrap(BootstrapContext<PlacedFeature> context) {")
        code.append("        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);")
        code.append("")
        
        # Генерируем размещение для руд
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            
            height_range = ore["height_range"]
            count = self._get_count_per_chunk(ore)
            
            code.append(f"        register(context, {upper_name}_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_{upper_name}_ORE_KEY),")
            code.append(f"                {count});")
        
        # Генерируем размещение для блоков сырого металла
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                raw_count = self._get_raw_block_count_per_chunk(ore)
                code.append(f"        register(context, RAW_{upper_name}_BLOCK_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_RAW_{upper_name}_BLOCK_KEY),")
                code.append(f"                {raw_count});")
        
        code.append("    }")
        code.append("")
        code.append("    private static ResourceKey<PlacedFeature> registerKey(String name){")
        code.append("        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));")
        code.append("    }")
        code.append("")
        code.append("    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,")
        code.append("                                 List<PlacementModifier> modifiers) {")
        code.append("        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));")
        code.append("    }")
        code.append("}")
        
        return "\n".join(code)
    
    def generate_biome_modifiers(self) -> str:
        """Генерирует код для ModBiomeModifiers.java"""
        code = []
        code.append("package TechCraft.worldgen;")
        code.append("")
        code.append("import TechCraft.TechCraft;")
        code.append("import net.minecraft.core.HolderSet;")
        code.append("import net.minecraft.core.registries.Registries;")
        code.append("import net.minecraft.data.worldgen.BootstrapContext;")
        code.append("import net.minecraft.resources.ResourceKey;")
        code.append("import net.minecraft.resources.ResourceLocation;")
        code.append("import net.minecraft.tags.BiomeTags;")
        code.append("import net.minecraft.world.level.levelgen.GenerationStep;")
        code.append("import net.neoforged.neoforge.common.world.BiomeModifier;")
        code.append("import net.neoforged.neoforge.common.world.BiomeModifiers;")
        code.append("import net.neoforged.neoforge.registries.NeoForgeRegistries;")
        code.append("")
        code.append("public class ModBiomeModifiers {")
        
        # Генерируем ключи для руд
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            code.append(f"    public static final ResourceKey<BiomeModifier> ADD_{upper_name}_ORE = registerKey(\"add_{name}_ore\");")
            
            # Ключ для блока сырого металла
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                code.append(f"    public static final ResourceKey<BiomeModifier> ADD_RAW_{upper_name}_BLOCK = registerKey(\"add_raw_{name}_block\");")
        
        code.append("")
        code.append("    public static void bootstrap(BootstrapContext<BiomeModifier> context) {")
        code.append("        var placeFeatures = context.lookup(Registries.PLACED_FEATURE);")
        code.append("        var biomes =  context.lookup(Registries.BIOME);")
        code.append("")
        
        # Генерируем модификеры для руд
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            code.append(f"        context.register(ADD_{upper_name}_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(")
            code.append(f"                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),")
            code.append(f"                HolderSet.direct(placeFeatures.getOrThrow(ModPlacedFeatures.{upper_name}_ORE_PLACED_KEY)),")
            code.append(f"                GenerationStep.Decoration.UNDERGROUND_ORES));")
        
        # Генерируем модификеры для блоков сырого металла
        for ore in self.ores:
            name = ore["name"]
            upper_name = name.replace("_", " ").title().replace(" ", "_")
            
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                code.append(f"        context.register(ADD_RAW_{upper_name}_BLOCK, new BiomeModifiers.AddFeaturesBiomeModifier(")
                code.append(f"                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),")
                code.append(f"                HolderSet.direct(placeFeatures.getOrThrow(ModPlacedFeatures.RAW_{upper_name}_BLOCK_PLACED_KEY)),")
                code.append(f"                GenerationStep.Decoration.UNDERGROUND_ORES));")
        
        code.append("    }")
        code.append("")
        code.append("    private static ResourceKey<BiomeModifier> registerKey(String name){")
        code.append("     return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(TechCraft.MOD_ID, name));")
        code.append("    }")
        code.append("}")
        
        return "\n".join(code)
    
    def _get_experience(self, ore: Dict[str, Any]) -> str:
        """Возвращает код для опыта"""
        exp = ore.get("experience", 0)
        if isinstance(exp, int):
            return f"ConstantInt.of({exp})"
        elif isinstance(exp, dict):
            return f"UniformInt.of({exp['min']}, {exp['max']})"
        return "ConstantInt.of(0)"
    
    def _get_vein_size(self, ore: Dict[str, Any]) -> str:
        """Возвращает размер жилы"""
        vein = ore.get("vein_size", 10)
        if isinstance(vein, int):
            return str(vein)
        elif isinstance(vein, dict):
            # Для диапазона используем среднее значение (Minecraft не поддерживает диапазон в OreConfiguration)
            return str((vein['min'] + vein['max']) // 2)
        return "10"
    
    def _get_raw_block_vein_size(self, ore: Dict[str, Any]) -> str:
        """Возвращает размер жилы для блока сырого металла"""
        raw_block = ore.get("raw_block", {})
        vein = raw_block.get("vein_size", 20)
        if isinstance(vein, int):
            return str(vein)
        elif isinstance(vein, dict):
            return str((vein['min'] + vein['max']) // 2)
        return "20"
    
    def _get_count_per_chunk(self, ore: Dict[str, Any]) -> str:
        """Возвращает код для количества жил на чанк"""
        count = ore.get("count_per_chunk", 16)
        height_range = ore["height_range"]
        
        if isinstance(count, int):
            return f"ModOrePlacement.commonOrePlacement({count}, HeightRangePlacement.triangle(VerticalAnchor.absolute({height_range['min']}), VerticalAnchor.absolute({height_range['max']})))"
        elif isinstance(count, dict) and count.get("rare", False):
            chance = count.get("chance", 10)
            return f"ModOrePlacement.rareOrePlacement({chance}, HeightRangePlacement.triangle(VerticalAnchor.absolute({height_range['min']}), VerticalAnchor.absolute({height_range['max']})))"
        
        return f"ModOrePlacement.commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute({height_range['min']}), VerticalAnchor.absolute({height_range['max']})))"
    
    def _get_raw_block_count_per_chunk(self, ore: Dict[str, Any]) -> str:
        """Возвращает код для количества жил на чанк для блока сырого металла"""
        raw_block = ore.get("raw_block", {})
        count = raw_block.get("count_per_chunk", 8)
        height_range = ore["height_range"]
        
        return f"ModOrePlacement.commonOrePlacement({count}, HeightRangePlacement.triangle(VerticalAnchor.absolute({height_range['min']}), VerticalAnchor.absolute({height_range['max']})))"
    
    def generate_loot_table(self, ore: Dict[str, Any], is_deepslate: bool = False) -> dict:
        """Генерирует loot table JSON для руды"""
        drops = ore.get("drops", {})
        drop_type = drops.get("type", "block")
        
        loot_table = {
            "type": "minecraft:block",
            "pools": []
        }
        
        if drop_type == "item":
            item_id = drops.get("item", f"techcraft:raw_{ore['name']}")
            amount = drops.get("amount", 1)
            fortune_bonus = drops.get("fortune_bonus", True)
            
            pool = {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": item_id
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
            
            # Добавляем Fortune если нужно
            if fortune_bonus:
                if isinstance(amount, int):
                    # Фиксированное количество с Fortune
                    pool["functions"] = [
                        {
                            "function": "minecraft:apply_bonus",
                            "enchantment": "minecraft:fortune",
                            "formula": "minecraft:ore_drops"
                        },
                        {
                            "function": "minecraft:set_count",
                            "count": {
                                "type": "minecraft:uniform",
                                "min": amount,
                                "max": amount
                            }
                        }
                    ]
                elif isinstance(amount, dict):
                    # Диапазон с Fortune
                    pool["functions"] = [
                        {
                            "function": "minecraft:apply_bonus",
                            "enchantment": "minecraft:fortune",
                            "formula": "minecraft:ore_drops"
                        },
                        {
                            "function": "minecraft:set_count",
                            "count": {
                                "type": "minecraft:uniform",
                                "min": amount["min"],
                                "max": amount["max"]
                            }
                        }
                    ]
            else:
                # Без Fortune
                if isinstance(amount, int):
                    pool["functions"] = [
                        {
                            "function": "minecraft:set_count",
                            "count": {
                                "type": "minecraft:uniform",
                                "min": amount,
                                "max": amount
                            }
                        }
                    ]
                elif isinstance(amount, dict):
                    pool["functions"] = [
                        {
                            "function": "minecraft:set_count",
                            "count": {
                                "type": "minecraft:uniform",
                                "min": amount["min"],
                                "max": amount["max"]
                            }
                        }
                    ]
            
            loot_table["pools"].append(pool)
        
        elif drop_type == "block":
            # Выпадает сам блок
            pool = {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": f"techcraft:{ore['name']}_ore" if not is_deepslate else f"techcraft:deepslate_{ore['name']}_ore"
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
            loot_table["pools"].append(pool)
        
        return loot_table
    
    def generate_all(self):
        """Генерирует все файлы"""
        self.load_ores()
        
        if not self.ores:
            print("Нет JSON файлов для генерации.")
            return
        
        # Создаем папки для Java файлов
        block_dir = self.java_src_dir / "block"
        worldgen_dir = self.java_src_dir / "worldgen"
        block_dir.mkdir(parents=True, exist_ok=True)
        worldgen_dir.mkdir(parents=True, exist_ok=True)
        
        # Создаем папки для loot tables
        resources_dir = self.output_dir / "src" / "main" / "resources" / "data" / "techcraft" / "loot_table" / "blocks"
        resources_dir.mkdir(parents=True, exist_ok=True)
        
        # Генерируем Java файлы
        java_files = {
            (block_dir, "ModBlocks.java"): self.generate_mod_blocks(),
            (worldgen_dir, "ModConfiguredFeatures.java"): self.generate_configured_features(),
            (worldgen_dir, "ModPlacedFeatures.java"): self.generate_placed_features(),
            (worldgen_dir, "ModBiomeModifiers.java"): self.generate_biome_modifiers()
        }
        
        for (dir_path, filename), content in java_files.items():
            filepath = dir_path / filename
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Сгенерирован: {filepath}")
        
        # Генерируем loot tables
        for ore in self.ores:
            name = ore["name"]
            
            # Stone ore loot table
            stone_loot = self.generate_loot_table(ore, is_deepslate=False)
            stone_loot_path = resources_dir / f"{name}_ore.json"
            with open(stone_loot_path, 'w', encoding='utf-8') as f:
                json.dump(stone_loot, f, indent=2)
            print(f"Сгенерирован: {stone_loot_path}")
            
            # Deepslate ore loot table
            if ore.get("generate_deepslate", True):
                deepslate_loot = self.generate_loot_table(ore, is_deepslate=True)
                deepslate_loot_path = resources_dir / f"deepslate_{name}_ore.json"
                with open(deepslate_loot_path, 'w', encoding='utf-8') as f:
                    json.dump(deepslate_loot, f, indent=2)
                print(f"Сгенерирован: {deepslate_loot_path}")
            
            # Raw block loot table
            raw_block = ore.get("raw_block", {})
            if raw_block.get("enabled", False):
                raw_loot = {
                    "type": "minecraft:block",
                    "pools": [{
                        "rolls": 1,
                        "entries": [{
                            "type": "minecraft:item",
                            "name": f"techcraft:raw_{name}_block"
                        }],
                        "conditions": [{
                            "condition": "minecraft:survives_explosion"
                        }]
                    }]
                }
                raw_loot_path = resources_dir / f"raw_{name}_block.json"
                with open(raw_loot_path, 'w', encoding='utf-8') as f:
                    json.dump(raw_loot, f, indent=2)
                print(f"Сгенерирован: {raw_loot_path}")
        
        print(f"\nГенерация завершена! Файлы сохранены в {self.output_dir.resolve()}")


def main():
    script_dir = Path(__file__).resolve().parent
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ores-dir", type=Path, default=script_dir / "ores")
    parser.add_argument("--output-dir", type=Path, default=script_dir / "build/generated/ores",
                        help="Review generated snippets here before integrating them into source files")
    args = parser.parse_args()
    ores_dir = args.ores_dir
    output_dir = args.output_dir
    
    generator = OreGenerator(str(ores_dir), str(output_dir))
    generator.generate_all()


if __name__ == "__main__":
    main()
