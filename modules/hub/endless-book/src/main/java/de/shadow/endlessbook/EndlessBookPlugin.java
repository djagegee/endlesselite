package de.shadow.endlessbook;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.quest.QuestComponent;
import java.nio.file.Path;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class EndlessBookPlugin extends JavaPlugin {
  static final String ITEM_ID = "EndlessBook_LevelMenu";
  private static volatile Path dataDirectory;
  private ScheduledExecutorService executor;

  public EndlessBookPlugin(JavaPluginInit init) { super(init); }
  static Path dataDirectory() { return dataDirectory; }

  @Override protected void setup() {
    dataDirectory = getDataDirectory();
    try { EndlessBookConfig.bootstrap(dataDirectory); }
    catch (Exception e) { getLogger().at(Level.SEVERE).log("Could not create Endless Book core config: %s", e.getMessage()); }
    EndlessBookIntegrations.registerCommands(command -> getCommandRegistry().registerCommand(command));
    getCodecRegistry(Interaction.CODEC).register("EndlessLevelMenu", BookInteraction.class, BookInteraction.CODEC);
    getEventRegistry().registerGlobal(EventPriority.LAST, PlayerReadyEvent.class, event -> placeIfEntitled(event.getPlayer(), "player ready"));
    executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "EndlessBook-SlotGuard");
      thread.setDaemon(true);
      return thread;
    });
    executor.scheduleAtFixedRate(this::queueSlotGuard, 500L, 500L, TimeUnit.MILLISECONDS);
  }

  @Override protected void shutdown() {
    if (executor != null) executor.shutdownNow();
    executor = null;
    dataDirectory = null;
  }

  private void queueSlotGuard() {
    try {
      for (World world : Universe.get().getWorlds().values()) {
        if (world.isAlive()) world.execute(() -> world.getPlayers().forEach(player -> placeIfEntitled((Player) player, "quest slot guard")));
      }
    } catch (Throwable t) {
      getLogger().at(Level.FINE).log("Could not queue Endless Book slot guard: %s", t.getMessage());
    }
  }

  private void placeIfEntitled(Player player, String source) {
    if (player == null || player.getReference() == null || player.getInventory() == null || QuestComponent.TYPE == null) return;
    QuestComponent quests = player.getReference().getStore().getComponent(player.getReference(), QuestComponent.TYPE);
    if (quests == null || !QuestEntitlement.mayOwnBook(quests.isQuestCompleted(QuestEntitlement.REQUIRED_QUEST))) return;
    placeBook(player, source);
  }

  private void placeBook(Player player, String source) {
    Item item = Item.getAssetMap().getAsset(ITEM_ID);
    InventoryComponent.Hotbar component = player.getPlayerRef().getComponent(InventoryComponent.Hotbar.getComponentType());
    if (item == null || component == null || component.getInventory() == null || component.getInventory().getCapacity() == 0) return;
    ItemContainer hotbar = component.getInventory();
    short slot = (short) (hotbar.getCapacity() - 1);
    ItemStack current = hotbar.getItemStack(slot);
    if (!isBook(current)) {
      unlock(hotbar, slot);
      hotbar.setItemStackForSlot(slot, new ItemStack(item.getId(), 1));
      component.markDirty();
      if (current != null && !current.isEmpty()) Player.giveItem(current.cleanCopy(), player.getReference(), (ComponentAccessor) player.getReference().getStore());
      getLogger().at(Level.INFO).log("Placed Endless Book in final hotbar slot (%s)", source);
    }
    lock(hotbar, slot);
    removeDuplicates(player, hotbar, slot);
  }

  private void removeDuplicates(Player player, ItemContainer hotbar, short protectedSlot) {
    removeBooks(hotbar, protectedSlot);
    removeBooks(player.getPlayerRef().getComponent(InventoryComponent.Storage.getComponentType()), (short)-1);
    removeBooks(player.getPlayerRef().getComponent(InventoryComponent.Backpack.getComponentType()), (short)-1);
    removeBooks(player.getPlayerRef().getComponent(InventoryComponent.Utility.getComponentType()), (short)-1);
    removeBooks(player.getPlayerRef().getComponent(InventoryComponent.Tool.getComponentType()), (short)-1);
  }

  private void removeBooks(InventoryComponent component, short protectedSlot) {
    if (component == null || component.getInventory() == null) return;
    ItemContainer inventory = component.getInventory();
    boolean changed = removeBooks(inventory, protectedSlot);
    if (changed) component.markDirty();
  }

  private boolean removeBooks(ItemContainer inventory, short protectedSlot) {
    boolean changed = false;
    for (short slot = 0; slot < inventory.getCapacity(); slot++) {
      if (slot != protectedSlot && isBook(inventory.getItemStack(slot))) { inventory.removeItemStackFromSlot(slot); changed = true; }
    }
    return changed;
  }

  private static boolean isBook(ItemStack stack) { return stack != null && !stack.isEmpty() && ITEM_ID.equals(stack.getItemId()); }
  private static void lock(ItemContainer bar, short slot) {
    bar.setSlotFilter(FilterActionType.ADD, slot, SlotFilter.DENY);
    bar.setSlotFilter(FilterActionType.REMOVE, slot, SlotFilter.DENY);
    bar.setSlotFilter(FilterActionType.DROP, slot, SlotFilter.DENY);
  }
  private static void unlock(ItemContainer bar, short slot) {
    bar.setSlotFilter(FilterActionType.ADD, slot, SlotFilter.ALLOW);
    bar.setSlotFilter(FilterActionType.REMOVE, slot, SlotFilter.ALLOW);
    bar.setSlotFilter(FilterActionType.DROP, slot, SlotFilter.ALLOW);
  }
}
