# feature

- Область: feature-модули в `feature/*`.
- Перед изменениями читай `docs/architecture.md` и `docs/patterns.md`.
- Для интеграции экранов и маршрутов переходи в `composeApp/AGENTS.md`; для UI-компонентов — в `core/ui/AGENTS.md`.
- Не размещай в feature app-level инфраструктуру: глобальную локаль, SDK/application-контейнеры, инициализацию сторонних SDK, root DI/navigation, общие settings. Если нужно переиспользование или состояние всего приложения — сначала добавь/расширь профильный `core/*` API.
- В feature-классах используй узкие зависимости (`Interactor`, `UseCase`, `DataSource`), а не цельные SDK/application-контейнеры вроде `SpecialistApp`.
- Если добавляешь новую фичу или меняешь её публичную интеграцию, синхронизируй изменения с `composeApp` и соответствующей документацией.
