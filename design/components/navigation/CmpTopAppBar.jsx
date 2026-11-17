import React from "react";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpTopAppBar — kit TopBar: transparent 64 bar over the page, title headline1,
 * title headline2 19 (large → headline1 24), optional subtitle (additional0 @ 60%). Use CmpIconButton variant="Glass" for
 * navigation/actions. Horizontal padding 16 (icons sit near edges).
 */
export function CmpTopAppBar({ title, subtitle, navigationIcon, actions, centered = false, large = false, style }) {
  return (
    <div
      style={{
        position: "relative", display: "flex", alignItems: "center", gap: "var(--space-12)",
        minHeight: "var(--h-topbar)", padding: "var(--space-8) var(--space-16)", boxSizing: "border-box",
        background: "transparent", color: "var(--foreground-on-dark-primary-default)",
        ...style,
      }}
    >
      {navigationIcon}
      <div
        style={{
          flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2,
          alignItems: centered ? "center" : "flex-start", textAlign: centered ? "center" : "left",
          paddingLeft: navigationIcon || centered ? 0 : "var(--space-8)",
        }}
      >
        {title && <CmpText variant={large ? "headline1" : "headline2"} maxLines={1}>{title}</CmpText>}
        {subtitle && <CmpText variant="additional0" color="var(--foreground-on-dark-secondary-default)" maxLines={1}>{subtitle}</CmpText>}
      </div>
      {actions && <div style={{ display: "flex", alignItems: "center", gap: "var(--space-8)" }}>{actions}</div>}
      {centered && !actions && navigationIcon && <span style={{ width: 56, flexShrink: 0 }} />}
    </div>
  );
}
