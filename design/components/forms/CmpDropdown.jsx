import React, { useState } from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpDropdown — select in the InputMain shell (72, translucent fill, 1px border).
 * Menu = card surface with 1px border, rows 56 (≥48 touch).
 */
export function CmpDropdown({ selectedValue = null, placeholder = "Выберите", options = [], onOptionSelected, disabled = false, rootStyle }) {
  const [open, setOpen] = useState(false);
  const { handlers, pressStyle, focusRing } = useStateLayer();
  return (
    <div style={{ position: "relative", width: "100%", opacity: disabled ? 0.4 : 1, ...rootStyle }}>
      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen((o) => !o)}
        {...handlers}
        style={{
          width: "100%", minHeight: "var(--h-input)", padding: "0 var(--space-16) 0 var(--space-20)",
          display: "flex", alignItems: "center", gap: "var(--space-12)", boxSizing: "border-box",
          background: open ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
          border: `1px solid ${open ? "var(--border-focus)" : "var(--border-light)"}`,
          borderRadius: "var(--r-inputs-radius)",
          color: selectedValue ? "var(--foreground-on-dark-primary-default)" : "var(--foreground-on-dark-secondary-default)",
          fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", fontWeight: 700, textAlign: "left",
          cursor: disabled ? "default" : "pointer", boxShadow: focusRing,
        }}
      >
        <span style={{ flex: 1, minWidth: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{selectedValue ?? placeholder}</span>
        <span style={{ display: "inline-flex", transform: open ? "rotate(180deg)" : "none", transition: "transform var(--motion-standard)" }}>
          <CmpIcon icon="expand_more" size={32} color="var(--foreground-on-dark-primary-default)" />
        </span>
      </button>
      {open && (
        <div
          style={{
            position: "absolute", left: 0, right: 0, top: "calc(100% + 8px)", zIndex: 40, padding: "var(--space-8)",
            background: "var(--surface-container-lowest)", border: "1px solid var(--border-light)",
            borderRadius: "var(--r-card-small-radius)", boxShadow: "var(--elevation-3)", maxHeight: 320, overflowY: "auto",
          }}
        >
          {options.map((opt, i) => {
            const sel = opt === selectedValue;
            return (
              <button
                key={i}
                type="button"
                onClick={() => { setOpen(false); onOptionSelected && onOptionSelected(i, opt); }}
                style={{
                  width: "100%", minHeight: 56, padding: "0 var(--space-16)", display: "flex", alignItems: "center", gap: "var(--space-12)",
                  border: "none", borderRadius: "var(--radius-s)", textAlign: "left", cursor: "pointer",
                  background: sel ? "var(--background-surface-highlight)" : "transparent",
                  color: "var(--foreground-on-dark-primary-default)", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph1-size)", fontWeight: sel ? 700 : 500,
                }}
              >
                <span style={{ flex: 1 }}>{opt}</span>
                {sel && <CmpIcon icon="check" size={24} color="var(--brand-primary-default)" />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
