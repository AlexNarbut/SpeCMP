import * as React from "react";
export interface CmpHoldToConfirmButtonProps {
  text?: string;
  doneText?: string;
  onConfirm?: () => void;
  /** Hold time, ms. Default 1500. */
  duration?: number;
  /** Red fill (default) or brand blue. */
  danger?: boolean;
  disabled?: boolean;
  style?: React.CSSProperties;
}
export declare function CmpHoldToConfirmButton(props: CmpHoldToConfirmButtonProps): JSX.Element;
