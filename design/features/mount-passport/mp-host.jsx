// mp-host.jsx — демо-панель хостов «Паспорта монтажа» (не часть экрана).
const MP_SCREENS = [["list", "Список"], ["lookup", "Блок (без блока)"], ["read", "Считывание"], ["editor", "Предотчёт"], ["ready", "Паспорт готов"]];
const MP_LIST = [["list", "Список · 10+"], ["one", "1 паспорт"], ["loading", "Загрузка"], ["empty", "Пусто"], ["search", "Поиск"], ["noresults", "Поиск: нет"]];
const MP_LOOKUP = [["live", "Ввод"], ["errEmpty", "Ошибка: пусто"], ["errDigits", "Ошибка: не цифры"], ["found", "Найден"], ["notfound", "Не найден"], ["offline", "Нет интернета"]];
const MP_READ = [["live", "▶ Живой"], ["run1", "Этап 1"], ["run3", "Этап 3"], ["done", "Всё прочитано"], ["partFail", "Этап не прочитан"], ["blockFail", "Блок не прочитан"], ["lost", "Связь потеряна"], ["offline", "Блок не на связи"], ["usb", "USB"], ["setRun", "Настройки: идёт"], ["setSkip", "Пропущены"], ["setNa", "Недоступны"]];
const MP_ED = [["fast", "Быстрый монтаж"], ["ext", "Расширенный"], ["partial", "Часть не прочитана"], ["noblock", "Без блока"]];
const MP_PH = [["none", "Фото: нет"], ["three", "3 фото"], ["eight", "8 фото"], ["loading", "Загружается"]];
const MP_XF = [["none", "—"], ["pick", "Выбор для экспорта"], ["export", "Экспорт: 1"], ["exportMany", "Экспорт: 3"], ["import", "Импорт"]];
const MP_IMP = [["ok", "Импорт: новые"], ["dupes", "Есть дубликаты"], ["bad", "Не тот файл"]];
const MP_RDY = [["live", "▶ Формируем → готово"], ["forming", "Формируем"], ["ready", "Готово · 3 стр."], ["ready1", "Готово · 1 стр."], ["error", "Ошибка"], ["saveFail", "Не удалось сохранить"]];

function MpSeg({ value, onChange, options }) {
  return <div className="seg">{options.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
function useMpHost(defaults = {}) {
  const q = new URLSearchParams(location.search);
  const fromUrl = {};
  if (q.get("entry")) { fromUrl.entry = q.get("entry"); fromUrl.screen = "list"; }
  const read = () => { try { return JSON.parse(localStorage.getItem("mp-host2") || "{}"); } catch (e) { return {}; } };
  const [s, setS] = React.useState(() => ({ dark: false, entry: "archive", screen: "list", list: "list", connected: false, lookup: "live", read: "live", ed: "fast", rdy: "live", ph: "none", cam: false, xf: "none", imp: "dupes", ...defaults, ...read(), ...fromUrl }));
  const [key, setKey] = React.useState(0);
  React.useEffect(() => { document.body.classList.toggle("page-dark", s.dark); }, [s.dark]);
  const set = (k) => (v) => { setS((x) => { const n = { ...x, [k]: v }; localStorage.setItem("mp-host2", JSON.stringify(n)); return n; }); setKey((n) => n + 1); };
  const dev = (() => { const d = q.get("dev"); if (!d) return undefined; const [name, id, tr] = d.split("|"); return { name, id, transport: tr === "usb" ? "usb" : "bt" }; })();
  return { s, set, key, reset: () => setKey((n) => n + 1), dev };
}
function MpToolbar({ h, extra, links = [] }) {
  const { s, set, reset } = h;
  return (
    <div className="toolbar">
      <MpSeg value={s.dark ? "d" : "l"} onChange={(v) => set("dark")(v === "d")} options={[["l", "Светлая"], ["d", "Тёмная"]]} />
      <MpSeg value={s.entry} onChange={set("entry")} options={[["archive", "Архив (главная)"], ["mount", "Расширенный монтаж"]]} />
      {s.entry === "archive" && <MpSeg value={s.connected ? "y" : "n"} onChange={(v) => set("connected")(v === "y")} options={[["n", "Блок не подключён"], ["y", "Подключён DX-4GS"]]} />}
      {extra}
      <div className="break" />
      <MpSeg value={s.screen} onChange={set("screen")} options={MP_SCREENS} />
      <div className="break" />
      {s.screen === "list" && <MpSeg value={s.list} onChange={set("list")} options={MP_LIST} />}
      {s.screen === "list" && <React.Fragment><div className="break" /><span style={{ fontSize: 12, fontWeight: 600, color: "#4A5874" }}>Перенос:</span><MpSeg value={s.xf} onChange={set("xf")} options={MP_XF} /><MpSeg value={s.imp} onChange={set("imp")} options={MP_IMP} /><MpSeg value={s.bk === false ? "n" : "y"} onChange={(v) => set("bk")(v === "y")} options={[["y", "Напоминание о копии"], ["n", "Без напоминания"]]} /></React.Fragment>}
      {s.screen === "lookup" && <MpSeg value={s.lookup} onChange={set("lookup")} options={MP_LOOKUP} />}
      {s.screen === "read" && <MpSeg value={s.read} onChange={set("read")} options={MP_READ} />}
      {s.screen === "editor" && <MpSeg value={s.ed} onChange={set("ed")} options={MP_ED} />}
      {s.screen === "editor" && <MpSeg value={s.ph} onChange={set("ph")} options={MP_PH} />}
      {s.screen === "editor" && <MpSeg value={s.nospace ? "s" : s.cam ? "y" : "n"} onChange={(v) => { set("cam")(v === "y"); set("nospace")(v === "s"); }} options={[["n", "Камера: есть доступ"], ["y", "Нет доступа к камере"], ["s", "Нет места на устройстве"]]} />}
      {s.screen === "editor" && s.entry === "archive" && <span style={{ fontSize: 12, color: "#4A5874", maxWidth: 260 }}>Архив + «Блок не подключён» → правятся только ручные поля; «Без блока» + «Подключён» → «Считать блок»</span>}
      {s.screen === "ready" && <MpSeg value={s.rdy} onChange={set("rdy")} options={MP_RDY} />}
      <button className="link" onClick={reset}>↻ Сброс</button>
      {links.map(([k, l]) => <button key={k} className="link" onClick={() => window.psGo(k)}>{l}</button>)}
    </div>
  );
}
function MpApp({ h, platform }) {
  const { s, key, dev } = h;
  return <window.PassportApp key={key + s.screen + s.entry} platform={platform} entry={s.entry} dev={dev} listState={s.list} connected={s.connected} lookup={s.lookup}
    start={s.screen} readState={s.read} scenario={s.ed} ready={s.rdy} photosInit={s.ph} cameraDenied={s.cam} transfer={s.screen === "list" && s.xf !== "none" ? s.xf : null} importCase={s.imp} backupDue={s.bk !== false} noSpace={!!s.nospace} />;
}
Object.assign(window, { useMpHost, MpToolbar, MpApp, MpSeg });
