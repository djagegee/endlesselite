# Mod-Inventur

Audit: 2026-08-10

## Vollständige Quellmodule

| Modul | POM-Version | Manifest-Version | Main-Java | Test-Java | Ressourcen | Status |
|---|---:|---:|---:|---:|---:|---|
| EndlessBook | `2.0.0-SNAPSHOT` | `2.0.0` | 17 | 9 | 14 | vollständiges Serverplugin-Quellmodul |
| Hymann | `0.1.9` | `0.1.9` | 34 | 2 | 50 | vollständiges Serverplugin-Quellmodul |
| Nachtweber | `0.1.0-SNAPSHOT` | `0.1.0` | 74 | 13 | 2 | vollständiges Serverplugin-Quellmodul |
| Seuchenweber | `0.1.0-SNAPSHOT` | `0.1.0` | 37 | 34 | 29 | vollständiges Serverplugin-Quellmodul; 4 erzeugte Patcher-`.class`-Dateien ausgeschlossen |
| RiftMageDungeon | `0.1.1-SNAPSHOT` | `—` | 0 | 6 | 9 | datengetriebenes Content-/Contract-Modul |

## Eigene Binär-/Backupfassungen

72 unterschiedliche eigene JAR-Inhalte wurden per SHA-256 dedupliziert. Binärdateien selbst werden nicht in Git übernommen. Details und alle kategorisierten Fundstellen stehen in [`mod-inventory.json`](mod-inventory.json).

### Integrationsregel

- Quellmodule werden aus der neuesten vollständigen Quelle in das Monorepo kopiert.
- Binär-only-Artefakte werden dokumentiert, aber ohne belegte vollständige Quelle und API-Vertrag nicht blind dekompiliert oder in produktiven Code gemischt.
- Drittanbieter-JARs bleiben lokale Build-/Runtime-Abhängigkeiten und sind durch `.gitignore` ausgeschlossen.
- Originalprojekte und Backups werden weder verändert noch gelöscht.
