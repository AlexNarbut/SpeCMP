import React from "react";

/** CmpLinearProgressIndicator — 4dp track (ink 15%), brand-blue fill; null → sweeping. */
export function CmpLinearProgressIndicator({ progress = null, color = "var(--brand-primary-default)", trackColor = "var(--background-surface-medium-highlight)", height = 4, style }) {
  const det = typeof progress === "number";
  return (
    <div style={{ position: "relative", width: "100%", height, borderRadius: height, background: trackColor, overflow: "hidden", ...style }}>
      <div
        style={{
          position: "absolute", top: 0, bottom: 0, borderRadius: height, background: color,
          left: det ? 0 : undefined,
          width: det ? `${Math.max(0, Math.min(1, progress)) * 100}%` : "35%",
          animation: det ? undefined : "cmp-sweep 1.4s cubic-bezier(0.4,0,0.2,1) infinite",
          transition: "width var(--motion-standard)",
        }}
      />
      <style>{"@keyframes cmp-sweep{0%{left:-35%}100%{left:100%}}"}</style>
    </div>
  );
}
