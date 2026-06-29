# core/ui

- Область: `LCEState`, тема, `Cmp*` UI Kit и механизм SideEffect в `core/ui`.
- Перед изменениями читай `docs/ui-kit.md`; для Compose-поведения и производительности — `docs/compose-best-prac.md`.
- Если меняешь публичные компоненты, тему или соглашения UI Kit, обновляй `docs/ui-kit.md`.
- One-shot события (Effects): механизм изолирован в `ru.alarmtrade.specmp.core.ui.effect` — фабрика `sideEffectFlow()` (emit-сторона) и composable `collectSideEffect { }` (lifecycle-aware сбор). Фичи используют только их; ручной `LaunchedEffect` + `repeatOnLifecycle` не пишут. Контракт и правила — `docs/patterns.md`, раздел «One-shot события (Effects)».
