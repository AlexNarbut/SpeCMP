import { useState } from "react";

/**
 * Internal helper: Material 3 state-layer tracking for hover / focus /
 * pressed. Returns spread-able handlers and the current overlay opacity
 * (hover .08, focus .10, pressed .12). Not a public component.
 */
export function useStateLayer() {
  const [hover, setHover] = useState(false);
  const [focus, setFocus] = useState(false);
  const [active, setActive] = useState(false);

  const handlers = {
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => {
      setHover(false);
      setActive(false);
    },
    onMouseDown: () => setActive(true),
    onMouseUp: () => setActive(false),
    onFocus: () => setFocus(true),
    onBlur: () => setFocus(false),
  };

  const opacity = active ? 0.12 : focus ? 0.1 : hover ? 0.08 : 0;
  return { hover, focus, active, opacity, handlers };
}
