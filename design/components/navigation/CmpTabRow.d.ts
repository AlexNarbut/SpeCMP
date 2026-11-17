import * as React from "react";

export interface CmpTabItem {
  title: string;
  /** Optional catalog icon name. */
  icon?: string;
}

export interface CmpTabRowProps {
  tabs: CmpTabItem[];
  selectedTabIndex?: number;
  onTabSelected?: (index: number) => void;
  /** Scrollable (non-equal width) vs fixed. */
  scrollable?: boolean;
  /** Horizontal margin, px or CSS length. Default 16; 0 = full width. */
  inset?: number | string;
  style?: React.CSSProperties;
}

/** In-screen tabs. Fixed or scrollable; indicator + selected label = primary. */
export declare function CmpTabRow(props: CmpTabRowProps): JSX.Element;
