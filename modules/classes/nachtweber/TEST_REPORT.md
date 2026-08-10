# Nachtweber – Testbericht 0.1.0

`NachtweberCoreContractTest` prüft Kardinalität, owner-isolierte Verstrickung, additive Caps, Ablauf/Refresh, Fessel-Schwelle, owner-spezifische Reinigung, Giftimmunität, Fanggift-Tick ohne Replay-Burst, Anti-Rekursion, reduzierte Boss-/Elite-/PvP-Kontrolle und endliche Operatorwerte.

`NachtweberRuntimeAdapterTest` prüft den EndlessLeveling-Klassenvertrag, Reihenfolge und Idempotenz von Permission-/Klassenregistrierung, fail-closed Verhalten bei abgelehnter Registrierung, genau einmaliges Unregister sowie Hytale-Konstruktor und deaktiviertes Dependency-Manifest.

`NachtweberRuntimeWiringTest` prüft identity-basierte Store-Partitionierung auch für `equals()`-gleiche Storeobjekte, exakt einmaligen World-Remove-/Shutdown-Cleanup, fail-closed Bindings nach Shutdown, Maintenance ohne implizite State-Erzeugung, die binär bestätigte `TickingSystem<EntityStore>`-Signatur, idempotente System-/Eventregistrierung, Entrypoint-Ownership sowie eine konkrete pro Store gekapselte Ledger-Runtime. Deren Maintenance entfernt abgelaufene Verstrickung und Gift ohne einen Damage-Tick zu konsumieren, meldet den ersten Tick genau einmal und bleibt nach `close()` fail-closed. Der Shadow-Swing-Reconciliation-Core ist ebenfalls pro Store gekapselt und nach Runtime-Close nicht mehr dispatchbar. `NachtweberMultiplayerIsolationPreflightTest` prüft zwei UUID-Owner in zwei Store-Runtimes und ownerselektives Gesamtcleanup über Ledgers, Cooldowns und Swing-Sessions.

`NachtweberMmoInstallerTest` prüft Skill-ID, sieben Unlocks, aktive/passive Kardinalität, additive Ability-Installation, Fremdschlüssel-Erhalt, Einmalbackup, atomisch-idempotente Wiederholung sowie DE-/EN-Lokalisierungsverträge.

`BlackThreadServiceTest` prüft erfolgreichen Stapelauf bis zur Fessel, serverseitige Autorisierung, Reichweite, ungültige Ziele, owner-spezifischen Cooldown ohne Zustandsmutation, saturierte Zeitaddition ohne `long`-Überlauf, MMOSkillTree-Effect-/ParamSpec-Vertrag, fail-closed Null-Caster sowie die Sekunden-/Blöcke-/Anzahl-Parität der gebündelten Konfiguration.

`HuntingCocoonServiceTest` prüft owner-spezifischen atomaren Verstrickungsverbrauch, Erhalt fremder Owner-Zustände, owner-isoliertes Fanggift, Autorität, Reichweite, Cooldown, Immunität, DoT-/Reflexionsschutz, unzureichende Stapel ohne Mutation, saturierte Venom-/Cooldownzeiten, MMOSkillTree-Effect-/ParamSpec-Vertrag, fail-closed Null-Caster und Schema-7-Config-Parität.

`VenomTickServiceTest` prüft owner-isolierten Tick-Drain, höchstens einen nachgeholten Tick ohne Replay-Burst, Sorcery-Skalierung, Damage-Cap, interne Cause-Kennzeichnung, abgefangene Runtime-Portfehler, exakte installierte EndlessLeveling-11.6.1-ABI sowie fail-closed Factory-Verhalten vor Initialisierung des Hytale-`DamageCause`-AssetStores. Die gebündelte Schema-7-Konfiguration verwendet Sekunden, Stapelschaden, explizite Sorcery-Punkte bis zur Verdopplung, Maximalschaden und genau einen maximal überfälligen Tick.

`PassiveVenomServiceTest` prüft `TOXIC_GLANDS` und `HUNTING_INSTINCT`: bestätigter Direct-Hit, Immunität, DoT-/Reflexionsrekursion, owner+target-Cooldown, own-entanglement-only Bonus, Erhalt fremder Zustände, owner-spezifische Cooldownbereinigung und Schema-7-Config-Parität.

`NachtweberCompletionTest` prüft `SHADOW_SWING`-Anker-, Impuls- und Cooldownregeln, `WALL_HUNTER`-Wandkontakt-/Eingabegates, den begrenzten nearest-visible-hostile-Vertrag von `DANGER_SENSE`, exakt drei vollständige aktive Effects und die finalen Einheiten der Schema-7-Konfiguration. `ShadowSwingReconciliationControllerTest` prüft zusätzlich owner-isolierte Sessions, Ablauf, Anchor-/Tethergrenzen, vektorielle Velocity-Korrekturgrenze, Reattach-Rate-Limit, stale Steps, NaN-Fail-closed, Cancel/Cleanse/Close und explizite Configeinheiten. Die Runtime-Adapter kompilieren gegen die binär geprüften nativen Hytale-Komponenten `Velocity`, `TransformComponent`, `HeadRotation`, `CollisionResultComponent`, `MovementStatesComponent` und `BlockRaystep`.

Der nachgelagerte Hytale-0.5.7-Bytecodeaudit präzisiert die Bewegungsgrenze: `SHADOW_SWING` validiert Anker und mutiert serverseitige `Velocity`, aber Player-Reconciliation und dauerhafte Trajektorienautorität sind ohne Boot-/Ingame-Test nicht belegt. Die Auditregression prüft zusätzlich Danger-Sense-Preflight vor dem Spatial Scan, harte Grenzen von 16 Blöcken/64 behaltenen Kandidaten sowie fail-closed `WALL_HUNTER` bei pending Collision-Ergebnissen. `MovementStates.jumping` gilt nur als advisory Input; horizontaler Wandkontakt stammt aus dem fertigen serverseitigen Collision-Ergebnis.

## Finaler Abschlussnachweis

- Java-25 `mvn clean verify`: **BUILD SUCCESS**
- Tests: **63**, Failures: **0**, Errors: **0**, Skipped: **0**
- Produktionsquellen: **66**; Testklassen: **11**
- Unabhängiger frischer Verifier: `NACHTWEBER_COMPLETION_ARTIFACT_PASS`
- JAR-/Config-/Manifest-Gates: Schema 7, 3 aktive + 4 passive Fähigkeiten, keine fehlenden Klassen, `DisabledByDefault: true`
- Native ABI-Gates: `Velocity.addVelocity`, `Velocity.addInstruction`, `ChangeVelocityType.Add`, Player-/Generic-Instruction-Systeme, CollisionResult, MovementStates und Kollisionsnormalen vorhanden
- Lifecycle-Gate: pluginlokales Store-Maintenance-System, `StartWorldEvent`-Binding und globaler World-Remove-Cleanup sind ownership-sicher registriert; MMOSkillTree bietet weiter `register(...)`, aber kein `unregister`, daher bleiben Gameplay-Effects dort `liveWired=false`.
- SHA-256 des aktuellen reproduzierbaren `clean verify`-Artefakts: `ffc4c9f981d9b478f7791108a4dd2cbcb8dae6b71d04b2b15ebf8f068aae4732`
- Temporärer Verifier nach PASS gelöscht; keine `hermes-verify-*.py`-Reste.

Manuelle Client-, Bewegungs-, Multiplayer-, aktive Fähigkeits-/Gameplay-Ausführung und positive Server-Damage-Tests bleiben ungetestet. Die reale Store-Maintenance-Tick-Ausführung ist inzwischen separat isoliert belegt. Das Artefakt darf weiterhin nicht deployed werden.

## Isolierter Serverboot – 2026-08-09

Freigegebener loopback-only Boot mit dem hashgeprüften Nachtweber-JAR und dependency-complete Baseline (`EndlessLeveling 11.6.1`, `MMOSkillTree 1.5.2`, `ZiggfreedCommon 1.3.0`, `EEM 3.10.0`) ergab `NACHTWEBER_ISOLATED_SERVER_BOOT_PASS`:

Der Runtime-Boot verwendete das damalige Artefakt SHA-256 `7bcbf1902c5b40e7f33f220548ccca009467aee443c498eed2d0638e86a537cf`. Ein späterer kanonischer Rebuild derselben Quellen erzeugte wegen Paketzeitstempeln den oben genannten neuen JAR-Hash; für diesen neuen Bytehash wird kein zusätzlicher Boot-PASS behauptet.

- `Shadow:Nachtweber adapter initialized: class elite_nightweaver; gameplay systems remain fail-closed`
- `Enabled plugin Shadow:Nachtweber`
- Listener auf `127.0.0.1:5523`
- `Hytale Server Booted! [Multiplayer]`
- keine Nachtweber-Warn-/Fehlerzeile und keine Exception, Linkage-, Setup-, Enable- oder Assetvalidation-Failure
- `Shadow:Nachtweber adapter shut down`, Plugin-Shutdown und `Shutdown completed!`
- Port und Java-Prozess anschließend freigegeben

Der erste Minimalboot ohne EEM zeigte eine EndlessLeveling-NPC-Referenz auf `EEM_Weapon_Spear_ALPHA01`; EEM wurde deshalb als einzige Differentialvariable ergänzt. Fünf nichtfatale HytaleGenerator-`SERR Reallocate`-Zeilen blieben ohne Exception bestehen und werden nicht als Nachtweberfehler attribuiert. Vollständige Evidenz: `.hermes/runtime/nightweaver-isolated-boot-20260809/evidence/` (Bootlog-SHA-256 `cb0dbafc5f992e3b51676075f0cca6857482e1f43a140a9b839bb0fc99e230a0`).

Der Boot belegt ausschließlich Plugin-/Klassen-Lifecycle und Dependencyauflösung. MMOSkillTree-Effect-Wiring, ECS-/Passive-Scheduler, positiver Damagepfad, Shadow-Swing-Reconciliation, Zwei-Spieler-Isolation und Client-/Ingame-Verhalten bleiben ausdrücklich unbelegt; kein Deployment-PASS.

## Reproduzierbares Artefakt – 2026-08-09

Ein realer RED-Doppelbuild derselben Quellen erzeugte zunächst zwei unterschiedliche JAR-Hashes (`01b5c0e6…` und `95ead1bd…`). Der neue Contracttest `mavenBuildPinsAReproducibleOutputTimestamp` war wegen des fehlenden Pins rot. Nach `project.build.outputTimestamp=2026-08-09T00:00:00Z` ist der Test GREEN.

Zwei anschließende unabhängige Java-25-`clean verify`-Builds bestanden jeweils mit 40 Tests und erzeugten bytegleich:

`007fe5d2db4d7d889db9476f8c6ac3660c769f2bd4c9cb176c81d8057a0db972`

Damit sind neue Builds derselben Quellen paketdeterministisch. Der ältere isolierte Runtime-Boot bleibt historische Evidenz für das damalige Artefakt `7bcbf190…`; der aktuelle reproduzierbare Hash wurde anschließend separat runtime-geprüft.

## Reproduzierbarer Hash – isolierter Serverboot – 2026-08-09

Das reproduzierbare Artefakt SHA-256 `007fe5d2db4d7d889db9476f8c6ac3660c769f2bd4c9cb176c81d8057a0db972` wurde in einer neuen dependency-complete Testwurzel mit EndlessLeveling 11.6.1, MMOSkillTree 1.5.2, ZiggfreedCommon 1.3.0 und EEM 3.10.0 auf `127.0.0.1:5523` gebootet.

- Plugin entdeckt und `elite_nightweaver`-Adapter initialisiert
- `gameplay systems remain fail-closed` explizit geloggt
- `Shadow:Nachtweber` aktiviert
- Loopback-Listener und `Hytale Server Booted!` bestätigt
- 0 `NoClassDefFoundError`, `NoSuchMethodError`, `LinkageError`, Exceptions, Setup-/Enable- und Assetvalidation-Fehler
- vier nicht kandidatenattribuierte HytaleGenerator-`SERR Reallocate`-Zeilen
- Adapter und Plugin geordnet heruntergefahren; `Shutdown completed!`
- Port 5523 und Java-/Hytale-Prozess anschließend frei
- isolierte JARs, Cache-, Welt- und Moddaten entfernt; nur Evidenz erhalten

Status: `NACHTWEBER_REPRODUCIBLE_ARTIFACT_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-reproducible-artifact-boot-20260809/evidence/`; vollständiger Bootlog SHA-256 `f8d2d366da6cbf9bfa923e2208894cb612aacbf616ccc6873ac8c61234b556fe`.

Dieser Nachweis umfasst weiterhin kein Client-/Ingame-Verhalten, keine Live-MMOSkillTree-Effectregistrierung, keinen Scheduler-/positiven Damagepfad, keine Zwei-Spieler-Isolation und keine Shadow-Swing-Reconciliation. Kein Deployment-PASS.

## Store-Lifecycle ECS-Wiring – isolierter Serverboot – 2026-08-09

Der ownership-sichere Runtime-Infrastrukturslice wurde gegen die installierte Hytale-0.5.7-ABI umgesetzt: identity-basierte Store-State-Registry, idempotenter Coordinator, pluginlokales `TickingSystem<EntityStore>`, globaler `RemoveWorldEvent`-Cleanup und pluginweiter Shutdown-Cleanup. `ComponentRegistryProxy` besitzt dafür einen pluginlokalen Unregister-Vertrag; der MMOSkillTree-`ActiveAbilityService` weiterhin nicht und wurde nicht verändert.

RED→GREEN-Nachweise deckten fehlende Store-Registry, Shutdown-Cleanup, state-freie Maintenance-Lookups, Coordinator-Lifecycle, konkrete Store-Tick-Signatur, Registrierungsorchestrierung und Entrypoint-Ownership ab. Java-25 `mvn clean verify` bestand anschließend mit 47 Tests. Zwei unabhängige Builds erzeugten bytegleich:

`6658e8e24971ff413f56b865efa520d3b181d057e57019c3ad7752316113bc82`

Genau dieses Artefakt wurde dependency-complete auf `127.0.0.1:5523` gebootet. Der frische Root respektierte zunächst `DisabledByDefault: true`; ausschließlich die isolierte Config erhielt danach `Mods[Shadow:Nachtweber].Enabled=true`.

- exakter Marker: `adapter initialized: class elite_nightweaver; store lifecycle ECS wired; gameplay effects remain fail-closed`
- Plugin aktiviert und `Hytale Server Booted!` erreicht
- 0 Klassen-, Linkage-, Exception-, Setup-, Enable-, Assetvalidation- oder Runtime-Registration-Fehler
- Adapter, Plugin und Server kontrolliert heruntergefahren
- Port 5523 und Java-/Hytale-Prozess frei
- isolierte JARs, Config, Cache-, Welt- und Moddaten entfernt; nur Evidenz erhalten
- Live-Modordner und Live-Konfiguration unverändert

Status: `NACHTWEBER_STORE_LIFECYCLE_ECS_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-live-runtime-wiring-boot-20260809/evidence/`; Bootlog SHA-256 `07e24969a02e7aa1ee6019ed66818c1b090105647b076514d0e42baee6933047`.

Belegt ist die erfolgreiche Annahme des Store-level Systems und des World-Remove-Handlers durch die pluginlokalen Hytale-Registries. Nicht behauptet werden Tick-Ausführung mit gebundenem Gameplayzustand, MMOSkillTree-Live-Effectregistrierung, positiver Damagepfad, Shadow-Swing-Reconciliation, Zwei-Spieler- oder Client-/Ingame-Verhalten. Kein Deployment-PASS.

## Storegebundener Gameplayzustand und realer Maintenance-Tick – 2026-08-09

`NachtweberLedgerStoreRuntime` kapselt pro Hytale-`Store<EntityStore>` jeweils eigene `EntanglementLedger`- und `VenomLedger`-Instanzen. Der Maintenance-Pfad entfernt ausschließlich abgelaufenen Zustand; er konsumiert keinen fälligen Gift-Damage-Tick und ruft weder Damage-, Proc-, Effect- noch MMOSkillTree-Pfade auf. World-Remove und Plugin-Shutdown leeren und schließen den Zustand weiterhin höchstens einmal.

Der erste isolierte Boot war ein echter Runtime-RED-Nachweis: `AddWorldEvent` wurde vor Initialisierung von `EntityStore.getStore()` ausgelöst, der Store war `null`, Nachtweber erzeugte eine `NullPointerException`, und der Tickmarker blieb aus. Die RED-Evidenz wurde erhalten. Nach ABI-Prüfung wurde die Bindung auf `StartWorldEvent` verschoben und zusätzlich null-fail-closed abgesichert.

Java-25 `mvn clean verify` bestand danach mit **48 Tests**, 0 Failures, 0 Errors und 0 Skips. Zwei unabhängige Builds erzeugten bytegleich:

`1105ac1c56b29fdbdabc8c2b58f8d37317bd7af49b16d9eb3a48b66d955060b9`

Genau dieser Hash wurde dependency-complete auf `127.0.0.1:5523` gebootet:

- Nachtweber entdeckt, initialisiert und aktiviert
- `Hytale Server Booted!` erreicht
- realer Marker `Shadow:Nachtweber first bound-store maintenance tick observed at … ms`
- 0 `NullPointerException`, Exceptions, Klassen-, Linkage-, Setup-, Enable-, Assetvalidation- oder Runtime-Registration-Fehler
- sechs bekannte, nicht kandidatenattribuierte HytaleGenerator-`[SERR] Reallocate`-Zeilen
- Adapter, Plugin und Server geordnet heruntergefahren; Port und Prozesse frei
- isolierte Config-, JAR-, Cache-, Welt- und Moddaten entfernt; nur RED-/GREEN-Evidenz erhalten

Status: `NACHTWEBER_BOUND_STORE_MAINTENANCE_TICK_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-bound-store-tick-boot-20260809/evidence/`; GREEN-Bootlog SHA-256 `373c415e15cbb2eead5dc8d507679a6e642b38dd029c86f2c60ebdb54b07dba0`.

Nicht belegt bleiben positive `DamageCause`-Ausführung, aktive Ability-/MMOSkillTree-Effectregistrierung, Shadow-Swing-Reconciliation, Zwei-Spieler-Isolation und Client-/Ingame-Verhalten. Kein Deployment-PASS.

## Storegebundene Gameplay-Fassade – 2026-08-09

Die gebundene `NachtweberLedgerStoreRuntime` stellt nun ausschließlich operationale, post-close gesperrte Zugriffe für `BLACK_THREAD`, `HUNTING_COCOON`, passive Giftanwendung und `VENOM_TICK` bereit. Rohe Ledgers oder Serviceobjekte werden nicht herausgegeben. Black Thread, Cocoon, Passive und Venom-Tick teilen innerhalb eines Stores exakt dieselben Entanglement-/Venom-Ledgers und ihre storelokalen Cooldownmaps; ein identity-verschiedener Store sieht keinen dieser Zustände.

Der Coordinator und das Plugin-Wiring geben diese Fassade nur für einen bereits gebundenen exakten Store frei. Unbekannte, entfernte, null oder nach Shutdown angesprochene Stores bleiben fail-closed. Der EndlessLeveling-Sorcery-Provider ist als ruhende Dependency injiziert; es existiert weiterhin kein automatischer Damage-, Effect- oder Ability-Aufruf.

RED→GREEN deckte fehlende Cross-Service-State-Sharing-Fassade, Store-spezifischen Coordinator-Dispatch, produktive Power-Provider-Injection und den schmalen Wiring-Dispatch ab. Java-25 `mvn clean verify` bestand mit **52 Tests**, 0 Failures, 0 Errors und 0 Skips; `NachtweberRuntimeWiringTest` enthält **12** grüne Tests. Zwei unabhängige Builds erzeugten bytegleich:

`8efde02bf9a7eb4b42b9de9a0cd2ff98e2ffe5f0f30160ec5aa5a1af0c6b8ec3`

Genau dieser Hash wurde dependency-complete auf `127.0.0.1:5523` gebootet: Discovery, Initialisierung, Plugin-Enable, Store-Maintenance-Tick, `Hytale Server Booted!`, Adapter-/Plugin-Shutdown und `Shutdown completed!` sind belegt; alle strikten Fehlerzähler sind 0. Vier bekannte HytaleGenerator-`[SERR] Reallocate`-Zeilen sind nicht kandidatenattribuiert. Teststate wurde entfernt, ausschließlich Evidenz blieb erhalten.

Status: `NACHTWEBER_STORE_GAMEPLAY_FACADE_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-store-gameplay-facade-boot-20260809/evidence/`; Bootlog SHA-256 `8d617e2d257644efb672d47936e7585ef65bb3dc7fd52c7f4c82fa53e213e6df`.

Der Boot löste bewusst keine Ability und keinen Damage-Aufruf aus und änderte keine MMOSkillTree-Registry. Kein Client/Ingame und kein Deployment-PASS.

## DamageCause-AssetStore-Readiness – 2026-08-09

Lokales `javap` belegt `DamageCause.getAssetStore()`, `getAssetMap()` und `IndexedLookupTableAssetMap.getIndexOrDefault/getAsset`. EndlessLeveling 11.6.1 erzeugt seinen Ability-DoT intern mit Hytales physischem DamageCause; der Source-Name `Poison` ist kein DamageCause-Assetkey. `Assets.zip` enthält kanonisch `Server/Entity/Damage/Physical.json`, daher prüft die read-only Probe den nicht-deprecated Key `Physical` statt des veralteten statischen Alias `DamageCause.PHYSICAL`.

Vor Serverboot meldet die Probe fail-closed `UNAVAILABLE`. Ein `UNAVAILABLE`-Ergebnis verbraucht den Einmalmarker nicht; das erste konsistente `READY` wird genau einmal beobachtet. Null Stores lösen keine Probe aus. Die Runtimeprüfung verifiziert ausschließlich `AssetStore → identische AssetMap → Physical-Index → Asset mit ID Physical`; sie konstruiert und emittiert keinen Schaden.

Java-25 `mvn clean verify`: **55 Tests**, 0 Failures, 0 Errors, 0 Skips und keine neue Deprecationwarnung. Bytegleicher Doppelbuild: `e003fb8ca0ba7acc553dea0b54a818d4a6577be000e33c3471b8d6b911462fff`.

Exakt dieser Hash wurde isoliert auf `127.0.0.1:5523` gebootet. Der Marker `DamageCause AssetStore ready: cause=Physical index=2; damage execution remains disabled` erschien genau einmal. Discovery, Plugin-Enable, Store-Maintenance-Tick, Serverboot und kontrollierter Shutdown sind belegt; alle strikten Fehlerzähler sind 0. Sechs bekannte HytaleGenerator-`[SERR] Reallocate`-Zeilen sind nicht kandidatenattribuiert. Cleanupgate PASS, ausschließlich Evidenz blieb erhalten.

Status: `NACHTWEBER_DAMAGECAUSE_READINESS_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-damagecause-readiness-boot-20260809/evidence/`; Bootlog SHA-256 `2ef4b908304aa81dd206b0f2fc5f8fdfce86a8df965941ef2afc05a73b43d06b`.

Dies ist ausdrücklich **kein positiver Damage-PASS**: Es gab kein Owner-/Target-Entitypaar, kein `Damage`-Objekt, keinen `DamageSystems.executeDamage`-Aufruf und keine Client-/Ingame-Ausführung.

## Shadow-Swing-Reconciliation-Core – 2026-08-09

`ShadowSwingReconciliationController` ist eine reine, serverseitig zu speisende Zustandsmaschine ohne Hytale-Entity- oder Velocity-Aufruf. Jede storegebundene `NachtweberLedgerStoreRuntime` besitzt eine eigene Instanz. Start/Step/Cancel werden nur über die schmale Gameplay-Fassade angeboten und liefern nach Store-Remove/Close keine Ergebnisse mehr.

Pro Owner gelten: maximal 1,5 Sekunden Sessiondauer, 18 Blöcke Start-Anchor, 22 Blöcke Tetherbruch, 1,25 Blöcke Releaseabstand, 14 Blöcke/s Pull-Zielgeschwindigkeit, maximal 3 Blöcke/s Korrektur pro Step und 0,25 Sekunden Reattach-Intervall. Stale Timestamps mutieren nicht; nicht-endliche serverseitige Position/Velocity beendet nur die betroffene Session fail-closed. Cancel behält das Reattach-Limit, explizites Owner-Cleanup entfernt beide Zustände, Close ist idempotent und endgültig.

Die gebündelte Config wurde additiv auf Schema 7 migriert und benennt alle Werte in Sekunden, Blöcken oder Blöcken/s. Es wurden keine Live-Konfigurationen verändert. Java-25 `mvn clean verify`: **62 Tests**, 0 Failures, 0 Errors, 0 Skips; Controller 6 Tests, Wiring 14 Tests. Bytegleicher Doppelbuild: `64bd21779b7780a090eb40d508e31e82023de545113adab33083c621cfc45dca`.

Der erste isolierte Boot war ein Runtime-RED: Das neue Root respektierte `DisabledByDefault: true` und übersprang Nachtweber. Nach lokalem ABI-Nachweis des Configvertrags wurde ausschließlich im isolierten Root `Mods[Shadow:Nachtweber].Enabled=true` gesetzt. Der korrigierte Boot desselben Hashes erreichte Discovery, Plugin-Enable, DamageCause-Readiness, Store-Maintenance-Tick und `Hytale Server Booted!`; alle strikten Fehlerzähler sind 0. Shadow Swing, Velocity und Damage wurden nicht ausgeführt. Sechs bekannte HytaleGenerator-`[SERR] Reallocate`-Zeilen sind nicht kandidatenattribuiert.

Der Server meldete Adapter-/Plugin-Shutdown und `Shutdown completed!`. Danach blieb ein fremder JVM-Thread hängen; PID 22000 wurde erst nach ausdrücklicher Nutzerfreigabe gezielt beendet. Anschließend waren Port und Java-/Hytale-Prozesse frei. Ein erster Cleanupversuch scheiterte an einem Windows-Longpath in `.cache`; dessen voreilig gedruckter Marker wurde verworfen. Der Longpath-Retry und das erneute strikte Gate bestätigten schließlich exakt einen Top-Level-Eintrag (`evidence`).

Status: `NACHTWEBER_SHADOW_SWING_RECONCILIATION_CORE_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-shadow-swing-reconciliation-core-boot-20260809/evidence/`; Bootlog SHA-256 `ee155abe8092ac4f073850f69bb31a2325cb5ac28ed9262e9cde7c887e0a58b6`.

Nicht belegt sind ECS-Tick-/Movement-Wiring, tatsächlicher Swing-Start, native Velocity-Mutation, Client-Reconciliation, Ingame-Verhalten oder Multiplayer. Kein Deployment-PASS.

## Zwei-Owner-/Zwei-Store-Isolations-Preflight – 2026-08-09

Die storegebundene Gameplay-Fassade bietet nun `cleanupOwner(UUID)` als fail-closed Disconnect-/Klassenwechsel-Primitiv. Innerhalb exakt einer `NachtweberLedgerStoreRuntime` entfernt es Verstrickung, Gift, Black-Thread-/Hunting-Cocoon-/Passive-Cooldowns sowie Shadow-Swing-Session und Reattach-Zeit des angegebenen Owners. Null oder eine geschlossene Runtime werden abgewiesen. Zustand anderer Owner und identity-verschiedener Stores bleibt unverändert.

Der End-to-End-RED-Test scheiterte zunächst beim Testcompile, weil `cleanupOwner(UUID)` fehlte. Minimal-GREEN ergänzte nur ownerselektive Ledger-/Cooldown-Primitiven und den Fassade-Dispatch. Der Test belegt parallel Owner A und B im ersten Store sowie Owner A in einem zweiten Store: Nach Cleanup von A in Store 1 sind dessen Zustand und Cooldowns zurückgesetzt, während Owner B und Store 2 unverändert weiterarbeiten.

Java-25 `mvn clean verify`: **63 Tests**, 0 Failures, 0 Errors, 0 Skips. Bytegleicher Doppelbuild: `a5ce7943dc7f09222c7a7deaf3fb11b60c753871843a8b03531f0feb097c5d15`.

Genau dieser Hash wurde dependency-complete auf `127.0.0.1:5523` gebootet. Discovery, Plugin-Enable, DamageCause-Readiness, Store-Maintenance-Tick, `Hytale Server Booted!`, Nachtweber-/Plugin-Shutdown und `Shutdown completed!` sind belegt; alle strikten Fehlerzähler sind 0. Der Boot erzeugte keinen Spieler, rief `cleanupOwner` nicht auf und führte weder Ability, Damage noch Movement aus. Fünf bekannte HytaleGenerator-`[SERR] Reallocate`-Zeilen sind nicht kandidatenattribuiert.

Nach vollständigem Server-Shutdown blieb erneut ein fremder JVM-Thread aktiv; PID 14552 wurde nach ausdrücklicher Nutzerfreigabe beendet. Der später sichtbare `HytaleClient.exe` PID 9992 gehörte nicht zum Testserver und wurde nicht verändert. Port 5523 und Server-PID sind frei; Cleanupgate PASS, nur `evidence` blieb erhalten.

Status: `NACHTWEBER_MULTIPLAYER_ISOLATION_PREFLIGHT_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweber-multiplayer-isolation-preflight-boot-20260809/evidence/`; Bootlog SHA-256 `5c4055b93c96cd030d8d4545025bd0bfbb5eb4fad7d7f285ad95f418f951ac3c`.

Dies ist ausdrücklich **kein echter Zwei-Spieler-/Multiplayer-PASS**: Die zwei Owner sind deterministische UUIDs im Unit-Preflight, keine verbundenen Hytale-Spieler. Reconnect-/Disconnect-Event-Wiring, reale Entities, Client/Ingame und Deployment bleiben offen.

## Owner-Cleanup bei PlayerDisconnectEvent – 2026-08-09

Der lokale Hytale-0.5.7-Bytecodeaudit belegt den Ablauf in `Universe.removePlayer(PlayerRef)`: `PlayerDisconnectEvent` wird global dispatcht, bevor der Server `PlayerRef.getReference()` liest und bevor die Entity aus dem Store entfernt wird. Der Event liefert den autoritativen `PlayerRef`; daraus werden `getUuid()` und bei noch vorhandener Referenz `getReference().getStore()` gelesen. Null PlayerRef, UUID, Referenz oder Store bleiben fail-closed.

`NachtweberRuntimeWiring.RegistrationPort` registriert genau einen globalen Disconnect-Handler über die pluginlokale EventRegistry. Der Handler dispatcht `cleanupOwner(UUID)` ausschließlich in den identity-genau gebundenen Store. Unbekannte oder bereits entfernte Stores erzeugen keinen Zustand. Die Eventregistrierung gehört weiterhin dem Plugin-Lifecycle; es wurde keine fremde MMOSkillTree-Registry verändert.

Der RED-Test beobachtete zunächst 0 statt 1 Disconnect-Registrierungen. Minimal-GREEN ergänzte nur RegistrationPort, Wiring-Dispatch und den Hytale-Adapter. Nullkontexte sind getestet; der eigentliche Zwei-Owner-/Zwei-Store-Cleanup bleibt durch `NachtweberMultiplayerIsolationPreflightTest` belegt. Java-25 `mvn clean verify`: **63 Tests**, 0 Failures, 0 Errors, 0 Skips. Bytegleicher Doppelbuild: `ffc4c9f981d9b478f7791108a4dd2cbcb8dae6b71d04b2b15ebf8f068aae4732`.

Genau dieser Hash wurde dependency-complete auf `127.0.0.1:5523` gebootet. Die neue `PlayerDisconnectEvent`-/`PlayerRef.getReference()`-Linkage wurde vom Pluginsetup akzeptiert; Discovery, Enable, DamageCause-Readiness, Maintenance-Tick, Serverboot und vollständiger Shutdown sind belegt. Alle strikten Fehlerzähler sind 0, fünf bekannte Generator-SERRs nicht kandidatenattribuiert. Es verband sich kein Spieler; daher wurde kein realer Disconnect und kein owner-Cleanup ausgelöst. Kein Damage, Movement, Clientsteuerung oder Ingame.

Nach `Shutdown completed!` blieb der bekannte Fremdthread hängen; PID 220 wurde nach ausdrücklicher Nutzerfreigabe beendet. Der während des Boots separat laufende HytaleClient PID 9992 wurde nicht gesteuert und war beim finalen Cleanupgate bereits selbst beendet. Port, Server-PID und Runtime-State sind bereinigt; nur `evidence` blieb erhalten.

Status: `NACHTWEBER_OWNER_CLEANUP_EVENT_WIRING_BOOT_PASS`. Evidenz: `.hermes/runtime/nightweaver-owner-cleanup-event-wiring-boot-20260809/evidence/`; Bootlog SHA-256 `4e64135a68eb8d5f6fec3073f7f9414449b7950abac6a99250a786c5cb680a4d`.

Ein realer Player-Disconnect bleibt ohne Client-/Ingame-Freigabe ungetestet. Kein echter Multiplayer- oder Deployment-PASS.

### Klassenwechsel-ABI-Abgrenzung

EndlessLeveling 11.6.1 besitzt mit `AbilityBridge.Hooks.onClassChanged(UUID)` einen direkten Klassenwechsel-Callback. Dieser ist für Nachtweber nicht ownership-sicher nutzbar: `AbilityBridge` verwaltet genau einen globalen Hook und bietet `register(Hooks)`, aber kein passendes `unregister(Hooks)`. Eine Registrierung könnte einen fremden Owner überschreiben oder nach Plugin-Shutdown bestehen bleiben. `ProfileSwitchedEvent` und `BuildAppliedEvent` besitzen zwar symmetrische Add-/Remove-Listener, sind aber keine belegten Klassenwechsel-Events; außerdem tragen sie nur UUID/Profil- beziehungsweise Builddaten und keinen Store. Deshalb wurde kein Klassenwechsel-Wiring ergänzt. Diese Grenze ist ein bestätigter ABI-Blocker, kein fehlgeschlagener Test.

## Shadow-Swing ECS-/Velocity-ABI-Preflight – 2026-08-10

Der Preflight war strikt read-only und verwendete Java-25-`javap` sowie JAR-Bytecodesuche gegen `HytaleServer.jar` SHA-256 `43d9bcff1dd31574577dbfc82147718dbe2ac16c19000071f965b521ba808cdc` und `MMOSkillTree-1.5.2.jar` SHA-256 `9d15eb57f016f595b40001cff510497a18f358caa4198d7d163835d3ca300eb6`.

Bestätigt sind `PlayerRef.getReference()`, `Ref.getStore()`, `Store.getComponent(...)`, `TransformComponent.getPosition()`, `Velocity.getComponentType()`, `Velocity.addVelocity(...)`, `Velocity.addInstruction(Vector3d, VelocityConfig, ChangeVelocityType)`, `ChangeVelocityType.Add`, `World.execute(Runnable)` und die ECS-Tick-Signaturen mit `ArchetypeChunk`, `Store` und `CommandBuffer`. `GenericVelocityInstructionSystem` konsumiert Add-Instruktionen und ruft danach `Velocity.addVelocity(...)`; `PlayerVelocityInstructionSystem` sendet für dieselbe Instruktion `ChangeVelocity(Add)` an den Player und leert die Liste. Damit ist `addInstruction(..., Add)` der belegte server-/client-synchronisierte Impulspfad.

MMOSkillTrees Hotbar-Filter und Ability-Command rufen Ability-Aktivierung innerhalb `World.execute(...)` auf. `ActiveAbilityService.tryActivate(...)` ruft `AbilityEffect.execute(...)` dagegen ohne eigenen `Store.assertThread()`- oder `Store.isInThread()`-Guard auf. Der bestehende, nicht registrierte `ShadowSwingAbility`-Prototyp mutiert direkt über `Velocity.addVelocity(...)`; er umgeht damit die Instruction-/Packet-Reconciliation und nutzt den storelokalen `ShadowSwingReconciliationController` noch nicht.

Ergebnis: **ABI-Primitiven READY, produktives Movement-Wiring NO-GO**. Vor einer Aktivierung müssen der reine Reconciliation-Core, Store/Ref-Identität, World-Thread, Component-Readiness, `addInstruction(..., Add)`, ECS-Systemreihenfolge und ein ownership-sicherer Effect-Registrierungsvertrag gemeinsam umgesetzt und danach mit Client/Ingame freigegeben geprüft werden. Es wurde keine Entity erzeugt, keine Komponente gelesen oder mutiert, kein MMOSkillTree-Effect registriert und kein Client gestartet oder gesteuert. Dies ist kein Movement-, Reconciliation- oder Deployment-PASS.

## Shadow-Swing Core→ECS-Instruction-Wiring – 2026-08-10

RED→GREEN ergänzte `ShadowSwingMovementSystem` als pluginlokales `EntityTickingSystem`. Der Queryvertrag umfasst PlayerRef, Transform und Velocity. Der Tick verwirft null/fremde Stores, Nicht-Store-Threads, ungültige Refs, fehlende Komponenten und nicht-endliche Position/Velocity. Nur ein akzeptierter, begrenzter Step des storelokalen `ShadowSwingReconciliationController` erzeugt `Velocity.addInstruction(correction, null, ChangeVelocityType.Add)`; direkte `velocity.addVelocity(...)`-Mutation ist ausgeschlossen. `SystemDependency(Order.BEFORE, PlayerVelocityInstructionSystem.class)` verankert den Producer vor dem Player-Paketconsumer.

Java-25 `mvn clean verify` bestand mit **64 Tests**, 0 Failures, 0 Errors und 0 Skips. Nach der Schema-8-Migration erzeugten zwei unabhängige Builds bytegleich `b9cc1b718538c6329e4abd9f7ab082bd061b28b0033c168a19decfa0c25ac935`.

Exakt dieser finale Schema-8-Hash wurde dependency-complete und loopback-only auf `127.0.0.1:5523` gebootet. Nachtweber-Enable, DamageCause-Readiness (`Physical`, Index 2), erster storegebundener Maintenance-Tick, `Hytale Server Booted!`, pluginlokaler Shutdown und `Shutdown completed!` sind belegt. Alle strikten Linkage-/Setup-/Enable-Fehlerzähler sind 0. Port und Server-JVM waren anschließend frei; kein harter Prozesskill. Status: `NACHTWEBER_SCHEMA8_MOVEMENT_BOOT_PASS`; Bootlog SHA-256 `27240692ee91c7f86e1cf31adeec952b2c1a899a2137edf6976595e42ebc9e2a`.

Dieser Boot verband keinen Spieler und startete keine Core-Session; daher wurden weder Movement noch Damage ausgeführt. Das ECS-System ist registriert und inert. Live-MMOSkillTree-Effectregistrierung bleibt mangels öffentlichem ownership-sicheren Unregistervertrag getrennt. Kein Client-/Ingame- oder Deployment-PASS.

## Owned-Effects-, Damage- und Schema-9-PASS – 2026-08-10

- MMOSkillTree-Patcher RED gegen unverändertes JAR: erwartete Ablehnung (`changed entries: []`).
- Patcher GREEN: `registerIfAbsent` und exakt-instance `unregister`; exakt eine zusätzliche geänderte Klasse; reproduzierbarer SHA-256 `076108affe785c66e4a470c6a77779a349e83e3e2c532f661e18769b37a4e371`.
- Drei live registrierte Effects dispatchen ausschließlich über die identity-gebundene Gameplay-Runtime. Shadow Swing startet den Core, Cocoon liefert 2,0 Sekunden Immobilisierung und bindet den Owner-Ref.
- `VenomDamageSystem` queryt reale NPC-Targets und darf den EndlessLeveling-DoT nur mit validem Owner-/Target-Ref im identischen Store ausführen. Disconnect, WorldRemoval und Shutdown löschen Bindings.
- Java 25 `mvn clean verify`: **69 Tests**, 0 Failures, 0 Errors, 0 Skips.
- Nachtweber-Doppelbuild bytegleich; SHA-256 `53257e2d9b4de121ef3ea95e841d1bcd8b9a43f0372eb2d11b43eaf2da0c7683`.
- Isolierter dependency-complete Boot mit Hytale 0.5.7, EndlessLeveling, EEM 3.10.1, ZiggfreedCommon 1.3.0, gepatchtem MMOSkillTree und Nachtweber auf `127.0.0.1:5523`: `NACHTWEBER_OWNED_EFFECTS_SCHEMA9_BOOT_PASS`.
- Bestätigt: Plugin enabled, drei owned Effects registriert, `Physical` Index 2 ready, Server booted, erster Maintenance-Tick, Nachtweber-Shutdown vor MMOSkillTree-Shutdown, keine Nachtweber-SEVERE, kein `NoSuchMethod`, keine Registrykollision.
- Bootlog-SHA-256 `1ef7382cbff0518928d7ccc095b9e6dcc8471457be3e5c6fecb1e6081c26e47b`; Evidenz `.hermes/runtime/nightweaver-owned-effects-schema9-boot-20260810/evidence/`.

Nicht ausgeführt: Client, echte Skillaktivierung, reale Movement-/Damageausführung, realer Disconnect, Zwei-Spieler-Isolation und Deployment. Der erste Bootversuch ohne `ZiggfreedCommon` war ein dependency-incomplete FAIL; der frische vollständige Wiederholungslauf bestand. Er wird nicht als Produktfehler umgedeutet.
