import * as React from "react";

/**
 * @startingPoint section="Buttons" subtitle="Filled primary action" viewport="700x220"
 */
export interface CmpButtonProps {
  /** Button label. */
  text?: string;
  onClick?: () => void;
  /** Disabled state — muted, non-interactive. */
  disabled?: boolean;
  /** Optional leading catalog icon name. */
  icon?: string;
  /** Stretch to container width. */
  fullWidth?: boolean;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}

/**
 * Filled button — the primary action of a screen. Height 40dp, stadium
 * shape, label = labelLarge.
 */
export declare function CmpButton(props: CmpButtonProps): JSX.Element;
