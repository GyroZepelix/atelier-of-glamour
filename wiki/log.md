# Wiki Log

Curated append-only timeline of durable wiki maintenance events. This is not a codebase changelog, commit log, or session transcript.

Use this shape for new entries:

- Heading: `## [YYYY-MM-DD] <kind> | <short title>`.
- Trigger: why the wiki was updated.
- Inputs: source paths, commit ranges, specs, ADRs, URLs, or raw files used as evidence.
- Wiki pages changed: wiki files changed.
- Verification: checks or source verification.
- Notes: gaps, conflicts, stale areas, or exceptions.

## [2026-06-20] install | repo wiki template

- Trigger: user requested installation from `https://git.dgjalic.com/dgjalic/repo-wiki-template`.
- Inputs: template `install/template/` from `https://git.dgjalic.com/dgjalic/repo-wiki-template`; existing `specs/` tree.
- Wiki pages changed: `wiki/AGENTS.md`, `wiki/index.md`, `wiki/log.md`, `wiki/state.md`, `wiki/raw/README.md`, `wiki/references/hexical/*.md`.
- Verification: required files exist and existing files were preserved or migrated; `llms.txt` and template research/implementation specs were not installed.
- Notes: installed payload also created or merged root and spec files; initial codebase ingest still needed.
