import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpTextButton — tertiary action / dialog actions. Primary text,
 * no background, no border. Height 40dp.
 */
export function CmpTextButton({
  text,
  onClick,
  disabled = false,
  icon,
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
        minWidth: 48,
        padding: icon ? "0 16px 0 12px" : "0 12px",
        border: "none",
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
