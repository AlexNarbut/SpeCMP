import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

const SIZES = {
  Small: { box: 40, radius: 12, icon: 24 },
  Default: { box: 56, radius: 16, icon: 24 },
  Large: { box: 96, radius: 28, icon: 36 },
};

/**
 * CmpFloatingActionButton — floating primary action. Sizes Small (40),
 * Default (56), Large (96). Background = primaryContainer.
 */
export function CmpFloatingActionButton({
  icon,
  onClick,
  size = "Default",
  contentDescription,
  children,
  style,
  ...rest
}) {
  const { opacity, handlers } = useStateLayer();
  const s = SIZES[size] || SIZES.Default;
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={contentDescription}
      {...handlers}
      style={{
        position: "relative",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        width: s.box,
        height: s.box,
        border: "none",
        borderRadius: s.radius,
        background: "var(--primary-container)",
        color: "var(--on-primary-container)",
        boxShadow: "var(--elevation-3)",
        cursor: "pointer",
        overflow: "hidden",
        ...style,
      }}
      {...rest}
    >
      <span style={{ position: "absolute", inset: 0, background: "var(--on-primary-container)", opacity, pointerEvents: "none" }} />
      {children || <CmpIcon icon={icon} size={s.icon} color="currentColor" title={contentDescription} />}
    </button>
  );
}
