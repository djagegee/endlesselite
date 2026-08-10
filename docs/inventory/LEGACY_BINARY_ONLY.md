# Legacy and Binary-Only Mod Families

Audit: 2026-08-10

## Complete Current Sources Available

| Family | Current Source | Integration Status |
|---|---|---|
| EndlessBook | 2.0.0 | ready |
| Hymann | 0.1.9 | ready |
| Nachtweber | 0.1.0 | ready; actual in-game acceptance remains open separately |
| Seuchenweber | 0.1.0 | ready |
| Rift Mage Dungeon | 0.1.1 | data-driven content/contract module; ready with documented runtime boundaries |

## Binary Backup Only

| Family | Versions Found | Decision |
|---|---|---|
| HyGunsMMOCompat | 1.0.5, 1.0.6, 1.0.7 | do not integrate into production: no complete source found; JARs remain external recovery evidence |
| EndlessGuildsPatches | manifest 1.0.1 (filename sometimes 1.0.0) | do not integrate into production: version discrepancy and missing source |
| StarterkitChatter | 1.0.0–1.0.7 | do not integrate into production: no complete source found |

## Build Tools

- Patched MMOSkillTree artifacts are not standalone Endless Elite gameplay modules.
- The associated patcher sources are preserved with Nachtweber and Seuchenweber, respectively.
- Patched or third-party JARs are not checked into Git.

## Recovery Rule

Any future restoration from a binary-only artifact must take place in a separate recovery slice: preserve its hash/manifest, verify the API contract against the installed JARs, clearly identify reconstructed source, rewrite tests, and integrate only after the build/runtime gates pass. The existing original JARs and backups will not be deleted.
