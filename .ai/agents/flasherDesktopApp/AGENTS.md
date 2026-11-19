# flasherDesktopApp

- Область: desktop-обёртка приложения прошивки (`flasherApp`): `main()`, окно, упаковка (`SpeCMPFlasher`). Логика — в KMP-модулях.
- Версия — `flasher-versionName` / `flasher-versionCode` (`gradle/libs.versions.toml` → `version.properties`).
- JDK запуска выбирается как у `desktopApp` (`compose.jvmHome` / x86_64-JDK 17 на macOS arm64 — для USB-HID через usb4java, см. `docs/build.md`). COM-порт (jSerialComm в SDK) работает на любом JDK.
- Запуск: `./gradlew :flasherDesktopApp:run`.
- Windows: рендер Skiko — из `graphics.settings` в ресурсах пакета (`appResources/windows/`, `appResourcesRootDir`), как в `desktopApp`: `DesktopGraphicsConfig.apply(restoreSavedSettings = false)` — сохранённый апдейтером файл `~/PandoraSpec/tmp/graphics.settings` принадлежит «Pandora Spec», flasher его не забирает. См. `docs/build.md` → «Рендер на Windows».
