// AuthScreens — presentational auth screens for Pandora Spec, built ONLY from
// design-system Cmp* components (window.PandoraSpecDesignSystem_b8d6e1).
//
// These are pure/visual: every screen takes an explicit display state + values
// + handlers, so the same components render both the live interactive flow
// (Auth.jsx → AuthFlow) and the pinned state gallery (AuthStates.html).
//
// Adaptivity — Material 3 Window Size Classes by container width (CLAUDE.md):
//   • compact  (<600)   — form full-width, one column.
//   • medium   (600–959)— centered card on the background.
//   • expanded (≥960)   — two-pane: brand rail + form card.
//
// Candidate UI-Kit compositions (assembled from primitives, flagged in spec):
//   • PasswordField — CmpTextField + trailing visibility CmpIconButton.
//   • LangSegment   — EN | RU segmented toggle for the top app bar.

const AS_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpTextButton,
  CmpTextField, CmpCheckbox, CmpDialog, CmpCard, CmpText, CmpIcon,
  CmpCircularProgressIndicator, CmpLinearProgressIndicator,
} = AS_DS;

// Layered brand backdrop — soft primary/tertiary "blooms" over the page tone.
// Gives the auth flow depth without leaving the palette (roles adapt per theme).
const AUTH_BG =
  "radial-gradient(140% 95% at 100% -15%, color-mix(in srgb, var(--primary-container) 88%, transparent) 0%, transparent 55%)," +
  "radial-gradient(125% 90% at -20% 115%, color-mix(in srgb, var(--tertiary-container) 80%, transparent) 0%, transparent 55%)," +
  "var(--background)";
// Big, finger-friendly primary CTA (taller + softer corners than the 40dp default).
const BIG_BTN = { height: 52, borderRadius: 16, fontSize: 16 };

function AuthBackdrop() {
  return <div aria-hidden="true" style={{ position: "absolute", inset: 0, background: AUTH_BG, pointerEvents: "none" }} />;
}

// App icon (real brand mark dropped in by the user).
const APP_ICON = "./app-icon.png";

// ── Container-width hook → Window Size Class ─────────────────────────────────
function useAuthMode() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const remeasure = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => { ro.disconnect(); window.removeEventListener("resize", remeasure); };
  }, []);
  let mode = "compact";
  if (w >= 960) mode = "expanded";
  else if (w >= 600) mode = "medium";
  return [ref, mode];
}

// ── EN | RU segment (UI-Kit candidate: LangSegment) ──────────────────────────
function LangSegment({ lang, setLang }) {
  return (
    <div style={{ display: "flex", background: "var(--surface-variant)", borderRadius: 999, padding: 3 }}>
      {["en", "ru"].map((l) => {
        const on = lang === l;
        return (
          <button
            key={l}
            type="button"
            onClick={() => setLang && setLang(l)}
            aria-pressed={on}
            style={{
              border: "none", cursor: "pointer", minWidth: 46, height: 38,
              borderRadius: 999, padding: "0 14px",
              background: on ? "var(--primary)" : "transparent",
              color: on ? "var(--on-primary)" : "var(--on-surface-variant)",
              fontFamily: "var(--font-sans)", fontSize: 13, fontWeight: 600, letterSpacing: 0.4,
            }}
          >
            {l.toUpperCase()}
          </button>
        );
      })}
    </div>
  );
}

// ── Brand lockup: app icon + wordmark ────────────────────────────────────────
function LogoPlaceholder({ t, big = false, align = "center" }) {
  const size = big ? 84 : 60;
  const center = align === "center";
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: center ? "center" : "flex-start", gap: big ? 18 : 12 }}>
      <img
        src={APP_ICON}
        alt={t("appName")}
        width={size}
        height={size}
        style={{ display: "block", filter: "drop-shadow(0 10px 22px color-mix(in srgb, var(--primary) 35%, transparent))" }}
      />
      <div style={{ display: "flex", flexDirection: "column", alignItems: center ? "center" : "flex-start", gap: 2 }}>
        <CmpText variant={big ? "headlineMedium" : "titleLarge"} weight={700}>{t("appName")}</CmpText>
      </div>
    </div>
  );
}

// ── PasswordField (UI-Kit candidate) ─────────────────────────────────────────
function PasswordField({ t, value, onChange, show, setShow, isError, supportingText, disabled }) {
  return (
    <CmpTextField
      label={t("password")}
      value={value}
      onChange={onChange}
      type={show ? "text" : "password"}
      singleLine
      disabled={disabled}
      isError={isError}
      supportingText={supportingText}
      leadingIcon={<CmpIcon icon="Lock" />}
      trailingIcon={
        <CmpIconButton
          icon={show ? "VisibilityOff" : "Visibility"}
          contentDescription={show ? t("hidePassword") : t("showPassword")}
          onClick={() => !disabled && setShow && setShow(!show)}
          disabled={disabled}
          style={{ width: 40, height: 40 }}
        />
      }
    />
  );
}

// ── Soft error banner ────────────────────────────────────────────────────────
function ErrorBanner({ text }) {
  return (
    <div
      role="alert"
      style={{
        display: "flex", gap: "var(--space-m)", alignItems: "flex-start",
        padding: "12px 16px", borderRadius: "var(--radius-s)",
        background: "var(--error-container)", color: "var(--on-error-container)",
      }}
    >
      <CmpIcon icon="AlertCircle" color="var(--on-error-container)" />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="bodyMedium" color="var(--on-error-container)">{text}</CmpText>
      </div>
    </div>
  );
}

// ── Submit button with built-in loading spinner ──────────────────────────────
function SubmitButton({ loading, disabled, text, onClick, icon }) {
  if (loading) {
    return (
      <CmpButton fullWidth onClick={undefined} style={{ ...BIG_BTN, cursor: "default", pointerEvents: "none" }}>
        <CmpCircularProgressIndicator size={24} strokeWidth={3} color="var(--on-primary)" />
      </CmpButton>
    );
  }
  return <CmpButton fullWidth text={text} icon={icon} disabled={disabled} onClick={onClick} style={BIG_BTN} />;
}

// ── Shared chrome: layered backdrop + top bar + loading bar + body + dialog ───
function ScreenChrome({ topBar, loading, children, overlay }) {
  return (
    <div style={{ position: "relative", height: "100%", width: "100%", overflow: "hidden", color: "var(--on-background)" }}>
      <AuthBackdrop />
      <div style={{ position: "relative", zIndex: 1, height: "100%", display: "flex", flexDirection: "column" }}>
        {topBar}
        <div style={{ height: 4, flex: "0 0 auto" }}>
          {loading && <CmpLinearProgressIndicator />}
        </div>
        {children}
      </div>
      {overlay}
      <style>{`@keyframes authIn{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:none}}@media (prefers-reduced-motion:reduce){.auth-anim{animation:none!important}}`}</style>
    </div>
  );
}

// ── Body layout by window-size class ─────────────────────────────────────────
// A "floating" surface card on the brand backdrop. The card keeps --surface so
// the outlined-field label notch (also --surface) blends in; the backdrop uses
// --background so the card visibly lifts off it. `logoTop` adds the brand hero
// above the card on compact/medium (login only).
function AuthBody({ t, mode, logoTop, children }) {
  const card = (
    <CmpCard className="auth-anim" elevation={2} border style={{ boxSizing: "border-box", width: "100%", padding: 28, borderRadius: 28, animation: "authIn .5s ease both" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: "var(--space-l)" }}>{children}</div>
    </CmpCard>
  );

  if (mode === "expanded") {
    return (
      <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex" }}>
        <div style={{ margin: "auto", minWidth: 0, display: "flex", alignItems: "center", gap: 64, padding: "48px 56px", boxSizing: "border-box" }}>
          <div style={{ flex: "1 1 0", maxWidth: 420, display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: "var(--space-l)" }}>
            <LogoPlaceholder t={t} big />
            <div style={{ maxWidth: 360 }}>
              <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{t("loginSubtitle")}</CmpText>
            </div>
            <div style={{ width: 64, height: 4, borderRadius: 999, background: "var(--primary)", marginTop: 4 }} />
          </div>
          <div style={{ flex: "0 0 440px", maxWidth: 440 }}>{card}</div>
        </div>
      </div>
    );
  }

  if (mode === "medium") {
    return (
      <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex" }}>
        <div style={{ margin: "auto", minWidth: 0, display: "flex", flexDirection: "column", alignItems: "center", gap: "var(--space-xl)", padding: "40px 24px", boxSizing: "border-box", width: "100%", maxWidth: 420 }}>
          {logoTop && <LogoPlaceholder t={t} />}
          {card}
        </div>
      </div>
    );
  }

  // compact
  return (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex" }}>
      <div style={{ margin: "auto", minWidth: 0, display: "flex", flexDirection: "column", alignItems: "center", gap: "var(--space-xl)", padding: "24px 24px 32px", boxSizing: "border-box", width: "100%", maxWidth: 460 }}>
        {logoTop && <LogoPlaceholder t={t} />}
        {card}
      </div>
    </div>
  );
}

// Success panel (registration / forgot success) — replaces the form.
function SuccessPanel({ icon, title, text, primary }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: "var(--space-l)", padding: "8px 0" }}>
      <div style={{ width: 72, height: 72, borderRadius: 999, background: "color-mix(in srgb, var(--success) 18%, transparent)", display: "grid", placeItems: "center" }}>
        <CmpIcon icon={icon} size={40} color="var(--success)" />
      </div>
      <CmpText variant="headlineSmall">{title}</CmpText>
      <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{text}</CmpText>
      <div style={{ width: "100%", paddingTop: "var(--space-s)" }}>{primary}</div>
    </div>
  );
}

// ════════════════════════════════════════════════════════════════════════════
// AUTH ROOT — session check
// ════════════════════════════════════════════════════════════════════════════
function AuthRootScreen({ t, lang, setLang, state = "loading", onRetry }) {
  const topBar = <CmpTopAppBar title="" style={{ background: "transparent" }} actions={<LangSegment lang={lang} setLang={setLang} />} />;
  return (
    <ScreenChrome topBar={topBar}>
      <div style={{ flex: 1, minHeight: 0, display: "flex", alignItems: "center", justifyContent: "center", padding: 32 }}>
        {state === "error" ? (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: "var(--space-l)", maxWidth: 360 }}>
            <CmpIcon icon="AlertCircle" size={40} color="var(--error)" />
            <CmpText variant="titleLarge">{t("rootErrorTitle")}</CmpText>
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t("rootErrorText")}</CmpText>
            <div style={{ paddingTop: "var(--space-s)" }}>
              <CmpButton text={t("retry")} icon="Refresh" onClick={onRetry} />
            </div>
          </div>
        ) : (
          // Loading — calm, no "checking authorization" text.
          <CmpCircularProgressIndicator size={48} />
        )}
      </div>
    </ScreenChrome>
  );
}

// ════════════════════════════════════════════════════════════════════════════
// LOGIN
// ════════════════════════════════════════════════════════════════════════════
function LoginScreen(props) {
  const {
    t, lang, setLang, mode = "compact",
    email = "", password = "", showPassword = false,
    onEmail, onPassword, onToggleShow,
    loading = false, error = false, canSubmit = false,
    onSubmit, onForgot, onSignUp,
    activationOpen = false, activationSent = false, onResendActivation, onCloseActivation,
  } = props;

  const topBar = <CmpTopAppBar title="" style={{ background: "transparent" }} actions={<LangSegment lang={lang} setLang={setLang} />} />;

  const form = (
    <React.Fragment>
      {/* Heading (shown in body on compact/medium; expanded rail carries brand) */}
      {mode === "expanded" && (
        <CmpText variant="headlineSmall">{t("loginTitle")}</CmpText>
      )}

      {error && <ErrorBanner text={t("loginError")} />}

      <CmpTextField
        label={t("email")} value={email} onChange={onEmail}
        type="email" singleLine disabled={loading} isError={error}
        leadingIcon={<CmpIcon icon="Email" />}
      />
      <PasswordField t={t} value={password} onChange={onPassword} show={showPassword} setShow={onToggleShow} disabled={loading} isError={error} />

      <div style={{ display: "flex", justifyContent: "flex-end", marginTop: -12 }}>
        <CmpTextButton text={t("forgot")} onClick={onForgot} disabled={loading} />
      </div>

      <div style={{ marginTop: "var(--space-s)" }}>
        <SubmitButton loading={loading} disabled={!canSubmit} text={t("loginBtn")} onClick={onSubmit} />
      </div>

      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 4, flexWrap: "wrap", paddingTop: "var(--space-xs)" }}>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t("noAccount")}</CmpText>
        <CmpTextButton text={t("signUp")} onClick={onSignUp} disabled={loading} />
      </div>
    </React.Fragment>
  );

  const dialog = (
    <CmpDialog
      open={activationOpen}
      onDismissRequest={onCloseActivation}
      icon="Email"
      title={t("activationTitle")}
      text={activationSent ? t("activationSent") : t("activationText")}
      dismissButton={<CmpTextButton text={t("close")} onClick={onCloseActivation} />}
      confirmButton={!activationSent && <CmpTextButton text={t("resendActivation")} onClick={onResendActivation} />}
    />
  );

  return (
    <ScreenChrome topBar={topBar} loading={loading} overlay={dialog}>
      <AuthBody t={t} mode={mode} logoTop={mode !== "expanded"}>{form}</AuthBody>
    </ScreenChrome>
  );
}

// ════════════════════════════════════════════════════════════════════════════
// REGISTRATION
// ════════════════════════════════════════════════════════════════════════════
function RegisterScreen(props) {
  const {
    t, lang, setLang, mode = "compact", onBack,
    name = "", email = "", phone = "", password = "", showPassword = false, installer = false,
    onName, onEmail, onPhone, onPassword, onToggleShow, onInstaller,
    loading = false, errors = {}, serverError = false, success = false,
    canSubmit = false, onSubmit, onGoLogin,
  } = props;

  const topBar = (
    <CmpTopAppBar
      title={t("registerTitle")}
      style={{ background: "transparent" }}
      navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={t("backToLogin")} onClick={onBack} />}
      actions={<LangSegment lang={lang} setLang={setLang} />}
    />
  );

  let body;
  if (success) {
    body = (
      <AuthBody t={t} mode={mode}>
        <SuccessPanel
          icon="CheckCircle"
          title={t("regSuccessTitle")}
          text={t("regSuccessText")}
          primary={<CmpButton fullWidth text={t("goToLogin")} onClick={onGoLogin} style={BIG_BTN} />}
        />
      </AuthBody>
    );
  } else {
    const form = (
      <React.Fragment>
        {serverError && <ErrorBanner text={t("serverError")} />}
        <CmpTextField
          label={t("fullName")} value={name} onChange={onName} singleLine disabled={loading}
          isError={!!errors.name} supportingText={errors.name ? t("errName") : undefined}
          leadingIcon={<CmpIcon icon="Person" />}
        />
        <CmpTextField
          label={t("email")} value={email} onChange={onEmail} type="email" singleLine disabled={loading}
          isError={!!errors.email} supportingText={errors.email ? t("errEmail") : undefined}
          leadingIcon={<CmpIcon icon="Email" />}
        />
        <CmpTextField
          label={t("phone")} value={phone} onChange={onPhone} type="tel" singleLine disabled={loading}
          isError={!!errors.phone} supportingText={errors.phone ? t("errPhone") : undefined}
          leadingIcon={<CmpIcon icon="Phone" />}
        />
        <PasswordField
          t={t} value={password} onChange={onPassword} show={showPassword} setShow={onToggleShow} disabled={loading}
          isError={!!errors.password} supportingText={errors.password ? t("errPasswordShort") : undefined}
        />
        <label style={{ display: "flex", alignItems: "center", gap: 4, cursor: loading ? "default" : "pointer", marginLeft: -12 }}>
          <CmpCheckbox checked={installer} onCheckedChange={loading ? undefined : onInstaller} disabled={loading} />
          <CmpText variant="bodyMedium">{t("installer")}</CmpText>
        </label>
        <div style={{ marginTop: "var(--space-s)" }}>
          <SubmitButton loading={loading} disabled={!canSubmit} text={t("registerBtn")} onClick={onSubmit} />
        </div>
        <div style={{ display: "flex", justifyContent: "center", paddingTop: "var(--space-xs)" }}>
          <CmpTextButton text={t("backToLogin")} icon="ArrowBack" onClick={onBack} disabled={loading} />
        </div>
      </React.Fragment>
    );
    body = <AuthBody t={t} mode={mode}>{form}</AuthBody>;
  }

  return (
    <ScreenChrome topBar={topBar} loading={loading}>
      {body}
    </ScreenChrome>
  );
}

// ════════════════════════════════════════════════════════════════════════════
// FORGOT PASSWORD
// ════════════════════════════════════════════════════════════════════════════
function ForgotScreen(props) {
  const {
    t, lang, setLang, mode = "compact", onBack,
    email = "", onEmail,
    loading = false, error = false, success = false,
    canSubmit = false, onSubmit, onGoLogin,
  } = props;

  const topBar = (
    <CmpTopAppBar
      title={t("forgotTitle")}
      style={{ background: "transparent" }}
      navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={t("backToLogin")} onClick={onBack} />}
      actions={<LangSegment lang={lang} setLang={setLang} />}
    />
  );

  let body;
  if (success) {
    body = (
      <AuthBody t={t} mode={mode}>
        <SuccessPanel
          icon="Email"
          title={t("forgotSuccessTitle")}
          text={t("forgotSuccessText")}
          primary={<CmpButton fullWidth text={t("goToLogin")} onClick={onGoLogin} style={BIG_BTN} />}
        />
      </AuthBody>
    );
  } else {
    const form = (
      <React.Fragment>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t("forgotSubtitle")}</CmpText>
        {error && <ErrorBanner text={t("forgotError")} />}
        <CmpTextField
          label={t("email")} value={email} onChange={onEmail} type="email" singleLine disabled={loading} isError={error}
          leadingIcon={<CmpIcon icon="Email" />}
        />
        <div style={{ marginTop: "var(--space-s)" }}>
          <SubmitButton loading={loading} disabled={!canSubmit} text={t("restoreBtn")} onClick={onSubmit} />
        </div>
        <div style={{ display: "flex", justifyContent: "center", paddingTop: "var(--space-xs)" }}>
          <CmpTextButton text={t("backToLogin")} icon="ArrowBack" onClick={onBack} disabled={loading} />
        </div>
      </React.Fragment>
    );
    body = <AuthBody t={t} mode={mode}>{form}</AuthBody>;
  }

  return (
    <ScreenChrome topBar={topBar} loading={loading}>
      {body}
    </ScreenChrome>
  );
}

// ════════════════════════════════════════════════════════════════════════════
// LOGGED-IN PLACEHOLDER
// ════════════════════════════════════════════════════════════════════════════
function LoggedInScreen(props) {
  const {
    t, lang, setLang, mode = "compact",
    loading = false, error = false, onLogout,
  } = props;

  const topBar = <CmpTopAppBar title={t("appName")} style={{ background: "transparent" }} actions={<LangSegment lang={lang} setLang={setLang} />} />;

  const content = (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: "var(--space-l)" }}>
      <div style={{ width: 72, height: 72, borderRadius: 999, background: "var(--primary-container)", display: "grid", placeItems: "center" }}>
        <CmpIcon icon="CheckCircle" size={40} color="var(--on-primary-container)" />
      </div>
      <CmpText variant="headlineSmall">{t("loggedInTitle")}</CmpText>
      <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t("loggedInText")}</CmpText>
      {error && <div style={{ width: "100%" }}><ErrorBanner text={t("logoutError")} /></div>}
      <div style={{ width: "100%", paddingTop: "var(--space-s)" }}>
        {loading ? (
          <CmpOutlinedButton fullWidth onClick={undefined} style={{ ...BIG_BTN, cursor: "default", pointerEvents: "none" }}>
            <CmpCircularProgressIndicator size={24} strokeWidth={3} color="var(--primary)" />
          </CmpOutlinedButton>
        ) : (
          <CmpOutlinedButton fullWidth text={t("logout")} icon="ExitToApp" onClick={onLogout} style={BIG_BTN} />
        )}
      </div>
    </div>
  );

  const wrap = (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex", padding: mode === "compact" ? "24px 24px 32px" : 40, boxSizing: "border-box" }}>
      <CmpCard className="auth-anim" elevation={2} border style={{ boxSizing: "border-box", margin: "auto", minWidth: 0, width: "100%", maxWidth: mode === "compact" ? 440 : 420, padding: 28, borderRadius: 28, animation: "authIn .5s ease both" }}>
        {content}
      </CmpCard>
    </div>
  );

  return (
    <ScreenChrome topBar={topBar} loading={loading}>
      {wrap}
    </ScreenChrome>
  );
}

window.AuthScreens = {
  useAuthMode, LangSegment, LogoPlaceholder, PasswordField,
  AuthRootScreen, LoginScreen, RegisterScreen, ForgotScreen, LoggedInScreen,
};
