import React from "react";

/**
 * CmpLinearProgressIndicator — determinate when `progress` (0..1)
 * is given, otherwise indeterminate. Track = surfaceVariant.
 */
export function CmpLinearProgressIndicator({
  progress = null,
  color = "var(--primary)",
  trackColor = "var(--surface-variant)",
  height = 4,
  style,
  ...rest
}) {
  const indeterminate = progress == null;
  return (
    <div
      role="progressbar"
      aria-valuenow={indeterminate ? undefined : Math.round(progress * 100)}
      style={{
        width: "100%",
        height,
        borderRadius: height,
        background: trackColor,
        overflow: "hidden",
        position: "relative",
        ...style,
      }}
      {...rest}
    >
      <div
        style={
          indeterminate
            ? {
                position: "absolute",
                height: "100%",
                width: "40%",
                borderRadius: height,
                background: color,
                animation: "cmp-linear 1.4s ease-in-out infinite",
              }
            : {
                height: "100%",
                width: `${Math.max(0, Math.min(1, progress)) * 100}%`,
                borderRadius: height,
                background: color,
                transition: "width var(--motion-standard)",
              }
        }
      />
      <style>{`@keyframes cmp-linear{0%{left:-40%}100%{left:100%}}`}</style>
    </div>
  );
}
