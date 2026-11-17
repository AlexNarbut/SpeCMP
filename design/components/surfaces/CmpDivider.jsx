import React from "react";

/** CmpDivider — 1px ink-10 line (border-highlight). */
export function CmpDivider({ orientation = "horizontal", thickness = 1, color = "var(--border-highlight)", inset = 0, style, ...rest }) {
  const h = orientation === "horizontal";
  return (
    <div
      role="separator"
      style={{
        flexShrink: 0, background: color,
        width: h ? "auto" : thickness, height: h ? thickness : "auto", alignSelf: h ? undefined : "stretch",
        marginLeft: h ? inset : 0,
        ...style,
      }}
      {...rest}
    />
  );
}
