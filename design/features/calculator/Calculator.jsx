// CalculatorScreen — faithful recreation of feature/calculator CalculatorScreen.kt.
// Built from Pandora Spec design-system components (window namespace).
// Loaded as a Babel script by each UI kit's index.html.
//
// Адаптивность по Material 3 Window Size Classes (CLAUDE.md → «Размеры экранов»):
//   • compact — телефон-портрет: один столбец, клавиатура у нижнего края.
//   • stacked — планшет-портрет (Medium+): широкий центрированный столбец,
//     крупные клавиши и дисплей.
//   • split   — ландшафт/Desktop (Medium-ландшафт + Expanded): две колонки —
//     слева таймер+дисплей, справа клавиатура.
// Класс выбирается по реальной ширине/высоте контейнера, а не по устройству.

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpSurface, CmpText, CmpSnackbar,
} = DS;

const OPS = {
  Add: { symbol: "+", fn: (a, b) => a + b },
  Subtract: { symbol: "−", fn: (a, b) => a - b },
  Multiply: { symbol: "×", fn: (a, b) => a * b },
  Divide: { symbol: "÷", fn: (a, b) => a / b },
};

function formatNumber(v) {
  if (!isFinite(v)) return "0";
  return Number.isInteger(v) ? String(v) : String(parseFloat(v.toFixed(8)));
}
function formatTimer(total) {
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  const p = (n) => String(n).padStart(2, "0");
  return h > 0 ? `${p(h)}:${p(m)}:${p(s)}` : `${p(m)}:${p(s)}`;
}

// ── Adaptive layout ─────────────────────────────────────────────────────────
// Measures the host container and returns the active layout mode + key height.
function useLayout() {
  const ref = React.useRef(null);
  const [size, setSize] = React.useState({ w: 0, h: 0 });
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const measure = (w, h) => setSize({ w, h });
    const remeasure = () => {
      // offsetWidth/Height — layout-размер без учёта CSS-transform родителя
      // (getBoundingClientRect отдал бы масштабированный размер в scale()-обёртке).
      measure(el.offsetWidth, el.offsetHeight);
    };
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => { ro.disconnect(); window.removeEventListener("resize", remeasure); };
  }, []);

  const { w, h } = size;
  let mode = "compact";
  if (w >= 600) mode = w > h * 1.1 ? "split" : "stacked";

  // Key height: never below the 48dp touch target; scale to available height.
  let keyH = 72;
  if (mode === "stacked") keyH = Math.max(56, Math.min(88, Math.floor((h - 360) / 6)));
  else if (mode === "split") keyH = Math.max(48, Math.min(80, Math.floor((h - 96) / 6)));
  return [ref, mode, keyH, size];
}

// One calculator key — a clickable Surface tile, radius 16.
function CalcKey({ label, color, contentColor, weight = 1, fontWeight = 400, keyH = 72, onClick }) {
  const variant = keyH >= 80 ? "headlineMedium" : "headlineSmall";
  return (
    <div style={{ flex: weight, display: "flex" }}>
      <CmpSurface
        onClick={onClick}
        color={color}
        contentColor={contentColor}
        radius={16}
        style={{ flex: 1, height: keyH, display: "grid", placeItems: "center" }}
      >
        <CmpText variant={variant} weight={fontWeight}>{label}</CmpText>
      </CmpSurface>
    </div>
  );
}

function TimerBtn({ label, color, contentColor, height = 40, onClick }) {
  return (
    <div style={{ flex: 1, display: "flex" }}>
      <CmpSurface onClick={onClick} color={color} contentColor={contentColor} radius={12}
        style={{ flex: 1, height, display: "grid", placeItems: "center" }}>
        <CmpText variant="bodyMedium" weight={500}>{label}</CmpText>
      </CmpSurface>
    </div>
  );
}

// ── Sections (shared across layouts) ─────────────────────────────────────────
function TimerSection({ seconds, running, setRunning, setSeconds, big }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: big ? 12 : 8, padding: "0 24px", width: "100%", boxSizing: "border-box" }}>
      <span style={{ fontFamily: "var(--font-mono)", fontSize: big ? 48 : 36, fontWeight: 300, color: "var(--on-background)" }}>
        {formatTimer(seconds)}
      </span>
      <div style={{ display: "flex", gap: 8, width: "100%" }}>
        {!running ? (
          <TimerBtn label="Старт" color="var(--primary-container)" contentColor="var(--on-primary-container)" height={big ? 52 : 40} onClick={() => setRunning(true)} />
        ) : (
          <TimerBtn label="Стоп" color="var(--secondary-container)" contentColor="var(--on-secondary-container)" height={big ? 52 : 40} onClick={() => setRunning(false)} />
        )}
        <TimerBtn label="Сброс" color="var(--surface-variant)" contentColor="var(--on-surface-variant)" height={big ? 52 : 40} onClick={() => { setRunning(false); setSeconds(0); }} />
      </div>
    </div>
  );
}

function DisplaySection({ display, operation, firstOperand, big }) {
  const long = display.length > 9;
  const size = big ? (long ? 56 : 88) : (long ? 40 : 64);
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", padding: "0 24px", width: "100%", boxSizing: "border-box" }}>
      {operation && (
        <span style={{ fontFamily: "var(--font-mono)", fontSize: big ? 20 : 16, color: "color-mix(in srgb, var(--on-background) 50%, transparent)" }}>
          {firstOperand != null ? formatNumber(firstOperand) : ""} {OPS[operation].symbol}
        </span>
      )}
      <span style={{ fontFamily: "var(--font-mono)", fontSize: size, fontWeight: 300, lineHeight: 1.1, maxWidth: "100%", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
        {display}
      </span>
    </div>
  );
}

function Keypad({ keyH, onClear, onDelete, onOperation, onDigit, onEqual, onDecimal }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, padding: "0 12px", width: "100%", boxSizing: "border-box" }}>
      <div style={{ display: "flex", gap: 8 }}>
        <CalcKey label="C" keyH={keyH} color="var(--surface-variant)" contentColor="var(--on-surface-variant)" onClick={onClear} />
        <CalcKey label="⌫" keyH={keyH} color="var(--surface-variant)" contentColor="var(--on-surface-variant)" onClick={onDelete} />
        <CalcKey label="÷" keyH={keyH} color="var(--secondary-container)" contentColor="var(--on-secondary-container)" onClick={() => onOperation("Divide")} />
        <CalcKey label="×" keyH={keyH} color="var(--secondary-container)" contentColor="var(--on-secondary-container)" onClick={() => onOperation("Multiply")} />
      </div>
      {[["7", "8", "9", "Subtract", "−"], ["4", "5", "6", "Add", "+"]].map((row, i) => (
        <div key={i} style={{ display: "flex", gap: 8 }}>
          <CalcKey label={row[0]} keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={() => onDigit(row[0])} />
          <CalcKey label={row[1]} keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={() => onDigit(row[1])} />
          <CalcKey label={row[2]} keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={() => onDigit(row[2])} />
          <CalcKey label={row[4]} keyH={keyH} color="var(--secondary-container)" contentColor="var(--on-secondary-container)" onClick={() => onOperation(row[3])} />
        </div>
      ))}
      <div style={{ display: "flex", gap: 8 }}>
        <CalcKey label="1" keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={() => onDigit("1")} />
        <CalcKey label="2" keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={() => onDigit("2")} />
        <CalcKey label="3" keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={() => onDigit("3")} />
        <CalcKey label="=" keyH={keyH} color="var(--primary-container)" contentColor="var(--on-primary-container)" onClick={onEqual} />
      </div>
      <div style={{ display: "flex", gap: 8 }}>
        <CalcKey label="0" keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" weight={2} onClick={() => onDigit("0")} />
        <CalcKey label="." keyH={keyH} color="var(--surface)" contentColor="var(--on-surface)" onClick={onDecimal} />
        <div style={{ flex: 1 }} />
      </div>
    </div>
  );
}

function CalculatorScreen({ onBack = () => {}, showBack = true }) {
  const { useState, useEffect, useRef } = React;
  const [rootRef, mode, keyH] = useLayout();

  // ── calculator state ──
  const [display, setDisplay] = useState("0");
  const [firstOperand, setFirstOperand] = useState(null);
  const [operation, setOperation] = useState(null);
  const [overwrite, setOverwrite] = useState(true);
  const [snack, setSnack] = useState(false);

  // ── timer state ──
  const [seconds, setSeconds] = useState(0);
  const [running, setRunning] = useState(false);
  const timer = useRef(null);
  useEffect(() => {
    if (running) {
      timer.current = setInterval(() => setSeconds((s) => s + 1), 1000);
      return () => clearInterval(timer.current);
    }
  }, [running]);

  const showSnack = () => {
    setSnack(true);
    setTimeout(() => setSnack(false), 3000);
  };

  const onDigit = (d) => {
    setDisplay((cur) => {
      if (overwrite) {
        setOverwrite(false);
        return d;
      }
      return cur === "0" ? d : (cur.length < 12 ? cur + d : cur);
    });
  };
  const onDecimal = () => {
    setDisplay((cur) => {
      if (overwrite) {
        setOverwrite(false);
        return "0.";
      }
      return cur.includes(".") ? cur : cur + ".";
    });
  };
  const onClear = () => {
    setDisplay("0");
    setFirstOperand(null);
    setOperation(null);
    setOverwrite(true);
  };
  const onDelete = () => {
    setDisplay((cur) => {
      if (overwrite) return cur;
      const next = cur.slice(0, -1);
      return next === "" || next === "-" ? "0" : next;
    });
  };
  const onOperation = (opKey) => {
    const cur = parseFloat(display);
    if (firstOperand != null && operation && !overwrite) {
      const r = OPS[operation].fn(firstOperand, cur);
      if (operation === "Divide" && cur === 0) { showSnack(); onClear(); return; }
      setFirstOperand(r);
      setDisplay(formatNumber(r));
    } else {
      setFirstOperand(cur);
    }
    setOperation(opKey);
    setOverwrite(true);
  };
  const onEqual = () => {
    if (firstOperand == null || !operation) return;
    const cur = parseFloat(display);
    if (operation === "Divide" && cur === 0) { showSnack(); onClear(); return; }
    const r = OPS[operation].fn(firstOperand, cur);
    setDisplay(formatNumber(r));
    setFirstOperand(null);
    setOperation(null);
    setOverwrite(true);
  };

  const keypadProps = { keyH, onClear, onDelete, onOperation, onDigit, onEqual, onDecimal };
  const big = mode !== "compact";
  const timerSection = <TimerSection seconds={seconds} running={running} setRunning={setRunning} setSeconds={setSeconds} big={big} />;
  const displaySection = <DisplaySection display={display} operation={operation} firstOperand={firstOperand} big={big} />;
  const keypad = <Keypad {...keypadProps} />;

  let layout;
  if (mode === "split") {
    // Ландшафт / Desktop — две колонки, контент центрирован с max-width.
    layout = (
      <div style={{ flex: 1, minHeight: 0, display: "flex", alignItems: "stretch", gap: 24, padding: 24, boxSizing: "border-box", maxWidth: 1040, width: "100%", margin: "0 auto" }}>
        <div style={{ flex: "1 1 0", minWidth: 260, display: "flex", flexDirection: "column", justifyContent: "center", gap: 28 }}>
          {timerSection}
          {displaySection}
        </div>
        <div style={{ flex: "1 1 0", maxWidth: 520, display: "flex", flexDirection: "column", justifyContent: "center" }}>
          {keypad}
        </div>
      </div>
    );
  } else if (mode === "stacked") {
    // Планшет-портрет — широкий центрированный столбец, крупные элементы.
    layout = (
      <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", justifyContent: "flex-end", gap: 20, maxWidth: 600, width: "100%", margin: "0 auto", padding: "0 0 16px", boxSizing: "border-box" }}>
        {timerSection}
        {displaySection}
        {keypad}
      </div>
    );
  } else {
    // Телефон-портрет — исходная компактная раскладка.
    layout = (
      <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "flex-end", gap: 16, paddingBottom: 16 }}>
        {timerSection}
        {displaySection}
        {keypad}
      </div>
    );
  }

  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)" }}>
      <CmpTopAppBar
        title="Калькулятор"
        navigationIcon={showBack ? <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} /> : undefined}
      />
      {layout}

      {snack && (
        <div style={{ position: "absolute", left: 16, right: 16, bottom: 16, display: "flex", justifyContent: "center" }}>
          <CmpSnackbar message="Деление на ноль невозможно" actionLabel="OK" onActionClick={() => setSnack(false)} />
        </div>
      )}
    </div>
  );
}

Object.assign(window, { CalculatorScreen });
