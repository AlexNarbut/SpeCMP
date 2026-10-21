import * as React from "react";

export interface CmpDropdownProps {
  selectedValue?: string | null;
  placeholder?: string;
  options?: string[];
  onOptionSelected?: (index: number, value: string) => void;
  disabled?: boolean;
  rootStyle?: React.CSSProperties;
}

/** Exposed dropdown menu. Min 56dp, medium radius; placeholder 12sp, items 14sp. */
export declare function CmpDropdown(props: CmpDropdownProps): JSX.Element;
