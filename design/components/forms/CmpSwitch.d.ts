import * as React from "react";

export interface CmpSwitchProps {
  checked?: boolean;
  onCheckedChange?: (checked: boolean) => void;
  disabled?: boolean;
  /** Show a check icon on the thumb when on. */
  thumbIcon?: boolean;
  style?: React.CSSProperties;
}

/** Binary on/off toggle. Active track = primary. */
export declare function CmpSwitch(props: CmpSwitchProps): JSX.Element;
