// HomeScreens — presentational main screen for Pandora Spec, built ONLY from
// design-system Cmp* components (window.PandoraSpecDesignSystem_b8d6e1).
//
// The screen is a calm "installer work panel": top app bar + drawer + two equal
// primary action cards + a quiet grid of secondary tools. No visible section
// headings ("Монтаж"/"Инструменты"), no marketing hero, no data slop.
//
// Adaptivity — Material 3 Window Size Classes by container width (CLAUDE.md):
//   • compact  (<600)   — 1-col cards, tools as rows, modal drawer.
//   • medium   (600–839)— 2-col cards, 2-col tools, modal drawer.
//   • expanded (840–1199)— 2-col cards, 3-col tools, side-panel drawer.
//   • large    (≥1200)  — 2-col cards, 4-col tools, side-panel drawer.
//
// Everything takes explicit props/state so the same blocks render both the live
// interactive prototype (Home.jsx → HomeApp) and the parts/states gallery.

const HS_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpSurface, CmpDivider,
  CmpSwitch, CmpCircularProgressIndicator, CmpLinearProgressIndicator,
  CmpButton, CmpTextButton,
} = HS_DS;

// ── Icon map (Material Icons filled ligatures; swap for project icons) ───────
const ICON = {
  quick: "bolt", extended: "tune",
  configurator: "settings", firmware: "archive", cars: "directions_car",
  requests: "assignment", feedback: "feedback", product: "manage_search",
  menu: "menu", sync: "sync", settings: "settings", logout: "logout",
  themeDark: "dark_mode", themeLight: "light_mode", status: "check_circle", person: "person",
  support: "support_agent",
  appUpdate: "system_update", download: "download",
};

// ── Content data (icons = old-project SVGs via window.PS_MAIN_ICONS) ─────────
const MAIN_ACTIONS = [
  { key: "quick",    svg: "fast_mount", title: "Быстрый монтаж",     desc: "Пошаговая установка по готовым настройкам" },
  { key: "extended", svg: "adv_mount",  title: "Расширенный монтаж", desc: "Детальная настройка и проверка системы" },
];
const TOOLS = [
  { key: "configurator", svg: "settings_configurator", title: "Конфигуратор",         desc: "Создание и изменение настроек системы" },
  { key: "firmware",     svg: "firmware_archive",      title: "Архив прошивок",        desc: "Поиск и загрузка прошивок" },
  { key: "cars",         svg: "vehicle_support",       title: "Автомобили",            desc: "Поддерживаемые автомобили и функции" },
  { key: "requests",     svg: "tech_requests",         title: "Заявки на техпомощь",   desc: "Обратный звонок специалиста и история заявок" },
  { key: "feedback",     svg: "feedback",              title: "Обратная связь",        desc: "Отзыв о монтаже и улучшениях" },
  { key: "product",      icon: "manage_search",       title: "Информация о товаре",   desc: "Найти информацию о товаре и комплектации" },
];

const PROFILES = {
  full:    { name: "Иван Петров", email: "ivan@pandora.pro" },
  partial: { name: "",            email: "installer@pandora.pro" },
  noEmail: { name: "Иван Петров", email: "" },
};

// ── Container-width hook → Window Size Class ─────────────────────────────────
function useHomeMode() {
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
  if (w >= 1200) mode = "large";
  else if (w >= 840) mode = "expanded";
  else if (w >= 600) mode = "medium";
  return [ref, mode];
}

// ── Forced state layer (for the gallery: hover/focus/pressed) ────────────────
function stateOpacity(s) { return s === "pressed" ? 0.12 : s === "focus" ? 0.1 : s === "hover" ? 0.08 : 0; }
function ForcedOverlay({ state }) {
  const o = stateOpacity(state);
  if (!o) return null;
  return <span style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity: o, pointerEvents: "none", borderRadius: "inherit" }} />;
}

// ── Status chip in the top app bar ───────────────────────────────────────────
function StatusChip({ compact = false, syncing = false }) {
  if (syncing) {
    return (
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: compact ? "8px" : "6px 14px 6px 10px", borderRadius: 999, background: compact ? "transparent" : "var(--surface-variant)" }}>
        <CmpCircularProgressIndicator size={18} strokeWidth={2.5} />
        {!compact && <CmpText variant="labelLarge" color="var(--on-surface-variant)">Обновление…</CmpText>}
      </div>
    );
  }
  if (compact) {
    return (
      <div title="База обновлена" aria-label="База обновлена" style={{ width: 40, height: 40, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
        <CmpIcon icon={ICON.status} size={22} color="var(--success)" title="База обновлена" />
      </div>
    );
  }
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "6px 14px 6px 10px", borderRadius: 999, background: "var(--surface-variant)" }}>
      <CmpIcon icon={ICON.status} size={18} color="var(--success)" />
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">База обновлена</CmpText>
    </div>
  );
}

// ── Inline SVG icon (old-project asset; inlined so it inherits currentColor) ──
function PsSvgIcon({ name, size = 32 }) {
  const html = (window.PS_SECTION_ICONS && window.PS_SECTION_ICONS[name]) || (window.PS_MAIN_ICONS && window.PS_MAIN_ICONS[name]) || "";
  return <span aria-hidden="true" style={{ width: size, height: size, display: "inline-flex", flex: "0 0 auto" }} dangerouslySetInnerHTML={{ __html: html }} />;
}

// ── Icon tile ─────────────────────────────────────────────────────────────────
function IconTile({ icon, svg, size = 64, iconSize = 32, tint = "primary", muted = false }) {
  const bg = tint === "primary"
    ? (muted ? "color-mix(in srgb, var(--on-surface) 8%, transparent)" : "var(--primary-container)")
    : (muted ? "color-mix(in srgb, var(--on-surface) 8%, transparent)" : "var(--surface-variant)");
  const fg = tint === "primary"
    ? (muted ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--on-primary-container)")
    : (muted ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--on-surface-variant)");
  return (
    <div style={{ width: size, height: size, borderRadius: "var(--radius-m)", background: bg, color: fg, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
      {svg ? <PsSvgIcon name={svg} size={iconSize} /> : <CmpIcon icon={icon} size={iconSize} color={fg} />}
    </div>
  );
}

// ── Primary action card (two equal, large) ───────────────────────────────────
// variant: "centered" (icon on top, centered) | "leading" (icon on the left)
function MainActionCard({ item, variant = "centered", state = "default", onClick }) {
  const disabled = state === "disabled";
  const clickable = !disabled && !!onClick;
  const focus = state === "focus";
  const centered = variant === "centered";

  const content = (
    <div
      style={{
        display: "flex",
        flexDirection: centered ? "column" : "row",
        alignItems: "center",
        textAlign: centered ? "center" : "left",
        gap: centered ? 16 : 18,
        opacity: disabled ? 0.5 : 1,
        width: "100%",
      }}
    >
      <IconTile svg={item.svg} icon={item.icon} size={64} iconSize={38} tint="primary" muted={disabled} />
      <div style={{ display: "flex", flexDirection: "column", gap: 6, alignItems: centered ? "center" : "flex-start", minWidth: 0 }}>
        <CmpText variant="titleLarge" weight={600} align={centered ? "center" : "left"}>{item.title}</CmpText>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)" align={centered ? "center" : "left"} style={{ textWrap: "pretty" }}>{item.desc}</CmpText>
      </div>
    </div>
  );

  return (
    <CmpSurface
      onClick={clickable ? onClick : undefined}
      color="var(--background-surface-card)"
      border
      radius="var(--r-card-medium-radius)"
      className="ps-focusable"
      role="button"
      tabIndex={clickable ? 0 : -1}
      aria-disabled={disabled || undefined}
      style={{
        boxSizing: "border-box",
        padding: centered ? "28px 24px" : "24px",
        minHeight: 168,
        display: "flex",
        alignItems: "center",
        justifyContent: centered ? "center" : "flex-start",
        cursor: clickable ? "pointer" : "default",
        outline: focus ? "2px solid var(--primary)" : "none",
        outlineOffset: 2,
        transition: "box-shadow var(--motion-fast)",
      }}
    >
      {content}
      <ForcedOverlay state={state} />
    </CmpSurface>
  );
}

// ── Secondary tool card (quieter than primary) ───────────────────────────────
// layout: "grid" (icon top, centered) | "row" (icon left, for compact lists)
function ToolCard({ item, layout = "grid", state = "default", onClick }) {
  const disabled = state === "disabled";
  const clickable = !disabled && !!onClick;
  const focus = state === "focus";
  const isRow = layout === "row";

  const content = (
    <div
      style={{
        display: "flex",
        flexDirection: isRow ? "row" : "column",
        alignItems: "center",
        textAlign: isRow ? "left" : "center",
        gap: isRow ? 16 : 12,
        opacity: disabled ? 0.5 : 1,
        width: "100%",
      }}
    >
      <IconTile svg={item.svg} icon={item.icon} size={48} iconSize={28} tint="neutral" muted={disabled} />
      <div style={{ display: "flex", flexDirection: "column", gap: 4, alignItems: isRow ? "flex-start" : "center", minWidth: 0 }}>
        <CmpText variant="titleMedium" align={isRow ? "left" : "center"} style={{ textWrap: "pretty" }}>{item.title}</CmpText>
        <CmpText variant="bodySmall" color="var(--on-surface-variant)" align={isRow ? "left" : "center"} maxLines={2} style={{ textWrap: "pretty" }}>{item.desc}</CmpText>
      </div>
    </div>
  );

  return (
    <CmpSurface
      onClick={clickable ? onClick : undefined}
      color="var(--surface)"
      border
      radius={12}
      className="ps-focusable"
      role="button"
      tabIndex={clickable ? 0 : -1}
      aria-disabled={disabled || undefined}
      style={{
        boxSizing: "border-box",
        padding: isRow ? "14px 16px" : "20px 16px",
        minHeight: isRow ? 72 : 132,
        display: "flex",
        alignItems: "center",
        justifyContent: isRow ? "flex-start" : "center",
        cursor: clickable ? "pointer" : "default",
        outline: focus ? "2px solid var(--primary)" : "none",
        outlineOffset: 2,
      }}
    >
      {content}
      <ForcedOverlay state={state} />
    </CmpSurface>
  );
}

// ── Drawer: profile block ─────────────────────────────────────────────────────
function ProfileBlock({ profile }) {
  const name = profile && profile.name ? profile.name.trim() : "";
  const email = profile && profile.email ? profile.email.trim() : "";
  const initials = name
    ? name.split(/\s+/).slice(0, 2).map((p) => p[0]).join("").toUpperCase()
    : "";
  const primary = name || email || "Профиль";
  const secondary = name ? (email || "Email не указан") : (name ? "" : "Имя не указано");

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 14, padding: "8px 4px 16px" }}>
      <div style={{ width: 52, height: 52, borderRadius: 999, background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}>
        {initials
          ? <CmpText variant="titleMedium" weight={600} color="var(--on-primary-container)">{initials}</CmpText>
          : <CmpIcon icon={ICON.person} size={28} color="var(--on-primary-container)" />}
      </div>
      <div style={{ minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <CmpText variant="titleMedium" maxLines={1}>{primary}</CmpText>
        {secondary && (
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)" maxLines={1}>{secondary}</CmpText>
        )}
      </div>
    </div>
  );
}

// ── Drawer: one row ───────────────────────────────────────────────────────────
function DrawerRow({ icon, label, trailing, onClick, spin = false }) {
  return (
    <CmpSurface
      onClick={onClick}
      color="transparent"
      radius={12}
      className="ps-focusable"
      role="button"
      tabIndex={0}
      style={{ boxSizing: "border-box", minHeight: 52, display: "flex", alignItems: "center", gap: 16, padding: "0 12px", cursor: onClick ? "pointer" : "default" }}
    >
      <CmpIcon icon={icon} size={24} color="var(--on-surface-variant)" style={spin ? { animation: "psSpin 1s linear infinite" } : undefined} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="bodyLarge" maxLines={1}>{label}</CmpText>
      </div>
      {trailing}
    </CmpSurface>
  );
}

// ── Drawer ────────────────────────────────────────────────────────────────────
function HomeDrawer({ variant = "modal", profile, dark, onDark, syncing, onSync, appUpdate = null, onNav = () => {} }) {
  const panel = variant === "panel";
  return (
    <nav
      aria-label="Меню"
      style={{
        width: panel ? 312 : "min(320px, 86vw)",
        maxWidth: panel ? 312 : 320,
        height: "100%",
        boxSizing: "border-box",
        display: "flex",
        flexDirection: "column",
        background: "var(--surface)",
        color: "var(--on-surface)",
        borderRight: panel ? "1px solid var(--border-highlight)" : "none",
        boxShadow: panel ? "none" : "var(--elevation-3)",
        padding: "20px 16px 16px",
      }}
    >
      <ProfileBlock profile={profile} />
      <CmpDivider />
      <div style={{ display: "flex", flexDirection: "column", gap: 2, paddingTop: 8 }}>
        <DrawerRow
          icon={dark ? ICON.themeDark : ICON.themeLight}
          label="Тёмная тема"
          onClick={() => onDark && onDark(!dark)}
          trailing={<CmpSwitch checked={dark} onCheckedChange={onDark} thumbIcon />}
        />
        <DrawerRow
          icon={ICON.sync}
          label={syncing ? "Обновление базы…" : "Обновить базу"}
          onClick={syncing ? undefined : onSync}
          spin={syncing}
          trailing={syncing ? <CmpCircularProgressIndicator size={20} strokeWidth={2.5} /> : null}
        />
        {appUpdate && appUpdate.platform === "desktop" && (
          <DrawerRow
            icon={ICON.appUpdate}
            label="Проверить обновления"
            onClick={appUpdate.onCheck}
          />
        )}
        {appUpdate && appUpdate.platform !== "desktop" && window.AppUpdate && (
          <window.AppUpdate.MenuUpdateRow dot={appUpdate.state === "available"} onClick={() => appUpdate.onOpen("menu")} />
        )}
        <DrawerRow icon={ICON.settings} label="Настройки" onClick={() => onNav("settings")} />
        <DrawerRow icon={ICON.logout} label="Выход" onClick={() => onNav("logout")} />
      </div>
      <div style={{ flex: 1 }} />
      <div style={{ display: "flex", flexDirection: "column", gap: 10, paddingTop: 12 }}>
        {appUpdate && appUpdate.platform === "desktop" && window.AppUpdate && (
          <window.AppUpdate.SideUpdateBlock c={appUpdate.ctl} onOpen={() => appUpdate.onOpen("sidebar")} />
        )}
        <div style={{ padding: "0 12px", display: "flex", alignItems: "center", gap: 6, flexWrap: "wrap" }}>
          <CmpText variant="labelMedium" color="var(--on-surface-variant)">Pandora Спец · версия {(appUpdate && appUpdate.current) || "2.4.1"}</CmpText>
          {appUpdate && appUpdate.state === "uptodate" && (
            <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>
              <CmpIcon icon="Check" size={14} color="var(--success)" />
              <CmpText variant="labelMedium" color="var(--success)">последняя</CmpText>
            </span>
          )}
        </div>
      </div>
    </nav>
  );
}

// ── Top app bar ───────────────────────────────────────────────────────────────
function HomeTopBar({ compactStatus = false, syncing = false, loading = false, onMenu, title = "Pandora Спец", back = false, onBack, required = null }) {
  return (
    <CmpTopAppBar
      centered
      title={title}
      style={{ flex: "0 0 auto" }}
      navigationIcon={back
        ? <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />
        : <CmpIconButton icon={ICON.menu} contentDescription="Открыть меню" onClick={onMenu} disabled={loading} />}
      actions={(loading || back) ? null : <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>{required && window.AppUpdate && <window.AppUpdate.RequiredUpdateButton onClick={required} />}<StatusChip compact={compactStatus} syncing={syncing} /></span>}
    />
  );
}

// ── Screen body (cards + tools), no visible section headings ─────────────────
function HomeContent({ mode, cardVariant = "centered", onNav = () => {}, banner = null }) {
  const compact = mode === "compact";
  const mainCols = compact ? 1 : 2;
  const toolCols = mode === "large" ? 4 : mode === "expanded" ? 3 : mode === "medium" ? 2 : 1;
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const maxW = mode === "large" || mode === "expanded" ? 1040 : "none";
  const toolLayout = compact ? "row" : "grid";

  return (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: maxW, margin: "0 auto", padding: `${pad}px ${pad}px ${pad + 8}px`, display: "flex", flexDirection: "column", gap: mode === "compact" ? 16 : 24, boxSizing: "border-box" }}>
        {banner}
        {/* Primary scenarios — two equal action cards */}
        <div style={{ display: "grid", gridTemplateColumns: `repeat(${mainCols}, 1fr)`, gap: mode === "compact" ? 12 : 16 }}>
          {MAIN_ACTIONS.map((item) => (
            <MainActionCard key={item.key} item={item} variant={cardVariant} onClick={() => onNav(item.key)} />
          ))}
        </div>
        {/* Secondary tools — quiet grid, no heading */}
        <div style={{ display: "grid", gridTemplateColumns: `repeat(${toolCols}, 1fr)`, gap: 12 }}>
          {TOOLS.map((item) => (
            <ToolCard key={item.key} item={item} layout={toolLayout} onClick={() => onNav(item.key)} />
          ))}
        </div>
      </div>
    </div>
  );
}

// ── Loading skeletons (screen loading state) ─────────────────────────
function Skel({ w, h, r = 8, style }) {
  return <div className="ps-skel" style={{ width: w, height: h, borderRadius: r, flex: "0 0 auto", ...style }} />;
}

function MainCardSkeleton({ variant = "centered" }) {
  const centered = variant === "centered";
  return (
    <div style={{ boxSizing: "border-box", padding: centered ? "28px 24px" : "24px", minHeight: 168, borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)", display: "flex", alignItems: "center", justifyContent: centered ? "center" : "flex-start" }}>
      <div style={{ display: "flex", flexDirection: centered ? "column" : "row", alignItems: "center", gap: centered ? 16 : 18, width: "100%" }}>
        <Skel w={64} h={64} r={16} />
        <div style={{ display: "flex", flexDirection: "column", gap: 10, alignItems: centered ? "center" : "flex-start", width: centered ? "auto" : "100%" }}>
          <Skel w={centered ? 150 : 180} h={18} r={6} />
          <Skel w={centered ? 200 : "80%"} h={13} r={6} />
        </div>
      </div>
    </div>
  );
}

function ToolCardSkeleton({ layout = "grid" }) {
  const isRow = layout === "row";
  return (
    <div style={{ boxSizing: "border-box", padding: isRow ? "14px 16px" : "20px 16px", minHeight: isRow ? 72 : 132, borderRadius: "var(--r-card-small-radius)", background: "var(--surface)", border: "1px solid var(--border-highlight)", display: "flex", alignItems: "center", justifyContent: isRow ? "flex-start" : "center" }}>
      <div style={{ display: "flex", flexDirection: isRow ? "row" : "column", alignItems: "center", gap: isRow ? 16 : 12, width: "100%" }}>
        <Skel w={48} h={48} r={12} />
        <div style={{ display: "flex", flexDirection: "column", gap: 8, alignItems: isRow ? "flex-start" : "center", width: isRow ? "100%" : "auto" }}>
          <Skel w={isRow ? 140 : 96} h={15} r={6} />
          <Skel w={isRow ? "70%" : 120} h={12} r={6} />
        </div>
      </div>
    </div>
  );
}

// Mirrors HomeContent layout so the skeleton reflows at every window-size class.
function HomeSkeleton({ mode, cardVariant = "centered" }) {
  const compact = mode === "compact";
  const mainCols = compact ? 1 : 2;
  const toolCols = mode === "large" ? 4 : mode === "expanded" ? 3 : mode === "medium" ? 2 : 1;
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const maxW = mode === "large" || mode === "expanded" ? 1040 : "none";
  const toolLayout = compact ? "row" : "grid";
  return (
    <div aria-busy="true" style={{ flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div style={{ maxWidth: maxW, margin: "0 auto", padding: `${pad}px ${pad}px ${pad + 8}px`, display: "flex", flexDirection: "column", gap: compact ? 16 : 24, boxSizing: "border-box" }}>
        <div style={{ display: "grid", gridTemplateColumns: `repeat(${mainCols}, 1fr)`, gap: compact ? 12 : 16 }}>
          {MAIN_ACTIONS.map((it) => <MainCardSkeleton key={it.key} variant={cardVariant} />)}
        </div>
        <div style={{ display: "grid", gridTemplateColumns: `repeat(${toolCols}, 1fr)`, gap: 12 }}>
          {TOOLS.map((it) => <ToolCardSkeleton key={it.key} layout={toolLayout} />)}
        </div>
      </div>
    </div>
  );
}

// ── App-version update: the update screen now lives in its own feature
// (features/app-update/AppUpdateScreen.jsx). The home update route reuses
// window.AppUpdate.UpdateFlow so there is a single source of truth.

// ── Full screen (chrome + drawer + content) ──────────────────────────────────
function HomeScreen({
  dark = false, onDark = () => {},
  menuOpen = false, onMenu = () => {},
  syncing = false, onSync = () => {},
  loading = false,
  appUpdate = null,
  route = "home", onBackHome = () => {}, onDownload = () => {},
  cardVariant = "centered",
  profile = PROFILES.full,
  onNav = () => {},
}) {
  const [rootRef, mode] = useHomeMode();
  const compact = mode === "compact";
  const sidePanel = mode === "expanded" || mode === "large";
  const showPanel = !loading && sidePanel && menuOpen;
  const showModal = !loading && !sidePanel && menuOpen;
  const inUpdate = route === "update";

  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", width: "100%", display: "flex", flexDirection: "column", overflow: "hidden", background: "var(--background)", color: "var(--on-background)" }}>
      <style>{`
        @keyframes psSpin { to { transform: rotate(360deg); } }
        @keyframes psDrawerIn { from { transform: translateX(-100%); } to { transform: none; } }
        @keyframes psScrimIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes psShimmer { from { background-position: 100% 0; } to { background-position: 0 0; } }
        .ps-focusable:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
        .ps-skel { background: linear-gradient(90deg, var(--surface-variant) 25%, var(--surface-container-highest) 37%, var(--surface-variant) 63%); background-size: 400% 100%; animation: psShimmer 1.4s ease infinite; }
        @media (prefers-reduced-motion: reduce) { .ps-anim { animation: none !important; } .ps-skel { animation: none !important; } }
      `}</style>

      <HomeTopBar
        compactStatus={compact}
        syncing={syncing}
        loading={loading}
        onMenu={onMenu}
        title={inUpdate ? "Обновление приложения" : "Pandora Спец"}
        back={inUpdate}
        onBack={onBackHome}
        required={appUpdate && appUpdate.required ? () => appUpdate.onOpen("required") : null}
      />
      {loading && <CmpLinearProgressIndicator />}

      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        {showPanel && (
          <div className="ps-anim" style={{ flex: "0 0 auto", animation: "psDrawerIn var(--motion-standard) both" }}>
            <HomeDrawer variant="panel" profile={profile} dark={dark} onDark={onDark} syncing={syncing} onSync={onSync} appUpdate={appUpdate} onNav={onNav} />
          </div>
        )}
        {loading
          ? <HomeSkeleton mode={mode} cardVariant={cardVariant} />
          : inUpdate
            ? (window.AppUpdate && appUpdate
                ? <window.AppUpdate.UpdateRoute ctl={appUpdate.ctl} mode={mode} from={appUpdate.from} onLater={onBackHome} />
                : null)
            : <HomeContent mode={mode} cardVariant={cardVariant} onNav={onNav}
                banner={appUpdate && appUpdate.platform !== "desktop" && appUpdate.state === "available" && !appUpdate.bannerHidden && window.AppUpdate
                  ? <window.AppUpdate.UpdateBanner latest={appUpdate.latest} onOpen={() => appUpdate.onOpen("banner")} onClose={appUpdate.onHideBanner} />
                  : null} />}
      </div>

      {appUpdate && appUpdate.toast && !inUpdate && window.AppUpdate && (
        <div style={{ position: "absolute", left: 16, right: 16, bottom: 16, zIndex: 20, display: "flex", justifyContent: "center" }}>
          <window.AppUpdate.UpdateToast latest={appUpdate.latest} onUpdate={appUpdate.onToastUpdate} onClose={appUpdate.onToastClose} />
        </div>
      )}
      {appUpdate && window.AppUpdate && <window.AppUpdate.UpdateDialogs c={appUpdate.ctl} />}

      {showModal && (
        <div style={{ position: "absolute", inset: 0, zIndex: 10, display: "flex" }}>
          <div
            className="ps-anim"
            onClick={onMenu}
            style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)", animation: "psScrimIn var(--motion-fast) both" }}
          />
          <div className="ps-anim" style={{ position: "relative", height: "100%", animation: "psDrawerIn var(--motion-standard) both" }}>
            <HomeDrawer variant="modal" profile={profile} dark={dark} onDark={onDark} syncing={syncing} onSync={onSync} appUpdate={appUpdate} onNav={onNav} />
          </div>
        </div>
      )}
    </div>
  );
}

window.HomeScreens = {
  useHomeMode, HomeScreen, HomeTopBar, HomeContent, HomeDrawer, ProfileBlock,
  MainActionCard, ToolCard, StatusChip, IconTile, PsSvgIcon,
  HomeSkeleton, MainCardSkeleton, ToolCardSkeleton,
  ICON, MAIN_ACTIONS, TOOLS, PROFILES,
};
