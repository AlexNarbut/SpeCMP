import React, { useState } from "react";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpTextField — outlined (default) or filled text input. Height 56dp,
 * floating label, optional leading/trailing icon, supporting text and
 * error state. Border outline → primary on focus, error on error.
 */
export function CmpTextField({
  value = "",
  onChange,
  label,
  placeholder,
  style: variant = "Outlined",
  disabled = false,
  readOnly = false,
  isError = false,
  singleLine = false,
  supportingText,
  leadingIcon,
  trailingIcon,
  type = "text",
  rootStyle,
  ...rest
}) {
  const [focus, setFocus] = useState(false);
  const filled = variant === "Filled";
  const floated = focus || value !== "" || placeholder;

  const borderColor = isError
    ? "var(--error)"
    : focus
    ? "var(--primary)"
    : "var(--outline)";
  const labelColor = isError
    ? "var(--error)"
    : focus
    ? "var(--primary)"
    : "var(--on-surface-variant)";
  const muted = disabled ? 0.38 : 1;

  return (
    <div style={{ width: "100%", opacity: muted, ...rootStyle }}>
      <div
        style={{
          position: "relative",
          display: "flex",
          alignItems: "center",
          gap: "var(--space-m)",
          minHeight: "var(--field-height)",
          padding: "0 16px",
          background: filled ? "var(--surface-container-highest)" : "transparent",
          border: filled ? "none" : `${focus ? 2 : 1}px solid ${borderColor}`,
          borderBottom: filled ? `${focus ? 2 : 1}px solid ${borderColor}` : undefined,
          borderRadius: filled ? "var(--radius-xs) var(--radius-xs) 0 0" : "var(--radius-xs)",
        }}
      >
        {leadingIcon && (
          <span style={{ color: "var(--on-surface-variant)", display: "inline-flex" }}>{leadingIcon}</span>
        )}
        {label && (
          <label
            style={{
              position: "absolute",
              left: leadingIcon ? 48 : 12,
              padding: "0 4px",
              background: filled ? "transparent" : "var(--surface)",
              pointerEvents: "none",
              color: labelColor,
              fontFamily: "var(--font-sans)",
              transition: "all var(--motion-fast)",
              top: floated ? -8 : "50%",
              transform: floated ? "none" : "translateY(-50%)",
              fontSize: floated ? "var(--body-small-size)" : "var(--body-large-size)",
              lineHeight: 1,
            }}
          >
            {label}
          </label>
        )}
        <input
          value={value}
          type={type}
          placeholder={floated ? placeholder : undefined}
          disabled={disabled}
          readOnly={readOnly}
          onChange={(e) => onChange && onChange(e.target.value)}
          onFocus={() => setFocus(true)}
          onBlur={() => setFocus(false)}
          style={{
            flex: 1,
            minWidth: 0,
            border: "none",
            outline: "none",
            background: "transparent",
            color: "var(--on-surface)",
            fontFamily: "var(--font-sans)",
            fontSize: "var(--body-large-size)",
            lineHeight: "var(--body-large-line)",
            padding: singleLine ? "0" : "16px 0",
          }}
          {...rest}
        />
        {trailingIcon && (
          <span style={{ color: isError ? "var(--error)" : "var(--on-surface-variant)", display: "inline-flex" }}>
            {trailingIcon}
          </span>
        )}
      </div>
      {supportingText && (
        <div style={{ padding: "4px 16px 0" }}>
          <CmpText variant="bodySmall" color={isError ? "var(--error)" : "var(--on-surface-variant)"}>
            {supportingText}
          </CmpText>
        </div>
      )}
    </div>
  );
}
