# MMOSkillTree custom-description and passive-card patch

Reproducible compatibility patch for MMOSkillTree 1.5.2.

Exactly two classes are changed:

- `AbilityDescriptionRenderer`: abilities whose `params` contain `"useCustomCardDescription": true` use only `ability.<id>.flavor` in card and tooltip views. Other abilities keep the original generated-description path.
- `AbilityBindPage`: the disabled button on an unlocked passive uses `ability.bind_page.passive_tag` instead of the misleading active-slot text. No selection event is added; passives remain unbindable.

Compile and run with JDK 25 and ASM 9.8:

```bash
ASM="$HOME/.m2/repository/org/ow2/asm/asm/9.8/asm-9.8.jar"
javac -cp "$ASM" CardDescriptionPatcher.java
java -cp ".;$ASM" CardDescriptionPatcher input.jar output.jar
python verify_patch.py input.jar output.jar
```

The verifier requires exactly the two documented class changes and inspects their bytecode contracts. Never patch the active server JAR in place. Build a separate output, verify it, back up the active JAR, then deploy during a stopped-server window.
