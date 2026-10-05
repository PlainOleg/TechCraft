# TechCraft — заметки для Claude

Мод для Minecraft 1.21.1 на NeoForge 21.1.209, Java 21. `mod_id = techcraft`, код в пакете `com.plainoleg.techcraft`.
Автор пишет по-русски — отвечай по-русски.

## Команды

```sh
./gradlew compileJava      # быстрая проверка, что всё компилируется
./gradlew build            # сборка + regressionTest + test + verifyResources
./gradlew runClient        # запуск игры для проверки
./gradlew runData          # datagen → src/generated/resources
./gradlew generateOres     # заготовки руд из tools/oregen/ores/*.json → build/generated/ores
```

Первая сборка скачивает Minecraft и NeoForge (нужен доступ к maven.neoforged.net и серверам Mojang).

## Структура

Одинаковая раскладка в основной части (`com.plainoleg.techcraft`) и в модулях `solar`, `lumenmesh`:

- `registry/` — все `DeferredRegister`. Имена: `Mod*` в основной части, `Solar*` / `Lumen*` в модулях.
- `block/` — блоки и их блок-сущности; у устройства из нескольких классов своя подпапка.
- `menu/` — контейнеры; `client/` — экраны и любой клиентский код; `item/` — предметы (`item/tool`, `item/armor`).
- Корень модуля — точка входа и сервисы: `LumenMesh`, `LumenMeshIntegration`, `SolarGenerationService`.
- `tools/` — скрипты разработки: `migrate_packages.py` (перенос из старого пакета `TechCraft`), `oregen/` (генератор руд).

Подробно — в README, раздел «Структура».

## Правила

- Клиентские классы (всё, что трогает `net.minecraft.client`) — только в папках `client/`; общий код на них не ссылается, иначе падает выделенный сервер.
- ID в реестрах (`techcraft:...`) — часть сохранений игроков. Не переименовывай без явной просьбы и без миграции.
- Форматы сохранений обратно совместимы:
  - `LumenNetworkSavedData` пишет версию 2 (ListTag) и читает версию 1 (ключи `node_N`);
  - энергия предметов — компонент `techcraft:energy` (`ModDataComponents.ENERGY`, `item/ItemEnergy`); старый ключ `Energy` в `CUSTOM_DATA` читается и переносится при записи.
- Публичный API `lumenmesh/network` (`LumenNetworkManager`, `LumenGraph`, `LumenNetwork`, `LumenNetworkSavedData`) используют устройства сети — сигнатуры не ломай.
- Генерация руд лежит в `src/generated/resources` (результат datagen, обновляется `./gradlew runData`). Не дублируй эти файлы в `src/main/resources`: Gradle упадёт на одинаковых путях.
- Перед коммитом: `./gradlew compileJava` (а лучше `build`). Работай в отдельной ветке, в `main` не пушь без просьбы автора.

## Известные проблемы

- В Git-репозитории не было ~25 классов и ~130 текстур, которые есть только у автора локально (хранилище и терминалы Lumen Mesh, `LumenCapabilities`, `LumenFacingBlock`, `LumenActiveState`, `MiningArea`, `NonNegativeMath`, `MenuAccess`, часть экранов и меню, `src/regression`, `tools/verify_resources.py`). Пока их нет в репозитории, сборка на GitHub падает.
- Нет blockstate и моделей для ~40 блоков (большинство устройств Lumen Mesh, часть руд и блоков металлов).
- Призмитовые и квантовые инструменты, луки и мечи — пока обычные `Item` без поведения.
- Плавильня (`block/alloysmelter`) сжигает топливо, но ничего не плавит: `getMeltResult` возвращает пустой результат, рецепты закомментированы.
- Копание по площади для дрели и молота не подключено (`getBlocksToBeDestroyed` нигде не вызывается), поэтому дрель не тратит энергию.
- `LumenCapabilities` должен регистрировать `Capabilities.EnergyStorage.ITEM` для дрелей через `DrillItem.getEnergyStorage(stack)` — иначе зарядники других модов их не видят.
- Энергия при слиянии сетей Lumen Mesh: ёмкость = большая из двух, энергия сверх неё теряется.
