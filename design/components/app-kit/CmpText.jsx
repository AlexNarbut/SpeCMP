import React from "react";

/** Style scale (UI kit): name → [size var, line var, weight]. */
const SCALE = {
  accent1: ["--t-accent1-size", "--t-accent1-line", 700],
  accent2: ["--t-accent2-size", "--t-accent2-line", 700],
  accent3: ["--t-accent3-size", "--t-accent3-line", 700],
  title1: ["--t-title1-size", "--t-title1-line", 700],
  title2: ["--t-title2-size", "--t-title2-line", 700],
  headline1: ["--t-headline1-size", "--t-headline1-line", 700],
  headline2: ["--t-headline2-size", "--t-headline2-line", 700],
  headline3: ["--t-headline3-size", "--t-headline3-line", 700],
  paragraph1: ["--t-paragraph1-size", "--t-paragraph1-line", 500],
  paragraph2: ["--t-paragraph2-size", "--t-paragraph2-line", 500],
  paragraph3: ["--t-paragraph3-size", "--t-paragraph3-line", 500],
  additional0: ["--t-additional0-size", "--t-additional0-line", 700],
  additional1: ["--t-additional1-size", "--t-additional1-line", 700],
  additional2: ["--t-additional2-size", "--t-additional2-line", 700],
};

/** Legacy M3 role → scale style (kept so existing screens keep working). */
const LEGACY = {
  displayLarge: "accent2", displayMedium: "accent3", displaySmall: "title1",
  headlineLarge: "title1", headlineMedium: "title2", headlineSmall: "headline1",
  titleLarge: "headline1", titleMedium: "headline2", titleSmall: "headline3",
  bodyLarge: "paragraph1", bodyMedium: "paragraph2", bodySmall: "paragraph3",
  labelLarge: "headline3", labelMedium: "additional0", labelSmall: "additional1",
};

/**
 * CmpText — every piece of text. Pick a style from the 15-style scale
 * (`accent1…additional2`); legacy M3 names map onto it. Size/line/weight
 * are owned by the style — only color and align may change.
 */
export function CmpText({
  children,
  variant = "paragraph2",
  color = "currentColor",
  weight,
  align,
  maxLines,
  uppercase = false,
  as: Tag = "span",
  style,
  ...rest
}) {
  const key = SCALE[variant] ? variant : LEGACY[variant] || "paragraph2";
  const [sizeVar, lineVar, defWeight] = SCALE[key];
  const clamp = maxLines === 1
    ? { display: "block", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", minWidth: 0, maxWidth: "100%" }
    : maxLines
    ? { display: "-webkit-box", WebkitLineClamp: maxLines, WebkitBoxOrient: "vertical", overflow: "hidden" }
    : null;
  return (
    <Tag
      style={{
        margin: 0,
        fontFamily: "var(--font-sans)",
        fontSize: `var(${sizeVar})`,
        lineHeight: `var(${lineVar})`,
        fontWeight: weight ?? defWeight,
        textTransform: uppercase ? "uppercase" : undefined,
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
