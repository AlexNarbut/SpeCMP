import React from "react";

/**
 * CmpCircularProgressIndicator — kit SpinningProgressBar: 30dp arc, ink track 15%,
 * brand-blue arc. progress null → spinning; 0..1 → determinate.
 */
export function CmpCircularProgressIndicator({ progress = null, size = 30, strokeWidth = 3, color = "var(--brand-primary-default)", trackColor = "var(--background-surface-medium-highlight)", style }) {
  const r = (size - strokeWidth) / 2;
  const c = 2 * Math.PI * r;
  const det = typeof progress === "number";
  const dash = det ? c * Math.max(0, Math.min(1, progress)) : c * 0.28;
  return (
    <span style={{ display: "inline-flex", width: size, height: size, flexShrink: 0, ...style }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ animation: det ? undefined : "cmp-spin 0.9s linear infinite", transform: det ? "rotate(-90deg)" : undefined }}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={trackColor} strokeWidth={strokeWidth} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeDasharray={`${dash} ${c}`} style={{ transition: "stroke-dasharray var(--motion-standard)" }} />
      </svg>
      <style>{"@keyframes cmp-spin{to{transform:rotate(360deg)}}"}</style>
    </span>
  );
}
