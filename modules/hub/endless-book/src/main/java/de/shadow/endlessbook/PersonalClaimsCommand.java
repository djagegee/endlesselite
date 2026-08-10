package de.shadow.endlessbook;

import com.airijko.endlessguilds.guild.GuildService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;

final class PersonalClaimsCommand extends AbstractPlayerCommand {
  PersonalClaimsCommand() {
    super(PersonalClaimsContract.COMMAND, "Open your personal Endless Guilds claim map");
    addAliases(PersonalClaimsContract.ALIASES.toArray(String[]::new));
  }

  @Override protected boolean canGeneratePermission() { return false; }

  @Override protected void execute(CommandContext context, Store<EntityStore> store,
      Ref<EntityStore> playerEntityRef, PlayerRef playerRef, World world) {
    GuildService guilds = GuildService.get();
    if (guilds != null && guilds.isInGuild(playerRef.getUuid())) {
      playerRef.sendMessage(Message.raw(localized(playerRef,
          "Du bist in einer Gilde. Öffne die Gildenkarte mit /claims.",
          "You are in a guild. Open the guild map with /claims.")).color("#ffd58a"));
      return;
    }
    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
    if (player != null) player.getPageManager().openCustomPage(playerEntityRef, store, new PersonalClaimsPage(playerRef));
  }

  private static String localized(PlayerRef player, String german, String english) {
    String language = player.getLanguage();
    return language != null && language.toLowerCase(Locale.ROOT).startsWith("de") ? german : english;
  }
}
