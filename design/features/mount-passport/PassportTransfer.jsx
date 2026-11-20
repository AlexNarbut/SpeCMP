// PassportTransfer.jsx — экспорт/импорт паспортов архивом (перенос на другой компьютер, бэкап).
// Формат: обычный ZIP — открывается где угодно, PDF внутри виден сразу.
//   Паспорт_<№>_<авто>.pdf  — документ (всегда)
//   passport.json            — данные паспорта для импорта (всегда)
//   settings.dxs             — снимок настроек блока (если есть; по умолчанию включён)
//   photos/                  — фото в полном качестве, как хранятся в приложении: 2048 px, JPEG (по умолчанию выключено: в PDF фото уже есть)
// Несколько паспортов — по папке на паспорт внутри одного ZIP.
//
// PtExport({ items, platform, wide, onClose, onDone })
// PtImport({ platform, wide, scenario: "ok" | "dupes" | "bad", existing: [№…], onClose, onDone(newItems) })

const PT_DS = window.PandoraSpecDesignSystem_b8d6e1;
const PtText = (p) => <PT_DS.CmpText {...p} style={{ display: "block", ...p.style }} />;
const { CmpIcon: PtIcon, CmpButton: PtButton, CmpOutlinedButton: PtOutlined, CmpIconButton: PtIconBtn, CmpSwitch: PtSwitch, CmpRadioButton: PtRadio, CmpLinearProgressIndicator: PtLinear } = PT_DS;
const PtTextBtn = (p) => (PT_DS.CmpTextButton ? <PT_DS.CmpTextButton {...p} /> : <PtOutlined {...p} />);
const PT_EASE = "cubic-bezier(.23,1,.32,1)";
const ptMuted = "var(--foreground-on-dark-secondary-default)";

(function ptCss() {
  if (document.getElementById("pt-css")) return;
  const s = document.createElement("style");
  s.id = "pt-css";
  s.textContent =
    "@keyframes ptFade{from{opacity:0}to{opacity:1}}" +
    "@keyframes ptSheet{from{transform:translateY(100%)}to{transform:none}}" +
    "@keyframes ptPop{from{opacity:0;transform:scale(.96)}to{opacity:1;transform:none}}" +
    "@keyframes ptSpin{to{transform:rotate(360deg)}}" +
    ".pt-tap{transition:background-color 150ms ease}" +
    "@media (hover:hover) and (pointer:fine){.pt-tap:hover{background-color:var(--background-surface-ultra-highlight)}}" +
    "@media (prefers-reduced-motion:reduce){.pt-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

const ptMb = (x) => (x >= 10 ? `${Math.round(x)} МБ` : `${x.toFixed(1).replace(".", ",")} МБ`);
const ptPlural = (n, a, b, c) => { const m = n % 10, h = n % 100; return m === 1 && h !== 11 ? a : m >= 2 && m <= 4 && (h < 12 || h > 14) ? b : c; };
const ptPass = (n) => `${n} ${ptPlural(n, "паспорт", "паспорта", "паспортов")}`;
const ptSlug = (s) => (s || "без-авто").replace(/\s+/g, "-");

// Модальное окно: на compact — лист снизу, шире — диалог по центру.
function PtModal({ wide, title, onClose, children, footer, busy }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 60, display: "flex", alignItems: wide ? "center" : "flex-end", justifyContent: "center", padding: wide ? 24 : 0 }}>
      <div className="pt-anim" onClick={busy ? undefined : onClose} style={{ position: "absolute", inset: 0, background: "var(--background-overlay)", animation: "ptFade 200ms ease" }} />
      <div className="pt-anim" role="dialog" aria-label={title} style={{ position: "relative", width: "100%", maxWidth: wide ? 560 : "none", maxHeight: wide ? "100%" : "92%", display: "flex", flexDirection: "column", background: "var(--surface-container-lowest)", color: "var(--on-surface)", borderRadius: wide ? 20 : "20px 20px 0 0", boxShadow: "0 16px 48px rgba(10,26,58,.28)", animation: wide ? `ptPop 220ms ${PT_EASE}` : `ptSheet 320ms ${PT_EASE}`, overflow: "hidden" }}>
        {!wide && <div style={{ width: 36, height: 4, borderRadius: 2, background: "var(--border-tertiary)", margin: "10px auto 0", flex: "none" }} />}
        <div style={{ flex: "none", display: "flex", alignItems: "center", gap: 8, padding: wide ? "16px 12px 4px 24px" : "8px 12px 4px 20px" }}>
          <PtText variant="headline1" style={{ flex: 1 }}>{title}</PtText>
          {!busy && <PtIconBtn icon="close" contentDescription="Закрыть" onClick={onClose} />}
        </div>
        <div style={{ flex: 1, minHeight: 0, overflowY: "auto", padding: wide ? "4px 24px 16px" : "4px 20px 16px" }}>{children}</div>
        {footer && <div style={{ flex: "none", padding: wide ? "12px 24px 20px" : "12px 20px 16px", borderTop: "1px solid var(--border-highlight)", display: "flex", gap: 10, flexDirection: wide ? "row-reverse" : "column" }}>{footer}</div>}
      </div>
    </div>
  );
}

function PtSpinner({ size = 22 }) {
  return <span className="pt-anim" style={{ width: size, height: size, borderRadius: 999, border: "3px solid var(--brand-primary-highlight)", borderTopColor: "var(--primary)", animation: "ptSpin .8s linear infinite", flex: "none", boxSizing: "border-box", display: "inline-block" }} />;
}

// Строка состава архива.
function PtPart({ icon, title, sub, file, on, locked, onToggle, size }) {
  return (
    <label className={locked ? undefined : "pt-tap"} style={{ display: "flex", alignItems: "center", gap: 14, minHeight: 64, padding: "8px 8px 8px 12px", margin: "0 -12px", borderRadius: 12, cursor: locked ? "default" : "pointer" }}>
      <PtIcon icon={icon} size={26} color={on ? "var(--on-surface)" : ptMuted} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <PtText variant="headline3" color={on ? undefined : ptMuted}>{title}</PtText>
        <PtText variant="paragraph3" color={ptMuted} style={{ marginTop: 2 }}>{sub}</PtText>
        {file && <PtText variant="additional1" color={ptMuted} style={{ fontFamily: "var(--font-mono)", marginTop: 3 }}>{file}{size ? ` · ${size}` : ""}</PtText>}
      </div>
      {locked ? <PtIcon icon="lock" size={20} color={ptMuted} /> : PtSwitch ? <PtSwitch checked={on} onCheckedChange={onToggle} /> : <input type="checkbox" checked={on} onChange={(e) => onToggle(e.target.checked)} />}
    </label>
  );
}

// ── Экспорт ───────────────────────────────────────────────────────────────────
function PtExport({ items, platform = "mobile", wide, onClose, onDone }) {
  const many = items.length > 1;
  const withSettings = items.filter((x) => x.settings).length;
  const photoCount = items.reduce((a, x) => a + (x.photos != null ? x.photos : 4), 0);
  const [settings, setSettings] = React.useState(withSettings > 0);
  const [orig, setOrig] = React.useState(false);
  const [phase, setPhase] = React.useState("form"); // form | packing | done
  const [prog, setProg] = React.useState(0);
  const desk = platform === "desktop";
  const pdfMb = items.length * 1.4, setMb = settings ? withSettings * 0.06 : 0, origMb = orig ? photoCount * 0.5 : 0;
  const total = pdfMb + setMb + origMb + items.length * 0.01;
  const first = items[0] || {};
  const name = many ? `Паспорта_монтажа_20.11.2026_${items.length}шт.zip` : `Паспорт_${first.no}_${ptSlug(first.car)}.zip`;

  const start = (how) => {
    setPhase("packing"); setProg(0);
    let i = 0;
    const iv = setInterval(() => {
      i++; setProg(i / (items.length * 3));
      if (i >= items.length * 3) { clearInterval(iv); setPhase("done"); setTimeout(() => onDone && onDone({ how, name }), 700); }
    }, many ? 220 : 380);
  };
  const cur = Math.min(items.length, Math.floor(prog * items.length) + 1);

  const footer = phase === "form" ? (desk
    ? <React.Fragment><PtButton text="Сохранить файл" icon="download" type="Primary" size="L" onClick={() => start("save")} /><PtTextBtn text="Отмена" onClick={onClose} /></React.Fragment>
    : <React.Fragment><PtButton text="Поделиться" icon="share" type="Primary" size="XL" fullWidth onClick={() => start("share")} /><PtOutlined text="Сохранить файл" icon="download" fullWidth onClick={() => start("save")} /></React.Fragment>) : null;

  return (
    <PtModal wide={wide} title={many ? `Экспорт: ${ptPass(items.length)}` : "Экспорт паспорта"} onClose={onClose} footer={footer} busy={phase !== "form"}>
      {phase === "form" && (
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <PtText variant="paragraph2" color={ptMuted} style={{ textWrap: "pretty", marginBottom: 8 }}>
            Один ZIP-файл — перенести на другой компьютер или сохранить копию. Открыть его можно в Pandora Spec: «Паспорта монтажа → Импорт».
          </PtText>
          {many && (
            <div style={{ display: "flex", flexWrap: "wrap", gap: 6, marginBottom: 8 }}>
              {items.slice(0, 4).map((x) => <span key={x.no} style={{ padding: "6px 10px", borderRadius: 8, background: "var(--background-surface-highlight)" }}><PT_DS.CmpText variant="additional0">{x.car || "Без авто"}{x.plate ? " · " + x.plate : ""}</PT_DS.CmpText></span>)}
              {items.length > 4 && <span style={{ padding: "6px 10px" }}><PT_DS.CmpText variant="additional0" color={ptMuted}>и ещё {items.length - 4}</PT_DS.CmpText></span>}
            </div>
          )}
          <PtPart icon="picture_as_pdf" title="Документ PDF" sub="Как у клиента: с фото и подписями" file={many ? "<паспорт>/Паспорт.pdf" : `Паспорт_${first.no}.pdf`} size={ptMb(pdfMb)} on locked />
          <PtPart icon="data_object" title="Данные паспорта" sub="Чтобы паспорт открылся и правился на другом устройстве" file="passport.json" on locked />
          <PtPart icon="tune" title="Снимок настроек блока" on={settings} onToggle={setSettings} file="settings.dxs" size={settings && withSettings ? ptMb(setMb) : null}
            sub={withSettings ? (many ? `Есть у ${withSettings} из ${items.length}. Можно загрузить в блок через «Загрузить из…»` : "Можно загрузить в блок через «Загрузить из…»") : "У этого паспорта снимка нет"} locked={!withSettings} />
          <PtPart icon="photo_library" title="Фото в полном качестве (2048 px)" on={orig} onToggle={setOrig} file="photos/" size={orig ? ptMb(origMb) : null}
            sub={photoCount ? `В PDF фото уже есть. Отдельными файлами — если нужно крупно: ${photoCount} шт., ≈ ${ptMb(photoCount * 0.5)}` : "Фото нет"} locked={!photoCount} />
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginTop: 12, padding: "12px 14px", borderRadius: 12, background: "var(--background-surface-highlight)" }}>
            <PtIcon icon="folder_zip" size={24} color="var(--on-surface)" />
            <div style={{ flex: 1, minWidth: 0 }}>
              <PtText variant="paragraph3" style={{ fontFamily: "var(--font-mono)", overflowWrap: "anywhere" }}>{name}</PtText>
              <PtText variant="additional0" color={ptMuted} style={{ marginTop: 2 }}>≈ {ptMb(total)}</PtText>
            </div>
          </div>
        </div>
      )}
      {phase !== "form" && (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 14, padding: "24px 0 16px", textAlign: "center" }}>
          {phase === "packing"
            ? <PtSpinner size={40} />
            : <span className="pt-anim" style={{ width: 56, height: 56, borderRadius: 999, background: "var(--success-default)", display: "grid", placeItems: "center", animation: `ptPop 240ms ${PT_EASE}` }}><PtIcon icon="check" size={32} color="#fff" /></span>}
          <PtText variant="headline2" align="center">{phase === "packing" ? "Собираем архив…" : "Архив готов"}</PtText>
          <PtText variant="paragraph2" color={ptMuted} align="center">{phase === "packing" ? (many ? `Паспорт ${cur} из ${items.length}` : "PDF, данные и настройки") : name}</PtText>
          {phase === "packing" && <div style={{ width: "100%", maxWidth: 360 }}><PtLinear progress={prog} /></div>}
        </div>
      )}
    </PtModal>
  );
}

// ── Импорт ────────────────────────────────────────────────────────────────────
const PT_INCOMING = [
  { no: "20261105-771204-1", car: "Nissan X-Trail", plate: "С 112 АВ 199", model: "Pandora DX-91", devId: "7712040", serial: "DX91-0039-771204", date: "5 ноября, 16:20", status: "done", settings: true, doc: "full" },
  { no: "20261104-330915-1", car: "Renault Duster", plate: "У 870 ОК 750", model: "Pandora VX-4G", devId: "3309151", serial: "VX4G-0097-330915", date: "4 ноября, 11:05", status: "done", doc: "min" },
  { no: "20261103-560044-1", car: "Kia K5", plate: "", model: "Pandora DX-4GS", devId: "5600441", serial: "DX4GS-0018-560044", date: "3 ноября, 18:40", status: "done", settings: true, doc: "full" },
  { no: "20261118-118004-2", car: "Toyota RAV4", plate: "О 456 КМ 199", model: "Pandora DX-91", devId: "1180042", serial: "DX91-0031-220917", date: "18 ноября, 17:20", status: "done", settings: true, doc: "full" },
  { no: "20261102-090377-1", car: "Mitsubishi Outlander", plate: "Н 305 РА 77", model: "Pandora DX-91", devId: "0903771", serial: "DX91-0036-090377", date: "2 ноября, 09:55", status: "done", doc: "full" },
];

function PtImport({ platform = "mobile", wide, scenario = "ok", existing = [], onClose, onDone }) {
  const desk = platform === "desktop";
  const [phase, setPhase] = React.useState("pick"); // pick | reading | summary | bad | importing | done
  const [dupe, setDupe] = React.useState("skip");
  const [drag, setDrag] = React.useState(false);
  const file = scenario === "bad" ? "Фото_монтажа.zip" : "Паспорта_монтажа_19.11.2026_5шт.zip";
  const incoming = scenario === "dupes" ? PT_INCOMING : PT_INCOMING.filter((x) => !existing.includes(x.no));
  const isDupe = (x) => existing.includes(x.no);
  const dupes = incoming.filter(isDupe), fresh = incoming.filter((x) => !isDupe(x));
  const willAdd = fresh.length + (dupe === "replace" ? dupes.length : 0);

  const pick = () => { setPhase("reading"); setTimeout(() => setPhase(scenario === "bad" ? "bad" : "summary"), 1100); };
  const run = () => { setPhase("importing"); setTimeout(() => { setPhase("done"); setTimeout(() => onDone && onDone({ added: fresh, replaced: dupe === "replace" ? dupes : [] }), 800); }, 1500); };

  let body, footer = null;
  if (phase === "pick") {
    body = (
      <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <PtText variant="paragraph2" color={ptMuted} style={{ textWrap: "pretty" }}>Выберите ZIP-файл, сохранённый через «Экспорт» на другом устройстве. Паспорта появятся в архиве вместе с фото и снимками настроек.</PtText>
        <button className="pt-tap" onClick={pick} onDragOver={(e) => { e.preventDefault(); setDrag(true); }} onDragLeave={() => setDrag(false)} onDrop={(e) => { e.preventDefault(); setDrag(false); pick(); }}
          style={{ all: "unset", boxSizing: "border-box", minHeight: desk ? 180 : 140, borderRadius: 16, border: `2px dashed ${drag ? "var(--primary)" : "var(--border-tertiary)"}`, background: drag ? "var(--brand-primary-highlight)" : "transparent", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 10, cursor: "pointer", padding: 16, textAlign: "center" }}>
          <PtIcon icon="folder_zip" size={40} color="var(--primary)" />
          <PtText variant="headline3" color="var(--primary)" align="center">{desk ? "Перетащите файл сюда или нажмите, чтобы выбрать" : "Выбрать файл"}</PtText>
          <PtText variant="additional0" color={ptMuted} align="center">ZIP с паспортами монтажа</PtText>
        </button>
      </div>
    );
  } else if (phase === "reading" || phase === "importing") {
    body = (
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 14, padding: "28px 0 20px", textAlign: "center" }}>
        <PtSpinner size={40} />
        <PtText variant="headline2" align="center">{phase === "reading" ? "Читаем файл…" : "Импортируем…"}</PtText>
        <PtText variant="paragraph2" color={ptMuted} align="center" style={{ fontFamily: phase === "reading" ? "var(--font-mono)" : undefined }}>{phase === "reading" ? file : `${ptPass(willAdd)}, фото и настройки`}</PtText>
      </div>
    );
  } else if (phase === "bad") {
    body = (
      <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: "8px 0" }}>
        <div style={{ display: "flex", gap: 12, alignItems: "flex-start", padding: "14px 16px", borderRadius: 12, background: "var(--error-highlight)" }}>
          <PtIcon icon="error" size={26} color="var(--error-default)" />
          <div style={{ flex: 1, minWidth: 0 }}>
            <PtText variant="headline3">Это не архив паспортов</PtText>
            <PtText variant="paragraph2" color={ptMuted} style={{ marginTop: 4, textWrap: "pretty" }}>В файле <span style={{ fontFamily: "var(--font-mono)" }}>{file}</span> нет данных паспорта. Выберите файл, сохранённый через «Экспорт» в Pandora Spec.</PtText>
          </div>
        </div>
      </div>
    );
    footer = <React.Fragment><PtButton text="Выбрать другой файл" icon="folder_open" type="Primary" size={wide ? "L" : "XL"} fullWidth={!wide} onClick={() => setPhase("pick")} /><PtTextBtn text="Отмена" onClick={onClose} /></React.Fragment>;
  } else if (phase === "summary") {
    body = (
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <PtIcon icon="folder_zip" size={22} color={ptMuted} />
          <PtText variant="paragraph3" color={ptMuted} style={{ fontFamily: "var(--font-mono)", overflowWrap: "anywhere" }}>{file}</PtText>
        </div>
        <PtText variant="headline2">{`В файле ${ptPass(incoming.length)}`}{dupes.length ? ` · ${dupes.length} уже есть` : ""}</PtText>
        <div style={{ border: "1px solid var(--border-highlight)", borderRadius: 16, overflow: "hidden" }}>
          {incoming.map((x, i) => {
            const d = isDupe(x);
            return (
              <div key={x.no} style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "8px 14px", borderTop: i ? "1px solid var(--border-ultra-highlight)" : "none" }}>
                <PtIcon icon={d ? "content_copy" : "add_circle"} size={22} color={d ? "var(--warning-default)" : "var(--success-default)"} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <PtText variant="headline3" maxLines={1}>{x.car}{x.plate ? " · " + x.plate : ""}</PtText>
                  <PtText variant="additional0" color={ptMuted}>№ {x.no}{x.settings ? " · настройки" : ""}</PtText>
                </div>
                <PT_DS.CmpText variant="additional0" color={d ? "var(--warning-default)" : ptMuted} style={{ whiteSpace: "nowrap" }}>{d ? "Уже есть" : "Новый"}</PT_DS.CmpText>
              </div>
            );
          })}
        </div>
        {dupes.length > 0 && (
          <div role="radiogroup" aria-label="Уже есть в архиве" style={{ display: "flex", flexDirection: "column", gap: 2, paddingTop: 4 }}>
            <PtText variant="headline3" style={{ marginBottom: 4 }}>Если паспорт уже есть</PtText>
            {[["skip", "Пропустить", "Оставить паспорт на этом устройстве как есть"], ["replace", "Заменить", "Взять версию из файла — правки на этом устройстве пропадут"]].map(([k, t, sub]) => (
              <label key={k} className="pt-tap" style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "6px 12px 6px 4px", margin: "0 -4px", borderRadius: 12, cursor: "pointer" }} onClick={() => setDupe(k)}>
                {PtRadio ? <PtRadio selected={dupe === k} onClick={() => setDupe(k)} /> : <input type="radio" checked={dupe === k} readOnly />}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <PtText variant="headline3">{t}</PtText>
                  <PtText variant="paragraph3" color={ptMuted}>{sub}</PtText>
                </div>
              </label>
            ))}
          </div>
        )}
      </div>
    );
    footer = <React.Fragment><PtButton text={willAdd ? `Импортировать ${ptPass(willAdd)}` : "Нечего импортировать"} icon="download_done" type="Primary" size={wide ? "L" : "XL"} fullWidth={!wide} disabled={!willAdd} onClick={run} /><PtTextBtn text="Отмена" onClick={onClose} /></React.Fragment>;
  } else {
    body = (
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 14, padding: "28px 0 20px", textAlign: "center" }}>
        <span className="pt-anim" style={{ width: 56, height: 56, borderRadius: 999, background: "var(--success-default)", display: "grid", placeItems: "center", animation: `ptPop 240ms ${PT_EASE}` }}><PtIcon icon="check" size={32} color="#fff" /></span>
        <PtText variant="headline2" align="center">Готово</PtText>
      </div>
    );
  }
  return <PtModal wide={wide} title="Импорт паспортов" onClose={onClose} footer={footer} busy={phase === "reading" || phase === "importing" || phase === "done"}>{body}</PtModal>;
}

Object.assign(window, { PtExport, PtImport, PtModal, PT_INCOMING });
