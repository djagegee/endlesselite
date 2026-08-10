# Build, Test und lokale Abhängigkeiten

## Voraussetzungen

- JDK 25 (`JAVA_HOME` gesetzt)
- Maven 3.9+
- lokale Hytale-Serverinstallation mit:
  - `HytaleServer.jar`
  - `mods/EndlessLeveling.jar`
  - `mods/MMOSkillTree-1.5.2.jar`
  - `mods/EndlessGuilds-1.13.0.jar`
  - `mods/Perfect Utils-1.1.0.jar`
- für Seuchenwebers historische ABI-Baseline zusätzlich:
  - `disabled-mods/baseline-isolation-2026-08-05/EndlessLeveling.jar`
  - `disabled-mods/baseline-isolation-2026-08-05/MMOSkillTree-1.5.2.jar`

Lokale Abhängigkeiten und Drittanbieter-JARs werden nicht eingecheckt.

## 1. Nachtweber-Abhängigkeit reproduzieren

Windows-Beispiel:

```bash
python tools/setup_local_dependencies.py \
  --server-root "C:/Pfad/zum/Hytale-Server" \
  --maven "C:/Pfad/zu/apache-maven/bin/mvn.cmd"
```

Das Skript:

1. verlangt den bekannten Stock-MMOSkillTree-1.5.2-Hash,
2. lädt ASM 9.8 mit gepinntem SHA-256,
3. kompiliert den mitgesicherten Patcher,
4. erzeugt das additive Owned-Effects-JAR,
5. prüft Entryset und Bytecode mit `javap`,
6. installiert es lokal als `com.ziggfreed:mmo-skill-tree:1.5.2-owned-local`.

Erwarteter Marker:

```text
ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS
```

## 2. Gesamtprojekt bauen

```bash
mvn -Dhytale.server.root="C:/Pfad/zum/Hytale-Server" clean verify
```

Ohne Override verwendet der Parent für Agegees lokale Umgebung:

```text
C:/Users/agege/Desktop/LOKAL SERVER
```

Der Reactor enthält:

1. `endless-elite-parent`
2. `endless-book`
3. `hymann`
4. `nachtweber`
5. `seuchenweber`
6. `rift-mage-dungeon`

## 3. Repository-Gate

```bash
python tools/verify_repository.py
```

Erwarteter Marker:

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
```

## 4. Buildartefakte sammeln

Nach einem grünen Reactor:

```bash
python tools/collect_distribution.py
```

Erwarteter Marker:

```text
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Die Ausgabe liegt ignoriert unter `dist/`. Das Manifest bezeichnet die JARs nur als **buildverifiziert**. Es setzt ausdrücklich:

```json
{
  "deploymentApproved": false,
  "deploymentPerformed": false
}
```

## Aktueller verifizierter Lauf

- Java: 25.0.4
- Maven: 3.9.16
- Reactor: 6/6 Projekte SUCCESS
- Tests: 206
- Failures: 0
- Errors: 0
- Skipped: 0
- `ENDLESS_ELITE_REPOSITORY_VERIFY_PASS`
- `ENDLESS_ELITE_DISTRIBUTION_PASS`
- `ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS`

Die Hytale-API erzeugt in EndlessBook, Hymann und Seuchenweber Warnungen zu als veraltet markierten APIs. Diese Warnungen sind dokumentierte technische Schulden, keine im Build ignorierten Testfehler.
