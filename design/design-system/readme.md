# design-system/ — Pandora Spec

This folder holds the **visual source of truth** for Pandora Spec (Material 3, seed `#2C3639`,
built for auto-mechanics: large touch targets, light + dark themes).

➡️ **The full design guide** — context, content fundamentals, visual foundations, iconography,
and the project index — lives in the **workspace-root `README.md`**. Read that first. The root
`CLAUDE.md` documents the folder map and working conventions.

## What's here
- `styles.css` — manifest of token `@import`s (the design-system entry point). The workspace
  root `styles.css` is a thin shim that re-exports this one, so the platform compiler can index
  every token and `@font-face`. Link either.
- `tokens/` — CSS custom properties:
  - `colors.css` — M3 color roles, light (`:root`) + dark (`[data-theme="dark"]`).
  - `typography.css` — font families, weights, full type scale.
  - `spacing.css` — spacing, radius, component sizes, elevation, motion.
  - `fonts.css` — Roboto / Roboto Mono / Material Icons webfonts.
- `guidelines/*.html` — foundation specimen cards (Colors, Type, Spacing, Brand) shown in the
  Design System tab.
- `handoff/*.md` — per-screen developer specs (e.g. `calculator.md`).
- `SKILL.md` — Agent Skill wrapper so this system is usable from Claude Code.

Components live in `../components/`, product screens in `../features/`, shared resources in
`../res/` — see the root `README.md` index.
