#!/usr/bin/env python
"""Fail-closed structural checks for the Endless Elite monorepo."""
from __future__ import annotations

import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULES = ROOT / "modules"
EXPECTED_PLUGIN_IDS = {
    "Shadow:EndlessElite",
    "Shadow:EndlessBook",
    "Shadow:Hymann",
    "Shadow:Nachtweber",
    "Shadow:Seuchenweber",
}
FORBIDDEN_TRACKED_SUFFIXES = {".class", ".jar", ".log", ".env"}
IGNORED_OUTPUT_PARTS = {".git", ".local", "dist", "target", "build", "out", "__pycache__"}
SECRET_PATTERNS = (
    re.compile(r"(?:api[_-]?key|access[_-]?token|refresh[_-]?token|client[_-]?secret)\s*[:=]", re.I),
    re.compile(r"authorization\s*:\s*(?:bearer|basic)\s+", re.I),
    re.compile(r"password\s*[:=]\s*['\"][^'\"]+", re.I),
)
PRIVATE_PUBLIC_PATH = re.compile(r"(?i)[A-Z]:[\\/]Users[\\/][^\\/\s\"']+")


def fail(message: str) -> None:
    raise AssertionError(message)


def main() -> int:
    manifests = []
    fqcn_to_paths: dict[str, list[str]] = defaultdict(list)

    maven_namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
    system_paths = []
    user_paths = []
    for pom in ROOT.rglob("pom.xml"):
        document = ET.parse(pom)
        for node in document.findall(".//m:systemPath", maven_namespace):
            system_paths.append(f"{pom.relative_to(ROOT)}:{node.text}")
        text = pom.read_text(encoding="utf-8")
        if re.search(r"[A-Za-z]:[/\\]Users[/\\]", text, re.I):
            user_paths.append(str(pom.relative_to(ROOT)))
    if system_paths:
        fail(f"Non-portable Maven systemPath dependencies: {system_paths}")
    if user_paths:
        fail(f"User-specific absolute paths in Maven configuration: {user_paths}")

    class_modules = ("hymann", "nachtweber", "seuchenweber")
    for module in class_modules:
        module_root = MODULES / f"classes/{module}"
        pom_document = ET.parse(module_root / "pom.xml")
        mmo_versions = []
        for dependency in pom_document.findall(".//m:dependency", maven_namespace):
            if dependency.findtext("m:groupId", namespaces=maven_namespace) == "com.ziggfreed" and dependency.findtext("m:artifactId", namespaces=maven_namespace) == "mmo-skill-tree":
                mmo_versions.append(dependency.findtext("m:version", namespaces=maven_namespace))
        if mmo_versions != ["1.5.2-owned-local"]:
            fail(f"{module} must compile against exactly one hash-verified MMOSkillTree owned-effects ABI dependency: {mmo_versions}")
        manifest = json.loads((module_root / "src/main/resources/manifest.json").read_text(encoding="utf-8"))
        if "owned-effects ABI patch" not in manifest.get("Description", ""):
            fail(f"{module} manifest must disclose the required MMOSkillTree owned-effects ABI patch")
        plugin_source = next((module_root / "src/main/java").rglob("*Plugin.java"))
        plugin_text = plugin_source.read_text(encoding="utf-8")
        preflight = f"MmoOwnedEffectAbi.requireAvailable({module.capitalize()}Plugin.class.getClassLoader());"
        preflight_position = plugin_text.find(preflight)
        mutation_markers = ("PermissionsModule.registerPermission", "RuntimePort port = new RuntimePort()")
        mutation_positions = [plugin_text.find(marker) for marker in mutation_markers if plugin_text.find(marker) >= 0]
        if preflight_position < 0 or not mutation_positions or preflight_position > min(mutation_positions):
            fail(f"{module} must fail closed before its first plugin mutation when the owned-effects ABI is absent")
    for module in class_modules:
        plugin_text = next((MODULES / f"classes/{module}/src/main/java").rglob("*Plugin.java")).read_text(encoding="utf-8")
        if ".registerIfAbsent(" not in plugin_text or ".unregister(" not in plugin_text:
            fail(f"{module} must own and symmetrically unregister exact MMOSkillTree effect instances")
        if "ActiveAbilityService.getInstance().register(" in plugin_text:
            fail(f"{module} must not overwrite MMOSkillTree effects with direct register()")
        if "rollbackSetup(error" not in plugin_text or "throw propagateSetupFailure(error);" not in plugin_text:
            fail(f"{module} must roll back and rethrow setup failures")
        if "BestEffortCleanup.run(" not in plugin_text:
            fail(f"{module} must continue all cleanup actions after Throwable/Error")

    rift_waves = json.loads((MODULES / "content/rift-mage-dungeon/src/main/dlc/waves/rift_mage_technodistrict_waves.json").read_text(encoding="utf-8"))
    if rift_waves.get("mob_scaling_owner") != "EndlessEliteMobs":
        fail("Rift Mage must name EndlessEliteMobs as the sole general mob-scaling owner")

    for manifest_path in MODULES.rglob("src/main/resources/manifest.json"):
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        plugin_id = f"{manifest.get('Group')}:{manifest.get('Name')}"
        main_class = manifest.get("Main")
        if not main_class:
            fail(f"Missing Main in {manifest_path.relative_to(ROOT)}")
        manifests.append((plugin_id, main_class, manifest_path))

    ids = [entry[0] for entry in manifests]
    if set(ids) != EXPECTED_PLUGIN_IDS or len(ids) != len(set(ids)):
        fail(f"Unexpected or duplicate plugin IDs: {ids}")

    resource_owners: dict[str, list[str]] = defaultdict(list)
    for plugin_id, _, manifest_path in manifests:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        if plugin_id != "Shadow:EndlessElite" and "Shadow:EndlessElite" not in manifest.get("Dependencies", {}):
            fail(f"Feature plugin does not require Shadow:EndlessElite: {plugin_id}")
        resource_root = manifest_path.parent
        for resource in resource_root.rglob("*"):
            if resource.is_file() and resource.name != "manifest.json":
                relative = resource.relative_to(resource_root).as_posix()
                resource_owners[relative].append(plugin_id)
    duplicate_resources = {path: owners for path, owners in resource_owners.items() if len(owners) > 1}
    if duplicate_resources:
        fail(f"Competing plugin resource paths: {duplicate_resources}")

    for source in MODULES.rglob("src/main/java/**/*.java"):
        text = source.read_text(encoding="utf-8", errors="strict")
        package = re.search(r"^\s*package\s+([\w.]+);", text, re.M)
        if package:
            fqcn_to_paths[f"{package.group(1)}.{source.stem}"].append(str(source.relative_to(ROOT)))

    duplicates = {name: paths for name, paths in fqcn_to_paths.items() if len(paths) > 1}
    if duplicates:
        fail(f"Duplicate Java FQCNs: {duplicates}")

    for plugin_id, main_class, manifest_path in manifests:
        if main_class not in fqcn_to_paths:
            fail(f"Entrypoint {main_class} for {plugin_id} has no source")

    tracked_candidates = [
        path for path in ROOT.rglob("*")
        if path.is_file() and not any(part in IGNORED_OUTPUT_PARTS for part in path.relative_to(ROOT).parts)
    ]
    forbidden = [str(path.relative_to(ROOT)) for path in tracked_candidates if path.suffix.lower() in FORBIDDEN_TRACKED_SUFFIXES]
    if forbidden:
        fail(f"Forbidden generated/binary files outside ignored outputs: {forbidden}")

    secret_hits = []
    private_path_hits = []
    for path in tracked_candidates:
        if path.suffix.lower() in {".png", ".zip"}:
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        for line_number, line in enumerate(text.splitlines(), 1):
            if any(pattern.search(line) for pattern in SECRET_PATTERNS):
                secret_hits.append(f"{path.relative_to(ROOT)}:{line_number}")
            if PRIVATE_PUBLIC_PATH.search(line):
                private_path_hits.append(f"{path.relative_to(ROOT)}:{line_number}")
    if secret_hits:
        fail(f"Potential secrets: {secret_hits}")
    if private_path_hits:
        fail(f"User-specific paths in public text: {private_path_hits}")

    rift_gate = json.loads((MODULES / "content/rift-mage-dungeon/src/main/dlc/release-gate.json").read_text(encoding="utf-8"))
    if rift_gate.get("deployment_allowed") is not False:
        fail("Rift Mage release gate must remain fail-closed until manual acceptance")

    portal_gate = json.loads((MODULES / "content/portal-spawn/src/main/snapshot/release-gate.json").read_text(encoding="utf-8"))
    if portal_gate.get("deployment_allowed") is not False:
        fail("Portal Spawn release gate must remain fail-closed until manifest repair and manual acceptance")
    mjolnir_gate = json.loads((MODULES / "content/mjolnir-safety-patch/src/main/snapshot/release-gate.json").read_text(encoding="utf-8"))
    if mjolnir_gate.get("deployment_allowed") is not False:
        fail("Mjolnir Safety Patch gate must remain fail-closed until version-bound runtime acceptance")

    print("ENDLESS_ELITE_REPOSITORY_VERIFY_PASS")
    print(f"plugins={len(manifests)} java_fqcns={len(fqcn_to_paths)} unique_resource_paths={len(resource_owners)} rift_deployment_allowed=false portal_deployment_allowed=false mjolnir_patch_deployment_allowed=false")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (AssertionError, OSError, ValueError, json.JSONDecodeError) as error:
        print(f"ENDLESS_ELITE_REPOSITORY_VERIFY_FAIL: {error}", file=sys.stderr)
        raise SystemExit(1)
