import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";

/**
 * CmpSurface — base container. Square corners (0dp) and no shadow by
 * default; opt into tonal/shadow elevation, border, and a custom shape.
 * Pass `onClick` for a clickable surface (used e.g. for calculator keys).
 */
export function CmpSurface({
  children,
  onClick,
  color = "var(--surface)",
  contentColor = "var(--on-surface)",
  radius = 0,
  shadowElevation = 0,
  border = false,
  style,
  ...rest
}) {
  const { opacity, handlers } = useStateLayer();
  const clickable = !!onClick;
  const shadow = shadowElevation === 0 ? "none" : shadowElevation >= 3 ? "var(--elevation-3)" : shadowElevation === 2 ? "var(--elevation-2)" : "var(--elevation-1)";
  return (
    <div
      onClick={onClick}
      {...(clickable ? handlers : {})}
      style={{
        position: "relative",
        background: color,
        color: contentColor,
        borderRadius: radius,
        boxShadow: shadow,
        border: border ? "1px solid var(--outline-variant)" : "none",
        overflow: "hidden",
        cursor: clickable ? "pointer" : "default",
        ...style,
      }}
      {...rest}
    >
      {clickable && (
        <span style={{ position: "absolute", inset: 0, background: "currentColor", opacity, pointerEvents: "none" }} />
      )}
      {children}
    </div>
  );
}
