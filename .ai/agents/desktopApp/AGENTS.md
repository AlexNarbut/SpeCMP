# desktopApp

- Область: JVM entry point, desktop window, packaging и platform bootstrap в `desktopApp`.
- Перед изменениями читай `docs/build.md`.
- Общую UI- и бизнес-логику ищи в KMP-модулях, а не в `desktopApp`.
- USB на macOS arm64: `usb4java` (SDK) работает только на x86_64-JDK — JDK запуска задаёт `compose.jvmHome` (`-P` или `local.properties`), а без него `desktopApp/build.gradle.kts` сам выбирает установленный x86_64-JDK 17 (`java_home -a x86_64 -v 17`), см. `docs/build.md`. Запуск из IDE — Run-конфигурация `desktopApp` (`.run/desktopApp.run.xml`, Gradle `:desktopApp:run`), не `main()` из гуттера.
