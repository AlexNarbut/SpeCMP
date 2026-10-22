
// Панель сценариев для макетов «Настройки устройства» (общая для mobile/tablet/desktop).
(function () {
const SC = [["", "Список"], ["startId:3.2", "Раздел 3.2"], ["startId:4", "Входы"], ["startId:5", "Выходы"], ["edit:3.2.4", "Число"], ["edit:2.3", "Дробное"], ["edit:1.3", "Выбор (27)"], ["edit:3.2.6", "Выбор (длинные)"], ["edit:1.1", "Текст"], ["edit:2.5", "Время"],
  ["edit:2.4.2", "Расписание"], ["edit:3.3.3", "Сирена"], ["edit:6.1", "CAN-код"], ["edit:4.2", "Вход"], ["edit:5.1", "Выход"]];
const FL = [["loading", "S1 Чтение"], ["error", "S2 Ошибка чтения"], ["write-progress", "S4 Запись"], ["write-ok", "S5 Успех"], ["write-error", "S6 Ошибка записи"], ["exit", "S8 Выход"],
  ["lost", "S7 Обрыв связи"], ["reset", "S9 Сброс"], ["reset-progress", "S9 Прогресс"], ["can", "S10 CAN"], ["empty", "S11 Пустой раздел"], ["menu", "Меню ⋯"], ["search", "S3 Поиск"], ["search-busy", "S3 Идёт"], ["search-empty", "S3 Пусто"], ["info", "ⓘ"]];
function Seg({ items, value, onChange }) {
  return <div className="seg">{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
function useDsCtl() {
  const [dark, setDark] = React.useState(false);
  const [sc, setSc] = React.useState(new URLSearchParams(location.search).get("s") || "");
  const [factory, setFactory] = React.useState(true);
  const [fail, setFail] = React.useState(false);
  const [edits, setEdits] = React.useState(true);
  const props = { canFactory: factory, failWrite: fail, seedEdits: edits };
  if (sc === "loading" || sc === "error") props.initialState = sc;
  else if (sc.startsWith("startId:")) props.startId = sc.slice(8);
  else if (sc) props.demo = sc;
  const bar = (
    <React.Fragment>
      <div className="toolbar">
        <Seg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(v) => setDark(v === "d")} />
        <Seg items={[["y", "Правки: есть"], ["n", "Правок нет"]]} value={edits ? "y" : "n"} onChange={(v) => setEdits(v === "y")} />
        <Seg items={[["y", "CAN: единая (блок.)"], ["n", "CAN: обычная"]]} value={factory ? "y" : "n"} onChange={(v) => setFactory(v === "y")} />
        <Seg items={[["n", "Запись: успех"], ["y", "Запись: ошибка"]]} value={fail ? "y" : "n"} onChange={(v) => setFail(v === "y")} />
      </div>
      <div className="toolbar"><Seg items={SC} value={sc} onChange={setSc} /></div>
      <div className="toolbar"><Seg items={FL} value={sc} onChange={setSc} /></div>
    </React.Fragment>
  );
  return { dark, props, bar, key: [sc, factory, fail, edits].join() };
}
window.useDsCtl = useDsCtl;
window.DsSeg = Seg;
})();
