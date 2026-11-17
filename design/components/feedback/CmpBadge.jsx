import React from "react";

/** CmpBadge — pill counter (additional1), brand blue by default; dot when no count. */
export function CmpBadge({ count, containerColor = "var(--brand-primary-default)", contentColor = "var(--on-brand)", style }) {
  const dot = count === undefined || count === null || count === "";
  return (
    <span
      style={{
        display: "inline-flex", alignItems: "center", justifyContent: "center", boxSizing: "border-box",
        minWidth: dot ? 8 : 20, height: dot ? 8 : 20, padding: dot ? 0 : "0 6px", borderRadius: 9999,
        background: containerColor, color: contentColor,
        fontFamily: "var(--font-sans)", fontSize: "var(--t-additional1-size)", fontWeight: 700, lineHeight: 1,
        ...style,
      }}
    >
      {dot ? null : count}
    </span>
  );
}

export function CmpBadgedBox({ badge, children, style }) {
  return (
    <span style={{ position: "relative", display: "inline-flex", ...style }}>
      {children}
      <span style={{ position: "absolute", top: 0, right: 0, transform: "translate(35%, -35%)" }}>{badge}</span>
    </span>
  );
}
