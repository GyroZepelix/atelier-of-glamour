# Transmog Name Preservation and Tag Rename

## Purpose

Adjust the existing item transmogrification MVP so names behave naturally and the stack NBT root tag is simplified. Transmog should preserve existing custom names, display default target names without italics only when the real stack had no custom name, and store transmog data under a `Transmog` compound tag going forward.

## Context

The repository is a Fabric 1.20.1 mod using Java 17 and Fabric API. The transmog MVP currently exists in uncommitted source changes from the prior implementation. The relevant files are:

- `src/main/java/com/dgjalic/transmog/TransmogData.java`: stores transmog NBT, applies target names, clears transmog, and creates target appearance stacks.
- `src/main/java/com/dgjalic/command/AtelierCommands.java`: registers `/aog transmog set <item_id>` and `/aog transmog clear`.
- `src/client/java/com/dgjalic/client/mixin/ItemRendererTransmogMixin.java`: renders transmogrified items using a default target `ItemStack` through `ItemRenderer#renderStatic`.
- `src/main/java/com/dgjalic/AtelierOfGlamour.java`: wires command registration.
- `src/client/resources/atelier_of_glamour.client.mixins.json`: wires the client render mixin.

Current `TransmogData` uses `ROOT_TAG = "AtelierTransmog"`. It always calls `stack.setHoverName(targetStack.getHoverName())` when applying transmog, which overwrites existing custom names and makes generated target names display with vanilla custom-name italic styling. The user confirmed that old test items using `AtelierTransmog` can be ignored, so no backward compatibility or migration is required.

Repository rules require plans/specs under `specs/` and maintenance of `specs/INDEX.md`. No wiki index files were present. No external web research was needed because this is a small follow-up against already-compiled local Fabric 1.20.1 code.

## Requirements

- Change the transmog root NBT compound from `AtelierTransmog` to exactly `Transmog`.
- Ignore old stacks that only contain the previous `AtelierTransmog` test tag. Do not read, migrate, or clear the old tag.
- If a held item already has a custom hover name before transmog is applied, applying transmog must keep that custom name unchanged.
- If a held item has no custom hover name before transmog is applied, applying transmog must display the target item's default name.
- The generated target display name for a previously unnamed item must not be italicized like a vanilla custom/anvil name.
- Clearing transmog must remove only the new `Transmog` root compound and restore normal name behavior:
  - Existing custom names remain unchanged.
  - Generated target names on previously unnamed items are removed so the real item shows its default name again.
- Preserve the existing command syntax: `/aog transmog set <item_id>` and `/aog transmog clear`.
- Preserve the existing rendering approach and gameplay behavior: transmog changes appearance/name only, not real item identity, NBT, spellbook data, enchantments, durability, or behavior.

## Out of Scope

- No backward compatibility or migration for old `AtelierTransmog` test items.
- No redesign of the render mixin, command tree, permissions, tab completion, or transmog target storage beyond the root tag rename and name behavior changes.
- No new dependencies.
- No survival UI, crafting/anvil/smithing flow, recipe, Patchouli documentation, or broader gameplay integration.
- No broad cleanup, unrelated refactors, commits, pushes, destructive file operations, migrations, production actions, or external service changes.

## Assumptions

- In Minecraft 1.20.1, generated non-italic names can be represented by setting a hover-name `Component` with `Style.EMPTY.withItalic(false)` or equivalent style override. This should be confirmed by `./gradlew build` and manual tooltip checks.
- The implementation can continue using a small amount of NBT metadata to distinguish whether transmog applied a generated name. This is safe because transmog data is already stored per stack.
- Existing custom names should not be snapshotted for restoration if transmog never changes them. This reduces stale-name risks if an item is renamed while transmogged.
- Manual acceptance checks are required because tooltip italics and visible names are UI behavior not covered by the current automated test setup.

## Plan

1. Update transmog storage constants and NBT shape.
   - Target file: `src/main/java/com/dgjalic/transmog/TransmogData.java`.
   - Change `ROOT_TAG` from `"AtelierTransmog"` to `"Transmog"`.
   - Remove fields and logic that are only needed for preserving/restoring overwritten custom names, such as `HadOriginalCustomName`, `OriginalCustomName`, and `AppliedName`, unless the implementer finds a minimal flag still useful for generated-name cleanup.
   - Keep `TargetItem` or an equivalent target-id key inside `Transmog`.
   - Expected result: new transmog stacks use only the new root tag, and old `AtelierTransmog` data is ignored.

2. Change apply-name behavior.
   - Target file: `src/main/java/com/dgjalic/transmog/TransmogData.java`.
   - In `apply`, first record whether `stack.hasCustomHoverName()` is true before any mutation.
   - Always store the target item id under `Transmog`.
   - If the stack already has a custom hover name, do not call `setHoverName`; leave the visible name exactly as it is.
   - If the stack has no custom hover name, set a generated target hover name using the target stack's default hover name with italic styling disabled.
   - Store a small boolean such as `AppliedGeneratedName` only for the no-custom-name case if needed so `clear` knows whether it should remove the generated name.
   - Expected result: `Doomslayer` stays `Doomslayer` when transmogged to potato, while an unnamed diamond sword displays non-italic `Potato` when transmogged to potato.

3. Change clear-name behavior.
   - Target file: `src/main/java/com/dgjalic/transmog/TransmogData.java`.
   - In `clear`, read only the new `Transmog` compound.
   - If the transmog data indicates this feature applied a generated target name, call `stack.resetHoverName()` before or after removing the `Transmog` tag.
   - If the item had an existing custom name and transmog did not apply a generated name, do not reset the hover name.
   - Remove the new `Transmog` root tag.
   - Expected result: clearing preserves pre-existing custom names and removes only generated target names.

4. Verify command and render code still use the helper correctly.
   - Target files: `src/main/java/com/dgjalic/command/AtelierCommands.java` and `src/client/java/com/dgjalic/client/mixin/ItemRendererTransmogMixin.java`.
   - Confirm no command changes are needed beyond whatever compile errors the helper API changes create.
   - Confirm `TransmogData.hasTransmog` and `TransmogData.createAppearanceStack` read the new `Transmog` root tag.
   - Expected result: existing command syntax and render substitution continue working with the new NBT tag.

5. Update the original transmog MVP plan progress if implementation proceeds.
   - Optional target: `specs/260620-1239-any-item-transmogrification-command.md`.
   - If later implementation changes the already-saved MVP behavior, record a short decision/progress note there or in this new plan.
   - Expected result: the planning record stays understandable for future sessions.

## Verification

- Run `./gradlew build`.
  - Expected: build succeeds with Fabric 1.20.1 and Java 17.
- Manual check: unnamed item.
  - Hold an unnamed `minecraft:diamond_sword` or `atelier_of_glamour:dyeable_spellbook`.
  - Run `/aog transmog set minecraft:potato`.
  - Expected: item renders as potato and displays `Potato` without italics.
  - Run `/aog transmog clear`.
  - Expected: item returns to its real default name and real visual behavior.
- Manual check: pre-named item.
  - Rename a diamond sword to `Doomslayer`.
  - Run `/aog transmog set minecraft:potato`.
  - Expected: item renders as potato but still displays `Doomslayer` with the same custom-name behavior it had before transmog.
  - Run `/aog transmog clear`.
  - Expected: item remains named `Doomslayer` and returns to its real visual behavior.
- Manual NBT check, if convenient with `/data get entity` or an NBT viewer.
  - Expected: new transmogged stacks use a `Transmog` compound and do not create `AtelierTransmog`.
- Manual old-tag check is optional.
  - Expected: old `AtelierTransmog` test stacks are ignored by new logic, as confirmed by the user.

## Risks and Blockers

- Minecraft tooltip styling can inherit italics from vanilla custom-name rendering. Mitigation: explicitly set the generated target name component's italic style to false and verify in-game.
- If the item is renamed while transmogged, clear behavior depends on whether transmog applied a generated name. Mitigation: only reset names that were generated by transmog for previously unnamed items; leave existing/custom names alone otherwise.
- Existing old test items with `AtelierTransmog` will stop working. This is intentional and user-confirmed.
- Any dependency changes, broad refactors, migrations, destructive operations, commits, pushes, external service changes, production actions, or scope expansion require explicit approval during later implementation.

## Progress

- [x] Planning complete and saved.
- [x] Implementation complete.
- [x] Verification run: `./gradlew build` passed.
- [ ] Manual in-game named/unnamed item checks not run in this session.

## Decision Log

- Decision: Ignore old `AtelierTransmog` test items.
  Rationale: The user explicitly confirmed backward compatibility is not needed.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Preserve pre-existing custom names rather than snapshotting and restoring them.
  Rationale: Transmog should not alter custom names in the first place, which avoids stale restoration bugs.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Use non-italic generated target names only for items that were unnamed before transmog.
  Rationale: This matches vanilla default-name appearance while still communicating the transmog target name.
  Date/Author: 2026-06-20, PI planning agent

- Decision: Store `AppliedGeneratedName` and `GeneratedName` under the new `Transmog` tag.
  Rationale: This lets clear remove only names generated by transmog while preserving real custom names, including a custom rename applied while the item is transmogged.
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

    Implement the transmog name preservation and tag rename follow-up from this saved plan. Keep `/aog transmog set <item_id>` and `/aog transmog clear`, change the root tag to `Transmog`, preserve existing custom names, use non-italic generated target names only for previously unnamed items, and verify with `./gradlew build` plus the manual named/unnamed item checks.

## Notes

- This plan is a small follow-up to `specs/260620-1239-any-item-transmogrification-command.md`.
- Primary implementation target should be `src/main/java/com/dgjalic/transmog/TransmogData.java`.
- Keep old `AtelierTransmog` behavior intentionally unsupported unless the user later asks for migration.
