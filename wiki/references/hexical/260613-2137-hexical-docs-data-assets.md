# Docs, Data, and Assets Pipeline

## Datagen entrypoint

Source files:
- `src/main/resources/fabric.mod.json`
- `src/main/java/miyucomics/hexical/datagen/HexicalDatagen.kt`
- `build.gradle`

Hexical registers `miyucomics.hexical.datagen.HexicalDatagen` as the `fabric-datagen` entrypoint and enables Fabric data generation in `build.gradle` with mod ID `hexical`.

`HexicalDatagen` initializes content providers, then adds generators:
- `HexicalAdvancementGenerator`
- `HexicalBlockLootTableGenerator`
- `HexicalDyeingGenerator`
- `HexicalModelGenerator`
- `HexicalPatchouliGenerator`
- `HexicalRecipeGenerator`

Reusable pattern:
- Provider objects create shared data lists first.
- Generators consume those lists to write recipes, pages, models, loot, and advancements.

## Generated output locations

Observed generated paths:
- `src/main/generated/assets/hexical/blockstates`
- `src/main/generated/assets/hexical/models/block`
- `src/main/generated/assets/hexical/models/item`
- `src/main/generated/assets/hexcasting/patchouli_books/thehexbook/en_us/entries`
- `src/main/generated/data/hexical/advancements`
- `src/main/generated/data/hexical/dyeing`
- `src/main/generated/data/hexical/loot_tables`
- `src/main/generated/data/hexical/recipes/curio`
- `src/main/generated/data/hexical/recipes/flora`
- `src/main/generated/data/hexical/recipes/transmuting`

Implication:
- Generated files are committed and used as part of the mod resource/data packs.

## Patchouli page generation

Sources:
- `datagen/generators/HexicalPatchouliGenerator.kt`
- `datagen/PatchouliPageUtils.kt`
- `datagen/providers/FloraProvider.kt`
- `datagen/providers/DyeingProvider.kt`
- `datagen/providers/TransmutationProvider.kt`

Pattern:
- `EntryBuilder` creates Patchouli JSON with common fields: `name`, `icon`, `category`, `advancement`, `sortnum`, and `pages`.
- Page helpers add `patchouli:text`, `hexcasting:pattern`, `patchouli:crafting`, and `patchouli:spotlight` pages.
- Feature providers append generated extra pages, such as flora recipes and dyeing tables.

Reusable idea:
- Build documentation from the same provider data used to generate recipes.

## Hexcasting book integration

Generated and static pages target Hexcasting's book namespace:
- Book path: `assets/hexcasting/patchouli_books/thehexbook/en_us/...`
- Categories include `hexcasting:items` and `hexcasting:patterns/spells`.
- Pattern pages use `type: "hexcasting:pattern"`, `op_id`, `input`, `output`, and `text`.

Implication:
- Hexical integrates into Hexcasting's existing book instead of shipping a fully separate book.

## Hexdoc pipeline

Source: `doc/hexdoc.toml`

Key settings:
- `modid = "hexical"`
- `book = "hexcasting:thehexbook"`
- `default_lang = "en_us"`
- Resource directories include doc resources, common resources, generated data, Hexcasting, Hexpose, Minecraft, and hexdoc.
- Exports generated docs to `doc/src/hexdoc_hexical/_export/generated`.
- Pattern stubs are extracted from `src/main/java/miyucomics/hexical/inits/HexicalActions.kt` via regex.

Reusable pattern:
- Keep action registration syntax regular enough that documentation tooling can extract IDs, signatures, and start directions.

## Static resources

Important static resource roots:
- `src/main/resources/assets/hexical/lang`: translations.
- `src/main/resources/assets/hexical/textures`: block, item, entity, GUI, misc, mob effect, particle textures.
- `src/main/resources/assets/hexical/shaders`: core and post shaders.
- `src/main/resources/assets/hexical/sounds`: sound event JSON.
- `src/main/resources/assets/hexical/particles`: particle JSON.
- `src/main/resources/data/hexical`: damage types, recipes, tags, prestidigitation data.
- `src/main/resources/data/hexcasting/tags`: Hexcasting-specific tags for actions and items.

## Asset tooling

Source root: `misc`

Observed scripts and artifacts:
- `misc/gosper/main.py`
- `misc/noise/main.py`
- `misc/noise/get_colors.py`
- `misc/shader_source/main.py`
- small source/output images used for noise/media/shader assets.

Reusable pattern:
- Keep asset-generation experiments/scripts separate from runtime source.
- If outputs are checked in, document how to regenerate them.

## Documentation drift controls

Strong controls observed:
- Action IDs/signatures live in `HexicalActions.kt` and are regex-readable by hexdoc.
- Flora/Dyeing/Transmutation providers generate both data and docs fragments.
- Patchouli generator centralizes page layout.

Potential drift points:
- Lang keys are still manual and can drift from page/action IDs.
- Static Patchouli entries can drift from code if not generated.
- Generated files must be regenerated when providers change.

## Checklist for our own feature docs/data

For each new feature:

1. Register action/block/item/entity IDs.
2. Add lang keys for item names, action names, mishaps, docs text, and tooltips.
3. Add static assets: textures, models, blockstates, sounds, particles, shaders as needed.
4. Add generated recipes/loot/models/advancements where applicable.
5. Add Patchouli page through generator if content is data-backed.
6. Add static Patchouli page only for narrative or special content.
7. Update hexdoc extraction/config if action registration syntax changes.
8. Verify generated output is committed or reproducible according to project policy.
9. For data-driven features, ensure docs use the same provider list as recipes.
10. For client visuals, document assets and renderer registration together.

## Completion status for this phase

Done. Datagen, generated outputs, Patchouli integration, hexdoc extraction, resources, asset scripts, and drift controls are documented.
