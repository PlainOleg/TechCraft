#!/usr/bin/env python3
"""Validate JSON syntax and local model/texture references without modifying assets."""
import json
from pathlib import Path
import sys


def validate(root: Path) -> list[str]:
    errors = []
    documents = {}
    for path in sorted(root.rglob("*")):
        if path.suffix not in {".json", ".mcmeta"}:
            continue
        try:
            documents[path] = json.loads(path.read_text(encoding="utf-8"))
        except (ValueError, OSError) as error:
            errors.append(f"{path}: {error}")

    assets = root / "assets"

    def check_reference(source, reference, kind, suffix):
        if not isinstance(reference, str) or not reference.startswith("techcraft:"):
            return
        name = reference.split(":", 1)[1]
        target = assets / "techcraft" / kind / (name + suffix)
        if not target.is_file():
            errors.append(f"{source.relative_to(root)}: missing {kind} {reference}")

    for path, data in documents.items():
        if not isinstance(data, dict):
            continue
        relative = path.relative_to(root).parts
        if "models" in relative:
            check_reference(path, data.get("parent"), "models", ".json")
            for texture in data.get("textures", {}).values():
                check_reference(path, texture, "textures", ".png")
        elif "blockstates" in relative:
            def walk(value):
                if isinstance(value, dict):
                    check_reference(path, value.get("model"), "models", ".json")
                    for child in value.values():
                        walk(child)
                elif isinstance(value, list):
                    for child in value:
                        walk(child)
            walk(data)

    print(f"Checked {len(documents)} JSON/metadata files; {len(errors)} errors")
    return errors


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1] / "src/main/resources"
    errors = validate(root)
    for error in errors:
        print(error, file=sys.stderr)
    sys.exit(bool(errors))
