# Hide Dyeable Spellbook and Document Patchouli Guide Plan

## Purpose

Hide the Atelier dyeable spellbook from vanilla creative inventory tabs and creative search while documenting its existence and crafting recipe inside Hexcasting's Patchouli guide book.

## Context

- Repository: Fabric 1.20.1 Java mod `atelier-of-glamour`, Java 17, Fabric Loom split main/client source sets.
- Build and run commands are defined by `justfile`: `just build`, `just runclient`, `just datagen`, with direct Gradle equivalents such as `./gradlew build` and `./gradlew runClient`.
- The dyeable spellbook item is registered in `src/main/java/com/dgjalic/registry/AtelierItems.java` as `AtelierItems.DYEABLE_SPELLBOOK`.
- Current creative exposure is in `AtelierItems.register()`, which calls `ItemGroupEvents.modifyEntriesEvent(HEXCASTING_TAB).register(entries -> entries.addAfter(HexItems.SPELLBOOK, DYEABLE_SPELLBOOK));`.
- The item remains registered in `BuiltInRegistries.ITEM`; hiding it from creative tabs must not remove registration, recipes, models, lang, client color providers, mixins, or spellbook behavior.
- The current acquisition recipe is `src/main/resources/data/atelier-of-glamour/recipes/dyeable_spellbook.json` with custom serializer `atelier-of-glamour:dyeable_spellbook`; Java logic in `src/main/java/com/dgjalic/recipe/DyeableSpellbookRecipe.java` crafts it from a regular Hexcasting spellbook plus one or more dyes and preserves spellbook NBT.
- Existing prior plan `specs/260616-2035-dyeable-spellbook-dye-tag-recipe.md` records that the leather recipe was replaced by the dye recipe and that build verification previously passed.
- This mod currently has no Patchouli book resources under `src/main/resources` and has an empty datagen entrypoint in `src/client/java/com/dgjalic/client/AtelierOfGlamourDataGenerator.java`.
- The local Hexcasting jar extraction does not include Patchouli book JSON files, but the Hexical reference documentation in `specs/references/hexical/260613-2137-hexical-docs-data-assets.md` confirms the integration path used by Hexcasting addons: `assets/hexcasting/patchouli_books/thehexbook/en_us/...`, with entries targeting categories such as `hexcasting:items`.
- User clarified scope: hide only from vanilla/Hexcasting creative inventory tabs and creative search, not from JEI, EMI, REI, recipe viewers, recipes, commands, loot, or registries.
- Repository rule: implementation plans are stored in `specs/` and `specs/INDEX.md` must be maintained.

## Requirements

- Remove the dyeable spellbook from creative inventory tab population so it no longer appears in the Hexcasting creative tab or creative search.
- Keep `atelier-of-glamour:dyeable_spellbook` registered and fully obtainable through crafting.
- Do not hide the recipe from the recipe book or item/recipe browser mods such as JEI, EMI, or REI.
- Add in-game Patchouli documentation inside Hexcasting's guide book, not a separate Atelier book.
- Document both the existence and crafting of the dyeable spellbook.
- Place the documentation in an appropriate Hexcasting guide category, preferably `hexcasting:items`, because this is an item variant and recipe documentation rather than a casting pattern.
- Include enough page content for players to discover that a regular spellbook plus dye produces a dyeable spellbook and that the color can be changed with dyes while retaining spellbook behavior.
- Reuse the existing custom recipe id `atelier-of-glamour:dyeable_spellbook` in the Patchouli crafting page if Patchouli can display the custom recipe acceptably.
- Keep changes small and limited to creative tab registration and Patchouli resource files, plus minimal language/resource support if needed.

## Out of Scope

- Hiding the item from JEI, EMI, REI, recipe viewers, command give autocomplete, registries, loot tables, or datapack recipe discovery.
- Changing the dyeable spellbook item id, display name, textures, models, color provider, recipe behavior, sealing behavior, shift-scroll behavior, or Hexcasting internals.
- Adding a new creative tab, new advancement chain, or separate Patchouli book.
- Adding dependencies, migrations, generated-data infrastructure, or broad datagen refactors.
- Reworking existing recipes or adding new acquisition paths.

## Assumptions

- Removing the `ItemGroupEvents.modifyEntriesEvent(HEXCASTING_TAB)` registration in `AtelierItems.register()` is sufficient to remove the item from both the Hexcasting creative tab and vanilla creative search, because Fabric creative search is populated from creative tab entries rather than every registered item.
- `AtelierItems.register()` can become an empty method or be simplified without affecting item static initialization, because `AtelierItems.DYEABLE_SPELLBOOK` is initialized when `AtelierItems.register()` is called from `AtelierOfGlamour.onInitialize()`.
- A static Patchouli entry is more appropriate than adding datagen for this small narrative/recipe page because the current datagen entrypoint is empty and this task does not require a provider-backed documentation system.
- The correct addon integration path is `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/...`, based on the local Hexical reference. If the first client run reports Patchouli missing the target category or entry path, adjust only the entry path/category while keeping the content in Hexcasting's book.
- The best initial entry location is an item-category entry such as `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`; the implementer should confirm naming conventions by testing in the client because the extracted Hexcasting jar does not expose source book entries.

## Plan

1. Remove creative tab insertion for the dyeable spellbook.
   - Target file: `src/main/java/com/dgjalic/registry/AtelierItems.java`.
   - Delete the Hexcasting creative tab key, the `ItemGroupEvents` registration, and imports that become unused: `HexItems`, `ItemGroupEvents`, `Registries`, `ResourceKey`, and `CreativeModeTab` if no longer needed.
   - Keep `DYEABLE_SPELLBOOK` registration and the private `register(String, T)` helper unchanged.
   - Leave `AtelierItems.register()` present and harmless, either empty or containing only future-safe non-tab registration if required by the compiler.
   - Expected result: the item remains registered, but no creative tab entry is added by Atelier.

2. Add a static Patchouli entry to Hexcasting's book.
   - Target directory to create: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/`.
   - Target file: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
   - Use category `hexcasting:items`, icon `atelier-of-glamour:dyeable_spellbook`, and a stable `sortnum` near the regular spellbook if testing shows ordering can be tuned. If exact neighboring sort numbers are unknown, choose a conservative positive sort value and verify it appears in Items.
   - Include a `patchouli:text` page that explains the dyeable spellbook is an Atelier variant of the normal Hexcasting spellbook, is crafted instead of taken from creative search, can be dyed, and preserves stored spellbook information.
   - Include a `patchouli:crafting` page referencing recipe `atelier-of-glamour:dyeable_spellbook` with text describing `hexcasting:spellbook` plus one or more dyes.
   - Expected result: Hexcasting's Patchouli guide shows a discoverable entry for the Dyeable Spellbook under the Items category.

3. Add or adjust localization only if needed by the Patchouli entry format chosen.
   - Target file: `src/main/resources/assets/atelier-of-glamour/lang/en_us.json` only if the entry uses translation keys instead of inline text.
   - Prefer inline Patchouli entry text for this small static documentation page to avoid adding avoidable indirection.
   - Do not rename the existing item display name unless the implementer discovers the user wants a distinct visible item name in-game.
   - Expected result: no missing translation keys appear in the guide or tooltips.

4. Build and fix compile or resource issues.
   - Target areas: Java imports in `AtelierItems.java`, JSON syntax in the new Patchouli entry, and resource path spelling.
   - Run `./gradlew build` from the repository root.
   - If build fails on unused imports or malformed JSON, fix those directly.
   - Expected result: the project builds successfully with the item still registered and the new resources packaged.

5. Run manual in-client acceptance checks.
   - Launch with `./gradlew runClient` or `just runclient`.
   - Confirm the dyeable spellbook is absent from the Hexcasting creative tab.
   - Confirm searching the creative inventory for the item's display name or item id does not show the dyeable spellbook.
   - Confirm crafting a regular Hexcasting spellbook with a dye still produces `atelier-of-glamour:dyeable_spellbook`.
   - Open Hexcasting's Patchouli guide book and confirm the new Dyeable Spellbook entry appears under the Items category, renders its icon and pages, and the crafting page displays or describes the recipe clearly.
   - Expected result: players can discover the item through the guide and craft it, but cannot pull it from creative tabs/search.

6. Update the living plan after implementation.
   - Target file: this saved plan under `specs/`.
   - Mark implementation and verification progress, and record any Patchouli path/category adjustment in Decision Log or Notes.
   - Expected result: a future PI Agent session can tell exactly what was changed and verified.

## Verification

- Compile/build check: run `./gradlew build`. Expected result: build succeeds, including Java compilation and resource processing.
- Packaged resource check: after build, inspect `build/resources/main/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json` or the equivalent processed resource output. Expected result: the new Patchouli entry is included in the built resources.
- Creative tab manual check: run `./gradlew runClient`, open the Hexcasting creative tab, and confirm the dyeable spellbook is not listed.
- Creative search manual check: in creative mode, search for `Spellbook`, `dyeable`, and `atelier-of-glamour:dyeable_spellbook` if id search is available. Expected result: the Atelier dyeable spellbook does not appear in creative inventory search results.
- Crafting manual check: craft with one regular `hexcasting:spellbook` plus a vanilla dye. Expected result: output is the Atelier dyeable spellbook and it retains dye behavior.
- Patchouli manual check: open Hexcasting's Patchouli guide book and navigate to Items. Expected result: a Dyeable Spellbook entry exists, has the Atelier spellbook icon, explains the item, and shows or clearly documents the crafting recipe.
- Log check during client launch: watch for Patchouli JSON errors, missing category errors, missing recipe errors, or missing translation warnings related to the new entry. Expected result: no relevant errors.

## Risks and Blockers

- The extracted Hexcasting reference lacks Patchouli book entry JSON, so the exact subdirectory and neighboring `sortnum` cannot be confirmed statically. Mitigation: use the Hexical-documented integration root and verify in-client; adjust only path/category/sort if Patchouli reports issues.
- Patchouli may not render a custom `CustomRecipe` grid nicely from `atelier-of-glamour:dyeable_spellbook`. Mitigation: keep explanatory text on the crafting page; if rendering fails, use a text or spotlight page and record the limitation rather than changing recipe behavior.
- If Fabric creative search includes all registered items in this Minecraft/Fabric version, removing tab insertion may not hide search. Mitigation: verify manually; if it still appears, investigate Fabric API options for search tab exclusion without changing registry or recipe availability.
- Empty `AtelierItems.register()` may look odd but is acceptable if it preserves class initialization. Do not remove the call from `AtelierOfGlamour.onInitialize()` unless implementation proves item initialization still happens reliably another way.
- Approval is required before destructive file operations, dependency changes, migrations, external service changes, commits, pushes, production actions, or broad unrequested refactors.

## Progress

- [x] Planning complete and saved.
- [x] Implementation completed in `src/main/java/com/dgjalic/registry/AtelierItems.java` and `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
- [x] Automated verification run: `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null` passed.
- [x] Automated verification run: `./gradlew build` passed.
- [x] Packaged resource check passed: `build/resources/main/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json` exists.
- [ ] Manual in-client creative tab, creative search, crafting, Patchouli rendering, and client log checks not run.

## Decision Log

- Decision: Hide the item only by removing Atelier's creative tab entry insertion.
  Rationale: The user explicitly limited scope to creative inventory tabs and search, and the item must remain craftable and registered.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Document the item in Hexcasting's Patchouli book under the Items category.
  Rationale: The dyeable spellbook is an item variant with a crafting recipe, and local Hexical docs show addons should integrate under `assets/hexcasting/patchouli_books/thehexbook/en_us/...` for the Hexcasting guide.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Prefer a static Patchouli JSON entry over datagen.
  Rationale: The project has no existing docs datagen, the requested documentation is one small entry, and adding generator infrastructure would exceed the task scope.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Use static sort number `25` for the Dyeable Spellbook Patchouli entry.
  Rationale: The local Hexcasting extraction does not include source book entries for exact neighboring sort numbers, so a conservative positive value was chosen and left for in-client ordering verification.
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
- The user clarified not to hide the item from JEI, EMI, REI, or other recipe/item browsers.
- Existing item language currently maps `item.atelier-of-glamour.dyeable_spellbook` to `Spellbook`; the plan does not require changing it.
- If later implementation needs a more polished title in the guide without changing the item name, use the Patchouli entry `name` field as `Dyeable Spellbook`.
