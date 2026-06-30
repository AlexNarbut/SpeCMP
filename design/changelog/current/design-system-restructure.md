# 2026-06-30 — Дизайн-система Pandora Spec + реструктуризация под workspace-раскладку

**Экраны:** calculator
**Файлы:**
- styles.css, design-system/styles.css, design-system/tokens/colors.css, design-system/tokens/typography.css, design-system/tokens/spacing.css, design-system/tokens/fonts.css
- design-system/guidelines/*.html
- components/app-kit/*, components/buttons/*, components/forms/*, components/surfaces/*, components/navigation/*, components/feedback/*, components/progress/*
- features/calculator/Calculator.jsx, features/calculator/Calculator.html, features/calculator/CalculatorDesktop.html
- res/nav.js, res/icons/cmp-icons.js, index.html, CLAUDE.md
- skills/merge-changelog, skills/import-by-changelog, skills/export-handoff

## Что изменилось
- Создана дизайн-система на основе Material 3 (seed `#2C3639`): токены цветов (светлая/тёмная),
  типографики (Roboto), спейсинга/радиусов/высот, шрифты (Roboto + Material Icons).
- Цветовая палитра подобрана по реальным скриншотам приложения (Pandora Спец): светлая —
  нейтральный светло-серый chrome + тёмно-синий акцент `#1A3E7A`; тёмная — десатурированный
  тёмно-синий фон `#1F2636` + яркий синий акцент `#4AA3EA`. Лестница `surfaceContainer*` выводится
  через `color-mix`. Добавлены доменные токены `--success` и цвета шин `--bus-can1…--bus-unknown`
  (CAN/LIN). Обновлены hex-подписи в гайдлайнах и бренд-ссылки.
- Собраны 28 компонентов `Cmp*` (app-kit, buttons, forms, surfaces, navigation, feedback, progress)
  с `.d.ts`, `.prompt.md` и демо-карточками; 23 `@dsCard` карточки.
- UI-kit: экран «Калькулятор» (порт `CalculatorScreen.kt`) — mobile (Android-фрейм) и desktop
  (опорное окно 800×600), обе темы.
- **Реструктуризация** проекта под workspace-раскладку референса: дизайн-система вынесена в
  `design-system/` (tokens, guidelines, handoff, readme, SKILL); компоненты сгруппированы
  (`inputs`→`forms`, `content`→`app-kit`, `Divider`→`surfaces`); `ui_kits`→`features/calculator`;
  добавлены `res/` (nav.js, icons), корневой `index.html`, `CLAUDE.md`, `changelog/`, `skills/`.
- Корневой `styles.css` стал тонким shim'ом → `design-system/styles.css` (требование компилятора).

## Зачем
Привести проект к структуре и принципам работы референс-проекта: единая дизайн-система как источник
правды, фичи отдельно от примитивов, кросс-макетная навигация, журнал изменений и проектные скиллы.

## Заметки
- Логотип Pandora Spec в кодовой базе отсутствует — в Brand-карточках используется типографический
  заглушка-wordmark. Нужны реальный логотип и (опционально) брендовые шрифты.
- Генерируемые артефакты (`_ds_bundle.js`, `_ds_manifest.json`) лежат в КОРНЕ, а не в
  `design-system/` (в отличие от референса) — этого требует платформенный компилятор.
