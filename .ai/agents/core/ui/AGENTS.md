# core/ui

- Область: `LCEState`, `BaseViewModel`, тема, `Cmp*` UI Kit и механизм SideEffect в `core/ui`.
- `BaseViewModel` (`ru.alarmtrade.specmp.core.ui.viewmodel`) — базовый класс всех ViewModel: protected `logger` (тег = имя класса), `safeLaunch` с `CoroutineExceptionHandler` для необработанных исключений и `executeWithProceedLock` (skip-дубль по умолчанию / сериализация при `needSkipAnother = false`; от отмены не защищает). Фичи наследуются от него, а не от androidx `ViewModel`; правила — `docs/patterns.md`.
- Перед изменениями читай `docs/ui-kit.md`; для Compose-поведения и производительности — `docs/compose-best-prac.md`.
- Если меняешь публичные компоненты, тему или соглашения UI Kit, обновляй `docs/ui-kit.md`.
- One-shot события (Effects): используй общий механизм core UI для emit-стороны и lifecycle-aware сборки. Фичи используют только публичный API core UI; ручной `LaunchedEffect` + `repeatOnLifecycle` не пишут. Контракт и правила — `docs/patterns.md`, раздел «One-shot события (Effects)».
