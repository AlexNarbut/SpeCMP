import React, { useRef } from "react";

/**
 * CmpInputDigit — kit InputDigit (OTP / PIN): cells 56×72, digit title1, radius r-inputs.
 * One hidden input drives all cells (paste works). Focused cell: border-focus 2px.
 * Error: red border + shake-free (static) emphasis.
 */
export function CmpInputDigit({ value = "", onChange, length = 4, error = false, disabled = false, secure = false, autoFocus = false, style }) {
  const ref = useRef(null);
  const [focus, setFocus] = React.useState(false);
  const cells = Array.from({ length }, (_, i) => value[i] || "");
  const active = Math.min(value.length, length - 1);
  return (
    <div onClick={() => ref.current && ref.current.focus()} style={{ position: "relative", display: "flex", gap: "var(--space-8)", justifyContent: "center", opacity: disabled ? 0.4 : 1, ...style }}>
      <input
        ref={ref} value={value} disabled={disabled} autoFocus={autoFocus} inputMode="numeric" autoComplete="one-time-code" maxLength={length}
        onChange={(e) => onChange && onChange(e.target.value.replace(/\D/g, "").slice(0, length))}
        onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
        aria-label="Код"
        style={{ position: "absolute", inset: 0, opacity: 0, border: "none", background: "transparent", color: "transparent", caretColor: "transparent" }}
      />
      {cells.map((c, i) => {
        const on = focus && i === active;
        const bc = error ? "var(--error-default)" : on ? "var(--border-focus)" : c ? "var(--border-tertiary)" : "var(--border-light)";
        return (
          <span key={i} aria-hidden="true" style={{
            width: 56, height: 72, boxSizing: "border-box", borderRadius: "var(--r-inputs-radius)",
            border: `1px solid ${bc}`, boxShadow: on || error ? `inset 0 0 0 1px ${bc}` : "none",
            background: on ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
            display: "grid", placeItems: "center", color: "var(--foreground-on-dark-primary-default)",
            fontFamily: "var(--font-sans)", fontSize: "var(--t-title1-size)", fontWeight: 700, lineHeight: 1,
            transition: "border-color var(--motion-fast), background var(--motion-fast)",
          }}>{c ? (secure ? "•" : c) : on ? <span style={{ width: 2, height: 32, background: "var(--brand-primary-default)" }} /> : ""}</span>
        );
      })}
    </div>
  );
}
