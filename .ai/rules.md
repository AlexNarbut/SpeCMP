# cpm_template

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
- Документация модулей и подсистем ведётся в `.ai/agents/**/AGENTS.md` (источник) → `.ai/generate.sh`. НЕ создавай новые `README.md` ради документации: ключевые моменты вноси в соответствующий `.ai/agents/**/AGENTS.md`. Существующие `README.md` не являются source of truth.
- Скиллы и субагенты для всех агентов (Claude Code, Codex, Kimi Code, Gemini CLI) ведутся в `.ai/skills/<skill>/` (`SKILL.md`, `roles/*.md`, ресурсы) → `.ai/generate.sh` раскладывает их в `.agents/skills/`, `.claude/skills/`, `.claude/agents/`, `.codex/agents/`, `.agents/agents/`, `.gemini/agents/`. Сгенерированное вручную не правь. Скилл UI-тестирования — `ui-test`; результаты прогонов — в `.ai-qa/` (не в git). Скилл аудита производительности (репозитории, ViewModel, Compose) — `perf-audit`; отчёты — в `.ai-perf/` (не в git). Скилл бенчмарка производительности до/после задачи на эмуляторе (старт, кадры, память, Perfetto, сценарии фич, стабильность) — `perf-bench`; замеры — в `.ai-perf/bench/` (не в git). Сгенерированное сверяет pre-commit-хук `ai-generate` (`.ai/generate.sh --check`). Скилл брифа для ручного QA (HTML «что нового и что проверить» по разделам: всё приложение, ветка или период develop) — `qa-brief`; брифы — в `.ai-qa/briefs/` (не в git).

## Project Map

- `core/*` — общая KMP-инфраструктура.
- `feature/*` — feature-модули.
- `composeApp` — composition root, навигация и интеграция фич.
- `androidApp`, `desktopApp`, `iosApp` — платформенные обёртки.
- `flasherApp` (+ обёртки `flasherAndroidApp`, `flasherDesktopApp`) — отдельное мини-приложение прошивки (ветка `feature/flasher-app`): свой composition root поверх тех же `core/*` и `feature/device/*`.
- `build-logic/` — included build с convention-плагинами (`specmp.kmp.android-jvm`, `specmp.kmp.library`, `specmp.kmp.compose`, `specmp.detekt`), см. `docs/build.md`.
- `quality/` — инструменты проверок кода: `quality/detekt-rules` (свои правила detekt, набор `specmp`), `quality/konsist` (архитектурные тесты Konsist), см. `docs/code-quality.md`.
- `docs/` — тематические правила и справка по архитектуре, UI, навигации и сборке.

## Read Before Editing

- Если агент заходит в директорию или редактирует файлы внутри неё, он сначала должен прочитать ближайший применимый `AGENTS.md` для этой директории.
- `core/*`: `docs/architecture.md`, `core/AGENTS.md`
- `feature/*`: `docs/patterns.md`
- Compose UI: `docs/ui-kit.md`, `docs/compose-best-prac.md`
- Navigation: `docs/navigation.md`
- Локализация / строковые ресурсы: `docs/localization.md`
- Качество кода (ktlint/compose-rules через Spotless, detekt, git-хуки, хук агентов) — перед коммитом Kotlin-кода: `docs/code-quality.md`
- UI-тестирование агентами (скилл `ui-test`, `ui.sh`, платформы, правка скилла): `docs/test/ui/index.md`
- Аудит производительности (скилл `perf-audit`, чек-лист R/V/C, отчёты Compose-компилятора): `.ai/skills/perf-audit/checklist.md`
- Бенчмарк производительности до/после задачи (скилл `perf-bench`, `bench.sh`, сценарии, вердикт): `docs/test/perf-bench.md`
- Бриф для ручного QA (скилл `qa-brief`, карта разделов приложения): `.ai/skills/qa-brief/sections.md`

## Build / Platform

- Сборка и tooling: `docs/build.md`
- Платформенные изменения: перед правками в `androidApp`, `desktopApp` или `iosApp` сначала прочитай ближайший применимый `AGENTS.md` и сверяйся с `docs/build.md`

## Validation

- После изменений кода запускай `./gradlew build`; для чисто документационных и agent-config правок это не требуется.
- Качество кода (обязательная стадия, перед коммитом): `./gradlew spotlessApply && ./gradlew spotlessCheck && ./gradlew detektCheck` — без замечаний. Подробности — `docs/code-quality.md`.
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
