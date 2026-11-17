import * as React from "react";
export type CmpButtonType = "Primary" | "Accent" | "Secondary" | "Tertiary" | "Danger";
export type CmpButtonSize = "S" | "M" | "L" | "XL";
export interface CmpButtonProps {
  text?: string;
  onClick?: () => void;
  disabled?: boolean;
  /** Hides the label (width kept) and shows a spinner. */
  loading?: boolean;
  icon?: string;
  /** Default "Primary" (ink fill). */
  type?: CmpButtonType;
  /** S 48 · M 52 · L 56 (default) · XL 64. */
  size?: CmpButtonSize;
  fullWidth?: boolean;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}
export declare function CmpButton(props: CmpButtonProps): JSX.Element;
