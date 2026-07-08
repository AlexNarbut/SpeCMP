import * as React from "react";

/**
 * @startingPoint section="Forms" subtitle="Outlined text field" viewport="700x180"
 */
export interface CmpTextFieldProps {
  value?: string;
  onChange?: (value: string) => void;
  /** Floating label. */
  label?: string;
  placeholder?: string;
  /** "Outlined" (default) or "Filled". */
  style?: "Outlined" | "Filled";
  disabled?: boolean;
  readOnly?: boolean;
  /** Error state — border/text/supporting text turn error red. */
  isError?: boolean;
  singleLine?: boolean;
  /** Helper / error message below the field. */
  supportingText?: React.ReactNode;
  leadingIcon?: React.ReactNode;
  trailingIcon?: React.ReactNode;
  type?: string;
  rootStyle?: React.CSSProperties;
}

/**
 * Text input — outlined (default) or filled. 56dp tall, floating label,
 * focus/error states, optional icons and supporting text.
 */
export declare function CmpTextField(props: CmpTextFieldProps): JSX.Element;
