# Seuchenweber – Manual Client Acceptance 2026-08-09

## Result

**PARTIAL / BLOCKED**

All single-player, UI, gameplay, reconnect, and profile-isolation checks passed in the complete local 40-JAR mod stack. The mandatory real two-player test could not be run because no second authenticated Hytale account is available. Automated owner/store-isolation tests pass, but do not replace this manual multiplayer gate test.

## Build and Deployment

- Project: original standalone Seuchenweber workspace (preserved locally outside this repository)
- Command: `mvn.cmd -q clean verify` via Apache Maven 3.9.16 and Java 25
- Tests: 87
- Failures: 0
- Errors: 0
- Skipped: 0
- Core performance: p95 0.030700 ms, p99 0.045600 ms for 8 owners × 16 targets
- JAR SHA-256: `f8281b5d3376adb33117f5fec14d9c8138cdd92951b1ce845009afe003c67e7b`
- Build and deployment JAR: byte-identical
- Rollback: timestamped local rollback snapshot retained outside this repository

## Full-Stack Boot

- Launcher: `START-LOCAL-HYTALE-SERVER.cmd`
- Bind: `127.0.0.1:5520`
- Log: timestamped local full-stack server log retained outside this repository
- OAuth: `OAUTH_STORE`, saved session successfully restored
- Boot: `Hytale Server Booted! [Multiplayer]` after 3 min 29.848 s
- Plugin: `Shadow:Seuchenweber` enabled
- Class: `elite_plagueweaver` registered
- Skill tree: `SEUCHENWEBER_MASTERY`, 38 milestones, exactly 3 active and 4 passive unlocks

## Manual PASS Results

Tested player: one authenticated local test account (stable identifier intentionally omitted from the public report).

1. The client connects authenticated and joins the `default` world.
2. MMOSkillTree shows the Seuchenweber category, exactly seven cards, icons, German text, and levels 2/10/25/40/55/70/90 correctly.
3. All seven nodes are present; the three active abilities can be bound.
4. EndlessLeveling shows Seuchenweber with the correct Cosmic Ruin spellbook icon; selection works.
5. Seal of Decay: casting, direct damage, Necrotoxin stacks, poison visual, and DoT work.
6. Astral Rift: teleport, rift duration, pulses, and poison work.
7. Chronodecay: damage, stacks, and a brief full-mark stun work; no permanent stun.
8. Plague Breath: automatic Necrotoxin stacks and visuals within the radius work.
9. Astral Echo: limited transfer to a nearby target works; no infinite loop observed.
10. Soul Diagnosis: the mark appears after sufficient stacks and disappears on expiry.
11. Relic Resonance: mana and cooldown refunds upon natural expiry of full marks are visible.
12. The Cosmic Ruin spellbook grants exactly +65 `SEUCHENWEBER_MASTERY` XP.
13. Reconnecting preserves the selected class, seven claims, three bindings, and level/XP.
14. Switching profiles A→B→A preserves separate states and fully restores profile A.
15. All three Seuchenweber XP shop tiers show their detailed description. The content-audit warning `UNLOCALIZED_DESC` is a stale validator finding for these entries.

## Visual Evidence

Screenshot:

A timestamped local Hytale screenshot is retained outside this repository.

Clearly visible:

- running Hytale client in the `Seedling Woods` world,
- selected `Seuchenweber` class,
- prestige display `P30`,
- three assigned active slots `A1`, `A2`, `A3`,
- three distinct green ability icons,
- target display `[Lv. 7] … [24/61]`,
- green status effect on the target,
- active combat/damage HUD with `Damage: 37` and `DPS: 3.8`.

The screenshot serves as UI/visual evidence. By itself, it proves neither server-side tick logic nor two-player owner isolation.

## Outstanding Multiplayer Gate Test

Still required:

1. Connect two authenticated players simultaneously.
2. Both use Seuchenweber against the same target.
3. Verify owner-separated Necrotoxin stacks, visual tier, diagnosis, echo, natural expiration, and damage attribution.
4. PvP remains disabled according to the active configuration (`allowPvp=false`).
5. One player logs out while Necrotoxin/rift is active; the other remains connected.
6. Verify world switching and return with both players.
7. No foreign marks, rewards, cooldowns, or visuals may be inherited.

## Known Third-Party Mod Issues in the Full Stack

Not attributed to Seuchenweber:

- The Forerunner 1.0.1: invalid `DispatchEventPhase`, failed Golem builders, and a `CustomUIHud` `NoSuchMethodError` on every player join.
- CreditAsset is registered twice by third-party mods.
- Starky's RayGun/ThunderGun register commands with invalid permission nodes.
- Void Asylum contains an invalid NPC motion controller.
- MmoMobScaling tries to add `ScaledMobComponent` multiple times and produces a heavy warning flood.
- EndlessFates disables Fate Zones because the `fatezones` world is missing.

These errors must be addressed separately; they did not prevent the tested Seuchenweber functions, but they mean that the overall mod stack cannot be described as error-free.
