import React from "react";

/**
 * CmpBadge — count or dot marker over an element. Error fill, onError
 * text, labelSmall. Wrap a target with CmpBadgedBox to position it.
 */
export function CmpBadge({ count, containerColor, contentColor, style }) {
  const dot = count == null;
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        minWidth: dot ? 6 : 16,
        height: dot ? 6 : 16,
        padding: dot ? 0 : "0 4px",
        borderRadius: 999,
        background: containerColor || "var(--error)",
        color: contentColor || "var(--on-error)",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--label-small-size)",
        fontWeight: "var(--weight-medium)",
        lineHeight: 1,
        ...style,
      }}
    >
      {!dot && count}
    </span>
  );
}

/** Positions a CmpBadge over the top-right of its child. */
export function CmpBadgedBox({ badge, children, style }) {
  return (
    <span style={{ position: "relative", display: "inline-flex", ...style }}>
      {children}
      <span style={{ position: "absolute", top: -2, right: -2 }}>{badge}</span>
    </span>
  );
}
