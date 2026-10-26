# Pandora Spec — Design System

A self-contained design system for **Pandora Spec** (codename **SpeCMP**), a
Compose Multiplatform application that guides the **installation of Pandora car
alarm systems** on **Android, iOS and Desktop** from a single codebase.

> **Primary users — auto-mechanics, 30–50, often with reduced finger sensitivity.**
> This is the defining constraint of the whole system: **no small elements**, generous
> hit areas (≥ 48 dp), large legible type, and high-contrast role colors in both light
> and dark themes (workshops are bright *and* dim).

The system is Material 3 at its core (`Cmp*` components are thin, brand-tuned wrappers
over Material 3), generated from a single brand seed color **`#2C3639`**.

## Sources

Everything here was reverse-engineered from real product source — explore these to build
more accurate designs (access may be required):

- **GitHub:** `AlexNarbut/SpeCMP` @ branch `develop` — the Compose Multiplatform app.
  - Design system: `core/ui/src/commonMain/.../ui/` (theme + `Cmp*` view components).
  - Docs: `docs/ui-kit.md` (component map & rules), `CLAUDE.md` (architecture).
  - Feature: `feature/calculator/.../CalculatorScreen.kt` (the only feature screen so far).
- **Spec:** `uploads/design-system.md` — the designer's workbook (tokens, scales, rules).

---

## Content fundamentals

The product UI is **in Russian**, written for working professionals.

- **Tone:** plain, instructional, no marketing fluff. Speak to the task ("Деление на ноль
  невозможно", "Выберите точку", "Сохранить"). The mechanic is mid-job — clarity beats
  cleverness.
- **Voice:** neutral/impersonal. Actions are imperatives ("Сохранить", "Старт", "Сброс",
  "Удалить"). Avoid "вы/ты" framing; label the action, not the user.
- **Casing:** sentence case for titles and body ("Калькулятор", "Точка подключения").
  Button labels are sentence/word case in M3 (not ALL-CAPS) — e.g. "Сохранить", "Отмена".
- **Numbers & units:** technical and tabular — currents in A, time as `mm:ss` / `hh:mm:ss`,
  VINs, connector pins. Set figures in **Roboto Mono** so columns align.
- **Errors:** state the problem directly and briefly ("Введите число", "Деление на ноль
  невозможно"). Surface transient ones in a **snackbar**, validation ones as field
  **supportingText** in the error role.
- **Emoji:** none. Iconography is Material Icons only. Glyphs like `÷ × − ⌫` appear inside
  the calculator keypad as content, not decoration.
- **Vibe:** a precise, trustworthy workshop instrument — like a good multimeter. Calm
  surfaces, one clear primary action per screen, nothing flashy.

Examples (verbatim from the codebase): `Калькулятор`, `Назад`, `Старт`, `Стоп`, `Сброс`,
`Деление на ноль невозможно`.

---

## Visual foundations

**Palette.** Material 3 role-based color generated from seed `#2C3639` (a dark slate).
Designers never pick raw hex — they pick a **role** (`primary`, `surface`, `error`,
`secondaryContainer`, …) and the matching `on*` role for text/icons on top. Every role has
a light and a dark value so contrast is correct automatically.
- Light: neutral light-grey chrome — background `#ECECEC`, surface `#F3F3F3`, near-black ink
  `#262626`, muted-grey secondary text `#8E8E8E`, **navy-blue accent** `#1A3E7A` (line icons / CTAs).
- Dark: desaturated **dark-navy** background `#1F2636`, near-white ink `#ECEEF2`, blue-grey
  secondary text `#9AA4B8`, **bright-blue accent** `#4AA3EA`.
- Accents: secondary a steel blue (`#3F5A82` / container `#DAE3F3`), tertiary a teal
  (`#2C6A6E` / container `#C9EAEC`), error red (`#C62828`). Used sparingly — secondaryContainer
  tints operator keys, primaryContainer the promoted action.
- Domain tokens: `--success` (`#79BD5B`) and the wiring-bus family colors `--bus-can`, `--bus-canfd`, `--bus-lin`, `--bus-bean`, `--bus-imo`, `--bus-unknown`
  — one hue per bus family (CAN blue, CAN FD teal, LIN purple, BEAN orange, IMO-KEY green, unknown = neutral dashed outline); the bus number is always in the chip label.

**Surfaces & depth.** A 5-step *surface container* ladder (Lowest → Highest) expresses
elevation **by tone** — here **derived** from `surface`→`onSurface` via `color-mix`, since
the source export doesn't ship the container roles. Complemented by a restrained shadow
scale. Default card = tone + **1 dp** shadow; dialogs and FABs go higher. Backgrounds are
**flat** — no gradients, no photographic hero imagery, no textures or patterns. The
aesthetic is clean Material, not decorated.

**Type.** **Roboto** (the Material default — no custom face ships in the codebase), with
**Roboto Mono** for figures/timers. Full Material 3 type scale: display (57/45/36),
headline (32/28/24), title (22/16/14 · Medium), body (16/14/12 · Regular), label
(14/12/11 · Medium). Designers pick a **role**, not a px size. On 1920-scale artwork keep
body ≥ a comfortable size; in-product minimum touch text is bodyMedium 14.

**Shape.** Corner-radius scale: xs 4 · s 8 · **m 12** (cards, dropdowns, selectable cards)
· l 16 (bottom-sheet top) · **xl 28** (dialogs). Buttons are full **stadium** (pill).
Base surface is square (0 dp) by default.

**Spacing & sizing.** Strict **4 dp grid**: xs 4 · s 8 · m 12 · l 16 · xl 24 · xxl 32.
Fixed sizes: button 40, **touch target 48**, field 56, top bar 64, nav bar 80, icon 24,
FAB 40/56/96. The 48 dp minimum is non-negotiable (mechanics' fingers).

**Borders & dividers.** 1 dp dividers in `outlineVariant`; field/outline borders in
`outline`, going **`primary` (2 dp) on focus** and **`error`** on error. The selectable
card uses a **1.5 dp** border that animates between `outline` and `primary`.

**Cards.** Rounded (12 dp), `surface` fill, soft 1 dp shadow, no accent borders. Selectable
cards add a 16 dp inner pad, a 1.5 dp border, and a 300 ms color crossfade to
`primaryContainer` when selected. No "colored-left-border" cards, no gradient cards.

**Motion.** Subtle and functional. Standard transition ≈ **300 ms** emphasized easing
(`cubic-bezier(0.2,0,0,1)`); fast interactions ≈ 150 ms. State changes crossfade color;
no bounces, no decorative looping animation. Progress indicators are the main animated
elements.

**Interaction states (all interactive components).** Material 3 **state layers**:
hover ≈ 8 %, focus ≈ 10 %, pressed ≈ 12 % overlay of the relevant `on*` color. Selected =
`primary` accent / `primaryContainer` fill. Disabled = 12 % surface fill + 38 % content.
**Hover and focus are first-class** because Desktop is a target (mouse + keyboard).

**Transparency & blur.** Used only for the modal **scrim** (`scrim` at ~40 %); no glassy
blur effects elsewhere. Imagery, when present, is not part of the core system — the look is
solid color on flat surfaces.

---

## Iconography

- **System:** **Material Icons (Filled)** — the exact set the codebase resolves via
  `CmpIcons` → `Icons.Filled.*` (see `CmpIconsResolver.kt`). Loaded here from the Google
  **Material Icons** webfont so glyphs match 1:1.
- **Catalog only:** icons come from a fixed enum catalog (`CmpIcons`), never ad-hoc. The
  full mapping (catalog name → ligature) lives in `components/app-kit/CmpIcon.jsx`
  (`CMP_ICONS`). Use `<CmpIcon icon="ArrowBack" />` etc.
- **Default size 24 dp**, color inherits content (`currentColor`). Every meaningful icon
  gets an accessible label (`title` / `contentDescription`); decorative ones are
  `aria-hidden`.
- **Groups:** Navigation, Actions, Content, Communication, User, Status, Places, Other —
  ~60 icons (ArrowBack, Build, Settings, Search, Delete, Warning, Lock, Notifications, …).
- **No emoji**, no Unicode-as-icon (the keypad's `÷ × − ⌫` are content glyphs, not the icon
  system). Brand/custom icons, when needed, are added to the catalog as vector assets.
- **Logo:** the codebase ships **no logo asset**. The Brand cards use a typographic
  wordmark + the `build` glyph as a placeholder lockup. **→ Please provide the real Pandora
  Spec logo** so it can be added to `assets/`.

---

## Index — project layout

This is the canonical design guide for the workspace (the root `CLAUDE.md` documents the
full folder map and working conventions). Paths below are workspace-relative.

**Design system** (`design-system/` — source of truth for the visual language)
- `design-system/styles.css` — manifest of token `@import`s. The workspace root also has a
  thin `styles.css` shim that re-exports it (platform requirement — link either).
- `design-system/tokens/colors.css` — M3 color roles, light (`:root`) + dark (`[data-theme="dark"]`).
- `design-system/tokens/typography.css` — font families, weights, full type scale.
- `design-system/tokens/spacing.css` — spacing, radius, sizes, elevation, motion.
- `design-system/tokens/fonts.css` — Roboto / Roboto Mono / Material Icons webfonts.
- `design-system/guidelines/*.html` — specimen cards (Colors, Type, Spacing, Brand) for the Design System tab.
- `design-system/handoff/*.md` — per-screen dev specs.
- `design-system/readme.md` (folder doc) · `design-system/SKILL.md` (Agent Skill wrapper).

**Components** (`components/<group>/` — React `Cmp*` primitives, namespace
`window.PandoraSpecDesignSystem_b8d6e1`)
- `app-kit/` — CmpText, CmpIcon (+ `CMP_ICONS`), `useStateLayer`, `PhoneFrame` (device bezel)
- `buttons/` — CmpButton, CmpOutlinedButton, CmpTextButton, CmpIconButton,
  CmpFloatingActionButton
- `forms/` — CmpTextField, CmpDropdown, CmpSwitch, CmpCheckbox, CmpRadioButton, CmpSlider
- `surfaces/` — CmpCard, CmpSelectableCard, CmpSurface, CmpDivider
- `navigation/` — CmpTopAppBar, CmpNavigationBar, CmpTabRow
- `feedback/` — CmpDialog, CmpSnackbar, CmpBadge (+ CmpBadgedBox), CmpBottomSheet
- `progress/` — CmpCircularProgressIndicator, CmpLinearProgressIndicator

**Features** (`features/<feature>/` — product screens)
- `calculator/` — `Calculator.jsx` (the **Калькулятор** screen, real logic) +
  `Calculator.html` (mobile, Android frame) + `CalculatorDesktop.html` (800 × 600 window).

**Shared & process**
- `res/nav.js` — cross-mockup navigation (`window.psGo('calculator')`).
- `res/icons/cmp-icons.js` — CmpIcons catalog for bundle-less prototypes.
- `index.html` (root) — entry: device frame + theme switch.
- `changelog/` — mockup change history (drafts in buckets → merged final entries).
- `skills/` — project skills: merge-changelog, import-by-changelog, export-handoff.
- `_ds_bundle.js`, `_ds_manifest.json` (workspace root) — generated automatically; do not edit.

---

## Working with this system

Pick a **role**, not a hex. Pick a **type role**, not a px. Stay on the **4 dp grid**.
Keep hit areas **≥ 48 dp**. Build every screen for **both themes**, and design **hover +
focus** for Desktop. Use only catalog icons. One clear primary action per screen.
