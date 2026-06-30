import React from "react";

/**
 * CmpRadioButton — single choice from a group. Active = primary.
 * 48dp touch area.
 */
export function CmpRadioButton({
  selected = false,
  onClick,
  disabled = false,
  style,
  ...rest
}) {
  const color = disabled
    ? "color-mix(in srgb, var(--on-surface) 38%, transparent)"
    : selected
    ? "var(--primary)"
    : "var(--on-surface-variant)";
  return (
    <button
      type="button"
      role="radio"
      aria-checked={selected}
      disabled={disabled}
      onClick={onClick}
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        width: "var(--touch-min)",
        height: "var(--touch-min)",
        padding: 0,
        border: "none",
        background: "transparent",
        cursor: disabled ? "default" : "pointer",
        flex: "0 0 auto",
        ...style,
      }}
      {...rest}
    >
      <span
        style={{
          width: 20,
          height: 20,
          borderRadius: 999,
          border: `2px solid ${color}`,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        {selected && (
          <span style={{ width: 10, height: 10, borderRadius: 999, background: color }} />
        )}
      </span>
    </button>
  );
}
