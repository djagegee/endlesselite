# Seuchenweber – manuelle Client-Abnahme 2026-08-09

## Ergebnis

**PARTIAL / BLOCKED**

Alle Einzelspieler-, UI-, Gameplay-, Reconnect- und Profilisolationsprüfungen wurden im vollständigen lokalen 40-JAR-Modstack bestanden. Der verpflichtende echte Zwei-Spieler-Test konnte nicht ausgeführt werden, weil kein zweiter authentifizierter Hytale-Account verfügbar ist. Automatisierte Owner-/Store-Isolationstests bestehen, ersetzen diesen manuellen Multiplayer-Gate-Test aber nicht.

## Build und Deployment

- Projekt: `C:\Users\agege\Projects\EndlessElite\Seuchenweber`
- Befehl: `mvn.cmd -q clean verify` über Apache Maven 3.9.16 und Java 25
- Tests: 87
- Failures: 0
- Errors: 0
- Skipped: 0
- Core-Performance: p95 0.030700 ms, p99 0.045600 ms für 8 Owner × 16 Ziele
- JAR SHA-256: `f8281b5d3376adb33117f5fec14d9c8138cdd92951b1ce845009afe003c67e7b`
- Build- und Deployment-JAR: bytegleich
- Rollback: `C:\Users\agege\Desktop\LOKAL SERVER\backups\seuchenweber-client-gate-20260809-122154`

## Fullstack-Boot

- Launcher: `START-LOCAL-HYTALE-SERVER.cmd`
- Bind: `127.0.0.1:5520`
- Log: `C:\Users\agege\Desktop\LOKAL SERVER\logs\2026-08-09_12-22-02_server.log`
- OAuth: `OAUTH_STORE`, gespeicherte Session erfolgreich wiederhergestellt
- Boot: `Hytale Server Booted! [Multiplayer]` nach 3 min 29.848 s
- Plugin: `Shadow:Seuchenweber` aktiviert
- Klasse: `elite_plagueweaver` registriert
- Skilltree: `SEUCHENWEBER_MASTERY`, 38 Milestones, exakt 3 aktive und 4 passive Unlocks

## Manuelle PASS-Ergebnisse

Getesteter Spieler: Acehoroth (`121d54a3-1fee-46ac-b5af-c23fc39da804`).

1. Client verbindet authentifiziert und tritt der Welt `default` bei.
2. MMOSkillTree zeigt Seuchenweber-Kategorie, exakt sieben Karten, Icons, deutsche Texte und Level 2/10/25/40/55/70/90 korrekt.
3. Alle sieben Knoten sind vorhanden; die drei aktiven Fähigkeiten lassen sich binden.
4. EndlessLeveling zeigt Seuchenweber mit korrektem Cosmic-Ruin-Spellbook-Icon; Auswahl funktioniert.
5. Siegel der Fäulnis: Cast, Direktschaden, Nekrotoxinstapel, Giftvisual und DoT funktionieren.
6. Astralriss: Teleport, Rissdauer, Pulse und Gift funktionieren.
7. Chronofäule: Schaden, Stapel und kurzer Vollmarken-Stun funktionieren; kein Dauerstun.
8. Pesthauch: automatische Nekrotoxinstapel und Visuals im Radius funktionieren.
9. Astrales Echo: begrenzte Übertragung auf nahes Ziel funktioniert; keine Endlosschleife beobachtet.
10. Seelendiagnose: Markierung erscheint ab ausreichenden Stapeln und verschwindet nach Ablauf.
11. Reliktresonanz: Mana- und Cooldown-Erstattung beim natürlichen Ablauf voller Marken sichtbar.
12. Cosmic Ruin Spellbook gewährt exakt +65 `SEUCHENWEBER_MASTERY` XP.
13. Reconnect erhält gewählte Klasse, sieben Claims, drei Bindings sowie Level/XP.
14. Profilwechsel A→B→A erhält getrennte Zustände und stellt Profil A vollständig wieder her.
15. Alle drei Seuchenweber-XP-Shopstufen zeigen ihre Detailbeschreibung. Die Content-Audit-Warnung `UNLOCALIZED_DESC` ist für diese Einträge ein staler Validatorbefund.

## Visuelle Evidenz

Screenshot:

`C:\Users\agege\Pictures\Hytale Screenshots\Hytale2026-08-09_12-28-26.png`

Klar sichtbar:

- laufender Hytale-Client in der Welt `Seedling Woods`,
- ausgewählte Klasse `Seuchenweber`,
- Prestigeanzeige `P30`,
- drei belegte aktive Slots `A1`, `A2`, `A3`,
- drei unterscheidbare grüne Ability-Icons,
- Zielanzeige `[Lv. 7] … [24/61]`,
- grüner Statuseffekt am Ziel,
- aktives Kampf-/Schadens-HUD mit `Damage: 37` und `DPS: 3.8`.

Der Screenshot dient als UI-/Visualnachweis. Er belegt für sich allein weder serverseitige Ticklogik noch Zwei-Spieler-Owner-Isolation.

## Offener Multiplayer-Gate-Test

Noch erforderlich:

1. Zwei authentifizierte Spieler gleichzeitig verbinden.
2. Beide verwenden Seuchenweber gegen dasselbe Ziel.
3. Owner-getrennte Nekrotoxinstapel, Visualtier, Diagnose, Echo, natürliche Expiration und Schadenszuordnung prüfen.
4. PvP bleibt laut aktiver Konfiguration deaktiviert (`allowPvp=false`).
5. Ein Spieler loggt während aktivem Nekrotoxin/Riss aus; der andere bleibt verbunden.
6. Weltwechsel und Rückkehr mit beiden Spielern prüfen.
7. Keine fremden Marks, Rewards, Cooldowns oder Visuals dürfen übernommen werden.

## Bekannte Fremdmodprobleme im Fullstack

Nicht Seuchenweber zugeordnet:

- The Forerunner 1.0.1: ungültiger `DispatchEventPhase`, fehlgeschlagene Golem-Builder und `CustomUIHud`-`NoSuchMethodError` bei jedem Spielerjoin.
- CreditAsset wird durch Drittmods doppelt registriert.
- Starky's RayGun/ThunderGun registrieren Commands mit ungültigen Permission-Nodes.
- Void Asylum enthält einen ungültigen NPC-Motion-Controller.
- MmoMobScaling versucht `ScaledMobComponent` mehrfach hinzuzufügen und erzeugt starke Warnungsflut.
- EndlessFates deaktiviert Fate Zones, weil Welt `fatezones` fehlt.

Diese Fehler müssen separat bearbeitet werden; sie verhinderten die geprüften Seuchenweber-Funktionen nicht, bedeuten aber, dass der Gesamt-Modstack nicht als fehlerfrei bezeichnet werden darf.
