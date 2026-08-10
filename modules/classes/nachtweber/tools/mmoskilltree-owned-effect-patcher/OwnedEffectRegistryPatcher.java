import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Adds ownership-safe effect registration lifecycle primitives to MMOSkillTree 1.5.2. */
public final class OwnedEffectRegistryPatcher {
  private static final String ENTRY =
      "com/ziggfreed/mmoskilltree/ability/ActiveAbilityService.class";
  private static final String OWNER =
      "com/ziggfreed/mmoskilltree/ability/ActiveAbilityService";
  private static final String EFFECT =
      "com/ziggfreed/mmoskilltree/ability/AbilityEffect";
  private static final String MAP = "java/util/Map";
  private static final String STRING = "java/lang/String";
  private static final String REGISTER_DESC = "(Ljava/lang/String;L" + EFFECT + ";)Z";

  private OwnedEffectRegistryPatcher() { }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: OwnedEffectRegistryPatcher <input.jar> <output.jar>");
    }
    Path input = Path.of(args[0]).toAbsolutePath().normalize();
    Path output = Path.of(args[1]).toAbsolutePath().normalize();
    if (input.equals(output)) throw new IllegalArgumentException("Input and output must differ");
    if (!Files.isRegularFile(input)) throw new IllegalArgumentException("Input JAR missing: " + input);
    Files.createDirectories(output.getParent());
    Path temporary = Files.createTempFile(output.getParent(), "mmoskilltree-owned-", ".jar");
    boolean patched = false;
    try (JarFile source = new JarFile(input.toFile());
         OutputStream raw = Files.newOutputStream(temporary);
         JarOutputStream target = new JarOutputStream(raw)) {
      Enumeration<JarEntry> entries = source.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        target.putNextEntry(new JarEntry(entry));
        if (!entry.isDirectory()) {
          try (InputStream stream = source.getInputStream(entry)) {
            byte[] bytes = stream.readAllBytes();
            if (ENTRY.equals(entry.getName())) {
              if (patched) throw new IllegalStateException("Duplicate ActiveAbilityService entry");
              bytes = patch(bytes);
              patched = true;
            }
            target.write(bytes);
          }
        }
        target.closeEntry();
      }
    } catch (Throwable failure) {
      Files.deleteIfExists(temporary);
      throw failure;
    }
    if (!patched) {
      Files.deleteIfExists(temporary);
      throw new IllegalStateException("ActiveAbilityService class missing");
    }
    Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);
    System.out.println("PATCHED=" + output);
  }

  private static byte[] patch(byte[] original) {
    ClassReader reader = new ClassReader(original);
    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    int[] existingRegisterIfAbsent = {0};
    int[] existingUnregister = {0};
    ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor,
          String signature, String[] exceptions) {
        if ("registerIfAbsent".equals(name) && REGISTER_DESC.equals(descriptor)) {
          existingRegisterIfAbsent[0]++;
        }
        if ("unregister".equals(name) && REGISTER_DESC.equals(descriptor)) {
          existingUnregister[0]++;
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
      }

      @Override
      public void visitEnd() {
        if (existingRegisterIfAbsent[0] != 0 || existingUnregister[0] != 0) {
          throw new IllegalStateException("Patch already present or incompatible ABI");
        }
        addRegisterIfAbsent(super.visitMethod(Opcodes.ACC_PUBLIC, "registerIfAbsent",
            REGISTER_DESC, null, null));
        addUnregister(super.visitMethod(Opcodes.ACC_PUBLIC, "unregister",
            REGISTER_DESC, null, null));
        super.visitEnd();
      }
    };
    reader.accept(visitor, 0);
    return writer.toByteArray();
  }

  private static void loadMapAndUpperKey(MethodVisitor method) {
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitFieldInsn(Opcodes.GETFIELD, OWNER, "effectsByDiscriminator", "Ljava/util/Map;");
    method.visitVarInsn(Opcodes.ALOAD, 1);
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, STRING, "toUpperCase", "()Ljava/lang/String;", false);
  }

  private static void addRegisterIfAbsent(MethodVisitor method) {
    method.visitCode();
    loadMapAndUpperKey(method);
    method.visitVarInsn(Opcodes.ALOAD, 2);
    method.visitMethodInsn(Opcodes.INVOKEINTERFACE, MAP, "putIfAbsent",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
    Label occupied = new Label();
    method.visitJumpInsn(Opcodes.IFNONNULL, occupied);
    method.visitInsn(Opcodes.ICONST_1);
    method.visitInsn(Opcodes.IRETURN);
    method.visitLabel(occupied);
    method.visitInsn(Opcodes.ICONST_0);
    method.visitInsn(Opcodes.IRETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
  }

  private static void addUnregister(MethodVisitor method) {
    method.visitCode();
    loadMapAndUpperKey(method);
    method.visitVarInsn(Opcodes.ALOAD, 2);
    method.visitMethodInsn(Opcodes.INVOKEINTERFACE, MAP, "remove",
        "(Ljava/lang/Object;Ljava/lang/Object;)Z", true);
    method.visitInsn(Opcodes.IRETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
  }
}
