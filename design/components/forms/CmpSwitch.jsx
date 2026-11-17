import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";

/**
 * CmpSwitch — kit Toggle, scaled for gloved fingers: track 52×32, thumb 24,
 * hit area 48. On = brand blue track + white thumb; off = ink 15% track + ink thumb.
 */
export function CmpSwitch({ checked = false, onCheckedChange, disabled = false, thumbIcon, style, ...rest }) {
  const { handlers, active, focusRing } = useStateLayer();
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => !disabled && onCheckedChange && onCheckedChange(!checked)}
      {...handlers}
      style={{
        width: 64, height: 48, padding: 0, border: "none", background: "transparent",
        display: "inline-flex", alignItems: "center", justifyContent: "center",
        cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.4 : 1, flexShrink: 0,
        ...style,
      }}
      {...rest}
    >
      <span
        style={{
          position: "relative", width: 52, height: 32, borderRadius: 9999, boxSizing: "border-box",
          background: checked ? "var(--brand-primary-default)" : "var(--background-surface-medium-highlight)",
          border: checked ? "none" : "1px solid var(--border-light)",
          boxShadow: focusRing,
          transition: "background var(--motion-standard)",
        }}
      >
        <span
          style={{
            position: "absolute", top: "50%", left: checked ? 24 : 4,
            width: 24, height: 24, borderRadius: "50%",
            background: checked ? "#FFFFFF" : "var(--foreground-on-dark-primary-default)",
            transform: `translateY(-50%) scale(${active ? 0.9 : 1})`,
            transition: "left var(--motion-press), transform var(--motion-press)",
          }}
        />
      </span>
    </button>
  );
}
