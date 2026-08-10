# Endless Elite test report

Date: 2026-08-10
Tested branch: `fix/late-architecture-remediation-20260810` (based on `integration/endless-elite-final-20260810`)

## Toolchain

- Java 25.0.4 (Eclipse Adoptium)
- Maven 3.9.16
- Windows 11 / amd64

## Reproduced local dependencies

Command: `python tools/setup_local_dependencies.py ...`

- Marker: `ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS`
- Five proprietary input JARs: SHA-256 verified before local installation
- Stock MMOSkillTree input SHA-256: `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`
- Owned-effects output SHA-256: `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`
- Patch verification: exactly one changed JAR entry
- `registerIfAbsent`: PASS
- exact-instance `unregister`: PASS
- local Maven installation: PASS
- Python bootstrap contract tests: 3 PASS
- Reactor POMs: no `systemPath` or user-specific absolute paths
- Rift native-asset evidence: committed SHA-256 contract PASS; optional live `Assets.zip` comparison PASS

## Maven reactor

Command:

```bash
mvn clean verify
```

| Module | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| Endless Elite Core | 13 | 0 | 0 | 0 |
| EndlessBook | 22 | 0 | 0 | 0 |
| Hymann | 12 | 0 | 0 | 0 |
| Nachtweber | 70 | 0 | 0 | 0 |
| Seuchenweber | 89 | 0 | 0 | 0 |
| Rift Mage Dungeon | 17 | 0 | 0 | 0 |
| Portal Spawn Snapshot | 4 | 0 | 0 | 0 |
| Mjolnir Safety Patch | 4 | 0 | 0 | 0 |
| **Total** | **231** | **0** | **0** | **0** |

Reactor summary: parent and all eight modules `SUCCESS`.
Marker: `BUILD SUCCESS`.

The Core contracts now also prove exact-instance registry ownership, single-attempt collision rollback, mutate-then-throw registration compensation, reverse cleanup after `Error`, best-effort continuation across all cleanup actions, first-failure preservation, self-suppression safety, and reflective detection of the required MMOSkillTree owned-effects ABI. Hymann, Nachtweber, and Seuchenweber module contracts bind build, manifest disclosure, preflight, rollback, and error propagation to that ABI. Hymann, Nachtweber, and Seuchenweber clear owned fields before each potentially failing cleanup action and continue all remaining cleanup through the shared sequencer. The Seuchenweber configuration contract continues to bind operator documentation and the default profile to the actual schema-4 runtime defaults.

## Repository and distribution gates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=5 java_fqcns=171 unique_resource_paths=87 rift_deployment_allowed=false portal_deployment_allowed=false mjolnir_patch_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Build-verified artifacts:

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| EndlessEliteCore.jar | 26,847 | `709da974328f95ef9dd361ab2b10939fe0190768a20546e77f29fff0637c320c` |
| EndlessBook.jar | 82,940 | `51b61377859108b9e951d38e38840595557d03bdfe2e26ae9ee6bdba17ebb4e1` |
| Hymann.jar | 352,397 | `622fbe5f88224836062a66e683881f82d0b288bbd69f4eaad13fad44527a1d8a` |
| Nachtweber.jar | 163,682 | `b609a9f37f7f35fc1d0f4535dbba82ce8bc3f593c6ee6e2b2dc7c9d448cc22b8` |
| Seuchenweber.jar | 134,826 | `293aae7d041fb7dbd68aafd99921f059a63dc32e93762b80307112a2f8335fe4` |

The distribution manifest explicitly sets:

- `deploymentApproved=false`
- `deploymentPerformed=false`
- `thirdPartyArtifactsBundled=false`

## Warnings and outstanding acceptance

The build reports deprecated Hytale API usage in EndlessBook, Hymann, and Seuchenweber. These warnings are technical debt, not test failures.

This run does **not** prove:

- a combined server cold boot,
- real client/in-game behavior,
- Nachtweber binding/movement/damage/disconnect/two-player behavior,
- Hymann persistence across profile changes or crashes,
- conflict-free global MMOSkillTree writer ordering,
- Rift Mage gameplay/visual acceptance,
- Portal Spawn manifest repair and gameplay/visual acceptance,
- Mjolnir Safety Patch acceptance with Patchly and Starky's Mjolnir 1.6.1,
- live deployment.
