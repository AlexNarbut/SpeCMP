# feature/configurator/testing

## Scope

Общие тестовые дублёры контрактов `feature/configurator` (пакет `ru.alarmtrade.specmp.feature.configurator.testing`). Код в `commonMain`, сборка — `specmp.kmp.library`; подключается **только** в тестовых source set'ах: `commonTest.dependencies { implementation(project(":feature:configurator:testing")) }` (Konsist `testing-modules-test-only`). Потребители — `configurator-ui`, `device/settings-ui`.

## Состав

- `FakeLocalConfigurationsRepository` — `items` (ответ `all()`, счётчик `allCalls`), `changes` (`MutableSharedFlow`, тест делает `tryEmit`), `createResult` (по умолчанию `LocalConfiguration(100, "DX-9X", "Новая")`) и `createGate` (пока не завершён — создание «идёт»), записи `createCalls`/`deleted`; `updateInfo` отдаёт `id + 1000`.

## Rules

- Сама `feature/configurator` модуль не подключает (цикл через тестовую конфигурацию).
- Особое поведение одного теста — локальный фейк; общее — параметром/состоянием здесь, а не копией.
- Подробно: `docs/patterns.md` → «Тестовые дублёры».
