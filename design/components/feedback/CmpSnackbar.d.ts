import * as React from "react";

export interface CmpSnackbarProps {
  message: React.ReactNode;
  /** Optional action label (inversePrimary). */
  actionLabel?: string;
  onActionClick?: () => void;
  /** Show a ✕ dismiss button. */
  withDismissAction?: boolean;
  onDismiss?: () => void;
  style?: React.CSSProperties;
}

/** Brief notification. Inverse-surface fill, inversePrimary action. */
export declare function CmpSnackbar(props: CmpSnackbarProps): JSX.Element;
