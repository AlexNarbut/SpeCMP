import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpContentError — kit ContentError: centered state block for empty/error/offline.
 * 80 icon circle, title title2 (one line), text headline2 @ 60%, optional action slot.
 * tone: error (red) · empty (ink) · offline (warning).
 */
export function CmpContentError({ icon, title, text, action, tone = "error", style }) {
  const t = { error: ["error", "var(--error-default)", "var(--error-highlight)"], empty: ["inbox", "var(--foreground-on-dark-primary-default)", "var(--background-surface-highlight)"], offline: ["wifi_off", "var(--warning-default)", "var(--warning-highlight)"] }[tone] || [];
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: "var(--space-16)", padding: "var(--space-32) var(--space-24)", maxWidth: 480, margin: "0 auto", ...style }}>
      <span style={{ width: 80, height: 80, borderRadius: "var(--r-round-radius)", background: t[2], display: "grid", placeItems: "center" }}>
        <CmpIcon icon={icon ?? t[0]} size={40} color={t[1]} />
      </span>
      <CmpText variant="title2" as="div">{title}</CmpText>
      {text && <CmpText variant="headline2" as="div" color="var(--foreground-on-dark-secondary-default)" style={{ textWrap: "pretty" }}>{text}</CmpText>}
      {action && <div style={{ marginTop: "var(--space-8)", display: "flex", flexDirection: "column", alignSelf: "stretch", gap: "var(--space-8)" }}>{action}</div>}
    </div>
  );
}
