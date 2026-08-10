# Seuchenweber Configuration – Schema 4

Active operator file after the first server start:

```text
mods/Seuchenweber/seuchenweber.json
```

Changes take effect on the next full server start. Invalid values are not silently overwritten: the runtime reports the full path, the received value, the valid range, and uses the safe default value for that start.

## Plague Breath

| Path | Unit | Default | Minimum | Maximum | Effect | Scope |
|---|---:|---:|---:|---:|---|---|
| `passives.necrotoxic_mastery.auraRadiusBlocks` | Blocks | `8.0` | `0.5` | `32.0` | A larger value reaches more distant hostile combat NPCs; a smaller value restricts the proximity aura more strongly. | PvE; PvP remains excluded by the existing target check. |
| `passives.necrotoxic_mastery.auraPulseIntervalSeconds` | Seconds | `2.0` | `0.25` | `30.0` | A smaller value grants stacks more frequently; a larger value slows stack application. | Per active Seuchenweber and World Store. |

Plague Breath continues to grant **exactly one owner-bound Necrotoxin stack per valid target** per pulse. Stack count, damage, and particles are intentionally not duplicated here:

- `NekrotoxinDamageSystem` remains the sole damage authority.
- The visual class aura remains damage-free and independent of the Plague Breath cadence.
- Target particles remain in the existing poison visual path.

## Migration from Schema 3

On the first start with Schema 4:

1. `seuchenweber.json.schema-v3.bak` is created once and without overwriting an existing file;
2. unknown operator keys are retained;
3. only missing Plague Breath values are supplemented with `8.0` blocks and `2.0` seconds;
4. the file is replaced atomically;
5. a subsequent start does not perform a second migration.
