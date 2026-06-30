import React from "react";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpTopAppBar — screen header. 64dp tall, background fill, onBackground
 * content. Slots: navigationIcon, title (+ optional subtitle), actions.
 * Style "Small" (leading title) or "CenterAligned".
 */
export function CmpTopAppBar({
  title,
  subtitle,
  navigationIcon,
  actions,
  centered = false,
  style,
  ...rest
}) {
  return (
    <header
      style={{
        display: "flex",
        alignItems: "center",
        height: "var(--topbar-height)",
        padding: "0 8px",
        background: "var(--background)",
        color: "var(--on-background)",
        gap: 8,
        ...style,
      }}
      {...rest}
    >
      {navigationIcon && <div style={{ display: "flex", flex: "0 0 auto" }}>{navigationIcon}</div>}
      <div
        style={{
          flex: 1,
          minWidth: 0,
          paddingLeft: 8,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: centered ? "center" : "flex-start",
        }}
      >
        {typeof title === "string" ? (
          <CmpText variant="titleLarge" maxLines={1}>{title}</CmpText>
        ) : (
          title
        )}
        {subtitle &&
          (typeof subtitle === "string" ? (
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)" maxLines={1}>{subtitle}</CmpText>
          ) : (
            subtitle
          ))}
      </div>
      {actions && (
        <div style={{ display: "flex", alignItems: "center", gap: 8, paddingRight: 8, flex: "0 0 auto" }}>
          {actions}
        </div>
      )}
    </header>
  );
}
