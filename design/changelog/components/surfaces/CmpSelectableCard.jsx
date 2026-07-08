import React from "react";

/**
 * CmpSelectableCard — card with a selected state. 16dp padding, 1.5dp
 * border, medium radius, 300ms color transition.
 * Selected: primary border + primaryContainer fill.
 * Unselected: outline border + surface fill.
 */
export function CmpSelectableCard({
  selected = false,
  onClick,
  icon,
  children,
  style,
  ...rest
}) {
  return (
    <div
      role="button"
      aria-pressed={selected}
      onClick={onClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: "var(--space-m)",
        width: "100%",
        padding: "var(--space-l)",
        borderRadius: "var(--radius-m)",
        border: `1.5px solid ${selected ? "var(--primary)" : "var(--outline)"}`,
        background: selected ? "var(--primary-container)" : "var(--surface)",
        color: selected ? "var(--on-primary-container)" : "var(--on-surface)",
        cursor: "pointer",
        transition: "background 300ms cubic-bezier(0.2,0,0,1), border-color 300ms cubic-bezier(0.2,0,0,1)",
        boxSizing: "border-box",
        ...style,
      }}
      {...rest}
    >
      {icon && <span style={{ display: "inline-flex", flex: "0 0 auto" }}>{icon}</span>}
      <div style={{ flex: 1, minWidth: 0 }}>{children}</div>
    </div>
  );
}
