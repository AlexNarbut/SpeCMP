import * as React from "react";

/** Catalog name from the CmpIcons enum, or any raw Material Icons ligature. */
export type CmpIconName = string;

export interface CmpIconProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** CmpIcons catalog name (e.g. "ArrowBack") or a raw Material Icons ligature. */
  icon: CmpIconName;
  /** Size in dp. Default 24. */
  size?: number;
  /** Color. Defaults to currentColor (inherits content color). */
  color?: string;
  /** Accessible label. Omit for purely decorative icons (renders aria-hidden). */
  title?: string;
}

/** Map of CmpIcons catalog names → Material Icons ligatures. */
export declare const CMP_ICONS: Record<string, string>;

/**
 * Single catalog icon. Default 24dp, inherits text color.
 * @dsCard
 */
export declare function CmpIcon(props: CmpIconProps): JSX.Element;
