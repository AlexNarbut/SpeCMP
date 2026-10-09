# core/ui

- Область: `LCEState`, `BaseViewModel`, тема, `Cmp*` UI Kit и механизм SideEffect в `core/ui`.
- `BaseViewModel` (`ru.alarmtrade.specmp.core.ui.viewmodel`) — базовый класс всех ViewModel: protected `logger` (тег = имя класса), `safeLaunch` с `CoroutineExceptionHandler` для необработанных исключений и `executeWithProceedLock` (skip-дубль по умолчанию / сериализация при `needSkipAnother = false`; от отмены не защищает). Фичи наследуются от него, а не от androidx `ViewModel`; правила — `docs/patterns.md`.
- Перед изменениями читай `docs/ui-kit.md`; для Compose-поведения и производительности — `docs/compose-best-prac.md`.
- Если меняешь публичные компоненты, тему или соглашения UI Kit, обновляй `docs/ui-kit.md`.
- Если фиче не хватает возможностей `Cmp*` (полноэкранный режим, загрузка по url, роль текста) — расширяй компонент здесь и обновляй карту в `docs/ui-kit.md`; обход через Material3/Foundation/Coil в фиче не допускается.
- Coil живёт только здесь: зависимости `coil`/`coil-network-ktor`, обёртки `CmpAsyncImage`/`CmpSubcomposeAsyncImage` (без протечки coil-типов в сигнатурах) и `LocalCmpImageLoader` + `ProvideCmpImageLoader`/`rememberCmpImageLoader`. Единый `ImageLoader` провайдится из `composeApp` (`App.kt`); фичи coil не используют.
- `CmpText` поддерживает типографические роли `CmpTextStyle` (маппинг на `MaterialTheme.typography`); `CmpBox` имеет вариант без content-лямбды; полноэкранные диалоги — `CmpFullScreenDialog` (AlertDialog-обёртка полноэкранный режим не поддерживает).
- Общий лейблер дат `rememberCmpDateLabeler()` (`core/ui/format/`, строки `coreui_common_*`): «Сегодня»/«Вчера»/«14 марта», год добавляется при отличии от текущего. Live-смена языка инвалидирует лямбду через recomposition.
- One-shot события (Effects): используй общий механизм core UI для emit-стороны и lifecycle-aware сборки. Фичи используют только публичный API core UI; ручной `LaunchedEffect` + `repeatOnLifecycle` не пишут. Контракт и правила — `docs/patterns.md`, раздел «One-shot события (Effects)».
