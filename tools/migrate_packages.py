#!/usr/bin/env python3
"""
Переносит Java-классы TechCraft из старой структуры пакетов (корень `TechCraft`)
в новую (`com.plainoleg.techcraft`).

Что делает с каждым .java, у которого пакет начинается с `TechCraft`:
  * переносит файл в папку нового пакета (в том же source root: src/main/java, src/test/java, src/regression/java);
  * переписывает `package`, импорты и полные имена классов в коде;
  * добавляет импорты там, где классы раньше лежали в одном пакете, а теперь в разных;
  * переименовывает классы, у которых поменялось имя (см. CLASS_MOVES);
  * упорядочивает импорты: классы мода, библиотеки, java.*.

Классы, которых нет в таблице (например, ваши локальные файлы), переезжают по правилу пакета:
`TechCraft.x.y` -> `com.plainoleg.techcraft.x.y` (с учётом PACKAGE_MOVES).

Использование (из корня проекта):
    python3 tools/migrate_packages.py --dry-run   # показать, что будет сделано
    python3 tools/migrate_packages.py             # выполнить
Повторный запуск безопасен: уже перенесённые файлы пропускаются.
"""
import argparse
import re
import sys
from pathlib import Path

OLD_ROOT = "TechCraft"
NEW_ROOT = "com.plainoleg.techcraft"
N = NEW_ROOT

# Пакеты, которые меняются не только корнем. По этому правилу переезжают и классы, не перечисленные ниже.
PACKAGE_MOVES = {
    "TechCraft.item.custom": f"{N}.item",
    "TechCraft.lumenmesh.blockentity": f"{N}.lumenmesh.block",
}

# Классы, которые переезжают иначе, чем их пакет, или меняют имя.
CLASS_MOVES = {
    # точка входа клиента и клиентские утилиты — в client
    "TechCraft.TechCraftClient": f"{N}.client.TechCraftClient",
    "TechCraft.util.NumberFormat": f"{N}.client.NumberFormat",
    # регистрации основного мода — в registry
    "TechCraft.block.ModBlocks": f"{N}.registry.ModBlocks",
    "TechCraft.block.ModBlockEntities": f"{N}.registry.ModBlockEntities",
    "TechCraft.block.ModMenuTypes": f"{N}.registry.ModMenuTypes",
    "TechCraft.block.ModRecipeTypes": f"{N}.registry.ModRecipeTypes",
    "TechCraft.item.ModItems": f"{N}.registry.ModItems",
    "TechCraft.item.ModCreativeModTabs": f"{N}.registry.ModCreativeModTabs",
    "TechCraft.item.ModArmorMaterials": f"{N}.registry.ModArmorMaterials",
    "TechCraft.item.ModDataComponents": f"{N}.registry.ModDataComponents",
    "TechCraft.item.MaterialType": f"{N}.registry.MaterialType",
    # плавильня: блок, меню, экран и рецепты — по ролям
    "TechCraft.block.AlloySmelterBlock": f"{N}.block.alloysmelter.AlloySmelterBlock",
    "TechCraft.block.AlloySmelterBlockEntity": f"{N}.block.alloysmelter.AlloySmelterBlockEntity",
    "TechCraft.block.AlloySmelterMenu": f"{N}.menu.AlloySmelterMenu",
    "TechCraft.block.AlloySmelterScreen": f"{N}.client.AlloySmelterScreen",
    "TechCraft.block.AlloyRecipe": f"{N}.recipe.AlloyRecipe",
    "TechCraft.block.AlloySmelterInput": f"{N}.recipe.AlloySmelterInput",
    # предметы: инструменты и броня — в подпапки
    "TechCraft.item.custom.DrillItem": f"{N}.item.tool.DrillItem",
    "TechCraft.item.custom.HammerItem": f"{N}.item.tool.HammerItem",
    "TechCraft.item.custom.DamageOnCraftUseItem": f"{N}.item.tool.DamageOnCraftUseItem",
    "TechCraft.item.custom.MiningArea": f"{N}.item.tool.MiningArea",
    "TechCraft.item.custom.QuantumArmorItem": f"{N}.item.armor.QuantumArmorItem",
    # солнечные панели: registry / block / menu / client
    "TechCraft.solar.ModSolarBlocks": f"{N}.solar.registry.SolarBlocks",
    "TechCraft.solar.ModSolarBlockEntities": f"{N}.solar.registry.SolarBlockEntities",
    "TechCraft.solar.ModSolarMenuTypes": f"{N}.solar.registry.SolarMenuTypes",
    "TechCraft.solar.SolarPanelBlock": f"{N}.solar.block.SolarPanelBlock",
    "TechCraft.solar.SolarPanelBlockEntity": f"{N}.solar.block.SolarPanelBlockEntity",
    "TechCraft.solar.SolarPanelBankBlock": f"{N}.solar.block.SolarPanelBankBlock",
    "TechCraft.solar.SolarPanelBankBlockEntity": f"{N}.solar.block.SolarPanelBankBlockEntity",
    "TechCraft.solar.SolarPanelBankMenu": f"{N}.solar.menu.SolarPanelBankMenu",
    "TechCraft.solar.SolarPanelBankScreen": f"{N}.solar.client.SolarPanelBankScreen",
    # Lumen Mesh: все регистрации — в lumenmesh.registry, общий доступ к сети — в корень модуля
    "TechCraft.lumenmesh.ModLumenMenuTypes": f"{N}.lumenmesh.registry.LumenMenuTypes",
    "TechCraft.lumenmesh.block.LumenBlocks": f"{N}.lumenmesh.registry.LumenBlocks",
    "TechCraft.lumenmesh.blockentity.LumenBlockEntities": f"{N}.lumenmesh.registry.LumenBlockEntities",
    "TechCraft.lumenmesh.item.LumenItems": f"{N}.lumenmesh.registry.LumenItems",
    "TechCraft.lumenmesh.block.core.LumenMeshIntegration": f"{N}.lumenmesh.LumenMeshIntegration",
}

# Классы, которые есть у автора локально, но не в репозитории. Нужны, чтобы правильно
# расставить импорты, если скрипт запускают без этих файлов. Переезжают по правилу пакета.
KNOWN_CLASSES = [
    "TechCraft.lumenmesh.LumenCapabilities",
    "TechCraft.lumenmesh.block.LumenActiveState",
    "TechCraft.lumenmesh.block.LumenFacingBlock",
    "TechCraft.lumenmesh.block.LumenFacingEntityBlock",
    "TechCraft.lumenmesh.block.cable.MeshCableBlockEntity",
    "TechCraft.lumenmesh.block.storage.PrismDriveBlock",
    "TechCraft.lumenmesh.block.storage.PrismDriveBlockEntity",
    "TechCraft.lumenmesh.block.storage.StorageLinkBlock",
    "TechCraft.lumenmesh.block.storage.StorageLinkBlockEntity",
    "TechCraft.lumenmesh.block.terminal.ItemTerminalBlock",
    "TechCraft.lumenmesh.block.terminal.ItemTerminalBlockEntity",
    "TechCraft.lumenmesh.client.ItemTerminalScreen",
    "TechCraft.lumenmesh.client.PrismDriveScreen",
    "TechCraft.lumenmesh.client.StorageLinkScreen",
    "TechCraft.lumenmesh.client.LumenGuiStyle",
    "TechCraft.lumenmesh.item.LumenBlockItem",
    "TechCraft.lumenmesh.item.LumenProcessorItem",
    "TechCraft.lumenmesh.item.StorageMediumItem",
    "TechCraft.lumenmesh.menu.ItemTerminalMenu",
    "TechCraft.lumenmesh.menu.PrismDriveMenu",
    "TechCraft.lumenmesh.menu.StorageLinkMenu",
    "TechCraft.util.NonNegativeMath",
    "TechCraft.util.MenuAccess",
]

# Все остальные классы репозитория в старой структуре (переезжают по правилу пакета).
# Нужны, чтобы при переносе ваших файлов скрипт знал, где теперь лежит каждый класс,
# даже когда сами эти файлы уже перенесены.
REPO_CLASSES = [
    "TechCraft.Config",
    "TechCraft.TechCraft",
    "TechCraft.datagen.DataGenerators",
    "TechCraft.datagen.ModDatapackProvider",
    "TechCraft.event.ModEvents",
    "TechCraft.item.ForgeBook",
    "TechCraft.item.SmelterUpgradeItem",
    "TechCraft.item.custom.ItemEnergy",
    "TechCraft.item.custom.PlateItem",
    "TechCraft.lumenmesh.LumenMesh",
    "TechCraft.lumenmesh.block.cable.MeshCableBlock",
    "TechCraft.lumenmesh.block.core.MeshCoreBlock",
    "TechCraft.lumenmesh.block.core.MeshCoreBlockEntity",
    "TechCraft.lumenmesh.block.energy.EnergyBridgeBlock",
    "TechCraft.lumenmesh.block.energy.EnergyBridgeBlockEntity",
    "TechCraft.lumenmesh.client.EnergyBridgeScreen",
    "TechCraft.lumenmesh.client.GuiLayout",
    "TechCraft.lumenmesh.client.MeshCoreScreen",
    "TechCraft.lumenmesh.config.LumenMeshConfig",
    "TechCraft.lumenmesh.energy.LumenEnergyService",
    "TechCraft.lumenmesh.menu.EnergyBridgeMenu",
    "TechCraft.lumenmesh.menu.MeshCoreMenu",
    "TechCraft.lumenmesh.network.LumenGraph",
    "TechCraft.lumenmesh.network.LumenNetwork",
    "TechCraft.lumenmesh.network.LumenNetworkManager",
    "TechCraft.lumenmesh.network.LumenNetworkNode",
    "TechCraft.lumenmesh.network.LumenNetworkSavedData",
    "TechCraft.lumenmesh.network.LumenNode",
    "TechCraft.lumenmesh.network.NbtCompat",
    "TechCraft.solar.SolarGenerationService",
    "TechCraft.solar.SolarPanelRegistry",
    "TechCraft.solar.SolarPanelType",
    "TechCraft.worldgen.ModBiomeModifiers",
    "TechCraft.worldgen.ModConfiguredFeatures",
    "TechCraft.worldgen.ModOrePlacement",
    "TechCraft.worldgen.ModPlacedFeatures",
]

SOURCE_ROOTS = ["src/main/java", "src/test/java", "src/regression/java"]

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.M)
IMPORT_RE = re.compile(r"^([ \t]*import\s+)(static\s+)?([\w.]+?)(\.\*)?(\s*;)", re.M)
IMPORT_LINE_RE = re.compile(r"^[ \t]*import\s+([\w.]+)\s*;[^\n]*\n", re.M)
# Полное имя класса проекта в коде: TechCraft[.пакет...].Класс
FQN_RE = re.compile(r"(?<![\w.])(TechCraft(?:\.[a-z_][a-z0-9_]*)*\.[A-Z][A-Za-z0-9_]*)")
TYPE_DECL_RE = re.compile(r"\b(?:class|interface|enum|record)\s+([A-Z]\w*)")
# Комментарии и строковые литералы: ссылки в них не требуют импортов.
NON_CODE_RE = re.compile(r"//[^\n]*|/\*.*?\*/|\"(?:\\.|[^\"\\\n])*\"|'(?:\\.|[^'\\\n])*'", re.S)


def is_old(name):
    return name == OLD_ROOT or name.startswith(OLD_ROOT + ".")


def split(fqn):
    pkg, _, simple = fqn.rpartition(".")
    return pkg, simple


def map_package(pkg):
    if pkg in PACKAGE_MOVES:
        return PACKAGE_MOVES[pkg]
    if is_old(pkg):
        return NEW_ROOT + pkg[len(OLD_ROOT):]
    return pkg


SIMPLE_IMPORT_RE = re.compile(r"^import\s+(static\s+)?([\w.]+(?:\.\*)?)\s*;\s*$")


def sort_imports(text):
    """Импорты по группам: классы мода, библиотеки, java.*/javax.*, static. Порядок импортов на код не влияет."""
    lines = text.split("\n")
    pkg = next((i for i, line in enumerate(lines) if line.startswith("package ")), None)
    last = max((i for i, line in enumerate(lines) if SIMPLE_IMPORT_RE.match(line)), default=None)
    if pkg is None or last is None or last < pkg:
        return text
    imports, other = {}, []
    for line in lines[pkg + 1:last + 1]:
        m = SIMPLE_IMPORT_RE.match(line)
        if m:
            imports[(bool(m.group(1)), m.group(2))] = line.strip()
        elif line.strip():
            other.append(line)  # например, javadoc класса, оказавшийся между импортами
    groups = {}
    for (static, name), line in imports.items():
        key = 3 if static else 0 if name.startswith(NEW_ROOT + ".") else 2 if name.startswith(("java.", "javax.")) else 1
        groups.setdefault(key, []).append((name, line))
    block = []
    for key in sorted(groups):
        block += [line for _, line in sorted(groups[key])] + [""]
    rest = lines[last + 1:]
    while rest and not rest[0].strip():
        rest = rest[1:]
    result = lines[:pkg + 1] + [""] + block
    result += other if other else []
    return "\n".join(result + rest)


class Migration:
    def __init__(self, project_dir):
        self.project_dir = project_dir
        self.files = []  # (root, path, old_pkg, simple)
        for root_name in SOURCE_ROOTS:
            root = project_dir / root_name
            if not root.is_dir():
                continue
            for path in sorted(root.rglob("*.java")):
                text = path.read_text(encoding="utf-8")
                m = PACKAGE_RE.search(text)
                if m and is_old(m.group(1)):
                    self.files.append((root, path, m.group(1), path.stem))

        known = (set(CLASS_MOVES) | set(KNOWN_CLASSES) | set(REPO_CLASSES)
                 | {f"{pkg}.{simple}" for _, _, pkg, simple in self.files})
        self.table = {fqn: self.map_class(fqn) for fqn in known}
        self.by_old_package = {}
        for fqn in known:
            pkg, simple = split(fqn)
            self.by_old_package.setdefault(pkg, set()).add(simple)
        self.new_classes = set(self.table.values())
        self.renames = {}
        for old, new in CLASS_MOVES.items():
            if split(old)[1] != split(new)[1]:
                self.renames[split(old)[1]] = split(new)[1]

    def map_class(self, fqn):
        if fqn in CLASS_MOVES:
            return CLASS_MOVES[fqn]
        pkg, simple = split(fqn)
        return f"{map_package(pkg)}.{simple}"

    def map_import(self, name):
        """Импорт вида a.b.C или a.b.C.member (static)."""
        if not is_old(name):
            return name
        if name in self.table:
            return self.table[name]
        parts = name.split(".")
        # класс — первый сегмент с заглавной буквы после корня (сам корень `TechCraft` тоже с заглавной)
        for i, part in enumerate(parts):
            if i > 0 and part[:1].isupper():
                cls = ".".join(parts[: i + 1])
                rest = ".".join(parts[i + 1:])
                mapped = self.table.get(cls, self.map_class(cls))
                return mapped + ("." + rest if rest else "")
        return map_package(name)

    def rewrite(self, text, old_pkg, simple):
        new_pkg = split(self.table[f"{old_pkg}.{simple}"])[0]
        head_end = self._imports_end(text)
        head, body = text[:head_end], text[head_end:]

        head = PACKAGE_RE.sub(f"package {new_pkg};", head, count=1)
        old_wildcards = [m.group(3) for m in IMPORT_RE.finditer(head)
                         if m.group(4) and not m.group(2) and is_old(m.group(3))]

        def repl_import(m):
            prefix, static, name, star, end = m.groups()
            if not is_old(name):
                return m.group(0)
            if star and not static:
                mapped = map_package(name)
            else:
                mapped = self.map_import(name)
            return f"{prefix}{static or ''}{mapped}{star or ''}{end}"

        head = IMPORT_RE.sub(repl_import, head)

        imports = {m.group(3) for m in IMPORT_RE.finditer(head) if not m.group(2) and not m.group(4)}
        imported_simple = {split(i)[1]: i for i in imports}
        non_code = [m.span() for m in NON_CODE_RE.finditer(body)]
        code_only = NON_CODE_RE.sub(lambda m: " " * len(m.group(0)), body)
        declared = set(TYPE_DECL_RE.findall(code_only))

        def in_non_code(pos):
            return any(start <= pos < end for start, end in non_code)
        new_imports = []

        def can_use_simple(cls_fqn):
            s = split(cls_fqn)[1]
            if s in imported_simple and imported_simple[s] != cls_fqn:
                return False
            if s in declared and cls_fqn != f"{new_pkg}.{simple}":
                return False
            same_pkg = {split(c)[1] for c in self.new_classes if split(c)[0] == new_pkg}
            if s in same_pkg and split(cls_fqn)[0] != new_pkg:
                return False
            return True

        def need_import(cls_fqn):
            if split(cls_fqn)[0] == new_pkg or cls_fqn in imports:
                return
            imports.add(cls_fqn)
            imported_simple[split(cls_fqn)[1]] = cls_fqn
            new_imports.append(cls_fqn)

        # 1. Полные имена классов в коде: заменяем на короткие + импорт (если нет конфликта имён).
        def repl_fqn(m):
            old = m.group(1)
            if old.count(".") == 1 and old not in self.table:
                return old  # TechCraft.MOD_ID и т.п.: обращение к полю класса TechCraft, а не полное имя
            mapped = self.table.get(old, self.map_class(old))
            if in_non_code(m.start()):
                return mapped  # в комментарии оставляем полное имя и импорт не добавляем
            if can_use_simple(mapped):
                need_import(mapped)
                return split(mapped)[1]
            return mapped

        body = FQN_RE.sub(repl_fqn, body)

        # 2. Классы, которые были видны без импорта (тот же пакет или import пакет.*),
        #    а теперь лежат в другом пакете: нужен явный импорт.
        visible = [(old_pkg, new_pkg)] + [(w, map_package(w)) for w in old_wildcards]
        for package, visible_as in visible:
            for other in sorted(self.by_old_package.get(package, ())):
                if package == old_pkg and other == simple:
                    continue
                target = self.table[f"{package}.{other}"]
                if split(target)[0] != visible_as and re.search(rf"\b{re.escape(other)}\b", code_only):
                    if can_use_simple(target):
                        need_import(target)

        # 3. Переименованные классы.
        for old_name, new_name in self.renames.items():
            body = re.sub(rf"\b{old_name}\b", new_name, body)
            head = re.sub(rf"\b{old_name}\b", new_name, head)

        # 4. Импорты из собственного пакета не нужны; дубликаты убираем.
        seen = set()

        def drop_redundant(m):
            name = m.group(1)
            if split(name)[0] == new_pkg or name in seen:
                return ""
            seen.add(name)
            return m.group(0)

        head = IMPORT_LINE_RE.sub(drop_redundant, head)
        head = re.sub(r"\n{3,}", "\n\n", head)

        if new_imports:
            lines = "".join(f"import {i};\n" for i in sorted(new_imports))
            last = list(IMPORT_RE.finditer(head))
            if last:
                newline = head.find("\n", last[-1].end())
                pos = len(head) if newline < 0 else newline + 1
                head = head[:pos] + lines + head[pos:]
            else:
                pkg_m = PACKAGE_RE.search(head)
                pos = pkg_m.end()
                head = head[:pos] + "\n\n" + lines.rstrip("\n") + head[pos:]

        new_simple = split(self.table[f"{old_pkg}.{simple}"])[1]
        return sort_imports(head + body), new_pkg, new_simple

    @staticmethod
    def _imports_end(text):
        """Конец «шапки» файла: package + import (до первого объявления типа или аннотации)."""
        end = 0
        for m in re.finditer(r"^\s*(package|import)\b[^;]*;[^\n]*\n?", text, re.M):
            end = m.end()
        return end

    def run(self, dry_run):
        if not self.files:
            print("Файлов в старых пакетах не найдено — переносить нечего.")
            return 0
        moved = 0
        for root, path, old_pkg, simple in self.files:
            text = path.read_text(encoding="utf-8")
            new_text, new_pkg, new_simple = self.rewrite(text, old_pkg, simple)
            target = root / Path(*new_pkg.split(".")) / f"{new_simple}.java"
            rel_from = path.relative_to(self.project_dir)
            rel_to = target.relative_to(self.project_dir)
            print(f"{rel_from}  ->  {rel_to}")
            if dry_run:
                continue
            if target.exists() and target.resolve() != path.resolve():
                print(f"  ! {rel_to} уже существует, файл пропущен", file=sys.stderr)
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(new_text, encoding="utf-8")
            if target.resolve() != path.resolve():
                path.unlink()
            moved += 1
        if not dry_run:
            for root_name in SOURCE_ROOTS:
                old_dir = self.project_dir / root_name / OLD_ROOT
                if old_dir.is_dir():
                    for d in sorted(old_dir.rglob("*"), key=lambda p: len(p.parts), reverse=True):
                        if d.is_dir() and not any(d.iterdir()):
                            d.rmdir()
                    if not any(old_dir.iterdir()):
                        old_dir.rmdir()
            print(f"\nПеренесено файлов: {moved}")
        return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dry-run", action="store_true", help="только показать план, ничего не менять")
    parser.add_argument("--project-dir", type=Path, default=Path(__file__).resolve().parent.parent)
    args = parser.parse_args()
    return Migration(args.project_dir.resolve()).run(args.dry_run)


if __name__ == "__main__":
    sys.exit(main())
