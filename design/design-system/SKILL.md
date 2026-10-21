---
name: pandora-spec-design
description: Use this skill to generate well-branded interfaces and assets for Pandora Spec (SpeCMP) — the Compose Multiplatform car-alarm installation app for Android, iOS and Desktop — either for production or throwaway prototypes/mocks. Contains essential design guidelines, Material 3 color roles, type scale, fonts, icon catalog, and the Cmp* UI-kit components for prototyping.
user-invocable: true
---

Read the `readme.md` file next to this skill first, then explore the other files. The
root `CLAUDE.md` documents the full workspace layout and conventions.

This is the **Pandora Spec** design system: Material 3, generated from seed `#2C3639`,
built for **auto-mechanics (30–50, low finger sensitivity)** — so large touch targets
(≥ 48 dp), no small elements, light + dark themes, Desktop hover/focus. UI copy is in
Russian, plain and instructional.

**Where things are** (workspace-relative)
- `design-system/styles.css` — link this to get all tokens + webfonts (root `styles.css` re-exports it).
- `design-system/tokens/` — color roles (light `:root`, dark `[data-theme="dark"]`), type scale,
  spacing, radius, elevation, motion.
- `components/<group>/` — React `Cmp*` primitives (app-kit, buttons, forms, surfaces, navigation,
  feedback, progress). Load `_ds_bundle.js` and read them from
  `window.PandoraSpecDesignSystem_b8d6e1` (e.g. `const { CmpButton } = window.PandoraSpecDesignSystem_b8d6e1`).
  Each component has a `.prompt.md` with usage.
- `features/calculator/` — full screens (the Калькулятор screen for mobile and the 800×600 desktop window).
- `design-system/guidelines/` — specimen cards for colors, type, spacing, brand.
- `res/nav.js` (cross-mockup nav), `changelog/`, `skills/` — see root `CLAUDE.md`.

**How to work**
- If creating visual artifacts (slides, mocks, throwaway prototypes): copy the assets/fonts
  you need and produce static HTML files for the user to view. Pick **roles**, not raw hex;
  pick **type roles**, not px; stay on the **4 dp grid**; keep hit areas **≥ 48 dp**; design
  for **both themes** and for **hover/focus** on Desktop. Use only **Material Icons (Filled)**
  from the `CmpIcons` catalog — never emoji or hand-rolled icons.
- If working on production code, copy assets and read the rules here to design accurately for
  this brand (the upstream code lives in `AlexNarbut/SpeCMP` @ `develop`).

If the user invokes this skill without further guidance, ask what they want to build, ask a
few clarifying questions, then act as an expert designer who outputs HTML artifacts **or**
production code, depending on the need.

## Креативная дирекция (обязательно)

Работай как креативный UX/UI-дизайнер уровня Apple. Интерфейс должен быть **актуальным,
живым, интересным и — главное — удобным**. Material 3 и `Cmp*` — это фундамент, а не потолок:
не оставляй экраны «голым дефолтным материалом».

- **Глубина и композиция.** Избегай плоских одно-тоновых заливок. Давай слоистый брендовый
  фон (мягкие радиальные «блики» из `--primary-container` / `--tertiary-container` поверх
  `--background`), «парящие» карточки контента (`--surface` + тень + тонкая граница), щедрый
  воздух и явную визуальную иерархию. Композиция, а не просто стек компонентов.
- **Бренд-присутствие.** Выраженная hero-зона: логотип/иконка приложения, крупный заголовок,
  спокойный подзаголовок. На Expanded — двухколоночный layout (бренд-рейл + карточка).
- **Живость с мерой.** Деликатные входные анимации (fade/slide-up), один чёткий акцент,
  крупные дружелюбные CTA. Анимации гасятся под `prefers-reduced-motion`. Это рабочий
  инструмент монтажника, а не маркетинговый лендинг — энергично, но без шума и слопа.
- **Рамки.** Только токены и роли (без сырых hex; гармоничные оттенки — через `color-mix`/oklch
  от палитры). Обе темы. Зоны касания ≥ 48 dp, крупная типографика. Иконки — из `CmpIcons`.
  Если нужен новый паттерн — собирай его из примитивов и помечай как кандидата в UI Kit.
