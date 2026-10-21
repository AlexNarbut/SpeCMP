import * as React from "react";

export interface CmpOutlinedButtonProps {
  text?: string;
  onClick?: () => void;
  disabled?: boolean;
  icon?: string;
  fullWidth?: boolean;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}

/** Outlined button — secondary action. Outline border, primary text. */
export declare function CmpOutlinedButton(props: CmpOutlinedButtonProps): JSX.Element;
