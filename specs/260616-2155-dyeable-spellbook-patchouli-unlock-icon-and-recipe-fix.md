# Dyeable Spellbook Patchouli Unlock, Icon, and Recipe Fix Plan

## Purpose

Fix the Dyeable Spellbook Hexcasting guide entry so it unlocks with the regular Spellbook entry, has a simple cycling-color icon, and displays its crafting ingredients correctly in Patchouli.

## Context

- Repository: Fabric 1.20.1 Java mod `atelier-of-glamour`, Java 17, Fabric Loom split main/client source sets.
- Build and run commands are available through `justfile`: `just build`, `just runclient`, `just datagen`, plus direct Gradle commands such as `./gradlew build` and `./gradlew runClient`.
- The current Dyeable Spellbook Patchouli entry is `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
- The current entry uses category `hexcasting:items`, icon `atelier-of-glamour:dyeable_spellbook`, `sortnum: 25`, a text page, and a `patchouli:crafting` page for `atelier-of-glamour:dyeable_spellbook`.
- Local Hexcasting book reference files exist under `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/...`.
- The regular Hexcasting Spellbook entry is `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/spellbook.json`; it uses `category: hexcasting:items`, `icon: hexcasting:spellbook`, `sortnum: 3`, and `advancement: hexcasting:root`.
- The user wants the Dyeable Spellbook entry to remain its own entry, not merged into the regular Spellbook entry.
- Patchouli source from the local Gradle cache shows entry lock status is controlled by the entry `advancement` field in `vazkii.patchouli.client.book.BookEntry`.
- Patchouli source also shows entry icons are parsed by `BookIcon.from(String)` into either one texture icon or one item stack icon. Static JSON does not support an array of cycling entry icons.
- Patchouli item icon strings can include NBT because `ItemStackUtil.loadStackFromString` parses strings like `mod:item{...}`.
- The current client item color provider is in `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java`. It tints layer 0 with `AtelierItems.DYEABLE_SPELLBOOK.getColor(stack)` and already uses `ClientTickCounter` for animated fallback iota overlay color.
- The Dyeable Spellbook item is `src/main/java/com/dgjalic/item/DyeableSpellbookItem.java`, where `getColor` reads standard leather-style `display.color` and defaults to white.
- The current custom recipe class is `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java`. It matches a regular Hexcasting spellbook plus one or more dyes, applies vanilla leather dye color math, preserves spellbook NBT, and returns `AtelierItems.DYEABLE_SPELLBOOK`.
- The current recipe JSON is `src/main/resources/data/atelier-of-glamour/recipes/dyeable_spellbook.json` and only names the custom recipe type and category. Ingredient logic lives in Java.
- Patchouli `PageCrafting` renders crafting ingredients by calling `recipe.getIngredients()`. A `CustomRecipe` with no ingredient override can render no input ingredients even though crafting works.
- User choices during planning: keep an own entry, use a simple cycling icon, and keep `patchouli:crafting` by fixing the custom recipe ingredients.
- Repository rule: plans are stored in `specs/` and `specs/INDEX.md` must be maintained.

## Requirements

- Keep the Dyeable Spellbook as its own Hexcasting guide entry.
- Unlock the Dyeable Spellbook guide entry at the same time as the regular Hexcasting Spellbook entry.
- Match the regular Spellbook entry unlock by adding `advancement: "hexcasting:root"` to the Dyeable Spellbook entry.
- Move the Dyeable Spellbook entry near the regular Spellbook entry by changing `sortnum` from `25` to a nearby value, preferably `4` unless manual testing suggests another order.
- Make the Dyeable Spellbook entry icon cycle through colors with a small, contained implementation.
- Do not implement hover-only icon animation, because the user selected the simpler cycling behavior.
- Keep the Patchouli crafting page and make it show the required inputs.
- Do not change the actual crafting behavior: a regular `hexcasting:spellbook` plus one or more dyes should still craft a dyeable spellbook and preserve spellbook NBT.
- Keep the dyeable spellbook hidden from creative tabs and creative search as previously implemented.
- Keep changes minimal and local to the Patchouli entry, client color provider, and custom recipe ingredient metadata.

## Out of Scope

- Merging Dyeable Spellbook content into Hexcasting's regular Spellbook entry.
- Hover-only or focus-only Patchouli icon animation that requires Patchouli GUI mixins.
- Adding dependencies, datagen infrastructure, new recipe serializers, migrations, or a separate Patchouli book.
- Changing the item id, display name, textures, models, recipe matching semantics, NBT preservation, sealing behavior, shift-scroll behavior, or Hexcasting internals.
- Hiding or changing recipe visibility in JEI, EMI, REI, vanilla recipe discovery, commands, registries, or datapacks.
- Reworking unrelated Patchouli entries or localizing the entry into additional languages.

## Assumptions

- `hexcasting:root` is the correct unlock gate for matching the regular Spellbook entry because the local reference Spellbook entry uses exactly that advancement.
- `sortnum: 4` should place the Dyeable Spellbook near the regular Spellbook entry with `sortnum: 3`; if another Hexcasting entry already uses 4, Patchouli will still sort deterministically by translated name for equal sort groups.
- Static Patchouli JSON cannot cycle entry icons by itself in Patchouli 1.20.1-80-FABRIC; cycling requires a code path, not only JSON.
- A special private NBT flag on the Patchouli icon stack is the smallest viable way to animate only this documentation icon without changing normal dyed spellbook color behavior.
- A color-provider branch that checks that private NBT flag on tint layer 0 will affect any dyeable spellbook stack carrying that flag, but only the Patchouli icon should normally carry it.
- Overriding `DyeableSpellbookRecipe.getIngredients()` to return regular spellbook plus the conventional dye tag is enough for Patchouli's `patchouli:crafting` page to render inputs, because Patchouli calls `recipe.getIngredients()` directly.
- If `Ingredient.of(ConventionalItemTags.DYES)` is not accepted by the exact mappings, implementation can use the equivalent supported `Ingredient` factory for tag ingredients after checking compiler feedback.

## Plan

1. Update the Patchouli entry unlock and order.
   - Target file: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
   - Add `"advancement": "hexcasting:root"` to match `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/spellbook.json`.
   - Change `"sortnum": 25` to `"sortnum": 4` so the entry appears adjacent to the regular Spellbook entry, which uses `sortnum: 3`.
   - Expected result: the Dyeable Spellbook entry is locked and unlocked at the same progression point as the regular Spellbook entry and appears near it in the Items category.

2. Mark the Patchouli icon stack as a cycling documentation icon.
   - Target file: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
   - Change the icon from `atelier-of-glamour:dyeable_spellbook` to an NBT-bearing item stack string, for example `atelier-of-glamour:dyeable_spellbook{AtelierPatchouliColorCycle:1b}`.
   - Keep the NBT key private to Atelier and use it only for this documentation presentation.
   - Expected result: Patchouli still renders the Dyeable Spellbook item icon, and client code can detect that this stack should animate.

3. Add simple cycling color support to the dyeable spellbook item color provider.
   - Target file: `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java`.
   - Add a private static final NBT key constant, such as `PATCHOULI_COLOR_CYCLE_TAG = "AtelierPatchouliColorCycle"`.
   - Replace the layer 0 color branch with a small helper, for example `getDyeableSpellbookColor(ItemStack stack)`.
   - In the helper, if the stack tag has the private cycle flag, return a time-based HSV color using `ClientTickCounter.getTotal()` and `Mth.hsvToRgb`, similar to the existing animated fallback in `getIotaColor`.
   - Otherwise return `AtelierItems.DYEABLE_SPELLBOOK.getColor(stack)` exactly as before.
   - Expected result: normal dyeable spellbook stacks keep their real dye color, while the special Patchouli icon stack cycles colors while rendered.

4. Expose custom recipe ingredients for Patchouli rendering.
   - Target file: `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java`.
   - Override `getIngredients()`.
   - Return a `NonNullList<Ingredient>` containing two ingredients: one regular `HexItems.SPELLBOOK` and one dye-tag ingredient using `ConventionalItemTags.DYES`.
   - Do not change `matches`, `assemble`, `canCraftInDimensions`, `getResultItem`, or serializer registration unless compiler feedback shows a small import or mapping adjustment is needed.
   - Expected result: Patchouli's `PageCrafting` can render the input slots for the custom recipe while the real recipe continues accepting one or more dyes.

5. Validate JSON and build.
   - Target areas: Patchouli entry JSON syntax, Java imports, and mappings for `Ingredient` and `NonNullList`.
   - Run `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null`.
   - Run `./gradlew build` from the repository root.
   - Expected result: JSON validates and Java/resource build succeeds.

6. Run manual in-client acceptance checks.
   - Launch with `./gradlew runClient` or `just runclient`.
   - In a fresh or appropriately reset test profile, confirm the Dyeable Spellbook entry unlocks when the regular Spellbook entry unlocks.
   - In the Hexcasting Items category, confirm the Dyeable Spellbook entry appears adjacent to the regular Spellbook entry.
   - Confirm the Dyeable Spellbook entry icon cycles colors while visible in the guide entry list.
   - Open the entry and confirm the crafting page now shows a regular spellbook input and a dye input, with the dye input cycling through tagged dyes if Patchouli displays the tag normally.
   - Confirm the actual crafting recipe still works with a regular spellbook plus a dye and still preserves spellbook NBT.
   - Confirm the dyeable spellbook remains absent from creative tabs and creative search.
   - Expected result: guide progression, icon presentation, recipe display, and crafting behavior all match the requested behavior.

7. Update the living plan during implementation.
   - Target file: this saved plan under `specs/`.
   - Mark progress items completed after implementation and verification.
   - Record any API or mapping adjustment, such as the exact `Ingredient` factory used, in the Decision Log or Notes.
   - Expected result: future sessions can tell what was implemented and verified.

## Verification

- JSON syntax check: `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null`. Expected result: no output and exit code 0.
- Build check: `./gradlew build`. Expected result: Java compilation, resource processing, remapping, and build complete successfully.
- Packaged resource check: inspect `build/resources/main/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`. Expected result: it contains `advancement: "hexcasting:root"`, `sortnum: 4`, and the special NBT-bearing icon string.
- Manual unlock check: run `./gradlew runClient`, open Hexcasting's guide under conditions where the regular Spellbook entry is unlocked. Expected result: the Dyeable Spellbook entry is unlocked at the same time.
- Manual ordering check: in the Hexcasting Items category, confirm the Dyeable Spellbook entry appears near the regular Spellbook entry.
- Manual icon check: while viewing the Items entry list, confirm the Dyeable Spellbook icon cycles through cover colors.
- Manual recipe page check: open the Dyeable Spellbook entry crafting page. Expected result: it shows the normal spellbook and a dye ingredient instead of blank inputs.
- Manual crafting regression check: craft a regular Hexcasting spellbook with a dye. Expected result: the output is `atelier-of-glamour:dyeable_spellbook`, dyed correctly, and prior spellbook NBT is preserved.
- Manual creative regression check: confirm the dyeable spellbook is still absent from creative tabs and creative search.
- Client log check: during guide opening, watch for Patchouli warnings about invalid icon item stack, missing recipe, missing category, or malformed entry. Expected result: no relevant warnings or errors.

## Risks and Blockers

- Patchouli static entry icons do not support multi-stack or animated icons directly. Mitigation: use an NBT-marked item stack and the existing item color provider rather than a Patchouli GUI mixin.
- The NBT-marked icon stack may render a tooltip with hidden NBT only if advanced tooling exposes it. Mitigation: use a private harmless boolean flag and keep it limited to the guide entry icon.
- If `Ingredient.of(ConventionalItemTags.DYES)` does not compile under the current mappings, implementation must inspect the available `Ingredient` factories and use the equivalent tag ingredient construction.
- Patchouli may display only one dye slot even though the real recipe accepts multiple dyes. Mitigation: keep the page text explicit that one or more dyes may be used, and treat the grid as the minimal recipe display.
- If the dye tag contains many items, Patchouli may cycle the dye input visually. This is acceptable, but manual verification should confirm it is not unreadable or broken.
- Approval is required before destructive file operations, dependency changes, migrations, external service changes, commits, pushes, production actions, broad unrequested refactors, or scope expansion.

## Progress

- [x] Planning complete and saved.
- [x] Implementation completed in `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`, `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java`, and `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java`.
- [x] Automated verification run: `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null` passed.
- [x] Automated verification run: `./gradlew build` passed.
- [x] Packaged resource check passed: `build/resources/main/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json` contains `advancement: "hexcasting:root"`, `sortnum: 4`, and the special NBT-bearing icon string.
- [ ] Manual in-client unlock, ordering, icon cycling, recipe page, crafting, creative hiding, and client log checks not run.

## Decision Log

- Decision: Keep the Dyeable Spellbook as its own guide entry.
  Rationale: The user selected an own entry instead of merging into the regular Spellbook entry.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Unlock with `hexcasting:root`.
  Rationale: The regular Spellbook entry in the local Hexcasting reference uses `advancement: "hexcasting:root"`, so matching it satisfies "unlocked at the same time".
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Use a simple cycling icon implemented through an NBT-marked icon stack and the existing item color provider.
  Rationale: Patchouli static JSON parses only one icon stack or texture, while the client color provider can animate the tint for a specially marked stack with minimal code.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Keep the `patchouli:crafting` page and fix `DyeableSpellbookRecipe.getIngredients()`.
  Rationale: The user selected Patchouli recipe rendering, and Patchouli's source renders recipe inputs from `recipe.getIngredients()`.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Use `Ingredient.of(ConventionalItemTags.DYES)` for the Patchouli-visible dye input.
  Rationale: The current mappings accept the Fabric conventional dye tag directly, and `./gradlew build` passed with this API.
  Date/Author: 2026-06-16 / PI implementation agent

- Decision: Use `AtelierPatchouliColorCycle` as the private icon NBT marker.
  Rationale: Patchouli can parse NBT-bearing item stack strings, and the marker lets the client color provider animate only documentation icon stacks without changing ordinary dyed spellbooks.
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

- Implementation has now been performed after the plan was created.
- The currently implemented creative-tab hiding from the previous task was preserved.
- The current Patchouli entry uses inline English text; this plan does not require moving it to language keys.
- If manual testing shows `sortnum: 4` is not visually ideal, adjust only the sort number and record the final value in the Decision Log.
