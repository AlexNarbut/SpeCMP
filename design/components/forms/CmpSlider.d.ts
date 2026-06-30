import * as React from "react";

export interface CmpSliderProps {
  value?: number;
  onValueChange?: (value: number) => void;
  min?: number;
  max?: number;
  /** Discrete step size; 0 = continuous. */
  step?: number;
  disabled?: boolean;
  style?: React.CSSProperties;
}

/** Choose a value in a range. Active track = primary. */
export declare function CmpSlider(props: CmpSliderProps): JSX.Element;
