# Endless Elite – Configuration

Status: schema `4`, currently implemented scope: **Seuchenweber**.

The shipped runtime configuration is currently located at `Seuchenweber/src/main/resources/config/seuchenweber.json`. JSON does not support comments, so this file is the authoritative operator reference. The YAML files under `config/examples/` are readable profiles for the future centralized Endless Elite configuration.

## Numbers and Units

- The decimal separator is always a period, regardless of the system language.
- `1.0 = 100 %`, `0.9 = 90 %`, `0.75 = 75 %`, `0.5 = 50 %`, `0.1 = 10 %`.
- Public time values are stored exclusively in **seconds**.
- Distances and radii are specified in **blocks**.
- Stacks, targets, mana, and thresholds are whole-number counts.
- Automatically written decimal values are rounded to no more than two meaningful decimal places.
- Changes currently take effect after a plugin/server restart; hot reload is not yet approved.

## General and Necrotoxin Settings

| Configuration Path | Description | Unit | Default | Minimum | Maximum | Lower/Higher Values | Activation | Scope |
|---|---|---:|---:|---:|---:|---|---|---|
| `schemaVersion` | Configuration schema version; do not reset manually. | Version | `4` | `4` | `4` | Do not use as a balance value. | Restart | Server |
| `necrotoxin.durationSeconds` | Duration of one Necrotoxin application. | Seconds | `10.0` | `0.05` | `300.0` | Lower: shorter DoTs; higher: longer DoTs. | Restart | PvE/PvP |
| `necrotoxin.tickIntervalSeconds` | Interval between DoT ticks. | Seconds | `1.0` | `0.05` | `60.0` | Lower: more frequent ticks/more load; higher: less frequent ticks. | Restart | PvE/PvP |
| `necrotoxin.maximumStacksPerOwner` | Maximum stacks per target and applying player. | Count | `5` | `1` | `16` | Lower: less scaling; higher: more DoT potential. | Restart | PvE/PvP |
| `necrotoxin.baseDamagePerTick` | Base damage per stack and tick. | Damage | `6.0` | `0.0` | `1000.0` | Lower: weaker DoT; higher: stronger DoT. | Restart | PvE/PvP |
| `necrotoxin.bossDamageMultiplier` | Necrotoxin damage against bosses. | Factor | `0.7` | `0.0` | `2.0` | Lower reduces boss damage; higher increases it. | Restart | PvE |
| `necrotoxin.eliteDamageMultiplier` | Necrotoxin damage against elite enemies. | Factor | `0.95` | `0.0` | `2.0` | Lower reduces elite damage; higher increases it. | Restart | PvE |
| `necrotoxin.pvpPlayerDamageMultiplier` | Necrotoxin damage against players. | Factor | `0.65` | `0.0` | `2.0` | Lower reduces player damage; higher increases it. | Restart | PvP |
| `necrotoxin.pvpSummonDamageMultiplier` | Necrotoxin damage against player summons. | Factor | `0.75` | `0.0` | `2.0` | Lower reduces damage; higher increases it. | Restart | PvP |
| `necrotoxin.allowPvp` | Allows Necrotoxin against players. | Boolean | `false` | – | – | `false` disables PvP application. | Restart | PvP |
| `necrotoxin.allowBosses` | Allows Necrotoxin against bosses. | Boolean | `true` | – | – | `false` excludes bosses. | Restart | PvE |
| `necrotoxin.maximumOwnerEntries` | Safety limit for owner states stored simultaneously. | Count | `256` | `1` | `4096` | Lower saves memory; higher allows more simultaneous states. | Restart | Server/Performance |
| `necrotoxin.overdueTickPolicy` | Behavior for overdue ticks; must remain `single_tick_no_replay`. | ID | `single_tick_no_replay` | – | – | Prevents catch-up damage bursts. | Restart | Server/Performance |
| `necrotoxin.rejectDotProcRecursion` | Prevents DoT proc recursion. | Boolean | `true` | – | – | `false` is not approved for release. | Restart | PvE/PvP |
| `necrotoxin.rejectReflectionRecursion` | Prevents reflection recursion. | Boolean | `true` | – | – | `false` is not approved for release. | Restart | PvE/PvP |

## Active Abilities

| Configuration Path | Description | Unit | Default | Minimum | Maximum | Lower/Higher Values | Activation | Scope |
|---|---|---:|---:|---:|---:|---|---|---|
| `abilities.seal_of_decay.manaCost` | Mana cost of Seal of Decay. | Mana | `25` | `0` | `1000` | Lower: usable more often; higher: more expensive. | Restart | PvE/PvP |
| `abilities.seal_of_decay.cooldownSeconds` | Cooldown of Seal of Decay. | Seconds | `8.0` | `0.5` | `300.0` | Lower: more often; higher: less often. | Restart | PvE/PvP |
| `abilities.seal_of_decay.baseDamage` | Direct initial damage. | Damage | `18.0` | `0.0` | `1000.0` | Lower reduces direct damage; higher increases it. | Restart | PvE/PvP |
| `abilities.seal_of_decay.nekrotoxinStacksApplied` | Necrotoxin stacks applied. | Count | `2` | `1` | `16` | Lower: less DoT; higher: more DoT. | Restart | PvE/PvP |
| `abilities.seal_of_decay.castRangeBlocks` | Maximum target range. | Blocks | `22.0` | `1.0` | `100.0` | Lower: closer; higher: farther. | Restart | PvE/PvP |
| `abilities.astral_rift.manaCost` | Mana cost of Astral Rift. | Mana | `35` | `0` | `1000` | Lower: cheaper; higher: more expensive. | Restart | PvE/PvP |
| `abilities.astral_rift.cooldownSeconds` | Cooldown of Astral Rift. | Seconds | `14.0` | `0.5` | `300.0` | Lower: more often; higher: less often. | Restart | PvE/PvP |
| `abilities.astral_rift.teleportRangeBlocks` | Maximum blink range. | Blocks | `12.0` | `1.0` | `100.0` | Lower: shorter; higher: farther. | Restart | PvE/PvP |
| `abilities.astral_rift.riftRadiusBlocks` | Radius of a rift pulse. | Blocks | `5.0` | `0.5` | `32.0` | Lower: less area; higher: more targets/load. | Restart | PvE/PvP |
| `abilities.astral_rift.durationSeconds` | Rift lifetime. | Seconds | `5.0` | `0.05` | `60.0` | Lower: fewer pulses; higher: more pulses. | Restart | PvE/PvP |
| `abilities.astral_rift.pulseIntervalSeconds` | Interval between rift pulses. | Seconds | `1.0` | `0.05` | `10.0` | Lower: more load; higher: fewer pulses. | Restart | PvE/PvP |
| `abilities.astral_rift.nekrotoxinStacksPerPulse` | Stacks per affected target and pulse. | Count | `1` | `1` | `16` | Lower: less DoT; higher: more DoT. | Restart | PvE/PvP |
| `abilities.astral_rift.maximumTargetsPerPulse` | Maximum targets per pulse; code hard cap is 16. | Count | `8` | `1` | `16` | Lower: less load; higher: more hits. | Restart | PvE/PvP/Performance |
| `abilities.chronoblight.manaCost` | Mana cost of Chronoblight. | Mana | `45` | `0` | `1000` | Lower: cheaper; higher: more expensive. | Restart | PvE/PvP |
| `abilities.chronoblight.cooldownSeconds` | Cooldown of Chronoblight. | Seconds | `18.0` | `0.5` | `300.0` | Lower: more often; higher: less often. | Restart | PvE/PvP |
| `abilities.chronoblight.baseDamage` | Direct initial damage. | Damage | `12.0` | `0.0` | `1000.0` | Lower reduces direct damage; higher increases it. | Restart | PvE/PvP |
| `abilities.chronoblight.nekrotoxinStacksApplied` | Additional Necrotoxin stacks. | Count | `2` | `1` | `16` | Lower: less DoT; higher: more DoT. | Restart | PvE/PvP |
| `abilities.chronoblight.castRangeBlocks` | Maximum target range. | Blocks | `20.0` | `1.0` | `100.0` | Lower: closer; higher: farther. | Restart | PvE/PvP |
| `abilities.chronoblight.fullMarkStunSeconds` | Optional control at full mark; currently blocked by the API. | Seconds | `0.75` | `0.0` | `10.0` | Lower: shorter; higher: longer. | Restart | PvE/PvP |

## Passive Abilities and Damage Budget

| Configuration Path | Description | Unit | Default | Minimum | Maximum | Lower/Higher Values | Activation | Scope |
|---|---|---:|---:|---:|---:|---|---|---|
| `passives.necrotoxic_mastery.tickDamageMultiplier` | Mastery DoT multiplier. | Factor | `1.25` | `0.0` | `3.0` | `1.0` unchanged; higher increases it. | Restart | PvE/PvP |
| `passives.necrotoxic_mastery.auraRadiusBlocks` | Blight Breath radius around the active Seuchenweber. | Blocks | `8.0` | `0.5` | `32.0` | Lower: tighter aura; higher: more potential PvE targets and load. | Restart | PvE/Performance |
| `passives.necrotoxic_mastery.auraPulseIntervalSeconds` | Interval between Blight Breath pulses. | Seconds | `2.0` | `0.25` | `30.0` | Lower: more frequent stacks/more load; higher: slower stack application. | Restart | PvE/Performance |
| `passives.astral_echo.maximumEchoTargetsPerTick` | Maximum echo targets per tick. | Count | `1` | `0` | `16` | Lower reduces echoes; higher expands them. | Restart | PvE/PvP/Performance |
| `passives.astral_echo.echoRadiusBlocks` | Echo range. | Blocks | `5.0` | `0.0` | `32.0` | Lower: tighter; higher: more range/load. | Restart | PvE/PvP |
| `passives.astral_echo.echoStacksApplied` | Necrotoxin stacks per echo. | Count | `1` | `1` | `16` | Lower: less DoT; higher: more DoT. | Restart | PvE/PvP |
| `passives.soul_diagnosis.diagnosisStackThreshold` | Stack threshold for Soul Diagnosis. | Count | `3` | `1` | `16` | Lower: earlier; higher: later. | Restart | PvE/PvP |
| `passives.relic_attunement.manaRefundAmount` | Mana refund. | Mana | `8` | `0` | `1000` | Lower: less; higher: more returned. | Restart | PvE/PvP |
| `passives.relic_attunement.cooldownRefundSeconds` | Cooldown refund. | Seconds | `1.2` | `0.0` | `60.0` | Lower: less; higher: larger refund. | Restart | PvE/PvP |
| `damageBudget.representativeDirectDamage` | Modeled direct damage for balance verification. | Damage | `30.0` | `0.0` | `100000.0` | Used only by the balance contract. | Restart | QA |
| `damageBudget.representativeDotDamage` | Modeled DoT damage for balance verification. | Damage | `120.0` | `0.0` | `100000.0` | Used only by the balance contract. | Restart | QA |
| `damageBudget.minimumDotShare` | Minimum share of DoT damage. | Factor | `0.7` | `0.7` | `1.0` | Higher enforces stronger DoT dominance. | Restart | QA/PvE/PvP |

## Error Handling

Invalid values are not stored silently. The loader names the affected key and rejects non-finite numbers, decimal commas, negative time values, and non-integer counts. Schema 3 is migrated to schema 4 once, backup-first: unknown keys are retained, missing Blight Breath values are added with `8.0` blocks and `2.0` seconds, and a second run does not modify the file again.
