package de.shadow.endlessbook;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

public final class EndlessBookDetailsPage extends InteractiveCustomUIPage<EndlessBookDetailsPage.ActionData> {
  private static final String ROW_RESOURCE = "EndlessBook/EndlessBookDetailRow.ui";
  private final PlayerRef playerRef;

  public EndlessBookDetailsPage(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CantClose, ActionData.CODEC);
    this.playerRef = playerRef;
  }

  @Override public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
    commands.append("EndlessBook/EndlessBookDetails.ui");
    events.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton",
        EventData.of("Action", "back"), false);

    EndlessDetailsData data = EndlessDetailsBridge.load(playerRef.getUuid());
    EndlessDetailsPresenter presenter = new EndlessDetailsPresenter(data);
    Map<String, Object> profile = data.profile();

    commands.set("#PlayerName.Text", playerRef.getUsername());
    commands.set("#PrestigeValue.Text", text(profile, "prestige", "--"));
    commands.set("#LevelValue.Text", text(profile, "level", "--"));
    commands.set("#RaceValue.Text", text(profile, "race", "--"));
    commands.set("#PrimaryClassValue.Text", text(profile, "primaryClass", text(profile, "charClass", "--")));
    commands.set("#SecondaryClassValue.Text", text(profile, "secondaryClass", "--"));
    commands.set("#XpValue.Text", formatNumber(data.currentXp()) + " / " + formatNumber(data.nextLevelXp()));
    double progress = data.nextLevelXp() <= 0.0 ? 0.0 : data.currentXp() / data.nextLevelXp();
    commands.set("#XpProgress.Value", (float) Math.max(0.0, Math.min(1.0, progress)));

    boolean german = language().startsWith("de");
    appendRows(commands, "#AttributeRows", presenter.attributeRows(), german ? "Keine Attributdaten" : "No attribute data");
    appendRows(commands, "#AugmentRows", presenter.augmentRows(), german ? "Keine Augments ausgerüstet" : "No augments equipped");
    appendRows(commands, "#FateRows", presenter.fateRows(), german ? "Keine Fate-Boni aktiv" : "No fate bonuses active");
    appendRows(commands, "#SetBonusRows", presenter.setBonusRows(), german ? "Keine Fate-Setboni aktiv" : "No fate set bonuses active");
  }

  @Override public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
      @Nonnull ActionData data) {
    if (BookNavigation.resolve(data.action) != BookNavigation.Action.BACK) return;
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player != null) player.getPageManager().openCustomPage(ref, store, new EndlessBookPage(playerRef));
  }

  private static void appendRows(UICommandBuilder commands, String container,
      List<EndlessDetailsPresenter.DetailRow> rows, String emptyLabel) {
    List<EndlessDetailsPresenter.DetailRow> actual = rows.isEmpty()
        ? List.of(new EndlessDetailsPresenter.DetailRow(emptyLabel, "", "")) : rows;
    for (int i = 0; i < actual.size(); i++) {
      EndlessDetailsPresenter.DetailRow row = actual.get(i);
      commands.append(container, ROW_RESOURCE);
      String selector = container + "[" + i + "]";
      commands.set(selector + " #RowLabel.Text", row.label());
      commands.set(selector + " #RowValue.Text", row.value());
      commands.set(selector + " #RowDetails.Text", row.details());
    }
  }

  private String language() {
    String language = playerRef.getLanguage();
    return language == null ? "en" : language.toLowerCase(Locale.ROOT);
  }

  private static String text(Map<String, Object> map, String key, String fallback) {
    Object value = map.get(key);
    return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
  }

  private static String formatNumber(double value) {
    return value == Math.rint(value) ? Long.toString((long) value) : String.format(Locale.ROOT, "%.2f", value);
  }

  public static final class ActionData {
    static final BuilderCodec<ActionData> CODEC = BuilderCodec.builder(ActionData.class, ActionData::new)
        .append(new KeyedCodec<>("Action", Codec.STRING), (data, action) -> data.action = action, data -> data.action)
        .add().build();
    private String action;
  }
}
