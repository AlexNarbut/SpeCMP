import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpOutlinedButton — secondary action. Outline border, primary text,
 * transparent background. Height 40dp, stadium shape.
 */
export function CmpOutlinedButton({
  text,
  onClick,
  disabled = false,
  icon,
  fullWidth = false,
  children,
  style,
  ...rest
}) {
  const { opacity, handlers } = useStateLayer();
  const label = text ?? children;
  const fg = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--primary)";
  return (
    <button
      type="button"
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      {...handlers}
      style={{
        position: "relative",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "var(--space-s)",
        height: "var(--button-height)",
        minWidth: 64,
        width: fullWidth ? "100%" : undefined,
        padding: icon ? "0 24px 0 16px" : "0 24px",
        border: `1px solid ${disabled ? "color-mix(in srgb, var(--on-surface) 12%, transparent)" : "var(--outline)"}`,
        borderRadius: 999,
        background: "transparent",
        color: fg,
        fontFamily: "var(--font-sans)",
        fontSize: "var(--label-large-size)",
        lineHeight: "var(--label-large-line)",
        fontWeight: "var(--weight-medium)",
        cursor: disabled ? "default" : "pointer",
        overflow: "hidden",
        ...style,
      }}
      {...rest}
    >
      {!disabled && (
        <span style={{ position: "absolute", inset: 0, background: "var(--primary)", opacity, pointerEvents: "none" }} />
      )}
      {icon && <CmpIcon icon={icon} size={18} color="currentColor" />}
      {label}
    </button>
  );
}
