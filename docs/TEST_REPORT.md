# Endless Elite – Testreport

Datum: 2026-08-10
Branch zum Testzeitpunkt: `feat/mod-consolidation-20260810`

## Toolchain

- Java 25.0.4 (Eclipse Adoptium)
- Maven 3.9.16
- Windows 11 / amd64

## Reproduzierte lokale Abhängigkeit

`python tools/setup_local_dependencies.py ...`

- Marker: `ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS`
- Stock-MMOSkillTree Input SHA-256: `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`
- Owned-Effects Output SHA-256: `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`
- Patchprüfung: genau ein geänderter JAR-Eintrag
- `registerIfAbsent`: PASS
- exakt-instanzgebundenes `unregister`: PASS
- lokale Maven-Installation: PASS

## Maven-Reactor

Befehl:

```bash
mvn clean verify
```

| Modul | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| EndlessBook | 22 | 0 | 0 | 0 |
| Hymann | 11 | 0 | 0 | 0 |
| Nachtweber | 69 | 0 | 0 | 0 |
| Seuchenweber | 87 | 0 | 0 | 0 |
| Rift Mage Dungeon | 17 | 0 | 0 | 0 |
| **Gesamt** | **206** | **0** | **0** | **0** |

Reactor Summary: Parent und alle fünf Module `SUCCESS`.
Marker: `BUILD SUCCESS`.

## Repository- und Distributionsgates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=4 java_fqcns=162 rift_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Buildverifizierte Artefakte:

| Artefakt | Bytes | SHA-256 |
|---|---:|---|
| EndlessBook.jar | 82.917 | `758ba3c1df721173918dfc4596a698af605066e660d929df90f810a51ed76db9` |
| Hymann.jar | 355.487 | `51b725cb39cf9ed7aa27631665eaf75cb7b75d83a03fdf828410a647888fcfc7` |
| Nachtweber.jar | 165.140 | `03bbb792159692f997bec46d9731c7a7b4f2ab2be38327147449e3ab4c4a67da` |
| Seuchenweber.jar | 137.810 | `6ee8dbd4542186a364f0695e42202d2e7af610b7093f00fea308b7ddb631bd0f` |

Das Distributionsmanifest setzt ausdrücklich:

- `deploymentApproved=false`
- `deploymentPerformed=false`
- `thirdPartyArtifactsBundled=false`

## Warnungen und offene Abnahme

Der Build meldet veraltete Hytale-API-Nutzungen in EndlessBook, Hymann und Seuchenweber. Sie sind technische Schulden, aber keine Testfehler.

Nicht durch diesen Lauf bewiesen:

- kombinierter Server-Cold-Boot,
- reale Client-/Ingame-Funktion,
- Nachtweber-Binding/Movement/Damage/Disconnect/Zwei-Spieler-Test,
- Hymann-Persistenz bei Profilwechsel/Crash,
- konfliktfreie globale MMOSkillTree-Writerreihenfolge,
- Rift-Mage-Gameplay-/Visualabnahme,
- Live-Deployment.
