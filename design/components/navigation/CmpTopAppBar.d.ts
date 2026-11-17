import * as React from "react";

/**
 * @startingPoint section="Navigation" subtitle="Top app bar" viewport="700x120"
 */
export interface CmpTopAppBarProps {
  /** Title text or custom node. */
  title?: React.ReactNode;
  subtitle?: React.ReactNode;
  /** Leading slot — typically a back CmpIconButton. */
  navigationIcon?: React.ReactNode;
  /** Trailing action slot — CmpIconButtons. */
  actions?: React.ReactNode;
  /** CenterAligned style when true. */
  centered?: boolean;
  /** Title headline1 24 instead of headline2 19 (wide windows, short titles). */
  large?: boolean;
  style?: React.CSSProperties;
}

/**
 * Screen header. 64dp tall, background fill. Title is titleLarge.
 */
export declare function CmpTopAppBar(props: CmpTopAppBarProps): JSX.Element;
