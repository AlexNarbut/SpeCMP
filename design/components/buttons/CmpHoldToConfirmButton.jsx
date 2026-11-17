import React, { useEffect, useRef, useState } from "react";

/**
 * CmpHoldToConfirmButton — kit HoldToConfirm: for dangerous actions (reset, erase, flash).
 * Height 64, radius r-buttons, label UPPERCASE headline2. Press and hold `duration` ms:
 * a fill sweeps left→right; releasing early cancels. Fires onConfirm at 100%.
 */
export function CmpHoldToConfirmButton({ text = "Удерживайте", doneText = "Готово", onConfirm, duration = 1500, danger = true, disabled = false, style }) {
  const [p, setP] = useState(0);
  const [done, setDone] = useState(false);
  const raf = useRef(0), t0 = useRef(0);
  const stop = () => { cancelAnimationFrame(raf.current); if (!done) setP(0); };
  const tick = () => {
    const v = Math.min(1, (performance.now() - t0.current) / duration);
    setP(v);
    if (v >= 1) { setDone(true); onConfirm && onConfirm(); return; }
    raf.current = requestAnimationFrame(tick);
  };
  const start = () => { if (disabled || done) return; t0.current = performance.now(); raf.current = requestAnimationFrame(tick); };
  useEffect(() => () => cancelAnimationFrame(raf.current), []);
  const fill = danger ? "var(--error-default)" : "var(--brand-primary-default)";
  return (
    <button
      type="button"
      disabled={disabled}
      onPointerDown={start} onPointerUp={stop} onPointerLeave={stop}
      onKeyDown={(e) => { if ((e.key === " " || e.key === "Enter") && !e.repeat) start(); }}
      onKeyUp={stop}
      style={{
        position: "relative", overflow: "hidden", width: "100%", height: "var(--h-hold)", boxSizing: "border-box",
        border: `1px solid ${disabled ? "transparent" : fill}`, borderRadius: "var(--r-buttons-radius)",
        background: disabled ? "var(--background-surface-ultra-highlight)" : "var(--background-surface-ultra-highlight)",
        color: disabled ? "var(--foreground-on-dark-primary-disabled)" : p > 0.5 || done ? "#FFFFFF" : fill,
        fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.03em",
        cursor: disabled ? "default" : "pointer", touchAction: "none", userSelect: "none",
        transform: p > 0 && !done ? "scale(var(--press-scale))" : "scale(1)", transition: "transform var(--motion-press), color var(--motion-fast)",
        ...style,
      }}
    >
      <span style={{ position: "absolute", inset: 0, width: `${(done ? 1 : p) * 100}%`, background: fill, transition: p === 0 ? "width var(--motion-standard)" : "none" }} />
      <span style={{ position: "relative" }}>{done ? doneText : text}</span>
    </button>
  );
}
