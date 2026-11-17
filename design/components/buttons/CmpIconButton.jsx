import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpIconButton — round icon action, 48 (Standard) or 56 (Glass, top bar).
 * Glass = translucent ink fill + 1px light border + backdrop blur (kit TopBarIcon).
 * Press: scale .96 + ripple.
 */
export function CmpIconButton({
  icon,
  onClick,
  contentDescription,
  disabled = false,
  selected = false,
  variant = "Standard",
  tint,
  style,
  ...rest
}) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  const glass = variant === "Glass";
  const size = glass ? 56 : 48;
  const fg = disabled
    ? "var(--foreground-on-dark-primary-disabled)"
    : selected
    ? "var(--foreground-on-light-primary-default)"
    : tint ?? "var(--foreground-on-dark-primary-default)";
  return (
    <button
      type="button"
      aria-label={contentDescription}
      title={contentDescription}
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      {...handlers}
      style={{
        position: "relative",
        width: size,
        height: size,
        flexShrink: 0,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        borderRadius: "var(--r-icon-radius)",
        border: glass ? "var(--w-icon-width) solid var(--border-light)" : "none",
        background: selected ? "var(--background-surface-primary)" : glass ? "var(--background-glass)" : "transparent",
        backdropFilter: glass ? "blur(var(--blur-glass))" : undefined,
        WebkitBackdropFilter: glass ? "blur(var(--blur-glass))" : undefined,
        color: fg,
        cursor: disabled ? "default" : "pointer",
        overflow: "hidden",
        padding: 0,
        boxShadow: focusRing,
        ...(disabled ? {} : pressStyle),
        ...style,
      }}
      {...rest}
    >
      {!disabled && <span style={{ position: "absolute", inset: 0, background: "currentColor", opacity, pointerEvents: "none" }} />}
      <CmpIcon icon={icon} size={24} color="currentColor" />
    </button>
  );
}
