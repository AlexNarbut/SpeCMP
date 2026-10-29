# Compose Rules

[compose-rules](https://mrmans0n.github.io/compose-rules/) (mrmans0n) — набор правил для Compose, развитие форка правил Twitter. Не самостоятельный инструмент: подключается **к ktlint или к detekt**.

## Что проверяет

Основные группы (полный список с обоснованием — [на сайте проекта](https://mrmans0n.github.io/compose-rules/rules/)):

| Область | Примеры правил |
|---|---|
| `Modifier` | `ModifierMissing`, `ModifierWithoutDefault`, `ModifierNotUsedAtRoot`, `ModifierReused`, `ModifierClickableOrder`, `ModifierComposable` |
| Параметры | `ComposableParamOrder`, `ContentTrailingLambda`, `LambdaParameterEventTrailing`, `MutableParams`, `ParameterNaming` |
| Состояние | `RememberMissing`, `RememberContentMissing`, `MutableStateAutoboxing`, `LambdaParameterInRestartableEffect` |
| Структура | `MultipleEmitters`, `ContentEmitterReturningValues`, `ComposableNaming` |
| Стабильность | `UnstableCollections` (`List` вместо `ImmutableList`) |
| ViewModel | `ViewModelInjection`, `ViewModelForwarding` |
| Preview и локали | `PreviewPublic`, `PreviewAnnotationNaming`, `CompositionLocalAllowlist`, `CompositionLocalNaming` |

## Как подключить

**Вариант A: через ktlint (Spotless).** Правила идут в тот же запуск, что форматирование, есть часть автофиксов:

```kotlin
ktlint(libs.versions.ktlint.get())
    .customRuleSets(listOf("io.nlopez.compose.rules:ktlint:<version>"))
```

**Вариант B: через detekt.** Артефакт `io.nlopez.compose.rules:detekt:<version>`, подключается как плагин:

```kotlin
dependencies {
    detektPlugins("io.nlopez.compose.rules:detekt:<version>")
}
```

Настройки — в yaml под ключом `Compose:` (правила включаются и настраиваются как обычные правила detekt).

### Какой вариант выбрать

| | ktlint | detekt |
|---|---|---|
| Зависит от версии | ktlint | detekt |
| Автофикс | есть у части правил | нет |
| Риск для нас | небольшой | версия detekt должна совпадать с той, под которую собраны правила, а detekt 2.0 ещё в альфе |

У нас выбран **ktlint-вариант** (через Spotless): он не зависит от альфа-версии detekt и даёт часть автофиксов.

## Настройка под наш проект

Правила по умолчанию рассчитаны на Android-Compose с Hilt; что учтено у нас:

- **`ViewModelInjection` / `ViewModelForwarding`.** ViewModel'и у нас на `BaseViewModel` и Koin (`koinViewModel` правила знают). ViewModel получает только Screen, вниз передаются значения и колбэки ([`../patterns.md`](../patterns.md)).
- **`ComposableParamOrder`** и **`ModifierMissing`.** Порядок параметров соответствует [`../compose-best-prac.md`](../compose-best-prac.md); `ModifierMissing`/`ModifierWithoutDefault` выключены (много Screen-функций).
- **`CompositionLocalAllowlist`.** Свои `CompositionLocal` (`LocalCmpImageLoader`, `LocalThemeIsDark`) внесены в `compose_allowed_composition_locals` в [`.editorconfig`](../../.editorconfig).
- **`UnstableCollections`.** Списки в параметрах composable — `ImmutableList` ([`../compose-best-prac.md`](../compose-best-prac.md) §3).
- **`Material2` и др.** UI Kit у нас свой — `Cmp*` из `core/ui` ([`../ui-kit.md`](../ui-kit.md)).
- **`PreviewPublic`.** Превью в `commonMain` (KMP) могут иметь особенности видимости.

Отключать правило точечно: `@Suppress("ktlint:compose:<правило>")` либо глобально в `.editorconfig` (`ktlint_compose_<правило> = disabled`).

## Ссылки

- [Сайт и список правил](https://mrmans0n.github.io/compose-rules/)
- [Использование с ktlint](https://mrmans0n.github.io/compose-rules/ktlint/) · [с detekt](https://mrmans0n.github.io/compose-rules/detekt/)
- [Репозиторий](https://github.com/mrmans0n/compose-rules)
