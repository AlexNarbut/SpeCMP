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

Разрешены собственные `CompositionLocal` (`compose_allowed_composition_locals`): `LocalCmpImageLoader`,
`LocalThemeIsDark`, `LocalClock`, `LocalTimeZoneSource` (`core/ui`) и `LocalWizardWide` (`fast-mount`, ширина окна
мастера на все его шаги). Новый `CompositionLocal` добавляется в этот список.

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

`StateFlowValueMutation` не проверяет тестовые source set'ы и модули тестовых дублёров (`**/testing/src/**`): тестовые фейки по [`patterns.md`](patterns.md#корутины-и-flow)
могут писать `MutableStateFlow.value` напрямую. Правило берёт тип ресивера из Analysis API (Compose `MutableState`
не попадает) и молчит без type resolution — в `iosMain`.

#### Свои правила (`quality/detekt-rules`, набор `specmp`)

Набор из BabySteps (тот же detekt 2.0.0-alpha.6): правила — в `quality/detekt-rules/src/main/kotlin/.../detekt`,
регистрация — `SpecmpRuleSetProvider` (+ `META-INF/services`), конфиг — раздел `specmp:` в `detekt.yml`.
`SpecmpRuleSetProviderTest` проверяет, что каждое правило зарегистрировано и есть в `detekt.yml`. Новое правило:
класс + тест на `detekt-test` → строка в провайдере и в тесте провайдера → раздел в `detekt.yml` → проверка на
временном нарушителе (`detektCheck` падает) → нарушителя удалить. Правила ViewModel (`ViewModel*`,
`SafeRunInViewModel`) включены без исключений для наследников (`ViewModelContractNames` пропускает собственные
параметры типа промежуточной базы `BaseDeviceViewModel`). `RawFlowFromRepository` (публичный поток реализации
`*Repository` без `Response`) и `RepositoryWithContext` (исключения структурные: класс со свойством/параметром/супертипом `CoroutineScope` и
`withContext(NonCancellable)` — без `@Suppress`) включены, как и все остальные правила набора (`Response`, данные, время,
`SettingsInstantiation`).

Пакет «гонки и корутины» ([`patterns.md`](patterns.md), «Конкурентность»; тесты вне охвата):

| Правило | Что ловит |
|---|---|
| `UpdateLambdaSideEffects` | в лямбде `MutableStateFlow.update/getAndUpdate/updateAndGet` (через `inline`-лямбды): suspend-вызов, вызов чужого `*Repository`/`*DataSource`/`*Coordinator`/`*Controller`/`*Scheduler`/`*Service`/`*Api`/`*Tracker`, запись `put…/set…/remove…/save…/write…` у `*Settings`/`*Store`/`*Storage`/`*Preferences`/`*Cache`, `emit/tryEmit/send/trySend/postSideEffect`, `launch/async/launchIn`; метод своего класса — на один уровень |
| `CatchSwallowsCancellation` | `catch (Throwable/Exception/RuntimeException/IllegalStateException)` в корутине, `try` которого содержит suspend-вызов, без проброса `CancellationException` |
| `MutexIsLockedCheck` | чтение `Mutex.isLocked` |
| `NestedSameMutexLock` | `m.withLock` внутри `m.withLock` в одной функции (в т. ч. через свой метод); внутри `launch`/`async` — не считается |
| `SuspendInsideSynchronized` | suspend-вызов между `lock()` и `unlock()` (`java.util.concurrent.locks.Lock`), в `synchronized { }`/`@Synchronized` |
| `SharedMutableStateInSingleton` | `var` и `val` с изменяемой коллекцией у класса со своим `CoroutineScope` или сервиса data-слоя (`..data..`, `*Repository`/`*Coordinator`/`*Controller`/`*Session`/`*Impl`) |
| `UnmanagedCoroutineScope` | `CoroutineScope(…)` вне пакета `di` без `<holder>.cancel()`; контекст без `SupervisorJob()` или обработчика — в том числе в `di`; фабрика `core/common/coroutines/ServiceScope.kt` исключена |
| `SharingInsideFunction` | `.shareIn`/`.stateIn` в теле функции или геттера |
| `UnawaitedAsync` | `async { }` оператором или в неиспользуемой переменной |
| `NestedCollect` | `collect`/`collectLatest` внутри лямбды `collect`/`collectLatest` |

`UpdateLambdaSideEffects`, `CatchSwallowsCancellation`, `MutexIsLockedCheck`, `SuspendInsideSynchronized`,
`UnawaitedAsync` требуют type resolution (в `iosMain` молчат), остальные — по именам. Подавление своего
правила — `@Suppress("ForbiddenPrintln")` (имя правила без набора) с комментарием «почему».

Осознанно выключено или смягчено: `LongParameterList` (наборы колбэков `*Actions` длинные по построению; порог
конструктора ViewModel — Konsist `view-model-constructor`), `TooManyFunctions`, `NestedBlockDepth`, `LargeClass`,
`ReturnCount`, `AbstractClassCanBeInterface`, `AbstractClassCanBeConcreteClass`, `UseOrEmpty`.

Корутины (раздел `coroutines`): `GlobalCoroutineUsage` и `SuspendFunSwallowedCancellation` включены (в detekt 2.0
выключены по умолчанию), `InjectDispatcher` включён везде, кроме самого `AppDispatchers.kt` (диспетчеры — из DI);
`SleepInsteadOfDelay`, `RedundantSuspendModifier`, `SuspendFunWithFlowReturnType` включены по умолчанию.

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
(`composeApp`, `flasherApp`) и обёрток без тестовых source set'ов и без модулей тестовых дублёров `*/testing`
(их `commonMain` — тестовый код). Имя модуля Konsist — путь до `src/`
(`feature/device/settings-ui`). `testName` правила — его id; сообщение правила ссылается на раздел доков.

| id | Что проверяет |
|---|---|
| `module-direction` | `core/*` импортирует только `core/*`, `feature/*` — `core/*` и `feature/*`; пакеты приложений не импортируются |
| `module-package` | пакет = базовый пакет модуля (`feature/auth-state` → `…feature.authstate`, суффиксы `-ui`/`-core` → `.ui`/`.core`) |
| `domain-isolation` | `domain` без data/presentation/di, Compose, ресурсов, `core.ui`, multiplatform-settings, сериализации и SDK |
| `presentation-isolation` | пакеты `..data..` видят только data/di фичи, не-presentation core и di приложений |
| `data-isolation` | `data` не импортирует presentation и di |
| `feature-data-isolation` | фича не импортирует data другой фичи |
| `layer-placement` | класс `*Repository` (реализация, `Specialist*Repository`) — в `data/repository`; `*DataSource` — `data/datasource`; `*Coordinator`/`*Controller` — интерфейс в `domain/service` |
| `repository-in-domain` | интерфейс `*Repository` фичи — в `domain` |
| `repository-impl` | класс `*Repository`/`*RepositoryImpl` реализует интерфейс, которым оканчивается его имя (`SpecialistFooRepository : FooRepository`) |
| `data-visibility` | декларации верхнего уровня в `..data..` — `internal`/`private` |
| `impl-visibility` | реализации — `*Impl` и классы `..data..` с супертипом `*Repository` (`Specialist*Repository`) — `internal`/`private` |
| `koin-wiring` | реализацию (`*Impl`, класс `..data..` с супертипом `*Repository`) создаёт (`Foo(…)`, `::Foo`) только пакет `di` |
| `koin-module` | Koin-модуль (свойство типа `Module` или `= module {…}`) — `val <name>Module` в пакете `di` |
| `domain-model-purity` | `domain/model`: только `val`, read-only коллекции, без `@Serializable` |
| `serializable-placement` | `@Serializable` — в `..data..`, `..navigation..`, у `NavKey` и типов аргументов маршрутов |
| `composable-in-presentation` | `@Composable` фичи — только в `presentation` |
| `state-immutable` | `data class *State` в `presentation` помечен `@Immutable` или `@Stable` |
| `sdk-confinement` | типы AlarmTrade SDK (`ru.alarmtrade.*` вне `ru.alarmtrade.specmp`) — data/di фич, `core/specialist`, `core/permissions`, `core/network`, приложения |
| `no-fakes-in-production` | `Fake*`/`Stub*` — только в тестах |
| `testing-modules-test-only` | `project(":…:testing")` в `build.gradle.kts` — только в тестовом блоке/конфигурации (`commonTest.dependencies`, `*TestImplementation`); проверка разбирает скрипты, а не Kotlin-код |
| `forbidden-suffix` | `*Helper`/`*Utils` запрещены; `*Manager` — только в `androidMain`/`iosMain`/`jvmMain`; `*Store`/`*Storage` в фичах — нет (это `*Repository`) |
| `mapper-placement` | top-level `toDomain*`/`to*Dto` — `internal`/`private` в пакете `data.mapper` (в `core` — `mapper`) своего модуля |
| `storage-usage` | в фиче `com.russhwolf.settings.*` и `core.common.settings.*` — только `data` и `di` |
| `use-case-package` | `*UseCase` — в `domain/usecase` фичи (в `core` — пакет `usecase`) |
| `use-case-contract` | `*UseCase` — ≥ 2 зависимостей-ролей в конструкторе (`*Repository`/`*UseCase`/`*Coordinator`/`*Controller`/`*Service`/`*Session`/`*Closeable`, `List<…>` — по элементу) и единственный публичный `operator fun invoke`: `suspend` → `Response`, иначе `Flow`/`StateFlow<Response>` |
| `capability-usage` | API `core.permissions` (`PermissionsProvider`, `BindEffect`, `permissionsModule`, `blePermissions`) и moko-permissions в фиче — только `data`/`di`/файл ViewModel; порты `presentation/platform` — только presentation своего модуля, `di`, приложения; общие порты `device:presentation-core` — presentation любой фичи |
| `cross-feature-presentation` | фича не импортирует `presentation` другого модуля (кроме общего `device:presentation-core`); экраны другой фичи — слот из composition root |
| `foreground-usage` | импорт `core.lifecycle` (кроме `core.lifecycle.di`) — только `data`, `di`, файлы ViewModel и сам `core/lifecycle` |
| `top-level-init-order` | верхнеуровневое свойство не читает свойство, объявленное ниже в файле |
| `data-source-consumers` | `*DataSource` в конструкторе — только у реализаций репозиториев и составных `*DataSource` |
| `repository-impl-tested` | у реализации репозитория (класс `..data..` с супертипом `*Repository`) есть тест в тестовом source set её модуля, использующий класс по имени в коде (не в `import`/KDoc/строке); временный allowlist `UNTESTED_REPOSITORIES` — репозитории, ещё не покрытые тестом |
| `view-model` | ViewModel наследует `BaseViewModel` (в т.ч. через `BaseDeviceViewModel`), имя `*ViewModel`, в фиче — пакет `presentation` |
| `view-model-owns-no-flows` | у наследника `BaseViewModel` нет своих `MutableStateFlow`/`MutableSharedFlow`/`Channel`/`sideEffectFlow`/`mutableStateOf` |
| `repository-contract` | члены интерфейсов `*Repository` и интерфейсов пакета `domain/repository`: `suspend` → `Response<…>`, `Flow`/`StateFlow`/`SharedFlow` → `…<Response<…>>`; синхронных функций и свойств-не-потоков нет |
| `service-contract` | члены интерфейсов `*Coordinator`/`*Controller` и пакета `domain/service`: `suspend` → `Response<…>`, потоки → `…<Response<…>>`, синхронная функция — только команда с результатом `Unit`/`Boolean`; состояние сервиса в памяти (`DeviceSession.state`/`reboot`/`transport`) — `@Suppress` с причиной |
| `no-constructor-side-effects` | у класса нет `init { }` (кроме `AndroidNetworkMonitor`/`IosNetworkMonitor` — регистрация в ОС) и инициализаторов свойств, запускающих корутины (`launch`/`async`/`launchIn` без `CoroutineStart.LAZY`, `SharingStarted.Eagerly`); геттер и `by lazy` — можно |
| `start-once-cas` | класс с `start()` без параметров не держит guard в `var` (`Job`, `Boolean`, инициализатор `true`/`false`/`launch`) — guard `MutableStateFlow(false).compareAndSet(false, true)` |
| `view-model-dependencies` | параметры конструктора VM — domain/presentation фич, `AppDispatchers` (диспетчер выбирает VM — [`patterns.md`](patterns.md), «Диспетчеры»), `AppRestarter`, `AppVersionProvider`, `AppForegroundProvider`, `PermissionsProvider`, `AppLocaleStore`, `ConfigurationLanguageStore`, `TimeSource`, `Clock`/`TimeZoneSource` («Источник времени»), `Logger`, типы Kotlin; без функциональных типов и `CoroutineScope` |
| `view-model-constructor` | у наследника `BaseViewModel` не больше 12 параметров первичного конструктора |
| `no-service-locator` | Koin как сервис-локатор — `org.koin.core.component.*` (`KoinComponent`, `inject`, `get`), `org.koin.core.context.*` (`GlobalContext`, `startKoin`, `loadKoinModules`), `org.koin.mp.KoinPlatformTools`/`KoinPlatform`, `org.koin.java.KoinJavaComponent`, `org.koin.compose.getKoin`, `org.koin.core.Koin`, `org.koin.android.ext.android.*` — импортом (включая алиас) или полным именем — только пакеты `..di..`, composition roots и обёртки (`composeApp`, `flasherApp`, `androidApp`, `desktopApp`, `flasherAndroidApp`, `flasherDesktopApp`) и файлы с Android-компонентом, который создаёт система (`Activity`/`Application`/`Service`/`BroadcastReceiver`/`Worker`/`Initializer`…). Нарушение — декларация верхнего уровня (подавление — `@Suppress` на ней), импорт со звёздочкой и неиспользованный импорт — сам импорт. `koinInject` в composable — правило `composable-injection` |
| `composable-injection` | Koin-API для зависимостей в composable — `org.koin.compose.koinInject`, `currentKoinScope`, `LocalKoinScope`, `LocalKoinApplication` (импортом, алиасом, полным именем или звёздочкой `org.koin.compose.*`) — только корни композиции `composeApp`, `flasherApp`; `koinViewModel` разрешён. Фича получает зависимость параметром из корня (`FirmwareBootGraph(archivePicker = …)`, `rememberFirmwareFilePicker(appScope, ioDispatcher)`), через ViewModel или `CompositionLocal`, заданный в корне. Нарушение — декларация верхнего уровня |

Остальные правила BabySteps добавляются в подэтапах миграции, где убираются их нарушения.
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

Общий presentation фич устройства — не `-ui`, а `device:presentation-core`: на него правило не распространяется.
Модули тестовых дублёров `*:testing` подчиняются тем же рёбрам (`core:testing` ↛ фичи); что они подключаются только
в тестовых конфигурациях, проверяет Konsist `testing-modules-test-only` — набор конфигураций у плагина общий на все правила.
Экраны другой фичи подключаются слотом (`@Composable`-колбэком) из composition root, импорты — Konsist
`cross-feature-presentation`. Регэксп правила матчит всю строку `"<from> -X> <to>"`: для исключения через негативный
lookahead конец имени модуля слева — пробел, а не `$`.

---

## Правило → проверка

Правила из [`patterns.md`](patterns.md) держатся проверками, не документом. Новое правило записывается в `patterns.md`
вместе с id проверки и строкой здесь; проверка включается вместе с исправлением всех нарушений одним изменением.
Правила `patterns.md` без id — договорённости ревью. ⏳ — правило перенесено из BabySteps, но ещё выключено или не
написано: включается в подэтапе миграции архитектуры, где убираются его нарушения.

| Правило | Проверка | Статус |
|---|---|---|
| Публичный API ViewModel — только `state`, `sideEffect` и интенты с результатом `Unit` (не `suspend`) | detekt `ViewModelPublicApi` | ✅ |
| У ViewModel нет `var`-свойств и изменяемых коллекций (включая `private`/`lateinit`, вложенные классы и конструктор): служебное — в state, перезапуск корутины — `launchReplacing` | detekt `ViewModelMutableProperty` | ✅ |
| ViewModel не держит своих `Mutable*Flow`/`Channel`/`mutableStateOf`: входы подписки — поля state + `select { }`, «Повторить» — `reload()`/`launchReplacing` | Konsist `view-model-owns-no-flows` | ✅ |
| ViewModel — наследник `BaseViewModel<S, E>` (в т.ч. через `BaseDeviceViewModel`), имя `*ViewModel`, в фиче — в `presentation`; дженерики `…State`/`…Effect`\|`…SideEffect`\|`Nothing` | Konsist `view-model`, detekt `ViewModelContractNames` | ✅ |
| Корутины VM — `intent`/`safeLaunch`/`launchReplacing`, не `viewModelScope.launch`/`launchIn`; без `init { }` (старт — `onStart()`); в файле VM — только VM | detekt `ViewModelScopeLaunch`, `ViewModelInitBlock`, `ViewModelFileContents` | ✅ |
| Зависимости VM — только из разрешающего списка (domain, presentation, `AppDispatchers`, порты `core`); без `CoroutineScope` и функциональных типов; нет `safeRun` во VM | Konsist `view-model-dependencies`, detekt `SafeRunInViewModel` | ✅ |
| Конструктор ViewModel — не больше 12 параметров | Konsist `view-model-constructor` | ✅ |
| Импорт `androidx.lifecycle.ViewModel` — только `core/ui` (база) | detekt `ForbiddenImport` | ✅ |
| Koin как сервис-локатор — только `di`, composition roots и обёртки, Android-компоненты | Konsist `no-service-locator` | ✅ |
| `..data..` и реализации (`*Impl`, `Specialist*Repository`) — `internal`; реализацию создаёт только Koin-модуль; Koin-модули — `val *Module` в `di` | Konsist `data-visibility`, `impl-visibility`, `repository-impl`, `koin-wiring`, `koin-module` | ✅ |
| `MutableStateFlow` меняется только `update { }` | detekt `StateFlowValueMutation` | ✅ |
| Граница с бросающим кодом — `safeRun`, не `runCatching`; лог — Kermit, не `println` | detekt `ForbiddenRunCatching`, `ForbiddenPrintln` | ✅ |
| Направления импортов между модулями, пакет = модуль, слои domain/data/presentation | Konsist `module-direction`, `module-package`, `domain-isolation`, `presentation-isolation`, `data-isolation`, `feature-data-isolation`; graph-assert | ✅ |
| Раскладка и имена: реализации репозиториев в `data/repository`, интерфейсы в `domain`, `*DataSource` только у репозиториев, нет `*Helper`/`*Utils` | Konsist `layer-placement`, `repository-in-domain`, `data-source-consumers`, `forbidden-suffix` | ✅ |
| SDK AlarmTrade — только data/di фич и профильные `core/*`; `@Serializable` — data/navigation; модели `domain/model` неизменяемы | Konsist `sdk-confinement`, `serializable-placement`, `domain-model-purity` | ✅ |
| Члены репозиториев (интерфейсы `*Repository` и все интерфейсы `domain/repository`): `suspend` → `Response<T>`, потоки → `Flow`/`StateFlow`/`SharedFlow<Response<T>>`, иных членов нет; публичные потоки реализаций — тоже `Response` | Konsist `repository-contract`, detekt `RawFlowFromRepository` | ✅ |
| Члены доменных сервисов (`*Coordinator`/`*Controller`, `domain/service`): `suspend` → `Response<T>`, потоки → `…<Response<T>>`, синхронно — только команды `Unit`/`Boolean` | Konsist `service-contract` | ✅ |
| Создание объекта без побочных эффектов (нет `init`, корутин в инициализаторах свойств); фоновая работа — `start()` доменного сервиса из `AppServicesStarter`, guard `start()` — CAS | Konsist `no-constructor-side-effects`, `start-once-cas` | ✅ |
| Диспетчер выбирает вызывающий (VM/точка входа): в `*Repository`/`*DataSource` нет `withContext`, кроме класса со своим `CoroutineScope` и `withContext(NonCancellable)` | detekt `RepositoryWithContext` | ✅ |
| Гигиена `Response`: не выбрасывать, не превращать в исключение, не оборачивать повторно; без `throw` в репозиториях и use case | detekt `ResponseIgnored`, `ResponseGetOrThrow`, `RedundantSafeRun`, `ThrowInRepository` | ✅ |
| «Сейчас» и пояс — из внедрённых `Clock`/`TimeZoneSource`, в composable — `LocalClock`/`LocalTimeZoneSource` | detekt `DirectClockUsage` | ✅ |
| Платформенный `Settings()` — только `core/common/di` (`platformSettings`); в фиче хранилище — только data/di, `*Store` → `*Repository` | detekt `SettingsInstantiation`, Konsist `storage-usage`, `forbidden-suffix` | ✅ |
| Мапперы `toDomain*` — `internal` в `data/mapper` | Konsist `mapper-placement` | ✅ |
| `*UseCase` — в `domain/usecase`, оркестрация ≥ 2 зависимостей, только `operator fun invoke` с `Response` | Konsist `use-case-package`, `use-case-contract` | ✅ |
| У реализации репозитория есть содержательный тест (успех → доменная модель, ошибка SDK → `Response.Error`, ключевые операции); SDK — за `*DataSource` | Konsist `repository-impl-tested` (allowlist непокрытых) | ✅ |
| Платформенные возможности (`core.permissions`, порты `presentation/platform`, `core.lifecycle`) — где начинается вызов | Konsist `capability-usage`, `foreground-usage` | ✅ |
| Лямбда `update`/`getAndUpdate`/`updateAndGet` чистая (без suspend, сервисов, записи в хранилище, emit, launch) | detekt `UpdateLambdaSideEffects` | ✅ |
| Широкий `catch` в корутине пробрасывает `CancellationException` | detekt `CatchSwallowsCancellation`, `SuspendFunSwallowedCancellation` | ✅ |
| `Mutex`: без `isLocked` и без вложенного `withLock` того же мьютекса; без suspend под `lock()`/`synchronized` | detekt `MutexIsLockedCheck`, `NestedSameMutexLock`, `SuspendInsideSynchronized` | ✅ |
| Нет `var`/изменяемых коллекций в объектах со своим scope и сервисах data-слоя | detekt `SharedMutableStateInSingleton` | ✅ |
| `CoroutineScope` — из DI (`serviceScope`) или с явным `cancel()`; `SupervisorJob()` + обработчик; без `GlobalScope`; диспетчеры — `AppDispatchers` | detekt `UnmanagedCoroutineScope`, `GlobalCoroutineUsage`, `InjectDispatcher` | ✅ |
| `shareIn`/`stateIn` — только в инициализаторе свойства; `async` дожидаются; без `collect` в `collect` | detekt `SharingInsideFunction`, `UnawaitedAsync`, `NestedCollect` | ✅ |
| Start-once сервис и место с `Mutex`/`tryLock` покрыты стресс-тестом «32 параллельных вызова → эффект один раз» | ревью (эталон `DeviceSessionImplTest`) | — |
| Composable не достаёт зависимости из Koin (`koinInject`) — только корни композиции `composeApp`/`flasherApp` | Konsist `composable-injection` | ✅ |
| UI-state `*State` — `@Immutable`/`@Stable`; `@Composable` фичи — только в `presentation` | Konsist `state-immutable`, `composable-in-presentation` | ✅ |
| Свой `CompositionLocal` — только из списка `compose_allowed_composition_locals` | compose-rules `compositionlocal-allowlist` | ✅ |

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
