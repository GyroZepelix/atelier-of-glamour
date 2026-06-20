# Any Item Transmogrification Command

## Purpose

Add a simple first version of item transmogrification for Fabric 1.20.1: an op-only `/aog transmog` command that lets a player make the item in their main hand visually use another registered item's name and client model/texture, such as making `atelier_of_glamour:dyeable_spellbook` appear as `minecraft:potato`, without changing the held item's gameplay behavior or identity.

## Context

The repository is a Fabric 1.20.1 mod named `atelier_of_glamour` using Java 17, Fabric API, Hex Casting, Patchouli, Inline, Cloth Config, and Cardinal Components. The current mod entrypoint is `src/main/java/com/dgjalic/AtelierOfGlamour.java`, which registers items and recipe serializers. The dyeable spellbook is registered in `src/main/java/com/dgjalic/registry/AtelierItems.java` and implemented in `src/main/java/com/dgjalic/item/DyeableSpellbookItem.java` as a Hex Casting `ItemSpellbook` with vanilla-style dye NBT. Client item color and predicate behavior for the spellbook already lives in `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java`. Existing mixins are small and targeted: `src/main/java/com/dgjalic/mixin/MsgShiftScrollC2SMixin.java` and `src/client/java/com/dgjalic/client/mixin/ShiftScrollListenerMixin.java`, with client mixins listed in `src/client/resources/atelier_of_glamour.client.mixins.json`.

There is no existing command registration. Fabric commands should be registered during mod initialization with Fabric API `CommandRegistrationCallback`. Static item model predicates are not a good fit for arbitrary item-id transmogrification because they select among finite predeclared JSON overrides. For a feature where any registered item can be the appearance target, the simplest effective approach is to store a target item id in the real stack's NBT and use a small client-side ItemRenderer mixin to substitute a default `ItemStack` of that target item when resolving/rendering the item appearance.

Project repository rules require plans/specs under `specs/` and `specs/INDEX.md` maintenance. No wiki index files were present.

External references consulted:

- Fabric commands documentation: https://docs.fabricmc.net/develop/commands/basics
- Fabric Wiki model predicate providers before 1.21.4: https://wiki.fabricmc.net/tutorial:model_predicate_providers
- Minecraft 1.20.1 ItemRenderer mapping reference: https://lexxie.dev/forge/1.20.1/net/minecraft/client/renderer/entity/ItemRenderer.html

## Requirements

- Add an op-only server command rooted at `/aog transmog`.
- The first version must support applying any registered item id as the visual target for the item stack in the executing player's main hand.
- Provide a way to clear the transmog from the held stack.
- Applying transmog must change the visible item name to the target item's default display name.
- Applying transmog must change the client-rendered model/texture to the target item's default item model where vanilla item rendering is used.
- The original stack must keep its actual item id, behavior, durability, enchantments, NBT, spellbook data, dye data, recipes, tags, stack size, and server-side semantics.
- The target item id must be stored per stack in NBT so the feature works in singleplayer, multiplayer, saves, item transfers, inventory, hand rendering, and dropped-item rendering.
- Invalid target item ids, empty hand usage, and non-player command sources must fail with clear command feedback.
- The implementation must target Minecraft 1.20.1 with Fabric and Mojang mappings as configured in this repository.

## Out of Scope

- No survival UI, crafting mechanic, anvil UI, smithing UI, spellbook UI, recipe, or non-command application flow in the first version.
- No permission system beyond vanilla operator permission level for the command.
- No new dependencies unless implementation proves the vanilla/Fabric API approach cannot compile.
- No support for storing the target stack's full NBT in the first version, unless needed to fix the accepted potato/spellbook use case.
- No attempt to make every dynamic target visual exact, such as potion colors, filled maps, bundles, clocks, compasses, tridents-in-hand special cases, or modded block-entity item renderers.
- No broad refactors of existing item, recipe, Hex Casting, Patchouli, or dye logic.
- No migrations, destructive file operations, commits, pushes, production actions, or external service changes.

## Assumptions

- Command syntax should be `/aog transmog set <item_id>` and `/aog transmog clear`, with an optional convenience alias `/aog transmog <item_id>` only if it stays simple and unambiguous in Brigadier.
- Operator-only means `.requires(source -> source.hasPermission(2))`, matching common Minecraft 1.20.1 command practice.
- The default target appearance should use a new `ItemStack(targetItem)` rather than copying target NBT, which is sufficient for the confirmed `minecraft:potato` to `dyeable_spellbook` case and avoids changing real stack behavior.
- Name restoration should preserve a prior custom name if the stack already had one before transmog. If the stack did not have a custom name, clearing transmog should remove only the transmog-applied custom name and return to the real item's default name.
- Plain NBT is better than Cardinal Components for this first version because the data belongs to one `ItemStack`, must travel with the stack, and does not need component lifecycle hooks.
- The implementer should confirm exact mapped signatures in the generated IDE sources before writing the ItemRenderer mixin. Public 1.20.1 references show `ItemRenderer#getModel(ItemStack, Level, LivingEntity, int)` returning `BakedModel`.

## Plan

1. Add a small transmog NBT helper.
   - Target file: `src/main/java/com/dgjalic/transmog/TransmogData.java`.
   - Define stable NBT keys under a mod-prefixed compound, for example `AtelierTransmog`, `TargetItem`, `AppliedName`, and `OriginalCustomName`.
   - Implement methods to check for transmog, read the target `ResourceLocation`, validate it against `BuiltInRegistries.ITEM`, create a default target `ItemStack`, apply transmog to a stack, and clear transmog from a stack.
   - Keep this helper free of client-only classes so commands and client mixins can both use it.
   - Expected result: one shared source of truth for transmog storage and name restoration.

2. Implement `/aog transmog` command registration.
   - Target files: `src/main/java/com/dgjalic/command/AtelierCommands.java` and `src/main/java/com/dgjalic/AtelierOfGlamour.java`.
   - Register commands from `AtelierOfGlamour.onInitialize()` after existing registry calls.
   - Use Fabric API `net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback` and Brigadier.
   - Use a simple string argument for `<item_id>` and parse it as `ResourceLocation` to minimize version-specific command argument complexity.
   - Command tree:
     - `/aog transmog set <item_id>` applies the target to the executing player's main-hand item.
     - `/aog transmog clear` clears transmog from the executing player's main-hand item.
     - Optional only if trivial: `/aog transmog <item_id>` delegates to `set`.
   - Gate the root or `transmog` node with `.requires(source -> source.hasPermission(2))`.
   - Fail if the source is not a player, the hand is empty, the id is malformed, or the item id is not present in `BuiltInRegistries.ITEM`.
   - On apply, store the target id and set the stack hover name to the target item's default display name, while preserving any pre-existing custom name for clear.
   - On clear, remove transmog NBT and restore the prior custom name if one was saved, otherwise remove the transmog-applied custom name.
   - Expected result: operators can apply and clear appearance data using server-authoritative commands.

3. Add the client rendering hook for arbitrary target models.
   - Target files: `src/client/java/com/dgjalic/client/mixin/ItemRendererTransmogMixin.java` and `src/client/resources/atelier_of_glamour.client.mixins.json`.
   - Prefer the smallest hook that works across inventory, hands, and dropped items. Start by injecting into `net.minecraft.client.renderer.entity.ItemRenderer#getModel(ItemStack, Level, LivingEntity, int)` at `HEAD`, cancellable.
   - If the incoming stack has valid transmog data, create the default target appearance stack through `TransmogData` and return the target stack's model by calling the renderer/model shaper for that target stack.
   - Avoid recursion by ensuring the target appearance stack has no transmog NBT.
   - If `getModel` substitution compiles but leaves important tint/color visuals wrong, adjust the mixin to substitute the stack earlier in the render path for vanilla `ItemRenderer#renderStatic(...)` or `ItemRenderer#render(...)`, but keep the scope limited to vanilla item rendering.
   - Expected result: the real stack visually uses the target item's default baked model and texture without changing server-side item behavior.

4. Preserve existing dyeable spellbook and Hex Casting behavior.
   - Target files to inspect during implementation: `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java`, `src/main/java/com/dgjalic/item/DyeableSpellbookItem.java`, and existing mixins.
   - Do not alter dye recipes, scroll behavior, Hex Casting spellbook storage, or Patchouli data.
   - Confirm that transmog rendering bypasses the spellbook's dyeable model only while transmog is set, and returns to current spellbook rendering after clear.
   - Expected result: transmog is an overlaying appearance feature, not a rewrite of the spellbook.

5. Add focused tests or compile-time verification where practical.
   - If the repository has no automated game tests, add no large test harness.
   - Keep verification to `./gradlew build` and manual in-game acceptance checks.
   - If small pure Java unit coverage is not already configured, do not introduce a test framework just for this feature.
   - Expected result: build passes and behavior is validated manually in a dev client/server.

6. Update user-facing or developer notes only if a nearby documentation pattern exists.
   - Optional target: `README.md` or Patchouli docs only if the implementer and user agree after the first version works.
   - Default for this plan: no documentation changes beyond updating this plan's Progress and Decision Log during implementation.
   - Expected result: first version remains minimal and implementation-focused.

## Verification

- Run `./gradlew build`.
  - Expected: Gradle completes successfully with Java 17 and Fabric 1.20.1 mappings.
- Run a client from the development environment, join a world with cheats/op permissions, hold `atelier_of_glamour:dyeable_spellbook`, then execute `/aog transmog set minecraft:potato`.
  - Expected: command succeeds, held stack remains a dyeable spellbook behaviorally, visible name becomes Potato, and the item appears as the potato model/texture in inventory and hand.
- Drop the transmogrified spellbook in-world.
  - Expected: dropped item uses the potato appearance while remaining pickupable as the original spellbook stack.
- Execute `/aog transmog clear` while holding the item.
  - Expected: transmog NBT is removed, name restores to the prior custom name if one existed or the dyeable spellbook default if not, and visual rendering returns to the existing dyeable spellbook model.
- Execute `/aog transmog set not_a_valid_id` and `/aog transmog set minecraft:not_real`.
  - Expected: command fails with readable feedback and does not mutate the held stack.
- Execute `/aog transmog set minecraft:potato` as a non-op player.
  - Expected: command is unavailable or rejected due to permission level.
- Confirm no regression to existing dyeable spellbook behavior after clear.
  - Expected: dye color, filled/sealed overlay, scroll behavior, and existing spellbook data still work as before.

## Risks and Blockers

- ItemRenderer mapped method signatures can differ from public examples. Mitigation: confirm exact signatures in IDE/generated sources before writing the mixin, and keep the mixin in the client source set and client mixin config only.
- Substituting only `getModel` may not perfectly reproduce all target item-specific tint providers or dynamic NBT visuals. Mitigation: first acceptance target is `minecraft:potato`, and if basic model substitution is insufficient, move the hook earlier in the render path to render with a default target stack.
- Some items have special built-in renderers or context-sensitive models. Mitigation: document first-version behavior as default target item appearance rather than exact cloned target stack state.
- Changing hover names must not destroy pre-existing custom names. Mitigation: store original custom-name JSON/NBT before setting the target name and restore it on clear.
- Dependency changes are not expected. If a new dependency becomes necessary, stop and ask for approval before editing Gradle files.
- Destructive operations, migrations, external service changes, commits, pushes, production actions, broad refactors, and scope expansion require explicit approval during later implementation.

## Progress

- Planning complete and saved.
- Implementation complete.
- Claude CLI read-only review completed; blocking command parser issue and rendering-hook limitations were addressed.
- Verification run: `./gradlew build` passed after review fixes.
- Manual in-game acceptance checks not run in this session.

## Decision Log

- Decision: Use a command-first MVP rooted at `/aog transmog`.
  Rationale: The user requested the simplest beginning and confirmed an op-only command is acceptable.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Store the target item id directly on the `ItemStack` with plain NBT.
  Rationale: The data is per-stack, must persist and travel with the item, and does not need the complexity of Cardinal Components.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Do not use static JSON model predicates for arbitrary transmog.
  Rationale: Fabric/Minecraft 1.20.1 model predicates select among finite JSON overrides and cannot dynamically choose any registry item's model from a string NBT id.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Avoid new dependencies for the first version.
  Rationale: Fabric API, vanilla NBT, Brigadier, and a small client mixin are enough for the confirmed potato-to-spellbook use case.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Do not keep `/aog transmog <item_id>` as a convenience alias.
  Rationale: Claude CLI review flagged Brigadier ambiguity with sibling `set` and `clear` literals. The implemented command shape is the unambiguous `/aog transmog set <item_id>` plus `/aog transmog clear`.
  Date/Author: 2026-06-20, PI implementation agent

- Decision: Use `ResourceLocationArgument.id()` with item registry suggestions instead of a string argument.
  Rationale: Claude CLI review found that `StringArgumentType.word()` does not parse namespaced ids like `minecraft:potato`, which broke the primary acceptance case.
  Date/Author: 2026-06-20, PI implementation agent

- Decision: Substitute a default target `ItemStack` at `ItemRenderer#renderStatic` instead of only replacing the baked model in `ItemRenderer#getModel`.
  Rationale: Claude CLI review found model-only substitution would use the original stack for tint providers and built-in renderers. Rendering the target stack better matches arbitrary item model/texture behavior while preserving the original stack server-side.
  Date/Author: 2026-06-20, PI implementation agent

## Execution Handoff

Use PI Agent in a fresh session with this prompt:

    Read the saved plan file path reported by the planning agent.
    Implement it step by step. Before editing, re-read the Requirements, Out of Scope, Risks and Blockers, and Verification sections.
    Update the Progress and Decision Log sections as work proceeds.
    Run the Verification commands before reporting done.
    Do not commit, push, run migrations, add dependencies, or perform destructive operations without explicit approval.
    If PI plan mode is active, use a numbered Plan: section and mark completed implementation steps with [DONE:n].

Suggested implementation prompt:

    Implement the item transmogrification command from this saved plan. Use `/aog transmog set <item_id>` and `/aog transmog clear`. Keep the feature minimal, do not add dependencies, and verify with `./gradlew build` plus the manual potato-to-dyeable-spellbook checks.

## Notes

- Recommended first manual test target: `/aog transmog set minecraft:potato` while holding `atelier_of_glamour:dyeable_spellbook`.
- If exact target tinting becomes important later, consider storing an optional full target `ItemStack` NBT snapshot, but do not include that in this MVP unless the basic implementation cannot satisfy the accepted use case.
- Optional PI subagent use during implementation: a read-only Explore subagent can verify ItemRenderer method signatures and call sites if the implementer cannot quickly locate generated Minecraft sources.
