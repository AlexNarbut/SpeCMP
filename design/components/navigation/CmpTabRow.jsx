import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpTabRow — switch views within a screen. Fixed (equal width) or
 * scrollable. Label = labelLarge; selected tab + indicator use primary.
 * tabs: [{ title, icon }]
 */
export function CmpTabRow({
  tabs = [],
  selectedTabIndex = 0,
  onTabSelect,
  scrollable = false,
  style,
  ...rest
}) {
  return (
    <div
      role="tablist"
      style={{
        display: "flex",
        overflowX: scrollable ? "auto" : "visible",
        borderBottom: "1px solid var(--outline-variant)",
        background: "var(--background)",
        ...style,
      }}
      {...rest}
    >
      {tabs.map((tab, i) => {
        const selected = i === selectedTabIndex;
        const color = selected ? "var(--primary)" : "var(--on-surface-variant)";
        return (
          <button
            key={i}
            role="tab"
            aria-selected={selected}
            type="button"
            onClick={() => onTabSelect && onTabSelect(i)}
            style={{
              position: "relative",
              flex: scrollable ? "0 0 auto" : 1,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              gap: 4,
              minHeight: 48,
              padding: scrollable ? "0 24px" : "0 16px",
              border: "none",
              background: "transparent",
              color,
              cursor: "pointer",
              fontFamily: "var(--font-sans)",
              fontSize: "var(--label-large-size)",
              lineHeight: "var(--label-large-line)",
              fontWeight: "var(--weight-medium)",
              whiteSpace: "nowrap",
            }}
          >
            {tab.icon && <CmpIcon icon={tab.icon} size={24} color={color} />}
            {tab.title}
            <span
              style={{
                position: "absolute",
                bottom: 0,
                left: scrollable ? 24 : 12,
                right: scrollable ? 24 : 12,
                height: 3,
                borderRadius: "3px 3px 0 0",
                background: selected ? "var(--primary)" : "transparent",
                transition: "background var(--motion-fast)",
              }}
            />
          </button>
        );
      })}
    </div>
  );
}
