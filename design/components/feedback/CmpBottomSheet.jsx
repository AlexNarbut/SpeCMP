import React from "react";

/**
 * CmpBottomSheet — panel sliding up from the bottom. large radius (16dp)
 * on top corners, surface fill, drag handle, scrim behind. Scrim fills
 * the nearest positioned ancestor (wrap your screen in position:relative).
 */
export function CmpBottomSheet({
  open = true,
  onDismissRequest,
  children,
  containerColor = "var(--surface)",
  style,
}) {
  if (!open) return null;
  return (
    <div
      onClick={onDismissRequest}
      style={{
        position: "absolute",
        inset: 0,
        zIndex: 60,
        background: "color-mix(in srgb, var(--scrim) 40%, transparent)",
        display: "flex",
        alignItems: "flex-end",
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: "100%",
          maxHeight: "70%",
          overflowY: "auto",
          background: containerColor,
          color: "var(--on-surface)",
          borderRadius: "var(--radius-l) var(--radius-l) 0 0",
          padding: "var(--space-m) var(--space-l) var(--space-xl)",
          boxShadow: "var(--elevation-3)",
          ...style,
        }}
      >
        <div
          style={{
            width: 32,
            height: 4,
            borderRadius: 999,
            background: "var(--outline-variant)",
            margin: "0 auto var(--space-l)",
          }}
        />
        {children}
      </div>
    </div>
  );
}
