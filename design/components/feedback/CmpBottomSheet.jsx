import React from "react";

/**
 * CmpBottomSheet — kit BottomSheet: frosted surface (blur) over the overlay,
 * top radius r-sheet, 1px top border, 40×4 handle. Max-width 640 on wide windows.
 */
export function CmpBottomSheet({ open = true, onDismissRequest, children, containerColor, style }) {
  if (!open) return null;
  return (
    <div
      onClick={onDismissRequest}
      style={{ position: "absolute", inset: 0, zIndex: 50, background: "var(--background-overlay)", display: "flex", alignItems: "flex-end", justifyContent: "center" }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: "100%", maxWidth: 640, boxSizing: "border-box", maxHeight: "90%", overflowY: "auto",
          background: containerColor ?? "var(--surface-container-lowest)",
          color: "var(--foreground-on-dark-primary-default)",
          borderTop: "1px solid var(--border-light)",
          borderRadius: "var(--r-sheet-radius) var(--r-sheet-radius) 0 0",
          padding: "var(--space-12) var(--screen-padding-h) var(--space-32)",
          ...style,
        }}
      >
        <div style={{ width: 40, height: 4, borderRadius: 2, background: "var(--background-surface-tertiary)", margin: "0 auto var(--space-20)" }} />
        {children}
      </div>
    </div>
  );
}
