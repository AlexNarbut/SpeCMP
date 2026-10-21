import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpIconButton — action shown as a single catalog icon. Touch target
 * 48dp, icon 24dp. Content is ALWAYS an icon (never arbitrary UI).
 */
export function CmpIconButton({
  icon,
  onClick,
  contentDescription,
  disabled = false,
  selected = false,
  tint,
  style,
  ...rest
}) {
  const { opacity, handlers } = useStateLayer();
  const color = disabled
    ? "color-mix(in srgb, var(--on-surface) 38%, transparent)"
    : tint || (selected ? "var(--primary)" : "var(--on-surface-variant)");
  return (
    <button
      type="button"
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      aria-label={contentDescription}
      aria-pressed={selected || undefined}
      {...handlers}
      style={{
        position: "relative",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        width: "var(--touch-min)",
        height: "var(--touch-min)",
        border: "none",
        borderRadius: 999,
        background: selected ? "var(--surface-variant)" : "transparent",
        color,
        cursor: disabled ? "default" : "pointer",
        overflow: "hidden",
        flex: "0 0 auto",
        ...style,
      }}
      {...rest}
    >
      {!disabled && (
        <span style={{ position: "absolute", inset: 0, borderRadius: 999, background: "currentColor", opacity, pointerEvents: "none" }} />
      )}
      <CmpIcon icon={icon} size={24} color="currentColor" title={contentDescription} />
    </button>
  );
}
