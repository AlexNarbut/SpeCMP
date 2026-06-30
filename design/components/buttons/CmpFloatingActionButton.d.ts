import * as React from "react";

export interface CmpFloatingActionButtonProps {
  /** Catalog icon name. */
  icon?: string;
  onClick?: () => void;
  /** Small 40dp · Default 56dp · Large 96dp. */
  size?: "Small" | "Default" | "Large";
  contentDescription?: string;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}

/** Floating action button. primaryContainer fill, elevated. */
export declare function CmpFloatingActionButton(
  props: CmpFloatingActionButtonProps
): JSX.Element;
