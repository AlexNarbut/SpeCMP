import * as React from "react";

export interface CmpNavigationItem {
  /** Catalog icon name. */
  icon: string;
  label?: string;
  selected?: boolean;
  onClick?: () => void;
  /** Optional badge count shown over the icon. */
  badge?: number | string;
}

export interface CmpNavigationBarProps {
  items: CmpNavigationItem[];
  style?: React.CSSProperties;
}

/** Bottom navigation bar. 80dp tall; selected item gets a pill indicator. */
export declare function CmpNavigationBar(props: CmpNavigationBarProps): JSX.Element;
