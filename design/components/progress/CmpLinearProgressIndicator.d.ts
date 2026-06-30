import * as React from "react";

export interface CmpLinearProgressIndicatorProps {
  /** 0..1 for determinate; omit/null for indeterminate. */
  progress?: number | null;
  /** Bar color. Default primary. */
  color?: string;
  /** Track color. Default surfaceVariant. */
  trackColor?: string;
  /** Bar height in dp. Default 4. */
  height?: number;
  style?: React.CSSProperties;
}

/** Linear wait/progress indicator. Track is surfaceVariant. */
export declare function CmpLinearProgressIndicator(
  props: CmpLinearProgressIndicatorProps
): JSX.Element;
