# core

- Область: `core/common`, `core/localization`, `core/network`, `core/specialist`, `core/ui`.
- Перед изменениями читай `core/README.md`; при изменении модульных границ и зависимостей между `core`-модулями — `docs/architecture.md`.
- Для сети переходи в `core/network/AGENTS.md`; для UI-слоя — в `core/ui/AGENTS.md`.
- `core/localization` владеет app-level локалью (`AppLocale`, чтение/применение локали), feature-модули не должны объявлять собственную глобальную локаль приложения.
- `core/specialist` владеет инициализацией `SpecialistApp`, регистрацией SDK-зависимостей в DI и platform-specific сборкой device information для SDK; feature-модули получают конкретные SDK interactors/use cases/provider-ы, а не создают `SpecialistApp`.
- Если меняешь публичные возможности `core`-модулей, обновляй `core/README.md` и соответствующий тематический документ в `docs/`.
