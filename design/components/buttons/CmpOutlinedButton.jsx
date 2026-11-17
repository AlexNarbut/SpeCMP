import React from "react";
import { CmpButton } from "./CmpButton.jsx";

/** @deprecated → CmpButton type="Secondary". Kept for existing screens. */
export function CmpOutlinedButton({ size = "L", ...props }) {
  return <CmpButton type="Secondary" size={size} {...props} />;
}
