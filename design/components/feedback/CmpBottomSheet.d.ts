import * as React from "react";

export interface CmpBottomSheetProps {
  open?: boolean;
  onDismissRequest?: () => void;
  children?: React.ReactNode;
  containerColor?: string;
  style?: React.CSSProperties;
}

/**
 * Panel sliding up from the bottom. 16dp top radius, drag handle, scrim.
 * Wrap the screen in position:relative so the scrim fills it.
 */
export declare function CmpBottomSheet(props: CmpBottomSheetProps): JSX.Element | null;
