import * as React from "react";

export interface CmpDividerProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Default "horizontal". */
  orientation?: "horizontal" | "vertical";
  /** Line weight in dp. Default 1. */
  thickness?: number;
  /** Line color. Default outlineVariant. */
  color?: string;
  /** Margin along the divider's run. */
  inset?: number | string;
}

/** 1dp separator, horizontal or vertical. Color defaults to outlineVariant. */
export declare function CmpDivider(props: CmpDividerProps): JSX.Element;
