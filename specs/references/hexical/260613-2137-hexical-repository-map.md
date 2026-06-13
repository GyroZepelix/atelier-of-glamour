# Hexical Repository Map

## Snapshot

- Source: https://github.com/miyucomics/hexical
- Commit analyzed: `8a7b0f04c03830eee158efb3760a1cc0179302d6`
- License: MIT. Reuse requires preserving the copyright/license notice for substantial copied portions.
- Important attribution note: README and LICENSE state that Amber Seal/block entity wrapping work is based on Affinity.

## Stack

Source evidence:
- `build.gradle`
- `gradle.properties`
- `src/main/resources/fabric.mod.json`

Observed stack:
- Minecraft `1.20.1`
- Fabric Loader `0.16.14`
- Fabric API `0.92.2+1.20.1`
- Fabric Loom `1.9-SNAPSHOT`
- Kotlin JVM `2.2.21`
- Java `17`
- Hexcasting `0.11.2-pre-702`
- Hexpose `2.0.0`
- Patchouli, EMI, Cardinal Components, Player Animator, Inline, Paucal, Cloth Config runtime

Inventory at this commit:
- Kotlin source files under `src`: 322
- Java source files under `src`: 34
- Resource/generated files counted under `src/main/resources`, `src/client/resources`, and `src/main/generated`: 478
- Action registrations found in `HexicalActions.kt`: 146 action-style registrations plus one arithmetic registry entry

## Top-level layout

- `build.gradle`: Fabric/Kotlin build, split environment source sets, dependencies, Java 17 target, datagen config.
- `gradle.properties`: pinned Minecraft, Fabric, Hexcasting, Hexpose, and addon versions.
- `README.md`: project philosophy and explicit Affinity credit for Amber Seal.
- `LICENSE`: MIT license with Hexical and Affinity copyright notices.
- `CHANGELOG.md`: feature evolution and useful historical context.
- `doc`: hexdoc configuration, templates, export support, and documentation assets.
- `misc`: small Python scripts and source images for generated/derived visuals.
- `src/main`: common/server code, resources, generated assets/data.
- `src/client`: client-only code and client mixin config.

## Fabric entry points

Source: `src/main/resources/fabric.mod.json`

- Main initializer: `miyucomics.hexical.HexicalMain`
- Client initializer: `miyucomics.hexical.HexicalClient`
- Datagen initializer: `miyucomics.hexical.datagen.HexicalDatagen`
- Cardinal Components entrypoint: `miyucomics.hexical.inits.HexicalCardinalComponents`
- EMI entrypoint: `miyucomics.hexical.features.emi.HexicalEmi`
- Common mixins: `hexical.mixins.json`
- Client mixins: `hexical.client.mixins.json`
- Access widener: `hexical.accesswidener`

## Source sets and responsibilities

### Common/server

Key paths:
- `src/main/java/miyucomics/hexical/HexicalMain.kt`
- `src/main/java/miyucomics/hexical/inits`
- `src/main/java/miyucomics/hexical/features`
- `src/main/java/miyucomics/hexical/mixin`
- `src/main/java/miyucomics/hexical/datagen`
- `src/main/resources`
- `src/main/generated`

Responsibilities:
- Registry setup for actions, blocks, items, entities, iota types, particles, sounds.
- Server hooks and packet receivers.
- Hexcasting action implementations.
- Blocks, block entities, item logic, entities, recipes, data generation.
- Common mixins into Minecraft and Hexcasting internals.

### Client

Key paths:
- `src/client/java/miyucomics/hexical/HexicalClient.kt`
- `src/client/java/miyucomics/hexical/inits`
- `src/client/java/miyucomics/hexical/features`
- `src/client/java/miyucomics/hexical/mixin`
- `src/client/resources/hexical.client.mixins.json`

Responsibilities:
- Render layers, renderer registration, block/entity/item renderers.
- Client packet receivers.
- Particles, overlays, shaders, tooltips, keybinds, player animation.
- Client-only mixins.

## Main feature directories

Common feature directories at this commit:

`akashic_shelves`, `amber_seal`, `autographs`, `block_mimicry`, `breaking`, `charms`, `circle`, `confection`, `confetti`, `conjure`, `curios`, `dda`, `driver_dots`, `dyes`, `evocation`, `flora`, `grimoires`, `grok`, `hex_candles`, `item_cache`, `jailbreak`, `lamps`, `lesser_sentinels`, `mage_blocks`, `mage_hand`, `mage_mouth`, `magic_missile`, `media_jar`, `media_log`, `misc_actions`, `mute`, `pattern_manipulation`, `pedestal`, `periwinkle`, `personal_inventory`, `pigments`, `player`, `prestidigitation`, `rotate`, `scarabs`, `sentinel_beds`, `sentinel_defense`, `shaders`, `sparkle`, `specklikes`, `spike`, `telepathy`, `toast`, `transmuting`, `wristpocket`, `zap`.

Client feature directories at this commit:

`amber_seal`, `autographs`, `charms`, `confetti`, `curios`, `emi`, `evocation`, `jailbreak`, `lamps`, `lesser_sentinels`, `mage_blocks`, `magic_missile`, `media_jar`, `media_log`, `mute`, `patchouli`, `pedestal`, `player`, `scarabs`, `shaders`, `sparkle`, `specklikes`, `spike`, `telepathy`, `toast`.

## Registry and initialization files

- `HexicalMain.kt`: common initialization order.
- `HexicalClient.kt`: client initialization order.
- `inits/HexicalActions.kt`: action and arithmetic registration.
- `inits/HexicalBlocks.kt`: block, block item, and block entity registration.
- `inits/HexicalItems.kt`: item registration and creative item group construction.
- `inits/HexicalEntities.kt`: custom entity type registration.
- `inits/HexicalIota.kt`: custom Hexcasting iota type registration.
- `inits/HexicalHooksServer.kt`: server hook aggregation.
- `client/inits/HexicalHooksClient.kt`: client hook aggregation.
- `client/inits/HexicalBlocksClient.kt`: render layers, block entity renderers, Scrying Lens overlays.
- `client/inits/HexicalEntitiesClient.kt`: entity renderers.
- `client/inits/HexicalParticlesClient.kt`: particle factories.
- `client/inits/HexicalRenderLayers.kt`: custom render layers and shader program registration.

## Resource/data map

- `src/main/resources/assets/hexical`: static blockstates, models, particles, shaders, sounds, textures, lang files.
- `src/main/resources/assets/hexcasting/patchouli_books`: static Patchouli entries integrated into Hexcasting's book.
- `src/main/resources/data/hexical`: damage types, recipes, tags, prestidigitation data.
- `src/main/resources/data/hexcasting/tags`: Hexcasting action/item tags.
- `src/main/generated`: Fabric datagen output for models, loot tables, recipes, advancements, dyeing data, flora/transmuting recipes, and generated Patchouli pages.
- `doc/hexdoc.toml`: hexdoc uses source regex extraction for action signatures and combines common resources, generated data, Hexcasting, Hexpose, Minecraft, and hexdoc resources.

## Where to look by concern

- Add or study Hexcasting action: `inits/HexicalActions.kt` plus `features/<feature>/Op*.kt`.
- Add item: `inits/HexicalItems.kt`, item class under `features`, assets/models/lang/recipes.
- Add block or block entity: `inits/HexicalBlocks.kt`, `features/<feature>`, client renderer if needed, blockstate/model/loot/datagen.
- Add server hook: create `InitHook` implementor and register in `HexicalHooksServer.kt`.
- Add client hook: create client `InitHook` implementor and register in `HexicalHooksClient.kt`.
- Add packet effect: common/server helper with `ServerPlayNetworking`, client receiver under `src/client`.
- Add documentation: datagen provider plus Patchouli/hexdoc resources and lang entries.

## Completion status for this phase

Done. The repository structure, dependencies, feature directories, entry points, resources, and primary lookup paths are mapped against the pinned commit.
