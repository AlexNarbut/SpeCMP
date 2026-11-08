# core/localization

- Область: язык приложения (`AppLocale`, `currentAppLocale()` / `saveAppLocale()`, `AppLocaleStore`, `prepareAppLocale()` на старте, Android `Context.withAppLocale()` для `attachBaseContext` и `reapplyAppLocale()` для `onConfigurationChanged`) и **все строки UI приложения**. Язык применяется только на старте процесса, смена — перезапуском (`docs/localization.md`, «Язык приложения»); `CompositionLocal` локали нет.
- Строки: `src/commonMain/composeResources/values*/<префикс фичи>.xml` (en — `values/`, ru — `values-ru/`, одинаковые ключи), публичный класс `Strings` (`ru.alarmtrade.specmp.core.localization.resources`): `stringResource(Strings.string.<ключ>)`, `pluralStringResource(Strings.plurals.…)`, `stringArrayResource(Strings.array.…)`.
- Ключи — `{feature}_{screen}_{type}[_{name}]`, префикс = имя файла, реестр префиксов и таблица типов — `docs/localization.md`. Новая фича — новый префикс в реестре и пара файлов `values/` + `values-ru/`.
- Своих `strings.xml` в фичах нет; фиче для строк нужна только зависимость `:core:localization`.
- `di/localizationModule` — Koin-привязка `AppLocaleStore` → `DefaultAppLocaleStore`; подключают composition roots.
