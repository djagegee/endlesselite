/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.airijko.endlessleveling.api.EndlessLevelingAPI
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.command.system.CommandContext
 *  com.hypixel.hytale.server.core.command.system.CommandManager
 *  com.hypixel.hytale.server.core.command.system.CommandSender
 *  com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
 *  com.hypixel.hytale.server.core.permissions.PermissionsModule
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.hymann.HymannAccess;
import java.util.Set;

final class HymannClaimCommand
extends AbstractPlayerCommand {
    private static final int REQUIRED_PRESTIGE = 25;

    HymannClaimCommand() {
        super("hymann", "Unlock the Hymann ascension with Mjolnir and a Captain shield");
    }

    protected boolean canGeneratePermission() {
        return false;
    }

    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> playerEntityRef, PlayerRef playerRef, World world) {
        if (playerRef.hasPermission("shadow.hymann.worthy")) {
            playerRef.sendMessage(HymannClaimCommand.message(playerRef, "Hymann ist bereits f\u00fcr dich freigeschaltet.", "Hymann is already unlocked for you.", "#63e6ff"));
            return;
        }
        int prestige = EndlessLevelingAPI.get().getPlayerPrestigeLevel(playerRef.getUuid());
        if (prestige < 25) {
            playerRef.sendMessage(HymannClaimCommand.message(playerRef, "Hymann wird erst ab Prestige 25 freigeschaltet. Aktuell: " + prestige + ".", "Hymann unlocks at prestige 25. Current prestige: " + prestige + ".", "#ff6b6b"));
            return;
        }
        HymannAccess.RelicCheck relics = HymannAccess.inspectInventory(playerEntityRef, store);
        if (!relics.hasBothRelics()) {
            String missing = !relics.hasMjolnir() && !relics.hasShield() ? HymannClaimCommand.localized(playerRef, "Mj\u00f6lnir und einen Starky-Captain-Schild", "Mjolnir and a Starky Captain shield") : (!relics.hasMjolnir() ? HymannClaimCommand.localized(playerRef, "Mj\u00f6lnir", "Mjolnir") : HymannClaimCommand.localized(playerRef, "einen Starky-Captain-Schild", "a Starky Captain shield"));
            playerRef.sendMessage(HymannClaimCommand.message(playerRef, "Hymann verlangt als Beweis der W\u00fcrdigkeit: " + missing + ".", "Hymann requires proof of worthiness: " + missing + ".", "#ff6b6b"));
            return;
        }
        PermissionsModule permissions = PermissionsModule.get();
        if (permissions == null) {
            playerRef.sendMessage(HymannClaimCommand.message(playerRef, "Die Berechtigungsverwaltung ist noch nicht bereit. Bitte versuche es gleich erneut.", "The permissions service is not ready yet. Please try again in a moment.", "#ff6b6b"));
            return;
        }
        permissions.addUserPermission(playerRef.getUuid(), Set.of("shadow.hymann.worthy"));
        playerRef.sendMessage(HymannClaimCommand.message(playerRef, "W\u00fcrdig befunden. Hymann wurde freigeschaltet und wird jetzt als Prim\u00e4rklasse gew\u00e4hlt.", "Worthy. Hymann is unlocked and will now be selected as your primary class.", "#63e6ff"));
        CommandManager.get().handleCommand((CommandSender)playerRef, "class choose primary hymann");
    }

    private static Message message(PlayerRef player, String german, String english, String color) {
        return Message.raw((String)HymannClaimCommand.localized(player, german, english)).color(color);
    }

    private static String localized(PlayerRef player, String german, String english) {
        String language = player.getLanguage();
        return language != null && language.toLowerCase().startsWith("de") ? german : english;
    }
}
