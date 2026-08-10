# Endless Elite test report

Date: 2026-08-10
Tested branch: `fix/late-review-remediation-20260810` (based on `integration/endless-elite-final-20260810`)

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
| Endless Elite Core | 7 | 0 | 0 | 0 |
| EndlessBook | 22 | 0 | 0 | 0 |
| Hymann | 11 | 0 | 0 | 0 |
| Nachtweber | 69 | 0 | 0 | 0 |
| Seuchenweber | 88 | 0 | 0 | 0 |
| Rift Mage Dungeon | 17 | 0 | 0 | 0 |
| Portal Spawn Snapshot | 4 | 0 | 0 | 0 |
| Mjolnir Safety Patch | 4 | 0 | 0 | 0 |
| **Total** | **222** | **0** | **0** | **0** |

Reactor summary: parent and all eight modules `SUCCESS`.
Marker: `BUILD SUCCESS`.

The three added Core tests prove best-effort reverse cleanup after `Error` during normal stop and rollback, including first-failure preservation and suppressed exceptions. The added Seuchenweber contract test binds the operator documentation and default profile to the actual schema-4 runtime defaults.

## Repository and distribution gates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=5 java_fqcns=169 unique_resource_paths=87 rift_deployment_allowed=false portal_deployment_allowed=false mjolnir_patch_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Build-verified artifacts:

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| EndlessEliteCore.jar | 20,735 | `121166f0e7d4e38ce90aae0d0f3ee4c4b608a7760d1d3fedc32e97a7c95301e1` |
| EndlessBook.jar | 82,940 | `4f44fac6eccfb0ad636e7931b0adcab8d7c73ee12a75749e90469433e2c54de8` |
| Hymann.jar | 350,401 | `08dfeabfdd4707078bed84eba616c99c9206195acf789056ae17601a4608ac66` |
| Nachtweber.jar | 165,137 | `6b2400b357849ba0d7d3cba9429bfbe0f989938423775d90a7ed8ac675615faf` |
| Seuchenweber.jar | 132,983 | `5f019d0f24d93e406e27157795f0846bb100a0652f86ff70be587cbc361148a3` |

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
