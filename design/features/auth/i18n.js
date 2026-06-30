// Pandora Spec — Auth flow i18n dictionary (EN default/fallback, RU required).
// Plain JS module: exposes window.AuthI18N = { en: {...}, ru: {...} }.
// All user-facing auth strings live here so EN/RU can be toggled without
// clearing the form. English wording can run longer than Russian — layouts
// must hold both (see designer-requirements.md → «Языки и тексты»).
(function () {
  var STR = {
    en: {
      appName: "Pandora Spec",
      logoPlaceholder: "LOGO",

      // Auth root / session check
      rootErrorTitle: "Couldn't verify your session",
      rootErrorText: "Check your connection and try again.",
      retry: "Retry",

      // Login
      loginTitle: "Sign in",
      loginSubtitle: "Sign in to continue the installation",
      email: "Email",
      password: "Password",
      showPassword: "Show password",
      hidePassword: "Hide password",
      forgot: "Forgot password?",
      loginBtn: "Log in",
      noAccount: "Don't have an account?",
      signUp: "Sign up",
      loginError: "Wrong email or password",

      // Activation required (dialog)
      activationTitle: "Account not activated",
      activationText: "We sent an activation link to your email. Confirm it to sign in.",
      resendActivation: "Resend email",
      activationSent: "Activation email sent again",
      close: "Close",

      // Registration
      registerTitle: "Create account",
      fullName: "Full name",
      phone: "Phone number",
      installer: "I am an installer",
      registerBtn: "Create account",
      backToLogin: "Back to login",
      errEmail: "Enter a valid email",
      errPhone: "Enter a valid phone number",
      errName: "Enter your full name",
      errPasswordShort: "Password is too short",
      serverError: "Something went wrong. Please try again.",
      regSuccessTitle: "Account created",
      regSuccessText: "Your installer account is ready. Sign in to continue.",
      goToLogin: "Go to login",

      // Forgot password
      forgotTitle: "Reset password",
      forgotSubtitle: "Enter your email — we'll send recovery instructions.",
      restoreBtn: "Send instructions",
      forgotError: "Couldn't send. Check the email and try again.",
      forgotSuccessTitle: "Check your email",
      forgotSuccessText: "We sent recovery instructions to your email.",

      // Logged-in placeholder
      loggedInTitle: "You are signed in",
      loggedInText: "Your dashboard isn't ready yet — it's coming in a later version.",
      logout: "Log out",
      logoutError: "Couldn't log out. Please try again.",

      // Misc
      signedInAs: "Signed in as",
    },

    ru: {
      appName: "Pandora Spec",
      logoPlaceholder: "ЛОГО",

      rootErrorTitle: "Не удалось проверить сессию",
      rootErrorText: "Проверьте подключение и попробуйте снова.",
      retry: "Повторить",

      loginTitle: "Вход",
      loginSubtitle: "Войдите, чтобы продолжить монтаж",
      email: "Email",
      password: "Пароль",
      showPassword: "Показать пароль",
      hidePassword: "Скрыть пароль",
      forgot: "Забыли пароль?",
      loginBtn: "Войти",
      noAccount: "Нет аккаунта?",
      signUp: "Зарегистрироваться",
      loginError: "Неверный email или пароль",

      activationTitle: "Аккаунт не активирован",
      activationText: "Мы отправили ссылку активации на ваш email. Подтвердите её, чтобы войти.",
      resendActivation: "Отправить письмо повторно",
      activationSent: "Письмо активации отправлено повторно",
      close: "Закрыть",

      registerTitle: "Регистрация",
      fullName: "Имя и фамилия",
      phone: "Телефон",
      installer: "Я установщик",
      registerBtn: "Создать аккаунт",
      backToLogin: "Вернуться ко входу",
      errEmail: "Введите корректный email",
      errPhone: "Введите корректный телефон",
      errName: "Введите имя и фамилию",
      errPasswordShort: "Пароль слишком короткий",
      serverError: "Что-то пошло не так. Попробуйте снова.",
      regSuccessTitle: "Аккаунт создан",
      regSuccessText: "Аккаунт установщика готов. Войдите, чтобы продолжить.",
      goToLogin: "Перейти ко входу",

      forgotTitle: "Восстановление пароля",
      forgotSubtitle: "Введите email — пришлём инструкцию по восстановлению.",
      restoreBtn: "Отправить инструкцию",
      forgotError: "Не удалось отправить. Проверьте email и попробуйте снова.",
      forgotSuccessTitle: "Проверьте почту",
      forgotSuccessText: "Мы отправили инструкцию по восстановлению на ваш email.",

      loggedInTitle: "Вы вошли в систему",
      loggedInText: "Личный кабинет пока в разработке — появится в следующей версии.",
      logout: "Выйти",
      logoutError: "Не удалось выйти. Попробуйте снова.",

      signedInAs: "Вы вошли как",
    },
  };

  window.AuthI18N = STR;
  // Helper: t(lang) → function(key) with EN fallback.
  window.AuthT = function (lang) {
    var d = STR[lang] || STR.en;
    return function (key) {
      return d[key] != null ? d[key] : STR.en[key] != null ? STR.en[key] : key;
    };
  };
})();
