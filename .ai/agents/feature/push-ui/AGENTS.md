# feature/push-ui

- Экран правил push (какие типы присылать аккаунту) поверх `feature/push`: `PushRulesRoute` / `PushRulesEntry(onBack)`, вход — «Настройки приложения» → «Выбрать уведомления» (связка — в `composeApp/AppNavigation`). Зависят только composition roots.
- Список — `PushType.entries` (уведомления, предупреждения, чат техподдержки); новости/видео/статьи не показываются, но их коды сохраняются (`PushRules.with`).
- `PushRulesViewModel`: загрузка в `onStart` (`LCEState`, ошибка — экран с «Повторить»), отметки — локально (`selected`), «Сохранить» активна только при отличии от сервера; сохранение под `executeWithProceedLock`, во время него отметки не меняются. Результат — эффект → снекбар («Правила обновлены» / ошибка), выбор при ошибке остаётся.
- Ошибки — `PushRulesError` (`Common(AppError)` / `Failed`), текст `Failed` зависит от операции (`push_rules_error_load` / `_save`).
- Строки — префикс `push_rules_*` в `core/localization` (`push.xml`). Тест — `PushRulesViewModelTest`.
