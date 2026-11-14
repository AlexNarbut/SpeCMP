// Информация о товаре — поиск по ID устройства или цифрам штрих-кода, карточки «Товар» и «Комплект», сканер штрих-кода.
// Входы: главный (поле пустое), расширенный монтаж и ассистент (номер подставлен, результат грузится сразу).
// Адаптив по ширине контейнера (M3 WSC): compact <600 — одна колонка, компоненты списком; medium 600–839 — так же,
// с большими отступами (max-width 720); expanded ≥840 — «Товар» | «Комплект» в две колонки, компоненты таблицей (max-width 1040).
// Сканер: кнопка в поле (телефон, планшет); на Desktop камеры нет — кнопки нет.

const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpTextButton, CmpText, CmpIcon, CmpSnackbar } = window.PandoraSpecDesignSystem_b8d6e1;
const { useState, useEffect, useRef } = React;

(function () {
  if (document.getElementById("pi-css")) return;
  const s = document.createElement("style"); s.id = "pi-css";
  s.textContent = `
@keyframes piIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
@keyframes piFade{from{opacity:0}to{opacity:1}}
@keyframes piScan{0%{transform:translateY(-62px)}50%{transform:translateY(62px)}100%{transform:translateY(-62px)}}
@keyframes piSk{0%{opacity:.55}50%{opacity:1}100%{opacity:.55}}
.pi-in{animation:piIn .26s cubic-bezier(.2,0,0,1) both}
.pi-fade{animation:piFade .2s ease both}
.pi-scan{animation:piScan 2.4s ease-in-out infinite}
.pi-sk{animation:piSk 1.4s ease-in-out infinite}
.pi-hov:hover{background-color:color-mix(in srgb, var(--on-surface) 8%, transparent)!important}
.pi-hov:active{background-color:color-mix(in srgb, var(--on-surface) 12%, transparent)!important}
.pi-f:focus-visible{outline:2px solid var(--primary);outline-offset:2px}
.pi-sel{user-select:text;-webkit-user-select:text}
@media (prefers-reduced-motion:reduce){.pi-in,.pi-fade,.pi-scan,.pi-sk{animation:none!important}}`;
  document.head.appendChild(s);
})();

// ── Данные ──────────────────────────────────────────────────────────────────
const PI_CONNECTED = "107131";
const PI_BARCODE = "4607123456789";
const PI_NOTFOUND = "1234567";
const F = (m, v, d) => ({ m, v, d });
const C = (name, id, serial, fw = []) => ({ name, id, serial, fw });
const PI_PRODUCT = { name: "Pandora DX-4GS", id: "107131", serial: "4G24 1003 5871", date: "14.10.2024", fw: [F("Основной", "3.57", "14.10.2024"), F("CAN", "12.05", "14.10.2024"), F("Bluetooth", "1.14", "02.09.2024")] };
const PI_COMPS = [
  C("Брелок Pandora D-030", "3100217", "0302 4410 0217", [F("Основной", "2.11", "20.09.2024")]),
  C("Метка Pandora BT-760", "7600451", "0760 4409 0451", [F("Основной", "1.08", "11.09.2024")]),
  C("Метка Pandora BT-760", "7600452", "0760 4409 0452", [F("Основной", "1.08", "11.09.2024")]),
  C("Реле блокировки Pandora BTR-101", "1010388", "0101 4408 0388", [F("Основной", "1.10", "28.08.2024")]),
  C("Модуль GPS/ГЛОНАСС Pandora NAV-12", "1200917", "0012 4410 0917", [F("Основной", "1.02", "01.10.2024"), F("Навигация", "4.3", "01.10.2024")]),
  C("Сирена Pandora PS-331", null, "0331 4407 1182"),
  C("Датчик температуры двигателя", null, null),
  C("Модуль Bluetooth Pandora BTM2", "2200713", "0202 4409 0713", [F("Основной", "1.14", "02.09.2024")]),
  C("Датчик удара и наклона Pandora SS-04", "0400562", "0004 4408 0562", [F("Основной", "1.03", "15.08.2024")]),
  C("Микрофон Pandora MIC-2", null, "0002 4407 3310"),
  C("Модуль управления люком Pandora RHM-03", "0300119", "0003 4409 0119", [F("Основной", "1.01", "05.09.2024")]),
  C("Антенна GSM выносная", null, null),
];
const PI_LONG = {
  product: { ...PI_PRODUCT, name: "Pandora DX-4GS Pro LoRa — исполнение для коммерческого транспорта, расширенная гарантия 5 лет", serial: "PND-DX4GS-2024-10-000035871-RU" },
  kit: { name: "Комплект Pandora DX-4GS Pro LoRa для коммерческого транспорта с двумя метками и выносной антенной", serial: "KIT-DX4GS-PRO-2024-10-0118342", packed: "21.10.2024", comps: [
    C("Брелок-пейджер Pandora D-030 с OLED-дисплеем и зарядкой по USB-C, исполнение для холодного климата", "3100217", "PND-D030-2024-09-000000217", [F("Основной", "2.11", "20.09.2024"), F("Радиомодуль 868 МГц", "1.0.14-r2", "20.09.2024")]),
    C("Метка Pandora BT-760", "7600451", "PND-BT760-2024-09-000000451", [F("Основной", "1.08", "11.09.2024")]),
    C("Модуль GPS/ГЛОНАСС Pandora NAV-12 с выносной активной антенной", "1200917", "PND-NAV12-2024-10-000000917", [F("Основной", "1.02", "01.10.2024"), F("Навигация", "4.3", "01.10.2024")]),
  ] },
};
const PI_KIT = (n) => ({ name: n > 7 ? "Pandora DX-4GS · полный комплект" : n > 2 ? "Pandora DX-4GS · комплект с брелоком D-030" : "Pandora DX-4GS · базовый", serial: "K-2410-118342", packed: "21.10.2024", comps: PI_COMPS.slice(0, n) });
// Данные по номеру и сценарию витрины.
function piLookup(num, scenario = "kit7") {
  if (!num || num === PI_NOTFOUND) return null;
  const id = num.length <= 10 ? num : PI_PRODUCT.id;
  if (scenario === "long") return { product: { ...PI_LONG.product, id }, kit: PI_LONG.kit };
  if (scenario === "sparse") return { product: { name: "Pandora DX-4GS", id }, kit: { name: "Pandora DX-4GS · базовый", comps: [C("Брелок Pandora D-030", null, null), C("Сирена Pandora PS-331", null, null)] } };
  const n = { nokit: 0, kit2: 2, kit7: 7, kit12: 12 }[scenario] ?? 7;
  return { product: { ...PI_PRODUCT, id }, kit: n ? PI_KIT(n) : null };
}
const piClean = (v) => v.replace(/[\s\-–—]/g, "");
const piValidate = (v) => !v ? "Введите номер" : /\D/.test(v) ? "Только цифры" : v.length > 19 ? "Слишком длинный номер" : null;
const piFw = (n) => n === 1 ? "прошивка" : "прошивки";
const piComps = (n) => { const a = n % 10, b = n % 100; return `${n} ${a === 1 && b !== 11 ? "компонент" : a >= 2 && a <= 4 && (b < 12 || b > 14) ? "компонента" : "компонентов"}`; };

function usePiWidth() {
  const ref = useRef(null); const [w, setW] = useState(0);
  useEffect(() => { const el = ref.current; if (!el) return; const m = () => setW(el.offsetWidth); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, []);
  return [ref, w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact"];
}

// ── Примитивы ───────────────────────────────────────────────────────────────
const MONO = { fontFamily: "var(--font-mono)", overflowWrap: "anywhere" };
function IconBtn({ icon, label, onClick, color = "var(--on-surface-variant)", size = 24 }) {
  return (
    <button type="button" className="pi-hov pi-f" aria-label={label} title={label} onClick={onClick}
      style={{ width: 48, height: 48, flex: "0 0 auto", border: "none", borderRadius: 999, background: "transparent", color, display: "grid", placeItems: "center", cursor: "pointer" }}>
      <CmpIcon icon={icon} size={size} color="currentColor" />
    </button>
  );
}
function Card({ children, pad = 16, gap = 12, style }) {
  return <div className="pi-in" style={{ boxSizing: "border-box", borderRadius: 20, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", padding: pad, display: "flex", flexDirection: "column", gap, minWidth: 0, ...style }}>{children}</div>;
}
function Kicker({ icon, text, right }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
      <span style={{ width: 40, height: 40, borderRadius: 12, background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={icon} size={24} color="currentColor" /></span>
      <span style={{ flex: 1 }}><CmpText variant="titleMedium" color="var(--primary)">{text}</CmpText></span>
      {right}
    </div>
  );
}
function Info({ label, value, mono, onCopy, top }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 56, borderTop: top ? "1px solid var(--outline-variant)" : "none", padding: "4px 0" }}>
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{label}</CmpText>
        <span className="pi-sel" style={{ ...(mono ? MONO : null), fontSize: 17, lineHeight: "24px", fontWeight: 500, color: "var(--on-surface)", overflowWrap: "anywhere" }}>{value}</span>
      </div>
      {onCopy && <span style={{ marginRight: -8 }}><IconBtn icon="content_copy" label={`Скопировать ${label.toLowerCase()}`} onClick={() => onCopy(value)} /></span>}
    </div>
  );
}
function FwRows({ fw, dense }) {
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {fw.map((f, i) => (
        <div key={f.m + i} style={{ display: "flex", alignItems: "center", flexWrap: "wrap", columnGap: 12, rowGap: 2, minHeight: dense ? 40 : 48, borderTop: i ? "1px solid var(--outline-variant)" : "none", padding: "4px 0" }}>
          <span style={{ flex: "1 1 120px", minWidth: 0 }}><CmpText variant="bodyLarge">{f.m}</CmpText></span>
          <span className="pi-sel" style={{ display: "inline-flex", alignItems: "baseline", gap: 10 }}>
            <span style={{ ...MONO, fontSize: 17, fontWeight: 600, color: "var(--primary)" }}>{f.v}</span>
            {f.d && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{f.d}</CmpText>}
          </span>
        </div>
      ))}
    </div>
  );
}
function Badge({ icon, children }) {
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, minHeight: 32, padding: "4px 12px", boxSizing: "border-box", borderRadius: 8, background: "var(--secondary-container)", color: "var(--on-secondary-container)", fontFamily: "var(--font-sans)", fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500 }}><CmpIcon icon={icon} size={18} color="currentColor" />{children}</span>;
}

// ── Карточки результата ─────────────────────────────────────────────────────
function ProductCard({ p, connected, big, solo, onCopy }) {
  const rows = [p.id && ["ID", p.id, true, true], p.serial && ["Серийный номер", p.serial, true, true], p.date && ["Заводская дата", p.date, false, false]].filter(Boolean);
  const info = <div>{rows.map(([l, v, mono, copy], i) => <Info key={l} label={l} value={v} mono={mono} onCopy={copy ? onCopy : null} top={i > 0} />)}</div>;
  const fw = p.fw && p.fw.length ? (
    <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
      <CmpText variant="titleSmall" color="var(--on-surface-variant)">Прошивки</CmpText>
      <FwRows fw={p.fw} />
    </div>
  ) : null;
  return (
    <Card pad={big ? 24 : 16}>
      <Kicker icon="sell" text="Товар" right={connected ? <Badge icon="link">Подключённый блок</Badge> : null} />
      <span className="pi-sel" style={{ overflowWrap: "anywhere" }}><CmpText variant={big ? "headlineSmall" : "titleLarge"}>{p.name}</CmpText></span>
      {solo && fw
        ? <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) minmax(0,1fr)", gap: 32, alignItems: "start" }}>{info}{fw}</div>
        : <>{info}{fw}</>}
    </Card>
  );
}
function KitCard({ k, big, onCopy }) {
  const rows = [k.serial && ["Серийный номер", k.serial, true], k.packed && ["Дата упаковки", k.packed, false]].filter(Boolean);
  return (
    <Card pad={big ? 24 : 16}>
      <Kicker icon="inventory_2" text="Комплект" />
      <span className="pi-sel" style={{ overflowWrap: "anywhere" }}><CmpText variant={big ? "headlineSmall" : "titleLarge"}>{k.name}</CmpText></span>
      <div>
        {rows.map(([l, v, mono], i) => <Info key={l} label={l} value={v} mono={mono} onCopy={mono ? onCopy : null} top={i > 0} />)}
        <Info label="В комплекте" value={piComps(k.comps.length)} top={rows.length > 0} />
      </div>
    </Card>
  );
}
function Num({ i }) {
  return <span style={{ width: 32, height: 32, borderRadius: 999, flex: "0 0 auto", background: "var(--surface-container-highest)", color: "var(--on-surface-variant)", display: "grid", placeItems: "center", fontFamily: "var(--font-sans)", fontSize: 14, fontWeight: 600 }}>{i + 1}</span>;
}
function CompDetails({ c, onCopy }) {
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {c.serial && <Info label="Серийный номер" value={c.serial} mono onCopy={onCopy} />}
      {c.id && <Info label="ID" value={c.id} mono onCopy={onCopy} top={!!c.serial} />}
      {c.fw.length > 0
        ? <div style={{ borderTop: c.serial || c.id ? "1px solid var(--outline-variant)" : "none", paddingTop: 8 }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{c.fw.length > 1 ? "Прошивки" : "Прошивка"}</CmpText><FwRows fw={c.fw} dense /></div>
        : <div style={{ minHeight: 48, display: "flex", alignItems: "center", borderTop: c.serial || c.id ? "1px solid var(--outline-variant)" : "none" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Без прошивки</CmpText></div>}
    </div>
  );
}
// Телефон и планшет-портрет — строки с раскрытием: серийник виден сразу, прошивки и копирование — по нажатию.
function CompRows({ comps, open, onToggle, onCopy }) {
  return (
    <Card pad={0} gap={0} style={{ overflow: "hidden" }}>
      {comps.map((c, i) => {
        const o = open.has(i);
        const sum = c.fw.length === 1 ? `Прошивка ${c.fw[0].v}` : c.fw.length ? `${c.fw.length} ${piFw(c.fw.length)}` : null;
        return (
          <div key={i} style={{ borderTop: i ? "1px solid var(--outline-variant)" : "none" }}>
            <button type="button" className="pi-hov pi-f" aria-expanded={o} onClick={() => onToggle(i)}
              style={{ width: "100%", display: "flex", alignItems: "center", gap: 12, minHeight: 72, padding: "12px 8px 12px 16px", border: "none", background: o ? "var(--surface-container)" : "transparent", textAlign: "left", cursor: "pointer", color: "var(--on-surface)", fontFamily: "var(--font-sans)", boxSizing: "border-box" }}>
              <Num i={i} />
              <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
                <CmpText variant="titleMedium">{c.name}</CmpText>
                <span style={{ display: "flex", flexWrap: "wrap", columnGap: 12 }}>
                  <span style={{ ...MONO, fontSize: 15, lineHeight: "22px", color: c.serial ? "var(--on-surface)" : "var(--on-surface-variant)" }}>{c.serial ? `S/N ${c.serial}` : "Без серийного номера"}</span>
                  {sum && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{sum}</CmpText>}
                </span>
              </span>
              <span style={{ width: 48, height: 48, display: "grid", placeItems: "center", flex: "0 0 auto", transform: o ? "rotate(180deg)" : "none", transition: "transform var(--motion-fast)", color: "var(--on-surface-variant)" }}><CmpIcon icon="expand_more" size={28} color="currentColor" /></span>
            </button>
            {o && <div className="pi-fade" style={{ padding: "0 16px 8px 60px", background: "var(--surface-container)" }}><CompDetails c={c} onCopy={onCopy} /></div>}
          </div>
        );
      })}
    </Card>
  );
}
// Широкое окно — таблица «Название | Серийный номер | Прошивки».
function CompTable({ comps, onCopy }) {
  const cols = "minmax(0,1.3fr) minmax(0,1fr) minmax(0,1.15fr)";
  return (
    <Card pad={0} gap={0} style={{ overflow: "hidden" }}>
      <div style={{ display: "grid", gridTemplateColumns: cols, gap: 16, alignItems: "center", minHeight: 52, padding: "0 16px 0 24px", background: "var(--surface-container)" }}>
        {["Название", "Серийный номер", "Прошивки"].map((h) => <CmpText key={h} variant="labelLarge" color="var(--on-surface-variant)">{h}</CmpText>)}
      </div>
      {comps.map((c, i) => (
        <div key={i} className="pi-hov" style={{ display: "grid", gridTemplateColumns: cols, gap: 16, alignItems: "center", minHeight: 72, padding: "10px 16px 10px 24px", borderTop: "1px solid var(--outline-variant)", boxSizing: "border-box" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, minWidth: 0 }}>
            <Num i={i} />
            <div style={{ minWidth: 0, display: "flex", flexDirection: "column" }}>
              <span className="pi-sel"><CmpText variant="titleMedium">{c.name}</CmpText></span>
              {c.id && <span className="pi-sel" style={{ ...MONO, fontSize: 14, lineHeight: "20px", color: "var(--on-surface-variant)" }}>ID {c.id}</span>}
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 4, minWidth: 0 }}>
            {c.serial
              ? <><span className="pi-sel" style={{ ...MONO, flex: 1, minWidth: 0, fontSize: 16, lineHeight: "24px", fontWeight: 500 }}>{c.serial}</span><IconBtn icon="content_copy" label="Скопировать серийный номер" onClick={() => onCopy(c.serial)} /></>
              : <CmpText variant="bodyLarge" color="var(--on-surface-variant)">—</CmpText>}
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 2, minWidth: 0 }}>
            {c.fw.length ? c.fw.map((f, j) => (
              <span key={j} className="pi-sel" style={{ display: "flex", flexWrap: "wrap", columnGap: 8, alignItems: "baseline" }}>
                <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{f.m}</CmpText>
                <span style={{ ...MONO, fontSize: 16, fontWeight: 600, color: "var(--primary)" }}>{f.v}</span>
                <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{f.d}</CmpText>
              </span>
            )) : <CmpText variant="bodyLarge" color="var(--on-surface-variant)">—</CmpText>}
          </div>
        </div>
      ))}
    </Card>
  );
}
function SkCard({ big, lines = 4 }) {
  const bar = (w, h = 20) => <div className="pi-sk" style={{ width: w, height: h, borderRadius: 8, background: "var(--surface-variant)" }} />;
  return (
    <Card pad={big ? 24 : 16} gap={16}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}><div className="pi-sk" style={{ width: 40, height: 40, borderRadius: 12, background: "var(--surface-variant)" }} />{bar(96)}</div>
      {bar("72%", 28)}
      {Array.from({ length: lines }).map((_, i) => <div key={i} style={{ display: "flex", flexDirection: "column", gap: 6 }}>{bar(110, 14)}{bar(`${60 - i * 8}%`)}</div>)}
    </Card>
  );
}
function StateView({ icon, tone = "neutral", title, text, action }) {
  return (
    <div className="pi-in" style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: 12, padding: "40px 16px", maxWidth: 480, margin: "0 auto" }}>
      <span style={{ width: 96, height: 96, borderRadius: 999, display: "grid", placeItems: "center", background: tone === "primary" ? "var(--primary-container)" : "var(--surface-container-high)", color: tone === "primary" ? "var(--on-primary-container)" : "var(--on-surface-variant)", marginBottom: 4 }}><CmpIcon icon={icon} size={48} color="currentColor" /></span>
      <span style={{ textWrap: "balance" }}><CmpText variant="titleLarge">{title}</CmpText></span>
      {text && <span style={{ textWrap: "pretty" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{text}</CmpText></span>}
      {action && <div style={{ marginTop: 8 }}>{action}</div>}
    </div>
  );
}
// Начальное состояние: где взять номер.
function EmptyHint({ onScan, wide }) {
  const tip = (icon, t, d) => (
    <div style={{ flex: "1 1 220px", display: "flex", gap: 12, alignItems: "flex-start", padding: 16, borderRadius: 16, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", textAlign: "left", boxSizing: "border-box" }}>
      <span style={{ width: 40, height: 40, borderRadius: 12, background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={icon} size={24} color="currentColor" /></span>
      <div style={{ display: "flex", flexDirection: "column", gap: 2, minWidth: 0 }}><CmpText variant="titleMedium">{t}</CmpText><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{d}</CmpText></div>
    </div>
  );
  return (
    <div className="pi-in" style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16, padding: wide ? "40px 0" : "28px 0", textAlign: "center" }}>
      <span style={{ width: 112, height: 112, borderRadius: 999, display: "grid", placeItems: "center", background: "var(--primary-container)", color: "var(--on-primary-container)" }}><CmpIcon icon="manage_search" size={56} color="currentColor" /></span>
      <div style={{ display: "flex", flexDirection: "column", gap: 6, maxWidth: 440 }}>
        <CmpText variant="headlineSmall">Найдите товар по номеру</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Покажем, что это за товар, какие прошивки стояли с завода и что должно быть в комплекте.</CmpText>
      </div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 12, width: "100%", maxWidth: 640 }}>
        {tip("memory", "ID устройства", "На наклейке блока и в приложении при подключении")}
        {tip("inventory_2", "Штрих-код", "На коробке — цифры под штрихами")}
      </div>
      {onScan && <CmpOutlinedButton text="Сканировать штрих-код" icon="qr_code_scanner" onClick={onScan} style={{ height: 56, padding: "0 24px", marginTop: 4 }} />}
    </div>
  );
}

// ── Поле поиска ─────────────────────────────────────────────────────────────
function SearchBar({ value, setValue, err, onSearch, onScan, inputRef, compact }) {
  const [f, setF] = useState(false);
  const bc = err ? "var(--error)" : f ? "var(--primary)" : "var(--outline)";
  const thick = f || err;
  return (
    <form onSubmit={(e) => { e.preventDefault(); onSearch(); }} style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <label htmlFor="pi-q"><CmpText variant="labelLarge" color={err ? "var(--error)" : "var(--on-surface-variant)"}>ID устройства или цифры штрих-кода</CmpText></label>
      <div style={{ display: "flex", gap: 8, alignItems: "stretch" }}>
        <div style={{ flex: 1, minWidth: 0, display: "flex", alignItems: "center", height: 56, borderRadius: 12, border: `${thick ? 2 : 1}px solid ${bc}`, padding: thick ? "0 3px 0 15px" : "0 4px 0 16px", background: "var(--surface-container-lowest)", boxSizing: "border-box" }}>
          <input id="pi-q" ref={inputRef} value={value} onChange={(e) => setValue(e.target.value)} onFocus={() => setF(true)} onBlur={() => setF(false)}
            inputMode="numeric" enterKeyHint="search" autoComplete="off" placeholder="Например, 107131" aria-invalid={!!err}
            style={{ flex: 1, minWidth: 0, height: "100%", border: "none", outline: "none", background: "transparent", color: "var(--on-surface)", fontFamily: "var(--font-mono)", fontSize: compact ? 18 : 20 }} />
          {value && <IconBtn icon="close" label="Очистить" onClick={() => { setValue(""); inputRef.current && inputRef.current.focus(); }} />}
          {onScan && <><span style={{ width: 1, height: 28, background: "var(--outline-variant)", margin: "0 2px" }} /><IconBtn icon="qr_code_scanner" label="Сканировать штрих-код" onClick={onScan} color="var(--primary)" size={26} /></>}
        </div>
        <CmpButton text="Найти" icon={compact ? undefined : "search"} onClick={onSearch} style={{ height: 56, padding: compact ? "0 18px" : "0 24px", whiteSpace: "nowrap", flex: "0 0 auto" }} />
      </div>
      {err && <div role="alert" style={{ display: "flex", alignItems: "center", gap: 6, minHeight: 24, color: "var(--error)" }}><CmpIcon icon="error" size={20} color="currentColor" /><CmpText variant="bodyMedium" color="var(--error)">{err}</CmpText></div>}
    </form>
  );
}

// ── Сканер штрих-кода ───────────────────────────────────────────────────────
// Поток камеры всегда тёмный и не зависит от темы приложения; оверлей рендерится в тёмной теме.
const CAM_BG = "#0d0f10";
const CAM_DIM = "rgba(0,0,0,.58)";
const WHITE = "#fff";
function CamBtn({ icon, label, onClick, on }) {
  return (
    <button type="button" className="pi-f" aria-label={label} title={label} aria-pressed={on} onClick={onClick}
      style={{ width: 56, height: 56, borderRadius: 999, border: "none", flex: "0 0 auto", display: "grid", placeItems: "center", cursor: "pointer", background: on ? "var(--primary)" : "rgba(255,255,255,.16)", color: on ? "var(--on-primary)" : WHITE }}>
      <CmpIcon icon={icon} size={28} color="currentColor" />
    </button>
  );
}
function Scanner({ mode, code, wide, onClose, onManual, onTap }) {
  const [torch, setTorch] = useState(false);
  // Камера не работает: denied — отказали один раз (системный запрос можно повторить), blocked — отказали навсегда,
  // unavailable — камеры нет, занята другим приложением или не запускается.
  const NOCAM = {
    denied: { icon: "no_photography", t: "Нет доступа к камере", x: "Чтобы считать штрих-код, разрешите приложению Pandora Spec использовать камеру. Или введите цифры вручную.", btn: ["Разрешить доступ", "photo_camera"] },
    blocked: { icon: "no_photography", t: "Нет доступа к камере", x: "Доступ к камере запрещён. Разрешите его в настройках телефона или введите цифры вручную.", btn: ["Открыть настройки", "settings"] },
    unavailable: { icon: "videocam_off", t: "Камера недоступна", x: "Закройте другие приложения, которые её используют, или введите цифры вручную." },
  };
  const found = mode === "found", nc = NOCAM[mode], denied = !!nc;
  const fw = wide ? 460 : 320, fh = wide ? 220 : 170;
  const corner = (pos) => {
    const c = found ? "var(--success)" : WHITE, b = `5px solid ${c}`;
    const s = { position: "absolute", width: 40, height: 40, boxSizing: "border-box", transition: "border-color var(--motion-fast)" };
    if (pos === "tl") Object.assign(s, { left: -2, top: -2, borderLeft: b, borderTop: b, borderTopLeftRadius: 18 });
    if (pos === "tr") Object.assign(s, { right: -2, top: -2, borderRight: b, borderTop: b, borderTopRightRadius: 18 });
    if (pos === "bl") Object.assign(s, { left: -2, bottom: -2, borderLeft: b, borderBottom: b, borderBottomLeftRadius: 18 });
    if (pos === "br") Object.assign(s, { right: -2, bottom: -2, borderRight: b, borderBottom: b, borderBottomRightRadius: 18 });
    return <span key={pos} style={s} />;
  };
  const fmtCode = (c) => c.length === 13 ? `${c[0]} ${c.slice(1, 7)} ${c.slice(7)}` : c;
  return (
    <div data-theme="dark" className="pi-fade" role="dialog" aria-label="Сканер штрих-кода" style={{ position: "absolute", inset: 0, zIndex: 30, background: CAM_BG, color: WHITE, display: "flex", flexDirection: "column", overflow: "hidden", fontFamily: "var(--font-sans)" }}>
      {!denied && <div aria-hidden="true" style={{ position: "absolute", inset: 0, background: "repeating-linear-gradient(135deg, rgba(255,255,255,.04) 0 14px, transparent 14px 28px)" }} />}
      <div style={{ position: "relative", zIndex: 2, display: "flex", alignItems: "center", gap: 12, padding: "12px 12px 12px 12px" }}>
        <CamBtn icon="close" label="Закрыть сканер" onClick={onClose} />
        <span style={{ flex: 1, minWidth: 0 }}><CmpText variant="titleLarge" color={WHITE}>Сканер штрих-кода</CmpText></span>
        {!denied && <CamBtn icon={torch ? "flash_on" : "flash_off"} label={torch ? "Выключить фонарик" : "Включить фонарик"} on={torch} onClick={() => setTorch(!torch)} />}
      </div>
      <div style={{ position: "relative", flex: 1, minHeight: 0, display: "grid", placeItems: "center", padding: 16 }}>
        {denied ? (
          <div className="pi-in" style={{ position: "relative", zIndex: 2, maxWidth: 400, display: "flex", flexDirection: "column", alignItems: "center", gap: 12, textAlign: "center" }}>
            <span style={{ width: 96, height: 96, borderRadius: 999, background: "rgba(255,255,255,.12)", display: "grid", placeItems: "center" }}><CmpIcon icon={nc.icon} size={48} color={WHITE} /></span>
            <CmpText variant="headlineSmall" color={WHITE}>{nc.t}</CmpText>
            <CmpText variant="bodyLarge" color="rgba(255,255,255,.8)">{nc.x}</CmpText>
            {nc.btn && <CmpButton text={nc.btn[0]} icon={nc.btn[1]} style={{ height: 56, padding: "0 24px", marginTop: 8 }} />}
          </div>
        ) : (
          <div style={{ position: "relative", display: "flex", flexDirection: "column", alignItems: "center", gap: 20 }}>
            <span aria-hidden="true" style={{ position: "absolute", top: -44, fontFamily: "var(--font-mono)", fontSize: 13, color: "rgba(255,255,255,.5)", whiteSpace: "nowrap" }}>видео с камеры</span>
            <div onClick={onTap} style={{ position: "relative", width: fw, height: fh, borderRadius: 18, boxShadow: `0 0 0 200vmax ${CAM_DIM}`, cursor: found ? "default" : "pointer", display: "grid", placeItems: "center" }}>
              {["tl", "tr", "bl", "br"].map(corner)}
              {found
                ? <div className="pi-fade" style={{ width: "74%", height: "66%", borderRadius: 8, background: WHITE, padding: "10px 14px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 6, outline: "3px solid var(--success)", outlineOffset: 4 }}>
                    <div style={{ flex: 1, background: "repeating-linear-gradient(90deg, #111 0 3px, transparent 3px 5px, #111 5px 6px, transparent 6px 9px, #111 9px 13px, transparent 13px 15px, #111 15px 16px, transparent 16px 19px)" }} />
                    <div style={{ fontFamily: "var(--font-mono)", fontSize: 13, color: "#111", textAlign: "center", letterSpacing: ".18em" }}>{fmtCode(code)}</div>
                  </div>
                : <span className="pi-scan" style={{ position: "absolute", left: 18, right: 18, top: "50%", height: 3, marginTop: -1.5, borderRadius: 2, background: "var(--error)", boxShadow: "0 0 12px 2px color-mix(in srgb, var(--error) 70%, transparent)" }} />}
            </div>
            <div style={{ position: "relative", zIndex: 2, minHeight: 56, display: "grid", placeItems: "center" }}>
              {found
                ? <span className="pi-in" style={{ display: "inline-flex", alignItems: "center", gap: 10, minHeight: 56, padding: "8px 20px", borderRadius: 16, background: "var(--surface-container-high)", color: "var(--on-surface)" }}>
                    <CmpIcon icon="check_circle" size={28} color="var(--success)" />
                    <span style={{ display: "flex", flexDirection: "column", textAlign: "left" }}>
                      <span style={{ fontFamily: "var(--font-mono)", fontSize: 22, lineHeight: "28px", fontWeight: 600 }}>{fmtCode(code)}</span>
                      <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Распознано · ищем товар</CmpText>
                    </span>
                  </span>
                : <CmpText variant="titleMedium" color={WHITE}>Совместите штрих-код с рамкой</CmpText>}
            </div>
          </div>
        )}
      </div>
      <div style={{ position: "relative", zIndex: 2, padding: "12px 24px 24px", display: "flex", flexDirection: "column", alignItems: "center", gap: 12 }}>
        {mode === "hint" && (
          <div className="pi-in" style={{ width: "100%", maxWidth: 480, display: "flex", gap: 12, padding: 16, borderRadius: 16, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", boxSizing: "border-box" }}>
            <CmpIcon icon="lightbulb" size={24} color="currentColor" />
            <CmpText variant="bodyLarge" color="currentColor">Не распознаётся? Поднесите телефон ближе, включите фонарик или введите цифры вручную.</CmpText>
          </div>
        )}
        {mode === "aim" && <span style={{ maxWidth: 480, textAlign: "center" }}><CmpText variant="bodyLarge" color="rgba(255,255,255,.8)">Штрих-код — на коробке. Распознаётся сам, нажимать ничего не нужно.</CmpText></span>}
        {!found && <CmpOutlinedButton text="Ввести вручную" icon="keyboard" onClick={onManual} style={{ height: 56, width: "100%", maxWidth: 480 }} />}
      </div>
    </div>
  );
}

// ── Экран ───────────────────────────────────────────────────────────────────
// entry: home | mount | assistant. state0 — статичное состояние для витрины (без таймеров):
// empty | loading | content | notfound | offline | server | input. scan0: aim | found | hint | denied.
function ProductInfoApp({ entry = "home", number, state0, scenario = "kit7", scanner = true, scan0 = null, inputErr0 = null, open0 = [], platform = "mobile", onBack }) {
  const [rootRef, layout] = usePiWidth();
  const wide = layout === "expanded", compact = layout === "compact";
  const pre = number !== undefined ? number : entry !== "home" ? PI_CONNECTED : "";
  const ERRV = { empty: "", letters: "10713I", long: "107131071310713107131071" };
  const [value, setValue] = useState(inputErr0 ? ERRV[inputErr0] : state0 === "notfound" ? PI_NOTFOUND : state0 && state0 !== "empty" && state0 !== "input" ? pre || PI_BARCODE : pre);
  const [err, setErr] = useState(inputErr0 ? piValidate(piClean(ERRV[inputErr0])) : null);
  const [st, setSt] = useState(() => {
    if (inputErr0) return { s: "empty" };
    if (state0) return { s: state0 === "input" ? "empty" : state0, q: state0 === "notfound" ? PI_NOTFOUND : pre || PI_BARCODE };
    return entry !== "home" && pre ? { s: "loading", q: pre, auto: true } : { s: "empty" };
  });
  const [open, setOpen] = useState(() => new Set(open0));
  const [snack, setSnack] = useState(null);
  const [scan, setScan] = useState(scan0 ? { mode: scan0, code: PI_BARCODE, live: false } : null);
  const inputRef = useRef(null), timer = useRef(null), sTimer = useRef(null), scTimer = useRef(null);
  const canScan = scanner && platform !== "desktop";

  useEffect(() => { if (st.auto) run(st.q); return () => { clearTimeout(timer.current); clearTimeout(sTimer.current); clearTimeout(scTimer.current); }; }, []);

  function run(q) {
    setSt({ s: "loading", q }); setOpen(new Set());
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setSt({ s: q === PI_NOTFOUND ? "notfound" : "content", q }), 900);
  }
  const search = () => {
    const v = piClean(value), e = piValidate(v);
    setValue(v); setErr(e);
    if (!e) { inputRef.current && inputRef.current.blur(); run(v); }
  };
  const onType = (v) => { setValue(v); if (err) setErr(null); };
  const copy = (v) => {
    try { navigator.clipboard && navigator.clipboard.writeText(v).catch(() => {}); } catch (e) {}
    setSnack(`Скопировано: ${v}`); clearTimeout(sTimer.current); sTimer.current = setTimeout(() => setSnack(null), 2600);
  };
  const openScan = () => {
    setScan({ mode: "aim", code: PI_BARCODE, live: true });
    clearTimeout(scTimer.current); scTimer.current = setTimeout(() => found(), 2800);
  };
  function found() {
    clearTimeout(scTimer.current);
    setScan((s) => s && { ...s, mode: "found" });
    scTimer.current = setTimeout(() => { setScan(null); setValue(PI_BARCODE); setErr(null); run(PI_BARCODE); }, 1300);
  }
  const closeScan = () => { clearTimeout(scTimer.current); setScan(null); };
  const manual = () => { closeScan(); setTimeout(() => inputRef.current && inputRef.current.focus(), 50); };
  const toggle = (i) => setOpen((o) => { const n = new Set(o); n.has(i) ? n.delete(i) : n.add(i); return n; });

  const data = st.s === "content" ? piLookup(st.q, scenario) : null;
  // «Подключённый блок» — по ID найденного товара, а не по тексту поля: пока на экране результат блока, пометка есть;
  // нашли другой номер — пропала; снова нашли блок (по ID или штрих-коду его коробки) — вернулась.
  const connected = entry !== "home" && !!data && data.product.id === PI_CONNECTED;
  const pad = compact ? 16 : 24;
  const maxW = wide ? 1040 : compact ? "none" : 720;

  let body = null;
  if (st.s === "empty") body = <EmptyHint onScan={canScan ? openScan : null} wide={!compact} />;
  else if (st.s === "loading") body = wide
    ? <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1.1fr) minmax(0,1fr)", gap: 16, alignItems: "start" }}><SkCard big lines={4} /><SkCard big lines={2} /></div>
    : <div style={{ display: "flex", flexDirection: "column", gap: 12 }}><SkCard lines={4} /><SkCard lines={2} /></div>;
  else if (st.s === "notfound") body = <StateView icon="search_off" title={`Товар с номером ${st.q} не найден`} text="Проверьте номер и попробуйте ещё раз." action={canScan ? <CmpOutlinedButton text="Сканировать штрих-код" icon="qr_code_scanner" onClick={openScan} style={{ height: 56, padding: "0 24px" }} /> : null} />;
  else if (st.s === "offline") body = <StateView icon="wifi_off" title="Нет подключения к интернету" text="Информация о товаре загружается с сервера. Подключитесь к сети и повторите." action={<CmpButton text="Повторить" icon="refresh" onClick={() => run(st.q)} style={{ height: 56, padding: "0 28px" }} />} />;
  else if (st.s === "server") body = <StateView icon="cloud_off" title="Не удалось загрузить данные" text="Сервер не ответил. Повторите через минуту." action={<CmpButton text="Повторить" icon="refresh" onClick={() => run(st.q)} style={{ height: 56, padding: "0 28px" }} />} />;
  else if (data) {
    const k = data.kit;
    const allOpen = k && open.size === k.comps.length;
    const compsHead = k && (
      <div style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 48, marginTop: 8 }}>
        <span style={{ flex: 1 }}><CmpText variant="titleLarge">{`Компоненты · ${k.comps.length}`}</CmpText></span>
        {!wide && k.comps.length > 1 && <CmpTextButton text={allOpen ? "Свернуть все" : "Раскрыть все"} icon={allOpen ? "unfold_less" : "unfold_more"} onClick={() => setOpen(allOpen ? new Set() : new Set(k.comps.map((_, i) => i)))} style={{ height: 48 }} />}
      </div>
    );
    const list = k && (wide ? <CompTable comps={k.comps} onCopy={copy} /> : <CompRows comps={k.comps} open={open} onToggle={toggle} onCopy={copy} />);
    body = wide ? (
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        {k ? <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1.1fr) minmax(0,1fr)", gap: 16, alignItems: "start" }}><ProductCard p={data.product} connected={connected} big onCopy={copy} /><KitCard k={k} big onCopy={copy} /></div>
          : <ProductCard p={data.product} connected={connected} big solo onCopy={copy} />}
        {compsHead}{list}
      </div>
    ) : (
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <ProductCard p={data.product} connected={connected} big={!compact} onCopy={copy} />
        {k && <KitCard k={k} big={!compact} onCopy={copy} />}
        {compsHead}{list}
      </div>
    );
  }

  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden", fontFamily: "var(--font-sans)" }}>
      <CmpTopAppBar title="Информация о товаре" navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} />
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ position: "sticky", top: 0, zIndex: 3, background: "var(--background)", padding: `4px ${pad}px 12px`, borderBottom: st.s === "content" ? "1px solid var(--outline-variant)" : "1px solid transparent" }}>
          <div style={{ maxWidth: maxW, margin: "0 auto" }}><SearchBar value={value} setValue={onType} err={err} onSearch={search} onScan={canScan ? openScan : null} inputRef={inputRef} compact={compact} /></div>
        </div>
        <div key={st.s + st.q} style={{ maxWidth: maxW, margin: "0 auto", padding: `16px ${pad}px 96px`, boxSizing: "content-box" }}>{body}</div>
      </div>
      {snack && <div className="pi-in" style={{ position: "absolute", left: 16, right: 16, bottom: 16, zIndex: 20, display: "flex", justifyContent: "center", pointerEvents: "none" }}><div style={{ pointerEvents: "auto", width: "100%", maxWidth: 480 }}><CmpSnackbar message={snack} /></div></div>}
      {scan && <Scanner mode={scan.mode} code={scan.code} wide={!compact} onClose={closeScan} onManual={manual} onTap={scan.live ? found : undefined} />}
    </div>
  );
}

// ── Витрина ─────────────────────────────────────────────────────────────────
const DF = "Roboto, system-ui, sans-serif";
function PiSeg({ items, value, onChange }) {
  return (
    <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", background: "#fff", borderRadius: 22, padding: 4, boxShadow: "0 1px 3px rgba(0,0,0,.18)" }}>
      {items.map(([k, l]) => <button key={k} onClick={() => onChange(k)} style={{ border: "none", background: value === k ? "#1A3E7A" : "transparent", color: value === k ? "#fff" : "#444", padding: "8px 14px", borderRadius: 999, fontFamily: DF, fontSize: 13, fontWeight: 500, cursor: "pointer", whiteSpace: "nowrap" }}>{l}</button>)}
    </div>
  );
}
function PiPhone({ dark, children, width = 412, height = 840 }) {
  const c = dark ? "#fff" : "#1A1C1C";
  return (
    <div style={{ width, height, borderRadius: 22, overflow: "hidden", border: "8px solid rgba(116,119,117,.5)", boxShadow: "0 30px 80px rgba(0,0,0,.25)", display: "flex", flexDirection: "column", boxSizing: "border-box", flex: "0 0 auto", background: "#000" }}>
      <div data-theme={dark ? "dark" : undefined} style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", background: "var(--background)" }}>
        <div style={{ height: 32, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 18px", fontFamily: DF, fontSize: 14, color: c, flex: "0 0 auto" }}><span>9:30</span><span style={{ display: "flex", gap: 4 }}><CmpIcon icon="signal_cellular_alt" size={16} /><CmpIcon icon="battery_full" size={16} /></span></div>
        <div style={{ flex: 1, minHeight: 0 }}>{children}</div>
        <div style={{ height: 20, display: "grid", placeItems: "center", flex: "0 0 auto" }}><div style={{ width: 108, height: 4, borderRadius: 2, background: c, opacity: 0.4 }} /></div>
      </div>
    </div>
  );
}
const PI_WINDOWS = { tl: { w: 1280, h: 800, l: "Планшет · ландшафт 1280×800", kind: "tablet" }, tp: { w: 800, h: 1280, l: "Планшет · портрет 800×1280", kind: "tablet" }, dk: { w: 1280, h: 800, l: "Desktop 1280×800", kind: "desktop" }, dmin: { w: 800, h: 600, l: "Desktop · мин. 800×600", kind: "desktop" } };
function PiFit({ w, h, max = 1, children }) {
  const ref = useRef(null); const [s, setS] = useState(1);
  useEffect(() => { const el = ref.current; const m = () => setS(Math.min(max, el.offsetWidth / w)); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, [w, max]);
  return <div ref={ref} style={{ width: "100%", display: "flex", justifyContent: "center" }}><div style={{ width: w * s, height: h * s }}><div style={{ width: w, height: h, transform: `scale(${s})`, transformOrigin: "top left" }}>{children}</div></div></div>;
}
function PiWindow({ win, dark, children, max }) {
  const desk = win.kind === "desktop", bw = desk ? 1 : 12;
  const W = win.w + bw * 2, H = win.h + bw * 2 + (desk ? 36 : 0);
  return (
    <PiFit w={W} h={H} max={max}>
      <div style={{ width: W, height: H, boxSizing: "border-box", borderRadius: desk ? 10 : 28, overflow: "hidden", border: desk ? "1px solid rgba(0,0,0,.25)" : "12px solid #2b2d30", background: "#2b2d30", boxShadow: "0 30px 80px rgba(0,0,0,.25)", display: "flex", flexDirection: "column" }}>
        {desk && <div style={{ height: 36, background: dark ? "#2a3142" : "#dcdcdc", display: "flex", alignItems: "center", gap: 8, padding: "0 12px", fontFamily: DF, fontSize: 13, color: dark ? "#ECEEF2" : "#262626" }}><span style={{ fontWeight: 600 }}>Pandora Spec</span><span style={{ opacity: 0.6 }}>— Информация о товаре</span></div>}
        <div data-theme={dark ? "dark" : undefined} style={{ width: win.w, height: win.h, background: "var(--background)" }}>{children}</div>
      </div>
    </PiFit>
  );
}
const PI_STATES = [["empty", "Начальное"], ["loading", "Загрузка"], ["content", "Контент"], ["notfound", "Не найден"], ["offline", "Нет сети"], ["server", "Ошибка сервера"], ["input", "Ошибка ввода"]];
const PI_SCEN = [["nokit", "Без комплекта"], ["kit2", "2 компонента"], ["kit7", "7 компонентов"], ["kit12", "12 компонентов"], ["long", "Длинные названия"], ["sparse", "Мало данных"]];
const PI_SCAN = [["off", "Закрыт"], ["aim", "Наведение"], ["found", "Распознано"], ["hint", "Подсказка"], ["denied", "Нет доступа · спросить снова"], ["blocked", "Нет доступа · навсегда"], ["unavailable", "Камера недоступна"]];
function usePiCtl(wideDemo) {
  const qs = new URLSearchParams(location.search);
  const from = ["home", "mount", "assistant"].includes(qs.get("from")) ? qs.get("from") : "home";
  const [c, setC] = useState(() => {
    let dark = false; try { const v = localStorage.getItem("ps-theme"); dark = v ? v === "dark" : false; } catch (e) {}
    return { dark, entry: from, state: from === "home" ? "empty" : "content", scen: "kit7", scanner: "on", scan: "off", ierr: "empty", win: PI_WINDOWS[qs.get("win")] ? qs.get("win") : "tl", n: qs.get("n") || "" };
  });
  const set = (k) => (v) => setC((s) => {
    const n = { ...s, [k]: v };
    if (k === "entry") n.state = v === "home" ? "empty" : "content";
    if (k === "dark") { try { localStorage.setItem("ps-theme", v ? "dark" : "light"); } catch (e) {} }
    return n;
  });
  useEffect(() => { document.body.style.background = c.dark ? "#101314" : "#c9ccce"; }, [c.dark]);
  const desk = wideDemo && PI_WINDOWS[c.win].kind === "desktop";
  const row = { display: "flex", gap: 10, flexWrap: "wrap", justifyContent: "center", maxWidth: 1200 };
  const bar = (
    <div style={{ display: "flex", flexDirection: "column", gap: 10, alignItems: "center" }}>
      <div style={row}>
        <PiSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={c.dark ? "d" : "l"} onChange={(v) => set("dark")(v === "d")} />
        <PiSeg items={[["home", "Вход: главный"], ["mount", "Вход: монтаж"], ["assistant", "Вход: ассистент"]]} value={c.entry} onChange={set("entry")} />
        {wideDemo && <PiSeg items={Object.entries(PI_WINDOWS).map(([k, v]) => [k, v.l])} value={c.win} onChange={set("win")} />}
      </div>
      <div style={row}><PiSeg items={PI_STATES} value={c.state} onChange={set("state")} /></div>
      <div style={row}>
        {c.state === "content" && <PiSeg items={PI_SCEN} value={c.scen} onChange={set("scen")} />}
        {c.entry !== "home" && c.state === "content" && <PiSeg items={[["", "Номер блока 107131"], ["107140", "Другой номер 107140"]]} value={c.n} onChange={set("n")} />}
        {c.state === "input" && <PiSeg items={[["empty", "Пустое поле"], ["letters", "Не цифры"], ["long", "Слишком длинный"]]} value={c.ierr} onChange={set("ierr")} />}
      </div>
      {!desk && <div style={row}>
        <PiSeg items={[["off", "Без сканера"], ["on", "Сканер в поле"]]} value={c.scanner} onChange={set("scanner")} />
        {c.scanner === "on" && <PiSeg items={PI_SCAN.map(([k, l]) => [k, "Окно сканера: " + l])} value={c.scan} onChange={set("scan")} />}
      </div>}
      <div style={{ fontFamily: DF, fontSize: 13, color: c.dark ? "#c7cccf" : "#444", maxWidth: 820, textAlign: "center" }}>Поиск живой: {PI_NOTFOUND} — «не найден», любой другой номер — найден. Иконка сканера в поле открывает камеру: через 3 с штрих-код распознаётся сам (или нажмите на рамку).</div>
    </div>
  );
  const props = { entry: c.entry, number: c.n || undefined, state0: c.state, scenario: c.scen, scanner: c.scanner === "on", scan0: c.scanner === "on" && c.scan !== "off" ? c.scan : null, inputErr0: c.state === "input" ? c.ierr : null };
  const key = [c.entry, c.state, c.scen, c.n, c.scanner, c.scan, c.ierr, c.win].join("|");
  const back = (wide) => () => { const m = { home: wide ? (desk ? "home-desktop" : "home-tablet") : "home", mount: wide ? (desk ? "advanced-mount-desktop" : "advanced-mount-tablet") : "advanced-mount", assistant: wide ? "assistant-wide" : "assistant" }; window.psGo && window.psGo(m[c.entry]); };
  return { c, bar, props, key, back };
}
function ProductInfoDemo() {
  const { c, bar, props, key, back } = usePiCtl(false);
  return (
    <div style={{ minHeight: "100vh", padding: "20px 16px 32px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 20 }}>
      {bar}
      <PiPhone dark={c.dark}><ProductInfoApp key={key} {...props} platform="mobile" onBack={back(false)} /></PiPhone>
    </div>
  );
}
function ProductInfoWideDemo() {
  const { c, bar, props, key, back } = usePiCtl(true);
  const win = PI_WINDOWS[c.win];
  return (
    <div style={{ minHeight: "100vh", padding: "20px 24px 32px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 20 }}>
      {bar}
      <PiWindow win={win} dark={c.dark}><ProductInfoApp key={key} {...props} platform={win.kind} onBack={back(true)} /></PiWindow>
    </div>
  );
}
// Все состояния на одном холсте.
function Mini({ cap, dark, scale = 0.6, children }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, width: 412 * scale }}>
      <div style={{ fontFamily: DF, fontSize: 13, fontWeight: 600, color: "#262626", minHeight: 36 }}>{cap}</div>
      <div style={{ width: 412 * scale, height: 840 * scale }}><div style={{ width: 412, height: 840, transform: `scale(${scale})`, transformOrigin: "top left" }}><PiPhone dark={dark}>{children}</PiPhone></div></div>
    </div>
  );
}
function Group({ title, children }) {
  return (
    <section style={{ display: "flex", flexDirection: "column", gap: 12, width: "100%", maxWidth: 1480 }}>
      <div style={{ fontFamily: DF, fontSize: 18, fontWeight: 700, color: "#1A1C1C" }}>{title}</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 24, alignItems: "flex-start" }}>{children}</div>
    </section>
  );
}
function ProductInfoStatesDemo() {
  const A = (p) => <ProductInfoApp {...p} />;
  return (
    <div style={{ minHeight: "100vh", padding: "24px 24px 48px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 36, background: "#c9ccce" }}>
      <Group title="Состояния экрана · телефон, светлая">
        <Mini cap="1 Начальное (с главного)">{A({ state0: "empty" })}</Mini>
        <Mini cap="2 Загрузка">{A({ entry: "mount", state0: "loading" })}</Mini>
        <Mini cap="4 Не найден">{A({ state0: "notfound" })}</Mini>
        <Mini cap="5 Нет сети">{A({ entry: "mount", state0: "offline" })}</Mini>
        <Mini cap="6 Ошибка сервера">{A({ entry: "mount", state0: "server" })}</Mini>
      </Group>
      <Group title="7 Ошибки ввода">
        <Mini cap="Пустое поле">{A({ inputErr0: "empty" })}</Mini>
        <Mini cap="Не цифры">{A({ inputErr0: "letters" })}</Mini>
        <Mini cap="Слишком длинный">{A({ inputErr0: "long" })}</Mini>
      </Group>
      <Group title="3 Контент">
        <Mini cap="Без комплекта · из монтажа (подключённый блок)">{A({ entry: "mount", state0: "content", scenario: "nokit" })}</Mini>
        <Mini cap="Комплект из 2 компонентов">{A({ state0: "content", scenario: "kit2" })}</Mini>
        <Mini cap="7 компонентов · строки, один раскрыт">{A({ state0: "content", scenario: "kit7", open0: [0] })}</Mini>
        <Mini cap="12 компонентов · все раскрыты">{A({ state0: "content", scenario: "kit12", open0: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] })}</Mini>
        <Mini cap="Длинные названия · раскрыт">{A({ state0: "content", scenario: "long", open0: [0] })}</Mini>
        <Mini cap="Мало данных (только название и ID)">{A({ state0: "content", scenario: "sparse" })}</Mini>
      </Group>
      <Group title="«Подключённый блок» · когда показывается">
        <Mini cap="Из монтажа: номер блока → пометка есть">{A({ entry: "mount", state0: "content", scenario: "kit2" })}</Mini>
        <Mini cap="Номер стёрт, найден другой (107140) → пометки нет">{A({ entry: "mount", number: "107140", state0: "content", scenario: "kit2" })}</Mini>
        <Mini cap="Снова найден блок (штрих-код его коробки) → пометка вернулась">{A({ entry: "mount", number: PI_BARCODE, state0: "content", scenario: "kit2" })}</Mini>
        <Mini cap="Ассистент → «Подробнее» → пометка есть">{A({ entry: "assistant", state0: "content", scenario: "kit7" })}</Mini>
      </Group>
      <Group title="Сканер штрих-кода">
        <Mini cap="Поле с кнопкой сканера">{A({ state0: "empty" })}</Mini>
        <Mini cap="Наведение">{A({ state0: "empty", scan0: "aim" })}</Mini>
        <Mini cap="Распознано → поиск">{A({ state0: "empty", scan0: "found" })}</Mini>
        <Mini cap="Долго не распознаётся">{A({ state0: "empty", scan0: "hint" })}</Mini>
        <Mini cap="Нет доступа · отказали один раз">{A({ state0: "empty", scan0: "denied" })}</Mini>
        <Mini cap="Нет доступа · отказали навсегда">{A({ state0: "empty", scan0: "blocked" })}</Mini>
        <Mini cap="Камера недоступна">{A({ state0: "empty", scan0: "unavailable" })}</Mini>
        <Mini cap="Без сканера (текущий релиз)">{A({ state0: "empty", scanner: false })}</Mini>
      </Group>
      <Group title="Тёмная тема">
        <Mini dark cap="Контент · 7 компонентов">{A({ entry: "assistant", state0: "content", scenario: "kit7", open0: [1] })}</Mini>
        <Mini dark cap="Не найден">{A({ state0: "notfound" })}</Mini>
        <Mini dark cap="Начальное">{A({ state0: "empty" })}</Mini>
        <Mini dark cap="Сканер · распознано">{A({ state0: "empty", scan0: "found" })}</Mini>
        <Mini dark cap="Сканер · камера недоступна">{A({ state0: "empty", scan0: "unavailable" })}</Mini>
      </Group>
      <Group title="Сканер на планшете · камера недоступна · светлая и тёмная">
        <div style={{ width: "100%", display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(min(100%, 620px), 1fr))", gap: 24 }}>
          <PiWindow win={PI_WINDOWS.tl} max={0.6}><ProductInfoApp state0="empty" scan0="unavailable" platform="tablet" /></PiWindow>
          <PiWindow win={PI_WINDOWS.tl} dark max={0.6}><ProductInfoApp state0="empty" scan0="unavailable" platform="tablet" /></PiWindow>
        </div>
      </Group>
      <Group title="Широкое окно · Desktop 1280×800 · 12 компонентов">
        <div style={{ width: "100%", display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(min(100%, 620px), 1fr))", gap: 24 }}>
          <PiWindow win={PI_WINDOWS.dk} max={0.6}><ProductInfoApp entry="mount" state0="content" scenario="kit12" platform="desktop" /></PiWindow>
          <PiWindow win={PI_WINDOWS.dk} dark max={0.6}><ProductInfoApp entry="mount" state0="content" scenario="kit12" platform="desktop" /></PiWindow>
        </div>
      </Group>
    </div>
  );
}

const PiLoaded = () => null;
Object.assign(window, { ProductInfoApp, ProductInfoDemo, ProductInfoWideDemo, ProductInfoStatesDemo, PiLoaded, piLookup, PI_CONNECTED });
