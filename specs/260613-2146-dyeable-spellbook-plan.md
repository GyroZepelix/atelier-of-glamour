# Dyeable Hexcasting Spellbook Plan

## Goal

Add an Atelier of Glamour item that behaves like Hexcasting's `hexcasting:spellbook`, but renders with an Atelier texture that can be dyed by vanilla dyes.

Proposed id: `atelier-of-glamour:dyeable_spellbook`.

## Findings from current codebase

- The project is a Fabric 1.20.1 Java mod using official Mojang mappings.
- Hexcasting 0.11.3 is already a dependency.
- The mod currently only has template entrypoints and no item registry layer yet.
- Hexcasting's `ItemSpellbook` is public and implements `IotaHolderItem` and `VariantItem`.
- Hexcasting Cardinal Components attach to any item implementing `IotaHolderItem` and `VariantItem`, so a subclass will get Hexcasting iota/variant components.
- Hexcasting shift-scroll and seal crafting use exact `HexItems.SPELLBOOK` checks, so subclassing alone is not enough for complete spellbook behavior.

## Implementation outline

### 1. Add mod item registration

Create `com.dgjalic.registry.AtelierItems` with:

- `public static final DyeableSpellbookItem DYEABLE_SPELLBOOK`
- helper registration against `BuiltInRegistries.ITEM`
- `register()` called from `AtelierOfGlamour.onInitialize()`
- creative tab entry insertion, preferably into Hexcasting's main creative tab if its public API is usable; otherwise add to a vanilla tab or create a small Atelier tab later

### 2. Add the item class

Create `com.dgjalic.item.DyeableSpellbookItem`:

- extend `at.petrak.hexcasting.common.items.storage.ItemSpellbook`
- implement `net.minecraft.world.item.DyeableLeatherItem`
- use `new Item.Properties().stacksTo(1)` to match Hexcasting's unstackable spellbook
- keep Hexcasting spellbook NBT keys untouched so pages, names, selected page, sealed pages, visual override, and variant data remain compatible
- override the default leather color to white (`0xFFFFFF`) instead of vanilla leather brown

This gives:

- Hex iota read/write behavior
- sealed-page behavior
- page rotation static methods compatibility
- vanilla dye crafting through Minecraft's existing special armor dye recipe, because the item implements `DyeableLeatherItem`

### 3. Register client predicates and color provider

In `AtelierOfGlamourClient.onInitializeClient()` register item property functions for the custom item:

- `hexcasting:overlay_layer`: copy Hexcasting's logic:
  - `0` when no selected page iota and no visual override
  - `1` when filled/unsealed or visually overridden/unsealed
  - `2` when filled/sealed or visually overridden/sealed
- no `hexcasting:variant` predicate is needed because the item intentionally has one visual variant

Register an item color provider:

- tint layer 0 with `DyeableLeatherItem#getColor(stack)` for the dyeable cover texture
- tint layer 1 with a dedicated Hex iota/visual overlay color helper, because `DyeableSpellbookItem#getColor(stack)` must return the dye color for vanilla dye support
- return white for all other layers

Do not reuse Hexcasting's `makeIotaStorageColorizer` directly because it only tints layer 1 and would not dye the cover.

### 4. Add resources

Add language and model resources under `src/main/resources/assets/atelier-of-glamour/`:

- `lang/en_us.json`
- `models/item/dyeable_spellbook.json`
- one filled model: `models/item/dyeable_spellbook_filled.json`
- one sealed model: `models/item/dyeable_spellbook_sealed.json`
- textures under `textures/item/`

Model strategy:

- use `minecraft:item/generated`
- keep Hexcasting predicate `hexcasting:overlay_layer`
- make layer 0 a grayscale/tintable Atelier cover texture
- make layer 1 the existing-style iota overlay texture, also grayscale/tintable by Hex's iota color

Decision: use one visible cover variant, not Hexcasting's 8 visual variants. Override `numVariants()` to `1` in the subclass and provide only one model set for the three states (`empty`, `filled`, `sealed`).

### 5. Make shift-scroll work

Hexcasting hard-codes scrollability to `HexItems.SPELLBOOK` and `HexItems.ABACUS`. Add mixins instead of copying the whole listener/network path:

Client mixin:

- target `at.petrak.hexcasting.client.ShiftScrollListener`
- inject into private static `IsScrollableItem(Item)` at `HEAD`, cancellable
- return `true` when the item is `AtelierItems.DYEABLE_SPELLBOOK`

Server mixin:

- target `at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S`
- inject into private `handleForHand(ServerPlayer, InteractionHand, double)` at `HEAD`, cancellable
- when the held item is `AtelierItems.DYEABLE_SPELLBOOK` and delta is nonzero, invoke the same private spellbook branch or replicate its page-rotation/status-message logic
- prefer a Mixin `@Invoker` for the private `spellbook(...)` method to avoid diverging behavior

This preserves Hexcasting's own packet, config options, and scroll behavior.

### 6. Make sealing work

Hexcasting's `hexcasting:seal_spellbook` recipe checks exactly `HexItems.SPELLBOOK`. Add a custom special recipe rather than modifying Hexcasting's recipe globally:

- create `DyeableSealSpellbookRecipe` extending `CustomRecipe`
- match exactly one unsealed `DYEABLE_SPELLBOOK` with iota data plus exactly one item from `hexcasting:seal_materials`
- assemble a copy of the custom spellbook, preserve all NBT including dye color, call `ItemSpellbook.setSealed(out, true)`, and return count 1
- register a recipe serializer, e.g. `atelier-of-glamour:seal_dyeable_spellbook`
- add `data/atelier-of-glamour/recipes/dynamicseal_dyeable_spellbook.json`

This makes sealed custom spellbooks behave like sealed Hexcasting spellbooks without changing the original recipe.

### 7. Add acquisition recipe

Decision: craft the custom spellbook from `hexcasting:spellbook` plus `minecraft:leather`. This keeps Hexcasting progression intact and makes the dyeable version an upgrade/variant of the base spellbook.

### 8. Verify

Run:

- `./gradlew build`
- datagen if resources are generated instead of handwritten
- in a dev client:
  - craft/acquire dyeable spellbook
  - dye it with vanilla dyes and confirm layer 0 changes color
  - write an iota into it like a normal spellbook
  - shift-scroll pages in main hand and off hand
  - seal a filled page and confirm writing is blocked
  - confirm tooltip, selected page name restoration, filled/sealed model predicates, and iota overlay color
  - confirm dye color persists through sealing and page scrolling

## Key risks

- Private Hexcasting method descriptors may differ under official mappings; inspect decompiled/remapped classes when implementing the mixins. Pin the supported Hexcasting range and retest before widening it.
- Client color layers must be ordered carefully: dye on layer 0, Hex iota color on layer 1.
- If only one visual variant is provided while retaining `ItemSpellbook.numVariants() == 8`, models for missing variants will render as missing textures. Either provide all 8 model sets or override `numVariants()` to `1`.
- Vanilla dye recipe will only work if `DyeableLeatherItem` is implemented and the tinting provider uses layer 0.
