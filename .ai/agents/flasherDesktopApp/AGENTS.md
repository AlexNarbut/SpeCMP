# flasherDesktopApp

- Область: desktop-обёртка приложения прошивки (`flasherApp`): `main()`, окно, упаковка (`SpeCMPFlasher`). Логика — в KMP-модулях.
- Версия — `flasher-versionName` / `flasher-versionCode` (`gradle/libs.versions.toml` → `version.properties`).
- JDK запуска выбирается как у `desktopApp` (`compose.jvmHome` / x86_64-JDK 17 на macOS arm64 — для USB-HID через usb4java, см. `docs/build.md`). COM-порт (jSerialComm в SDK) работает на любом JDK.
- Запуск: `./gradlew :flasherDesktopApp:run`.
- Windows: рендер Skiko — OpenGL, как в `desktopApp` (`useOpenGlOnWindows()` в `Main.kt`).
