# Build, Testing, and Local Dependencies

## Prerequisites

- JDK 25 (`JAVA_HOME` set)
- Maven 3.9+
- local Hytale server installation with:
  - `HytaleServer.jar`
  - `mods/EndlessLeveling.jar`
  - `mods/MMOSkillTree-1.5.2.jar`
  - `mods/EndlessGuilds-1.13.0.jar`
  - `mods/Perfect Utils-1.1.0.jar`
- additionally, for Seuchenweber's historical ABI baseline:
  - `disabled-mods/baseline-isolation-2026-08-05/EndlessLeveling.jar`
  - `disabled-mods/baseline-isolation-2026-08-05/MMOSkillTree-1.5.2.jar`

Local dependencies and third-party JARs are not checked in.

## 1. Reproduce the Nachtweber Dependency

Windows example:

```bash
python tools/setup_local_dependencies.py \
  --server-root "C:/Path/to/Hytale-Server" \
  --maven "C:/Path/to/apache-maven/bin/mvn.cmd"
```

The script:

1. requires the known stock MMOSkillTree 1.5.2 hash,
2. downloads ASM 9.8 with a pinned SHA-256,
3. compiles the included patcher,
4. produces the additive Owned-Effects JAR,
5. verifies the entry set and bytecode with `javap`,
6. installs it locally as `com.ziggfreed:mmo-skill-tree:1.5.2-owned-local`.

Expected marker:

```text
ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS
```

## 2. Build the Entire Project

```bash
mvn -Dhytale.server.root="C:/Path/to/Hytale-Server" clean verify
```

Without an override, the parent uses the following path for Agegee's local environment:

```text
C:/Users/agege/Desktop/LOKAL SERVER
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

## 3. Repository Gate

```bash
python tools/verify_repository.py
```

Expected marker:

```text
ENDLESS_ELITE_REPOSITORY_VERIFY_PASS
```

## 4. Collect Build Artifacts

After a green reactor run:

```bash
python tools/collect_distribution.py
```

Expected marker:

```text
ENDLESS_ELITE_DISTRIBUTION_PASS
```

The output is stored under the ignored `dist/` directory. The manifest labels the JARs only as **build-verified**. It explicitly sets:

```json
{
  "deploymentApproved": false,
  "deploymentPerformed": false
}
```

## Current Verified Run

- Java: 25.0.4
- Maven: 3.9.16
- Reactor: 9/9 projects SUCCESS
- Tests: 217
- Failures: 0
- Errors: 0
- Skipped: 0
- `ENDLESS_ELITE_REPOSITORY_VERIFY_PASS`
- `ENDLESS_ELITE_DISTRIBUTION_PASS`
- `ENDLESS_ELITE_LOCAL_DEPENDENCY_SETUP_PASS`

The Hytale API emits warnings about APIs marked as deprecated in EndlessBook, Hymann, and Seuchenweber. These warnings are documented technical debt, not test failures ignored by the build.
