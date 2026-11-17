# cpm_template

## Первым делом (окружение)

- Перед любой работой в клоне проверь и при отсутствии установи: **Python 3** (`python3 --version`; macOS — `brew install python`) и **pre-commit** (`pre-commit --version`; `brew install pre-commit` или `pipx install pre-commit`), затем активируй git-хуки: `pre-commit install` (ставит pre-commit, pre-push и pre-merge-commit — `default_install_hook_types`). Проверка: в `.git/hooks/` есть `pre-commit` и `pre-push`. Без них не коммить: хуки — обязательная часть проверок (`docs/code-quality.md` → «Команды»). Не можешь установить сам (нет прав/сети) — остановись и попроси пользователя.

## Working Rules

- Изучай затронутую подсистему и предпочитай минимальные локальные изменения.
- Сохраняй текущую KMP-архитектуру: `core/*` для общей инфраструктуры, `feature/*` для фич, `composeApp` как composition root, `androidApp`, `desktopApp` и `iosApp` как тонкие платформенные обёртки.
- Не размещай app-level инфраструктуру внутри `feature/*`: глобальную локаль, SDK/application-контейнеры, инициализацию сторонних SDK, root DI/navigation, общие settings. Выноси это в профильный `core/*` модуль или composition root, а feature давай только узкие зависимости.
- Не редактируй вручную сгенерированные `AGENTS.md`, `CLAUDE.md`, `GEMINI.md` и вложенные `*/AGENTS.md`; меняй `.ai/*` и затем запускай `.ai/generate.sh`.
- Не трогай автогенерируемые файлы, build outputs, `local.properties`, `.env` и другие секреты.
- Ветки: все изменения и слияния идут **только в `develop`** (фичи — из `feature/*` в `develop`). В `main`/`master` вливается **только `develop`** — кастомные ветки (`feature/*`, фиксы и любые другие) и прямые коммиты туда запрещены. Релизы (`develop` → `main`/`master`) и теги делает владелец: агент в них не коммитит, не вливает, не пушит и не ставит теги. Защита: хук `scripts/agent-hooks/protect-main.sh` для агентов (Claude, `.ai/hooks/claude-settings.json`); для всех — `config/hooks/check-master-from-develop.sh` в `.pre-commit-config.yaml` (`master-from-develop-merge` на pre-merge-commit, `master-from-develop-push` на pre-push: каждый новый коммит `main`/`master` должен быть из `develop`). Перед слиянием/пушем проверяй целевую ветку.
- Локальные workflow-артефакты не являются source of truth проекта.
- Если изменение затрагивает архитектуру, публичные соглашения модуля или правила разработки, обновляй соответствующую документацию проекта и локальные `AGENTS.md`.
- Каждый `@Serializable` — с `@SerialName("<модуль>.<Класс>")`, имя после появления не меняется (`docs/patterns.md`, «Сериализация»; pre-commit хук `serial-name-check`).
- Все строки UI — только в `core/localization` (`values*/<префикс фичи>.xml`, класс `Strings`), ключи `{feature}_{screen}_{type}[_{name}]` — `docs/localization.md`. Своих `strings.xml` в фичах не заводить.
- Комментарии в коде — по умолчанию нет. Короткий комментарий (1–2 строки, про «почему», не «что») оставляй только у хуков/обходов (платформа, баг библиотеки/SDK, порядок вызовов ОС) и у очень специфичной реализации, которую без него не разобрать за 5 минут. Без KDoc на очевидные классы/функции/поля, без пересказа кода, истории правок и тикетов, без разделителей-секций. Подробнее — `docs/code-quality.md` → «Комментарии».
- Документация модулей и подсистем ведётся в `.ai/agents/**/AGENTS.md` (источник) → `.ai/generate.sh`. НЕ создавай новые `README.md` ради документации: ключевые моменты вноси в соответствующий `.ai/agents/**/AGENTS.md`. Существующие `README.md` не являются source of truth.
- Скиллы и субагенты для всех агентов (Claude Code, Codex, Kimi Code, Gemini CLI) ведутся в `.ai/skills/<skill>/` (`SKILL.md`, `roles/*.md`, ресурсы) → `.ai/generate.sh` раскладывает их в `.agents/skills/`, `.claude/skills/`, `.claude/agents/`, `.codex/agents/`, `.agents/agents/`, `.gemini/agents/`. Сгенерированное вручную не правь. Скилл UI-тестирования — `ui-test`; результаты прогонов — в `.ai-qa/` (не в git). Скилл аудита производительности (репозитории, ViewModel, Compose) — `perf-audit`; отчёты — в `.ai-perf/` (не в git). Скилл бенчмарка производительности до/после задачи на эмуляторе (старт, кадры, память, Perfetto, сценарии фич, стабильность) — `perf-bench`; замеры — в `.ai-perf/bench/` (не в git). Сгенерированное сверяет pre-commit-хук `ai-generate` (`.ai/generate.sh --check`). Скилл брифа для ручного QA (HTML «что нового и что проверить» по разделам: всё приложение, ветка или период develop) — `qa-brief`; брифы — в `.ai-qa/briefs/` (не в git).

## Project Map

- `core/*` — общая KMP-инфраструктура; `core/testing` и `feature/<x>/testing` (сейчас `feature/device/interaction/testing`, `feature/configurator/testing`, `feature/push/testing`) — модули общих тестовых дублёров, подключаются только в тестовых source set'ах (`docs/patterns.md` → «Тестовые дублёры»).
- `feature/*` — feature-модули.
- `composeApp` — composition root, навигация и интеграция фич.
- `androidApp`, `desktopApp`, `iosApp` — платформенные обёртки.
- `flasherApp` (+ обёртки `flasherAndroidApp`, `flasherDesktopApp`) — отдельное мини-приложение прошивки (ветка `feature/flasher-app`): свой composition root поверх тех же `core/*` и `feature/device/*`.
- `build-logic/` — included build с convention-плагинами (`specmp.kmp.android-jvm`, `specmp.kmp.library`, `specmp.kmp.compose`, `specmp.detekt`), см. `docs/build.md`.
- `quality/` — инструменты проверок кода: `quality/detekt-rules` (свои правила detekt, набор `specmp`), `quality/konsist` (архитектурные тесты Konsist), `quality/script-tests` (тесты скриптов-гейтов), см. `docs/code-quality.md`.
- `scripts/agent-hooks/` — хуки агентов: `kotlin-quality.sh` (Spotless по правленому `.kt`/`.kts`), `protect-main.sh` (защита `main`/`master`); регистрация — `.ai/hooks/*` → `.ai/generate.sh`, см. `docs/code-quality.md` → «Хуки агентов».
- `docs/` — тематические правила и справка по архитектуре, UI, навигации и сборке.

## Read Before Editing

- Если агент заходит в директорию или редактирует файлы внутри неё, он сначала должен прочитать ближайший применимый `AGENTS.md` для этой директории.
- `core/*`: `docs/architecture.md`, `core/AGENTS.md`
- `feature/*`: `docs/patterns.md`
- Compose UI: `docs/ui-kit.md`, `docs/compose-best-prac.md`
- Navigation: `docs/navigation.md`
- Локализация / строковые ресурсы: `docs/localization.md`
- Качество кода (ktlint/compose-rules через Spotless, detekt, Konsist, graph-assert, CPD, git-хуки, хук агентов) — перед коммитом Kotlin-кода: `docs/code-quality.md`; соответствие правил `docs/patterns.md` проверкам — `docs/code-quality.md` → «Правило → проверка»
- UI-тестирование агентами (скилл `ui-test`, `ui.sh`, платформы, правка скилла): `docs/test/ui/index.md`
- Аудит производительности (скилл `perf-audit`, чек-лист R/V/C, отчёты Compose-компилятора): `.ai/skills/perf-audit/checklist.md`
- Бенчмарк производительности до/после задачи (скилл `perf-bench`, `bench.sh`, сценарии, вердикт): `docs/test/perf-bench.md`
- Бриф для ручного QA (скилл `qa-brief`, карта разделов приложения): `.ai/skills/qa-brief/sections.md`

## Build / Platform

- Сборка и tooling: `docs/build.md`
- Платформенные изменения: перед правками в `androidApp`, `desktopApp` или `iosApp` сначала прочитай ближайший применимый `AGENTS.md` и сверяйся с `docs/build.md`

## Validation

- После изменений кода собирай оба composition root на Android и desktop: `./gradlew :androidApp:assembleDebug :flasherAndroidApp:assembleDebug :desktopApp:compileKotlin :flasherDesktopApp:compileKotlin` (не `./gradlew build`: он тянет iOS); для чисто документационных и agent-config правок сборка не требуется. Правки `iosMain` или `expect`/`actual` — ещё `./gradlew :composeApp:compileKotlinIosArm64`. См. `docs/build.md` → «Протокол верификации».
- После изменений кода запускай тесты затронутых модулей: `./gradlew :<модуль>:jvmTest` (все — `./gradlew jvmTest`; `allTests`/`test` не использовать — гоняют iOS), см. `docs/build.md`.
- После изменений кода запускай `./gradlew spotlessCheck` (автофикс — `./gradlew spotlessApply`, переписывает всё дерево), см. `docs/code-quality.md`.
- После изменений кода запускай `./gradlew detektCheck --continue` (без автофикса), см. `docs/code-quality.md` → «detekt».
- После изменений кода запускай `./gradlew cpdCheck` (поиск скопированного кода, порог 100 токенов): находку выноси в общий код, а не исключай, см. `docs/code-quality.md` → «CPD».
- После правок `*.gradle.kts`, `build-logic/` или `gradle/libs.versions.toml` запускай `./gradlew assertModuleGraph` (направления зависимостей между модулями: `core` ↛ `feature`, `core`/`feature` ↛ `composeApp`/`flasherApp`, никто ↛ платформенные обёртки и `quality`, от `*-ui` зависят только composition roots); нарушение исправляй переносом кода, а не исключением, см. `docs/code-quality.md` → «graph-assert».
- После структурных изменений (новые классы/файлы, перенос пакетов, ViewModel, репозитории, Koin-модули, импорты между слоями и модулями) запускай `./gradlew :quality:konsist:test`, см. `docs/code-quality.md` → «Konsist». После правки своих правил detekt — `./gradlew :quality:detekt-rules:test`.
- После правок UI в `feature/*`, `core/*`, `composeApp`, `flasherApp` запускай `./gradlew uiAudit` (обходы дизайн-системы `core/ui`: сырые цвета, Material/Coil/`Image`/`Dialog` мимо `Cmp*` — сверх baseline `scripts/ui-audit/baseline.json`; свои размеры текста — без baseline), см. `docs/code-quality.md` → «UI-аудит».
- Текст — только `CmpText(style = CmpTextStyle.*)` из шкалы 14 стилей: своих размеров, межстрочного, `TextStyle`/`TextUnit`, `.sp` вне `core/ui` нет (detekt `RawTextSize`, строгие правила UI-аудита), не хватает стиля — добавляется в шкалу, см. `docs/ui-kit.md` → «Типографика».
- Токены темы (`core/ui/.../theme/tokens/`) генерируются из `design/design-system/tokens/*.css` скриптом `scripts/ds-tokens/generate.py` — руками не править; после правки CSS-токенов запусти генератор, сверка — pre-commit хук `ds-tokens`, см. `docs/ui-kit.md` → «Система тем».
- После правок `config/hooks/*`, `scripts/agent-hooks/*`, `scripts/ui-audit/*` или `scripts/ds-tokens/*` запускай `./gradlew scriptTest` (unittest в `quality/script-tests/`) и добавляй тест на новый случай, см. `docs/code-quality.md` → «Тесты скриптов проверок».
- После правки `*.kt`/`*.kts`, если твой агент не запускает хук автоматически (Claude Code, Codex, Gemini CLI — запускают), выполни `scripts/agent-hooks/kotlin-quality.sh <изменённые файлы>` и исправь замечания до завершения задачи.
- Всё одной строкой перед коммитом: `./gradlew spotlessApply && ./gradlew :androidApp:assembleDebug :flasherAndroidApp:assembleDebug :desktopApp:compileKotlin :flasherDesktopApp:compileKotlin <:модуль:jvmTest…> spotlessCheck detektCheck cpdCheck uiAudit assertModuleGraph :quality:konsist:test :quality:detekt-rules:test --continue`.
- Производительность (скилл `perf-bench`, `B=.agents/skills/perf-bench/scripts/bench.sh`) — **не обязательна**:
  по просьбе пользователя и для задач, где важна производительность (старт, списки, тяжёлые экраны, память,
  оптимизации). В остальных задачах не запускать.

  ```bash
  $B baseline --scope static,startup,trace,feature:auto   # в начале задачи, до правок кода
  $B run      --scope static,startup,trace,feature:auto   # после сборки и проверки качества
  ```

  `,compose` — если задача трогала UI (`feature/*`, `core/ui`, `composeApp`); `,stability` — если `core/*` или
  `composeApp`. Вердикт, причины и путь к отчёту из `report.md` — в итог задачи (и в её файл в `.claude/tasks/`, если
  он есть); ❌ в отчёте — задача не завершена, пока регресс не объяснён пользователю. Правила и метрики —
  `docs/test/perf-bench.md`.

## Instruction Maintenance

- Обновляй `.ai/rules.md`, если меняются маршрутизация инструкций, структура проекта, обязательные документы для чтения или команды валидации.
- Обновляй `.ai/agents/**`, если меняются границы подсистемы или локальные правила для конкретной директории.
- После изменений в `.ai/rules.md`, `.ai/agents/**`, `.ai/skills/**` или `.ai/hooks/*` (хуки агентов, см. `docs/code-quality.md` → «Хуки агентов») запускай `.ai/generate.sh` и коммить результат вместе с источником (`.claude/settings.json`, `.gemini/settings.json`, `.codex/hooks.json` версионируются). Сверка — `.ai/generate.sh --check` (pre-commit хук `ai-generate`).
- Новая проверка (правило detekt/Konsist, ребро графа, исключение CPD) — описывай её в `docs/code-quality.md` в том же изменении: правило — в `docs/patterns.md` с id проверки и строкой в «Правило → проверка», подавление архитектурного правила — в «Подавления». У каждого правила detekt/Konsist — минимум 4 теста на попытки обхода (`docs/code-quality.md` → «Как менять правила»). История внедрения проверок в нормативные документы не пишется.
