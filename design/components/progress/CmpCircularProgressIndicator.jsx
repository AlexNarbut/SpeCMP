import React from "react";

/**
 * CmpCircularProgressIndicator — determinate when `progress` (0..1)
 * is given, otherwise indeterminate (spins). Arc thickness 4dp.
 */
export function CmpCircularProgressIndicator({
  progress = null,
  size = 48,
  strokeWidth = 4,
  color = "var(--primary)",
  trackColor = "transparent",
  style,
  ...rest
}) {
  const r = (size - strokeWidth) / 2;
  const c = 2 * Math.PI * r;
  const indeterminate = progress == null;
  const dash = indeterminate ? c * 0.25 : c * progress;
  return (
    <span
      role="progressbar"
      aria-valuenow={indeterminate ? undefined : Math.round(progress * 100)}
      style={{ display: "inline-flex", width: size, height: size, ...style }}
      {...rest}
    >
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        {trackColor !== "transparent" && (
          <circle cx={size / 2} cy={size / 2} r={r} fill="none"
            stroke={trackColor} strokeWidth={strokeWidth} />
        )}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={`${dash} ${c}`}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
          style={
            indeterminate
              ? { transformOrigin: "center", animation: "cmp-spin 1s linear infinite" }
              : { transition: "stroke-dasharray var(--motion-standard)" }
          }
        />
      </svg>
      <style>{`@keyframes cmp-spin{to{transform:rotate(270deg)}}`}</style>
    </span>
  );
}
