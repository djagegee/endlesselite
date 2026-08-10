# Rift Mage Dungeon

Integrated alpha contract for the Endless Elite flagship dungeon, **Rift Mage: Shattered Technodistrict**.

## Current state

The project validates `import.json` directly with `ImportManifest` from the active EndlessLeveling `11.6.1` JAR and validates all native references against the active Hytale `Assets.zip`.

The contract artifact now contains one integrated content package:

- additive `EndlessElite_RiftMage` boss role with DE/EN localization;
- additive `EndlessElite_RiftMage_Portal` using Hytale's proven `PortalKey` contract;
- additive `EndlessElite_RiftMage_Portal` PortalType routed only to the custom instance;
- `EndlessElite_RiftMage` Hytale instance template with unique UUID, deterministic seed `2026080801` and measured surface spawn `[0.5, 89.0, 0.5]`;
- complete mutable instance-resource baseline;
- additive `EndlessElite_RiftMage_Technodistrict` world structure;
- native `ScifiBlockLandscape` biome reference for the procedural destroyed-technology terrain;
- three-wave encounter ending with one `EndlessElite_RiftMage` boss authority, resolved by EL from the installed asset pack's `waves/` root;
- EndlessLeveling instance routing, wave HUD and floor/time-lock configuration;
- reference-compatible `dungeons_entry["instance-endless_elite_rift_mage-*"]` registration;
- a dedicated imported-dungeon gear card under EL's `assets/UI/Custom/Pages/Dungeons/Cards/Imports/Gear/` contract;
- EL's native immediate ENTER/teleport route to `EndlessElite_RiftMage`.

## Native ownership and scaling

All authored assets use their own `EndlessElite_*` IDs. Native Hytale assets are referenced or extended and never overwritten:

- `ScifiBlockLandscape` provides the procedural technology terrain;
- `Eye_Void`, `Spectre_Void` and `Outlander_Sorcerer` provide native encounter roles;
- public `Template_Trork_Mage`, `Necromancer_Void`, `Weapon_Spellbook_Demon` and the proven Outlander ranged sequence underpin the Rift Mage role;
- native portal-key icon, shard model and texture underpin portal activation.

The wave contract intentionally defines no health or damage multiplier. `mob_scaling_owner` is fixed to `EndlessEliteMobs`, the sole owner of general mob scaling; EndlessLeveling supplies progression/import behavior only.

## Fail-closed alpha gate

The package has passed isolated import, boot, real instance-generation and block-level spawn-safety validation, but is **not release-ready and not approved for deployment** until manual client acceptance:

```json
{
  "release_ready": false,
  "deployment_allowed": false
}
```

Remaining gate:

1. obtain manual visual and gameplay acceptance for terrain/arena appearance, three waves, Rift Mage combat, death and floor-fallback flow.

Dungeon-menu teleport is enabled through EndlessLeveling's own imported-dungeon path. Matchmaking, XP banking and reward payout remain disabled. The portal key has no recipe and is `Technical`, so it is not part of normal progression while the gate is closed.

Do not release or permanently deploy the contract while `deployment_allowed` is false. A controlled pending-import copy for the explicit manual validation gate requires user approval, backup and hash verification.

## Verification evidence

- `mvn -q clean verify`: PASS, 17 tests, 0 failures/errors/skips;
- contract ZIP `0.1.1`: 20 files, SHA-256 `549e1aa4d55e75e7c03dbeea7a887a3bda2d36686ac1134d0a9917708454362d`;
- known-good reference comparison: `outlander-warchief-boss (1).zip`; corrected world-pattern-keyed `dungeons_entry`, imported gear-card asset branch and runtime-resolvable packroot wave path;
- independent manifest, ZIP, native-reference, portal, instance and wave verification: PASS;
- isolated Hytale asset/WorldGen validation found and enabled correction of the boss's invalid nested-variant reference;
- after correction, no project-specific NPC, item, portal or WorldGen error remained;
- isolated EndlessLeveling `11.6.1` auto-import: PASS (`1 installed`, `0 errored`, 16 pack-server files);
- isolated EL + EndlessEliteMobs `3.5.4` boot: PASS; import pack loaded, no Rift-specific error and no asset-validation shutdown;
- repeat boot recognized the unchanged import state (`0 pending`, `1 installed`, `0 updates`, `0 errored`);
- structured evidence: `docs/isolated-runtime-validation-2026-08-07.json`;
- real isolated `InstancesPlugin.spawnInstance("EndlessElite_RiftMage", ...)`: PASS on `127.0.0.1:5534`;
- RED found seed-dependent terrain heights `112` and `81` under fixed spawn `Y=120`; deterministic seed `2026080801` produced repeatable surface `Y=88`;
- final block-level spawn probe: floor `Rock_Stone` at `Y=88`, feet/head `Empty` at `Y=89/90`, `safe=true`, instance cleanup requested and no project-specific error;
- spawn, portal override, floor fallback and arena center now share measured point `[0.5, 89.0, 0.5]`;
- structured spawn evidence: `docs/isolated-spawn-validation-2026-08-07.json`;
- isolated EndlessLeveling `11.6.1` server-registry and immediate-teleport contract probe: PASS on `127.0.0.1:5535`; `ImportRegistry`, `DungeonCardInfo`, installed asset pack, teleport permission and `InstanceDungeonDefinition` all resolved to `endless-elite-rift-mage` / `EndlessElite_RiftMage`; this is not client-render evidence;
- structured Dungeon-tab evidence: `docs/isolated-dungeon-tab-validation-2026-08-08.json`;
- real authenticated client validation on `127.0.0.1:5520`: PASS; Agegee confirmed the visible registered Dungeon entry, native start route, spawn point and leave/return; server log independently confirms `/el import install endless-elite-rift-mage`, `/el reload`, instance creation with seed `2026080801`, arrival at `[0.5, 89.0, 0.5]` and instance removal;
- structured manual client evidence: `docs/manual-dungeon-tab-validation-2026-08-08.json`;
- Hytale's instance-validation phase remains non-dispositive on this Windows build because every native instance, including `Default`, `Default_Flat` and `Forgotten_Temple`, fails identically with `IllegalArgumentException: Invalid path`.
