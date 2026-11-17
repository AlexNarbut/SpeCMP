import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpSnackbar — kit TopToast: ink-filled bar (on-light text, headline3),
 * radius r-snackbar, min-height 56. Place it at the TOP of the screen.
 * `tone` adds a leading status icon.
 */
const TONES = { success: ["check_circle", "var(--success-default)"], error: ["error", "var(--error-default)"], info: ["info", "var(--brand-secondary-default)"] };

export function CmpSnackbar({ message, actionLabel, onActionClick, withDismissAction = false, onDismiss, tone, style }) {
  const t = TONES[tone];
  return (
    <div
      role="status"
      style={{
        display: "flex", alignItems: "center", gap: "var(--space-12)", minHeight: 56, boxSizing: "border-box",
        padding: "var(--space-8) var(--space-8) var(--space-8) var(--space-16)",
        background: "var(--background-surface-primary)", color: "var(--foreground-on-light-primary-default)",
        borderRadius: "var(--r-snackbar-radius)", boxShadow: "var(--elevation-3)",
        fontFamily: "var(--font-sans)", fontSize: "var(--t-headline3-size)", lineHeight: "var(--t-headline3-line)", fontWeight: 700,
        ...style,
      }}
    >
      {t && <CmpIcon icon={t[0]} size={24} color={t[1]} />}
      <span style={{ flex: 1, minWidth: 0, padding: "var(--space-8) 0" }}>{message}</span>
      {actionLabel && (
        <button
          type="button"
          onClick={onActionClick}
          style={{ minHeight: 48, padding: "0 var(--space-12)", border: "none", background: "transparent", color: "inherit", fontFamily: "inherit", fontSize: "inherit", fontWeight: 700, textTransform: "uppercase", textDecoration: "underline", textUnderlineOffset: 3, cursor: "pointer" }}
        >
          {actionLabel}
        </button>
      )}
      {withDismissAction && (
        <button type="button" aria-label="Закрыть" onClick={onDismiss} style={{ width: 48, height: 48, border: "none", background: "transparent", color: "inherit", display: "inline-flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}>
          <CmpIcon icon="close" size={24} color="currentColor" />
        </button>
      )}
    </div>
  );
}
