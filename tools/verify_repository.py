#!/usr/bin/env python
"""Fail-closed structural checks for the Endless Elite monorepo."""
from __future__ import annotations

import json
import re
import subprocess
import sys
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
FORBIDDEN_TRACKED_ROOTS = {"graphify-out"}
IGNORED_OUTPUT_PARTS = {".git", ".local", "dist", "target", "build", "out", "__pycache__"}
SECRET_PATTERNS = (
    re.compile(r"(?:api[_-]?key|access[_-]?token|refresh[_-]?token|client[_-]?secret)\s*[:=]", re.I),
    re.compile(r"authorization\s*:\s*(?:bearer|basic)\s+", re.I),
    re.compile(r"password\s*[:=]\s*['\"][^'\"]+", re.I),
)


def fail(message: str) -> None:
    raise AssertionError(message)


def main() -> int:
    manifests = []
    fqcn_to_paths: dict[str, list[str]] = defaultdict(list)

    language_gate = subprocess.run(
        [sys.executable, str(ROOT / "tools" / "verify_english_public_content.py")],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if language_gate.returncode != 0:
        details = (language_gate.stderr or language_gate.stdout).strip()
        fail(
            "Public repository content must be English. "
            "Run tools/verify_english_public_content.py for the complete report."
            + (f"\n{details[:4000]}" if details else "")
        )

    tracked_paths = subprocess.check_output(
        ["git", "ls-files", "-z"], cwd=ROOT
    ).decode("utf-8").split("\0")
    forbidden_roots = sorted(
        path for path in tracked_paths
        if path and path.split("/", 1)[0] in FORBIDDEN_TRACKED_ROOTS
    )
    if forbidden_roots:
        fail(f"Generated repository outputs must not be tracked: {forbidden_roots}")

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
    for path in tracked_candidates:
        if path.suffix.lower() in {".png", ".zip"}:
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        for line_number, line in enumerate(text.splitlines(), 1):
            if any(pattern.search(line) for pattern in SECRET_PATTERNS):
                secret_hits.append(f"{path.relative_to(ROOT)}:{line_number}")
    if secret_hits:
        fail(f"Potential secrets: {secret_hits}")

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
