# Build, tests, and local dependencies

## Prerequisites

- JDK 25 (`JAVA_HOME` set)
- Maven 3.9+
- a local Hytale server installation containing:
  - `HytaleServer.jar`
  - `mods/EndlessLeveling.jar`
  - `mods/MMOSkillTree-1.5.2.jar`
  - `mods/EndlessGuilds-1.13.0.jar`
  - `mods/Perfect Utils-1.1.0.jar`

Local dependencies and third-party JARs are never committed.

## 1. Bootstrap local Maven dependencies

Windows example:

```bash
python tools/setup_local_dependencies.py \
  --server-root "C:/Path/to/Hytale-Server" \
  --maven "C:/Path/to/apache-maven/bin/mvn.cmd"
```

The script:

1. verifies all five proprietary source JARs against pinned SHA-256 values;
2. installs them into the local Maven repository under explicit `*-local` coordinates;
3. downloads ASM 9.8 only after verifying its pinned SHA-256;
4. compiles the preserved MMOSkillTree owned-effect patcher;
5. produces and bytecode-verifies the additive owned-effects JAR;
6. installs the derived JAR as `com.ziggfreed:mmo-skill-tree:1.5.2-owned-local`.

Expected marker:

```text
ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS
```

This bootstrap is fail-closed: a missing file or unexpected hash stops before that artifact is installed. Reactor POMs use normal `provided` dependencies and contain no `systemPath` or user-specific absolute path.

## 2. Build the complete project

After the bootstrap, no local server-path property is required:

```bash
mvn clean verify
```

The default reactor validates committed SHA-256 evidence for the native Rift Mage references. To additionally compare those references byte-for-byte against a local original asset archive:

```bash
mvn -Dhytale.assets.zip="C:/Path/to/Assets.zip" -pl modules/content/rift-mage-dungeon test
```

The reactor contains:

1. `endless-elite-parent`
2. `endless-elite-core`
3. `endless-book`
4. `hymann`
5. `nachtweber`
6. `seuchenweber`
7. `rift-mage-dungeon`
8. `portal-spawn`
9. `mjolnir-safety-patch`

## 3. Repository gate

```bash
python tools/verify_repository.py
```

Expected marker:

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
```

The gate also rejects Maven `systemPath` dependencies and user-specific Windows paths in POM files.

## 4. Collect build artifacts

After a green reactor:

```bash
python tools/collect_distribution.py
```

Expected marker:

```text
ENDLESS_ELITE_DISTRIBUTION_PASS
```

Output is ignored under `dist/`. The manifest calls the JARs **build-verified** only and explicitly sets:

```json
{
  "deploymentApproved": false,
  "deploymentPerformed": false
}
```

## Current verified run

- Java: 25.0.4
- Maven: 3.9.16
- Reactor: 9/9 projects SUCCESS
- Maven tests and integration tests: 233
- Failures: 0
- Errors: 0
- Skipped: 0
- Python bootstrap contract tests: 3 PASS
- `ENDLESS_ELITE_REPOSITORY_VERIFY_PASS`
- `ENDLESS_ELITE_DISTRIBUTION_PASS`
- `ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS`

The Hytale API emits deprecation warnings in EndlessBook, Hymann, and Seuchenweber. These are documented technical debt, not ignored test failures.
