import * as React from "react";

export interface CmpBadgeProps {
  /** Count to show. Omit for a dot badge. */
  count?: number | string;
  containerColor?: string;
  contentColor?: string;
  style?: React.CSSProperties;
}

export interface CmpBadgedBoxProps {
  badge: React.ReactNode;
  children: React.ReactNode;
  style?: React.CSSProperties;
}

/** Count/dot marker. Error fill, onError text. */
export declare function CmpBadge(props: CmpBadgeProps): JSX.Element;
/** Positions a CmpBadge over the top-right of its child. */
export declare function CmpBadgedBox(props: CmpBadgedBoxProps): JSX.Element;
