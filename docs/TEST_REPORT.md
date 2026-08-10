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
| Portal Spawn Snapshot | 4 | 0 | 0 | 0 |
| **Gesamt** | **213** | **0** | **0** | **0** |

Reactor Summary: Parent und alle sieben Module `SUCCESS`.
Marker: `BUILD SUCCESS`.

## Repository- und Distributionsgates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=5 java_fqcns=169 unique_resource_paths=87 rift_deployment_allowed=false portal_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Buildverifizierte Artefakte:

| Artefakt | Bytes | SHA-256 |
|---|---:|---|
| EndlessEliteCore.jar | 20.514 | `8fa78362e4aa3a6cacdbbf51aa78f5258c2581cd70486bd2c50d2dc95c122c27` |
| EndlessBook.jar | 82.954 | `761e3ae05e46160e22a7c5ea3d3a81fd96f9fcd74d58df727c194f9ecd348026` |
| Hymann.jar | 350.355 | `2d100ddaa3c3a34755d558b8a88e687997bb1c4d57ee89d013f8498591f48cdd` |
| Nachtweber.jar | 165.175 | `f09d3535458d67c104a98d3a953c3b900f7b39c85a2ddaa223d9d273297d02fb` |
| Seuchenweber.jar | 133.057 | `038f0a1f4227d0bc4d5b602b8890bb10ecfa6d765528acedf553f0731be3ac22` |

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
