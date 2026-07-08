import React from "react";

/** Material 3 type-scale role → CSS token pair. */
const VARIANTS = {
  displayLarge: ["--display-large-size", "--display-large-line", 400],
  displayMedium: ["--display-medium-size", "--display-medium-line", 400],
  displaySmall: ["--display-small-size", "--display-small-line", 400],
  headlineLarge: ["--headline-large-size", "--headline-large-line", 400],
  headlineMedium: ["--headline-medium-size", "--headline-medium-line", 400],
  headlineSmall: ["--headline-small-size", "--headline-small-line", 400],
  titleLarge: ["--title-large-size", "--title-large-line", 500],
  titleMedium: ["--title-medium-size", "--title-medium-line", 500],
  titleSmall: ["--title-small-size", "--title-small-line", 500],
  bodyLarge: ["--body-large-size", "--body-large-line", 400],
  bodyMedium: ["--body-medium-size", "--body-medium-line", 400],
  bodySmall: ["--body-small-size", "--body-small-line", 400],
  labelLarge: ["--label-large-size", "--label-large-line", 500],
  labelMedium: ["--label-medium-size", "--label-medium-line", 500],
  labelSmall: ["--label-small-size", "--label-small-line", 500],
};

/**
 * CmpText — every piece of text. Pick a `variant` from the M3 type
 * scale (designer chooses a role, not a raw size). `color` inherits
 * (currentColor) by default; override with a role token where needed.
 */
export function CmpText({
  children,
  variant = "bodyLarge",
  color = "currentColor",
  weight,
  align,
  maxLines,
  as: Tag = "span",
  style,
  ...rest
}) {
  const [sizeVar, lineVar, defWeight] = VARIANTS[variant] || VARIANTS.bodyLarge;
  const clamp = maxLines
    ? {
        display: "-webkit-box",
        WebkitLineClamp: maxLines,
        WebkitBoxOrient: "vertical",
        overflow: "hidden",
      }
    : null;
  return (
    <Tag
      style={{
        margin: 0,
        fontFamily: "var(--font-sans)",
        fontSize: `var(${sizeVar})`,
        lineHeight: `var(${lineVar})`,
        fontWeight: weight ?? defWeight,
        color,
        textAlign: align,
        ...clamp,
        ...style,
      }}
      {...rest}
    >
      {children}
    </Tag>
  );
}
