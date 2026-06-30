import * as React from "react";

export interface CmpCircularProgressIndicatorProps {
  /** 0..1 for determinate; omit/null for indeterminate (spins). */
  progress?: number | null;
  /** Diameter in dp. Default 48. */
  size?: number;
  /** Arc thickness in dp. Default 4. */
  strokeWidth?: number;
  /** Arc color. Default primary. */
  color?: string;
  /** Track color. Default transparent. */
  trackColor?: string;
  style?: React.CSSProperties;
}

/** Circular wait/progress indicator. 4dp arc, primary color. */
export declare function CmpCircularProgressIndicator(
  props: CmpCircularProgressIndicatorProps
): JSX.Element;
