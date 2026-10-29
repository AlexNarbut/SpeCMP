# Качество кода — форматирование и статический анализ ⚠️ ОБЯЗАТЕЛЬНАЯ СТАДИЯ

Проверка качества кода — **одна из ключевых стадий** разработки, наравне со сборкой
([`build.md`](build.md#протокол-верификации)): изменение не считается готовым, пока `spotlessCheck` и `detektCheck`
проходят чисто. Стадия ловит то, что компилятор пропускает: неиспользуемые импорты, неверные имена пакетов и
классов, небезопасные конструкции (`!!`, проглоченные исключения, `runCatching`) и типичные ошибки Compose.

> Справка по каждому инструменту (что умеет, как настраивается, подводные камни) — в каталоге
> [`code-quality/`](code-quality/index.md). Этот документ — актуальное описание того, **что подключено у нас**.

---

## Что применяем

| Инструмент | Версия | Для чего | Умеет чинить сам |
|---|---|---|---|
| **ktlint** | 1.8.0 | стиль и форматирование: импорты (неиспользуемые, порядок, wildcard), отступы, имена (`package-name`, `class-naming`, `function-naming`, `filename`) | да |
| **compose-rules** | 0.6.7 (плагин ktlint) | правила Compose: `modifier`, `remember`, порядок и имена параметров, ViewModel в composable, эффекты | частично |
| **Spotless** | 8.10.4 | Gradle-обёртка над ktlint + compose-rules: `spotlessCheck` / `spotlessApply`, работа только по изменённым файлам | да (через ktlint) |
| **detekt** | 2.0.0-alpha.6 | смысл кода: мёртвый код, `!!`, исключения, неверный пакет, запрещённые вызовы, сложность и длина, магические числа | нет |

Разделение труда: **Spotless — стиль и автофикс, detekt — смысловые замечания без автофикса**. Правила не
дублируются: форматирование, импорты и wildcard проверяет ktlint (`detekt-formatting` не подключён,
`WildcardImport` в detekt оставлен как дополнительная страховка).

---

## Как это вызывается (архитектура)

```
git commit ──► pre-commit ──► ./gradlew spotlessCheck        (стиль, только файлы изменённые с HEAD)
                          ├─► config/hooks/check-serial-name.sh (@SerialName у @Serializable, коммитимые .kt)
                          ├─► config/hooks/check-stateflow-value.py (MutableStateFlow только через update, .kt)
                          └─► config/hooks/check-strings.py     (строки, ключи, языки — docs/localization.md)
git push   ──► pre-push   ──► ./gradlew detektCheck          (смысл, весь проект, с type resolution)
Claude Edit/Write *.kt ──► .claude/hooks/kotlin-quality.sh ──► check-stateflow-value.py + spotlessCheck (по файлу)
```

| Файл | Роль |
|---|---|
| [`build.gradle.kts`](../build.gradle.kts) | блок `spotless { … }` (один корневой на весь репозиторий): цели, версии ktlint/compose-rules, ratchet; блок `subprojects { … }`: подключение detekt ко всем подпроектам, конфиг, исключение `build/`, агрегирующая задача `detektCheck` |
| [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) | версии `detekt`, `spotless`, `ktlint`, `composeRules`; плагины `detekt`, `spotless` |
| [`.editorconfig`](../.editorconfig) | правила ktlint и compose-rules (стиль, отключённые правила, allowlist `CompositionLocal`) |
| [`config/detekt/detekt.yml`](../config/detekt/detekt.yml) | только отличия от дефолтов detekt |
| [`.pre-commit-config.yaml`](../.pre-commit-config.yaml) | локальные хуки `spotless-check`, `serial-name-check`, `stateflow-value-check`, `strings-check` (pre-commit) и `detekt-check` (pre-push) |
| [`config/hooks/check-strings.py`](../config/hooks/check-strings.py) | строки: только в `core/localization`, формат ключей, одинаковые ключи во всех языках ([`localization.md`](localization.md)) |
| [`config/hooks/check-serial-name.sh`](../config/hooks/check-serial-name.sh) | у каждого `@Serializable` есть `@SerialName` (`docs/patterns.md`, «Сериализация»); без аргументов — весь репозиторий |
| [`.claude/settings.json`](../.claude/settings.json), [`.claude/hooks/kotlin-quality.sh`](../.claude/hooks/kotlin-quality.sh) | хук для агентов: после правки `.kt`/`.kts` возвращает нарушения этого файла |

Почему так устроено:

- **Spotless один, на корне.** Отдельные ktlint-запуски по модулям заметно медленнее, а правила одинаковы для всех.
  Файлы для обхода задаются через `fileTree` с исключением `build/`, `generated/`, `.gradle/`, `.kotlin/`: иначе
  Spotless заходит в build-выходы (в том числе `iosApp/build`) и сгенерированные `Res`. `kotlinGradle` охватывает
  `*.gradle.kts` до трёх уровней вложенности (`feature/device/<module>/build.gradle.kts`).
- **detekt из корневого `build.gradle.kts`, без `build-logic`.** Он нужен в каждом модуле (свои source set'ы и type
  resolution), поэтому плагин объявлен в корне с `apply false` и применяется в `subprojects { apply(plugin = "dev.detekt") }`.
  Отдельный included build ради одного плагина не заводим.
- **`detektCheck` — своя задача.** Голая `detekt` в KMP ничего не запускает. `detektCheck` в каждом подпроекте
  собирает задачи с type resolution:

  | Модуль | Задачи | Что покрывают |
  |---|---|---|
  | KMP-модули (`core/*`, `feature/*`, `composeApp`) | `detektMainAndroid` | `commonMain` + `androidMain` |
  | | `detektMainJvm` | `commonMain` + `jvmMain` |
  | | `detektTestJvm` | `commonTest` + `jvmTest` (host-тесты) |
  | | `detektIosMainSourceSet` | `iosMain` (без type resolution) |
  | `androidApp` | `detektDebug`, `detektDebugUnitTest` | debug-вариант и его unit-тесты |
  | `desktopApp` | `detektMain`, `detektTest` | `main` и `test` |

  Dev-компиляция Compose Hot Reload (`detektDevJvm`, `detektDev`) не анализируется — это служебная компиляция с
  тем же кодом. В `check`/`build` и в сборку APK ни detekt, ни Spotless не входят: плагин detekt по умолчанию
  вешает `detekt` на `check`, Spotless — `spotlessCheck`; обе связи отключены в корневом `build.gradle.kts`
  (`isEnforceCheck = false`, фильтр зависимостей `check`), чтобы `./gradlew build` не падал на стиле
  незакоммиченных файлов.
- **Ratchet у Spotless.** По умолчанию проверяются только файлы, изменённые относительно `HEAD` — ровно то, что
  разработчик собирается коммитить; старый код не блокирует. Для проверки ветки целиком:
  `-PspotlessRatchetFrom=origin/develop`.

---

## Как не получать замечаний

Требования анализаторов вынесены в разделы «Что проверяют анализаторы» и подобные, чтобы код писался сразу
правильно, а не чинился после прогона:

- [`patterns.md`](patterns.md#что-проверяют-анализаторы-spotless--detekt) — числа, длина строки, запрещённые вызовы
  (`runCatching`, `!!`, `println`, запись `MutableStateFlow.value`), исключения, мёртвый код, имена файлов и пакетов;
- [`compose-best-prac.md`](compose-best-prac.md#10-требования-анализаторов-compose-rules) — порядок и имена
  параметров, `modifier`, ViewModel в composable, эффекты, превью, `CompositionLocal`.

---

## Команды

| Что | Команда |
|---|---|
| Проверить стиль изменённых файлов | `./gradlew spotlessCheck` |
| Исправить стиль и импорты автоматически | `./gradlew spotlessApply` |
| Проверить стиль относительно ветки | `./gradlew spotlessCheck -PspotlessRatchetFrom=origin/develop` |
| Смысловая проверка всего проекта | `./gradlew detektCheck` |
| Смысловая проверка одного модуля | `./gradlew :feature:login:detektCheck` |
| `@SerialName` у всех `@Serializable` | `config/hooks/check-serial-name.sh` |
| Строки и ключи локализации | `config/hooks/check-strings.py` |
| `MutableStateFlow` без `.value =` | `config/hooks/check-stateflow-value.py` (без аргументов — весь репозиторий) |

Порядок перед коммитом:

```bash
./gradlew spotlessApply                  # автофикс стиля и импортов
./gradlew spotlessCheck                  # убедиться, что чисто
./gradlew detektCheck                    # смысловые замечания — чинить руками
./gradlew :androidApp:assembleDebug      # сборка (см. build.md)
```

Особенности `spotlessApply`:

- Падает, если в изменённых файлах есть нарушения, которые нельзя исправить автоматически (wildcard-импорт,
  нарушение compose-правила): выводит список, чинить надо вручную, после этого повторить.
- Переписывает **все** файлы, изменённые с `HEAD`, в том числе чужие незакоммиченные. Запускать, когда в дереве
  только то, что вы собираетесь коммитить.

Git-хуки активируются один раз на клон (нужен `pre-commit`, например `brew install pre-commit`):

```bash
pre-commit install --hook-type pre-commit --hook-type pre-push
```

---

## Правила и принятые решения

### ktlint (`.editorconfig`)

- Стиль `android_studio`, длина строки 120, отступ 4.
- Включено `no-unused-imports` (в ktlint 1.8 по умолчанию выключено).
- `function-naming` не ругается на `@Composable` (PascalCase).

### compose-rules (`.editorconfig`)

Отключено осознанно:

| Правило | Причина |
|---|---|
| `modifier-missing-check`, `modifier-without-default-check` | относятся к переиспользуемым компонентам, у нас много Screen-функций |
| `state-param-check` | `State<T>` передаётся сознательно: значение читается в дочернем scope без рекомпозиции родителя ([`compose-best-prac.md`](compose-best-prac.md) §5) |

Разрешены собственные `CompositionLocal` (`compose_allowed_composition_locals`): `LocalCmpImageLoader` и
`LocalThemeIsDark` (`core/ui`). Новый `CompositionLocal` добавляется
в этот список.

Остальные правила включены. Частые требования: имена лямбд событий в настоящем времени (`onClick`, не
`onClicked`), порядок параметров «обязательные → `modifier` → с дефолтами → контентная лямбда», не прокидывать
ViewModel через несколько composable, эффекты читают лямбды через `rememberUpdatedState`.

### detekt (`config/detekt/detekt.yml`)

Включённые проверки смысла: `UnsafeCallOnNullableType`, `UnusedParameter`/`UnusedPrivate*`, `NoNameShadowing`,
`SwallowedException`, `MatchingDeclarationName`, `InvalidPackageDeclaration`, `PackageNaming`,
`TopLevelPropertyNaming`, `SuspendFunWithFlowReturnType`, `MagicNumber`, `MaxLineLength`, `LongMethod` (лимит
120 строк), `CyclomaticComplexMethod` и др.

Запрещено:

| Что | Чем заменить |
|---|---|
| `kotlin.runCatching` | `safeRun` / `safeResponseRun` из `core/common/.../ResponseExt.kt` ([`patterns.md`](patterns.md)) |
| `println` / `print` | логгер Kermit |
| `MutableStateFlow.value = …` (запись) | `flow.update { … }` ([`patterns.md`](patterns.md#корутины-и-flow)); чтение `.value` разрешено |
| комментарии `FIXME:` и `STOPSHIP:` | закрыть до коммита (обычные `TODO` разрешены) |
| импорт `androidx.lifecycle.ViewModel` вне `core/ui` (`ForbiddenImport`) | наследоваться от `BaseViewModel` ([`patterns.md`](patterns.md), «BaseViewModel») |

`ForbiddenMethodCall` не проверяет тестовые source set'ы: тестовые фейки по [`patterns.md`](patterns.md#корутины-и-flow)
могут писать `MutableStateFlow.value` напрямую.

Осознанно выключено или смягчено: `LongParameterList`, `TooManyFunctions`, `NestedBlockDepth`, `LargeClass`,
`ReturnCount`, `InjectDispatcher`, `AbstractClassCanBeInterface`, `AbstractClassCanBeConcreteClass`, `UseOrEmpty`.

`TooGenericExceptionCaught` не проверяется на границах, где по нашим правилам ловится всё и нормализуется в
`Response.Error`: `core/network` и safe*-хелперы `core/common/.../ResponseExt.kt` (`safeRun`, `safeResponseRun`,
`safeFlowRun`). Остальной код ловит конкретный тип либо идёт через `safeRun`. Если исключение
сознательно игнорируется (например, SDK бросает на недоступном транспорте и это означает «нет зависимости»), переменная
называется `ignored`: так её пропускают и `TooGenericExceptionCaught`, и `SwallowedException`
(`allowedExceptionNameRegex` по умолчанию — `_|(ignore|expected).*`).

`@Preview` не считается неиспользуемой функцией, `@Composable` не подпадает под `FunctionNaming`.

Сгенерированный код (`build/`, `generated/`) исключён. **Baseline не используется**: замечаний ноль, новые
нарушения блокируют сразу.

---

## Подавления

Подавление — исключение, а не способ пропустить проверку. Ставить его нужно на конкретное место и только если
исправление меняет поведение или вёрстку.

- **detekt:** `@Suppress("LongMethod", "CyclomaticComplexMethod")` на функции; для целого файла —
  `@file:Suppress("MatchingDeclarationName")` перед `package`.
- **compose-rules:** `@Suppress("ktlint:compose:<правило>")`. Для правил `multiple-emitters-check` и
  `lambda-param-in-effect` аннотация на функции **не срабатывает**, работает только файловая:
  `@file:Suppress("ktlint:compose:multiple-emitters-check")`.
- Не повторяйте `@Suppress` дважды на одной функции: аннотация не повторяемая, объединяйте правила в один вызов.

---

## Ограничения

- `iosMain` анализируется без type resolution (`detektIosMainSourceSet`), часть правил на нём не работает.
- В KMP-модулях type resolution пишет «compiler errors found during analysis» из-за `expect`/`actual` в одном
  модуле. Это шум, он слегка снижает точность только для этих деклараций.
- detekt 2.0 — альфа. alpha.6 собран под Kotlin 2.4.10, у нас Kotlin 2.4.0 — проверено, анализ с type resolution
  работает. Обновление Kotlin выше версии сборки detekt может ломать анализ: при апгрейде Kotlin прогнать
  `detektCheck` и при необходимости поднять detekt.
- Состав `detektCheck` — фиксированный список имён задач. Новые source set'ы (`androidHostTest`, `iosTest`,
  `appleMain` и т. п.) сами в анализ не попадут: добавить их задачи в список в корневом `build.gradle.kts`.
- Хук для агентов и `spotlessCheck` в pre-commit показывают нарушения всех файлов, изменённых с `HEAD`, поэтому
  чужая незакоммиченная работа с нарушениями будет мешать, пока не будет исправлена.

---

## Как менять правила

1. **Стиль и Compose:** править `.editorconfig` (`ktlint_standard_<правило> = disabled`,
   `ktlint_compose_<правило> = disabled`), затем прогнать `./gradlew spotlessCheck -PspotlessRatchetFrom=origin/develop`.
2. **Смысловые проверки:** править `config/detekt/detekt.yml` (в 2.0 неверное имя параметра валит запуск —
   detekt сам назовёт допустимые). Исключения (`excludes`) делать узкими — по конкретному модулю или файлу — и
   описывать причину здесь.
3. **Версии:** `gradle/libs.versions.toml` (`detekt`, `spotless`, `ktlint`, `composeRules`). Версию ktlint в
   Spotless и версию compose-rules нужно менять вместе, плагин compose-rules собран под конкретный ktlint.
4. После смены правил проверить оба прогона и сборку ([`build.md`](build.md)).
