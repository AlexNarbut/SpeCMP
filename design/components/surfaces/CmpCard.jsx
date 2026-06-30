import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";

/**
 * CmpCard — content container. Shadow 1dp, medium radius, surface fill.
 * Pass `onClick` to make it clickable (adds a hover state layer).
 */
export function CmpCard({
  children,
  onClick,
  elevation = 1,
  border = false,
  style,
  ...rest
}) {
  const { opacity, handlers } = useStateLayer();
  const clickable = !!onClick;
  const shadow = elevation === 0 ? "none" : elevation >= 3 ? "var(--elevation-3)" : elevation === 2 ? "var(--elevation-2)" : "var(--elevation-1)";
  return (
    <div
      onClick={onClick}
      {...(clickable ? handlers : {})}
      style={{
        position: "relative",
        background: "var(--surface)",
        color: "var(--on-surface)",
        borderRadius: "var(--radius-m)",
        boxShadow: shadow,
        border: border ? "1px solid var(--outline-variant)" : "none",
        overflow: "hidden",
        cursor: clickable ? "pointer" : "default",
        ...style,
      }}
      {...rest}
    >
      {clickable && (
        <span style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity, pointerEvents: "none" }} />
      )}
      {children}
    </div>
  );
}
