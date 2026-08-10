# Nightweaver – Core Design 0.1.0

**Class ID:** `elite_nightweaver` · **Title:** Hunter of the Black Web

This slice contains exactly three active contracts (`BLACK_THREAD`, `SHADOW_SWING`, `HUNTING_COCOON`) and four passive contracts (`DANGER_SENSE`, `WALL_HUNTER`, `TOXIC_GLANDS`, `HUNTING_INSTINCT`).

`nightweaver_entanglement` and `nightweaver_venom` are tracked server-side per `(target, owner)`. Stacks are capped, duration is refreshed on reapplication, and cleanup is owner-specific. When delayed, hunting venom catches up by at most one tick and then schedules relative to the current time; DoT/reflection damage must not retrigger hunting venom. Boss, elite, and PvP factors reduce control linearly.

## Fail-Closed Runtime Adapter

The verified Hytale lifecycle is wired through `NachtweberPlugin(JavaPluginInit)`, `setup()`, and `shutdown()`. The adapter registers only `nachtweber.use` and the EndlessLeveling class `elite_nightweaver`; a rejected class registration is not treated as owned state and is therefore not removed incorrectly during shutdown. Successfully registered state is unregistered exactly once.

The manifest has the verified `Main` entrypoint and remains `DisabledByDefault: true`. Only the class/permission lifecycle and maintenance infrastructure encapsulated per store are registered. MMOSkillTree effects, damage, grappling, climbing, and positive gameplay paths remain unregistered. Deployment remains blocked.

## MMOSkillTree Staging Contract

`NACHTWEBER_MASTERY` defines seven original unlock IDs at levels 1/2/25/40/55/70/90. `NachtweberMmoInstaller` can create ability and DE/EN owner files additively, atomically, and with exactly one non-overwritten prior backup. Foreign entries are preserved; repeated execution is idempotent.

The live plugin lifecycle deliberately does not invoke this installer yet: active effects, SkillRegistry/SkillTreeConfig, icons, and gameplay hooks are not yet fully registered. This prevents unknown effects from entering the production MMOSkillTree configuration.

## BLACK_THREAD Gameplay Slice

`BlackThreadService` accepts only server-verified casts and checks target, finite distance, maximum range, and an owner-specific cooldown before modifying `nightweaver_entanglement`. Successful hits add two owner-isolated stacks; at three stacks, a bounded control impulse is created. Rejected casts modify neither ledger nor cooldown.

`BlackThreadAbility` implements the verified MMOSkillTree `AbilityEffect` contract and server-side selects only visible, hostile combat NPCs based on store, facing direction, and block raystep. Service NPCs and PvP players are excluded. Until an evidenced boss/elite classification API exists, the runtime adapter conservatively uses the boss factor `0.35` for all selected NPCs; this can make control too short, but never too strong.

The effect adapter and its registry are built, but not yet registered in `NachtweberPlugin`: `ActiveAbilityService` has no unregister contract in the verified API. This prevents a non-reversible partial reload. Live activation waits for a safe ownership/boot slice.

## HUNTING_COCOON Gameplay Slice

`HuntingCocoonService` accepts only server-verified casts that are finite and within range. Success requires at least three active `nightweaver_entanglement` stacks for the same `(target, owner)`. Only after authority, target, range, cooldown, damage-cause, and immunity checks are exactly those own stacks fully consumed and two owner-isolated `nightweaver_venom` stacks created. Foreign owner state is preserved; rejections modify neither entanglement, hunting venom, nor cooldown.

`VenomDamageCause` prevents DoT/reflection recursion. The immunity contract is injectable through `VenomImmunityResolver`; the runtime effect uses the same server-side visible hostile-NPC targeter as `BLACK_THREAD`. Expiry, tick, and cooldown addition saturate at `Long.MAX_VALUE` instead of producing negative time values. Operator values are presented in schema 4 as blocks, seconds, and stack counts.

`HuntingCocoonAbility`, the effect contract, and the extended registry are built and test-secured, but remain outside the plugin lifecycle for the same missing MMOSkillTree unregister/ownership reason as `BLACK_THREAD`. No live configuration mutation or deployment occurred.

## VENOM_TICK Damage Slice

`VenomTickService` drains at most one due tick per `(target, owner)` per update. Overdue intervals are not replayed as a burst; after a runtime failure, the tick remains consumed and cannot be replayed. Damage is calculated from own active venom stacks and the displayed EndlessLeveling `SORCERY`, bounded to a finite maximum value, and passed only as `VENOM_TICK` to the damage port.

The local active contract is `EndlessLevelingCore 11.6.1`. `getDisplayedAttributeTotal(UUID, SORCERY, fallback)`, `createAbilityDotDamage(Ref, float, String)`, and `DamageSystems.executeDamage(...)` are binary-verified. According to verified bytecode, the EndlessLeveling factory path sets `AUGMENT_DOT_DAMAGE` and `ABILITY_ORIGIN_PROC`; `shouldBypassOutgoingAugmentMath(...)` recognizes this DoT. `EndlessLevelingVenomDamageAdapter` binds damage to the owner `EntitySource`, validates owner, target, Ref/store, and cause, and fails closed if the runtime/asset contract is absent.

The positive factory/damage call requires the started Hytale `DamageCause` AssetStore and is therefore deliberately not simulated in isolated Maven tests. The tick service, ABI, and pre-boot fail-closed behavior are test-secured. Schema 9 registers a store-specific `VenomDamageSystem`; without real owner/target entities it remains inert, so isolated boot proves linkage but not positive damage execution.

## Store-Bound Runtime Maintenance

`NachtweberLedgerStoreRuntime` encapsulates separate entanglement and venom ledgers for each `Store<EntityStore>`. Store partitioning is based on object identity. Binding occurs only on the binary-confirmed `StartWorldEvent`, because a real isolated RED boot demonstrated that `AddWorldEvent` can occur before initialization of `EntityStore.getStore()`. A missing store is rejected fail-closed.

The plugin-local `TickingSystem<EntityStore>` removes only expired ledger state. It consumes no venom-damage tick and invokes no damage, proc, ability, or MMOSkillTree registration. The first real tick of a bound store is reported exactly once. World removal and shutdown remove state before `close()`, so every store is closed at most once; new bindings are blocked after coordinator shutdown.

The store-bound gameplay facade delegates Black Thread, Hunting Cocoon, passive venom application, venom tick, and Shadow Swing start/step/cancel to store-local services. Within the same store, Black Thread, Cocoon, and venom share exactly these two ledgers; the Shadow Swing reconciliation controller maintains separate owner-isolated session state. Within exactly that store, `cleanupOwner(UUID)` selectively removes both owner ledger states, all associated ability/passive cooldowns, and swing session plus reattach time; other owners and stores remain unchanged. The facade exposes no raw ledger, service, or controller references; after world removal/shutdown, every operation returns `Optional.empty()` or `false`. Coordinator dispatch accepts only the identical bound store. The EndlessLeveling sorcery provider is injected but read only on an explicit venom-tick invocation. Neither maintenance nor binding triggers damage, movement, or effects.

Owner cleanup is bound to Hytale's global `PlayerDisconnectEvent`. Local server bytecode proves that `Universe.removePlayer` dispatches this event before reading or removing the PlayerRef store reference. The adapter reads only `PlayerRef.getUuid()` and `PlayerRef.getReference().getStore()` and fails closed on incomplete context; the coordinator accepts only the already bound identical store. Registration belongs to the plugin-local EventRegistry.

Class-change cleanup remains separately open. EndlessLeveling 11.6.1 exposes the direct callback only as `AbilityBridge.Hooks.onClassChanged(UUID)` through a global single hook. `AbilityBridge.register(Hooks)` has no ownership-safe `unregister(Hooks)` contract. The symmetrically unregisterable `ProfileSwitchedEvent` and `BuildAppliedEvent` listeners are semantically not evidenced class-change events and provide no store. Nightweaver therefore does not automatically register any of these hooks.

At the first non-null `StartWorldEvent`, a read-only `DamageCauseReadinessProbe` may inspect the started Hytale asset chain once. It uses the `Assets.zip`-evidenced key `Physical` and verifies AssetStore, identical IndexedLookupMap, index, and asset ID consistently. `UNAVAILABLE`, inconsistent maps, runtime/linkage errors, and null stores remain fail-closed. The probe creates no `Damage` object and never invokes `DamageSystems.executeDamage`; its PASS is only a prerequisite, not positive damage evidence.

## Integrated Completion Slice

`TOXIC_GLANDS` processes only an explicit direct hit marked as server-verified. Its own DoT/proc/reflection paths are rejected before mutation. The passive creates one owner-isolated hunting-venom stack for six seconds and has an owner+target-specific internal cooldown of one second. `HUNTING_INSTINCT` adds exactly one stack, but only when that same target currently carries active entanglement from the same owner. Foreign Nightweaver state does not count. Cooldowns can be cleaned up owner-specifically.

`SHADOW_SWING` has a store-local reconciliation core with owner-isolated sessions, duration/anchor/tether/release limits, vector-bounded correction recommendations, reattach limit, stale-step protection, and fail-closed cleanup. The live-registered `StoreBoundShadowSwingAbility` checks store, thread, Ref, Transform, HeadRotation, and block anchor, and starts only that core session. `ShadowSwingMovementSystem` queries PlayerRef+Transform+Velocity, rejects foreign/invalid contexts, and passes only accepted core steps as bounded `Velocity.addInstruction(..., ChangeVelocityType.Add)`. It has `Order.BEFORE` relative to `PlayerVelocityInstructionSystem`; direct velocity mutation by the effect is excluded.

The read-only Hytale-0.5.7/MMOSkillTree-1.5.2 ABI preflight confirmed `Velocity.addInstruction(vector, config, ChangeVelocityType.Add)` as the Player package path. `PlayerVelocityInstructionSystem` emits `ChangeVelocity(Add)` and then clears the instruction list; MMOSkillTree's own `VelocityImpulseUtil` also uses `addInstruction(...)`. Nightweaver's producer therefore explicitly runs before this consumer. The effect and ECS producer enforce store/thread/Ref gates. Only the real client/in-game trajectory remains open, not the effect→core→ECS coupling.

`WALL_HUNTER` reads the native `CollisionResultComponent`, rejects pending collision checks, and accepts only completed touching/overlapping block collisions with a horizontal collision normal. Client-fed jumping movement counts only as advisory upward input; no movement occurs without server-confirmed wall contact. `DANGER_SENSE` performs authority, unlock, and cooldown preflight before the spatial query, validates a hard radius of at most 16 blocks, and retains at most 64 candidates. `Selector.selectNearbyEntities` itself has no early termination; the spatial scan therefore remains radius- and cadence-bounded. The adapter filters the same store, hostile combat NPCs, and line of sight, then reports the nearest visible target through an injectable alert port.

The bundled config uses schema 9 and contains concrete blocks, seconds, speeds, stack counts, and candidate limits for all three active and all four passive abilities. Schema 9 adds 2.0 seconds of Cocoon immobilization and the MMOSkillTree-owned registry contract (`registerIfAbsent`, exact-instance `unregister`). Live configurations are not migrated automatically. All seven MMOSkillTree definitions have concrete parameters; the runtime factory returns exactly three store-bound active effects.

Domain logic, configuration, staging definitions, fail-closed adapters, store maintenance, three store-bound active effects, the reconciliation core, Shadow Swing ECS instruction wiring, and the venom damage system are evidenced at source, packaging, and isolated runtime levels. The reproducible MMOSkillTree patch adds `registerIfAbsent` and `unregister(discriminator, expectedEffect)`; Nightweaver registers and removes only its three instances. `DisabledByDefault: true` remains in place. Positive damage/movement execution, actual disconnect, two-player isolation, client/in-game behavior, and deployment still require real entities and separate acceptance.
