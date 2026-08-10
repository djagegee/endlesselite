# Endless Elite – Konfiguration

Stand: Schema `4`, aktuell implementierter Bereich: **Seuchenweber**.

Die ausgelieferte Runtimekonfiguration liegt derzeit unter `Seuchenweber/src/main/resources/config/seuchenweber.json`. JSON unterstützt keine Kommentare; deshalb ist diese Datei die verbindliche Betreiberreferenz. Die YAML-Dateien unter `config/examples/` sind lesbare Profile für die spätere zentrale Endless-Elite-Konfiguration.

## Zahlen und Einheiten

- Dezimaltrennzeichen ist immer ein Punkt, unabhängig von der Systemsprache.
- `1.0 = 100 %`, `0.9 = 90 %`, `0.75 = 75 %`, `0.5 = 50 %`, `0.1 = 10 %`.
- Zeiten werden öffentlich ausschließlich in **Sekunden** gespeichert.
- Entfernungen und Radien werden in **Blöcken** angegeben.
- Stapel, Ziele, Mana und Schwellenwerte sind ganze Anzahlen.
- Automatisch geschriebene Dezimalwerte werden auf höchstens zwei sinnvolle Nachkommastellen gerundet.
- Änderungen werden derzeit nach einem Plugin-/Serverneustart aktiv; Hot-Reload ist noch nicht freigegeben.

## Allgemeine und Nekrotoxin-Einstellungen

| Konfigurationspfad | Beschreibung | Einheit | Standard | Minimum | Maximum | Kleinere/größere Werte | Aktivierung | Scope |
|---|---|---:|---:|---:|---:|---|---|---|
| `schemaVersion` | Version des Konfigurationsschemas; nicht manuell zurücksetzen. | Version | `4` | `4` | `4` | Nicht als Balancewert verwenden. | Neustart | Server |
| `necrotoxin.durationSeconds` | Dauer eines Nekrotoxin-Auftrags. | Sekunden | `10.0` | `0.05` | `300.0` | Kleiner: kürzere DoTs; größer: längere DoTs. | Neustart | PvE/PvP |
| `necrotoxin.tickIntervalSeconds` | Abstand zwischen DoT-Ticks. | Sekunden | `1.0` | `0.05` | `60.0` | Kleiner: häufigere Ticks/mehr Last; größer: seltenere Ticks. | Neustart | PvE/PvP |
| `necrotoxin.maximumStacksPerOwner` | Maximale Stacks je Ziel und wirkendem Spieler. | Anzahl | `5` | `1` | `16` | Kleiner: weniger Skalierung; größer: mehr DoT-Potenzial. | Neustart | PvE/PvP |
| `necrotoxin.baseDamagePerTick` | Grundschaden je Stack und Tick. | Schaden | `6.0` | `0.0` | `1000.0` | Kleiner: schwächerer DoT; größer: stärkerer DoT. | Neustart | PvE/PvP |
| `necrotoxin.bossDamageMultiplier` | Nekrotoxinschaden gegen Bosse. | Faktor | `0.7` | `0.0` | `2.0` | Kleiner reduziert, größer erhöht Bossschaden. | Neustart | PvE |
| `necrotoxin.eliteDamageMultiplier` | Nekrotoxinschaden gegen Elitegegner. | Faktor | `0.95` | `0.0` | `2.0` | Kleiner reduziert, größer erhöht Eliteschaden. | Neustart | PvE |
| `necrotoxin.pvpPlayerDamageMultiplier` | Nekrotoxinschaden gegen Spieler. | Faktor | `0.65` | `0.0` | `2.0` | Kleiner reduziert, größer erhöht Spielerschaden. | Neustart | PvP |
| `necrotoxin.pvpSummonDamageMultiplier` | Nekrotoxinschaden gegen Spielerkreaturen. | Faktor | `0.75` | `0.0` | `2.0` | Kleiner reduziert, größer erhöht Schaden. | Neustart | PvP |
| `necrotoxin.allowPvp` | Erlaubt Nekrotoxin gegen Spieler. | Boolean | `false` | – | – | `false` deaktiviert PvP-Anwendung. | Neustart | PvP |
| `necrotoxin.allowBosses` | Erlaubt Nekrotoxin gegen Bosse. | Boolean | `true` | – | – | `false` schließt Bosse aus. | Neustart | PvE |
| `necrotoxin.maximumOwnerEntries` | Sicherheitslimit gleichzeitig gespeicherter Owner-Zustände. | Anzahl | `256` | `1` | `4096` | Kleiner spart Speicher; größer erlaubt mehr gleichzeitige Zustände. | Neustart | Server/Performance |
| `necrotoxin.overdueTickPolicy` | Verhalten bei verspäteten Ticks; muss `single_tick_no_replay` bleiben. | ID | `single_tick_no_replay` | – | – | Verhindert nachgeholte Schadensbursts. | Neustart | Server/Performance |
| `necrotoxin.rejectDotProcRecursion` | Verhindert DoT-Auslösungsrekursion. | Boolean | `true` | – | – | `false` ist nicht releasefreigegeben. | Neustart | PvE/PvP |
| `necrotoxin.rejectReflectionRecursion` | Verhindert reflektierte Rekursion. | Boolean | `true` | – | – | `false` ist nicht releasefreigegeben. | Neustart | PvE/PvP |

## Aktive Fähigkeiten

| Konfigurationspfad | Beschreibung | Einheit | Standard | Minimum | Maximum | Kleinere/größere Werte | Aktivierung | Scope |
|---|---|---:|---:|---:|---:|---|---|---|
| `abilities.seal_of_decay.manaCost` | Manakosten von Siegel der Fäulnis. | Mana | `25` | `0` | `1000` | Kleiner: häufiger nutzbar; größer: teurer. | Neustart | PvE/PvP |
| `abilities.seal_of_decay.cooldownSeconds` | Abklingzeit von Siegel der Fäulnis. | Sekunden | `8.0` | `0.5` | `300.0` | Kleiner: häufiger; größer: seltener. | Neustart | PvE/PvP |
| `abilities.seal_of_decay.baseDamage` | Direkter Initialschaden. | Schaden | `18.0` | `0.0` | `1000.0` | Kleiner reduziert, größer erhöht Direktschaden. | Neustart | PvE/PvP |
| `abilities.seal_of_decay.nekrotoxinStacksApplied` | Aufgetragene Nekrotoxinstacks. | Anzahl | `2` | `1` | `16` | Kleiner: weniger DoT; größer: mehr DoT. | Neustart | PvE/PvP |
| `abilities.seal_of_decay.castRangeBlocks` | Maximale Zielreichweite. | Blöcke | `22.0` | `1.0` | `100.0` | Kleiner: näher; größer: weiter. | Neustart | PvE/PvP |
| `abilities.astral_rift.manaCost` | Manakosten von Astralriss. | Mana | `35` | `0` | `1000` | Kleiner: günstiger; größer: teurer. | Neustart | PvE/PvP |
| `abilities.astral_rift.cooldownSeconds` | Abklingzeit von Astralriss. | Sekunden | `14.0` | `0.5` | `300.0` | Kleiner: häufiger; größer: seltener. | Neustart | PvE/PvP |
| `abilities.astral_rift.teleportRangeBlocks` | Maximale Blinkreichweite. | Blöcke | `12.0` | `1.0` | `100.0` | Kleiner: kürzer; größer: weiter. | Neustart | PvE/PvP |
| `abilities.astral_rift.riftRadiusBlocks` | Radius eines Risspulses. | Blöcke | `5.0` | `0.5` | `32.0` | Kleiner: weniger Fläche; größer: mehr Ziele/Last. | Neustart | PvE/PvP |
| `abilities.astral_rift.durationSeconds` | Lebensdauer des Risses. | Sekunden | `5.0` | `0.05` | `60.0` | Kleiner: weniger Pulse; größer: mehr Pulse. | Neustart | PvE/PvP |
| `abilities.astral_rift.pulseIntervalSeconds` | Zeitabstand der Risspulse. | Sekunden | `1.0` | `0.05` | `10.0` | Kleiner: mehr Last; größer: weniger Pulse. | Neustart | PvE/PvP |
| `abilities.astral_rift.nekrotoxinStacksPerPulse` | Stacks je betroffenem Ziel und Puls. | Anzahl | `1` | `1` | `16` | Kleiner: weniger DoT; größer: mehr DoT. | Neustart | PvE/PvP |
| `abilities.astral_rift.maximumTargetsPerPulse` | Zielobergrenze je Puls; Code-Hardcap ist 16. | Anzahl | `8` | `1` | `16` | Kleiner: weniger Last; größer: mehr Treffer. | Neustart | PvE/PvP/Performance |
| `abilities.chronoblight.manaCost` | Manakosten von Chronofäule. | Mana | `45` | `0` | `1000` | Kleiner: günstiger; größer: teurer. | Neustart | PvE/PvP |
| `abilities.chronoblight.cooldownSeconds` | Abklingzeit von Chronofäule. | Sekunden | `18.0` | `0.5` | `300.0` | Kleiner: häufiger; größer: seltener. | Neustart | PvE/PvP |
| `abilities.chronoblight.baseDamage` | Direkter Initialschaden. | Schaden | `12.0` | `0.0` | `1000.0` | Kleiner reduziert, größer erhöht Direktschaden. | Neustart | PvE/PvP |
| `abilities.chronoblight.nekrotoxinStacksApplied` | Zusätzliche Nekrotoxinstacks. | Anzahl | `2` | `1` | `16` | Kleiner: weniger DoT; größer: mehr DoT. | Neustart | PvE/PvP |
| `abilities.chronoblight.castRangeBlocks` | Maximale Zielreichweite. | Blöcke | `20.0` | `1.0` | `100.0` | Kleiner: näher; größer: weiter. | Neustart | PvE/PvP |
| `abilities.chronoblight.fullMarkStunSeconds` | Optionale Kontrolle bei voller Marke; derzeit API-geblockt. | Sekunden | `0.75` | `0.0` | `10.0` | Kleiner: kürzer; größer: länger. | Neustart | PvE/PvP |

## Passive Fähigkeiten und Schadensbudget

| Konfigurationspfad | Beschreibung | Einheit | Standard | Minimum | Maximum | Kleinere/größere Werte | Aktivierung | Scope |
|---|---|---:|---:|---:|---:|---|---|---|
| `passives.necrotoxic_mastery.tickDamageMultiplier` | DoT-Multiplikator der Meisterschaft. | Faktor | `1.25` | `0.0` | `3.0` | `1.0` unverändert; größer verstärkt. | Neustart | PvE/PvP |
| `passives.necrotoxic_mastery.auraRadiusBlocks` | Pesthauch-Radius um den aktiven Seuchenweber. | Blöcke | `8.0` | `0.5` | `32.0` | Kleiner: engere Aura; größer: mehr mögliche PvE-Ziele und Last. | Neustart | PvE/Performance |
| `passives.necrotoxic_mastery.auraPulseIntervalSeconds` | Abstand zwischen Pesthauch-Impulsen. | Sekunden | `2.0` | `0.25` | `30.0` | Kleiner: häufigere Stapel/mehr Last; größer: langsamere Stapelvergabe. | Neustart | PvE/Performance |
| `passives.astral_echo.maximumEchoTargetsPerTick` | Maximale Echoziele pro Tick. | Anzahl | `1` | `0` | `16` | Kleiner reduziert, größer erweitert Echos. | Neustart | PvE/PvP/Performance |
| `passives.astral_echo.echoRadiusBlocks` | Reichweite des Echos. | Blöcke | `5.0` | `0.0` | `32.0` | Kleiner: enger; größer: mehr Reichweite/Last. | Neustart | PvE/PvP |
| `passives.astral_echo.echoStacksApplied` | Nekrotoxinstacks pro Echo. | Anzahl | `1` | `1` | `16` | Kleiner: weniger DoT; größer: mehr DoT. | Neustart | PvE/PvP |
| `passives.soul_diagnosis.diagnosisStackThreshold` | Stackschwelle für Seelendiagnose. | Anzahl | `3` | `1` | `16` | Kleiner: früher; größer: später. | Neustart | PvE/PvP |
| `passives.relic_attunement.manaRefundAmount` | Mana-Rückerstattung. | Mana | `8` | `0` | `1000` | Kleiner: weniger; größer: mehr Rückgabe. | Neustart | PvE/PvP |
| `passives.relic_attunement.cooldownRefundSeconds` | Abklingzeit-Rückerstattung. | Sekunden | `1.2` | `0.0` | `60.0` | Kleiner: weniger; größer: stärkere Rückerstattung. | Neustart | PvE/PvP |
| `damageBudget.representativeDirectDamage` | Modellierter Direktschaden für Balanceprüfung. | Schaden | `30.0` | `0.0` | `100000.0` | Dient nur dem Balancecontract. | Neustart | QA |
| `damageBudget.representativeDotDamage` | Modellierter DoT-Schaden für Balanceprüfung. | Schaden | `120.0` | `0.0` | `100000.0` | Dient nur dem Balancecontract. | Neustart | QA |
| `damageBudget.minimumDotShare` | Mindestanteil des DoT-Schadens. | Faktor | `0.7` | `0.7` | `1.0` | Größer erzwingt stärkere DoT-Dominanz. | Neustart | QA/PvE/PvP |

## Fehlerverhalten

Ungültige Werte werden nicht still gespeichert. Der Loader nennt den betroffenen Schlüssel und lehnt nicht-endliche Zahlen, Dezimalkommas, negative Zeiten sowie nicht-ganzzahlige Anzahlen ab. Schema 3 wird einmalig und backup-first zu Schema 4 migriert: unbekannte Schlüssel bleiben erhalten, fehlende Pesthauch-Werte werden mit `8.0` Blöcken und `2.0` Sekunden ergänzt, und ein zweiter Lauf verändert die Datei nicht erneut.
