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

Весь код лежит в пакете `com.plainoleg.techcraft` (`src/main/java/com/plainoleg/techcraft/`).
Мод разделён на **основную часть** (корневые пакеты) и **модули** `solar` и `lumenmesh`.
Внутри каждого из них одинаковые папки:

| Папка | Что в ней лежит |
| --- | --- |
| `registry/` | Все `DeferredRegister`: блоки, предметы, блок-сущности, меню, вкладки. Здесь видно, что добавляет мод или модуль |
| `block/` | Классы блоков и их блок-сущности; у устройства с несколькими классами — своя подпапка (`block/alloysmelter`, `lumenmesh/block/core`) |
| `menu/` | Серверные контейнеры (`AbstractContainerMenu`) |
| `client/` | Только клиентский код: экраны, отрисовка, клиентские утилиты. Из остального кода сюда не ссылаются — иначе выделенный сервер упадёт |
| `item/` | Классы предметов с поведением (`item/tool`, `item/armor` — инструменты и броня) |
| корень модуля | Точка входа модуля и его сервисы/API (`LumenMesh`, `LumenMeshIntegration`, `SolarGenerationService`) |

```
com/plainoleg/techcraft/
├── TechCraft.java          точка входа @Mod: вызывает регистрации
├── Config.java             общий конфиг
├── registry/               ModBlocks, ModItems, ModBlockEntities, ModMenuTypes, ModCreativeModTabs, ...
├── block/alloysmelter/     плавильня: блок и блок-сущность
├── menu/                   AlloySmelterMenu
├── recipe/                 рецепты плавильни
├── item/                   ForgeBook, ItemEnergy, улучшения; tool/ — молот, дрели, резак; armor/ — квантовая броня
├── client/                 TechCraftClient (регистрация экранов), экраны основной части
├── event/                  игровые события (поглощение урона бронёй)
├── worldgen/, datagen/     генерация мира и данных
├── util/                   общие утилиты
├── solar/                  солнечные панели: registry/, block/, menu/, client/ + SolarGenerationService
└── lumenmesh/              цифровая сеть: registry/, block/, item/, menu/, client/, network/, energy/, config/
```

Остальное в репозитории:

| Папка | Ответственность |
| --- | --- |
| `src/main/resources` | Рецепты, модели, текстуры, локализация и книга |
| `src/generated/resources` | Результат datagen (генерация руд); перезаписывается `./gradlew runData` |
| `src/regression/java`, `src/test/java` | Регрессионные и интеграционные проверки |
| `tools/` | Скрипты разработки: валидация ресурсов, генератор руд (`tools/oregen`), перенос пакетов |

### Соглашения

- Классы регистрации называются по модулю: `Mod*` в основной части, `Solar*` и `Lumen*` в модулях (`SolarBlocks`, `LumenBlockEntities`).
- Новый блок с блок-сущностью: класс блока и сущности — в `block/<устройство>/`, регистрация — в `registry/`, меню — в `menu/`, экран — в `client/` и регистрация экрана в `TechCraftClient`.
- Клиентские классы — только в папках `client/`.
- Для аддонов: зависеть стоит от `registry/` (что добавлено), `item/ItemEnergy` и сервисов в корне модулей; всё остальное — детали реализации.

Менеджер Lumen Mesh работает на серверном потоке. Выгрузка чанка освобождает живой объект устройства, сохраняя топологию; разрушение блока удаляет узел. Внешние инвентари и интерфейсы обращаются к хранилищу через `NetworkStorageService`.

### Переход со старой структуры (пакет `TechCraft`)

Если у вас есть локальные файлы в старых пакетах (`src/main/java/TechCraft/...`), перенесите их скриптом —
он переносит файлы, переписывает `package`, импорты и полные имена классов по той же таблице, что использовалась для репозитория:

```sh
python3 tools/migrate_packages.py --dry-run   # посмотреть план
python3 tools/migrate_packages.py             # перенести
./gradlew compileJava
```

Скрипт обрабатывает `src/main/java`, `src/test/java` и `src/regression/java`; повторный запуск ничего не ломает.

## Генерация руд

```sh
./gradlew generateOres
# или:
python3 tools/oregen/generate_ores.py --output-dir build/generated/ores
```

Результат сохраняется в `build/generated/ores/` и не подключается автоматически к сборке. Сравните заготовки с исходниками и перенесите необходимые регистрации. В `tools/oregen/ores/` сейчас описана только часть зарегистрированных руд; полная замена `ModBlocks.java` результатом генерации удалит ручные регистрации.

Подробности: [tools/oregen/README.md](tools/oregen/README.md). Результат аудита и ограничения: [docs/PROJECT_AUDIT.md](docs/PROJECT_AUDIT.md).
