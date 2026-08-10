/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.airijko.endlessleveling.api.EndlessLevelingAPI
 *  com.airijko.endlessleveling.enums.SkillAttributeType
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.airijko.endlessleveling.enums.SkillAttributeType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import de.shadow.hymann.HymannTreeRewards;
import java.util.UUID;

final class HymannDamageScaling {
    private HymannDamageScaling() {
    }

    static float scaleSkillDamage(UUID playerUuid, Store<EntityStore> store, Ref<EntityStore> player, float baseDamage) {
        if (playerUuid == null || store == null || player == null || baseDamage <= 0.0f) {
            return baseDamage;
        }
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, player);
        double treeMultiplier = 1.0 + HymannTreeRewards.skillDamagePercent(skills) / 100.0;
        double sorcery = EndlessLevelingAPI.get().getDisplayedAttributeTotal(playerUuid, SkillAttributeType.SORCERY, 0.0);
        return (float)(((double)baseDamage + Math.max(0.0, sorcery)) * treeMultiplier);
    }
}

