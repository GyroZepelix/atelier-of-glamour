# Dyeable Spellbook Dye Tag Recipe Plan

## Purpose

Change the dyeable spellbook acquisition recipe so a Hexcasting regular spellbook plus a dye produces an Atelier dyeable spellbook already colored with that dye, while preserving all iota and spellbook NBT from the input spellbook.

## Context

- Repository: Fabric 1.20.1 Java mod `atelier-of-glamour`, Java 17, Fabric Loom split main/client source sets.
- Relevant build files: `build.gradle`, `gradle.properties`, and `justfile` define `./gradlew build`, `./gradlew runClient`, and `./gradlew runDatagen`.
- Existing dyeable spellbook item: `src/main/java/com/dgjalic/item/DyeableSpellbookItem.java` extends Hexcasting `ItemSpellbook`, implements `DyeableLeatherItem`, returns default color `0xFFFFFF`, and uses one visual variant.
- Existing acquisition recipe: `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java` is a `CustomRecipe` registered as `atelier-of-glamour:dyeable_spellbook`. It currently matches exactly one `HexItems.SPELLBOOK` plus exactly one `Items.LEATHER`, then creates `AtelierItems.DYEABLE_SPELLBOOK` and copies the input spellbook tag.
- Existing recipe JSON: `src/main/resources/data/atelier-of-glamour/recipes/dyeable_spellbook.json` only declares the custom recipe type and category, so ingredient logic lives entirely in Java.
- Existing sealing recipe: `DyeableSealSpellbookRecipe` already copies the dyeable spellbook stack and preserves NBT, including dye color. This task should not change sealing.
- Fabric API convention tags are available on the classpath. Local source confirms `net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags.DYES` exists and maps to the common dye tag.
- Minecraft 1.20.1 mappings expose `DyeableLeatherItem.dyeArmor(ItemStack, List<DyeItem>)`, `DyeableLeatherItem.setColor(ItemStack, int)`, `DyeItem#getDyeColor()`, and `DyeColor#getTextureDiffuseColors()`.
- Repository planning rule: plans are stored in `specs/` and indexed in `specs/INDEX.md`.

## Requirements

- Replace the current regular spellbook plus leather recipe behavior with regular spellbook plus dye behavior.
- Accept dyes through the common dye tag, so vanilla dyes and modded dye items registered in `ConventionalItemTags.DYES` can work.
- The recipe must still require exactly one regular Hexcasting spellbook, not an Atelier dyeable spellbook.
- The recipe must require at least one dye and reject leather, extra spellbooks, and unrelated items.
- The recipe must accept multiple dye items and combine their colors through `DyeableLeatherItem.dyeArmor`.
- The crafted result must be `atelier-of-glamour:dyeable_spellbook` with the color applied as though the resulting dyeable spellbook had been dyed with the supplied dye or dyes.
- The crafted result must preserve the input regular spellbook NBT, including stored iotas, selected page, names, sealed or visual metadata if present, and any other Hexcasting data.
- Keep the existing recipe serializer id and recipe JSON path unless implementation discovers a compatibility reason to rename them.
- Do not change spellbook rendering, item registration, sealing, shift-scroll behavior, textures, or Hexcasting internals.

## Out of Scope

- Adding new dyeable spellbook variants, models, textures, or color layers.
- Changing the dyeable spellbook item id, creative tab placement, or language text.
- Changing the seal recipe or Hexcasting's original spellbook recipes.
- Adding dependencies or data migrations.
- Broad refactors of recipe registration or item registration.

## Assumptions

- "Regular spellbook" means `HexItems.SPELLBOOK`, matching the existing conversion recipe input.
- "Any kind of dye" means any item that is in Fabric's common `ConventionalItemTags.DYES` tag and exposes a usable dye color, normally by being a `DyeItem`. If a mod puts a non-`DyeItem` with no color API into the tag, the implementation should not guess a color and should document that limitation.
- Calling `DyeableLeatherItem.dyeArmor` on the newly created dyeable spellbook output with all supplied `DyeItem`s is the closest vanilla-compatible way to produce the same color as normal leather-style dyeing.
- Copying the input spellbook tag before applying the dye color should preserve iotas and then add or replace only the dye color under the standard display color tag.
- The current recipe JSON can stay unchanged because the custom Java recipe controls matching and assembly.

## Plan

1. Update the acquisition recipe matching logic.
   - Target file: `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java`.
   - Replace the `Items.LEATHER` check with a dye check using `ConventionalItemTags.DYES` and `DyeItem` color support.
   - Track exactly one regular `HexItems.SPELLBOOK` and one or more dye items; return false for duplicate spellbooks, leather, or unrelated inputs.
   - Expected result: the recipe grid accepts `hexcasting:spellbook` plus one or more tagged dyes and rejects the old leather recipe.

2. Apply the dye color during assembly while preserving NBT.
   - Target file: `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java`.
   - In `assemble`, find the input spellbook and collect all dye stacks.
   - Create a new `ItemStack(AtelierItems.DYEABLE_SPELLBOOK)`, copy the input spellbook tag exactly as the current code does, then apply the dye colors.
   - Use `DyeableLeatherItem.dyeArmor(output, dyes)` after resolving each matched dye to a `DyeItem`, because it reuses vanilla leather dye color math and writes the standard display color tag.
   - Ensure the returned stack has count 1 and the copied tag is not mutated from the input stack.
   - Expected result: output is a dyed Atelier spellbook whose Hexcasting data is retained from the regular spellbook.

3. Keep serializer and recipe data stable.
   - Target files: `src/main/java/com/dgjalic/registry/AtelierRecipeSerializers.java` and `src/main/resources/data/atelier-of-glamour/recipes/dyeable_spellbook.json`.
   - Confirm no serializer rename is needed and the JSON custom recipe type still points to `atelier-of-glamour:dyeable_spellbook`.
   - Leave `dynamicseal_dyeable_spellbook.json` and `DyeableSealSpellbookRecipe.java` unchanged.
   - Expected result: existing worlds and datapacks that reference this custom recipe id continue to load, but the behavior changes from leather to dye.

4. Add lightweight automated coverage if practical in this mod layout.
   - Target area: existing or new test sources only if the project has an established Minecraft recipe test pattern after a quick check.
   - If no practical test harness exists, do not create a large custom harness just for this change. Rely on build plus manual in-client checks.
   - Expected result: verification stays proportional to the small recipe behavior change.

5. Update the saved plan during implementation.
   - Target file: this plan in `specs/`.
   - Mark progress items as completed as implementation and verification proceed. Record any API discovery, such as a non-`DyeItem` dye tag limitation, in Decision Log or Notes.
   - Expected result: the plan remains a useful handoff record.

## Verification

- Build check: run `./gradlew build` from the repository root. Expected result: Java compilation and resource processing complete successfully.
- Recipe data check: confirm `src/main/resources/data/atelier-of-glamour/recipes/dyeable_spellbook.json` still loads with type `atelier-of-glamour:dyeable_spellbook` and no missing serializer errors appear during build or client launch.
- Manual client check: run `./gradlew runClient`, place a regular Hexcasting spellbook and a vanilla red dye in the crafting grid, and confirm the output is an Atelier dyeable spellbook with a red cover.
- Tag compatibility check: in the dev client or with a small temporary datapack/modded dye if available, confirm an item in `ConventionalItemTags.DYES` is accepted by the recipe when it is a dye item.
- Old recipe regression check: place a regular Hexcasting spellbook and leather in the crafting grid. Expected result: no dyeable spellbook output.
- NBT retention check: write or store an iota in a regular spellbook, craft it with a dye, then confirm the resulting dyeable spellbook still contains the iota and its selected page data.
- Color behavior check: craft with individual red and blue dyes and with red plus blue together, and confirm each output color matches normal leather dye color behavior.
- Existing behavior regression check: after conversion, confirm the dyeable spellbook can still be shift-scrolled, rendered in empty or filled states, and sealed using the existing dyeable seal recipe.

## Risks and Blockers

- Some modded items may be placed in the common dye tag without extending `DyeItem`. Minecraft does not provide a universal color API for arbitrary tagged items, so supporting those may require color-specific sub-tags or an explicit mapping. Do not silently guess colors.
- `DyeableLeatherItem.dyeArmor` returns a new stack. Implementation must use the returned stack rather than assuming it mutates the output in place.
- Applying dye before copying the spellbook NBT could overwrite the display color or lose dye data. Copy NBT first, then apply color.
- If the copied regular spellbook NBT already has a `display.color` value for unrelated reasons, dyeing will overwrite it. This matches normal dyeing behavior and should be acceptable.
- Approval is required before destructive file operations, dependency changes, migrations, external service changes, commits, pushes, production actions, or broad unrequested refactors.

## Progress

- [x] Planning complete and saved.
- [x] Implementation completed in `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java`.
- [x] Automated verification run: `./gradlew build` passed.
- [ ] Manual in-client recipe, color, and NBT retention checks not run.

## Decision Log

- Decision: Use Fabric's `ConventionalItemTags.DYES` as the recipe input tag.
  Rationale: The user requested a dye tag so modded dyes can work, and Fabric convention tags are already available through the project's Fabric API dependency.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Keep the existing custom recipe id `atelier-of-glamour:dyeable_spellbook`.
  Rationale: The JSON already routes to custom Java logic, so the behavior can change without renaming recipe files or serializers.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Preserve input spellbook NBT before applying the dye color.
  Rationale: The current leather conversion already preserves iotas by copying the tag. Applying the dye after the copy keeps that behavior while writing only the standard dye color metadata.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Resolve dyes to `DyeItem` instances and use `DyeableLeatherItem.dyeArmor` for color application.
  Rationale: Claude CLI review and local bytecode inspection confirmed vanilla `ArmorDyeRecipe` delegates dye math to `DyeableLeatherItem.dyeArmor`. The implementation now uses real `DyeItem`s directly and maps Fabric conventional color subtags to `DyeItem.byColor(color)` for tagged non-`DyeItem` inputs.
  Date/Author: 2026-06-16 / PI implementation agent

- Decision: Allow multiple dye items in the conversion craft.
  Rationale: The user requested vanilla-style multi-dye color blending, and `DyeableLeatherItem.dyeArmor` accepts a list of dye items for exactly this behavior.
  Date/Author: 2026-06-16 / PI implementation agent

## Execution Handoff

Use PI Agent in a fresh session with this prompt:

    Read the saved plan file path reported by the planning agent.
    Implement it step by step. Before editing, re-read the Requirements, Out of Scope, Risks and Blockers, and Verification sections.
    Update the Progress and Decision Log sections as work proceeds.
    Run the Verification commands before reporting done.
    Do not commit, push, run migrations, add dependencies, or perform destructive operations without explicit approval.
    If PI plan mode is active, use a numbered Plan: section and mark completed implementation steps with [DONE:n].

## Notes

- No implementation was performed while creating this plan.
- Implementation uses `net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags`, `net.minecraft.world.item.DyeItem`, `net.minecraft.world.item.DyeColor`, and `net.minecraft.world.item.DyeableLeatherItem`.
- Implementation copies the input spellbook tag, then calls `DyeableLeatherItem.dyeArmor(output, dyes)` so vanilla applies the leather dye color math for one or more dyes.
- Tagged non-`DyeItem` modded dyes are supported only when they are also in one of Fabric's conventional color-specific dye tags, such as `red_dyes` or `blue_dyes`.
