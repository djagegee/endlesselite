# Graph Report - endlesselite  (2026-08-10)

## Corpus Check
- 356 files · ~1,344,986 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2261 nodes · 5915 edges · 128 communities (82 shown, 46 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 468 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3039b731`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .setup
- org.junit.jupiter.api.Test
- HymannMmoBridge
- com.hypixel.hytale.server.core.universe.world.storage.EntityStore
- HymannProfileProgressSystem
- HymannRegistrar
- com.hypixel.hytale.server.core.universe.PlayerRef
- NekrotoxinRuntimeLedger
- com.hypixel.hytale.component.system.tick.EntityTickingSystem
- Konflikte und Entscheidungen
- org.joml.Vector3d
- EndlessDetailsBridge
- BlackThreadService
- RiftMageTechnodistrictContractTest
- EntanglementLedger
- ShadowSwingReconciliationController
- .onDirectHit
- com.hypixel.hytale.component.query.Query
- .setup
- NachtweberLedgerStoreRuntime
- com.ziggfreed.mmoskilltree.ability.AbilityEffect
- NachtweberGameplayRuntime
- VenomLedger
- .cleanupOwnerRemovesOnlyThatOwnersStateFromTheExactStore
- NachtweberRuntimeWiringTest
- SeuchenweberRuntimeConfig
- .build
- com.hypixel.hytale.server.core.plugin.JavaPlugin
- AstralRiftRuntimeLedger
- EndlessDetailsData
- .apply
- .createRuntimeEffects
- NekrotoxinDamageSystem
- NachtweberAbilityRegistry
- CardDescriptionPatcher
- com.hypixel.hytale.component.CommandBuffer
- SeuchenweberConfigMigration
- com.google.gson.JsonObject
- hymann/src/main/resources/manifest.json
- ManagedModule
- Nachtweber – Test Report 0.1.0
- endless-book/src/main/resources/manifest.json
- PersonalClaimsPage
- .start
- StoreScopedState
- com.ziggfreed.mmoskilltree.data.SkillComponent
- seuchenweber/src/main/resources/manifest.json
- EndlessDetailsPresenter
- EndlessBookPlugin
- .create
- RecordingRegistrationPort
- StepStatus
- NachtweberRuntimeWiring
- nachtweber/src/main/resources/manifest.json
- .scan
- .tick
- com.hypixel.hytale.component.dependency.Dependency
- com.airijko.endlessleveling.classes.CharacterClassDefinition
- Action
- OwnerEntityBindingRegistry
- BookInteraction.java
- HuntingCocoonService
- .formatForStorage
- .requiredDouble
- NekrotoxinDamageSystemTest
- core/src/main/resources/manifest.json
- .build
- HymannPlugin.java
- Seuchenweber Runtime Acceptance
- ArtifactContractTest
- LocalDependencySetupTest
- EndlessEliteConfigArtifactsTest
- portal-spawn/src/main/snapshot/manifest.json
- QuestEntitlement
- com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
- HymannCriticalAttributeSystem.java
- com.hypixel.hytale.logger.HytaleLogger
- Nightweaver – Core Design 0.1.0
- HymannArmamentMasterySystem
- Seuchenweber – Manual Client Acceptance 2026-08-09
- .boundedTargetLimit
- CastStatus
- SeuchenweberUnlockRules
- .mayCreateClaim
- mmoskilltree-card-description-patcher/verify_patch.py
- EndlessBookIntegrations
- PortalSpawnSnapshotTest
- NachtweberConfig
- .closestVisibleHostile
- EndlessClassId
- .execute
- .secondsToMilliseconds
- DamageBudget
- NativePoisonVisualTier
- .boundedTargetLimit
- Action
- SeuchenweberArtifactContractTest
- mjolnir-safety-patch/src/main/snapshot/manifest.json
- NachtweberMaintenanceSystem
- Endless Elite test report
- StartStatus
- setup_local_dependencies.py
- Legacy- und Binär-only-Modfamilien
- Eigene Binär-/Backupfassungen
- NachtweberAbilityCatalog
- mmoskilltree-owned-effect-patcher/verify_patch.py
- Rift Mage Dungeon
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
7. `HymannGuardAuraSystem` - 25 edges
8. `NachtweberRuntimeCoordinator` - 25 edges
9. `AstralRiftPulseSystem` - 24 edges
10. `NekrotoxinRuntimeLedger` - 24 edges

## Surprising Connections (you probably didn't know these)
- `HymannSkillLifecycleSystem` --references--> `State`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannSkillLifecycleSystem.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannAbilityBindingPersistenceSystem.java
- `HymannPlugin` --references--> `HymannArmamentMasterySystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannArmamentMasterySystem.java
- `HymannPlugin` --references--> `HymannCriticalAttributeSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannCriticalAttributeSystem.java
- `HymannPlugin` --references--> `HymannMmoBridge`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannMmoBridge.java
- `HymannPlugin` --references--> `HymannProfileProgressSystem`  [EXTRACTED]
  modules/classes/hymann/src/main/java/de/shadow/hymann/HymannPlugin.java → modules/classes/hymann/src/main/java/de/shadow/hymann/HymannProfileProgressSystem.java

## Import Cycles
- None detected.

## Communities (128 total, 46 thin omitted)

### Community 0 - ".setup"
Cohesion: 0.05
Nodes (12): com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem, HymannBossBreakerDamageSystem, HymannBossBreakerSystem, ComboState, HymannCombatPassiveSystem, HymannGuardWaveSystem, HymannPassiveAuraSystem, PulseRank (+4 more)

### Community 1 - "org.junit.jupiter.api.Test"
Cohesion: 0.03
Nodes (25): HymannMeleeAndDescriptionTest, HymannProgressionContractTest, ShadowSwingMovementSystemTest, MmoSkillTreePatcherContractTest, NativePoisonVisualContractTest, NekrotoxinEndlessLevelingDamageContractTest, OperatorConfigNumbersTest, PesthauchAuraContractTest (+17 more)

### Community 2 - "HymannMmoBridge"
Cohesion: 0.07
Nodes (15): com.airijko.endlessleveling.api.DamageEventListener, com.ziggfreed.mmoskilltree.skilltree.SkillReward, com.ziggfreed.mmoskilltree.skilltree.SkillRewardType, com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode, HymannMmoBridge, SkillReward, SkillTreeNode, TreeTier (+7 more)

### Community 3 - "com.hypixel.hytale.server.core.universe.world.storage.EntityStore"
Cohesion: 0.08
Nodes (13): com.hypixel.hytale.component.Ref, com.hypixel.hytale.component.Store, com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent, com.hypixel.hytale.server.core.universe.world.storage.EntityStore, Source, Source, Damage, Vector3d (+5 more)

### Community 4 - "HymannProfileProgressSystem"
Cohesion: 0.08
Nodes (8): com.ziggfreed.mmoskilltree.ability.MmoSlot, com.ziggfreed.mmoskilltree.ability.TriggerKey, HymannAbilityBindingPersistenceSystem, SavedBindings, State, Context, HymannProfileProgressSystem, Progress

### Community 6 - "com.hypixel.hytale.server.core.universe.PlayerRef"
Cohesion: 0.18
Nodes (9): com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand, com.hypixel.hytale.server.core.command.system.CommandContext, com.hypixel.hytale.server.core.Message, com.hypixel.hytale.server.core.universe.PlayerRef, com.hypixel.hytale.server.core.universe.world.World, RelicCheck, HymannClaimCommand, Override (+1 more)

### Community 7 - "NekrotoxinRuntimeLedger"
Cohesion: 0.09
Nodes (9): DueTick, Expiration, NaturalExpiration, NekrotoxinRuntimeLedger, State, TargetExpiration, TickResult, NekrotoxinRuntimeLedgerTest (+1 more)

### Community 8 - "com.hypixel.hytale.component.system.tick.EntityTickingSystem"
Cohesion: 0.19
Nodes (11): com.hypixel.hytale.component.system.tick.EntityTickingSystem, com.hypixel.hytale.server.core.entity.entities.Player, com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap, com.ziggfreed.mmoskilltree.ability.ActiveAbilityService, HymannDamageScaling, HymannSignatureEnergy (+3 more)

### Community 9 - "Konflikte und Entscheidungen"
Cohesion: 0.05
Nodes (39): 1. MMOSkillTree-Binärvertrag — kontrollierter Superset-Kandidat, 2. Globale MMOSkillTree-Konfigurationswriter — konfliktträchtig, 3. Ability-Registrierung und Reload — konfliktträchtig, 4. Spielerfortschritt — Hymann als zusätzlicher Owner, 5. Event-/Damage-Reihenfolge, 6. Ressourcen und UI, 7. Rift Mage Dungeon, 8. Portal Spawn Snapshot (+31 more)

### Community 10 - "org.joml.Vector3d"
Cohesion: 0.15
Nodes (11): KnockbackComponent, HymannThunderAegisAbility, KnockbackComponent, Vector3d, Candidate, HymannThunderStepAbility, Vector3d, Vector3d (+3 more)

### Community 11 - "EndlessDetailsBridge"
Cohesion: 0.11
Nodes (11): AugmentDetailEnricher, FunctionalInterface, Resolver, EndlessDetailsBridge, FatesReader, FunctionalInterface, Override, LevelingReader (+3 more)

### Community 12 - "BlackThreadService"
Cohesion: 0.09
Nodes (12): BlackThreadRules, BlackThreadService, ControlProfile, TargetKind, BOSS, ELITE, NORMAL, PVP_PLAYER (+4 more)

### Community 13 - "RiftMageTechnodistrictContractTest"
Cohesion: 0.10
Nodes (6): com.google.gson.JsonArray, NativeAssetEvidence, RiftMageAssetBaselineTest, RiftMageBossAssetTest, RiftMagePortalAssetTest, RiftMageTechnodistrictContractTest

### Community 14 - "EntanglementLedger"
Cohesion: 0.11
Nodes (7): EntanglementLedger, State, CastRequest, TimeMath, DueTick, HuntingCocoonServiceTest, NachtweberCoreContractTest

### Community 15 - "ShadowSwingReconciliationController"
Cohesion: 0.19
Nodes (7): Session, ShadowSwingReconciliationController, StartOutcome, StartRequest, StepRequest, ShadowSwingReconciliationRules, ShadowSwingReconciliationControllerTest

### Community 16 - ".onDirectHit"
Cohesion: 0.08
Nodes (18): java.util.function.BooleanSupplier, Ref, Store, PassiveVenomDamageAdapter, PassiveVenomOutcome, PassiveVenomRequest, PassiveVenomRules, Key (+10 more)

### Community 17 - "com.hypixel.hytale.component.query.Query"
Cohesion: 0.10
Nodes (8): com.hypixel.hytale.component.ComponentRegistryProxy, com.hypixel.hytale.component.query.Query, HymannGuardAuraSystem, HymannGuardAuraTickSystem, HymannSkillLifecycleSystem, State, Override, SeuchenweberUnlockSystem

### Community 18 - ".setup"
Cohesion: 0.13
Nodes (7): Entry, NachtweberOwnedEffectLifecycle, Registry, Override, MmoEffectRegistry, NachtweberPlugin, RuntimePort

### Community 20 - "com.ziggfreed.mmoskilltree.ability.AbilityEffect"
Cohesion: 0.07
Nodes (31): com.hypixel.hytale.server.core.modules.entity.component.HeadRotation, com.ziggfreed.mmoskilltree.ability.AbilityDefinition, com.ziggfreed.mmoskilltree.ability.AbilityEffect, com.ziggfreed.mmoskilltree.ability.AbilityResult, com.ziggfreed.mmoskilltree.ability.CasterContext, com.ziggfreed.mmoskilltree.ability.ParamSpec, java.util.function.LongSupplier, BlackThreadAbility (+23 more)

### Community 21 - "NachtweberGameplayRuntime"
Cohesion: 0.11
Nodes (8): NachtweberGameplayRuntime, VenomDamageCause, DIRECT_HIT, ENVIRONMENT, REFLECTION, VENOM_TICK, FunctionalInterface, VenomDamagePort

### Community 22 - "VenomLedger"
Cohesion: 0.09
Nodes (15): EndlessLevelingVenomPowerProvider, Override, State, VenomLedger, FunctionalInterface, VenomPowerProvider, VenomTickRules, TickOutcome (+7 more)

### Community 23 - ".cleanupOwnerRemovesOnlyThatOwnersStateFromTheExactStore"
Cohesion: 0.24
Nodes (5): CastOutcome, CastRequest, Override, CastRequest, NachtweberMultiplayerIsolationPreflightTest

### Community 24 - "NachtweberRuntimeWiringTest"
Cohesion: 0.19
Nodes (3): NachtweberStoreStateRegistry, EqualStore, NachtweberRuntimeWiringTest

### Community 26 - ".build"
Cohesion: 0.12
Nodes (9): BookSettings, CoreConfig, EndlessBookConfig, Localization, ActionData, EndlessBookPage, Override, LinkDefinition (+1 more)

### Community 27 - "com.hypixel.hytale.server.core.plugin.JavaPlugin"
Cohesion: 0.10
Nodes (11): com.hypixel.hytale.server.core.plugin.JavaPlugin, com.hypixel.hytale.server.core.plugin.JavaPluginInit, EndlessEliteCorePlugin, Override, GameplayConcern, ACTIVE_ABILITIES, MOB_SCALING, PERSISTENCE (+3 more)

### Community 28 - "AstralRiftRuntimeLedger"
Cohesion: 0.14
Nodes (4): AstralRiftRuntimeLedger, RiftPulse, RiftState, AstralRiftRuntimeLedgerTest

### Community 29 - "EndlessDetailsData"
Cohesion: 0.16
Nodes (4): EndlessDetailsData, JsonObject, EndlessDetailsBridgeTest, EndlessDetailsDataTest

### Community 30 - ".apply"
Cohesion: 0.05
Nodes (25): com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent, ShadowSwingOutcome, ShadowSwingRequest, ShadowSwingRules, ShadowSwingService, ShadowSwingStatus, APPLIED, COOLDOWN (+17 more)

### Community 31 - ".createRuntimeEffects"
Cohesion: 0.10
Nodes (8): Contract, ParamEntry, SeuchenweberAbilityContracts, ContractBoundEffect, Override, SeuchenweberAbilityRegistry, SeuchenweberAbilityContractsTest, SeuchenweberAbilityRegistryTest

### Community 32 - "NekrotoxinDamageSystem"
Cohesion: 0.08
Nodes (14): AstralRiftAbility, AstralRiftPulseSystem, RuntimeState, MmoPassiveResolver, DiagnosisKey, Override, NekrotoxinDamageSystem, RuntimeState (+6 more)

### Community 34 - "CardDescriptionPatcher"
Cohesion: 0.19
Nodes (5): Label, OwnedEffectRegistryPatcher, CardDescriptionPatcher, org.objectweb.asm.Label, org.objectweb.asm.MethodVisitor

### Community 35 - "com.hypixel.hytale.component.CommandBuffer"
Cohesion: 0.12
Nodes (11): com.hypixel.hytale.component.ArchetypeChunk, com.hypixel.hytale.component.CommandBuffer, com.hypixel.hytale.server.core.inventory.InventoryComponent, com.hypixel.hytale.server.core.inventory.ItemStack, com.hypixel.hytale.server.core.modules.entity.damage.Damage, HymannAccess, HymannMmoCriticalFeedbackSystem, Source (+3 more)

### Community 36 - "SeuchenweberConfigMigration"
Cohesion: 0.19
Nodes (5): java.util.regex.Pattern, Migration, Result, SeuchenweberConfigMigration, SeuchenweberConfigMigrationTest

### Community 37 - "com.google.gson.JsonObject"
Cohesion: 0.07
Nodes (13): com.google.gson.Gson, com.google.gson.JsonObject, JsonObject, NachtweberMmoContract, Unlock, JsonObject, NachtweberMmoInstaller, NachtweberMmoInstallerTest (+5 more)

### Community 38 - "hymann/src/main/resources/manifest.json"
Cohesion: 0.11
Nodes (17): Authors, Dependencies, Airijko:EndlessLevelingCore, narwhals:Perfect Utils, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault (+9 more)

### Community 39 - "ManagedModule"
Cohesion: 0.11
Nodes (5): ManagedModule, ModuleCatalog, ModuleCoordinator, ModuleDescriptor, EndlessEliteCoreTest

### Community 40 - "Nachtweber – Test Report 0.1.0"
Cohesion: 0.12
Nodes (16): Class change ABI demarcation, DamageCause-AssetStore-Readiness - 2026-08-09, Final proof of completion, Isolated Server Boot - 2026-08-09, Nachtweber – Test Report 0.1.0, Owned Effects, Damage and Schema 9 PASS - 2026-08-10, Owner cleanup at PlayerDisconnectEvent – 2026-08-09, Reproducible Artifact - 2026-08-09 (+8 more)

### Community 41 - "endless-book/src/main/resources/manifest.json"
Cohesion: 0.12
Nodes (16): Authors, Dependencies, Airijko:EndlessGuilds, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+8 more)

### Community 42 - "PersonalClaimsPage"
Cohesion: 0.18
Nodes (14): ClaimResult, com.airijko.endlessguilds.guild.claim.ClaimCell, com.airijko.endlessguilds.guild.claim.ClaimService, com.hypixel.hytale.codec.builder.BuilderCodec, com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage, com.hypixel.hytale.server.core.ui.builder.UICommandBuilder, com.hypixel.hytale.server.core.ui.builder.UIEventBuilder, ActionData (+6 more)

### Community 44 - "StoreScopedState"
Cohesion: 0.18
Nodes (4): Override, StoreScopedState, EqualStore, StoreScopedStateTest

### Community 46 - "seuchenweber/src/main/resources/manifest.json"
Cohesion: 0.12
Nodes (15): Authors, Dependencies, Airijko:EndlessLevelingCore, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+7 more)

### Community 47 - "EndlessDetailsPresenter"
Cohesion: 0.28
Nodes (4): DetailRow, EndlessDetailsPresenter, JsonObject, EndlessDetailsPresenterTest

### Community 48 - "EndlessBookPlugin"
Cohesion: 0.27
Nodes (4): com.hypixel.hytale.server.core.inventory.container.ItemContainer, ItemStack, EndlessBookPlugin, Override

### Community 50 - "RecordingRegistrationPort"
Cohesion: 0.19
Nodes (4): NachtweberStoreRuntime, Override, RecordingRegistrationPort, RecordingRuntime

### Community 51 - "StepStatus"
Cohesion: 0.20
Nodes (9): StepOutcome, StepStatus, CORRECTION, EXPIRED, INVALID_REQUEST, NO_SESSION, REACHED, STALE_STEP (+1 more)

### Community 52 - "NachtweberRuntimeWiring"
Cohesion: 0.22
Nodes (8): DamageCauseReadinessProbe, Result, Status, INCONSISTENT, READY, UNAVAILABLE, AbilityEffect, NachtweberRuntimeWiring

### Community 53 - "nachtweber/src/main/resources/manifest.json"
Cohesion: 0.14
Nodes (13): Authors, Dependencies, Airijko:EndlessLevelingCore, Shadow:EndlessElite, Ziggfreed:MMOSkillTree, Description, DisabledByDefault, Group (+5 more)

### Community 54 - ".scan"
Cohesion: 0.10
Nodes (16): DangerCandidate, DangerSenseAlertPort, FunctionalInterface, DangerSenseOutcome, DangerSenseRules, DangerSenseRuntimeAdapter, Ref, Store (+8 more)

### Community 55 - ".tick"
Cohesion: 0.27
Nodes (4): Override, Vector3d, Candidate, SeuchenweberTargeting

### Community 56 - "com.hypixel.hytale.component.dependency.Dependency"
Cohesion: 0.36
Nodes (4): com.hypixel.hytale.component.dependency.Dependency, Override, Vector3d, ShadowSwingMovementSystem

### Community 57 - "com.airijko.endlessleveling.classes.CharacterClassDefinition"
Cohesion: 0.11
Nodes (8): com.airijko.endlessleveling.classes.CharacterClassDefinition, CharacterClassDefinition, NachtweberClassDefinition, NachtweberLifecycle, RuntimePort, Override, NachtweberRuntimeAdapterTest, RecordingPort

### Community 58 - "Action"
Cohesion: 0.18
Nodes (7): Action, BACK, DETAILS, LINK, NONE, BookNavigation, BookNavigationTest

### Community 59 - "OwnerEntityBindingRegistry"
Cohesion: 0.24
Nodes (3): java.util.IdentityHashMap, OwnerEntityBindingRegistry, Override

### Community 60 - "BookInteraction.java"
Cohesion: 0.33
Nodes (6): com.hypixel.hytale.protocol.InteractionType, com.hypixel.hytale.server.core.entity.InteractionContext, com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction, com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler, BookInteraction, Override

### Community 61 - "HuntingCocoonService"
Cohesion: 0.15
Nodes (12): HuntingCocoonRules, CastOutcome, CastStatus, APPLIED, REJECTED_COOLDOWN, REJECTED_IMMUNE, REJECTED_INSUFFICIENT_ENTANGLEMENT, REJECTED_INVALID_TARGET (+4 more)

### Community 65 - "core/src/main/resources/manifest.json"
Cohesion: 0.18
Nodes (10): Authors, Dependencies, Description, DisabledByDefault, Group, IncludesAssetPack, Main, Name (+2 more)

### Community 67 - "HymannPlugin.java"
Cohesion: 0.16
Nodes (4): HymannConfig, HymannStormFuryAbility, ActiveBuff, HymannStormFurySystem

### Community 68 - "Seuchenweber Runtime Acceptance"
Cohesion: 0.20
Nodes (9): Audit hardening, Core benchmark scope, Evidence classes, Explicit exclusions, Final server evidence, Lifecycle release evidence, Release gate, Seuchenweber Runtime Acceptance (+1 more)

### Community 72 - "portal-spawn/src/main/snapshot/manifest.json"
Cohesion: 0.20
Nodes (9): Authors, Dependencies, DisabledByDefault, Group, IncludesAssetPack, LoadBefore, Name, OptionalDependencies (+1 more)

### Community 75 - "HymannCriticalAttributeSystem.java"
Cohesion: 0.12
Nodes (7): com.airijko.endlessleveling.api.EndlessLevelingAPI, com.airijko.endlessleveling.enums.SkillAttributeType, HymannCriticalAttributeSystem, PublishedAttributes, HymannCriticalProfile, Values, HymannCriticalSystem

### Community 76 - "com.hypixel.hytale.logger.HytaleLogger"
Cohesion: 0.21
Nodes (3): com.hypixel.hytale.logger.HytaleLogger, Values, HymannTreeManaRegenSystem

### Community 77 - "Nightweaver – Core Design 0.1.0"
Cohesion: 0.22
Nodes (8): BLACK_THREAD gameplay slice, Fail-closed runtime adapter, HUNTING_COCOON gameplay slice, Integrated completion slice, MMOSkillTree staging contract, Nightweaver – Core Design 0.1.0, Store-bound runtime maintenance, VENOM_TICK Damage slice

### Community 79 - "Seuchenweber – Manual Client Acceptance 2026-08-09"
Cohesion: 0.22
Nodes (8): Build and Deployment, Full-Stack Boot, Known Third-Party Mod Issues in the Full Stack, Manual PASS Results, Outstanding Multiplayer Gate Test, Result, Seuchenweber – Manual Client Acceptance 2026-08-09, Visual Evidence

### Community 81 - "CastStatus"
Cohesion: 0.29
Nodes (6): CastStatus, APPLIED, REJECTED_COOLDOWN, REJECTED_INVALID_TARGET, REJECTED_NOT_SERVER, REJECTED_OUT_OF_RANGE

### Community 83 - ".mayCreateClaim"
Cohesion: 0.38
Nodes (3): FunctionalInterface, MembershipLookup, PersonalClaimsAccess

### Community 84 - "mmoskilltree-card-description-patcher/verify_patch.py"
Cohesion: 0.60
Nodes (5): javap(), javap_tool(), main(), method_slice(), Path

### Community 91 - ".execute"
Cohesion: 0.23
Nodes (3): Override, ChronoblightAbility, Override

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

### Community 101 - "Endless Elite test report"
Cohesion: 0.29
Nodes (6): Endless Elite test report, Maven reactor, Repository and distribution gates, Reproduced local dependencies, Toolchain, Warnings and outstanding acceptance

### Community 104 - "StartStatus"
Cohesion: 0.29
Nodes (7): StartStatus, ANCHOR_OUT_OF_RANGE, CLOSED, INVALID_REQUEST, NOT_AUTHORIZED, REATTACH_RATE_LIMIT, STARTED

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
Cohesion: 0.16
Nodes (6): Override, SealOfDecayAbility, AuraSession, Override, SeuchenweberClassAuraSystem, SeuchenweberTreeBonusResolver

### Community 122 - "Seuchenweber Configuration – Schema 4"
Cohesion: 0.50
Nodes (3): Migration from Schema 3, Plague Breath, Seuchenweber Configuration – Schema 4

### Community 124 - "sha256"
Cohesion: 0.67
Nodes (3): main(), Path, sha256()

## Knowledge Gaps
- **266 isolated node(s):** `de.shadow:hymann`, `Group`, `Name`, `Version`, `Description` (+261 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HymannMmoBridge` connect `HymannMmoBridge` to `.setup`, `com.hypixel.hytale.component.CommandBuffer`, `com.hypixel.hytale.server.core.universe.world.storage.EntityStore`, `HymannPlugin.java`, `com.hypixel.hytale.component.system.tick.EntityTickingSystem`, `HymannCriticalAttributeSystem.java`, `com.hypixel.hytale.logger.HytaleLogger`, `com.ziggfreed.mmoskilltree.data.SkillComponent`, `com.hypixel.hytale.component.query.Query`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Why does `HuntingCocoonService` connect `HuntingCocoonService` to `BlackThreadService`, `EntanglementLedger`, `.onDirectHit`, `NachtweberLedgerStoreRuntime`, `com.ziggfreed.mmoskilltree.ability.AbilityEffect`, `VenomLedger`, `.cleanupOwner`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **Why does `VenomDamagePort` connect `NachtweberGameplayRuntime` to `com.hypixel.hytale.component.CommandBuffer`, `VenomLedger`, `.cleanupOwnerRemovesOnlyThatOwnersStateFromTheExactStore`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **What connects `de.shadow:hymann`, `Group`, `Name` to the rest of the system?**
  _266 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.setup` be split into smaller, more focused modules?**
  _Cohesion score 0.053246753246753244 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.030713058419243985 - nodes in this community are weakly interconnected._
- **Should `HymannMmoBridge` be split into smaller, more focused modules?**
  _Cohesion score 0.07136404697380307 - nodes in this community are weakly interconnected._