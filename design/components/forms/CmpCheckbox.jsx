import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpCheckbox — kit CheckBox: box 24, radius r-checkbox, hit area 48.
 * Off = 1.5px ink-30 border. On/indeterminate = ink fill + on-light check.
 */
export function CmpCheckbox({ checked = false, indeterminate = false, onCheckedChange, disabled = false, style, ...rest }) {
  const { handlers, pressStyle, focusRing } = useStateLayer();
  const on = checked || indeterminate;
  return (
    <button
      type="button"
      role="checkbox"
      aria-checked={indeterminate ? "mixed" : checked}
      disabled={disabled}
      onClick={() => !disabled && onCheckedChange && onCheckedChange(!checked)}
      {...handlers}
      style={{
        width: 48, height: 48, padding: 0, border: "none", background: "transparent", flexShrink: 0,
        display: "inline-flex", alignItems: "center", justifyContent: "center",
        cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.4 : 1,
        ...style,
      }}
      {...rest}
    >
      <span
        style={{
          width: 24, height: 24, boxSizing: "border-box", borderRadius: "var(--r-checkbox-radius)",
          display: "inline-flex", alignItems: "center", justifyContent: "center",
          background: on ? "var(--background-surface-primary)" : "transparent",
          border: on ? "none" : "1.5px solid var(--border-tertiary)",
          color: "var(--foreground-on-light-primary-default)",
          boxShadow: focusRing,
          ...(disabled ? {} : pressStyle),
        }}
      >
        {on && <CmpIcon icon={indeterminate ? "remove" : "check"} size={18} color="currentColor" />}
      </span>
    </button>
  );
}
