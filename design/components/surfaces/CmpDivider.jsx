import React from "react";

/**
 * CmpDivider — 1dp separator. Horizontal by default; pass
 * `orientation="vertical"` for a vertical rule. Color defaults to
 * outlineVariant.
 */
export function CmpDivider({
  orientation = "horizontal",
  thickness = 1,
  color = "var(--outline-variant)",
  inset = 0,
  style,
  ...rest
}) {
  const vertical = orientation === "vertical";
  return (
    <div
      role="separator"
      aria-orientation={orientation}
      style={
        vertical
          ? {
              width: `${thickness}px`,
              alignSelf: "stretch",
              background: color,
              marginBlock: inset,
              flex: "0 0 auto",
              ...style,
            }
          : {
              height: `${thickness}px`,
              width: "100%",
              background: color,
              marginInline: inset,
              border: "none",
              ...style,
            }
      }
      {...rest}
    />
  );
}
