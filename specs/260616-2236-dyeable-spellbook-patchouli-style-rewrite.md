# Dyeable Spellbook Patchouli Style Rewrite Plan

## Purpose

Rewrite the Dyeable Spellbook Hexcasting guide entry so it reads like the local Hex Casting Patchouli guidebook: an in-universe first-person wizard's note rather than a fourth-wall addon description. The plan enables a later implementation pass to change only the entry prose and optional localization keys while preserving current gameplay documentation and Patchouli behavior.

## Context

- Repository: Fabric 1.20.1 Java mod `atelier-of-glamour`, Java 17, Fabric Loom.
- Project commands are available through `justfile`: `just build`, `just runclient`, `just datagen`, plus direct Gradle commands such as `./gradlew build` and `./gradlew runClient`.
- Repository rule: implementation plans live under `specs/`, and `specs/INDEX.md` must be maintained.
- Target Patchouli entry: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
- Current target entry uses inline prose that says `Atelier of Glamour adds...`, which breaks the in-universe Hex Casting guide voice.
- Current target entry metadata should be preserved unless implementation discovers it has already changed: `category: hexcasting:items`, `advancement: hexcasting:root`, `sortnum: 4`, an animated or NBT-bearing dyeable spellbook icon, a text page, and a `patchouli:crafting` page for `atelier-of-glamour:dyeable_spellbook`.
- Local Hexcasting guide reference files are available under `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/...` with language text in `references/textures/assets/hexcasting/lang/en_us.json`.
- Relevant local style references inspected:
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/spellbook.json`
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/focus.json`
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/scroll.json`
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/staff.json`
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/pigments.json`
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/casting/101.json`
  - `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/basics/start_to_see.json`
  - representative lore entries under `references/textures/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/lore/`.
- Style findings from the local dependency:
  - The guide speaks in first person singular: `I can`, `my art`, `my research`, `I believe`, `I ought to...`.
  - It explains mechanics as observations by a practitioner, not as mod documentation.
  - It uses Patchouli formatting heavily: `$(l:...)`, `$(item)`, `$(action)`, `$(thing)`, `$(italic)`, `$(br2)`.
  - It mixes practical instruction with personality: curiosity, pride, mild arrogance, dry humor, and occasional ominous or whimsical asides.
  - Item entries often explain exact usage plainly, then end recipe pages with a short italic quote or diary-like aside.
- Current project localization file `src/main/resources/assets/atelier-of-glamour/lang/en_us.json` only contains the item name key and can safely receive Patchouli text keys if the implementation chooses to remove inline prose.
- No external web research was needed because the user specifically requested analysis of the downloaded local Hexcasting dependency.

## Requirements

- Rewrite the Dyeable Spellbook Patchouli entry prose to match the local Hex Casting guidebook voice.
- Remove fourth-wall wording, including direct addon wording such as `Atelier of Glamour adds...` and mod-name references in the prose.
- Preserve gameplay facts:
  - The item is a spellbook variant with a dyeable cover.
  - It behaves like a normal Hexcasting spellbook.
  - It stores and scrolls through pages or iotas like a normal spellbook.
  - Filled and sealed pages or stored spellbook data are preserved.
  - Crafting uses a regular Hexcasting spellbook with one or more dyes.
- Keep the entry as its own Hexcasting Patchouli entry under the Items category.
- Keep the current unlock, ordering, icon behavior, recipe id, page count, and page types unless implementation discovers the source already differs and must be preserved as-is.
- Prefer localization keys over inline long prose, because the user explicitly allowed localization keys and upstream Hexcasting uses keys for most guide text.
- If using localization keys, add only the minimal necessary keys to `src/main/resources/assets/atelier-of-glamour/lang/en_us.json` and point the entry `name` and page `text` fields at those keys.
- Use the repository's existing JSON formatting conventions for the touched files.

## Out of Scope

- Gameplay, recipe, item, model, texture, color-provider, creative-tab, advancement, or registry changes.
- Changing the dyeable spellbook item display name outside the guide entry.
- Reworking unrelated Patchouli entries or adding translations for other languages.
- Adding dependencies, datagen infrastructure, migrations, generated resources, or broad refactors.
- Changing Hexcasting reference files under `references/`.
- Commits, pushes, destructive file operations, production actions, or external service changes.

## Assumptions

- Patchouli will resolve arbitrary translation keys from this mod's `assets/atelier-of-glamour/lang/en_us.json` even though the entry is injected under the `hexcasting` book namespace, because Minecraft localization is global by key.
- Keeping the guide entry name as `Dyeable Spellbook` is acceptable even though the actual item localization currently displays as `Spellbook`; the guide entry can be more specific without changing the item name.
- A concise two-page entry is enough because the current entry has one text page and one crafting page, and the task is a style rewrite rather than an expanded documentation feature.
- If localization keys unexpectedly fail to render in Patchouli during manual testing, the fallback is to keep the same text inline in the entry JSON rather than changing behavior or adding infrastructure.

## Plan

1. Re-read the target entry and preserve non-prose metadata.
   - Target file: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
   - Confirm the current `category`, `advancement`, `sortnum`, `icon`, page types, and recipe id.
   - Do not alter metadata unrelated to prose.
   - Expected result: the implementation changes only the entry's displayed text and optional translation-key wiring.

2. Add Patchouli localization keys for the entry text.
   - Target file: `src/main/resources/assets/atelier-of-glamour/lang/en_us.json`.
   - Add keys with a consistent namespace, for example:
     - `atelier-of-glamour.patchouli.entry.dyeable_spellbook`
     - `atelier-of-glamour.patchouli.page.dyeable_spellbook.1`
     - `atelier-of-glamour.patchouli.page.dyeable_spellbook.crafting.desc`
   - Preserve the existing `item.atelier-of-glamour.dyeable_spellbook` value unless the user separately asks to rename the item.
   - Expected result: guide text is stored alongside this mod's other English localization and can be referenced from the Patchouli entry.

3. Rewrite the guide prose in Hex Casting's first-person diary voice.
   - Target file: `src/main/resources/assets/atelier-of-glamour/lang/en_us.json` if using localization keys, otherwise the target Patchouli entry.
   - Use first-person phrasing and in-universe observation rather than addon documentation.
   - Include Patchouli markup for the regular spellbook link and item terms, such as `$(l:items/spellbook)$(item)Spellbook/$`, `$(item)dyes/$`, `$(br2)`, and optional `$(italic)` for a short aside.
   - Keep the mechanics explicit enough for players to understand the recipe and behavior.
   - A suitable starting draft for the text page is:
     - `I thought my $(l:items/spellbook)$(item)Spellbook/$ already too fine a thing to improve, until I realized how dreadful it is to mistake one volume for another at the bottom of my pack.$(br2)A little dye worked into the cover marks it plainly, and the art within is undisturbed: each page still holds its iota, sealed pages remain sealed, and I can select the active page just as before.`
   - A suitable starting draft for the crafting page text is:
     - `Working one or more $(item)dyes/$ into a $(l:items/spellbook)$(item)Spellbook/$ stains only the cover. Most pleasingly, the contents survive the operation.$(br2)$(italic)I should stop treating that as surprising; the book, at least, has better discipline than I do./$`
   - Expected result: the entry reads like a Hex Casting wizard's practical notebook while retaining all facts from the current prose.

4. Point the Patchouli entry at the localization keys.
   - Target file: `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`.
   - Change `name` from hardcoded `Dyeable Spellbook` to `atelier-of-glamour.patchouli.entry.dyeable_spellbook` if using localization keys.
   - Change the first text page `text` field to `atelier-of-glamour.patchouli.page.dyeable_spellbook.1`.
   - Change the crafting page `text` field to `atelier-of-glamour.patchouli.page.dyeable_spellbook.crafting.desc`.
   - Keep `type: patchouli:text`, `type: patchouli:crafting`, and `recipe: atelier-of-glamour:dyeable_spellbook` unchanged.
   - Expected result: the Patchouli entry renders the new localized text and keeps the existing recipe page.

5. Validate the edited JSON files.
   - Run `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null`.
   - Run `python3 -m json.tool src/main/resources/assets/atelier-of-glamour/lang/en_us.json >/dev/null`.
   - Fix malformed JSON, missing commas, or escaping issues if either command fails.
   - Expected result: both JSON files parse successfully.

6. Build and review the rendered content.
   - Run `./gradlew build` or `just build` from the repository root.
   - If time permits, run `./gradlew runClient` or `just runclient`, open Hexcasting's guide, and inspect the Dyeable Spellbook entry.
   - Confirm no missing translation keys appear, the recipe page still displays, and the prose fits the Hex Casting voice without mentioning the mod.
   - Expected result: build passes, and manual review confirms the entry reads as an in-universe wizard note.

7. Update this living plan during implementation.
   - Target file: this saved plan under `specs/`.
   - Mark progress items complete after implementation and verification.
   - Record any wording deviations, localization-key fallback, or manual rendering observations in Decision Log or Notes.
   - Expected result: a future session can see what was changed and verified.

## Verification

- JSON syntax check for the Patchouli entry: `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null`. Expected result: no output and exit code 0.
- JSON syntax check for localization: `python3 -m json.tool src/main/resources/assets/atelier-of-glamour/lang/en_us.json >/dev/null`. Expected result: no output and exit code 0.
- Build check: `./gradlew build` or `just build`. Expected result: build succeeds with resources processed.
- Packaged resource check after build: inspect `build/resources/main/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json` and `build/resources/main/assets/atelier-of-glamour/lang/en_us.json`. Expected result: the entry references the new keys and the language file contains them.
- Manual Patchouli check: run `./gradlew runClient` or `just runclient`, open the Hexcasting guide, and inspect the Dyeable Spellbook entry. Expected result: the title and both page texts render without missing-key strings, no prose mentions `Atelier of Glamour`, and the recipe page still shows `atelier-of-glamour:dyeable_spellbook`.
- Style acceptance check by reading the final text: expected result is first-person in-universe wizard prose with practical mechanics and a small diary-like aside, matching the local Hexcasting samples more closely than the current hardcoded addon description.

## Risks and Blockers

- Localization-key rendering may fail if Patchouli handles this injected entry differently than expected. Mitigation: fall back to inline text using the same final prose, then record the decision.
- The draft prose may be slightly too whimsical or too long for Patchouli page layout. Mitigation: trim wording while preserving first-person voice and mechanics.
- The current source may already include icon NBT or other metadata from previous plans. Mitigation: preserve existing metadata exactly and only change `name` and `text` fields.
- If manual client testing is skipped, rendered line wrapping and missing-key behavior remain unverified. Mitigation: report manual verification as not run and rely only on JSON/build checks.
- Approval is required before destructive file operations, dependency changes, migrations, external service changes, commits, pushes, production actions, broad unrequested refactors, or scope expansion.

## Progress

- [x] Planning complete and saved.
- [x] Implementation completed in `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json` and `src/main/resources/assets/atelier-of-glamour/lang/en_us.json`.
- [x] Automated verification run: `python3 -m json.tool src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json >/dev/null` passed.
- [x] Automated verification run: `python3 -m json.tool src/main/resources/assets/atelier-of-glamour/lang/en_us.json >/dev/null` passed.
- [x] Automated verification run: `./gradlew build` passed.
- [x] Packaged resource check passed: the built Patchouli entry references the new localization keys, all keys exist, the recipe id remains `atelier-of-glamour:dyeable_spellbook`, and the localized guide prose does not mention `Atelier of Glamour`.
- [ ] Manual in-client Patchouli rendering and line-wrap review not run.

## Decision Log

- Decision: Use the local downloaded Hexcasting dependency as the style source.
  Rationale: The user explicitly requested analysis of the downloaded dependency rather than upstream web research.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Prefer localization keys for the new Patchouli prose.
  Rationale: Upstream Hexcasting guide entries generally reference language keys, and the user explicitly allowed localization keys in this mod's entry.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Preserve the target entry's gameplay and Patchouli metadata.
  Rationale: The task is a prose/style rewrite only, and prior plans already handled unlock, icon, recipe, and creative-tab behavior.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Implement the rewrite through localization keys instead of inline prose.
  Rationale: This matches the local Hexcasting guide structure and keeps the injected entry concise while preserving the existing item display name.
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

- Implementation was performed after planning, at the user's request.
- The current hardcoded text to replace is descriptive but fourth-wall: it explains that `Atelier of Glamour adds a dyeable spellbook` and that players should craft a normal Hexcasting spellbook with dyes.
- The local Hexcasting Spellbook text is the closest mechanical style reference because it describes page selection, iota storage, sealing, and naming in first person.
- The Focus, Scroll, Staff, Pigments, Casting 101, and lore samples support adding a small personal aside while keeping the mechanics clear.
