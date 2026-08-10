package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class ShadowSwingMovementSystemTest {
  @Test void movementSystemUsesTheVerifiedEntityTickSignatureAndInstructionPath() throws Exception {
    Class<?> system = Class.forName("de.shadow.nachtweber.ShadowSwingMovementSystem");
    assertTrue(EntityTickingSystem.class.isAssignableFrom(system));
    assertNotNull(system.getDeclaredMethod(
        "tick", float.class, int.class, ArchetypeChunk.class, Store.class, CommandBuffer.class));
    ParameterizedType base = (ParameterizedType) system.getGenericSuperclass();
    assertEquals(EntityStore.class, base.getActualTypeArguments()[0]);

    String source = Files.readString(Path.of(
        "src/main/java/de/shadow/nachtweber/ShadowSwingMovementSystem.java"),
        StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(source.contains("store.isInThread()")),
        () -> assertTrue(source.contains("PlayerRef.getComponentType()")),
        () -> assertTrue(source.contains("TransformComponent.getComponentType()")),
        () -> assertTrue(source.contains("Velocity.getComponentType()")),
        () -> assertTrue(source.contains("velocity.addInstruction(")),
        () -> assertTrue(source.contains("ChangeVelocityType.Add")),
        () -> assertTrue(source.contains("Order.BEFORE")),
        () -> assertTrue(source.contains("PlayerVelocityInstructionSystem.class")),
        () -> assertFalse(source.contains("velocity.addVelocity(")));
  }
}
