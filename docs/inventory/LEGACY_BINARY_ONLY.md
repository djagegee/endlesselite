# Legacy- und Binär-only-Modfamilien

Audit: 2026-08-10

## Vollständige aktuelle Quellen vorhanden

| Familie | Aktuelle Quelle | Integrationsstatus |
|---|---|---|
| EndlessBook | 2.0.0 | bereit |
| Hymann | 0.1.9 | bereit |
| Nachtweber | 0.1.0 | bereit; reale Ingame-Abnahme bleibt separat offen |
| Seuchenweber | 0.1.0 | bereit |
| Rift Mage Dungeon | 0.1.1 | datengetriebenes Content-/Contract-Modul; bereit mit dokumentierten Runtime-Grenzen |

## Nur Binärbackup gefunden

| Familie | gefundene Versionen | Entscheidung |
|---|---|---|
| HyGunsMMOCompat | 1.0.5, 1.0.6, 1.0.7 | nicht produktiv integrieren: keine vollständige Quelle gefunden; JARs bleiben externe Recovery-Evidenz |
| EndlessGuildsPatches | Manifest 1.0.1 (Dateiname teils 1.0.0) | nicht produktiv integrieren: Versionsabweichung und fehlende Quelle |
| StarterkitChatter | 1.0.0–1.0.7 | nicht produktiv integrieren: keine vollständige Quelle gefunden |
| MjolnirSafetyPatch | unversioniertes archiviertes Contentfragment | nicht produktiv integrieren: nur historisches Manifest-/Assetfragment |

## Buildwerkzeuge

- Gepatchte MMOSkillTree-Artefakte sind keine eigenständigen Endless-Elite-Gameplaymodule.
- Die zugehörigen Patcher-Quellen werden mit Nachtweber beziehungsweise Seuchenweber erhalten.
- Gepatchte oder fremde JARs werden nicht in Git eingecheckt.

## Recovery-Regel

Eine spätere Wiederherstellung aus einem Binär-only-Artefakt erfolgt nur in einem separaten Recovery-Slice: Hash/Manifest sichern, API-Vertrag gegen die installierten JARs prüfen, rekonstruierte Quelle klar kennzeichnen, Tests neu schreiben und erst nach Build-/Runtime-Gates integrieren. Die vorhandenen Original-JARs und Backups werden nicht gelöscht.
