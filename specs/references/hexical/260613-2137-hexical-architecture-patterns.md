# Hexical Architecture Patterns

## Startup flow

Source files:
- `src/main/java/miyucomics/hexical/HexicalMain.kt`
- `src/client/java/miyucomics/hexical/HexicalClient.kt`
- `src/main/resources/fabric.mod.json`

Common initialization order:

1. `HexicalActions.init()`
2. `HexicalAdvancements.init()`
3. `HexicalBlocks.init()`
4. `HexicalEntities.init()`
5. `HexicalIota.init()`
6. `HexicalItems.init()`
7. `HexicalParticles.init()`
8. `HexicalSounds.init()`
9. `HexicalHooksServer.init()`

Client initialization order:

1. `HexicalRenderLayers.clientInit()`
2. `HexicalBlocksClient.clientInit()`
3. `HexicalEntitiesClient.clientInit()`
4. `HexicalHooksClient.init()`
5. `HexicalKeybinds.clientInit()`
6. `HexicalParticlesClient.clientInit()`
7. Global client tick counter setup.
8. Built-in item renderer registration for Amber Seal and Media Jar.

Reusable pattern:
- Keep entry points small.
- Put registry logic in named initializer objects.
- Put optional event/packet/render behaviors behind `InitHook` implementors.
- Keep client-only classes under the client source set.

## Shared ID convention

Source: `HexicalMain.kt`

- `const val MOD_ID = "hexical"`
- `HexicalMain.id(path)` creates `Identifier(MOD_ID, path)`.
- Most registries, packets, shaders, and recipe types use this helper.

Reusable pattern:
- One ID helper prevents namespace drift.
- Keep IDs as stable strings because actions, recipes, docs, and lang entries refer to them.

## Centralized registry pattern

Sources:
- `inits/HexicalActions.kt`
- `inits/HexicalBlocks.kt`
- `inits/HexicalItems.kt`
- `inits/HexicalEntities.kt`
- `inits/HexicalIota.kt`

Hexical uses central registries rather than per-feature auto-discovery. This makes initialization explicit and searchable.

Strengths:
- Easy to audit all public features.
- Stable action signature list in one file.
- Hexdoc can extract action signatures with a regex from `HexicalActions.kt`.

Weaknesses:
- Large files grow with feature count.
- Merge conflicts become more likely.

Use when:
- A mod exposes many registry objects that must be stable and documented.

Avoid when:
- Feature modules are intended to be dynamically enabled/disabled.

## Hook aggregation pattern

Sources:
- `misc/InitHook.kt`
- `inits/HexicalHooksServer.kt`
- `client/inits/HexicalHooksClient.kt`

`InitHook` is a small abstract class with `init()`. Server and client hook registries collect hook objects then invoke all of them.

Examples:
- Server hooks: `ServerCharmedUseReceiver`, `TransmutingHelper`, `ConjureFloraHook`, `ZapManager`.
- Client hooks: `ClientConfettiReceiver`, `ClientShaderReceiver`, `PlayerAnimatorHook`, tooltips, overlays.

Reusable pattern:
- Feature-specific event registration lives beside feature code.
- The central init file remains a manifest of enabled hooks.

Caveat:
- Registration order matters if hooks depend on each other. Hexical does not encode dependencies, so dependent hooks should be registered explicitly and sparingly.

## Feature package pattern

Common layout is feature-first:

```text
features/<feature>/
  OpSomething.kt
  SomethingBlock.kt
  SomethingBlockEntity.kt
  SomethingItem.kt
  helpers/registries as needed
```

Client layout mirrors only client concerns:

```text
src/client/java/.../features/<feature>/
  SomethingRenderer.kt
  ClientSomethingReceiver.kt
  Tooltip/Model/Overlay hooks
```

Reusable pattern:
- Keep the gameplay concept as the package boundary.
- Keep registration in `inits`, behavior in `features`.
- Mirror common/client package names when a feature has both server and client halves.

## Client/server boundary pattern

Sources:
- `build.gradle`: `loom.splitEnvironmentSourceSets()`
- `fabric.mod.json`: separate client mixin config and client entrypoint.
- `client/inits/*`

Rules observed:
- Common code can define packet channel IDs, recipe types, server-side behavior, and entity/block logic.
- Client source set owns renderers, shaders, model loading, key polling, tooltips, overlays, and client packet receivers.
- Cross-boundary behavior is done with Fabric networking or synced entity/block entity data.

Risk:
- Referencing client classes from common code can crash dedicated servers. Hexical's split source sets reduce that risk.

## Mixin pattern

Sources:
- `src/main/resources/hexical.mixins.json`
- `src/client/resources/hexical.client.mixins.json`
- `mixin/WorldChunkMixin.java`
- client mixins such as `MouseMixin.java`, `GameRendererMixin.java`, `ScryingLensOverlaysMixin.java`

Patterns:
- Common mixins target gameplay/casting/block/entity behavior.
- Client mixins target rendering, GUI, input, and overlays.
- Mixin config separation matches source-set separation.

Reuse guidance:
- Prefer public Fabric/Hexcasting events where possible.
- Use mixins only for behavior that cannot be achieved through APIs.
- Keep attribution notes for Amber Seal/WorldChunkMixin due explicit Affinity credit.

## Data-driven feature pattern

Sources:
- `features/flora/ConjureFloraHook.kt`
- `features/transmuting/TransmutingHelper.kt`
- `datagen/providers/FloraProvider.kt`
- `datagen/providers/DyeingProvider.kt`

Hexical converts feature rules into recipe/data registries where practical. Examples:
- Conjure Flora uses a custom recipe type and generated `data/hexical/recipes/flora` entries.
- Transmuting uses a custom recipe type and generated `data/hexical/recipes/transmuting` entries.
- Dyeing uses generated data files mapping color families.

Reusable pattern:
- Put balancing/content lists in providers/data instead of hard-coding every case in spell code.
- Let docs pages consume the same provider-generated lists.

## New feature skeleton

For a new Hexcasting feature in our codebase, mirror the useful parts:

1. Create `features/<name>/Op<Name>.kt`.
2. Implement `SpellAction` or `ConstMediaAction`.
3. Validate stack inputs with Hexcasting helpers.
4. Validate world range and target existence before returning a result.
5. Put side effects in a `RenderedSpell`.
6. Register the action in a central action manifest with ID, pattern signature, and start direction.
7. Add any data/recipe types through a server hook if needed.
8. Add client receivers/renderers under the client source set only.
9. Add resource assets, lang keys, docs, and generated data.
10. Add tests or a manual verification checklist for the spell behavior.

## Completion status for this phase

Done. Startup, registration, hooks, package organization, source-set boundaries, mixins, and feature skeletons are documented with current source references.
