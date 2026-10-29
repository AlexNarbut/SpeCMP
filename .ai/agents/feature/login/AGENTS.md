# feature/login

- Авторизация специалиста, только login-flow: вход, регистрация, восстановление пароля, активация аккаунта.
- `domain`, `data`, `presentation` и DI живут в `commonMain`; app-level инициализация `SpecialistApp` и platform-specific `AppDeviceInformation` — в `core/specialist`.
- `LoginRepository` описывает auth-операции (`login`, `signUp`, `resetPassword`, `resendActivationEmail`) и возвращает `Response<T>`. Session-операции (`observeIsLoggedIn`, `logout`) здесь не живут — они в `feature/auth-state`.
- ViewModel вызывает `LoginRepository` напрямую, управляет dispatcher boundary и клиентской валидацией submit-flow.
- Репозиторий получает только нужные SDK interactors и device information provider, не создаёт `SpecialistApp` и не содержит platform-specific DI.
- Успешный вход не навигирует сам: смену зоны выполняет `feature/auth-state`, увидев обновление состояния сессии; ViewModel только сбрасывает форму.
- Корень флоу — `LoginNavGraph` (`presentation/navigation`): собственный nav3-граф с маршрутами `LoginRoute` / `RegistrationRoute` / `ForgotPasswordRoute`, общий chrome снаружи `NavDisplay` (см. `docs/navigation.md`).
- Локалью фича не управляет и переключателя языка на экранах входа нет: язык выбирается в «Настройках» после входа (`feature/app-settings`, см. `docs/localization.md`).
- Ошибки submit-флоу — типизированные (`AuthFormError` в `presentation/model`), строковые коды ошибок между ViewModel и UI запрещены (см. `docs/patterns.md`).
