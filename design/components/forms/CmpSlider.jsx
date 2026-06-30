import React, { useRef, useCallback } from "react";

/**
 * CmpSlider — choose a value in a range. Active track = primary,
 * inactive = surfaceVariant. Optional discrete steps.
 */
export function CmpSlider({
  value = 0,
  onValueChange,
  min = 0,
  max = 1,
  step = 0,
  disabled = false,
  style,
  ...rest
}) {
  const ref = useRef(null);
  const pct = ((value - min) / (max - min)) * 100;

  const setFromClientX = useCallback(
    (clientX) => {
      const el = ref.current;
      if (!el) return;
      const rect = el.getBoundingClientRect();
      let r = (clientX - rect.left) / rect.width;
      r = Math.max(0, Math.min(1, r));
      let v = min + r * (max - min);
      if (step > 0) v = Math.round(v / step) * step;
      onValueChange && onValueChange(Math.max(min, Math.min(max, v)));
    },
    [min, max, step, onValueChange]
  );

  const onDown = (e) => {
    if (disabled) return;
    setFromClientX(e.clientX);
    const move = (ev) => setFromClientX(ev.clientX);
    const up = () => {
      window.removeEventListener("mousemove", move);
      window.removeEventListener("mouseup", up);
    };
    window.addEventListener("mousemove", move);
    window.addEventListener("mouseup", up);
  };

  return (
    <div
      ref={ref}
      role="slider"
      aria-valuemin={min}
      aria-valuemax={max}
      aria-valuenow={value}
      onMouseDown={onDown}
      style={{
        position: "relative",
        height: 44,
        display: "flex",
        alignItems: "center",
        cursor: disabled ? "default" : "pointer",
        opacity: disabled ? 0.38 : 1,
        ...style,
      }}
      {...rest}
    >
      <div style={{ position: "absolute", left: 0, right: 0, height: 4, borderRadius: 999, background: "var(--surface-variant)" }} />
      <div style={{ position: "absolute", left: 0, width: `${pct}%`, height: 4, borderRadius: 999, background: "var(--primary)" }} />
      <div
        style={{
          position: "absolute",
          left: `calc(${pct}% - 10px)`,
          width: 20,
          height: 20,
          borderRadius: 999,
          background: "var(--primary)",
          boxShadow: "var(--elevation-1)",
        }}
      />
    </div>
  );
}
