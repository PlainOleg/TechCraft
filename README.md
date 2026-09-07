# TechCraft

Технологический мод для Minecraft 1.21.1 / NeoForge 21.1.209: материалы, инструменты, квантовая броня, солнечные панели и цифровая сеть Lumen Mesh.

## Сборка и проверки

Нужны JDK 21 и Python 3. При первой сборке Gradle загружает зависимости и ресурсы Minecraft.

```sh
./gradlew build
./gradlew regressionTest
./gradlew test
./gradlew verifyResources
```

`build` включает все три проверки. `regressionTest` проверяет граф, сохранение энергии и геометрию без запуска мира; `test` запускает JUnit в окружении NeoForge для проверки предметов, хранилищ и жидкостей. После загрузки зависимостей доступен режим `--offline`.

Готовый мод: `build/libs/techcraft-0.0.1.jar`. Для разработки используйте `./gradlew runClient` или `./gradlew runServer`. Обычные игровые запуски используют `run/`; тестовое окружение NeoForge — отдельную папку `build/minecraft-junit/`.

## Структура

| Папка | Ответственность |
| --- | --- |
| `src/main/java/TechCraft/block` | Регистрация блоков, плавильня и её интерфейс |
| `src/main/java/TechCraft/item` | Регистрация предметов и поведение инструментов/брони |
| `src/main/java/TechCraft/event` | События добычи, боя, полёта; клиентские подсказки отдельно |
| `src/main/java/TechCraft/solar` | Солнечная генерация, накопитель панелей и зарядка |
| `src/main/java/TechCraft/lumenmesh/network` | Топология, принадлежность узлов, состояние и сохранение сетей |
| `src/main/java/TechCraft/lumenmesh/storage` | Хранение предметов, агрегация и операции над сетью |
| `src/main/java/TechCraft/lumenmesh/block` | Игровые устройства и их жизненный цикл |
| `src/main/java/TechCraft/lumenmesh/menu`, `client` | Серверные контейнеры и клиентские экраны |
| `src/main/java/TechCraft/worldgen`, `datagen` | Генерация мира и данных |
| `src/main/resources` | Рецепты, модели, текстуры, локализация и книга |
| `src/regression/java`, `src/test/java` | Регрессионные и интеграционные проверки |
| `tools/` | Валидация ресурсов и вспомогательные генераторы |

Менеджер Lumen Mesh работает на серверном потоке. Выгрузка чанка освобождает живой объект устройства, сохраняя топологию; разрушение блока удаляет узел. Внешние инвентари и интерфейсы обращаются к хранилищу через `NetworkStorageService`.

## Генерация руд

```sh
./gradlew generateOres
# или:
python3 generate_ores.py --output-dir build/generated/ores
```

Результат сохраняется в `build/generated/ores/` и не подключается автоматически к сборке. Сравните заготовки с исходниками и перенесите необходимые регистрации. В `ores/` сейчас описана только часть зарегистрированных руд; полная замена `ModBlocks.java` результатом генерации удалит ручные регистрации.

Подробности: [JSON_ORES_README.md](JSON_ORES_README.md). Результат аудита и ограничения: [docs/PROJECT_AUDIT.md](docs/PROJECT_AUDIT.md).
