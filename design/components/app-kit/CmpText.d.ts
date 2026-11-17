import * as React from "react";
/** 15-style scale (UI kit). Prefer these. */
export type CmpTextStyle =
  | "accent1" | "accent2" | "accent3"
  | "title1" | "title2"
  | "headline1" | "headline2" | "headline3"
  | "paragraph1" | "paragraph2" | "paragraph3"
  | "additional0" | "additional1" | "additional2";
/** Legacy M3 names — aliased onto the scale. */
export type CmpTextLegacyVariant =
  | "displayLarge" | "displayMedium" | "displaySmall"
  | "headlineLarge" | "headlineMedium" | "headlineSmall"
  | "titleLarge" | "titleMedium" | "titleSmall"
  | "bodyLarge" | "bodyMedium" | "bodySmall"
  | "labelLarge" | "labelMedium" | "labelSmall";
export type CmpTextVariant = CmpTextStyle | CmpTextLegacyVariant;
export interface CmpTextProps extends React.HTMLAttributes<HTMLElement> {
  variant?: CmpTextVariant;
  color?: string;
  /** @deprecated weight is owned by the style */
  weight?: number;
  align?: "left" | "center" | "right" | "start" | "end";
  maxLines?: number;
  uppercase?: boolean;
  as?: keyof JSX.IntrinsicElements;
  children?: React.ReactNode;
}
export declare function CmpText(props: CmpTextProps): JSX.Element;
