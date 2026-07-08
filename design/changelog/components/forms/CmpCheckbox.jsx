import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpCheckbox — multi-select / on-off. Active = primary fill with check.
 * Supports indeterminate (tri-state). 48dp touch area.
 */
export function CmpCheckbox({
  checked = false,
  indeterminate = false,
  onCheckedChange,
  disabled = false,
  style,
  ...rest
}) {
  const on = checked || indeterminate;
  const boxColor = disabled
    ? "color-mix(in srgb, var(--on-surface) 38%, transparent)"
    : on
    ? "var(--primary)"
    : "transparent";
  const borderColor = disabled
    ? "color-mix(in srgb, var(--on-surface) 38%, transparent)"
    : on
    ? "var(--primary)"
    : "var(--on-surface-variant)";

  return (
    <button
      type="button"
      role="checkbox"
      aria-checked={indeterminate ? "mixed" : checked}
      disabled={disabled}
      onClick={() => onCheckedChange && onCheckedChange(!checked)}
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
          width: 18,
          height: 18,
          borderRadius: 2,
          border: `2px solid ${borderColor}`,
          background: boxColor,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          transition: "all var(--motion-fast)",
        }}
      >
        {indeterminate ? (
          <span style={{ width: 10, height: 2, background: "var(--on-primary)", borderRadius: 1 }} />
        ) : (
          checked && <CmpIcon icon="Check" size={16} color="var(--on-primary)" />
        )}
      </span>
    </button>
  );
}
