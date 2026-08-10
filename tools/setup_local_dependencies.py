#!/usr/bin/env python
"""Hash-verify and install local proprietary dependencies for the Endless Elite reactor."""
from __future__ import annotations

import argparse
import hashlib
import os
import shutil
import subprocess
import sys
import urllib.request
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATCHER_DIR = ROOT / "modules/classes/nachtweber/tools/mmoskilltree-owned-effect-patcher"
ASM_URL = "https://repo1.maven.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.jar"
ASM_SHA256 = "876eab6a83daecad5ca67eb9fcabb063c97b5aeb8cf1fca7a989ecde17522051"


@dataclass(frozen=True)
class LocalArtifact:
    relative_path: Path
    group_id: str
    artifact_id: str
    version: str
    sha256: str

    @property
    def coordinates(self) -> str:
        return f"{self.group_id}:{self.artifact_id}:{self.version}"


LOCAL_ARTIFACTS = (
    LocalArtifact(
        Path("HytaleServer.jar"),
        "com.hypixel.hytale", "hytale-server", "0.5.0-local",
        "43d9bcff1dd31574577dbfc82147718dbe2ac16c19000071f965b521ba808cdc",
    ),
    LocalArtifact(
        Path("mods/EndlessLeveling.jar"),
        "com.airijko", "endless-leveling-core", "11.6.1-local",
        "3eea089f5400bf1d9405b8e736f9ac7639fbc423d6870f3ad1ac093fe14b7087",
    ),
    LocalArtifact(
        Path("mods/MMOSkillTree-1.5.2.jar"),
        "com.ziggfreed", "mmo-skill-tree", "1.5.2-local",
        "9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6",
    ),
    LocalArtifact(
        Path("mods/Perfect Utils-1.1.0.jar"),
        "com.narwhals", "perfect-utils", "1.1.0-local",
        "cb1ea6b184a25d0baf8d8d513a3015a682eb462f5f7eb321398901a8b7bd235a",
    ),
    LocalArtifact(
        Path("mods/EndlessGuilds-1.13.0.jar"),
        "com.airijko", "endless-guilds", "1.13.0-local",
        "ef1e0c009f0543ed7716130fd726411c90f4b2359ee925b562cc843b618f856a",
    ),
)
OWNED_MMO_ARTIFACT = LocalArtifact(
    Path(".local/mmoskilltree-owned/MMOSkillTree-1.5.2-owned-effects.jar"),
    "com.ziggfreed", "mmo-skill-tree", "1.5.2-owned-local", "",
)


def digest(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def validated_source(server_root: Path, artifact: LocalArtifact) -> Path:
    source = server_root.resolve() / artifact.relative_path
    if not source.is_file():
        raise RuntimeError(f"Missing local dependency: {source}")
    actual = digest(source)
    if actual != artifact.sha256:
        raise RuntimeError(
            f"Unexpected SHA-256 for {artifact.coordinates}: {actual}; expected {artifact.sha256}"
        )
    return source


def install_arguments(source: Path, artifact: LocalArtifact) -> list[str]:
    return [
        "install:install-file",
        f"-Dfile={source}",
        f"-DgroupId={artifact.group_id}",
        f"-DartifactId={artifact.artifact_id}",
        f"-Dversion={artifact.version}",
        "-Dpackaging=jar",
        "-DgeneratePom=true",
    ]


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


def install(maven: Path, source: Path, artifact: LocalArtifact) -> None:
    run([str(maven), *install_arguments(source, artifact)], command_script=maven.suffix.lower() == ".cmd")


def build_owned_mmo(source_jar: Path, maven: Path) -> Path:
    workspace = ROOT / ".local/mmoskilltree-owned"
    classes = workspace / "classes"
    asm = workspace / "asm-9.8.jar"
    output = workspace / "MMOSkillTree-1.5.2-owned-effects.jar"
    workspace.mkdir(parents=True, exist_ok=True)

    if not asm.is_file() or digest(asm) != ASM_SHA256:
        temporary = asm.with_suffix(".download")
        urllib.request.urlretrieve(ASM_URL, temporary)
        if digest(temporary) != ASM_SHA256:
            temporary.unlink(missing_ok=True)
            raise RuntimeError("ASM 9.8 SHA-256 verification failed")
        temporary.replace(asm)

    javac = java_tool("javac")
    java = java_tool("java")
    patcher_source = PATCHER_DIR / "OwnedEffectRegistryPatcher.java"
    verifier = PATCHER_DIR / "verify_patch.py"
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    run([str(javac), "-cp", str(asm), "-d", str(classes), str(patcher_source)])
    run([
        str(java), "-cp", os.pathsep.join((str(classes), str(asm))),
        "OwnedEffectRegistryPatcher", str(source_jar), str(output),
    ])
    run([sys.executable, str(verifier), str(source_jar), str(output)])
    install(maven, output, OWNED_MMO_ARTIFACT)
    return output


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--server-root", type=Path, required=True)
    parser.add_argument("--maven", help="Path to mvn or mvn.cmd when Maven is not on PATH")
    args = parser.parse_args()

    maven = maven_tool(args.maven)
    sources: dict[str, Path] = {}
    for artifact in LOCAL_ARTIFACTS:
        source = validated_source(args.server_root, artifact)
        install(maven, source, artifact)
        sources[artifact.coordinates] = source
        print(f"installed={artifact.coordinates} sha256={artifact.sha256}")

    raw_mmo = sources["com.ziggfreed:mmo-skill-tree:1.5.2-local"]
    owned_mmo = build_owned_mmo(raw_mmo, maven)
    print("ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS")
    print(f"owned_mmo_sha256={digest(owned_mmo)}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, RuntimeError, subprocess.CalledProcessError) as error:
        print(f"ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_FAIL: {error}", file=sys.stderr)
        raise SystemExit(1)
