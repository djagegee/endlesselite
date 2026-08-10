# Seuchenweber Runtime Acceptance

Status: 2026-08-07

## Evidence classes

| Gate | Result | Evidence |
|---|---|---|
| Compile and unit contracts | PASS | Final Maven `clean verify`; 87 active tests, 0 failures/errors/skips; unlock, XP-map, tree, localization, runtime config, migration and idempotent shared-config preservation covered |
| Core performance, 8 owners × 16 targets | PASS | 500 warmups + 2,000 measured samples; exactly 128 due owner-target ticks per sample; steady-state global expiration sweep included; p95 0.030700 ms, p99 0.045600 ms in the 2026-08-09 full build |
| Full local server boot | PASS | `logs/2026-08-09_12-22-02_server.log`; OAuth restore, class `elite_plagueweaver`, `SEUCHENWEBER_MASTERY`, 3 active and 4 passive hooks, plugin enablement and multiplayer boot completed in 3 min 29.848 s |
| Deployment identity | PASS | Build and deployed JAR are SHA-256-identical: `f8281b5d3376adb33117f5fec14d9c8138cdd92951b1ce845009afe003c67e7b` |
| Client/Ingame | PASS | 2026-08-09: class selection/icon, seven cards, DE texts/icons, claims/bindings, all 3 active + 4 passive behaviors, +65 mastery XP, reconnect and A→B→A profile isolation manually confirmed; see `MANUAL_CLIENT_ACCEPTANCE_2026-08-09.md` |
| Two-player multiplayer | BLOCKED | No second authenticated Hytale account is available. Automated owner/store isolation is green but does not replace the required live two-player test |

## Final server evidence

```text
[2026/08/07 11:57:33 INFO] [Seuchenweber|P]
Registered MMOSkillTree category SEUCHENWEBER_MASTERY with 3 active and 4 passive unlocks

[2026/08/07 11:57:33 INFO] [MMOSkillTreePlugin]
ActiveAbilitiesConfig: loaded 81 ability definition(s), 16 override(s)

[2026/08/07 11:57:33 INFO] [Seuchenweber|P]
Shadow:Seuchenweber runtime initialized: class elite_plagueweaver, Cosmic Ruin unlock at Prestige 30, 7 MMO ability definitions, 3 active abilities, 4 passive hooks

[2026/08/07 12:00:35 INFO] [HytaleServer]
Hytale Server Booted! [Multiplayer] took 3min 20sec 516ms 522us 600ns
```

## Unlock and MMOSkillTree progression

- Persistent class permission is granted server-authoritatively only after both conditions hold: EndlessLeveling prestige ≥ 30 and `ArcanePower_CosmicRuin_Spellbook` exists in the combined player inventory.
- The item is not consumed and access is not revoked when the item is later removed.
- The class definition uses the Cosmic Ruin Spellbook as its icon/signature item and grants the spellbook weapon category its highest multiplier.
- `Server/MMOSkillTree/XpMaps/Seuchenweber.json` maps only `ArcanePower_CosmicRuin_Spellbook` to `SEUCHENWEBER_MASTERY` XP (65 XP per qualifying MMOSkillTree weapon event).
- Tree unlock levels are: passive Blight Breath 1, active Seal of Decay 2, active Astral Rift 25, passive Astral Echo 40, active Chronoblight 55, passive Soul Diagnosis 70, passive Relic Attunement 90.
- German and English localization keys exist for all seven tree cards.
- Shared `custom-skills.json` and `abilities.json` are updated idempotently; unrelated Hymann content is preserved and the first pre-change abilities file is backed up.
- During boot Hymann performs an earlier validation before Seuchenweber's effect handlers exist and logs three temporary unknown-effect warnings. Seuchenweber then registers all handlers and reloads the same 81 definitions without validation issues. The final active configuration is usable, but eliminating the early load-order noise remains a hygiene task.
- Actual inventory unlock, XP gain, tree UI, reward claiming, binding, reconnect and profile-switch persistence were manually confirmed on 2026-08-09. The live two-player owner-isolation test remains **BLOCKED** because no second authenticated Hytale account is available.

## Core benchmark scope

`SeuchenweberCorePerformanceProbeTest` measures the server-authoritative pure-Java core for:

- 8 independent owners,
- 16 targets,
- 128 due owner-target marks per measured sample,
- ledger due-tick consumption and rescheduling,
- mastery damage calculation,
- Astral-Echo eligibility and per-selection cap helpers (the owner-interval budget is covered by a separate unit contract),
- global expiration-map scanning for despawned target cleanup.

The probe verifies the actual processed-tick count on every measured sample. A missing/empty fast path therefore cannot pass as a valid performance result.

Ephemeral caster, diagnosis and Astral-Rift owner references are released when their final mark/rift expires. A bounded one-second global expiration sweep also removes marks whose NPC target despawned before receiving another entity tick.

## Audit hardening

- Runtime maps and ledgers are isolated by `Store` identity; refs from one world are never consumed in another world's tick.
- Entity-index reuse discards the previous entity's marks and throttles without generating a natural-expiration reward.
- Echo-created targets are bound through the same target-identity guard; global full-mark rewards require a valid, store-local target `Ref`.
- Astral Echo permits only one bounded owner selection per toxin interval; echo-created marks remain ineligible for further echo propagation.
- Astral Rift caches its next pulse/expiration timestamp, avoiding one global ledger scan per NPC between real events.
- Chronoblight applies its published full-mark stun through the installed optional Perfect Utils 1.1 `StunMobAPI`; absence of the optional API degrades safely without linkage failure.
- Invalid Rift durations and cross-store ability contexts fail closed before components or caster references are retained.
- Store-level `TickingSystem` maintenance sweeps expired Nekrotoxin state and expires Astral Rifts even when no `NPCMarkerComponent` archetype is ticking.
- The Astral Rift active contract publishes only `teleportRange` and `riftDurationMs`; pulse radius remains the central runtime-configured value.

## Lifecycle release evidence

`StoreScopedState` uses identity-scoped strong references and is cleared on plugin shutdown/reload. The verified Hytale `RemoveWorldEvent` global event is now registered through `getEventRegistry().registerGlobal(...)`; it obtains `event.getWorld().getEntityStore().getStore()` and releases both Nekrotoxin and Astral-Rift runtime state for that exact Store before the world is removed. Store-level maintenance also removes expired references between world events.

## Explicit exclusions

The core result does **not** include:

- Hytale ECS chunk iteration,
- `Selector.selectNearbyEntities(...)`,
- component lookups or actual damage dispatch,
- MMOSkillTree component persistence,
- mana/stat mutations,
- client rendering, networking, multiplayer contention or garbage collection behavior during a live game session.

Therefore p95 ≤ 2.5 ms and p99 ≤ 5.0 ms are proven for the bounded core workload only. Final ECS/Ingame acceptance remains open.

## Release gate

Nachtweber and Endless-Elite Phase 3 remain blocked until the live two-player owner-isolation gate is completed. Client gameplay, all seven abilities, skilltree UI, claims/bindings, +65 mastery XP, reconnect and A→B→A profile isolation passed manually on 2026-08-09.
