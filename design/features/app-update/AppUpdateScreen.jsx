// AppUpdateScreen — «Обновление приложения».
// Desktop (Windows, macOS): проверка → загрузка → установка, каналы обновлений.
// Телефон/планшет (Android, iOS): описание новой версии и переход в магазин, каналов нет.
//
//   • useUpdateCtl(opts) — контроллер (состояние, канал, загрузка, диалоги). Им же пользуется главный экран,
//     поэтому загрузка идёт в фоне и видна в блоке бокового бара.
//   • UpdateBody / UpdateRoute — тело экрана (топбар даёт хост). UpdateDialogs — диалоги «Идёт работа с блоком», «Перейти на бету».
//   • AppUpdateApp — полный экран (топбар + тело + диалоги).
//   • Входы на главном: SideUpdateBlock (desktop, низ бокового бара), UpdateToast (desktop, при старте),
//     UpdateBanner (телефон/планшет, плашка на главном), MenuUpdateRow (телефон/планшет, пункт меню с точкой).
//   • Витрины: AppUpdateDeskDemo, AppUpdatePhoneDemo, AppUpdateStatesDemo.
// Всё экспортируется в window.AppUpdate.

// Ждём бандл дизайн-системы: в .dc.html он может догрузиться позже этого файла.
(function boot() {
if (!window.PandoraSpecDesignSystem_b8d6e1 || !window.React) return void setTimeout(boot, 30);
const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpButton, CmpOutlinedButton, CmpTextButton, CmpCircularProgressIndicator, CmpLinearProgressIndicator, CmpRadioButton, CmpDialog, CmpSnackbar, CmpBadge } = DS;
const { useState, useEffect, useRef } = React;

if (!document.getElementById("au-css")) {
  const s = document.createElement("style"); s.id = "au-css";
  s.textContent = `
@keyframes auIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}
.au-in{animation:auIn .24s cubic-bezier(.2,0,0,1) both}
.au-hov:hover{background-color:color-mix(in srgb, var(--on-surface) 8%, transparent)!important}
.au-hov:active{background-color:color-mix(in srgb, var(--on-surface) 12%, transparent)!important}
.au-hovp:hover{filter:brightness(.96)}
.au-f:focus-visible{outline:2px solid var(--primary);outline-offset:2px}
@media (prefers-reduced-motion:reduce){.au-in{animation:none!important}}`;
  document.head.appendChild(s);
}

// ── Данные ──────────────────────────────────────────────────────────────────
const isDesk = (p) => p === "win" || p === "mac";
const cmpV = (a, b) => { const x = String(a).split(".").map(Number), y = String(b).split(".").map(Number); for (let i = 0; i < 3; i++) { if ((x[i] || 0) !== (y[i] || 0)) return (x[i] || 0) - (y[i] || 0); } return 0; };
const INST = { old: { v: "2.0.4", b: "204" }, cur: { v: "2.1.0", b: "210" }, beta: { v: "2.2.0", b: "220" } };
const BASE = "s1234";
const REL = { id: "rel", name: "Релиз", v: "2.1.0", d: "12.11.2026", def: true };
const BETA = { id: "beta", name: "Бета", v: "2.2.0", d: "10.11.2026" };
const CHANNELS = {
  one: [REL],
  three: [REL, BETA, { id: "test", name: "Тест", v: "2.2.1", d: "13.11.2026" }],
  many: [REL, BETA,
    { id: "test", name: "Тест — внутренние сборки отдела разработки, не для монтажа у клиентов", v: "2.2.1", d: "13.11.2026" },
    { id: "pilot", name: "Пилот: дилерские центры Северо-Западного региона", v: "2.1.1", d: "11.11.2026" },
    { id: "can", name: "Пилот CAN-модулей нового поколения", v: null },
    { id: "w7", name: "Windows 7 и старые компьютеры", v: "1.9.8", d: "02.06.2026" }],
};
const MOBILE_CH = { id: "store", name: "", v: "2.1.0", d: "12.11.2026", def: true };
const NOTES = {
  short: "- Ускорена запись прошивок и синхронизация базы\n- Новые автомобили и точки подключения\n- Исправлены сбои при работе по CAN/LIN",
  long: [
    "Монтаж", "- Быстрый монтаж: подсказки по точкам подключения для 140 новых моделей", "- Расширенный монтаж: поиск блока по ID и штрих-коду", "- Ассистент монтажа запоминает последние действия",
    "", "Прошивка", "- Запись прошивок по USB стала примерно вдвое быстрее", "- Проверка файла прошивки перед записью", "- Архив прошивок: фильтр по дате выпуска",
    "", "Настройки", "- Новые параметры автозапуска для дизельных двигателей", "- Сравнение настроек с заводскими", "- Экспорт настроек в файл",
    "", "Исправления", "- Сбой при обрыве Bluetooth во время записи настроек", "- Неверная дата в журнале событий", "- Зависание при обновлении базы на медленной сети", "- Обрезка длинных названий автомобилей", "- Мелкие исправления интерфейса",
  ].join("\n"),
  empty: "",
};
const BETA_HINT = "Бета — новые функции раньше всех, но возможны ошибки";
const SIZE = 180;

// ── Контроллер ──────────────────────────────────────────────────────────────
// initial: "auto" — проверка при открытии; иначе — стартовое состояние (для витрины и главного).
function useUpdateCtl({ platform = "win", inst0 = "old", chans = "three", ch0, sizeKnown = false, busy = false, initial = "auto", dialog0 = null } = {}) {
  const desk = isDesk(platform);
  const channels = desk ? (CHANNELS[chans] || CHANNELS.three) : [MOBILE_CH];
  const [st, set] = useState(() => ({
    s: initial === "auto" ? "checking" : initial, inst: INST[inst0] || INST.old,
    chId: (desk && ch0) || channels[0].id, prog: initial === "downloading" ? 0.24 : initial === "downloaded" ? 1 : 0,
    dialog: dialog0, pend: dialog0 === "beta" ? channels[1] && channels[1].id : null,
  }));
  const T = useRef({});
  const clear = () => { clearTimeout(T.current.t); clearInterval(T.current.i); };
  const chOf = (id) => channels.find((c) => c.id === id) || channels[0];
  const resolve = (inst, c) => !c.v ? "err_empty" : cmpV(c.v, inst.v) > 0 ? "available" : cmpV(c.v, inst.v) === 0 ? "uptodate" : desk ? "older" : "uptodate";
  function check(id) {
    clear();
    set((s) => ({ ...s, s: "checking", chId: id || s.chId, prog: 0 }));
    T.current.t = setTimeout(() => set((s) => ({ ...s, s: resolve(s.inst, chOf(s.chId)) })), 1200);
  }
  useEffect(() => { if (initial === "auto") check(); return clear; }, []);
  function update() {
    clear();
    set((s) => ({ ...s, s: "downloading", prog: 0 }));
    T.current.i = setInterval(() => set((s) => {
      if (s.s !== "downloading") return s;
      const p = s.prog + 0.02;
      if (p >= 1) { clearInterval(T.current.i); return { ...s, s: "downloaded", prog: 1 }; }
      return { ...s, prog: p };
    }), 110);
  }
  function doInstall() {
    clear();
    set((s) => ({ ...s, s: "installing", dialog: null }));
    T.current.t = setTimeout(() => set((s) => { const c = chOf(s.chId); return { ...s, s: "uptodate", inst: { v: c.v, b: c.v.replace(/\./g, "") }, prog: 0 }; }), 2600);
  }
  return {
    ...st, platform, channels, ch: chOf(st.chId), sizeMB: sizeKnown ? SIZE : null, dlMB: st.prog * SIZE,
    check: () => check(), update,
    cancel: () => { clear(); set((s) => ({ ...s, s: "available", prog: 0 })); },
    install: () => busy ? set((s) => ({ ...s, dialog: "busy" })) : doInstall(),
    pick: (id) => {
      if (id === st.chId) return;
      if (!chOf(id).def) set((s) => ({ ...s, dialog: "beta", pend: id }));
      else check(id);
    },
    confirm: () => { if (st.dialog === "busy") doInstall(); else { const id = st.pend; set((s) => ({ ...s, dialog: null, pend: null })); check(id); } },
    dismiss: () => set((s) => ({ ...s, dialog: null, pend: null })),
  };
}

// ── Ширина контейнера → класс окна ──────────────────────────────────────────
function useAuMode() {
  const ref = useRef(null); const [w, setW] = useState(0);
  useEffect(() => { const el = ref.current; if (!el) return; const m = () => setW(el.offsetWidth); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, []);
  return [ref, w >= 1200 ? "large" : w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact"];
}

// ── Примитивы ───────────────────────────────────────────────────────────────
const TONE = {
  primary: { bg: "var(--brand-primary-highlight)", fg: "var(--brand-primary-default)" },
  neutral: { bg: "var(--background-surface-highlight)", fg: "var(--foreground-on-dark-secondary-default)" },
  success: { bg: "var(--success-highlight)", fg: "var(--success-default)" },
  error: { bg: "var(--error-highlight)", fg: "var(--error-default)" },
};
const B48 = { height: 48, whiteSpace: "nowrap" };
function Card({ children, gap = 20, style }) {
  return <div className="au-in" style={{ boxSizing: "border-box", borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)", padding: 24, display: "flex", flexDirection: "column", gap, minWidth: 0, ...style }}>{children}</div>;
}
function Head({ icon, tone = "primary", title, sub, spin }) {
  const c = TONE[tone];
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
      <span style={{ width: 56, height: 56, borderRadius: "var(--r-icon-radius)", background: c.bg, color: c.fg, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
        {spin ? <CmpCircularProgressIndicator size={28} strokeWidth={3} /> : <CmpIcon icon={icon} size={30} color="currentColor" />}
      </span>
      <div style={{ minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <span style={{ textWrap: "balance", overflowWrap: "anywhere" }}><CmpText variant="headlineSmall">{title}</CmpText></span>
        {sub && <span style={{ textWrap: "pretty" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{sub}</CmpText></span>}
      </div>
    </div>
  );
}
function VerPill({ label, value, accent = false }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 2, padding: "10px 16px", borderRadius: "var(--r-card-small-radius)", minWidth: 120, border: "1px solid var(--border-highlight)", background: accent ? "var(--brand-primary-highlight)" : "var(--background-surface-ultra-highlight)", color: accent ? "var(--on-primary-container)" : "var(--on-surface-variant)" }}>
      <CmpText variant="labelMedium" color="currentColor">{label}</CmpText>
      <CmpText variant="titleMedium" color="currentColor">{value}</CmpText>
    </div>
  );
}
function Pills({ from, to }) {
  return (
    <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
      <VerPill label="Установлена" value={from} />
      <CmpIcon icon="arrow_forward" size={22} color="var(--on-surface-variant)" />
      <VerPill label="Новая" value={to} accent />
    </div>
  );
}
function Note({ icon = "info", children }) {
  return (
    <div style={{ display: "flex", gap: 12, alignItems: "flex-start", padding: "12px 16px", borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-ultra-highlight)", border: "1px solid var(--border-highlight)" }}>
      <span style={{ marginTop: 1, flex: "0 0 auto", color: "var(--on-surface-variant)" }}><CmpIcon icon={icon} size={22} color="currentColor" /></span>
      <span style={{ textWrap: "pretty" }}><CmpText variant="bodyLarge" color="var(--on-surface)">{children}</CmpText></span>
    </div>
  );
}
const Divider = () => <div style={{ height: 1, background: "var(--outline-variant)" }} />;

// «Что нового»: простой текст, «-» — маркер, пустая строка — отступ, остальные строки — подзаголовки.
// Длиннее 8 строк — свёрнут до 6 с «Показать полностью».
function Notes({ text, open, onToggle, fadeBg = "var(--background-surface-card)" }) {
  const lines = text.split("\n");
  const LIM = 6, long = lines.length > LIM + 2;
  const shown = long && !open ? lines.slice(0, LIM) : lines;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      <CmpText variant="titleMedium">Что нового</CmpText>
      <div style={{ position: "relative", display: "flex", flexDirection: "column", gap: 6 }}>
        {shown.map((l, i) => {
          if (!l.trim()) return <div key={i} style={{ height: 6 }} />;
          const m = l.match(/^\s*[-•]\s+(.*)$/);
          return m
            ? <div key={i} style={{ display: "flex", gap: 12, alignItems: "baseline" }}><span style={{ width: 6, height: 6, borderRadius: 999, background: "var(--primary)", flex: "0 0 auto", transform: "translateY(-3px)" }} /><span style={{ textWrap: "pretty" }}><CmpText variant="bodyLarge">{m[1]}</CmpText></span></div>
            : <div key={i} style={{ paddingTop: i ? 2 : 0 }}><CmpText variant="titleSmall" color="var(--on-surface-variant)">{l}</CmpText></div>;
        })}
        {long && !open && <div aria-hidden="true" style={{ position: "absolute", left: 0, right: 0, bottom: 0, height: 48, background: `linear-gradient(to bottom, transparent, ${fadeBg})` }} />}
      </div>
      {long && <CmpTextButton text={open ? "Свернуть" : "Показать полностью"} icon={open ? "expand_less" : "expand_more"} onClick={onToggle} style={{ ...B48, alignSelf: "flex-start", marginLeft: -12 }} />}
    </div>
  );
}
function Progress({ sizeMB, dlMB, bg }) {
  const p = sizeMB ? dlMB / sizeMB : null;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      <CmpLinearProgressIndicator progress={p} height={8} />
      <span style={{ fontVariantNumeric: "tabular-nums" }}><CmpText variant="titleMedium">{p != null ? `${Math.round(dlMB)} из ${sizeMB} МБ · ${Math.round(p * 100)} %` : `${Math.round(dlMB)} МБ загружено`}</CmpText></span>
      {bg && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Можно вернуться на главный экран — загрузка продолжится.</CmpText>}
    </div>
  );
}

// ── Тело экрана ─────────────────────────────────────────────────────────────
function headOf(c, multi) {
  const ch = c.ch, n = ch.name, size = c.sizeMB ? ` · ${c.sizeMB} МБ` : "";
  switch (c.s) {
    case "checking": return { spin: 1, title: "Проверяем обновления", sub: multi ? `Канал «${n}»` : "Это займёт несколько секунд" };
    case "available": return { icon: "system_update", title: "Доступна новая версия", sub: `Версия ${ch.v} от ${ch.d}${isDesk(c.platform) ? size : ""}` };
    case "downloading": return { icon: "download", title: "Загружаем обновление", sub: `Версия ${ch.v}` };
    case "downloaded": return { icon: "download_done", title: "Обновление загружено", sub: `Версия ${ch.v} готова к установке` };
    case "installing": return { spin: 1, title: "Устанавливаем обновление…", sub: "Приложение закроется через несколько секунд" };
    case "uptodate": return { icon: "check_circle", tone: "success", title: "У вас последняя версия", sub: multi ? `Канал «${n}»` : null };
    case "older": return { icon: "check_circle", tone: "success", title: "Обновлений нет", sub: `Канал «${n}»` };
    case "err_net": return { icon: "wifi_off", tone: "neutral", title: "Нет подключения к интернету", sub: "Проверьте сеть и повторите" };
    case "err_server": return { icon: "cloud_off", tone: "neutral", title: "Не удалось проверить обновления", sub: "Сервер не ответил. Повторите через минуту" };
    case "err_empty": return { icon: "inbox", tone: "neutral", title: `В канале «${n}» пока нет версий`, sub: "Выберите другой канал или проверьте позже" };
    case "err_download": return { icon: "error", tone: "error", title: "Не удалось загрузить обновление", sub: "Загрузка прервалась. «Повторить» начнёт её заново" };
    case "err_file": return { icon: "file_download_off", tone: "neutral", title: "Файл обновления не подходит", sub: "Файл обновления не подходит для этого компьютера. Скачайте установщик с сайта" };
    default: return { icon: "system_update", title: "" };
  }
}
function CurrentRow({ c, multi }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap", minHeight: 48 }}>
      <div style={{ flex: "1 1 220px", minWidth: 0, display: "flex", flexDirection: "column" }}>
        <CmpText variant="labelLarge" color="var(--on-surface-variant)">Установлена версия</CmpText>
        <span style={{ display: "flex", alignItems: "baseline", columnGap: 12, flexWrap: "wrap" }}>
          <CmpText variant="titleLarge">{`${c.inst.v} (${c.inst.b})`}</CmpText>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{`база настроек ${BASE}`}</CmpText>
        </span>
      </div>
      {multi && (
        <span style={{ display: "inline-flex", alignItems: "center", gap: 8, minHeight: 36, padding: "6px 14px", boxSizing: "border-box", borderRadius: "var(--r-tags-radius)", border: "1px solid var(--border-highlight)", background: "var(--background-surface-highlight)", color: "var(--on-secondary-container)", maxWidth: "100%" }}>
          <CmpIcon icon="layers" size={20} color="currentColor" />
          <span style={{ overflowWrap: "anywhere" }}><CmpText variant="labelLarge" color="currentColor">{`Канал: ${c.ch.name}`}</CmpText></span>
        </span>
      )}
    </div>
  );
}
function ChannelCard({ c }) {
  const lock = c.s === "downloading" || c.s === "installing";
  return (
    <Card gap={12} style={{ padding: "20px 16px 16px" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 2, padding: "0 8px" }}>
        <CmpText variant="titleLarge">Канал обновлений</CmpText>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Откуда приходят новые версии</CmpText>
      </div>
      <div role="radiogroup" aria-label="Канал обновлений" style={{ display: "flex", flexDirection: "column" }}>
        {c.channels.map((ch) => {
          const on = ch.id === c.chId;
          const desc = !ch.v ? "Пока нет версий" : `Версия ${ch.v} · ${ch.d}${ch.v === c.inst.v ? " · установлена" : ""}`;
          return (
            <div key={ch.id} role="radio" aria-checked={on} aria-disabled={lock || undefined} tabIndex={lock ? -1 : 0} className={lock ? "" : "au-hov au-f"}
              onClick={lock ? undefined : () => c.pick(ch.id)} onKeyDown={(e) => { if (!lock && (e.key === "Enter" || e.key === " ")) { e.preventDefault(); c.pick(ch.id); } }}
              style={{ display: "flex", alignItems: "center", gap: 4, minHeight: 64, padding: "6px 12px 6px 0", borderRadius: 12, cursor: lock ? "default" : "pointer", opacity: lock && !on ? 0.5 : 1, background: on ? "color-mix(in srgb, var(--primary) 8%, transparent)" : "transparent" }}>
              <CmpRadioButton selected={on} tabIndex={-1} aria-hidden="true" style={{ pointerEvents: "none" }} />
              <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
                <span style={{ overflowWrap: "anywhere" }}><CmpText variant="titleMedium">{ch.name}</CmpText></span>
                <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{desc}</CmpText>
              </div>
            </div>
          );
        })}
      </div>
      <Note icon="science">{BETA_HINT}</Note>
    </Card>
  );
}
// c — контроллер (useUpdateCtl), from: menu | banner | toast | sidebar.
function UpdateBody({ c, mode = "expanded", from = "menu", notes = "long", layout = "two", bg = true, notesOpen0 = false, onLater = () => {} }) {
  const [open, setOpen] = useState(notesOpen0);
  const desk = isDesk(c.platform), multi = desk && c.channels.length > 1;
  const wide = mode === "expanded" || mode === "large";
  const two = multi && layout !== "one" && wide;
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const h = headOf(c, multi), s = c.s, nv = c.ch.v;
  const later = from !== "menu" ? <CmpTextButton text="Позже" onClick={onLater} style={B48} /> : null;
  const store = c.platform === "ios" ? "Открыть в App Store" : "Открыть в Google Play";
  const txt = NOTES[notes] != null ? NOTES[notes] : notes;
  let act = null;
  if (s === "available") act = desk
    ? <><CmpButton text="Обновить" icon="system_update" onClick={c.update} style={B48} /><CmpOutlinedButton text="Скачать файл" icon="download" style={B48} />{later}</>
    : <><CmpButton text={store} icon="open_in_new" style={B48} />{later}</>;
  if (s === "downloading") act = <CmpOutlinedButton text="Отменить" icon="close" onClick={c.cancel} style={B48} />;
  if (s === "downloaded") act = <><CmpButton text="Установить и перезапустить" icon="restart_alt" onClick={c.install} style={B48} /><CmpTextButton text="Позже" onClick={onLater} style={B48} /></>;
  if (s === "uptodate" || s === "older" || s === "err_empty") act = <CmpOutlinedButton text="Проверить снова" icon="refresh" onClick={c.check} style={B48} />;
  if (s === "err_net" || s === "err_server") act = <CmpButton text="Повторить" icon="refresh" onClick={c.check} style={B48} />;
  if (s === "err_download") act = <><CmpButton text="Повторить" icon="refresh" onClick={c.update} style={B48} /><CmpOutlinedButton text="Скачать файл" icon="download" style={B48} /></>;
  if (s === "err_file") act = <CmpButton text="Скачать файл" icon="download" style={B48} />;
  const main = (
    <Card key={s}>
      <Head {...h} />
      {(s === "available" || s === "downloaded") && <Pills from={c.inst.v} to={nv} />}
      {s === "downloading" && <Progress sizeMB={c.sizeMB} dlMB={c.dlMB} bg={bg} />}
      {s === "older" && <Note icon="info">{`В канале «${c.ch.name}» сейчас ${nv} — она старше установленной. Обновление придёт, когда в канале выйдет версия новее.`}</Note>}
      {act && <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>{act}</div>}
      {s === "available" && !desk && <span style={{ marginTop: -8 }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)">Если обновление ещё не появилось в магазине, попробуйте позже.</CmpText></span>}
      {s === "downloaded" && (
        <div style={{ display: "flex", flexDirection: "column", gap: 12, marginTop: -4 }}>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Приложение закроется, установит обновление и откроется снова.</CmpText>
          <Note icon={c.platform === "mac" ? "laptop_mac" : "desktop_windows"}>{c.platform === "mac"
            ? "macOS: установка пройдёт без окон — через несколько секунд приложение откроется само."
            : "Windows: откроется окно установщика — в нём нажмите «Далее», затем «Установить»."}</Note>
        </div>
      )}
      {s === "available" && txt && <><Divider /><Notes text={txt} open={open} onToggle={() => setOpen(!open)} /></>}
    </Card>
  );
  const chans = multi ? <ChannelCard c={c} /> : null;
  return (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: two ? 1040 : 720, margin: "0 auto", padding: `${pad}px ${pad}px ${pad + 16}px`, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 16 }}>
        <CurrentRow c={c} multi={multi} />
        {two
          ? <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) minmax(300px,360px)", gap: 24, alignItems: "start" }}>{main}{chans}</div>
          : <>{main}{chans}</>}
      </div>
    </div>
  );
}
function UpdateDialogs({ c }) {
  if (c.dialog === "busy") return (
    <CmpDialog icon="warning" title="Идёт работа с блоком" text="Обновиться сейчас? Соединение будет прервано." onDismissRequest={c.dismiss}
      dismissButton={<CmpTextButton text="Отмена" onClick={c.dismiss} style={B48} />} confirmButton={<CmpButton text="Обновить" onClick={c.confirm} style={B48} />} />
  );
  if (c.dialog === "beta") {
    const ch = c.channels.find((x) => x.id === c.pend) || c.channels[1];
    const rel = c.channels.find((x) => x.def) || c.channels[0];
    return (
      <CmpDialog icon="science" title={`Перейти на канал «${ch.name}»?`} text={`Новые функции появляются здесь раньше, но возможны ошибки. Вернуться на «${rel.name}» можно в любой момент.`} onDismissRequest={c.dismiss}
        dismissButton={<CmpTextButton text="Отмена" onClick={c.dismiss} style={B48} />} confirmButton={<CmpButton text="Перейти" onClick={c.confirm} style={B48} />} />
    );
  }
  return null;
}
// Для главного экрана: тело с контроллером главного (фоновая загрузка).
function UpdateRoute({ ctl, mode, from = "menu", onLater }) {
  return <UpdateBody c={ctl} mode={mode} from={from} onLater={onLater} bg />;
}
// Полный экран.
function AppUpdateApp({ platform = "win", initial = "auto", inst0 = "old", chans = "three", ch0, sizeKnown = false, busy = false, dialog0 = null, notes = "long", notesOpen0 = false, from = "menu", layout = "two", onBack }) {
  const [ref, mode] = useAuMode();
  const c = useUpdateCtl({ platform, initial, inst0, chans, ch0, sizeKnown, busy, dialog0 });
  return (
    <div ref={ref} style={{ position: "relative", height: "100%", width: "100%", display: "flex", flexDirection: "column", overflow: "hidden", background: "var(--background)", color: "var(--on-background)", fontFamily: "var(--font-sans)" }}>
      <CmpTopAppBar title="Обновление приложения" style={{ flex: "0 0 auto" }} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} />
      <UpdateBody c={c} mode={mode} from={from} notes={notes} notesOpen0={notesOpen0} layout={layout} onLater={onBack} bg />
      <UpdateDialogs c={c} />
    </div>
  );
}
// Совместимость со старым API.
function UpdateFlow({ mode, onLater, platform = "win" }) {
  const c = useUpdateCtl({ platform });
  return <UpdateBody c={c} mode={mode} onLater={onLater} />;
}

// ── Входы на главном ────────────────────────────────────────────────────────
// Desktop: блок внизу бокового бара — доступна / загрузка / загружено.
function SideUpdateBlock({ c, onOpen }) {
  if (!c) return null;
  const box = { boxSizing: "border-box", borderRadius: "var(--r-card-small-radius)", padding: "12px 14px", display: "flex", flexDirection: "column", gap: 10 };
  const keys = (fn) => (e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); fn(); } };
  if (c.s === "available") return (
    <div role="button" tabIndex={0} className="au-hovp au-f au-in" onClick={onOpen} onKeyDown={keys(onOpen)} style={{ ...box, flexDirection: "row", alignItems: "center", gap: 12, minHeight: 64, background: "var(--brand-primary-highlight)", color: "var(--on-primary-container)", cursor: "pointer" }}>
      <CmpIcon icon="system_update" size={24} color="currentColor" />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="labelLarge" weight={600} color="currentColor">Доступна новая версия</CmpText>
        <div><CmpText variant="labelMedium" color="currentColor">{`${c.ch.v} · нажмите, чтобы обновить`}</CmpText></div>
      </div>
      <CmpIcon icon="chevron_right" size={22} color="currentColor" />
    </div>
  );
  if (c.s === "downloading") {
    const p = c.sizeMB ? c.dlMB / c.sizeMB : null;
    return (
      <div role="button" tabIndex={0} className="au-hov au-f au-in" onClick={onOpen} onKeyDown={keys(onOpen)} style={{ ...box, background: "var(--background-surface-ultra-highlight)", border: "1px solid var(--border-highlight)", color: "var(--on-surface)", cursor: "pointer" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <CmpIcon icon="download" size={24} color="var(--primary)" />
          <div style={{ flex: 1, minWidth: 0 }}>
            <CmpText variant="labelLarge" weight={600}>{`Загрузка ${c.ch.v}`}</CmpText>
            <div style={{ fontVariantNumeric: "tabular-nums" }}><CmpText variant="labelMedium" color="var(--on-surface-variant)">{p != null ? `${Math.round(c.dlMB)} из ${c.sizeMB} МБ · ${Math.round(p * 100)} %` : `${Math.round(c.dlMB)} МБ загружено`}</CmpText></div>
          </div>
        </div>
        <CmpLinearProgressIndicator progress={p} height={6} />
      </div>
    );
  }
  if (c.s === "downloaded") return (
    <div className="au-in" style={{ ...box, background: "var(--brand-primary-highlight)", border: "1px solid var(--border-highlight)", color: "var(--foreground-on-dark-primary-default)" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <CmpIcon icon="download_done" size={24} color="currentColor" />
        <div style={{ flex: 1, minWidth: 0 }}>
          <CmpText variant="labelLarge" weight={600} color="currentColor">Обновление загружено</CmpText>
          <div><CmpText variant="labelMedium" color="currentColor">{`Версия ${c.ch.v} готова к установке`}</CmpText></div>
        </div>
      </div>
      <CmpButton text="Установить" icon="restart_alt" fullWidth onClick={c.install} style={B48} />
    </div>
  );
  if (c.s === "installing") return (
    <div className="au-in" style={{ ...box, flexDirection: "row", alignItems: "center", gap: 12, minHeight: 64, background: "var(--background-surface-ultra-highlight)", border: "1px solid var(--border-highlight)" }}>
      <CmpCircularProgressIndicator size={22} strokeWidth={2.5} />
      <CmpText variant="labelLarge" weight={600}>Устанавливаем обновление…</CmpText>
    </div>
  );
  return null;
}
// Desktop: уведомление при старте, один раз за запуск.
function UpdateToast({ latest, onUpdate, onClose }) {
  return <div className="au-in" style={{ width: "100%", maxWidth: 480 }}><CmpSnackbar message={`Доступна версия ${latest}`} actionLabel="Обновить" onActionClick={onUpdate} withDismissAction onDismiss={onClose} /></div>;
}
// Телефон/планшет: закрываемая плашка на главном. Скрыта до следующей новой версии.
function UpdateBanner({ latest, onOpen, onClose }) {
  return (
    <div role="button" tabIndex={0} className="au-hovp au-f au-in" onClick={onOpen} onKeyDown={(e) => { if (e.key === "Enter") onOpen(); }}
      style={{ display: "flex", alignItems: "center", gap: 14, minHeight: 72, padding: "10px 4px 10px 16px", boxSizing: "border-box", borderRadius: "var(--r-card-small-radius)", border: "1px solid var(--border-highlight)", background: "var(--brand-primary-highlight)", color: "var(--on-primary-container)", cursor: "pointer" }}>
      <CmpIcon icon="system_update" size={28} color="currentColor" />
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <CmpText variant="titleMedium" color="currentColor">{`Доступна новая версия ${latest}`}</CmpText>
        <CmpText variant="bodyMedium" color="currentColor">Что нового и как обновиться</CmpText>
      </div>
      <button type="button" className="au-f" aria-label="Скрыть" title="Скрыть" onClick={(e) => { e.stopPropagation(); onClose(); }}
        style={{ width: 48, height: 48, border: "none", borderRadius: 999, background: "transparent", color: "currentColor", display: "grid", placeItems: "center", cursor: "pointer", flex: "0 0 auto" }}>
        <CmpIcon icon="close" size={24} color="currentColor" />
      </button>
    </div>
  );
}
// Телефон/планшет: пункт бокового меню с точкой, когда есть новая версия.
function MenuUpdateRow({ dot, onClick }) {
  return (
    <div role="button" tabIndex={0} className="au-hov au-f" onClick={onClick} onKeyDown={(e) => { if (e.key === "Enter") onClick && onClick(); }}
      style={{ boxSizing: "border-box", minHeight: 52, display: "flex", alignItems: "center", gap: 16, padding: "0 12px", borderRadius: 12, cursor: "pointer", color: "var(--foreground-on-dark-primary-default)" }}>
      <CmpIcon icon="system_update" size={24} color="var(--on-surface-variant)" />
      <div style={{ flex: 1, minWidth: 0 }}><CmpText variant="bodyLarge">Обновление приложения</CmpText></div>
      {dot && <span aria-label="Есть новая версия" style={{ width: 10, height: 10, borderRadius: 999, background: "var(--brand-primary-default)", flex: "0 0 auto" }} />}
    </div>
  );
}
// Топбар главного: значок «Нужно обновить приложение» (база настроек требует более новую версию).
function RequiredUpdateButton({ onClick }) {
  return (
    <span style={{ position: "relative", display: "inline-flex" }}>
      <CmpIconButton icon="system_update" contentDescription="Нужно обновить приложение" onClick={onClick} />
      <span aria-hidden="true" style={{ position: "absolute", top: 8, right: 8, width: 10, height: 10, borderRadius: 999, background: "var(--brand-primary-default)", pointerEvents: "none" }} />
    </span>
  );
}

// ── Витрины ─────────────────────────────────────────────────────────────────
const DF = "var(--font-sans)";
function Seg({ items, value, onChange }) {
  return (
    <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", background: "#fff", borderRadius: 22, padding: 4, boxShadow: "0 1px 3px rgba(0,0,0,.18)" }}>
      {items.map(([k, l]) => <button key={k} onClick={() => onChange(k)} style={{ border: "none", background: value === k ? "#0A1A3A" : "transparent", color: value === k ? "#fff" : "#4A5874", padding: "8px 14px", borderRadius: 999, fontFamily: DF, fontSize: "var(--t-additional0-size)", fontWeight: 500, cursor: "pointer", whiteSpace: "nowrap" }}>{l}</button>)}
    </div>
  );
}
function Fit({ w, h, max = 1, children }) {
  const ref = useRef(null); const [s, setS] = useState(max);
  useEffect(() => { const el = ref.current; const m = () => setS(Math.min(max, el.offsetWidth / w)); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, [w, max]);
  return <div ref={ref} style={{ width: "100%", display: "flex", justifyContent: "center" }}><div style={{ width: w * s, height: h * s, flex: "0 0 auto" }}><div style={{ width: w, height: h, transform: `scale(${s})`, transformOrigin: "top left" }}>{children}</div></div></div>;
}
function DeskWindow({ w = 1280, h = 800, os = "win", dark, children }) {
  const bar = dark ? "#2a2e30" : "#e3e5e6", fg = dark ? "#e2e4e5" : "#262626";
  const dot = (c) => <span style={{ width: 12, height: 12, borderRadius: 999, background: c }} />;
  return (
    <div style={{ width: w, height: h + 36, borderRadius: 10, overflow: "hidden", border: "1px solid rgba(0,0,0,.25)", boxShadow: "0 30px 80px rgba(0,0,0,.25)", display: "flex", flexDirection: "column", boxSizing: "border-box" }}>
      <div style={{ height: 36, flex: "0 0 auto", background: bar, display: "flex", alignItems: "center", gap: 8, padding: "0 12px", fontFamily: DF, fontSize: "var(--t-additional0-size)", color: fg }}>
        {os === "mac" && <span style={{ display: "flex", gap: 8, marginRight: 8 }}>{dot("#ff5f57")}{dot("#febc2e")}{dot("#28c840")}</span>}
        <span style={{ fontWeight: 600 }}>Pandora Спец</span>
        <span style={{ flex: 1 }} />
        {os === "win" && <span style={{ display: "flex", gap: 18, opacity: 0.7 }}><CmpIcon icon="remove" size={16} color={fg} /><CmpIcon icon="crop_square" size={16} color={fg} /><CmpIcon icon="close" size={16} color={fg} /></span>}
      </div>
      <div data-theme={dark ? "dark" : undefined} style={{ flex: 1, minHeight: 0, background: "var(--background)" }}>{children}</div>
    </div>
  );
}
function Phone({ dark, w = 412, h = 840, children }) {
  const c = dark ? "#fff" : "#1A1C1C";
  return (
    <div style={{ width: w, height: h, borderRadius: 22, overflow: "hidden", border: "8px solid rgba(116,119,117,.5)", boxShadow: "0 30px 80px rgba(0,0,0,.25)", display: "flex", flexDirection: "column", boxSizing: "border-box", background: "#000" }}>
      <div data-theme={dark ? "dark" : undefined} style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", background: "var(--background)" }}>
        <div style={{ height: 32, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 18px", fontFamily: DF, fontSize: "var(--t-additional0-size)", color: c, flex: "0 0 auto" }}><span>9:30</span><span style={{ display: "flex", gap: 4 }}><CmpIcon icon="signal_cellular_alt" size={16} color={c} /><CmpIcon icon="battery_full" size={16} color={c} /></span></div>
        <div style={{ flex: 1, minHeight: 0 }}>{children}</div>
        <div style={{ height: 20, display: "grid", placeItems: "center", flex: "0 0 auto" }}><div style={{ width: 108, height: 4, borderRadius: 2, background: c, opacity: 0.4 }} /></div>
      </div>
    </div>
  );
}
const DESK_STATES = [["checking", "Проверка"], ["available", "Доступна"], ["downloading", "Загрузка"], ["downloaded", "Загружено"], ["installing", "Установка"], ["uptodate", "Последняя версия"], ["older", "В канале старше"], ["err_net", "Нет сети"], ["err_server", "Ошибка сервера"], ["err_empty", "Канал пуст"], ["err_download", "Сбой загрузки"], ["err_file", "Файл не подходит"]];
const MOB_STATES = [["checking", "Проверка"], ["available", "Доступна"], ["uptodate", "Последняя версия"], ["err_net", "Нет сети"], ["err_server", "Ошибка сервера"]];
// Стартовые данные под закреплённое состояние.
function preset(st, chans) {
  if (st === "uptodate") return { inst0: "cur" };
  if (st === "older") return { inst0: "beta", ch0: "rel" };
  if (st === "err_empty") return { chans: "many", ch0: "can" };
  return { chans };
}
const UPDATE_STATES = DESK_STATES.map(([k]) => k);
function useDemoTheme() {
  const [dark, setDark] = useState(() => { try { return localStorage.getItem("ps-theme") === "dark"; } catch (e) { return false; } });
  useEffect(() => { document.body.style.background = dark ? "#101314" : "#c9ccce"; try { localStorage.setItem("ps-theme", dark ? "dark" : "light"); } catch (e) {} }, [dark]);
  return [dark, setDark];
}
function AppUpdateDeskDemo() {
  const [dark, setDark] = useDemoTheme();
  const [c, setC] = useState({ os: "win", run: "proto", st: "available", layout: "two", chans: "three", size: "unk", busy: "off", notes: "long", from: "menu", dlg: "none", win: "dk" });
  const set = (k) => (v) => setC((s) => ({ ...s, [k]: v }));
  const W = c.win === "dk" ? [1280, 800] : [800, 600];
  const pin = c.run === "states";
  const pr = pin ? preset(c.st, c.chans) : { chans: c.chans };
  const props = { platform: c.os, initial: pin ? c.st : "auto", chans: c.chans, ...pr, sizeKnown: c.size === "known", busy: c.busy === "on", dialog0: pin && c.dlg !== "none" ? c.dlg : null, notes: c.notes, from: c.from, layout: c.layout, onBack: () => window.psGo && window.psGo("home-desktop") };
  const key = JSON.stringify([c, pin]);
  const row = { display: "flex", gap: 10, flexWrap: "wrap", justifyContent: "center", maxWidth: 1240 };
  return (
    <div style={{ minHeight: "100vh", padding: "20px 24px 32px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 14 }}>
      <div style={row}>
        <Seg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(v) => setDark(v === "d")} />
        <Seg items={[["win", "Windows"], ["mac", "macOS"]]} value={c.os} onChange={set("os")} />
        <Seg items={[["proto", "Прототип"], ["states", "Состояния"]]} value={c.run} onChange={set("run")} />
        <Seg items={[["dk", "Окно 1280×800"], ["dmin", "Мин. 800×600"]]} value={c.win} onChange={set("win")} />
      </div>
      {pin && <div style={row}><Seg items={DESK_STATES} value={c.st} onChange={set("st")} /></div>}
      <div style={row}>
        <Seg items={[["one", "1 канал"], ["three", "3 канала"], ["many", "6 каналов, длинные"]]} value={c.chans} onChange={set("chans")} />
        <Seg items={[["two", "Каналы справа"], ["one", "Каналы ниже"]]} value={c.layout} onChange={set("layout")} />
        <Seg items={[["unk", "Размер неизвестен"], ["known", "Размер известен"]]} value={c.size} onChange={set("size")} />
      </div>
      <div style={row}>
        <Seg items={[["long", "«Что нового» длинное"], ["short", "короткое"], ["empty", "пустое"]]} value={c.notes} onChange={set("notes")} />
        <Seg items={[["menu", "Открыт из меню"], ["toast", "из уведомления"]]} value={c.from} onChange={set("from")} />
        <Seg items={[["off", "Блок не подключён"], ["on", "Идёт работа с блоком"]]} value={c.busy} onChange={set("busy")} />
        {pin && <Seg items={[["none", "Без диалога"], ["busy", "Диалог: работа с блоком"], ["beta", "Диалог: переход на бету"]]} value={c.dlg} onChange={set("dlg")} />}
        <button onClick={() => window.psGo && window.psGo("home-desktop")} style={{ border: "none", background: "#fff", borderRadius: 999, padding: "8px 14px", fontFamily: DF, fontSize: "var(--t-additional0-size)", fontWeight: 500, color: "#1A3E7A", cursor: "pointer", boxShadow: "0 1px 3px rgba(0,0,0,.18)" }}>Главный экран →</button>
      </div>
      <div style={{ fontFamily: DF, fontSize: "var(--t-additional0-size)", color: dark ? "#c7cccf" : "#444", maxWidth: 820, textAlign: "center" }}>Прототип: «Обновить» → загрузка → «Установить и перезапустить». Смена канала запускает проверку; переход на не-релизный канал — через диалог. «Идёт работа с блоком» включает предупреждение перед установкой.</div>
      <Fit w={W[0]} h={W[1] + 36}><DeskWindow w={W[0]} h={W[1]} os={c.os} dark={dark}><AppUpdateApp key={key} {...props} /></DeskWindow></Fit>
    </div>
  );
}
function AppUpdatePhoneDemo() {
  const [dark, setDark] = useDemoTheme();
  const [c, setC] = useState({ os: "android", run: "proto", st: "available", notes: "long", from: "menu", dev: "phone" });
  const set = (k) => (v) => setC((s) => ({ ...s, [k]: v }));
  const pin = c.run === "states";
  const props = { platform: c.os, initial: pin ? c.st : "auto", ...(pin ? preset(c.st) : {}), notes: c.notes, from: c.from, onBack: () => window.psGo && window.psGo(c.dev === "tablet" ? "home-tablet" : "home") };
  const key = JSON.stringify([c]);
  const row = { display: "flex", gap: 10, flexWrap: "wrap", justifyContent: "center", maxWidth: 1000 };
  const app = <AppUpdateApp key={key} {...props} />;
  return (
    <div style={{ minHeight: "100vh", padding: "20px 16px 32px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 14 }}>
      <div style={row}>
        <Seg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(v) => setDark(v === "d")} />
        <Seg items={[["android", "Android"], ["ios", "iOS"]]} value={c.os} onChange={set("os")} />
        <Seg items={[["phone", "Телефон 412×840"], ["tablet", "Планшет 800×1280"]]} value={c.dev} onChange={set("dev")} />
        <Seg items={[["proto", "Прототип"], ["states", "Состояния"]]} value={c.run} onChange={set("run")} />
      </div>
      {pin && <div style={row}><Seg items={MOB_STATES} value={c.st} onChange={set("st")} /></div>}
      <div style={row}>
        <Seg items={[["long", "«Что нового» длинное"], ["short", "короткое"], ["empty", "пустое"]]} value={c.notes} onChange={set("notes")} />
        <Seg items={[["menu", "Открыт из меню"], ["banner", "из плашки на главном"]]} value={c.from} onChange={set("from")} />
      </div>
      {c.dev === "phone"
        ? <Phone dark={dark}>{app}</Phone>
        : <Fit w={800} h={1280} max={0.7}><Phone dark={dark} w={800} h={1280}>{app}</Phone></Fit>}
    </div>
  );
}
// Холст всех состояний.
function Cap({ children }) { return <div style={{ fontFamily: DF, fontSize: "var(--t-additional0-size)", fontWeight: 600, color: "#262626", minHeight: 20 }}>{children}</div>; }
function Group({ title, note, children }) {
  return (
    <section style={{ display: "flex", flexDirection: "column", gap: 12, width: "100%", maxWidth: 1760 }}>
      <div style={{ fontFamily: DF, fontSize: "var(--t-headline2-size)", fontWeight: 700, color: "#1A1C1C" }}>{title}</div>
      {note && <div style={{ fontFamily: DF, fontSize: "var(--t-additional0-size)", color: "#3b3f41", maxWidth: 900 }}>{note}</div>}
      <div style={{ display: "flex", flexWrap: "wrap", gap: 24, alignItems: "flex-start" }}>{children}</div>
    </section>
  );
}
function D({ cap, dark, os = "win", w = 1280, h = 800, scale = 0.4, ...p }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, width: w * scale }}>
      <Cap>{cap}</Cap>
      <div style={{ width: w * scale, height: (h + 36) * scale }}><div style={{ width: w, height: h + 36, transform: `scale(${scale})`, transformOrigin: "top left" }}><DeskWindow w={w} h={h} os={os} dark={dark}><AppUpdateApp platform={os} {...p} /></DeskWindow></div></div>
    </div>
  );
}
function M({ cap, dark, scale = 0.55, w = 412, h = 840, ...p }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, width: w * scale }}>
      <Cap>{cap}</Cap>
      <div style={{ width: w * scale, height: h * scale }}><div style={{ width: w, height: h, transform: `scale(${scale})`, transformOrigin: "top left" }}><Phone dark={dark} w={w} h={h}><AppUpdateApp {...p} /></Phone></div></div>
    </div>
  );
}
function Piece({ cap, dark, w = 360, children }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, width: w }}>
      <Cap>{cap}</Cap>
      <div data-theme={dark ? "dark" : undefined} style={{ padding: 16, borderRadius: 16, background: "var(--surface)", color: "var(--foreground-on-dark-primary-default)", border: "1px solid var(--border-highlight)", display: "flex", flexDirection: "column", gap: 10 }}>{children}</div>
    </div>
  );
}
function PieceCtl({ initial, sizeKnown, children }) { const c = useUpdateCtl({ platform: "win", initial, sizeKnown }); return children(c); }
function AppUpdateStatesDemo() {
  const P = (st, x = {}) => ({ initial: st, ...preset(st, "three"), ...x });
  return (
    <div style={{ minHeight: "100vh", padding: "24px 24px 48px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 40, background: "#c9ccce" }}>
      <Group title="Desktop · состояния · Windows, светлая, 3 канала (каналы справа)">
        <D cap="1 Проверка" {...P("checking")} />
        <D cap="2 Доступна новая версия · «Что нового» свёрнуто" {...P("available")} />
        <D cap="2 Доступна · из уведомления (есть «Позже»), текст раскрыт" {...P("available", { from: "toast", notesOpen0: true })} h={1100} />
        <D cap="2 Доступна · «Что нового» пустое, размер известен" {...P("available", { notes: "empty", sizeKnown: true })} />
        <D cap="3 Загрузка · размер известен" {...P("downloading", { sizeKnown: true })} />
        <D cap="3 Загрузка · размер неизвестен (сейчас в приложении)" {...P("downloading")} />
        <D cap="4 Готово к установке · Windows" {...P("downloaded")} />
        <D cap="4 Готово к установке · macOS" os="mac" {...P("downloaded")} />
        <D cap="5 Установка запущена" {...P("installing")} />
        <D cap="6 Установлена последняя версия" {...P("uptodate")} />
      </Group>
      <Group title="Desktop · ошибки">
        <D cap="Нет сети" {...P("err_net")} />
        <D cap="Ошибка сервера" {...P("err_server")} />
        <D cap="В канале нет сборок (6 каналов)" {...P("err_empty")} />
        <D cap="Загрузка прервалась" {...P("err_download")} />
        <D cap="Файл не подходит" {...P("err_file")} />
      </Group>
      <Group title="Desktop · диалоги">
        <D cap="Идёт работа с блоком → «Установить и перезапустить»" {...P("downloaded", { dialog0: "busy" })} />
        <D cap="Переход на «Бета»" {...P("available", { dialog0: "beta" })} />
      </Group>
      <Group title="Desktop · каналы" note="Один канал — секции нет. Смена канала сразу запускает проверку; переход на не-релизный канал — через короткий диалог. Отката нет: если в канале версия старше установленной, приложение остаётся на текущей.">
        <D cap="1 канал · секции нет" {...P("available", { chans: "one" })} />
        <D cap="6 каналов, длинные названия" {...P("available", { chans: "many" })} h={900} />
        <D cap="Версия в канале старше установленной (Бета 2.2.0 → Релиз 2.1.0)" {...P("older")} />
        <D cap="Вариант «каналы ниже» · одна колонка" {...P("available", { layout: "one", notes: "short" })} h={1060} />
        <D cap="Мин. окно 800×600 · каналы всегда ниже" w={800} h={600} scale={0.55} {...P("available", { notes: "short" })} />
      </Group>
      <Group title="Desktop · тёмная тема">
        <D dark cap="Доступна" {...P("available")} />
        <D dark cap="Загрузка · размер неизвестен" {...P("downloading")} />
        <D dark cap="Версия в канале старше" {...P("older")} />
        <D dark cap="Диалог «Идёт работа с блоком»" {...P("downloaded", { dialog0: "busy" })} />
      </Group>
      <Group title="Телефон · Android и iOS" note="Обновление только через магазин, каналов и загрузки нет.">
        <M cap="Проверка" platform="android" initial="checking" />
        <M cap="Доступна · Android" platform="android" initial="available" />
        <M cap="Доступна · iOS · из плашки (есть «Позже»)" platform="ios" initial="available" from="banner" notes="short" />
        <M cap="Последняя версия" platform="android" initial="uptodate" inst0="cur" />
        <M cap="Нет сети" platform="android" initial="err_net" />
        <M cap="Ошибка сервера" platform="ios" initial="err_server" />
        <M dark cap="Тёмная · Android" platform="android" initial="available" />
        <M dark cap="Тёмная · iOS · последняя версия" platform="ios" initial="uptodate" inst0="cur" />
      </Group>
      <Group title="Планшет 800×1280 · как телефон с большими отступами">
        <M cap="Доступна · Android" platform="android" initial="available" w={800} h={1280} scale={0.4} />
        <M dark cap="Доступна · iOS, тёмная" platform="ios" initial="available" notes="short" w={800} h={1280} scale={0.4} />
      </Group>
      <Group title="Входы на главном" note="Живьём — в макетах главного экрана (телефон, планшет, Desktop).">
        <Piece cap="Телефон/планшет · плашка на главном (закрываемая)"><UpdateBanner latest="2.1.0" onOpen={() => {}} onClose={() => {}} /></Piece>
        <Piece cap="Телефон/планшет · пункт меню с точкой"><MenuUpdateRow dot onClick={() => {}} /></Piece>
        <Piece cap="Топбар · «Нужно обновить приложение»"><div style={{ display: "flex", justifyContent: "flex-end" }}><RequiredUpdateButton onClick={() => {}} /></div></Piece>
        <Piece cap="Desktop · блок бокового бара: доступна"><PieceCtl initial="available">{(c) => <SideUpdateBlock c={c} onOpen={() => {}} />}</PieceCtl></Piece>
        <Piece cap="Desktop · загрузка (размер известен / нет)">
          <PieceCtl initial="downloading" sizeKnown>{(c) => <SideUpdateBlock c={c} onOpen={() => {}} />}</PieceCtl>
          <PieceCtl initial="downloading">{(c) => <SideUpdateBlock c={c} onOpen={() => {}} />}</PieceCtl>
        </Piece>
        <Piece cap="Desktop · готово к установке"><PieceCtl initial="downloaded">{(c) => <SideUpdateBlock c={c} onOpen={() => {}} />}</PieceCtl></Piece>
        <Piece cap="Desktop · уведомление при старте" w={480}><UpdateToast latest="2.1.0" onUpdate={() => {}} onClose={() => {}} /></Piece>
        <Piece dark cap="Тёмная · плашка и пункт меню"><UpdateBanner latest="2.1.0" onOpen={() => {}} onClose={() => {}} /><MenuUpdateRow dot onClick={() => {}} /></Piece>
        <Piece dark cap="Тёмная · блок бокового бара"><PieceCtl initial="downloaded">{(c) => <SideUpdateBlock c={c} onOpen={() => {}} />}</PieceCtl></Piece>
      </Group>
    </div>
  );
}

window.AppUpdate = {
  useUpdateCtl, useAuMode, UpdateBody, UpdateRoute, UpdateDialogs, AppUpdateApp, UpdateFlow, UpdateScreen: UpdateBody, VerPill,
  SideUpdateBlock, UpdateToast, UpdateBanner, MenuUpdateRow, RequiredUpdateButton,
  AppUpdateDeskDemo, AppUpdatePhoneDemo, AppUpdateStatesDemo, UPDATE_STATES, CHANNELS, NOTES,
};
// Для .dc.html: скомпилированный _ds_bundle.js содержит старый window.AppUpdate, поэтому витрины — отдельными глобалами.
window.AuPhoneDemo = AppUpdatePhoneDemo;
window.AuStatesDemo = AppUpdateStatesDemo;
})();
