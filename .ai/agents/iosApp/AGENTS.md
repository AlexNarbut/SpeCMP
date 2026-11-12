# iosApp

- Область: Swift bootstrap, iOS project settings и интеграция общего framework в `iosApp`.
- Перед изменениями читай `docs/build.md`.
- Общую UI- и бизнес-логику ищи в KMP-модулях, а не в `iosApp`.
- Экран не гаснет, пока приложение открыто: `UIApplication.shared.isIdleTimerDisabled = true` в `iosApp.swift` — прошивка блока идёт 15–20 мин, автоблокировка приостанавливала приложение, и блок сбрасывал приём (`ERROR_TIMEOUT`).
- `Info.plist`: `UIBackgroundModes = bluetooth-central` — BLE-сессия и прошивка продолжаются в фоне и при заблокированном экране (события CoreBluetooth будят приложение). При ревью App Store режим обосновывается настройкой и прошивкой своего оборудования.
