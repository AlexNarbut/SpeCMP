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
  initialUpdateState = "available",
  updateRequired = false,
  onLogout = null,
}) {
  const { useState, useRef, useEffect } = React;
  const [menuOpen, setMenuOpen] = useState(initialMenuOpen);
  const [syncing, setSyncing] = useState(false);
  const [loading, setLoading] = useState(initialLoading);
  const [route, setRoute] = useState("home");
  const isDesktop = platform === "desktop";
  // Обновление приложения: один контроллер на главный и экран обновления (загрузка идёт в фоне).
  // Desktop — загрузка и установка; телефон/планшет — переход в магазин. Требует ../app-update/AppUpdateScreen.jsx.
  const AU = window.AppUpdate;
  const upd = AU ? AU.useUpdateCtl({ platform: isDesktop ? "win" : "android", initial: initialUpdateState }) : null;
  const [updFrom, setUpdFrom] = useState("menu");
  const [bannerHidden, setBannerHidden] = useState(false);
  const [toast, setToast] = useState(false);
  const toastDone = useRef(false);
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

  // Desktop: уведомление «Доступна версия…» — один раз за запуск, после загрузки главного.
  useEffect(() => {
    if (!isDesktop || loading || !upd || upd.s !== "available" || toastDone.current) return;
    toastDone.current = true; setToast(true);
    clearTimeout(upTimer.current); upTimer.current = setTimeout(() => setToast(false), 8000);
  }, [loading, upd && upd.s]);

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
    // «Информация о товаре» → экран с пустым полем.
    if (key === "product" && window.psProduct) window.psProduct("home");
  };

  // Открыть экран обновления. Из меню — с новой проверкой (если не идёт загрузка), из плашки/уведомления/блока — как есть.
  const openUpdate = (from = "menu") => {
    if (!isDesktop) setMenuOpen(false);
    setToast(false); setUpdFrom(from); setRoute("update");
    if (from === "menu" && upd && !/^(downloading|downloaded|installing)$/.test(upd.s)) upd.check();
  };
  const onBackHome = () => setRoute("home");
  const onDownload = () => {};

  const appUpdate = upd ? {
    platform: isDesktop ? "desktop" : "mobile",
    ctl: upd, state: upd.s, current: upd.inst.v, latest: upd.ch.v, from: updFrom,
    onCheck: () => openUpdate("menu"), onOpen: openUpdate,
    bannerHidden, onHideBanner: () => setBannerHidden(true),
    toast, onToastClose: () => setToast(false), onToastUpdate: () => { upd.update(); openUpdate("toast"); },
    required: updateRequired,
  } : null;

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
