# Client, Rendering, and Networking Study

## Source-set split

Source: `build.gradle`

Hexical uses Fabric Loom `splitEnvironmentSourceSets()`. Common code is under `src/main`, and client-only code is under `src/client`.

Practical rule:
- Put any reference to Minecraft client classes, renderers, model loading, key polling, tooltips, and client packet receivers in `src/client`.
- Shared packet IDs and server senders can live in common code.

## Client initialization map

Source: `src/client/java/miyucomics/hexical/HexicalClient.kt`

Client init calls:
- `HexicalRenderLayers.clientInit()`
- `HexicalBlocksClient.clientInit()`
- `HexicalEntitiesClient.clientInit()`
- `HexicalHooksClient.init()`
- `HexicalKeybinds.clientInit()`
- `HexicalParticlesClient.clientInit()`

Then it registers a tick counter and two built-in item renderers.

Reusable pattern:
- Render layer/shader registration first.
- Block/entity renderers before arbitrary hooks.
- Keybinds and particles as explicit client init concerns.

## Server-to-client visual packet pattern

Sources:
- `features/confetti/OpConfetti.kt`
- `features/confetti/ConfettiHelper.kt`
- `src/client/.../features/confetti/ClientConfettiReceiver.kt`

Flow:
1. Spell validates inputs and returns rendered spell.
2. Rendered spell calls `ConfettiHelper.spawn` on the server.
3. Helper writes seed, position, direction, and speed into a packet.
4. Server sends to nearby players with `world.sendToPlayerIfNearby` and `ServerPlayNetworking.createS2CPacket`.
5. Client receiver reads values, switches onto the client thread with `client.execute`, plays sound, and expands 100 particles locally.

Key takeaways:
- Packet payload is compact.
- Client does cosmetic work.
- Sender limits recipients by distance.

Risk:
- The same `PacketByteBuf` is created once and reused in a loop. If adapting this pattern, verify Fabric's packet creation semantics for the target version or create a fresh buffer per recipient.

## Server-to-client state packet pattern

Sources:
- `features/shaders/ServerShaderManager.kt`
- `features/shaders/OpShader.kt`
- `src/client/.../features/shaders/ClientShaderReceiver.kt`

Flow:
1. Player-only spell calls `ServerShaderManager.setShader`.
2. Server writes shader ID string to `hexical:shader`.
3. Client receiver clears effect if string is `"null"`; otherwise it applies the shader ID.
4. Disconnect event clears the shader.
5. Server respawn event clears shader after death.

Reusable pattern:
- Client-local state should have cleanup on disconnect and respawn/death.

Caveat:
- `setShader(player, null)` serializes `shader.toString()` even when null in the source as read. Kotlin nullable `toString()` resolves to `"null"`, and the client special-cases that string. For our code, prefer an explicit boolean or optional encoding.

## Client-to-server input mirroring

Sources:
- `src/client/.../features/telepathy/ClientPeripheralPusher.kt`
- `features/telepathy/ServerPeripheralReceiver.kt`

Flow:
1. Client tick checks movement/action/custom keybind states.
2. Only transitions send packets.
3. Press and release use separate channels.
4. Scroll sends an integer only when telepathy key is held.
5. Server updates per-player key maps and invokes evocation lifecycle hooks for the evocation key.

Reusable pattern:
- Send transitions instead of polling full state server-side.
- Store server state on player-attached fields/components.

Risk:
- Packet spam is bounded by transitions, but key-list growth must stay controlled.

## Block and item rendering

Sources:
- `client/inits/HexicalBlocksClient.kt`
- `client/inits/HexicalRenderLayers.kt`
- `src/client/.../features/media_jar/MediaJarRenderer.kt`
- `src/client/.../features/amber_seal/AmberSealBlockEntityRenderer.kt`
- `HexicalClient.kt`

Observed surfaces:
- Cutout render layers for Amber Seal, Media Jar, Periwinkle.
- Block entity renderers for Amber Seal and Media Jar.
- Built-in dynamic item renderers for Amber Seal and Media Jar items.
- Scrying Lens overlay displayer for Media Jar media amount.
- Custom render layer using a registered shader program and Perlin/noise texture.

Reusable pattern:
- Keep renderer registration in one client block initializer.
- Put reusable drawing code in a feature renderer object.
- Use specialized Hexcasting client APIs, such as Scrying Lens overlay registry, where available.

## Entity rendering

Sources:
- `inits/HexicalEntities.kt`
- `client/inits/HexicalEntitiesClient.kt`
- `src/client/.../features/magic_missile/MagicMissileRenderer.kt`
- `features/magic_missile/MagicMissileEntity.kt`

Pattern:
- Common source registers `EntityType` with dimensions/tracking.
- Client source registers renderer.
- Entity owns behavior and status effects; renderer owns texture choice.

Use when:
- Effects persist, collide, or need tracking.

Avoid when:
- A one-shot particle packet is enough.

## Particles

Sources:
- `features/sparkle/OpSparkle.kt`
- `src/client/.../features/sparkle/SparkleParticle.kt`
- `src/client/.../features/confetti/ConfettiParticle.kt`
- `client/inits/HexicalParticlesClient.kt`

Pattern:
- Common code can spawn particle effects on the server world when particle type/data are registered.
- Client source registers factories for rendering particle instances.
- Complex cosmetic bursts use custom networking to avoid server particle load.

## Player animation

Source: `src/client/.../features/player/PlayerAnimatorHook.kt`

Pattern:
- Register animation layers through Player Animator's client API.
- Use modifier layers and mirror modifiers to respect main/off hand.
- Store per-player associated animation layer data when other feature code needs to trigger animation.

Use when:
- Items/keybinds require visible third-person animation.

## Dedicated-server safety checklist

- Do not import `net.minecraft.client.*` in common source files.
- Define packet channel IDs in common only if both sides need them.
- Register client packet receivers only in `src/client` hooks.
- Register entity types in common and entity renderers in client.
- Register block/entity logic in common and block entity renderers in client.
- Keep mixin configs split by environment.
- For client state effects, add disconnect/death cleanup.

## Completion status for this phase

Done. Server-to-client visual packets, state packets, client-to-server input packets, block/entity/item renderers, particles, shaders, overlays, and client-only boundaries are documented.
