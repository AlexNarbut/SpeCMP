# Приведение feature/login (+auth-state) к дизайну auth-флоу

Источник дизайна: `design/features/auth/` (`AuthScreens.jsx`, `Auth.jsx` — каталог `AUTH_STATES`,
словарь `i18n.js`). Сверка выполнена по всем экранам и подсостояниям; тексты EN/RU в ресурсах
уже совпадают со словарём дизайна. Ниже — расхождения и поэтапный план правок.

Статусы: [ ] не начато · [x] готово

---

## Этап 1 — Auth Root: состояние ошибки проверки сессии + Retry

Дизайн `root/error`: AlertCircle 40dp цвета error, titleLarge «Не удалось проверить сессию»,
bodyMedium «Проверьте подключение и попробуйте снова.», кнопка «Повторить» (icon Refresh).
Сейчас `AuthStateViewModel` молча маппит `Response.Error` → `NO_AUTH` — сбой проверки
неотличим от «не залогинен».

- [x] `feature/auth-state`: добавить `AuthState.CHECK_FAILED`; в VM — retry-триггер
      (`MutableStateFlow` + `flatMapLatest`, `WAITING` на время повторной проверки), `fun retry()`.
- [x] `AuthStateNavGraph`: новый слот `errorState: @Composable (onRetry: () -> Unit) -> Unit`.
- [x] Экран ошибки `SessionCheckErrorScreen` в `feature/auth-state` + compose resources модуля
      (`values/strings.xml`, `values-ru/strings.xml`, ключи `authstate_root_*`, тексты из i18n:
      rootErrorTitle / rootErrorText / retry).
- [x] `composeApp/AppNavigation`: прокинуть слот.

## Этап 2 — Registration / Forgot: карточка, success-панели, error-баннеры

Дизайн: оба экрана рендерятся в той же «парящей» карточке, что и логин (без бренда над
карточкой на compact/medium, с бренд-рейлом на expanded); success **заменяет форму** панелью
(`SuccessPanel`: иконка 40dp в круге, заголовок, текст, полноширинная CTA «Перейти ко входу»);
ошибка — `ErrorBanner` (errorContainer + AlertCircle) **над** полями.

- [x] `LoginNavGraph`: Registration/Forgot из `AuthCenteredBody` → `AuthBody(layoutMode,
      showCompactBrand = false)`; `AuthCenteredBody` удалить (станет мёртвым).
- [x] Registration success: подключить готовый `AuthSuccessPanel` (CheckCircle,
      `login_registration_title_success`, msg_success, CTA go-to-login) вместо формы.
- [x] Forgot success: `AuthSuccessPanel` (Email, `login_forgot_title_success`, msg_success,
      CTA go-to-login) вместо формы.
- [x] Строка CTA: `login_common_btn_go_to_login` (общая для двух экранов; убрать неиспользуемый
      `login_registration_btn_go_to_login`).
- [x] Ошибки: `AuthErrorBanner` над полями вместо плоской `AuthErrorMessage`;
      `AuthMessage.kt` (Error/Success) удалить как мёртвый код.
- [x] Forgot: subtitle `login_forgot_subtitle_screen` над полем email.
- [x] Ссылки «Вернуться ко входу» под CTA (registration: `login_registration_btn_back_to_login`,
      forgot: `login_forgot_link_back_to_login`).
- [x] Submit-кнопки: заменить голый `CmpButton` + спиннер+текст на `AuthSubmitButton`
      (спиннер без текста, как в дизайне); убрать ставшую неиспользуемой
      `login_common_loading_registration`.

## Этап 3 — Registration: per-field валидация + иконки полей

Дизайн `register/validation`: у каждого поля `isError` + supportingText
(имя не пустое / валидный email / валидный телефон / пароль ≥ 6 символов); чекбокс гейтит
кнопку. Строки уже в ресурсах (`login_common_error_*_invalid`, `..._full_name_required`,
`..._password_short`), не используются.

- [x] domain: `SignUpValidationError` → случаи InvalidFullName / InvalidEmail /
      InvalidPhoneNumber / PasswordTooShort; `SignUpCredentials.validationErrors(): Set<...>`
      (email-формат, телефон, длина пароля ≥ 6).
- [x] presentation: `RegistrationFieldError` (enum) в state — `fieldErrors: Set<...>`;
      ошибка поля снимается при правке этого поля; при ошибках валидации submit не уходит
      в Loading.
- [x] UI: `isError` + supportingText на каждом поле; leading-иконки Person / Email / Phone;
      `AuthPasswordField` — параметр `supportingText`.
- [x] `AuthFormError.RequiredFields` у registration больше не используется (остаётся для forgot).

## Этап 4 — Activation dialog: иконка и логика текста

Дизайн `login/activation`: у диалога `icon="Email"`; после успешной повторной отправки текст
**заменяется** на «Письмо активации отправлено повторно», кнопка resend исчезает, остаётся
только «Закрыть».

- [x] `CmpDialog(icon = CmpIcons.Email.imageVector())`.
- [x] Текст диалога: один слот — error / sent / основной (замена, не дописывание).
- [x] После успеха скрыть confirm-кнопку (убрать дубль «Готово» при живой «Закрыть»).
- [x] Resend-loading (спиннер + «Отправка письма…») — сознательное расширение дизайна
      под реальную асинхронность, остаётся.

## Этап 5 — LangSegment по дизайну (компактный трек)

Дизайн: серый трек (surface-variant, radius 999, padding 3) + кнопки высотой 38dp
(minWidth 46), невыбранная — прозрачная на треке. Текущий компактный вариант без трека —
отступление; для точного трека нужно отключить M3-инфляцию touch-таргета внутри него.

- [x] `core/ui`: параметр `enforceMinimumTouchTarget: Boolean = true` у `CmpClickableSurface`
      (`LocalMinimumInteractiveComponentSize provides Dp.Unspecified` при false).
- [x] `AuthLanguageSegment`: вернуть трек, кнопки 38dp/minWidth 46, невыбранная прозрачная.
      Итоговая высота ~44dp (в 54dp-старом виде виновата была инфляция).

## Этап 6 — Полировка (отложено, обсудить)

- [ ] Root-экраны (loading/error) в дизайне используют auth-chrome (backdrop + LangSegment
      в топ-баре) — у нас плоский спиннер без хрома. Требует выноса chrome из feature/login.
- [ ] Expanded бренд-рейл: в дизайне subtitle «Sign in to continue the installation»
      (`login_login_subtitle_screen`) + акцент-черта 64×4 primary; у нас всюду
      «Specialist authorization» и черты нет.
- [ ] В теме нет success-цвета: `AuthSuccessPanel` использует primaryContainer
      (дизайн — `--success` 18%). Либо добавить success в `CmpThemeColors`, либо зафиксировать.
- [ ] Радиус CTA: дизайн 16dp, у нас дефолт `CmpButton`.
- [x] Прочие неиспользуемые строки удалены на этапах 2–5 (`btn_later`, `btn_send_email`,
      `btn_done`, `label_login_email`, `btn_show/hide_password`, `link_registration`,
      `loading_registration`, `registration_btn_go_to_login`). Остался только
      `login_login_subtitle_screen` — понадобится для бренд-рейла (пункт выше).

---

## Верификация

- Точечно: `./gradlew :feature:login:compileCommonMainKotlinMetadata`
  (+ `:feature:auth-state:...`, `:core:ui:...` на затронутых этапах).
- Финально: `./gradlew :androidApp:assembleDebug`.
