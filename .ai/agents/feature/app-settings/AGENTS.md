# feature/app-settings

- Область: настройки приложения — сейчас язык. Вход — главная → боковое меню → «Настройки» (`AppSettingsRoute`, `AppSettingsEntry(onBack)` в `composeApp/AppNavigation`).
- Язык применяется только на старте процесса (`docs/localization.md`, «Язык приложения»): `AppSettingsViewModel` по «Применить» → подтверждение «приложение перезапустится» → `AppLocaleStore.save` → `AppRestarter.restart()`. Android перезапускает процесс; iOS и desktop — `false`, экран просит перезапустить вручную.
- Зависимости узкие: `AppLocaleStore` (`core/localization`) и `AppRestarter` (`core/common`) приходят из composition root (`App()` и платформенные Koin-модули); app-level хранение и применение локали — не в фиче.
- Строки — префикс `appsettings` в `core/localization`; названия языков — на самих языках (`translatable="false"`).
