# Architecture and Conflict Matrix

Status: 2026-08-10
Scope: source integration and shared Java 25/Maven reactor. This is **not** approval for live deployment.

## Integration Model

Endless Elite is a Maven monorepo with separate Hytale runtime artifacts. The separation preserves unambiguous plugin IDs, entry points, resources, and lifecycle owners. A single monolithic JAR was deliberately not created because it would alter the existing plugins' manifests, classloader contracts, and shutdown contracts without validation.

| Module | Role | Runtime ID | Result |
|---|---|---|---|
| Endless Elite Core | shared lifecycle/ownership catalog and global localization | `Shadow:EndlessElite` | integrated, built, and tested |
| EndlessBook | shared UI/hub access | `Shadow:EndlessBook` | integrated, built, and tested |
| Hymann | class/progression | `Shadow:Hymann` | integrated, built, and tested; persistence/reload gates open |
| Nachtweber | elite class | `Shadow:Nachtweber` | integrated, built, and tested; `DisabledByDefault` and in-game gates remain |
| Seuchenweber | elite class | `Shadow:Seuchenweber` | integrated, built, and tested; writer/reload gates open |
| Rift Mage Dungeon | content/import contract | `endless-elite-rift-mage` | source/tests integrated; deployment blocked by its own gate |
| Portal Spawn Snapshot | recovery/prefab contract | incomplete source manifest | integrated byte-for-byte and tested; deployment blocked |
| Mjolnir Safety Patch | Patchly safety contract | `Shadow:MjolnirSafetyPatch` 1.0.0 | integrated byte-for-byte and tested; version-specific deployment gate closed |

## Automatically Verified Uniqueness

`tools/verify_repository.py` checks the following fail-closed:

- exactly five distinct plugin IDs,
- Java source present for every `Main` entry point,
- no duplicate Java FQCNs,
- no checked-in JARs, classes, logs, or `.env` files outside ignored outputs,
- no detected secret assignments,
- Rift Mage remains `deployment_allowed=false`.

Current run: `ENDLESS_ELITE_REPOSITORY_VERIFY_PASS`, 5 plugins, 169 main FQCNs, and 87 unique resource paths; Rift, Portal, and Mjolnir patch gates closed.

## Shared Systems and Unambiguous Owners

| System | Canonical Owner | Integration Decision |
|---|---|---|
| Build/toolchain | root `pom.xml` | Java 25, shared reactor, and centralized local Hytale paths |
| Lifecycle/ownership catalog | Endless Elite Core | fail-closed owner registration and ordered rollback contract; feature plugins require Core in their manifests |
| Global localization | Endless Elite Core | exact, conflict-free union of the former Hymann/Seuchenweber language keys |
| Operator configuration | root `config/examples/` + `docs/CONFIGURATION.md` | shared readable profiles; runtime schemas remain module-specific and tested |
| UI hub | EndlessBook | centralized access and presentation; class modules do not register a competing hub UI |
| Player level, class, prestige | EndlessLeveling (external) | no separate monorepo replacement owner |
| Skills, unlocks, bindings | MMOSkillTree (external) | class modules integrate additively; global writers remain a runtime gate |
| Mob scaling | EndlessEliteMobs (external) | no module in this repository claims general mob scaling |
| Combat state | respective class module | store-/owner-isolated; not moved to a global monorepo singleton |
| Content/dungeon instance | Rift Mage Dungeon + external dungeon runtime | no separate player progression owner |
| Distribution | `tools/collect_distribution.py` | collects only first-party, build-verified artifacts; no third-party JARs |

## Conflicts and Decisions

### 1. MMOSkillTree Binary Contract — Controlled Superset Candidate

- Hymann and Seuchenweber compile against stock MMOSkillTree 1.5.2.
- Nachtweber requires `1.5.2-owned-local` with additive `registerIfAbsent` and exact-instance-bound `unregister`.
- After verification, the reproducible patcher modifies exactly one class and adds only these methods.
- Input SHA-256: `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`.
- Reproduced output SHA-256: `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`.

**Decision:** The patch is the intended additive shared runtime candidate. Combined cold-boot/in-game acceptance with all classes is still outstanding, however; therefore there is no deployment PASS.

### 2. Global MMOSkillTree Configuration Writers — Conflict-Prone

- Seuchenweber writes `abilities.json` as well as German/English messages.
- Hymann writes `abilities.json`, custom skills, and skill-tree configuration.
- Nachtweber has a third installer, but does not invoke it automatically in the current plugin setup.

The IDs are separate, but there is not yet a shared transaction/lock owner. The current integration preserves all sources but activates **no** new parallel writer. Until a controlled writer coordinator exists, cold-boot-only operation and combined runtime acceptance remain open requirements.

### 3. Ability Registration and Reload — Conflict-Prone

- Nachtweber has ownership-safe, instance-exact shutdown.
- Seuchenweber and Hymann register global ability effects without a demonstrated equivalent unregister path.

**Decision:** Source and build integration are approved; hot reload is not. Production operation requires a cold boot or a separate ownership-safe lifecycle slice.

### 4. Player Progression — Hymann as an Additional Owner

Hymann mirrors MMOSkillTree `SkillComponent` state per profile in `profile-progress.properties`. This may be the intended EndlessLeveling profile separation, but it is a second save/rehydration owner. The dormant ability-binding persistence system is not currently registered.

**Decision:** Existing functionality is preserved, but will not be centralized further or additionally activated. Logout, profile changes, and crash recovery remain runtime gates.

### 5. Event/Damage Ordering

There are no duplicate event or system IDs. Hymann does, however, have several damage systems with order sensitivity; Seuchenweber and Nachtweber have their own damage/maintenance systems. The separate plugin modules prevent class collisions but do not prove combined in-game ordering.

### 6. Resources and UI

- No duplicate Java classes.
- First-party asset namespaces are separate.
- As expected, `manifest.json` exists once per separate plugin JAR.
- EndlessBook remains the UI hub; Rift Mage provides a namespaced content/UI import package.

### 7. Rift Mage Dungeon

The Maven JAR is intentionally not a runtime plugin. The actual output is the contract ZIP from `src/main/dlc`. `release-gate.json` blocks deployment until manual gameplay/visual acceptance. The source contract is integrated and tested, but is not emitted as an approved mod JAR.

### 8. Portal Spawn Snapshot

The local Hytale editor export contains two different prefab JSON files, but no complete plugin manifest: `Version` and `Main` are missing, and `IncludesAssetPack` is `false` despite the assets. The manifest and both prefabs match the original source byte-for-byte; the redundant 7.7 MB `.bak` is represented only by size and SHA-256. The contract ZIP is tested but not included in the plugin distribution. `deployment_allowed=false` remains in place until the manifest is repaired and isolated gameplay/visual acceptance is complete.

### 9. Mjolnir Safety Patch

The complete first-party Patchly asset patch 1.0.0 was preserved byte-for-byte from its own backup. It removes only `Mjolnir_Held_Passive_Pulse` and retains `Mjolnir_Charging_Spin_Loop`; Starky's third-party assets or JARs are not included. The contract is explicitly tied to Starky's Mjolnir 1.6.1 and remains `deployment_allowed=false` until the combined cold-boot/gameplay test is complete.

## Modules Not Integrated from Binary Backups

`HyGunsMMOCompat`, `EndlessGuildsPatches`, and `StarterkitChatter` have no complete sources within the examined scope. Blind decompilation would compromise provenance, licensing, tests, and API contracts. They remain documented as hash-based recovery evidence; original backups were not modified or deleted.
