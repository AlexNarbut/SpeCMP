import React, { useState } from "react";
import { CmpText } from "../app-kit/CmpText.jsx";

/**
 * CmpTextField — kit InputMain. Height 72, translucent ink fill + 1px border,
 * radius r-inputs. Floating label (additional0) above value (headline2).
 * Focus: border-focus 2px. Error: error-default border + hint (red, our decision).
 * `style="Filled"` kept for API compat (same look).
 */
export function CmpTextField({
  value = "",
  onChange,
  label,
  placeholder,
  style: _variant,
  disabled = false,
  readOnly = false,
  isError = false,
  singleLine = true,
  supportingText,
  leadingIcon,
  trailingIcon,
  type = "text",
  rootStyle,
  ...rest
}) {
  const [focus, setFocus] = useState(false);
  const floated = focus || value !== "" || !!placeholder;
  const borderColor = isError ? "var(--error-default)" : focus ? "var(--border-focus)" : "var(--border-light)";
  const labelColor = isError ? "var(--error-default)" : "var(--foreground-on-dark-secondary-default)";

  return (
    <div style={{ width: "100%", opacity: disabled ? 0.4 : 1, display: "flex", flexDirection: "column", gap: "var(--space-8)", ...rootStyle }}>
      <div
        style={{
          position: "relative",
          display: "flex",
          alignItems: "center",
          gap: "var(--space-12)",
          minHeight: "var(--h-input)",
          padding: "0 var(--space-20)",
          background: focus ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
          border: `var(--w-inputs-width) solid ${borderColor}`,
          boxShadow: focus || isError ? `inset 0 0 0 1px ${borderColor}` : "none",
          borderRadius: "var(--r-inputs-radius)",
          transition: "background var(--motion-fast), border-color var(--motion-fast)",
          boxSizing: "border-box",
        }}
      >
        {leadingIcon && <span style={{ color: "var(--foreground-on-dark-secondary-default)", display: "inline-flex" }}>{leadingIcon}</span>}
        <div style={{ position: "relative", flex: 1, minWidth: 0, alignSelf: "stretch", display: "flex", flexDirection: "column", justifyContent: "center" }}>
          {label && (
            <label
              style={{
                position: "absolute",
                left: 0,
                pointerEvents: "none",
                color: labelColor,
                fontFamily: "var(--font-sans)",
                fontWeight: floated ? 700 : 500,
                transition: "all var(--motion-fast)",
                top: floated ? 12 : "50%",
                transform: floated ? "none" : "translateY(-50%)",
                fontSize: floated ? "var(--t-additional0-size)" : "var(--t-paragraph1-size)",
                lineHeight: 1.2,
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
              width: "100%",
              border: "none",
              outline: "none",
              background: "transparent",
              color: "var(--foreground-on-dark-primary-default)",
              caretColor: "var(--brand-primary-default)",
              fontFamily: "var(--font-sans)",
              fontSize: "var(--t-headline2-size)",
              fontWeight: 700,
              lineHeight: "var(--t-headline2-line)",
              padding: label ? "26px 0 8px" : 0,
            }}
            {...rest}
          />
        </div>
        {trailingIcon && (
          <span style={{ color: isError ? "var(--error-default)" : "var(--foreground-on-dark-secondary-default)", display: "inline-flex" }}>{trailingIcon}</span>
        )}
      </div>
      {supportingText && (
        <CmpText variant="headline3" color={isError ? "var(--error-default)" : "var(--foreground-on-dark-secondary-default)"} style={{ padding: "0 var(--space-4)" }}>
          {supportingText}
        </CmpText>
      )}
    </div>
  );
}
