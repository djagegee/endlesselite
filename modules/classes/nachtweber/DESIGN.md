# Nightweaver – Core Design 0.1.0

**Class ID:** `elite_nightweaver` · **Title:** Black Web Hunter

This slice has exactly three active contracts (`BLACK_THREAD`, `SHADOW_SWING`, `HUNTING_COCOON`) and four passive contracts (`DANGER_SENSE`, `WALL_HUNTER`, `TOXIC_GLANDS`, `HUNTING_INSTINCT`).

`nightweaver_entanglement` and `nightweaver_venom` are managed on the server side for each `(target, owner)`. Stacks are limited, duration is renewed when reapplied, cleanup is owner-specific. If there is a delay, venom catches up by at most one tick and then plans relative to the current time; DoT/Reflection damage may not trigger venom again. Boss, Elite and PvP factors reduce control linearly.

## Fail-closed runtime adapter

The proven Hytale lifecycle is connected via `NachtweberPlugin(JavaPluginInit)`, `setup()` and `shutdown()`. The adapter only registers `nachtweber.use` and the EndlessLeveling class `elite_nightweaver`; a rejected class registration is not treated as a separate condition and is therefore not erroneously removed during shutdown. Successfully registered state is unregistered exactly once.

The manifest has the verified `Main` entry point and remains `DisabledByDefault: true`. Only the class/permission lifecycle and the maintenance infrastructure encapsulated per store are registered. MMOSkillTree effects, damage, grappling, climbing and positive gameplay paths are still not registered. Deployment remains blocked.

## MMOSkillTree staging contract

`NACHTWEBER_MASTERY` defines seven original unlock IDs at levels 1/2/25/40/55/70/90. `NachtweberMmoInstaller` can create Ability and DE/EN owner files additively, atomically and with exactly one non-overwritten prior backup. Foreign entries are retained; repetition is idempotent.

The live plugin lifecycle deliberately does not call this installer yet: active effects, SkillRegistry/SkillTreeConfig, icons and gameplay hooks are not yet fully registered. This means that no unknown effects get into the production MMOSkillTree configuration.

## BLACK_THREAD gameplay slice

`BlackThreadService` only accepts server-verified casts, checks target, finite distance, maximum range and an owner-specific cooldown before changing `nightweaver_entanglement`. Successful hits add two owner-isolated stacks; from three stacks onwards, a limited control impulse is created. Rejected casts do not change the ledger or cooldown.

`BlackThreadAbility` implements the proven MMOSkillTree `AbilityEffect` contract and selects only visible, enemy combat NPCs on the server side based on store, view direction and block raystep. Service NPCs and PvP players are excluded. As long as there is no documented boss/elite classification API, the runtime adapter conservatively uses the boss factor `0.35` for all selected NPCs; as a result, control can be too short, but never too strong.

The effect adapter and its registry are built, but not yet registered in `NachtweberPlugin`: `ActiveAbilityService` does not have an unregister contract in the tested API. This prevents a partial reload that cannot be rolled back. Live activation awaits a secure ownership/boot slice.

## HUNTING_COCOON gameplay slice

`HuntingCocoonService` only accepts server-verified, finite, and within-range casts. A success requires at least three active `nightweaver_entanglement` stacks of the same `(target, owner)`. Only after the authority, target, range, cooldown, damage cause and immunity checks are these own stacks fully consumed and two owner-isolated `nightweaver_venom` stacks are created. Foreign owner states are retained; rejections do not change entanglement, venom, or cooldown.

`VenomDamageCause` prevents DoT/reflection recursion. The immunity contract is injectable via `VenomImmunityResolver`; the runtime effect uses the same visible hostile-NPC targeter on the server side as `BLACK_THREAD`. Expiration, tick and cooldown addition saturate with `Long.MAX_VALUE` instead of generating negative time values. Operator values are shown in Schema 4 as blocks, seconds, and batch counts.

`HuntingCocoonAbility`, Effect-Contract and the extended registry are built and test-covered, but remain outside the plugin lifecycle for the same missing MMOSkillTree unregister/ownership reason as `BLACK_THREAD`. There was no live configuration mutation and no deployment.

## VENOM_TICK Damage slice

`VenomTickService` drains a maximum of one due tick per update per `(target, owner)`. Overdue intervals are not replayed as a burst; after a runtime error, the tick remains used up and cannot be played again. Damage is calculated from your own active poison stacks and the displayed EndlessLeveling-`SORCERY`, limited to a finite maximum value and passed exclusively to the damage port as `VENOM_TICK`.

The local active contract is `EndlessLevelingCore 11.6.1`. Binary tested are `getDisplayedAttributeTotal(UUID, SORCERY, fallback)`, `createAbilityDotDamage(Ref, float, String)` and `DamageSystems.executeDamage(...)`. According to the checked bytecode, the EndlessLeveling factory path sets `AUGMENT_DOT_DAMAGE` and `ABILITY_ORIGIN_PROC`; `shouldBypassOutgoingAugmentMath(...)` detects this DoT. `EndlessLevelingVenomDamageAdapter` binds the damage to the owner `EntitySource`, validates owner, target, ref/store and cause and fails closed if the runtime/asset contract is missing.

The positive factory/damage call requires the started Hytale-`DamageCause`-AssetStore and is therefore deliberately not simulated in isolated Maven tests. Tick service, ABI and pre-boot fail-closed behavior are test-covered. Schema 9 registers a store-specific `VenomDamageSystem`; without real owner/target entities it remains inert, so the isolated boot proves linkage but not positive damage execution.

## Store-bound runtime maintenance

`NachtweberLedgerStoreRuntime` encapsulates separate entanglement and poison ledgers for each `Store<EntityStore>`. Store partitioning is based on object identity. Binding only occurs on the binary confirmed `StartWorldEvent` because a real isolated RED boot proved that `AddWorldEvent` can occur before initialization of `EntityStore.getStore()`. A missing store is discarded fail-closed.

The plugin-local `TickingSystem<EntityStore>` only removes expired ledger states. It does not consume a poison damage tick or invoke any damage, proc, ability or MMOSkillTree registration. The first real tick of a bound store is reported exactly once. World-Remove and Shutdown remove the state before `close()` so that each store is closed at most once; after coordinator shutdown, new bindings are blocked.

The store-bound gameplay facade delegates Black Thread, Hunting Cocoon, passive poison application, Venom tick and Shadow Swing start/step/cancel to store-local services. Black Thread, Cocoon and venom share exactly these two ledgers within the same store; the Shadow Swing reconciliation controller has separate owner-isolated session state. `cleanupOwner(UUID)` owner-selectively removes both ledger states, all associated ability/passive cooldowns as well as swing session and reattach time within this exact store; other owners and stores remain unchanged. The facade does not give out raw ledger, service or controller references; each operation returns `Optional.empty()` or `false` after world remove/shutdown. The coordinator dispatch only accepts the identical bound store. The EndlessLeveling sorcery provider is injected, but is only read upon an explicit Venom tick call. Neither maintenance nor binding triggers damage, movement or effects.

Owner cleanup is tied to Hytale's global `PlayerDisconnectEvent`. The local server bytecode proves that `Universe.removePlayer` dispatches this event before reading or removing the PlayerRef store reference. The adapter only reads `PlayerRef.getUuid()` and `PlayerRef.getReference().getStore()` and fails closed if the context is incomplete; the coordinator only accepts the identical store that has already been bound. The registration belongs to the plugin-local EventRegistry.

Class change cleanup remains open separately. EndlessLeveling 11.6.1 exposes the direct callback exclusively as `AbilityBridge.Hooks.onClassChanged(UUID)` via a global single hook. `AbilityBridge.register(Hooks)` does not have an ownership-secure `unregister(Hooks)` contract. The symmetrically unsubscribeable `ProfileSwitchedEvent` and `BuildAppliedEvent` listeners are semantically not proven class-change events and do not provide a store. Therefore, Nachtweber does not register any of these hooks automatically.

At the first non-null `StartWorldEvent`, a read-only `DamageCauseReadinessProbe` may check the started Hytale asset chain once. It uses the key `Physical` evidenced by `Assets.zip` and verifies AssetStore, identical IndexedLookupMap, index and asset ID consistently. `UNAVAILABLE`, inconsistent maps, runtime/linkage errors and null stores remain fail-closed. The probe does not create a `Damage` object and never calls `DamageSystems.executeDamage`; its PASS is only a requirement, not positive proof of damage.

## Integrated completion slice

`TOXIC_GLANDS` only processes a direct hit that is explicitly verified as server-side. Its own DoT/proc/reflection paths are rejected before mutation. The passive creates an owner-isolated venom stack for six seconds and has an owner+target-specific internal cooldown of one second. `HUNTING_INSTINCT` adds exactly one stack, but only if the same target has active entanglement from the same owner at that time. Foreign Nightweaver conditions do not count. Cooldowns can be cleaned up on an owner-specific basis.

`SHADOW_SWING` has a store-local reconciliation core with owner-isolated sessions, duration/anchor/tether/release limits, vector-bounded correction recommendation, reattach limit, stale step protection and fail-closed cleanup. The live registered `StoreBoundShadowSwingAbility` checks store, thread, ref, transform, head rotation and block anchor and only starts this core session. `ShadowSwingMovementSystem` queries PlayerRef+Transform+Velocity, discards foreign/invalid contexts and only passes accepted core steps as bounded `Velocity.addInstruction(..., ChangeVelocityType.Add)`. It has `Order.BEFORE` to `PlayerVelocityInstructionSystem`; direct velocity mutation by the effect is excluded.

The read-only Hytale-0.5.7/MMOSkillTree-1.5.2 ABI preflight confirmed `Velocity.addInstruction(vector, config, ChangeVelocityType.Add)` as the player package path. `PlayerVelocityInstructionSystem` emits `ChangeVelocity(Add)` and then empties the instruction list; MMOSkillTree's own `VelocityImpulseUtil` also uses `addInstruction(...)`. Nachtweber's producer therefore runs explicitly before this consumer. Effect and ECS producers enforce store/thread/ref gates. Only the real client/in-game trajectory remains open, no longer the Effect→Core→ECS coupling.

`WALL_HUNTER` reads the native `CollisionResultComponent`, discards pending collision checks and only accepts completed touching/overlapping block collisions with horizontal collision normals. The client-powered jump move is only considered an advisory upward entry; without wall contact confirmed by the server, no movement occurs. `DANGER_SENSE` executes authority, unlock and cooldown preflight before the spatial query, validates a hard radius of maximum 16 blocks and keeps a maximum of 64 candidates. `Selector.selectNearbyEntities` itself does not have early termination; that is why the spatial scan remains radius and cadence bound. The adapter filters the same store, enemy combat NPCs and line of sight and reports the next visible target via an injectable alert port.

The bundled config uses Schema 9 and contains specific blocks, seconds, speeds, stacks and candidate limits for all three active and all four passive abilities. Schema 9 adds 2.0 second Cocoon immobilization and the MMOSkillTree-Owned registry contract (`registerIfAbsent`, exact-instance `unregister`). Live configurations are not automatically migrated. All seven MMOSkillTree definitions have concrete parameters; the runtime factory delivers exactly three store-bound active effects.

Domain logic, configuration, staging definitions, fail-closed adapters, store maintenance, three store-bound active effects, the reconciliation core, Shadow Swing ECS instruction wiring and Venom damage system are documented on the source, packaging and isolated runtime side. The reproducible MMOSkillTree patch complements `registerIfAbsent` and `unregister(discriminator, expectedEffect)`; Nachtweber only registers and removes its three instances. `DisabledByDefault: true` remains. Positive damage/movement execution, true disconnect, two-player isolation, client/in-game and deployment still require real entities and separate acceptance.
