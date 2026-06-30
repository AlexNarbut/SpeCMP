import * as React from "react";

export interface CmpDialogProps {
  open?: boolean;
  onDismissRequest?: () => void;
  /** Optional catalog icon name shown centered above the title. */
  icon?: string;
  title?: React.ReactNode;
  text?: React.ReactNode;
  /** Confirm action (usually a CmpTextButton). */
  confirmButton?: React.ReactNode;
  /** Dismiss action (usually a CmpTextButton). */
  dismissButton?: React.ReactNode;
  style?: React.CSSProperties;
}

/**
 * Modal confirmation/choice. 28dp radius, scrim behind. Wrap the screen
 * in position:relative so the scrim fills it.
 */
export declare function CmpDialog(props: CmpDialogProps): JSX.Element | null;
