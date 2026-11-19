// TechRequests.jsx — «Заявки на техпомощь» (Pandora Spec).
// Монтажник заказывает обратный звонок специалиста и следит за заявками. Это не чат:
// к заявке автоматически прикладываются данные подключённого блока.
//
// TechRequestsApp props:
//   lang       "ru" | "en"
//   platform   "mobile" | "tablet" | "desktop"   (desktop — подсказки клавиш)
//   grouping   "sections" | "chips"              (активные/завершённые: секции или фильтр-чипы)
//   wide       "split" | "dialog"                (≥840: список + панель формы | узкий список + диалог)
//   scenario   list | empty | loading | error | pageError | form | formErrors | sending |
//              success | limit | confirm | busy | draft
//   outcome    ok | limit | offline | block | other | actionFail   (ответ сервера в демо)
//   freeze     true — таймеры не идут (статичные кадры для доски состояний)
// Адаптив (M3 Window Size Classes по ширине контейнера): compact <600 · medium 600–839 · expanded ≥840.
// split работает только на expanded; на medium форма открывается диалогом.

const TR_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpButton, CmpOutlinedButton,
  CmpTextField, CmpFloatingActionButton, CmpDialog, CmpSnackbar,
  CmpLinearProgressIndicator, CmpCircularProgressIndicator,
} = TR_DS;
const TrTextBtn = (p) => (TR_DS.CmpTextButton ? <TR_DS.CmpTextButton {...p} /> : <CmpOutlinedButton {...p} />);

(function trInjectCss() {
  if (document.getElementById("tr-css")) return;
  const s = document.createElement("style");
  s.id = "tr-css";
  s.textContent =
    // Предупреждающий тон — в ДС нет warning-роли (тот же локальный токен, что в peripheral-devices).
    ":root{--tr-warn:#8A5300;--tr-warn-container:#FBE4C4;--on-tr-warn-container:#2E1A00}" +
    "[data-theme=dark]{--tr-warn:#F2B455;--tr-warn-container:#4A3410;--on-tr-warn-container:#FFE2B8}" +
    "@keyframes trShimmer{from{background-position:100% 0}to{background-position:0 0}}" +
    ".tr-skel{background:linear-gradient(90deg,var(--surface-variant) 25%,var(--surface-container-highest) 37%,var(--surface-variant) 63%);background-size:400% 100%;animation:trShimmer 1.4s ease infinite}" +
    ".tr-focus:focus-visible{outline:2px solid var(--primary);outline-offset:2px}" +
    ".tr-area::placeholder{color:var(--on-surface-variant);opacity:.8}" +
    "@media (prefers-reduced-motion:reduce){.tr-skel{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Тексты RU / EN ────────────────────────────────────────────────────────────
const TR_I18N = {
  ru: {
    title: "Заявки на техпомощь", back: "Назад", newReq: "Новая заявка",
    active: "Активные", finished: "Завершённые", all: "Все",
    reqNo: (n) => `Заявка №${n}`,
    status: { OPEN: "Открыта", IN_PROCESS: "В работе", SUSPENDED: "Отложена", FINISHED: "Выполнена", FINISHED_BY_USER: "Закрыта вами", CANCELED_BY_USER: "Отменена вами" },
    showMore: "Показать полностью", showLess: "Свернуть",
    resolved: "Вопрос решён", cancelReq: "Отменить заявку", closing: "Закрываем…", canceling: "Отменяем…",
    callback: "Перезвонят",
    emptyTitle: "Заявок пока нет",
    emptyText: "Опишите проблему — специалист техподдержки перезвонит. Данные блока приложим автоматически.",
    writeChat: "Написать в чат",
    errTitle: "Не удалось загрузить заявки", errText: "Проверьте подключение к интернету и повторите.", retry: "Повторить",
    loadingMore: "Загружаем ещё…", pageError: "Не удалось загрузить",
    noActive: "Активных заявок нет", noFinished: "Завершённых заявок нет",
    cancelTitle: (n) => `Отменить заявку №${n}?`, cancelText: "Специалист не будет звонить по ней.", keep: "Не отменять",
    closeTitle: (n) => `Закрыть заявку №${n}?`, closeText: "Отметим, что вопрос решён.", close: "Закрыть", backBtn: "Назад",
    snackCanceled: "Заявка отменена", snackClosed: "Заявка закрыта",
    snackCancelFail: "Не удалось отменить заявку: нет подключения к интернету",
    snackCloseFail: "Не удалось закрыть заявку: нет подключения к интернету",
    snackSendStopped: "Отправка остановлена. Если заявка успела уйти, она появится в списке.",
    formTitle: "Новая заявка", formSub: "Специалист перезвонит по указанному номеру",
    fromNote: (f) => `Открыто из «${f}». Текст ошибки уже в описании — допишите, что делали.`,
    name: "Как к вам обращаться", phone: "Телефон для обратного звонка", problem: "Опишите проблему",
    problemPh: "Не запускается автозапуск после установки CAN-прошивки…",
    problemHelp: "Марка и модель авто, что уже пробовали",
    errName: "Укажите имя", errPhoneEmpty: "Укажите телефон", errPhone: "Номер: от 10 до 15 цифр",
    errProblem: "Опишите проблему",
    attachTitle: "Что отправим вместе с заявкой", attachSum: "Блок и его настройки",
    attachRows: [["memory", "Блок"], ["tune", "Настройки блока"], ["language", "Язык приложения"]],
    attachVals: (d) => [`${d.model} · прошивка ${d.fw}`, "Все, прочитаем с блока при отправке", "Русский"],
    attachNote: "Эти данные нужны специалисту, чтобы разобраться без лишних вопросов. Отключить их нельзя.",
    send: "Отправить заявку", kbSend: "Ctrl+Enter — отправить", kbEsc: "Esc — закрыть",
    stages: ["Читаем настройки блока…", "Отправляем заявку…"], cancel: "Отмена",
    cancelNote: "Если заявка уже ушла, она появится в списке.",
    okTitle: (n) => `Заявка №${n} отправлена`, okText: (p) => <>Специалист перезвонит по номеру <span style={{ whiteSpace: "nowrap" }}>{p}</span></>, done: "Готово",
    eLimit: "У вас много незакрытых заявок. Дождитесь звонка или закройте лишние.", toList: "К заявкам",
    eOffline: "Нет подключения к интернету",
    eBlock: "Не удалось прочитать данные блока. Проверьте подключение",
    eOther: "Не удалось отправить заявку",
    draftTitle: "Удалить черновик?", draftText: "Введённый текст не сохранится.", del: "Удалить", cont: "Продолжить",
    months: ["января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"],
  },
  en: {
    title: "Tech support requests", back: "Back", newReq: "New request",
    active: "Active", finished: "Closed", all: "All",
    reqNo: (n) => `Request #${n}`,
    status: { OPEN: "Open", IN_PROCESS: "In progress", SUSPENDED: "On hold", FINISHED: "Completed", FINISHED_BY_USER: "Closed by you", CANCELED_BY_USER: "Cancelled by you" },
    showMore: "Show more", showLess: "Show less",
    resolved: "Issue resolved", cancelReq: "Cancel request", closing: "Closing…", canceling: "Cancelling…",
    callback: "Call back",
    emptyTitle: "No requests yet",
    emptyText: "Describe the problem and a support specialist will call you back. Device data is attached automatically.",
    writeChat: "Write to chat",
    errTitle: "Couldn't load requests", errText: "Check your internet connection and try again.", retry: "Retry",
    loadingMore: "Loading more…", pageError: "Couldn't load",
    noActive: "No active requests", noFinished: "No closed requests",
    cancelTitle: (n) => `Cancel request #${n}?`, cancelText: "The specialist won't call you about it.", keep: "Keep",
    closeTitle: (n) => `Close request #${n}?`, closeText: "We'll mark the issue as resolved.", close: "Close", backBtn: "Back",
    snackCanceled: "Request cancelled", snackClosed: "Request closed",
    snackCancelFail: "Couldn't cancel the request: no internet connection",
    snackCloseFail: "Couldn't close the request: no internet connection",
    snackSendStopped: "Sending stopped. If the request already went through, it will appear in the list.",
    formTitle: "New request", formSub: "A specialist will call you at this number",
    fromNote: (f) => `Opened from “${f}”. The error text is already in the description — add what you did.`,
    name: "Your name", phone: "Phone for the call back", problem: "Describe the problem",
    problemPh: "Remote start doesn't work after installing CAN firmware…",
    problemHelp: "Car make and model, what you've already tried",
    errName: "Enter your name", errPhoneEmpty: "Enter a phone number", errPhone: "Number: 10 to 15 digits",
    errProblem: "Describe the problem",
    attachTitle: "Sent along with the request", attachSum: "Device and its settings",
    attachRows: [["memory", "Device"], ["tune", "Device settings"], ["language", "App language"]],
    attachVals: (d) => [`${d.model} · firmware ${d.fw}`, "All, read from the device when sending", "English"],
    attachNote: "The specialist needs this data to sort things out without extra questions. It can't be turned off.",
    send: "Send request", kbSend: "Ctrl+Enter — send", kbEsc: "Esc — close",
    stages: ["Reading device settings…", "Sending request…"], cancel: "Cancel",
    cancelNote: "If the request already went through, it will appear in the list.",
    okTitle: (n) => `Request #${n} sent`, okText: (p) => <>A specialist will call you at <span style={{ whiteSpace: "nowrap" }}>{p}</span></>, done: "Done",
    eLimit: "You have too many open requests. Wait for a call or close the ones you no longer need.", toList: "To requests",
    eOffline: "No internet connection",
    eBlock: "Couldn't read device data. Check the connection",
    eOther: "Couldn't send the request",
    draftTitle: "Discard draft?", draftText: "The text you entered will be lost.", del: "Discard", cont: "Keep editing",
    months: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
  },
};

// ── Данные демо ───────────────────────────────────────────────────────────────
const TR_DEVICE = { model: "Pandora DX-91", fw: "2.41", transport: "Bluetooth" };
const TR_PROFILE = { name: { ru: "Алексей Смирнов", en: "Alexey Smirnov" }, phone: "+7 912 345-67-89" };
const TR_IGOR = { ru: "Игорь Петров", en: "Igor Petrov" };
const TR_SAMPLE = {
  ru: "Не запускается автозапуск после установки CAN-прошивки. Kia Sportage 2024, обходчик штатный. Блок видит зажигание, стартер не крутит.",
  en: "Remote start doesn't work after installing CAN firmware. Kia Sportage 2024, stock immobilizer bypass. The unit sees the ignition, the starter doesn't crank.",
};
const TR_ACTIVE = ["OPEN", "IN_PROCESS", "SUSPENDED"];
const TR_ITEMS = [
  { no: 10482, status: "OPEN", at: "2026-11-13T14:05", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Не запускается автозапуск после установки CAN-прошивки 12.05. Kia Sportage 2024, обходчик штатный, CAN подключён к жгуту за блоком BCM. Блок видит зажигание и статус дверей, но по команде автозапуска стартер не крутит: реле щёлкает, на панели загорается «Smart key not detected». Пробовал перепрошить блок и заново пройти обучение ключа — без изменений.",
      en: "Remote start doesn't work after installing CAN firmware 12.05. Kia Sportage 2024, stock immobilizer bypass, CAN connected to the harness behind the BCM. The unit sees the ignition and door status, but on a remote start command the starter doesn't crank: the relay clicks and the dash shows “Smart key not detected”. Tried reflashing the unit and re-learning the key — no change." } },
  { no: 10471, status: "IN_PROCESS", at: "2026-11-12T09:40", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Не читается CAN-шина — статус «нет связи». Hyundai Tucson 2023, подключение к OBD.", en: "CAN bus isn't read — status “no connection”. Hyundai Tucson 2023, connected via OBD." } },
  { no: 10455, status: "SUSPENDED", at: "2026-11-10T17:22", name: TR_IGOR, phone: "+7 922 018-44-30",
    text: { ru: "Метка BT-760 не приживляется: блок не выходит в режим программирования, светодиод метки моргает красным.", en: "BT-760 tag won't pair: the unit doesn't enter programming mode, the tag LED blinks red." } },
  { no: 10390, status: "FINISHED", at: "2026-11-02T11:15", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Нужна схема подключения датчика наклона для Toyota RAV4 2022.", en: "Need the tilt sensor wiring diagram for Toyota RAV4 2022." } },
  { no: 10377, status: "FINISHED_BY_USER", at: "2026-10-28T16:48", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "После обновления прошивки сбросились настройки автозапуска по температуре.", en: "Temperature-based remote start settings were reset after a firmware update." } },
  { no: 10301, status: "CANCELED_BY_USER", at: "2026-10-15T10:02", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Ошибочная заявка.", en: "Created by mistake." } },
  { no: 10215, status: "FINISHED", at: "2026-09-30T13:30", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Не открывается багажник с брелока, с кнопки в салоне открывается.", en: "Trunk doesn't open from the key fob, opens from the cabin button." } },
];
const TR_OLDER = [
  { no: 9870, status: "FINISHED", at: "2025-12-18T11:20", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Не приходят уведомления о тревоге в приложение.", en: "Alarm notifications don't reach the app." } },
  { no: 9702, status: "CANCELED_BY_USER", at: "2025-11-03T09:05", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Вопрос по комплектации, разобрался сам.", en: "Question about the kit, figured it out myself." } },
  { no: 9655, status: "FINISHED_BY_USER", at: "2025-10-21T15:44", name: TR_PROFILE.name, phone: TR_PROFILE.phone,
    text: { ru: "Не работает управление штатной сигнализацией после монтажа.", en: "Stock alarm control doesn't work after installation." } },
];
const TR_YEAR = 2026;

function trDate(iso, lang) {
  const [d, tm] = iso.split("T");
  const [y, m, day] = d.split("-").map(Number);
  const mon = TR_I18N[lang].months[m - 1];
  return y === TR_YEAR ? `${day} ${mon}, ${tm}` : `${day} ${mon} ${y}, ${tm}`;
}

function trValidate(v, t) {
  const digits = v.phone.replace(/\D/g, "").length;
  return {
    name: v.name.trim() ? null : t.errName,
    phone: !v.phone.trim() ? t.errPhoneEmpty : (!/^[+\d\s()\-]+$/.test(v.phone) || digits < 10 || digits > 15) ? t.errPhone : null,
    problem: v.problem.trim() ? null : t.errProblem,
  };
}

function trReduced() {
  return window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}
const TR_EASE = "cubic-bezier(.2,0,0,1)";

// Счётчик секции: при изменении число «прокручивается» вверх/вниз.
function TrCount({ n }) {
  const ref = React.useRef(null);
  const prev = React.useRef(n);
  React.useEffect(() => {
    if (prev.current !== n && ref.current && ref.current.animate && !trReduced()) {
      const dir = n > prev.current ? 1 : -1;
      ref.current.animate([{ transform: `translateY(${8 * dir}px)`, opacity: 0 }, { transform: "none", opacity: 1 }], { duration: 260, easing: TR_EASE });
    }
    prev.current = n;
  }, [n]);
  return <span ref={ref} style={{ display: "inline-block" }}><CmpText variant="labelMedium" weight={700} color="var(--on-surface-variant)">{n}</CmpText></span>;
}

// FLIP-анимации списка: все элементы с [data-tr-anim] внутри колонки.
//  • новый ключ — появление (fade + подъём, стаггер 45 мс);
//  • сместившийся — плавное перемещение со старого места;
//  • исчезнувший — клон снимка гаснет на старом месте.
// offsetTop (не getBoundingClientRect) — чтобы не зависеть от масштаба хоста и прокрутки.
function useTrFlip(colRef, enabled) {
  const snap = React.useRef(new Map());
  React.useLayoutEffect(() => {
    const col = colRef.current;
    if (!col) return;
    const els = Array.from(col.querySelectorAll(":scope > [data-tr-anim]"));
    const next = new Map();
    const go = enabled && !trReduced() && !!col.animate;
    let enter = 0;
    els.forEach((el) => {
      const k = el.getAttribute("data-tr-anim");
      const top = el.offsetTop;
      next.set(k, { top, left: el.offsetLeft, w: el.offsetWidth, h: el.offsetHeight, node: el.cloneNode(true) });
      if (!go) return;
      const p = snap.current.get(k);
      if (!p) {
        el.animate([{ opacity: 0, transform: "translateY(16px) scale(.98)" }, { opacity: 1, transform: "none" }],
          { duration: 360, delay: Math.min(enter++, 8) * 45, easing: TR_EASE, fill: "backwards" });
      } else if (Math.abs(p.top - top) > 1) {
        el.animate([{ transform: `translateY(${p.top - top}px)` }, { transform: "none" }], { duration: 420, easing: TR_EASE });
      }
    });
    if (go) {
      snap.current.forEach((p, k) => {
        if (next.has(k) || k.startsWith("s")) return;
        const g = p.node;
        Object.assign(g.style, { position: "absolute", top: p.top + "px", left: p.left + "px", width: p.w + "px", height: p.h + "px", margin: 0, pointerEvents: "none", zIndex: 0 });
        g.removeAttribute("data-tr-anim");
        col.appendChild(g);
        const a = g.animate([{ opacity: 1, transform: "none" }, { opacity: 0, transform: "scale(.96)" }], { duration: 220, easing: "ease-in" });
        a.onfinish = () => g.remove();
      });
    }
    snap.current = next;
  });
}

function useTrMode() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const m = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(m);
    ro.observe(el); m();
    return () => ro.disconnect();
  }, []);
  return [ref, w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact", w];
}

// ── Статус ────────────────────────────────────────────────────────────────────
const TR_TONE = {
  OPEN:             { bg: "var(--primary-container)", fg: "var(--on-primary-container)", icon: "schedule" },
  IN_PROCESS:       { bg: "color-mix(in srgb, var(--success) 24%, var(--surface-container-high))", fg: "var(--on-surface)", icon: "support_agent" },
  SUSPENDED:        { bg: "var(--tr-warn-container)", fg: "var(--on-tr-warn-container)", icon: "pause_circle" },
  FINISHED:         { bg: "var(--background-surface-highlight)", fg: "var(--on-surface-variant)", icon: "check_circle" },
  FINISHED_BY_USER: { bg: "var(--background-surface-highlight)", fg: "var(--on-surface-variant)", icon: "done" },
  CANCELED_BY_USER: { bg: "transparent", fg: "var(--on-surface-variant)", icon: "block", border: true },
};

function TrStatusChip({ status, t }) {
  const s = TR_TONE[status];
  const ref = React.useRef(null);
  const first = React.useRef(true);
  React.useEffect(() => {
    if (first.current) { first.current = false; return; }
    if (ref.current && ref.current.animate && !trReduced()) ref.current.animate(
      [{ transform: "scale(.8)", opacity: 0.3 }, { transform: "scale(1.06)", opacity: 1, offset: 0.6 }, { transform: "scale(1)" }],
      { duration: 360, easing: "cubic-bezier(.2,0,0,1)" });
  }, [status]);
  return (
    <span ref={ref} style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 32, padding: "0 12px 0 8px", borderRadius: 8, background: s.bg, color: s.fg, border: s.border ? "1px solid var(--border-highlight)" : "none", boxSizing: "border-box", flex: "0 0 auto" }}>
      <CmpIcon icon={s.icon} size={18} color="currentColor" />
      <CmpText variant="labelLarge" weight={600} color="currentColor">{t.status[status]}</CmpText>
    </span>
  );
}

// ── Карточка заявки ───────────────────────────────────────────────────────────
function TrCard({ r, t, lang, busy, highlight, onClose, onCancel }) {
  const [open, setOpen] = React.useState(false);
  const active = TR_ACTIVE.includes(r.status);
  const text = r.text[lang];
  const long = text.length > (active ? 170 : 110);
  const lines = active ? 4 : 2;
  const muted = active ? "var(--on-surface)" : "var(--on-surface-variant)";
  return (
    <div
      data-tr-anim={`c${r.no}`}
      style={{
        position: "relative", overflow: "hidden", borderRadius: "var(--r-card-medium-radius)", boxSizing: "border-box",
        padding: active ? "16px 16px 12px" : "14px 16px",
        background: highlight ? "color-mix(in srgb, var(--primary-container) 55%, var(--surface-container-low))" : active ? "var(--background-surface-card)" : "transparent",
        border: highlight ? "2px solid var(--primary)" : `1px solid ${active ? "var(--outline-variant)" : "color-mix(in srgb, var(--outline-variant) 70%, transparent)"}`,
        boxShadow: active ? "var(--elevation-1)" : "none",
        display: "flex", flexDirection: "column", gap: 10,
        transition: "background .6s, border-color .6s",
      }}
    >
      {busy && <div style={{ position: "absolute", left: 0, right: 0, top: 0 }}><CmpLinearProgressIndicator /></div>}
      <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "6px 12px", flexWrap: "wrap" }}>
          <CmpText variant={active ? "titleMedium" : "titleSmall"} weight={700} color={muted} style={{ whiteSpace: "nowrap", flex: "0 0 auto" }}>{t.reqNo(r.no)}</CmpText>
          <TrStatusChip status={r.status} t={t} />
        </div>
        <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <CmpIcon icon="calendar_today" size={16} color="var(--on-surface-variant)" />
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)" style={{ whiteSpace: "nowrap" }}>{trDate(r.at, lang)}</CmpText>
        </span>
      </div>
      <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-start", gap: 2 }}>
        <CmpText variant={active ? "bodyLarge" : "bodyMedium"} color={muted} maxLines={open ? undefined : lines} style={{ textWrap: "pretty", overflowWrap: "anywhere" }}>{text}</CmpText>
        {long && (
          <TrTextBtn text={open ? t.showLess : t.showMore} onClick={() => setOpen(!open)} style={{ height: 48, marginLeft: -12 }} />
        )}
      </div>
      {active && (
        <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: "4px 16px" }}>
          <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <CmpIcon icon="person" size={18} color="var(--on-surface-variant)" />
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{r.name[lang]}</CmpText>
          </span>
          <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <CmpIcon icon="call" size={18} color="var(--on-surface-variant)" />
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)" style={{ fontVariantNumeric: "tabular-nums" }}>{r.phone}</CmpText>
          </span>
        </div>
      )}
      {active && (
        <div style={{ display: "flex", flexWrap: "nowrap", alignItems: "center", gap: 8, paddingTop: 8, borderTop: "1px solid var(--border-highlight)", marginTop: 2 }}>
          <CmpOutlinedButton icon={busy === "close" ? undefined : "check"} text={busy === "close" ? t.closing : t.resolved} disabled={!!busy} onClick={onClose} style={{ height: 48, padding: "0 14px 0 10px", whiteSpace: "nowrap", flex: "0 1 auto", minWidth: 0 }} />
          <TrTextBtn icon={busy === "cancel" ? undefined : "close"} text={busy === "cancel" ? t.canceling : t.cancelReq} disabled={!!busy} onClick={onCancel} style={{ height: 48, padding: "0 12px 0 10px", whiteSpace: "nowrap", flex: "0 1 auto", minWidth: 0, color: busy ? undefined : "var(--error)" }} />
        </div>
      )}
    </div>
  );
}

function TrCardSkeleton({ h = 168 }) {
  return (
    <div style={{ borderRadius: "var(--r-card-medium-radius)", border: "1px solid var(--border-highlight)", padding: 16, display: "flex", flexDirection: "column", gap: 12, height: h, boxSizing: "border-box" }}>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <div className="tr-skel" style={{ width: 150, height: 22, borderRadius: 6 }} />
        <div className="tr-skel" style={{ width: 96, height: 32, borderRadius: 8 }} />
      </div>
      <div className="tr-skel" style={{ width: 120, height: 16, borderRadius: 6 }} />
      <div className="tr-skel" style={{ width: "92%", height: 16, borderRadius: 6 }} />
      <div className="tr-skel" style={{ width: "70%", height: 16, borderRadius: 6 }} />
    </div>
  );
}

// Иллюстрация пустого состояния — гарнитура из легаси `tech_support.xml` (контуры перенесены как есть).
function TrHeadset({ size = 112 }) {
  const d = [
    "M20.28,31.77h-3.2A7.56,7.56 0,0 0,14 32.4c-2,0.88 -4.65,2.95 -4.65,7.67a8.22,8.22 0,0 0,2.73 6.64A7.57,7.57 0,0 0,17 48.45h3.28a1.19,1.19 0,0 0,1.19 -1.19V33A1.19,1.19 0,0 0,20.28 31.77Z",
    "M54.72,48.45h3.2A7.56,7.56 0,0 0,61 47.82c2,-0.88 4.65,-2.95 4.65,-7.67a8.22,8.22 0,0 0,-2.73 -6.64A7.57,7.57 0,0 0,58 31.77H54.72A1.19,1.19 0,0 0,53.53 33V47.26A1.19,1.19 0,0 0,54.72 48.45Z",
    "M18.57,48.87c-0.06,0.06 2.83,7.42 8.81,10.82A10.58,10.58 0,0 0,32.6 61,21.46 21.46,0 0,0 34.86,61",
    "M38.18,60.76m-3.32,0a3.32,3.32 0,1 1,6.64 0a3.32,3.32 0,1 1,-6.64 0",
    "M17.82,31.12c0,-9.72 8.87,-20.2 19.81,-20.2S57.44,21.4 57.44,31.12",
    "M44.47,17.72a16.16,16.16 0,0 1,1.82 1.17",
    "M26.27,21.4a15.28,15.28 0,0 1,11.37 -5.49,13.57 13.57,0 0,1 3.41,0.44",
  ];
  return (
    <div aria-hidden="true" style={{ width: size, height: size, borderRadius: "50%", background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}>
      <svg viewBox="0 0 75 75" width={size * 0.66} height={size * 0.66} fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
        {d.map((p, i) => <path key={i} d={p} />)}
      </svg>
    </div>
  );
}

function TrEmpty({ t, onNew }) {
  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 20, padding: "40px 24px", textAlign: "center" }}>
      <TrHeadset />
      <div style={{ display: "flex", flexDirection: "column", gap: 8, maxWidth: 380 }}>
        <CmpText variant="headlineSmall" weight={600} align="center">{t.emptyTitle}</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)" align="center" style={{ textWrap: "pretty" }}>{t.emptyText}</CmpText>
      </div>
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4 }}>
        {onNew && <CmpButton icon="add" text={t.newReq} onClick={onNew} style={{ height: 56, padding: "0 28px 0 22px" }} />}
        <TrTextBtn icon="forum" text={t.writeChat} onClick={() => window.psGo && window.psGo("support-chat")} style={{ height: 48 }} />
      </div>
    </div>
  );
}

function TrError({ t, onRetry }) {
  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 20, padding: "40px 24px", textAlign: "center" }}>
      <div style={{ width: 96, height: 96, borderRadius: "50%", background: "var(--error-container)", display: "grid", placeItems: "center" }}>
        <CmpIcon icon="cloud_off" size={44} color="var(--on-error-container)" />
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8, maxWidth: 360 }}>
        <CmpText variant="headlineSmall" weight={600} align="center">{t.errTitle}</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)" align="center">{t.errText}</CmpText>
      </div>
      <CmpButton icon="refresh" text={t.retry} onClick={onRetry} style={{ height: 56, padding: "0 28px 0 22px" }} />
    </div>
  );
}

function TrSectionHead({ label, count, anim }) {
  return (
    <div data-tr-anim={anim} style={{ display: "flex", alignItems: "center", gap: 8, padding: "12px 4px 2px" }}>
      <CmpText variant="titleSmall" weight={700} color="var(--on-surface-variant)">{label}</CmpText>
      <span style={{ minWidth: 24, height: 24, padding: "0 8px", boxSizing: "border-box", borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)", display: "grid", placeItems: "center" }}>
        <TrCount n={count} />
      </span>
    </div>
  );
}

function TrChips({ t, filter, setFilter, counts }) {
  const opts = [["active", t.active, counts.active], ["finished", t.finished, counts.finished], ["all", t.all, null]];
  return (
    <div role="tablist" style={{ display: "flex", gap: 8, flexWrap: "wrap", padding: "4px 0 6px" }}>
      {opts.map(([k, label, n]) => {
        const on = filter === k;
        return (
          <button key={k} role="tab" aria-selected={on} className="tr-focus" onClick={() => setFilter(k)}
            style={{ height: 48, padding: on ? "0 18px 0 12px" : "0 18px", borderRadius: "var(--r-card-small-radius)", cursor: "pointer", display: "flex", alignItems: "center", gap: 8,
              border: on ? "1px solid transparent" : "1px solid var(--border-light)", background: on ? "var(--secondary-container)" : "transparent", color: on ? "var(--on-secondary-container)" : "var(--on-surface-variant)", fontFamily: "var(--font-sans)" }}>
            {on && <CmpIcon icon="check" size={18} color="currentColor" />}
            <CmpText variant="labelLarge" weight={600} color="currentColor">{label}{n != null ? ` · ${n}` : ""}</CmpText>
          </button>
        );
      })}
    </div>
  );
}

function TrListFooter({ t, loadingMore, pageFailed, onRetry }) {
  if (loadingMore) return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 12, minHeight: 64 }}>
      <CmpCircularProgressIndicator size={24} strokeWidth={3} />
      <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t.loadingMore}</CmpText>
    </div>
  );
  if (pageFailed) return (
    <div role="status" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, minHeight: 64, padding: "4px 4px 4px 16px", borderRadius: "var(--r-card-medium-radius)", background: "var(--error-container)", color: "var(--on-error-container)" }}>
      <span style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <CmpIcon icon="error" size={22} color="currentColor" />
        <CmpText variant="bodyLarge" weight={500} color="currentColor">{t.pageError}</CmpText>
      </span>
      <TrTextBtn icon="refresh" text={t.retry} onClick={onRetry} style={{ height: 48, color: "var(--on-error-container)" }} />
    </div>
  );
  return null;
}

// ── Поле «Проблема» (многострочное; в ДС только однострочный CmpTextField) ───
function TrProblemField({ value, onChange, t, error, disabled, taRef, minH = 160 }) {
  const [focus, setFocus] = React.useState(false);
  const len = value.length;
  const bc = error ? "var(--error)" : focus ? "var(--primary)" : "var(--outline)";
  return (
    <div style={{ width: "100%", opacity: disabled ? 0.38 : 1 }}>
      <div style={{ position: "relative", border: `${focus ? 2 : 1}px solid ${bc}`, borderRadius: "var(--radius-xs)", padding: focus ? 0 : 1 }}>
        <label style={{ position: "absolute", left: 12, top: -9, padding: "0 4px", background: "var(--surface)", color: error ? "var(--error)" : focus ? "var(--primary)" : "var(--on-surface-variant)", fontFamily: "var(--font-sans)", fontSize: "var(--body-small-size)", lineHeight: 1.2 }}>{t.problem}</label>
        <textarea
          ref={taRef} className="tr-area" value={value} disabled={disabled} maxLength={2000} placeholder={t.problemPh} aria-label={t.problem} aria-invalid={!!error}
          onChange={(e) => onChange(e.target.value)} onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
          style={{ display: "block", width: "100%", boxSizing: "border-box", minHeight: minH, resize: "vertical", border: "none", outline: "none", background: "transparent", color: "var(--on-surface)", fontFamily: "var(--font-sans)", fontSize: "var(--body-large-size)", lineHeight: "var(--body-large-line)", padding: "16px 15px" }}
        />
      </div>
      <div style={{ display: "flex", justifyContent: "space-between", gap: 12, padding: "4px 16px 0" }}>
        <CmpText variant="bodySmall" color={error ? "var(--error)" : "var(--on-surface-variant)"}>{error || t.problemHelp}</CmpText>
        {len >= 1700 && <CmpText variant="bodySmall" weight={600} color={len >= 2000 ? "var(--error)" : "var(--on-surface-variant)"} style={{ fontVariantNumeric: "tabular-nums", flex: "0 0 auto" }}>{len} / 2000</CmpText>}
      </div>
    </div>
  );
}

function TrAttachBlock({ t, open, setOpen }) {
  const vals = t.attachVals(TR_DEVICE);
  return (
    <div style={{ borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
      <button className="tr-focus" onClick={() => setOpen(!open)} aria-expanded={open}
        style={{ width: "100%", minHeight: 64, border: "none", background: "transparent", cursor: "pointer", display: "flex", alignItems: "center", gap: 12, padding: "8px 12px 8px 16px", textAlign: "left", color: "var(--on-surface)", fontFamily: "var(--font-sans)", borderRadius: "var(--r-card-medium-radius)" }}>
        <CmpIcon icon="outbox" size={24} color="var(--primary)" />
        <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
          <CmpText variant="titleSmall" weight={600}>{t.attachTitle}</CmpText>
          {!open && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{TR_DEVICE.model} · {TR_DEVICE.fw} · {t.attachSum}</CmpText>}
        </span>
        <span style={{ width: 48, height: 48, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
          <CmpIcon icon={open ? "expand_less" : "expand_more"} size={24} color="var(--on-surface-variant)" />
        </span>
      </button>
      {open && (
        <div style={{ padding: "0 16px 14px", display: "flex", flexDirection: "column", gap: 10 }}>
          {t.attachRows.map(([ic, label], i) => (
            <div key={ic} style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
              <CmpIcon icon={ic} size={20} color="var(--on-surface-variant)" style={{ marginTop: 2 }} />
              <span style={{ display: "flex", flexDirection: "column", minWidth: 0 }}>
                <CmpText variant="labelLarge" weight={600}>{label}</CmpText>
                <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{vals[i]}</CmpText>
              </span>
            </div>
          ))}
          <CmpText variant="bodySmall" color="var(--on-surface-variant)" style={{ textWrap: "pretty", paddingTop: 2 }}>{t.attachNote}</CmpText>
        </div>
      )}
    </div>
  );
}

const TR_ERR_ICON = { limit: "pending_actions", offline: "wifi_off", block: "bluetooth_disabled", other: "error" };

function TrSending({ t, stage, onCancel }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      {t.stages.map((s, i) => (
        <div key={i} style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 32 }}>
          <span style={{ width: 24, height: 24, display: "grid", placeItems: "center" }}>
            {i < stage ? <CmpIcon icon="check_circle" size={24} color="var(--success)" />
              : i === stage ? <CmpCircularProgressIndicator size={22} strokeWidth={3} />
              : <CmpIcon icon="radio_button_unchecked" size={24} color="var(--outline)" />}
          </span>
          <CmpText variant="bodyLarge" weight={i === stage ? 600 : 400} color={i > stage ? "var(--on-surface-variant)" : "var(--on-surface)"}>{s}</CmpText>
        </div>
      ))}
      <div style={{ display: "flex", alignItems: "center", gap: 12, justifyContent: "space-between" }}>
        <CmpText variant="bodySmall" color="var(--on-surface-variant)" style={{ textWrap: "pretty" }}>{t.cancelNote}</CmpText>
        <CmpOutlinedButton text={t.cancel} onClick={onCancel} style={{ height: 48, flex: "0 0 auto" }} />
      </div>
    </div>
  );
}

function TrSuccess({ t, no, phone, onDone }) {
  return (
    <div role="status" style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 20, padding: "32px 24px", textAlign: "center" }}>
      <div style={{ width: 104, height: 104, borderRadius: "50%", background: "color-mix(in srgb, var(--success) 24%, var(--surface-container-high))", display: "grid", placeItems: "center" }}>
        <CmpIcon icon="check_circle" size={56} color="var(--success)" />
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8, maxWidth: 360 }}>
        <CmpText variant="headlineSmall" weight={600} align="center">{t.okTitle(no)}</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)" align="center" style={{ textWrap: "pretty" }}>{t.okText(phone)}</CmpText>
      </div>
      <CmpButton text={t.done} onClick={onDone} style={{ height: 56, minWidth: 200 }} />
    </div>
  );
}

// ── Форма «Новая заявка» ─────────────────────────────────────────────────────
// kind: "page" (телефон, полноэкранная) | "dialog" (широкое окно) | "panel" (split, справа от списка)
function TrForm({ kind, t, platform, values, setValues, attempted, phase, stage, errorKind, newNo, origin,
  onSubmit, onCancelSend, onDone, onRequestClose, onToList, onErrorAction, focusTick, attachOpen, setAttachOpen }) {
  const taRef = React.useRef(null);
  const errs = attempted ? trValidate(values, t) : {};
  const sending = phase === "sending";
  const set = (k) => (v) => setValues((s) => ({ ...s, [k]: v }));
  const desktop = platform === "desktop";

  React.useEffect(() => {
    if ((kind !== "panel" || focusTick > 0) && values.name && values.phone && phase === "edit" && taRef.current) taRef.current.focus();
  }, [focusTick]);

  const onKey = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") { e.preventDefault(); if (!sending && phase !== "success") onSubmit(); }
    if (e.key === "Escape" && onRequestClose) { e.preventDefault(); onRequestClose(); }
  };

  const header = kind === "page" ? (
    <CmpTopAppBar title={t.formTitle} style={{ flex: "0 0 auto" }}
      navigationIcon={<CmpIconButton icon="Close" contentDescription={t.close} onClick={onRequestClose} />} />
  ) : (
    <div style={{ flex: "0 0 auto", display: "flex", alignItems: "flex-start", gap: 8, padding: kind === "dialog" ? "20px 12px 8px 24px" : "20px 24px 8px" }}>
      <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 4 }}>
        <CmpText variant="headlineSmall" weight={600}>{t.formTitle}</CmpText>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{t.formSub}</CmpText>
      </div>
      {kind === "dialog" && <CmpIconButton icon="Close" contentDescription={t.close} onClick={onRequestClose} />}
    </div>
  );

  if (phase === "success") {
    return (
      <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--surface)" }} onKeyDown={onKey}>
        {kind === "page" && header}
        <TrSuccess t={t} no={newNo} phone={values.phone} onDone={onDone} />
      </div>
    );
  }

  const errText = phase === "error" ? { limit: t.eLimit, offline: t.eOffline, block: t.eBlock, other: t.eOther }[errorKind] : null;
  const pad = kind === "page" ? 16 : 24;

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--surface)", color: "var(--on-surface)" }} onKeyDown={onKey}>
      {header}
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ padding: `16px ${pad}px 20px`, display: "flex", flexDirection: "column", gap: 20 }}>
          {origin && (
            <div style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "12px 14px", borderRadius: "var(--r-card-medium-radius)", background: "var(--secondary-container)", color: "var(--on-secondary-container)" }}>
              <CmpIcon icon="link" size={22} color="currentColor" style={{ marginTop: 1 }} />
              <CmpText variant="bodyMedium" color="currentColor" style={{ textWrap: "pretty" }}>{t.fromNote(origin)}</CmpText>
            </div>
          )}
          <CmpTextField label={t.name} value={values.name} onChange={set("name")} disabled={sending} isError={!!errs.name} supportingText={errs.name} singleLine
            leadingIcon={<CmpIcon icon="person" size={22} color="currentColor" />} autoComplete="name" />
          <CmpTextField label={t.phone} value={values.phone} onChange={set("phone")} disabled={sending} isError={!!errs.phone} supportingText={errs.phone} singleLine type="tel" inputMode="tel"
            leadingIcon={<CmpIcon icon="call" size={22} color="currentColor" />} autoComplete="tel" />
          <TrProblemField t={t} value={values.problem} onChange={set("problem")} error={errs.problem} disabled={sending} taRef={taRef} minH={kind === "page" ? 168 : 200} />
          <TrAttachBlock t={t} open={attachOpen} setOpen={setAttachOpen} />
        </div>
      </div>
      <div style={{ flex: "0 0 auto", position: "relative", borderTop: "1px solid var(--border-highlight)", background: "var(--surface)", padding: `12px ${pad}px ${kind === "page" ? 16 : 20}px`, display: "flex", flexDirection: "column", gap: 10 }}>
        {sending && <div style={{ position: "absolute", left: 0, right: 0, top: -1 }}><CmpLinearProgressIndicator /></div>}
        {errText && (
          <div role="alert" style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "12px 8px 8px 14px", borderRadius: "var(--r-card-medium-radius)", background: "var(--error-container)", color: "var(--on-error-container)", flexWrap: "wrap" }}>
            <CmpIcon icon={TR_ERR_ICON[errorKind]} size={24} color="currentColor" style={{ marginTop: 1 }} />
            <CmpText variant="bodyLarge" weight={500} color="currentColor" style={{ flex: "1 1 0", minWidth: 0, textWrap: "pretty", paddingTop: 1, paddingRight: 6 }}>{errText}</CmpText>
            <div style={{ flexBasis: "100%", height: 0 }} />
            <TrTextBtn icon={errorKind === "limit" ? "list" : "refresh"} text={errorKind === "limit" ? t.toList : t.retry} onClick={errorKind === "limit" ? onToList : onErrorAction}
              style={{ height: 48, color: "var(--on-error-container)", marginLeft: "auto" }} />
          </div>
        )}
        {sending ? <TrSending t={t} stage={stage} onCancel={onCancelSend} /> : (
          errorKind !== "limit" || phase !== "error" ? (
            <CmpButton icon="send" text={t.send} fullWidth onClick={onSubmit} style={{ height: 56 }} />
          ) : null
        )}
        {desktop && !sending && (
          <CmpText variant="labelMedium" color="var(--on-surface-variant)" align="center">{t.kbSend}{onRequestClose && kind !== "panel" ? ` · ${t.kbEsc}` : ""}</CmpText>
        )}
      </div>
    </div>
  );
}

// ── Контроллер экрана ─────────────────────────────────────────────────────────
function trInit(scenario, lang, prefill) {
  const s = {
    items: TR_ITEMS, listState: "ready", formOpen: false, phase: "edit", stage: 0, errorKind: null, newNo: null,
    values: { name: TR_PROFILE.name[lang], phone: TR_PROFILE.phone, problem: "" }, attempted: false,
    confirm: null, busy: null, draftAsk: false, pageFail: false,
  };
  const withText = { ...s.values, problem: TR_SAMPLE[lang] };
  switch (scenario) {
    case "empty": s.items = []; break;
    case "loading": s.listState = "loading"; break;
    case "error": s.listState = "error"; break;
    case "pageError": s.pageFail = true; break;
    case "form": s.formOpen = true; break;
    case "formErrors": s.formOpen = true; s.attempted = true; s.values = { name: "", phone: "+7 912 34", problem: "" }; break;
    case "sending": s.formOpen = true; s.values = withText; s.phase = "sending"; s.stage = 1; break;
    case "success": s.formOpen = true; s.values = withText; s.phase = "success"; s.newNo = 10483; break;
    case "limit": s.formOpen = true; s.values = withText; s.phase = "error"; s.errorKind = "limit"; break;
    case "confirm": s.confirm = { kind: "cancel", no: 10482 }; break;
    case "busy": s.busy = { no: 10471, kind: "close" }; break;
    case "draft": s.formOpen = true; s.values = withText; s.draftAsk = true; break;
  }
  if (prefill && prefill.text) { s.formOpen = true; s.values = { ...s.values, problem: prefill.text + " " }; }
  return s;
}

function TechRequestsApp({ lang = "ru", platform = "mobile", grouping = "sections", wide = "split", scenario = "list", outcome = "ok", freeze = false, prefill = null }) {
  const t = TR_I18N[lang];
  const { useState, useRef, useEffect } = React;
  const init = React.useMemo(() => trInit(scenario, lang, prefill), []);
  const [origin, setOrigin] = useState(prefill && prefill.from ? prefill.from : null);
  const [rootRef, mode, width] = useTrMode();
  const [items, setItems] = useState(init.items);
  const [listState, setListState] = useState(init.listState);
  const [formOpen, setFormOpen] = useState(init.formOpen);
  const [phase, setPhase] = useState(init.phase);
  const [stage, setStage] = useState(init.stage);
  const [errorKind, setErrorKind] = useState(init.errorKind);
  const [newNo, setNewNo] = useState(init.newNo);
  const [values, setValues] = useState(init.values);
  const [attempted, setAttempted] = useState(init.attempted);
  const [confirm, setConfirm] = useState(init.confirm);
  const [busy, setBusy] = useState(init.busy);
  const [draftAsk, setDraftAsk] = useState(init.draftAsk);
  const [snack, setSnack] = useState(null);
  const [highlight, setHighlight] = useState(null);
  const [filter, setFilter] = useState("active");
  const [loadingMore, setLoadingMore] = useState(false);
  const [pageFailed, setPageFailed] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [focusTick, setFocusTick] = useState(0);
  const [attachOpen, setAttachOpen] = useState(false);
  const pageFailRef = useRef(init.pageFail);
  const outcomeRef = useRef(outcome);
  const noRef = useRef(10483);
  const listRef = useRef(null);
  const colRef = useRef(null);
  useTrFlip(colRef, !freeze);
  const timers = useRef([]);
  const sendTimers = useRef([]);
  const later = (fn, ms, bag = timers) => { if (freeze) return; const id = setTimeout(fn, ms); bag.current.push(id); };
  useEffect(() => () => { timers.current.forEach(clearTimeout); sendTimers.current.forEach(clearTimeout); }, []);

  const split = wide === "split" && (mode === "expanded" || (platform === "desktop" && width >= 780));
  const compact = mode === "compact";
  const formVisible = split || formOpen;

  useEffect(() => { if (listState === "loading") later(() => setListState("ready"), 1600); }, [listState]);
  useEffect(() => { if (scenario === "sending" && !freeze) { setStage(0); runSend(true); } }, []);
  useEffect(() => { setAttachOpen(split); }, [split]);

  const showSnack = (msg) => { setSnack(msg); later(() => setSnack((s) => (s === msg ? null : s)), 4000); };

  function runSend(skipValidate) {
    if (!skipValidate) {
      setAttempted(true);
      const e = trValidate(values, t);
      if (e.name || e.phone || e.problem) return;
    }
    sendTimers.current.forEach(clearTimeout); sendTimers.current = [];
    const out = outcomeRef.current === "actionFail" ? "ok" : outcomeRef.current;
    setPhase("sending"); setStage(0); setErrorKind(null);
    const fail = (k) => { setPhase("error"); setErrorKind(k); outcomeRef.current = "ok"; };
    if (out === "block") { later(() => fail("block"), 1700, sendTimers); return; }
    later(() => setStage(1), 1700, sendTimers);
    later(() => {
      if (out !== "ok") return fail(out);
      const no = noRef.current++;
      setNewNo(no); setPhase("success");
      setItems((xs) => [{ no, status: "OPEN", at: "2026-11-13T15:12", name: { ru: values.name, en: values.name }, phone: values.phone, text: { ru: values.problem, en: values.problem } }, ...xs]);
    }, 3000, sendTimers);
  }
  const cancelSend = () => {
    sendTimers.current.forEach(clearTimeout); sendTimers.current = [];
    setPhase("edit"); setStage(0); showSnack(t.snackSendStopped);
  };
  const resetForm = () => {
    setPhase("edit"); setStage(0); setErrorKind(null); setAttempted(false); setOrigin(null);
    setValues({ name: TR_PROFILE.name[lang], phone: TR_PROFILE.phone, problem: "" });
  };
  const onDone = () => {
    const no = newNo;
    resetForm(); setFormOpen(false); setFilter("active");
    setHighlight(no); later(() => setHighlight(null), 4000);
    if (listRef.current) listRef.current.scrollTop = 0;
  };
  const requestClose = () => {
    if (phase === "sending") return;
    if (phase === "success") return onDone();
    if (values.problem.trim()) return setDraftAsk(true);
    resetForm(); setFormOpen(false);
  };
  const discard = () => { setDraftAsk(false); resetForm(); setFormOpen(false); };
  const openNew = () => { if (split) setFocusTick((n) => n + 1); else setFormOpen(true); };
  const toList = () => { setFormOpen(false); setPhase("edit"); setErrorKind(null); setFilter("active"); };

  const doAction = () => {
    const { kind, no } = confirm;
    setConfirm(null); setBusy({ no, kind });
    later(() => {
      setBusy(null);
      if (outcome === "actionFail") return showSnack(kind === "cancel" ? t.snackCancelFail : t.snackCloseFail);
      setItems((xs) => xs.map((r) => (r.no === no ? { ...r, status: kind === "cancel" ? "CANCELED_BY_USER" : "FINISHED_BY_USER" } : r)));
      showSnack(kind === "cancel" ? t.snackCanceled : t.snackClosed);
    }, 1300);
  };

  const loadMore = () => {
    if (loadingMore || !hasMore || freeze) return;
    setPageFailed(false); setLoadingMore(true);
    later(() => {
      setLoadingMore(false);
      if (pageFailRef.current) { pageFailRef.current = false; setPageFailed(true); return; }
      setItems((xs) => [...xs, ...TR_OLDER]); setHasMore(false);
    }, 1400);
  };
  const sentinelRef = useRef(null);
  const loadMoreRef = useRef(loadMore);
  loadMoreRef.current = loadMore;
  useEffect(() => {
    const el = sentinelRef.current;
    if (!el || !listRef.current) return;
    const io = new IntersectionObserver((es) => { if (es[0].isIntersecting) loadMoreRef.current(); }, { root: listRef.current, rootMargin: "80px" });
    io.observe(el);
    return () => io.disconnect();
  });
  const onScroll = undefined;

  const act = items.filter((r) => TR_ACTIVE.includes(r.status));
  const fin = items.filter((r) => !TR_ACTIVE.includes(r.status));
  const card = (r) => (
    <TrCard key={r.no} r={r} t={t} lang={lang} busy={busy && busy.no === r.no ? busy.kind : null} highlight={highlight === r.no}
      onClose={() => setConfirm({ kind: "close", no: r.no })} onCancel={() => setConfirm({ kind: "cancel", no: r.no })} />
  );
  const filtered = filter === "active" ? act : filter === "finished" ? fin : items;
  const empty = listState === "ready" && items.length === 0;

  const colMax = split ? 680 : mode === "expanded" ? 840 : mode === "medium" ? 720 : "none";
  const pad = compact ? 16 : 24;
  const panelW = width >= 1200 ? 480 : width >= 960 ? 440 : 380;

  const list = (
    <div ref={listRef} onScroll={onScroll} style={{ position: "absolute", inset: 0, overflow: "auto", display: "flex", flexDirection: "column" }}>
      <div ref={colRef} style={{ position: "relative", maxWidth: colMax, width: "100%", margin: "0 auto", padding: `${compact ? 8 : 16}px ${pad}px ${compact ? 104 : 32}px`, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 12, flex: empty || listState === "error" ? 1 : "0 0 auto" }}>
        {listState === "loading" && <>{[0, 1, 2, 3].map((i) => <div key={i} data-tr-anim={`s${i}`}><TrCardSkeleton h={i === 0 ? 196 : 160} /></div>)}</>}
        {listState === "error" && <TrError t={t} onRetry={() => setListState("loading")} />}
        {empty && <TrEmpty t={t} onNew={openNew} />}
        {listState === "ready" && !empty && grouping === "sections" && (
          <>
            {act.length > 0 && <TrSectionHead anim="h-act" label={t.active} count={act.length} />}
            {act.map(card)}
            {fin.length > 0 && <TrSectionHead anim="h-fin" label={t.finished} count={fin.length} />}
            {fin.map(card)}
          </>
        )}
        {listState === "ready" && !empty && grouping === "chips" && (
          <>
            <div data-tr-anim="chips"><TrChips t={t} filter={filter} setFilter={setFilter} counts={{ active: act.length, finished: fin.length }} /></div>
            {filtered.map(card)}
            {filtered.length === 0 && (
              <div data-tr-anim={`none-${filter}`} style={{ padding: "40px 16px", display: "flex", flexDirection: "column", alignItems: "center", gap: 12 }}>
                <CmpIcon icon="inbox" size={40} color="var(--on-surface-variant)" />
                <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{filter === "active" ? t.noActive : t.noFinished}</CmpText>
              </div>
            )}
          </>
        )}
        {listState === "ready" && !empty && (
          <>
            <div data-tr-anim={loadingMore ? "f-load" : pageFailed ? "f-err" : "f"}><TrListFooter t={t} loadingMore={loadingMore} pageFailed={pageFailed} onRetry={loadMore} /></div>
            {hasMore && !pageFailed && <div ref={sentinelRef} style={{ height: 1 }} />}
          </>
        )}
      </div>
    </div>
  );

  const formProps = {
    t, platform, values, setValues, attempted, phase, stage, errorKind, newNo, focusTick, attachOpen, setAttachOpen, origin,
    onSubmit: () => runSend(false), onCancelSend: cancelSend, onDone: split ? () => { const no = newNo; resetForm(); setHighlight(no); later(() => setHighlight(null), 4000); } : onDone,
    onRequestClose: split ? undefined : requestClose, onToList: toList, onErrorAction: () => runSend(false),
  };

  const confirmDlg = confirm && (
    <CmpDialog
      open
      onDismissRequest={() => setConfirm(null)}
      icon={confirm.kind === "cancel" ? "block" : "check_circle"}
      title={confirm.kind === "cancel" ? t.cancelTitle(confirm.no) : t.closeTitle(confirm.no)}
      text={confirm.kind === "cancel" ? t.cancelText : t.closeText}
      dismissButton={<TrTextBtn text={confirm.kind === "cancel" ? t.keep : t.backBtn} onClick={() => setConfirm(null)} style={{ height: 48 }} />}
      confirmButton={<TrTextBtn text={confirm.kind === "cancel" ? t.cancelReq : t.close} onClick={doAction} style={{ height: 48, color: confirm.kind === "cancel" ? "var(--error)" : undefined }} />}
    />
  );

  return (
    <div ref={rootRef} data-screen-label="Заявки на техпомощь" style={{ position: "relative", height: "100%", width: "100%", display: "flex", flexDirection: "column", overflow: "hidden", background: "var(--background)", color: "var(--on-background)", fontFamily: "var(--font-sans)" }}>
      <CmpTopAppBar
        title={t.title}
        subtitle={`${TR_DEVICE.model} · ${TR_DEVICE.transport}`}
        style={{ flex: "0 0 auto" }}
        navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={t.back} onClick={() => window.psGo && window.psGo(compact ? "advanced-mount" : platform === "desktop" ? "advanced-mount-desktop" : "advanced-mount-tablet")} />}
        actions={!compact && !split && !empty && listState === "ready" ? <CmpButton icon="add" text={t.newReq} onClick={openNew} style={{ height: 48, marginRight: 8, padding: "0 22px 0 16px" }} /> : null}
      />
      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <div style={{ flex: 1, minWidth: 0, position: "relative" }}>{list}</div>
        {split && (
          <div style={{ width: panelW, flex: "0 0 auto", borderLeft: "1px solid var(--border-highlight)", background: "var(--surface)" }}>
            <TrForm kind="panel" {...formProps} />
          </div>
        )}
      </div>

      {compact && listState === "ready" && !empty && !formOpen && (
        <CmpFloatingActionButton contentDescription={t.newReq} onClick={openNew}
          style={{ position: "absolute", right: 16, bottom: snack ? 96 : 20, width: "auto", height: 56, padding: "0 22px 0 18px", transition: "bottom .2s", zIndex: 5 }}>
          <span style={{ position: "relative", display: "flex", alignItems: "center", gap: 10 }}>
            <CmpIcon icon="add" size={24} color="currentColor" />
            <CmpText variant="labelLarge" weight={700} color="currentColor">{t.newReq}</CmpText>
          </span>
        </CmpFloatingActionButton>
      )}

      {compact && formOpen && (
        <div style={{ position: "absolute", inset: 0, zIndex: 30 }}>
          <TrForm kind="page" {...formProps} />
        </div>
      )}
      {!compact && !split && formOpen && (
        <div onClick={requestClose} style={{ position: "absolute", inset: 0, zIndex: 30, background: "color-mix(in srgb, var(--scrim) 40%, transparent)", display: "grid", placeItems: "center", padding: 24 }}>
          <div onClick={(e) => e.stopPropagation()} role="dialog" aria-label={t.formTitle}
            style={{ width: "min(640px, 100%)", height: "min(780px, 100%)", borderRadius: "var(--r-card-medium-radius)", overflow: "hidden", background: "var(--surface)", boxShadow: "var(--elevation-3)" }}>
            <TrForm kind="dialog" {...formProps} />
          </div>
        </div>
      )}

      {confirmDlg}
      {draftAsk && (
        <CmpDialog open onDismissRequest={() => setDraftAsk(false)} title={t.draftTitle} text={t.draftText}
          dismissButton={<TrTextBtn text={t.cont} onClick={() => setDraftAsk(false)} style={{ height: 48 }} />}
          confirmButton={<TrTextBtn text={t.del} onClick={discard} style={{ height: 48, color: "var(--error)" }} />} />
      )}
      {snack && (
        <div style={{ position: "absolute", left: 16, right: split ? panelW + 16 : 16, bottom: 16, zIndex: 50, display: "flex", justifyContent: "center", pointerEvents: "none" }}>
          <div style={{ width: "100%", maxWidth: 560, pointerEvents: "auto" }}>
            <CmpSnackbar message={snack} withDismissAction onDismiss={() => setSnack(null)} />
          </div>
        </div>
      )}
    </div>
  );
}

window.TechRequestsApp = TechRequestsApp;
window.TR_I18N = TR_I18N;
