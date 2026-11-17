import * as React from "react";
export interface CmpChipProps {
  label: React.ReactNode;
  selected?: boolean;
  icon?: string;
  onClick?: () => void;
  onRemove?: () => void;
  disabled?: boolean;
  style?: React.CSSProperties;
}
export declare function CmpChip(props: CmpChipProps): JSX.Element;
