# Nachtweber – Test Report 0.1.0

`NachtweberCoreContractTest` verifies cardinality, owner-isolated entanglement, additive caps, expiry/refresh, bind threshold, owner-specific cleanse, poison immunity, venom tick without replay burst, anti-recursion, reduced boss/elite/PvP control, and finite operator values.

`NachtweberRuntimeAdapterTest` verifies the EndlessLeveling class contract, ordering and idempotence of permission/class registration, fail-closed behavior when registration is rejected, exactly-once unregister, and the Hytale constructor and disabled dependency manifest.

`NachtweberRuntimeWiringTest` verifies identity-based store partitioning even for store objects equal under `equals()`, exactly-once World-Remove/shutdown cleanup, fail-closed bindings after shutdown, maintenance without implicit state creation, the binary-confirmed `TickingSystem<EntityStore>` signature, idempotent system/event registration, entrypoint ownership, and a concrete ledger runtime encapsulated per store. Its maintenance removes expired entanglement and venom without consuming a damage tick, reports the first tick exactly once, and remains fail-closed after `close()`. The Shadow-Swing reconciliation core is likewise encapsulated per store and can no longer be dispatched after runtime close. `NachtweberMultiplayerIsolationPreflightTest` verifies two UUID owners in two store runtimes and owner-selective full cleanup across ledgers, cooldowns, and swing sessions.

`NachtweberMmoInstallerTest` verifies the skill ID, seven unlocks, active/passive cardinality, additive ability installation, foreign-key preservation, one-time backup, atomic-idempotent repetition, and DE/EN localization contracts.

`BlackThreadServiceTest` verifies successful stack buildup through bind, server-side authorization, range, invalid targets, owner-specific cooldown without state mutation, saturated time addition without `long` overflow, the MMOSkillTree Effect/ParamSpec contract, fail-closed null caster, and seconds/blocks/count parity of the bundled configuration.

`HuntingCocoonServiceTest` verifies owner-specific atomic entanglement consumption, preservation of foreign owner states, owner-isolated venom, authority, range, cooldown, immunity, DoT/reflection protection, insufficient stacks without mutation, saturated venom/cooldown times, the MMOSkillTree Effect/ParamSpec contract, fail-closed null caster, and Schema-7 config parity.

`VenomTickServiceTest` verifies owner-isolated tick drain, at most one catch-up tick without replay burst, Sorcery scaling, damage cap, internal cause marking, intercepted runtime port errors, the exact installed EndlessLeveling-11.6.1 ABI, and fail-closed factory behavior before initialization of the Hytale `DamageCause` asset store. The bundled Schema-7 configuration uses seconds, stack damage, explicit Sorcery points up to doubling, maximum damage, and exactly one maximum overdue tick.

`PassiveVenomServiceTest` verifies `TOXIC_GLANDS` and `HUNTING_INSTINCT`: confirmed direct hit, immunity, DoT/reflection recursion, owner+target cooldown, own-entanglement-only bonus, preservation of foreign states, owner-specific cooldown cleanup, and Schema-7 config parity.

`NachtweberCompletionTest` verifies `SHADOW_SWING` anchor, impulse, and cooldown rules, `WALL_HUNTER` wall-contact/input gates, the bounded nearest-visible-hostile contract of `DANGER_SENSE`, exactly three complete active Effects, and the final units of the Schema-7 configuration. `ShadowSwingReconciliationControllerTest` additionally verifies owner-isolated sessions, expiry, anchor/tether limits, vector velocity correction limit, reattach rate limit, stale steps, NaN fail-closed behavior, Cancel/Cleanse/Close, and explicit config units. The runtime adapters compile against the binary-verified native Hytale components `Velocity`, `TransformComponent`, `HeadRotation`, `CollisionResultComponent`, `MovementStatesComponent`, and `BlockRaystep`.

The subsequent Hytale-0.5.7 bytecode audit narrows the movement boundary: `SHADOW_SWING` validates anchors and mutates server-side `Velocity`, but Player reconciliation and persistent trajectory authority are not demonstrated without a boot/in-game test. The audit regression additionally verifies the Danger-Sense preflight before the spatial scan, hard limits of 16 blocks/64 retained candidates, and fail-closed `WALL_HUNTER` behavior with pending collision results. `MovementStates.jumping` is advisory input only; horizontal wall contact comes from the completed server-side collision result.

## Final completion evidence

- Java-25 `mvn clean verify`: **BUILD SUCCESS**
- Tests: **63**, Failures: **0**, Errors: **0**, Skipped: **0**
- Production sources: **66**; test classes: **11**
- Independent fresh verifier: `NACHTWEBER_COMPLETION_ARTIFACT_PASS`
- JAR/config/manifest gates: Schema 7, 3 active + 4 passive abilities, no missing classes, `DisabledByDefault: true`
- Native ABI gates: `Velocity.addVelocity`, `Velocity.addInstruction`, `ChangeVelocityType.Add`, Player/generic instruction systems, CollisionResult, MovementStates, and collision normals present
- Lifecycle gate: the plugin-local store maintenance system, `StartWorldEvent` binding, and global World-Remove cleanup are registered ownership-safely; MMOSkillTree still provides `register(...)`, but no `unregister`, so gameplay Effects there remain `liveWired=false`.
- SHA-256 of the current reproducible `clean verify` artifact: `ffc4c9f981d9b478f7791108a4dd2cbcb8dae6b71d04b2b15ebf8f068aae4732`
- Temporary verifier deleted after PASS; no `hermes-verify-*.py` remnants.

Manual client, movement, multiplayer, active ability/gameplay execution, and positive server damage tests remain untested. Real store maintenance tick execution is now separately demonstrated in isolation. The artifact must still not be deployed.

## Isolated server boot – 2026-08-09

An approved loopback-only boot with the hash-verified Nachtweber JAR and dependency-complete baseline (`EndlessLeveling 11.6.1`, `MMOSkillTree 1.5.2`, `ZiggfreedCommon 1.3.0`, `EEM 3.10.0`) produced `NACHTWEBER_ISOLATED_SERVER_BOOT_PASS`:

The runtime boot used the then-current artifact SHA-256 `7bcbf1902c5b40e7f33f220548ccca009467aee443c498eed2d0638e86a537cf`. A later canonical rebuild of the same sources produced the new JAR hash stated above due to package timestamps; no additional boot PASS is claimed for this new byte hash.

- `Shadow:Nachtweber adapter initialized: class elite_nightweaver; gameplay systems remain fail-closed`
- `Enabled plugin Shadow:Nachtweber`
- Listener on `127.0.0.1:5523`
- `Hytale Server Booted! [Multiplayer]`
- no Nachtweber warning/error line and no exception, linkage, setup, enable, or asset validation failure
- `Shadow:Nachtweber adapter shut down`, plugin shutdown, and `Shutdown completed!`
- Port and Java process released afterwards

The first minimal boot without EEM showed an EndlessLeveling NPC reference to `EEM_Weapon_Spear_ALPHA01`; EEM was therefore added as the only differential variable. Five nonfatal HytaleGenerator `SERR Reallocate` lines remained without an exception and are not attributed as Nachtweber errors. Complete evidence: `.hermes/runtime/nightweaver-isolated-boot-20260809/evidence/` (boot log SHA-256 `cb0dbafc5f992e3b51676075f0cca6857482e1f43a140a9b839bb0fc99e230a0`).

The boot demonstrates plugin/class lifecycle and dependency resolution only. MMOSkillTree Effect wiring, ECS/passive scheduler, positive damage path, Shadow-Swing reconciliation, two-player isolation, and client/in-game behavior remain explicitly unproven; no deployment PASS.

## Reproducible artifact – 2026-08-09

A real RED double build of the same sources initially produced two different JAR hashes (`01b5c0e6…` and `95ead1bd…`). The new contract test `mavenBuildPinsAReproducibleOutputTimestamp` was red because the pin was missing. After `project.build.outputTimestamp=2026-08-09T00:00:00Z`, the test is GREEN.

Two subsequent independent Java-25 `clean verify` builds each passed with 40 tests and produced byte-identical output:

`007fe5d2db4d7d889db9476f8c6ac3660c769f2bd4c9cb176c81d8057a0db972`

This makes new builds from the same sources package-deterministic. The older isolated runtime boot remains historical evidence for the then-current artifact `7bcbf190…`; the current reproducible hash was then separately runtime-verified.

## Reproducible hash – isolated server boot – 2026-08-09

The reproducible artifact SHA-256 `007fe5d2db4d7d889db9476f8c6ac3660c769f2bd4c9cb176c81d8057a0db972` was booted in a fresh dependency-complete test root with EndlessLeveling 11.6.1, MMOSkillTree 1.5.2, ZiggfreedCommon 1.3.0, and EEM 3.10.0 on `127.0.0.1:5523`.

- Plugin discovered and `elite_nightweaver` adapter initialized
- `gameplay systems remain fail-closed` explicitly logged
- `Shadow:Nachtweber` enabled
- Loopback listener and `Hytale Server Booted!` confirmed
- 0 `NoClassDefFoundError`, `NoSuchMethodError`, `LinkageError`, exceptions, setup/enable, and asset validation errors
- four HytaleGenerator `SERR Reallocate` lines not attributed to the candidate
- adapter and plugin shut down cleanly; `Shutdown completed!`
- Port 5523 and Java/Hytale process free afterwards
- isolated JARs, cache, world, and mod data removed; only evidence retained

Status: `NACHTWEBER_REPRODUCIBLE_ARTIFACT_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-reproducible-artifact-boot-20260809/evidence/`; complete boot log SHA-256 `f8d2d366da6cbf9bfa923e2208894cb612aacbf616ccc6873ac8c61234b556fe`.

This evidence still covers no client/in-game behavior, no live MMOSkillTree Effect registration, no scheduler/positive damage path, no two-player isolation, and no Shadow-Swing reconciliation. No deployment PASS.

## Store-lifecycle ECS wiring – isolated server boot – 2026-08-09

The ownership-safe runtime infrastructure slice was implemented against the installed Hytale-0.5.7 ABI: identity-based store-state registry, idempotent coordinator, plugin-local `TickingSystem<EntityStore>`, global `RemoveWorldEvent` cleanup, and plugin-wide shutdown cleanup. `ComponentRegistryProxy` has a plugin-local unregister contract for this purpose; the MMOSkillTree `ActiveAbilityService` still does not and was not changed.

RED→GREEN evidence covered missing store registry, shutdown cleanup, state-free maintenance lookups, coordinator lifecycle, concrete store tick signature, registration orchestration, and entrypoint ownership. Java-25 `mvn clean verify` then passed with 47 tests. Two independent builds produced byte-identical output:

`6658e8e24971ff413f56b865efa520d3b181d057e57019c3ad7752316113bc82`

This exact artifact was booted dependency-complete on `127.0.0.1:5523`. The fresh root initially respected `DisabledByDefault: true`; only the isolated config then received `Mods[Shadow:Nachtweber].Enabled=true`.

- exact marker: `adapter initialized: class elite_nightweaver; store lifecycle ECS wired; gameplay effects remain fail-closed`
- Plugin enabled and `Hytale Server Booted!` reached
- 0 class, linkage, exception, setup, enable, asset validation, or runtime registration errors
- adapter, plugin, and server shut down in a controlled manner
- Port 5523 and Java/Hytale process free
- isolated JARs, config, cache, world, and mod data removed; only evidence retained
- live mod folder and live configuration unchanged

Status: `NACHTWEBER_STORE_LIFECYCLE_ECS_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-live-runtime-wiring-boot-20260809/evidence/`; boot log SHA-256 `07e24969a02e7aa1ee6019ed66818c1b090105647b076514d0e42baee6933047`.

What is demonstrated is successful acceptance of the store-level system and World-Remove handler by the plugin-local Hytale registries. Tick execution with bound gameplay state, live MMOSkillTree Effect registration, positive damage path, Shadow-Swing reconciliation, two-player behavior, and client/in-game behavior are not claimed. No deployment PASS.

## Store-bound gameplay state and real maintenance tick – 2026-08-09

`NachtweberLedgerStoreRuntime` encapsulates its own `EntanglementLedger` and `VenomLedger` instances for each Hytale `Store<EntityStore>`. The maintenance path removes expired state only; it does not consume a due venom damage tick and does not call damage, proc, Effect, or MMOSkillTree paths. World-Remove and plugin shutdown still clear and close the state at most once.

The first isolated boot was real runtime-RED evidence: `AddWorldEvent` fired before initialization of `EntityStore.getStore()`, the store was `null`, Nachtweber produced a `NullPointerException`, and the tick marker did not appear. The RED evidence was retained. After ABI inspection, the binding was moved to `StartWorldEvent` and additionally guarded null-fail-closed.

Java-25 `mvn clean verify` then passed with **48 tests**, 0 Failures, 0 Errors, and 0 Skips. Two independent builds produced byte-identical output:

`1105ac1c56b29fdbdabc8c2b58f8d37317bd7af49b16d9eb3a48b66d955060b9`

This exact hash was booted dependency-complete on `127.0.0.1:5523`:

- Nachtweber discovered, initialized, and enabled
- `Hytale Server Booted!` reached
- real marker `Shadow:Nachtweber first bound-store maintenance tick observed at … ms`
- 0 `NullPointerException`, exceptions, class, linkage, setup, enable, asset validation, or runtime registration errors
- six known HytaleGenerator `[SERR] Reallocate` lines not attributed to the candidate
- adapter, plugin, and server shut down cleanly; port and processes free
- isolated config, JAR, cache, world, and mod data removed; only RED/GREEN evidence retained

Status: `NACHTWEBER_BOUND_STORE_MAINTENANCE_TICK_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-bound-store-tick-boot-20260809/evidence/`; GREEN boot log SHA-256 `373c415e15cbb2eead5dc8d507679a6e642b38dd029c86f2c60ebdb54b07dba0`.

Positive `DamageCause` execution, active ability/MMOSkillTree Effect registration, Shadow-Swing reconciliation, two-player isolation, and client/in-game behavior remain unproven. No deployment PASS.

## Store-bound gameplay facade – 2026-08-09

The bound `NachtweberLedgerStoreRuntime` now provides only operational, post-close-locked access for `BLACK_THREAD`, `HUNTING_COCOON`, passive venom application, and `VENOM_TICK`. Raw ledgers or service objects are not exposed. Black Thread, Cocoon, Passive, and Venom Tick share exactly the same Entanglement/Venom ledgers and store-local cooldown maps within a store; an identity-distinct store sees none of this state.

The coordinator and plugin wiring expose this facade only for an already-bound exact store. Unknown, removed, null, or post-shutdown stores remain fail-closed. The EndlessLeveling Sorcery provider is injected as a dormant dependency; there is still no automatic damage, Effect, or ability call.

RED→GREEN covered the missing cross-service state-sharing facade, store-specific coordinator dispatch, productive power-provider injection, and narrow wiring dispatch. Java-25 `mvn clean verify` passed with **52 tests**, 0 Failures, 0 Errors, and 0 Skips; `NachtweberRuntimeWiringTest` contains **12** green tests. Two independent builds produced byte-identical output:

`8efde02bf9a7eb4b42b9de9a0cd2ff98e2ffe5f0f30160ec5aa5a1af0c6b8ec3`

This exact hash was booted dependency-complete on `127.0.0.1:5523`: discovery, initialization, plugin enable, store maintenance tick, `Hytale Server Booted!`, adapter/plugin shutdown, and `Shutdown completed!` are demonstrated; all strict error counters are 0. Four known HytaleGenerator `[SERR] Reallocate` lines are not attributed to the candidate. Test state was removed; only evidence was retained.

Status: `NACHTWEBER_STORE_GAMEPLAY_FACADE_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-store-gameplay-facade-boot-20260809/evidence/`; boot log SHA-256 `8d617e2d257644efb672d47936e7585ef65bb3dc7fd52c7f4c82fa53e213e6df`.

The boot deliberately triggered no ability or damage call and changed no MMOSkillTree registry. No client/in-game and no deployment PASS.

## DamageCause asset store readiness – 2026-08-09

Local `javap` demonstrates `DamageCause.getAssetStore()`, `getAssetMap()`, and `IndexedLookupTableAssetMap.getIndexOrDefault/getAsset`. EndlessLeveling 11.6.1 internally creates its Ability DoT with Hytale's physical DamageCause; the source name `Poison` is not a DamageCause asset key. `Assets.zip` canonically contains `Server/Entity/Damage/Physical.json`, so the read-only probe checks the non-deprecated key `Physical` rather than the obsolete static alias `DamageCause.PHYSICAL`.

Before server boot, the probe reports fail-closed `UNAVAILABLE`. An `UNAVAILABLE` result does not consume the one-time marker; the first consistent `READY` is observed exactly once. Null stores do not trigger a probe. The runtime check verifies only `AssetStore → identical AssetMap → Physical index → Asset with ID Physical`; it constructs and emits no damage.

Java-25 `mvn clean verify`: **55 tests**, 0 Failures, 0 Errors, 0 Skips, and no new deprecation warning. Byte-identical double build: `e003fb8ca0ba7acc553dea0b54a818d4a6577be000e33c3471b8d6b911462fff`.

This exact hash was booted in isolation on `127.0.0.1:5523`. The marker `DamageCause AssetStore ready: cause=Physical index=2; damage execution remains disabled` appeared exactly once. Discovery, plugin enable, store maintenance tick, server boot, and controlled shutdown are demonstrated; all strict error counters are 0. Six known HytaleGenerator `[SERR] Reallocate` lines are not attributed to the candidate. Cleanup gate PASS; only evidence was retained.

Status: `NACHTWEBER_DAMAGECAUSE_READINESS_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-damagecause-readiness-boot-20260809/evidence/`; boot log SHA-256 `2ef4b908304aa81dd206b0f2fc5f8fdfce86a8df965941ef2afc05a73b43d06b`.

This is explicitly **not a positive damage PASS**: there was no owner/target entity pair, no `Damage` object, no `DamageSystems.executeDamage` call, and no client/in-game execution.

## Shadow-Swing reconciliation core – 2026-08-09

`ShadowSwingReconciliationController` is a pure state machine to be fed server-side, without Hytale entity or Velocity calls. Each store-bound `NachtweberLedgerStoreRuntime` has its own instance. Start/Step/Cancel are offered only through the narrow gameplay facade and no longer produce results after store remove/close.

The following apply per owner: maximum session duration of 1.5 seconds, 18 blocks start anchor, 22 blocks tether break, 1.25 blocks release distance, 14 blocks/s pull target velocity, maximum 3 blocks/s correction per step, and 0.25 seconds reattach interval. Stale timestamps do not mutate; non-finite server-side position/velocity ends only the affected session fail-closed. Cancel retains the reattach limit, explicit owner cleanup removes both states, and Close is idempotent and final.

The bundled config was additively migrated to Schema 7 and names every value in seconds, blocks, or blocks/s. No live configurations were changed. Java-25 `mvn clean verify`: **62 tests**, 0 Failures, 0 Errors, 0 Skips; Controller 6 tests, Wiring 14 tests. Byte-identical double build: `64bd21779b7780a090eb40d508e31e82023de545113adab33083c621cfc45dca`.

The first isolated boot was runtime-RED: the new root respected `DisabledByDefault: true` and skipped Nachtweber. After local ABI confirmation of the config contract, only `Mods[Shadow:Nachtweber].Enabled=true` was set in the isolated root. The corrected boot of the same hash reached discovery, plugin enable, DamageCause readiness, store maintenance tick, and `Hytale Server Booted!`; all strict error counters are 0. Shadow Swing, Velocity, and damage were not executed. Six known HytaleGenerator `[SERR] Reallocate` lines are not attributed to the candidate.

The server reported adapter/plugin shutdown and `Shutdown completed!`. Afterwards, an unrelated JVM thread remained hung; PID 22000 was terminated selectively only after explicit user approval. Port and Java/Hytale processes were then free. A first cleanup attempt failed on a Windows long path in `.cache`; its prematurely printed marker was discarded. The long-path retry and the repeated strict gate finally confirmed exactly one top-level entry (`evidence`).

Status: `NACHTWEBER_SHADOW_SWING_RECONCILIATION_CORE_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-shadow-swing-reconciliation-core-boot-20260809/evidence/`; boot log SHA-256 `ee155abe8092ac4f073850f69bb31a2325cb5ac28ed9262e9cde7c887e0a58b6`.

ECS tick/movement wiring, actual swing start, native Velocity mutation, client reconciliation, in-game behavior, and multiplayer are not demonstrated. No deployment PASS.

## Two-owner/two-store isolation preflight – 2026-08-09

The store-bound gameplay facade now provides `cleanupOwner(UUID)` as a fail-closed disconnect/class-change primitive. Within exactly one `NachtweberLedgerStoreRuntime`, it removes the stated owner's entanglement, venom, Black-Thread/Hunting-Cocoon/Passive cooldowns, and Shadow-Swing session and reattach time. Null or closed runtimes are rejected. Other owners' state and identity-distinct stores remain unchanged.

The end-to-end RED test initially failed at test compile because `cleanupOwner(UUID)` was absent. Minimal-GREEN added only owner-selective ledger/cooldown primitives and the facade dispatch. The test demonstrates Owner A and B in the first store and Owner A in a second store in parallel: after cleanup of A in Store 1, its state and cooldowns are reset while Owner B and Store 2 continue unchanged.

Java-25 `mvn clean verify`: **63 tests**, 0 Failures, 0 Errors, 0 Skips. Byte-identical double build: `a5ce7943dc7f09222c7a7deaf3fb11b60c753871843a8b03531f0feb097c5d15`.

This exact hash was booted dependency-complete on `127.0.0.1:5523`. Discovery, plugin enable, DamageCause readiness, store maintenance tick, `Hytale Server Booted!`, Nachtweber/plugin shutdown, and `Shutdown completed!` are demonstrated; all strict error counters are 0. The boot created no player, did not call `cleanupOwner`, and performed neither ability, damage, nor movement. Five known HytaleGenerator `[SERR] Reallocate` lines are not attributed to the candidate.

After full server shutdown, an unrelated JVM thread again remained active; PID 14552 was terminated after explicit user approval. The later visible `HytaleClient.exe` PID 9992 did not belong to the test server and was not changed. Port 5523 and the server PID are free; cleanup gate PASS, with only `evidence` retained.

Status: `NACHTWEBER_MULTIPLAYER_ISOLATION_PREFLIGHT_BOOT_PASS`. Evidence: `.hermes/runtime/nightweber-multiplayer-isolation-preflight-boot-20260809/evidence/`; boot log SHA-256 `5c4055b93c96cd030d8d4545025bd0bfbb5eb4fad7d7f285ad95f418f951ac3c`.

This is explicitly **not a real two-player/multiplayer PASS**: the two owners are deterministic UUIDs in the unit preflight, not connected Hytale players. Reconnect/disconnect event wiring, real entities, client/in-game, and deployment remain open.

## Owner cleanup on PlayerDisconnectEvent – 2026-08-09

The local Hytale-0.5.7 bytecode audit demonstrates the sequence in `Universe.removePlayer(PlayerRef)`: `PlayerDisconnectEvent` is dispatched globally before the server reads `PlayerRef.getReference()` and before the entity is removed from the store. The event provides the authoritative `PlayerRef`; `getUuid()` and, while still present, `getReference().getStore()` are read from it. Null PlayerRef, UUID, reference, or store remain fail-closed.

`NachtweberRuntimeWiring.RegistrationPort` registers exactly one global disconnect handler through the plugin-local EventRegistry. The handler dispatches `cleanupOwner(UUID)` only into the identity-exact bound store. Unknown or already removed stores do not create state. Event registration remains owned by the plugin lifecycle; no foreign MMOSkillTree registry was changed.

The RED test initially observed 0 instead of 1 disconnect registrations. Minimal-GREEN added only RegistrationPort, wiring dispatch, and the Hytale adapter. Null contexts are tested; the actual two-owner/two-store cleanup remains demonstrated by `NachtweberMultiplayerIsolationPreflightTest`. Java-25 `mvn clean verify`: **63 tests**, 0 Failures, 0 Errors, 0 Skips. Byte-identical double build: `ffc4c9f981d9b478f7791108a4dd2cbcb8dae6b71d04b2b15ebf8f068aae4732`.

This exact hash was booted dependency-complete on `127.0.0.1:5523`. The new `PlayerDisconnectEvent`/`PlayerRef.getReference()` linkage was accepted by plugin setup; discovery, enable, DamageCause readiness, maintenance tick, server boot, and full shutdown are demonstrated. All strict error counters are 0, with five known generator SERRs not attributed to the candidate. No player connected; therefore, no real disconnect or owner cleanup was triggered. No damage, movement, client control, or in-game behavior.

After `Shutdown completed!`, the known unrelated thread remained hung; PID 220 was terminated after explicit user approval. The separately running HytaleClient PID 9992 during boot was not controlled and had already ended itself by the final cleanup gate. Port, server PID, and runtime state are clean; only `evidence` was retained.

Status: `NACHTWEBER_OWNER_CLEANUP_EVENT_WIRING_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-owner-cleanup-event-wiring-boot-20260809/evidence/`; boot log SHA-256 `4e64135a68eb8d5f6fec3073f7f9414449b7950abac6a99250a786c5cb680a4d`.

A real player disconnect remains untested without client/in-game approval. No real multiplayer or deployment PASS.

### Class-change ABI boundary

EndlessLeveling 11.6.1 has a direct class-change callback through `AbilityBridge.Hooks.onClassChanged(UUID)`. It cannot be used ownership-safely for Nachtweber: `AbilityBridge` manages exactly one global hook and provides `register(Hooks)`, but no matching `unregister(Hooks)`. Registration could overwrite a foreign owner or persist after plugin shutdown. `ProfileSwitchedEvent` and `BuildAppliedEvent` do have symmetric add/remove listeners, but they are not demonstrated class-change events; moreover, they carry only UUID/profile or build data and no store. Therefore, no class-change wiring was added. This boundary is a confirmed ABI blocker, not a failed test.

## Shadow-Swing ECS/Velocity ABI preflight – 2026-08-10

The preflight was strictly read-only and used Java-25 `javap` and JAR bytecode search against `HytaleServer.jar` SHA-256 `43d9bcff1dd31574577dbfc82147718dbe2ac16c19000071f965b521ba808cdc` and `MMOSkillTree-1.5.2.jar` SHA-256 `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`.

Confirmed are `PlayerRef.getReference()`, `Ref.getStore()`, `Store.getComponent(...)`, `TransformComponent.getPosition()`, `Velocity.getComponentType()`, `Velocity.addVelocity(...)`, `Velocity.addInstruction(Vector3d, VelocityConfig, ChangeVelocityType)`, `ChangeVelocityType.Add`, `World.execute(Runnable)`, and the ECS tick signatures with `ArchetypeChunk`, `Store`, and `CommandBuffer`. `GenericVelocityInstructionSystem` consumes Add instructions and then calls `Velocity.addVelocity(...)`; for the same instruction, `PlayerVelocityInstructionSystem` sends `ChangeVelocity(Add)` to the Player and clears the list. Thus, `addInstruction(..., Add)` is the demonstrated server/client-synchronized impulse path.

MMOSkillTree's hotbar filter and Ability command call Ability activation within `World.execute(...)`. In contrast, `ActiveAbilityService.tryActivate(...)` calls `AbilityEffect.execute(...)` without its own `Store.assertThread()` or `Store.isInThread()` guard. The existing unregistered `ShadowSwingAbility` prototype mutates directly through `Velocity.addVelocity(...)`; it therefore bypasses instruction/packet reconciliation and does not yet use the store-local `ShadowSwingReconciliationController`.

Result: **ABI primitives READY, productive movement wiring NO-GO**. Before activation, the pure reconciliation core, store/Ref identity, World thread, component readiness, `addInstruction(..., Add)`, ECS system ordering, and an ownership-safe Effect registration contract must be implemented together and then tested with client/in-game approval. No entity was created, no component read or mutated, no MMOSkillTree Effect registered, and no client started or controlled. This is not a movement, reconciliation, or deployment PASS.

## Shadow-Swing core→ECS instruction wiring – 2026-08-10

RED→GREEN added `ShadowSwingMovementSystem` as a plugin-local `EntityTickingSystem`. The query contract includes PlayerRef, Transform, and Velocity. The tick discards null/foreign stores, non-store threads, invalid Refs, missing components, and non-finite position/velocity. Only an accepted, bounded step of the store-local `ShadowSwingReconciliationController` produces `Velocity.addInstruction(correction, null, ChangeVelocityType.Add)`; direct `velocity.addVelocity(...)` mutation is excluded. `SystemDependency(Order.BEFORE, PlayerVelocityInstructionSystem.class)` anchors the producer before the Player packet consumer.

Java-25 `mvn clean verify` passed with **64 tests**, 0 Failures, 0 Errors, and 0 Skips. After the Schema-8 migration, two independent builds produced byte-identical output `b9cc1b718538c6329e4abd9f7ab082bd061b28b0033c168a19decfa0c25ac935`.

This exact final Schema-8 hash was booted dependency-complete and loopback-only on `127.0.0.1:5523`. Nachtweber enable, DamageCause readiness (`Physical`, Index 2), first store-bound maintenance tick, `Hytale Server Booted!`, plugin-local shutdown, and `Shutdown completed!` are demonstrated. All strict linkage/setup/enable error counters are 0. Port and server JVM were free afterwards; no hard process kill. Status: `NACHTWEBER_SCHEMA8_MOVEMENT_BOOT_PASS`; boot log SHA-256 `27240692ee91c7f86e1cf31adeec952b2c1a899a2137edf6976595e42ebc9e2a`.

This boot connected no player and started no core session; therefore, neither movement nor damage was executed. The ECS system is registered and inert. Live MMOSkillTree Effect registration remains separate because there is no public ownership-safe unregister contract. No client/in-game or deployment PASS.

## Owned-Effects, damage, and Schema-9 PASS – 2026-08-10

- MMOSkillTree patcher RED against unmodified JAR: expected rejection (`changed entries: []`).
- Patcher GREEN: `registerIfAbsent` and exact-instance `unregister`; exactly one additional changed class; reproducible SHA-256 `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`.
- Three live registered Effects dispatch only through the identity-bound gameplay runtime. Shadow Swing starts the core, Cocoon provides 2.0 seconds of immobilization and binds the owner Ref.
- `VenomDamageSystem` queries real NPC targets and may execute the EndlessLeveling DoT only with valid owner/target Ref in the same store. Disconnect, WorldRemoval, and shutdown clear bindings.
- Java 25 `mvn clean verify`: **69 tests**, 0 Failures, 0 Errors, 0 Skips.
- Nachtweber double build byte-identical; SHA-256 `53257e2d9b4de121ef3ea95e841d1bcd8b9a43f0372eb2d11b43eaf2da0c7683`.
- Isolated dependency-complete boot with Hytale 0.5.7, EndlessLeveling, EEM 3.10.1, ZiggfreedCommon 1.3.0, patched MMOSkillTree, and Nachtweber on `127.0.0.1:5523`: `NACHTWEBER_OWNED_EFFECTS_SCHEMA9_BOOT_PASS`.
- Confirmed: Plugin enabled, three owned Effects registered, `Physical` Index 2 ready, Server booted, first Maintenance tick, Nachtweber shutdown before MMOSkillTree shutdown, no Nachtweber SEVERE, no `NoSuchMethod`, no registry collision.
- Boot log SHA-256 `1ef7382cbff0518928d7ccc095b9e6dcc8471457be3e5c6fecb1e6081c26e47b`; evidence `.hermes/runtime/nightweaver-owned-effects-schema9-boot-20260810/evidence/`.

Not executed: client, real skill activation, real movement/damage execution, real disconnect, two-player isolation, and deployment. The first boot attempt without `ZiggfreedCommon` was a dependency-incomplete FAIL; the fresh complete repeat run passed. It is not reinterpreted as a product error.
