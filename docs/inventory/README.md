# Mod-Inventur

Audit: 2026-08-10

## Vollständige Quell- und Contentmodule

| Modul | POM-Version | Manifest-/Quellversion | Main-Java | Test-Java | Nicht-Java-Quelldateien | Status |
|---|---:|---:|---:|---:|---:|---|
| Endless Elite Core | `0.1.0-SNAPSHOT` | `0.1.0` | 7 | 1 | 5 | gemeinsamer Runtime-/Asset-Owner; neu aus Integrationsverträgen erstellt |
| EndlessBook | `2.0.0-SNAPSHOT` | `2.0.0` | 17 | 9 | 14 | vollständiges Serverplugin-Quellmodul |
| Hymann | `0.1.9` | `0.1.9` | 34 | 2 | 46 | vollständiges Serverplugin-Quellmodul |
| Nachtweber | `0.1.0-SNAPSHOT` | `0.1.0` | 74 | 13 | 2 | vollständiges Serverplugin-Quellmodul |
| Seuchenweber | `0.1.0-SNAPSHOT` | `0.1.0` | 37 | 34 | 25 | vollständiges Serverplugin-Quellmodul; erzeugte Patcher-`.class`-Dateien ausgeschlossen |
| RiftMageDungeon | `0.1.1-SNAPSHOT` | Contract `0.1.1` | 0 | 6 | 21 | datengetriebenes Content-/Contract-Modul; Gate geschlossen |
| Portal Spawn | `0.0.0-SNAPSHOT` | unversionierter Originalexport | 0 | 2 | 5 | zwei Prefabs bytegenau erhalten; identische `.bak` nur per SHA dokumentiert; Gate geschlossen |
| MjolnirSafetyPatch | `1.0.0-SNAPSHOT` | `1.0.0` | 0 | 2 | 5 | vollständiger eigener Patchly-Assetpatch; keine Third-Party-Assets; Gate geschlossen |

Die vier früher zwischen Hymann und Seuchenweber kollidierenden Language-Pfade liegen als konfliktfreie Schlüsselunion ausschließlich im Shared Core. Originalprojekte und Quellbackups wurden nicht verändert.

## Eigene Binär-/Backupfassungen

72 unterschiedliche eigene JAR-Inhalte wurden per SHA-256 dedupliziert. Binärdateien selbst werden nicht in Git übernommen. Details und alle kategorisierten Fundstellen stehen in [`mod-inventory.json`](mod-inventory.json).

### Nur binär belegt und deshalb nicht als produktive Quelle integriert

- `HyGunsMMOCompat` 1.0.5–1.0.7
- `EndlessGuildsPatches` (Manifest 1.0.1; abweichende Dateinamen)
- `StarterkitChatter` 1.0.0–1.0.7

### Integrationsregel

- Quellmodule werden aus der neuesten vollständigen Quelle in das Monorepo übernommen.
- Bytegenaue Content-Snapshots erhalten Quellhashes und bleiben bei unvollständigen Runtime-Verträgen fail-closed.
- Binär-only-Artefakte werden dokumentiert, aber ohne belegte vollständige Quelle und API-Vertrag nicht blind dekompiliert oder in produktiven Code gemischt.
- Drittanbieter-JARs und -Assets bleiben lokale Build-/Runtime-Abhängigkeiten und sind durch `.gitignore` ausgeschlossen.
- Originalprojekte und Backups werden weder verändert noch gelöscht.
