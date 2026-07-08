import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpSwitch — binary on/off. Active track = primary. Optional check
 * icon on the thumb when checked. 48dp touch area.
 */
export function CmpSwitch({
  checked = false,
  onCheckedChange,
  disabled = false,
  thumbIcon = false,
  style,
  ...rest
}) {
  const trackColor = disabled
    ? "color-mix(in srgb, var(--on-surface) 12%, transparent)"
    : checked
    ? "var(--primary)"
    : "var(--surface-container-highest)";
  const borderColor = checked ? "var(--primary)" : "var(--outline)";
  const thumbColor = disabled
    ? "color-mix(in srgb, var(--on-surface) 38%, transparent)"
    : checked
    ? "var(--on-primary)"
    : "var(--outline)";
  const thumbSize = checked ? 24 : 16;

  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => onCheckedChange && onCheckedChange(!checked)}
      style={{
        position: "relative",
        width: 52,
        height: 32,
        padding: 0,
        borderRadius: 999,
        border: `2px solid ${borderColor}`,
        background: trackColor,
        cursor: disabled ? "default" : "pointer",
        transition: "background var(--motion-fast)",
        flex: "0 0 auto",
        ...style,
      }}
      {...rest}
    >
      <span
        style={{
          position: "absolute",
          top: "50%",
          left: checked ? `calc(100% - ${thumbSize}px - 4px)` : 6,
          width: thumbSize,
          height: thumbSize,
          transform: "translateY(-50%)",
          borderRadius: 999,
          background: thumbColor,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          transition: "all var(--motion-fast)",
        }}
      >
        {thumbIcon && checked && <CmpIcon icon="Check" size={16} color="var(--primary)" />}
      </span>
    </button>
  );
}
