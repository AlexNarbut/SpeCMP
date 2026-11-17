import * as React from "react";
export interface CmpInputDigitProps {
  value?: string;
  onChange?: (value: string) => void;
  /** Number of cells. Default 4. */
  length?: number;
  error?: boolean;
  disabled?: boolean;
  /** Show • instead of digits (PIN). */
  secure?: boolean;
  autoFocus?: boolean;
  style?: React.CSSProperties;
}
export declare function CmpInputDigit(props: CmpInputDigitProps): JSX.Element;
