# Seuchenweber – Manual Client Acceptance 2026-08-09

## Result

**PARTIAL / BLOCKED**

All single-player, UI, gameplay, reconnect, and profile-isolation checks passed in the full local 40-JAR mod stack. The mandatory real two-player test could not be performed because no second authenticated Hytale account is available. Automated owner/store-isolation tests pass, but they do not replace this manual multiplayer gate test.

## Build and Deployment

- Project: `C:\Users\agege\Projects\EndlessElite\Seuchenweber`
- Command: `mvn.cmd -q clean verify` via Apache Maven 3.9.16 and Java 25
- Tests: 87
- Failures: 0
- Errors: 0
- Skipped: 0
- Core performance: p95 0.030700 ms, p99 0.045600 ms for 8 owners × 16 targets
- JAR SHA-256: `f8281b5d3376adb33117f5fec14d9c8138cdd92951b1ce845009afe003c67e7b`
- Build and deployment JAR: byte-identical
- Rollback: `C:\Users\agege\Desktop\LOKAL SERVER\backups\seuchenweber-client-gate-20260809-122154`

## Full-Stack Boot

- Launcher: `START-LOCAL-HYTALE-SERVER.cmd`
- Bind: `127.0.0.1:5520`
- Log: `C:\Users\agege\Desktop\LOKAL SERVER\logs\2026-08-09_12-22-02_server.log`
- OAuth: `OAUTH_STORE`, saved session restored successfully
- Boot: `Hytale Server Booted! [Multiplayer]` after 3 min 29.848 s
- Plugin: `Shadow:Seuchenweber` enabled
- Class: `elite_plagueweaver` registered
- Skill tree: `SEUCHENWEBER_MASTERY`, 38 milestones, exactly 3 active and 4 passive unlocks

## Manual PASS Results

Tested player: Acehoroth (`121d54a3-1fee-46ac-b5af-c23fc39da804`).

1. The client connects with authentication and joins the `default` world.
2. MMOSkillTree displayed the Seuchenweber category, exactly seven cards, icons, German text, and the then-active levels 2/10/25/40/55/70/90 correctly.
3. All seven nodes are present; the three active abilities can be bound.
4. EndlessLeveling displays Seuchenweber with the correct Cosmic Ruin spellbook icon; selection works.
5. Seal of Decay: casting, direct damage, Necrotoxin stacks, poison visual, and DoT work.
6. Astral Rift: teleport, rift duration, pulses, and poison work.
7. Chronoblight: damage, stacks, and a short full-mark stun work; no permanent stun.
8. Blight Breath: automatic Necrotoxin stacks and visuals within the radius work.
9. Astral Echo: limited transfer to a nearby target works; no infinite loop observed.
10. Soul Diagnosis: the mark appears once sufficient stacks are present and disappears on expiration.
11. Relic Attunement: mana and cooldown reimbursement on the natural expiration of full marks is visible.
12. Cosmic Ruin Spellbook grants exactly +65 `SEUCHENWEBER_MASTERY` XP.
13. Reconnect preserves the selected class, seven claims, three bindings, and level/XP.
14. Profile switch A→B→A preserves separate states and fully restores profile A.
15. All three Seuchenweber XP shop tiers show their detailed description. The content-audit warning `UNLOCALIZED_DESC` is a stale validator finding for these entries.

> This historical client evidence predates the current repository contract that moves the `Blight Breath` / `Pesthauch` passive to level 1. The current canonical sequence is 1/2/25/40/55/70/90 and requires fresh client acceptance; this 2026-08-09 run does not prove that revised level assignment.

## Visual Evidence

Screenshot:

`C:\Users\agege\Pictures\Hytale Screenshots\Hytale2026-08-09_12-28-26.png`

Clearly visible:

- running Hytale client in the `Seedling Woods` world,
- selected `Seuchenweber` class,
- `P30` prestige display,
- three assigned active slots `A1`, `A2`, `A3`,
- three distinguishable green ability icons,
- target display `[Lv. 7] … [24/61]`,
- green status effect on the target,
- active combat/damage HUD with `Damage: 37` and `DPS: 3.8`.

The screenshot serves as UI/visual evidence. By itself, it proves neither server-side tick logic nor two-player owner isolation.

## Outstanding Multiplayer Gate Test

Still required:

1. Connect two authenticated players simultaneously.
2. Have both use Seuchenweber against the same target.
3. Verify owner-separated Necrotoxin stacks, visual tier, diagnosis, echo, natural expiration, and damage attribution.
4. PvP remains disabled according to the active configuration (`allowPvp=false`).
5. One player logs out while Necrotoxin/Rift is active; the other remains connected.
6. Verify world switching and return with both players.
7. No foreign marks, rewards, cooldowns, or visuals may be inherited.

## Known Third-Party Mod Issues in the Full Stack

Not attributed to Seuchenweber:

- The Forerunner 1.0.1: invalid `DispatchEventPhase`, failed Golem builders, and `CustomUIHud` `NoSuchMethodError` on every player join.
- CreditAsset is registered twice by third-party mods.
- Starky's RayGun/ThunderGun register commands with invalid permission nodes.
- Void Asylum contains an invalid NPC motion controller.
- MmoMobScaling attempts to add `ScaledMobComponent` multiple times and produces a severe warning flood.
- EndlessFates disables Fate Zones because the `fatezones` world is missing.

These errors must be addressed separately; they did not prevent the tested Seuchenweber functions, but they mean the overall mod stack cannot be described as error-free.
