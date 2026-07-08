import * as React from "react";

/**
 * @startingPoint section="Surfaces" subtitle="Content card" viewport="700x200"
 */
export interface CmpCardProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: React.ReactNode;
  /** Makes the card clickable (adds a hover/press state layer). */
  onClick?: () => void;
  /** Shadow elevation level 0–3. Default 1. */
  elevation?: number;
  /** Draw a 1dp outlineVariant border. */
  border?: boolean;
}

/**
 * Content container. 1dp shadow, medium (12dp) radius, surface fill.
 */
export declare function CmpCard(props: CmpCardProps): JSX.Element;
