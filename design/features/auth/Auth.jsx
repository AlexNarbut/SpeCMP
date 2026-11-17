// Auth.jsx — Pandora Spec auth flow controller.
//   • AuthFlow   — live, interactive flow (real navigation + form state +
//                  scripted async). Used by Auth.html / AuthTablet.html /
//                  AuthDesktop.html.
//   • AuthPinned — renders one screen pinned to one explicit state with
//                  representative values. Used by AuthStates.html (gallery)
//                  and by the demo state-selector.
// Both reuse the presentational components from AuthScreens.jsx, so the live
// flow and the gallery never drift apart. Built only from the design system.

const {
  AuthRootScreen, LoginScreen, RegisterScreen, ForgotScreen, LoggedInScreen, useAuthMode,
} = window.AuthScreens || {};

// ── validation helpers ───────────────────────────────────────────────────────
const reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const rePhone = /^[+\d][\d\s()-]{5,}$/;
const validEmail = (v) => reEmail.test(v.trim());
const validPhone = (v) => rePhone.test(v.trim());

// ════════════════════════════════════════════════════════════════════════════
// AuthFlow — interactive
// ════════════════════════════════════════════════════════════════════════════
function AuthFlow({ lang: langProp, onLang, initialScreen = "login", onLoggedIn = null }) {
  const { useState, useRef, useEffect } = React;
  const [rootRef, mode] = useAuthMode();

  const [langState, setLangState] = useState("en");
  const lang = langProp || langState;
  const setLang = (l) => (onLang ? onLang(l) : setLangState(l));
  const t = window.AuthT(lang);

  const [screen, setScreen] = useState(initialScreen);
  const [busy, setBusy] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const timer = useRef(null);
  const after = (ms, fn) => { clearTimeout(timer.current); timer.current = setTimeout(fn, ms); };
  useEffect(() => () => clearTimeout(timer.current), []);

  // ── login state ──
  const [lEmail, setLEmail] = useState("");
  const [lPass, setLPass] = useState("");
  const [lError, setLError] = useState(false);
  const [activationOpen, setActivationOpen] = useState(false);
  const [activationSent, setActivationSent] = useState(false);

  // ── register state ──
  const [rName, setRName] = useState("");
  const [rEmail, setREmail] = useState("");
  const [rPhone, setRPhone] = useState("");
  const [rPass, setRPass] = useState("");
  const [installer, setInstaller] = useState(false);
  const [rErrors, setRErrors] = useState({});
  const [rServerError, setRServerError] = useState(false);
  const [rSuccess, setRSuccess] = useState(false);

  // ── forgot state ──
  const [fEmail, setFEmail] = useState("");
  const [fError, setFError] = useState(false);
  const [fSuccess, setFSuccess] = useState(false);

  // ── logged-in state ──
  const [logoutError, setLogoutError] = useState(false);

  // Root: simulate the session check, then land on login.
  useEffect(() => {
    if (initialScreen === "root") { setBusy(true); after(1300, () => { setBusy(false); setScreen("login"); }); }
    // eslint-disable-next-line
  }, []);

  const go = (s) => { setBusy(false); setLError(false); setRServerError(false); setFError(false); setLogoutError(false); setScreen(s); };

  // ── login submit (scripted): email contains "fail" → error,
  //    "activate"/"new" → activation dialog, else success → loggedIn. ──
  const loginCanSubmit = lEmail.trim() !== "" && lPass !== "" && !busy;
  const submitLogin = () => {
    if (!loginCanSubmit) return;
    setLError(false); setBusy(true);
    after(1300, () => {
      setBusy(false);
      const e = lEmail.toLowerCase();
      if (e.includes("fail")) setLError(true);
      else if (e.includes("activate") || e.includes("new")) { setActivationSent(false); setActivationOpen(true); }
      else if (onLoggedIn) onLoggedIn();      // app entry: hand off to the main feature
      else go("loggedIn");
    });
  };

  const registerCanSubmit = rName.trim() && rEmail.trim() && rPhone.trim() && rPass !== "" && installer && !busy;
  const submitRegister = () => {
    if (busy) return;
    const errs = {};
    if (!rName.trim()) errs.name = true;
    if (!validEmail(rEmail)) errs.email = true;
    if (!validPhone(rPhone)) errs.phone = true;
    if (rPass.length < 6) errs.password = true;
    if (!installer) return; // checkbox gates the button; nothing to submit
    if (Object.keys(errs).length) { setRErrors(errs); return; }
    setRErrors({}); setRServerError(false); setBusy(true);
    after(1400, () => {
      setBusy(false);
      if (rEmail.toLowerCase().includes("fail")) setRServerError(true);
      else setRSuccess(true);
    });
  };

  const forgotCanSubmit = fEmail.trim() !== "" && !busy;
  const submitForgot = () => {
    if (!forgotCanSubmit) return;
    setFError(false); setBusy(true);
    after(1300, () => {
      setBusy(false);
      if (fEmail.toLowerCase().includes("fail")) setFError(true);
      else setFSuccess(true);
    });
  };

  const submitLogout = () => {
    if (busy) return;
    setLogoutError(false); setBusy(true);
    after(1200, () => {
      setBusy(false);
      // reset login form for a clean return
      setLEmail(""); setLPass(""); setLError(false);
      go("login");
    });
  };

  let node;
  if (screen === "root") {
    node = <AuthRootScreen t={t} lang={lang} setLang={setLang} state={busy ? "loading" : "error"} onRetry={() => { setBusy(true); after(1300, () => { setBusy(false); go("login"); }); }} />;
  } else if (screen === "login") {
    node = (
      <LoginScreen
        t={t} lang={lang} setLang={setLang} mode={mode}
        email={lEmail} password={lPass} showPassword={showPassword}
        onEmail={setLEmail} onPassword={setLPass} onToggleShow={setShowPassword}
        loading={busy} error={lError} canSubmit={loginCanSubmit}
        onSubmit={submitLogin}
        onForgot={() => { setFEmail(""); setFError(false); setFSuccess(false); go("forgot"); }}
        onSignUp={() => { setRErrors({}); setRServerError(false); setRSuccess(false); go("register"); }}
        activationOpen={activationOpen} activationSent={activationSent}
        onResendActivation={() => setActivationSent(true)}
        onCloseActivation={() => setActivationOpen(false)}
      />
    );
  } else if (screen === "register") {
    node = (
      <RegisterScreen
        t={t} lang={lang} setLang={setLang} mode={mode} onBack={() => go("login")}
        name={rName} email={rEmail} phone={rPhone} password={rPass} showPassword={showPassword} installer={installer}
        onName={setRName} onEmail={setREmail} onPhone={setRPhone} onPassword={setRPass}
        onToggleShow={setShowPassword} onInstaller={setInstaller}
        loading={busy} errors={rErrors} serverError={rServerError} success={rSuccess}
        canSubmit={registerCanSubmit} onSubmit={submitRegister}
        onGoLogin={() => { setRSuccess(false); go("login"); }}
      />
    );
  } else if (screen === "forgot") {
    node = (
      <ForgotScreen
        t={t} lang={lang} setLang={setLang} mode={mode} onBack={() => go("login")}
        email={fEmail} onEmail={setFEmail}
        loading={busy} error={fError} success={fSuccess}
        canSubmit={forgotCanSubmit} onSubmit={submitForgot}
        onGoLogin={() => { setFSuccess(false); go("login"); }}
      />
    );
  } else {
    node = (
      <LoggedInScreen
        t={t} lang={lang} setLang={setLang} mode={mode}
        loading={busy} error={logoutError} onLogout={submitLogout}
      />
    );
  }

  return <div ref={rootRef} style={{ height: "100%", width: "100%" }}>{node}</div>;
}

// ════════════════════════════════════════════════════════════════════════════
// AuthPinned — render a screen pinned to an explicit state
// ════════════════════════════════════════════════════════════════════════════
const PIN = {
  login: {
    empty:      { email: "", password: "" },
    partial:    { email: "installer@", password: "" },
    ready:      { email: "ivan@pandora.pro", password: "service123" },
    loading:    { email: "ivan@pandora.pro", password: "service123", loading: true },
    error:      { email: "ivan@pandora.pro", password: "service123", error: true },
    activation: { email: "new.installer@pandora.pro", password: "service123", activationOpen: true },
  },
  register: {
    empty:      { name: "", email: "", phone: "", password: "", installer: false },
    partial:    { name: "Иван Петров", email: "ivan@", phone: "", password: "", installer: false },
    ready:      { name: "Иван Петров", email: "ivan@pandora.pro", phone: "+7 912 345-67-89", password: "service123", installer: true },
    loading:    { name: "Иван Петров", email: "ivan@pandora.pro", phone: "+7 912 345-67-89", password: "service123", installer: true, loading: true },
    validation: { name: "Иван", email: "ivan@", phone: "12", password: "123", installer: true, errors: { email: true, phone: true, password: true } },
    server:     { name: "Иван Петров", email: "ivan@pandora.pro", phone: "+7 912 345-67-89", password: "service123", installer: true, serverError: true },
    success:    { success: true },
  },
  forgot: {
    empty:   { email: "" },
    ready:   { email: "ivan@pandora.pro" },
    loading: { email: "ivan@pandora.pro", loading: true },
    error:   { email: "ivan@pandora.pro", error: true },
    success: { success: true },
  },
  loggedIn: {
    loggedIn:      {},
    logoutLoading: { loading: true },
    logoutError:   { error: true },
  },
  root: {
    loading: { state: "loading" },
    error:   { state: "error" },
  },
};

function AuthPinned({ screen, state, lang, setLang = () => {}, mode = "compact" }) {
  const { useState } = React;
  const t = window.AuthT(lang);
  const p = (PIN[screen] && PIN[screen][state]) || {};
  const [show, setShow] = useState(false);
  const noop = () => {};

  if (screen === "root") {
    return <AuthRootScreen t={t} lang={lang} setLang={setLang} state={p.state} onRetry={noop} />;
  }
  if (screen === "login") {
    const canSubmit = !!(p.email && p.password) && !p.loading;
    return (
      <LoginScreen
        t={t} lang={lang} setLang={setLang} mode={mode}
        email={p.email} password={p.password} showPassword={show}
        onEmail={noop} onPassword={noop} onToggleShow={setShow}
        loading={!!p.loading} error={!!p.error} canSubmit={canSubmit}
        onSubmit={noop} onForgot={noop} onSignUp={noop}
        activationOpen={!!p.activationOpen} activationSent={false}
        onResendActivation={noop} onCloseActivation={noop}
      />
    );
  }
  if (screen === "register") {
    const canSubmit = !!(p.name && p.email && p.phone && p.password && p.installer) && !p.loading;
    return (
      <RegisterScreen
        t={t} lang={lang} setLang={setLang} mode={mode} onBack={noop}
        name={p.name} email={p.email} phone={p.phone} password={p.password} showPassword={show} installer={!!p.installer}
        onName={noop} onEmail={noop} onPhone={noop} onPassword={noop} onToggleShow={setShow} onInstaller={noop}
        loading={!!p.loading} errors={p.errors || {}} serverError={!!p.serverError} success={!!p.success}
        canSubmit={canSubmit} onSubmit={noop} onGoLogin={noop}
      />
    );
  }
  if (screen === "forgot") {
    const canSubmit = !!p.email && !p.loading;
    return (
      <ForgotScreen
        t={t} lang={lang} setLang={setLang} mode={mode} onBack={noop}
        email={p.email} onEmail={noop}
        loading={!!p.loading} error={!!p.error} success={!!p.success}
        canSubmit={canSubmit} onSubmit={noop} onGoLogin={noop}
      />
    );
  }
  return (
    <LoggedInScreen
      t={t} lang={lang} setLang={setLang} mode={mode}
      loading={!!p.loading} error={!!p.error} onLogout={noop}
    />
  );
}

// State catalog (for selectors / gallery), in display order.
const AUTH_STATES = {
  root: ["loading", "error"],
  login: ["empty", "partial", "ready", "loading", "error", "activation"],
  register: ["empty", "partial", "ready", "loading", "validation", "server", "success"],
  forgot: ["empty", "ready", "loading", "error", "success"],
  loggedIn: ["loggedIn", "logoutLoading", "logoutError"],
};

window.AuthFlow = AuthFlow;
window.AuthPinned = AuthPinned;
window.AUTH_STATES = AUTH_STATES;
