import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpBadge, CmpBadgedBox } from "../feedback/CmpBadge.jsx";

/**
 * CmpNavigationBar — frosted bottom bar, 1px top border. Selected item =
 * ink pill behind icon + ink label; others 60%. Labels additional1.
 * (Not used in the app today — kept for completeness.)
 */
export function CmpNavigationBar({ items = [], style }) {
  return (
    <nav
      style={{
        display: "flex", height: "var(--navbar-height)", boxSizing: "border-box",
        background: "var(--background-glass)", backdropFilter: "blur(var(--blur-glass))", WebkitBackdropFilter: "blur(var(--blur-glass))",
        borderTop: "1px solid var(--border-highlight)",
        ...style,
      }}
    >
      {items.map((it, i) => {
        const icon = <CmpIcon icon={it.icon} size={24} color="currentColor" />;
        return (
          <button
            key={i}
            type="button"
            onClick={it.onClick}
            style={{
              flex: 1, border: "none", background: "transparent", cursor: "pointer",
              display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "var(--space-4)",
              color: it.selected ? "var(--foreground-on-dark-primary-default)" : "var(--foreground-on-dark-secondary-default)",
            }}
          >
            <span
              style={{
                width: 64, height: 32, borderRadius: 9999, display: "inline-flex", alignItems: "center", justifyContent: "center",
                background: it.selected ? "var(--background-surface-primary)" : "transparent",
                color: it.selected ? "var(--foreground-on-light-primary-default)" : "inherit",
                transition: "background var(--motion-standard)",
              }}
            >
              {it.badge !== undefined ? <CmpBadgedBox badge={<CmpBadge count={it.badge} />}>{icon}</CmpBadgedBox> : icon}
            </span>
            {it.label && <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-additional1-size)", fontWeight: 700 }}>{it.label}</span>}
          </button>
        );
      })}
    </nav>
  );
}
