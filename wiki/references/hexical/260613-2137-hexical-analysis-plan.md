# Hexical Analysis and Pattern Extraction Plan

## Goal

Analyze https://github.com/miyucomics/hexical to extract reusable knowledge, implementation patterns, and design principles that can inform our own Hexcasting-related work without blindly copying code.

## Source baseline

- Repository: https://github.com/miyucomics/hexical
- Current upstream snapshot checked: main branch, latest observed commit `8a7b0f04c03830eee158efb3760a1cc0179302d6`
- License: MIT, with explicit attribution requirements and noted Affinity-derived portions
- Technology stack observed:
  - Minecraft 1.20.1
  - Fabric Loom
  - Kotlin 2.2.21 and Java 17
  - Hexcasting, Hexpose, Patchouli, EMI, Cardinal Components, Fabric API, Fabric Language Kotlin, Player Animator

## Ground rules

- Treat Hexical as a learning reference, not a code source to paste from.
- Record every copied or adapted implementation separately with license attribution before any reuse.
- Prefer extracting patterns, architecture, naming conventions, and integration techniques.
- Avoid importing feature behavior wholesale unless it directly matches our own product goals.
- Keep Affinity-derived areas flagged, especially amber seal and block entity wrapping/restoration logic.

## Analysis questions

1. How does Hexical organize many independent gameplay features while keeping initialization manageable?
2. How are Hexcasting actions, patterns, spell costs, mishaps, media handling, and validation represented?
3. How does it separate common, client-only, resource, data generation, and documentation concerns?
4. What patterns does it use for Minecraft/Fabric registration, mixins, networking, rendering, and block entities?
5. Which UX ideas improve Hexcasting play without reducing mechanical depth?
6. Which parts are robust reusable patterns, and which are one-off feature implementations?

## Work phases

### Phase 1: Repository map

Deliverable: `wiki/references/hexical/260613-2137-hexical-repository-map.md`

Tasks:
- Clone or fetch the upstream repository into a temporary research location.
- Capture top-level structure: Gradle files, `src/main`, `src/client`, `doc`, `misc`, workflows, resources.
- List feature directories under common and client source sets.
- Identify entry points such as mod initializers, client initializers, registration classes, and mixin config files.
- Build a dependency map from `build.gradle` and `gradle.properties`.

Acceptance criteria:
- We can explain where to look for actions, blocks, items, entities, renderers, networking, docs, and data assets.
- We know which dependencies are required for each major category.

### Phase 2: Architecture and initialization patterns

Deliverable: `wiki/references/hexical/260613-2137-hexical-architecture-patterns.md`

Tasks:
- Trace startup flow from mod entry points into registration/init modules.
- Document how features are grouped and registered.
- Identify conventions for IDs, registries, tags, resources, translation keys, and generated data.
- Separate stable reusable patterns from project-specific naming.

Acceptance criteria:
- A new feature skeleton can be described from package to registration to resource wiring.
- Any client/server boundary rules are documented.

### Phase 3: Hexcasting integration study

Deliverable: `wiki/references/hexical/260613-2137-hexical-hexcasting-patterns.md`

Tasks:
- Inventory Hexcasting actions and related classes.
- For representative spells, document:
  - Input stack contract
  - Output stack contract
  - Media cost model
  - Validation and mishap behavior
  - World interaction boundaries
  - Serialization or sync needs
- Compare simple actions, entity actions, item/block actions, and complex multi-step actions.
- Extract a template for implementing our own action safely.

Acceptance criteria:
- We have a checklist for adding a Hexcasting pattern/action.
- We know which Hexical abstractions are worth mirroring and which should stay unique to Hexical.

### Phase 4: Feature pattern catalog

Deliverable: `wiki/references/hexical/260613-2137-hexical-feature-pattern-catalog.md`

Tasks:
- Review representative feature families instead of every file equally:
  - Inventory and item utility features
  - Media storage and manipulation features
  - Entity/projectile/spell effect features
  - Block and block entity features
  - Rendering, particles, shaders, and overlays
  - Player animation and input features
  - Documentation and Patchouli/hexdoc integration
- For each family, capture:
  - Problem solved
  - Core classes and resources
  - Reusable pattern
  - Risks and hidden dependencies
  - Applicability to our project

Acceptance criteria:
- The catalog distinguishes architecture patterns from gameplay ideas.
- Each pattern has a short "use when" and "avoid when" note.

### Phase 5: Client, rendering, and networking study

Deliverable: `wiki/references/hexical/260613-2137-hexical-client-render-networking.md`

Tasks:
- Trace how server actions cause client effects, particles, overlays, animations, or sounds.
- Document packet/channel conventions and data payload shapes.
- Review renderer registration, block entity renderers, item renderers, entity renderers, and shader hooks.
- Identify safe client-only package boundaries.

Acceptance criteria:
- We can implement a server-triggered visual effect without leaking client-only classes into common code.
- We have examples of renderer lifecycle and registration order.

### Phase 6: Data, assets, and documentation pipeline

Deliverable: `wiki/references/hexical/260613-2137-hexical-docs-data-assets.md`

Tasks:
- Inspect `doc`, `src/main/resources`, generated data, lang files, models, textures, recipes, loot, tags, and Patchouli/hexdoc configuration.
- Document how gameplay code, translation keys, book pages, and generated documentation stay aligned.
- Identify any tooling scripts in `misc` that produce assets or visual resources.

Acceptance criteria:
- We have a repeatable docs/data checklist for our own features.
- Asset and localization requirements are visible before implementation starts.

### Phase 7: Reuse recommendations

Deliverable: `wiki/references/hexical/260613-2137-hexical-reuse-recommendations.md`

Tasks:
- Rank extracted patterns as:
  - Adopt now
  - Adapt with changes
  - Learn only
  - Avoid
- Include reason, implementation effort, dependency impact, and legal/attribution notes.
- Propose 1 to 3 small proof-of-concept tasks that apply the highest-value patterns in our codebase.

Acceptance criteria:
- The final document gives actionable next steps, not just observations.
- Every recommendation links back to source files or commits reviewed.

## Suggested research workflow

1. Create a temporary checkout outside our source tree.
2. Generate file inventories with paths, languages, and line counts.
3. Read build files and entry points first.
4. Follow one simple feature end to end.
5. Follow one complex feature end to end.
6. Generalize repeated structure into pattern notes.
7. Validate each pattern by checking at least two examples in the repository.
8. Write recommendations only after source evidence is captured.

## Evidence template

Use this structure for each extracted pattern:

```md
## Pattern name

- Source files:
  - `path/to/file.kt`
- Problem:
- Approach:
- Key constraints:
- Dependencies:
- Reusable idea:
- Do not copy:
- Applicability to our project:
- Attribution needed: yes/no
```

## Risks

- Upstream is actively changing, so pin findings to commit hashes.
- Some areas depend on Fabric, Minecraft, Hexcasting, or Hexpose internals and may not transfer cleanly.
- Client-only code can easily break dedicated servers if copied into common code.
- MIT license permits reuse, but attribution is required for substantial copied portions.
- Amber seal logic has an explicit Affinity influence and needs extra attribution review.

## Final output bundle

The completed research should produce:

- `wiki/references/hexical/260613-2137-hexical-repository-map.md`
- `wiki/references/hexical/260613-2137-hexical-architecture-patterns.md`
- `wiki/references/hexical/260613-2137-hexical-hexcasting-patterns.md`
- `wiki/references/hexical/260613-2137-hexical-feature-pattern-catalog.md`
- `wiki/references/hexical/260613-2137-hexical-client-render-networking.md`
- `wiki/references/hexical/260613-2137-hexical-docs-data-assets.md`
- `wiki/references/hexical/260613-2137-hexical-reuse-recommendations.md`

## Definition of done

- Each deliverable is backed by source file references.
- Each reusable pattern includes constraints, dependencies, and applicability.
- License and attribution notes are included where needed.
- At least one simple and one complex Hexical feature are traced end to end.
- Recommendations are prioritized and converted into small follow-up implementation tasks.
