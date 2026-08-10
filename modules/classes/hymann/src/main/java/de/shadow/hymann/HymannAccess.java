/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.inventory.InventoryComponent
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;

final class HymannAccess {
    static final String PERMISSION = "shadow.hymann.worthy";

    private HymannAccess() {
    }

    static RelicCheck inspectInventory(Ref<EntityStore> playerEntityRef, Store<EntityStore> store) {
        CombinedItemContainer inventory = InventoryComponent.getCombined(store, playerEntityRef, (ComponentType[])InventoryComponent.EVERYTHING);
        if (inventory == null) {
            return new RelicCheck(false, false);
        }
        boolean hasMjolnir = false;
        boolean hasShield = false;
        for (short slot = 0; slot < inventory.getCapacity(); slot = (short)(slot + 1)) {
            ItemStack stack = inventory.getItemStack(slot);
            if (stack == null || stack.isEmpty()) continue;
            String itemId = stack.getItemId();
            if ((hasMjolnir |= HymannAccess.isMjolnir(itemId)) && (hasShield |= HymannAccess.isCaptainShield(itemId))) break;
        }
        return new RelicCheck(hasMjolnir, hasShield);
    }

    static boolean isMjolnir(String itemId) {
        return "Weapon_Mjolnir_Starky".equals(itemId) || "Weapon_Mjolnir_Calling_Starky".equals(itemId) || "Weapon_Mjolnir_Caller_Starky".equals(itemId);
    }

    static boolean isCaptainShield(String itemId) {
        return itemId != null && (itemId.startsWith("Weapon_Shield_") || itemId.startsWith("Weapon_ShieldLeft_")) && itemId.endsWith("_Starky");
    }

    static boolean isArmament(String itemId) {
        return HymannAccess.isMjolnir(itemId) || HymannAccess.isCaptainShield(itemId) || HymannAccess.isMace(itemId);
    }

    static boolean isMace(String itemId) {
        return itemId != null && itemId.toLowerCase(Locale.ROOT).contains("mace");
    }

    record RelicCheck(boolean hasMjolnir, boolean hasShield) {
        boolean hasBothRelics() {
            return this.hasMjolnir && this.hasShield;
        }
    }
}
