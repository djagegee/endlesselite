/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.ability.AbilityDefinition
 *  com.ziggfreed.mmoskilltree.ability.AbilityEffect
 *  com.ziggfreed.mmoskilltree.ability.AbilityResult
 *  com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil
 *  com.ziggfreed.mmoskilltree.ability.ActiveAbilityService
 *  com.ziggfreed.mmoskilltree.ability.CasterContext
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil;
import com.ziggfreed.mmoskilltree.ability.ActiveAbilityService;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannDamageScaling;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannSignatureEnergy;
import de.shadow.hymann.HymannStunService;
import de.shadow.hymann.HymannTargeting;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannThunderAegisAbility
implements AbilityEffect {
    static final String EFFECT_ID = "HYMANN_THUNDER_AEGIS";
    private static final String CAST_EFFECT = "Hymann_Thunder_Aegis_Cast";

    private HymannThunderAegisAbility() {
    }

    static AbilityEffect create() {
        return new HymannThunderAegisAbility();
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
        HymannConfig.Values config = HymannConfig.values();
        double radius = config.thunderAegisRadius;
        float damage = HymannDamageScaling.scaleSkillDamage(context.casterUuid(), (Store<EntityStore>)store, (Ref<EntityStore>)caster, config.thunderAegisDamage);
        AbilityStatusEffectUtil.tryAttach((Store)store, (Ref)caster, (String)CAST_EFFECT, (float)1.3f);
        Vector3d center = new Vector3d((Vector3dc)casterTransform.getPosition());
        HymannSignatureEnergy.grantForAttack((Store<EntityStore>)store, (Ref<EntityStore>)caster);
        HymannThunderAegisAbility.healNearbyPlayers((Store<EntityStore>)store, center, radius, config.thunderAegisHealPercent);
        HymannThunderAegisAbility.strikeNearbyEnemies((Store<EntityStore>)store, (Ref<EntityStore>)caster, center, radius, damage, config.thunderAegisStunMs);
        return AbilityResult.success();
    }

    private static void healNearbyPlayers(Store<EntityStore> store, Vector3d center, double radius, float percentOfMax) {
        World world = ((EntityStore)store.getExternalData()).getWorld();
        double radiusSquared = radius * radius;
        for (PlayerRef ally : world.getPlayerRefs()) {
            EntityStatValue health;
            EntityStatMap stats;
            TransformComponent transform;
            Ref entity = ally.getReference();
            if (entity == null || !entity.isValid() || (transform = (TransformComponent)store.getComponent(entity, TransformComponent.getComponentType())) == null || transform.getPosition().distanceSquared((Vector3dc)center) > radiusSquared || (stats = (EntityStatMap)store.getComponent(entity, EntityStatMap.getComponentType())) == null || (health = stats.get(DefaultEntityStatTypes.getHealth())) == null || !(health.get() < health.getMax())) continue;
            stats.addStatValue(DefaultEntityStatTypes.getHealth(), health.getMax() * percentOfMax);
        }
    }

    private static void strikeNearbyEnemies(Store<EntityStore> store, Ref<EntityStore> caster, Vector3d center, double radius, float damage, long stunMs) {
        long casterIndex = caster.getIndex();
        Selector.selectNearbyEntities(store, (Vector3d)center, (double)radius, target -> {
            if (!HymannTargeting.isHostileCombatMob((Ref<EntityStore>)target, caster, store)) {
                return;
            }
            TransformComponent targetTransform = (TransformComponent)store.getComponent(target, TransformComponent.getComponentType());
            if (targetTransform == null) {
                return;
            }
            HymannStunService.apply(store, (Ref<EntityStore>)target, stunMs, caster);
            Damage thunder = new Damage((Damage.Source)new Damage.EntitySource(caster), DamageCause.PHYSICAL, damage);
            thunder.putMetaObject(Damage.KNOCKBACK_COMPONENT, HymannThunderAegisAbility.knockback(center, new Vector3d((Vector3dc)targetTransform.getPosition())));
            DamageSystems.executeDamage((Ref)target, (ComponentAccessor)store, (Damage)thunder);
        }, target -> target != null && (long)target.getIndex() != casterIndex);
    }

    private static KnockbackComponent knockback(Vector3d center, Vector3d target) {
        Vector3d direction = target.sub((Vector3dc)center);
        direction.y = 0.0;
        if (direction.lengthSquared() < 0.001) {
            direction.set(0.0, 0.0, 1.0);
        } else {
            direction.normalize();
        }
        direction.mul(11.0).add(0.0, 5.5, 0.0);
        KnockbackComponent knockback = new KnockbackComponent();
        knockback.setVelocity(direction);
        knockback.setDuration(0.55f);
        return knockback;
    }
}
