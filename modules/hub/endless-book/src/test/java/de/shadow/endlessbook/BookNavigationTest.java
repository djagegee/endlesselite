package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookNavigationTest {
  @Test void distinguishesDetailsBackAndConfiguredLinks() {
    assertEquals(BookNavigation.Action.DETAILS, BookNavigation.resolve("details"));
    assertEquals(BookNavigation.Action.BACK, BookNavigation.resolve("back"));
    assertEquals(BookNavigation.Action.LINK, BookNavigation.resolve("profile"));
    assertEquals(BookNavigation.Action.NONE, BookNavigation.resolve(""));
  }
}
