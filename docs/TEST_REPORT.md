# Endless Elite – Testreport

Datum: 2026-08-10
Branch zum Testzeitpunkt: `integration/endless-elite-final-20260810`

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
| Mjolnir Safety Patch | 4 | 0 | 0 | 0 |
| **Gesamt** | **217** | **0** | **0** | **0** |

Reactor Summary: Parent und alle acht Module `SUCCESS`.
Marker: `BUILD SUCCESS`.

## Repository- und Distributionsgates

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
plugins=5 java_fqcns=169 unique_resource_paths=87 rift_deployment_allowed=false portal_deployment_allowed=false mjolnir_patch_deployment_allowed=false
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Buildverifizierte Artefakte:

| Artefakt | Bytes | SHA-256 |
|---|---:|---|
| EndlessEliteCore.jar | 20.628 | `adecbcf6573cd892256f37ab3a2c510f78bedcae66ba28a5cb5197a1d23e3f28` |
| EndlessBook.jar | 82.959 | `f22806686ef1845c2db4838cdb63ece3579ea3891a836f68f38d25907dab184a` |
| Hymann.jar | 350.362 | `cffa40c6f20d9b6e235a8406ad2c2828a2057662b6f2b0a3672a1ebd28b967e6` |
| Nachtweber.jar | 165.179 | `3c21ab251c3e1734fe83fc611ba0d74530a147efafaaa49721c08d9d2268e3e5` |
| Seuchenweber.jar | 133.062 | `227b8efb5a6afa67dbacaef856b25f10ca44200a441b1c3b2160a66f6828bdc7` |

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
- Portal-Spawn-Manifestreparatur und Gameplay-/Visualabnahme,
- Mjolnir-Safety-Patch-Abnahme mit Patchly und Starky's Mjolnir 1.6.1,
- Live-Deployment.
