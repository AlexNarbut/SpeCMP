import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpSelectableCard — kit SelectableListItem: min-height 80, radius r-list-item.
 * Off: ink 5% fill + 1px ink-10 border. On: 2px ink border + ink check circle.
 */
export function CmpSelectableCard({ selected = false, onClick, icon, children, style }) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  return (
    <div
      role="button"
      tabIndex={0}
      aria-pressed={selected}
      onClick={onClick}
      {...handlers}
      style={{
        position: "relative", overflow: "hidden", boxSizing: "border-box",
        minHeight: "var(--h-list-item)", padding: "var(--space-16) var(--space-20)",
        display: "flex", alignItems: "center", gap: "var(--space-16)",
        background: selected ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
        color: "var(--foreground-on-dark-primary-default)",
        border: selected ? "2px solid var(--border-primary)" : "1px solid var(--border-highlight)",
        borderRadius: "var(--r-list-item-radius)",
        cursor: "pointer", boxShadow: focusRing,
        ...pressStyle,
        ...style,
      }}
    >
      <span style={{ position: "absolute", inset: 0, background: "var(--foreground-on-dark-primary-default)", opacity, pointerEvents: "none" }} />
      {icon && <span style={{ display: "inline-flex", flexShrink: 0 }}>{icon}</span>}
      <div style={{ flex: 1, minWidth: 0 }}>{children}</div>
      <span
        style={{
          width: 28, height: 28, borderRadius: "50%", flexShrink: 0, boxSizing: "border-box",
          display: "inline-flex", alignItems: "center", justifyContent: "center",
          background: selected ? "var(--background-surface-primary)" : "transparent",
          border: selected ? "none" : "1.5px solid var(--border-tertiary)",
        }}
      >
        {selected && <CmpIcon icon="check" size={20} color="var(--foreground-on-light-primary-default)" />}
      </span>
    </div>
  );
}
