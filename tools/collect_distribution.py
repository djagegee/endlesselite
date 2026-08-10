#!/usr/bin/env python
"""Collect build-verified Endless Elite artifacts after a green reactor build."""
from __future__ import annotations

import hashlib
import json
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIST = ROOT / "dist"
ARTIFACTS = {
    "EndlessEliteCore.jar": ROOT / "modules/core/target/EndlessEliteCore.jar",
    "EndlessBook.jar": ROOT / "modules/hub/endless-book/target/EndlessBook.jar",
    "Hymann.jar": ROOT / "modules/classes/hymann/target/Hymann.jar",
    "Nachtweber.jar": ROOT / "modules/classes/nachtweber/target/Nachtweber.jar",
    "Seuchenweber.jar": ROOT / "modules/classes/seuchenweber/target/Seuchenweber.jar",
}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main() -> int:
    missing = [str(path.relative_to(ROOT)) for path in ARTIFACTS.values() if not path.is_file()]
    if missing:
        print(f"ENDLESS_ELITE_DISTRIBUTION_FAIL: missing build artifacts {missing}", file=sys.stderr)
        return 1

    rift_gate_path = ROOT / "modules/content/rift-mage-dungeon/src/main/dlc/release-gate.json"
    rift_gate = json.loads(rift_gate_path.read_text(encoding="utf-8"))
    if rift_gate.get("deployment_allowed") is not False:
        print("ENDLESS_ELITE_DISTRIBUTION_FAIL: unexpected Rift Mage gate state", file=sys.stderr)
        return 1

    if DIST.exists():
        shutil.rmtree(DIST)
    plugins = DIST / "plugins"
    plugins.mkdir(parents=True)

    records = []
    for name, source in ARTIFACTS.items():
        target = plugins / name
        shutil.copy2(source, target)
        records.append({"file": f"plugins/{name}", "sha256": sha256(target), "bytes": target.stat().st_size})

    manifest = {
        "project": "Endless Elite",
        "buildVerifiedArtifacts": records,
        "deploymentApproved": False,
        "deploymentPerformed": False,
        "gates": [
            {
                "module": "rift-mage-dungeon",
                "reason": "release-gate.json keeps deployment_allowed=false until manual gameplay and visual acceptance",
            },
            {
                "module": "portal-spawn",
                "reason": "source manifest is unversioned, has no Main and declares IncludesAssetPack=false; contract snapshot is not deployable",
            },
            {
                "module": "mjolnir-safety-patch",
                "reason": "Patchly asset patch is bound to Starky Mjolnir 1.6.1 and awaits combined-stack runtime acceptance",
            },
            {
                "module": "nachtweber",
                "reason": "client binding, movement, damage, disconnect and two-player acceptance remain open",
            },
            {
                "module": "combined-stack",
                "reason": "shared MMOSkillTree writer order and cold-boot integration are not yet runtime-accepted",
            }
        ],
        "thirdPartyArtifactsBundled": False,
    }
    (DIST / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print("ENDLESS_ELITE_DISTRIBUTION_PASS")
    print(json.dumps(manifest, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
