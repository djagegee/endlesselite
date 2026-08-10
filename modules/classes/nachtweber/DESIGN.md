# Nachtweber – Core-Design 0.1.0

**Klassen-ID:** `elite_nightweaver` · **Titel:** Jäger des Schwarzen Netzes

Dieser Slice besitzt exakt drei aktive Verträge (`BLACK_THREAD`, `SHADOW_SWING`, `HUNTING_COCOON`) und vier passive Verträge (`DANGER_SENSE`, `WALL_HUNTER`, `TOXIC_GLANDS`, `HUNTING_INSTINCT`).

`nightweaver_entanglement` und `nightweaver_venom` werden serverseitig pro `(target, owner)` geführt. Stapel sind begrenzt, Dauer wird beim erneuten Anwenden erneuert, Reinigung ist owner-spezifisch. Fanggift holt bei Verzögerung höchstens einen Tick nach und plant danach relativ zur aktuellen Zeit; DoT-/Reflexionsschaden darf Fanggift nicht erneut auslösen. Boss-, Elite- und PvP-Faktoren reduzieren Kontrolle linear.

## Fail-closed Runtime-Adapter

Der nachgewiesene Hytale-Lifecycle ist über `NachtweberPlugin(JavaPluginInit)`, `setup()` und `shutdown()` angebunden. Der Adapter registriert ausschließlich `nachtweber.use` sowie die EndlessLeveling-Klasse `elite_nightweaver`; eine abgelehnte Klassenregistrierung wird nicht als eigener Zustand behandelt und beim Shutdown daher nicht irrtümlich entfernt. Erfolgreich registrierter Zustand wird genau einmal abgemeldet.

Das Manifest besitzt den geprüften `Main`-Entrypoint und bleibt `DisabledByDefault: true`. Registriert sind ausschließlich der Klassen-/Permission-Lifecycle sowie die pro Store gekapselte Maintenance-Infrastruktur. MMOSkillTree-Effects, Damage-, Grappling-, Kletter- und positive Gameplaypfade werden weiter nicht registriert. Deployment bleibt gesperrt.

## MMOSkillTree-Stagingvertrag

`NACHTWEBER_MASTERY` definiert sieben originale Unlock-IDs auf den Stufen 1/2/25/40/55/70/90. `NachtweberMmoInstaller` kann Ability- und DE-/EN-Ownerdateien additiv, atomar und mit genau einem nicht überschriebenen Vorher-Backup erzeugen. Fremde Einträge bleiben erhalten; Wiederholung ist idempotent.

Der Live-Plugin-Lifecycle ruft diesen Installer bewusst noch nicht auf: aktive Effekte, SkillRegistry/SkillTreeConfig, Icons und Gameplay-Hooks sind noch nicht vollständig registriert. Damit gelangen keine unbekannten Effekte in die produktive MMOSkillTree-Konfiguration.

## BLACK_THREAD Gameplay-Slice

`BlackThreadService` akzeptiert ausschließlich serververifizierte Casts, prüft Ziel, endliche Distanz, maximale Reichweite und einen owner-spezifischen Cooldown, bevor `nightweaver_entanglement` verändert wird. Erfolgreiche Treffer addieren zwei owner-isolierte Stapel; ab drei Stapeln entsteht ein begrenzter Kontrollimpuls. Abgelehnte Casts verändern weder Ledger noch Cooldown.

`BlackThreadAbility` implementiert den nachgewiesenen MMOSkillTree-`AbilityEffect`-Vertrag und wählt nur sichtbare, feindliche Kampf-NPCs serverseitig anhand Store, Blickrichtung und Block-Raystep. Service-NPCs und PvP-Spieler sind ausgeschlossen. Solange keine belegte Boss-/Elite-Klassifizierungs-API vorliegt, nutzt der Runtime-Adapter konservativ den Boss-Faktor `0.35` für alle ausgewählten NPCs; dadurch kann Kontrolle zu kurz, aber nie zu stark ausfallen.

Der Effect-Adapter und seine Registry sind gebaut, aber noch nicht in `NachtweberPlugin` registriert: `ActiveAbilityService` besitzt in der geprüften API keinen Unregister-Vertrag. Das verhindert einen nicht rückrollbaren partiellen Reload. Live-Aktivierung wartet auf einen sicheren Ownership-/Boot-Slice.

## HUNTING_COCOON Gameplay-Slice

`HuntingCocoonService` akzeptiert ausschließlich serververifizierte, endliche und in Reichweite liegende Casts. Ein Erfolg benötigt mindestens drei aktive `nightweaver_entanglement`-Stapel desselben `(target, owner)`. Erst nach Autoritäts-, Ziel-, Reichweiten-, Cooldown-, Schadensursachen- und Immunitätsprüfung werden ausschließlich diese eigenen Stapel vollständig konsumiert und zwei owner-isolierte `nightweaver_venom`-Stapel erzeugt. Fremde Owner-Zustände bleiben erhalten; Ablehnungen verändern weder Verstrickung, Fanggift noch Cooldown.

`VenomDamageCause` verhindert DoT-/Reflexionsrekursion. Der Immunitätsvertrag ist über `VenomImmunityResolver` injizierbar; der Runtime-Effect nutzt serverseitig denselben sichtbaren Hostile-NPC-Targeter wie `BLACK_THREAD`. Ablauf-, Tick- und Cooldownaddition saturieren bei `Long.MAX_VALUE`, statt negative Zeitwerte zu erzeugen. Operatorwerte stehen in Schema 4 als Blöcke, Sekunden und Stapelanzahlen.

`HuntingCocoonAbility`, Effect-Contract und die erweiterte Registry sind gebaut und testgesichert, bleiben aber aus demselben fehlenden MMOSkillTree-Unregister-/Ownership-Grund wie `BLACK_THREAD` außerhalb des Plugin-Lifecycles. Es erfolgte keine Live-Konfigurationsmutation und kein Deployment.

## VENOM_TICK Damage-Slice

`VenomTickService` drainiert pro `(target, owner)` höchstens einen fälligen Tick pro Update. Überfällige Intervalle werden nicht als Burst nachgespielt; nach einem Runtime-Fehler bleibt der Tick verbraucht und kann nicht erneut abgespielt werden. Schaden wird aus eigenen aktiven Giftstapeln und der angezeigten EndlessLeveling-`SORCERY` berechnet, auf einen endlichen Maximalwert begrenzt und ausschließlich als `VENOM_TICK` an den Damage-Port übergeben.

Der lokale aktive Vertrag ist `EndlessLevelingCore 11.6.1`. Binär geprüft sind `getDisplayedAttributeTotal(UUID, SORCERY, fallback)`, `createAbilityDotDamage(Ref, float, String)` und `DamageSystems.executeDamage(...)`. Der EndlessLeveling-Factorypfad setzt laut geprüftem Bytecode `AUGMENT_DOT_DAMAGE` und `ABILITY_ORIGIN_PROC`; `shouldBypassOutgoingAugmentMath(...)` erkennt diesen DoT. `EndlessLevelingVenomDamageAdapter` bindet den Schaden an den Owner-`EntitySource`, validiert Owner, Ziel, Ref/Store und Cause und fällt bei fehlendem Runtime-/Assetvertrag geschlossen aus.

Der positive Factory-/Damage-Aufruf benötigt den gestarteten Hytale-`DamageCause`-AssetStore und ist in isolierten Maven-Tests daher bewusst nicht simuliert. Tick-Service, ABI und Vor-Boot-Fail-closed-Verhalten sind testgesichert. Schema 9 registriert ein storegenaues `VenomDamageSystem`; ohne reale Owner-/Target-Entities bleibt es inert, sodass der isolierte Boot Linkage, aber keine positive Schadensausführung beweist.

## Storegebundene Runtime-Maintenance

`NachtweberLedgerStoreRuntime` kapselt je `Store<EntityStore>` eigene Verstrickungs- und Giftledgers. Storepartitionierung basiert auf Objektidentität. Bindung erfolgt erst auf dem binär bestätigten `StartWorldEvent`, weil ein realer isolierter RED-Boot belegte, dass `AddWorldEvent` vor Initialisierung von `EntityStore.getStore()` auftreten kann. Ein fehlender Store wird fail-closed verworfen.

Das pluginlokale `TickingSystem<EntityStore>` entfernt ausschließlich abgelaufene Ledgerzustände. Es konsumiert keinen Gift-Damage-Tick und ruft keine Damage-, Proc-, Ability- oder MMOSkillTree-Registrierung auf. Der erste reale Tick eines gebundenen Stores wird genau einmal gemeldet. World-Remove und Shutdown entfernen den Zustand vor `close()`, sodass jeder Store höchstens einmal geschlossen wird; nach Coordinator-Shutdown sind neue Bindungen gesperrt.

Die storegebundene Gameplay-Fassade delegiert Black Thread, Hunting Cocoon, passive Giftanwendung, Venom-Tick sowie Shadow-Swing-Start/Step/Cancel an storelokale Services. Black Thread, Cocoon und Gift teilen innerhalb desselben Stores exakt diese beiden Ledgers; der Shadow-Swing-Reconciliation-Controller besitzt getrennten owner-isolierten Sessionzustand. `cleanupOwner(UUID)` entfernt innerhalb genau dieses Stores ownerselektiv beide Ledgerzustände, alle zugehörigen Ability-/Passive-Cooldowns sowie Swing-Session und Reattach-Zeit; andere Owner und Stores bleiben unverändert. Die Fassade gibt keine rohen Ledger-, Service- oder Controllerverweise heraus; jede Operation liefert nach World-Remove/Shutdown `Optional.empty()` beziehungsweise `false`. Der Coordinator-Dispatch akzeptiert ausschließlich den identischen gebundenen Store. Der EndlessLeveling-Sorcery-Provider ist injiziert, wird aber erst bei einem expliziten Venom-Tick-Aufruf gelesen. Weder Maintenance noch Binding lösen Damage, Movement oder Effects aus.

Owner-Cleanup ist an Hytales globales `PlayerDisconnectEvent` gebunden. Der lokale Serverbytecode belegt, dass `Universe.removePlayer` dieses Event vor dem Lesen beziehungsweise Entfernen der PlayerRef-Store-Referenz dispatcht. Der Adapter liest nur `PlayerRef.getUuid()` und `PlayerRef.getReference().getStore()` und fällt bei unvollständigem Kontext geschlossen aus; der Coordinator akzeptiert nur den bereits gebundenen identischen Store. Die Registrierung gehört der pluginlokalen EventRegistry.

Klassenwechsel-Cleanup bleibt separat offen. EndlessLeveling 11.6.1 exponiert den direkten Callback ausschließlich als `AbilityBridge.Hooks.onClassChanged(UUID)` über einen globalen Single-Hook. `AbilityBridge.register(Hooks)` besitzt keinen ownership-sicheren `unregister(Hooks)`-Vertrag. Die symmetrisch abmeldbaren `ProfileSwitchedEvent`- und `BuildAppliedEvent`-Listener sind semantisch keine belegten Klassenwechsel-Events und liefern keinen Store. Nachtweber registriert daher keinen dieser Hooks automatisch.

Beim ersten nicht-null `StartWorldEvent` darf eine read-only `DamageCauseReadinessProbe` einmalig die gestartete Hytale-Assetkette prüfen. Sie verwendet den durch `Assets.zip` belegten Key `Physical` und verifiziert AssetStore, identische IndexedLookupMap, Index und Asset-ID konsistent. `UNAVAILABLE`, inkonsistente Maps, Runtime-/Linkagefehler und null Stores bleiben fail-closed. Die Probe erstellt kein `Damage`-Objekt und ruft niemals `DamageSystems.executeDamage` auf; ihr PASS ist nur eine Voraussetzung, kein positiver Damage-Nachweis.

## Integrierter Abschluss-Slice

`TOXIC_GLANDS` verarbeitet ausschließlich einen explizit als serververifizierten direkten Treffer. Eigene DoT-/Proc-/Reflection-Pfade werden vor Mutation abgewiesen. Die Passive erzeugt einen owner-isolierten Fanggiftstapel für sechs Sekunden und besitzt einen owner+target-spezifischen internen Cooldown von einer Sekunde. `HUNTING_INSTINCT` ergänzt genau einen Stapel, aber nur wenn dasselbe Ziel zu diesem Zeitpunkt aktive Verstrickung desselben Owners trägt. Fremde Nachtweber-Zustände zählen nicht. Cooldowns sind owner-spezifisch bereinigbar.

`SHADOW_SWING` besitzt einen storelokalen Reconciliation-Core mit owner-isolierten Sessions, Dauer-/Anchor-/Tether-/Releasegrenzen, vektoriell begrenzter Korrekturempfehlung, Reattach-Limit, stale-Step-Schutz und fail-closed Cleanup. Der live registrierte `StoreBoundShadowSwingAbility` prüft Store, Thread, Ref, Transform, HeadRotation und Blockanker und startet ausschließlich diese Core-Session. `ShadowSwingMovementSystem` queryt PlayerRef+Transform+Velocity, verwirft fremde/ungültige Kontexte und übergibt nur akzeptierte Core-Steps als begrenzte `Velocity.addInstruction(..., ChangeVelocityType.Add)`. Es besitzt `Order.BEFORE` zu `PlayerVelocityInstructionSystem`; direkte Velocity-Mutation durch den Effect ist ausgeschlossen.

Der read-only Hytale-0.5.7-/MMOSkillTree-1.5.2-ABI-Preflight bestätigte `Velocity.addInstruction(vector, config, ChangeVelocityType.Add)` als Player-Paketpfad. `PlayerVelocityInstructionSystem` emittiert `ChangeVelocity(Add)` und leert danach die Instruktionsliste; MMOSkillTrees eigener `VelocityImpulseUtil` verwendet ebenfalls `addInstruction(...)`. Nachtwebers Producer läuft deshalb explizit vor diesem Consumer. Effect und ECS-Producer erzwingen Store-/Thread-/Ref-Gates. Offen bleibt nur die reale Client-/Ingame-Trajektorie, nicht mehr die Effect→Core→ECS-Kopplung.

`WALL_HUNTER` liest den nativen `CollisionResultComponent`, verwirft ausstehende Collision-Checks und akzeptiert ausschließlich fertige berührende/überlappende Blockkollisionen mit horizontaler Kollisionsnormalen. Die clientgespeiste Sprungbewegung gilt nur als advisory Aufwärtseingabe; ohne serverseitig bestätigten Wandkontakt entsteht keine Bewegung. `DANGER_SENSE` führt Autoritäts-, Unlock- und Cooldown-Preflight vor der Spatial Query aus, validiert einen harten Radius von maximal 16 Blöcken und behält höchstens 64 Kandidaten. `Selector.selectNearbyEntities` selbst besitzt keine Early-Termination; deshalb bleibt auch der Spatial-Scan radius- und cadence-gebunden. Der Adapter filtert denselben Store, feindliche Kampf-NPCs und Sichtlinie und meldet das nächste sichtbare Ziel über einen injizierbaren Alert-Port.

Die gebündelte Config verwendet Schema 9 und enthält für alle drei aktiven sowie alle vier passiven Fähigkeiten konkrete Blöcke, Sekunden, Geschwindigkeiten, Stapel und Kandidatenlimits. Schema 9 ergänzt 2,0 Sekunden Cocoon-Immobilisierung sowie den MMOSkillTree-Owned-Registryvertrag (`registerIfAbsent`, exakt-instance `unregister`). Live-Konfigurationen werden nicht automatisch migriert. Alle sieben MMOSkillTree-Definitionen besitzen konkrete Parameter; die Runtimefactory liefert exakt drei storegebundene aktive Effects.

Domänenlogik, Konfiguration, Stagingdefinitionen, fail-closed Adapter, Store-Maintenance, drei storegebundene aktive Effects, der Reconciliation-Core, Shadow-Swing-ECS-Instruction-Wiring und Venom-Damage-System sind quell-, paketierungs- und isoliert runtime-seitig belegt. Der reproduzierbare MMOSkillTree-Patch ergänzt `registerIfAbsent` und `unregister(discriminator, expectedEffect)`; Nachtweber registriert und entfernt ausschließlich seine drei Instanzen. `DisabledByDefault: true` bleibt bestehen. Positive Damage-/Movementausführung, echter Disconnect, Zwei-Spieler-Isolation, Client/Ingame und Deployment benötigen weiterhin reale Entities und gesonderte Abnahme.
