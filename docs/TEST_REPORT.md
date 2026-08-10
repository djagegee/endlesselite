# Endless Elite – Test Report

Date: 2026-08-10
Branch at test time: `feat/mod-consolidation-20260810`

## Toolchain

- Java 25.0.4 (Eclipse Adoptium)
- Maven 3.9.16
- Windows 11 / amd64

## Reproduced Local Dependency

`python tools/setup_local_dependencies.py ...`

- Marker: `ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS`
- Stock MMOSkillTree input SHA-256: `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`
- Owned-Effects output SHA-256: `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`
- Patch verification: exactly one modified JAR entry
- `registerIfAbsent`: PASS
- exact-instance-bound `unregister`: PASS
- local Maven installation: PASS

## Maven Reactor

Command:

```bash
mvn clean verify
```

| Module | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| Endless Elite Core | 3 | 0 | 0 | 0 |
| EndlessBook | 22 | 0 | 0 | 0 |
| Hymann | 11 | 0 | 0 | 0 |
| Nachtweber | 69 | 0 | 0 | 0 |
| Seuchenweber | 87 | 0 | 0 | 0 |
| Rift Mage Dungeon | 17 | 0 | 0 | 0 |
| Portal Spawn Snapshot | 4 | 0 | 0 | 0 |
| Mjolnir Safety Patch | 4 | 0 | 0 | 0 |
| **Total** | **217** | **0** | **0** | **0** |

Reactor Summary: parent and all eight modules `SUCCESS`.
Marker: `BUILD SUCCESS`.

## Repository and Distribution Gates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=5 java_fqcns=169 unique_resource_paths=87 rift_deployment_allowed=false portal_deployment_allowed=false mjolnir_patch_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Build-verified artifacts:

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| EndlessEliteCore.jar | 20.634 | `da935a8c112d44cdacd30b8ece40715f5765c74d58a5aed9bd6457154dda7bbd` |
| EndlessBook.jar | 82.959 | `8a45ebe4e7a1af4d901297d60be5572b1d98164de1a4bc44aaabb7198e4ddddd` |
| Hymann.jar | 350.362 | `f3ee1d9702749e7d4a55f45ab5d280eb341f9bd9a492450822b02c9912366ac1` |
| Nachtweber.jar | 165.056 | `a33fbc874f9f4a50f84fcbedc53cfbfbd3916fa7213dc508339e9cee6b7cd849` |
| Seuchenweber.jar | 132.761 | `c0c4de7d73effbf65f1a0ca61558a50bb820251673dd8d38028778668e43ab40` |

The distribution manifest explicitly sets:

- `deploymentApproved=false`
- `deploymentPerformed=false`
- `thirdPartyArtifactsBundled=false`

## Warnings and Outstanding Acceptance Checks

The build reports deprecated Hytale API usage in EndlessBook, Hymann, and Seuchenweber. This is technical debt, not test failures.

Not proven by this run:

- combined server cold boot,
- actual client/in-game functionality,
- Nachtweber binding/movement/damage/disconnect/two-player test,
- Hymann persistence during profile changes/crashes,
- conflict-free ordering of global MMOSkillTree writers,
- Rift Mage gameplay/visual acceptance,
- Portal Spawn manifest repair and gameplay/visual acceptance,
- Mjolnir Safety Patch acceptance with Patchly and Starky's Mjolnir 1.6.1,
- live deployment.
