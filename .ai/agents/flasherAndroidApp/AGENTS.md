# flasherAndroidApp

- Область: Android-обёртка приложения прошивки (`flasherApp`): manifest, `FlasherActivity`, иконки. Логика — в KMP-модулях.
- `applicationId` — `ru.alarmtrade.specmp.flasher` (ставится рядом с основным приложением), версия — `flasher-versionName` / `flasher-versionCode` в `gradle/libs.versions.toml`.
- Manifest: BLE-разрешения как у `androidApp`, `usb.host` (HID-блоки и USB-UART адаптер COM-порта по OTG). Разрешение на USB-устройство SDK запрашивает сам при открытии порта. Без `FileProvider` и чата.
- `FlasherActivity` повторяет `AppActivity`: `singleTask`, язык в `attachBaseContext`, `testTagsAsResourceId`.
- Сборка: `./gradlew :flasherAndroidApp:assembleDebug`.
