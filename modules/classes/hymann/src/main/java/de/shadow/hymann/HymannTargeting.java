/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.hypixel.hytale.server.npc.entities.NPCEntity
 *  com.hypixel.hytale.server.npc.role.Role
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Locale;

final class HymannTargeting {
    private static final String[] NON_COMBAT_ROLE_HINTS = new String[]{"aetherhaven", "banker", "blacksmith", "citizen", "guide", "hycitizens", "merchant", "quest", "shop", "tamer", "trader", "trainer", "vendor", "villager"};

    private HymannTargeting() {
    }

    static boolean isHostileCombatMob(Ref<EntityStore> target, Ref<EntityStore> caster, Store<EntityStore> store) {
        Role role;
        if (target == null || caster == null || !target.isValid() || target.getIndex() == caster.getIndex() || store.getComponent(target, NPCMarkerComponent.getComponentType()) == null) {
            return false;
        }
        NPCEntity npc = (NPCEntity)store.getComponent(target, NPCEntity.getComponentType());
        Role role2 = role = npc == null ? null : npc.getRole();
        if (role == null || role.isInvulnerable() || role.isFriendly(caster, store) || HymannTargeting.isServiceRole(npc.getRoleName()) || HymannTargeting.isServiceRole(role.getRoleName()) || HymannTargeting.isServiceRole(role.getNameTranslationKey())) {
            return false;
        }
        return npc.getCanCauseDamage(caster, store);
    }

    private static boolean isServiceRole(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String hint : NON_COMBAT_ROLE_HINTS) {
            if (!normalized.contains(hint)) continue;
            return true;
        }
        return false;
    }
}
