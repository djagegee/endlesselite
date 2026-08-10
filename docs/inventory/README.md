# Mod Inventory

Audit: 2026-08-10

## Complete Source and Content Modules

| Module | POM Version | Manifest/Source Version | Main Java | Test Java | Non-Java Source Files | Status |
|---|---:|---:|---:|---:|---:|---|
| Endless Elite Core | `0.1.0-SNAPSHOT` | `0.1.0` | 7 | 1 | 5 | shared runtime/asset owner; newly created from integration contracts |
| EndlessBook | `2.0.0-SNAPSHOT` | `2.0.0` | 17 | 9 | 14 | complete server plugin source module |
| Hymann | `0.1.9` | `0.1.9` | 34 | 2 | 46 | complete server plugin source module |
| Nachtweber | `0.1.0-SNAPSHOT` | `0.1.0` | 74 | 13 | 2 | complete server plugin source module |
| Seuchenweber | `0.1.0-SNAPSHOT` | `0.1.0` | 37 | 34 | 25 | complete server plugin source module; generated patcher `.class` files excluded |
| RiftMageDungeon | `0.1.1-SNAPSHOT` | Contract `0.1.1` | 0 | 6 | 21 | data-driven content/contract module; gate closed |
| Portal Spawn | `0.0.0-SNAPSHOT` | unversioned original export | 0 | 2 | 5 | two prefabs preserved byte-for-byte; identical `.bak` documented only by SHA; gate closed |
| MjolnirSafetyPatch | `1.0.0-SNAPSHOT` | `1.0.0` | 0 | 2 | 5 | complete first-party Patchly asset patch; no third-party assets; gate closed |

The four language paths that previously conflicted between Hymann and Seuchenweber exist exclusively in Shared Core as a conflict-free union of keys. Original projects and source backups were not modified.

## First-Party Binary/Backup Versions

72 distinct first-party JAR contents were deduplicated by SHA-256. The binary files themselves are not added to Git. Details and all categorized locations are listed in [`mod-inventory.json`](mod-inventory.json).

### Documented Only as Binaries and Therefore Not Integrated as Production Source

- `HyGunsMMOCompat` 1.0.5–1.0.7
- `EndlessGuildsPatches` (manifest 1.0.1; differing filenames)
- `StarterkitChatter` 1.0.0–1.0.7

### Integration Rule

- Source modules are transferred into the monorepo from the latest complete source.
- Byte-exact content snapshots retain source hashes and remain fail-closed when runtime contracts are incomplete.
- Binary-only artifacts are documented, but are not blindly decompiled or mixed into production code without verified complete source and an API contract.
- Third-party JARs and assets remain local build/runtime dependencies and are excluded by `.gitignore`.
- Original projects and backups are neither modified nor deleted.
