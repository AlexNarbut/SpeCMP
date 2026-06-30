import * as React from "react";

export interface CmpSurfaceProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: React.ReactNode;
  onClick?: () => void;
  /** Fill color. Default surface. */
  color?: string;
  /** Content color. Default onSurface. */
  contentColor?: string;
  /** Corner radius in dp. Default 0. */
  radius?: number;
  /** Shadow elevation level 0–3. Default 0. */
  shadowElevation?: number;
  border?: boolean;
}

/** Base container. Square corners and no shadow by default; fully configurable. */
export declare function CmpSurface(props: CmpSurfaceProps): JSX.Element;
