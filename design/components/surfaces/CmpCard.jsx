import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";

/**
 * CmpCard — card surface (white in light, ink 5% in dark) + 1px ink-10 border,
 * radius r-card-medium, no shadow. Clickable → press scale .96 + ripple.
 * `elevation` kept for API compat (≥2 = one step brighter fill).
 */
export function CmpCard({ children, onClick, elevation = 0, border = true, style, ...rest }) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  const click = !!onClick;
  return (
    <div
      onClick={onClick}
      role={click ? "button" : undefined}
      tabIndex={click ? 0 : undefined}
      {...(click ? handlers : {})}
      style={{
        position: "relative", overflow: "hidden", boxSizing: "border-box",
        background: elevation >= 2 ? "var(--background-surface-highlight)" : "var(--background-surface-card)",
        color: "var(--foreground-on-dark-primary-default)",
        border: border ? "1px solid var(--border-highlight)" : "none",
        borderRadius: "var(--r-card-medium-radius)",
        cursor: click ? "pointer" : undefined,
        boxShadow: focusRing,
        ...(click ? pressStyle : {}),
        ...style,
      }}
      {...rest}
    >
      {click && <span style={{ position: "absolute", inset: 0, background: "var(--foreground-on-dark-primary-default)", opacity, pointerEvents: "none" }} />}
      {children}
    </div>
  );
}
