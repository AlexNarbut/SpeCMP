# core

- Область: core-модули проекта.
- Перед изменениями читай `core/README.md`; при изменении модульных границ и зависимостей между `core`-модулями — `docs/architecture.md`.
- Для сети переходи в `core/network/AGENTS.md`; для UI-слоя — в `core/ui/AGENTS.md`; для жизненного цикла приложения — в `core/lifecycle/AGENTS.md`.
- Если меняешь публичные возможности `core`-модулей, обновляй `core/README.md` и соответствующий тематический документ в `docs/`.
- `core/common/file`: `safeFileName` / `uniqueFileName` (имена файлов извне → безопасный basename), Android — `queryNameAndSize` и `Context.copyToCache` (копия документа SAF в `cacheDir`). Общие для пикеров `composeApp` и `feature:device:firmware`.
- `core/common/di/platformSettings` — единственный платформенный `Settings()` процесса (detekt `SettingsInstantiation`): поверх него `AppSettingsFactory` (хранилища фич с префиксом), напрямую — только язык (`core/localization`) и тема (`core/ui`) до старта Koin, ключи без префикса (`docs/patterns.md`, «Хранение настроек»).
- `core/common/time`: `TimeZoneSource` (пояс на вызов, `System`/`fixed(tz)`); `commonModule` привязывает `kotlinx.datetime.Clock` и `TimeZoneSource` — production берёт «сейчас» только из них (detekt `DirectClockUsage`, `docs/patterns.md` → «Источник времени»). `core/common` отдаёт `kotlinx-datetime` через `api`.
- `core/permissions`: `PermissionsProvider` (Koin, `permissionsModule()`) — единственная точка доступа к разрешениям и состоянию радио: `requestBlePermissions(): BlePermissionResult` (`Granted` / `Denied(canOpenSettings)`), `deviceAccessRequirements()`, `isBluetoothEnabled()`, `isLocationEnabled()`, открытие системных настроек. Типы библиотеки разрешений (`ru.alarmtrade.permissions.*`) наружу из модуля не выходят — фичи их не импортируют; платформенные `expect` — `internal`.
- Сборка модулей — convention-плагины `build-logic`: `id("specmp.kmp.library")` или `id("specmp.kmp.compose")` (таргеты, JVM 17, SDK, namespace из пути); в `build.gradle.kts` модуля — только iOS-framework, `compose.resources` и зависимости (`docs/build.md` → «Convention-плагины»).
