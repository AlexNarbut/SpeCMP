// Home.jsx — Pandora Spec main-screen controller.
//   • HomeApp — live, interactive screen. Owns ephemeral UI (menu open, sync
//     in progress); theme / card-variant / profile are controlled from the host
//     HTML so the demo toolbars stay in sync with the in-screen drawer.
// Presentational blocks come from HomeScreens.jsx, so the prototype and the
// parts/states gallery never drift apart. Built only from the design system.

const { HomeScreen, PROFILES } = window.HomeScreens;

// Card interaction states, in display order (used by the parts gallery).
window.HOME_CARD_STATES = ["default", "hover", "focus", "pressed", "disabled"];

function HomeApp({
  dark = false,
  onDark = () => {},
  cardVariant = "centered",
  profile = "full",
  initialMenuOpen = false,
  initialLoading = true,
  loadMs = 1500,
  platform = "mobile",
  appVersion = "2.4.1",
  latestVersion = "2.5.0",
  initialUpdateState = "available",
  onLogout = null,
}) {
  const { useState, useRef, useEffect } = React;
  const [menuOpen, setMenuOpen] = useState(initialMenuOpen);
  const [syncing, setSyncing] = useState(false);
  const [loading, setLoading] = useState(initialLoading);
  const [route, setRoute] = useState("home");
  const isDesktop = platform === "desktop";
  // App-version update is a desktop-only capability (download a newer .dmg build).
  const [updateState, setUpdateState] = useState(isDesktop ? initialUpdateState : "idle");
  const timer = useRef(null);
  const loadTimer = useRef(null);
  const upTimer = useRef(null);
  useEffect(() => () => { clearTimeout(timer.current); clearTimeout(loadTimer.current); clearTimeout(upTimer.current); }, []);

  // Simulate initial data load, then reveal the panel (scripted, like auth root).
  useEffect(() => {
    if (!loading) return;
    clearTimeout(loadTimer.current);
    loadTimer.current = setTimeout(() => setLoading(false), loadMs);
    return () => clearTimeout(loadTimer.current);
  }, [loading, loadMs]);

  // Follow host toggles for the persistent panel (expanded/large).
  useEffect(() => { setMenuOpen(initialMenuOpen); }, [initialMenuOpen]);

  const onSync = () => {
    if (syncing) return;
    setSyncing(true);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setSyncing(false), 1700);
  };

  const onNav = (key) => {
    // Prototype: real routing lands here. Close the drawer on selection.
    if (key === "settings" || key === "logout") setMenuOpen(false);
    if (key === "logout" && onLogout) onLogout();
    if (key === "support" || key === "requests") {
      // «Заявки на техпомощь» → экран заявок для этой платформы.
      const target = isDesktop ? "tech-requests-desktop" : "tech-requests";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "quick") {
      // «Быстрый монтаж» → мастер быстрого монтажа (поиск только сигнализаций — его первый шаг).
      const target = isDesktop ? "fast-mount-desktop" : "fast-mount";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "extended") {
      // «Расширенный монтаж» → экран поиска и выбора устройства.
      const target = isDesktop ? "device-select-desktop" : "device-select";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "firmware") {
      // «Архив прошивок» без подключённого блока → вход Б: выбор модели → архив.
      const target = isDesktop ? "firmware-desktop" : "firmware";
      if (window.psHas && window.psHas(target)) window.psGo(target, "nounit");
    }
    if (key === "configurator") {
      const target = isDesktop ? "configurator-desktop" : "configurator";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
  };

  // Desktop: check for a newer app build, then surface the update banner.
  const onCheckUpdate = () => {
    if (updateState === "checking") return;
    setUpdateState("checking");
    clearTimeout(upTimer.current);
    upTimer.current = setTimeout(() => setUpdateState("available"), 1400);
  };
  const onOpenUpdate = () => { setMenuOpen(false); setRoute("update"); };
  const onBackHome = () => setRoute("home");
  const onDownload = () => { /* prototype: real app would start the .dmg download */ };

  const appUpdate = isDesktop
    ? { state: updateState, current: appVersion, latest: latestVersion, onCheck: onCheckUpdate, onOpen: onOpenUpdate }
    : null;

  const profileObj = PROFILES[profile] || PROFILES.full;

  return (
    <HomeScreen
      dark={dark}
      onDark={onDark}
      menuOpen={menuOpen}
      onMenu={() => setMenuOpen((v) => !v)}
      syncing={syncing}
      onSync={onSync}
      loading={loading}
      appUpdate={appUpdate}
      route={route}
      onBackHome={onBackHome}
      onDownload={onDownload}
      cardVariant={cardVariant}
      profile={profileObj}
      onNav={onNav}
    />
  );
}

window.HomeApp = HomeApp;
