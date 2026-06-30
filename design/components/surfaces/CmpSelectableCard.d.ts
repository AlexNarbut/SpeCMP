import * as React from "react";

export interface CmpSelectableCardProps {
  selected?: boolean;
  onClick?: () => void;
  /** Optional leading element (icon, radio, checkbox). */
  icon?: React.ReactNode;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}

/**
 * Card with a selected state. 16dp padding, 1.5dp border, medium radius,
 * 300ms transition. Selected → primary border + primaryContainer fill.
 */
export declare function CmpSelectableCard(props: CmpSelectableCardProps): JSX.Element;
