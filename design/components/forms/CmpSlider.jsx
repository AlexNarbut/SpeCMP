import React, { useRef, useState } from "react";

/**
 * CmpSlider — kit Slider: track 4 (ink 15%), active part ink 100%,
 * round thumb (24 here, for fingers) in a 48-high hit area. Drag or tap.
 */
export function CmpSlider({ value = 0, onValueChange, min = 0, max = 1, step, disabled = false, style, ...rest }) {
  const ref = useRef(null);
  const [drag, setDrag] = useState(false);
  const pct = Math.max(0, Math.min(1, (value - min) / (max - min || 1))) * 100;
  const setFrom = (clientX) => {
    const r = ref.current.getBoundingClientRect();
    let v = min + Math.max(0, Math.min(1, (clientX - r.left) / r.width)) * (max - min);
    if (step) v = Math.round((v - min) / step) * step + min;
    onValueChange && onValueChange(v);
  };
  const down = (e) => {
    if (disabled) return;
    setDrag(true);
    setFrom(e.clientX);
    const move = (ev) => setFrom(ev.clientX);
    const up = () => { setDrag(false); window.removeEventListener("pointermove", move); window.removeEventListener("pointerup", up); };
    window.addEventListener("pointermove", move);
    window.addEventListener("pointerup", up);
  };
  return (
    <div
      ref={ref}
      onPointerDown={down}
      style={{ position: "relative", height: 48, width: "100%", touchAction: "none", cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.4 : 1, ...style }}
      {...rest}
    >
      <div style={{ position: "absolute", left: 0, right: 0, top: 22, height: 4, borderRadius: 2, background: "var(--background-surface-medium-highlight)" }} />
      <div style={{ position: "absolute", left: 0, width: `${pct}%`, top: 22, height: 4, borderRadius: 2, background: "var(--foreground-on-dark-primary-default)" }} />
      <div
        style={{
          position: "absolute", left: `${pct}%`, top: 24, width: 24, height: 24, borderRadius: "50%",
          background: "var(--foreground-on-dark-primary-default)",
          transform: `translate(-50%, -50%) scale(${drag ? 1.15 : 1})`,
          transition: "transform var(--motion-press)",
          boxShadow: "0 0 0 4px var(--background-page-primary)",
        }}
      />
    </div>
  );
}
