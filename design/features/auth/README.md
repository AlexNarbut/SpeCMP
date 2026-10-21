# Авторизация — фича

Auth-flow Pandora Spec: проверка сессии, вход, регистрация установщика, восстановление
пароля и временный logged-in placeholder с logout. Реализует
`uploads/designer-requirements.md`. EN/RU, светлая/тёмная тема, адаптив по Material 3
Window Size Classes (Compact / Medium / Expanded).

## Файлы
- `i18n.js` — словарь EN/RU (`window.AuthI18N`, `window.AuthT(lang)`). EN — fallback;
  смена языка не очищает форму.
- `app-icon.png` — иконка приложения (gear + lock), используется в hero-зоне экранов.
- `AuthScreens.jsx` — презентационные экраны (`window.AuthScreens`): `AuthRootScreen`,
  `LoginScreen`, `RegisterScreen`, `ForgotScreen`, `LoggedInScreen`, плюс shell,
  `LangSegment`, `LogoPlaceholder`, `PasswordField`, хук `useAuthMode`. Чистая визуалка:
  состояние/значения/хендлеры приходят пропсами.
- `Auth.jsx` — `window.AuthFlow` (живой интерактивный поток со скриптованной асинхронностью)
  и `window.AuthPinned` (рендер экрана в конкретном состоянии). `window.AUTH_STATES` —
  каталог состояний по экранам.
- `Auth.html` — mobile (Android Compact 412×840): прототип + переключатель состояний.
- `AuthTablet.html` — планшет: портрет 800×1280 (medium) и ландшафт 1280×800 (expanded).
- `AuthDesktop.html` — окно Compose Desktop 1280×800 (expanded).
- `AuthStates.html` — галерея всех экранов × состояний, EN/RU, light & dark.
- `AuthFlow.html` — диаграмма потока (auth root → login → registration / forgot → logged-in).

## Экраны и состояния
- **Auth Root** — `loading` (тихий индикатор, без текста про проверку), `error` (с retry).
- **Login** — `empty / partial / ready / loading / error / activation` (диалог поверх логина).
- **Registration** — `empty / partial / ready / loading / validation / server / success`.
- **Forgot Password** — `empty / ready / loading / error / success`.
- **Logged-in placeholder** — `loggedIn / logoutLoading / logoutError`.

## Адаптив
`useAuthMode` измеряет контейнер и выбирает класс окна:
- **compact** (`<600`) — форма во всю ширину, одна колонка.
- **medium** (`600–959`) — центрированная карточка на фоне.
- **expanded** (`≥960`) — две колонки: бренд-рейл + карточка формы.

## Как собрано
Только из компонентов дизайн-системы (`window.PandoraSpecDesignSystem_b8d6e1`):
`CmpTopAppBar`, `CmpIconButton` (back — иконка `ArrowBack`), `CmpTextField`, `CmpButton`,
`CmpOutlinedButton`, `CmpTextButton`, `CmpCheckbox`, `CmpDialog`,
`CmpCircularProgressIndicator`, `CmpLinearProgressIndicator`, `CmpCard`, `CmpText`,
`CmpIcon`. Цвета — через роли, поэтому экраны корректны в обеих темах.

## Кандидаты в UI Kit
Композиции из существующих примитивов (новых `Cmp*` не вводим, помечены как кандидаты):
- **PasswordField** — `CmpTextField` + trailing `CmpIconButton` (Visibility/VisibilityOff).
- **LangSegment** — сегментированный переключатель EN | RU для топ-бара.

## Поведение прототипа (скриптовка)
- Login: email с «fail» → ошибка; «new»/«activate» → диалог активации; иначе → logged-in.
- Registration: невалидные поля → validation; email с «fail» → server error; иначе → success.
- Forgot: email с «fail» → error; иначе → success.
- Logout всегда успешен и возвращает на чистый Login.
