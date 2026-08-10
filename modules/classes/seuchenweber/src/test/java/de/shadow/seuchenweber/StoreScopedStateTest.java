package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoreScopedStateTest {
  @Test void isolatesEqualButDistinctStoreIdentities() {
    StoreScopedState<EqualStore, Object> states = new StoreScopedState<>();
    EqualStore first = new EqualStore("world");
    EqualStore second = new EqualStore("world");

    Object firstState = states.getOrCreate(first, ignored -> new Object());
    Object secondState = states.getOrCreate(second, ignored -> new Object());

    assertNotSame(firstState, secondState);
    assertSame(firstState, states.getOrCreate(first, ignored -> new Object()));
    assertEquals(2, states.size());
  }

  @Test void clearVisitsAndRemovesEveryStoreState() {
    StoreScopedState<Object, Object> states = new StoreScopedState<>();
    Object first = states.getOrCreate(new Object(), ignored -> new Object());
    Object second = states.getOrCreate(new Object(), ignored -> new Object());
    List<Object> cleared = new ArrayList<>();

    states.clear(cleared::add);

    assertEquals(0, states.size());
    assertTrue(cleared.contains(first));
    assertTrue(cleared.contains(second));
  }

  @Test void removeVisitsAndRemovesOnlyTheRequestedStoreState() {
    StoreScopedState<Object, Object> states = new StoreScopedState<>();
    Object firstStore = new Object();
    Object secondStore = new Object();
    Object first = states.getOrCreate(firstStore, ignored -> new Object());
    Object second = states.getOrCreate(secondStore, ignored -> new Object());
    List<Object> removed = new ArrayList<>();

    states.remove(firstStore, removed::add);

    assertEquals(1, states.size());
    assertTrue(removed.contains(first));
    assertTrue(!removed.contains(second));
    assertSame(second, states.getOrCreate(secondStore, ignored -> new Object()));
  }

  @Test void ownerIntervalBudgetIsOwnerIsolatedAndBounded() {
    OwnerIntervalBudget budget = new OwnerIntervalBudget(1_000L);
    UUID first = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");

    assertTrue(budget.tryClaim(first, 5_000L));
    assertEquals(false, budget.tryClaim(first, 5_999L));
    assertTrue(budget.tryClaim(second, 5_999L));
    assertTrue(budget.tryClaim(first, 6_000L));
  }

  private record EqualStore(String id) { }
}
