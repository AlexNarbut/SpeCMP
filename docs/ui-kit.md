# UI Kit — Дизайн-система

## Концепция

`core/ui` содержит **всю дизайн-систему**: обёртки над Material3 и другими UI-библиотеками.
Feature-модули и `composeApp` **НИКОГДА не используют** компоненты Material3/Foundation напрямую — только через `Cmp*`-функции из `core/ui`.

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
CmpText(text = "Hello")
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
| Scaffold (каркас экрана) | `scaffold/CmpScaffold.kt` |
| Кнопка (filled) | `button/CmpButton.kt` |
| Кнопка (outlined) | `button/CmpOutlinedButton.kt` |
| Кнопка (text) | `button/CmpTextButton.kt` |
| Кнопка с иконкой | `button/CmpIconButton.kt` |
| Кнопка шага −/+ с автоповтором при удержании | `button/CmpStepButton.kt` |
| FAB | `button/CmpFloatingActionButton.kt` |
| Текстовое поле | `input/CmpTextField.kt` |
| Поле поиска (лупа, «очистить») | `input/CmpSearchField.kt` |
| Поле цифрового кода по ячейкам (CAN-код) | `input/CmpCodeField.kt` |
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
| Блок состояния вместо содержимого (ошибка, пусто, ничего не найдено + действия) | `state/CmpStateBox.kt` |
| Чип (фильтр, недавнее, тип; выбранный, с «✕») | `chip/CmpChip.kt` |
| Card | `surface/CmpCard.kt` |
| Выбираемая карточка | `surface/CmpSelectableCard.kt` |
| Surface | `surface/CmpSurface.kt` |
| Bottom sheet | `sheet/CmpBottomSheet.kt` |
| Top bar | `navigation/CmpTopAppBar.kt` |
| Нижняя навигация | `navigation/CmpNavigationBar.kt` |
| Вкладки | `navigation/CmpTab.kt` |
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
| Snackbar | `dialog/CmpSnackbar.kt` |
| Badge | `badge/CmpBadge.kt` |
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

Галерея новых компонентов (PNG, светлая/тёмная тема, без окна): `./gradlew :core:ui:jvmTest --tests '*KitGallery*'` →
`core/ui/build/kit-gallery/`. Добавляя компонент, дополняй `KitGalleryTest` и смотри результат глазами.

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

## Исключения из «только Cmp*»

Lazy-контейнеры (`LazyColumn`, `LazyRow`, `items {}`) и `Canvas` — сознательное
исключение (U-5): осмысленная обёртка неизбежно протечёт наружу `LazyListScope`/
`DrawScope`, поэтому фичи используют их напрямую. Coil в фичах запрещён — только
`CmpAsyncImage`/`CmpSubcomposeAsyncImage` (общий `ImageLoader` провайдится из
`composeApp`).

## Типографика

`CmpText` принимает роль `style: CmpTextStyle?` (`Title`/`BodyLarge`/`Body`/`Caption` → `titleLarge`/`bodyLarge` 16 sp/`bodyMedium` 14 sp/`bodySmall` 12 sp) с
маппингом на `MaterialTheme.typography` внутри обёртки (U-6) — размеры шрифтов
в фичах числами не задаются. Параметр `fontSize` остаётся для исключений.

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

## Система тем

Расположение: `core/ui/src/commonMain/kotlin/ru/alarmtrade/specmp/core/ui/theme/`
Базовый цвет (seed): `#1D3557`

Feature-модули читают цвета через `cmpThemeColors()`, не импортируя `MaterialTheme`
напрямую. `CmpThemeColors` раскрывает общие Material-роли: базовые
`background` / `surface` / `primary` / `secondary` / `tertiary` / `error`,
container-роли (`primaryContainer`, `secondaryContainer`, `tertiaryContainer`,
`errorContainer`, `surfaceContainer*`), outline-роли и `scrim`.

Доменные цвета, которых нет в Material-схеме, — `cmpDomainColors()` (`theme/CmpDomainColors.kt`, токены
`--success` и `--bus-*` из `design/design-system/tokens/colors.css`, своя пара значений на светлую и тёмную тему):
`success` («поддерживается») и цвета семейств шин автомобиля (`busCan`, `busCanFd`, `busLin`, `busBean`,
`busImmobilizer`, `busUnknown`) — цвет = семейство, номер шины пишется текстом.

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
