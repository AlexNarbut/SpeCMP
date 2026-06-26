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
| Кнопка (filled) | `button/CmpButton.kt` |
| Кнопка (outlined) | `button/CmpOutlinedButton.kt` |
| Кнопка (text) | `button/CmpTextButton.kt` |
| Кнопка с иконкой | `button/CmpIconButton.kt` |
| FAB | `button/CmpFloatingActionButton.kt` |
| Текстовое поле | `input/CmpTextField.kt` |
| Switch | `input/CmpSwitch.kt` |
| Checkbox | `input/CmpCheckbox.kt` |
| Radio button | `input/CmpRadioButton.kt` |
| Slider | `input/CmpSlider.kt` |
| Круговой прогресс | `progress/CmpCircularProgressIndicator.kt` |
| Линейный прогресс | `progress/CmpLinearProgressIndicator.kt` |
| Card | `surface/CmpCard.kt` |
| Surface | `surface/CmpSurface.kt` |
| Top bar | `navigation/CmpTopAppBar.kt` |
| Нижняя навигация | `navigation/CmpNavigationBar.kt` |
| Вкладки | `navigation/CmpTab.kt` |
| Иконка | `icon/CmpIcon.kt` |
| Изображение | `image/CmpImage.kt` |
| Диалог | `dialog/CmpDialog.kt` |
| Snackbar | `dialog/CmpSnackbar.kt` |
| Badge | `badge/CmpBadge.kt` |
| Разделитель | `divider/CmpDivider.kt` |
| Текст | `text/CmpText.kt` |

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

```kotlin
// Прочитать текущую тему
val isDark by LocalThemeIsDark.current

// Переключить тему
var isDark by LocalThemeIsDark.current
isDark = !isDark
```
