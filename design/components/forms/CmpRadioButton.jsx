import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";

/** CmpRadioButton — circle 24 in a 48 hit area. Selected = ink ring + ink dot. */
export function CmpRadioButton({ selected = false, onClick, disabled = false, style, ...rest }) {
  const { handlers, pressStyle, focusRing } = useStateLayer();
  return (
    <button
      type="button"
      role="radio"
      aria-checked={selected}
      disabled={disabled}
      onClick={disabled ? undefined : onClick}
      {...handlers}
      style={{
        width: 48, height: 48, padding: 0, border: "none", background: "transparent", flexShrink: 0,
        display: "inline-flex", alignItems: "center", justifyContent: "center",
        cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.4 : 1,
        ...style,
      }}
      {...rest}
    >
      <span
        style={{
          width: 24, height: 24, boxSizing: "border-box", borderRadius: "50%",
          display: "inline-flex", alignItems: "center", justifyContent: "center",
          border: `2px solid ${selected ? "var(--foreground-on-dark-primary-default)" : "var(--border-tertiary)"}`,
          boxShadow: focusRing,
          ...(disabled ? {} : pressStyle),
        }}
      >
        {selected && <span style={{ width: 12, height: 12, borderRadius: "50%", background: "var(--foreground-on-dark-primary-default)" }} />}
      </span>
    </button>
  );
}
