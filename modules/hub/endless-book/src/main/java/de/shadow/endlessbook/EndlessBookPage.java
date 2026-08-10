package de.shadow.endlessbook;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EndlessBookPage extends InteractiveCustomUIPage<EndlessBookPage.ActionData> {
  private static final String ROW_RESOURCE = "EndlessBook/EndlessBookDetailRow.ui";
  private static final BuilderCodec<ActionData> ACTION_CODEC = BuilderCodec.builder(ActionData.class, ActionData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action).add().build();
  private List<LinkDefinition> links = List.of();

  public EndlessBookPage(PlayerRef playerRef) { super(playerRef, CustomPageLifetime.CanDismiss, ACTION_CODEC); }

  @Override public void build(Ref<EntityStore> entityRef, UICommandBuilder commands, UIEventBuilder events, Store<EntityStore> store) {
    commands.append("EndlessBook/EndlessBookHub.ui");
    boolean german = language().startsWith("de");
    commands.set("#BookTitle.Text", "ENDLESS BOOK");
    commands.set("#BookSubtitle.Text", german ? "DEIN ENDLESS-CHARAKTERBOGEN" : "YOUR ENDLESS CHARACTER SHEET");
    commands.set("#QuickAccessTitle.Text", german ? "SCHNELLZUGRIFF" : "QUICK ACCESS");

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
    appendRows(commands, "#AttributeRows", presenter.attributeRows(), german ? "Keine Attributdaten" : "No attribute data");
    appendRows(commands, "#AugmentRows", presenter.augmentRows(), german ? "Keine Augments ausgerüstet" : "No augments equipped");
    appendRows(commands, "#FateRows", presenter.fateRows(), german ? "Keine Fate-Boni aktiv" : "No fate bonuses active");
    appendRows(commands, "#SetBonusRows", presenter.setBonusRows(), german ? "Keine Fate-Setboni aktiv" : "No fate set bonuses active");

    try { links = EndlessBookConfig.load(EndlessBookPlugin.dataDirectory()).links(); }
    catch (Exception ignored) { links = EndlessBookConfig.defaultLinks(); }
    for (int index = 0; index < 12; index++) {
      String selector = "#LinkButton" + (index + 1);
      boolean visible = index < links.size();
      commands.set(selector + ".Visible", visible);
      if (visible) {
        LinkDefinition link = links.get(index);
        commands.set(selector + ".Text", EndlessBookConfig.localized(link, playerRef.getLanguage()));
        events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Action", Integer.toString(index)), false);
      }
    }
  }

  @Override public void handleDataEvent(Ref<EntityStore> entityRef, Store<EntityStore> store, ActionData data) {
    if (data == null || data.action == null) return;
    try {
      int index = Integer.parseInt(data.action);
      if (index < 0 || index >= links.size()) return;
      String command = EndlessBookConfig.normalizeCommand(links.get(index).command());
      if (EndlessBookConfig.isSafeCommand(command)) CommandManager.get().handleCommand((CommandSender) playerRef, command);
    } catch (NumberFormatException ignored) { }
  }

  private String language() {
    String language = playerRef.getLanguage();
    return language == null ? "en" : language.toLowerCase(Locale.ROOT);
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

  private static String text(Map<String, Object> map, String key, String fallback) {
    Object value = map.get(key);
    return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
  }

  private static String formatNumber(double value) {
    return value == Math.rint(value) ? Long.toString((long) value) : String.format(Locale.ROOT, "%.2f", value);
  }

  public static final class ActionData { private String action; }
}
