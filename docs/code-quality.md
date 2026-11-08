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
| **Spotless** | 8.10.4 | Gradle-обёртка над ktlint + compose-rules: `spotlessCheck` / `spotlessApply`, всё дерево (опционально ratchet) | да (через ktlint) |
| **detekt** | 2.0.0-alpha.6 | смысл кода: мёртвый код, `!!`, исключения, неверный пакет, запрещённые вызовы, сложность и длина, магические числа | нет |
| **Konsist** | 0.18.1 | архитектура: направление импортов между модулями и слоями, пакет = модуль, раскладка ролей, граница SDK | нет |
| **PMD CPD** | 7.29.0 | скопированный код: одинаковые последовательности ≥ 100 токенов Kotlin | нет |
| **modules-graph-assert** | 2.9.1 | направления зависимостей между Gradle-модулями (объявленные `project(…)`) | нет |

Разделение труда: **Spotless — стиль и автофикс, detekt — смысловые замечания без автофикса**. Правила не
дублируются: форматирование, импорты и wildcard проверяет ktlint (`detekt-formatting` не подключён,
`WildcardImport` в detekt оставлен как дополнительная страховка).

---

## Как это вызывается (архитектура)

```
git commit ──► pre-commit ──► ./gradlew spotlessCheck        (стиль, всё дерево)
                          ├─► config/hooks/check-serial-name.sh (@SerialName у @Serializable, коммитимые .kt)
                          └─► config/hooks/check-strings.py     (строки, ключи, языки — docs/localization.md)
git push   ──► pre-push   ──► ./gradlew detektCheck          (смысл, весь проект, с type resolution)
                          └─► config/hooks/check-master-from-develop.sh push  (в main/master — только коммиты из develop)
git merge  ──► pre-merge-commit ──► config/hooks/check-master-from-develop.sh merge (в main/master вливается только develop)
Агент Edit/Write *.kt ──► scripts/agent-hooks/kotlin-quality.sh ──► Spotless ideHook (только этот файл)
Claude Bash          ──► scripts/agent-hooks/protect-main.sh   (не вливать/пушить в main/master)
```

| Файл | Роль |
|---|---|
| [`build.gradle.kts`](../build.gradle.kts) | блок `spotless { … }` (один корневой на весь репозиторий): цели, версии ktlint/compose-rules, ratchet; блок `subprojects { … }`: применение `specmp.detekt` ко всем Kotlin-модулям |
| [`build-logic/.../DetektConventionPlugin.kt`](../build-logic/src/main/kotlin/DetektConventionPlugin.kt) | convention `specmp.detekt`: конфиг, свой набор правил (`detektPlugins`), исключение `build/`, отвязка от `check`, задача `detektCheck` модуля |
| [`quality/konsist`](../quality/konsist) | архитектурные тесты Konsist (`ArchitectureTest`): читают исходники как текст, к сборке не относятся |
| [`quality/detekt-rules`](../quality/detekt-rules) | свои правила detekt (набор `specmp`, `SpecmpRuleSetProvider`) и их тесты; сам модуль detekt не проверяет |
| [`build.gradle.kts`](../build.gradle.kts) | задача `cpdCheck` (конфигурация `cpd`: PMD CLI + `pmd-kotlin`); плагин `module-graph-assert` и блок `moduleGraphAssert { … }` — задача `assertModuleGraph` |
| [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) | версии `detekt`, `spotless`, `ktlint`, `composeRules`, `junit`; плагины `detekt`, `spotless`; `detekt-api`/`detekt-test` для своих правил |
| [`.editorconfig`](../.editorconfig) | правила ktlint и compose-rules (стиль, отключённые правила, allowlist `CompositionLocal`) |
| [`config/detekt/detekt.yml`](../config/detekt/detekt.yml) | только отличия от дефолтов detekt и раздел `specmp:` своих правил |
| [`.pre-commit-config.yaml`](../.pre-commit-config.yaml) | локальные хуки `ai-generate`, `spotless-check`, `serial-name-check`, `strings-check` (pre-commit), `detekt-check`, `master-from-develop-push` (pre-push) и `master-from-develop-merge` (pre-merge-commit) |
| [`config/hooks/check-master-from-develop.sh`](../config/hooks/check-master-from-develop.sh) | в `main`/`master` — только `develop`: `merge` проверяет вливаемую ветку, `push` — что каждый новый коммит достижим из `develop` (или это merge-коммит `develop` → `master`); fast-forward чужой ветки ловит `push` |
| [`config/hooks/check-strings.py`](../config/hooks/check-strings.py) | строки: только в `core/localization`, формат ключей, одинаковые ключи во всех языках ([`localization.md`](localization.md)) |
| [`config/hooks/check-serial-name.sh`](../config/hooks/check-serial-name.sh) | у каждого `@Serializable` есть `@SerialName` (`docs/patterns.md`, «Сериализация»); без аргументов — весь репозиторий |
| [`scripts/agent-hooks/kotlin-quality.sh`](../scripts/agent-hooks/kotlin-quality.sh) | хук агентов (Claude, Gemini, Codex) после правки `.kt`/`.kts`: Spotless `ideHook` по этому файлу, без git; вручную — `scripts/agent-hooks/kotlin-quality.sh <файлы>` (plain-отчёт, exit 1 при замечаниях) |
| [`.ai/hooks/*.json`](../.ai/hooks) | регистрация хуков: `.ai/generate.sh` сливает их в `.claude/settings.json`, `.gemini/settings.json`, `.codex/hooks.json` (остальные ключи сохраняются); руками сгенерированные файлы не править |

Почему так устроено:

- **Spotless один, на корне.** Отдельные ktlint-запуски по модулям заметно медленнее, а правила одинаковы для всех.
  Файлы для обхода задаются через `fileTree` с исключением `build/`, `generated/`, `.gradle/`, `.kotlin/`: иначе
  Spotless заходит в build-выходы (в том числе `iosApp/build`) и сгенерированные `Res`. `kotlinGradle` охватывает
  `*.gradle.kts` до трёх уровней вложенности (`feature/device/<module>/build.gradle.kts`).
- **detekt — convention `specmp.detekt` из `build-logic`, применяется из корня.** Он нужен в каждом модуле (свои
  source set'ы и type resolution), поэтому корневой `build.gradle.kts` вешает его на все модули с плагином KMP,
  `kotlin("android")`, `kotlin("jvm")` или `com.android.application` (кроме `quality/*`) — в build-файлах модулей
  detekt не упоминается.
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
  тем же кодом. `spotlessCheck` входит в `check`/`build`; detekt — нет: плагин
  detekt по умолчанию вешает `detekt` на `check`, связь отключена фильтром зависимостей `check` в `build-logic`.
  В сборку APK ни detekt, ни Spotless не входят.
- **Spotless — всё дерево.** По умолчанию проверяются все `**/src/**/*.kt` и `*.gradle.kts`, git не нужен.
  Ограничить файлами, изменёнными относительно ветки: `-PspotlessRatchetFrom=origin/develop`.
- **`.editorconfig` и Spotless.** `ij_kotlin_allow_trailing_comma*` и `ktlint_ignore_back_ticked_identifier`
  Spotless из файла не берёт — они продублированы `editorConfigOverride` в корневом `build.gradle.kts`; держать
  значения в синхроне. Висячие запятые обязательны (ktlint `trailing-comma-*`).

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
| Проверить стиль всего дерева | `./gradlew spotlessCheck` |
| Исправить стиль и импорты автоматически | `./gradlew spotlessApply` |
| Проверить стиль относительно ветки | `./gradlew spotlessCheck -PspotlessRatchetFrom=origin/develop` |
| Автофикс одного файла, не трогая остальные | `./gradlew spotlessApply -PspotlessIdeHook=<абсолютный путь>` |
| Проверка правленых файлов (как хук агентов) | `scripts/agent-hooks/kotlin-quality.sh <файлы>` |
| Смысловая проверка всего проекта | `./gradlew detektCheck --continue` |
| Смысловая проверка одного модуля | `./gradlew :feature:login:detektCheck` |
| Тесты своих правил detekt | `./gradlew :quality:detekt-rules:test` |
| Архитектурные правила (Konsist) | `./gradlew :quality:konsist:test` |
| Скопированный код (CPD) | `./gradlew cpdCheck` |
| Направления зависимостей модулей | `./gradlew assertModuleGraph` |
| Граф модулей: статистика / Graphviz | `./gradlew generateModulesGraphStatistics` / `./gradlew generateModulesGraphvizText -Pmodules.graph.output.gv=<файл>` |
| Сверка сгенерированных агентских конфигов | `.ai/generate.sh --check` |
| `@SerialName` у всех `@Serializable` | `config/hooks/check-serial-name.sh` |
| Строки и ключи локализации | `config/hooks/check-strings.py` |

Порядок перед коммитом:

```bash
./gradlew spotlessApply && ./gradlew spotlessCheck
./gradlew :androidApp:assembleDebug :flasherAndroidApp:assembleDebug \
    :desktopApp:compileKotlin :flasherDesktopApp:compileKotlin        # сборка (см. build.md)
./gradlew :<модуль>:jvmTest                # тесты затронутых модулей
./gradlew detektCheck --continue           # смысловые замечания — чинить руками
./gradlew :quality:konsist:test            # если менялась структура: классы, пакеты, импорты между слоями/модулями
./gradlew cpdCheck
./gradlew assertModuleGraph                # если менялись *.gradle.kts, build-logic или libs.versions.toml
./gradlew :quality:detekt-rules:test       # если менялись свои правила detekt
```

Особенности `spotlessApply`:

- Без ratchet переписывает **все** файлы репозитория, с `-PspotlessRatchetFrom=<ref>` — все изменённые
  относительно ref, включая чужие незакоммиченные. Если в дереве не только своё — автофикс по одному файлу
  (`-PspotlessIdeHook=<путь>`).
- Ratchet проверяет только изменённые файлы, но **файл целиком**: тронул старый файл — он проверяется весь.
- Файл с неавтофиксимым нарушением (wildcard-импорт, compose-правило) Spotless не форматирует вообще и показывает
  только первое нарушение в нём. Исправь его и снова запусти `spotlessApply` — всплывут следующие.
- После правки своих правил detekt или `.editorconfig` — `./gradlew --stop`: daemon держит старые классы и конфиг.

Git-хуки активируются один раз на клон (нужен `pre-commit`, например `brew install pre-commit`):

```bash
pre-commit install   # типы хуков (pre-commit, pre-push, pre-merge-commit) — default_install_hook_types в конфиге
```

---

## Хуки агентов

[`scripts/agent-hooks/kotlin-quality.sh`](../scripts/agent-hooks/kotlin-quality.sh) проверяет **только переданные
`.kt`/`.kts`** (прочие файлы — мгновенный выход 0) и не зависит от агента:

- **Spotless** — через `spotlessApply -PspotlessIdeHook=<файл> -PspotlessIdeHookUseStdOut`: файл не переписывается,
  git и чужие незакоммиченные файлы не влияют. Неавтофиксимое нарушение показывается первым (как и в
  `spotlessCheck`), автофиксимое — диффом «как должно быть» и командой автофикса этого файла.
- detekt, Konsist, CPD хук не запускает (медленно) — они на pre-push и в ручном прогоне перед коммитом.
- Отчёт — по каждому файлу с замечаниями (до 60 строк на файл).

Вход и выход:

| Кто | Вход | `--format` | Ответ при замечаниях |
|---|---|---|---|
| Человек / агент без хуков | пути аргументами | `plain` (по умолчанию) | отчёт в stdout, exit 1 |
| Claude Code, `PostToolUse` `Edit\|Write` | stdin: `tool_input.file_path` | `claude` | stderr + exit 2 |
| Gemini CLI, `AfterTool` `write_file\|replace` | stdin: `tool_input.file_path` (относительный — от `cwd`) | `gemini` | exit 0 + JSON `hookSpecificOutput.additionalContext` |
| Codex CLI, `PostToolUse` `apply_patch\|Edit\|Write` | stdin: патч в `tool_input.command`, пути из `*** Update File:` / `*** Add File:` / `*** Move to:` | `codex` | exit 0 + JSON `{"decision":"block","reason":…}` |

Без `--format` формат stdin определяется по payload: `hook_event_name: AfterTool` → gemini; `tool_name:
apply_patch` или поле `turn_id` → codex; иначе claude. Правка к моменту хука уже применена — агент чинит следующим
шагом. Без `python3` разбирается только `file_path`, ответ — как у claude.

```bash
scripts/agent-hooks/kotlin-quality.sh feature/x/src/.../A.kt feature/x/src/.../B.kt   # вручную
printf '{"tool_input":{"file_path":"%s"}}' "$PWD/<путь>.kt" | scripts/agent-hooks/kotlin-quality.sh --format claude
```

[`scripts/agent-hooks/protect-main.sh`](../scripts/agent-hooks/protect-main.sh) — `PreToolUse` `Bash` у Claude:
блокирует (exit 2) пуш в `main`/`master`, слияние в них и любые изменения истории, пока `HEAD` на `main`/`master`
(правило веток — `.ai/rules.md`). Для людей — `config/hooks/check-master-from-develop.sh` (pre-push и pre-merge-commit).

Регистрация — версионируемые фрагменты в [`.ai/hooks/`](../.ai/hooks/): `claude-settings.json` →
`.claude/settings.json`, `gemini-settings.json` → `.gemini/settings.json`, `codex-hooks.json` → `.codex/hooks.json`.
После изменения фрагмента запусти `.ai/generate.sh`: он вмерживает фрагменты, сохраняя остальные ключи и заменяя
запись с той же `command` или тем же скриптом; `.claude/settings.local.json` не трогается. Результат коммитится
вместе с фрагментом — `.ai/generate.sh --check` (pre-commit `ai-generate`) ловит расхождение и неотслеживаемые
сгенерированные файлы. Особенности:

- **Gemini CLI** и **Codex** запускают проектные хуки только после доверия: Gemini — fingerprint хука (изменённая
  команда снова недоверенная), Codex — доверенный слой `.codex/` и ревью хука через `/hooks`.
- Остальные агенты (Cursor, Kimi Code и пр.) хук не подключают — для них правило в `.ai/rules.md` → «Validation»:
  запусти скрипт вручную по изменённым файлам.

Когда что запускается:

| Момент | Что | Объём |
|---|---|---|
| Правка файла агентом (или вручную) | `kotlin-quality.sh`: Spotless (ideHook) | правленые `.kt`/`.kts` |
| `git commit` (pre-commit) | `spotlessCheck`, `check-serial-name.sh`, `check-strings.py`, `.ai/generate.sh --check` | Spotless — всё дерево (неиндексированное pre-commit прячет), если в коммите есть `.kt`/`.kts` |
| `git push` (pre-push) | `detektCheck`, `check-master-from-develop.sh push` | весь проект |
| Перед коммитом, руками | сборка, `jvmTest`, `cpdCheck`, `:quality:konsist:test`, `assertModuleGraph` | см. «Команды» |

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
| `runCatching` (`specmp:ForbiddenRunCatching`) | `safeRun` / `safeResponseRun` из `core/common/.../ResponseExt.kt` ([`patterns.md`](patterns.md)) |
| `println` / `print` (`specmp:ForbiddenPrintln`) | логгер Kermit |
| `MutableStateFlow.value = …`, `+=`, `++` (`specmp:StateFlowValueMutation`) | `flow.update { … }` ([`patterns.md`](patterns.md#корутины-и-flow)); чтение `.value` разрешено |
| комментарии `FIXME:` и `STOPSHIP:` | закрыть до коммита (обычные `TODO` разрешены) |
| импорт `androidx.lifecycle.ViewModel` вне `core/ui` (`ForbiddenImport`) | наследоваться от `BaseViewModel` ([`patterns.md`](patterns.md), «BaseViewModel») |
| импорт `androidx.compose.material3.*` вне `core/ui` (`ForbiddenImport`) | компоненты `Cmp*` из `core/ui` ([`ui-kit.md`](ui-kit.md)) |

`StateFlowValueMutation` не проверяет тестовые source set'ы: тестовые фейки по [`patterns.md`](patterns.md#корутины-и-flow)
могут писать `MutableStateFlow.value` напрямую. Правило берёт тип ресивера из Analysis API (Compose `MutableState`
не попадает) и молчит без type resolution — в `iosMain`.

#### Свои правила (`quality/detekt-rules`, набор `specmp`)

Набор из BabySteps (тот же detekt 2.0.0-alpha.6): правила — в `quality/detekt-rules/src/main/kotlin/.../detekt`,
регистрация — `SpecmpRuleSetProvider` (+ `META-INF/services`), конфиг — раздел `specmp:` в `detekt.yml`.
`SpecmpRuleSetProviderTest` проверяет, что каждое правило зарегистрировано и есть в `detekt.yml`. Новое правило:
класс + тест на `detekt-test` → строка в провайдере и в тесте провайдера → раздел в `detekt.yml` → проверка на
временном нарушителе (`detektCheck` падает) → нарушителя удалить. Правила ViewModel, `Response`, данных и времени
перенесены, но выключены (`active: false`) до подэтапов миграции, где убираются их нарушения. Подавление своего
правила — `@Suppress("ForbiddenPrintln")` (имя правила без набора) с комментарием «почему».

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

### Konsist (`quality/konsist`)

`ArchitectureTest` (JUnit 5) — порт из BabySteps: Konsist разбирает исходники `core/*`, `feature/*`, composition roots
(`composeApp`, `flasherApp`) и обёрток без тестовых source set'ов. Имя модуля Konsist — путь до `src/`
(`feature/device/settings-ui`). `testName` правила — его id; сообщение правила ссылается на раздел доков.

| id | Что проверяет |
|---|---|
| `module-direction` | `core/*` импортирует только `core/*`, `feature/*` — `core/*` и `feature/*`; пакеты приложений не импортируются |
| `module-package` | пакет = базовый пакет модуля (`feature/auth-state` → `…feature.authstate`, суффикс `-ui` → `.ui`) |
| `domain-isolation` | `domain` без data/presentation/di, Compose, ресурсов, `core.ui`, multiplatform-settings, сериализации и SDK |
| `presentation-isolation` | пакеты `..data..` видят только data/di фичи, не-presentation core и di приложений |
| `data-isolation` | `data` не импортирует presentation и di |
| `feature-data-isolation` | фича не импортирует data другой фичи |
| `layer-placement` | класс `*Repository` (реализация, `Specialist*Repository`) — в `data/repository`; `*DataSource` — `data/datasource`; `*Coordinator`/`*Controller` — интерфейс в `domain/service` |
| `repository-in-domain` | интерфейс `*Repository` фичи — в `domain` |
| `domain-model-purity` | `domain/model`: только `val`, read-only коллекции, без `@Serializable` |
| `serializable-placement` | `@Serializable` — в `..data..`, `..navigation..`, у `NavKey` и типов аргументов маршрутов |
| `composable-in-presentation` | `@Composable` фичи — только в `presentation` |
| `sdk-confinement` | типы AlarmTrade SDK (`ru.alarmtrade.*` вне `ru.alarmtrade.specmp`) — data/di фич, `core/specialist`, `core/permissions`, `core/network`, приложения |
| `no-fakes-in-production` | `Fake*`/`Stub*` — только в тестах |
| `forbidden-suffix` | `*Helper`/`*Utils` запрещены; `*Manager` — только в `androidMain`/`iosMain`/`jvmMain` |
| `top-level-init-order` | верхнеуровневое свойство не читает свойство, объявленное ниже в файле |
| `data-source-consumers` | `*DataSource` в конструкторе — только у реализаций репозиториев и составных `*DataSource` |

Остальные правила BabySteps (ViewModel, контракт репозиториев, видимость `*Impl`, `koin-module`,
`mapper-placement`, Koin в composable и др.) добавляются в подэтапах миграции, где убираются их нарушения.
Подавление — `@Suppress("konsist.<id>")` (для файла — `@file:Suppress`) с причиной комментарием строкой выше.
Новое правило проверяется на временном нарушителе (тест падает), затем нарушитель удаляется.

### CPD (`cpdCheck`)

Корневая задача `cpdCheck` (`JavaExec` над CLI PMD 7, язык `kotlin`) падает на любой последовательности от **100**
одинаковых токенов. Baseline нет. Проверяются `src/**/*.kt` модулей `core/*`, `feature/*`, `composeApp`, `flasherApp`,
`quality/*` (все продакшен-source set'ы, iOS читается как текст); исключены `build/`, `generated/` и тестовые source
set'ы (`src/test`, `src/*Test`) — фикстуры тестов повторяются намеренно. `--ignore-identifiers`/`--ignore-literals` не
используются. В `check`/`build` задача не входит.

**Нашлась копия — вынеси общее, а не исключай:** внутри фичи — `internal`-функция/composable (`FirmwareCenteredBody`,
`FirmwareLoadError`, `CheckDialogTransitions.kt` в `feature:device:firmware`); общее двух корней (`composeApp`,
`flasherApp`) — в фичу или `core/*` (`rememberFirmwareFilePicker()` в `feature:device:firmware`, `core/common/file`,
`specialistModules()`, `localizationModule`), корни друг от друга не зависят. Исключение по пути — только для повторов
по построению, с комментарием-причиной рядом с путём в `cpdCheck`.

### graph-assert (`assertModuleGraph`)

Плагин `com.jraska.module.graph.assertion` подключён к корневому проекту и строит граф по объявленным `project(…)` в
конфигурациях из `moduleGraphAssert.configurations` (`api`/`implementation`, KMP `commonMain`/`androidMain`/`iosMain`/
`jvmMain`, `commonTestImplementation`, `jvmTestImplementation`). Импорты в коде проверяет Konsist (`module-direction`).

| Правило | Смысл |
|---|---|
| `:core:.* -X> :feature:.*` | core не зависит от фич |
| `:(core\|feature):.* -X> :(composeApp\|flasherApp)` | composition roots — листья для core и фич |
| `.* -X> :(androidApp\|desktopApp\|flasherAndroidApp\|flasherDesktopApp)` | от платформенных обёрток не зависит никто |
| `.* -X> :quality:.*`, `:quality:.* -X> :(core\|feature):.*` | инструменты проверок отделены от продукта |
| `:feature:.* -X> :feature:.*-ui` | от `*-ui` зависят только composition roots |

Временный allowlist последнего правила (в регэкспе, снимается в подэтапе, где ребро убрано): `advanced-mount`,
`fast-mount`, `configurator-ui`, `device:{autostart,firmware,state,settings-ui}` → `device:interaction-ui`
(уходит с `presentation-core`); `configurator-ui` → `device:settings-ui`. Регэксп матчит всю строку
`"<from> -X> <to>"`, поэтому конец имени модуля слева в негативном lookahead — пробел, а не `$`.

---

## Подавления

Подавление — исключение, а не способ пропустить проверку. Ставить его нужно на конкретное место и только если
исправление меняет поведение или вёрстку.

- **detekt:** `@Suppress("LongMethod", "CyclomaticComplexMethod")` на функции; для целого файла —
  `@file:Suppress("MatchingDeclarationName")` перед `package`.
- **Konsist:** `@Suppress("konsist.<id>")` на декларации, `@file:Suppress("konsist.<id>")` на файле.
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
  `appleMain` и т. п.) сами в анализ не попадут: добавить их задачи в список в `build-logic/src/main/kotlin/DetektConventionPlugin.kt`.
- `spotlessCheck` в pre-commit проверяет всё дерево: чужая проиндексированная работа с нарушениями остановит
  коммит, пока не будет исправлена. Хук агентов смотрит только правленый файл.

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
