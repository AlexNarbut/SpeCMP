# desktopApp

- Область: JVM entry point, desktop window, packaging и platform bootstrap в `desktopApp`.
- Перед изменениями читай `docs/build.md`.
- Общую UI- и бизнес-логику ищи в KMP-модулях, а не в `desktopApp`.
- USB на macOS arm64: `usb4java` (SDK) работает только на x86_64-JDK — JDK запуска задаёт `compose.jvmHome` (`-P` или `local.properties`), см. `docs/build.md`.
