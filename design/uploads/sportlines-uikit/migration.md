# Перенос в SpeCMP

Как этот UI kit ложится на нашу дизайн-систему `core/ui` и в каком порядке переносить. Сначала дизайнер собирает библиотеку в Figma, затем мы переносим её в код и адаптируем экраны.

## Что у нас сейчас

- Тема — Material3, кастомизирована только цветовая схема (светлая/тёмная, синий primary #1A3E7A / #4AA3EA). Типографика, формы, отступы и тени — дефолты M3, шрифт системный.
- Компоненты `Cmp*` — тонкие обёртки над M3: кнопка 40dp-пилюля, поле 56dp с радиусом 4, диалог с радиусом 28.
- Токенов отступов и радиусов в коде нет: в фичах около 3 000 литералов `.dp` и около 420 сырых `RoundedCornerShape`/`CircleShape`. Ещё 195 обходов через сырые `.sp`.
- Блюра, стекла и общих фонов нет. Градиенты нарисованы вручную в `main`, `login`, `support-chat`.
- 169 иконок `CmpIcons` — Material Icons Filled, плюс ~180 своих XML в фичах устройства.

## Решения для дизайнера (обсудить до начала)

1. **Светлая тема.** У Sportlines фактически только тёмная. У нас есть светлая, и монтажники работают в машине днём. Варианты: (а) только тёмная, как в Sportlines; (б) тёмная основная + светлая по той же схеме «ступени прозрачности чёрного» (в Figma уже есть Light-режим как основа). Рекомендация — (б).
2. **Акцент.** Зелёный #53F153 — цвет бренда Sportlines. Нам нужен свой акцент в той же роли (`brand-primary`), например фирменный цвет Pandora/Alarmtrade. Структура токенов не меняется, меняются только значения примитивов `accent` / `accent-2`.
3. **Углы.** Острые 0px (Sportlines) или скругления (1x2). Это одна переменная на компонент (`r-*`), можно решить позже и поменять разом.
4. **Шрифт и шкала стилей.** Space Grotesk или свой, но с тем же набором весов 400/500/700. Шкалу из 15 стилей фиксируем сразу и целиком: после этого текст на макетах и в коде — только этими стилями. Нужен новый размер — дизайнер добавляет стиль в шкалу, а не делает исключение на экране.
5. **Цвета статусов CAN/LIN и т.п.** (`busCan`, `busLin`, …) — доменные. Переносим как отдельную группу семантики `domain-*` в тех же шкалах.
6. **Ошибка инпута синяя** — у Sportlines это решение бренда. Нам, вероятно, нужна красная (`error-default`).

## Как устроить Figma

- Коллекции переменных повторить 1:1: `Primitives colors` → `Semantic colors` (режимы Dark / Light) → `Components` (режимы по брендам при необходимости), `Primitives radius` → `Semantic border-radius`, `Primitives width` → `Semantic border-width`. Добавить `Spacing` и `Size`, которых у Sportlines в Figma нет.
- Имена переменных — как здесь (`background/surface/highlight`, `button/primary/pressed/bg`): у Sportlines по ним генерируется Kotlin-код, тот же пайплайн мы сможем поднять у себя.
- Стили текста — 15 стилей из раздела «Типографика».
- Компоненты — с вариантами по свойствам `Type`, `Size`, `State` (Default / Pressed / Loading / Disabled / Focus) как в таблицах на страницах компонентов.

## Соответствие компонентов

| Sportlines | SpeCMP сейчас | Что делаем |
|---|---|---|
| `Button` (7 типов × 4 размера) | `CmpButton`, `CmpOutlinedButton`, `CmpTextButton` (254 вызова) | Одна `CmpButton(type, size)`: Primary / Secondary / Tertiary / Accent; Outlined → Secondary, Text → Tertiary |
| `HoldToConfirmButton` | — | Новый: опасные действия (сброс, стирание, прошивка) |
| `StatusButton` | — | Новый, Warning |
| `IconButton`, `TopBarIcon` (стекло 56) | `CmpIconButton` (85) | Добавить стеклянный вариант для топбара |
| `InputMain` (72, плавающий лейбл) | `CmpTextField` (20) | Перерисовать |
| `InputCenter`, `PasswordInput` | `CmpTextField`, `CmpPhoneField` | Новый центрированный вариант для логина и форм-шагов |
| `InputDigit` (OTP) | `CmpCodeField` | Перерисовать: ячейки 44×72, цифра `title1` |
| `Tabs` / `SegmentedTabs` | `CmpTabRow`, `CmpSegmentedButton` | Перерисовать |
| `Chips` (+ `ChipSearchField`) | `CmpChip`, `CmpSearchField` | Перерисовать, градиентный фон и рамка |
| `Toggle` 40×22 | `CmpSwitch` | Перерисовать |
| `CheckBox` 24 | `CmpCheckbox` | Перерисовать |
| `Slider` (трек 4, бегунок 12) | `CmpSlider` | Перерисовать |
| `ListItem` / `StatusListItem` / `SelectableListItem` / `InfoRow` / `RowSection` | нет — собираются из `CmpSurface` + `CmpRow` | Новые: основа экранов настроек, устройства, периферии |
| `TopBar` / `CollapseTopBar` + блюр | `CmpTopAppBar` (32) | Прозрачный топбар + прогрессивный блюр |
| `BottomSheet` (прямоугольная, блюр 64) | `CmpBottomSheet` (8) | Перерисовать |
| `Modal` / `ModalDialog` (на весь экран поверх блюра) | `CmpDialog` (51), `CmpFullScreenDialog` | Перерисовать; самый заметный визуальный сдвиг |
| `TopToast` | `CmpSnackbar` (14) | Тост сверху вместо снекбара снизу |
| `SpinningProgressBar` | `CmpCircularProgressIndicator` (62) | Заменить |
| `TriangleLoader` | `CmpSkeleton` (37) | Заменить |
| `ContentError` | `CmpStateBox` (30) | Перерисовать |
| `Highlight`, `InfoMessage`, `SpeechBubble` | — | Новые, по потребности |
| `AppBackground` (свечения) | ручные градиенты в main/login/chat | Новый общий фон в `core/ui` |
| `glassSurface` | — | Новый примитив |
| Нижняя навигация-карусель | `CmpNavigationBar` (не используется) | Не переносим: у нас нет таб-бара |
| Камера, промо-карточка, конфетти, чарты | — | Не переносим (специфика беттинга) |

## Порядок работ

1. **Фундамент в Figma.** Примитивы, семантика (Dark + решение по Light), типографика, spacing/size, радиусы, рамки. Согласовать решения 1–6.
2. **Базовые компоненты.** Button, IconButton/TopBarIcon, инпуты, Toggle/CheckBox/Slider, Tabs/Chips, строки списков, TopBar, Modal, BottomSheet, Toast, лоадеры.
3. **Код: типографика как закон.** `CmpText(style = …)` с обязательным стилем и без параметров `fontSize` / `lineHeight` / `fontWeight`. Свой объект `CmpTypography` с 15 ролями вместо M3 `Typography`. Правило detekt по образцу `RawTextSize` из Sportlines: запрет литералов `.sp` и `TextStyle(...)` вне `core/ui`. Сейчас таких обходов 195 (`raw_sp` в baseline ui-audit) — их переводим на роли, baseline обнуляем.
4. **Код: тема.** В `core/ui`: свои `Colors` / `Typography` / `Shapes` / `Spacing` / `Size` вместо M3-дефолтов, шрифт, `cmpThemeColors()` → новые семантические роли. На этом шаге визуально меняются сразу все `Cmp*`.
5. **Код: компоненты.** Перерисовать `Cmp*` по таблице, добавить новые (ListItem, HoldToConfirm, glass, фон). Расширить ui-audit правилами на сырые `.dp` и формы.
6. **Экраны.** По модулям, от самых заметных: `main`, `login`, `device/state`, `device/settings-ui`, `fast-mount`, `firmware`, дальше по списку. Каждый — замена сырых отступов на токены и сборка из новых компонентов.
