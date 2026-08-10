from __future__ import annotations
import hashlib
import subprocess
import sys
import zipfile
from pathlib import Path

ENTRY = "com/ziggfreed/mmoskilltree/ability/ActiveAbilityService.class"
JAVAP = Path(r"C:\Users\agege\AppData\Local\Programs\Java\jdk-25.0.4+7\bin\javap.exe")


def javap(jar: Path) -> str:
    result = subprocess.run(
        [str(JAVAP), "-classpath", str(jar), "-p", "-c",
         "com.ziggfreed.mmoskilltree.ability.ActiveAbilityService"],
        check=True, capture_output=True)
    return result.stdout.decode("utf-8", "replace")


def method(text: str, marker: str) -> str:
    start = text.index(marker)
    end = text.find("\n  public ", start + len(marker))
    if end < 0:
        end = text.find("\n  static", start + len(marker))
    return text[start:] if end < 0 else text[start:end]


def main() -> int:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: verify_patch.py <input.jar> <patched.jar>")
    before_path, after_path = map(Path, sys.argv[1:])
    with zipfile.ZipFile(before_path) as before, zipfile.ZipFile(after_path) as after:
        names = before.namelist()
        assert names == after.namelist(), "JAR entry order/content set changed"
        changed = {name for name in names if before.read(name) != after.read(name)}
        assert changed == {ENTRY}, f"unexpected changed entries: {sorted(changed)}"
    text = javap(after_path)
    register = method(text, "public boolean registerIfAbsent")
    unregister = method(text, "public boolean unregister")
    assert "String.toUpperCase" in register
    assert "Map.putIfAbsent" in register
    assert "String.toUpperCase" in unregister
    assert "Map.remove:(Ljava/lang/Object;Ljava/lang/Object;)Z" in unregister
    print("OWNED_EFFECT_REGISTRY_PATCH=PASS")
    print("REGISTER_IF_ABSENT=PASS")
    print("EXACT_INSTANCE_UNREGISTER=PASS")
    print("CHANGED_ENTRIES=1")
    print("INPUT_SHA256=" + hashlib.sha256(before_path.read_bytes()).hexdigest())
    print("OUTPUT_SHA256=" + hashlib.sha256(after_path.read_bytes()).hexdigest())
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
