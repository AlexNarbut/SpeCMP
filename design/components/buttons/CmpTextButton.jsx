import React from "react";
import { CmpButton } from "./CmpButton.jsx";

/** @deprecated → CmpButton type="Tertiary". Kept for existing screens. */
export function CmpTextButton({ size = "S", style, ...props }) {
  return <CmpButton type="Tertiary" size={size} style={{ minWidth: 64, padding: "0 16px", ...style }} {...props} />;
}
