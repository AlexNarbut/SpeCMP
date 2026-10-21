# ktlint

[ktlint](https://pinterest.github.io/ktlint/) — линтер и форматтер Kotlin. Правила настраиваются через `.editorconfig`, большинство умеет автофикс (`--format`).

Отдельно CLI не запускаем: ktlint работает как движок внутри [Spotless](spotless.md), а в IDE подключается плагином.

## Что даёт

| Правило (`standard`) | Что проверяет | Автофикс |
|---|---|---|
| `no-unused-imports` | Неиспользуемые импорты | да |
| `import-ordering` | Порядок и раскладка импортов | да |
| `no-wildcard-imports` | Запрет `import a.b.*` | нет |
| `package-name` | Формат имени пакета (нижний регистр, без `_`) | нет |
| `class-naming` | `PascalCase` для классов | нет |
| `function-naming` | `camelCase` для функций | нет |
| `property-naming` | Именование свойств и констант | нет |
| `filename` | Имя файла соответствует единственному верхнеуровневому классу | нет |
| `trailing-comma-on-*` | Trailing comma | да |
| `indent`, `max-line-length`, `wrapping` и т.д. | Формат | в основном да |

Что ktlint **не** умеет: сверять пакет с директорией, ловить баги и сложность. Для этого нужен [detekt](detekt.md).

## `.editorconfig`

Все настройки живут в [`.editorconfig`](../../.editorconfig) в корне репозитория. Общая схема (наш файл — стиль `android_studio` плюс настройки compose-rules):

```ini
root = true

[*.{kt,kts}]
ktlint_code_style = android_studio
max_line_length = 120
indent_size = 4
insert_final_newline = true

# Composable-функции пишутся в PascalCase
ktlint_function_naming_ignore_when_annotated_with = Composable

# В ktlint 1.8 правило по умолчанию выключено
ktlint_standard_no-unused-imports = enabled
```

### Стили кода

`ktlint_code_style` задаёт набор умолчаний:

| Значение | Описание |
|---|---|
| `ktlint_official` | Собственный стиль ktlint, самый строгий |
| `intellij_idea` | Как форматирует IntelliJ по умолчанию |
| `android_studio` | Как Android Studio, ближе к Android-гайду |

У нас выбран `android_studio`: он ближе всего к тому, как форматирует Android Studio, и дал наименьший разовый дифф.

### Отключение и настройка правил

```ini
ktlint_standard_filename = disabled          # выключить правило
ktlint_standard_function-naming = enabled    # включить экспериментальное
```

Точечно в коде: `@Suppress("ktlint:standard:function-naming")`.

## Подводные камни для Compose

- **`function-naming`** ругается на `@Composable fun Screen()` без настройки `ktlint_function_naming_ignore_when_annotated_with = Composable`.
- **Trailing lambda и много параметров.** Форматирование Compose-вызовов ktlint делает плотнее, чем привыкли, посмотрите дифф на реальном экране.
- **Генерируемые ресурсы** (`Res`, Compose Resources, BuildConfig) лежат в `build/` и исключаются на уровне Spotless.

## IDE

- Плагин **Ktlint** из Marketplace или встроенное `Editor → Code Style → Kotlin → Set from → ...`.
- Файл `.editorconfig` подхватывается и Android Studio: часть настроек (отступы, `ij_kotlin_*`) применяется при обычном Reformat Code.
- Разработчику полезно иметь «Reformat on save» + «Optimize imports on save» (`Settings → Tools → Actions on Save`).

## Ссылки

- [Документация ktlint: правила](https://pinterest.github.io/ktlint/latest/rules/standard/)
- [Настройка `.editorconfig`](https://pinterest.github.io/ktlint/latest/rules/configuration-ktlint/)
