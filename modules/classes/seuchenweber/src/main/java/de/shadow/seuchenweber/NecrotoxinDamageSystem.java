package de.shadow.seuchenweber;

import com.airijko.endlessleveling.systems.PlayerCombatSystem;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Server-authoritative periodic damage adapter for owner-isolated Nekrotoxin. */
final class NekrotoxinDamageSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> targets;
  private final StoreScopedState<Store<EntityStore>, RuntimeState> states = new StoreScopedState<>();
  private final int stackCap;
  private final long tickIntervalMs;
  private final float baseTickDamage;
  private final double masteryTickDamageMultiplier;
  private final int astralEchoMaxTargets;
  private final double astralEchoRadius;
  private final int astralEchoStacks;
  private final long toxinDurationMs;
  private final int soulDiagnosisStackThreshold;
  private final float relicManaRefundAmount;
  private final long relicCooldownRefundMs;
  private static final long DIAGNOSIS_INTERVAL_MS = 2_000L;
  private static final long EXPIRATION_SWEEP_INTERVAL_MS = 1_000L;

  NekrotoxinDamageSystem(int stackCap, long tickIntervalMs, float baseTickDamage,
      double masteryTickDamageMultiplier, int astralEchoMaxTargets, double astralEchoRadius,
      int astralEchoStacks, long toxinDurationMs, int soulDiagnosisStackThreshold,
      float relicManaRefundAmount, long relicCooldownRefundMs) {
    this.targets = Query.and(NPCMarkerComponent.getComponentType());
    this.stackCap = stackCap;
    this.tickIntervalMs = tickIntervalMs;
    this.baseTickDamage = baseTickDamage;
    this.masteryTickDamageMultiplier = masteryTickDamageMultiplier;
    this.astralEchoMaxTargets = AstralEchoBudget.boundedTargetLimit(astralEchoMaxTargets);
    this.astralEchoRadius = astralEchoRadius;
    this.astralEchoStacks = astralEchoStacks;
    this.toxinDurationMs = toxinDurationMs;
    this.soulDiagnosisStackThreshold = soulDiagnosisStackThreshold;
    this.relicManaRefundAmount = relicManaRefundAmount;
    this.relicCooldownRefundMs = relicCooldownRefundMs;
  }

  void register(ComponentRegistryProxy<EntityStore> registry) {
    registry.registerSystem((ISystem) this);
  }

  int apply(Store<EntityStore> store, Ref<EntityStore> target, Ref<EntityStore> caster,
      UUID owner, int stacks, long durationMs) {
    if (!belongsToStore(target, store) || !belongsToStore(caster, store) || owner == null) return 0;
    RuntimeState state = stateFor(store);
    synchronized (state) {
      bindTarget(state, target);
      state.sourceByOwner.put(owner, caster);
      long nowMs = System.currentTimeMillis();
      int ownerStacks = state.ledger.apply(target.getIndex(), owner, stacks, durationMs, nowMs);
      synchronizePoisonVisual(state, store, target, nowMs);
      return ownerStacks;
    }
  }

  int applyByTargetIndex(Store<EntityStore> store, int targetEntityIndex,
      Ref<EntityStore> caster, UUID owner, int stacks, long durationMs) {
    if (store == null || !belongsToStore(caster, store) || owner == null || targetEntityIndex < 0) return 0;
    RuntimeState state = states.getIfPresent(store);
    if (state == null) return 0;
    Ref<EntityStore> target;
    synchronized (state) {
      target = state.targetRefByIndex.get(targetEntityIndex);
    }
    if (!belongsToStore(target, store) || target.getIndex() != targetEntityIndex) return 0;
    return apply(store, target, caster, owner, stacks, durationMs);
  }

  boolean isFullMark(int stacks) {
    return stacks >= stackCap;
  }

  void release(Store<EntityStore> store) {
    states.remove(store, RuntimeState::clear);
  }

  void clear() {
    states.clear(RuntimeState::clear);
  }

  static float damageForTick(float baseDamage, int stacks) {
    return damageForTick(baseDamage, stacks, false, 1.0);
  }

  static float damageForTick(float baseDamage, int stacks, boolean hasMastery, double masteryMultiplier) {
    if (!Float.isFinite(baseDamage) || baseDamage <= 0.0f || stacks <= 0) return 0.0f;
    if (!Double.isFinite(masteryMultiplier) || masteryMultiplier < 0.0) return 0.0f;
    double multiplier = hasMastery ? masteryMultiplier : 1.0;
    double damage = baseDamage * stacks * multiplier;
    return Double.isFinite(damage) && damage > 0.0 && damage <= Float.MAX_VALUE ? (float) damage : 0.0f;
  }

  static boolean shouldApplyAstralEcho(boolean sourceAllowsEcho, boolean hasUnlock,
      int configuredLimit, int alreadyApplied) {
    int limit = AstralEchoBudget.boundedTargetLimit(configuredLimit);
    return sourceAllowsEcho && hasUnlock && alreadyApplied >= 0 && alreadyApplied < limit;
  }

  static boolean shouldReportSoulDiagnosis(boolean hasUnlock, int stacks, int threshold,
      long nowMs, long nextAllowedAtMs) {
    return hasUnlock && threshold > 0 && stacks >= threshold && nowMs >= nextAllowedAtMs;
  }

  static long remainingCooldownAfterRefund(long readyAtMs, long nowMs, long refundMs) {
    if (refundMs <= 0L || readyAtMs <= nowMs) return 0L;
    return Math.max(0L, readyAtMs - nowMs - refundMs);
  }

  @Override public Query<EntityStore> getQuery() {
    return targets;
  }

  void sweepExpired(Store<EntityStore> store, long nowMs) {
    RuntimeState state = stateFor(store);
    synchronized (state) {
      if (nowMs < state.nextExpirationSweepAtMs) return;
      state.nextExpirationSweepAtMs = nowMs + EXPIRATION_SWEEP_INTERVAL_MS;
      for (NekrotoxinRuntimeLedger.TargetExpiration expiration
          : state.ledger.consumeAllExpirations(nowMs)) {
        cleanExpiredState(state, store, expiration, nowMs);
      }
    }
  }

  @Override public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    Ref<EntityStore> target = chunk.getReferenceTo(index);
    if (!belongsToStore(target, store)) return;
    RuntimeState state = stateFor(store);
    synchronized (state) {
      bindTarget(state, target);
      long tickNowMs = System.currentTimeMillis();
      sweepExpired(store, tickNowMs);
      for (NekrotoxinRuntimeLedger.DueTick dueTick
          : state.ledger.consumeDueTicks(target.getIndex(), tickNowMs)) {
        Ref<EntityStore> caster = state.sourceByOwner.get(dueTick.owner());
        if (!belongsToStore(caster, store)) continue;
        boolean hasMastery = MmoPassiveResolver.hasUnlocked(
            store, caster, MmoPassiveResolver.NECROTOXIC_MASTERY_ID);
        float damage = damageForTick(baseTickDamage, dueTick.stacks(), hasMastery, masteryTickDamageMultiplier);
        damage = SeuchenweberTreeBonusResolver.applyPercent(damage,
            SeuchenweberTreeBonusResolver.claimedPercent(
                store, caster, SeuchenweberTreeBonusResolver.DOT_DAMAGE));
        if (damage <= 0.0f) continue;
        Damage damageEvent = PlayerCombatSystem.createAbilityDotDamage(
            caster, damage, "seuchenweber_necrotoxin");
        damageEvent.setDamageCauseIndex(SeuchenweberDamageCause.requireIndex());
        DamageSystems.executeDamage(target, buffer, damageEvent);
        long nowMs = System.currentTimeMillis();
        reportSoulDiagnosis(state, store, target, caster, dueTick.owner(), dueTick.stacks(), nowMs);
        boolean hasAstralEcho = MmoPassiveResolver.hasUnlocked(
            store, caster, MmoPassiveResolver.ASTRAL_ECHO_ID);
        if (shouldApplyAstralEcho(dueTick.allowAstralEcho(), hasAstralEcho, astralEchoMaxTargets, 0)
            && state.echoBudget.tryClaim(dueTick.owner(), nowMs)) {
          applyAstralEcho(state, store, target, caster, dueTick.owner(), nowMs);
        }
      }
    }
  }

  private void cleanExpiredState(RuntimeState state, Store<EntityStore> store,
      NekrotoxinRuntimeLedger.TargetExpiration expiration, long nowMs) {
      state.nextDiagnosisAtMs.remove(new DiagnosisKey(expiration.targetEntityIndex(), expiration.owner()));
      Ref<EntityStore> targetRef = state.targetRefByIndex.get(expiration.targetEntityIndex());
      Ref<EntityStore> caster = state.sourceByOwner.get(expiration.owner());
      if (expiration.fullMark() && belongsToStore(targetRef, store)
          && belongsToStore(caster, store)) {
        rewardRelicAttunement(store, caster, nowMs);
      }
      if (!state.ledger.hasOwner(expiration.owner())) {
        state.sourceByOwner.remove(expiration.owner());
        state.echoBudget.remove(expiration.owner());
      }
      if (!state.ledger.hasTarget(expiration.targetEntityIndex())) {
        state.targetRefByIndex.remove(expiration.targetEntityIndex());
      }
      if (belongsToStore(targetRef, store)) synchronizePoisonVisual(state, store, targetRef, nowMs);
  }

  private void rewardRelicAttunement(Store<EntityStore> store, Ref<EntityStore> caster, long nowMs) {
    if (!MmoPassiveResolver.hasUnlocked(store, caster, MmoPassiveResolver.RELIC_ATTUNEMENT_ID)) return;
    EntityStatMap stats = store.getComponent(caster, EntityStatMap.getComponentType());
    int manaStat = DefaultEntityStatTypes.getMana();
    if (stats != null && manaStat >= 0 && stats.get(manaStat) != null && relicManaRefundAmount > 0.0f) {
      stats.addStatValue(manaStat, relicManaRefundAmount);
    }
    SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, caster);
    if (skills == null || relicCooldownRefundMs <= 0L) return;
    for (String abilityId : skills.getCooldownAbilityIds()) {
      long remaining = remainingCooldownAfterRefund(
          skills.getAbilityReadyAtMs(abilityId), nowMs, relicCooldownRefundMs);
      if (remaining == 0L) skills.clearCooldown(abilityId);
      else skills.stampAbilityCooldown(abilityId, remaining);
    }
  }

  private void reportSoulDiagnosis(RuntimeState state, Store<EntityStore> store, Ref<EntityStore> target,
      Ref<EntityStore> caster, UUID owner, int stacks, long nowMs) {
    DiagnosisKey key = new DiagnosisKey(target.getIndex(), owner);
    boolean unlocked = MmoPassiveResolver.hasUnlocked(store, caster, MmoPassiveResolver.SOUL_DIAGNOSIS_ID);
    long nextAllowedAt = state.nextDiagnosisAtMs.getOrDefault(key, 0L);
    if (!shouldReportSoulDiagnosis(unlocked, stacks, soulDiagnosisStackThreshold, nowMs, nextAllowedAt)) return;
    PlayerRef player = store.getComponent(caster, PlayerRef.getComponentType());
    if (player == null) return;
    player.sendMessage(Message.raw("Soul Diagnosis · Necrotoxin " + stacks + " stacks"));
    state.nextDiagnosisAtMs.put(key, nowMs + DIAGNOSIS_INTERVAL_MS);
  }

  private void applyAstralEcho(RuntimeState state, Store<EntityStore> store, Ref<EntityStore> sourceTarget,
      Ref<EntityStore> caster, UUID owner, long nowMs) {
    if (astralEchoRadius <= 0.0 || astralEchoStacks <= 0 || toxinDurationMs <= 0L) return;
    TransformComponent transform = store.getComponent(sourceTarget, TransformComponent.getComponentType());
    if (transform == null) return;
    Vector3d origin = new Vector3d((Vector3dc) transform.getPosition());
    int[] applied = {0};
    Selector.selectNearbyEntities(store, origin, astralEchoRadius, target -> {
      if (!shouldApplyAstralEcho(true, true, astralEchoMaxTargets, applied[0])) return;
      if (!SeuchenweberTargeting.isHostileCombatMob(target, caster, store)) return;
      bindTarget(state, target);
      state.ledger.apply(target.getIndex(), owner, astralEchoStacks, toxinDurationMs, nowMs, false);
      synchronizePoisonVisual(state, store, target, nowMs);
      applied[0]++;
    }, target -> target != null && target.isValid()
        && target.getIndex() != sourceTarget.getIndex() && target.getIndex() != caster.getIndex());
  }

  private RuntimeState stateFor(Store<EntityStore> store) {
    return states.getOrCreate(store,
        ignored -> new RuntimeState(stackCap, tickIntervalMs));
  }

  private static void synchronizePoisonVisual(RuntimeState state, Store<EntityStore> store,
      Ref<EntityStore> target, long nowMs) {
    int targetIndex = target.getIndex();
    NativePoisonVisuals.synchronize(store, target,
        state.ledger.maximumActiveStacks(targetIndex),
        state.ledger.maximumRemainingDurationMs(targetIndex, nowMs));
  }

  private static void bindTarget(RuntimeState state, Ref<EntityStore> target) {
    int targetIndex = target.getIndex();
    Ref<EntityStore> previous = state.targetRefByIndex.put(targetIndex, target);
    if (previous == null || previous == target) return;
    for (UUID owner : state.ledger.discardTarget(targetIndex)) {
      state.nextDiagnosisAtMs.remove(new DiagnosisKey(targetIndex, owner));
      if (!state.ledger.hasOwner(owner)) {
        state.sourceByOwner.remove(owner);
        state.echoBudget.remove(owner);
      }
    }
  }

  private static boolean belongsToStore(Ref<EntityStore> reference, Store<EntityStore> store) {
    return reference != null && store != null && reference.isValid() && reference.getStore() == store;
  }

  private static final class RuntimeState {
    private final NekrotoxinRuntimeLedger ledger;
    private final Map<UUID, Ref<EntityStore>> sourceByOwner = new HashMap<>();
    private final Map<DiagnosisKey, Long> nextDiagnosisAtMs = new HashMap<>();
    private final Map<Integer, Ref<EntityStore>> targetRefByIndex = new HashMap<>();
    private final OwnerIntervalBudget echoBudget;
    private long nextExpirationSweepAtMs;

    private RuntimeState(int stackCap, long tickIntervalMs) {
      ledger = new NekrotoxinRuntimeLedger(stackCap, tickIntervalMs);
      echoBudget = new OwnerIntervalBudget(tickIntervalMs);
    }

    private void clear() {
      synchronized (this) {
        ledger.clear();
        sourceByOwner.clear();
        nextDiagnosisAtMs.clear();
        targetRefByIndex.clear();
        echoBudget.clear();
        nextExpirationSweepAtMs = 0L;
      }
    }
  }

  private record DiagnosisKey(int targetEntityIndex, UUID owner) { }
}
