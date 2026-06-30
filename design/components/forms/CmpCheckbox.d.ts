import * as React from "react";

export interface CmpCheckboxProps {
  checked?: boolean;
  /** Tri-state middle value. */
  indeterminate?: boolean;
  onCheckedChange?: (checked: boolean) => void;
  disabled?: boolean;
  style?: React.CSSProperties;
}

/** Multi-select checkbox, supports indeterminate. Active = primary. */
export declare function CmpCheckbox(props: CmpCheckboxProps): JSX.Element;
