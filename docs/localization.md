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
feature   — имя feature-модуля (login, ...)
screen    — экран или секция (main, settings, common)
type      — тип элемента (см. таблицу ниже)
name      — краткое описание содержимого
```

### Типы элементов

| Тип | Что это | Пример |
|-----|---------|--------|
| `title` | Заголовок экрана/секции | `login_login_title_screen` |
| `subtitle` | Подзаголовок | `login_authroot_subtitle_specialist` |
| `btn` | Текст кнопки | `login_login_btn_submit` |
| `label` | Label поля ввода | `login_common_label_email` |
| `placeholder` | Placeholder поля | `login_common_placeholder_email` |
| `hint` | Подсказка под полем | `login_common_hint_email` |
| `link` | Кликабельная ссылка/текст | `login_login_link_forgot_password` |
| `error` | Текст ошибки | `login_common_error_auth_failed` |
| `msg` | Информационное сообщение | `login_forgot_msg_success` |
| `loading` | Текст при загрузке | `login_common_loading_auth` |
| `a11y` | Accessibility (contentDescription) | `login_common_a11y_back` |

### Общие строки внутри feature

Если строка одинакова на нескольких экранах модуля — используйте `common` как screen:

```xml
<string name="login_common_a11y_back">Back</string>
<string name="login_common_btn_retry">Retry</string>
```

### Примеры

```xml
<!-- Main screen -->
<string name="login_login_title_screen">Sign in</string>
<string name="login_login_btn_submit">Sign in</string>
<string name="login_common_label_email">Email</string>
<string name="login_common_error_auth_failed">Unable to sign in.</string>
<string name="login_common_loading_auth">Signing in...</string>
```

---

## Правила

1. **Default locale = English.** Файл `values/strings.xml` содержит английские строки — это fallback.

2. **Не дублировать строки между модулями.** Если строка нужна в нескольких feature-модулях — вынести в `core/ui/src/commonMain/composeResources/values/strings.xml`.

3. **Использование в коде:**
   ```kotlin
   // Пакет ресурсов формируется из rootProject.name + путь модуля.
   // Для feature/login: specmp.feature.login.generated.resources
   import specmp.feature.login.generated.resources.Res
   import specmp.feature.login.generated.resources.login_login_title_screen
   import org.jetbrains.compose.resources.stringResource

   Text(text = stringResource(Res.string.login_login_title_screen))
   ```

4. **Не передавать строки через ViewModel.** Строки — ответственность UI-слоя. ViewModel возвращает enum/sealed class, UI маппит в `stringResource`.

5. **Параметры.** Для строк с подстановкой использовать `%s`, `%d`:
   ```xml
   <string name="login_common_msg_email_sent">Email sent to %s</string>
   ```
   ```kotlin
   stringResource(Res.string.login_common_msg_email_sent, email)
   ```

6. **Не склеивать UI-текст в коде.** Один видимый пользователю текст — **один ключ**
   с плейсхолдерами, а не конкатенация из `stringResource` и литералов. Разделители
   (`·`, `—`, `, `), предлоги и порядок слов — часть перевода: в другом языке они другие
   или не нужны вовсе.

   ```kotlin
   // ❌ Плохо — разделитель и порядок слов зашиты в коде
   CmpText(text = "$author · ${stringResource(Res.string.support_chat_typing)}")

   // ✅ Хорошо — целая фраза в ресурсе
   CmpText(text = stringResource(Res.string.support_chat_typing_author, author))
   ```
   ```xml
   <string name="support_chat_typing_author">%s is typing…</string>
   ```

   То же касается **единиц измерения** (`KB`, `MB`, `Б`, `шт.`) и форматов размеров/дат:
   это UI-текст, он переводится — значит живёт в ресурсе (`<string name="..._size_kb">%s KB</string>`),
   а не в Kotlin-литерале.

7. **Технические строки — не в ресурсах, но и не inline.** В `strings.xml` попадает только то,
   что видит пользователь. URI-схемы и пути (`file://`), префиксы ключей `LazyColumn`,
   log-теги, `label` у Compose-анимаций, ключи хранилищ — не переводятся и в ресурсы
   **не выносятся**; они объявляются `private const val` рядом с использованием, а не пишутся
   литералом в месте вызова (см. `docs/patterns.md`, «строки для логики — через `const val`»).

   ```kotlin
   // ❌ Плохо — техническая строка литералом в коде
   model = localPath?.let { "file://$it" } ?: url
   key = { "typing_${item.author}" }

   // ✅ Хорошо
   private const val FILE_URI_SCHEME = "file://"
   private const val TYPING_KEY_PREFIX = "typing_"
   ```

8. **Plurals** не поддерживаются CMP напрямую. Используйте несколько ключей или `when` в коде:
   ```xml
   <string name="login_common_one_attempt">%d attempt</string>
   <string name="login_common_other_attempts">%d attempts</string>
   ```

9. **App-level локаль живёт в `core/localization` и `composeApp`.**
   Feature-модули не читают системный язык и не сохраняют выбранный язык самостоятельно.
   Composition root вызывает `prepareAppLocale()` на старте, хранит `AppLocale` в Compose state,
   провайдит его через `LocalAppLocale` и передаёт в фичи только callback `onLocaleChanged`.
   Пользовательский выбор из switcher применяется через `applyAppLocale(locale)`: он
   сохраняется и переживает перезапуск. Если пользователь ещё ничего не выбирал,
   используется системный язык устройства.

---

## Смена языка в рантайме — контракт

Live-переключение языка — app-level механизм. Он обязан работать для **всех** фич сразу,
а не только для той, где физически расположен переключатель.

1. **Единственный механизм инвалидации строк — `LocalAppLocale` из `core/localization`.**
   Официального API для смены локали в рантайме в CMP нет (на 1.11.x): `stringResource`
   резолвит язык через `Locale.current`, который читает платформенную локаль
   **нереактивно**. Поэтому `LocalAppLocale` объявлен как `staticCompositionLocalOf`:
   смена значения статического local принудительно рекомпозирует всё поддерево
   (без пересоздания — `remember`/back stack не сбрасываются), и `stringResource`
   во всех модулях перечитывает уже применённую платформенную локаль.
   Composition root (`App()`) провайдит его один раз над всем деревом:

   ```kotlin
   var appLocale by remember { mutableStateOf(prepareAppLocale()) }

   CompositionLocalProvider(LocalAppLocale provides appLocale) {
       AppTheme(onThemeChanged) {
           AppNavigation(
               onLocaleChanged = { locale ->
                   applyAppLocale(locale)   // сначала платформенная локаль,
                   appLocale = locale       // затем инвалидация дерева
               },
           )
       }
   }
   ```

2. **`key(locale)` для форс-рекомпозиции запрещён** — и в фичах, и в composition root.
   Он пересоздаёт поддерево (сбрасывает `remember` / `rememberSaveable` / back stack)
   и «чинит» строки только там, где написан: остальные экраны приложения смену языка
   не увидят. Если строки не обновляются — чинить провайдер локали, а не оборачивать
   экран в `key`.

3. **Фичи не управляют локалью.** Feature-модуль:
   - текущую локаль читает через `LocalAppLocale.current` (или получает готовый `AppLocale` параметром);
   - о выборе пользователя сообщает только через callback `onLocaleChanged(AppLocale)`, пришедший из composition root;
   - не вызывает `applyAppLocale` / `prepareAppLocale` и платформенные locale-API.

4. **Платформенная мутация локали — только внутри `core/localization`**
   (`Locale.setDefault`, `AppleLanguages`). Это дополнение для форматирования дат/чисел
   и для следующего запуска, а не механизм live-обновления UI: на iOS `AppleLanguages`
   применяется только после перезапуска процесса. Live-обновление обязано работать
   через `LocalAppLocale` (п.1), без платформенных хаков.

---

## Особенности CMP-ресурсов (отличия от Android AAPT)

Строки парсит **не** AAPT, а build-time конвертер Compose Multiplatform
(`XmlValuesConverterTask` → `PrepareComposeResourcesKt.handleSpecialCharacters`).
Поведение отличается от привычного Android `strings.xml` — учитывайте при переносе строк.

### Экранирование

CMP-парсер обрабатывает **только** эти последовательности:

| Последовательность | Результат |
|--------------------|-----------|
| `\n` | перевод строки |
| `\t` | таб |
| `\uXXXX` | юникод-символ |
| `\\` | один `\` |

Всё остальное проходит **как есть** — бэкслеш остаётся в строке.

- **Прямой слеш `/` экранировать не нужно** — он обычный символ и пишется как есть
  (`Downloads/Pandora Spec`). Экранируется только **обратный** слеш `\`, и только в четырёх
  последовательностях из таблицы выше; любой другой `\x` попадёт в UI дословно, вместе с
  бэкслешем. Литеральный `\` в тексте — это `\\`.

  ```xml
  <!-- ПРАВИЛЬНО -->
  <string name="support_chat_download_success">File saved to Downloads/Pandora Spec</string>

  <!-- НЕПРАВИЛЬНО — \/ не распознаётся, в UI будет "Downloads\/Pandora Spec" -->
  <string name="support_chat_download_success">File saved to Downloads\/Pandora Spec</string>
  ```

- **Апострофы и кавычки пишутся сырыми**, без бэкслеша. `\'` и `\"` НЕ
  распознаются: бэкслеш попадёт в UI (`What\'s` вместо `What's`).

  ```xml
  <!-- ПРАВИЛЬНО -->
  <string name="profile_phase_upcoming_header">What's coming next</string>
  <string name="auth_choose_msg_legal">Terms "as is"</string>

  <!-- НЕПРАВИЛЬНО — \ покажется в приложении -->
  <string name="profile_phase_upcoming_header">What\'s coming next</string>
  ```

- **Нет режима «всё в одинарных кавычках».** В AAPT строку в `'...'` можно не
  экранировать — в CMP такого нет, кавычки выводятся буквально.

- **Исключение — `androidMain/res/values/`.** Эти строки идут через обычный AAPT,
  там `\'` обязателен. Правило про сырые апострофы касается только `composeResources/`.

### Прочие отличия

- **`%`-аргументы.** Поддерживаются `%s` / `%d` (и позиционные `%1$s`); plurals — нет
  (см. п. 8 выше).
- **HTML/разметка не интерпретируется.** `<b>`, `<i>`, `&lt;` и т.п. в текст не
  превращаются в стили — для форматирования используйте `AnnotatedString` в коде.

---


## Чеклист при добавлении нового экрана

1. Создать `values/strings.xml` и `values-ru/strings.xml` в `composeResources/` модуля (если ещё нет)
2. Добавить все видимые строки с ключами по конвенции
3. Убедиться что в Kotlin-коде нет захардкоженных строк (кроме технических: log tags, debug);
   технические строки — в `private const val`, UI-текст не собирается конкатенацией (п. 6, 7)
4. Проверить что `compose.resources` доступен (транзитивно через `core/ui` или явно)
5. Убедиться что в `build.gradle.kts` модуля указано `androidResources.enable = true` (см. `docs/build.md`)
