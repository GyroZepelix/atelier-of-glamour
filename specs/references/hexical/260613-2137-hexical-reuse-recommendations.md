# Hexical Reuse Recommendations

## Basis

- Repository: https://github.com/miyucomics/hexical
- Commit analyzed: `8a7b0f04c03830eee158efb3760a1cc0179302d6`
- License: MIT.
- Attribution-sensitive area: Amber Seal and `WorldChunkMixin.java` are explicitly credited to Affinity in Hexical README and LICENSE.

These recommendations prioritize patterns we can learn from and reimplement cleanly. They do not recommend direct copying unless explicitly reviewed for license attribution and compatibility.

## Adopt now

### Explicit registry manifests

Source evidence:
- `HexicalMain.kt`
- `HexicalClient.kt`
- `inits/HexicalActions.kt`
- `inits/HexicalBlocks.kt`
- `inits/HexicalItems.kt`
- `inits/HexicalEntities.kt`

Recommendation:
- Adopt the pattern of one explicit manifest per registry category.

Why:
- It is easy to audit, easy to document, and avoids hidden auto-discovery behavior.

Implementation effort:
- Low.

Dependency impact:
- None beyond Fabric/Hexcasting already needed.

Attribution:
- Not needed if independently implemented.

### Feature-local hooks with central hook lists

Source evidence:
- `misc/InitHook.kt`
- `inits/HexicalHooksServer.kt`
- `client/inits/HexicalHooksClient.kt`

Recommendation:
- Adopt a tiny hook interface/abstract type and register feature hook objects centrally.

Why:
- Keeps event/packet setup near feature code without making the entrypoint large.

Implementation effort:
- Low.

Dependency impact:
- None.

Attribution:
- Not needed if independently implemented.

### Hexcasting action checklist

Source evidence:
- `features/sparkle/OpSparkle.kt`
- `features/confetti/OpConfetti.kt`
- `features/dyes/actions/OpDye.kt`
- `features/flora/OpConjureFlora.kt`

Recommendation:
- Adopt the validation order: typed iota getters, range checks, domain checks, `RenderedSpell` side effects.

Why:
- It reduces partial side effects and makes mishaps predictable.

Implementation effort:
- Low.

Dependency impact:
- Hexcasting API.

Attribution:
- Not needed if independently implemented.

## Adapt with changes

### Server action, compact client visual packet

Source evidence:
- `features/confetti/OpConfetti.kt`
- `features/confetti/ConfettiHelper.kt`
- `src/client/.../features/confetti/ClientConfettiReceiver.kt`

Recommendation:
- Adapt the pattern for cosmetic effects: server validates/charges, client expands compact packet into particles/sound.

Changes to make:
- Use explicit payload types or a fresh buffer per recipient depending on our Fabric version.
- Add null/world guards in client receivers.
- Keep all client effect classes in the client source set.

Implementation effort:
- Medium.

Dependency impact:
- Fabric networking.

Attribution:
- Not needed if independently implemented.

### Data-driven spell content

Source evidence:
- `features/flora/ConjureFloraHook.kt`
- `features/flora/OpConjureFlora.kt`
- `features/transmuting/TransmutingHelper.kt`
- `datagen/providers/FloraProvider.kt`
- `datagen/providers/DyeingProvider.kt`

Recommendation:
- Adapt custom recipe/provider patterns for spells with large content tables.

Changes to make:
- Define smaller schemas first.
- Write docs from the same provider data.
- Avoid creating recipe types for one-off behavior.

Implementation effort:
- Medium.

Dependency impact:
- Fabric datagen and recipe serializers.

Attribution:
- Not needed if schema/code are ours.

### Custom iotas for domain values

Source evidence:
- `features/dyes/DyeIota.kt`
- `inits/HexicalIota.kt`

Recommendation:
- Adapt this when our spell language needs a repeated domain value with serialization/display behavior.

Changes to make:
- Define display color/text and serialization deliberately.
- Add helper getters that throw project-specific mishaps.

Implementation effort:
- Medium.

Dependency impact:
- Hexcasting API.

Attribution:
- Not needed if independently implemented.

### Media storage surfaces

Source evidence:
- `features/media_jar/MediaJarBlock.kt`
- `features/media_jar/MediaJarBlockEntity.kt`
- `features/media_jar/MediaJarItem.kt`
- `features/transmuting/TransmutingHelper.kt`

Recommendation:
- Adapt the block/item storage continuity pattern only if we need placeable and carried media storage.

Changes to make:
- Design our own NBT schema.
- Add explicit sync/update semantics.
- Test break/place/carry/refill flows.

Implementation effort:
- Medium to high.

Dependency impact:
- Hexcasting media holder API and block entity syncing.

Attribution:
- Not needed if independently implemented.

## Learn only

### Amber Seal block entity wrapping

Source evidence:
- `features/amber_seal/OpAmberSeal.kt`
- `features/amber_seal/AmberSealBlock.kt`
- `features/amber_seal/AmberSealBlockEntity.kt`
- `features/amber_seal/DanglingAmberState.kt`
- `mixin/WorldChunkMixin.java`
- `README.md`
- `LICENSE`

Recommendation:
- Learn the lifecycle risks, but do not adopt unless arbitrary block entity capture/restoration becomes a core requirement.

Why:
- It relies on a mixin around block entity construction and a queued dangling state.
- Hexical explicitly credits Affinity for this area.
- It is powerful but fragile and likely version-sensitive.

Implementation effort:
- High.

Dependency impact:
- Mixins into Minecraft internals.

Attribution:
- Yes, if copied or substantially adapted.

### Player input mirroring

Source evidence:
- `src/client/.../features/telepathy/ClientPeripheralPusher.kt`
- `features/telepathy/ServerPeripheralReceiver.kt`

Recommendation:
- Learn the transition-based packet approach, but avoid implementing until a server-side spell genuinely needs live input state.

Why:
- It adds persistent server state, custom packets, and input edge cases.

Implementation effort:
- Medium.

Dependency impact:
- Fabric networking and keybinding APIs.

Attribution:
- Not needed if independently implemented.

### Shader spell effects

Source evidence:
- `features/shaders/OpShader.kt`
- `features/shaders/ServerShaderManager.kt`
- `src/client/.../features/shaders/ClientShaderReceiver.kt`
- `client/inits/HexicalRenderLayers.kt`

Recommendation:
- Learn the cleanup lifecycle and client state model. Only implement when shader effects are central to our feature direction.

Why:
- Shaders add asset, compatibility, and client-state complexity.

Implementation effort:
- Medium to high.

Dependency impact:
- Client renderer/shader APIs.

Attribution:
- Not needed if independently implemented.

## Avoid for now

### Kitchen-sink feature breadth

Source evidence:
- 50 common feature directories and 25 client feature directories at the analyzed commit.

Recommendation:
- Do not imitate Hexical's breadth early.

Why:
- Hexical is intentionally a passion-project/kitchen-sink addon. That is appropriate for Hexical but risky as a planning model for our own codebase.

Alternative:
- Select one or two coherent mechanics and build them deeply.

### Large action registry without grouping if our action count grows

Source evidence:
- `HexicalActions.kt` contains a long list of action registrations.

Recommendation:
- Use the manifest pattern, but if our action count grows, group actions by feature with clear sections or generated docs extraction that supports multiple files.

Why:
- A single large file is searchable, but it can become conflict-prone.

## Proposed proof-of-concept tasks

### POC 1: Minimal action registry and one safe spell

Goal:
- Implement one small `SpellAction` using the extracted checklist.

Scope:
- Action manifest.
- One action with typed inputs, range check, media cost, and `RenderedSpell` side effect.
- One docs entry with input/output notes.

Success criteria:
- Action registers and can be found by ID.
- Invalid inputs produce mishaps before side effects.
- Server and client both start without classloader errors.

### POC 2: Cosmetic packet effect

Goal:
- Implement a compact server-to-client visual effect based on the confetti pattern.

Scope:
- Common packet channel ID.
- Server sender from a controlled trigger.
- Client receiver under client source set.
- Particle/sound expansion client-side.

Success criteria:
- Dedicated server does not load client classes.
- Packet is sent only to relevant players.
- Disconnect/world-null cases do not crash.

### POC 3: Data-driven recipe-backed spell or interaction

Goal:
- Implement a small custom data schema and consume it from one action or block interaction.

Scope:
- Recipe/data type.
- Datagen provider.
- Runtime lookup helper.
- Generated docs page from the same provider data.

Success criteria:
- Adding one data entry changes behavior and docs without code changes.
- Missing data produces a clear mishap or pass result.

## Legal and attribution guidance

- Hexical is MIT licensed, so reuse is permitted if license terms are followed.
- Preserve Hexical copyright/license notices for copied substantial code.
- Preserve Affinity attribution if copying or substantially adapting Amber Seal/block entity wrapping code.
- Prefer independent reimplementation of patterns over copying code.
- Record source commit and source files in our implementation notes for any adapted logic.

## Final recommendation

Adopt Hexical's organization and Hexcasting action discipline first. Adapt data-driven spell content and compact client visual packets when needed. Treat Amber Seal and shader systems as advanced references, not first targets.
