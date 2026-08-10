/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.ability.AbilityDefinition
 *  com.ziggfreed.mmoskilltree.ability.AbilityEffect
 *  com.ziggfreed.mmoskilltree.ability.AbilityResult
 *  com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil
 *  com.ziggfreed.mmoskilltree.ability.ActiveAbilityService
 *  com.ziggfreed.mmoskilltree.ability.CasterContext
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil;
import com.ziggfreed.mmoskilltree.ability.ActiveAbilityService;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannStormFurySystem;

final class HymannStormFuryAbility
implements AbilityEffect {
    static final String EFFECT_ID = "HYMANN_STORM_FURY";
    private static final String AURA_EFFECT = "Hymann_Storm_Fury";

    private HymannStormFuryAbility() {
    }

    static void register(HytaleLogger logger) {
        ActiveAbilityService.getInstance().register(EFFECT_ID, (AbilityEffect)new HymannStormFuryAbility());
        ((HytaleLogger.Api)logger.atInfo()).log("Registered Hymann MMO active ability: Storm Fury");
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
        long durationMs = HymannConfig.values().stormFuryDurationMs;
        HymannStormFurySystem.activate(context.casterUuid(), (Store<EntityStore>)store, (Ref<EntityStore>)caster, durationMs);
        AbilityStatusEffectUtil.tryAttach((Store)store, (Ref)caster, (String)AURA_EFFECT, (float)((float)durationMs / 1000.0f));
        return AbilityResult.success();
    }
}

