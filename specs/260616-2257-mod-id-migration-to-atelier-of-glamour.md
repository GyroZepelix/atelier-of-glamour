# Mod ID Migration to atelier_of_glamour

## Purpose

Plan a full early-stage migration of the Minecraft Fabric mod ID and resource namespace from `atelier-of-glamour` to `atelier_of_glamour`, if implementation is later requested. The outcome is a consistent, standards-aligned mod ID across Fabric metadata, Java registry namespaces, resource and data folders, model references, recipes, localization keys, Patchouli references, mixin config names, and Gradle metadata.

## Context

The repository is a Fabric 1.20.1 Java mod using Java 17, Fabric Loom split main/client source sets, Hex Casting, Patchouli, and related Fabric dependencies. The repository rules require plans under `specs/` and maintenance of `specs/INDEX.md`.

Project context detection found no wiki files. Build and run commands are exposed through `justfile`: `just build`, `just runclient`, `just datagen`, plus direct Gradle commands such as `./gradlew build` and `./gradlew runClient`.

Current source scan found only `atelier-of-glamour` in active source and build configuration. No active source or build configuration currently uses `atelier_of_glamour` or `atelierofglamour`.

Current active usages of `atelier-of-glamour` include:

- `src/main/java/com/dgjalic/AtelierOfGlamour.java`: `MOD_ID = "atelier-of-glamour"`, which feeds Java `ResourceLocation` registration for items and recipe serializers.
- `src/main/resources/fabric.mod.json`: mod `id`, icon path, and mixin config references.
- `build.gradle`: Loom `mods { "atelier-of-glamour" { ... } }` entry.
- `settings.gradle`: `rootProject.name = 'atelier-of-glamour'`.
- `src/main/resources/atelier-of-glamour.mixins.json` and `src/client/resources/atelier-of-glamour.client.mixins.json`: mixin config filenames referenced by `fabric.mod.json`.
- `src/main/resources/assets/atelier-of-glamour/`: namespace folder for icon, lang, models, and textures.
- `src/main/resources/data/atelier-of-glamour/`: namespace folder for recipe JSON files.
- `src/main/resources/assets/atelier-of-glamour/lang/en_us.json`: translation keys using `item.atelier-of-glamour...` and `atelier-of-glamour.patchouli...`.
- `src/main/resources/assets/atelier-of-glamour/models/item/*.json`: model and texture references using `atelier-of-glamour:item/...`.
- `src/main/resources/data/atelier-of-glamour/recipes/*.json`: custom recipe serializer IDs using `atelier-of-glamour:...`.
- `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`: Patchouli translation, icon, and recipe references using `atelier-of-glamour`.

Research findings:

- Fabric `fabric.mod.json` allows IDs that start with a letter and contain ASCII letters, digits, underscores, or hyphens, 2 to 64 characters. Fabric's own example uses `example-mod`.
- Fabric wiki says mod IDs can contain lowercase `a-z`, digits `0-9`, and `_` or `-`; examples include `myproject`, `my_project`, or `my-project`.
- Minecraft Java identifiers allow lowercase letters, digits, underscore, hyphen, and dot in namespace and path, but the preferred naming convention is `snake_case`.
- NeoForge documentation says mod IDs may only contain lowercase letters, digits, and underscores. This makes `atelier_of_glamour` more cross-loader-friendly than `atelier-of-glamour` if the project ever ports or shares conventions.
- Modrinth project slugs allow a broad regex and do not force the mod ID choice.

Sources:

- Fabric `fabric.mod.json`: https://docs.fabricmc.net/develop/loader/fabric-mod-json
- Fabric mod ID conventions: https://wiki.fabricmc.net/tutorial:terms
- Minecraft identifiers: https://minecraft.wiki/w/Identifier
- NeoForge mod files and mod ID rules: https://docs.neoforged.net/docs/gettingstarted/modfiles/
- Modrinth project slug docs: https://docs.modrinth.com/api/operations/getproject/

## Requirements

- Migrate the canonical mod ID and namespace from `atelier-of-glamour` to `atelier_of_glamour` across active source, resource, and build configuration files.
- Use `atelier_of_glamour` as the target, not `atelierofglamour`, because it matches Minecraft's preferred `snake_case` naming and remains compatible with Fabric and NeoForge-style mod ID rules.
- Keep the display name `Atelier of Glamour` unchanged unless the user separately asks to rename branding.
- Preserve current gameplay behavior: dyeable spellbook item registration, custom recipe serializers, item models, textures, coloring, Patchouli entry, crafting behavior, and mixins should continue to load with the new namespace.
- Because the mod is early enough, do not add backwards compatibility aliases, data fixers, old recipe IDs, or old resource namespaces for `atelier-of-glamour`.
- Keep Java package names and class names unchanged unless directly required by the mod ID migration. The current package `com.dgjalic` is not a mod ID and does not need to become `com.dgjalic.atelier_of_glamour` for this task.
- Avoid modifying generated or local runtime outputs under `build/`, `bin/`, `run/`, `.gradle/`, `.bloop/`, `.metals/`, and `references/`.
- During later implementation, stop and ask before dependency changes, migrations beyond this early-stage ID replacement, commits, pushes, force operations, production actions, external service changes, destructive operations, or broad refactors.

## Out of Scope

- Publishing the mod, updating Modrinth/CurseForge project metadata, or changing external service configuration.
- Preserving compatibility with worlds, saves, resource packs, datapacks, recipes, commands, or configs that reference `atelier-of-glamour`.
- Renaming Java packages, class names, item names, user-facing display strings, or Git repository name unless separately requested.
- Editing historical plan files in `specs/` or reference material under `references/` to rewrite old IDs.
- Cleaning unrelated `.DS_Store`, IDE, build, runtime, or generated files.
- Adding dependencies, changing Minecraft/Fabric/Hexcasting/Patchouli versions, or introducing new tooling.

## Assumptions

- The confirmation `c` means the understanding summary is accepted as written.
- `atelier_of_glamour` is the intended target because it is valid in Fabric, valid as a Minecraft identifier namespace, preferred by Minecraft's `snake_case` convention, and compatible with NeoForge mod ID rules.
- Old ID compatibility is not required because the user confirmed the mod is early enough.
- Historical `specs/` references to `atelier-of-glamour` should remain as historical records and should be excluded from source migration verification.
- Generated directories may continue to contain stale old IDs until regenerated or cleaned. Verification should focus on active source and configuration first, then packaged output after `./gradlew build`.

## Plan

1. Audit active ID surfaces before editing.
   - Target areas: `src/`, `build.gradle`, `settings.gradle`, `gradle.properties`, `README.md`, `justfile`, and `.github/workflows/`.
   - Re-run a bounded search for `atelier-of-glamour`, `atelier_of_glamour`, and `atelierofglamour` excluding generated and reference directories.
   - Expected result: implementation starts from a current list of active files and avoids changing historical specs or generated outputs.

2. Rename resource and data namespace paths.
   - Move `src/main/resources/assets/atelier-of-glamour/` to `src/main/resources/assets/atelier_of_glamour/`.
   - Move `src/main/resources/data/atelier-of-glamour/` to `src/main/resources/data/atelier_of_glamour/`.
   - Rename `src/main/resources/atelier-of-glamour.mixins.json` to `src/main/resources/atelier_of_glamour.mixins.json`.
   - Rename `src/client/resources/atelier-of-glamour.client.mixins.json` to `src/client/resources/atelier_of_glamour.client.mixins.json`.
   - Expected result: pack namespace folders and mixin config filenames align with the new mod ID.

3. Update canonical Java and Fabric metadata IDs.
   - In `src/main/java/com/dgjalic/AtelierOfGlamour.java`, change `MOD_ID` to `atelier_of_glamour`.
   - In `src/main/resources/fabric.mod.json`, change `id` to `atelier_of_glamour`, icon path to `assets/atelier_of_glamour/icon.png`, and mixin config references to the renamed mixin JSON files.
   - Expected result: Fabric loads the mod under the new ID and Java registrations create `atelier_of_glamour:*` registry IDs.

4. Update Gradle and project metadata references.
   - In `build.gradle`, change the Loom `mods` key from `atelier-of-glamour` to `atelier_of_glamour`.
   - In `settings.gradle`, change `rootProject.name` from `atelier-of-glamour` to `atelier_of_glamour` if artifact naming should track the mod ID. If the implementer finds a strong reason to keep the artifact name hyphenated, stop and record the tradeoff before proceeding.
   - Expected result: development run configuration and produced artifacts consistently use the new project/mod identity.

5. Update JSON resource references and localization keys.
   - In `src/main/resources/assets/atelier_of_glamour/lang/en_us.json`, change translation keys from `item.atelier-of-glamour...` and `atelier-of-glamour.patchouli...` to `item.atelier_of_glamour...` and `atelier_of_glamour.patchouli...`.
   - In model JSON files under `src/main/resources/assets/atelier_of_glamour/models/item/`, change texture and model references from `atelier-of-glamour:item/...` to `atelier_of_glamour:item/...`.
   - In recipe JSON files under `src/main/resources/data/atelier_of_glamour/recipes/`, change recipe `type` values from `atelier-of-glamour:...` to `atelier_of_glamour:...`.
   - In the Patchouli entry at `src/main/resources/assets/hexcasting/patchouli_books/thehexbook/en_us/entries/items/dyeable_spellbook.json`, change translation keys, icon ID, and recipe ID to `atelier_of_glamour`.
   - Expected result: assets, recipes, lang keys, and Patchouli references resolve under the new namespace.

6. Check for old ID leftovers in active files.
   - Run search commands from the Verification section.
   - If old ID references remain in active source/config, classify each as either a missed migration or a deliberate historical/non-source reference.
   - Expected result: no `atelier-of-glamour` remains in active source/config paths or file contents, excluding historical specs and generated outputs.

7. Build and inspect packaged output.
   - Run `./gradlew build` or `just build`.
   - Inspect `build/resources/main/fabric.mod.json`, `build/resources/main/assets/atelier_of_glamour/`, `build/resources/main/data/atelier_of_glamour/`, and packaged model/Patchouli JSON as needed.
   - Expected result: build succeeds and packaged resources use the new namespace.

8. Perform optional runtime acceptance checks.
   - Run `./gradlew runClient` or `just runclient` if a graphical client check is practical.
   - Create or load a disposable dev world, craft a Hexcasting spellbook with one or more dyes, inspect the resulting item, and open the Hexcasting Patchouli entry.
   - Expected result: the dyeable spellbook works, renders with textures/colors, recipe display still resolves, and no missing texture/missing translation/missing recipe errors appear for the new namespace.

## Verification

- Source old-ID absence check:

    `rg -n "atelier-of-glamour" src build.gradle settings.gradle gradle.properties README.md justfile .github --glob '!build/**' --glob '!bin/**' --glob '!run/**' --glob '!references/**' --glob '!specs/**'`

  Expected result: no output and exit code 1, meaning active source/config no longer contains the old ID.

- New-ID presence check:

    `rg -n "atelier_of_glamour" src build.gradle settings.gradle gradle.properties README.md justfile .github --glob '!build/**' --glob '!bin/**' --glob '!run/**' --glob '!references/**' --glob '!specs/**'`

  Expected result: output includes `AtelierOfGlamour.java`, `fabric.mod.json`, `build.gradle`, `settings.gradle`, assets/data JSON references, Patchouli entry references, and renamed mixin config references.

- Alternative concatenated ID check:

    `rg -n "atelierofglamour" src build.gradle settings.gradle gradle.properties README.md justfile .github --glob '!build/**' --glob '!bin/**' --glob '!run/**' --glob '!references/**' --glob '!specs/**'`

  Expected result: no output and exit code 1.

- Old path check:

    `find src -path '*atelier-of-glamour*' -print`

  Expected result: no output.

- JSON syntax check:

    `find src/main/resources src/client/resources -name '*.json' -print0 | xargs -0 -n1 python3 -m json.tool >/dev/null`

  Expected result: no output and exit code 0.

- Build check:

    `./gradlew build`

  Expected result: Gradle build succeeds.

- Packaged namespace check after build:

    `find build/resources/main -path '*atelier*glamour*' -print | sort`

  Expected result: paths use `atelier_of_glamour` for active resources and do not include `atelier-of-glamour`.

- Optional manual runtime check:

    `./gradlew runClient`

  Expected result: client starts, Fabric loads the mod as `atelier_of_glamour`, dyeable spellbook crafting works, item textures render, and the Hexcasting Patchouli guide entry has no missing keys or unresolved recipe/icon references.

## Risks and Blockers

- Risk: Fabric accepts hyphenated IDs, so this migration changes valid existing IDs rather than fixing a Fabric load error.
  Mitigation: the research shows `atelier_of_glamour` better matches Minecraft `snake_case` preference and NeoForge-style mod ID constraints, and the user confirmed early-stage replacement is acceptable.

- Risk: Renaming registry IDs changes item and recipe identifiers, which would break existing worlds, saved items, recipes, resource packs, datapacks, or commands using `atelier-of-glamour`.
  Mitigation: no compatibility path is needed because the user confirmed the mod is early enough.

- Risk: Missing a hardcoded JSON reference could produce missing textures, missing translations, or Patchouli unresolved recipe/icon references.
  Mitigation: run grep, JSON syntax, build, packaged resource, and optional runtime checks.

- Risk: Generated directories may continue to contain old names and confuse searches.
  Mitigation: exclude generated/runtime/reference directories during source verification and inspect `build/resources/main` only after a fresh build.

- Blocker: If later implementation discovers loader or dependency code that requires hyphenated mixin config names or artifact naming, stop and ask before diverging from this plan.

## Progress

- [x] Planning complete and saved.
- [x] Implementation completed: active source/config/resource IDs were migrated from `atelier-of-glamour` to `atelier_of_glamour`.
- [x] Verification completed: source grep checks, path check, JSON syntax check, `./gradlew build`, and packaged resource checks passed.
- [ ] Optional runtime client/manual gameplay check not run.

## Decision Log

- Decision: Use `atelier_of_glamour` as the target mod ID and namespace.
  Rationale: It is valid for Fabric and Minecraft identifiers, follows Minecraft's preferred `snake_case` convention, and is compatible with NeoForge-style mod ID restrictions. `atelierofglamour` is valid but less readable and less aligned with the documented preference.
  Date/Author: 2026-06-16, PI planning agent.

- Decision: Do not preserve `atelier-of-glamour` compatibility.
  Rationale: The user confirmed the mod is early enough for a direct ID replacement without aliases, data fixers, or old resources.
  Date/Author: 2026-06-16, PI planning agent.

- Decision: Leave historical specs and generated outputs out of the source migration.
  Rationale: Specs are historical planning records and generated outputs should be regenerated by build tasks rather than manually edited.
  Date/Author: 2026-06-16, PI planning agent.

- Decision: Rename `settings.gradle` `rootProject.name` to `atelier_of_glamour`.
  Rationale: Artifact naming should track the canonical mod ID for consistency, and no conflicting requirement for a hyphenated artifact name was found.
  Date/Author: 2026-06-16, PI implementation agent.

## Execution Handoff

Use PI Agent in a fresh session with this prompt:

    Read the saved plan file path reported by the planning agent.
    Implement it step by step. Before editing, re-read the Requirements, Out of Scope, Risks and Blockers, and Verification sections.
    Update the Progress and Decision Log sections as work proceeds.
    Run the Verification commands before reporting done.
    Do not commit, push, run migrations, add dependencies, or perform destructive operations without explicit approval.
    If PI plan mode is active, use a numbered Plan: section and mark completed implementation steps with [DONE:n].

Optional subagent recommendation for later implementation:

- Use an Explore subagent only if a fresh search reveals additional broad ID surfaces not covered by this plan. Provide the saved plan path and ask for a read-only list of remaining old/new ID references. Do not use a subagent for the straightforward file edits.

## Notes

- No implementation was performed while creating this plan.
- Keep display strings such as `Atelier of Glamour` unless the user asks for branding changes.
- If using `git mv`, inspect source paths first and avoid touching generated directories.
- After a successful build, stale old artifacts under `build/libs` from earlier builds may remain until `./gradlew clean build`; do not treat old artifact names from prior builds as active source failures unless they are produced by the current build.
- Implementation note: after `./gradlew build`, fresh `atelier_of_glamour` jars were produced, but old `atelier-of-glamour` jars also remained in `build/libs` and `build/devlibs` as stale generated artifacts because `clean` was not run.
