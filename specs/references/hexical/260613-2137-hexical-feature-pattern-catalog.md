# Hexical Feature Pattern Catalog

## Pattern: explicit feature manifest

- Source files:
  - `inits/HexicalActions.kt`
  - `inits/HexicalBlocks.kt`
  - `inits/HexicalItems.kt`
  - `inits/HexicalHooksServer.kt`
  - `client/inits/HexicalHooksClient.kt`
- Problem: a large addon needs many features discoverable and consistently registered.
- Approach: central manifests call feature-local implementation objects.
- Reusable idea: maintain one registry manifest per registry type.
- Use when: public IDs and docs must be stable.
- Avoid when: feature modules need runtime enable/disable.
- Attribution needed: no, if implemented independently.

## Pattern: server action, client effect

- Source files:
  - `features/confetti/OpConfetti.kt`
  - `features/confetti/ConfettiHelper.kt`
  - `src/client/.../features/confetti/ClientConfettiReceiver.kt`
- Problem: a spell should trigger rich visual/audio effects without doing particle-heavy work server-side.
- Approach: validate and charge media server-side, send a compact packet to nearby clients, expand into many particles/sounds client-side.
- Reusable idea: send seed, position, direction, and speed instead of every particle.
- Use when: the effect is cosmetic and deterministic enough to reconstruct client-side.
- Avoid when: particles encode gameplay state or must be authoritative.
- Attribution needed: no, unless copying exact packet/effect code.

## Pattern: data-driven spell recipe

- Source files:
  - `features/flora/OpConjureFlora.kt`
  - `features/flora/ConjureFloraHook.kt`
  - `datagen/providers/FloraProvider.kt`
  - `features/transmuting/TransmutingHelper.kt`
- Problem: many spell targets need per-target costs and outputs.
- Approach: register custom recipe types and generate recipe JSON from providers.
- Reusable idea: spell code performs generic lookup and validation; content is data.
- Use when: balancing/content changes should not require code changes.
- Avoid when: behavior is unique per target and cannot be expressed as data.
- Attribution needed: no, if data model is independently designed.

## Pattern: custom domain iota

- Source files:
  - `features/dyes/DyeIota.kt`
  - `inits/HexicalIota.kt`
  - `features/dyes/actions/OpDye.kt`
- Problem: domain values need first-class stack representation.
- Approach: define `Iota` and `IotaType`, register it, and provide typed stack getters.
- Reusable idea: prefer typed iotas over overloaded numbers/strings.
- Use when: values recur across multiple actions or need display/serialization.
- Avoid when: a value is only internal to one spell.
- Attribution needed: no, if implemented independently.

## Pattern: media-holding block/item pair

- Source files:
  - `features/media_jar/MediaJarBlock.kt`
  - `features/media_jar/MediaJarBlockEntity.kt`
  - `features/media_jar/MediaJarItem.kt`
  - `src/client/.../features/media_jar/MediaJarRenderer.kt`
  - `client/inits/HexicalBlocksClient.kt`
- Problem: media storage should exist both as a placed block and as an item preserving contents.
- Approach: block entity implements `ADMediaHolder`, clamps media, writes NBT, emits update packets, and serializes to item NBT on break. The item reads `BlockEntityTag` for tooltips and inventory interactions.
- Reusable idea: one media API, two surfaces: placed and inventory.
- Use when: the player should carry and place the same storage object.
- Avoid when: storage does not need to survive block/item transitions.
- Attribution needed: no, unless copying exact implementation.

## Pattern: generic transmutation helper

- Source files:
  - `features/transmuting/TransmutingHelper.kt`
  - `features/media_jar/MediaJarBlock.kt`
  - `features/media_jar/MediaJarBlockEntity.kt`
  - `features/media_jar/MediaJarItem.kt`
- Problem: media absorption, refilling holders, and item transmutation need identical behavior across block and inventory contexts.
- Approach: one helper receives lambdas for media insertion/withdrawal and returns a sealed `TransmutationResult`.
- Reusable idea: pass storage operations as functions so the same rule engine works for block entities and item NBT.
- Use when: multiple surfaces share resource conversion rules.
- Avoid when: only one surface exists.
- Attribution needed: no, if independently written.

## Pattern: entity spell with isolated gameplay behavior

- Source files:
  - `features/magic_missile/OpMagicMissile.kt`
  - `features/magic_missile/MagicMissileEntity.kt`
  - `inits/HexicalEntities.kt`
  - `client/inits/HexicalEntitiesClient.kt`
  - `src/client/.../features/magic_missile/MagicMissileRenderer.kt`
- Problem: a spell creates an object with lifetime, collision, damage, sound, and renderer behavior.
- Approach: action only resolves spawn position/velocity; entity owns tick, hit, damage, shatter, and rendering texture.
- Reusable idea: keep spell actions thin and move temporal behavior into entities.
- Use when: behavior persists after casting.
- Avoid when: effect is instantaneous.
- Attribution needed: no, if independently implemented.

## Pattern: block entity wrapping/restoration

- Source files:
  - `features/amber_seal/OpAmberSeal.kt`
  - `features/amber_seal/AmberSealBlock.kt`
  - `features/amber_seal/AmberSealBlockEntity.kt`
  - `features/amber_seal/DanglingAmberState.kt`
  - `mixin/WorldChunkMixin.java`
  - `README.md`
  - `LICENSE`
- Problem: a block, including its block entity, is captured into a wrapper block and restored later.
- Approach: remove block entity, replace state with Amber Seal, store original state/entity in seal block entity, and use a queued dangling block entity plus `WorldChunkMixin` to restore the original entity during block replacement.
- Reusable idea: state/entity capture needs explicit lifecycle control and a bridge around vanilla block entity creation.
- Use when: wrapping arbitrary block entities is required.
- Avoid when: only simple blocks need transformation.
- Attribution needed: yes. Hexical explicitly credits Affinity for this area.
- Recommendation: learn from this but avoid reusing until we have a clear requirement and attribution plan.

## Pattern: player input mirrored to server

- Source files:
  - `features/telepathy/ServerPeripheralReceiver.kt`
  - `src/client/.../features/telepathy/ClientPeripheralPusher.kt`
  - `client/inits/HexicalKeybinds.kt`
- Problem: server-side spells need access to client key and scroll state.
- Approach: client detects key transitions each tick and sends press/release/scroll packets; server stores per-player key states.
- Reusable idea: send deltas, not full state every tick.
- Use when: gameplay needs server-readable live input.
- Avoid when: vanilla input events already reach the server.
- Attribution needed: no, if independently implemented.

## Pattern: docs generated from gameplay providers

- Source files:
  - `datagen/HexicalDatagen.kt`
  - `datagen/generators/HexicalPatchouliGenerator.kt`
  - `datagen/PatchouliPageUtils.kt`
  - `datagen/providers/FloraProvider.kt`
  - `datagen/providers/DyeingProvider.kt`
  - `doc/hexdoc.toml`
- Problem: docs can drift from actions, recipes, and generated data.
- Approach: provider lists generate both data JSON and Patchouli pages; hexdoc extracts action patterns from source.
- Reusable idea: use one source of truth for recipes and docs.
- Use when: feature docs include large generated tables/lists.
- Avoid when: all docs are hand-written narrative pages.
- Attribution needed: no, if independently implemented.

## Pattern: custom shader/render layer surface

- Source files:
  - `client/inits/HexicalRenderLayers.kt`
  - `features/shaders/ServerShaderManager.kt`
  - `features/shaders/OpShader.kt`
  - `src/client/.../features/shaders/ClientShaderReceiver.kt`
- Problem: spells/items need custom visual presentation beyond vanilla render layers.
- Approach: register shader program/render layer on client, trigger post effects by server packet.
- Reusable idea: isolate render layer creation and shader state changes in client init/hooks.
- Use when: visuals are an important feature surface.
- Avoid when: built-in render layers are enough.
- Attribution needed: no, if independently implemented.

## Completion status for this phase

Done. The catalog separates architecture patterns, gameplay concepts, client effects, media storage, data generation, and attribution-sensitive block entity wrapping.
