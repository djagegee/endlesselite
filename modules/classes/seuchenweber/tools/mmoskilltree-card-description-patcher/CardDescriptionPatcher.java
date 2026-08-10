import java.io.IOException;
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

/**
 * Applies opt-in custom descriptions and an unambiguous passive-card label to MMOSkillTree 1.5.2.
 * Exactly AbilityDescriptionRenderer and AbilityBindPage are changed.
 */
public final class CardDescriptionPatcher {
  private static final String RENDERER_ENTRY =
      "com/ziggfreed/mmoskilltree/ability/AbilityDescriptionRenderer.class";
  private static final String BIND_PAGE_ENTRY =
      "com/ziggfreed/mmoskilltree/pages/skill/AbilityBindPage.class";
  private static final String RENDERER =
      "com/ziggfreed/mmoskilltree/ability/AbilityDescriptionRenderer";
  private static final String DEFINITION =
      "com/ziggfreed/mmoskilltree/ability/AbilityDefinition";
  private static final String SKILL_COMPONENT =
      "com/ziggfreed/mmoskilltree/data/SkillComponent";
  private static final String MESSAGES =
      "com/ziggfreed/mmoskilltree/i18n/Messages";
  private static final String MESSAGE =
      "com/hypixel/hytale/server/core/Message";
  private static final String CARD_DESC = "(L" + DEFINITION + ";)L" + MESSAGE + ";";
  private static final String FULL_DESC = "(L" + SKILL_COMPONENT + ";L" + DEFINITION
      + ";)L" + MESSAGE + ";";
  private static final String ORIGINAL_RENDER_MSG = "renderMsg$seuchenweberOriginal";
  private static final String OPT_IN_PARAM = "useCustomCardDescription";
  private static final String SELECT_DISABLED_KEY = "ability.bind_page.select_disabled";
  private static final String PASSIVE_TAG_KEY = "ability.bind_page.passive_tag";

  private CardDescriptionPatcher() { }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: CardDescriptionPatcher <input.jar> <output.jar>");
    }
    Path input = Path.of(args[0]).toAbsolutePath().normalize();
    Path output = Path.of(args[1]).toAbsolutePath().normalize();
    if (input.equals(output)) throw new IllegalArgumentException("Input and output must differ");
    if (!Files.isRegularFile(input)) throw new IOException("Input JAR not found: " + input);
    Files.createDirectories(output.getParent());
    Path temporary = Files.createTempFile(output.getParent(), "mmoskilltree-patch-", ".jar");
    boolean rendererPatched = false;
    boolean bindPagePatched = false;
    try (JarFile source = new JarFile(input.toFile());
         OutputStream raw = Files.newOutputStream(temporary);
         JarOutputStream target = new JarOutputStream(raw)) {
      Enumeration<JarEntry> entries = source.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        JarEntry copy = new JarEntry(entry);
        target.putNextEntry(copy);
        if (!entry.isDirectory()) {
          try (InputStream stream = source.getInputStream(entry)) {
            byte[] bytes = stream.readAllBytes();
            if (RENDERER_ENTRY.equals(entry.getName())) {
              if (rendererPatched) throw new IllegalStateException("Duplicate renderer class entry");
              bytes = patchRenderer(bytes);
              rendererPatched = true;
            } else if (BIND_PAGE_ENTRY.equals(entry.getName())) {
              if (bindPagePatched) throw new IllegalStateException("Duplicate bind-page class entry");
              bytes = patchBindPage(bytes);
              bindPagePatched = true;
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
    if (!rendererPatched || !bindPagePatched) {
      Files.deleteIfExists(temporary);
      throw new IllegalStateException("Required MMOSkillTree classes missing: renderer="
          + rendererPatched + ", bindPage=" + bindPagePatched);
    }
    Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);
    System.out.println("PATCHED=" + output);
  }

  private static byte[] patchRenderer(byte[] original) {
    ClassReader reader = new ClassReader(original);
    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    int[] cardMethods = {0};
    int[] fullMethods = {0};
    ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor,
          String signature, String[] exceptions) {
        if ("renderCardMsg".equals(name) && CARD_DESC.equals(descriptor)) {
          cardMethods[0]++;
          return null;
        }
        if ("renderMsg".equals(name) && FULL_DESC.equals(descriptor)) {
          fullMethods[0]++;
          return super.visitMethod(access, ORIGINAL_RENDER_MSG, descriptor, signature, exceptions);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
      }

      @Override
      public void visitEnd() {
        if (cardMethods[0] != 1 || fullMethods[0] != 1) {
          throw new IllegalStateException("Expected one renderCardMsg and one renderMsg; found card="
              + cardMethods[0] + ", full=" + fullMethods[0]);
        }
        addCustomCardMethod(super.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "renderCardMsg", CARD_DESC, null, null));
        addCustomFullMethod(super.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "renderMsg", FULL_DESC, null, null));
        super.visitEnd();
      }
    };
    reader.accept(visitor, 0);
    return writer.toByteArray();
  }

  private static void addCustomCardMethod(MethodVisitor method) {
    method.visitCode();
    Label fallback = new Label();
    Label missingFlavor = new Label();
    emitCustomDescriptionGuard(method, 0, fallback, missingFlavor);
    method.visitLabel(missingFlavor);
    method.visitInsn(Opcodes.POP);
    method.visitLabel(fallback);
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitMethodInsn(Opcodes.INVOKESTATIC, RENDERER, "renderPlainEnglishMsg",
        CARD_DESC, false);
    method.visitInsn(Opcodes.ARETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
  }

  private static void addCustomFullMethod(MethodVisitor method) {
    method.visitCode();
    Label fallback = new Label();
    Label missingFlavor = new Label();
    emitCustomDescriptionGuard(method, 1, fallback, missingFlavor);
    method.visitLabel(missingFlavor);
    method.visitInsn(Opcodes.POP);
    method.visitLabel(fallback);
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitVarInsn(Opcodes.ALOAD, 1);
    method.visitMethodInsn(Opcodes.INVOKESTATIC, RENDERER, ORIGINAL_RENDER_MSG,
        FULL_DESC, false);
    method.visitInsn(Opcodes.ARETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
  }

  private static void emitCustomDescriptionGuard(MethodVisitor method, int definitionSlot,
      Label fallback, Label missingFlavor) {
    method.visitVarInsn(Opcodes.ALOAD, definitionSlot);
    method.visitLdcInsn(OPT_IN_PARAM);
    method.visitInsn(Opcodes.ICONST_0);
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DEFINITION, "getBool",
        "(Ljava/lang/String;Z)Z", false);
    method.visitJumpInsn(Opcodes.IFEQ, fallback);
    method.visitVarInsn(Opcodes.ALOAD, definitionSlot);
    method.visitMethodInsn(Opcodes.INVOKESTATIC, MESSAGES, "abilityFlavorMsg",
        CARD_DESC, false);
    method.visitInsn(Opcodes.DUP);
    method.visitJumpInsn(Opcodes.IFNULL, missingFlavor);
    method.visitInsn(Opcodes.ARETURN);
  }

  private static byte[] patchBindPage(byte[] original) {
    ClassReader reader = new ClassReader(original);
    ClassWriter writer = new ClassWriter(reader, 0);
    int[] targetMethods = {0};
    int[] disabledKeys = {0};
    ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor,
          String signature, String[] exceptions) {
        MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (!"populateAbilityList".equals(name)) return delegate;
        targetMethods[0]++;
        return new MethodVisitor(Opcodes.ASM9, delegate) {
          @Override
          public void visitLdcInsn(Object value) {
            if (SELECT_DISABLED_KEY.equals(value)) {
              disabledKeys[0]++;
              if (disabledKeys[0] == 1) value = PASSIVE_TAG_KEY;
            }
            super.visitLdcInsn(value);
          }
        };
      }
    };
    reader.accept(visitor, 0);
    if (targetMethods[0] != 1 || disabledKeys[0] != 2) {
      throw new IllegalStateException("Unexpected AbilityBindPage 1.5.2 shape: methods="
          + targetMethods[0] + ", selectDisabledKeys=" + disabledKeys[0]);
    }
    return writer.toByteArray();
  }
}
