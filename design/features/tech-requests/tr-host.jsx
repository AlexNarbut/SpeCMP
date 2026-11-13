// tr-host.jsx — общая демо-панель для хостов «Заявок на техпомощь» (не часть экрана).
const TR_SCENARIOS = [
  ["list", "Список"], ["empty", "Пусто"], ["loading", "Загрузка"], ["error", "Ошибка"], ["pageError", "Ошибка дозагрузки"],
  ["form", "Форма"], ["formErrors", "Ошибки полей"], ["sending", "Отправка"], ["success", "Успех"], ["limit", "Лимит заявок"],
  ["confirm", "Подтверждение"], ["busy", "Действие в процессе"], ["draft", "Черновик"],
];
const TR_OUTCOMES = [["ok", "ок"], ["limit", "лимит"], ["offline", "нет сети"], ["block", "блок не читается"], ["other", "прочее"], ["actionFail", "отмена/закрытие не удались"]];

function TrSeg({ value, onChange, options }) {
  return (
    <div className="seg">
      {options.map(([k, label]) => (
        <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{label}</button>
      ))}
    </div>
  );
}

function useTrHost(defaults = {}) {
  const read = () => { try { return JSON.parse(localStorage.getItem("tr-host") || "{}"); } catch (e) { return {}; } };
  const [s, setS] = React.useState(() => ({ dark: false, lang: "ru", grouping: "sections", wide: "split", scenario: "list", outcome: "ok", ...defaults, ...read() }));
  const [key, setKey] = React.useState(0);
  // ?new=1&from=…&text=… — переход «Заявка на техпомощь» из обхода / Clone / ассистента.
  const [prefill] = React.useState(() => { const q = new URLSearchParams(location.search); return q.get("new") ? { from: q.get("from") || "", text: q.get("text") || "" } : null; });
  React.useEffect(() => { if (prefill) setS((x) => ({ ...x, scenario: "form" })); }, []);
  const set = (k) => (v) => { setS((x) => { const n = { ...x, [k]: v }; localStorage.setItem("tr-host", JSON.stringify(n)); return n; }); setKey((n) => n + 1); };
  return [{ ...s, prefill: key === 0 ? prefill : null }, set, key, () => setKey((n) => n + 1)];
}

function TrToolbar({ s, set, reset, showWide, links }) {
  return (
    <div className="toolbar">
      <TrSeg value={s.dark ? "d" : "l"} onChange={(v) => set("dark")(v === "d")} options={[["l", "Светлая"], ["d", "Тёмная"]]} />
      <TrSeg value={s.lang} onChange={set("lang")} options={[["ru", "RU"], ["en", "EN"]]} />
      <TrSeg value={s.grouping} onChange={set("grouping")} options={[["sections", "Секции"], ["chips", "Фильтр-чипы"]]} />
      {showWide && <TrSeg value={s.wide} onChange={set("wide")} options={[["split", "Список + панель"], ["dialog", "Список + диалог"]]} />}
      <div className="break" />
      <TrSeg value={s.scenario} onChange={set("scenario")} options={TR_SCENARIOS} />
      <div className="break" />
      <span className="lbl">Ответ сервера:</span>
      <TrSeg value={s.outcome} onChange={set("outcome")} options={TR_OUTCOMES} />
      <button className="link" onClick={reset}>↻ Сброс</button>
      {links.map(([k, label]) => <button key={k} className="link" onClick={() => window.psGo(k)}>{label}</button>)}
    </div>
  );
}
window.TrToolbar = TrToolbar;
window.useTrHost = useTrHost;
window.TR_SCENARIOS = TR_SCENARIOS;
