# Nachtweber – Test Report 0.1.0

`NachtweberCoreContractTest` checks cardinality, owner-isolated entanglement, additive caps, expiration/refresh, tether threshold, owner-specific cleanse, poison immunity, venom tick without replay burst, anti-recursion, reduced boss/elite/PvP control and finite operator values.

`NachtweberRuntimeAdapterTest` checks the EndlessLeveling class contract, order and idempotence of permission/class registration, fail-closed behavior when registration is rejected, exactly-once unregistration as well as Hytale constructor and disabled dependency manifest.

`NachtweberRuntimeWiringTest` checks identity-based store partitioning also for `equals()` identical store objects, exactly one-time world remove/shutdown cleanup, fail-closed bindings after shutdown, maintenance without implicit state generation, the binary confirmed `TickingSystem<EntityStore>` signature, idempotent system/event registration, Entrypoint ownership and a specific ledger runtime encapsulated per store. Their maintenance removes expired entanglement and poison without consuming a damage tick, reports the first tick exactly once and remains fail-closed after `close()`. The Shadow Swing Reconciliation Core is also encapsulated per store and can no longer be dispatched after runtime close. `NachtweberMultiplayerIsolationPreflightTest` checks two UUID owners in two store runtimes and owner-selective overall cleanup via ledgers, cooldowns and swing sessions.

`NachtweberMmoInstallerTest` checks skill ID, seven unlocks, active/passive cardinality, additive ability installation, foreign key preservation, one-time backup, atomic-idempotent retry and DE/EN localization contracts.

`BlackThreadServiceTest` checks successful stacking up to the tether threshold, server-side authorization, range, invalid targets, owner-specific cooldown without state mutation, saturated time addition without `long` overflow, MMOSkillTree effect/ParamSpec contract, fail-closed null caster, and the seconds/blocks/count parity of the bundled configuration.

`HuntingCocoonServiceTest` checks owner-specific atomic entanglement consumption, foreign owner state preservation, owner-isolated venom, authority, range, cooldown, immunity, DoT/Reflection protection, insufficient stacks without mutation, saturated Venom/cooldown times, MMOSkillTree effect/ParamSpec contract, fail-closed null caster and Schema 7 configuration parity.

`VenomTickServiceTest` checks owner-isolated tick drain, at most one caught-up tick without replay burst, sorcery scaling, damage cap, internal cause identification, intercepted runtime port errors, exact installed EndlessLeveling 11.6.1 ABI and fail-closed factory behavior before initialization of the Hytale-`DamageCause`-AssetStores. The bundled Schema 7 configuration uses seconds, stacking damage, explicit Sorcery points up to doubling, maximum damage, and exactly one maximum overdue tick.

`PassiveVenomServiceTest` checks `TOXIC_GLANDS` and `HUNTING_INSTINCT`: confirmed direct hit, immunity, DoT/reflection recursion, owner+target cooldown, own-entanglement-only bonus, foreign condition preservation, owner-specific cooldown cleanup and Schema 7 config parity.

`NachtweberCompletionTest` checks `SHADOW_SWING` anchor, pulse and cooldown rules, `WALL_HUNTER` wall contact/input gates, the limited nearest visible hostile contract of `DANGER_SENSE`, exactly three full active effects and the final units of the Schema 7 configuration. `ShadowSwingReconciliationControllerTest` additionally checks owner-isolated sessions, expiration, anchor/tether limits, vector velocity correction limit, reattach rate limit, stale steps, NaN fail-closed, cancel/cleanse/close and explicit config units. The runtime adapters compile against the binary-tested native Hytale components `Velocity`, `TransformComponent`, `HeadRotation`, `CollisionResultComponent`, `MovementStatesComponent` and `BlockRaystep`.

The downstream Hytale 0.5.7 bytecode audit clarifies the movement limit: `SHADOW_SWING` validates anchors and mutates server-side `Velocity`, but player reconciliation and persistent trajectory authority are not proven without boot/in-game testing. The audit regression additionally checks danger-sense preflight before the spatial scan, hard limits of 16 blocks/64 retained candidates and fail-closed `WALL_HUNTER` for pending collision results. `MovementStates.jumping` is only considered an advisory input; horizontal wall contact comes from the final server-side collision result.

## Final proof of completion

- Java-25 `mvn clean verify`: **BUILD SUCCESS**
- Tests: **63**, Failures: **0**, Errors: **0**, Skipped: **0**
- Production sources: **66**; Test classes: **11**
- Independent fresh verifier: `NACHTWEBER_COMPLETION_ARTIFACT_PASS`
- JAR/Config/Manifest Gates: Schema 7, 3 active + 4 passive abilities, no missing classes, `DisabledByDefault: true`
- Native ABI gates: `Velocity.addVelocity`, `Velocity.addInstruction`, `ChangeVelocityType.Add`, player/generic instruction systems, CollisionResult, MovementStates and collision normals available
- Lifecycle gate: plugin-local store maintenance system, `StartWorldEvent` binding and global world remove cleanup are registered in an ownership-safe manner; MMOSkillTree still offers `register(...)`, but no `unregister`, so gameplay effects remain there `liveWired=false`.
- SHA-256 of the current `clean verify` reproducible artifact: `ffc4c9f981d9b478f7791108a4dd2cbcb8dae6b71d04b2b15ebf8f068aae4732`
- Temporary verifier deleted after PASS; no `hermes-verify-*.py` residue.

Manual client, movement, multiplayer, active skill/gameplay execution, and positive server damage tests remain untested. The real store maintenance tick execution is now separately isolated. The artifact must still not be deployed.

## Isolated Server Boot - 2026-08-09

Released loopback-only boot with the hashed Nachtweber JAR and dependency-complete baseline (`EndlessLeveling 11.6.1`, `MMOSkillTree 1.5.2`, `ZiggfreedCommon 1.3.0`, `EEM 3.10.0`) resulted in `NACHTWEBER_ISOLATED_SERVER_BOOT_PASS`:

The runtime boot used the then artifact SHA-256 `7bcbf1902c5b40e7f33f220548ccca009467aee443c498eed2d0638e86a537cf`. A later canonical rebuild of the same sources produced the new JAR hash mentioned above because of packet timestamps; no additional boot PASS is claimed for this new bytehash.

- `Shadow:Nachtweber adapter initialized: class elite_nightweaver; gameplay systems remain fail-closed`
- `Enabled plugin Shadow:Nachtweber`
- Listener on `127.0.0.1:5523`
- `Hytale Server Booted! [Multiplayer]`
- no Nachtweber warning/error line and no exception, linkage, setup, enable or asset validation failure
- `Shadow:Nachtweber adapter shut down`, plugin shutdown and `Shutdown completed!`
- Port and Java process then released

The first minimal boot without EEM showed an EndlessLeveling NPC reference to `EEM_Weapon_Spear_ALPHA01`; EEM was therefore added as the only differential variable. Five non-fatal HytaleGenerator-`SERR Reallocate` lines persisted without an exception and are not attributed as night weaver errors. Full evidence: `.hermes/runtime/nightweaver-isolated-boot-20260809/evidence/` (Bootlog SHA-256 `cb0dbafc5f992e3b51676075f0cca6857482e1f43a140a9b839bb0fc99e230a0`).

The boot only uses plugin/class lifecycle and dependency resolution. MMOSkillTree effect wiring, ECS/passive scheduler, positive damage path, shadow swing reconciliation, two-player isolation and client/in-game behavior remain explicitly undocumented; no deployment PASS.

## Reproducible Artifact - 2026-08-09

A real RED double build from the same sources initially produced two different JAR hashes (`01b5c0e6…` and `95ead1bd…`). The new contract test `mavenBuildPinsAReproducibleOutputTimestamp` was red because of the missing pin. After pinning `project.build.outputTimestamp=2026-08-09T00:00:00Z`, the test is GREEN.

Two subsequent independent Java 25 `clean verify` builds each passed 40 tests and produced byte-identical:

`007fe5d2db4d7d889db9476f8c6ac3660c769f2bd4c9cb176c81d8057a0db972`

This means that new builds from the same sources are package deterministic. The older isolated runtime boot remains historical evidence for the then-artifact `7bcbf190…`; the current reproducible hash was then checked separately in runtime.

## Reproducible Hash - Isolated Server Boot - 2026-08-09

The reproducible artifact SHA-256 `007fe5d2db4d7d889db9476f8c6ac3660c769f2bd4c9cb176c81d8057a0db972` was booted to `127.0.0.1:5523` in a new dependency-complete test root with EndlessLeveling 11.6.1, MMOSkillTree 1.5.2, ZiggfreedCommon 1.3.0 and EEM 3.10.0.

- Plugin discovered and `elite_nightweaver` adapter initialized
- `gameplay systems remain fail-closed` explicitly logged
- `Shadow:Nachtweber` activated
- Loopback listener and `Hytale Server Booted!` confirmed
- 0 `NoClassDefFoundError`, `NoSuchMethodError`, `LinkageError`, exceptions, setup/enable and asset validation errors
- four non-candidate-attributed HytaleGenerator-`SERR Reallocate` lines
- Adapter and plugin shut down in an orderly manner; `Shutdown completed!`
- Port 5523 and Java/Hytale process then free
- removed isolated JARs, cache, world and mod data; only evidence was retained

Status: `NACHTWEBER_REPRODUCIBLE_ARTIFACT_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-reproducible-artifact-boot-20260809/evidence/`; full bootlog SHA-256 `f8d2d366da6cbf9bfa923e2208894cb612aacbf616ccc6873ac8c61234b556fe`.

This evidence also does not include client/in-game behavior, live MMOSkillTree effect registration, scheduler/positive damage path, two-player isolation, and shadow swing reconciliation. No deployment PASS.

## Store Lifecycle ECS Wiring - Isolated Server Boot - 2026-08-09

The ownership-safe runtime infrastructure slice was implemented against the installed Hytale 0.5.7 ABI: identity-based store state registry, idempotent coordinator, plugin-local `TickingSystem<EntityStore>`, global `RemoveWorldEvent` cleanup and plugin-wide shutdown cleanup. `ComponentRegistryProxy` has a plugin-local Unregister contract for this; the MMOSkillTree-`ActiveAbilityService` still has no unregister contract and has not been changed.

RED→GREEN evidence covered missing store registry, shutdown cleanup, state-free maintenance lookups, coordinator lifecycle, concrete store tick signature, registry orchestration and entrypoint ownership. Java-25 `mvn clean verify` then passed 47 tests. Two independent builds produced byte-identical:

`6658e8e24971ff413f56b865efa520d3b181d057e57019c3ad7752316113bc82`

This exact artifact was booted dependency-complete on `127.0.0.1:5523`. The fresh root initially respected `DisabledByDefault: true`; only the isolated config received `Mods[Shadow:Nachtweber].Enabled=true`.

- exact marker: `adapter initialized: class elite_nightweaver; store lifecycle ECS wired; gameplay effects remain fail-closed`
- Plugin activated and reached `Hytale Server Booted!`
- 0 class, linkage, exception, setup, enable, asset validation or runtime registration errors
- Adapter, plugin and server shut down in a controlled manner
- Port 5523 and Java/Hytale process free
- removed isolated JARs, config, cache, world and mod data; only evidence was retained
- Live mod folder and live configuration unchanged

Status: `NACHTWEBER_STORE_LIFECYCLE_ECS_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-live-runtime-wiring-boot-20260809/evidence/`; Bootlog SHA-256 `07e24969a02e7aa1ee6019ed66818c1b090105647b076514d0e42baee6933047`.

The successful adoption of the store-level system and the world-remove handler by the plugin-local Hytale registries is proven. Tick execution with bound gameplay state, MMOSkillTree live effect registration, positive damage path, shadow swing reconciliation, two-player or client/in-game behavior are not claimed. No deployment PASS.

## Store-bound gameplay state and real maintenance tick – 2026-08-09

`NachtweberLedgerStoreRuntime` encapsulates its own `EntanglementLedger` and `VenomLedger` instances for each Hytale `Store<EntityStore>`. The maintenance path only removes expired status; it does not consume any due poison damage ticks and does not invoke any damage, proc, effect or MMOSkillTree paths. World-Remove and Plugin-Shutdown continue to empty and close the state at most once.

The first isolated boot was a true runtime RED proof: `AddWorldEvent` was fired before `EntityStore.getStore()` was initialized, the store was `null`, Nachtweber generated a `NullPointerException`, and the tickmarker remained off. The RED evidence was obtained. After the ABI check, the binding was moved to `StartWorldEvent` and additionally secured with null fail-closed handling.

Java-25 `mvn clean verify` then passed with **48 tests**, 0 failures, 0 errors and 0 skips. Two independent builds produced byte-identical:

`1105ac1c56b29fdbdabc8c2b58f8d37317bd7af49b16d9eb3a48b66d955060b9`

Exactly this hash was booted dependency-complete on `127.0.0.1:5523`:

- Nightweaver discovered, initialized and activated
- `Hytale Server Booted!` reached
- real marker `Shadow:Nachtweber first bound-store maintenance tick observed at … ms`
- 0 `NullPointerException`, Exceptions, class, linkage, setup, enable, asset validation or runtime registration errors
- six known, non-candidate-attributed HytaleGenerator-`[SERR] Reallocate` lines
- Adapter, plugin and server shut down in an orderly manner; Port and processes free
- removed isolated config, jar, cache, world and mod data; only RED/GREEN evidence was retained

Status: `NACHTWEBER_BOUND_STORE_MAINTENANCE_TICK_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-bound-store-tick-boot-20260809/evidence/`; GREEN bootlog SHA-256 `373c415e15cbb2eead5dc8d507679a6e642b38dd029c86f2c60ebdb54b07dba0`.

Positive `DamageCause` execution, active Ability/MMOSkillTree effect registration, Shadow Swing Reconciliation, two-player isolation and client/in-game behavior remain unproven. No deployment PASS.

## Storebound Gameplay Facade – 2026-08-09

The bound `NachtweberLedgerStoreRuntime` now only provides operational, post-close blocked access for `BLACK_THREAD`, `HUNTING_COCOON`, passive poison application and `VENOM_TICK`. Raw ledgers or service objects will not be released. Black Thread, Cocoon, Passive and Venom-Tick share exactly the same Entanglement/Venom ledgers and their store-local cooldown maps within a store; an identity-different store does not see any of these states.

The coordinator and plugin wiring only release this facade for an exact store that has already been bound. Unknown, removed, null stores or stores addressed after shutdown remain fail-closed. The EndlessLeveling Sorcery Provider is injected as a dormant dependency; there is still no automatic damage, effect or ability call.

RED→GREEN covered missing cross-service state sharing facade, store-specific coordinator dispatch, production power provider injection and the narrow wiring dispatch. Java-25 `mvn clean verify` passed with **52 tests**, 0 failures, 0 errors and 0 skips; `NachtweberRuntimeWiringTest` contains **12** green tests. Two independent builds produced byte-identical:

`8efde02bf9a7eb4b42b9de9a0cd2ff98e2ffe5f0f30160ec5aa5a1af0c6b8ec3`

Exactly this hash was booted dependency-complete on `127.0.0.1:5523`: discovery, initialization, plugin enable, store maintenance tick, `Hytale Server Booted!`, adapter/plugin shutdown and `Shutdown completed!` are proven; all strict error counts are 0. Four known HytaleGenerator-`[SERR] Reallocate` lines are not attributed to the candidate. Test state was removed, only evidence remained.

Status: `NACHTWEBER_STORE_GAMEPLAY_FACADE_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-store-gameplay-facade-boot-20260809/evidence/`; Bootlog SHA-256 `8d617e2d257644efb672d47936e7585ef65bb3dc7fd52c7f4c82fa53e213e6df`.

The boot deliberately did not trigger any ability or damage calls and did not change the MMOSkillTree registry. No client/ingame and no deployment PASS.

## DamageCause-AssetStore-Readiness - 2026-08-09

Local `javap` confirms `DamageCause.getAssetStore()`, `getAssetMap()` and `IndexedLookupTableAssetMap.getIndexOrDefault/getAsset`. EndlessLeveling 11.6.1 generates its ability DoT internally with Hytale's physical DamageCause; the source name `Poison` is not a DamageCause asset key. `Assets.zip` canonically contains `Server/Entity/Damage/Physical.json`, so the read-only probe checks the non-deprecated key `Physical` instead of the deprecated static alias `DamageCause.PHYSICAL`.

Before server boot, the probe reports fail-closed `UNAVAILABLE`. A `UNAVAILABLE` result does not consume the one-time token; the first consistent `READY` is observed exactly once. Null stores do not trigger the probe. The runtime check only verifies `AssetStore → identische AssetMap → Physical-Index → Asset mit ID Physical`; it constructs and emits no damage.

Java-25 `mvn clean verify`: **55 tests**, 0 failures, 0 errors, 0 skips and no new deprecation warning. Byte-identical double build: `e003fb8ca0ba7acc553dea0b54a818d4a6577be000e33c3471b8d6b911462fff`.

This exact hash was booted isolated on `127.0.0.1:5523`. The marker `DamageCause AssetStore ready: cause=Physical index=2; damage execution remains disabled` appeared exactly once. Discovery, plugin enable, store maintenance tick, server boot and controlled shutdown are proven; all strict error counts are 0. Six known HytaleGenerator-`[SERR] Reallocate` lines are not attributed to the candidate. Cleanupgate PASS, only evidence was retained.

Status: `NACHTWEBER_DAMAGECAUSE_READINESS_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-damagecause-readiness-boot-20260809/evidence/`; Bootlog SHA-256 `2ef4b908304aa81dd206b0f2fc5f8fdfce86a8df965941ef2afc05a73b43d06b`.

This is explicitly **not a positive damage PASS**: there was no owner/target entity pair, no `Damage` object, no `DamageSystems.executeDamage` call, and no client/ingame execution.

## Shadow Swing Reconciliation Core – 2026-08-09

`ShadowSwingReconciliationController` is a pure state machine to be fed on the server side without Hytale Entity or Velocity calls. Each store-bound `NachtweberLedgerStoreRuntime` has its own instance. Start/Step/Cancel are only offered via the narrow gameplay facade and no longer provide any results after store remove/close.

The following applies per owner: a maximum of 1.5 seconds session duration, 18 blocks of start anchor, 22 blocks of tether break, 1.25 blocks of release distance, 14 blocks/s pull target speed, maximum of 3 blocks/s correction per step and 0.25 seconds reattach interval. Stale timestamps do not mutate; non-finite server-side position/velocity only terminates the affected session fail-closed. Cancel keeps the reattach limit, explicit owner cleanup removes both states, close is idempotent and final.

The bundled config has been additively migrated to Schema 7 and names all values in seconds, blocks or blocks/s. No live configurations were changed. Java-25 `mvn clean verify`: **62 tests**, 0 failures, 0 errors, 0 skips; Controller 6 tests, Wiring 14 tests. Byte-identical double build: `64bd21779b7780a090eb40d508e31e82023de545113adab33083c621cfc45dca`.

The first isolated boot was a runtime RED: The new root respected `DisabledByDefault: true` and skipped Nachtweber. After local ABI verification of the config contract, `Mods[Shadow:Nachtweber].Enabled=true` was set exclusively in the isolated root. The corrected boot of the same hash achieved Discovery, Plugin-Enable, DamageCause-Readiness, Store-Maintenance-Tick and `Hytale Server Booted!`; all strict error counters are 0. Shadow Swing, Velocity and Damage were not executed. Six known HytaleGenerator-`[SERR] Reallocate` lines are not attributed to the candidate.

The server reported adapter/plugin shutdown and `Shutdown completed!`. Afterwards, a foreign JVM thread got stuck; PID 22000 was only specifically terminated after explicit user approval. The port and Java/Hytale processes were then free. A first cleanup attempt failed due to a Windows long path in `.cache`; its hastily printed marker was discarded. The long path retry and the new strict gate finally confirmed exactly one top-level entry (`evidence`).

Status: `NACHTWEBER_SHADOW_SWING_RECONCILIATION_CORE_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-shadow-swing-reconciliation-core-boot-20260809/evidence/`; Bootlog SHA-256 `ee155abe8092ac4f073850f69bb31a2325cb5ac28ed9262e9cde7c887e0a58b6`.

ECS tick/movement wiring, actual swing start, native velocity mutation, client reconciliation, in-game behavior or multiplayer are not proven. No deployment PASS.

## Two-Owner/Two-Store Isolation Preflight - 2026-08-09

The store-bound gameplay facade now offers `cleanupOwner(UUID)` as a fail-closed disconnect/class change primitive. Within exactly one `NachtweberLedgerStoreRuntime` it removes entanglement, poison, black thread/hunting cocoon/passive cooldowns as well as shadow swing session and reattach time of the specified owner. A null or closed runtime is rejected. The status of other owners and different identity stores remains unchanged.

The end-to-end RED test initially failed during test compile because `cleanupOwner(UUID)` was missing. Minimal-GREEN only added owner-selective ledger/cooldown primitives and the facade dispatch. The test uses Owner A and B in the first store as well as Owner A in a second store in parallel: After A's cleanup in Store 1, its status and cooldowns are reset, while Owner B and Store 2 continue to work unchanged.

Java-25 `mvn clean verify`: **63 tests**, 0 failures, 0 errors, 0 skips. Byte-identical double build: `a5ce7943dc7f09222c7a7deaf3fb11b60c753871843a8b03531f0feb097c5d15`.

This exact hash was booted dependency-complete on `127.0.0.1:5523`. Discovery, Plugin-Enable, DamageCause-Readiness, Store-Maintenance-Tick, `Hytale Server Booted!`, Nachtweber/plugin shutdown and `Shutdown completed!` are proven; all strict error counters are 0. The boot did not create a player, did not call `cleanupOwner`, and did not execute an ability, damage, or movement. Five known HytaleGenerator-`[SERR] Reallocate` lines are not attributed to the candidate.

After a complete server shutdown, a foreign JVM thread remained active again; PID 14552 was terminated after explicit user approval. The `HytaleClient.exe` PID 9992 that was visible later did not belong to the test server and was not changed. Port 5523 and server PID are free; Cleanupgate PASS, only `evidence` remained.

Status: `NACHTWEBER_MULTIPLAYER_ISOLATION_PREFLIGHT_BOOT_PASS`. Evidence: `.hermes/runtime/nightweber-multiplayer-isolation-preflight-boot-20260809/evidence/`; Bootlog SHA-256 `5c4055b93c96cd030d8d4545025bd0bfbb5eb4fad7d7f285ad95f418f951ac3c`.

This is explicitly **not a true two-player/multiplayer PASS**: the two owners are deterministic UUIDs in the unit preflight, not connected Hytale players. Reconnect/disconnect event wiring, real entities, client/ingame and deployment remain open.

## Owner cleanup at PlayerDisconnectEvent – 2026-08-09

The local Hytale 0.5.7 bytecode audit shows the flow in `Universe.removePlayer(PlayerRef)`: `PlayerDisconnectEvent` is dispatched globally before the server reads `PlayerRef.getReference()` and before the entity is removed from the store. The event provides the authoritative `PlayerRef`; `getUuid()` is read from this and, if the reference is still present, `getReference().getStore()` is read. Null PlayerRef, UUID, Reference or Store remain fail-closed.

`NachtweberRuntimeWiring.RegistrationPort` registers exactly one global disconnect handler via the plugin-local EventRegistry. The handler dispatches `cleanupOwner(UUID)` exclusively to the identity-specifically bound store. Unknown or already removed stores do not create a state. Event registration still belongs to the plugin lifecycle; no external MMOSkillTree registry was changed.

The RED test initially observed 0 instead of 1 disconnect registrations. Minimal-GREEN only added RegistrationPort, Wiring-Dispatch and the Hytale adapter. Null contexts are tested; the actual two-owner/two-store cleanup remains proven by `NachtweberMultiplayerIsolationPreflightTest`. Java-25 `mvn clean verify`: **63 tests**, 0 failures, 0 errors, 0 skips. Byte-identical double build: `ffc4c9f981d9b478f7791108a4dd2cbcb8dae6b71d04b2b15ebf8f068aae4732`.

This exact hash was booted dependency-complete on `127.0.0.1:5523`. The new `PlayerDisconnectEvent`/`PlayerRef.getReference()` linkage was accepted by the plugin setup; Discovery, enable, damage cause readiness, maintenance tick, server boot and complete shutdown are proven. All strict error counts are 0, five known generator SERRs are not attributed to the candidate. No players connected; therefore no real disconnect and no owner cleanup was triggered. No damage, movement, client control or in-game.

After `Shutdown completed!` the familiar foreign thread got stuck; PID 220 was terminated after explicit user approval. The HytaleClient PID 9992, which ran separately during the boot, was not controlled and had already ended itself during the final cleanup gate. Port, server PID and runtime state are cleaned; only `evidence` remained.

Status: `NACHTWEBER_OWNER_CLEANUP_EVENT_WIRING_BOOT_PASS`. Evidence: `.hermes/runtime/nightweaver-owner-cleanup-event-wiring-boot-20260809/evidence/`; Bootlog SHA-256 `4e64135a68eb8d5f6fec3073f7f9414449b7950abac6a99250a786c5cb680a4d`.

A real player disconnect remains untested without client/in-game approval. No true multiplayer or deployment PASS.

### Class change ABI demarcation

EndlessLeveling 11.6.1 has a direct class change callback with `AbilityBridge.Hooks.onClassChanged(UUID)`. This cannot be used in an ownership-safe manner for Nachtweber: `AbilityBridge` manages exactly one global hook and offers `register(Hooks)`, but no matching `unregister(Hooks)`. A registration could be overwritten by a foreign owner or remain in place after a plugin shutdown. Although `ProfileSwitchedEvent` and `BuildAppliedEvent` have symmetrical add/remove listeners, they are not proven class-change events; in addition, they only carry UUID/profile or build data and no store. Therefore, no class change wiring was added. This limit is a confirmed ABI blocker, not a failed test.

## Shadow Swing ECS/Velocity ABI Preflight – 2026-08-10

The preflight was strictly read-only and used Java-25-`javap` and JAR bytecode lookup against `HytaleServer.jar` SHA-256 `43d9bcff1dd31574577dbfc82147718dbe2ac16c19000071f965b521ba808cdc` and `MMOSkillTree-1.5.2.jar` SHA-256 `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`.

Confirmed are `PlayerRef.getReference()`, `Ref.getStore()`, `Store.getComponent(...)`, `TransformComponent.getPosition()`, `Velocity.getComponentType()`, `Velocity.addVelocity(...)`, `Velocity.addInstruction(Vector3d, VelocityConfig, ChangeVelocityType)`, `ChangeVelocityType.Add`, `World.execute(Runnable)` and the ECS tick signatures with `ArchetypeChunk`, `Store` and `CommandBuffer`. `GenericVelocityInstructionSystem` consumes add instructions and then calls `Velocity.addVelocity(...)`; `PlayerVelocityInstructionSystem` sends `ChangeVelocity(Add)` to the player for the same instruction and clears the list. This makes `addInstruction(..., Add)` the proven server/client-synchronized impulse path.

MMOSkillTrees hotbar filter and ability command call ability activation within `World.execute(...)`. `ActiveAbilityService.tryActivate(...)`, on the other hand, calls `AbilityEffect.execute(...)` without its own `Store.assertThread()` or `Store.isInThread()` guard. The existing, unregistered `ShadowSwingAbility` prototype mutates directly via `Velocity.addVelocity(...)`; it bypasses the instruction/packet reconciliation and does not yet use the store-local `ShadowSwingReconciliationController`.

Result: **ABI primitives READY, production movement wiring NO-GO**. Before activation, the pure reconciliation core, store/ref identity, world thread, component readiness, `addInstruction(..., Add)`, ECS system order and an ownership-safe effect registration contract must be implemented together and then checked for release with client/ingame. No entity was created, no component was read or mutated, no MMOSkillTree effect was registered and no client was started or controlled. This is not a Movement, Reconciliation or Deployment PASS.

## Shadow-Swing Core→ECS-Instruction-Wiring – 2026-08-10

RED→GREEN added `ShadowSwingMovementSystem` as plugin-local `EntityTickingSystem`. The query contract includes PlayerRef, Transform and Velocity. The tick discards null/foreign stores, non-store threads, invalid refs, missing components and non-finite position/velocity. Only an accepted, limited step of the store-local `ShadowSwingReconciliationController` generates `Velocity.addInstruction(correction, null, ChangeVelocityType.Add)`; direct `velocity.addVelocity(...)` mutation is excluded. `SystemDependency(Order.BEFORE, PlayerVelocityInstructionSystem.class)` anchors the producer in front of the player package consumer.

Java-25 `mvn clean verify` passed with **64 tests**, 0 failures, 0 errors and 0 skips. After the Schema 8 migration, two independent builds produced byte-identical `b9cc1b718538c6329e4abd9f7ab082bd061b28b0033c168a19decfa0c25ac935`.

Exactly this final Schema 8 hash was booted dependency-complete and loopback-only on `127.0.0.1:5523`. Nachtweber enable, DamageCause-Readiness (`Physical`, Index 2), first store-bound maintenance tick, `Hytale Server Booted!`, plugin-local shutdown and `Shutdown completed!` are proven. All strict linkage/setup/enable error counters are 0. Port and server JVM were then free; no hard process kill. Status: `NACHTWEBER_SCHEMA8_MOVEMENT_BOOT_PASS`; Bootlog SHA-256 `27240692ee91c7f86e1cf31adeec952b2c1a899a2137edf6976595e42ebc9e2a`.

This boot did not connect any players or start a core session; therefore neither movement nor damage were carried out. The ECS system is registered and inert. Live MMOSkillTree effect registration remains separate due to the lack of a public ownership-safe unregister contract. No client/in-game or deployment PASS.

## Owned Effects, Damage and Schema 9 PASS - 2026-08-10

- MMOSkillTree patcher RED against unmodified JAR: expected rejection (`changed entries: []`).
- Patcher GREEN: `registerIfAbsent` and exact-instance `unregister`; exactly one additional changed class; reproducible SHA-256 `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`.
- Three live registered effects dispatch exclusively via the identity-bound gameplay runtime. Shadow Swing launches the Core, Cocoon delivers 2.0 seconds of immobilization and binds the owner ref.
- `VenomDamageSystem` queries real NPC targets and may only execute the EndlessLeveling DoT with a valid owner/target ref in the identical store. Disconnect, WorldRemoval and Shutdown delete bindings.
- Java 25 `mvn clean verify`: **69 tests**, 0 failures, 0 errors, 0 skips.
- Nachtweber double build was byte-identical; SHA-256 `53257e2d9b4de121ef3ea95e841d1bcd8b9a43f0372eb2d11b43eaf2da0c7683`.
- Isolated dependency-complete boot with Hytale 0.5.7, EndlessLeveling, EEM 3.10.1, ZiggfreedCommon 1.3.0, patched MMOSkillTree and Nachtweber on `127.0.0.1:5523`: `NACHTWEBER_OWNED_EFFECTS_SCHEMA9_BOOT_PASS`.
- Confirmed: Plugin enabled, three owned effects registered, `Physical` Index 2 ready, server booted, first maintenance tick, Nachtweber shutdown before MMOSkillTree shutdown, no Nachtweber SEVERE, no `NoSuchMethod`, no registry collision.
- Bootlog SHA-256 `1ef7382cbff0518928d7ccc095b9e6dcc8471457be3e5c6fecb1e6081c26e47b`; Evidence `.hermes/runtime/nightweaver-owned-effects-schema9-boot-20260810/evidence/`.

Not executed: client, real skill activation, real movement/damage execution, real disconnect, two-player isolation and deployment. The first boot attempt without `ZiggfreedCommon` was a dependency-incomplete FAIL; the fresh full repeat run passed. It will not be interpreted as a product defect.
