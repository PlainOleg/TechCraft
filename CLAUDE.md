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
- `tools/` — скрипты разработки: `migrate_packages.py` (перенос из старого пакета `TechCraft`), `oregen/` (генератор руд), `verify_resources.py` (проверка JSON, входит в `build`), генераторы GUI и моделей Lumen.
- `src/regression` — детерминированные сценарии сети, хранилища и генерации (`./gradlew regressionTest`); `src/test` — JUnit.

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

- Нет моделей и текстур для улучшений плавильни `heat/efficiency/capacity_upgrade_t2/t3` (в игре — фиолетово-чёрный куб).
- В `lang/*.json` есть ключи для незарегистрированных предметов `blank_prism` и `network_processor`.
- Призмитовые и квантовые инструменты, луки и мечи — пока обычные `Item` без поведения.
- Плавильня (`block/alloysmelter`) сжигает топливо, но ничего не плавит: `getMeltResult` возвращает пустой результат, рецепты закомментированы.
- `LumenCapabilities` регистрирует `Capabilities.EnergyStorage.ITEM` только для квантовой брони; дрели нужно добавить через `DrillItem.getEnergyStorage(stack)` — иначе зарядники других модов их не видят.
- Энергия при слиянии сетей Lumen Mesh: ёмкость = большая из двух (так решил автор: сумма давала рост ёмкости при каждом разрыве и починке кабеля), энергия сверх неё теряется.
