import * as React from "react";
export interface CmpIconButtonProps {
  icon: string;
  onClick?: () => void;
  contentDescription?: string;
  disabled?: boolean;
  /** Ink-filled circle. */
  selected?: boolean;
  /** Standard 48 (no fill) · Glass 56 (translucent + blur, for top bar). */
  variant?: "Standard" | "Glass";
  tint?: string;
  style?: React.CSSProperties;
}
export declare function CmpIconButton(props: CmpIconButtonProps): JSX.Element;
