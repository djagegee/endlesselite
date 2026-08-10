package de.shadow.endlessbook;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class BookInteraction extends SimpleInteraction {
  public static final BuilderCodec<BookInteraction> CODEC = BuilderCodec.builder(BookInteraction.class, BookInteraction::new, SimpleInteraction.CODEC).build();
  public BookInteraction() { super("endless_book_menu"); }
  @Override protected void tick0(boolean firstUpdate, float deltaTime, InteractionType actionType, InteractionContext context, CooldownHandler cooldownHandler) {
    if (firstUpdate && context != null) {
      Ref playerEntityRef = context.getOwningEntity();
      if (playerEntityRef != null) {
        Store store = playerEntityRef.getStore();
        PlayerRef ref = (PlayerRef) store.getComponent(playerEntityRef, PlayerRef.getComponentType());
        Player player = (Player) store.getComponent(playerEntityRef, Player.getComponentType());
        if (ref != null && player != null) player.getPageManager().openCustomPage(playerEntityRef, store, new EndlessBookPage(ref));
      }
    }
    super.tick0(firstUpdate, deltaTime, actionType, context, cooldownHandler);
  }
}
