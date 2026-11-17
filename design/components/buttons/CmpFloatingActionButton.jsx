import React from "react";
import { useStateLayer } from "../app-kit/useStateLayer.js";
import { CmpIcon } from "../app-kit/CmpIcon.jsx";

const SIZES = { Small: [48, 24], Default: [56, 24], Large: [96, 36] };

/** CmpFloatingActionButton — blue accent circle, no shadow, press scale .96. */
export function CmpFloatingActionButton({ icon = "add", onClick, size = "Default", contentDescription, children, style, ...rest }) {
  const { opacity, handlers, pressStyle, focusRing } = useStateLayer();
  const [box, ic] = SIZES[size] || SIZES.Default;
  return (
    <button
      type="button"
      aria-label={contentDescription}
      onClick={onClick}
      {...handlers}
      style={{
        position: "relative",
        width: box,
        height: box,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        border: "none",
        borderRadius: "var(--r-round-radius)",
        background: "var(--brand-primary-default)",
        color: "var(--on-brand)",
        cursor: "pointer",
        overflow: "hidden",
        boxShadow: focusRing,
        ...pressStyle,
        ...style,
      }}
      {...rest}
    >
      <span style={{ position: "absolute", inset: 0, background: "#FFFFFF", opacity, pointerEvents: "none" }} />
      {children ?? <CmpIcon icon={icon} size={ic} color="currentColor" />}
    </button>
  );
}
