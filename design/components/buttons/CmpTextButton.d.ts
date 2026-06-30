import * as React from "react";

export interface CmpTextButtonProps {
  text?: string;
  onClick?: () => void;
  disabled?: boolean;
  icon?: string;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}

/** Text button — tertiary action and dialog actions. No fill, no border. */
export declare function CmpTextButton(props: CmpTextButtonProps): JSX.Element;
