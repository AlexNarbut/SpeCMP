# core

- Область: core-модули проекта.
- Перед изменениями читай `core/README.md`; при изменении модульных границ и зависимостей между `core`-модулями — `docs/architecture.md`.
- Для сети переходи в `core/network/AGENTS.md`; для UI-слоя — в `core/ui/AGENTS.md`; для жизненного цикла приложения — в `core/lifecycle/AGENTS.md`.
- Если меняешь публичные возможности `core`-модулей, обновляй `core/README.md` и соответствующий тематический документ в `docs/`.
- `core/permissions`: `PermissionsProvider` (Koin, `permissionsModule()`) — единственная точка доступа к разрешениям и состоянию радио: `requestBlePermissions(): BlePermissionResult` (`Granted` / `Denied(canOpenSettings)`), `deviceAccessRequirements()`, `isBluetoothEnabled()`, `isLocationEnabled()`, открытие системных настроек. Типы библиотеки разрешений (`ru.alarmtrade.permissions.*`) наружу из модуля не выходят — фичи их не импортируют; платформенные `expect` — `internal`.
