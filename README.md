# Endless Elite

Shared, modular source repository for the Hytale mods developed by Agegee and Hermes.

[![Java 25](https://img.shields.io/badge/Java-25-blue)](#build-and-tests)
[![Tests](https://img.shields.io/badge/tests-233%20passing-brightgreen)](#build-and-tests)

> [!IMPORTANT]
> The repository is build-verified, but **not approved for live deployment**. No live server was modified. Nachtweber, the combined MMOSkillTree stack, Rift Mage, Portal Spawn, and the version-bound Mjolnir patch have outstanding in-game/release gates.

## Modules

| Path | Version | Purpose | Status |
|---|---:|---|---|
| `modules/core` | 0.1.0 | shared lifecycle/ownership catalog and conflict-free global localization | 13 tests GREEN |
| `modules/hub/endless-book` | 2.0.0 | shared UI/information hub, personal claims, progression display | 22 tests GREEN |
| `modules/classes/hymann` | 0.1.9 | Hymann class progression, Armament/Thunder abilities, combat systems | 12 tests GREEN |
| `modules/classes/nachtweber` | 0.1.0 | Nachtweber with three active and four passive abilities, store-bound state | 70 tests GREEN; in-game gates open |
| `modules/classes/seuchenweber` | 0.1.0 | Necrotoxin, Astral Rift, Chronoblight, auras, and MMOSkillTree integration | 89 tests GREEN |
| `modules/content/rift-mage-dungeon` | 0.1.1 | Rift Mage dungeon import, wave/asset contracts | 17 tests GREEN; deployment gate closed |
| `modules/content/portal-spawn` | unversioned | byte-exact recovery snapshot of two portal/spawn prefabs | 5 tests GREEN; not deployable |
| `modules/content/mjolnir-safety-patch` | 1.0.0 | byte-exact Patchly safety contract for Starky's Mjolnir 1.6.1 | 5 tests GREEN; version-bound gate closed |

## Shared structure

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
├── tools/                            # verifier, dependency reproduction, distribution
└── pom.xml                           # shared Java 25/Maven reactor
```

The project deliberately produces separate Hytale artifacts rather than an unverified monolith JAR. This keeps plugin IDs, entry points, resources, and lifecycle owners unambiguous.

## Architecture and ownership

- **Endless Elite Core** is the shared lifecycle, ownership, and global-localization owner.
- **EndlessBook** is the shared UI hub.
- **EndlessLeveling** remains the external owner for class, level, and prestige.
- **MMOSkillTree** remains the external owner for skills, unlocks, and bindings.
- **EndlessEliteMobs** remains the sole owner of general mob scaling.
- Class modules own only their store-/owner-bound combat state.
- Rift Mage has no player-progression owner of its own.
- Local proprietary build artifacts are hash-bootstrapped into Maven; operator profiles remain under `config/examples`.

Details and hard boundaries: [`docs/architecture/CONFLICT_MATRIX.md`](docs/architecture/CONFLICT_MATRIX.md).

## Configuration

Readable example profiles:

- `endlesselite-default.yml`
- `endlesselite-balanced.yml`
- `endlesselite-easy.yml`
- `endlesselite-hard.yml`
- `endlesselite-pvp.yml`

All public times are in seconds, distances in blocks, and factors as short decimal values (`1.0 = 100 %`). Full value ranges: [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

The runtime files for the individual plugins remain module-specific so that existing migrations or data contracts are not silently broken.

## Build and tests

### Prerequisites

- JDK 25
- Maven 3.9+
- local Hytale server/mod dependencies outside Git

Hash-verify and install all proprietary local dependencies, including Nachtweber's additive MMOSkillTree artifact:

```bash
python tools/setup_local_dependencies.py \
  --server-root "C:/Pfad/zum/Hytale-Server" \
  --maven "C:/Pfad/zu/apache-maven/bin/mvn.cmd"
```

Full build:

```bash
mvn clean verify
python tools/verify_repository.py
python tools/collect_distribution.py
```

Currently confirmed:

- Maven reactor: **9/9 SUCCESS**
- Tests: **233**, 0 Failures, 0 Errors, 0 Skips
- local dependency reproduction: PASS
- repository structure gate: PASS
- build artifact collection: PASS

Detailed instructions: [`docs/BUILD_AND_TEST.md`](docs/BUILD_AND_TEST.md).

## Installation

1. First run the build and repository gate.
2. `tools/collect_distribution.py` produces five separate plugin JARs under `dist/plugins/`, including Shared Core, and a hash manifest.
3. Third-party dependencies are not bundled and must be separately available in compatible versions.
4. **Do not copy directly to a live server.** First perform an isolated cold boot and the outstanding in-game gates.
5. Rift Mage, Portal Spawn, and Mjolnir Safety Patch are not produced as approved plugin JARs; their contract ZIPs remain blocked until their respective manual acceptance.

**No live deployment was performed** as part of this consolidation.

## Inventory and non-integrated binary states

Complete current sources were adopted for the five historical modules and supplemented with Shared Core and the byte-exact preserved content snapshots Portal Spawn and Mjolnir Safety Patch. Found only as binary backups and therefore not blindly decompiled or integrated into production:

- HyGunsMMOCompat 1.0.5–1.0.7
- EndlessGuildsPatches (Manifest 1.0.1, differing file names)
- StarterkitChatter 1.0.0–1.0.7

All original projects and binary backups remain unchanged as additional safety copies. Details: [`docs/inventory/`](docs/inventory/).

## Outstanding gates

- combined cold boot with a unified MMOSkillTree superset,
- transaction/order owner for global MMOSkillTree configuration writers,
- combined runtime acceptance of the hash-verified MMOSkillTree owned-effects ABI,
- Hymann profile persistence across logout/profile switch/crash recovery,
- Nachtweber: client binding, activation, movement, damage, disconnect, and two-player isolation,
- Rift Mage: manual gameplay/visual acceptance and `deployment_allowed=true`,
- Portal Spawn: manifest repair, asset-pack contract, and isolated gameplay/visual acceptance,
- Mjolnir Safety Patch: Patchly/Mjolnir-1.6.1 evidence and combined runtime acceptance,
- outdated Hytale API usage in EndlessBook, Hymann, and Seuchenweber.

## Security and Git

`.gitignore` excludes secrets, credentials, local runtime, caches, logs, and build output. External or derived third-party JARs are not committed. The initial state of the previously empty GitHub repository is preserved with the `baseline-before-mod-consolidation-20260810` tag.
