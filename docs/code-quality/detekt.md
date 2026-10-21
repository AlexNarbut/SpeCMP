# detekt

[detekt](https://detekt.dev/) — статический анализатор Kotlin. В отличие от ktlint смотрит на смысл кода: сложность, потенциальные баги, обработку исключений, именование, а также поддерживает **свои правила**.

## Что даёт

| Ruleset | Примеры правил | Зачем нам |
|---|---|---|
| `naming` | `PackageNaming`, `InvalidPackageDeclaration`, `MatchingDeclarationName`, `ClassNaming`, `FunctionNaming` | Неверные имена, **пакет не совпадает с директорией** |
| `style` | `UnusedImports`, `UnusedPrivateMember`, `MagicNumber`, `ForbiddenImport`, `ForbiddenMethodCall` | Мёртвый код, запрет конкретных API |
| `potential-bugs` | `UnsafeCast`, `UnsafeCallOnNullableType`, `IgnoredReturnValue` | Небезопасные касты и `!!` |
| `exceptions` | `SwallowedException`, `TooGenericExceptionCaught` | Проглоченные исключения |
| `coroutines` | `GlobalCoroutineUsage`, `InjectDispatcher`, `SuspendFunWithFlowReturnType` | Ошибки корутин |
| `complexity` | `LongMethod`, `CyclomaticComplexMethod`, `LongParameterList` | Слишком сложный код |
| `performance` | `SpreadOperator`, `ForEachOnRange` | Мелкие оптимизации |

### Что detekt не закрывает

- **Безопасность в узком смысле** (секреты, TLS, cleartext, `MODE_WORLD_READABLE`, `WebView` с включённым JS-интерфейсом): у detekt набор правил про «небезопасный код» ограничен потенциальными багами. Это зона Android Lint и внешних сканеров (gitleaks, Semgrep).
- Форматирование: для него используется ktlint через Spotless. **Не подключайте `detekt-formatting`**, иначе ktlint будет работать дважды.

## Версии: важный момент для нашего проекта

| Ветка | Состояние | Плагин |
|---|---|---|
| **1.23.x** (стабильная) | Собрана под более старый Kotlin, на Kotlin `2.4.x` возможны предупреждения о версии или сбои | `io.gitlab.arturbosch.detekt` |
| **2.0.0-alpha.x** | Альфа, но собрана под свежий Kotlin (alpha.6 — Kotlin `2.4.10`) | `dev.detekt` |

Проект на Kotlin `2.4.0`; выбрана `2.0.0-alpha.6` — на нашем проекте анализ с type resolution работает. Обновление Kotlin выше версии, под которую собран detekt, может ломать анализ, это надо учитывать при планировании апгрейдов Kotlin.

Актуальные версии: [Compatibility Table](https://detekt.dev/docs/introduction/compatibility/), [Releases](https://github.com/detekt/detekt/releases). В 2.0 переименованы плагин, пакеты и часть правил, конфиг 1.x нельзя использовать как есть (см. [Migration Guide](https://detekt.dev/changelog/)).

## Настройка (общая схема)

```kotlin
// плагин 1.23.x; в 2.0 id = "dev.detekt"
plugins { id("io.gitlab.arturbosch.detekt") }

detekt {
    buildUponDefaultConfig = true          // берём дефолты и переопределяем нужное
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    parallel = true
    autoCorrect = false
}
```

Файл `config/detekt/detekt.yml` хранит только отличия от дефолта:

```yaml
naming:
  FunctionNaming:
    ignoreAnnotated: ['Composable']
  TopLevelPropertyNaming:
    active: true
style:
  ForbiddenMethodCall:
    active: true
    methods:
      - reason: 'Use safeRun (см. docs/patterns.md)'
        value: 'kotlin.runCatching'
      - reason: 'Пишите состояние через MutableStateFlow.update { } (docs/patterns.md)'
        value: 'kotlinx.coroutines.flow.MutableStateFlow.value'
  MagicNumber:
    ignoreCompanionObjectPropertyDeclaration: true
```

## Type resolution

Часть правил (`UnsafeCallOnNullableType`, `UnusedPrivateMember` в точном виде, `IgnoredReturnValue`, `ForbiddenMethodCall` и др.) работает **только с type resolution**, то есть нужно запускать задачи по компиляциям (`detektMainAndroid`, `detektMainJvm`, `detektDebug`…), а не голый `detekt` / `detekt<SourceSet>SourceSet`, которые анализируют без типов. Точные названия — `./gradlew :<module>:tasks --all | grep detekt`; какие из них собирает наша задача `detektCheck` — в [`../code-quality.md`](../code-quality.md).

## Baseline

`./gradlew detektBaseline` записывает текущие нарушения в `detekt-baseline.xml`. Они не считаются ошибками, новые нарушения ломают сборку. **У нас baseline не используется**: замечаний ноль.

## Свои правила

Два уровня:

1. **Без кода** — `ForbiddenImport`, `ForbiddenMethodCall`, `ForbiddenAnnotation`, `ForbiddenComment` в yaml. Подходит для `runCatching`, `println`, записи `MutableStateFlow.value`.
2. **Свой RuleSet** — отдельный Gradle-модуль на `detekt-api`, регистрируется через `RuleSetProvider` и `META-INF/services`, подключается `detektPlugins(project(":tools:detekt-rules"))`. Нужен для проверок структуры: соглашение об именах `*Screen` / `*ViewModel`, зависимость presentation → data только через domain. API меняется между 1.x и 2.0.

## Отчёты и CI

detekt пишет отчёты в HTML, XML, SARIF и Markdown (`reports { ... }` в конфигурации). Для CI подходит XML/SARIF как артефакт.

## Ссылки

- [detekt.dev](https://detekt.dev/), [список правил](https://detekt.dev/docs/rules/naming)
- [Compatibility Table](https://detekt.dev/docs/introduction/compatibility/)
