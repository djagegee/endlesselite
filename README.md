# Endless Elite

Gemeinsames, modular aufgebautes Quellrepository für die von Agegee und Hermes entwickelten Hytale-Mods.

[![Java 25](https://img.shields.io/badge/Java-25-blue)](#build-und-tests)
[![Tests](https://img.shields.io/badge/tests-213%20passing-brightgreen)](#build-und-tests)

> [!IMPORTANT]
> Das Repository ist buildverifiziert, aber **nicht live-deploymentfreigegeben**. Es wurde kein Live-Server verändert. Nachtweber, der kombinierte MMOSkillTree-Stack und Rift Mage besitzen offene Ingame-/Release-Gates.

## Module

| Pfad | Version | Funktion | Status |
|---|---:|---|---|
| `modules/core` | 0.1.0 | gemeinsamer Lifecycle-/Ownership-Katalog und konfliktfreie globale Lokalisierung | 3 Tests GREEN |
| `modules/hub/endless-book` | 2.0.0 | gemeinsamer UI-/Informationshub, persönliche Claims, Progressionsdarstellung | 22 Tests GREEN |
| `modules/classes/hymann` | 0.1.9 | Hymann-Klassenprogression, Armament-/Thunder-Fähigkeiten, Combat-Systeme | 11 Tests GREEN |
| `modules/classes/nachtweber` | 0.1.0 | Nachtweber mit drei aktiven und vier passiven Fähigkeiten, storegebundene Zustände | 69 Tests GREEN; Ingame-Gates offen |
| `modules/classes/seuchenweber` | 0.1.0 | Nekrotoxin, Astralriss, Chronofäule, Auren und MMOSkillTree-Anbindung | 87 Tests GREEN |
| `modules/content/rift-mage-dungeon` | 0.1.1 | Rift-Mage-Dungeon-Import, Wellen-/Assetverträge | 17 Tests GREEN; Deployment-Gate geschlossen |
| `modules/content/portal-spawn` | unversioniert | bytegenauer Recovery-Snapshot zweier Portal-/Spawn-Prefabs | 4 Tests GREEN; nicht deploybar |

## Gemeinsame Struktur

```text
Endless Elite/
├── config/examples/                  # gemeinsame lesbare Betreiberprofile
├── docs/
│   ├── architecture/                 # Konflikt- und Ownershipentscheidungen
│   └── inventory/                    # Quell-, Versions- und Binärevidenz
├── modules/
│   ├── core/                         # gemeinsamer Runtime-/Asset-Owner
│   ├── hub/endless-book/
│   ├── classes/{hymann,nachtweber,seuchenweber}/
│   └── content/{rift-mage-dungeon,portal-spawn}/
├── tools/                            # Verifier, Dependency-Reproduktion, Distribution
└── pom.xml                           # gemeinsamer Java-25/Maven-Reactor
```

Das Projekt erzeugt bewusst getrennte Hytale-Artefakte statt eines ungeprüften Monolith-JARs. Dadurch bleiben Plugin-IDs, Entry-Points, Ressourcen und Lifecycle-Owner eindeutig.

## Architektur und Ownership

- **Endless Elite Core** ist der gemeinsame Lifecycle-, Ownership- und globale Lokalisierungsowner.
- **EndlessBook** ist der gemeinsame UI-Hub.
- **EndlessLeveling** bleibt externer Owner für Klasse, Level und Prestige.
- **MMOSkillTree** bleibt externer Owner für Skills, Unlocks und Bindings.
- **EndlessEliteMobs** bleibt alleiniger Owner des allgemeinen Mob-Scalings.
- Klassenmodule besitzen nur ihre store-/ownergebundenen Kampfzustände.
- Rift Mage besitzt keinen eigenen Spielerprogress-Owner.
- Buildpfade und Betreiberprofile werden zentral vom Parent beziehungsweise `config/examples` verwaltet.

Details und harte Grenzen: [`docs/architecture/CONFLICT_MATRIX.md`](docs/architecture/CONFLICT_MATRIX.md).

## Konfiguration

Lesbare Beispielprofile:

- `endlesselite-default.yml`
- `endlesselite-balanced.yml`
- `endlesselite-easy.yml`
- `endlesselite-hard.yml`
- `endlesselite-pvp.yml`

Alle öffentlichen Zeiten stehen in Sekunden, Entfernungen in Blöcken und Faktoren als kurze Dezimalwerte (`1.0 = 100 %`). Vollständige Wertebereiche: [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

Die Runtime-Dateien der einzelnen Plugins bleiben modulbezogen, damit keine bestehenden Migrationen oder Datenverträge heimlich gebrochen werden.

## Build und Tests

### Voraussetzungen

- JDK 25
- Maven 3.9+
- lokale Hytale-Server-/Modabhängigkeiten außerhalb von Git

Nachtwebers additive MMOSkillTree-Abhängigkeit reproduzieren:

```bash
python tools/setup_local_dependencies.py \
  --server-root "C:/Pfad/zum/Hytale-Server" \
  --maven "C:/Pfad/zu/apache-maven/bin/mvn.cmd"
```

Gesamtbuild:

```bash
mvn -Dhytale.server.root="C:/Pfad/zum/Hytale-Server" clean verify
python tools/verify_repository.py
python tools/collect_distribution.py
```

Aktuell bestätigt:

- Maven-Reactor: **8/8 SUCCESS**
- Tests: **213**, 0 Failures, 0 Errors, 0 Skips
- lokale Dependency-Reproduktion: PASS
- Repository-Strukturgate: PASS
- Buildartefaktsammlung: PASS

Ausführliche Anleitung: [`docs/BUILD_AND_TEST.md`](docs/BUILD_AND_TEST.md).

## Installation

1. Zuerst Build und Repository-Gate ausführen.
2. `tools/collect_distribution.py` erzeugt unter `dist/plugins/` fünf eigene Plugin-JARs einschließlich Shared Core und ein Hashmanifest.
3. Drittanbieterabhängigkeiten werden nicht gebündelt und müssen in kompatiblen Versionen separat vorliegen.
4. **Nicht direkt auf einen Live-Server kopieren.** Zuerst einen isolierten Cold-Boot und die offenen Ingame-Gates durchführen.
5. Rift Mage wird nicht als freigegebenes Plugin-JAR ausgegeben; sein Contract-ZIP bleibt bis zur manuellen Abnahme gesperrt.

Es wurde im Rahmen dieser Zusammenführung **kein Live-Deployment durchgeführt**.

## Inventur und nicht integrierte Binärstände

Vollständige aktuelle Quellen wurden für die fünf historischen Module übernommen und um den neu erstellten Shared Core ergänzt. Der unversionierte Portal-Spawn-Editorstand ist zusätzlich bytegenau als deploymentgesperrter Recovery-Vertrag erhalten. Nur als Binärbackup gefunden und daher nicht blind dekompiliert oder produktiv integriert:

- HyGunsMMOCompat 1.0.5–1.0.7
- EndlessGuildsPatches (Manifest 1.0.1, abweichende Dateinamen)
- StarterkitChatter 1.0.0–1.0.7
- unversioniertes MjolnirSafetyPatch-Fragment

Alle Originalprojekte und Binärbackups bleiben unverändert als zusätzliche Sicherheitskopien erhalten. Details: [`docs/inventory/`](docs/inventory/).

## Offene Gates

- kombinierter Cold-Boot mit einem einheitlichen MMOSkillTree-Superset,
- Transaktions-/Reihenfolgeowner für globale MMOSkillTree-Konfigurationswriter,
- ownership-sicherer Ability-Shutdown für Hymann und Seuchenweber oder Cold-Boot-only-Nachweis,
- Hymann-Profilpersistenz gegen Logout/Profilwechsel/Crash-Recovery,
- Nachtweber: Clientbinding, Aktivierung, Movement, Damage, Disconnect und Zwei-Spieler-Isolation,
- Rift Mage: manuelle Gameplay-/Visual-Abnahme und `deployment_allowed=true`,
- Portal Spawn: Manifestreparatur, Asset-Pack-Vertrag und isolierte Gameplay-/Visual-Abnahme,
- veraltete Hytale-API-Nutzungen in EndlessBook, Hymann und Seuchenweber.

## Sicherheit und Git

`.gitignore` schließt Secrets, Credentials, lokale Runtime, Caches, Logs und Buildausgaben aus. Fremde oder abgeleitete Drittanbieter-JARs werden nicht committed. Der Ausgangszustand des zuvor leeren GitHub-Repositorys ist mit dem Tag `baseline-before-mod-consolidation-20260810` gesichert.
