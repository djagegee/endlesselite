# Portal Spawn.Endless Elite – Source Snapshot

Read-only snapshot of the locally preserved Hytale editor export named `Portal Spawn.Endless Elite`. The original remains outside this public repository.

The two distinct prefab JSON files and the original manifest are preserved byte-for-byte. The original `.bak` file is byte-identical to `spawn.prefab.json`; its SHA-256 and size are retained in `SOURCE_SNAPSHOT.json` instead of committing duplicate 7.7 MB content.

## Gate

This is **not a deployable plugin release**:

- the source manifest has no `Version`;
- it has no `Main`;
- it declares `IncludesAssetPack=false` despite containing prefab assets;
- no isolated load/gameplay validation exists for this exact snapshot.

`release-gate.json` therefore remains `deployment_allowed=false`. `mvn verify` validates JSON, source hashes, manifest defects and the generated contract ZIP without modifying the original export.
