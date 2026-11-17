import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpListItem — kit ListItem: min-height 80, ink 5% fill + 1px border, radius r-list-item.
 * Leading icon in a 48 circle, title headline2, subtitle paragraph3 @ 60%,
 * trailing slot (default chevron when clickable). `status` adds a colored dot.
 */
export function CmpListItem({ icon, title, subtitle, trailing, onClick, status, disabled = false, style }) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  const click = !!onClick && !disabled;
  const dot = { ok: "var(--success-default)", warn: "var(--warning-default)", error: "var(--error-default)", info: "var(--brand-primary-default)" }[status];
  return (
    <div
      role={click ? "button" : undefined}
      tabIndex={click ? 0 : undefined}
      onClick={click ? onClick : undefined}
      {...(click ? handlers : {})}
      style={{
        position: "relative", overflow: "hidden", boxSizing: "border-box", minHeight: "var(--h-list-item)",
        display: "flex", alignItems: "center", gap: "var(--space-16)", padding: "var(--space-12) var(--space-16) var(--space-12) var(--space-16)",
        background: "var(--background-surface-ultra-highlight)", border: "var(--w-list-item-width) solid var(--border-highlight)",
        borderRadius: "var(--r-list-item-radius)", color: "var(--foreground-on-dark-primary-default)",
        cursor: click ? "pointer" : undefined, opacity: disabled ? 0.4 : 1, boxShadow: focusRing,
        ...(click ? pressStyle : {}), ...style,
      }}
    >
      {click && <span style={{ position: "absolute", inset: 0, background: "currentColor", opacity, pointerEvents: "none" }} />}
      {icon && (
        <span style={{ position: "relative", width: 48, height: 48, flexShrink: 0, borderRadius: "var(--r-icon-radius)", background: "var(--background-surface-highlight)", display: "grid", placeItems: "center" }}>
          {typeof icon === "string" ? <CmpIcon icon={icon} size={24} color="currentColor" /> : icon}
          {dot && <span style={{ position: "absolute", right: 0, bottom: 0, width: 12, height: 12, borderRadius: "50%", background: dot, border: "2px solid var(--background-page-primary)" }} />}
        </span>
      )}
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: "var(--space-4)" }}>
        <CmpText variant="headline2" maxLines={1}>{title}</CmpText>
        {subtitle && <CmpText variant="paragraph3" color="var(--foreground-on-dark-secondary-default)" maxLines={2}>{subtitle}</CmpText>}
      </div>
      {trailing ?? (click && <CmpIcon icon="chevron_right" size={32} color="var(--foreground-on-dark-secondary-default)" />)}
    </div>
  );
}

/**
 * CmpInfoRow — kit InfoRow: label (additional0 @ 60%) above value (headline2), min-height 88,
 * optional trailing action (e.g. copy). Bottom divider unless `last`.
 */
export function CmpInfoRow({ label, value, mono = false, accent = false, trailing, last = false, style }) {
  return (
    <div style={{ minHeight: "var(--h-info-row)", boxSizing: "border-box", display: "flex", alignItems: "center", gap: "var(--space-12)", padding: "var(--space-12) 0", borderBottom: last ? "none" : "1px solid var(--border-highlight)", ...style }}>
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: "var(--space-4)" }}>
        <CmpText variant="additional0" color="var(--foreground-on-dark-secondary-default)">{label}</CmpText>
        <CmpText variant="headline2" color={accent ? "var(--brand-primary-default)" : "var(--foreground-on-dark-primary-default)"} style={{ fontFamily: mono ? "var(--font-mono)" : undefined, overflowWrap: "anywhere" }}>{value}</CmpText>
      </div>
      {trailing}
    </div>
  );
}
