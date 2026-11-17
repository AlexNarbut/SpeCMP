import * as React from "react";
export interface CmpContentErrorProps {
  icon?: string;
  title: React.ReactNode;
  text?: React.ReactNode;
  action?: React.ReactNode;
  tone?: "error" | "empty" | "offline";
  style?: React.CSSProperties;
}
export declare function CmpContentError(props: CmpContentErrorProps): JSX.Element;
