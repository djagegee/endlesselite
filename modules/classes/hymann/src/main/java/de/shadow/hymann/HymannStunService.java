/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.narwhals.perfectutils.api.StunMobAPI
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.narwhals.perfectutils.api.StunMobAPI;

final class HymannStunService {
    private HymannStunService() {
    }

    static void apply(Store<EntityStore> store, Ref<EntityStore> target, long durationMs, Ref<EntityStore> source) {
        if (store == null || target == null || !target.isValid() || durationMs <= 0L) {
            return;
        }
        StunMobAPI stun = StunMobAPI.get();
        if (stun != null) {
            stun.applyStun(store, target, durationMs, source);
        }
    }
}

