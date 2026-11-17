import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpDialog — kit Modal: centered content over a blurred overlay.
 * Card surface, 1px border, radius r-dialog. Title = title2, text = headline2 @ 60%.
 * Buttons stack full-width (confirm on top) — big targets.
 * Overlay fills the nearest positioned ancestor.
 */
export function CmpDialog({ open = true, onDismissRequest, icon, title, text, confirmButton, dismissButton, style }) {
  if (!open) return null;
  return (
    <div
      onClick={onDismissRequest}
      style={{
        position: "absolute", inset: 0, zIndex: 60,
        background: "var(--background-overlay)",
        backdropFilter: "blur(var(--blur-sheet))", WebkitBackdropFilter: "blur(var(--blur-sheet))",
        display: "flex", alignItems: "center", justifyContent: "center", padding: "var(--space-24)",
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: "100%", maxWidth: 440, boxSizing: "border-box",
          background: "var(--surface-container-lowest)",
          color: "var(--foreground-on-dark-primary-default)",
          border: "1px solid var(--border-highlight)",
          borderRadius: "var(--r-dialog-radius)",
          padding: "var(--space-32) var(--space-24) var(--space-24)",
          boxShadow: "var(--elevation-3)",
          display: "flex", flexDirection: "column", gap: "var(--space-16)",
          alignItems: "center", textAlign: "center",
          ...style,
        }}
      >
        {icon && (
          <span style={{ width: 64, height: 64, borderRadius: "50%", display: "inline-flex", alignItems: "center", justifyContent: "center", background: "var(--background-surface-highlight)" }}>
            <CmpIcon icon={icon} size={32} color="var(--foreground-on-dark-primary-default)" />
          </span>
        )}
        {title && <CmpText variant="title2" as="div">{title}</CmpText>}
        {text && <CmpText variant="headline2" as="div" color="var(--foreground-on-dark-secondary-default)">{text}</CmpText>}
        {(confirmButton || dismissButton) && (
          <div style={{ alignSelf: "stretch", display: "flex", flexDirection: "column", gap: "var(--space-8)", marginTop: "var(--space-8)" }}>
            {confirmButton && <div style={{ display: "flex", flexDirection: "column" }}>{confirmButton}</div>}
            {dismissButton && <div style={{ display: "flex", flexDirection: "column" }}>{dismissButton}</div>}
          </div>
        )}
      </div>
    </div>
  );
}
