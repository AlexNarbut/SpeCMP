import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

const TYPES = {
  // Ink fill (navy in light, blue-white in dark) — main action.
  Primary: { bg: "var(--background-surface-primary)", fg: "var(--foreground-on-light-primary-default)", ripple: "var(--foreground-on-light-primary-default)" },
  // Blue accent — special emphasis (start, flash, connect).
  Accent: { bg: "var(--brand-primary-default)", fg: "var(--on-brand)", ripple: "#FFFFFF" },
  // Translucent ink step + 1px border.
  Secondary: { bg: "var(--background-surface-highlight)", fg: "var(--foreground-on-dark-primary-default)", border: "var(--border-light)", ripple: "var(--foreground-on-dark-primary-default)" },
  // No fill.
  Tertiary: { bg: "transparent", fg: "var(--foreground-on-dark-primary-default)", ripple: "var(--foreground-on-dark-primary-default)" },
  // Destructive.
  Danger: { bg: "var(--error-default)", fg: "#FFFFFF", ripple: "#FFFFFF" },
};
const SIZES = {
  S: { h: "var(--h-button-s)", fs: "var(--t-headline3-size)", pad: 20 },
  M: { h: "var(--h-button-m)", fs: "var(--t-headline3-size)", pad: 24 },
  L: { h: "var(--h-button-l)", fs: "var(--t-headline2-size)", pad: 24 },
  XL: { h: "var(--h-button-xl)", fs: "var(--t-headline1-size)", pad: 32 },
};

/**
 * CmpButton — the one button. type: Primary | Accent | Secondary | Tertiary | Danger;
 * size: S 48 · M 52 · L 56 · XL 64. Label UPPERCASE, style by size: S/M headline3 16 · L headline2 19 · XL headline1 24.
 * Press: scale .96 + 10% ripple. loading: label hidden (width kept) + spinner.
 */
export function CmpButton({
  text,
  onClick,
  disabled = false,
  loading = false,
  icon,
  type = "Primary",
  size = "L",
  fullWidth = false,
  children,
  style,
  ...rest
}) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  const t = TYPES[type] || TYPES.Primary;
  const s = SIZES[size] || SIZES.L;
  const label = text ?? children;
  const off = disabled || loading;
  return (
    <button
      type="button"
      onClick={off ? undefined : onClick}
      disabled={disabled}
      {...handlers}
      style={{
        position: "relative",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "var(--space-8)",
        height: s.h,
        minWidth: 96,
        width: fullWidth ? "100%" : undefined,
        padding: `0 ${s.pad}px`,
        border: loading ? "1px solid var(--border-highlight)" : t.border ? `1px solid ${disabled ? "transparent" : t.border}` : "none",
        borderRadius: "var(--r-buttons-radius)",
        background: loading ? "transparent" : disabled ? (type === "Tertiary" ? "transparent" : "var(--background-surface-ultra-highlight)") : t.bg,
        color: disabled ? "var(--foreground-on-dark-primary-disabled)" : loading ? "var(--foreground-on-dark-primary-default)" : t.fg,
        fontFamily: "var(--font-sans)",
        fontSize: s.fs,
        lineHeight: 1,
        fontWeight: 700,
        textTransform: "uppercase",
        letterSpacing: "0.03em",
        cursor: off ? "default" : "pointer",
        overflow: "hidden",
        boxShadow: focusRing,
        ...(off ? {} : pressStyle),
        ...style,
      }}
      {...rest}
    >
      {!off && <span style={{ position: "absolute", inset: 0, background: t.ripple, opacity, pointerEvents: "none" }} />}
      <span style={{ display: "inline-flex", alignItems: "center", gap: "var(--space-8)", opacity: loading ? 0 : 1, whiteSpace: "nowrap", minWidth: 0, maxWidth: "100%", overflow: "hidden", textOverflow: "ellipsis" }}>
        {icon && <CmpIcon icon={icon} size={24} color="currentColor" />}
        {label}
      </span>
      {loading && (
        <span style={{ position: "absolute", width: 26, height: 26, borderRadius: "50%", border: "3px solid var(--border-light)", borderTopColor: "var(--brand-primary-default)", animation: "cmp-spin 0.9s linear infinite" }} />
      )}
      {loading && <style>{"@keyframes cmp-spin{to{transform:rotate(360deg)}}"}</style>}
    </button>
  );
}
