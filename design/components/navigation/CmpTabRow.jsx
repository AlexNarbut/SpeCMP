import React from "react";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpTabRow — kit SegmentedTabs: translucent ink track (radius r-controls),
 * selected segment = ink fill + on-light text. Tab height 48, label headline3 UPPERCASE.
 * `scrollable` → horizontal scroll with auto-width segments.
 * `inset` — horizontal margin so the track aligns with content (default 16; 0 = full width).
 */
export function CmpTabRow({ tabs = [], selectedTabIndex = 0, onTabSelected, scrollable = false, inset = 16, style }) {
  return (
    <div
      role="tablist"
      style={{
        display: "flex", gap: "var(--space-4)", padding: "var(--space-4)",
        background: "var(--background-surface-ultra-highlight)", border: "1px solid var(--border-highlight)",
        borderRadius: "calc(var(--r-controls-radius) + 4px)", overflowX: scrollable ? "auto" : "hidden",
        margin: `var(--space-8) ${typeof inset === "number" ? inset + "px" : inset}`, flexShrink: 0,
        ...style,
      }}
    >
      {tabs.map((t, i) => {
        const sel = i === selectedTabIndex;
        return (
          <button
            key={i}
            type="button"
            role="tab"
            aria-selected={sel}
            onClick={() => onTabSelected && onTabSelected(i)}
            style={{
              flex: scrollable ? "0 0 auto" : "1 1 0", minWidth: 0, height: "var(--h-tab)", padding: "0 var(--space-16)",
              display: "inline-flex", alignItems: "center", justifyContent: "center", gap: "var(--space-8)",
              border: "none", borderRadius: "var(--r-controls-radius)", cursor: "pointer",
              background: sel ? "var(--background-surface-primary)" : "transparent",
              color: sel ? "var(--foreground-on-light-primary-default)" : "var(--foreground-on-dark-secondary-default)",
              fontFamily: "var(--font-sans)", fontSize: "var(--t-headline3-size)", fontWeight: 700, textTransform: "uppercase",
              whiteSpace: "nowrap", transition: "background 600ms, color 600ms",
            }}
          >
            {t.icon && <CmpIcon icon={t.icon} size={24} color="currentColor" />}
            <span style={{ overflow: "hidden", textOverflow: "ellipsis" }}>{t.title}</span>
          </button>
        );
      })}
    </div>
  );
}
