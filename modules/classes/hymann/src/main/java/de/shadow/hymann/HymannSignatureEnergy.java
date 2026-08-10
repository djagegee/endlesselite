/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.hymann.HymannConfig;

final class HymannSignatureEnergy {
    private HymannSignatureEnergy() {
    }

    static void grantForAttack(Store<EntityStore> store, Ref<EntityStore> entity) {
        EntityStatMap stats = (EntityStatMap)store.getComponent(entity, EntityStatMap.getComponentType());
        if (stats == null) {
            return;
        }
        EntityStatValue signature = stats.get(DefaultEntityStatTypes.getSignatureEnergy());
        if (signature == null || signature.get() >= signature.getMax()) {
            return;
        }
        float amount = signature.getMax() * HymannConfig.values().attackSkillSignatureGainPercent;
        if (amount > 0.0f) {
            stats.addStatValue(DefaultEntityStatTypes.getSignatureEnergy(), amount);
        }
    }
}

