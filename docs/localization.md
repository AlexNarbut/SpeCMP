# Локализация — Строковые ресурсы

## Принцип

**Все строки UI всех модулей живут в одном модуле — `core/localization`** — и читаются через публичный класс
`Strings`: `stringResource(Strings.string.<ключ>)`. Один каталог строк — одна выгрузка локализаторам и одно место
для будущего брендирования. Захардкоженные строки в Kotlin-коде **запрещены**; своих `strings.xml` в фичах нет.

---

## Структура файлов

```
core/localization/src/commonMain/composeResources/
├── values/                ← английский (default / fallback)
│   ├── common.xml         ← общие строки приложения (бывший core/ui)
│   ├── app.xml            ← composeApp
│   ├── login.xml          ← одна фича — один файл, имя = префикс фичи
│   └── …
└── values-ru/             ← русский: те же файлы и те же ключи
```

- **Файл фичи = префикс её ключей** (`login.xml` → только `login_*`). Языки добавляются папками
  `values-<lang>/` с тем же набором файлов и ключей.
- В фиче — **только картинки** (`composeResources/drawable/`, свой `Res`). Блок `compose.resources` и
  `androidResources.enable = true` в `build.gradle.kts` фичи нужны **только если у неё есть свои картинки**;
  ради строк модуль ничего не настраивает — достаточно зависимости `implementation(project(":core:localization"))`.

### Реестр префиксов

| Префикс (= файл) | Модуль |
|---|---|
| `common` | общие строки (`core/ui`: даты, общие ошибки `AppError`) |
| `app` | `composeApp` |
| `login`, `authstate`, `main`, `supportchat` | `feature/login`, `feature/auth-state`, `feature/main`, `feature/support-chat` |
| `advancedmount` | `feature/advanced-mount` |
| `vehicle`, `configurator` | `feature/vehicle-ui`, `feature/configurator-ui` |
| `firmware`, `settings` | `feature/device/firmware`, `feature/device/settings-ui` |
| `deviceselect`, `deviceinteraction` | `feature/device/selection`, `feature/device/interaction-ui` |

Префикс — **одно слово без `_`** (иначе его не отличить от экрана). Новая фича — новая строка таблицы и новый файл
в `values/` и `values-ru/`.

---

## Конвенция именования ключей

Формат: **`{feature}_{screen}_{type}[_{name}]`**

```
feature   — префикс фичи из реестра (login, firmware, …)
screen    — экран, секция или общий блок фичи (login, update, archive, common, dialog…)
type      — тип элемента (таблица ниже) — всегда третьим сегментом
name      — что именно; можно опустить, когда тип однозначен (один заголовок экрана: `advancedmount_denied_title`)
```

### Типы элементов

| Тип | Что это | Пример |
|-----|---------|--------|
| `title` | Заголовок экрана/секции/диалога | `login_login_title_screen` |
| `subtitle` | Подзаголовок, подпись под заголовком/пунктом | `configurator_create_subtitle_cloud` |
| `btn` | Текст кнопки | `login_login_btn_submit` |
| `label` | Подпись поля/строки/колонки, метка | `firmware_update_label_confirm_current` |
| `placeholder` | Placeholder поля | `configurator_models_placeholder_search` |
| `hint` | Подсказка под полем / к элементу | `firmware_archive_hint_can` |
| `link` | Кликабельная ссылка/текст | `login_login_link_forgot_password` |
| `error` | Текст ошибки | `firmware_update_error_failure_crc` |
| `msg` | Информационное сообщение, пустое состояние, тост | `settings_common_msg_saved` |
| `loading` | Текст при загрузке | `settings_diff_loading_busy` |
| `status` | Статус, бейдж | `firmware_archive_status_installed` |
| `value` | Значение или вариант (режим, вид, канал, вкл/выкл) | `settings_common_value_on` |
| `item` | Пункт меню, шага, списка | `settings_menu_item_reset` |
| `format` | Шаблон без собственного смысла (`%1$d–%2$d`, `ID %1$s`) | `vehicle_common_format_years_range` |
| `a11y` | Accessibility (contentDescription) | `login_common_a11y_back` |
| `plurals` / `array` | Ключи `<plurals>` / `<string-array>` — тот же формат, тип по смыслу (`label`, `item`…) | `deviceselect_list_label_count` |

### Общие строки

- Одна строка на нескольких экранах **фичи** — `screen = common`: `login_common_btn_retry`.
- Строка, нужная **нескольким фичам**, — в `common.xml` (`common_*`), а не копией в каждой фиче.
  В `common.xml` сегмент экрана необязателен: общие строки без раздела — `common_{type}_{name}`
  (`common_error_no_connection`), с разделом — как обычно (`common_date_label_today`).

### Строки без перевода

Одинаковый на всех языках текст (обозначения шин `CAN1`, `LIN`, контакты `NO`/`NC`) — только в `values/` с
`translatable="false"`; в `values-*/` его нет, берётся fallback. Локализатор такие строки не получает.

### Автопроверка

`config/hooks/check-strings.py` (pre-commit хук `strings-check`, без аргументов — весь репозиторий): строки только в
`core/localization`, префикс = имя файла, формат и тип ключа, одинаковый набор ключей (`string`/`plurals`/`string-array`)
во всех языках, без `\'` и `\"`.

### Как завести строки новой фичи

1. Префикс — в реестр выше (одно слово, совпадает с именем файла).
2. Файлы `values/<префикс>.xml` (en) и `values-ru/<префикс>.xml` (ru) в `core/localization` — **оба сразу**, с одинаковыми ключами.
3. Модулю фичи — `implementation(project(":core:localization"))`; `compose.resources` не нужен (только для картинок).
4. В коде: `import ru.alarmtrade.specmp.core.localization.resources.Strings` + аксессор ключа из того же пакета.

---

## Правила

1. **Default locale = English.** `values/*.xml` — английский fallback; в `values-ru/` — тот же набор ключей.

2. **Не дублировать строки между фичами.** Общая строка — в `common.xml`.

3. **Использование в коде:**
   ```kotlin
   import org.jetbrains.compose.resources.stringResource
   import ru.alarmtrade.specmp.core.localization.resources.Strings
   import ru.alarmtrade.specmp.core.localization.resources.login_login_title_screen

   CmpText(text = stringResource(Strings.string.login_login_title_screen))
   ```
   Вне composable — `getString(Strings.string.…)` (suspend). Картинки фичи — по-прежнему её `Res.drawable.…`.

4. **Не передавать строки через ViewModel.** Строки — ответственность UI-слоя. ViewModel возвращает enum/sealed class, UI маппит в `stringResource`.

5. **Параметры.** Для строк с подстановкой использовать `%s`, `%d`:
   ```xml
   <string name="login_common_msg_email_sent">Email sent to %s</string>
   ```
   ```kotlin
   stringResource(Strings.string.login_common_msg_email_sent, email)
   ```

6. **Не склеивать UI-текст в коде.** Один видимый пользователю текст — **один ключ**
   с плейсхолдерами, а не конкатенация из `stringResource` и литералов. Разделители
   (`·`, `—`, `, `), предлоги и порядок слов — часть перевода: в другом языке они другие
   или не нужны вовсе.

   ```kotlin
   // ❌ Плохо — разделитель и порядок слов зашиты в коде
   CmpText(text = "$author · ${stringResource(Strings.string.supportchat_chat_msg_typing)}")

   // ✅ Хорошо — целая фраза в ресурсе
   CmpText(text = stringResource(Strings.string.supportchat_chat_msg_typing_author, author))
   ```
   ```xml
   <string name="supportchat_chat_msg_typing_author">%s is typing…</string>
   ```

   То же касается **единиц измерения** (`KB`, `MB`, `Б`, `шт.`) и форматов размеров/дат:
   это UI-текст, он переводится — значит живёт в ресурсе (`<string name="..._size_kb">%s KB</string>`),
   а не в Kotlin-литерале.

   - Разделитель между частями (`размер · тип`) — тоже в ресурсе (`%1$s · %2$s`); если известна
     только одна часть — это отдельная ветка без разделителя, а не `joinToString(" · ")`.
   - Десятичный разделитель зависит от языка (`1.5` / `1,5`) — не собирать дробь через `"."`
     вручную.
   - **Даты не собираются из ключей месяцев и `buildString`** — используй общий
     `rememberCmpDateLabeler()` из `core/ui` («Сегодня»/«Вчера»/«14 марта»). Нужен новый формат —
     расширяй хелпер в `core/ui`, а не заводи свои 12 ключей месяцев в фиче.

7. **Технические строки — не в ресурсах, но и не inline.** В файлы строк попадает только то,
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

8. **Числа и списки — `<plurals>` и `<string-array>`, а не ветки в коде.**
   - Текст с количеством, где форма слова зависит от числа (`1 устройство` / `2 устройства` / `5 устройств`), —
     `<plurals>` с формами языка (`one`/`few`/`many`/`other` для русского, `one`/`other` для английского):
     ```xml
     <plurals name="deviceselect_list_label_count">
         <item quantity="one">Найдено %1$d устройство</item>
         <item quantity="few">Найдено %1$d устройства</item>
         <item quantity="many">Найдено %1$d устройств</item>
         <item quantity="other">Найдено %1$d устройства</item>
     </plurals>
     ```
     ```kotlin
     pluralStringResource(Strings.plurals.deviceselect_list_label_count, count, count)
     ```
     Подписи вида «Найдено: %1$d» plurals не требуют — там число не склоняет слово.
   - Упорядоченный набор однотипных строк, который код перебирает (месяцы, шаги подсказки «1, 2, 3»), —
     `<string-array>` и `stringArrayResource(Strings.array.…)`, а не ключи `_1`, `_2`, `_3` и `when` по индексу.
   - Набор вариантов, выбираемых по enum (режим, тип устройства), — отдельные ключи `…_value_<вариант>` и
     исчерпывающий `when` по enum: массив по индексу ломается при перестановке enum.

9. **App-level локаль живёт в `core/localization` и `composeApp`.**
   Feature-модули не читают системный язык и не сохраняют выбранный язык самостоятельно.
   Composition root вызывает `prepareAppLocale()` на старте, хранит `AppLocale` в Compose state,
   провайдит его через `LocalAppLocale` и передаёт в фичи только callback `onLocaleChange`.
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
       AppTheme(onThemeChange) {
           AppNavigation(
               onLocaleChange = { locale ->
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
   - о выборе пользователя сообщает только через callback `onLocaleChange(AppLocale)`, пришедший из composition root;
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
  <string name="supportchat_chat_msg_download_success">File saved to Downloads/Pandora Spec</string>

  <!-- НЕПРАВИЛЬНО — \/ не распознаётся, в UI будет "Downloads\/Pandora Spec" -->
  <string name="supportchat_chat_msg_download_success">File saved to Downloads\/Pandora Spec</string>
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

- **`%`-аргументы.** Поддерживаются `%s` / `%d` (и позиционные `%1$s`); `<plurals>` и `<string-array>` —
  тоже (см. п. 8 выше).
- **HTML/разметка не интерпретируется.** `<b>`, `<i>`, `&lt;` и т.п. в текст не
  превращаются в стили — для форматирования используйте `AnnotatedString` в коде.

---


## Чеклист при добавлении нового экрана

1. Строки — в `core/localization/src/commonMain/composeResources/values/<префикс>.xml` **и** `values-ru/<префикс>.xml`
   (новая фича — новый префикс в реестре и новые файлы), одинаковые ключи в обоих языках.
2. Ключи по конвенции `{feature}_{screen}_{type}[_{name}]`, тип — из таблицы; числа — `<plurals>`, перебираемые
   наборы — `<string-array>` (п. 8).
3. В Kotlin-коде нет захардкоженных строк (кроме технических: log tags, debug); технические строки — в
   `private const val`, UI-текст не собирается конкатенацией (п. 6, 7).
4. Модуль зависит от `:core:localization`; `compose.resources` и `androidResources.enable = true` — только если у
   фичи есть свои картинки (`docs/build.md`, «Ресурсы»).
