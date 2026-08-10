package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Server-authoritative blink portion of Astral Rift; the bounded rift pulse is a separate slice. */
final class AstralRiftAbility implements AbilityEffect {
  static final String EFFECT_ID = "SEUCHENWEBER_ASTRAL_RIFT";
  private final AstralRiftPulseSystem pulseSystem;
  private final long defaultDurationMs;

  AstralRiftAbility(AstralRiftPulseSystem pulseSystem, long defaultDurationMs) {
    this.pulseSystem = pulseSystem;
    this.defaultDurationMs = defaultDurationMs;
  }

  @Override public ParamSpec getParamSpec() {
    return SeuchenweberAbilityContracts.byEffectId(EFFECT_ID).paramSpec();
  }

  @Override public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
    if (context == null || !context.isPlayer() || context.casterUuid() == null) {
      return AbilityResult.conditionFailed("Requires a player caster");
    }
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    if (store == null || caster == null || !caster.isValid() || caster.getStore() != store
        || pulseSystem == null) {
      return AbilityResult.error("Astral Rift runtime is unavailable");
    }
    TransformComponent casterTransform = store.getComponent(caster, TransformComponent.getComponentType());
    HeadRotation head = store.getComponent(caster, HeadRotation.getComponentType());
    if (casterTransform == null || head == null) return AbilityResult.error("Missing caster transform or view direction");
    Ref<EntityStore> target = SeuchenweberTargeting.closestVisibleHostile(
        store, caster, ability.getNumber("teleportRange", 0.0));
    if (target == null) return AbilityResult.conditionFailed("No visible hostile target in blink range");
    TransformComponent targetTransform = store.getComponent(target, TransformComponent.getComponentType());
    if (targetTransform == null) return AbilityResult.conditionFailed("Target position is unavailable");
    Vector3d landing = landingPosition(casterTransform.getPosition(), targetTransform.getPosition());
    long durationMs = (long) ability.getNumber("riftDurationMs", defaultDurationMs);
    if (durationMs <= 0L) return AbilityResult.error("Astral Rift duration is invalid");
    Rotation3f rotation = casterTransform.getRotation().clone();
    rotation.setYaw(head.getRotation().yaw());
    rotation.setPitch(0.0f);
    rotation.setRoll(0.0f);
    store.addComponent(caster, Teleport.getComponentType(),
        new Teleport(landing, rotation).setHeadRotation(head.getRotation().clone()));
    pulseSystem.open(store, context.casterUuid(), caster, landing, durationMs);
    return AbilityResult.success();
  }

  static Vector3d landingPosition(Vector3dc origin, Vector3dc target) {
    Vector3d approach = new Vector3d(target).sub(origin);
    approach.y = 0.0;
    if (approach.lengthSquared() < 0.001) approach.set(0.0, 0.0, 1.0); else approach.normalize();
    return new Vector3d(target).sub(approach.mul(2.0));
  }
}
