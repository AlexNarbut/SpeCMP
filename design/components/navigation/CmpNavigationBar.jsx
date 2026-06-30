import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpNavigationBar — bottom section switcher. 80dp tall. Each item:
 * 24dp icon (in a pill indicator when selected) + optional label.
 * items: [{ icon, label, selected, onClick, badge }]
 */
export function CmpNavigationBar({ items = [], style, ...rest }) {
  return (
    <nav
      style={{
        display: "flex",
        height: "var(--navbar-height)",
        background: "var(--surface-container)",
        borderTop: "1px solid var(--outline-variant)",
        ...style,
      }}
      {...rest}
    >
      {items.map((item, i) => {
        const selected = item.selected;
        const color = selected ? "var(--on-secondary-container)" : "var(--on-surface-variant)";
        return (
          <button
            key={i}
            type="button"
            onClick={item.onClick}
            aria-current={selected ? "page" : undefined}
            style={{
              flex: 1,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              gap: 4,
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "12px 0",
            }}
          >
            <span
              style={{
                position: "relative",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                width: 64,
                height: 32,
                borderRadius: 999,
                background: selected ? "var(--secondary-container)" : "transparent",
                transition: "background var(--motion-fast)",
              }}
            >
              <CmpIcon icon={item.icon} size={24} color={color} />
              {item.badge != null && (
                <span
                  style={{
                    position: "absolute",
                    top: 2,
                    right: 14,
                    minWidth: 16,
                    height: 16,
                    padding: "0 4px",
                    borderRadius: 999,
                    background: "var(--error)",
                    color: "var(--on-error)",
                    fontSize: 11,
                    fontFamily: "var(--font-sans)",
                    fontWeight: 500,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  {item.badge}
                </span>
              )}
            </span>
            {item.label && (
              <CmpText variant="labelMedium" color={color}>{item.label}</CmpText>
            )}
          </button>
        );
      })}
    </nav>
  );
}
