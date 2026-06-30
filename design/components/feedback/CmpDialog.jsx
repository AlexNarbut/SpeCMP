import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpDialog — modal confirmation/choice. extraLarge radius (28dp),
 * surface fill, scrim behind. Slots: optional icon, title, text,
 * confirmButton, dismissButton. Scrim fills the nearest positioned
 * ancestor (wrap your screen in position:relative).
 */
export function CmpDialog({
  open = true,
  onDismissRequest,
  icon,
  title,
  text,
  confirmButton,
  dismissButton,
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
        alignItems: "center",
        justifyContent: "center",
        padding: "var(--space-xl)",
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: "100%",
          maxWidth: 360,
          background: "var(--surface-container-high)",
          color: "var(--on-surface)",
          borderRadius: "var(--radius-xl)",
          padding: "var(--space-xl)",
          boxShadow: "var(--elevation-3)",
          display: "flex",
          flexDirection: "column",
          gap: "var(--space-l)",
          alignItems: icon ? "center" : "stretch",
          textAlign: icon ? "center" : "left",
          ...style,
        }}
      >
        {icon && <CmpIcon icon={icon} size={24} color="var(--secondary)" />}
        {title && <CmpText variant="headlineSmall">{title}</CmpText>}
        {text && (
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{text}</CmpText>
        )}
        <div style={{ display: "flex", justifyContent: "flex-end", gap: "var(--space-s)", flexWrap: "wrap" }}>
          {dismissButton}
          {confirmButton}
        </div>
      </div>
    </div>
  );
}
