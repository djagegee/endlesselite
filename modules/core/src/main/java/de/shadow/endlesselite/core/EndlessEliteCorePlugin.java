package de.shadow.endlesselite.core;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/** Shared runtime and asset owner for the Endless Elite distribution. */
public final class EndlessEliteCorePlugin extends JavaPlugin {
  private static volatile GameplayOwnership gameplayOwnership;

  public EndlessEliteCorePlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    GameplayOwnership ownership = new GameplayOwnership();
    ownership.claim(GameplayConcern.PLAYER_PROGRESSION, "Airijko:EndlessLevelingCore");
    ownership.claim(GameplayConcern.PLAYER_UI, "Shadow:EndlessBook");
    gameplayOwnership = ownership;
    ((HytaleLogger.Api) getLogger().atInfo()).log(
        "Shadow:EndlessElite initialized: shared localization and ownership registry active");
  }

  @Override
  protected void shutdown() {
    gameplayOwnership = null;
    ((HytaleLogger.Api) getLogger().atInfo()).log("Shadow:EndlessElite shut down");
  }

  public static GameplayOwnership gameplayOwnership() {
    GameplayOwnership ownership = gameplayOwnership;
    if (ownership == null) throw new IllegalStateException("Endless Elite core is not initialized");
    return ownership;
  }
}
