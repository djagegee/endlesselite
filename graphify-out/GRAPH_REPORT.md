# Graph Report - endlesselite  (2026-08-10)

## Corpus Check
- 359 files · ~1,346,948 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2308 nodes · 6021 edges · 137 communities (95 shown, 42 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 459 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `95671594`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .setup
- org.junit.jupiter.api.Test
- HymannMmoBridge
- com.hypixel.hytale.component.Store
- HymannProfileProgressSystem
- .values
- com.hypixel.hytale.server.core.universe.PlayerRef
- NekrotoxinRuntimeLedger
- com.hypixel.hytale.component.query.Query
- Konflikte und Entscheidungen
- .execute
- EndlessDetailsBridge
- BlackThreadService
- RiftMageTechnodistrictContractTest
- VenomLedger
- ShadowSwingReconciliationController
- .onDirectHit
- com.hypixel.hytale.server.core.universe.world.storage.EntityStore
- .setup
- NachtweberLedgerStoreRuntime
- com.ziggfreed.mmoskilltree.ability.ParamSpec
- VenomDamageCause
- .NachtweberLedgerStoreRuntime
- com.ziggfreed.mmoskilltree.ability.AbilityEffect
- NachtweberRuntimeWiringTest
- SeuchenweberRuntimeConfig
- .build
- com.hypixel.hytale.server.core.plugin.JavaPlugin
- AstralRiftRuntimeLedger
- EndlessDetailsData
- .apply
- .execute
- NekrotoxinDamageSystem
- HymannAbilityBindingPersistenceSystem
- CardDescriptionPatcher
- HymannCombatPassiveSystem.java
- SeuchenweberConfigMigration
- com.google.gson.JsonObject
- hymann/src/main/resources/manifest.json
- EndlessEliteCoreTest
- Nachtweber – Test Report 0.1.0
- endless-book/src/main/resources/manifest.json
- PersonalClaimsPage
- java.util.function.LongSupplier
- PesthauchAuraSystem
- com.ziggfreed.mmoskilltree.data.SkillComponent
- seuchenweber/src/main/resources/manifest.json
- EndlessDetailsPresenter
- EndlessBookPlugin
- .setup
- NachtweberPlugin.java
- StepStatus
- com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
- nachtweber/src/main/resources/manifest.json
- .scan
- .tick
- .treeUnlocks
- com.airijko.endlessleveling.classes.CharacterClassDefinition
- Action
- OwnerEntityBindingRegistry
- BookInteraction.java
- HuntingCocoonService
- NachtweberMmoContract
- SeuchenweberPlugin
- NekrotoxinDamageSystemTest
- core/src/main/resources/manifest.json
- .build
- HymannPlugin.java
- Seuchenweber Runtime Acceptance
- ArtifactContractTest
- LocalDependencySetupTest
- EndlessEliteConfigArtifactsTest
- portal-spawn/src/main/snapshot/manifest.json
- CoreContractTest
- PersonalClaimsPage.java
- HymannCriticalAttributeSystem
- com.hypixel.hytale.logger.HytaleLogger
- Nightweaver – Core Design 0.1.0
- com.hypixel.hytale.server.core.modules.entity.damage.Damage
- Seuchenweber – Manual Client Acceptance 2026-08-09
- .boundedTargetLimit
- com.google.gson.Gson
- SeuchenweberUnlockRules
- .mayCreateClaim
- mmoskilltree-card-description-patcher/verify_patch.py
- OwnedRegistryLifecycle
- PortalSpawnSnapshotTest
- NachtweberConfig
- .execute
- VenomTickServiceTest
- HymannThunderStepArrivalSystem
- HymannThunderAegisAbility.java
- .secondsToMilliseconds
- DamageBudget
- NativePoisonVisualTier
- .boundedTargetLimit
- HymannGuardWaveSystem
- Action
- SeuchenweberArtifactContractTest
- mjolnir-safety-patch/src/main/snapshot/manifest.json
- .effects
- Endless Elite test report
- SeuchenweberAbilityConfigInstaller
- AugmentDetailEnricher
- StartStatus
- .applyAbilitySettings
- StoreBoundShadowSwingAbility
- ModuleDescriptor
- setup_local_dependencies.py
- Legacy- und Binär-only-Modfamilien
- Eigene Binär-/Backupfassungen
- .execute
- NachtweberAbilityCatalog
- mmoskilltree-owned-effect-patcher/verify_patch.py
- PersonalClaimsIntegrationTest
- Rift Mage Dungeon
- MmoEffectRegistry
- SeuchenweberClassAuraSystem
- Seuchenweber Configuration – Schema 4
- SeuchenweberPublicConfigContractTest
- sha256
- Portal Spawn.Endless Elite – Source Snapshot
- PersonalClaimsContract
- verify_repository.py
- REMOTE_BASELINE.md
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
7. `HymannPlugin` - 26 edges
8. `HymannGuardAuraSystem` - 25 edges
9. `NachtweberRuntimeCoordinator` - 25 edges
10. `AstralRiftPulseSystem` - 24 edges

## Surprising Connections (you probably didn't know these)
- `HymannSkillLifecycleSystem` --references--> `State`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannSkillLifecycleSystem.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannAbilityBindingPersistenceSystem.java
- `HymannPlugin` --references--> `HymannArmamentMasterySystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannArmamentMasterySystem.java
- `HymannPlugin` --references--> `HymannCombatPassiveSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannCombatPassiveSystem.java
- `HymannPlugin` --references--> `HymannCriticalAttributeSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannCriticalAttributeSystem.java
- `HymannGuardAuraTickSystem` --references--> `HymannGuardAuraSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannGuardAuraTickSystem.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannGuardAuraSystem.java

## Import Cycles
- None detected.

## Communities (137 total, 42 thin omitted)

### Community 0 - ".setup"
Cohesion: 0.07
Nodes (8): HymannBossBreakerDamageSystem, HymannBossBreakerSystem, HymannPassiveAuraSystem, PulseRank, HymannPlugin, HymannSkillLifecycleSystem, HymannStaminaSystem, HymannTreeManaRegenSystem

### Community 1 - "org.junit.jupiter.api.Test"
Cohesion: 0.03
Nodes (23): HymannMeleeAndDescriptionTest, HymannProgressionContractTest, NachtweberAbiContractTest, ShadowSwingMovementSystemTest, MmoSkillTreePatcherContractTest, NativePoisonVisualContractTest, NekrotoxinEndlessLevelingDamageContractTest, PesthauchAuraContractTest (+15 more)

### Community 2 - "HymannMmoBridge"
Cohesion: 0.08
Nodes (13): com.airijko.endlessleveling.api.DamageEventListener, com.ziggfreed.mmoskilltree.skilltree.SkillReward, com.ziggfreed.mmoskilltree.skilltree.SkillRewardType, com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode, HymannMmoBridge, SkillReward, SkillTreeNode, TreeTier (+5 more)

### Community 3 - "com.hypixel.hytale.component.Store"
Cohesion: 0.07
Nodes (14): com.hypixel.hytale.component.Ref, com.hypixel.hytale.component.Store, com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent, Source, Damage, Vector3d, State, ActiveBuff (+6 more)

### Community 4 - "HymannProfileProgressSystem"
Cohesion: 0.15
Nodes (3): Context, HymannProfileProgressSystem, Progress

### Community 6 - "com.hypixel.hytale.server.core.universe.PlayerRef"
Cohesion: 0.20
Nodes (8): com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand, com.hypixel.hytale.server.core.command.system.CommandContext, com.hypixel.hytale.server.core.Message, com.hypixel.hytale.server.core.universe.PlayerRef, com.hypixel.hytale.server.core.universe.world.World, HymannClaimCommand, Override, PersonalClaimsCommand

### Community 7 - "NekrotoxinRuntimeLedger"
Cohesion: 0.08
Nodes (10): DueTick, Expiration, NaturalExpiration, NekrotoxinRuntimeLedger, State, TargetExpiration, TickResult, Result (+2 more)

### Community 8 - "com.hypixel.hytale.component.query.Query"
Cohesion: 0.18
Nodes (13): com.airijko.endlessleveling.api.EndlessLevelingAPI, com.airijko.endlessleveling.enums.SkillAttributeType, com.hypixel.hytale.component.ArchetypeChunk, com.hypixel.hytale.component.CommandBuffer, com.hypixel.hytale.component.ComponentRegistryProxy, com.hypixel.hytale.component.query.Query, com.hypixel.hytale.component.system.tick.EntityTickingSystem, com.hypixel.hytale.server.core.entity.entities.Player (+5 more)

### Community 9 - "Konflikte und Entscheidungen"
Cohesion: 0.05
Nodes (39): 1. MMOSkillTree-Binärvertrag — kontrollierter Superset-Kandidat, 2. Globale MMOSkillTree-Konfigurationswriter — konfliktträchtig, 3. Ability-Registrierung und Reload — ownership-sicher, Runtime-Abnahme offen, 4. Spielerfortschritt — Hymann als zusätzlicher Owner, 5. Event-/Damage-Reihenfolge, 6. Ressourcen und UI, 7. Rift Mage Dungeon, 8. Portal Spawn Snapshot (+31 more)

### Community 10 - ".execute"
Cohesion: 0.39
Nodes (3): Candidate, HymannThunderStepAbility, Vector3d

### Community 11 - "EndlessDetailsBridge"
Cohesion: 0.14
Nodes (8): EndlessDetailsBridge, FatesReader, FunctionalInterface, Override, LevelingReader, NumberCall, ReflectiveFatesReader, ReflectiveLevelingReader

### Community 12 - "BlackThreadService"
Cohesion: 0.07
Nodes (19): BlackThreadRules, BlackThreadService, CastOutcome, CastRequest, CastStatus, APPLIED, REJECTED_COOLDOWN, REJECTED_INVALID_TARGET (+11 more)

### Community 13 - "RiftMageTechnodistrictContractTest"
Cohesion: 0.10
Nodes (6): com.google.gson.JsonArray, NativeAssetEvidence, RiftMageAssetBaselineTest, RiftMageBossAssetTest, RiftMagePortalAssetTest, RiftMageTechnodistrictContractTest

### Community 14 - "VenomLedger"
Cohesion: 0.11
Nodes (8): EntanglementLedger, State, TimeMath, DueTick, State, VenomLedger, HuntingCocoonServiceTest, NachtweberCoreContractTest

### Community 15 - "ShadowSwingReconciliationController"
Cohesion: 0.16
Nodes (9): Override, Session, ShadowSwingReconciliationController, StartOutcome, StartRequest, StepOutcome, StepRequest, ShadowSwingReconciliationRules (+1 more)

### Community 16 - ".onDirectHit"
Cohesion: 0.10
Nodes (17): Ref, Store, PassiveVenomDamageAdapter, PassiveVenomOutcome, PassiveVenomRequest, PassiveVenomRules, Key, PassiveVenomService (+9 more)

### Community 17 - "com.hypixel.hytale.server.core.universe.world.storage.EntityStore"
Cohesion: 0.15
Nodes (7): com.hypixel.hytale.server.core.universe.world.storage.EntityStore, Source, EndlessClassId, HymannGuardAuraSystem, KnockbackComponent, Vector3d, Source

### Community 18 - ".setup"
Cohesion: 0.17
Nodes (4): Override, MmoEffectRegistry, NachtweberPlugin, RuntimePort

### Community 19 - "NachtweberLedgerStoreRuntime"
Cohesion: 0.17
Nodes (3): NachtweberLedgerStoreRuntime, CastRequest, NachtweberMultiplayerIsolationPreflightTest

### Community 20 - "com.ziggfreed.mmoskilltree.ability.ParamSpec"
Cohesion: 0.10
Nodes (12): com.ziggfreed.mmoskilltree.ability.ParamSpec, Override, BlackThreadAbilityContracts, ParamEntry, Override, HuntingCocoonAbilityContracts, ParamEntry, Override (+4 more)

### Community 21 - "VenomDamageCause"
Cohesion: 0.29
Nodes (5): VenomDamageCause, DIRECT_HIT, ENVIRONMENT, REFLECTION, VENOM_TICK

### Community 22 - ".NachtweberLedgerStoreRuntime"
Cohesion: 0.11
Nodes (13): java.util.function.LongConsumer, FunctionalInterface, VenomDamagePort, FunctionalInterface, VenomPowerProvider, VenomTickRules, TickOutcome, TickStatus (+5 more)

### Community 23 - "com.ziggfreed.mmoskilltree.ability.AbilityEffect"
Cohesion: 0.24
Nodes (9): com.hypixel.hytale.server.core.modules.entity.component.HeadRotation, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent, com.ziggfreed.mmoskilltree.ability.AbilityDefinition, com.ziggfreed.mmoskilltree.ability.AbilityEffect, com.ziggfreed.mmoskilltree.ability.AbilityResult, com.ziggfreed.mmoskilltree.ability.CasterContext, BlackThreadAbility, HuntingCocoonAbility (+1 more)

### Community 24 - "NachtweberRuntimeWiringTest"
Cohesion: 0.13
Nodes (5): NachtweberStoreRuntime, NachtweberStoreStateRegistry, EqualStore, NachtweberRuntimeWiringTest, RecordingRuntime

### Community 25 - "SeuchenweberRuntimeConfig"
Cohesion: 0.09
Nodes (6): OperatorConfigNumbers, OperatorConfigValidation, SeuchenweberRuntimeConfig, OperatorConfigNumbersTest, OperatorConfigValidationTest, SeuchenweberRuntimeConfigTest

### Community 26 - ".build"
Cohesion: 0.16
Nodes (8): BookSettings, CoreConfig, EndlessBookConfig, Localization, ActionData, EndlessBookPage, Override, LinkDefinition

### Community 27 - "com.hypixel.hytale.server.core.plugin.JavaPlugin"
Cohesion: 0.15
Nodes (10): com.hypixel.hytale.server.core.plugin.JavaPlugin, EndlessEliteCorePlugin, Override, GameplayConcern, ACTIVE_ABILITIES, MOB_SCALING, PERSISTENCE, PLAYER_PROGRESSION (+2 more)

### Community 28 - "AstralRiftRuntimeLedger"
Cohesion: 0.14
Nodes (4): AstralRiftRuntimeLedger, RiftPulse, RiftState, AstralRiftRuntimeLedgerTest

### Community 29 - "EndlessDetailsData"
Cohesion: 0.17
Nodes (4): EndlessDetailsData, JsonObject, EndlessDetailsBridgeTest, EndlessDetailsDataTest

### Community 30 - ".apply"
Cohesion: 0.05
Nodes (25): com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent, ShadowSwingOutcome, ShadowSwingRequest, ShadowSwingRules, ShadowSwingService, ShadowSwingStatus, APPLIED, COOLDOWN (+17 more)

### Community 31 - ".execute"
Cohesion: 0.08
Nodes (10): ChronoblightAbility, Override, Contract, ParamEntry, SeuchenweberAbilityContracts, ContractBoundEffect, Override, SeuchenweberAbilityRegistry (+2 more)

### Community 32 - "NekrotoxinDamageSystem"
Cohesion: 0.23
Nodes (5): RuntimeState, DiagnosisKey, Override, NekrotoxinDamageSystem, RuntimeState

### Community 33 - "HymannAbilityBindingPersistenceSystem"
Cohesion: 0.17
Nodes (5): com.ziggfreed.mmoskilltree.ability.MmoSlot, com.ziggfreed.mmoskilltree.ability.TriggerKey, HymannAbilityBindingPersistenceSystem, SavedBindings, State

### Community 34 - "CardDescriptionPatcher"
Cohesion: 0.19
Nodes (5): Label, OwnedEffectRegistryPatcher, CardDescriptionPatcher, org.objectweb.asm.Label, org.objectweb.asm.MethodVisitor

### Community 35 - "HymannCombatPassiveSystem.java"
Cohesion: 0.28
Nodes (6): com.hypixel.hytale.component.dependency.Dependency, com.hypixel.hytale.server.core.inventory.InventoryComponent, com.hypixel.hytale.server.core.inventory.ItemStack, com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem, HymannAccess, HymannMmoCriticalFeedbackSystem

### Community 36 - "SeuchenweberConfigMigration"
Cohesion: 0.20
Nodes (4): java.util.regex.Pattern, Migration, SeuchenweberConfigMigration, SeuchenweberConfigMigrationTest

### Community 37 - "com.google.gson.JsonObject"
Cohesion: 0.35
Nodes (3): com.google.gson.JsonObject, JsonObject, NachtweberMmoInstaller

### Community 38 - "hymann/src/main/resources/manifest.json"
Cohesion: 0.11
Nodes (17): Authors, Dependencies, Airijko:EndlessLevelingCore, narwhals:Perfect Utils, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault (+9 more)

### Community 39 - "EndlessEliteCoreTest"
Cohesion: 0.10
Nodes (6): ManagedModule, ModuleCoordinator, Registry, CompleteRegistry, EndlessEliteCoreTest, LegacyRegistry

### Community 40 - "Nachtweber – Test Report 0.1.0"
Cohesion: 0.12
Nodes (16): Class change ABI demarcation, DamageCause-AssetStore-Readiness - 2026-08-09, Final proof of completion, Isolated Server Boot - 2026-08-09, Nachtweber – Test Report 0.1.0, Owned Effects, Damage and Schema 9 PASS - 2026-08-10, Owner cleanup at PlayerDisconnectEvent – 2026-08-09, Reproducible Artifact - 2026-08-09 (+8 more)

### Community 41 - "endless-book/src/main/resources/manifest.json"
Cohesion: 0.12
Nodes (16): Authors, Dependencies, Airijko:EndlessGuilds, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+8 more)

### Community 42 - "PersonalClaimsPage"
Cohesion: 0.29
Nodes (7): ClaimResult, ActionData, PersonalClaimsPage, UiLanguage, ENGLISH, GERMAN, UnclaimResult

### Community 43 - "java.util.function.LongSupplier"
Cohesion: 0.06
Nodes (20): com.hypixel.hytale.component.system.tick.TickingSystem, java.util.function.LongSupplier, Override, NachtweberMaintenanceSystem, NachtweberRuntimeCoordinator, AbilityEffect, NachtweberRuntimeWiring, RegistrationPort (+12 more)

### Community 44 - "PesthauchAuraSystem"
Cohesion: 0.12
Nodes (6): OwnerIntervalBudget, Override, PesthauchAuraSystem, StoreScopedState, EqualStore, StoreScopedStateTest

### Community 45 - "com.ziggfreed.mmoskilltree.data.SkillComponent"
Cohesion: 0.13
Nodes (4): com.ziggfreed.mmoskilltree.data.SkillComponent, ComboState, HymannCombatPassiveSystem, HymannTreeRewards

### Community 46 - "seuchenweber/src/main/resources/manifest.json"
Cohesion: 0.12
Nodes (15): Authors, Dependencies, Airijko:EndlessLevelingCore, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+7 more)

### Community 47 - "EndlessDetailsPresenter"
Cohesion: 0.28
Nodes (4): DetailRow, EndlessDetailsPresenter, JsonObject, EndlessDetailsPresenterTest

### Community 48 - "EndlessBookPlugin"
Cohesion: 0.17
Nodes (6): com.hypixel.hytale.server.core.command.system.AbstractCommand, com.hypixel.hytale.server.core.inventory.container.ItemContainer, ItemStack, EndlessBookIntegrations, EndlessBookPlugin, Override

### Community 49 - ".setup"
Cohesion: 0.15
Nodes (3): AstralRiftPulseSystem, Override, SeuchenweberMaintenanceSystem

### Community 50 - "NachtweberPlugin.java"
Cohesion: 0.18
Nodes (6): ClassLoader, com.ziggfreed.mmoskilltree.ability.ActiveAbilityService, Action, BestEffortCleanup, FunctionalInterface, MmoOwnedEffectAbi

### Community 51 - "StepStatus"
Cohesion: 0.25
Nodes (8): StepStatus, CORRECTION, EXPIRED, INVALID_REQUEST, NO_SESSION, REACHED, STALE_STEP, TETHER_BROKEN

### Community 52 - "com.hypixel.hytale.server.core.modules.entity.damage.DamageCause"
Cohesion: 0.16
Nodes (8): com.hypixel.hytale.server.core.modules.entity.damage.DamageCause, DamageCauseReadinessProbe, Result, Status, INCONSISTENT, READY, UNAVAILABLE, SeuchenweberDamageCause

### Community 53 - "nachtweber/src/main/resources/manifest.json"
Cohesion: 0.14
Nodes (13): Authors, Dependencies, Airijko:EndlessLevelingCore, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+5 more)

### Community 54 - ".scan"
Cohesion: 0.10
Nodes (16): DangerCandidate, DangerSenseAlertPort, FunctionalInterface, DangerSenseOutcome, DangerSenseRules, DangerSenseRuntimeAdapter, Ref, Store (+8 more)

### Community 55 - ".tick"
Cohesion: 0.24
Nodes (4): Override, Vector3d, Candidate, SeuchenweberTargeting

### Community 56 - ".treeUnlocks"
Cohesion: 0.13
Nodes (4): SeuchenweberAbilityConfigInstallerTest, SeuchenweberClientLocalizationTest, SeuchenweberMmoBridgeTest, SeuchenweberPresentationInstallerTest

### Community 57 - "com.airijko.endlessleveling.classes.CharacterClassDefinition"
Cohesion: 0.09
Nodes (10): com.airijko.endlessleveling.classes.CharacterClassDefinition, CharacterClassDefinition, NachtweberClassDefinition, NachtweberLifecycle, RuntimePort, Override, NachtweberRuntimeAdapterTest, RecordingPort (+2 more)

### Community 58 - "Action"
Cohesion: 0.18
Nodes (7): Action, BACK, DETAILS, LINK, NONE, BookNavigation, BookNavigationTest

### Community 59 - "OwnerEntityBindingRegistry"
Cohesion: 0.22
Nodes (3): java.util.IdentityHashMap, OwnerEntityBindingRegistry, Override

### Community 60 - "BookInteraction.java"
Cohesion: 0.33
Nodes (6): com.hypixel.hytale.protocol.InteractionType, com.hypixel.hytale.server.core.entity.InteractionContext, com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction, com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler, BookInteraction, Override

### Community 61 - "HuntingCocoonService"
Cohesion: 0.09
Nodes (14): java.util.function.BooleanSupplier, HuntingCocoonRules, CastOutcome, CastRequest, CastStatus, APPLIED, REJECTED_COOLDOWN, REJECTED_IMMUNE (+6 more)

### Community 62 - "NachtweberMmoContract"
Cohesion: 0.21
Nodes (3): NachtweberMmoContract, Unlock, NachtweberMmoInstallerTest

### Community 63 - "SeuchenweberPlugin"
Cohesion: 0.18
Nodes (4): com.hypixel.hytale.server.core.plugin.JavaPluginInit, Override, MmoEffectRegistry, SeuchenweberPlugin

### Community 65 - "core/src/main/resources/manifest.json"
Cohesion: 0.18
Nodes (10): Authors, Dependencies, Description, DisabledByDefault, Group, IncludesAssetPack, Main, Name (+2 more)

### Community 66 - ".build"
Cohesion: 0.33
Nodes (3): ActionData, EndlessBookDetailsPage, Override

### Community 67 - "HymannPlugin.java"
Cohesion: 0.11
Nodes (5): HymannConfig, HymannCriticalProfile, Values, HymannStormFuryDamageSystem, HymannStormFurySystem

### Community 68 - "Seuchenweber Runtime Acceptance"
Cohesion: 0.20
Nodes (9): Audit hardening, Core benchmark scope, Evidence classes, Explicit exclusions, Final server evidence, Lifecycle release evidence, Release gate, Seuchenweber Runtime Acceptance (+1 more)

### Community 72 - "portal-spawn/src/main/snapshot/manifest.json"
Cohesion: 0.20
Nodes (9): Authors, Dependencies, DisabledByDefault, Group, IncludesAssetPack, LoadBefore, Name, OptionalDependencies (+1 more)

### Community 74 - "PersonalClaimsPage.java"
Cohesion: 0.33
Nodes (6): com.airijko.endlessguilds.guild.claim.ClaimCell, com.airijko.endlessguilds.guild.claim.ClaimService, com.hypixel.hytale.codec.builder.BuilderCodec, com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage, com.hypixel.hytale.server.core.ui.builder.UICommandBuilder, com.hypixel.hytale.server.core.ui.builder.UIEventBuilder

### Community 76 - "com.hypixel.hytale.logger.HytaleLogger"
Cohesion: 0.18
Nodes (5): com.hypixel.hytale.logger.HytaleLogger, Values, HymannCriticalSystem, Override, SeuchenweberUnlockSystem

### Community 77 - "Nightweaver – Core Design 0.1.0"
Cohesion: 0.22
Nodes (8): BLACK_THREAD gameplay slice, Fail-closed runtime adapter, HUNTING_COCOON gameplay slice, Integrated completion slice, MMOSkillTree staging contract, Nightweaver – Core Design 0.1.0, Store-bound runtime maintenance, VENOM_TICK Damage slice

### Community 78 - "com.hypixel.hytale.server.core.modules.entity.damage.Damage"
Cohesion: 0.11
Nodes (6): com.hypixel.hytale.server.core.modules.entity.damage.Damage, RelicCheck, HymannArmamentMasterySystem, Source, EndlessLevelingVenomDamageAdapter, Override

### Community 79 - "Seuchenweber – Manual Client Acceptance 2026-08-09"
Cohesion: 0.22
Nodes (8): Build and Deployment, Full-Stack Boot, Known Third-Party Mod Issues in the Full Stack, Manual PASS Results, Outstanding Multiplayer Gate Test, Result, Seuchenweber – Manual Client Acceptance 2026-08-09, Visual Evidence

### Community 83 - ".mayCreateClaim"
Cohesion: 0.38
Nodes (3): FunctionalInterface, MembershipLookup, PersonalClaimsAccess

### Community 84 - "mmoskilltree-card-description-patcher/verify_patch.py"
Cohesion: 0.60
Nodes (5): javap(), javap_tool(), main(), method_slice(), Path

### Community 88 - ".execute"
Cohesion: 0.24
Nodes (3): HymannStormFuryAbility, HymannThunderAegisAbility, Vector3d

### Community 89 - "VenomTickServiceTest"
Cohesion: 0.18
Nodes (4): EndlessLevelingVenomPowerProvider, Override, VenomTickServiceTest, Result

### Community 90 - "HymannThunderStepArrivalSystem"
Cohesion: 0.24
Nodes (3): HymannThunderStepArrivalSystem, Vector3d, PendingImpact

### Community 91 - "HymannThunderAegisAbility.java"
Cohesion: 0.16
Nodes (8): com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent, HymannSignatureEnergy, KnockbackComponent, AstralRiftAbility, Override, Vector3d, org.joml.Vector3d, org.joml.Vector3dc

### Community 94 - "NativePoisonVisualTier"
Cohesion: 0.29
Nodes (6): forStacks(), NativePoisonVisualTier, NONE, POISON_I, POISON_II, POISON_III

### Community 97 - "Action"
Cohesion: 0.29
Nodes (6): Action, ADD, NONE, REMOVE, RESTART, SeuchenweberClassAuraVisuals

### Community 99 - "mjolnir-safety-patch/src/main/snapshot/manifest.json"
Cohesion: 0.25
Nodes (7): Authors, Description, Group, IncludesAssetPack, Name, ServerVersion, Version

### Community 100 - ".effects"
Cohesion: 0.43
Nodes (3): FakeRegistry, Override, NachtweberOwnedRegistryLifecycleTest

### Community 101 - "Endless Elite test report"
Cohesion: 0.29
Nodes (6): Endless Elite test report, Maven reactor, Repository and distribution gates, Reproduced local dependencies, Toolchain, Warnings and outstanding acceptance

### Community 103 - "AugmentDetailEnricher"
Cohesion: 0.39
Nodes (3): AugmentDetailEnricher, FunctionalInterface, Resolver

### Community 104 - "StartStatus"
Cohesion: 0.29
Nodes (7): StartStatus, ANCHOR_OUT_OF_RANGE, CLOSED, INVALID_REQUEST, NOT_AUTHORIZED, REATTACH_RATE_LIMIT, STARTED

### Community 106 - "StoreBoundShadowSwingAbility"
Cohesion: 0.43
Nodes (3): Override, Vector3d, StoreBoundShadowSwingAbility

### Community 108 - "setup_local_dependencies.py"
Cohesion: 0.42
Nodes (11): build_owned_mmo(), digest(), install(), install_arguments(), java_tool(), LocalArtifact, main(), maven_tool() (+3 more)

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

### Community 118 - "SeuchenweberClassAuraSystem"
Cohesion: 0.19
Nodes (4): AuraSession, Override, SeuchenweberClassAuraSystem, SeuchenweberTreeBonusResolver

### Community 122 - "Seuchenweber Configuration – Schema 4"
Cohesion: 0.50
Nodes (3): Migration from Schema 3, Plague Breath, Seuchenweber Configuration – Schema 4

### Community 124 - "sha256"
Cohesion: 0.67
Nodes (3): main(), Path, sha256()

## Knowledge Gaps
- **266 isolated node(s):** `de.shadow:hymann`, `Group`, `Name`, `Version`, `Description` (+261 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **42 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HymannMmoBridge` connect `HymannMmoBridge` to `.setup`, `com.hypixel.hytale.component.Store`, `HymannCombatPassiveSystem.java`, `.values`, `HymannPlugin.java`, `com.hypixel.hytale.component.query.Query`, `com.hypixel.hytale.logger.HytaleLogger`, `com.hypixel.hytale.server.core.modules.entity.damage.Damage`, `com.ziggfreed.mmoskilltree.ability.AbilityEffect`, `HymannThunderAegisAbility.java`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **Why does `NekrotoxinDamageSystem` connect `NekrotoxinDamageSystem` to `NekrotoxinDamageSystemTest`, `com.hypixel.hytale.component.Store`, `com.hypixel.hytale.component.query.Query`, `PesthauchAuraSystem`, `.execute`, `.boundedTargetLimit`, `.setup`, `com.hypixel.hytale.server.core.universe.world.storage.EntityStore`, `SeuchenweberPlugin`, `.execute`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Why does `HymannProfileProgressSystem` connect `HymannProfileProgressSystem` to `.setup`, `HymannPlugin.java`, `com.hypixel.hytale.component.Store`, `com.hypixel.hytale.component.query.Query`, `com.hypixel.hytale.logger.HytaleLogger`, `com.hypixel.hytale.server.core.universe.world.storage.EntityStore`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **What connects `de.shadow:hymann`, `Group`, `Name` to the rest of the system?**
  _266 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.setup` be split into smaller, more focused modules?**
  _Cohesion score 0.07051282051282051 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.03111416151910318 - nodes in this community are weakly interconnected._
- **Should `HymannMmoBridge` be split into smaller, more focused modules?**
  _Cohesion score 0.08256201406886339 - nodes in this community are weakly interconnected._