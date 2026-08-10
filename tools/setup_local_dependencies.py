#!/usr/bin/env python
"""Reproduce and install Nachtweber's ownership-safe MMOSkillTree 1.5.2 dependency."""
from __future__ import annotations

import argparse
import hashlib
import os
import shutil
import subprocess
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATCHER_DIR = ROOT / "modules/classes/nachtweber/tools/mmoskilltree-owned-effect-patcher"
ASM_URL = "https://repo1.maven.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.jar"
ASM_SHA256 = "876eab6a83daecad5ca67eb9fcabb063c97b5aeb8cf1fca7a989ecde17522051"
MMO_SKILL_TREE_SHA256 = "9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6"


def digest(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def java_tool(name: str) -> Path:
    executable = f"{name}.exe" if os.name == "nt" else name
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        candidate = Path(java_home) / "bin" / executable
        if candidate.is_file():
            return candidate
    discovered = shutil.which(executable)
    if discovered:
        return Path(discovered)
    raise RuntimeError(f"{name} not found; set JAVA_HOME to JDK 25")


def maven_tool(explicit: str | None) -> Path:
    candidates = []
    if explicit:
        candidates.append(Path(explicit))
    maven_home = os.environ.get("MAVEN_HOME")
    if maven_home:
        candidates.append(Path(maven_home) / "bin" / ("mvn.cmd" if os.name == "nt" else "mvn"))
    for name in ("mvn.cmd", "mvn"):
        discovered = shutil.which(name)
        if discovered:
            candidates.append(Path(discovered))
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    raise RuntimeError("Maven not found; set MAVEN_HOME, add mvn to PATH, or pass --maven")


def run(command: list[str], *, command_script: bool = False) -> None:
    if command_script and os.name == "nt":
        command = ["cmd.exe", "/d", "/c", *command]
    subprocess.run(command, check=True, cwd=ROOT)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--server-root", type=Path, required=True)
    parser.add_argument("--maven", help="Path to mvn or mvn.cmd when Maven is not on PATH")
    args = parser.parse_args()

    source_jar = args.server_root.resolve() / "mods" / "MMOSkillTree-1.5.2.jar"
    if not source_jar.is_file():
        raise RuntimeError(f"Missing source JAR: {source_jar}")
    source_hash = digest(source_jar)
    if source_hash != MMO_SKILL_TREE_SHA256:
        raise RuntimeError(f"Unexpected MMOSkillTree input SHA-256: {source_hash}")

    workspace = ROOT / ".local" / "mmoskilltree-owned"
    classes = workspace / "classes"
    asm = workspace / "asm-9.8.jar"
    output = workspace / "MMOSkillTree-1.5.2-owned-effects.jar"
    classes.mkdir(parents=True, exist_ok=True)

    if not asm.is_file() or digest(asm) != ASM_SHA256:
        asm.parent.mkdir(parents=True, exist_ok=True)
        temporary = asm.with_suffix(".download")
        urllib.request.urlretrieve(ASM_URL, temporary)
        if digest(temporary) != ASM_SHA256:
            temporary.unlink(missing_ok=True)
            raise RuntimeError("ASM 9.8 SHA-256 verification failed")
        temporary.replace(asm)

    javac = java_tool("javac")
    java = java_tool("java")
    maven = maven_tool(args.maven)
    patcher_source = PATCHER_DIR / "OwnedEffectRegistryPatcher.java"
    verifier = PATCHER_DIR / "verify_patch.py"

    shutil.rmtree(classes)
    classes.mkdir(parents=True)
    run([str(javac), "-cp", str(asm), "-d", str(classes), str(patcher_source)])
    run([str(java), "-cp", os.pathsep.join((str(classes), str(asm))),
         "OwnedEffectRegistryPatcher", str(source_jar), str(output)])
    run([sys.executable, str(verifier), str(source_jar), str(output)])
    run([str(maven), "install:install-file", f"-Dfile={output}",
         "-DgroupId=com.ziggfreed", "-DartifactId=mmo-skill-tree",
         "-Dversion=1.5.2-owned-local", "-Dpackaging=jar", "-DgeneratePom=true"],
        command_script=maven.suffix.lower() == ".cmd")

    print("ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS")
    print(f"input_sha256={source_hash}")
    print(f"output_sha256={digest(output)}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, RuntimeError, subprocess.CalledProcessError) as error:
        print(f"ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_FAIL: {error}", file=sys.stderr)
        raise SystemExit(1)
