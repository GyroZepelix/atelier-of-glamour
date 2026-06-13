# Hexcasting Integration Patterns

## Action registration

Source: `src/main/java/miyucomics/hexical/inits/HexicalActions.kt`

Hexical registers actions with:

```kotlin
Registry.register(
  HexActions.REGISTRY,
  HexicalMain.id(name),
  ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), action)
)
```

The private `register(name, signature, startDir, action)` helper keeps public action IDs, pattern signatures, and implementations colocated. `doc/hexdoc.toml` then extracts these with a regex for generated documentation.

Reusable idea:
- A single action registry manifest doubles as code wiring and documentation source.

## Action implementation families

### `SpellAction` with `RenderedSpell`

Representative sources:
- `features/sparkle/OpSparkle.kt`
- `features/confetti/OpConfetti.kt`
- `features/magic_missile/OpMagicMissile.kt`
- `features/flora/OpConjureFlora.kt`
- `features/dyes/actions/OpDye.kt`

Pattern:
1. Declare `argc`.
2. Read typed iotas from the stack.
3. Validate range and target state.
4. Return `SpellAction.Result(spell, cost, particleSprays)`.
5. Perform world mutation inside `RenderedSpell.cast`.

Why this is useful:
- Execution planning and media accounting are separated from side effects.
- Validation failures become mishaps before world mutation.

### `ConstMediaAction`

Representative source:
- `features/shaders/OpShader.kt`

Pattern:
- No rendered spell is needed when the action is immediate, produces stack output, or triggers a controlled side effect.
- `OpShader` checks the caster is a player, sends a shader packet, and returns no iotas.

Use when:
- The operation does not need delayed rendered casting semantics.

### Stack-transforming rendered spell

Representative source:
- `features/conjure/OpConjureEntity.kt`

Pattern:
- The spell overrides `cast(env, image)` to mutate the casting image stack after spawning an entity.
- It appends an `EntityIota` for the spawned entity.

Use when:
- A spell both changes the world and needs to return a value discovered during casting.

## Input contracts from examples

### Sparkle

Source: `features/sparkle/OpSparkle.kt`

- Inputs: position vector, color vector, lifespan positive int <= 100.
- Validation: `env.assertVecInRange(position)`.
- Cost: `MediaConstants.DUST_UNIT / 100`.
- Output: none.
- Side effect: server spawns one custom particle effect at position.

Reusable pattern:
- Tiny visual spell with strict bounded inputs.

### Confetti

Sources:
- `features/confetti/OpConfetti.kt`
- `features/confetti/ConfettiHelper.kt`
- `src/client/.../features/confetti/ClientConfettiReceiver.kt`

- Inputs: position vector, then either number or velocity vector.
- Validation: position in range; speed <= 2; wrong type throws `MishapInvalidIota`.
- Cost: `MediaConstants.DUST_UNIT / 100`.
- Output: none.
- Side effect: server sends packet to nearby players; client plays sound and spawns particles.

Reusable pattern:
- Server-authoritative action, client-local visual expansion.

### Magic Missile

Sources:
- `features/magic_missile/OpMagicMissile.kt`
- `features/magic_missile/MagicMissileEntity.kt`
- `src/client/.../features/magic_missile/MagicMissileRenderer.kt`

- Inputs: relative spawn vector, velocity vector.
- Validation: resolved spawn position in range; rejects environments with no spatial axis by custom mishap.
- Cost: `MediaConstants.DUST_UNIT`.
- Output: none.
- Side effect: spawn a no-gravity projectile entity with owner, lifetime, hit damage, and client renderer.

Reusable pattern:
- For entity spells, put physics/combat behavior in the entity class and keep action code focused on spawn parameters.

### Dye

Sources:
- `features/dyes/DyeIota.kt`
- `features/dyes/actions/OpDye.kt`
- `features/dyes/DyeingUtils.kt`
- `features/dyes/entity/DyeingEntityRegistry.kt`

- Inputs: entity iota or position vector; dye iota.
- Validation: entity/block in range; dyeable registry lookup must succeed.
- Cost: `MediaConstants.DUST_UNIT / 8`.
- Output: none.
- Side effect: entity handler affects entity or block state is swapped while preserving shared properties.

Reusable pattern:
- Use a custom iota plus registries/handlers to keep one action generic across many target types.

### Conjure Flora

Sources:
- `features/flora/OpConjureFlora.kt`
- `features/flora/ConjureFloraHook.kt`
- `datagen/providers/FloraProvider.kt`

- Inputs: block position vector, identifier iota from Hexpose.
- Validation: block ID exists; custom recipe exists; target is replaceable or is compatible flower pot; support block is solid; tall plants have headroom.
- Cost: recipe-provided.
- Output: none.
- Side effect: plant block or potted variant.

Reusable pattern:
- Pair a generic action with a custom recipe type so content and balance are data-driven.

### Shader

Sources:
- `features/shaders/OpShader.kt`
- `features/shaders/ServerShaderManager.kt`
- `src/client/.../features/shaders/ClientShaderReceiver.kt`

- Inputs: none.
- Validation: caster must be a player.
- Cost: const media action default behavior, no explicit media in the file.
- Output: none.
- Side effect: server sends selected shader ID to the client.

Reusable pattern:
- Player-only spell that modifies a client-local presentation state through a server packet.

## Custom iota pattern

Source: `features/dyes/DyeIota.kt`

Pattern:
- Extend `Iota` with a typed payload.
- Define `IotaType` with `deserialize`, `display`, and `color`.
- Register in `HexicalIota.init()`.
- Provide stack helper functions such as `getDye` and `getColoredDye` that throw Hexcasting mishaps.

Reusable idea:
- Wrap domain values in iotas rather than passing loosely typed numbers/strings.

## Validation and mishap conventions

Observed practices:
- Use Hexcasting helpers such as `getVec3`, `getBlockPos`, `getEntity`, `getPositiveIntUnderInclusive`, and `getPositiveDoubleUnderInclusive`.
- Use `env.assertVecInRange`, `env.assertPosInRange`, and `env.assertEntityInRange` before side effects.
- Throw `MishapInvalidIota` for wrong input types or invalid values.
- Throw `MishapBadBlock` for invalid world targets.
- Throw custom mishaps for domain-specific failures, such as no spatial axis in Magic Missile.

## Action implementation checklist

- Define exact stack inputs and output iotas before coding.
- Use typed getters and fail with mishaps, not silent returns.
- Perform range checks for every position/entity target.
- Make media cost explicit or document why the action uses const/default media behavior.
- Put world mutation in `RenderedSpell.cast` unless there is a specific reason not to.
- For entity-spawning spells, set owner and tracking/rendering explicitly.
- For data-driven spells, put content lists in recipes/providers and keep action code generic.
- Add generated/static Patchouli pages with input/output descriptions.
- Link action ID to docs and lang keys.

## Completion status for this phase

Done. Simple visual, packeted visual, entity, data-driven block, custom iota, and client-state spell patterns are traced to source files.
