# Dyeable Spellbook Texture Layers Plan

## Purpose

Plan the change that lets the dyeable spellbook tint only the cover/colorable texture area while keeping pages and other uncolorable details at their authored colors across empty, filled, and sealed item states.

## Context

- Repository: Fabric 1.20.1 Minecraft mod `atelier-of-glamour`, Java 17, split main/client source sets via Fabric Loom.
- Current item class: `src/main/java/com/dgjalic/item/DyeableSpellbookItem.java` extends Hexcasting `ItemSpellbook` and implements `DyeableLeatherItem`; it returns default dye color `0xFFFFFF` and `numVariants() == 1`.
- Current client render setup: `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java` registers `hexcasting:overlay_layer` property values `0`, `1`, and `2` for empty, filled, and sealed spellbook states. It also registers an item color provider where tint index `0` is the leather dye color, tint index `1` is the Hexcasting iota overlay color, and every other tint index is white.
- Current models:
  - `src/main/resources/assets/atelier-of-glamour/models/item/dyeable_spellbook.json` uses a single `layer0` texture for the empty state and overrides to filled/sealed models.
  - `dyeable_spellbook_filled.json` and `dyeable_spellbook_sealed.json` use `layer0` for the book texture and `layer1` for the Hexcasting overlay texture.
- Current texture assets are 16x16 RGBA PNGs under `src/main/resources/assets/atelier-of-glamour/textures/item/`.
- Root problem: because the current base book texture is entirely on tint index `0`, painting the spellbook tints both the cover and pages/uncolorable details.
- Upstream Hexcasting spellbook models use generated item layers with `layer0` for the book state and `layer1` for the iota overlay. This mod needs one extra authored texture layer and an adjusted tint mapping to preserve the overlay behavior.
- Repository planning rule: plans are stored in `specs/` and indexed in `specs/INDEX.md`.

## Requirements

- Split the dyeable spellbook art so cover/colorable pixels are on a dye-tinted layer and pages/uncolorable pixels are on an untinted layer.
- Preserve empty, filled, and sealed spellbook model states selected by the existing `hexcasting:overlay_layer` item property.
- Preserve Hexcasting iota overlay coloring for filled and sealed states.
- Keep texture dimensions and paths compatible with Minecraft generated item models, using 16x16 RGBA PNG assets unless the existing art is intentionally replaced.
- Keep server/common item behavior unchanged unless implementation discovers that `numVariants()` directly affects model selection for this custom one-variant item.
- Do not add dependencies for image processing or rendering.

## Out of Scope

- Adding multiple visual spellbook variants beyond the existing single dyeable variant.
- Changing dye recipes, sealing recipes, NBT format, item registration, item group placement, or Hexcasting storage behavior.
- Reworking unrelated textures or upstream Hexcasting assets.
- Adding a custom item renderer or model loader unless vanilla generated item layers cannot satisfy the requirement after verification.

## Assumptions

- Minecraft's generated item model will render consecutive `layer0`, `layer1`, and `layer2` texture entries with tint indices matching their layer numbers, as used by the existing `layer0` and `layer1` models.
- The safest layer mapping is `layer0 = dye-tinted cover`, `layer1 = untinted pages/details`, and `layer2 = Hexcasting iota overlay` for filled/sealed states. This avoids tinting pages and keeps iota overlay colors separate from the page layer.
- The current `dyeable_spellbook_empty.png`, `dyeable_spellbook_filled.png`, and `dyeable_spellbook_sealed.png` contain both colorable and uncolorable pixels, so implementation will need to edit or recreate PNGs rather than only changing Java and JSON.
- Manual visual verification in a development client is required because automated tests cannot prove pixel-layer correctness for the PNG artwork.

## Plan

1. Inspect and define the final texture split.
   - Target files: `src/main/resources/assets/atelier-of-glamour/textures/item/dyeable_spellbook_empty.png`, `dyeable_spellbook_filled.png`, `dyeable_spellbook_sealed.png`, and their overlay files.
   - Decide exact new asset names before editing, preferably:
     - `dyeable_spellbook_empty_pages.png`
     - `dyeable_spellbook_filled_pages.png`
     - `dyeable_spellbook_sealed_pages.png`
   - Expected result: a clear mapping of which pixels remain in each dye-tinted base texture and which pixels move to the untinted page/detail texture.

2. Create or edit the texture assets.
   - Target files: texture PNGs under `src/main/resources/assets/atelier-of-glamour/textures/item/`.
   - For each spellbook state, leave only cover/colorable pixels in the existing base texture used as `layer0`.
   - Move pages and any uncolorable details to the new `_pages` texture for that state with transparent pixels everywhere else.
   - Keep existing `dyeable_spellbook_filled_overlay.png` and `dyeable_spellbook_sealed_overlay.png` as the iota overlay artwork unless visual inspection shows they also contain page pixels that must be separated.
   - Expected result: dye color affects only the cover layer when rendered.

3. Update item model JSON layer declarations.
   - Target files:
     - `src/main/resources/assets/atelier-of-glamour/models/item/dyeable_spellbook.json`
     - `src/main/resources/assets/atelier-of-glamour/models/item/dyeable_spellbook_filled.json`
     - `src/main/resources/assets/atelier-of-glamour/models/item/dyeable_spellbook_sealed.json`
   - Empty model should declare `layer0` as the colorable base texture and `layer1` as the new untinted pages texture.
   - Filled and sealed models should declare `layer0` as the colorable base texture, `layer1` as the new untinted pages texture, and `layer2` as the existing Hexcasting overlay texture.
   - Expected result: the model layer index gives Java a stable tint contract: dye at index `0`, no tint at index `1`, iota overlay at index `2`.

4. Update the client color provider tint mapping.
   - Target file: `src/client/java/com/dgjalic/client/AtelierOfGlamourClient.java`.
   - Change the registered color provider so tint index `0` returns `AtelierItems.DYEABLE_SPELLBOOK.getColor(stack)`, tint index `2` returns `getIotaColor(stack)`, and tint index `1` plus default return `0xFFFFFF`.
   - Expected result: pages/details render untinted and filled/sealed iota overlays continue to use Hexcasting colors.

5. Review common item code for no-op status.
   - Target file: `src/main/java/com/dgjalic/item/DyeableSpellbookItem.java`.
   - Confirm no source change is needed for `numVariants()` because this custom item exposes one visual variant and state switching is already handled by `hexcasting:overlay_layer`.
   - Expected result: item behavior, dye NBT, and spellbook storage behavior stay unchanged.

6. Update or add any lightweight asset documentation only if needed.
   - Target area: nearby resource files or the implementation notes in this plan.
   - If future maintainers would not understand the layer-index contract from code alone, add a small comment-equivalent note in the plan progress/notes rather than source comments, because JSON models do not support comments.
   - Expected result: no unnecessary code comments or documentation churn.

## Verification

- Build check: run `./gradlew build` from the repository root. Expected result: Gradle completes successfully with Java compilation and resource processing passing.
- Resource path check: confirm every texture path referenced by the three model JSON files exists under `src/main/resources/assets/atelier-of-glamour/textures/item/` as a `.png` file. Expected result: no missing texture references.
- Manual client check: run `./gradlew runClient`, create or obtain the dyeable spellbook, dye it with at least two noticeably different colors, and verify only the cover/colorable area changes while pages/details remain the authored neutral color.
- Filled/sealed visual check: put an iota in the spellbook and seal another copy, then verify filled and sealed overlays still appear and still use the iota/override color instead of the page tint.
- Regression check: verify empty, filled, and sealed state switching still follows the existing `hexcasting:overlay_layer` behavior and no missing-texture purple/black item appears.

## Risks and Blockers

- Texture editing is the main blocker: code and JSON can define layers, but the requirement is not satisfied until the PNG alpha masks actually separate cover pixels from page/detail pixels.
- If generated item models do not render `layer2` as expected in Minecraft 1.20.1, the fallback is to keep `layer0 = dyed cover`, use `layer1 = iota overlay`, and place untinted pages in duplicated overlay/state textures while adjusting the color provider carefully. Prefer verifying `layer2` first because it keeps responsibilities clean.
- If the filled/sealed overlay artwork visually needs to appear between cover and pages, implementation may need to adjust layer ordering and tint index mapping. Preserve the invariant that pages are white-tinted and iota overlay receives `getIotaColor(stack)`.
- Approval is required before destructive file operations, dependency changes, migrations, external service changes, commits, pushes, production actions, or broad unrequested refactors.

## Progress

- [x] Planning complete and saved.
- [x] Implementation completed: texture pages layers added, models rewired, and client tint index mapping updated.
- [x] Automated verification run: texture reference check and `./gradlew build` passed.
- [ ] Manual in-client visual verification not run.

## Decision Log

- Decision: Use three model tint roles: dye cover at tint index `0`, untinted pages/details at tint index `1`, and Hexcasting iota overlay at tint index `2`.
  Rationale: The current tint index `1` is already occupied by the iota overlay, so adding untinted pages requires moving the iota overlay to a separate tint index to avoid recoloring pages.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Store the plan in `specs/` and update `specs/INDEX.md`.
  Rationale: Repository instructions require `specs/` even though the generic plan skill defaults to `spec/`.
  Date/Author: 2026-06-16 / PI planning agent

- Decision: Split empty spellbook pages by neutral/highlight colors and split filled/sealed pages by the interior page rectangle.
  Rationale: The empty sprite's cover pixels are colored while the page/detail pixels are neutral; the filled/sealed sprites use grayscale rectangular book art where the interior page region must remain untinted.
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
- If an image editor is used outside PI, preserve 16x16 RGBA dimensions and transparent backgrounds for split layers.
- Optional implementation aid: after editing JSON, use a small script or manual grep to list `atelier-of-glamour:item/...` texture references and compare them to existing PNG names.
- Automated resource reference check passed after implementation; manual Minecraft client visual verification is still recommended.
