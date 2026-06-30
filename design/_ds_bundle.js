/* @ds-bundle: {"format":3,"namespace":"PandoraSpecDesignSystem_b8d6e1","components":[{"name":"CMP_ICONS","sourcePath":"components/app-kit/CmpIcon.jsx"},{"name":"CmpIcon","sourcePath":"components/app-kit/CmpIcon.jsx"},{"name":"CmpText","sourcePath":"components/app-kit/CmpText.jsx"},{"name":"CmpButton","sourcePath":"components/buttons/CmpButton.jsx"},{"name":"CmpFloatingActionButton","sourcePath":"components/buttons/CmpFloatingActionButton.jsx"},{"name":"CmpIconButton","sourcePath":"components/buttons/CmpIconButton.jsx"},{"name":"CmpOutlinedButton","sourcePath":"components/buttons/CmpOutlinedButton.jsx"},{"name":"CmpTextButton","sourcePath":"components/buttons/CmpTextButton.jsx"},{"name":"CmpBadge","sourcePath":"components/feedback/CmpBadge.jsx"},{"name":"CmpBadgedBox","sourcePath":"components/feedback/CmpBadge.jsx"},{"name":"CmpBottomSheet","sourcePath":"components/feedback/CmpBottomSheet.jsx"},{"name":"CmpDialog","sourcePath":"components/feedback/CmpDialog.jsx"},{"name":"CmpSnackbar","sourcePath":"components/feedback/CmpSnackbar.jsx"},{"name":"CmpCheckbox","sourcePath":"components/forms/CmpCheckbox.jsx"},{"name":"CmpDropdown","sourcePath":"components/forms/CmpDropdown.jsx"},{"name":"CmpRadioButton","sourcePath":"components/forms/CmpRadioButton.jsx"},{"name":"CmpSlider","sourcePath":"components/forms/CmpSlider.jsx"},{"name":"CmpSwitch","sourcePath":"components/forms/CmpSwitch.jsx"},{"name":"CmpTextField","sourcePath":"components/forms/CmpTextField.jsx"},{"name":"CmpNavigationBar","sourcePath":"components/navigation/CmpNavigationBar.jsx"},{"name":"CmpTabRow","sourcePath":"components/navigation/CmpTabRow.jsx"},{"name":"CmpTopAppBar","sourcePath":"components/navigation/CmpTopAppBar.jsx"},{"name":"CmpCircularProgressIndicator","sourcePath":"components/progress/CmpCircularProgressIndicator.jsx"},{"name":"CmpLinearProgressIndicator","sourcePath":"components/progress/CmpLinearProgressIndicator.jsx"},{"name":"CmpCard","sourcePath":"components/surfaces/CmpCard.jsx"},{"name":"CmpDivider","sourcePath":"components/surfaces/CmpDivider.jsx"},{"name":"CmpSelectableCard","sourcePath":"components/surfaces/CmpSelectableCard.jsx"},{"name":"CmpSurface","sourcePath":"components/surfaces/CmpSurface.jsx"}],"sourceHashes":{"components/app-kit/CmpIcon.jsx":"b3d94cf9b1e3","components/app-kit/CmpText.jsx":"cdee16d965f8","components/app-kit/PhoneFrame.jsx":"a9d47a57ac31","components/app-kit/useStateLayer.js":"2a1599d8c7c6","components/buttons/CmpButton.jsx":"5e4b9cff4fc3","components/buttons/CmpFloatingActionButton.jsx":"1158dcb8c4da","components/buttons/CmpIconButton.jsx":"6a7318af31c1","components/buttons/CmpOutlinedButton.jsx":"c1e9ba8c6875","components/buttons/CmpTextButton.jsx":"5ee3f901782e","components/feedback/CmpBadge.jsx":"547d9173e5bf","components/feedback/CmpBottomSheet.jsx":"54a97e4fb43b","components/feedback/CmpDialog.jsx":"2a1414da342e","components/feedback/CmpSnackbar.jsx":"fd293f936c21","components/forms/CmpCheckbox.jsx":"14129294474d","components/forms/CmpDropdown.jsx":"b4b6ca649867","components/forms/CmpRadioButton.jsx":"2f75844572ce","components/forms/CmpSlider.jsx":"1a752d0a3873","components/forms/CmpSwitch.jsx":"d19f299e5238","components/forms/CmpTextField.jsx":"fc85734a1a03","components/navigation/CmpNavigationBar.jsx":"24fe17d57b50","components/navigation/CmpTabRow.jsx":"76164e7ddc16","components/navigation/CmpTopAppBar.jsx":"2d8ea5fb2126","components/progress/CmpCircularProgressIndicator.jsx":"0d6435223dd7","components/progress/CmpLinearProgressIndicator.jsx":"ff47898fb86d","components/surfaces/CmpCard.jsx":"49545cb747b5","components/surfaces/CmpDivider.jsx":"a3a8fe9c2772","components/surfaces/CmpSelectableCard.jsx":"cc5403070197","components/surfaces/CmpSurface.jsx":"f8824849e0ac","features/auth/Auth.jsx":"730fafddac89","features/auth/AuthScreens.jsx":"4986c79933ee","features/auth/i18n.js":"9e4ecc677e9e","features/calculator/Calculator.jsx":"778bbbf9b839","res/icons/cmp-icons.js":"8c00925c86ca","res/nav.js":"45d4d98077d9"},"inlinedExternals":[],"unexposedExports":[{"name":"useStateLayer","sourcePath":"components/app-kit/useStateLayer.js"}]} */

(() => {

const __ds_ns = (window.PandoraSpecDesignSystem_b8d6e1 = window.PandoraSpecDesignSystem_b8d6e1 || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/app-kit/CmpIcon.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Catalog of icon names (CmpIcons enum) → Material Icons ligatures.
 * Mirrors core/ui/.../icon/CmpIconsResolver.kt exactly.
 * Icons render from the "Material Icons" (Filled) webfont.
 */
const CMP_ICONS = {
  // Navigation
  ArrowBack: "arrow_back",
  ArrowForward: "arrow_forward",
  ArrowDropDown: "arrow_drop_down",
  ChevronLeft: "keyboard_arrow_left",
  ChevronRight: "keyboard_arrow_right",
  KeyboardArrowDown: "keyboard_arrow_down",
  KeyboardArrowUp: "keyboard_arrow_up",
  Menu: "menu",
  MoreVert: "more_vert",
  Close: "close",
  Home: "home",
  // Actions
  Add: "add",
  Remove: "clear",
  Edit: "edit",
  Create: "create",
  Delete: "delete",
  Search: "search",
  Refresh: "refresh",
  Share: "share",
  Send: "send",
  Done: "done",
  Check: "check",
  CheckCircle: "check_circle",
  Clear: "clear",
  PlayArrow: "play_arrow",
  Pause: "pause",
  Build: "build",
  // Content
  Favorite: "favorite",
  FavoriteBorder: "favorite_border",
  Star: "star",
  ThumbUp: "thumb_up",
  List: "list",
  Tag: "sell",
  // Communication
  Email: "email",
  Phone: "phone",
  Call: "call",
  Notifications: "notifications",
  Mic: "mic",
  // User
  Person: "person",
  AccountCircle: "account_circle",
  Face: "face",
  // Status
  Info: "info",
  Warning: "warning",
  AlertTriangle: "warning",
  AlertCircle: "error",
  Lock: "lock",
  Visibility: "visibility",
  VisibilityOff: "visibility_off",
  // Places
  LocationOn: "location_on",
  Place: "place",
  // Other
  Settings: "settings",
  DateRange: "date_range",
  Schedule: "schedule",
  ShoppingCart: "shopping_cart",
  ExitToApp: "exit_to_app",
  DarkMode: "dark_mode",
  LightMode: "light_mode",
  NightsStay: "nights_stay",
  Lightbulb: "lightbulb",
  Moon: "nights_stay",
  Sun: "wb_sunny"
};

/**
 * CmpIcon — single catalog icon. Default 24dp, inherits text color.
 * `icon` is a CmpIcons catalog name (e.g. "ArrowBack") or any raw
 * Material Icons ligature. Never hand-roll icons outside the catalog.
 */
function CmpIcon({
  icon,
  size = 24,
  color = "currentColor",
  title,
  style,
  ...rest
}) {
  const ligature = CMP_ICONS[icon] || icon;
  return /*#__PURE__*/React.createElement("span", _extends({
    className: "material-icons",
    role: "img",
    "aria-label": title || icon,
    "aria-hidden": title ? undefined : true,
    style: {
      fontSize: `${size}px`,
      width: `${size}px`,
      height: `${size}px`,
      lineHeight: 1,
      color,
      userSelect: "none",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      flex: "0 0 auto",
      ...style
    }
  }, rest), ligature);
}
Object.assign(__ds_scope, { CMP_ICONS, CmpIcon });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/app-kit/CmpIcon.jsx", error: String((e && e.message) || e) }); }

// components/app-kit/CmpText.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/** Material 3 type-scale role → CSS token pair. */
const VARIANTS = {
  displayLarge: ["--display-large-size", "--display-large-line", 400],
  displayMedium: ["--display-medium-size", "--display-medium-line", 400],
  displaySmall: ["--display-small-size", "--display-small-line", 400],
  headlineLarge: ["--headline-large-size", "--headline-large-line", 400],
  headlineMedium: ["--headline-medium-size", "--headline-medium-line", 400],
  headlineSmall: ["--headline-small-size", "--headline-small-line", 400],
  titleLarge: ["--title-large-size", "--title-large-line", 500],
  titleMedium: ["--title-medium-size", "--title-medium-line", 500],
  titleSmall: ["--title-small-size", "--title-small-line", 500],
  bodyLarge: ["--body-large-size", "--body-large-line", 400],
  bodyMedium: ["--body-medium-size", "--body-medium-line", 400],
  bodySmall: ["--body-small-size", "--body-small-line", 400],
  labelLarge: ["--label-large-size", "--label-large-line", 500],
  labelMedium: ["--label-medium-size", "--label-medium-line", 500],
  labelSmall: ["--label-small-size", "--label-small-line", 500]
};

/**
 * CmpText — every piece of text. Pick a `variant` from the M3 type
 * scale (designer chooses a role, not a raw size). `color` inherits
 * (currentColor) by default; override with a role token where needed.
 */
function CmpText({
  children,
  variant = "bodyLarge",
  color = "currentColor",
  weight,
  align,
  maxLines,
  as: Tag = "span",
  style,
  ...rest
}) {
  const [sizeVar, lineVar, defWeight] = VARIANTS[variant] || VARIANTS.bodyLarge;
  const clamp = maxLines ? {
    display: "-webkit-box",
    WebkitLineClamp: maxLines,
    WebkitBoxOrient: "vertical",
    overflow: "hidden"
  } : null;
  return /*#__PURE__*/React.createElement(Tag, _extends({
    style: {
      margin: 0,
      fontFamily: "var(--font-sans)",
      fontSize: `var(${sizeVar})`,
      lineHeight: `var(${lineVar})`,
      fontWeight: weight ?? defWeight,
      color,
      textAlign: align,
      ...clamp,
      ...style
    }
  }, rest), children);
}
Object.assign(__ds_scope, { CmpText });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/app-kit/CmpText.jsx", error: String((e && e.message) || e) }); }

// components/app-kit/PhoneFrame.jsx
try { (() => {
// @ds-adherence-ignore -- omelette starter scaffold (raw elements/hex/px by design)

/* BEGIN USAGE */
// Android.jsx — Simplified Android (Material 3) device frame
// Status bar + top app bar + content + gesture nav + keyboard.
// Based on Figma M3 spec. No dependencies, no image assets.
// Exports (to window): AndroidDevice, AndroidStatusBar, AndroidAppBar, AndroidListItem, AndroidNavBar, AndroidKeyboard
//
// Usage — wrap your screen content in <AndroidDevice> to get the bezel, status
// bar and gesture nav (props: title, large, keyboard, dark):
//
//   <AndroidDevice title="Inbox" large>
//     ...your screen content...
//   </AndroidDevice>
//   <AndroidDevice title="Compose" keyboard>…</AndroidDevice>
/* END USAGE */

const MD_C = {
  surface: '#F9F9F9',
  surfaceVariant: '#DCE5D9',
  inverseOnSurface: '#ecf2ef',
  secondaryContainer: '#cde8e1',
  primaryFixedDim: '#83d5c6',
  onSurface: '#1A1C1C',
  onSurfaceVar: '#404941',
  onPrimaryContainer: '#00201c',
  primary: '#006a60',
  frameBorder: 'rgba(116,119,117,0.5)'
};

// ─────────────────────────────────────────────────────────────
// Status bar (time left, wifi/cell/battery right)
// ─────────────────────────────────────────────────────────────
function AndroidStatusBar({
  dark = false
}) {
  const c = dark ? '#fff' : MD_C.onSurface;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 40,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 16px',
      position: 'relative',
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 128,
      display: 'flex',
      alignItems: 'center',
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 14,
      fontWeight: 400,
      letterSpacing: 0.25,
      lineHeight: '20px',
      color: c
    }
  }, "9:30")), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      left: '50%',
      top: 8,
      transform: 'translateX(-50%)',
      width: 24,
      height: 24,
      borderRadius: 100,
      background: '#2e2e2e'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      paddingRight: 2
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: "16",
    height: "16",
    viewBox: "0 0 16 16",
    style: {
      marginRight: -2
    }
  }, /*#__PURE__*/React.createElement("path", {
    d: "M8 13.3L.67 5.97a10.37 10.37 0 0114.66 0L8 13.3z",
    fill: c
  })), /*#__PURE__*/React.createElement("svg", {
    width: "16",
    height: "16",
    viewBox: "0 0 16 16",
    style: {
      marginRight: -2
    }
  }, /*#__PURE__*/React.createElement("path", {
    d: "M14.67 14.67V1.33L1.33 14.67h13.34z",
    fill: c
  }))), /*#__PURE__*/React.createElement("svg", {
    width: "16",
    height: "16",
    viewBox: "0 0 16 16"
  }, /*#__PURE__*/React.createElement("rect", {
    x: "3.75",
    y: "2",
    width: "8.5",
    height: "13",
    rx: "1.5",
    fill: c
  }), /*#__PURE__*/React.createElement("rect", {
    x: "5.5",
    y: "0.9",
    width: "5",
    height: "2",
    rx: "0.5",
    fill: c
  }))));
}

// ─────────────────────────────────────────────────────────────
// Top app bar (Material 3 small/medium)
// ─────────────────────────────────────────────────────────────
function AndroidAppBar({
  title = 'Title',
  large = false
}) {
  const iconDot = /*#__PURE__*/React.createElement("div", {
    style: {
      width: 48,
      height: 48,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 22,
      height: 22,
      borderRadius: '50%',
      background: MD_C.onSurfaceVar,
      opacity: 0.3
    }
  }));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      background: MD_C.surface,
      padding: '4px 4px 0'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      height: 56,
      display: 'flex',
      alignItems: 'center',
      gap: 4
    }
  }, iconDot, !large && /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      fontSize: 22,
      fontWeight: 400,
      color: MD_C.onSurface,
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, title), large && /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }), iconDot), large && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px 16px 20px',
      fontSize: 28,
      fontWeight: 400,
      color: MD_C.onSurface,
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, title));
}

// ─────────────────────────────────────────────────────────────
// List item (Material 3)
// ─────────────────────────────────────────────────────────────
function AndroidListItem({
  headline,
  supporting,
  leading
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 16,
      padding: '12px 16px',
      minHeight: 56,
      boxSizing: 'border-box',
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, leading && /*#__PURE__*/React.createElement("div", {
    style: {
      width: 40,
      height: 40,
      borderRadius: '50%',
      background: MD_C.primary,
      color: '#fff',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontSize: 18,
      fontWeight: 500,
      flexShrink: 0
    }
  }, leading), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 16,
      color: MD_C.onSurface,
      lineHeight: '24px'
    }
  }, headline), supporting && /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 14,
      color: MD_C.onSurfaceVar,
      lineHeight: '20px'
    }
  }, supporting)));
}

// ─────────────────────────────────────────────────────────────
// Gesture nav bar (pill)
// ─────────────────────────────────────────────────────────────
function AndroidNavBar({
  dark = false
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 24,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 108,
      height: 4,
      borderRadius: 2,
      background: dark ? '#fff' : MD_C.onSurface,
      opacity: 0.4
    }
  }));
}

// ─────────────────────────────────────────────────────────────
// Device frame — wraps everything
// ─────────────────────────────────────────────────────────────
function AndroidDevice({
  children,
  width = 412,
  height = 892,
  dark = false,
  title,
  large = false,
  keyboard = false
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      width,
      height,
      borderRadius: 18,
      overflow: 'hidden',
      background: dark ? '#121414' : MD_C.surface,
      border: `8px solid ${MD_C.frameBorder}`,
      boxShadow: '0 30px 80px rgba(0,0,0,0.25)',
      display: 'flex',
      flexDirection: 'column',
      boxSizing: 'border-box'
    }
  }, /*#__PURE__*/React.createElement(AndroidStatusBar, {
    dark: dark
  }), title !== undefined && /*#__PURE__*/React.createElement(AndroidAppBar, {
    title: title,
    large: large
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      overflow: 'auto'
    }
  }, children), keyboard && /*#__PURE__*/React.createElement(AndroidKeyboard, null), /*#__PURE__*/React.createElement(AndroidNavBar, {
    dark: dark
  }));
}

// ─────────────────────────────────────────────────────────────
// Keyboard — Gboard (Material 3)
// ─────────────────────────────────────────────────────────────
function AndroidKeyboard() {
  let _k = 0;
  const key = (l, {
    flex = 1,
    bg = MD_C.surface,
    r = 6,
    minW,
    fs = 21
  } = {}) => /*#__PURE__*/React.createElement("div", {
    key: _k++,
    style: {
      height: 46,
      borderRadius: r,
      flex,
      minWidth: minW,
      background: bg,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: 'Roboto, system-ui',
      fontSize: fs,
      color: MD_C.onPrimaryContainer
    }
  }, l);
  const row = (keys, style = {}) => /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6,
      justifyContent: 'center',
      ...style
    }
  }, keys.map(l => key(l)));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      background: MD_C.inverseOnSurface,
      padding: '0 8px 8px',
      display: 'flex',
      flexDirection: 'column',
      gap: 4
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      height: 44
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }
  }, row(['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p']), row(['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'], {
    padding: '0 20px'
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6
    }
  }, key('', {
    bg: MD_C.surfaceVariant
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6,
      flex: 7,
      minWidth: 274
    }
  }, ['z', 'x', 'c', 'v', 'b', 'n', 'm'].map(l => key(l))), key('', {
    bg: MD_C.surfaceVariant
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6
    }
  }, key('?123', {
    bg: MD_C.secondaryContainer,
    r: 100,
    minW: 58,
    fs: 14
  }), key(',', {
    bg: MD_C.surfaceVariant
  }), key('', {
    flex: 3,
    minW: 154
  }), key('.', {
    bg: MD_C.surfaceVariant
  }), key('', {
    bg: MD_C.primaryFixedDim,
    r: 100,
    minW: 58
  }))));
}
Object.assign(window, {
  AndroidDevice,
  AndroidStatusBar,
  AndroidAppBar,
  AndroidListItem,
  AndroidNavBar,
  AndroidKeyboard
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/app-kit/PhoneFrame.jsx", error: String((e && e.message) || e) }); }

// components/app-kit/useStateLayer.js
try { (() => {
const {
  useState
} = React;
/**
 * Internal helper: Material 3 state-layer tracking for hover / focus /
 * pressed. Returns spread-able handlers and the current overlay opacity
 * (hover .08, focus .10, pressed .12). Not a public component.
 */
function useStateLayer() {
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
    onBlur: () => setFocus(false)
  };
  const opacity = active ? 0.12 : focus ? 0.1 : hover ? 0.08 : 0;
  return {
    hover,
    focus,
    active,
    opacity,
    handlers
  };
}
Object.assign(__ds_scope, { useStateLayer });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/app-kit/useStateLayer.js", error: String((e && e.message) || e) }); }

// components/buttons/CmpButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpButton — filled button. Primary action of a screen.
 * Height 40dp, stadium shape, label = labelLarge.
 */
function CmpButton({
  text,
  onClick,
  disabled = false,
  icon,
  fullWidth = false,
  children,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const label = text ?? children;
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: disabled ? undefined : onClick,
    disabled: disabled
  }, handlers, {
    style: {
      position: "relative",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      gap: "var(--space-s)",
      height: "var(--button-height)",
      minWidth: 64,
      width: fullWidth ? "100%" : undefined,
      padding: icon ? "0 24px 0 16px" : "0 24px",
      border: "none",
      borderRadius: 999,
      background: disabled ? "color-mix(in srgb, var(--on-surface) 12%, transparent)" : "var(--primary)",
      color: disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--on-primary)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--label-large-size)",
      lineHeight: "var(--label-large-line)",
      fontWeight: "var(--weight-medium)",
      cursor: disabled ? "default" : "pointer",
      overflow: "hidden",
      transition: "background var(--motion-fast)",
      ...style
    }
  }, rest), !disabled && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--on-primary)",
      opacity,
      pointerEvents: "none"
    }
  }), icon && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: icon,
    size: 18,
    color: "currentColor"
  }), label);
}
Object.assign(__ds_scope, { CmpButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpFloatingActionButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const SIZES = {
  Small: {
    box: 40,
    radius: 12,
    icon: 24
  },
  Default: {
    box: 56,
    radius: 16,
    icon: 24
  },
  Large: {
    box: 96,
    radius: 28,
    icon: 36
  }
};

/**
 * CmpFloatingActionButton — floating primary action. Sizes Small (40),
 * Default (56), Large (96). Background = primaryContainer.
 */
function CmpFloatingActionButton({
  icon,
  onClick,
  size = "Default",
  contentDescription,
  children,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const s = SIZES[size] || SIZES.Default;
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: onClick,
    "aria-label": contentDescription
  }, handlers, {
    style: {
      position: "relative",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      width: s.box,
      height: s.box,
      border: "none",
      borderRadius: s.radius,
      background: "var(--primary-container)",
      color: "var(--on-primary-container)",
      boxShadow: "var(--elevation-3)",
      cursor: "pointer",
      overflow: "hidden",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--on-primary-container)",
      opacity,
      pointerEvents: "none"
    }
  }), children || /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: icon,
    size: s.icon,
    color: "currentColor",
    title: contentDescription
  }));
}
Object.assign(__ds_scope, { CmpFloatingActionButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpFloatingActionButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpIconButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpIconButton — action shown as a single catalog icon. Touch target
 * 48dp, icon 24dp. Content is ALWAYS an icon (never arbitrary UI).
 */
function CmpIconButton({
  icon,
  onClick,
  contentDescription,
  disabled = false,
  selected = false,
  tint,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const color = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : tint || (selected ? "var(--primary)" : "var(--on-surface-variant)");
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: disabled ? undefined : onClick,
    disabled: disabled,
    "aria-label": contentDescription,
    "aria-pressed": selected || undefined
  }, handlers, {
    style: {
      position: "relative",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      width: "var(--touch-min)",
      height: "var(--touch-min)",
      border: "none",
      borderRadius: 999,
      background: selected ? "var(--surface-variant)" : "transparent",
      color,
      cursor: disabled ? "default" : "pointer",
      overflow: "hidden",
      flex: "0 0 auto",
      ...style
    }
  }, rest), !disabled && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      borderRadius: 999,
      background: "currentColor",
      opacity,
      pointerEvents: "none"
    }
  }), /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: icon,
    size: 24,
    color: "currentColor",
    title: contentDescription
  }));
}
Object.assign(__ds_scope, { CmpIconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpIconButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpOutlinedButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpOutlinedButton — secondary action. Outline border, primary text,
 * transparent background. Height 40dp, stadium shape.
 */
function CmpOutlinedButton({
  text,
  onClick,
  disabled = false,
  icon,
  fullWidth = false,
  children,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const label = text ?? children;
  const fg = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--primary)";
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: disabled ? undefined : onClick,
    disabled: disabled
  }, handlers, {
    style: {
      position: "relative",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      gap: "var(--space-s)",
      height: "var(--button-height)",
      minWidth: 64,
      width: fullWidth ? "100%" : undefined,
      padding: icon ? "0 24px 0 16px" : "0 24px",
      border: `1px solid ${disabled ? "color-mix(in srgb, var(--on-surface) 12%, transparent)" : "var(--outline)"}`,
      borderRadius: 999,
      background: "transparent",
      color: fg,
      fontFamily: "var(--font-sans)",
      fontSize: "var(--label-large-size)",
      lineHeight: "var(--label-large-line)",
      fontWeight: "var(--weight-medium)",
      cursor: disabled ? "default" : "pointer",
      overflow: "hidden",
      ...style
    }
  }, rest), !disabled && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--primary)",
      opacity,
      pointerEvents: "none"
    }
  }), icon && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: icon,
    size: 18,
    color: "currentColor"
  }), label);
}
Object.assign(__ds_scope, { CmpOutlinedButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpOutlinedButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpTextButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpTextButton — tertiary action / dialog actions. Primary text,
 * no background, no border. Height 40dp.
 */
function CmpTextButton({
  text,
  onClick,
  disabled = false,
  icon,
  children,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const label = text ?? children;
  const fg = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--primary)";
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: disabled ? undefined : onClick,
    disabled: disabled
  }, handlers, {
    style: {
      position: "relative",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      gap: "var(--space-s)",
      height: "var(--button-height)",
      minWidth: 48,
      padding: icon ? "0 16px 0 12px" : "0 12px",
      border: "none",
      borderRadius: 999,
      background: "transparent",
      color: fg,
      fontFamily: "var(--font-sans)",
      fontSize: "var(--label-large-size)",
      lineHeight: "var(--label-large-line)",
      fontWeight: "var(--weight-medium)",
      cursor: disabled ? "default" : "pointer",
      overflow: "hidden",
      ...style
    }
  }, rest), !disabled && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--primary)",
      opacity,
      pointerEvents: "none"
    }
  }), icon && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: icon,
    size: 18,
    color: "currentColor"
  }), label);
}
Object.assign(__ds_scope, { CmpTextButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpTextButton.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpBadge.jsx
try { (() => {
/**
 * CmpBadge — count or dot marker over an element. Error fill, onError
 * text, labelSmall. Wrap a target with CmpBadgedBox to position it.
 */
function CmpBadge({
  count,
  containerColor,
  contentColor,
  style
}) {
  const dot = count == null;
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      minWidth: dot ? 6 : 16,
      height: dot ? 6 : 16,
      padding: dot ? 0 : "0 4px",
      borderRadius: 999,
      background: containerColor || "var(--error)",
      color: contentColor || "var(--on-error)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--label-small-size)",
      fontWeight: "var(--weight-medium)",
      lineHeight: 1,
      ...style
    }
  }, !dot && count);
}

/** Positions a CmpBadge over the top-right of its child. */
function CmpBadgedBox({
  badge,
  children,
  style
}) {
  return /*#__PURE__*/React.createElement("span", {
    style: {
      position: "relative",
      display: "inline-flex",
      ...style
    }
  }, children, /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      top: -2,
      right: -2
    }
  }, badge));
}
Object.assign(__ds_scope, { CmpBadge, CmpBadgedBox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpBadge.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpBottomSheet.jsx
try { (() => {
/**
 * CmpBottomSheet — panel sliding up from the bottom. large radius (16dp)
 * on top corners, surface fill, drag handle, scrim behind. Scrim fills
 * the nearest positioned ancestor (wrap your screen in position:relative).
 */
function CmpBottomSheet({
  open = true,
  onDismissRequest,
  children,
  containerColor = "var(--surface)",
  style
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    onClick: onDismissRequest,
    style: {
      position: "absolute",
      inset: 0,
      zIndex: 60,
      background: "color-mix(in srgb, var(--scrim) 40%, transparent)",
      display: "flex",
      alignItems: "flex-end"
    }
  }, /*#__PURE__*/React.createElement("div", {
    onClick: e => e.stopPropagation(),
    style: {
      width: "100%",
      maxHeight: "70%",
      overflowY: "auto",
      background: containerColor,
      color: "var(--on-surface)",
      borderRadius: "var(--radius-l) var(--radius-l) 0 0",
      padding: "var(--space-m) var(--space-l) var(--space-xl)",
      boxShadow: "var(--elevation-3)",
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 32,
      height: 4,
      borderRadius: 999,
      background: "var(--outline-variant)",
      margin: "0 auto var(--space-l)"
    }
  }), children));
}
Object.assign(__ds_scope, { CmpBottomSheet });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpBottomSheet.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpDialog.jsx
try { (() => {
/**
 * CmpDialog — modal confirmation/choice. extraLarge radius (28dp),
 * surface fill, scrim behind. Slots: optional icon, title, text,
 * confirmButton, dismissButton. Scrim fills the nearest positioned
 * ancestor (wrap your screen in position:relative).
 */
function CmpDialog({
  open = true,
  onDismissRequest,
  icon,
  title,
  text,
  confirmButton,
  dismissButton,
  style
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    onClick: onDismissRequest,
    style: {
      position: "absolute",
      inset: 0,
      zIndex: 60,
      background: "color-mix(in srgb, var(--scrim) 40%, transparent)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "var(--space-xl)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    onClick: e => e.stopPropagation(),
    style: {
      width: "100%",
      maxWidth: 360,
      background: "var(--surface-container-high)",
      color: "var(--on-surface)",
      borderRadius: "var(--radius-xl)",
      padding: "var(--space-xl)",
      boxShadow: "var(--elevation-3)",
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-l)",
      alignItems: icon ? "center" : "stretch",
      textAlign: icon ? "center" : "left",
      ...style
    }
  }, icon && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: icon,
    size: 24,
    color: "var(--secondary)"
  }), title && /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "headlineSmall"
  }, title), text && /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, text), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "flex-end",
      gap: "var(--space-s)",
      flexWrap: "wrap"
    }
  }, dismissButton, confirmButton)));
}
Object.assign(__ds_scope, { CmpDialog });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpDialog.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpSnackbar.jsx
try { (() => {
/**
 * CmpSnackbar — brief notification. Inverse surface fill; action label
 * uses inversePrimary. Optional dismiss (✕) action.
 */
function CmpSnackbar({
  message,
  actionLabel,
  onActionClick,
  withDismissAction = false,
  onDismiss,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    role: "status",
    style: {
      display: "flex",
      alignItems: "center",
      gap: "var(--space-s)",
      minHeight: 48,
      maxWidth: 560,
      padding: "8px 8px 8px 16px",
      borderRadius: "var(--radius-xs)",
      background: "var(--inverse-surface)",
      color: "var(--inverse-on-surface)",
      boxShadow: "var(--elevation-3)",
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "bodyMedium",
    color: "var(--inverse-on-surface)"
  }, message)), actionLabel && /*#__PURE__*/React.createElement("button", {
    type: "button",
    onClick: onActionClick,
    style: {
      border: "none",
      background: "transparent",
      color: "var(--inverse-primary)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--label-large-size)",
      fontWeight: "var(--weight-medium)",
      padding: "10px 12px",
      borderRadius: 999,
      cursor: "pointer"
    }
  }, actionLabel), withDismissAction && /*#__PURE__*/React.createElement("button", {
    type: "button",
    onClick: onDismiss,
    "aria-label": "\u0417\u0430\u043A\u0440\u044B\u0442\u044C",
    style: {
      border: "none",
      background: "transparent",
      color: "var(--inverse-on-surface)",
      cursor: "pointer",
      width: 40,
      height: 40,
      borderRadius: 999,
      fontSize: 18
    }
  }, "\u2715"));
}
Object.assign(__ds_scope, { CmpSnackbar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpSnackbar.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpCheckbox.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpCheckbox — multi-select / on-off. Active = primary fill with check.
 * Supports indeterminate (tri-state). 48dp touch area.
 */
function CmpCheckbox({
  checked = false,
  indeterminate = false,
  onCheckedChange,
  disabled = false,
  style,
  ...rest
}) {
  const on = checked || indeterminate;
  const boxColor = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : on ? "var(--primary)" : "transparent";
  const borderColor = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : on ? "var(--primary)" : "var(--on-surface-variant)";
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    role: "checkbox",
    "aria-checked": indeterminate ? "mixed" : checked,
    disabled: disabled,
    onClick: () => onCheckedChange && onCheckedChange(!checked),
    style: {
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      width: "var(--touch-min)",
      height: "var(--touch-min)",
      padding: 0,
      border: "none",
      background: "transparent",
      cursor: disabled ? "default" : "pointer",
      flex: "0 0 auto",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      width: 18,
      height: 18,
      borderRadius: 2,
      border: `2px solid ${borderColor}`,
      background: boxColor,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      transition: "all var(--motion-fast)"
    }
  }, indeterminate ? /*#__PURE__*/React.createElement("span", {
    style: {
      width: 10,
      height: 2,
      background: "var(--on-primary)",
      borderRadius: 1
    }
  }) : checked && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: "Check",
    size: 16,
    color: "var(--on-primary)"
  })));
}
Object.assign(__ds_scope, { CmpCheckbox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpCheckbox.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpDropdown.jsx
try { (() => {
const {
  useState,
  useRef,
  useEffect
} = React;
/**
 * CmpDropdown — exposed dropdown menu. Read-only outlined field anchor;
 * placeholder 12sp, menu items 14sp. Min height 56dp, medium radius,
 * border outline → primary when open.
 */
function CmpDropdown({
  selectedValue = null,
  placeholder = "Выберите",
  options = [],
  onOptionSelected,
  disabled = false,
  rootStyle
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  useEffect(() => {
    if (!open) return;
    const close = e => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);
  return /*#__PURE__*/React.createElement("div", {
    ref: ref,
    style: {
      position: "relative",
      width: "100%",
      opacity: disabled ? 0.38 : 1,
      ...rootStyle
    }
  }, /*#__PURE__*/React.createElement("button", {
    type: "button",
    disabled: disabled,
    onClick: () => setOpen(o => !o),
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      gap: "var(--space-s)",
      width: "100%",
      minHeight: "var(--field-height)",
      padding: "0 16px",
      background: "transparent",
      border: `${open ? 2 : 1}px solid ${open ? "var(--primary)" : "var(--outline)"}`,
      borderRadius: "var(--radius-m)",
      cursor: disabled ? "default" : "pointer",
      textAlign: "left"
    }
  }, selectedValue ? /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "bodyLarge",
    color: "var(--on-surface)"
  }, selectedValue) : /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-sans)",
      fontSize: 12,
      color: "var(--on-surface-variant)"
    }
  }, placeholder), /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: open ? "KeyboardArrowUp" : "KeyboardArrowDown",
    size: 24,
    color: "var(--on-surface-variant)"
  })), open && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      top: "calc(100% + 4px)",
      left: 0,
      right: 0,
      zIndex: 20,
      background: "var(--surface-container)",
      borderRadius: "var(--radius-xs)",
      boxShadow: "var(--elevation-2)",
      padding: "8px 0",
      maxHeight: 280,
      overflowY: "auto"
    }
  }, options.map((opt, i) => /*#__PURE__*/React.createElement("button", {
    key: i,
    type: "button",
    onClick: () => {
      onOptionSelected && onOptionSelected(i, opt);
      setOpen(false);
    },
    style: {
      display: "flex",
      alignItems: "center",
      width: "100%",
      minHeight: 48,
      padding: "0 16px",
      border: "none",
      background: opt === selectedValue ? "var(--surface-variant)" : "transparent",
      color: "var(--on-surface)",
      fontFamily: "var(--font-sans)",
      fontSize: 14,
      lineHeight: "20px",
      textAlign: "left",
      cursor: "pointer"
    },
    onMouseEnter: e => {
      if (opt !== selectedValue) e.currentTarget.style.background = "color-mix(in srgb, var(--on-surface) 8%, transparent)";
    },
    onMouseLeave: e => {
      if (opt !== selectedValue) e.currentTarget.style.background = "transparent";
    }
  }, opt))));
}
Object.assign(__ds_scope, { CmpDropdown });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpDropdown.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpRadioButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpRadioButton — single choice from a group. Active = primary.
 * 48dp touch area.
 */
function CmpRadioButton({
  selected = false,
  onClick,
  disabled = false,
  style,
  ...rest
}) {
  const color = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : selected ? "var(--primary)" : "var(--on-surface-variant)";
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    role: "radio",
    "aria-checked": selected,
    disabled: disabled,
    onClick: onClick,
    style: {
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      width: "var(--touch-min)",
      height: "var(--touch-min)",
      padding: 0,
      border: "none",
      background: "transparent",
      cursor: disabled ? "default" : "pointer",
      flex: "0 0 auto",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      width: 20,
      height: 20,
      borderRadius: 999,
      border: `2px solid ${color}`,
      display: "flex",
      alignItems: "center",
      justifyContent: "center"
    }
  }, selected && /*#__PURE__*/React.createElement("span", {
    style: {
      width: 10,
      height: 10,
      borderRadius: 999,
      background: color
    }
  })));
}
Object.assign(__ds_scope, { CmpRadioButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpRadioButton.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpSlider.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useRef,
  useCallback
} = React;
/**
 * CmpSlider — choose a value in a range. Active track = primary,
 * inactive = surfaceVariant. Optional discrete steps.
 */
function CmpSlider({
  value = 0,
  onValueChange,
  min = 0,
  max = 1,
  step = 0,
  disabled = false,
  style,
  ...rest
}) {
  const ref = useRef(null);
  const pct = (value - min) / (max - min) * 100;
  const setFromClientX = useCallback(clientX => {
    const el = ref.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    let r = (clientX - rect.left) / rect.width;
    r = Math.max(0, Math.min(1, r));
    let v = min + r * (max - min);
    if (step > 0) v = Math.round(v / step) * step;
    onValueChange && onValueChange(Math.max(min, Math.min(max, v)));
  }, [min, max, step, onValueChange]);
  const onDown = e => {
    if (disabled) return;
    setFromClientX(e.clientX);
    const move = ev => setFromClientX(ev.clientX);
    const up = () => {
      window.removeEventListener("mousemove", move);
      window.removeEventListener("mouseup", up);
    };
    window.addEventListener("mousemove", move);
    window.addEventListener("mouseup", up);
  };
  return /*#__PURE__*/React.createElement("div", _extends({
    ref: ref,
    role: "slider",
    "aria-valuemin": min,
    "aria-valuemax": max,
    "aria-valuenow": value,
    onMouseDown: onDown,
    style: {
      position: "relative",
      height: 44,
      display: "flex",
      alignItems: "center",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.38 : 1,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 0,
      right: 0,
      height: 4,
      borderRadius: 999,
      background: "var(--surface-variant)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 0,
      width: `${pct}%`,
      height: 4,
      borderRadius: 999,
      background: "var(--primary)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: `calc(${pct}% - 10px)`,
      width: 20,
      height: 20,
      borderRadius: 999,
      background: "var(--primary)",
      boxShadow: "var(--elevation-1)"
    }
  }));
}
Object.assign(__ds_scope, { CmpSlider });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpSlider.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpSwitch.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSwitch — binary on/off. Active track = primary. Optional check
 * icon on the thumb when checked. 48dp touch area.
 */
function CmpSwitch({
  checked = false,
  onCheckedChange,
  disabled = false,
  thumbIcon = false,
  style,
  ...rest
}) {
  const trackColor = disabled ? "color-mix(in srgb, var(--on-surface) 12%, transparent)" : checked ? "var(--primary)" : "var(--surface-container-highest)";
  const borderColor = checked ? "var(--primary)" : "var(--outline)";
  const thumbColor = disabled ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : checked ? "var(--on-primary)" : "var(--outline)";
  const thumbSize = checked ? 24 : 16;
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    role: "switch",
    "aria-checked": checked,
    disabled: disabled,
    onClick: () => onCheckedChange && onCheckedChange(!checked),
    style: {
      position: "relative",
      width: 52,
      height: 32,
      padding: 0,
      borderRadius: 999,
      border: `2px solid ${borderColor}`,
      background: trackColor,
      cursor: disabled ? "default" : "pointer",
      transition: "background var(--motion-fast)",
      flex: "0 0 auto",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      top: "50%",
      left: checked ? `calc(100% - ${thumbSize}px - 4px)` : 6,
      width: thumbSize,
      height: thumbSize,
      transform: "translateY(-50%)",
      borderRadius: 999,
      background: thumbColor,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      transition: "all var(--motion-fast)"
    }
  }, thumbIcon && checked && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
    icon: "Check",
    size: 16,
    color: "var(--primary)"
  })));
}
Object.assign(__ds_scope, { CmpSwitch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpSwitch.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpTextField.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useState
} = React;
/**
 * CmpTextField — outlined (default) or filled text input. Height 56dp,
 * floating label, optional leading/trailing icon, supporting text and
 * error state. Border outline → primary on focus, error on error.
 */
function CmpTextField({
  value = "",
  onChange,
  label,
  placeholder,
  style: variant = "Outlined",
  disabled = false,
  readOnly = false,
  isError = false,
  singleLine = false,
  supportingText,
  leadingIcon,
  trailingIcon,
  type = "text",
  rootStyle,
  ...rest
}) {
  const [focus, setFocus] = useState(false);
  const filled = variant === "Filled";
  const floated = focus || value !== "" || placeholder;
  const borderColor = isError ? "var(--error)" : focus ? "var(--primary)" : "var(--outline)";
  const labelColor = isError ? "var(--error)" : focus ? "var(--primary)" : "var(--on-surface-variant)";
  const muted = disabled ? 0.38 : 1;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%",
      opacity: muted,
      ...rootStyle
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-m)",
      minHeight: "var(--field-height)",
      padding: "0 16px",
      background: filled ? "var(--surface-container-highest)" : "transparent",
      border: filled ? "none" : `${focus ? 2 : 1}px solid ${borderColor}`,
      borderBottom: filled ? `${focus ? 2 : 1}px solid ${borderColor}` : undefined,
      borderRadius: filled ? "var(--radius-xs) var(--radius-xs) 0 0" : "var(--radius-xs)"
    }
  }, leadingIcon && /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--on-surface-variant)",
      display: "inline-flex"
    }
  }, leadingIcon), label && /*#__PURE__*/React.createElement("label", {
    style: {
      position: "absolute",
      left: leadingIcon ? 48 : 12,
      padding: "0 4px",
      background: filled ? "transparent" : "var(--surface)",
      pointerEvents: "none",
      color: labelColor,
      fontFamily: "var(--font-sans)",
      transition: "all var(--motion-fast)",
      top: floated ? -8 : "50%",
      transform: floated ? "none" : "translateY(-50%)",
      fontSize: floated ? "var(--body-small-size)" : "var(--body-large-size)",
      lineHeight: 1
    }
  }, label), /*#__PURE__*/React.createElement("input", _extends({
    value: value,
    type: type,
    placeholder: floated ? placeholder : undefined,
    disabled: disabled,
    readOnly: readOnly,
    onChange: e => onChange && onChange(e.target.value),
    onFocus: () => setFocus(true),
    onBlur: () => setFocus(false),
    style: {
      flex: 1,
      minWidth: 0,
      border: "none",
      outline: "none",
      background: "transparent",
      color: "var(--on-surface)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--body-large-size)",
      lineHeight: "var(--body-large-line)",
      padding: singleLine ? "0" : "16px 0"
    }
  }, rest)), trailingIcon && /*#__PURE__*/React.createElement("span", {
    style: {
      color: isError ? "var(--error)" : "var(--on-surface-variant)",
      display: "inline-flex"
    }
  }, trailingIcon)), supportingText && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "4px 16px 0"
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "bodySmall",
    color: isError ? "var(--error)" : "var(--on-surface-variant)"
  }, supportingText)));
}
Object.assign(__ds_scope, { CmpTextField });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpTextField.jsx", error: String((e && e.message) || e) }); }

// components/navigation/CmpNavigationBar.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpNavigationBar — bottom section switcher. 80dp tall. Each item:
 * 24dp icon (in a pill indicator when selected) + optional label.
 * items: [{ icon, label, selected, onClick, badge }]
 */
function CmpNavigationBar({
  items = [],
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("nav", _extends({
    style: {
      display: "flex",
      height: "var(--navbar-height)",
      background: "var(--surface-container)",
      borderTop: "1px solid var(--outline-variant)",
      ...style
    }
  }, rest), items.map((item, i) => {
    const selected = item.selected;
    const color = selected ? "var(--on-secondary-container)" : "var(--on-surface-variant)";
    return /*#__PURE__*/React.createElement("button", {
      key: i,
      type: "button",
      onClick: item.onClick,
      "aria-current": selected ? "page" : undefined,
      style: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 4,
        border: "none",
        background: "transparent",
        cursor: "pointer",
        padding: "12px 0"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        position: "relative",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        width: 64,
        height: 32,
        borderRadius: 999,
        background: selected ? "var(--secondary-container)" : "transparent",
        transition: "background var(--motion-fast)"
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
      icon: item.icon,
      size: 24,
      color: color
    }), item.badge != null && /*#__PURE__*/React.createElement("span", {
      style: {
        position: "absolute",
        top: 2,
        right: 14,
        minWidth: 16,
        height: 16,
        padding: "0 4px",
        borderRadius: 999,
        background: "var(--error)",
        color: "var(--on-error)",
        fontSize: 11,
        fontFamily: "var(--font-sans)",
        fontWeight: 500,
        display: "flex",
        alignItems: "center",
        justifyContent: "center"
      }
    }, item.badge)), item.label && /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
      variant: "labelMedium",
      color: color
    }, item.label));
  }));
}
Object.assign(__ds_scope, { CmpNavigationBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/CmpNavigationBar.jsx", error: String((e && e.message) || e) }); }

// components/navigation/CmpTabRow.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpTabRow — switch views within a screen. Fixed (equal width) or
 * scrollable. Label = labelLarge; selected tab + indicator use primary.
 * tabs: [{ title, icon }]
 */
function CmpTabRow({
  tabs = [],
  selectedTabIndex = 0,
  onTabSelected,
  scrollable = false,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "tablist",
    style: {
      display: "flex",
      overflowX: scrollable ? "auto" : "visible",
      borderBottom: "1px solid var(--outline-variant)",
      background: "var(--background)",
      ...style
    }
  }, rest), tabs.map((tab, i) => {
    const selected = i === selectedTabIndex;
    const color = selected ? "var(--primary)" : "var(--on-surface-variant)";
    return /*#__PURE__*/React.createElement("button", {
      key: i,
      role: "tab",
      "aria-selected": selected,
      type: "button",
      onClick: () => onTabSelected && onTabSelected(i),
      style: {
        position: "relative",
        flex: scrollable ? "0 0 auto" : 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 4,
        minHeight: 48,
        padding: scrollable ? "0 24px" : "0 16px",
        border: "none",
        background: "transparent",
        color,
        cursor: "pointer",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--label-large-size)",
        lineHeight: "var(--label-large-line)",
        fontWeight: "var(--weight-medium)",
        whiteSpace: "nowrap"
      }
    }, tab.icon && /*#__PURE__*/React.createElement(__ds_scope.CmpIcon, {
      icon: tab.icon,
      size: 24,
      color: color
    }), tab.title, /*#__PURE__*/React.createElement("span", {
      style: {
        position: "absolute",
        bottom: 0,
        left: scrollable ? 24 : 12,
        right: scrollable ? 24 : 12,
        height: 3,
        borderRadius: "3px 3px 0 0",
        background: selected ? "var(--primary)" : "transparent",
        transition: "background var(--motion-fast)"
      }
    }));
  }));
}
Object.assign(__ds_scope, { CmpTabRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/CmpTabRow.jsx", error: String((e && e.message) || e) }); }

// components/navigation/CmpTopAppBar.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpTopAppBar — screen header. 64dp tall, background fill, onBackground
 * content. Slots: navigationIcon, title (+ optional subtitle), actions.
 * Style "Small" (leading title) or "CenterAligned".
 */
function CmpTopAppBar({
  title,
  subtitle,
  navigationIcon,
  actions,
  centered = false,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("header", _extends({
    style: {
      display: "flex",
      alignItems: "center",
      height: "var(--topbar-height)",
      padding: "0 8px",
      background: "var(--background)",
      color: "var(--on-background)",
      gap: 8,
      ...style
    }
  }, rest), navigationIcon && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flex: "0 0 auto"
    }
  }, navigationIcon), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      paddingLeft: 8,
      display: "flex",
      flexDirection: "column",
      justifyContent: "center",
      alignItems: centered ? "center" : "flex-start"
    }
  }, typeof title === "string" ? /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "titleLarge",
    maxLines: 1
  }, title) : title, subtitle && (typeof subtitle === "string" ? /*#__PURE__*/React.createElement(__ds_scope.CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)",
    maxLines: 1
  }, subtitle) : subtitle)), actions && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      paddingRight: 8,
      flex: "0 0 auto"
    }
  }, actions));
}
Object.assign(__ds_scope, { CmpTopAppBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/CmpTopAppBar.jsx", error: String((e && e.message) || e) }); }

// components/progress/CmpCircularProgressIndicator.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpCircularProgressIndicator — determinate when `progress` (0..1)
 * is given, otherwise indeterminate (spins). Arc thickness 4dp.
 */
function CmpCircularProgressIndicator({
  progress = null,
  size = 48,
  strokeWidth = 4,
  color = "var(--primary)",
  trackColor = "transparent",
  style,
  ...rest
}) {
  const r = (size - strokeWidth) / 2;
  const c = 2 * Math.PI * r;
  const indeterminate = progress == null;
  const dash = indeterminate ? c * 0.25 : c * progress;
  return /*#__PURE__*/React.createElement("span", _extends({
    role: "progressbar",
    "aria-valuenow": indeterminate ? undefined : Math.round(progress * 100),
    style: {
      display: "inline-flex",
      width: size,
      height: size,
      // Spin the whole element around its own centre. Rotating the inner
      // <circle> instead is unreliable: SVG shapes take transform-origin
      // from the viewport corner (0,0), so the arc orbits rather than spins.
      ...(indeterminate ? {
        animation: "cmp-spin 0.9s linear infinite"
      } : {}),
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("svg", {
    width: size,
    height: size,
    viewBox: `0 0 ${size} ${size}`
  }, trackColor !== "transparent" && /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: trackColor,
    strokeWidth: strokeWidth
  }), /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: color,
    strokeWidth: strokeWidth,
    strokeLinecap: "round",
    strokeDasharray: `${dash} ${c}`,
    transform: `rotate(-90 ${size / 2} ${size / 2})`,
    style: indeterminate ? undefined : {
      transition: "stroke-dasharray var(--motion-standard)"
    }
  })), /*#__PURE__*/React.createElement("style", null, `@keyframes cmp-spin{from{transform:rotate(0deg)}to{transform:rotate(360deg)}}`));
}
Object.assign(__ds_scope, { CmpCircularProgressIndicator });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/CmpCircularProgressIndicator.jsx", error: String((e && e.message) || e) }); }

// components/progress/CmpLinearProgressIndicator.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpLinearProgressIndicator — determinate when `progress` (0..1)
 * is given, otherwise indeterminate. Track = surfaceVariant.
 */
function CmpLinearProgressIndicator({
  progress = null,
  color = "var(--primary)",
  trackColor = "var(--surface-variant)",
  height = 4,
  style,
  ...rest
}) {
  const indeterminate = progress == null;
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "progressbar",
    "aria-valuenow": indeterminate ? undefined : Math.round(progress * 100),
    style: {
      width: "100%",
      height,
      borderRadius: height,
      background: trackColor,
      overflow: "hidden",
      position: "relative",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("div", {
    style: indeterminate ? {
      position: "absolute",
      height: "100%",
      width: "40%",
      borderRadius: height,
      background: color,
      animation: "cmp-linear 1.4s ease-in-out infinite"
    } : {
      height: "100%",
      width: `${Math.max(0, Math.min(1, progress)) * 100}%`,
      borderRadius: height,
      background: color,
      transition: "width var(--motion-standard)"
    }
  }), /*#__PURE__*/React.createElement("style", null, `@keyframes cmp-linear{0%{left:-40%}100%{left:100%}}`));
}
Object.assign(__ds_scope, { CmpLinearProgressIndicator });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/CmpLinearProgressIndicator.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpCard.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpCard — content container. Shadow 1dp, medium radius, surface fill.
 * Pass `onClick` to make it clickable (adds a hover state layer).
 */
function CmpCard({
  children,
  onClick,
  elevation = 1,
  border = false,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const clickable = !!onClick;
  const shadow = elevation === 0 ? "none" : elevation >= 3 ? "var(--elevation-3)" : elevation === 2 ? "var(--elevation-2)" : "var(--elevation-1)";
  return /*#__PURE__*/React.createElement("div", _extends({
    onClick: onClick
  }, clickable ? handlers : {}, {
    style: {
      position: "relative",
      background: "var(--surface)",
      color: "var(--on-surface)",
      borderRadius: "var(--radius-m)",
      boxShadow: shadow,
      border: border ? "1px solid var(--outline-variant)" : "none",
      overflow: "hidden",
      cursor: clickable ? "pointer" : "default",
      ...style
    }
  }, rest), clickable && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--on-surface)",
      opacity,
      pointerEvents: "none"
    }
  }), children);
}
Object.assign(__ds_scope, { CmpCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpCard.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpDivider.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpDivider — 1dp separator. Horizontal by default; pass
 * `orientation="vertical"` for a vertical rule. Color defaults to
 * outlineVariant.
 */
function CmpDivider({
  orientation = "horizontal",
  thickness = 1,
  color = "var(--outline-variant)",
  inset = 0,
  style,
  ...rest
}) {
  const vertical = orientation === "vertical";
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "separator",
    "aria-orientation": orientation,
    style: vertical ? {
      width: `${thickness}px`,
      alignSelf: "stretch",
      background: color,
      marginBlock: inset,
      flex: "0 0 auto",
      ...style
    } : {
      height: `${thickness}px`,
      width: "100%",
      background: color,
      marginInline: inset,
      border: "none",
      ...style
    }
  }, rest));
}
Object.assign(__ds_scope, { CmpDivider });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpDivider.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpSelectableCard.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSelectableCard — card with a selected state. 16dp padding, 1.5dp
 * border, medium radius, 300ms color transition.
 * Selected: primary border + primaryContainer fill.
 * Unselected: outline border + surface fill.
 */
function CmpSelectableCard({
  selected = false,
  onClick,
  icon,
  children,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "button",
    "aria-pressed": selected,
    onClick: onClick,
    style: {
      display: "flex",
      alignItems: "center",
      gap: "var(--space-m)",
      width: "100%",
      padding: "var(--space-l)",
      borderRadius: "var(--radius-m)",
      border: `1.5px solid ${selected ? "var(--primary)" : "var(--outline)"}`,
      background: selected ? "var(--primary-container)" : "var(--surface)",
      color: selected ? "var(--on-primary-container)" : "var(--on-surface)",
      cursor: "pointer",
      transition: "background 300ms cubic-bezier(0.2,0,0,1), border-color 300ms cubic-bezier(0.2,0,0,1)",
      boxSizing: "border-box",
      ...style
    }
  }, rest), icon && /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      flex: "0 0 auto"
    }
  }, icon), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, children));
}
Object.assign(__ds_scope, { CmpSelectableCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpSelectableCard.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpSurface.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSurface — base container. Square corners (0dp) and no shadow by
 * default; opt into tonal/shadow elevation, border, and a custom shape.
 * Pass `onClick` for a clickable surface (used e.g. for calculator keys).
 */
function CmpSurface({
  children,
  onClick,
  color = "var(--surface)",
  contentColor = "var(--on-surface)",
  radius = 0,
  shadowElevation = 0,
  border = false,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers
  } = __ds_scope.useStateLayer();
  const clickable = !!onClick;
  const shadow = shadowElevation === 0 ? "none" : shadowElevation >= 3 ? "var(--elevation-3)" : shadowElevation === 2 ? "var(--elevation-2)" : "var(--elevation-1)";
  return /*#__PURE__*/React.createElement("div", _extends({
    onClick: onClick
  }, clickable ? handlers : {}, {
    style: {
      position: "relative",
      background: color,
      color: contentColor,
      borderRadius: radius,
      boxShadow: shadow,
      border: border ? "1px solid var(--outline-variant)" : "none",
      overflow: "hidden",
      cursor: clickable ? "pointer" : "default",
      ...style
    }
  }, rest), clickable && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "currentColor",
      opacity,
      pointerEvents: "none"
    }
  }), children);
}
Object.assign(__ds_scope, { CmpSurface });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpSurface.jsx", error: String((e && e.message) || e) }); }

// features/auth/Auth.jsx
try { (() => {
// Auth.jsx — Pandora Spec auth flow controller.
//   • AuthFlow   — live, interactive flow (real navigation + form state +
//                  scripted async). Used by Auth.html / AuthTablet.html /
//                  AuthDesktop.html.
//   • AuthPinned — renders one screen pinned to one explicit state with
//                  representative values. Used by AuthStates.html (gallery)
//                  and by the demo state-selector.
// Both reuse the presentational components from AuthScreens.jsx, so the live
// flow and the gallery never drift apart. Built only from the design system.

const {
  AuthRootScreen,
  LoginScreen,
  RegisterScreen,
  ForgotScreen,
  LoggedInScreen,
  useAuthMode
} = window.AuthScreens;

// ── validation helpers ───────────────────────────────────────────────────────
const reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const rePhone = /^[+\d][\d\s()-]{5,}$/;
const validEmail = v => reEmail.test(v.trim());
const validPhone = v => rePhone.test(v.trim());

// ════════════════════════════════════════════════════════════════════════════
// AuthFlow — interactive
// ════════════════════════════════════════════════════════════════════════════
function AuthFlow({
  lang: langProp,
  onLang,
  initialScreen = "login"
}) {
  const {
    useState,
    useRef,
    useEffect
  } = React;
  const [rootRef, mode] = useAuthMode();
  const [langState, setLangState] = useState("en");
  const lang = langProp || langState;
  const setLang = l => onLang ? onLang(l) : setLangState(l);
  const t = window.AuthT(lang);
  const [screen, setScreen] = useState(initialScreen);
  const [busy, setBusy] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const timer = useRef(null);
  const after = (ms, fn) => {
    clearTimeout(timer.current);
    timer.current = setTimeout(fn, ms);
  };
  useEffect(() => () => clearTimeout(timer.current), []);

  // ── login state ──
  const [lEmail, setLEmail] = useState("");
  const [lPass, setLPass] = useState("");
  const [lError, setLError] = useState(false);
  const [activationOpen, setActivationOpen] = useState(false);
  const [activationSent, setActivationSent] = useState(false);

  // ── register state ──
  const [rName, setRName] = useState("");
  const [rEmail, setREmail] = useState("");
  const [rPhone, setRPhone] = useState("");
  const [rPass, setRPass] = useState("");
  const [installer, setInstaller] = useState(false);
  const [rErrors, setRErrors] = useState({});
  const [rServerError, setRServerError] = useState(false);
  const [rSuccess, setRSuccess] = useState(false);

  // ── forgot state ──
  const [fEmail, setFEmail] = useState("");
  const [fError, setFError] = useState(false);
  const [fSuccess, setFSuccess] = useState(false);

  // ── logged-in state ──
  const [logoutError, setLogoutError] = useState(false);

  // Root: simulate the session check, then land on login.
  useEffect(() => {
    if (initialScreen === "root") {
      setBusy(true);
      after(1300, () => {
        setBusy(false);
        setScreen("login");
      });
    }
    // eslint-disable-next-line
  }, []);
  const go = s => {
    setBusy(false);
    setLError(false);
    setRServerError(false);
    setFError(false);
    setLogoutError(false);
    setScreen(s);
  };

  // ── login submit (scripted): email contains "fail" → error,
  //    "activate"/"new" → activation dialog, else success → loggedIn. ──
  const loginCanSubmit = lEmail.trim() !== "" && lPass !== "" && !busy;
  const submitLogin = () => {
    if (!loginCanSubmit) return;
    setLError(false);
    setBusy(true);
    after(1300, () => {
      setBusy(false);
      const e = lEmail.toLowerCase();
      if (e.includes("fail")) setLError(true);else if (e.includes("activate") || e.includes("new")) {
        setActivationSent(false);
        setActivationOpen(true);
      } else go("loggedIn");
    });
  };
  const registerCanSubmit = rName.trim() && rEmail.trim() && rPhone.trim() && rPass !== "" && installer && !busy;
  const submitRegister = () => {
    if (busy) return;
    const errs = {};
    if (!rName.trim()) errs.name = true;
    if (!validEmail(rEmail)) errs.email = true;
    if (!validPhone(rPhone)) errs.phone = true;
    if (rPass.length < 6) errs.password = true;
    if (!installer) return; // checkbox gates the button; nothing to submit
    if (Object.keys(errs).length) {
      setRErrors(errs);
      return;
    }
    setRErrors({});
    setRServerError(false);
    setBusy(true);
    after(1400, () => {
      setBusy(false);
      if (rEmail.toLowerCase().includes("fail")) setRServerError(true);else setRSuccess(true);
    });
  };
  const forgotCanSubmit = fEmail.trim() !== "" && !busy;
  const submitForgot = () => {
    if (!forgotCanSubmit) return;
    setFError(false);
    setBusy(true);
    after(1300, () => {
      setBusy(false);
      if (fEmail.toLowerCase().includes("fail")) setFError(true);else setFSuccess(true);
    });
  };
  const submitLogout = () => {
    if (busy) return;
    setLogoutError(false);
    setBusy(true);
    after(1200, () => {
      setBusy(false);
      // reset login form for a clean return
      setLEmail("");
      setLPass("");
      setLError(false);
      go("login");
    });
  };
  let node;
  if (screen === "root") {
    node = /*#__PURE__*/React.createElement(AuthRootScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      state: busy ? "loading" : "error",
      onRetry: () => {
        setBusy(true);
        after(1300, () => {
          setBusy(false);
          go("login");
        });
      }
    });
  } else if (screen === "login") {
    node = /*#__PURE__*/React.createElement(LoginScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      email: lEmail,
      password: lPass,
      showPassword: showPassword,
      onEmail: setLEmail,
      onPassword: setLPass,
      onToggleShow: setShowPassword,
      loading: busy,
      error: lError,
      canSubmit: loginCanSubmit,
      onSubmit: submitLogin,
      onForgot: () => {
        setFEmail("");
        setFError(false);
        setFSuccess(false);
        go("forgot");
      },
      onSignUp: () => {
        setRErrors({});
        setRServerError(false);
        setRSuccess(false);
        go("register");
      },
      activationOpen: activationOpen,
      activationSent: activationSent,
      onResendActivation: () => setActivationSent(true),
      onCloseActivation: () => setActivationOpen(false)
    });
  } else if (screen === "register") {
    node = /*#__PURE__*/React.createElement(RegisterScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      onBack: () => go("login"),
      name: rName,
      email: rEmail,
      phone: rPhone,
      password: rPass,
      showPassword: showPassword,
      installer: installer,
      onName: setRName,
      onEmail: setREmail,
      onPhone: setRPhone,
      onPassword: setRPass,
      onToggleShow: setShowPassword,
      onInstaller: setInstaller,
      loading: busy,
      errors: rErrors,
      serverError: rServerError,
      success: rSuccess,
      canSubmit: registerCanSubmit,
      onSubmit: submitRegister,
      onGoLogin: () => {
        setRSuccess(false);
        go("login");
      }
    });
  } else if (screen === "forgot") {
    node = /*#__PURE__*/React.createElement(ForgotScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      onBack: () => go("login"),
      email: fEmail,
      onEmail: setFEmail,
      loading: busy,
      error: fError,
      success: fSuccess,
      canSubmit: forgotCanSubmit,
      onSubmit: submitForgot,
      onGoLogin: () => {
        setFSuccess(false);
        go("login");
      }
    });
  } else {
    node = /*#__PURE__*/React.createElement(LoggedInScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      loading: busy,
      error: logoutError,
      onLogout: submitLogout
    });
  }
  return /*#__PURE__*/React.createElement("div", {
    ref: rootRef,
    style: {
      height: "100%",
      width: "100%"
    }
  }, node);
}

// ════════════════════════════════════════════════════════════════════════════
// AuthPinned — render a screen pinned to an explicit state
// ════════════════════════════════════════════════════════════════════════════
const PIN = {
  login: {
    empty: {
      email: "",
      password: ""
    },
    partial: {
      email: "installer@",
      password: ""
    },
    ready: {
      email: "ivan@pandora.pro",
      password: "service123"
    },
    loading: {
      email: "ivan@pandora.pro",
      password: "service123",
      loading: true
    },
    error: {
      email: "ivan@pandora.pro",
      password: "service123",
      error: true
    },
    activation: {
      email: "new.installer@pandora.pro",
      password: "service123",
      activationOpen: true
    }
  },
  register: {
    empty: {
      name: "",
      email: "",
      phone: "",
      password: "",
      installer: false
    },
    partial: {
      name: "Иван Петров",
      email: "ivan@",
      phone: "",
      password: "",
      installer: false
    },
    ready: {
      name: "Иван Петров",
      email: "ivan@pandora.pro",
      phone: "+7 912 345-67-89",
      password: "service123",
      installer: true
    },
    loading: {
      name: "Иван Петров",
      email: "ivan@pandora.pro",
      phone: "+7 912 345-67-89",
      password: "service123",
      installer: true,
      loading: true
    },
    validation: {
      name: "Иван",
      email: "ivan@",
      phone: "12",
      password: "123",
      installer: true,
      errors: {
        email: true,
        phone: true,
        password: true
      }
    },
    server: {
      name: "Иван Петров",
      email: "ivan@pandora.pro",
      phone: "+7 912 345-67-89",
      password: "service123",
      installer: true,
      serverError: true
    },
    success: {
      success: true
    }
  },
  forgot: {
    empty: {
      email: ""
    },
    ready: {
      email: "ivan@pandora.pro"
    },
    loading: {
      email: "ivan@pandora.pro",
      loading: true
    },
    error: {
      email: "ivan@pandora.pro",
      error: true
    },
    success: {
      success: true
    }
  },
  loggedIn: {
    loggedIn: {},
    logoutLoading: {
      loading: true
    },
    logoutError: {
      error: true
    }
  },
  root: {
    loading: {
      state: "loading"
    },
    error: {
      state: "error"
    }
  }
};
function AuthPinned({
  screen,
  state,
  lang,
  setLang = () => {},
  mode = "compact"
}) {
  const {
    useState
  } = React;
  const t = window.AuthT(lang);
  const p = PIN[screen] && PIN[screen][state] || {};
  const [show, setShow] = useState(false);
  const noop = () => {};
  if (screen === "root") {
    return /*#__PURE__*/React.createElement(AuthRootScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      state: p.state,
      onRetry: noop
    });
  }
  if (screen === "login") {
    const canSubmit = !!(p.email && p.password) && !p.loading;
    return /*#__PURE__*/React.createElement(LoginScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      email: p.email,
      password: p.password,
      showPassword: show,
      onEmail: noop,
      onPassword: noop,
      onToggleShow: setShow,
      loading: !!p.loading,
      error: !!p.error,
      canSubmit: canSubmit,
      onSubmit: noop,
      onForgot: noop,
      onSignUp: noop,
      activationOpen: !!p.activationOpen,
      activationSent: false,
      onResendActivation: noop,
      onCloseActivation: noop
    });
  }
  if (screen === "register") {
    const canSubmit = !!(p.name && p.email && p.phone && p.password && p.installer) && !p.loading;
    return /*#__PURE__*/React.createElement(RegisterScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      onBack: noop,
      name: p.name,
      email: p.email,
      phone: p.phone,
      password: p.password,
      showPassword: show,
      installer: !!p.installer,
      onName: noop,
      onEmail: noop,
      onPhone: noop,
      onPassword: noop,
      onToggleShow: setShow,
      onInstaller: noop,
      loading: !!p.loading,
      errors: p.errors || {},
      serverError: !!p.serverError,
      success: !!p.success,
      canSubmit: canSubmit,
      onSubmit: noop,
      onGoLogin: noop
    });
  }
  if (screen === "forgot") {
    const canSubmit = !!p.email && !p.loading;
    return /*#__PURE__*/React.createElement(ForgotScreen, {
      t: t,
      lang: lang,
      setLang: setLang,
      mode: mode,
      onBack: noop,
      email: p.email,
      onEmail: noop,
      loading: !!p.loading,
      error: !!p.error,
      success: !!p.success,
      canSubmit: canSubmit,
      onSubmit: noop,
      onGoLogin: noop
    });
  }
  return /*#__PURE__*/React.createElement(LoggedInScreen, {
    t: t,
    lang: lang,
    setLang: setLang,
    mode: mode,
    loading: !!p.loading,
    error: !!p.error,
    onLogout: noop
  });
}

// State catalog (for selectors / gallery), in display order.
const AUTH_STATES = {
  root: ["loading", "error"],
  login: ["empty", "partial", "ready", "loading", "error", "activation"],
  register: ["empty", "partial", "ready", "loading", "validation", "server", "success"],
  forgot: ["empty", "ready", "loading", "error", "success"],
  loggedIn: ["loggedIn", "logoutLoading", "logoutError"]
};
window.AuthFlow = AuthFlow;
window.AuthPinned = AuthPinned;
window.AUTH_STATES = AUTH_STATES;
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/auth/Auth.jsx", error: String((e && e.message) || e) }); }

// features/auth/AuthScreens.jsx
try { (() => {
// AuthScreens — presentational auth screens for Pandora Spec, built ONLY from
// design-system Cmp* components (window.PandoraSpecDesignSystem_b8d6e1).
//
// These are pure/visual: every screen takes an explicit display state + values
// + handlers, so the same components render both the live interactive flow
// (Auth.jsx → AuthFlow) and the pinned state gallery (AuthStates.html).
//
// Adaptivity — Material 3 Window Size Classes by container width (CLAUDE.md):
//   • compact  (<600)   — form full-width, one column.
//   • medium   (600–959)— centered card on the background.
//   • expanded (≥960)   — two-pane: brand rail + form card.
//
// Candidate UI-Kit compositions (assembled from primitives, flagged in spec):
//   • PasswordField — CmpTextField + trailing visibility CmpIconButton.
//   • LangSegment   — EN | RU segmented toggle for the top app bar.

const AS_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar,
  CmpIconButton,
  CmpButton,
  CmpOutlinedButton,
  CmpTextButton,
  CmpTextField,
  CmpCheckbox,
  CmpDialog,
  CmpCard,
  CmpText,
  CmpIcon,
  CmpCircularProgressIndicator,
  CmpLinearProgressIndicator
} = AS_DS;

// Layered brand backdrop — soft primary/tertiary "blooms" over the page tone.
// Gives the auth flow depth without leaving the palette (roles adapt per theme).
const AUTH_BG = "radial-gradient(140% 95% at 100% -15%, color-mix(in srgb, var(--primary-container) 88%, transparent) 0%, transparent 55%)," + "radial-gradient(125% 90% at -20% 115%, color-mix(in srgb, var(--tertiary-container) 80%, transparent) 0%, transparent 55%)," + "var(--background)";
// Big, finger-friendly primary CTA (taller + softer corners than the 40dp default).
const BIG_BTN = {
  height: 52,
  borderRadius: 16,
  fontSize: 16
};
function AuthBackdrop() {
  return /*#__PURE__*/React.createElement("div", {
    "aria-hidden": "true",
    style: {
      position: "absolute",
      inset: 0,
      background: AUTH_BG,
      pointerEvents: "none"
    }
  });
}

// App icon (real brand mark dropped in by the user).
const APP_ICON = "./app-icon.png";

// ── Container-width hook → Window Size Class ─────────────────────────────────
function useAuthMode() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const remeasure = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => {
      ro.disconnect();
      window.removeEventListener("resize", remeasure);
    };
  }, []);
  let mode = "compact";
  if (w >= 960) mode = "expanded";else if (w >= 600) mode = "medium";
  return [ref, mode];
}

// ── EN | RU segment (UI-Kit candidate: LangSegment) ──────────────────────────
function LangSegment({
  lang,
  setLang
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      background: "var(--surface-variant)",
      borderRadius: 999,
      padding: 3
    }
  }, ["en", "ru"].map(l => {
    const on = lang === l;
    return /*#__PURE__*/React.createElement("button", {
      key: l,
      type: "button",
      onClick: () => setLang && setLang(l),
      "aria-pressed": on,
      style: {
        border: "none",
        cursor: "pointer",
        minWidth: 46,
        height: 38,
        borderRadius: 999,
        padding: "0 14px",
        background: on ? "var(--primary)" : "transparent",
        color: on ? "var(--on-primary)" : "var(--on-surface-variant)",
        fontFamily: "var(--font-sans)",
        fontSize: 13,
        fontWeight: 600,
        letterSpacing: 0.4
      }
    }, l.toUpperCase());
  }));
}

// ── Brand lockup: app icon + wordmark ────────────────────────────────────────
function LogoPlaceholder({
  t,
  big = false,
  align = "center"
}) {
  const size = big ? 84 : 60;
  const center = align === "center";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: center ? "center" : "flex-start",
      gap: big ? 18 : 12
    }
  }, /*#__PURE__*/React.createElement("img", {
    src: APP_ICON,
    alt: t("appName"),
    width: size,
    height: size,
    style: {
      display: "block",
      filter: "drop-shadow(0 10px 22px color-mix(in srgb, var(--primary) 35%, transparent))"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: center ? "center" : "flex-start",
      gap: 2
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: big ? "headlineMedium" : "titleLarge",
    weight: 700
  }, t("appName"))));
}

// ── PasswordField (UI-Kit candidate) ─────────────────────────────────────────
function PasswordField({
  t,
  value,
  onChange,
  show,
  setShow,
  isError,
  supportingText,
  disabled
}) {
  return /*#__PURE__*/React.createElement(CmpTextField, {
    label: t("password"),
    value: value,
    onChange: onChange,
    type: show ? "text" : "password",
    singleLine: true,
    disabled: disabled,
    isError: isError,
    supportingText: supportingText,
    leadingIcon: /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "Lock"
    }),
    trailingIcon: /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: show ? "VisibilityOff" : "Visibility",
      contentDescription: show ? t("hidePassword") : t("showPassword"),
      onClick: () => !disabled && setShow && setShow(!show),
      disabled: disabled,
      style: {
        width: 40,
        height: 40
      }
    })
  });
}

// ── Soft error banner ────────────────────────────────────────────────────────
function ErrorBanner({
  text
}) {
  return /*#__PURE__*/React.createElement("div", {
    role: "alert",
    style: {
      display: "flex",
      gap: "var(--space-m)",
      alignItems: "flex-start",
      padding: "12px 16px",
      borderRadius: "var(--radius-s)",
      background: "var(--error-container)",
      color: "var(--on-error-container)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "AlertCircle",
    color: "var(--on-error-container)"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-error-container)"
  }, text)));
}

// ── Submit button with built-in loading spinner ──────────────────────────────
function SubmitButton({
  loading,
  disabled,
  text,
  onClick,
  icon
}) {
  if (loading) {
    return /*#__PURE__*/React.createElement(CmpButton, {
      fullWidth: true,
      onClick: undefined,
      style: {
        ...BIG_BTN,
        cursor: "default",
        pointerEvents: "none"
      }
    }, /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 24,
      strokeWidth: 3,
      color: "var(--on-primary)"
    }));
  }
  return /*#__PURE__*/React.createElement(CmpButton, {
    fullWidth: true,
    text: text,
    icon: icon,
    disabled: disabled,
    onClick: onClick,
    style: BIG_BTN
  });
}

// ── Shared chrome: layered backdrop + top bar + loading bar + body + dialog ───
function ScreenChrome({
  topBar,
  loading,
  children,
  overlay
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      height: "100%",
      width: "100%",
      overflow: "hidden",
      color: "var(--on-background)"
    }
  }, /*#__PURE__*/React.createElement(AuthBackdrop, null), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      zIndex: 1,
      height: "100%",
      display: "flex",
      flexDirection: "column"
    }
  }, topBar, /*#__PURE__*/React.createElement("div", {
    style: {
      height: 4,
      flex: "0 0 auto"
    }
  }, loading && /*#__PURE__*/React.createElement(CmpLinearProgressIndicator, null)), children), overlay, /*#__PURE__*/React.createElement("style", null, `@keyframes authIn{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:none}}@media (prefers-reduced-motion:reduce){.auth-anim{animation:none!important}}`));
}

// ── Body layout by window-size class ─────────────────────────────────────────
// A "floating" surface card on the brand backdrop. The card keeps --surface so
// the outlined-field label notch (also --surface) blends in; the backdrop uses
// --background so the card visibly lifts off it. `logoTop` adds the brand hero
// above the card on compact/medium (login only).
function AuthBody({
  t,
  mode,
  logoTop,
  children
}) {
  const card = /*#__PURE__*/React.createElement(CmpCard, {
    className: "auth-anim",
    elevation: 2,
    border: true,
    style: {
      boxSizing: "border-box",
      width: "100%",
      padding: 28,
      borderRadius: 28,
      animation: "authIn .5s ease both"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-l)"
    }
  }, children));
  if (mode === "expanded") {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        overflow: "auto",
        display: "flex"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        margin: "auto",
        minWidth: 0,
        display: "flex",
        alignItems: "center",
        gap: 64,
        padding: "48px 56px",
        boxSizing: "border-box"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "1 1 0",
        maxWidth: 420,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        textAlign: "center",
        gap: "var(--space-l)"
      }
    }, /*#__PURE__*/React.createElement(LogoPlaceholder, {
      t: t,
      big: true
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        maxWidth: 360
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, t("loginSubtitle"))), /*#__PURE__*/React.createElement("div", {
      style: {
        width: 64,
        height: 4,
        borderRadius: 999,
        background: "var(--primary)",
        marginTop: 4
      }
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "0 0 440px",
        maxWidth: 440
      }
    }, card)));
  }
  if (mode === "medium") {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        overflow: "auto",
        display: "flex"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        margin: "auto",
        minWidth: 0,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: "var(--space-xl)",
        padding: "40px 24px",
        boxSizing: "border-box",
        width: "100%",
        maxWidth: 420
      }
    }, logoTop && /*#__PURE__*/React.createElement(LogoPlaceholder, {
      t: t
    }), card));
  }

  // compact
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "auto",
      display: "flex"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      margin: "auto",
      minWidth: 0,
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      gap: "var(--space-xl)",
      padding: "24px 24px 32px",
      boxSizing: "border-box",
      width: "100%",
      maxWidth: 460
    }
  }, logoTop && /*#__PURE__*/React.createElement(LogoPlaceholder, {
    t: t
  }), card));
}

// Success panel (registration / forgot success) — replaces the form.
function SuccessPanel({
  icon,
  title,
  text,
  primary
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      textAlign: "center",
      gap: "var(--space-l)",
      padding: "8px 0"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 72,
      height: 72,
      borderRadius: 999,
      background: "color-mix(in srgb, var(--success) 18%, transparent)",
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 40,
    color: "var(--success)"
  })), /*#__PURE__*/React.createElement(CmpText, {
    variant: "headlineSmall"
  }, title), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, text), /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%",
      paddingTop: "var(--space-s)"
    }
  }, primary));
}

// ════════════════════════════════════════════════════════════════════════════
// AUTH ROOT — session check
// ════════════════════════════════════════════════════════════════════════════
function AuthRootScreen({
  t,
  lang,
  setLang,
  state = "loading",
  onRetry
}) {
  const topBar = /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: "",
    style: {
      background: "transparent"
    },
    actions: /*#__PURE__*/React.createElement(LangSegment, {
      lang: lang,
      setLang: setLang
    })
  });
  return /*#__PURE__*/React.createElement(ScreenChrome, {
    topBar: topBar
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: 32
    }
  }, state === "error" ? /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      textAlign: "center",
      gap: "var(--space-l)",
      maxWidth: 360
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "AlertCircle",
    size: 40,
    color: "var(--error)"
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleLarge"
  }, t("rootErrorTitle")), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, t("rootErrorText")), /*#__PURE__*/React.createElement("div", {
    style: {
      paddingTop: "var(--space-s)"
    }
  }, /*#__PURE__*/React.createElement(CmpButton, {
    text: t("retry"),
    icon: "Refresh",
    onClick: onRetry
  }))) :
  /*#__PURE__*/
  // Loading — calm, no "checking authorization" text.
  React.createElement(CmpCircularProgressIndicator, {
    size: 48
  })));
}

// ════════════════════════════════════════════════════════════════════════════
// LOGIN
// ════════════════════════════════════════════════════════════════════════════
function LoginScreen(props) {
  const {
    t,
    lang,
    setLang,
    mode = "compact",
    email = "",
    password = "",
    showPassword = false,
    onEmail,
    onPassword,
    onToggleShow,
    loading = false,
    error = false,
    canSubmit = false,
    onSubmit,
    onForgot,
    onSignUp,
    activationOpen = false,
    activationSent = false,
    onResendActivation,
    onCloseActivation
  } = props;
  const topBar = /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: "",
    style: {
      background: "transparent"
    },
    actions: /*#__PURE__*/React.createElement(LangSegment, {
      lang: lang,
      setLang: setLang
    })
  });
  const form = /*#__PURE__*/React.createElement(React.Fragment, null, mode === "expanded" && /*#__PURE__*/React.createElement(CmpText, {
    variant: "headlineSmall"
  }, t("loginTitle")), error && /*#__PURE__*/React.createElement(ErrorBanner, {
    text: t("loginError")
  }), /*#__PURE__*/React.createElement(CmpTextField, {
    label: t("email"),
    value: email,
    onChange: onEmail,
    type: "email",
    singleLine: true,
    disabled: loading,
    isError: error,
    leadingIcon: /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "Email"
    })
  }), /*#__PURE__*/React.createElement(PasswordField, {
    t: t,
    value: password,
    onChange: onPassword,
    show: showPassword,
    setShow: onToggleShow,
    disabled: loading,
    isError: error
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "flex-end",
      marginTop: -12
    }
  }, /*#__PURE__*/React.createElement(CmpTextButton, {
    text: t("forgot"),
    onClick: onForgot,
    disabled: loading
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: "var(--space-s)"
    }
  }, /*#__PURE__*/React.createElement(SubmitButton, {
    loading: loading,
    disabled: !canSubmit,
    text: t("loginBtn"),
    onClick: onSubmit
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      gap: 4,
      flexWrap: "wrap",
      paddingTop: "var(--space-xs)"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, t("noAccount")), /*#__PURE__*/React.createElement(CmpTextButton, {
    text: t("signUp"),
    onClick: onSignUp,
    disabled: loading
  })));
  const dialog = /*#__PURE__*/React.createElement(CmpDialog, {
    open: activationOpen,
    onDismissRequest: onCloseActivation,
    icon: "Email",
    title: t("activationTitle"),
    text: activationSent ? t("activationSent") : t("activationText"),
    dismissButton: /*#__PURE__*/React.createElement(CmpTextButton, {
      text: t("close"),
      onClick: onCloseActivation
    }),
    confirmButton: !activationSent && /*#__PURE__*/React.createElement(CmpTextButton, {
      text: t("resendActivation"),
      onClick: onResendActivation
    })
  });
  return /*#__PURE__*/React.createElement(ScreenChrome, {
    topBar: topBar,
    loading: loading,
    overlay: dialog
  }, /*#__PURE__*/React.createElement(AuthBody, {
    t: t,
    mode: mode,
    logoTop: mode !== "expanded"
  }, form));
}

// ════════════════════════════════════════════════════════════════════════════
// REGISTRATION
// ════════════════════════════════════════════════════════════════════════════
function RegisterScreen(props) {
  const {
    t,
    lang,
    setLang,
    mode = "compact",
    onBack,
    name = "",
    email = "",
    phone = "",
    password = "",
    showPassword = false,
    installer = false,
    onName,
    onEmail,
    onPhone,
    onPassword,
    onToggleShow,
    onInstaller,
    loading = false,
    errors = {},
    serverError = false,
    success = false,
    canSubmit = false,
    onSubmit,
    onGoLogin
  } = props;
  const topBar = /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: t("registerTitle"),
    style: {
      background: "transparent"
    },
    navigationIcon: /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: t("backToLogin"),
      onClick: onBack
    }),
    actions: /*#__PURE__*/React.createElement(LangSegment, {
      lang: lang,
      setLang: setLang
    })
  });
  let body;
  if (success) {
    body = /*#__PURE__*/React.createElement(AuthBody, {
      t: t,
      mode: mode
    }, /*#__PURE__*/React.createElement(SuccessPanel, {
      icon: "CheckCircle",
      title: t("regSuccessTitle"),
      text: t("regSuccessText"),
      primary: /*#__PURE__*/React.createElement(CmpButton, {
        fullWidth: true,
        text: t("goToLogin"),
        onClick: onGoLogin,
        style: BIG_BTN
      })
    }));
  } else {
    const form = /*#__PURE__*/React.createElement(React.Fragment, null, serverError && /*#__PURE__*/React.createElement(ErrorBanner, {
      text: t("serverError")
    }), /*#__PURE__*/React.createElement(CmpTextField, {
      label: t("fullName"),
      value: name,
      onChange: onName,
      singleLine: true,
      disabled: loading,
      isError: !!errors.name,
      supportingText: errors.name ? t("errName") : undefined,
      leadingIcon: /*#__PURE__*/React.createElement(CmpIcon, {
        icon: "Person"
      })
    }), /*#__PURE__*/React.createElement(CmpTextField, {
      label: t("email"),
      value: email,
      onChange: onEmail,
      type: "email",
      singleLine: true,
      disabled: loading,
      isError: !!errors.email,
      supportingText: errors.email ? t("errEmail") : undefined,
      leadingIcon: /*#__PURE__*/React.createElement(CmpIcon, {
        icon: "Email"
      })
    }), /*#__PURE__*/React.createElement(CmpTextField, {
      label: t("phone"),
      value: phone,
      onChange: onPhone,
      type: "tel",
      singleLine: true,
      disabled: loading,
      isError: !!errors.phone,
      supportingText: errors.phone ? t("errPhone") : undefined,
      leadingIcon: /*#__PURE__*/React.createElement(CmpIcon, {
        icon: "Phone"
      })
    }), /*#__PURE__*/React.createElement(PasswordField, {
      t: t,
      value: password,
      onChange: onPassword,
      show: showPassword,
      setShow: onToggleShow,
      disabled: loading,
      isError: !!errors.password,
      supportingText: errors.password ? t("errPasswordShort") : undefined
    }), /*#__PURE__*/React.createElement("label", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 4,
        cursor: loading ? "default" : "pointer",
        marginLeft: -12
      }
    }, /*#__PURE__*/React.createElement(CmpCheckbox, {
      checked: installer,
      onCheckedChange: loading ? undefined : onInstaller,
      disabled: loading
    }), /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium"
    }, t("installer"))), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: "var(--space-s)"
      }
    }, /*#__PURE__*/React.createElement(SubmitButton, {
      loading: loading,
      disabled: !canSubmit,
      text: t("registerBtn"),
      onClick: onSubmit
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "center",
        paddingTop: "var(--space-xs)"
      }
    }, /*#__PURE__*/React.createElement(CmpTextButton, {
      text: t("backToLogin"),
      icon: "ArrowBack",
      onClick: onBack,
      disabled: loading
    })));
    body = /*#__PURE__*/React.createElement(AuthBody, {
      t: t,
      mode: mode
    }, form);
  }
  return /*#__PURE__*/React.createElement(ScreenChrome, {
    topBar: topBar,
    loading: loading
  }, body);
}

// ════════════════════════════════════════════════════════════════════════════
// FORGOT PASSWORD
// ════════════════════════════════════════════════════════════════════════════
function ForgotScreen(props) {
  const {
    t,
    lang,
    setLang,
    mode = "compact",
    onBack,
    email = "",
    onEmail,
    loading = false,
    error = false,
    success = false,
    canSubmit = false,
    onSubmit,
    onGoLogin
  } = props;
  const topBar = /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: t("forgotTitle"),
    style: {
      background: "transparent"
    },
    navigationIcon: /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: t("backToLogin"),
      onClick: onBack
    }),
    actions: /*#__PURE__*/React.createElement(LangSegment, {
      lang: lang,
      setLang: setLang
    })
  });
  let body;
  if (success) {
    body = /*#__PURE__*/React.createElement(AuthBody, {
      t: t,
      mode: mode
    }, /*#__PURE__*/React.createElement(SuccessPanel, {
      icon: "Email",
      title: t("forgotSuccessTitle"),
      text: t("forgotSuccessText"),
      primary: /*#__PURE__*/React.createElement(CmpButton, {
        fullWidth: true,
        text: t("goToLogin"),
        onClick: onGoLogin,
        style: BIG_BTN
      })
    }));
  } else {
    const form = /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium",
      color: "var(--on-surface-variant)"
    }, t("forgotSubtitle")), error && /*#__PURE__*/React.createElement(ErrorBanner, {
      text: t("forgotError")
    }), /*#__PURE__*/React.createElement(CmpTextField, {
      label: t("email"),
      value: email,
      onChange: onEmail,
      type: "email",
      singleLine: true,
      disabled: loading,
      isError: error,
      leadingIcon: /*#__PURE__*/React.createElement(CmpIcon, {
        icon: "Email"
      })
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: "var(--space-s)"
      }
    }, /*#__PURE__*/React.createElement(SubmitButton, {
      loading: loading,
      disabled: !canSubmit,
      text: t("restoreBtn"),
      onClick: onSubmit
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "center",
        paddingTop: "var(--space-xs)"
      }
    }, /*#__PURE__*/React.createElement(CmpTextButton, {
      text: t("backToLogin"),
      icon: "ArrowBack",
      onClick: onBack,
      disabled: loading
    })));
    body = /*#__PURE__*/React.createElement(AuthBody, {
      t: t,
      mode: mode
    }, form);
  }
  return /*#__PURE__*/React.createElement(ScreenChrome, {
    topBar: topBar,
    loading: loading
  }, body);
}

// ════════════════════════════════════════════════════════════════════════════
// LOGGED-IN PLACEHOLDER
// ════════════════════════════════════════════════════════════════════════════
function LoggedInScreen(props) {
  const {
    t,
    lang,
    setLang,
    mode = "compact",
    loading = false,
    error = false,
    onLogout
  } = props;
  const topBar = /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: t("appName"),
    style: {
      background: "transparent"
    },
    actions: /*#__PURE__*/React.createElement(LangSegment, {
      lang: lang,
      setLang: setLang
    })
  });
  const content = /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      textAlign: "center",
      gap: "var(--space-l)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 72,
      height: 72,
      borderRadius: 999,
      background: "var(--primary-container)",
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "CheckCircle",
    size: 40,
    color: "var(--on-primary-container)"
  })), /*#__PURE__*/React.createElement(CmpText, {
    variant: "headlineSmall"
  }, t("loggedInTitle")), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, t("loggedInText")), error && /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%"
    }
  }, /*#__PURE__*/React.createElement(ErrorBanner, {
    text: t("logoutError")
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%",
      paddingTop: "var(--space-s)"
    }
  }, loading ? /*#__PURE__*/React.createElement(CmpOutlinedButton, {
    fullWidth: true,
    onClick: undefined,
    style: {
      ...BIG_BTN,
      cursor: "default",
      pointerEvents: "none"
    }
  }, /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
    size: 24,
    strokeWidth: 3,
    color: "var(--primary)"
  })) : /*#__PURE__*/React.createElement(CmpOutlinedButton, {
    fullWidth: true,
    text: t("logout"),
    icon: "ExitToApp",
    onClick: onLogout,
    style: BIG_BTN
  })));
  const wrap = /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "auto",
      display: "flex",
      padding: mode === "compact" ? "24px 24px 32px" : 40,
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement(CmpCard, {
    className: "auth-anim",
    elevation: 2,
    border: true,
    style: {
      boxSizing: "border-box",
      margin: "auto",
      minWidth: 0,
      width: "100%",
      maxWidth: mode === "compact" ? 440 : 420,
      padding: 28,
      borderRadius: 28,
      animation: "authIn .5s ease both"
    }
  }, content));
  return /*#__PURE__*/React.createElement(ScreenChrome, {
    topBar: topBar,
    loading: loading
  }, wrap);
}
window.AuthScreens = {
  useAuthMode,
  LangSegment,
  LogoPlaceholder,
  PasswordField,
  AuthRootScreen,
  LoginScreen,
  RegisterScreen,
  ForgotScreen,
  LoggedInScreen
};
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/auth/AuthScreens.jsx", error: String((e && e.message) || e) }); }

// features/auth/i18n.js
try { (() => {
// Pandora Spec — Auth flow i18n dictionary (EN default/fallback, RU required).
// Plain JS module: exposes window.AuthI18N = { en: {...}, ru: {...} }.
// All user-facing auth strings live here so EN/RU can be toggled without
// clearing the form. English wording can run longer than Russian — layouts
// must hold both (see designer-requirements.md → «Языки и тексты»).
(function () {
  var STR = {
    en: {
      appName: "Pandora Spec",
      logoPlaceholder: "LOGO",
      // Auth root / session check
      rootErrorTitle: "Couldn't verify your session",
      rootErrorText: "Check your connection and try again.",
      retry: "Retry",
      // Login
      loginTitle: "Sign in",
      loginSubtitle: "Sign in to continue the installation",
      email: "Email",
      password: "Password",
      showPassword: "Show password",
      hidePassword: "Hide password",
      forgot: "Forgot password?",
      loginBtn: "Log in",
      noAccount: "Don't have an account?",
      signUp: "Sign up",
      loginError: "Wrong email or password",
      // Activation required (dialog)
      activationTitle: "Account not activated",
      activationText: "We sent an activation link to your email. Confirm it to sign in.",
      resendActivation: "Resend email",
      activationSent: "Activation email sent again",
      close: "Close",
      // Registration
      registerTitle: "Create account",
      fullName: "Full name",
      phone: "Phone number",
      installer: "I am an installer",
      registerBtn: "Create account",
      backToLogin: "Back to login",
      errEmail: "Enter a valid email",
      errPhone: "Enter a valid phone number",
      errName: "Enter your full name",
      errPasswordShort: "Password is too short",
      serverError: "Something went wrong. Please try again.",
      regSuccessTitle: "Account created",
      regSuccessText: "Your installer account is ready. Sign in to continue.",
      goToLogin: "Go to login",
      // Forgot password
      forgotTitle: "Reset password",
      forgotSubtitle: "Enter your email — we'll send recovery instructions.",
      restoreBtn: "Send instructions",
      forgotError: "Couldn't send. Check the email and try again.",
      forgotSuccessTitle: "Check your email",
      forgotSuccessText: "We sent recovery instructions to your email.",
      // Logged-in placeholder
      loggedInTitle: "You are signed in",
      loggedInText: "Your dashboard isn't ready yet — it's coming in a later version.",
      logout: "Log out",
      logoutError: "Couldn't log out. Please try again.",
      // Misc
      signedInAs: "Signed in as"
    },
    ru: {
      appName: "Pandora Spec",
      logoPlaceholder: "ЛОГО",
      rootErrorTitle: "Не удалось проверить сессию",
      rootErrorText: "Проверьте подключение и попробуйте снова.",
      retry: "Повторить",
      loginTitle: "Вход",
      loginSubtitle: "Войдите, чтобы продолжить монтаж",
      email: "Email",
      password: "Пароль",
      showPassword: "Показать пароль",
      hidePassword: "Скрыть пароль",
      forgot: "Забыли пароль?",
      loginBtn: "Войти",
      noAccount: "Нет аккаунта?",
      signUp: "Зарегистрироваться",
      loginError: "Неверный email или пароль",
      activationTitle: "Аккаунт не активирован",
      activationText: "Мы отправили ссылку активации на ваш email. Подтвердите её, чтобы войти.",
      resendActivation: "Отправить письмо повторно",
      activationSent: "Письмо активации отправлено повторно",
      close: "Закрыть",
      registerTitle: "Регистрация",
      fullName: "Имя и фамилия",
      phone: "Телефон",
      installer: "Я установщик",
      registerBtn: "Создать аккаунт",
      backToLogin: "Вернуться ко входу",
      errEmail: "Введите корректный email",
      errPhone: "Введите корректный телефон",
      errName: "Введите имя и фамилию",
      errPasswordShort: "Пароль слишком короткий",
      serverError: "Что-то пошло не так. Попробуйте снова.",
      regSuccessTitle: "Аккаунт создан",
      regSuccessText: "Аккаунт установщика готов. Войдите, чтобы продолжить.",
      goToLogin: "Перейти ко входу",
      forgotTitle: "Восстановление пароля",
      forgotSubtitle: "Введите email — пришлём инструкцию по восстановлению.",
      restoreBtn: "Отправить инструкцию",
      forgotError: "Не удалось отправить. Проверьте email и попробуйте снова.",
      forgotSuccessTitle: "Проверьте почту",
      forgotSuccessText: "Мы отправили инструкцию по восстановлению на ваш email.",
      loggedInTitle: "Вы вошли в систему",
      loggedInText: "Личный кабинет пока в разработке — появится в следующей версии.",
      logout: "Выйти",
      logoutError: "Не удалось выйти. Попробуйте снова.",
      signedInAs: "Вы вошли как"
    }
  };
  window.AuthI18N = STR;
  // Helper: t(lang) → function(key) with EN fallback.
  window.AuthT = function (lang) {
    var d = STR[lang] || STR.en;
    return function (key) {
      return d[key] != null ? d[key] : STR.en[key] != null ? STR.en[key] : key;
    };
  };
})();
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/auth/i18n.js", error: String((e && e.message) || e) }); }

// features/calculator/Calculator.jsx
try { (() => {
// CalculatorScreen — faithful recreation of feature/calculator CalculatorScreen.kt.
// Built from Pandora Spec design-system components (window namespace).
// Loaded as a Babel script by each UI kit's index.html.
//
// Адаптивность по Material 3 Window Size Classes (CLAUDE.md → «Размеры экранов»):
//   • compact — телефон-портрет: один столбец, клавиатура у нижнего края.
//   • stacked — планшет-портрет (Medium+): широкий центрированный столбец,
//     крупные клавиши и дисплей.
//   • split   — ландшафт/Desktop (Medium-ландшафт + Expanded): две колонки —
//     слева таймер+дисплей, справа клавиатура.
// Класс выбирается по реальной ширине/высоте контейнера, а не по устройству.

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar,
  CmpIconButton,
  CmpSurface,
  CmpText,
  CmpSnackbar
} = DS;
const OPS = {
  Add: {
    symbol: "+",
    fn: (a, b) => a + b
  },
  Subtract: {
    symbol: "−",
    fn: (a, b) => a - b
  },
  Multiply: {
    symbol: "×",
    fn: (a, b) => a * b
  },
  Divide: {
    symbol: "÷",
    fn: (a, b) => a / b
  }
};
function formatNumber(v) {
  if (!isFinite(v)) return "0";
  return Number.isInteger(v) ? String(v) : String(parseFloat(v.toFixed(8)));
}
function formatTimer(total) {
  const h = Math.floor(total / 3600);
  const m = Math.floor(total % 3600 / 60);
  const s = total % 60;
  const p = n => String(n).padStart(2, "0");
  return h > 0 ? `${p(h)}:${p(m)}:${p(s)}` : `${p(m)}:${p(s)}`;
}

// ── Adaptive layout ─────────────────────────────────────────────────────────
// Measures the host container and returns the active layout mode + key height.
function useLayout() {
  const ref = React.useRef(null);
  const [size, setSize] = React.useState({
    w: 0,
    h: 0
  });
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const measure = (w, h) => setSize({
      w,
      h
    });
    const remeasure = () => {
      // offsetWidth/Height — layout-размер без учёта CSS-transform родителя
      // (getBoundingClientRect отдал бы масштабированный размер в scale()-обёртке).
      measure(el.offsetWidth, el.offsetHeight);
    };
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => {
      ro.disconnect();
      window.removeEventListener("resize", remeasure);
    };
  }, []);
  const {
    w,
    h
  } = size;
  let mode = "compact";
  if (w >= 600) mode = w > h * 1.1 ? "split" : "stacked";

  // Key height: never below the 48dp touch target; scale to available height.
  let keyH = 72;
  if (mode === "stacked") keyH = Math.max(56, Math.min(88, Math.floor((h - 360) / 6)));else if (mode === "split") keyH = Math.max(48, Math.min(80, Math.floor((h - 96) / 6)));
  return [ref, mode, keyH, size];
}

// One calculator key — a clickable Surface tile, radius 16.
function CalcKey({
  label,
  color,
  contentColor,
  weight = 1,
  fontWeight = 400,
  keyH = 72,
  onClick
}) {
  const variant = keyH >= 80 ? "headlineMedium" : "headlineSmall";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: weight,
      display: "flex"
    }
  }, /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: onClick,
    color: color,
    contentColor: contentColor,
    radius: 16,
    style: {
      flex: 1,
      height: keyH,
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: variant,
    weight: fontWeight
  }, label)));
}
function TimerBtn({
  label,
  color,
  contentColor,
  height = 40,
  onClick
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      display: "flex"
    }
  }, /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: onClick,
    color: color,
    contentColor: contentColor,
    radius: 12,
    style: {
      flex: 1,
      height,
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    weight: 500
  }, label)));
}

// ── Sections (shared across layouts) ─────────────────────────────────────────
function TimerSection({
  seconds,
  running,
  setRunning,
  setSeconds,
  big
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      gap: big ? 12 : 8,
      padding: "0 24px",
      width: "100%",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-mono)",
      fontSize: big ? 48 : 36,
      fontWeight: 300,
      color: "var(--on-background)"
    }
  }, formatTimer(seconds)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8,
      width: "100%"
    }
  }, !running ? /*#__PURE__*/React.createElement(TimerBtn, {
    label: "\u0421\u0442\u0430\u0440\u0442",
    color: "var(--primary-container)",
    contentColor: "var(--on-primary-container)",
    height: big ? 52 : 40,
    onClick: () => setRunning(true)
  }) : /*#__PURE__*/React.createElement(TimerBtn, {
    label: "\u0421\u0442\u043E\u043F",
    color: "var(--secondary-container)",
    contentColor: "var(--on-secondary-container)",
    height: big ? 52 : 40,
    onClick: () => setRunning(false)
  }), /*#__PURE__*/React.createElement(TimerBtn, {
    label: "\u0421\u0431\u0440\u043E\u0441",
    color: "var(--surface-variant)",
    contentColor: "var(--on-surface-variant)",
    height: big ? 52 : 40,
    onClick: () => {
      setRunning(false);
      setSeconds(0);
    }
  })));
}
function DisplaySection({
  display,
  operation,
  firstOperand,
  big
}) {
  const long = display.length > 9;
  const size = big ? long ? 56 : 88 : long ? 40 : 64;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "flex-end",
      padding: "0 24px",
      width: "100%",
      boxSizing: "border-box"
    }
  }, operation && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-mono)",
      fontSize: big ? 20 : 16,
      color: "color-mix(in srgb, var(--on-background) 50%, transparent)"
    }
  }, firstOperand != null ? formatNumber(firstOperand) : "", " ", OPS[operation].symbol), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-mono)",
      fontSize: size,
      fontWeight: 300,
      lineHeight: 1.1,
      maxWidth: "100%",
      overflow: "hidden",
      textOverflow: "ellipsis",
      whiteSpace: "nowrap"
    }
  }, display));
}
function Keypad({
  keyH,
  onClear,
  onDelete,
  onOperation,
  onDigit,
  onEqual,
  onDecimal
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8,
      padding: "0 12px",
      width: "100%",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(CalcKey, {
    label: "C",
    keyH: keyH,
    color: "var(--surface-variant)",
    contentColor: "var(--on-surface-variant)",
    onClick: onClear
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: "\u232B",
    keyH: keyH,
    color: "var(--surface-variant)",
    contentColor: "var(--on-surface-variant)",
    onClick: onDelete
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: "\xF7",
    keyH: keyH,
    color: "var(--secondary-container)",
    contentColor: "var(--on-secondary-container)",
    onClick: () => onOperation("Divide")
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: "\xD7",
    keyH: keyH,
    color: "var(--secondary-container)",
    contentColor: "var(--on-secondary-container)",
    onClick: () => onOperation("Multiply")
  })), [["7", "8", "9", "Subtract", "−"], ["4", "5", "6", "Add", "+"]].map((row, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      display: "flex",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(CalcKey, {
    label: row[0],
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: () => onDigit(row[0])
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: row[1],
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: () => onDigit(row[1])
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: row[2],
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: () => onDigit(row[2])
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: row[4],
    keyH: keyH,
    color: "var(--secondary-container)",
    contentColor: "var(--on-secondary-container)",
    onClick: () => onOperation(row[3])
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(CalcKey, {
    label: "1",
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: () => onDigit("1")
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: "2",
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: () => onDigit("2")
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: "3",
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: () => onDigit("3")
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: "=",
    keyH: keyH,
    color: "var(--primary-container)",
    contentColor: "var(--on-primary-container)",
    onClick: onEqual
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(CalcKey, {
    label: "0",
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    weight: 2,
    onClick: () => onDigit("0")
  }), /*#__PURE__*/React.createElement(CalcKey, {
    label: ".",
    keyH: keyH,
    color: "var(--surface)",
    contentColor: "var(--on-surface)",
    onClick: onDecimal
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  })));
}
function CalculatorScreen({
  onBack = () => {},
  showBack = true
}) {
  const {
    useState,
    useEffect,
    useRef
  } = React;
  const [rootRef, mode, keyH] = useLayout();

  // ── calculator state ──
  const [display, setDisplay] = useState("0");
  const [firstOperand, setFirstOperand] = useState(null);
  const [operation, setOperation] = useState(null);
  const [overwrite, setOverwrite] = useState(true);
  const [snack, setSnack] = useState(false);

  // ── timer state ──
  const [seconds, setSeconds] = useState(0);
  const [running, setRunning] = useState(false);
  const timer = useRef(null);
  useEffect(() => {
    if (running) {
      timer.current = setInterval(() => setSeconds(s => s + 1), 1000);
      return () => clearInterval(timer.current);
    }
  }, [running]);
  const showSnack = () => {
    setSnack(true);
    setTimeout(() => setSnack(false), 3000);
  };
  const onDigit = d => {
    setDisplay(cur => {
      if (overwrite) {
        setOverwrite(false);
        return d;
      }
      return cur === "0" ? d : cur.length < 12 ? cur + d : cur;
    });
  };
  const onDecimal = () => {
    setDisplay(cur => {
      if (overwrite) {
        setOverwrite(false);
        return "0.";
      }
      return cur.includes(".") ? cur : cur + ".";
    });
  };
  const onClear = () => {
    setDisplay("0");
    setFirstOperand(null);
    setOperation(null);
    setOverwrite(true);
  };
  const onDelete = () => {
    setDisplay(cur => {
      if (overwrite) return cur;
      const next = cur.slice(0, -1);
      return next === "" || next === "-" ? "0" : next;
    });
  };
  const onOperation = opKey => {
    const cur = parseFloat(display);
    if (firstOperand != null && operation && !overwrite) {
      const r = OPS[operation].fn(firstOperand, cur);
      if (operation === "Divide" && cur === 0) {
        showSnack();
        onClear();
        return;
      }
      setFirstOperand(r);
      setDisplay(formatNumber(r));
    } else {
      setFirstOperand(cur);
    }
    setOperation(opKey);
    setOverwrite(true);
  };
  const onEqual = () => {
    if (firstOperand == null || !operation) return;
    const cur = parseFloat(display);
    if (operation === "Divide" && cur === 0) {
      showSnack();
      onClear();
      return;
    }
    const r = OPS[operation].fn(firstOperand, cur);
    setDisplay(formatNumber(r));
    setFirstOperand(null);
    setOperation(null);
    setOverwrite(true);
  };
  const keypadProps = {
    keyH,
    onClear,
    onDelete,
    onOperation,
    onDigit,
    onEqual,
    onDecimal
  };
  const big = mode !== "compact";
  const timerSection = /*#__PURE__*/React.createElement(TimerSection, {
    seconds: seconds,
    running: running,
    setRunning: setRunning,
    setSeconds: setSeconds,
    big: big
  });
  const displaySection = /*#__PURE__*/React.createElement(DisplaySection, {
    display: display,
    operation: operation,
    firstOperand: firstOperand,
    big: big
  });
  const keypad = /*#__PURE__*/React.createElement(Keypad, keypadProps);
  let layout;
  if (mode === "split") {
    // Ландшафт / Desktop — две колонки, контент центрирован с max-width.
    layout = /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        display: "flex",
        alignItems: "stretch",
        gap: 24,
        padding: 24,
        boxSizing: "border-box",
        maxWidth: 1040,
        width: "100%",
        margin: "0 auto"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "1 1 0",
        minWidth: 260,
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        gap: 28
      }
    }, timerSection, displaySection), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "1 1 0",
        maxWidth: 520,
        display: "flex",
        flexDirection: "column",
        justifyContent: "center"
      }
    }, keypad));
  } else if (mode === "stacked") {
    // Планшет-портрет — широкий центрированный столбец, крупные элементы.
    layout = /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        display: "flex",
        flexDirection: "column",
        justifyContent: "flex-end",
        gap: 20,
        maxWidth: 600,
        width: "100%",
        margin: "0 auto",
        padding: "0 0 16px",
        boxSizing: "border-box"
      }
    }, timerSection, displaySection, keypad);
  } else {
    // Телефон-портрет — исходная компактная раскладка.
    layout = /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        justifyContent: "flex-end",
        gap: 16,
        paddingBottom: 16
      }
    }, timerSection, displaySection, keypad);
  }
  return /*#__PURE__*/React.createElement("div", {
    ref: rootRef,
    style: {
      position: "relative",
      height: "100%",
      display: "flex",
      flexDirection: "column",
      background: "var(--background)",
      color: "var(--on-background)"
    }
  }, /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: "\u041A\u0430\u043B\u044C\u043A\u0443\u043B\u044F\u0442\u043E\u0440",
    navigationIcon: showBack ? /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: "\u041D\u0430\u0437\u0430\u0434",
      onClick: onBack
    }) : undefined
  }), layout, snack && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 16,
      right: 16,
      bottom: 16,
      display: "flex",
      justifyContent: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpSnackbar, {
    message: "\u0414\u0435\u043B\u0435\u043D\u0438\u0435 \u043D\u0430 \u043D\u043E\u043B\u044C \u043D\u0435\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u043E",
    actionLabel: "OK",
    onActionClick: () => setSnack(false)
  })));
}
Object.assign(window, {
  CalculatorScreen
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/calculator/Calculator.jsx", error: String((e && e.message) || e) }); }

// res/icons/cmp-icons.js
try { (() => {
// Pandora Spec — icon catalog (CmpIcons).
// Maps catalog names → Material Icons (Filled) ligatures, mirroring
// core/ui/.../icon/CmpIconsResolver.kt. The same map ships inside the
// bundled <CmpIcon> component; this standalone module is for prototypes
// that render icons without the design-system bundle.
//
// Usage in a plain prototype:
//   <link rel="stylesheet" href=".../tokens/fonts.css">  (loads Material Icons)
//   <script src=".../res/icons/cmp-icons.js"></script>
//   <span class="material-icons">{window.CmpIconLigature('ArrowBack')}</span>
(function () {
  var CMP_ICONS = {
    // Navigation
    ArrowBack: 'arrow_back',
    ArrowForward: 'arrow_forward',
    ArrowDropDown: 'arrow_drop_down',
    ChevronLeft: 'keyboard_arrow_left',
    ChevronRight: 'keyboard_arrow_right',
    KeyboardArrowDown: 'keyboard_arrow_down',
    KeyboardArrowUp: 'keyboard_arrow_up',
    Menu: 'menu',
    MoreVert: 'more_vert',
    Close: 'close',
    Home: 'home',
    // Actions
    Add: 'add',
    Remove: 'clear',
    Edit: 'edit',
    Create: 'create',
    Delete: 'delete',
    Search: 'search',
    Refresh: 'refresh',
    Share: 'share',
    Send: 'send',
    Done: 'done',
    Check: 'check',
    CheckCircle: 'check_circle',
    Clear: 'clear',
    PlayArrow: 'play_arrow',
    Pause: 'pause',
    Build: 'build',
    // Content
    Favorite: 'favorite',
    FavoriteBorder: 'favorite_border',
    Star: 'star',
    ThumbUp: 'thumb_up',
    List: 'list',
    Tag: 'sell',
    // Communication
    Email: 'email',
    Phone: 'phone',
    Call: 'call',
    Notifications: 'notifications',
    Mic: 'mic',
    // User
    Person: 'person',
    AccountCircle: 'account_circle',
    Face: 'face',
    // Status
    Info: 'info',
    Warning: 'warning',
    AlertTriangle: 'warning',
    AlertCircle: 'error',
    Lock: 'lock',
    Visibility: 'visibility',
    VisibilityOff: 'visibility_off',
    // Places
    LocationOn: 'location_on',
    Place: 'place',
    // Other
    Settings: 'settings',
    DateRange: 'date_range',
    Schedule: 'schedule',
    ShoppingCart: 'shopping_cart',
    ExitToApp: 'exit_to_app',
    DarkMode: 'dark_mode',
    LightMode: 'light_mode',
    NightsStay: 'nights_stay',
    Lightbulb: 'lightbulb',
    Moon: 'nights_stay',
    Sun: 'wb_sunny'
  };
  window.CMP_ICONS = CMP_ICONS;
  window.CmpIconLigature = function (name) {
    return CMP_ICONS[name] || name;
  };
})();
})(); } catch (e) { __ds_ns.__errors.push({ path: "res/icons/cmp-icons.js", error: String((e && e.message) || e) }); }

// res/nav.js
try { (() => {
// Pandora Spec — cross-mockup navigation.
// Lets one standalone layout link to another regardless of folder depth.
// Usage: window.psGo('calculator')  → opens that screen in the same tab.
(function () {
  // Find this script reliably (document.currentScript is unreliable in the
  // preview environment — it can point at a later-evaluated script).
  var me = document.currentScript;
  if (!me || !/res\/nav\.js/.test(me.src || '')) {
    me = [].slice.call(document.scripts).filter(function (s) {
      return /res\/nav\.js(?:\?|$)/.test(s.src || '');
    }).pop() || me;
  }
  // Project root = location of this file minus "res/nav.js"
  var ROOT = me && me.src ? me.src.replace(/res\/nav\.js(?:\?.*)?$/, '') : new URL('.', location.href).href;

  // Section key → file, relative to project root.
  var LINKS = {
    home: 'index.html',
    calculator: 'features/calculator/Calculator.html',
    'calculator-tablet': 'features/calculator/CalculatorTablet.html',
    'calculator-desktop': 'features/calculator/CalculatorDesktop.html',
    auth: 'features/auth/Auth.html',
    'auth-tablet': 'features/auth/AuthTablet.html',
    'auth-desktop': 'features/auth/AuthDesktop.html',
    'auth-states': 'features/auth/AuthStates.html',
    'auth-flow': 'features/auth/AuthFlow.html'
  };
  window.PS_ROOT = ROOT;
  window.PS_LINKS = LINKS;
  window.psHas = function (key) {
    return !!LINKS[key];
  };
  window.psGo = function (key) {
    var p = LINKS[key];
    if (p) location.href = ROOT + p;
  };
})();
})(); } catch (e) { __ds_ns.__errors.push({ path: "res/nav.js", error: String((e && e.message) || e) }); }

__ds_ns.CMP_ICONS = __ds_scope.CMP_ICONS;

__ds_ns.CmpIcon = __ds_scope.CmpIcon;

__ds_ns.CmpText = __ds_scope.CmpText;

__ds_ns.CmpButton = __ds_scope.CmpButton;

__ds_ns.CmpFloatingActionButton = __ds_scope.CmpFloatingActionButton;

__ds_ns.CmpIconButton = __ds_scope.CmpIconButton;

__ds_ns.CmpOutlinedButton = __ds_scope.CmpOutlinedButton;

__ds_ns.CmpTextButton = __ds_scope.CmpTextButton;

__ds_ns.CmpBadge = __ds_scope.CmpBadge;

__ds_ns.CmpBadgedBox = __ds_scope.CmpBadgedBox;

__ds_ns.CmpBottomSheet = __ds_scope.CmpBottomSheet;

__ds_ns.CmpDialog = __ds_scope.CmpDialog;

__ds_ns.CmpSnackbar = __ds_scope.CmpSnackbar;

__ds_ns.CmpCheckbox = __ds_scope.CmpCheckbox;

__ds_ns.CmpDropdown = __ds_scope.CmpDropdown;

__ds_ns.CmpRadioButton = __ds_scope.CmpRadioButton;

__ds_ns.CmpSlider = __ds_scope.CmpSlider;

__ds_ns.CmpSwitch = __ds_scope.CmpSwitch;

__ds_ns.CmpTextField = __ds_scope.CmpTextField;

__ds_ns.CmpNavigationBar = __ds_scope.CmpNavigationBar;

__ds_ns.CmpTabRow = __ds_scope.CmpTabRow;

__ds_ns.CmpTopAppBar = __ds_scope.CmpTopAppBar;

__ds_ns.CmpCircularProgressIndicator = __ds_scope.CmpCircularProgressIndicator;

__ds_ns.CmpLinearProgressIndicator = __ds_scope.CmpLinearProgressIndicator;

__ds_ns.CmpCard = __ds_scope.CmpCard;

__ds_ns.CmpDivider = __ds_scope.CmpDivider;

__ds_ns.CmpSelectableCard = __ds_scope.CmpSelectableCard;

__ds_ns.CmpSurface = __ds_scope.CmpSurface;

})();
