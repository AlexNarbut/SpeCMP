import * as React from "react";
export interface CmpListItemProps {
  icon?: string | React.ReactNode;
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  /** Default: chevron when clickable. */
  trailing?: React.ReactNode;
  onClick?: () => void;
  status?: "ok" | "warn" | "error" | "info";
  disabled?: boolean;
  style?: React.CSSProperties;
}
export interface CmpInfoRowProps {
  label: React.ReactNode;
  value: React.ReactNode;
  mono?: boolean;
  accent?: boolean;
  trailing?: React.ReactNode;
  last?: boolean;
  style?: React.CSSProperties;
}
export declare function CmpListItem(props: CmpListItemProps): JSX.Element;
export declare function CmpInfoRow(props: CmpInfoRowProps): JSX.Element;
