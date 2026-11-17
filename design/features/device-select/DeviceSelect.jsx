// DeviceSelectScreen — поиск и выбор устройства перед монтажом.
// Точка входа: после «Быстрый монтаж» / «Расширенный монтаж» на главном.
// Собран только из компонентов дизайн-системы (window namespace).
//
// Поток (state machine):
//   permission → (Разрешить) → scanning → list | empty
//   permission → (Отклонить) → denied
//   adapterOff → (Включить) → scanning
//   list → (выбор) → connecting        (compact/stacked)
//   list → (выбор) → detail → connecting (split/master-detail)
//
// Разрешения зависят от платформы:
//   android — BLE + USB;  ios — только BLE;  desktop — без запроса (сразу scanning).
//
// Адаптив по Material 3 Window Size Classes (CLAUDE.md → «Размеры экранов»):
//   compact (<600) — телефон: один столбец, строки на всю ширину.
//   stacked (600–839 портрет) — планшет-портрет: центрированный столбец, крупнее.
//   split (Expanded/ландшафт/Desktop) — master-detail: слева список, справа карточка+«Подключить».
//   Класс выбирается по реальной ширине контейнера, а не по типу устройства.

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton,
  CmpSurface, CmpText, CmpIcon, CmpDivider, CmpCircularProgressIndicator,
} = DS;

// ── Демо-данные найденных устройств (BLE + USB-HID) ──────────────────────────
const DEVICES = [
  { id: "dx4gs",  name: "Pandora DX-4GS",  transport: "ble", address: "AC:23:3F:A1:0C:7D", rssi: -48, type: "Сигнализация", mode: "normal",     phoneLink: true,  bound: true,  icon: "sensors" },
  { id: "dxl5000",name: "Pandora DXL-5000", transport: "ble", address: "C8:FD:19:22:8B:04", rssi: -67, type: "Сигнализация", mode: "pairing",    phoneLink: false, bound: false, icon: "sensors" },
  { id: "rmd5",   name: "Pandora RMD-5",    transport: "usb", address: "HID · 0483:5750",    systemId: "107131313131", vendor: "NPO Telemetry", vidPid: "0483:5750", firmware: "3.57", mode: "work",       type: "Модуль", icon: "memory" },
  { id: "rmd4",   name: "Pandora RMD-4",    transport: "usb", address: "HID · 0483:5751",    systemId: null,           vendor: "NPO Telemetry", vidPid: "0483:5751", firmware: "3.42", mode: "bootloader", type: "Модуль", icon: "memory" },
  { id: "d032",   name: "Pandora D-032",    transport: "ble", address: "7A:11:0D:5E:C3:90", rssi: -75, type: "Метка",        mode: "normal",     phoneLink: false, bound: false, icon: "vpn_key" },
  { id: "vx4g",   name: "Pandora VX-4G",    transport: "ble", address: "3C:2E:FF:90:11:AA", rssi: -83, type: "Сигнализация", mode: "bootloader", phoneLink: true,  bound: true,  icon: "sensors" },
];

// Поля «до подключения» (значения из эфира / USB-дескриптора).
const MODE_LABEL = { normal: "Обычный", pairing: "Сопряжение", bootloader: "Загрузчик", work: "Рабочий" };
function infoRows(d, platform) {
  const strong = d.mode === "pairing" || d.mode === "bootloader";
  if (d.transport === "usb") {
    return [
      { label: "ID блока (systemId)", value: d.systemId || "не определён", mono: true },
      { label: "Производитель", value: d.vendor },
      { label: "VID:PID", value: d.vidPid, mono: true },
      { label: "Версия прошивки", value: d.firmware, mono: true },
      { label: "Режим", value: MODE_LABEL[d.mode], strong },
    ];
  }
  const rows = [
    { label: platform === "ios" ? "ID (UUID)" : "MAC-адрес", value: d.address, mono: true },
    { label: "Тип устройства", value: d.type },
    { label: "Уровень сигнала", value: `${d.rssi} dBm`, mono: true },
    { label: "Режим", value: MODE_LABEL[d.mode], strong },
  ];
  if (platform === "android") rows.push({ label: "Связь с телефоном", value: d.phoneLink ? "есть" : "нет" });
  rows.push({ label: "Связь у самого устройства", value: d.bound ? "есть" : "нет" });
  return rows;
}
const INFO_NOTE = { ble: "Версия прошивки и ID блока в эфире не передаются — они появятся после подключения.", usb: "" };

// Список полей «ключ — значение».
function InfoRows({ rows }) {
  return (
    <div style={{ width: "100%", display: "flex", flexDirection: "column" }}>
      {rows.map((r, i) => (
        <React.Fragment key={r.label}>
          {i > 0 && <CmpDivider />}
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16, padding: "12px 2px" }}>
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{r.label}</CmpText>
            <span style={{ fontFamily: r.mono ? "var(--font-mono)" : "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: r.strong ? 700 : 500, color: r.strong ? "var(--primary)" : "var(--on-surface)", textAlign: "right" }}>{r.value}</span>
          </div>
        </React.Fragment>
      ))}
    </div>
  );
}

const TRANSPORT = {
  ble: { icon: "bluetooth", label: "Bluetooth", short: "BLE" },
  usb: { icon: "usb",       label: "USB" },
};

// Инъекция анимаций один раз (радар / появление).
(function injectStyles() {
  if (document.getElementById("ds-device-select-anim")) return;
  const s = document.createElement("style");
  s.id = "ds-device-select-anim";
  s.textContent =
    "@keyframes dsPing{0%{transform:scale(.65);opacity:.55}80%{transform:scale(1.9);opacity:0}100%{opacity:0}}" +
    "@keyframes dsRowIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}" +
    "@media (prefers-reduced-motion: reduce){.ds-ping,.ds-row{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Измерение контейнера → активный класс окна ───────────────────────────────
function useLayout() {
  const ref = React.useRef(null);
  const [size, setSize] = React.useState({ w: 0, h: 0 });
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const remeasure = () => setSize({ w: el.offsetWidth, h: el.offsetHeight });
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => { ro.disconnect(); window.removeEventListener("resize", remeasure); };
  }, []);
  const { w, h } = size;
  let mode = "compact";
  if (w >= 600) mode = w > h * 1.1 ? "split" : "stacked";
  return [ref, mode, size];
}

// ── Мелкие строительные блоки ────────────────────────────────────────────────

// Плейсхолдер-слот фото устройства (пользователь заменит реальным снимком).
function DeviceTile({ icon, size = 56, radius = 12, showCaption = false }) {
  return (
    <div style={{
      width: size, height: size, borderRadius: radius, flex: "0 0 auto",
      position: "relative", overflow: "hidden", display: "grid", placeItems: "center",
      border: "1px solid var(--border-highlight)",
      background: "repeating-linear-gradient(135deg, var(--surface-variant) 0 6px, color-mix(in srgb, var(--surface-variant) 55%, var(--surface)) 6px 12px)",
    }}>
      <CmpIcon icon={icon} size={Math.round(size * 0.42)} color="color-mix(in srgb, var(--on-surface-variant) 78%, transparent)" />
      {showCaption && (
        <span style={{
          position: "absolute", left: 0, right: 0, bottom: 0, padding: "2px 0",
          fontFamily: "var(--font-mono)", fontSize: 9, letterSpacing: .3, textAlign: "center",
          color: "var(--on-surface-variant)", background: "color-mix(in srgb, var(--surface) 70%, transparent)",
        }}>фото устройства</span>
      )}
    </div>
  );
}

// Правая метка карточки: RSSI (BLE) или systemId / «HID» (USB).
function MetaLabel({ text, selected }) {
  return (
    <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", fontWeight: 500, whiteSpace: "nowrap", color: selected ? "inherit" : "var(--on-surface-variant)" }}>{text}</span>
  );
}

// Чип транспорта (Bluetooth / USB).
function TransportChip({ transport, short }) {
  const t = TRANSPORT[transport];
  return (
    <span style={{
      display: "inline-flex", alignItems: "center", gap: 4, height: 22, padding: "0 8px",
      borderRadius: 999, background: "var(--secondary-container)", color: "var(--on-secondary-container)",
      fontFamily: "var(--font-sans)", fontSize: "var(--t-additional1-size)", fontWeight: 500, flex: "0 0 auto",
    }}>
      <CmpIcon icon={t.icon} size={14} />{short && t.short ? t.short : t.label}
    </span>
  );
}

// Строка устройства в списке.
function DeviceRow({ device, selected, onClick, onInfo, tileSize = 56 }) {
  const { hover, focus, active, opacity, handlers } = DS.useStateLayer ? DS.useStateLayer() : { opacity: 0, handlers: {} };
  return (
    <CmpSurface
      onClick={onClick}
      color={selected ? "var(--primary-container)" : "var(--background-surface-card)"}
      contentColor={selected ? "var(--on-primary-container)" : "var(--on-surface)"}
      radius="var(--r-card-medium-radius)"
      border={!selected}
      style={{ boxSizing: "border-box", position: "relative", overflow: "hidden", cursor: "pointer" }}
    >
      <div {...handlers} role="button" tabIndex={0}
        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); onClick && onClick(); } }}
        style={{ display: "flex", alignItems: "center", gap: 14, padding: "10px 14px", minHeight: 64, boxSizing: "border-box" }}>
        <div style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity, pointerEvents: "none" }} />
        <DeviceTile icon={device.icon} size={tileSize} />
        <div style={{ flex: 1, minWidth: 0, position: "relative" }}>
          <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 8 }}>
            <CmpText variant="titleMedium" maxLines={1}>{device.name}</CmpText>
            {device.transport === "ble" && <MetaLabel text={`${device.rssi} dBm`} selected={selected} />}
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 4, flexWrap: "wrap" }}>
            <TransportChip transport={device.transport} short />
            {device.transport === "ble"
              ? <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional1-size)", color: selected ? "inherit" : "var(--on-surface-variant)" }}>{device.address}</span>
              : <MetaLabel text={device.systemId || device.address} selected={selected} />}
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", position: "relative", flex: "0 0 auto", marginRight: onInfo ? -6 : 0 }}>
          {onInfo && (
            <div onClick={(e) => { e.stopPropagation(); onInfo(); }} onKeyDown={(e) => e.stopPropagation()}>
              <CmpIconButton icon="MoreVert" contentDescription="Информация об устройстве" />
            </div>
          )}
          <CmpIcon icon="chevron_right" size={22} color={selected ? "inherit" : "var(--on-surface-variant)"} />
        </div>
      </div>
    </CmpSurface>
  );
}

// Круглый герой-значок для состояний.
function Hero({ icon, tone = "primary", radar = false }) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "neutral" ? "var(--surface-variant)" : "var(--primary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "neutral" ? "var(--on-surface-variant)" : "var(--on-primary-container)";
  return (
    <div style={{ position: "relative", width: 112, height: 112, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
      {radar && [0, 1].map((i) => (
        <span key={i} className="ds-ping" style={{
          position: "absolute", inset: 0, borderRadius: "50%", border: "2px solid var(--primary)",
          animation: "dsPing 1.8s ease-out infinite", animationDelay: `${i * 0.9}s`,
        }} />
      ))}
      <div style={{ width: 96, height: 96, borderRadius: "50%", background: bg, display: "grid", placeItems: "center" }}>
        <CmpIcon icon={icon} size={44} color={fg} />
      </div>
    </div>
  );
}

// Обёртка центрированных состояний (permission/denied/adapterOff/scanning/empty/connecting).
function CenterState({ children }) {
  return (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 22, padding: "32px 24px", textAlign: "center", boxSizing: "border-box" }}>
      <div style={{ maxWidth: 460, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 22 }}>{children}</div>
    </div>
  );
}

// Строка разрешения в экране запроса.
function PermissionRow({ icon, label, desc }) {
  return (
    <CmpSurface color="var(--background-surface-card)" radius={14} border style={{ width: "100%", boxSizing: "border-box" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 14, padding: 14, textAlign: "left" }}>
        <div style={{ width: 44, height: 44, borderRadius: "var(--r-card-small-radius)", background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}>
          <CmpIcon icon={icon} size={24} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CmpText variant="titleSmall">{label}</CmpText>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{desc}</CmpText>
        </div>
      </div>
    </CmpSurface>
  );
}

const PERMS = {
  android: [
    { icon: "bluetooth", label: "Bluetooth (BLE)", desc: "Поиск и связь с блоком сигнализации" },
    { icon: "usb",       label: "USB",             desc: "Подключение модулей по проводу" },
  ],
  ios: [
    { icon: "bluetooth", label: "Bluetooth", desc: "Поиск и связь с блоком сигнализации" },
  ],
  desktop: [],
};

// ── Главный экран ─────────────────────────────────────────────────────────────
function DeviceSelectScreen({ platform = "android", initialStep, demoEmpty = false, showBack = true, onBack = () => {}, onConnected }) {
  const { useState, useEffect, useRef } = React;
  const [rootRef, mode] = useLayout();
  const firstStep = initialStep || (platform === "desktop" ? "scanning" : "permission");
  const [step, setStep] = useState(firstStep);
  const [selectedId, setSelectedId] = useState(null);
  const [connectingId, setConnectingId] = useState(null);
  const [infoId, setInfoId] = useState(null);
  const scanTimer = useRef(null);

  // Сканирование: авто-переход в список (или «пусто») через задержку.
  useEffect(() => {
    if (step !== "scanning") return;
    clearTimeout(scanTimer.current);
    scanTimer.current = setTimeout(() => setStep(demoEmpty ? "empty" : "list"), 2200);
    return () => clearTimeout(scanTimer.current);
  }, [step, demoEmpty]);

  // Подключение (заглушка) → переход в «Расширенный монтаж» только после реального выбора устройства.
  useEffect(() => {
    if (step !== "connecting" || !connectingId || !onConnected) return;
    const t = setTimeout(() => onConnected(DEVICES.find((x) => x.id === connectingId)), 1800);
    return () => clearTimeout(t);
  }, [step, connectingId]);

  const selectDevice = (d) => {
    if (mode === "split") setSelectedId(d.id);
    else { setConnectingId(d.id); setStep("connecting"); }
  };
  const connectSelected = () => { setConnectingId(selectedId); setStep("connecting"); };

  const perms = PERMS[platform] || PERMS.android;
  const tileSize = mode === "compact" ? 44 : 48;

  // ── содержимое по шагу ──
  let body;

  if (step === "permission") {
    body = (
      <CenterState>
        <Hero icon="bluetooth_searching" />
        <div>
          <CmpText variant="headlineSmall" as="h1">Доступ к устройствам</CmpText>
          <div style={{ marginTop: 8 }}>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">
              {platform === "ios"
                ? "Чтобы найти блок сигнализации, приложению нужен доступ к Bluetooth."
                : "Чтобы найти блок сигнализации, приложению нужен доступ к Bluetooth и USB."}
            </CmpText>
          </div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%" }}>
          {perms.map((p) => <PermissionRow key={p.label} {...p} />)}
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", marginTop: 4 }}>
          <CmpButton text="Разрешить" fullWidth onClick={() => setStep("scanning")} />
          <CmpTextButtonSafe text="Не сейчас" onClick={() => setStep("denied")} />
        </div>
      </CenterState>
    );
  } else if (step === "denied") {
    body = (
      <CenterState>
        <Hero icon="lock" tone="error" />
        <div>
          <CmpText variant="headlineSmall" as="h1">Нет доступа</CmpText>
          <div style={{ marginTop: 8 }}>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">
              Без разрешения приложение не сможет найти устройства. Включите доступ в настройках.
            </CmpText>
          </div>
        </div>
        <FixSteps platform={platform} />
        <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", marginTop: 4 }}>
          <CmpButton text="Открыть настройки" icon="settings" fullWidth onClick={() => setStep("permission")} />
        </div>
      </CenterState>
    );
  } else if (step === "adapterOff") {
    body = (
      <CenterState>
        <Hero icon="bluetooth_disabled" tone="neutral" />
        <div>
          <CmpText variant="headlineSmall" as="h1">Bluetooth выключен</CmpText>
          <div style={{ marginTop: 8 }}>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">
              Включите Bluetooth на устройстве, чтобы найти блок сигнализации.
            </CmpText>
          </div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", marginTop: 4 }}>
          <CmpButton text="Включить Bluetooth" icon="bluetooth" fullWidth onClick={() => setStep("scanning")} />
        </div>
      </CenterState>
    );
  } else if (step === "scanning") {
    body = (
      <CenterState>
        <Hero icon="bluetooth_searching" radar />
        <div>
          <CmpText variant="headlineSmall" as="h1">Поиск устройств…</CmpText>
          <div style={{ marginTop: 8 }}>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">
              Держите блок сигнализации рядом. Bluetooth и USB-устройства появятся в списке автоматически.
            </CmpText>
          </div>
        </div>
        <CmpCircularProgressIndicator />
      </CenterState>
    );
  } else if (step === "empty") {
    body = (
      <CenterState>
        <Hero icon="search_off" tone="neutral" />
        <div>
          <CmpText variant="headlineSmall" as="h1">Устройства не найдены</CmpText>
          <div style={{ marginTop: 8 }}>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">
              Поиск продолжается. Проверьте, что устройство готово к подключению.
            </CmpText>
          </div>
        </div>
        <FixSteps empty platform={platform} />
        <div style={{ display: "inline-flex", alignItems: "center", gap: 10, marginTop: 4 }}>
          <CmpCircularProgressIndicator size={18} strokeWidth={2.5} />
          <CmpText variant="labelLarge" color="var(--on-surface-variant)">Идёт поиск…</CmpText>
        </div>
      </CenterState>
    );
  } else if (step === "connecting") {
    const d = DEVICES.find((x) => x.id === connectingId) || DEVICES[0];
    body = (
      <CenterState>
        <div style={{ position: "relative" }}>
          <Hero icon={d.icon} radar />
        </div>
        <div>
          <CmpText variant="headlineSmall" as="h1">Подключение…</CmpText>
          <div style={{ marginTop: 8, display: "flex", flexDirection: "column", alignItems: "center", gap: 2 }}>
            <CmpText variant="titleMedium">{d.name}</CmpText>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", color: "var(--on-surface-variant)" }}>{d.address}</span>
          </div>
        </div>
        <CmpCircularProgressIndicator />
        <CmpOutlinedButton text="Отмена" onClick={() => { setConnectingId(null); setStep("list"); }} />
      </CenterState>
    );
  } else {
    // step === "list"
    body = <ListBody mode={mode} platform={platform} tileSize={tileSize} selectedId={selectedId} onSelect={selectDevice} onConnect={connectSelected} onInfo={setInfoId} />;
  }

  const infoDev = DEVICES.find((x) => x.id === infoId);
  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)" }}>
      <CmpTopAppBar
        title="Выбор устройства"
        navigationIcon={showBack ? <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} /> : undefined}
      />
      {body}
      {infoDev && (
        <DS.CmpDialog open onDismissRequest={() => setInfoId(null)}
          title={infoDev.name}
          text={<><InfoRows rows={infoRows(infoDev, platform)} />{INFO_NOTE[infoDev.transport] && <span style={{ display: "block", marginTop: 8 }}>{INFO_NOTE[infoDev.transport]}</span>}</>}
          dismissButton={<CmpTextButtonSafe text="Закрыть" onClick={() => setInfoId(null)} />}
          confirmButton={<CmpTextButtonSafe text="Подключить" onClick={() => { const id = infoDev.id; setInfoId(null); selectDevice(DEVICES.find((x) => x.id === id)); }} />} />
      )}
    </div>
  );
}

// Подсказки «как починить» / «почему пусто».
function FixSteps({ platform = "android", empty = false }) {
  const items = empty
    ? ["Убедитесь, что на устройстве включено питание", "Поднесите блок ближе — в пределах 1–2 метров", "Проверьте, что Bluetooth включён"]
    : platform === "ios"
      ? ["Настройки → Pandora Spec → Bluetooth → включить"]
      : ["Настройки → Приложения → Pandora Spec → Разрешения", "Включите «Устройства поблизости» и «Bluetooth»"];
  return (
    <CmpSurface color="var(--background-surface-card)" radius={14} border style={{ width: "100%", boxSizing: "border-box" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: 16, textAlign: "left" }}>
        {items.map((t, i) => (
          <div key={i} style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
            <CmpIcon icon={empty ? "check_circle" : "chevron_right"} size={20} color="var(--primary)" style={{ marginTop: 1 }} />
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t}</CmpText>
          </div>
        ))}
      </div>
    </CmpSurface>
  );
}

// Список найденных устройств: compact/stacked — один столбец; split — master-detail.
function ListBody({ mode, platform, tileSize, selectedId, onSelect, onConnect, onInfo }) {
  const selected = DEVICES.find((d) => d.id === selectedId) || null;

  const listHeader = (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 }}>
      <CmpText variant="titleMedium">Найдено устройств: {DEVICES.length}</CmpText>
      <div style={{ display: "inline-flex", alignItems: "center", gap: 8 }}>
        <CmpCircularProgressIndicator size={16} strokeWidth={2.5} />
        <CmpText variant="labelLarge" color="var(--on-surface-variant)">поиск…</CmpText>
      </div>
    </div>
  );

  const rows = (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      {DEVICES.map((d, i) => (
        <div key={d.id} className="ds-row" style={{ animation: "dsRowIn .28s ease both", animationDelay: `${i * 45}ms` }}>
          <DeviceRow device={d} tileSize={tileSize} selected={mode === "split" && d.id === selectedId} onClick={() => onSelect(d)} onInfo={mode === "split" ? undefined : () => onInfo(d.id)} />
        </div>
      ))}
    </div>
  );

  if (mode === "split") {
    return (
      <div style={{ flex: 1, minHeight: 0, display: "flex", gap: 20, padding: 24, boxSizing: "border-box", maxWidth: 1100, width: "100%", margin: "0 auto" }}>
        <div style={{ flex: "1 1 0", minWidth: 320, minHeight: 0, display: "flex", flexDirection: "column", gap: 14 }}>
          {listHeader}
          <div style={{ flex: 1, minHeight: 0, overflow: "auto", paddingRight: 4 }}>{rows}</div>
        </div>
        <CmpDivider orientation="vertical" />
        <div style={{ flex: "1 1 0", maxWidth: 420, minWidth: 300, minHeight: 0, overflow: "auto" }}>
          <DetailPanel device={selected} platform={platform} onConnect={onConnect} />
        </div>
      </div>
    );
  }

  // compact / stacked — один центрированный столбец.
  const maxW = mode === "stacked" ? 620 : "100%";
  return (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: maxW, width: "100%", margin: "0 auto", padding: mode === "stacked" ? "20px 24px 28px" : "14px 14px 22px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 14 }}>
        {listHeader}
        {rows}
      </div>
    </div>
  );
}

// Правая колонка master-detail: карточка выбранного устройства + «Подключить».
function DetailPanel({ device, platform, onConnect }) {
  if (!device) {
    return (
      <div style={{ height: "100%", minHeight: 260, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 14, textAlign: "center", padding: 24 }}>
        <div style={{ width: 72, height: 72, borderRadius: "50%", background: "var(--surface-variant)", display: "grid", placeItems: "center" }}>
          <CmpIcon icon="touch_app" size={34} color="var(--on-surface-variant)" />
        </div>
        <CmpText variant="titleMedium" color="var(--on-surface-variant)">Выберите устройство из списка</CmpText>
      </div>
    );
  }
  const rows = infoRows(device, platform);
  return (
    <CmpSurface color="var(--background-surface-card)" radius="var(--r-card-medium-radius)" border style={{ boxSizing: "border-box" }}>
      <div style={{ padding: 24, display: "flex", flexDirection: "column", alignItems: "center", gap: 18 }}>
        <DeviceTile icon={device.icon} size={128} radius="var(--r-card-medium-radius)" showCaption />
        <div style={{ textAlign: "center" }}>
          <CmpText variant="headlineSmall">{device.name}</CmpText>
          <div style={{ marginTop: 6, display: "flex", justifyContent: "center" }}>
            <TransportChip transport={device.transport} />
          </div>
        </div>
        <InfoRows rows={rows} />
        {INFO_NOTE[device.transport] && <CmpText variant="bodySmall" color="var(--on-surface-variant)">{INFO_NOTE[device.transport]}</CmpText>}
        <CmpButton text="Подключить" icon="link" fullWidth onClick={onConnect} />
      </div>
    </CmpSurface>
  );
}

// CmpTextButton может отсутствовать в некоторых сборках — безопасная обёртка.
function CmpTextButtonSafe(props) {
  const Btn = DS.CmpTextButton;
  return Btn ? <Btn {...props} /> : <CmpOutlinedButton {...props} />;
}

Object.assign(window, { DeviceSelectScreen });
