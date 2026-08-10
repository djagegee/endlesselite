# Architektur- und Konfliktmatrix

Stand: 2026-08-10  
Scope: Quellintegration und gemeinsamer Java-25/Maven-Reactor. Dies ist **keine** Live-Deploymentfreigabe.

## Integrationsmodell

Endless Elite ist ein Maven-Monorepo mit getrennten Hytale-Laufzeitartefakten. Die Trennung bewahrt eindeutige Plugin-IDs, Entry-Points, Ressourcen und Lifecycle-Owner. Ein einzelnes Monolith-JAR wurde bewusst nicht erzeugt, weil dies Manifeste, Classloader- und Shutdownverträge der vorhandenen Plugins ungesichert verändern würde.

| Modul | Rolle | Runtime-ID | Ergebnis |
|---|---|---|---|
| EndlessBook | gemeinsamer UI-/Hub-Zugang | `Shadow:EndlessBook` | integriert, gebaut und getestet |
| Hymann | Klasse/Progression | `Shadow:Hymann` | integriert, gebaut und getestet; Persistenz-/Reload-Gates offen |
| Nachtweber | Eliteklasse | `Shadow:Nachtweber` | integriert, gebaut und getestet; `DisabledByDefault` und Ingame-Gates bleiben |
| Seuchenweber | Eliteklasse | `Shadow:Seuchenweber` | integriert, gebaut und getestet; Writer-/Reload-Gates offen |
| Rift Mage Dungeon | Content-/Importvertrag | `endless-elite-rift-mage` | Quelle/Tests integriert; Deployment durch eigenes Gate gesperrt |

## Automatisch bestätigte Eindeutigkeit

`tools/verify_repository.py` prüft fail-closed:

- exakt vier unterschiedliche Plugin-IDs,
- vorhandene Java-Quelle für jeden `Main`-Entrypoint,
- keine doppelten Java-FQCNs,
- keine eingecheckten JARs, Klassen, Logs oder `.env`-Dateien außerhalb ignorierter Outputs,
- keine erkannten Secret-Zuweisungen,
- Rift Mage bleibt `deployment_allowed=false`.

Aktueller Lauf: `ENDLESS_ELITE_REPOSITORY_VERIFY_PASS`, 4 Plugins, 162 Main-FQCNs.

## Gemeinsame Systeme und eindeutige Owner

| System | kanonischer Owner | Integrationsentscheidung |
|---|---|---|
| Build/Toolchain | Root-`pom.xml` | Java 25, gemeinsamer Reactor und zentrale lokale Hytale-Pfade |
| Operator-Konfiguration | Root `config/examples/` + `docs/CONFIGURATION.md` | gemeinsame lesbare Profile; Runtime-Schemas bleiben modulbezogen und getestet |
| UI-Hub | EndlessBook | zentraler Zugang und Darstellung; Klassenmodule registrieren keine konkurrierende Hub-UI |
| Spielerlevel, Klasse, Prestige | EndlessLeveling (extern) | kein eigener Monorepo-Ersatzowner |
| Skills, Unlocks, Bindings | MMOSkillTree (extern) | Klassenmodule integrieren additiv; globale Writer bleiben ein Runtime-Gate |
| Mob-Scaling | EndlessEliteMobs (extern) | kein Modul in diesem Repository beansprucht allgemeines Mob-Scaling |
| Kampfzustand | jeweiliges Klassenmodul | Store-/Owner-isoliert; nicht in einen globalen Monorepo-Singleton verschoben |
| Content-/Dungeoninstanz | Rift Mage Dungeon + externe Dungeonruntime | kein eigener Spielerprogress-Owner |
| Distribution | `tools/collect_distribution.py` | sammelt nur eigene, buildverifizierte Artefakte; keine Drittanbieter-JARs |

## Konflikte und Entscheidungen

### 1. MMOSkillTree-Binärvertrag — kontrollierter Superset-Kandidat

- Hymann und Seuchenweber kompilieren gegen Stock-MMOSkillTree 1.5.2.
- Nachtweber benötigt `1.5.2-owned-local` mit additivem `registerIfAbsent` und exakt-instanzgebundenem `unregister`.
- Der reproduzierbare Patcher verändert nach Verifikation genau eine Klasse und fügt nur diese Methoden hinzu.
- Input-SHA-256: `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`.
- Reproduzierter Output-SHA-256: `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`.

**Entscheidung:** Der Patch ist der vorgesehene additive gemeinsame Runtime-Kandidat. Eine kombinierte Cold-Boot-/Ingame-Abnahme mit allen Klassen steht dennoch aus; daher kein Deployment-PASS.

### 2. Globale MMOSkillTree-Konfigurationswriter — konfliktträchtig

- Seuchenweber schreibt `abilities.json` sowie deutsche/englische Messages.
- Hymann schreibt `abilities.json`, Custom Skills und Skilltree-Konfiguration.
- Nachtweber besitzt einen dritten Installer, ruft ihn im aktuellen Plugin-Setup aber nicht automatisch auf.

IDs sind getrennt, aber es gibt noch keinen gemeinsamen Transaktions-/Lockowner. Die aktuelle Integration bewahrt alle Quellen, aktiviert aber **keinen** neuen parallelen Writer. Bis zu einem kontrollierten Writer-Koordinator gilt Cold-Boot-only und kombinierte Runtime-Abnahme als offen.

### 3. Ability-Registrierung und Reload — konfliktträchtig

- Nachtweber besitzt ownership-sicheren, instanzgenauen Shutdown.
- Seuchenweber und Hymann registrieren globale Ability-Effekte ohne belegten äquivalenten Unregisterpfad.

**Entscheidung:** Quell- und Buildintegration ja; Hot-Reload-Freigabe nein. Produktiver Betrieb benötigt Cold-Boot oder einen separaten ownership-sicheren Lifecycle-Slice.

### 4. Spielerfortschritt — Hymann als zusätzlicher Owner

Hymann spiegelt MMOSkillTree-`SkillComponent`-Zustand profilspezifisch in `profile-progress.properties`. Dies kann beabsichtigte EndlessLeveling-Profiltrennung sein, ist aber ein zweiter Save-/Rehydrationsowner. Das dormant vorhandene Ability-Binding-Persistence-System ist aktuell nicht registriert.

**Entscheidung:** Bestehende Funktion bleibt erhalten, wird aber nicht weiter zentralisiert oder zusätzlich aktiviert. Logout, Profilwechsel und Crash-Recovery bleiben Runtime-Gates.

### 5. Event-/Damage-Reihenfolge

Es gibt keine doppelten Event- oder System-IDs. Hymann besitzt jedoch mehrere Damage-Systeme mit Reihenfolgesensitivität; Seuchenweber und Nachtweber besitzen eigene Damage-/Maintenance-Systeme. Die getrennten Pluginmodule verhindern Klassenkollisionen, beweisen aber keine kombinierte Ingame-Reihenfolge.

### 6. Ressourcen und UI

- Keine doppelten Java-Klassen.
- Eigene Assetnamensräume sind getrennt.
- `manifest.json` existiert erwartungsgemäß einmal pro getrenntem Plugin-JAR.
- EndlessBook bleibt UI-Hub; Rift Mage liefert ein namespaced Content-/UI-Importpaket.

### 7. Rift Mage Dungeon

Das Maven-JAR ist absichtlich kein Runtimeplugin. Das eigentliche Ergebnis ist der Contract-ZIP aus `src/main/dlc`. `release-gate.json` sperrt Deployment bis zur manuellen Gameplay-/Visual-Abnahme. Der Quellvertrag ist integriert und getestet, wird aber nicht als freigegebene Mod-JAR ausgegeben.

## Nicht aus Binärbackups integrierte Module

`HyGunsMMOCompat`, `EndlessGuildsPatches`, `StarterkitChatter` und das unversionierte `MjolnirSafetyPatch`-Fragment besitzen im untersuchten Scope keine vollständigen Quellen. Eine blinde Dekompilierung würde Herkunft, Lizenz, Tests und API-Verträge verschlechtern. Sie bleiben als hashbasierte Recovery-Evidenz dokumentiert; Originalbackups wurden nicht verändert oder gelöscht.
