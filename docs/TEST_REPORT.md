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
| Endless Elite Core | 3 | 0 | 0 | 0 |
| EndlessBook | 22 | 0 | 0 | 0 |
| Hymann | 11 | 0 | 0 | 0 |
| Nachtweber | 69 | 0 | 0 | 0 |
| Seuchenweber | 87 | 0 | 0 | 0 |
| Rift Mage Dungeon | 17 | 0 | 0 | 0 |
| **Gesamt** | **209** | **0** | **0** | **0** |

Reactor Summary: Parent und alle sechs Module `SUCCESS`.
Marker: `BUILD SUCCESS`.

## Repository- und Distributionsgates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=5 java_fqcns=169 unique_resource_paths=87 rift_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Buildverifizierte Artefakte:

| Artefakt | Bytes | SHA-256 |
|---|---:|---|
| EndlessEliteCore.jar | 20.433 | `f665aad16b8a476c8852b03ecf8d38a24c44824a47f3550b4e26e008e698c1d6` |
| EndlessBook.jar | 82.954 | `b0271c9d77c3bcb20b57e28f7324e2b949e994a1fdeebf9ee9b7bb4cbaa757a6` |
| Hymann.jar | 350.355 | `e9b73f079fecb00b63bded4fd3eac97b3f84f82989e50deb3a2e66c7ce187035` |
| Nachtweber.jar | 165.175 | `f09d3535458d67c104a98d3a953c3b900f7b39c85a2ddaa223d9d273297d02fb` |
| Seuchenweber.jar | 133.057 | `75a92c9fb199ce0e8d162aa7e9b97c6f3e5cf16d2440ddcd6a54322822d90ea3` |

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
