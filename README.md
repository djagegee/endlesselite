# Endless Elite

Shared, modular source repository for the Hytale mods developed by Agegee and Hermes.

[![Java 25](https://img.shields.io/badge/Java-25-blue)](#build-and-tests)
[![Tests](https://img.shields.io/badge/tests-217%20passing-brightgreen)](#build-and-tests)

> [!IMPORTANT]
> The repository is build-verified, but **not approved for live deployment**. No live server was modified. Nachtweber, the combined MMOSkillTree stack, Rift Mage, Portal Spawn, and the version-specific Mjolnir patch have open in-game/release gates.

## Modules

| Path | Version | Function | Status |
|---|---:|---|---|
| `modules/core` | 0.1.0 | shared lifecycle/ownership catalog and conflict-free global localization | 3 tests GREEN |
| `modules/hub/endless-book` | 2.0.0 | shared UI/information hub, personal claims, progression display | 22 tests GREEN |
| `modules/classes/hymann` | 0.1.9 | Hymann class progression, Armament/Thunder abilities, combat systems | 11 tests GREEN |
| `modules/classes/nachtweber` | 0.1.0 | Nachtweber with three active and four passive abilities, store-bound states | 69 tests GREEN; in-game gates open |
| `modules/classes/seuchenweber` | 0.1.0 | Necrotoxin, Astral Rift, Chronoblight, auras, and MMOSkillTree integration | 87 tests GREEN |
| `modules/content/rift-mage-dungeon` | 0.1.1 | Rift Mage dungeon import, wave/asset contracts | 17 tests GREEN; deployment gate closed |
| `modules/content/portal-spawn` | unversioned | byte-exact recovery snapshot of two portal/spawn prefabs | 4 tests GREEN; not deployable |
| `modules/content/mjolnir-safety-patch` | 1.0.0 | byte-exact Patchly safety contract for Starky's Mjolnir 1.6.1 | 4 tests GREEN; version-specific gate closed |

## Shared Structure

```text
Endless Elite/
├── config/examples/                  # shared readable operator profiles
├── docs/
│   ├── architecture/                 # conflict and ownership decisions
│   └── inventory/                    # source, version, and binary evidence
├── modules/
│   ├── core/                         # shared runtime/asset owner
│   ├── hub/endless-book/
│   ├── classes/{hymann,nachtweber,seuchenweber}/
│   └── content/{rift-mage-dungeon,portal-spawn,mjolnir-safety-patch}/
├── tools/                            # verifiers, dependency reproduction, distribution
└── pom.xml                           # shared Java 25/Maven reactor
```

The project deliberately produces separate Hytale artifacts rather than an unverified monolithic JAR. This keeps plugin IDs, entry points, resources, and lifecycle owners unambiguous.

## Architecture and Ownership

- **Endless Elite Core** is the shared owner of lifecycle, ownership, and global localization.
- **EndlessBook** is the shared UI hub.
- **EndlessLeveling** remains the external owner of class, level, and prestige.
- **MMOSkillTree** remains the external owner of skills, unlocks, and bindings.
- **EndlessEliteMobs** remains the sole owner of general mob scaling.
- Class modules own only their store-/owner-bound combat states.
- Rift Mage has no player progression owner of its own.
- Build paths and operator profiles are managed centrally by the parent or `config/examples`.

Details and hard boundaries: [`docs/architecture/CONFLICT_MATRIX.md`](docs/architecture/CONFLICT_MATRIX.md).

## Configuration

Readable example profiles:

- `endlesselite-default.yml`
- `endlesselite-balanced.yml`
- `endlesselite-easy.yml`
- `endlesselite-hard.yml`
- `endlesselite-pvp.yml`

All public time values are in seconds, distances in blocks, and factors as short decimals (`1.0 = 100 %`). Complete value ranges: [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

The runtime files for the individual plugins remain module-specific so that no existing migrations or data contracts are broken silently.

## Build and Tests

### Prerequisites

- JDK 25
- Maven 3.9+
- local Hytale server/mod dependencies outside Git

Reproduce Nachtweber's additive MMOSkillTree dependency:

```bash
python tools/setup_local_dependencies.py \
  --server-root "C:/Path/to/Hytale-Server" \
  --maven "C:/Path/to/apache-maven/bin/mvn.cmd"
```

Full build:

```bash
mvn -Dhytale.server.root="C:/Path/to/Hytale-Server" clean verify
python tools/verify_repository.py
python tools/collect_distribution.py
```

Currently confirmed:

- Maven reactor: **9/9 SUCCESS**
- Tests: **217**, 0 failures, 0 errors, 0 skips
- local dependency reproduction: PASS
- repository structure gate: PASS
- build artifact collection: PASS

Detailed instructions: [`docs/BUILD_AND_TEST.md`](docs/BUILD_AND_TEST.md).

## Installation

1. Run the build and repository gate first.
2. `tools/collect_distribution.py` creates five first-party plugin JARs, including Shared Core, and a hash manifest under `dist/plugins/`.
3. Third-party dependencies are not bundled and must be provided separately in compatible versions.
4. **Do not copy directly to a live server.** First perform an isolated cold boot and the open in-game gates.
5. Rift Mage, Portal Spawn, and Mjolnir Safety Patch are not emitted as approved plugin JARs; their contract ZIPs remain blocked until their respective manual acceptance checks are complete.

**No live deployment was performed** as part of this consolidation.

## Inventory and Unintegrated Binary Versions

Complete current sources were incorporated for the five historical modules and supplemented with Shared Core and the byte-exact preserved Portal Spawn and Mjolnir Safety Patch content snapshots. The following were found only as binary backups and were therefore not blindly decompiled or integrated into production:

- HyGunsMMOCompat 1.0.5–1.0.7
- EndlessGuildsPatches (manifest 1.0.1, differing filenames)
- StarterkitChatter 1.0.0–1.0.7

All original projects and binary backups remain unchanged as additional safety copies. Details: [`docs/inventory/`](docs/inventory/).

## Open Gates

- combined cold boot with a uniform MMOSkillTree superset,
- transaction/order owner for global MMOSkillTree configuration writers,
- ownership-safe ability shutdown for Hymann and Seuchenweber, or cold-boot-only evidence,
- Hymann profile persistence across logout/profile changes/crash recovery,
- Nachtweber: client binding, activation, movement, damage, disconnect, and two-player isolation,
- Rift Mage: manual gameplay/visual acceptance and `deployment_allowed=true`,
- Portal Spawn: manifest repair, asset-pack contract, and isolated gameplay/visual acceptance,
- Mjolnir Safety Patch: Patchly/Mjolnir 1.6.1 evidence and combined runtime acceptance,
- deprecated Hytale API usage in EndlessBook, Hymann, and Seuchenweber.

## Security and Git

`.gitignore` excludes secrets, credentials, local runtime files, caches, logs, and build output. Third-party or derived third-party JARs are not committed. The initial state of the previously empty GitHub repository is preserved by the tag `baseline-before-mod-consolidation-20260810`.
