import * as React from "react";

export type CmpTextVariant =
  | "displayLarge" | "displayMedium" | "displaySmall"
  | "headlineLarge" | "headlineMedium" | "headlineSmall"
  | "titleLarge" | "titleMedium" | "titleSmall"
  | "bodyLarge" | "bodyMedium" | "bodySmall"
  | "labelLarge" | "labelMedium" | "labelSmall";

export interface CmpTextProps extends React.HTMLAttributes<HTMLElement> {
  /** Type-scale role. Default "bodyLarge". */
  variant?: CmpTextVariant;
  /** Text color. Defaults to currentColor. */
  color?: string;
  /** Override the role's default weight. */
  weight?: number;
  /** Text alignment. */
  align?: "left" | "center" | "right" | "start" | "end";
  /** Truncate to N lines with ellipsis. */
  maxLines?: number;
  /** Rendered element. Default "span". */
  as?: keyof JSX.IntrinsicElements;
  children?: React.ReactNode;
}

/** Every piece of text. Designer picks a role from the M3 type scale. */
export declare function CmpText(props: CmpTextProps): JSX.Element;
