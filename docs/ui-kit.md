# UI Kit — Дизайн-система

## Концепция

`core/ui` содержит **всю дизайн-систему**: обёртки над Material3 и другими UI-библиотеками.
Feature-модули и `composeApp` **НИКОГДА не используют** компоненты Material3 напрямую — только через `Cmp*`-функции из `core/ui`
(detekt `ForbiddenImport` на `androidx.compose.material3.*` вне `core/ui`). Визуальные компоненты других библиотек
(`foundation.Image`, `Dialog`, Coil) — тоже через `Cmp*`; layout-примитивы, модификаторы и формы `foundation`
(`Box`/`Column`/`Row`, `padding`, `RoundedCornerShape`, `BorderStroke`, `Lazy*`) фичи используют напрямую.

Обходы (сырые цвета, свои размеры текста, Material/Coil/`Image`/`Dialog` мимо `Cmp*`) ловит UI-аудит — `./gradlew uiAudit` и хук
агентов; текущие находки — в baseline, новые роняют проверку ([`code-quality.md`](code-quality.md), «UI-аудит»).

**Зачем:** замена компонента (например, миграция с Material3) требует изменения одного файла в `core/ui`, а не поиска по всему проекту.

---

## ЗАПРЕЩЕНО в feature-модулях

```kotlin
// ❌ ЗАПРЕЩЕНО — прямое использование material3 в features/composeApp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
Button(...) { }
Text(text = "Hello")
```

```kotlin
// ✅ ПРАВИЛЬНО — всегда через Cmp* из core/ui
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
CmpButton(text = "Hello", onClick = { })
CmpText(text = "Hello", style = CmpTextStyle.Paragraph2)
```

### Нет нужного `Cmp*` — расширяем `core/ui`, а не обходим

Правило распространяется на **любую** стороннюю UI-библиотеку (Coil, `androidx.compose.ui.window.Dialog`,
`foundation.Image` и т.п.), а не только на Material3. Если `Cmp*` не покрывает сценарий
(полноэкранный диалог, картинка по url, box без контента, типографическая роль):

1. Проверь карту компонентов ниже — возможно, обёртка уже есть (`CmpImage` вместо `foundation.Image`).
2. Нет — добавь обёртку/параметр в `core/ui` (см. «Добавление нового компонента») и обнови карту.
3. Молчаливый обход в фиче запрещён. Осознанные исключения перечислены в «Исключения из „только Cmp*“».

Инфраструктура сторонних UI-библиотек (`ImageLoader`, кэши, сетевой стек) создаётся один раз в
`core/ui`/`composeApp` и провайдится над деревом — фича не строит свой `ImageLoader`.

---

## Каркас экрана и навигация

Корневой composable экрана оборачивается в `CmpScaffold` (`scaffold/CmpScaffold.kt`); `paddingValues` из него применяются к контенту **первыми**. Верхнюю панель собирать **только** через `CmpTopAppBar` (`navigation/CmpTopAppBar.kt`) в слоте `topBar` — не делать руками `Row` с кнопкой и заголовком. Кнопка «назад» — `CmpIconButton(icon = CmpIcons.ArrowBack, ...)` в слоте `navigationIcon`.

```kotlin
CmpScaffold(
    topBar = {
        CmpTopAppBar(
            title = { CmpText(text = stringResource(Strings.string.login_login_title_screen)) },
            navigationIcon = {
                CmpIconButton(
                    onClick = onBack,
                    icon = CmpIcons.ArrowBack,
                    contentDescription = stringResource(Strings.string.login_common_a11y_back),
                )
            },
        )
    },
    snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
) { paddingValues ->
    Column(Modifier.fillMaxSize().padding(paddingValues)) { /* контент */ }
}
```

Для брендовых или визуально сложных экранов можно передавать plain `Color`:
`CmpScaffold(containerColor = Color.Transparent, contentColor = colors.onBackground)` и
`CmpTopAppBar(containerColor = Color.Transparent, contentColor = colors.onBackground)`.
Feature-код не должен импортировать `TopAppBarDefaults` или другие Material3-типы ради цветов.

WindowInsets (status bar) обрабатывает `CmpScaffold` через `topBar`-слот — `CmpTopAppBar` свои insets обнуляет, чтобы не было двойного отступа. Не добавлять `.statusBarsPadding()` / ручной `.background(...)` на корневом контейнере — фон и insets даёт Scaffold.

---

## Иконки

Иконки добавляются **ТОЛЬКО** через `CmpIcons` enum (`icon/CmpIcons.kt`). Feature-модули **НЕ импортируют** `androidx.compose.material.icons` напрямую.

```kotlin
// ✅ ПРАВИЛЬНО — material-иконка через каталог
CmpIcon(icon = CmpIcons.ArrowBack, contentDescription = "Назад")

// ❌ ЗАПРЕЩЕНО — прямой импорт material icons в feature
import androidx.compose.material.icons.filled.ArrowBack
Icon(Icons.Filled.ArrowBack, ...)
```

Порядок действий при отсутствии нужной иконки:
1. Добавить элемент в `CmpIcons` enum.
2. Добавить ветку в `CmpIcons.imageVector()` (`icon/CmpIconsResolver.kt`).
3. Брендовые/кастомные иконки — через compose resources и `CmpIcon(painter = ...)`.

**Архитектура:**
- `CmpIcons` — enum без данных, только имена.
- `CmpIcons.imageVector()` — `@Composable` расширение, резолвит enum → `ImageVector` из material-icons.
- `CmpIcon(icon: CmpIcons)` — вызывает `imageVector()` автоматически.
- Material-иконки берутся из `compose.materialIconsExtended` (запинен на 1.7.3; для новых иконок при желании можно мигрировать на Material Symbols через vector resources).

### CmpIconButton — только иконки

`CmpIconButton` принимает **только иконку** — `icon: CmpIcons` (предпочтительно) или `icon: ImageVector`. Произвольный `@Composable`-контент сознательно **не поддерживается**: содержимым иконочной кнопки должна быть иконка, а не любой UI.

```kotlin
// ✅ ПРАВИЛЬНО
CmpIconButton(onClick = onBack, icon = CmpIcons.ArrowBack, contentDescription = "Назад")

// ❌ больше не компилируется — произвольный content убран
CmpIconButton(onClick = onBack) { CmpText(text = "←") }
```

---

## Подключение к модулю

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation(project(":core:ui"))
    // НЕ добавлять material3/foundation/compose напрямую —
    // они доступны транзитивно через :core:ui (объявлены там как api)
}
```

---

## Карта компонентов

Все компоненты находятся в: `core/ui/src/commonMain/kotlin/ru/alarmtrade/specmp/core/ui/view/`

| Потребность | Файл |
|-------------|------|
| Scaffold (каркас экрана; блюр и градиент под прозрачным топбаром — контент рисуй под топбаром через `contentPadding`; `topBarBlur` OnScroll / Always (обратные списки) / Off) | `scaffold/CmpScaffold.kt` |
| Живой градиентный фон, стекло, нажатие, рамка | `effect/` (`cmpBackdrop`, `cmpGlassSurface`, `cmpPressable`, `cmpBorder`) |
| Кнопка: `type` Primary (чернила) / Accent (синяя) / Secondary (ступень + рамка) / Tertiary (без фона) / Danger, `size` S48 / M52 / L52 (по умолчанию) / XL56, `loading`, `icon`, `fullWidth`, `maxLines` (длинные подписи), `uppercase = false` (длинные Tertiary-подписи); `TertiaryDanger` — красный текст без фона; подпись капсом в стиле размера; в тесноте: боковые отступы сужаются до 12dp → подпись уменьшается (autoSize) → иконка скрывается (`maxLines = 1`) → многоточие | `button/CmpButton.kt` |
| Опасное действие удержанием (сброс, стирание, прошивка) | `button/CmpHoldToConfirmButton.kt` |
| Кнопка с иконкой (`CmpIcons` / `ImageVector` / `Painter`): `variant` Standard 48 / Glass 56 (стекло, топбар), `selected` | `button/CmpIconButton.kt` |
| Кнопка шага −/+ с автоповтором при удержании | `button/CmpStepButton.kt` |
| FAB | `button/CmpFloatingActionButton.kt` |
| Текстовое поле (многострочное — `singleLine = false`, `minLines`/`maxLines`) | `input/CmpTextField.kt` |
| Поле поиска (лупа, «очистить») | `input/CmpSearchField.kt` |
| Телефон с маской страны (Европа, СНГ; значение — цифры с кодом, логика — `PhoneNumbers` в `core/common`) | `input/CmpPhoneField.kt` |
| Код по ячейкам 56×72, цифра Title1 (CAN-код; `secure` — PIN точками) | `input/CmpCodeField.kt` |
| Скрытие клавиатуры: тап вне поля, прокрутка, зона «не снимать фокус» | `input/KeyboardDismiss.kt` |
| Выпадающий список (поле) | `input/CmpDropdown.kt` |
| Выпадающее меню у кнопки/чипа | `menu/CmpDropdownMenu.kt` |
| Switch | `input/CmpSwitch.kt` |
| Checkbox | `input/CmpCheckbox.kt` |
| Radio button | `input/CmpRadioButton.kt` |
| Сегменты (выбор из 2–3 коротких вариантов) | `input/CmpSegmentedButton.kt` |
| Slider | `input/CmpSlider.kt` |
| Круговой прогресс | `progress/CmpCircularProgressIndicator.kt` |
| Линейный прогресс | `progress/CmpLinearProgressIndicator.kt` |
| Скелетон загрузки (пульсирующий блок) | `progress/CmpSkeleton.kt` |
| Плашка-предупреждение (тон Info/Success/Warning/Error, иконка, заголовок, текст, действие, крестик закрытия `onDismiss`) | `banner/CmpBanner.kt` (`CmpBanner`) |
| Закреплённая нижняя панель действий (ступень 5%, линия сверху, отступ системной навигации) | `banner/CmpBanner.kt` (`CmpActionBar`) |
| Блок состояния вместо содержимого (ошибка, пусто, ничего не найдено + действия) | `state/CmpStateBox.kt` |
| Чип (фильтр, недавнее, тип; выбранный, с «✕», `dropdown` — стрелка выбора) | `chip/CmpChip.kt` |
| Card (`backgroundSurfaceCard` + рамка, без тени) | `surface/CmpCard.kt` |
| Строка списка: иконка в круге со статусом, заголовок/подзаголовок, шеврон; `flat` — пункт меню без фона; `leading` — фото/логотип вместо иконки, `below` — кнопка под текстом | `surface/CmpListItem.kt` (`CmpListItem`) |
| «Подпись над значением» (ID, серийник, версия; `mono`, `accent`, действие справа) | `surface/CmpListItem.kt` (`CmpInfoRow`) |
| Тег статуса (`CmpSize.tag` 32, тона Neutral/Outline/Brand/Success/Warning/Error/Info, `maxLines` для узких мест) | `surface/CmpListItem.kt` (`CmpTag`) |
| Выбираемая карточка | `surface/CmpSelectableCard.kt` |
| Surface | `surface/CmpSurface.kt` |
| Bottom sheet | `sheet/CmpBottomSheet.kt` |
| Top bar (прозрачный; `CmpTopAppBar(title: String, subtitle, large)` — заголовок Headline2/Headline1) | `navigation/CmpTopAppBar.kt` |
| Нижняя навигация | `navigation/CmpNavigationBar.kt` |
| Вкладки-сегменты (`inset`, `scrollable`) | `navigation/CmpTab.kt` |
| Хлебные крошки (путь, свёртка начала в «…») | `navigation/CmpBreadcrumbs.kt` |
| Иконка | `icon/CmpIcon.kt` |
| Каталог иконок (enum) | `icon/CmpIcons.kt` |
| Резолвер иконок (@Composable) | `icon/CmpIconsResolver.kt` |
| Изображение (painter) | `image/CmpImage.kt` |
| Загрузка изображения по url (Coil) | `image/CmpAsyncImage.kt` |
| Загрузка с кастомным placeholder'ом (Coil) | `image/CmpAsyncImage.kt` |
| Картинка по url с заглушкой на время загрузки и при ошибке | `image/CmpAsyncImage.kt` (`CmpAsyncImageWithPlaceholder`) |
| Картинка по url с зумом (щипок, двойной тап) | `image/CmpZoomableImage.kt` |
| Диалог | `dialog/CmpDialog.kt` |
| Полноэкранный диалог | `dialog/CmpFullScreenDialog.kt` |
| Snackbar (внизу; `showSnackbar(tone = Success/Error/Info)`) | `dialog/CmpSnackbar.kt` |
| Badge (синий по умолчанию); угловой счётчик с кольцом `CmpCountBadge`; плитка-подложка под иконку `CmpIconTile` (тон Neutral/Brand/Warning/Error) | `badge/CmpBadge.kt` |
| Счётчик-«таблетка» и точка-маркер «есть изменения» | `badge/CmpCountPill.kt` (`CmpCountPill`, `CmpDot`) |
| Разделитель | `divider/CmpDivider.kt` |
| Текст | `text/CmpText.kt` |
| Текст из простого HTML (абзацы, жирный, курсив, списки, ссылки) | `text/CmpHtmlText.kt` |
| Текст со стилями внутри строки (`AnnotatedString`: подсветка поиска, моноширинный код) | `text/CmpText.kt` (перегрузка) |
| Box | `layout/CmpBox.kt` |
| Column | `layout/CmpColumn.kt` |
| Row | `layout/CmpRow.kt` |
| Строка с переносом (FlowRow) | `layout/CmpFlowRow.kt` |
| Spacer | `layout/CmpSpacer.kt` |
| Размер области экрана и класс окна (Compact / Medium / Expanded / Large, низкое окно) | `layout/CmpWindowSize.kt` (`CmpAdaptiveBox`, `cmpWindowSize`, `CmpBreakpoints`) |
| Перетаскиваемая граница панелей (ширина левой панели; сохранение — `PaneWidthStore` через VM) | `layout/PaneWidthState.kt` (`CmpResizeHandle`, `rememberPaneWidthState`) |
| Постоянная полоса прокрутки `LazyColumn` (iOS своей не рисует) | `scroll/CmpScrollbar.kt` (`Modifier.cmpVerticalScrollbar`) |

Галерея новых компонентов (PNG, светлая/тёмная тема, без окна): `./gradlew :core:ui:jvmTest --tests '*KitGallery*'` →
`core/ui/build/kit-gallery/`. Добавляя компонент, дополняй `KitGalleryTest` и смотри результат глазами.

## Размеры окон и адаптивность

Раскладку экрана выбирают **только** через `core/ui` `view/layout/CmpWindowSize.kt` — своих `private val MediumFrom = 600.dp`
фичи не заводят. Меряется область экрана (`BoxWithConstraints`), а не всё окно: экран в панели мастера получает свой класс.

| Класс | Граница | Опорные окна | Обычная раскладка |
|---|---|---|---|
| `CmpWidthClass.Compact` | ширина < 600 | телефон 412×915, раскладной сложен 344×882 | одна колонка, вкладки |
| `CmpWidthClass.Medium` | 600–839 | раскладной разложен 690×829, планшет-портрет 800×1280 | две панели / форма диалогом |
| `CmpWidthClass.Expanded` | 840–1099 | телефон-ландшафт 915×412, окно desktop 800–1099 | master-detail в две колонки |
| `CmpWidthClass.Large` | ≥ 1100 | планшет-ландшафт 1280×800, desktop | три колонки |
| `isLowHeight` | высота < 520 при ширине ≥ 600 | телефон-ландшафт 915×412 | статус в шапку, без вкладок, главное без прокрутки |

```kotlin
CmpAdaptiveBox(modifier = Modifier.fillMaxSize()) { window ->
    when {
        window.isLowHeight -> LowLayout()
        window.atLeast(CmpWidthClass.Large) -> ThreeColumns()
        window.atLeast(CmpWidthClass.Medium) -> TwoPanes()
        else -> Tabs()
    }
}
// внутри своего BoxWithConstraints — `cmpWindowSize`; сами границы — `CmpBreakpoints.Medium / Expanded / Large / LowHeight`
```

- Классы — источник `design/CLAUDE.md` → «Размеры экранов»; новые границы появляются здесь и в макете вместе.
- Ширины колонок и панелей внутри класса (380–480 и т. п.) — решение экрана, их константы остаются в фиче.
- Раскладные: граница двух панелей — половина ширины (сгиб посередине); `FoldingFeature` пока не используется.
- Опорные окна рендерит галерея состояний (`StateGallery`, `feature/device/state` jvmTest) — образец для визуальной проверки.

## Ошибки запроса и форм

- **Текст ошибки** — ошибка фичи (sealed, с подтипом `Common(AppError)`) в `LCEState.Error` / поле state;
  экран разбирает её исчерпывающим `when`: `Common` → `AppError.message()` (общий текст `core/ui`: «Нет подключения
  к интернету», «Bluetooth выключен», «Связь с блоком прервана»…), свои подтипы → фразы экрана. Своих строк
  для общих ошибок фичи не заводят (подробнее — `docs/patterns.md`, «Ошибки фичи»).
- **Ошибка отправки формы не сдвигает форму.** Баннер не вставляется в поток формы над полями или под кнопкой:
  форма обычно центрирована, и появление баннера двигает поля и кнопку под пальцем. Показывай его поверх контента
  внизу экрана (с отступом от клавиатуры и навигационной панели, анимацией появления) или в заранее
  зарезервированном месте фиксированной высоты. Образец — `AuthBody(error = …)` в `feature/login`.
- **Поля подсвечиваются (`isError`) только когда виноват ввод** (сервер отклонил логин/пароль, не заполнено поле).
  При общей ошибке (`Common(AppError)` — нет связи и т.п.) поля не красятся.
- Блоки состояния вместо содержимого (ошибка загрузки списка, карточки) — `CmpStateBox` с заголовком
  `error.text(…)` и кнопкой «Повторить»; они заменяют содержимое целиком, поэтому прыжка формы нет.

## Поля ввода и клавиатура

На iOS нет системного «Назад», который прячет клавиатуру, поэтому скрытие собрано в `core/ui`
(`input/KeyboardDismiss.kt`) и работает одинаково на всех платформах:

- **Тап вне поля** — `Modifier.clearFocusOnTapOutside()` стоит в корнях `App()`/`FlasherApp()` и внутри `CmpDialog`,
  `CmpFullScreenDialog`, `CmpBottomSheet` (у диалога своё окно). Нажатия он не потребляет: кнопки и строки списков
  срабатывают, фокус снимается на отпускании до их `onClick` (клик, сам запросивший фокус, его сохранит); жест со
  сдвигом дальше touch slop (прокрутка) фокус не снимает. Тап внутри `CmpTextField`/`CmpCodeField` фокус не трогает:
  они помечены `Modifier.keepFocusOnTap()`. Тем же модификатором помечай зоны, где клавиатура должна остаться
  (строка поля чата с кнопкой «Отправить»). Фичам свой `clearFocus()` по тапу писать не нужно.
- **Прокрутка** — `Modifier.hideKeyboardOnScroll()` на `LazyColumn`/scroll-контейнере рядом с полем (результаты
  поиска, отфильтрованный список, история чата, форма в `verticalScroll`): прокрутка пальцем/колесом снимает фокус и
  прячет клавиатуру; программная прокрутка, fling и `bringIntoView` (фокус на поле у края — он тоже скроллит как
  `UserInput`) — нет. Новый экран «поле + список» — ставь модификатор на список.
- **Return** — `CmpSearchField` показывает «Поиск» (`ImeAction.Search`), однострочный `CmpTextField` без своего
  `imeAction` — «Готово» (`ImeAction.Done`); если экран не передал `keyboardActions`, Done/Go/Search снимают фокус
  и прячут клавиатуру. Свои `keyboardOptions`/`keyboardActions` (вход, PIN, отправка в чате) не переопределяются;
  многострочное поле без `imeAction` оставляет перевод строки.

## Исключения из «только Cmp*»

Lazy-контейнеры (`LazyColumn`, `LazyRow`, `items {}`) и `Canvas` — сознательное
исключение (U-5): осмысленная обёртка неизбежно протечёт наружу `LazyListScope`/
`DrawScope`, поэтому фичи используют их напрямую. Coil в фичах запрещён — только
`CmpAsyncImage`/`CmpSubcomposeAsyncImage` (общий `ImageLoader` провайдится из
`composeApp`).

## Типографика

**Закон: текст — только стилями шкалы.** Размер, межстрочный, вес и шрифт (IBM Plex Sans 400/500/700 из
`core/ui/src/commonMain/composeResources/font/`) зашиты в 14 стилей `CmpTextStyle`; своих размеров у текста нет.
Не хватает стиля — он добавляется в шкалу (`design/design-system/tokens/typography.css` → генератор токенов), а не
рисуется исключением на экране.

| Группа | Стили | Размер / межстрочный, вес | Где |
|---|---|---|---|
| Accent | `Accent1` · `Accent2` · `Accent3` | 88 · 56 · 40, Bold | крупные цифры, Highlight |
| Title | `Title1` · `Title2` | 32/34 · 28/30, Bold | заголовок экрана, Modal, ContentError, OTP |
| Headline | `Headline1` · `Headline2` · `Headline3` | 24/26 · 19/22 · 16/18, Bold | кнопки, табы, значения, топбар, чипы, жирный акцент |
| Paragraph | `Paragraph1` · `Paragraph2` · `Paragraph3` | 19/25 · 16/21 · 14/18, Medium | основной и вторичный текст |
| Additional | `Additional0` · `Additional1` · `Additional2` | 14/17 · 12/15 · 10/12, Bold | лейблы, подписи, бейджи |

- `CmpText(text, style = CmpTextStyle.Paragraph2, …, uppercase = false)` — `style` обязателен, параметров `fontSize`,
  `fontWeight`, `lineHeight` нет. Жирность меняется сменой стиля (`Paragraph2` → `Headline3`), а не весом.
- `autoSize = true` — короткая подпись встаёт в одну строку: размер уменьшается шагами 0,5sp от размера стиля
  (с учётом масштаба текста темы) до 70% от него, но не мельче 11sp. Не влезает и на минимуме: при `maxLines = 1` —
  минимум с многоточием, иначе обычный перенос в размере стиля. Для заголовков плиток, значений, тегов, табов,
  заголовка/подзаголовка карточки в тесных контейнерах; абзацы и длинные описания не трогать. Уже включено в
  `CmpTag`, `CmpChip`, сегменты `CmpTabRow`, `CmpButton`, заголовок `CmpListItem`, подпись и значение `CmpInfoRow`,
  заголовок `CmpTopAppBar(title: String)`. Реализация — `view/text/CmpTextAutoSize.kt`, тест — `CmpTextAutoSizeTest`.
- Внутри `AnnotatedString` допустимы `SpanStyle(fontWeight/color/background/fontFamily = Monospace)` — выделение
  фрагмента; размер в спане запрещён.
- Компоненты `core/ui` сами выбирают стиль своего текста (кнопка, поле, таб); фича стиль им не передаёт.
- Проверки (без исключений и baseline): detekt `specmp:RawTextSize` — `.sp`/`.em`/`.toSp()`, `fontSize =`,
  `lineHeight =`, `TextStyle(...)`, `TextUnit` (кроме `letterSpacing`) везде вне `core/ui`; UI-аудит —
  строгие правила `raw_sp`, `bare_text_style`, `text_size_arg`, `text_unit` ([`code-quality.md`](code-quality.md),
  «UI-аудит»). `TextStyle` строится только в `core/ui/theme/CmpTheme.kt` (`CmpTypography`, внутренний).
- Роли Material3 (`MaterialTheme.typography`) внутри `core/ui` — алиасы на шкалу (таблица в `typography.css`).

---

## Правила сигнатур компонентов

Допустимые типы параметров в `Cmp*`-функциях:
- ✅ `androidx.compose.ui.*` — всегда разрешено (`Modifier`, `Color`, `Dp`, `Shape`, `Alignment`, ...)
- ✅ `androidx.compose.foundation.*` — разрешено (`BorderStroke`, `KeyboardOptions`, `RowScope`, ...)
- ✅ Собственные enum/data class, объявленные в том же файле
- ❌ НЕ использовать типы `androidx.compose.material3.*` в сигнатурах (`ButtonColors`, `CardColors`, ...)

Для Material3-специфичных параметров — создать локальный enum и выполнить маппинг внутри обёртки:

```kotlin
// ❌ Плохо — material3-тип утекает в сигнатуру
fun CmpTextField(colors: TextFieldColors)

// ✅ Хорошо — собственный enum, маппинг внутри
enum class CmpTextFieldStyle { Filled, Outlined }
fun CmpTextField(style: CmpTextFieldStyle = CmpTextFieldStyle.Outlined) {
    when (style) {
        CmpTextFieldStyle.Outlined -> OutlinedTextField(...)
        CmpTextFieldStyle.Filled   -> TextField(...)
    }
}
```

---

## Добавление нового компонента

1. Выбрать нужную папку в `core/ui/.../view/` (или создать новую по категории)
2. Создать `Cmp<ComponentName>.kt`
3. Объявить `@Composable fun Cmp<ComponentName>(...)` только с типами параметров из `compose.ui` / `compose.foundation`
4. Для Material3-специфичных параметров — создать локальный enum/class и выполнить маппинг внутри
5. В реализации — свободно использовать любые material3-composable
6. Обновить таблицу компонентов выше

---

## Эффекты: нажатие, рамки, фон, блюр

Примитивы `core/ui/view/effect/` (блюр — haze 2.1, его типы наружу не отдаются):

| Что | API | Правило |
|---|---|---|
| Нажатие | `Modifier.cmpPressable(onClick, enabled, shape, role)` | сжатие до `CmpMotion.PRESS_SCALE` пружиной + ripple чернил + фокус-кольцо `borderFocus` 3dp; все интерактивные `Cmp*` нажимаются так |
| Рамка | `Modifier.cmpBorder(width, color \| brush, shape)` | толщина 0 (`CmpBorderWidth.buttons`) — рамки нет |
| Фон экрана | `Modifier.cmpBackdrop(CmpBackdrop.Home \| Auth)` | цвет страницы + синее свечение бренда сверху справа и тёплое снизу слева; градиентные фоны экранов делаются только им |
| Фон страницы по теме | `Modifier.cmpPageBackground()` | фон текущей темы (`CmpThemeProvider`): свечение (`cmpBackdrop(Home)`), заливка или градиент; для своих рамок вроде предпросмотра «Оформления» — сам экран фон получает от `CmpScaffold` |
| Блюр под топбаром | встроен в `CmpScaffold` | топбар прозрачный; когда контент уезжает под него (контент рисуется под топбаром через `contentPadding` списка), за 80dp прокрутки нарастает лёгкий прогрессивный блюр с тинтом страницы |
| Стекло | `Modifier.cmpGlassSurface(state, shape)` | блюр `CmpBlur.glass` + градиент 0 → 5% чернил + градиентная рамка 1dp; источник — `rememberCmpBlurState()` + `Modifier.cmpBlurSource(state)` |
| Полоса блюра у края | `Modifier.cmpEdgeBlur(state, CmpBlurEdge.Top \| Bottom) { alpha }` | для своих панелей вне `CmpScaffold`; `alpha` читается без рекомпозиции |

## Система тем

Источник истины — CSS-токены макетов `design/design-system/tokens/{colors,typography,spacing}.css`. Kotlin-токены
генерируются в `core/ui/.../theme/tokens/` (`CmpColors.kt`, `CmpTypeTokens.kt`, `CmpDimenTokens.kt`) скриптом
`scripts/ds-tokens/generate.py`; руками не правятся, расхождение ловит pre-commit хук `ds-tokens`
(`generate.py --check`), тесты — `quality/script-tests/test_ds_tokens.py`. Поменяли токен в дизайне — запустите
`scripts/ds-tokens/generate.py` и закоммитьте результат вместе с CSS.

Именование: CSS-переменная в camelCase.

| CSS | Kotlin |
|---|---|
| `--background-surface-highlight`, `--border-light`, `--brand-primary-default`, `--warning-highlight`, `--bus-can` | `CmpTheme.colors.backgroundSurfaceHighlight`, `.borderLight`, `.brandPrimaryDefault`, `.warningHighlight`, `.busCan` |
| `--space-16`, `--screen-padding-h` (+ `@media ≥600`) | `CmpSpacing.space16`, `CmpSpacing.screenPaddingH` / `screenPaddingHMedium` |
| `--r-buttons-radius`, `--r-card-medium-radius` | `CmpRadius.buttons`, `CmpRadius.cardMedium` (9999 — пилюля) |
| `--w-inputs-width`, `--w-focus-width` | `CmpBorderWidth.inputs`, `CmpBorderWidth.focus` (0 — рамку не рисовать) |
| `--icon-m`, `--h-button-l`, `--h-input`, `--touch-min` | `CmpSize.iconM`, `CmpSize.buttonL`, `CmpSize.input`, `CmpSize.touchMin` |
| `--blur-glass`, `--press-scale`, `--motion-standard` | `CmpBlur.glass`, `CmpMotion.PRESS_SCALE`, `CmpMotion.STANDARD_MS` / `standardEasing` |
| `--t-paragraph2-size/-line` | только через `CmpText(style = CmpTextStyle.Paragraph2)` |

Палитра — сине-белая: поверхности строятся ступенями прозрачности «чернил» над страницей (`backgroundSurface*`,
`border*`); имена `foregroundOnDark*` из kit означают «на странице» (чернила в обеих темах), `foregroundOnLight*` —
«на заливке чернилами». Роли Material3 (`MaterialTheme.colorScheme`, только внутри `core/ui`) — алиасы на семантику
(`primaryContainer` → `brand-primary-highlight`, `surfaceVariant`/`secondaryContainer` → `background-surface-highlight`,
`errorContainer` → `error-highlight`, `tertiaryContainer` → `warning-highlight`); фичи и `core/*` берут цвета только
из `CmpTheme.colors` (`cmpThemeColors()` удалён). Миграция экранов на семантику — `.claude/tasks/ds-migration.md` (локальный план).

Доменные цвета — `cmpDomainColors()` (`theme/CmpDomainColors.kt`): `success`, `warning`/`warningContainer` и шины
(`busCan`, `busCanFd`, `busLin`, `busBean`, `busImmobilizer`, `busUnknown`) берутся из тех же токенов; `rating` и слой
камеры — свои значения.

Сырые цвета в фичах (`Color(0x…)`, `Color.White`/`Black`/…, `Color(r, g, b)`) запрещены (UI-аудит `hex_color`,
`raw_color`); нет подходящей роли — токен в `colors.css` и перегенерация. `Color.Transparent`/`Unspecified` допустимы.

```kotlin
// Прочитать текущую тему
val isDark by LocalThemeIsDark.current

// Переключить тему
var isDark by LocalThemeIsDark.current
isDark = !isDark
```

Выбор темы запоминается между запусками: `AppTheme(onThemeChange, themeStore = DefaultAppThemeStore)` в корне
приложения (`App`, `FlasherApp`) берёт сохранённый выбор (`theme/AppThemeStore.kt`, платформенный `Settings` процесса
`platformSettings` из `core/common/di`, ключ `app_theme_dark`), а пока пользователь не переключал тему — системную. Переключение через `LocalThemeIsDark`
сохраняется само. Галереи и тесты вызывают `AppTheme` без хранилища — тема как в системе, сохранённый выбор на
снимки не влияет.

Пользовательская тема (экран «Оформление») — `AppTheme(…, customThemeStore = DefaultCustomThemeStore)` в корне `App`
(`theme/CustomThemeStore.kt`, тот же `platformSettings`, ключ `app_theme_custom`, значение — JSON файла темы
`ThemeFileCodec`); `FlasherApp`, галереи и тесты — без хранилища, тема по умолчанию. Модель — `theme/custom/`
(`ThemeSpec`: светлая и тёмная независимо, палитра из 12 ролей, форма, размеры, кнопки, фон; `ThemePreset` — готовые
темы, `Pandora` = CSS-токены), формат файла — `theme/custom/data/` (DTO + маппер). `CmpThemeProvider(spec, isDark)`
выводит из режима темы цвета (`ThemeColorDerivation`: неизменённая роль отдаёт CSS-токен как есть, изменённая
перекрашивает свои токены с их прозрачностью), радиусы, стиль кнопок/чипов, масштаб текста и фон страницы, и
раздаёт их через internal-локали; им же оборачивается предпросмотр (`feature/appearance`). Внутри `CmpThemeProvider`
`LocalThemeIsDark` — его `isDark` (только чтение: `cmpDomainColors()` и др. видят режим предпросмотра); `AppTheme` отдаёт
содержимому своё записываемое состояние (переключатель в меню). Поэтому:

- `CmpTheme.colors` — из `LocalCmpColors`, `MaterialTheme.colorScheme` — выведенная схема;
- `CmpRadius.*` — `@Composable`-геттеры из `LocalCmpRadii` (`CmpRadii`, значения по умолчанию — CSS из
  `spacing.css`): читаются **только в composition**. Вне её (Modifier-фабрики, `val` верхнего уровня, `remember`-
  лямбды без композиции) — передайте радиус параметром или сделайте геттер `@Composable @ReadOnlyComposable`;
  Modifier-фабрики `@Composable` не делать;
- масштаб текста темы (`TextScale`: 80/100/115/130/150%) умножается на системный, но итог не больше 150%
  (`effectiveTextScale`): при системном ≥150% тема не увеличивает текст, а 80% уменьшает его при любом системном.
  `CmpSpacing`/`CmpSize`/`CmpBorderWidth` от темы не зависят.
