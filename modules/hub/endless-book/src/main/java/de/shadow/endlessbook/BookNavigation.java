package de.shadow.endlessbook;

final class BookNavigation {
  enum Action { DETAILS, BACK, LINK, NONE }

  private BookNavigation() {}

  static Action resolve(String action) {
    if (action == null || action.isBlank()) return Action.NONE;
    return switch (action) {
      case "details" -> Action.DETAILS;
      case "back" -> Action.BACK;
      default -> Action.LINK;
    };
  }
}
