import React, { useState, useRef, useEffect } from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpDropdown — exposed dropdown menu. Read-only outlined field anchor;
 * placeholder 12sp, menu items 14sp. Min height 56dp, medium radius,
 * border outline → primary when open.
 */
export function CmpDropdown({
  selectedValue = null,
  placeholder = "Выберите",
  options = [],
  onOptionSelected,
  disabled = false,
  rootStyle,
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const close = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  return (
    <div ref={ref} style={{ position: "relative", width: "100%", opacity: disabled ? 0.38 : 1, ...rootStyle }}>
      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen((o) => !o)}
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "var(--space-s)",
          width: "100%",
          minHeight: "var(--field-height)",
          padding: "0 16px",
          background: "transparent",
          border: `${open ? 2 : 1}px solid ${open ? "var(--primary)" : "var(--outline)"}`,
          borderRadius: "var(--radius-m)",
          cursor: disabled ? "default" : "pointer",
          textAlign: "left",
        }}
      >
        {selectedValue ? (
          <CmpText variant="bodyLarge" color="var(--on-surface)">{selectedValue}</CmpText>
        ) : (
          <span style={{ fontFamily: "var(--font-sans)", fontSize: 12, color: "var(--on-surface-variant)" }}>
            {placeholder}
          </span>
        )}
        <CmpIcon icon={open ? "KeyboardArrowUp" : "KeyboardArrowDown"} size={24} color="var(--on-surface-variant)" />
      </button>

      {open && (
        <div
          style={{
            position: "absolute",
            top: "calc(100% + 4px)",
            left: 0,
            right: 0,
            zIndex: 20,
            background: "var(--surface-container)",
            borderRadius: "var(--radius-xs)",
            boxShadow: "var(--elevation-2)",
            padding: "8px 0",
            maxHeight: 280,
            overflowY: "auto",
          }}
        >
          {options.map((opt, i) => (
            <button
              key={i}
              type="button"
              onClick={() => {
                onOptionSelected && onOptionSelected(i, opt);
                setOpen(false);
              }}
              style={{
                display: "flex",
                alignItems: "center",
                width: "100%",
                minHeight: 48,
                padding: "0 16px",
                border: "none",
                background: opt === selectedValue ? "var(--surface-variant)" : "transparent",
                color: "var(--on-surface)",
                fontFamily: "var(--font-sans)",
                fontSize: 14,
                lineHeight: "20px",
                textAlign: "left",
                cursor: "pointer",
              }}
              onMouseEnter={(e) => {
                if (opt !== selectedValue) e.currentTarget.style.background = "color-mix(in srgb, var(--on-surface) 8%, transparent)";
              }}
              onMouseLeave={(e) => {
                if (opt !== selectedValue) e.currentTarget.style.background = "transparent";
              }}
            >
              {opt}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
