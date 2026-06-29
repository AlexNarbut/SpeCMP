# Локализация — Строковые ресурсы

## Принцип

Все строки пользовательского интерфейса хранятся в compose resources **внутри feature-модуля** и загружаются через `stringResource(Res.string.*)`. Захардкоженные строки в Kotlin-коде **запрещены**.

---

## Структура файлов

Каждый feature-модуль с UI содержит собственные строки:

```
feature/<name>/src/commonMain/composeResources/
├── values/strings.xml          ← английский (default / fallback)
└── values-ru/strings.xml       ← русский
```

Языки добавляются по мере необходимости: `values-uk`, `values-de`, и т.д.

---

## Конвенция именования ключей

Формат: **`{feature}_{screen}_{type}_{name}`**

```
feature   — имя feature-модуля (calculator, ...)
screen    — экран или секция (main, settings, common)
type      — тип элемента (см. таблицу ниже)
name      — краткое описание содержимого
```

### Типы элементов

| Тип | Что это | Пример |
|-----|---------|--------|
| `title` | Заголовок экрана/секции | `calculator_main_title` |
| `subtitle` | Подзаголовок | `calculator_main_subtitle` |
| `btn` | Текст кнопки | `calculator_main_btn_equals` |
| `label` | Label поля ввода | `calculator_main_label_input` |
| `placeholder` | Placeholder поля | `calculator_main_placeholder_value` |
| `hint` | Подсказка под полем | `calculator_main_hint_input` |
| `link` | Кликабельная ссылка/текст | `calculator_main_link_history` |
| `error` | Текст ошибки | `calculator_main_error_divzero` |
| `msg` | Информационное сообщение | `calculator_main_msg_overflow` |
| `loading` | Текст при загрузке | `calculator_main_loading_compute` |
| `a11y` | Accessibility (contentDescription) | `calculator_common_a11y_back` |

### Общие строки внутри feature

Если строка одинакова на нескольких экранах модуля — используйте `common` как screen:

```xml
<string name="calculator_common_a11y_back">Back</string>
<string name="calculator_common_btn_clear">Clear</string>
```

### Примеры

```xml
<!-- Main screen -->
<string name="calculator_main_title">Calculator</string>
<string name="calculator_main_btn_equals">=</string>
<string name="calculator_main_btn_clear">C</string>
<string name="calculator_main_error_divzero">Cannot divide by zero</string>
<string name="calculator_main_loading_compute">Computing…</string>
```

---

## Правила

1. **Default locale = English.** Файл `values/strings.xml` содержит английские строки — это fallback.

2. **Не дублировать строки между модулями.** Если строка нужна в нескольких feature-модулях — вынести в `core/ui/src/commonMain/composeResources/values/strings.xml`.

3. **Использование в коде:**
   ```kotlin
   // Пакет ресурсов формируется из rootProject.name + путь модуля.
   // Для feature/calculator: specmp.feature.calculator.generated.resources
   import specmp.feature.calculator.generated.resources.Res
   import specmp.feature.calculator.generated.resources.calculator_main_title
   import org.jetbrains.compose.resources.stringResource

   Text(text = stringResource(Res.string.calculator_main_title))
   ```

4. **Не передавать строки через ViewModel.** Строки — ответственность UI-слоя. ViewModel возвращает enum/sealed class, UI маппит в `stringResource`.

5. **Параметры.** Для строк с подстановкой использовать `%s`, `%d`:
   ```xml
   <string name="calculator_main_msg_result">Result: %s</string>
   ```
   ```kotlin
   stringResource(Res.string.calculator_main_msg_result, resultText)
   ```

6. **Plurals** не поддерживаются CMP напрямую. Используйте несколько ключей или `when` в коде:
   ```xml
   <string name="calculator_history_one">%d entry</string>
   <string name="calculator_history_other">%d entries</string>
   ```

---

## Чеклист при добавлении нового экрана

1. Создать `values/strings.xml` и `values-ru/strings.xml` в `composeResources/` модуля (если ещё нет)
2. Добавить все видимые строки с ключами по конвенции
3. Убедиться что в Kotlin-коде нет захардкоженных строк (кроме технических: log tags, debug)
4. Проверить что `compose.resources` доступен (транзитивно через `core/ui` или явно)
5. Убедиться что в `build.gradle.kts` модуля указано `androidResources.enable = true` (см. `docs/build.md`)
