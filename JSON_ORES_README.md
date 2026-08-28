# JSON Ore System - Система руд на основе JSON

Простая система для добавления руд через JSON файлы без написания Java кода.

## Архитектура проекта

**Источником правды являются JSON файлы в папке `ores/`**

Все изменения в блоках и генерации мира должны вноситься через JSON конфигурации. Java файлы в `src/main/java/` генерируются автоматически и не должны редактироваться вручную.

### Структура генерации

```
ores/ (JSON конфигурации - ИСТОЧНИК ПРАВДЫ)
  ├── tin_ore.json
  └── schema.json

generate_ores.py (Генератор)

src/main/java/TechCraft/ (ГЕНЕРИРУЕТСЯ)
  ├── block/
  │   └── ModBlocks.java
  └── worldgen/
      ├── ModConfiguredFeatures.java
      ├── ModPlacedFeatures.java
      └── ModBiomeModifiers.java

src/main/resources/data/techcraft/loot_table/blocks/ (ГЕНЕРИРУЕТСЯ)
  ├── tin_ore.json
  ├── deepslate_tin_ore.json
  └── raw_tin_block.json
```

## Как это работает

1. Создаете JSON файл в папке `ores/` с описанием руды
2. Запускаете генератор: `./gradlew generateOres` или `python3 generate_ores.py`
3. Скрипт автоматически генерирует Java код в `src/main/java/`
4. Скрипт автоматически генерирует loot tables в `src/main/resources/`

## Структура JSON файла

```json
{
  "name": "tin",
  "mining_level": "stone",
  "height_range": {
    "min": -16,
    "max": 112
  },
  "vein_size": 10,
  "count_per_chunk": 16,
  "drops": {
    "type": "item",
    "item": "techcraft:raw_tin",
    "amount": {
      "min": 1,
      "max": 3
    },
    "fortune_bonus": true
  },
  "experience": 0,
  "properties": {
    "stone": {
      "hardness": 3.0,
      "resistance": 3.0
    },
    "deepslate": {
      "hardness": 4.5,
      "resistance": 3.0
    }
  },
  "generate_deepslate": true
}
```

## Поля JSON

### Обязательные поля

- **name** (string) - имя руды (без `_ore` суффикса). Пример: `"tin"`, `"copper"`
- **mining_level** (string) - уровень кирки для добычи:
  - `"wooden"` - деревянная
  - `"stone"` - каменная
  - `"iron"` - железная
  - `"diamond"` - алмазная
  - `"netherite"` - незеритовая
- **height_range** (object) - диапазон высот генерации:
  - **min** (int) - минимальная высота Y
  - **max** (int) - максимальная высота Y
- **vein_size** (int или object) - размер жилы:
  - Число: фиксированный размер, например `10`
  - Объект: диапазон `{"min": 8, "max": 12}`
- **drops** (object) - что выпадает при добыче:
  - **type** (string) - `"block"` (выпадает блок) или `"item"` (выпадает предмет)
  - **item** (string) - ID предмета (если type="item")
  - **amount** (int или object) - количество:
    - Число: фиксированное количество, например `1`
    - Объект: диапазон `{"min": 1, "max": 3}`
  - **fortune_bonus** (boolean) - применяется ли Fortune к количеству (по умолчанию `true`)

### Необязательные поля

- **count_per_chunk** (int или object) - количество жил на чанк:
  - Число: обычная руда, например `16`
  - Объект с `"rare": true` - редкая руда:
    ```json
    {
      "rare": true,
      "chance": 10
    }
    ```
    где `chance` - шанс 1 из N (10 = 1 шанс из 10)
- **experience** (int или object) - опыт при добыче:
  - Число: фиксированный опыт, например `0`
  - Объект: диапазон `{"min": 1, "max": 3}`
- **properties** (object) - свойства блоков:
  - **stone** - свойства каменной версии:
    - **hardness** (float) - твердость (по умолчанию 3.0)
    - **resistance** (float) - взрывоустойчивость (по умолчанию 3.0)
  - **deepslate** - свойства deepslate версии:
    - **hardness** (float) - твердость (по умолчанию 4.5)
    - **resistance** (float) - взрывоустойчивость (по умолчанию 3.0)
- **generate_deepslate** (boolean) - генерировать ли deepslate версию (по умолчанию `true`)
- **raw_block** (object) - настройки блока сырого металла (как raw copper block):
  - **enabled** (boolean) - генерировать ли блок сырого металла (по умолчанию `false`)
  - **vein_size** (int или object) - размер жилы для блока сырого металла (по умолчанию 20)
  - **count_per_chunk** (int) - количество жил на чанк (по умолчанию 8)
  - **properties** - свойства блока сырого металла (hardness, resistance)
- **storage_block** (object) - настройки блока хранения металла (как tin block):
  - **enabled** (boolean) - генерировать ли блок хранения металла (по умолчанию `false`)
  - **properties** - свойства блока хранения (hardness, resistance)

## Примеры

### Обычная руда (tin)

```json
{
  "name": "tin",
  "mining_level": "stone",
  "height_range": {
    "min": -16,
    "max": 112
  },
  "vein_size": 10,
  "count_per_chunk": 16,
  "drops": {
    "type": "item",
    "item": "techcraft:raw_tin",
    "amount": {
      "min": 1,
      "max": 3
    }
  },
  "experience": 0,
  "raw_block": {
    "enabled": true,
    "vein_size": 20,
    "count_per_chunk": 8
  },
  "storage_block": {
    "enabled": true
  }
}
```

### Редкая руда (silver)

```json
{
  "name": "silver",
  "mining_level": "iron",
  "height_range": {
    "min": -32,
    "max": 48
  },
  "vein_size": 8,
  "count_per_chunk": {
    "rare": true,
    "chance": 8
  },
  "drops": {
    "type": "item",
    "item": "techcraft:raw_silver",
    "amount": 1
  },
  "experience": {
    "min": 1,
    "max": 3
  }
}
```

### Руда с выпадением блока

```json
{
  "name": "my_ore",
  "mining_level": "stone",
  "height_range": {
    "min": 0,
    "max": 64
  },
  "vein_size": 6,
  "count_per_chunk": 20,
  "drops": {
    "type": "block"
  },
  "experience": 2
}
```

## Использование

### 1. Создайте JSON файл

Создайте файл в папке `ores/`, например `ores/my_ore.json`:

```json
{
  "name": "my_ore",
  "mining_level": "stone",
  "height_range": {
    "min": -16,
    "max": 112
  },
  "vein_size": 10,
  "count_per_chunk": 16,
  "drops": {
    "type": "item",
    "item": "techcraft:raw_my",
    "amount": 1
  },
  "experience": 0
}
```

### 2. Запустите генератор

```bash
python3 generate_ores.py
```

или на macOS/Linux:

```bash
./generate_ores.py
```

### 3. Скопируйте сгенерированные файлы

Генератор создаст файлы в папке `generated/`:

- `ModBlocks.java` → скопируйте в `src/main/java/TechCraft/block/`
- `ModConfiguredFeatures.java` → скопируйте в `src/main/java/TechCraft/worldgen/`
- `ModPlacedFeatures.java` → скопируйте в `src/main/java/TechCraft/worldgen/`
- `ModBiomeModifiers.java` → скопируйте в `src/main/java/TechCraft/worldgen/`

### 4. Добавьте ресурсы

Вам все равно нужно будет создать:
- Текстуры блоков (assets/techcraft/textures/block/)
- Модели блоков (assets/techcraft/models/block/)
- Blockstates (assets/techcraft/blockstates/)
- Loot tables (data/techcraft/loot_table/blocks/)
- Рецепты (data/techcraft/recipe/) - если нужно

## Что генерируется автоматически

- **Блоки**: каменная и deepslate версии руды
- **Предметы**: BlockItem для обоих блоков
- **Worldgen**: configured features, placed features, biome modifiers
- **Свойства**: правильные твердость, устойчивость, звуки, цвета

## Валидация

JSON схема доступна в `ore_schema.json`. Можно использовать валидаторы JSON для проверки файлов перед генерацией.

## Советы

- Используйте понятные имена для руд (например, `copper`, `silver`, `lead`)
- Для редких руд используйте `"rare": true` с шансом
- Диапазоны высот: отрицательные значения для подземных руд
- Опыт обычно добавляют только к редким рудам (diamond, ancient debris)
- Fortune работает только с type="item"

## Примеры файлов

Смотрите папку `ores/` для примеров:
- `tin_ore.json` - обычная руда
- `copper_ore.json` - руда с диапазоном размера жилы
- `silver_ore.json` - редкая руда с опытом
