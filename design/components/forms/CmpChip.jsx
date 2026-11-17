import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

/**
 * CmpChip — kit Chips: pill 48 high, label headline3. Off: ink 5% fill + vertical-gradient
 * 1px border. Selected: ink fill + on-light text + check. Optional leading icon / onRemove.
 */
export function CmpChip({ label, selected = false, icon, onClick, onRemove, disabled = false, style }) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  return (
    <button
      type="button"
      aria-pressed={selected}
      disabled={disabled}
      onClick={disabled ? undefined : onClick}
      {...handlers}
      style={{
        position: "relative", overflow: "hidden", height: "var(--h-chip)", boxSizing: "border-box", flexShrink: 0,
        display: "inline-flex", alignItems: "center", gap: "var(--space-8)", padding: `0 ${onRemove ? 8 : 20}px 0 ${selected || icon ? 14 : 20}px`,
        borderRadius: "var(--r-chips-radius)",
        border: "var(--w-chips-width) solid transparent",
        background: selected
          ? "linear-gradient(var(--background-surface-primary), var(--background-surface-primary)) padding-box, linear-gradient(var(--border-light), var(--border-ultra-highlight)) border-box"
          : "linear-gradient(var(--background-surface-ultra-highlight), var(--background-surface-ultra-highlight)) padding-box, linear-gradient(var(--border-light), var(--border-ultra-highlight)) border-box",
        color: selected ? "var(--foreground-on-light-primary-default)" : "var(--foreground-on-dark-primary-default)",
        fontFamily: "var(--font-sans)", fontSize: "var(--t-headline3-size)", fontWeight: 700, whiteSpace: "nowrap",
        cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.4 : 1, boxShadow: focusRing,
        transition: "background 300ms, color 300ms", ...(disabled ? {} : pressStyle), ...style,
      }}
    >
      <span style={{ position: "absolute", inset: 0, background: "currentColor", opacity, pointerEvents: "none" }} />
      {selected ? <CmpIcon icon="check" size={20} color="currentColor" /> : icon && <CmpIcon icon={icon} size={20} color="currentColor" />}
      {label}
      {onRemove && (
        <span role="button" aria-label="Убрать" onClick={(e) => { e.stopPropagation(); onRemove(); }} style={{ width: 32, height: 32, borderRadius: "50%", display: "grid", placeItems: "center" }}>
          <CmpIcon icon="close" size={18} color="currentColor" />
        </span>
      )}
    </button>
  );
}
