# Seuchenweber-Konfiguration – Schema 4

Aktive Operator-Datei nach dem ersten Serverstart:

```text
mods/Seuchenweber/seuchenweber.json
```

Änderungen werden beim nächsten vollständigen Serverstart aktiv. Ungültige Werte werden nicht still überschrieben: Die Runtime nennt den vollständigen Pfad, den erhaltenen Wert, den gültigen Bereich und verwendet für diesen Start den sicheren Standardwert.

## Pesthauch

| Pfad | Einheit | Standard | Minimum | Maximum | Wirkung | Bereich |
|---|---:|---:|---:|---:|---|---|
| `passives.necrotoxic_mastery.auraRadiusBlocks` | Blöcke | `8.0` | `0.5` | `32.0` | Größer erfasst weiter entfernte feindliche Kampf-NPCs; kleiner begrenzt die Nähe-Aura stärker. | PvE; PvP bleibt durch die bestehende Zielprüfung ausgeschlossen. |
| `passives.necrotoxic_mastery.auraPulseIntervalSeconds` | Sekunden | `2.0` | `0.25` | `30.0` | Kleiner vergibt Stapel häufiger; größer verlangsamt die Stapelvergabe. | Pro aktivem Seuchenweber und World-Store. |

Pesthauch vergibt pro Impuls weiterhin **genau einen ownergebundenen Nekrotoxinstapel je gültigem Ziel**. Stapelzahl, Schaden und Partikel werden absichtlich nicht hier dupliziert:

- `NekrotoxinDamageSystem` bleibt die einzige Schadensautorität.
- Die visuelle Klassen-Aura bleibt schadensfrei und unabhängig vom Pesthauch-Takt.
- Zielpartikel bleiben im vorhandenen Poison-Visual-Pfad.

## Migration von Schema 3

Beim ersten Start mit Schema 4:

1. wird `seuchenweber.json.schema-v3.bak` einmalig und nicht überschreibend angelegt;
2. bleiben unbekannte Operator-Schlüssel erhalten;
3. werden nur fehlende Pesthauch-Werte mit `8.0` Blöcken und `2.0` Sekunden ergänzt;
4. wird die Datei atomar ersetzt;
5. führt ein weiterer Start keine zweite Migration aus.
