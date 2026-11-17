import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";

/**
 * CmpSurface — generic container. Default fill = ink 5% step (kit
 * background-surface-ultra-highlight); pass `color` for another step.
 * `border` → 1px border-highlight. `shadowElevation` ignored (no shadows).
 */
export function CmpSurface({ children, onClick, color = "var(--background-surface-ultra-highlight)", contentColor = "var(--foreground-on-dark-primary-default)", radius, shadowElevation, border = false, style, ...rest }) {
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
        background: color, color: contentColor,
        borderRadius: radius ?? "var(--r-card-small-radius)",
        border: border ? "1px solid var(--border-highlight)" : "none",
        cursor: click ? "pointer" : undefined,
        boxShadow: focusRing,
        ...(click ? pressStyle : {}),
        ...style,
      }}
      {...rest}
    >
      {click && <span style={{ position: "absolute", inset: 0, background: contentColor, opacity, pointerEvents: "none" }} />}
      {children}
    </div>
  );
}
