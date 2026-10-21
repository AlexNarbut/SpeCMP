import * as React from "react";

export interface CmpRadioButtonProps {
  selected?: boolean;
  onClick?: () => void;
  disabled?: boolean;
  style?: React.CSSProperties;
}

/** Single choice from a group. Active = primary. */
export declare function CmpRadioButton(props: CmpRadioButtonProps): JSX.Element;
