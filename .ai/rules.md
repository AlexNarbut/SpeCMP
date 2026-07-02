# cpm_template

## Working Rules

- Изучай затронутую подсистему и предпочитай минимальные локальные изменения.
- Сохраняй текущую KMP-архитектуру: `core/*` для общей инфраструктуры, `feature/*` для фич, `composeApp` как composition root, `androidApp`, `desktopApp` и `iosApp` как тонкие платформенные обёртки.
- Не размещай app-level инфраструктуру внутри `feature/*`: глобальную локаль, SDK/application-контейнеры, инициализацию сторонних SDK, root DI/navigation, общие settings. Выноси это в профильный `core/*` модуль или composition root, а feature давай только узкие зависимости.
- Не редактируй вручную сгенерированные `AGENTS.md`, `CLAUDE.md`, `GEMINI.md` и вложенные `*/AGENTS.md`; меняй `.ai/*` и затем запускай `.ai/generate.sh`.
- Не трогай автогенерируемые файлы, build outputs, `local.properties`, `.env` и другие секреты.
- Локальные workflow-артефакты не являются source of truth проекта.
- Если изменение затрагивает архитектуру, публичные соглашения модуля или правила разработки, обновляй соответствующую документацию проекта и локальные `AGENTS.md`.
- Документация модулей и подсистем ведётся в `.ai/agents/**/AGENTS.md` (источник) → `.ai/generate.sh`. НЕ создавай новые `README.md` ради документации: ключевые моменты вноси в соответствующий `.ai/agents/**/AGENTS.md`. Существующие `README.md` не являются source of truth.

## Project Map

- `core/*` — общая KMP-инфраструктура.
- `feature/*` — feature-модули.
- `composeApp` — composition root, навигация и интеграция фич.
- `androidApp`, `desktopApp`, `iosApp` — платформенные обёртки.
- `docs/` — тематические правила и справка по архитектуре, UI, навигации и сборке.

## Read Before Editing

- Если агент заходит в директорию или редактирует файлы внутри неё, он сначала должен прочитать ближайший применимый `AGENTS.md` для этой директории.
- `core/*`: `docs/architecture.md`, `core/AGENTS.md`
- `feature/*`: `docs/patterns.md`
- Compose UI: `docs/ui-kit.md`, `docs/compose-best-prac.md`
- Navigation: `docs/navigation.md`
- Локализация / строковые ресурсы: `docs/localization.md`

## Build / Platform

- Сборка и tooling: `docs/build.md`
- Платформенные изменения: перед правками в `androidApp`, `desktopApp` или `iosApp` сначала прочитай ближайший применимый `AGENTS.md` и сверяйся с `docs/build.md`

## Validation

- После изменений кода запускай `./gradlew build`; для чисто документационных и agent-config правок это не требуется.
