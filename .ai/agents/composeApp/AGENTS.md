# composeApp

- Область: `App()`, навигация, общая UI-интеграция и подключение feature-модулей в `composeApp`.
- Перед изменениями читай `docs/navigation.md`; для общих Compose-паттернов и производительности — `docs/compose-best-prac.md`.
- Для правил UI-компонентов переходи в `core/ui/AGENTS.md`; для устройства feature-модулей — в `feature/AGENTS.md`.
- Если меняешь структуру `App()`, навигацию или способ интеграции фич, обновляй `docs/navigation.md` и связанные разделы `docs/architecture.md`.
- Process-wide сервисы фич (`start()` доменных сервисов, сейчас `DeviceSession`) запускает `AppServicesStarter` (`core/common`): слоты — `di/AppServicesModule.kt`, `start()` — из `App()`. Новый сервис со `start()` — новый слот (и во `flasherApp`, если фича там есть). См. `docs/patterns.md`, «Доменные сервисы».
