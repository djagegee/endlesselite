from __future__ import annotations

import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

RENDERER = "com/ziggfreed/mmoskilltree/ability/AbilityDescriptionRenderer.class"
BIND_PAGE = "com/ziggfreed/mmoskilltree/pages/skill/AbilityBindPage.class"
EXPECTED = {RENDERER, BIND_PAGE}


def javap_tool() -> Path:
    executable = "javap.exe" if os.name == "nt" else "javap"
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        candidate = Path(java_home) / "bin" / executable
        if candidate.is_file():
            return candidate
    discovered = shutil.which(executable)
    if discovered:
        return Path(discovered)
    raise RuntimeError("javap not found; set JAVA_HOME to JDK 25")


def javap(jar: Path, class_name: str) -> str:
    result = subprocess.run(
        [str(javap_tool()), "-classpath", str(jar), "-p", "-c", class_name],
        check=True, capture_output=True)
    return result.stdout.decode("utf-8", "replace")


def method_slice(text: str, start_marker: str, next_marker: str = "\n  private ") -> str:
    start = text.index(start_marker)
    end = text.find(next_marker, start + len(start_marker))
    return text[start:] if end < 0 else text[start:end]


def main() -> int:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: verify_patch.py <original.jar> <patched.jar>")
    original, patched = map(Path, sys.argv[1:])
    with zipfile.ZipFile(original) as before, zipfile.ZipFile(patched) as after:
        before_names = before.namelist()
        after_names = after.namelist()
        assert before_names == after_names, "JAR entry order/content set changed"
        changed = {name for name in before_names if before.read(name) != after.read(name)}
        assert changed == EXPECTED, f"unexpected changed entries: {sorted(changed)}"
        assert b"useCustomCardDescription" in after.read(RENDERER)
        assert b"useCustomCardDescription" not in before.read(RENDERER)

    renderer = javap(patched, "com.ziggfreed.mmoskilltree.ability.AbilityDescriptionRenderer")
    card = method_slice(renderer,
        "public static com.hypixel.hytale.server.core.Message renderCardMsg")
    full = method_slice(renderer,
        "public static com.hypixel.hytale.server.core.Message renderMsg(",
        "\n  static")
    assert "AbilityDefinition.getBool" in card
    assert "Messages.abilityFlavorMsg" in card
    assert "renderPlainEnglishMsg" in card
    assert "AbilityDefinition.getBool" in full
    assert "Messages.abilityFlavorMsg" in full
    assert "renderMsg$seuchenweberOriginal" in renderer

    bind_page = javap(patched, "com.ziggfreed.mmoskilltree.pages.skill.AbilityBindPage")
    ability_list = method_slice(bind_page, "private void populateAbilityList")
    assert ability_list.count("ability.bind_page.passive_tag") == 2
    assert ability_list.count("ability.bind_page.select_disabled") == 1

    print("PATCH_CONTRACT=PASS")
    print("CUSTOM_TOOLTIP=PASS")
    print("PASSIVE_BUTTON=PASS")
    print("CHANGED_ENTRIES=2")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
