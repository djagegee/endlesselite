package de.shadow.endlessbook;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import java.util.List;
import java.util.function.Consumer;

final class EndlessBookIntegrations {
  private EndlessBookIntegrations() {}

  static List<String> commandNames() { return List.of(PersonalClaimsContract.COMMAND); }

  static void registerCommands(Consumer<AbstractCommand> registrar) {
    registrar.accept(new PersonalClaimsCommand());
  }
}
