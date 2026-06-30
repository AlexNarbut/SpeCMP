import * as React from "react";

export interface CmpIconButtonProps {
  /** Catalog icon name (CmpIcons). */
  icon: string;
  onClick?: () => void;
  /** Accessible label (required for icon-only controls). */
  contentDescription?: string;
  disabled?: boolean;
  /** Selected/toggled state — primary tint on a surfaceVariant fill. */
  selected?: boolean;
  /** Override icon color. */
  tint?: string;
  style?: React.CSSProperties;
}

/** Icon button — a single catalog icon. 48dp touch target, 24dp icon. */
export declare function CmpIconButton(props: CmpIconButtonProps): JSX.Element;
