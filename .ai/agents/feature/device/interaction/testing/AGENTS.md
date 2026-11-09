# feature/device/interaction/testing

## Scope

Общие тестовые дублёры контрактов `feature/device/interaction` (пакет `ru.alarmtrade.specmp.feature.device.interaction.testing`). Код в `commonMain`, сборка — `specmp.kmp.library`; подключается **только** в тестовых source set'ах: `commonTest.dependencies { implementation(project(":feature:device:interaction:testing")) }` (Konsist `testing-modules-test-only`).

## Состав

- `FakeDeviceSessionRepository(device, link)` — связь `link` (`MutableStateFlow`, по умолчанию `READY`) переключает тест, `readDevice` отдаёт `device`; сценарные ответы списками (последний повторяется): `bluetoothVersions`, `bootResults`, `exitResults`; счётчики/записи `bluetoothReads`, `bootCalls`/`bootTargets`, `exitCalls`/`exitTargets`, `reconnectCount`, `pins`; `disconnect()` роняет `link` в `DISCONNECTED`.
- `FakeDeviceConnectionRepository(states, hangAfterStates, connectDelayMillis, bleAttempts)` — статусы подключения, `closeCount`/`closed`, `bleConnectCount`, `serialConnectCount`.
- `FakeProgramModeRepository` — `results` по порядку, `calls`, `exits`.
- `testAlarmDevice`, `testBluetoothModule` — значения по умолчанию.
- `TestScope.testDeviceSession(sessionRepository, connectionRepository, dispatcher, transport)` — настоящая `DeviceSession` из `deviceInteractionModule` поверх переданных репозиториев: скоуп (`deviceSessionScopeQualifier`) — дочерний к `backgroundScope`, `start()` уже вызван, `transport` — сразу `attach`.

## Rules

- Сама `feature/device/interaction` модуль не подключает (цикл через тестовую конфигурацию): `DeviceSessionImplTest` держит свои дублёры (`data/service/FakeDeviceRepositories.kt` — зависания, гейты, ошибки потока связи).
- Koin-приложение в `testDeviceSession` закрывается сразу после `get`: экземпляры `single` живут в объекте общего `val`-модуля, без `close()` следующий тест получил бы ту же сессию.
- Особое поведение одного теста — локальный фейк; общее — параметром/состоянием здесь, а не копией.
- Подробно: `docs/patterns.md` → «Тестовые дублёры».
