/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.math.vector.Rotation3f
 *  com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.ability.AbilityDefinition
 *  com.ziggfreed.mmoskilltree.ability.AbilityEffect
 *  com.ziggfreed.mmoskilltree.ability.AbilityResult
 *  com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil
 *  com.ziggfreed.mmoskilltree.ability.ActiveAbilityService
 *  com.ziggfreed.mmoskilltree.ability.BlockRaystep
 *  com.ziggfreed.mmoskilltree.ability.CasterContext
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil;
import com.ziggfreed.mmoskilltree.ability.ActiveAbilityService;
import com.ziggfreed.mmoskilltree.ability.BlockRaystep;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannDamageScaling;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannSignatureEnergy;
import de.shadow.hymann.HymannTargeting;
import de.shadow.hymann.HymannThunderStepArrivalSystem;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannThunderStepAbility
implements AbilityEffect {
    static final String EFFECT_ID = "HYMANN_THUNDER_STEP";
    private static final String DEPART_EFFECT = "Hymann_Thunder_Aegis_Cast";
    private static final ParamSpec PARAM_SPEC = ParamSpec.of(
            ParamSpec.ParamEntry.required("range", ParamSpec.ParamType.NUMBER, 25.0, "Maximum blink range in blocks"),
            ParamSpec.ParamEntry.required("radius", ParamSpec.ParamType.NUMBER, 6.0, "Lightning impact radius in blocks"),
            ParamSpec.ParamEntry.required("damage", ParamSpec.ParamType.NUMBER, 75.0, "Base lightning impact damage"),
            ParamSpec.ParamEntry.required("stunDurationMs", ParamSpec.ParamType.NUMBER, 1500.0, "Stun duration in milliseconds")
    );

    private HymannThunderStepAbility() {
    }

    static void register(HytaleLogger logger) {
        ActiveAbilityService.getInstance().register(EFFECT_ID, (AbilityEffect)new HymannThunderStepAbility());
        ((HytaleLogger.Api)logger.atInfo()).log("Registered Hymann MMO active ability: Lightning Step");
    }

    public ParamSpec getParamSpec() {
        return PARAM_SPEC;
    }

    public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
        if (!context.isPlayer() || context.casterUuid() == null || !HymannMmoBridge.isHymann(context.casterUuid())) {
            return AbilityResult.conditionFailed((String)"Requires the Hymann class");
        }
        Store store = context.store();
        Ref caster = context.caster();
        if (store == null || caster == null || !caster.isValid() || !HymannAccess.inspectInventory((Ref<EntityStore>)caster, (Store<EntityStore>)store).hasBothRelics()) {
            return AbilityResult.conditionFailed((String)"Requires Mjolnir and a Captain shield");
        }
        TransformComponent casterTransform = (TransformComponent)store.getComponent(caster, TransformComponent.getComponentType());
        if (casterTransform == null) {
            return AbilityResult.error((String)"Missing caster position");
        }
        HeadRotation headRotation = (HeadRotation)store.getComponent(caster, HeadRotation.getComponentType());
        if (headRotation == null) {
            return AbilityResult.error((String)"Missing caster look direction");
        }
        HymannConfig.Values config = HymannConfig.values();
        double range = config.lightningStepRange;
        Candidate target = HymannThunderStepAbility.closestHostileInView((Store<EntityStore>)store, (Ref<EntityStore>)caster, casterTransform, headRotation, range);
        if (target == null) {
            return AbilityResult.conditionFailed((String)"No hostile mob in front of you");
        }
        double radius = config.lightningStepRadius;
        float damage = HymannDamageScaling.scaleSkillDamage(context.casterUuid(), (Store<EntityStore>)store, (Ref<EntityStore>)caster, config.lightningStepDamage);
        long stunMs = config.lightningStepStunMs;
        AbilityStatusEffectUtil.tryAttach((Store)store, (Ref)caster, (String)DEPART_EFFECT, (float)0.65f);
        HymannSignatureEnergy.grantForAttack((Store<EntityStore>)store, (Ref<EntityStore>)caster);
        Vector3d landing = HymannThunderStepAbility.landingPosition(casterTransform.getPosition(), target.position());
        Rotation3f bodyRotation = casterTransform.getRotation().clone();
        bodyRotation.setYaw(headRotation.getRotation().yaw());
        bodyRotation.setPitch(0.0f);
        bodyRotation.setRoll(0.0f);
        Teleport teleport = new Teleport(new Vector3d((Vector3dc)landing), bodyRotation).setHeadRotation(headRotation.getRotation().clone());
        store.addComponent(caster, Teleport.getComponentType(), (Component)teleport);
        HymannThunderStepArrivalSystem.queue(context.casterUuid(), landing, radius, damage, stunMs);
        return AbilityResult.success();
    }

    private static Candidate closestHostileInView(Store<EntityStore> store, Ref<EntityStore> caster, TransformComponent casterTransform, HeadRotation headRotation, double range) {
        Vector3d origin = new Vector3d((Vector3dc)casterTransform.getPosition());
        Vector3d eyePosition = new Vector3d((Vector3dc)origin).add(0.0, 1.6, 0.0);
        Vector3d forward = new Vector3d((Vector3dc)headRotation.getDirection());
        if (forward.lengthSquared() < 1.0E-4) {
            forward.set(0.0, 0.0, 1.0);
        } else {
            forward.normalize();
        }
        World world = ((EntityStore)store.getExternalData()).getWorld();
        Candidate[] best = new Candidate[1];
        Selector.selectNearbyEntities(store, (Vector3d)origin, (double)range, entity -> {
            if (!HymannTargeting.isHostileCombatMob((Ref<EntityStore>)entity, caster, store)) {
                return;
            }
            TransformComponent transform = (TransformComponent)store.getComponent(entity, TransformComponent.getComponentType());
            if (transform == null) {
                return;
            }
            Vector3d targetPosition = new Vector3d((Vector3dc)transform.getPosition());
            Vector3d offset = new Vector3d((Vector3dc)targetPosition).sub((Vector3dc)origin);
            double distanceSquared = offset.lengthSquared();
            Vector3d towardTarget = new Vector3d((Vector3dc)targetPosition).add(0.0, 0.9, 0.0).sub((Vector3dc)eyePosition);
            double targetDistance = towardTarget.length();
            if (targetDistance < 0.001) {
                return;
            }
            towardTarget.div(targetDistance);
            if (forward.dot((Vector3dc)towardTarget) < 0.55) {
                return;
            }
            double clearDistance = BlockRaystep.clearDistance((World)world, (Vector3d)eyePosition, (Vector3d)towardTarget, (double)targetDistance, (double)0.3, (double)0.3);
            if (clearDistance + 1.2 < targetDistance) {
                return;
            }
            Candidate current = best[0];
            if (current == null || distanceSquared < current.distanceSquared()) {
                best[0] = new Candidate((Ref<EntityStore>)entity, targetPosition, distanceSquared);
            }
        }, entity -> entity != null && entity.getIndex() != caster.getIndex());
        return best[0];
    }

    private static Vector3d landingPosition(Vector3d origin, Vector3d target) {
        Vector3d approach = new Vector3d((Vector3dc)target).sub((Vector3dc)origin);
        approach.y = 0.0;
        if (approach.lengthSquared() < 0.001) {
            approach.set(0.0, 0.0, 1.0);
        } else {
            approach.normalize();
        }
        return new Vector3d((Vector3dc)target).sub((Vector3dc)approach.mul(2.0));
    }

    private record Candidate(Ref<EntityStore> entity, Vector3d position, double distanceSquared) {
    }
}
