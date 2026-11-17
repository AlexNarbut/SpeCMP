import { useState } from "react";

/**
 * Internal helper: interaction state for every pressable Cmp*.
 * Kit behaviour: press = scale 0.96 (springy) + 10% ripple overlay.
 * Returns handlers, overlay `opacity` (hover .06, focus .08, pressed .10),
 * `pressStyle` (transform + transition) and `focusRing` (box-shadow for
 * keyboard focus, 3px border-focus). Not a public component.
 */
export function useStateLayer() {
  const [hover, setHover] = useState(false);
  const [focus, setFocus] = useState(false);
  const [active, setActive] = useState(false);

  const handlers = {
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => { setHover(false); setActive(false); },
    onMouseDown: () => setActive(true),
    onMouseUp: () => setActive(false),
    onTouchStart: () => setActive(true),
    onTouchEnd: () => setActive(false),
    onFocus: (e) => setFocus(e?.target?.matches ? e.target.matches(":focus-visible") : true),
    onBlur: () => setFocus(false),
  };

  const opacity = active ? 0.1 : focus ? 0.08 : hover ? 0.06 : 0;
  const pressStyle = {
    transform: active ? "scale(var(--press-scale))" : "scale(1)",
    transition: "transform var(--motion-press), background var(--motion-fast), color var(--motion-fast), border-color var(--motion-fast)",
  };
  const focusRing = focus ? "0 0 0 var(--w-focus-width) var(--border-focus)" : undefined;
  return { hover, focus, active, opacity, handlers, pressStyle, focusRing };
}
