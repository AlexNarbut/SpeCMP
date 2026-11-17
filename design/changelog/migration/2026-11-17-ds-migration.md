# 2026-11-17 — Перенос UI kit (Sportlines) в дизайн-систему

## Решения
- Светлая и тёмная темы в сине-белой гамме. Поверхности строятся ступенями прозрачности чернил, как в kit: navy `#0A1A3A` на `#F3F6FB` в светлой теме, `#F5F8FF` на `#0B1220` в тёмной.
- Акцент `brand-primary` `#2F6BFF`. Основная кнопка залита цветом чернил (navy или бело-голубой), Accent синяя.
- Углы мягкие: кнопки, инпуты, карточки и списки 12–16, чипы-пилюли.
- Шрифт IBM Plex Sans (400/500/700), выбран из сравнения в `design-system/guidelines/Font Options.dc.html`. Шкала из 15 стилей `accent1…additional2`.
- Текст кнопок капсом. Ошибка инпута красная (`error-default`).
- Минимальная высота контролов 48: кнопки S48 / M52 / L56 / XL64, чипы и табы 48, инпут 72.
- Без теней. Глубину задают рамки 1px и блюр, мягкая тень только у оверлеев.

## Токены
- `colors.css`: семантика kit (`brand-*`, `foreground-on-dark/light-*`, `background-surface-*`, `border-*`, статусы). M3-роли оставлены алиасами.
- `typography.css`: шкала `--t-*`, M3-переменные ссылаются на неё.
- `spacing.css`: `--space-4…56`, `--r-*`, `--w-*`, `--h-*`, `--press-scale`, `--blur-*`, `--screen-padding-h` (24, на ≥600 → 32).
- `fonts.css`: IBM Plex Sans (Google Fonts).

## Компоненты
- `useStateLayer`: нажатие = scale 0.96 + ripple 10%, фокус-кольцо 3px.
- `CmpText`: стили шкалы и `uppercase`, M3-имена работают как алиасы.
- `CmpButton`: `type` (Primary/Accent/Secondary/Tertiary/Danger), `size` (S/M/L/XL), `loading`. `CmpOutlinedButton` → Secondary, `CmpTextButton` → Tertiary (deprecated-обёртки).
- `CmpIconButton`: новый `variant="Glass"` (56, стекло для топбара).
- Перерисованы: TextField (InputMain 72), Dropdown, Switch, Checkbox, Radio, Slider, TabRow (сегменты), TopAppBar (прозрачный), Dialog (Modal поверх блюра, кнопки стопкой), BottomSheet, Snackbar (TopToast, `tone`), Badge, NavigationBar, Circular/Linear progress, Card, Surface, Divider, SelectableCard.

- Фикс: в тёмной теме фон и текст оставались светлыми. M3-алиасы (`--surface`, `--background`, `--on-surface`, `--success`, `--surface-tint`) теперь переобъявлены в `[data-theme="dark"]`.

- Экраны: оболочки превью (фон страницы в тёмной теме, переключатели, рамка планшета) переведены со старых серых на тёмно-синие (29 файлов). Демо-оболочки assistant/app-update/product-info используют `--font-sans`.

- `_ds_bundle.js`: секции 26 компонентов пересобраны из исходников (Babel), формат бандла сохранён.
- Главный экран (home) проверен в тёмной теме на новых токенах и компонентах.

- Все 17 фич (проход 1, механический): размеры шрифтов в экранах приведены к шкале `--t-*` (11–32 → ближайший стиль), радиус 14 → `--r-card-small-radius`, оболочки превью переведены на сине-белую палитру (фон #E6EBF3, активный сегмент #0A1A3A). Фичи из бандла (app-update, auth, device-select, home, support-chat) пересобраны.

- `CmpTabRow`: проп `inset` (по умолчанию 16) — трек выровнен с контентом, а не во всю ширину.
- Проход 2 (токены): M3-контейнеры (`primary/secondary-container`, `surface-variant` и их `on-*`) теперь ссылаются на ступени `brand-primary-highlight` / `background-surface-highlight` и чернила — все экраны получили kit-поверхности без правок кода.
- Бандл: Auth/Home/SupportChat больше не падают при загрузке (`window.*Screens || {}`).

- `CmpText maxLines={1}` → nowrap + ellipsis (раньше -webkit-box обрезал перенос по дефису без «…»). Advanced-mount: имя устройства на телефоне — headline2.

- `CmpButton`: текст на ступень мельче, чем в kit (капс крупным кеглем перегружал экран): S/M 16 · L 19 · XL 24, трекинг 0.03em.

- `CmpTopAppBar`: заголовок headline2 19 по умолчанию, проп `large` → headline1 24.

- advanced-mount (проход 2): теги статусов (32, радиус 8, рамка, статусные highlight), кнопка «Товар» (compact — полупрозрачная плитка с рамкой, капс; wide — CmpButton Secondary S «Товар»), бейдж замка — чернильный круг, счётчик чата — синий, метка «Новое» — синий тег капсом, синхронизация в топбаре — стеклянная кнопка 56.

- app-update (проход 2): тоны иконок/плашек → `*-highlight` + статусный цвет; карточки → `background-surface-card` + рамка, радиусы `--r-*`; баннеры и плашки версий — ступени прозрачности с рамкой; точки «есть обновление» синие вместо красных; иконка в шапке карточки — круг.

- assistant (проход 2): `--asst-card` → `background-surface-card` в обеих темах; радиусы карточек → `--r-card-*`; степпер и кнопка «Найти» — радиус кнопок, рамка, Primary-заливка чернилами; статусные шапки (ok/error/cancelled) → `*-highlight`.

- auth (проход 2): убран локальный BIG_BTN (кнопки — стандартный CmpButton L 56, радиус 12, капс); карточка входа — `--r-card-large-radius`; баннер ошибки — `error-highlight` + красная рамка; иконка успеха — синий круг.

- Токены: `error-container` / `tertiary-container` → `error-highlight` / `warning-highlight`, их `on-*` → чернила (все экраны с плашками ошибок и критичных предупреждений).
- bypass-clone (проход 2): радиусы 16–24 → `--r-card-medium-radius`.

- check-state, configurator, device-select, device-settings (проход 2): радиусы 16–28 → `--r-card-medium-radius`; цвета контейнеров уже приходят из токенов.

- fast-mount, firmware, home, peripheral-devices, product-info, support-chat, tech-requests, vehicle-select (проход 2): радиусы 16–28 → `--r-card-medium-radius`; home и support-chat пересобраны в бандле.

- Новые компоненты (в бандле, с .d.ts и .prompt.md): `CmpListItem`, `CmpInfoRow`, `CmpChip`, `CmpHoldToConfirmButton`, `CmpContentError`. Демо — `design-system/kit-components.card.html`.
- Гайдлайны `design-system/guidelines/*` переписаны под новую систему: принципы, темы, бренд, ступени поверхностей, рамки, текст, статусы, отступы, радиусы, высоты, глубина, шкала типографики (светлая и тёмная рядом).

- product-info (проход 3): строки ID / серийник / дата → `CmpInfoRow`; состояния «не найден / нет сети / ошибка сервера» → `CmpContentError` (empty / offline / error); карточки → `background-surface-card`.

- Проход 3 (16 файлов экранов): прямые `surface-container-*` → `background-surface-card / highlight / ultra-highlight`, рамки `outline(-variant)` → `border-light / border-highlight`, радиус 12 → `--r-card-small-radius`, вес 600 → 700 (в шкале нет 600). `CmpButton`: текст не переносится (nowrap + ellipsis).

- Токены: `color` по умолчанию на `:root` (navy) и `[data-theme="dark"]` (бело-голубой) — элементы с `color: inherit` больше не падают в чёрный браузерный цвет в тёмной теме (периферия, сводка «требуют внимания»).

- device-settings: сброс к заводским — `CmpHoldToConfirmButton` («Удерживайте для сброса») вместо обычной красной кнопки.

- Новый `CmpInputDigit` (OTP/PIN, ячейки 56×72, title1, secure). bypass-clone: диалог PIN-кода на нём.

## Не сделано / дальше
- Перевести экраны на новые компоненты (ручные строки → CmpListItem/CmpInfoRow, CmpStateBox-аналоги → CmpContentError, опасные действия → HoldToConfirm).
- AppBackground, glass-примитив.
- Проход 2 по фичам: заменить самописные кнопки, чипы и строки на Cmp*, а M3-роли контейнеров (`--secondary-container` и т.п.) на ступени `background-surface-*`.
- Экраны в `features/` нужно проверить: у кнопок и полей изменились высоты.
