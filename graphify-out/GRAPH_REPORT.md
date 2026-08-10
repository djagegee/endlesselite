# Graph Report - endlesselite-github  (2026-08-10)

## Corpus Check
- 354 files · ~1,342,460 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2240 nodes · 5877 edges · 143 communities (90 shown, 53 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 472 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `445cd6f5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- com.hypixel.hytale.server.core.universe.world.storage.EntityStore
- org.junit.jupiter.api.Test
- HymannMmoBridge
- com.hypixel.hytale.component.Store
- HymannProfileProgressSystem
- HymannRegistrar
- com.hypixel.hytale.component.ArchetypeChunk
- NekrotoxinRuntimeLedger
- com.hypixel.hytale.component.system.tick.EntityTickingSystem
- Konflikte und Entscheidungen
- .isHymann
- EndlessDetailsBridge
- BlackThreadService
- ZipFile
- .apply
- ShadowSwingReconciliationController
- .onDirectHit
- com.hypixel.hytale.component.CommandBuffer
- .setup
- VenomLedger
- java.util.function.LongSupplier
- com.ziggfreed.mmoskilltree.ability.ParamSpec
- .NachtweberLedgerStoreRuntime
- NachtweberLedgerStoreRuntime
- NachtweberRuntimeCoordinator
- SeuchenweberRuntimeConfig
- .build
- com.hypixel.hytale.server.core.universe.PlayerRef
- AstralRiftRuntimeLedger
- EndlessDetailsData
- ShadowSwingService
- SeuchenweberAbilityContracts
- NekrotoxinDamageSystem
- com.ziggfreed.mmoskilltree.ability.AbilityEffect
- CardDescriptionPatcher
- AstralRiftPulseSystem
- SeuchenweberConfigMigration
- com.google.gson.JsonObject
- hymann/src/main/resources/manifest.json
- GameplayConcern
- Nachtweber – Testbericht 0.1.0
- endless-book/src/main/resources/manifest.json
- PersonalClaimsPage
- .start
- PesthauchAuraSystem
- .treeUnlocks
- seuchenweber/src/main/resources/manifest.json
- EndlessDetailsPresenter
- EndlessBookPlugin
- NachtweberMmoContract
- RecordingRegistrationPort
- .apply
- NachtweberRuntimeWiring
- nachtweber/src/main/resources/manifest.json
- .scan
- .closestVisibleHostile
- com.google.gson.Gson
- .create
- Action
- OwnerEntityBindingRegistry
- HymannAbilityBindingPersistenceSystem
- CastStatus
- .decimal
- ManagedModule
- EndlessEliteCoreTest
- core/src/main/resources/manifest.json
- .handleDataEvent
- StoreBoundHuntingCocoonAbility
- Seuchenweber Runtime Acceptance
- .tick
- StoreScopedState
- EndlessEliteConfigArtifactsTest
- portal-spawn/src/main/snapshot/manifest.json
- CoreContractTest
- BookInteraction.java
- com.hypixel.hytale.server.core.plugin.JavaPlugin
- Values
- Nachtweber – Core-Design 0.1.0
- VenomTickServiceTest
- Seuchenweber – manuelle Client-Abnahme 2026-08-09
- .boundedTargetLimit
- NekrotoxinDamageSystemTest
- SeuchenweberUnlockRules
- .build
- ArtifactContractTest
- NachtweberCompletionTest
- DangerSenseStatus
- NachtweberConfig
- ShadowSwingMovementSystem
- StepStatus
- .shutdown
- .execute
- .secondsToMilliseconds
- DamageBudget
- NativePoisonVisualTier
- .boundedTargetLimit
- SeuchenweberAbilityConfigInstaller
- Action
- SeuchenweberArtifactContractTest
- mjolnir-safety-patch/src/main/snapshot/manifest.json
- NachtweberMaintenanceSystem
- Endless Elite – Testreport
- .applyAbilitySettings
- NachtweberGameplayRuntime
- StartStatus
- WallHunterStatus
- SeuchenweberTreeBonusResolver
- .mayCreateClaim
- setup_local_dependencies.py
- Legacy- und Binär-only-Modfamilien
- Eigene Binär-/Backupfassungen
- DangerSenseService
- NachtweberAbilityCatalog
- mmoskilltree-owned-effect-patcher/verify_patch.py
- PortalSpawnSnapshotTest
- Rift Mage Dungeon
- PersonalClaimsIntegrationTest
- EndlessBookIntegrations
- .execute
- SeuchenweberDamageCause
- mmoskilltree-card-description-patcher/verify_patch.py
- DangerSenseAlertPort
- Seuchenweber-Konfiguration – Schema 4
- SeuchenweberPublicConfigContractTest
- sha256
- HymannStormFuryAbility
- Portal Spawn.Endless Elite – Source Snapshot
- PersonalClaimsContract
- verify_repository.py
- REMOTE_BASELINE.md
- MmoPassiveResolver
- mmoskilltree-card-description-patcher/README.md
- mjolnir-safety-patch/README.md
- snapshot/README.md
- de.shadow:endless-book
- de.shadow.endlesselite:endless-elite-parent
- de.shadow:hymann
- de.shadow:mjolnir-safety-patch
- de.shadow:nachtweber
- de.shadow:portal-spawn
- de.shadow:rift-mage-dungeon
- de.shadow:seuchenweber
- endless-elite-core

## God Nodes (most connected - your core abstractions)
1. `HymannMmoBridge` - 65 edges
2. `NekrotoxinDamageSystem` - 44 edges
3. `HymannProfileProgressSystem` - 40 edges
4. `SeuchenweberMmoBridge` - 30 edges
5. `NachtweberLedgerStoreRuntime` - 29 edges
6. `HymannConfig` - 26 edges
7. `HymannGuardAuraSystem` - 25 edges
8. `NachtweberRuntimeCoordinator` - 25 edges
9. `AstralRiftPulseSystem` - 24 edges
10. `NekrotoxinRuntimeLedger` - 24 edges

## Surprising Connections (you probably didn't know these)
- `HymannSkillLifecycleSystem` --references--> `State`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannSkillLifecycleSystem.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannAbilityBindingPersistenceSystem.java
- `HymannPlugin` --references--> `HymannCombatPassiveSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannCombatPassiveSystem.java
- `HymannPlugin` --references--> `HymannGuardWaveSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannGuardWaveSystem.java
- `HymannPlugin` --references--> `HymannMmoBridge`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannMmoBridge.java
- `HymannPlugin` --references--> `HymannProfileProgressSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannProfileProgressSystem.java

## Import Cycles
- None detected.

## Communities (143 total, 53 thin omitted)

### Community 0 - "com.hypixel.hytale.server.core.universe.world.storage.EntityStore"
Cohesion: 0.06
Nodes (20): com.hypixel.hytale.component.ComponentRegistryProxy, com.hypixel.hytale.component.query.Query, com.hypixel.hytale.logger.HytaleLogger, com.hypixel.hytale.server.core.universe.world.storage.EntityStore, HymannArmamentMasterySystem, HymannBossBreakerDamageSystem, HymannBossBreakerSystem, HymannCriticalAttributeSystem (+12 more)

### Community 1 - "org.junit.jupiter.api.Test"
Cohesion: 0.04
Nodes (20): HymannMeleeAndDescriptionTest, HymannProgressionContractTest, ShadowSwingMovementSystemTest, MmoSkillTreePatcherContractTest, NativePoisonVisualContractTest, NekrotoxinEndlessLevelingDamageContractTest, PesthauchAuraContractTest, SeuchenweberAbilityContractsTest (+12 more)

### Community 2 - "HymannMmoBridge"
Cohesion: 0.09
Nodes (13): com.airijko.endlessleveling.api.DamageEventListener, com.ziggfreed.mmoskilltree.skilltree.SkillReward, com.ziggfreed.mmoskilltree.skilltree.SkillRewardType, com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode, HymannMmoBridge, SkillReward, SkillTreeNode, TreeTier (+5 more)

### Community 3 - "com.hypixel.hytale.component.Store"
Cohesion: 0.07
Nodes (16): com.hypixel.hytale.component.Ref, com.hypixel.hytale.component.Store, com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent, ComboState, HymannCombatPassiveSystem, Source, Damage, State (+8 more)

### Community 4 - "HymannProfileProgressSystem"
Cohesion: 0.08
Nodes (8): com.ziggfreed.mmoskilltree.ability.MmoSlot, com.ziggfreed.mmoskilltree.ability.TriggerKey, com.ziggfreed.mmoskilltree.data.SkillComponent, SavedBindings, Context, HymannProfileProgressSystem, Progress, HymannTreeRewards

### Community 5 - "HymannRegistrar"
Cohesion: 0.06
Nodes (10): com.airijko.endlessleveling.classes.CharacterClassDefinition, Ascension, HymannRegistrar, NachtweberClassDefinition, NachtweberLifecycle, RuntimePort, Override, RecordingPort (+2 more)

### Community 6 - "com.hypixel.hytale.component.ArchetypeChunk"
Cohesion: 0.11
Nodes (16): com.airijko.endlessleveling.api.EndlessLevelingAPI, com.airijko.endlessleveling.enums.SkillAttributeType, com.hypixel.hytale.component.ArchetypeChunk, com.hypixel.hytale.component.dependency.Dependency, com.hypixel.hytale.server.core.entity.entities.Player, com.hypixel.hytale.server.core.inventory.InventoryComponent, com.hypixel.hytale.server.core.inventory.ItemStack, com.hypixel.hytale.server.core.modules.entity.damage.Damage (+8 more)

### Community 7 - "NekrotoxinRuntimeLedger"
Cohesion: 0.09
Nodes (10): DueTick, Expiration, NaturalExpiration, NekrotoxinRuntimeLedger, State, TargetExpiration, TickResult, NekrotoxinRuntimeLedgerTest (+2 more)

### Community 8 - "com.hypixel.hytale.component.system.tick.EntityTickingSystem"
Cohesion: 0.13
Nodes (15): com.hypixel.hytale.component.system.tick.EntityTickingSystem, com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent, com.hypixel.hytale.server.core.modules.entity.damage.DamageCause, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap, com.ziggfreed.mmoskilltree.ability.ActiveAbilityService, HymannConfig, HymannDamageScaling (+7 more)

### Community 9 - "Konflikte und Entscheidungen"
Cohesion: 0.05
Nodes (39): 1. MMOSkillTree-Binärvertrag — kontrollierter Superset-Kandidat, 2. Globale MMOSkillTree-Konfigurationswriter — konfliktträchtig, 3. Ability-Registrierung und Reload — konfliktträchtig, 4. Spielerfortschritt — Hymann als zusätzlicher Owner, 5. Event-/Damage-Reihenfolge, 6. Ressourcen und UI, 7. Rift Mage Dungeon, 8. Portal Spawn Snapshot (+31 more)

### Community 10 - ".isHymann"
Cohesion: 0.07
Nodes (12): RelicCheck, Source, HymannGuardWaveSystem, Vector3d, Vector3d, PulseRank, Vector3d, Candidate (+4 more)

### Community 11 - "EndlessDetailsBridge"
Cohesion: 0.11
Nodes (11): AugmentDetailEnricher, FunctionalInterface, Resolver, EndlessDetailsBridge, FatesReader, FunctionalInterface, Override, LevelingReader (+3 more)

### Community 12 - "BlackThreadService"
Cohesion: 0.09
Nodes (17): BlackThreadRules, BlackThreadService, CastOutcome, CastRequest, CastStatus, APPLIED, REJECTED_COOLDOWN, REJECTED_INVALID_TARGET (+9 more)

### Community 13 - "ZipFile"
Cohesion: 0.08
Nodes (10): com.google.gson.JsonArray, java.util.zip.ZipFile, MjolnirSafetyPatchContractArtifactIT, PortalSpawnContractArtifactIT, RiftMageAssetBaselineTest, RiftMageBossAssetTest, RiftMageContractArtifactIT, RiftMagePortalAssetTest (+2 more)

### Community 14 - ".apply"
Cohesion: 0.11
Nodes (4): TimeMath, HuntingCocoonServiceTest, NachtweberCoreContractTest, PassiveVenomServiceTest

### Community 15 - "ShadowSwingReconciliationController"
Cohesion: 0.15
Nodes (8): Session, ShadowSwingReconciliationController, StartOutcome, StartRequest, StepOutcome, StepRequest, ShadowSwingReconciliationRules, ShadowSwingReconciliationControllerTest

### Community 16 - ".onDirectHit"
Cohesion: 0.07
Nodes (21): java.util.function.BooleanSupplier, Override, CastRequest, Ref, Store, PassiveVenomDamageAdapter, PassiveVenomOutcome, PassiveVenomRequest (+13 more)

### Community 17 - "com.hypixel.hytale.component.CommandBuffer"
Cohesion: 0.13
Nodes (7): com.hypixel.hytale.component.CommandBuffer, EndlessClassId, HymannGuardAuraSystem, KnockbackComponent, Vector3d, HymannGuardAuraTickSystem, EndlessLevelingVenomDamageAdapter

### Community 18 - ".setup"
Cohesion: 0.13
Nodes (7): Entry, NachtweberOwnedEffectLifecycle, Registry, Override, MmoEffectRegistry, NachtweberPlugin, RuntimePort

### Community 19 - "VenomLedger"
Cohesion: 0.09
Nodes (9): EntanglementLedger, State, HuntingCocoonRules, PassiveVenomRules, Key, PassiveVenomService, DueTick, State (+1 more)

### Community 20 - "java.util.function.LongSupplier"
Cohesion: 0.20
Nodes (8): com.hypixel.hytale.server.core.modules.entity.component.HeadRotation, com.ziggfreed.mmoskilltree.ability.AbilityDefinition, com.ziggfreed.mmoskilltree.ability.AbilityResult, com.ziggfreed.mmoskilltree.ability.CasterContext, java.util.function.LongSupplier, BlackThreadAbility, Vector3d, StoreBoundShadowSwingAbility

### Community 21 - "com.ziggfreed.mmoskilltree.ability.ParamSpec"
Cohesion: 0.10
Nodes (11): com.ziggfreed.mmoskilltree.ability.ParamSpec, Override, BlackThreadAbilityContracts, ParamEntry, Override, HuntingCocoonAbilityContracts, ParamEntry, Override (+3 more)

### Community 22 - ".NachtweberLedgerStoreRuntime"
Cohesion: 0.12
Nodes (13): java.util.function.LongConsumer, FunctionalInterface, VenomDamagePort, FunctionalInterface, VenomPowerProvider, VenomTickRules, TickOutcome, TickStatus (+5 more)

### Community 23 - "NachtweberLedgerStoreRuntime"
Cohesion: 0.17
Nodes (4): Override, NachtweberLedgerStoreRuntime, CastRequest, NachtweberMultiplayerIsolationPreflightTest

### Community 24 - "NachtweberRuntimeCoordinator"
Cohesion: 0.18
Nodes (4): NachtweberRuntimeCoordinator, NachtweberStoreStateRegistry, EqualStore, NachtweberRuntimeWiringTest

### Community 25 - "SeuchenweberRuntimeConfig"
Cohesion: 0.13
Nodes (4): OperatorConfigNumbers, SeuchenweberRuntimeConfig, OperatorConfigNumbersTest, SeuchenweberRuntimeConfigTest

### Community 26 - ".build"
Cohesion: 0.16
Nodes (8): BookSettings, CoreConfig, EndlessBookConfig, Localization, ActionData, EndlessBookPage, Override, LinkDefinition

### Community 27 - "com.hypixel.hytale.server.core.universe.PlayerRef"
Cohesion: 0.17
Nodes (8): com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand, com.hypixel.hytale.server.core.command.system.CommandContext, com.hypixel.hytale.server.core.Message, com.hypixel.hytale.server.core.universe.PlayerRef, com.hypixel.hytale.server.core.universe.world.World, HymannClaimCommand, Override, PersonalClaimsCommand

### Community 28 - "AstralRiftRuntimeLedger"
Cohesion: 0.14
Nodes (4): AstralRiftRuntimeLedger, RiftPulse, RiftState, AstralRiftRuntimeLedgerTest

### Community 29 - "EndlessDetailsData"
Cohesion: 0.17
Nodes (4): EndlessDetailsData, JsonObject, EndlessDetailsBridgeTest, EndlessDetailsDataTest

### Community 30 - "ShadowSwingService"
Cohesion: 0.12
Nodes (11): ShadowSwingAbility, ShadowSwingOutcome, ShadowSwingRequest, ShadowSwingRules, ShadowSwingService, ShadowSwingStatus, APPLIED, COOLDOWN (+3 more)

### Community 31 - "SeuchenweberAbilityContracts"
Cohesion: 0.13
Nodes (9): AstralRiftAbility, Override, Vector3d, Contract, ParamEntry, SeuchenweberAbilityContracts, ContractBoundEffect, Override (+1 more)

### Community 32 - "NekrotoxinDamageSystem"
Cohesion: 0.24
Nodes (4): RuntimeState, DiagnosisKey, Override, NekrotoxinDamageSystem

### Community 33 - "com.ziggfreed.mmoskilltree.ability.AbilityEffect"
Cohesion: 0.19
Nodes (9): com.ziggfreed.mmoskilltree.ability.AbilityEffect, HuntingCocoonAbility, HuntingCocoonService, NachtweberAbilityRegistry, FunctionalInterface, VenomImmunityResolver, FakeRegistry, Override (+1 more)

### Community 34 - "CardDescriptionPatcher"
Cohesion: 0.19
Nodes (5): Label, OwnedEffectRegistryPatcher, CardDescriptionPatcher, org.objectweb.asm.Label, org.objectweb.asm.MethodVisitor

### Community 35 - "AstralRiftPulseSystem"
Cohesion: 0.16
Nodes (4): AstralRiftPulseSystem, Override, SeuchenweberMaintenanceSystem, SeuchenweberPlugin

### Community 36 - "SeuchenweberConfigMigration"
Cohesion: 0.18
Nodes (5): java.util.regex.Pattern, Migration, Result, SeuchenweberConfigMigration, SeuchenweberConfigMigrationTest

### Community 37 - "com.google.gson.JsonObject"
Cohesion: 0.35
Nodes (3): com.google.gson.JsonObject, JsonObject, NachtweberMmoInstaller

### Community 38 - "hymann/src/main/resources/manifest.json"
Cohesion: 0.11
Nodes (17): Authors, Dependencies, Airijko:EndlessLevelingCore, narwhals:Perfect Utils, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault (+9 more)

### Community 39 - "GameplayConcern"
Cohesion: 0.16
Nodes (9): EndlessEliteCorePlugin, Override, GameplayConcern, ACTIVE_ABILITIES, MOB_SCALING, PERSISTENCE, PLAYER_PROGRESSION, PLAYER_UI (+1 more)

### Community 40 - "Nachtweber – Testbericht 0.1.0"
Cohesion: 0.12
Nodes (16): DamageCause-AssetStore-Readiness – 2026-08-09, Finaler Abschlussnachweis, Isolierter Serverboot – 2026-08-09, Klassenwechsel-ABI-Abgrenzung, Nachtweber – Testbericht 0.1.0, Owned-Effects-, Damage- und Schema-9-PASS – 2026-08-10, Owner-Cleanup bei PlayerDisconnectEvent – 2026-08-09, Reproduzierbarer Hash – isolierter Serverboot – 2026-08-09 (+8 more)

### Community 41 - "endless-book/src/main/resources/manifest.json"
Cohesion: 0.12
Nodes (16): Authors, Dependencies, Airijko:EndlessGuilds, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+8 more)

### Community 42 - "PersonalClaimsPage"
Cohesion: 0.31
Nodes (8): com.airijko.endlessguilds.guild.claim.ClaimCell, com.airijko.endlessguilds.guild.claim.ClaimService, com.hypixel.hytale.codec.builder.BuilderCodec, com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage, com.hypixel.hytale.server.core.ui.builder.UICommandBuilder, com.hypixel.hytale.server.core.ui.builder.UIEventBuilder, ActionData, PersonalClaimsPage

### Community 44 - "PesthauchAuraSystem"
Cohesion: 0.17
Nodes (4): RuntimeState, OwnerIntervalBudget, Override, PesthauchAuraSystem

### Community 45 - ".treeUnlocks"
Cohesion: 0.14
Nodes (4): SeuchenweberAbilityConfigInstallerTest, SeuchenweberClientLocalizationTest, SeuchenweberMmoBridgeTest, SeuchenweberPresentationInstallerTest

### Community 46 - "seuchenweber/src/main/resources/manifest.json"
Cohesion: 0.12
Nodes (15): Authors, Dependencies, Airijko:EndlessLevelingCore, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+7 more)

### Community 47 - "EndlessDetailsPresenter"
Cohesion: 0.28
Nodes (4): DetailRow, EndlessDetailsPresenter, JsonObject, EndlessDetailsPresenterTest

### Community 48 - "EndlessBookPlugin"
Cohesion: 0.27
Nodes (4): com.hypixel.hytale.server.core.inventory.container.ItemContainer, ItemStack, EndlessBookPlugin, Override

### Community 49 - "NachtweberMmoContract"
Cohesion: 0.21
Nodes (3): NachtweberMmoContract, Unlock, NachtweberMmoInstallerTest

### Community 50 - "RecordingRegistrationPort"
Cohesion: 0.19
Nodes (4): NachtweberStoreRuntime, Override, RecordingRegistrationPort, RecordingRuntime

### Community 51 - ".apply"
Cohesion: 0.21
Nodes (6): WallHunterOutcome, WallHunterRequest, WallHunterRules, Ref, Store, WallHunterService

### Community 52 - "NachtweberRuntimeWiring"
Cohesion: 0.24
Nodes (7): DamageCauseReadinessProbe, Result, Status, INCONSISTENT, READY, UNAVAILABLE, NachtweberRuntimeWiring

### Community 53 - "nachtweber/src/main/resources/manifest.json"
Cohesion: 0.14
Nodes (13): Authors, Dependencies, Airijko:EndlessLevelingCore, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+5 more)

### Community 54 - ".scan"
Cohesion: 0.27
Nodes (5): DangerCandidate, DangerSenseOutcome, DangerSenseRuntimeAdapter, Ref, Store

### Community 55 - ".closestVisibleHostile"
Cohesion: 0.22
Nodes (4): Candidate, NachtweberTargeting, Override, StoreBoundBlackThreadAbility

### Community 58 - "Action"
Cohesion: 0.18
Nodes (7): Action, BACK, DETAILS, LINK, NONE, BookNavigation, BookNavigationTest

### Community 61 - "CastStatus"
Cohesion: 0.20
Nodes (10): CastOutcome, CastStatus, APPLIED, REJECTED_COOLDOWN, REJECTED_IMMUNE, REJECTED_INSUFFICIENT_ENTANGLEMENT, REJECTED_INVALID_TARGET, REJECTED_NOT_SERVER (+2 more)

### Community 64 - "EndlessEliteCoreTest"
Cohesion: 0.22
Nodes (3): ModuleCatalog, ModuleDescriptor, EndlessEliteCoreTest

### Community 65 - "core/src/main/resources/manifest.json"
Cohesion: 0.18
Nodes (10): Authors, Dependencies, Description, DisabledByDefault, Group, IncludesAssetPack, Main, Name (+2 more)

### Community 66 - ".handleDataEvent"
Cohesion: 0.31
Nodes (5): ClaimResult, UiLanguage, ENGLISH, GERMAN, UnclaimResult

### Community 67 - "StoreBoundHuntingCocoonAbility"
Cohesion: 0.29
Nodes (4): FunctionalInterface, OwnerEntityBindingPort, Override, StoreBoundHuntingCocoonAbility

### Community 68 - "Seuchenweber Runtime Acceptance"
Cohesion: 0.20
Nodes (9): Audit hardening, Core benchmark scope, Evidence classes, Explicit exclusions, Final server evidence, Lifecycle release evidence, Release gate, Seuchenweber Runtime Acceptance (+1 more)

### Community 69 - ".tick"
Cohesion: 0.27
Nodes (4): Override, Vector3d, Candidate, SeuchenweberTargeting

### Community 70 - "StoreScopedState"
Cohesion: 0.40
Nodes (3): StoreScopedState, EqualStore, StoreScopedStateTest

### Community 72 - "portal-spawn/src/main/snapshot/manifest.json"
Cohesion: 0.20
Nodes (9): Authors, Dependencies, DisabledByDefault, Group, IncludesAssetPack, LoadBefore, Name, OptionalDependencies (+1 more)

### Community 74 - "BookInteraction.java"
Cohesion: 0.33
Nodes (6): com.hypixel.hytale.protocol.InteractionType, com.hypixel.hytale.server.core.entity.InteractionContext, com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction, com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler, BookInteraction, Override

### Community 77 - "Nachtweber – Core-Design 0.1.0"
Cohesion: 0.22
Nodes (8): BLACK_THREAD Gameplay-Slice, Fail-closed Runtime-Adapter, HUNTING_COCOON Gameplay-Slice, Integrierter Abschluss-Slice, MMOSkillTree-Stagingvertrag, Nachtweber – Core-Design 0.1.0, Storegebundene Runtime-Maintenance, VENOM_TICK Damage-Slice

### Community 78 - "VenomTickServiceTest"
Cohesion: 0.22
Nodes (3): EndlessLevelingVenomPowerProvider, Override, VenomTickServiceTest

### Community 79 - "Seuchenweber – manuelle Client-Abnahme 2026-08-09"
Cohesion: 0.22
Nodes (8): Bekannte Fremdmodprobleme im Fullstack, Build und Deployment, Ergebnis, Fullstack-Boot, Manuelle PASS-Ergebnisse, Offener Multiplayer-Gate-Test, Seuchenweber – manuelle Client-Abnahme 2026-08-09, Visuelle Evidenz

### Community 83 - ".build"
Cohesion: 0.39
Nodes (3): ActionData, EndlessBookDetailsPage, Override

### Community 85 - "NachtweberCompletionTest"
Cohesion: 0.29
Nodes (3): com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent, WallHunterRuntimeAdapter, NachtweberCompletionTest

### Community 86 - "DangerSenseStatus"
Cohesion: 0.25
Nodes (7): DangerSenseStatus, ALERT, COOLDOWN, INVALID_REQUEST, NOT_AUTHORIZED, NOT_UNLOCKED, QUIET

### Community 88 - "ShadowSwingMovementSystem"
Cohesion: 0.36
Nodes (3): Override, Vector3d, ShadowSwingMovementSystem

### Community 89 - "StepStatus"
Cohesion: 0.25
Nodes (8): StepStatus, CORRECTION, EXPIRED, INVALID_REQUEST, NO_SESSION, REACHED, STALE_STEP, TETHER_BROKEN

### Community 94 - "NativePoisonVisualTier"
Cohesion: 0.29
Nodes (6): forStacks(), NativePoisonVisualTier, NONE, POISON_I, POISON_II, POISON_III

### Community 97 - "Action"
Cohesion: 0.29
Nodes (6): Action, ADD, NONE, REMOVE, RESTART, SeuchenweberClassAuraVisuals

### Community 99 - "mjolnir-safety-patch/src/main/snapshot/manifest.json"
Cohesion: 0.25
Nodes (7): Authors, Description, Group, IncludesAssetPack, Name, ServerVersion, Version

### Community 100 - "NachtweberMaintenanceSystem"
Cohesion: 0.33
Nodes (3): com.hypixel.hytale.component.system.tick.TickingSystem, Override, NachtweberMaintenanceSystem

### Community 101 - "Endless Elite – Testreport"
Cohesion: 0.29
Nodes (6): Endless Elite – Testreport, Maven-Reactor, Repository- und Distributionsgates, Reproduzierte lokale Abhängigkeit, Toolchain, Warnungen und offene Abnahme

### Community 104 - "StartStatus"
Cohesion: 0.29
Nodes (7): StartStatus, ANCHOR_OUT_OF_RANGE, CLOSED, INVALID_REQUEST, NOT_AUTHORIZED, REATTACH_RATE_LIMIT, STARTED

### Community 105 - "WallHunterStatus"
Cohesion: 0.29
Nodes (6): WallHunterStatus, CLIMBING, INACTIVE, INVALID_REQUEST, NOT_AUTHORIZED, NOT_UNLOCKED

### Community 107 - ".mayCreateClaim"
Cohesion: 0.38
Nodes (3): FunctionalInterface, MembershipLookup, PersonalClaimsAccess

### Community 108 - "setup_local_dependencies.py"
Cohesion: 0.57
Nodes (6): digest(), java_tool(), main(), maven_tool(), Path, run()

### Community 109 - "Legacy- und Binär-only-Modfamilien"
Cohesion: 0.33
Nodes (5): Buildwerkzeuge, Legacy- und Binär-only-Modfamilien, Nur Binärbackup gefunden, Recovery-Regel, Vollständige aktuelle Quellen vorhanden

### Community 110 - "Eigene Binär-/Backupfassungen"
Cohesion: 0.33
Nodes (5): Eigene Binär-/Backupfassungen, Integrationsregel, Mod-Inventur, Nur binär belegt und deshalb nicht als produktive Quelle integriert, Vollständige Quell- und Contentmodule

### Community 113 - "mmoskilltree-owned-effect-patcher/verify_patch.py"
Cohesion: 0.60
Nodes (5): javap(), javap_executable(), main(), method(), Path

### Community 115 - "Rift Mage Dungeon"
Cohesion: 0.33
Nodes (5): Current state, Fail-closed alpha gate, Native ownership and scaling, Rift Mage Dungeon, Verification evidence

### Community 120 - "mmoskilltree-card-description-patcher/verify_patch.py"
Cohesion: 0.60
Nodes (4): javap(), main(), method_slice(), Path

### Community 122 - "Seuchenweber-Konfiguration – Schema 4"
Cohesion: 0.50
Nodes (3): Migration von Schema 3, Pesthauch, Seuchenweber-Konfiguration – Schema 4

### Community 124 - "sha256"
Cohesion: 0.67
Nodes (3): main(), Path, sha256()

## Knowledge Gaps
- **266 isolated node(s):** `de.shadow:hymann`, `Group`, `Name`, `Version`, `Description` (+261 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **53 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HymannMmoBridge` connect `HymannMmoBridge` to `com.hypixel.hytale.server.core.universe.world.storage.EntityStore`, `com.hypixel.hytale.component.Store`, `HymannProfileProgressSystem`, `HymannRegistrar`, `com.hypixel.hytale.component.ArchetypeChunk`, `com.hypixel.hytale.component.system.tick.EntityTickingSystem`, `.isHymann`, `HymannAbilityBindingPersistenceSystem`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Why does `HuntingCocoonService` connect `com.ziggfreed.mmoskilltree.ability.AbilityEffect` to `.apply`, `.onDirectHit`, `VenomLedger`, `.NachtweberLedgerStoreRuntime`, `NachtweberLedgerStoreRuntime`, `CastStatus`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **What connects `de.shadow:hymann`, `Group`, `Name` to the rest of the system?**
  _266 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `com.hypixel.hytale.server.core.universe.world.storage.EntityStore` be split into smaller, more focused modules?**
  _Cohesion score 0.059096459096459095 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.03733815115928937 - nodes in this community are weakly interconnected._
- **Should `HymannMmoBridge` be split into smaller, more focused modules?**
  _Cohesion score 0.08772635814889336 - nodes in this community are weakly interconnected._
- **Should `com.hypixel.hytale.component.Store` be split into smaller, more focused modules?**
  _Cohesion score 0.07242063492063493 - nodes in this community are weakly interconnected._