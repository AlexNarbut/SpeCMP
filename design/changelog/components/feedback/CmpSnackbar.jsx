import React from "react";
import { CmpText } from "../app-kit/CmpText.jsx";
import { CmpTextButton } from "../buttons/CmpTextButton.jsx";

/**
 * CmpSnackbar — brief notification. Inverse surface fill; action label
 * uses inversePrimary. Optional dismiss (✕) action.
 */
export function CmpSnackbar({
  message,
  actionLabel,
  onActionClick,
  withDismissAction = false,
  onDismiss,
  style,
}) {
  return (
    <div
      role="status"
      style={{
        display: "flex",
        alignItems: "center",
        gap: "var(--space-s)",
        minHeight: 48,
        maxWidth: 560,
        padding: "8px 8px 8px 16px",
        borderRadius: "var(--radius-xs)",
        background: "var(--inverse-surface)",
        color: "var(--inverse-on-surface)",
        boxShadow: "var(--elevation-3)",
        ...style,
      }}
    >
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="bodyMedium" color="var(--inverse-on-surface)">{message}</CmpText>
      </div>
      {actionLabel && (
        <button
          type="button"
          onClick={onActionClick}
          style={{
            border: "none",
            background: "transparent",
            color: "var(--inverse-primary)",
            fontFamily: "var(--font-sans)",
            fontSize: "var(--label-large-size)",
            fontWeight: "var(--weight-medium)",
            padding: "10px 12px",
            borderRadius: 999,
            cursor: "pointer",
          }}
        >
          {actionLabel}
        </button>
      )}
      {withDismissAction && (
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Закрыть"
          style={{
            border: "none",
            background: "transparent",
            color: "var(--inverse-on-surface)",
            cursor: "pointer",
            width: 40,
            height: 40,
            borderRadius: 999,
            fontSize: 18,
          }}
        >
          ✕
        </button>
      )}
    </div>
  );
}
