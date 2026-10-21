/* @ds-bundle: {"format":4,"namespace":"PandoraSpecDesignSystem_b8d6e1","components":[{"name":"CMP_ICONS","sourcePath":"components/app-kit/CmpIcon.jsx"},{"name":"CmpIcon","sourcePath":"components/app-kit/CmpIcon.jsx"},{"name":"CmpText","sourcePath":"components/app-kit/CmpText.jsx"},{"name":"CmpButton","sourcePath":"components/buttons/CmpButton.jsx"},{"name":"CmpFloatingActionButton","sourcePath":"components/buttons/CmpFloatingActionButton.jsx"},{"name":"CmpIconButton","sourcePath":"components/buttons/CmpIconButton.jsx"},{"name":"CmpOutlinedButton","sourcePath":"components/buttons/CmpOutlinedButton.jsx"},{"name":"CmpTextButton","sourcePath":"components/buttons/CmpTextButton.jsx"},{"name":"CmpBadge","sourcePath":"components/feedback/CmpBadge.jsx"},{"name":"CmpBadgedBox","sourcePath":"components/feedback/CmpBadge.jsx"},{"name":"CmpBottomSheet","sourcePath":"components/feedback/CmpBottomSheet.jsx"},{"name":"CmpDialog","sourcePath":"components/feedback/CmpDialog.jsx"},{"name":"CmpSnackbar","sourcePath":"components/feedback/CmpSnackbar.jsx"},{"name":"CmpCheckbox","sourcePath":"components/forms/CmpCheckbox.jsx"},{"name":"CmpDropdown","sourcePath":"components/forms/CmpDropdown.jsx"},{"name":"CmpRadioButton","sourcePath":"components/forms/CmpRadioButton.jsx"},{"name":"CmpSlider","sourcePath":"components/forms/CmpSlider.jsx"},{"name":"CmpSwitch","sourcePath":"components/forms/CmpSwitch.jsx"},{"name":"CmpTextField","sourcePath":"components/forms/CmpTextField.jsx"},{"name":"CmpNavigationBar","sourcePath":"components/navigation/CmpNavigationBar.jsx"},{"name":"CmpTabRow","sourcePath":"components/navigation/CmpTabRow.jsx"},{"name":"CmpTopAppBar","sourcePath":"components/navigation/CmpTopAppBar.jsx"},{"name":"CmpCircularProgressIndicator","sourcePath":"components/progress/CmpCircularProgressIndicator.jsx"},{"name":"CmpLinearProgressIndicator","sourcePath":"components/progress/CmpLinearProgressIndicator.jsx"},{"name":"CmpCard","sourcePath":"components/surfaces/CmpCard.jsx"},{"name":"CmpDivider","sourcePath":"components/surfaces/CmpDivider.jsx"},{"name":"CmpSelectableCard","sourcePath":"components/surfaces/CmpSelectableCard.jsx"},{"name":"CmpSurface","sourcePath":"components/surfaces/CmpSurface.jsx"}],"sourceHashes":{"components/app-kit/CmpIcon.jsx":"b3d94cf9b1e3","components/app-kit/CmpText.jsx":"cdee16d965f8","components/app-kit/PhoneFrame.jsx":"a9d47a57ac31","components/app-kit/useStateLayer.js":"2a1599d8c7c6","components/buttons/CmpButton.jsx":"5e4b9cff4fc3","components/buttons/CmpFloatingActionButton.jsx":"1158dcb8c4da","components/buttons/CmpIconButton.jsx":"6a7318af31c1","components/buttons/CmpOutlinedButton.jsx":"c1e9ba8c6875","components/buttons/CmpTextButton.jsx":"5ee3f901782e","components/feedback/CmpBadge.jsx":"547d9173e5bf","components/feedback/CmpBottomSheet.jsx":"54a97e4fb43b","components/feedback/CmpDialog.jsx":"2a1414da342e","components/feedback/CmpSnackbar.jsx":"fd293f936c21","components/forms/CmpCheckbox.jsx":"14129294474d","components/forms/CmpDropdown.jsx":"b4b6ca649867","components/forms/CmpRadioButton.jsx":"2f75844572ce","components/forms/CmpSlider.jsx":"1a752d0a3873","components/forms/CmpSwitch.jsx":"d19f299e5238","components/forms/CmpTextField.jsx":"fc85734a1a03","components/navigation/CmpNavigationBar.jsx":"24fe17d57b50","components/navigation/CmpTabRow.jsx":"76164e7ddc16","components/navigation/CmpTopAppBar.jsx":"2d8ea5fb2126","components/progress/CmpCircularProgressIndicator.jsx":"0d6435223dd7","components/progress/CmpLinearProgressIndicator.jsx":"ff47898fb86d","components/surfaces/CmpCard.jsx":"49545cb747b5","components/surfaces/CmpDivider.jsx":"a3a8fe9c2772","components/surfaces/CmpSelectableCard.jsx":"cc5403070197","components/surfaces/CmpSurface.jsx":"f8824849e0ac","features/app-update/AppUpdateScreen.jsx":"e61af0c29b73","features/auth/Auth.jsx":"6da450accbe9","features/auth/AuthScreens.jsx":"4986c79933ee","features/auth/i18n.js":"9e4ecc677e9e","features/calculator/Calculator.jsx":"778bbbf9b839","features/device-select/DeviceSelect.jsx":"d895b94043f0","features/home/Home.jsx":"b74520b8e19e","features/home/HomeScreens.jsx":"e06278dd1ea2","features/support-chat/SupportChat.jsx":"119d6686fd2c","features/support-chat/SupportChatScreens.jsx":"e8b12bf0ce32","res/icons/cmp-icons.js":"8c00925c86ca","res/icons/main-icons.js":"a5ee0c0aad77","res/nav.js":"2fe085bef38a"},"inlinedExternals":[],"unexposedExports":[{"name":"useStateLayer","sourcePath":"components/app-kit/useStateLayer.js"}]} */

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

// features/app-update/AppUpdateScreen.jsx
try { (() => {
// AppUpdateScreen — Pandora Spec desktop app-version update (download a newer
// build, e.g. .dmg). Built ONLY from design-system Cmp* components.
//
//   • UpdateScreen — stateless BODY, rendered per `state`. Chrome (top bar) is
//     supplied by the host, so it embeds inside the main screen (route=update)
//     and inside this feature's own AppUpdate.html the same way.
//   • UpdateFlow   — stateful controller: checking → available → downloading
//     (determinate progress) → downloaded → (install) up-to-date. Renders the
//     body only. Used by the main screen's update route and by AppUpdateApp.
//   • AppUpdateApp — full standalone screen (top bar + UpdateFlow), or pinned to
//     one explicit state for the states gallery.

const AU_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar,
  CmpIconButton,
  CmpText,
  CmpIcon,
  CmpSurface,
  CmpDivider,
  CmpButton,
  CmpTextButton,
  CmpCircularProgressIndicator
} = AU_DS;
const UPDATE_STATES = ["available", "checking", "downloading", "downloaded", "uptodate", "error"];
const AU_ICON = {
  appUpdate: "system_update",
  download: "download"
};
const DEFAULT_NOTES = ["Ускорена запись прошивок и синхронизация базы", "Новые поддерживаемые автомобили и точки подключения", "Исправления стабильности при работе по CAN/LIN"];

// ── Container-width hook → padding / max-width class ─────────────────────────
function useAuMode() {
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
  if (w >= 1200) mode = "large";else if (w >= 840) mode = "expanded";else if (w >= 600) mode = "medium";
  return [ref, mode];
}
function VerPill({
  label,
  value,
  accent = false
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 2,
      padding: "10px 16px",
      borderRadius: 12,
      minWidth: 120,
      background: accent ? "var(--primary-container)" : "var(--surface-variant)",
      color: accent ? "var(--on-primary-container)" : "var(--on-surface-variant)"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "currentColor"
  }, label), /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium",
    color: "currentColor"
  }, value));
}
const HEAD = {
  available: {
    icon: AU_ICON.appUpdate,
    tone: "primary",
    title: "Доступна новая версия",
    sub: (c, l) => `Версия ${l} · для macOS (.dmg)`
  },
  checking: {
    icon: null,
    tone: "primary",
    title: "Проверка обновлений",
    sub: () => "Ищем новую версию…"
  },
  downloading: {
    icon: AU_ICON.download,
    tone: "primary",
    title: "Загрузка обновления",
    sub: (c, l, s) => `Версия ${l} · ${s} МБ`
  },
  downloaded: {
    icon: "CheckCircle",
    tone: "primary",
    title: "Обновление готово к установке",
    sub: (c, l) => `Версия ${l} загружена`
  },
  uptodate: {
    icon: "CheckCircle",
    tone: "success",
    title: "Установлена последняя версия",
    sub: c => `Версия ${c}`
  },
  error: {
    icon: "AlertCircle",
    tone: "error",
    title: "Не удалось проверить обновления",
    sub: () => "Проверьте подключение и повторите"
  }
};

// ── Stateless body ────────────────────────────────────────────────────────────
function UpdateScreen({
  mode = "expanded",
  state = "available",
  current = "2.4.1",
  latest = "2.5.0",
  sizeMB = 84,
  progress = 0,
  notes = DEFAULT_NOTES,
  onDownload = () => {},
  onCancel = () => {},
  onInstall = () => {},
  onLater = () => {},
  onCheck = () => {}
}) {
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const h = HEAD[state] || HEAD.available;
  const tileBg = h.tone === "error" ? "var(--error-container)" : h.tone === "success" ? "color-mix(in srgb, var(--success) 20%, transparent)" : "var(--primary-container)";
  const tileFg = h.tone === "error" ? "var(--on-error-container)" : h.tone === "success" ? "var(--success)" : "var(--on-primary-container)";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "auto"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: 640,
      margin: "0 auto",
      padding: `${pad}px`,
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement(CmpSurface, {
    border: true,
    radius: 16,
    color: "var(--surface-container-low)",
    style: {
      padding: 24,
      display: "flex",
      flexDirection: "column",
      gap: 20
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 16
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 56,
      height: 56,
      borderRadius: "var(--radius-m)",
      background: tileBg,
      color: tileFg,
      display: "grid",
      placeItems: "center",
      flex: "0 0 auto"
    }
  }, state === "checking" ? /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
    size: 28,
    strokeWidth: 3
  }) : /*#__PURE__*/React.createElement(CmpIcon, {
    icon: h.icon,
    size: 30,
    color: tileFg
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "headlineSmall"
  }, h.title), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 2
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, h.sub(current, latest, sizeMB))))), (state === "available" || state === "downloaded") && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 10,
      flexWrap: "wrap",
      alignItems: "center"
    }
  }, /*#__PURE__*/React.createElement(VerPill, {
    label: "\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u043E",
    value: current
  }), /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "ArrowForward",
    size: 20,
    color: "var(--on-surface-variant)"
  }), /*#__PURE__*/React.createElement(VerPill, {
    label: "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u043E",
    value: latest,
    accent: true
  })), state === "downloading" && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      height: 6,
      borderRadius: 999,
      background: "var(--surface-variant)",
      overflow: "hidden"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      height: "100%",
      width: `${progress}%`,
      background: "var(--primary)",
      borderRadius: 999,
      transition: "width .2s linear"
    }
  })), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--on-surface-variant)"
  }, `Загрузка… ${Math.round(progress)}% · ${Math.round(sizeMB * progress / 100)} из ${sizeMB} МБ`)), state === "available" && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpDivider, null), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium"
  }, "\u0427\u0442\u043E \u043D\u043E\u0432\u043E\u0433\u043E"), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8
    }
  }, notes.map((n, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      display: "flex",
      gap: 10,
      alignItems: "flex-start"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "Check",
    size: 18,
    color: "var(--success)",
    style: {
      marginTop: 2
    }
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)",
    style: {
      textWrap: "pretty"
    }
  }, n)))))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      flexWrap: "wrap",
      paddingTop: 4
    }
  }, state === "available" && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
    text: "\u0421\u043A\u0430\u0447\u0430\u0442\u044C \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 (.dmg)",
    icon: AU_ICON.download,
    onClick: onDownload
  }), /*#__PURE__*/React.createElement(CmpTextButton, {
    text: "\u041F\u043E\u0437\u0436\u0435",
    onClick: onLater
  })), state === "checking" && /*#__PURE__*/React.createElement(CmpButton, {
    text: "\u041F\u0440\u043E\u0432\u0435\u0440\u043A\u0430\u2026",
    disabled: true,
    onClick: () => {}
  }), state === "downloading" && /*#__PURE__*/React.createElement(CmpTextButton, {
    text: "\u041E\u0442\u043C\u0435\u043D\u0438\u0442\u044C",
    onClick: onCancel
  }), state === "downloaded" && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
    text: "\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C \u0438 \u043F\u0435\u0440\u0435\u0437\u0430\u043F\u0443\u0441\u0442\u0438\u0442\u044C",
    icon: "Refresh",
    onClick: onInstall
  }), /*#__PURE__*/React.createElement(CmpTextButton, {
    text: "\u041F\u043E\u0437\u0436\u0435",
    onClick: onLater
  })), state === "uptodate" && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
    text: "\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u0442\u044C \u0441\u043D\u043E\u0432\u0430",
    icon: "Refresh",
    onClick: onCheck
  }), /*#__PURE__*/React.createElement(CmpTextButton, {
    text: "\u0413\u043E\u0442\u043E\u0432\u043E",
    onClick: onLater
  })), state === "error" && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
    text: "\u041F\u043E\u0432\u0442\u043E\u0440\u0438\u0442\u044C",
    icon: "Refresh",
    onClick: onCheck
  }), /*#__PURE__*/React.createElement(CmpTextButton, {
    text: "\u041F\u043E\u0437\u0436\u0435",
    onClick: onLater
  }))))));
}

// ── Stateful body: full download flow ────────────────────────────────────────
function UpdateFlow({
  mode = "expanded",
  initialState = "available",
  current = "2.4.1",
  latest = "2.5.0",
  sizeMB = 84,
  onLater = () => {}
}) {
  const {
    useState,
    useRef,
    useEffect
  } = React;
  const [state, setState] = useState(initialState);
  const [progress, setProgress] = useState(0);
  const t = useRef(null);
  const iv = useRef(null);
  useEffect(() => () => {
    clearTimeout(t.current);
    clearInterval(iv.current);
  }, []);
  const check = () => {
    setState("checking");
    clearTimeout(t.current);
    t.current = setTimeout(() => setState("available"), 1300);
  };
  const download = () => {
    setState("downloading");
    setProgress(0);
    clearInterval(iv.current);
    iv.current = setInterval(() => {
      setProgress(p => {
        const next = p + 100 / 22; // ~2.2s
        if (next >= 100) {
          clearInterval(iv.current);
          setState("downloaded");
          return 100;
        }
        return next;
      });
    }, 100);
  };
  const cancel = () => {
    clearInterval(iv.current);
    setProgress(0);
    setState("available");
  };
  const install = () => {
    setState("checking");
    clearTimeout(t.current);
    t.current = setTimeout(() => setState("uptodate"), 1200);
  };
  return /*#__PURE__*/React.createElement(UpdateScreen, {
    mode: mode,
    state: state,
    current: current,
    latest: latest,
    sizeMB: sizeMB,
    progress: progress,
    onDownload: download,
    onCancel: cancel,
    onInstall: install,
    onCheck: check,
    onLater: onLater
  });
}

// ── Full standalone screen (top bar + flow), or pinned to one state ──────────
function AppUpdateApp({
  pinnedState,
  current = "2.4.1",
  latest = "2.5.0",
  sizeMB = 84,
  onBack,
  showBack = false
}) {
  const [rootRef, mode] = useAuMode();
  return /*#__PURE__*/React.createElement("div", {
    ref: rootRef,
    style: {
      position: "relative",
      height: "100%",
      width: "100%",
      display: "flex",
      flexDirection: "column",
      overflow: "hidden",
      background: "var(--background)",
      color: "var(--on-background)"
    }
  }, /*#__PURE__*/React.createElement("style", null, `.au-focusable:focus-visible{outline:2px solid var(--primary);outline-offset:2px}`), /*#__PURE__*/React.createElement(CmpTopAppBar, {
    centered: true,
    title: "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u044F",
    style: {
      flex: "0 0 auto"
    },
    navigationIcon: showBack ? /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: "\u041D\u0430\u0437\u0430\u0434",
      onClick: onBack
    }) : undefined
  }), pinnedState ? /*#__PURE__*/React.createElement(UpdateScreen, {
    mode: mode,
    state: pinnedState,
    current: current,
    latest: latest,
    sizeMB: sizeMB,
    progress: pinnedState === "downloading" ? 46 : 0
  }) : /*#__PURE__*/React.createElement(UpdateFlow, {
    mode: mode,
    current: current,
    latest: latest,
    sizeMB: sizeMB,
    onLater: () => {}
  }));
}
window.AppUpdate = {
  UpdateScreen,
  UpdateFlow,
  AppUpdateApp,
  VerPill,
  useAuMode,
  UPDATE_STATES
};
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/app-update/AppUpdateScreen.jsx", error: String((e && e.message) || e) }); }

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
  initialScreen = "login",
  onLoggedIn = null
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
      } else if (onLoggedIn) onLoggedIn(); // app entry: hand off to the main feature
      else go("loggedIn");
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

// features/device-select/DeviceSelect.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// DeviceSelectScreen — поиск и выбор устройства перед монтажом.
// Точка входа: после «Быстрый монтаж» / «Расширенный монтаж» на главном.
// Собран только из компонентов дизайн-системы (window namespace).
//
// Поток (state machine):
//   permission → (Разрешить) → scanning → list | empty
//   permission → (Отклонить) → denied
//   adapterOff → (Включить) → scanning
//   list → (выбор) → connecting        (compact/stacked)
//   list → (выбор) → detail → connecting (split/master-detail)
//
// Разрешения зависят от платформы:
//   android — BLE + USB;  ios — только BLE;  desktop — без запроса (сразу scanning).
//
// Адаптив по Material 3 Window Size Classes (CLAUDE.md → «Размеры экранов»):
//   compact (<600) — телефон: один столбец, строки на всю ширину.
//   stacked (600–839 портрет) — планшет-портрет: центрированный столбец, крупнее.
//   split (Expanded/ландшафт/Desktop) — master-detail: слева список, справа карточка+«Подключить».
//   Класс выбирается по реальной ширине контейнера, а не по типу устройства.

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar,
  CmpIconButton,
  CmpButton,
  CmpOutlinedButton,
  CmpSurface,
  CmpText,
  CmpIcon,
  CmpDivider,
  CmpCircularProgressIndicator
} = DS;

// ── Демо-данные найденных устройств (BLE + USB-HID) ──────────────────────────
const DEVICES = [{
  id: "dx4gs",
  name: "Pandora DX-4GS",
  transport: "ble",
  address: "AC:23:3F:A1:0C:7D",
  rssi: -48,
  type: "Основной блок",
  icon: "sensors"
}, {
  id: "dxl5000",
  name: "Pandora DXL-5000",
  transport: "ble",
  address: "C8:FD:19:22:8B:04",
  rssi: -67,
  type: "Основной блок",
  icon: "sensors"
}, {
  id: "rmd5",
  name: "Pandora RMD-5",
  transport: "usb",
  address: "HID · 0483:5750",
  rssi: null,
  type: "Модуль обхода",
  icon: "memory"
}, {
  id: "d032",
  name: "Pandora D-032",
  transport: "ble",
  address: "7A:11:0D:5E:C3:90",
  rssi: -75,
  type: "Метка",
  icon: "vpn_key"
}, {
  id: "vx4g",
  name: "Pandora VX-4G",
  transport: "ble",
  address: "3C:2E:FF:90:11:AA",
  rssi: -83,
  type: "Основной блок",
  icon: "sensors"
}];
const TRANSPORT = {
  ble: {
    icon: "bluetooth",
    label: "Bluetooth"
  },
  usb: {
    icon: "usb",
    label: "USB"
  }
};

// Инъекция анимаций один раз (радар / появление).
(function injectStyles() {
  if (document.getElementById("ds-device-select-anim")) return;
  const s = document.createElement("style");
  s.id = "ds-device-select-anim";
  s.textContent = "@keyframes dsPing{0%{transform:scale(.65);opacity:.55}80%{transform:scale(1.9);opacity:0}100%{opacity:0}}" + "@keyframes dsRowIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}" + "@media (prefers-reduced-motion: reduce){.ds-ping,.ds-row{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Измерение контейнера → активный класс окна ───────────────────────────────
function useLayout() {
  const ref = React.useRef(null);
  const [size, setSize] = React.useState({
    w: 0,
    h: 0
  });
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const remeasure = () => setSize({
      w: el.offsetWidth,
      h: el.offsetHeight
    });
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
  return [ref, mode, size];
}

// ── Мелкие строительные блоки ────────────────────────────────────────────────

// Плейсхолдер-слот фото устройства (пользователь заменит реальным снимком).
function DeviceTile({
  icon,
  size = 56,
  radius = 12,
  showCaption = false
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      width: size,
      height: size,
      borderRadius: radius,
      flex: "0 0 auto",
      position: "relative",
      overflow: "hidden",
      display: "grid",
      placeItems: "center",
      border: "1px solid var(--outline-variant)",
      background: "repeating-linear-gradient(135deg, var(--surface-variant) 0 6px, color-mix(in srgb, var(--surface-variant) 55%, var(--surface)) 6px 12px)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: Math.round(size * 0.42),
    color: "color-mix(in srgb, var(--on-surface-variant) 78%, transparent)"
  }), showCaption && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      left: 0,
      right: 0,
      bottom: 0,
      padding: "2px 0",
      fontFamily: "var(--font-mono)",
      fontSize: 9,
      letterSpacing: .3,
      textAlign: "center",
      color: "var(--on-surface-variant)",
      background: "color-mix(in srgb, var(--surface) 70%, transparent)"
    }
  }, "\u0444\u043E\u0442\u043E \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430"));
}

// Уровень сигнала BLE — три столбика (простые прямоугольники).
function SignalBars({
  rssi
}) {
  const level = rssi >= -55 ? 3 : rssi >= -72 ? 2 : 1;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "flex-end",
      gap: 2,
      height: 16
    },
    title: `${rssi} dBm`
  }, [0, 1, 2].map(i => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      width: 4,
      height: 6 + i * 5,
      borderRadius: 1,
      background: i < level ? "var(--primary)" : "color-mix(in srgb, var(--on-surface-variant) 28%, transparent)"
    }
  })));
}

// Чип транспорта (Bluetooth / USB).
function TransportChip({
  transport
}) {
  const t = TRANSPORT[transport];
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: 4,
      height: 22,
      padding: "0 8px",
      borderRadius: 999,
      background: "var(--secondary-container)",
      color: "var(--on-secondary-container)",
      fontFamily: "var(--font-sans)",
      fontSize: 12,
      fontWeight: 500,
      flex: "0 0 auto"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: t.icon,
    size: 14
  }), t.label);
}

// Строка устройства в списке.
function DeviceRow({
  device,
  selected,
  onClick,
  tileSize = 56
}) {
  const {
    hover,
    focus,
    active,
    opacity,
    handlers
  } = DS.useStateLayer ? DS.useStateLayer() : {
    opacity: 0,
    handlers: {}
  };
  return /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: onClick,
    color: selected ? "var(--primary-container)" : "var(--surface-container-low)",
    contentColor: selected ? "var(--on-primary-container)" : "var(--on-surface)",
    radius: 16,
    border: !selected,
    style: {
      boxSizing: "border-box",
      position: "relative",
      overflow: "hidden",
      cursor: "pointer"
    }
  }, /*#__PURE__*/React.createElement("div", _extends({}, handlers, {
    role: "button",
    tabIndex: 0,
    onKeyDown: e => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        onClick && onClick();
      }
    },
    style: {
      display: "flex",
      alignItems: "center",
      gap: 14,
      padding: "12px 14px",
      minHeight: 72,
      boxSizing: "border-box"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--on-surface)",
      opacity,
      pointerEvents: "none"
    }
  }), /*#__PURE__*/React.createElement(DeviceTile, {
    icon: device.icon,
    size: tileSize
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      position: "relative"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium",
    maxLines: 1
  }, device.name), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      marginTop: 4,
      flexWrap: "wrap"
    }
  }, /*#__PURE__*/React.createElement(TransportChip, {
    transport: device.transport
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-mono)",
      fontSize: 12,
      color: selected ? "inherit" : "var(--on-surface-variant)"
    }
  }, device.address))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "flex-end",
      gap: 6,
      position: "relative",
      flex: "0 0 auto"
    }
  }, device.transport === "ble" ? /*#__PURE__*/React.createElement(SignalBars, {
    rssi: device.rssi
  }) : /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "usb",
    size: 18,
    color: "var(--on-surface-variant)"
  }), /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "chevron_right",
    size: 22,
    color: selected ? "inherit" : "var(--on-surface-variant)"
  }))));
}

// Круглый герой-значок для состояний.
function Hero({
  icon,
  tone = "primary",
  radar = false
}) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "neutral" ? "var(--surface-variant)" : "var(--primary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "neutral" ? "var(--on-surface-variant)" : "var(--on-primary-container)";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      width: 112,
      height: 112,
      display: "grid",
      placeItems: "center",
      flex: "0 0 auto"
    }
  }, radar && [0, 1].map(i => /*#__PURE__*/React.createElement("span", {
    key: i,
    className: "ds-ping",
    style: {
      position: "absolute",
      inset: 0,
      borderRadius: "50%",
      border: "2px solid var(--primary)",
      animation: "dsPing 1.8s ease-out infinite",
      animationDelay: `${i * 0.9}s`
    }
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      width: 96,
      height: 96,
      borderRadius: "50%",
      background: bg,
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 44,
    color: fg
  })));
}

// Обёртка центрированных состояний (permission/denied/adapterOff/scanning/empty/connecting).
function CenterState({
  children
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "auto",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 22,
      padding: "32px 24px",
      textAlign: "center",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: 460,
      width: "100%",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      gap: 22
    }
  }, children));
}

// Строка разрешения в экране запроса.
function PermissionRow({
  icon,
  label,
  desc
}) {
  return /*#__PURE__*/React.createElement(CmpSurface, {
    color: "var(--surface-container-low)",
    radius: 14,
    border: true,
    style: {
      width: "100%",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 14,
      padding: 14,
      textAlign: "left"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 44,
      height: 44,
      borderRadius: 12,
      background: "var(--secondary-container)",
      color: "var(--on-secondary-container)",
      display: "grid",
      placeItems: "center",
      flex: "0 0 auto"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 24
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleSmall"
  }, label), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, desc))));
}
const PERMS = {
  android: [{
    icon: "bluetooth",
    label: "Bluetooth (BLE)",
    desc: "Поиск и связь с блоком сигнализации"
  }, {
    icon: "usb",
    label: "USB",
    desc: "Подключение модулей по проводу"
  }],
  ios: [{
    icon: "bluetooth",
    label: "Bluetooth",
    desc: "Поиск и связь с блоком сигнализации"
  }],
  desktop: []
};

// ── Главный экран ─────────────────────────────────────────────────────────────
function DeviceSelectScreen({
  platform = "android",
  initialStep,
  demoEmpty = false,
  showBack = true,
  onBack = () => {}
}) {
  const {
    useState,
    useEffect,
    useRef
  } = React;
  const [rootRef, mode] = useLayout();
  const firstStep = initialStep || (platform === "desktop" ? "scanning" : "permission");
  const [step, setStep] = useState(firstStep);
  const [selectedId, setSelectedId] = useState(null);
  const [connectingId, setConnectingId] = useState(null);
  const scanTimer = useRef(null);

  // Сканирование: авто-переход в список (или «пусто») через задержку.
  useEffect(() => {
    if (step !== "scanning") return;
    clearTimeout(scanTimer.current);
    scanTimer.current = setTimeout(() => setStep(demoEmpty ? "empty" : "list"), 2200);
    return () => clearTimeout(scanTimer.current);
  }, [step, demoEmpty]);
  const selectDevice = d => {
    if (mode === "split") setSelectedId(d.id);else {
      setConnectingId(d.id);
      setStep("connecting");
    }
  };
  const connectSelected = () => {
    setConnectingId(selectedId);
    setStep("connecting");
  };
  const perms = PERMS[platform] || PERMS.android;
  const tileSize = mode === "compact" ? 56 : 60;

  // ── содержимое по шагу ──
  let body;
  if (step === "permission") {
    body = /*#__PURE__*/React.createElement(CenterState, null, /*#__PURE__*/React.createElement(Hero, {
      icon: "bluetooth_searching"
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall",
      as: "h1"
    }, "\u0414\u043E\u0441\u0442\u0443\u043F \u043A \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430\u043C"), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, platform === "ios" ? "Чтобы найти блок сигнализации, приложению нужен доступ к Bluetooth." : "Чтобы найти блок сигнализации, приложению нужен доступ к Bluetooth и USB."))), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10,
        width: "100%"
      }
    }, perms.map(p => /*#__PURE__*/React.createElement(PermissionRow, _extends({
      key: p.label
    }, p)))), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10,
        width: "100%",
        marginTop: 4
      }
    }, /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u0420\u0430\u0437\u0440\u0435\u0448\u0438\u0442\u044C",
      fullWidth: true,
      onClick: () => setStep("scanning")
    }), /*#__PURE__*/React.createElement(CmpTextButtonSafe, {
      text: "\u041D\u0435 \u0441\u0435\u0439\u0447\u0430\u0441",
      onClick: () => setStep("denied")
    })));
  } else if (step === "denied") {
    body = /*#__PURE__*/React.createElement(CenterState, null, /*#__PURE__*/React.createElement(Hero, {
      icon: "lock",
      tone: "error"
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall",
      as: "h1"
    }, "\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430"), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, "\u0411\u0435\u0437 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043D\u0438\u044F \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u043D\u0435 \u0441\u043C\u043E\u0436\u0435\u0442 \u043D\u0430\u0439\u0442\u0438 \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430. \u0412\u043A\u043B\u044E\u0447\u0438\u0442\u0435 \u0434\u043E\u0441\u0442\u0443\u043F \u0432 \u043D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0430\u0445."))), /*#__PURE__*/React.createElement(FixSteps, {
      platform: platform
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10,
        width: "100%",
        marginTop: 4
      }
    }, /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u041E\u0442\u043A\u0440\u044B\u0442\u044C \u043D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438",
      icon: "settings",
      fullWidth: true,
      onClick: () => setStep("permission")
    })));
  } else if (step === "adapterOff") {
    body = /*#__PURE__*/React.createElement(CenterState, null, /*#__PURE__*/React.createElement(Hero, {
      icon: "bluetooth_disabled",
      tone: "neutral"
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall",
      as: "h1"
    }, "Bluetooth \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D"), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, "\u0412\u043A\u043B\u044E\u0447\u0438\u0442\u0435 Bluetooth \u043D\u0430 \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0435, \u0447\u0442\u043E\u0431\u044B \u043D\u0430\u0439\u0442\u0438 \u0431\u043B\u043E\u043A \u0441\u0438\u0433\u043D\u0430\u043B\u0438\u0437\u0430\u0446\u0438\u0438."))), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10,
        width: "100%",
        marginTop: 4
      }
    }, /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u0412\u043A\u043B\u044E\u0447\u0438\u0442\u044C Bluetooth",
      icon: "bluetooth",
      fullWidth: true,
      onClick: () => setStep("scanning")
    })));
  } else if (step === "scanning") {
    body = /*#__PURE__*/React.createElement(CenterState, null, /*#__PURE__*/React.createElement(Hero, {
      icon: "bluetooth_searching",
      radar: true
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall",
      as: "h1"
    }, "\u041F\u043E\u0438\u0441\u043A \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u2026"), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, "\u0414\u0435\u0440\u0436\u0438\u0442\u0435 \u0431\u043B\u043E\u043A \u0441\u0438\u0433\u043D\u0430\u043B\u0438\u0437\u0430\u0446\u0438\u0438 \u0440\u044F\u0434\u043E\u043C. Bluetooth \u0438 USB-\u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430 \u043F\u043E\u044F\u0432\u044F\u0442\u0441\u044F \u0432 \u0441\u043F\u0438\u0441\u043A\u0435 \u0430\u0432\u0442\u043E\u043C\u0430\u0442\u0438\u0447\u0435\u0441\u043A\u0438."))), /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, null));
  } else if (step === "empty") {
    body = /*#__PURE__*/React.createElement(CenterState, null, /*#__PURE__*/React.createElement(Hero, {
      icon: "search_off",
      tone: "neutral"
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall",
      as: "h1"
    }, "\u0423\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u044B"), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, "\u041F\u043E\u0438\u0441\u043A \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0430\u0435\u0442\u0441\u044F. \u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435, \u0447\u0442\u043E \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u043E \u0433\u043E\u0442\u043E\u0432\u043E \u043A \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u044E."))), /*#__PURE__*/React.createElement(FixSteps, {
      empty: true,
      platform: platform
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "inline-flex",
        alignItems: "center",
        gap: 10,
        marginTop: 4
      }
    }, /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 18,
      strokeWidth: 2.5
    }), /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      color: "var(--on-surface-variant)"
    }, "\u0418\u0434\u0451\u0442 \u043F\u043E\u0438\u0441\u043A\u2026")));
  } else if (step === "connecting") {
    const d = DEVICES.find(x => x.id === connectingId) || DEVICES[0];
    body = /*#__PURE__*/React.createElement(CenterState, null, /*#__PURE__*/React.createElement("div", {
      style: {
        position: "relative"
      }
    }, /*#__PURE__*/React.createElement(Hero, {
      icon: d.icon,
      radar: true
    })), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall",
      as: "h1"
    }, "\u041F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435\u2026"), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 2
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleMedium"
    }, d.name), /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-mono)",
        fontSize: 13,
        color: "var(--on-surface-variant)"
      }
    }, d.address))), /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, null), /*#__PURE__*/React.createElement(CmpOutlinedButton, {
      text: "\u041E\u0442\u043C\u0435\u043D\u0430",
      onClick: () => {
        setConnectingId(null);
        setStep("list");
      }
    }));
  } else {
    // step === "list"
    body = /*#__PURE__*/React.createElement(ListBody, {
      mode: mode,
      tileSize: tileSize,
      selectedId: selectedId,
      onSelect: selectDevice,
      onConnect: connectSelected
    });
  }
  return /*#__PURE__*/React.createElement("div", {
    ref: rootRef,
    style: {
      height: "100%",
      display: "flex",
      flexDirection: "column",
      background: "var(--background)",
      color: "var(--on-background)"
    }
  }, /*#__PURE__*/React.createElement(CmpTopAppBar, {
    title: "\u0412\u044B\u0431\u043E\u0440 \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430",
    navigationIcon: showBack ? /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: "\u041D\u0430\u0437\u0430\u0434",
      onClick: onBack
    }) : undefined
  }), body);
}

// Подсказки «как починить» / «почему пусто».
function FixSteps({
  platform = "android",
  empty = false
}) {
  const items = empty ? ["Убедитесь, что на устройстве включено питание", "Поднесите блок ближе — в пределах 1–2 метров", "Проверьте, что Bluetooth включён"] : platform === "ios" ? ["Настройки → Pandora Spec → Bluetooth → включить"] : ["Настройки → Приложения → Pandora Spec → Разрешения", "Включите «Устройства поблизости» и «Bluetooth»"];
  return /*#__PURE__*/React.createElement(CmpSurface, {
    color: "var(--surface-container-low)",
    radius: 14,
    border: true,
    style: {
      width: "100%",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 12,
      padding: 16,
      textAlign: "left"
    }
  }, items.map((t, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      display: "flex",
      alignItems: "flex-start",
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: empty ? "check_circle" : "chevron_right",
    size: 20,
    color: "var(--primary)",
    style: {
      marginTop: 1
    }
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, t)))));
}

// Список найденных устройств: compact/stacked — один столбец; split — master-detail.
function ListBody({
  mode,
  tileSize,
  selectedId,
  onSelect,
  onConnect
}) {
  const selected = DEVICES.find(d => d.id === selectedId) || null;
  const listHeader = /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium"
  }, "\u041D\u0430\u0439\u0434\u0435\u043D\u043E \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432: ", DEVICES.length), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
    size: 16,
    strokeWidth: 2.5
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelLarge",
    color: "var(--on-surface-variant)"
  }, "\u043F\u043E\u0438\u0441\u043A\u2026")));
  const rows = /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 10
    }
  }, DEVICES.map((d, i) => /*#__PURE__*/React.createElement("div", {
    key: d.id,
    className: "ds-row",
    style: {
      animation: "dsRowIn .28s ease both",
      animationDelay: `${i * 45}ms`
    }
  }, /*#__PURE__*/React.createElement(DeviceRow, {
    device: d,
    tileSize: tileSize,
    selected: mode === "split" && d.id === selectedId,
    onClick: () => onSelect(d)
  }))));
  if (mode === "split") {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        display: "flex",
        gap: 20,
        padding: 24,
        boxSizing: "border-box",
        maxWidth: 1100,
        width: "100%",
        margin: "0 auto"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "1 1 0",
        minWidth: 320,
        minHeight: 0,
        display: "flex",
        flexDirection: "column",
        gap: 14
      }
    }, listHeader, /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        overflow: "auto",
        paddingRight: 4
      }
    }, rows)), /*#__PURE__*/React.createElement(CmpDivider, {
      orientation: "vertical"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "1 1 0",
        maxWidth: 420,
        minWidth: 300,
        minHeight: 0,
        overflow: "auto"
      }
    }, /*#__PURE__*/React.createElement(DetailPanel, {
      device: selected,
      onConnect: onConnect
    })));
  }

  // compact / stacked — один центрированный столбец.
  const maxW = mode === "stacked" ? 620 : "100%";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "auto"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: maxW,
      width: "100%",
      margin: "0 auto",
      padding: mode === "stacked" ? "20px 24px 28px" : "14px 14px 22px",
      boxSizing: "border-box",
      display: "flex",
      flexDirection: "column",
      gap: 14
    }
  }, listHeader, rows));
}

// Правая колонка master-detail: карточка выбранного устройства + «Подключить».
function DetailPanel({
  device,
  onConnect
}) {
  if (!device) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        height: "100%",
        minHeight: 260,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 14,
        textAlign: "center",
        padding: 24
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: 72,
        height: 72,
        borderRadius: "50%",
        background: "var(--surface-variant)",
        display: "grid",
        placeItems: "center"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "touch_app",
      size: 34,
      color: "var(--on-surface-variant)"
    })), /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleMedium",
      color: "var(--on-surface-variant)"
    }, "\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u043E \u0438\u0437 \u0441\u043F\u0438\u0441\u043A\u0430"));
  }
  const rows = [{
    label: "Тип",
    value: device.type
  }, {
    label: "Связь",
    value: TRANSPORT[device.transport].label
  }, {
    label: device.transport === "ble" ? "MAC-адрес" : "Адрес",
    value: device.address,
    mono: true
  }];
  if (device.transport === "ble") rows.push({
    label: "Сигнал",
    value: `${device.rssi} dBm`
  });
  return /*#__PURE__*/React.createElement(CmpSurface, {
    color: "var(--surface-container-low)",
    radius: 20,
    border: true,
    style: {
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      padding: 24,
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement(DeviceTile, {
    icon: device.icon,
    size: 128,
    radius: 20,
    showCaption: true
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      textAlign: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "headlineSmall"
  }, device.name), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 6,
      display: "flex",
      justifyContent: "center"
    }
  }, /*#__PURE__*/React.createElement(TransportChip, {
    transport: device.transport
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%",
      display: "flex",
      flexDirection: "column"
    }
  }, rows.map((r, i) => /*#__PURE__*/React.createElement(React.Fragment, {
    key: r.label
  }, i > 0 && /*#__PURE__*/React.createElement(CmpDivider, null), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 16,
      padding: "12px 2px"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)"
  }, r.label), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: r.mono ? "var(--font-mono)" : "var(--font-sans)",
      fontSize: 14,
      fontWeight: 500,
      color: "var(--on-surface)",
      textAlign: "right"
    }
  }, r.value))))), /*#__PURE__*/React.createElement(CmpButton, {
    text: "\u041F\u043E\u0434\u043A\u043B\u044E\u0447\u0438\u0442\u044C",
    icon: "link",
    fullWidth: true,
    onClick: onConnect
  })));
}

// CmpTextButton может отсутствовать в некоторых сборках — безопасная обёртка.
function CmpTextButtonSafe(props) {
  const Btn = DS.CmpTextButton;
  return Btn ? /*#__PURE__*/React.createElement(Btn, props) : /*#__PURE__*/React.createElement(CmpOutlinedButton, props);
}
Object.assign(window, {
  DeviceSelectScreen
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/device-select/DeviceSelect.jsx", error: String((e && e.message) || e) }); }

// features/home/Home.jsx
try { (() => {
// Home.jsx — Pandora Spec main-screen controller.
//   • HomeApp — live, interactive screen. Owns ephemeral UI (menu open, sync
//     in progress); theme / card-variant / profile are controlled from the host
//     HTML so the demo toolbars stay in sync with the in-screen drawer.
// Presentational blocks come from HomeScreens.jsx, so the prototype and the
// parts/states gallery never drift apart. Built only from the design system.

const {
  HomeScreen,
  PROFILES
} = window.HomeScreens;

// Card interaction states, in display order (used by the parts gallery).
window.HOME_CARD_STATES = ["default", "hover", "focus", "pressed", "disabled"];
function HomeApp({
  dark = false,
  onDark = () => {},
  cardVariant = "centered",
  profile = "full",
  initialMenuOpen = false,
  initialLoading = true,
  loadMs = 1500,
  platform = "mobile",
  appVersion = "2.4.1",
  latestVersion = "2.5.0",
  initialUpdateState = "available",
  onLogout = null
}) {
  const {
    useState,
    useRef,
    useEffect
  } = React;
  const [menuOpen, setMenuOpen] = useState(initialMenuOpen);
  const [syncing, setSyncing] = useState(false);
  const [loading, setLoading] = useState(initialLoading);
  const [route, setRoute] = useState("home");
  const isDesktop = platform === "desktop";
  // App-version update is a desktop-only capability (download a newer .dmg build).
  const [updateState, setUpdateState] = useState(isDesktop ? initialUpdateState : "idle");
  const timer = useRef(null);
  const loadTimer = useRef(null);
  const upTimer = useRef(null);
  useEffect(() => () => {
    clearTimeout(timer.current);
    clearTimeout(loadTimer.current);
    clearTimeout(upTimer.current);
  }, []);

  // Simulate initial data load, then reveal the panel (scripted, like auth root).
  useEffect(() => {
    if (!loading) return;
    clearTimeout(loadTimer.current);
    loadTimer.current = setTimeout(() => setLoading(false), loadMs);
    return () => clearTimeout(loadTimer.current);
  }, [loading, loadMs]);

  // Follow host toggles for the persistent panel (expanded/large).
  useEffect(() => {
    setMenuOpen(initialMenuOpen);
  }, [initialMenuOpen]);
  const onSync = () => {
    if (syncing) return;
    setSyncing(true);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setSyncing(false), 1700);
  };
  const onNav = key => {
    // Prototype: real routing lands here. Close the drawer on selection.
    if (key === "settings" || key === "logout") setMenuOpen(false);
    if (key === "logout" && onLogout) onLogout();
    if (key === "support" || key === "requests") {
      // «Заявки на техпомощь» / карточка «Заявки» → support chat mockup for this platform.
      const target = isDesktop ? "support-chat-desktop" : "support-chat";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "quick" || key === "extended") {
      // «Быстрый / Расширенный монтаж» → экран поиска и выбора устройства.
      const target = isDesktop ? "device-select-desktop" : "device-select";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
  };

  // Desktop: check for a newer app build, then surface the update banner.
  const onCheckUpdate = () => {
    if (updateState === "checking") return;
    setUpdateState("checking");
    clearTimeout(upTimer.current);
    upTimer.current = setTimeout(() => setUpdateState("available"), 1400);
  };
  const onOpenUpdate = () => {
    setMenuOpen(false);
    setRoute("update");
  };
  const onBackHome = () => setRoute("home");
  const onDownload = () => {/* prototype: real app would start the .dmg download */};
  const appUpdate = isDesktop ? {
    state: updateState,
    current: appVersion,
    latest: latestVersion,
    onCheck: onCheckUpdate,
    onOpen: onOpenUpdate
  } : null;
  const profileObj = PROFILES[profile] || PROFILES.full;
  return /*#__PURE__*/React.createElement(HomeScreen, {
    dark: dark,
    onDark: onDark,
    menuOpen: menuOpen,
    onMenu: () => setMenuOpen(v => !v),
    syncing: syncing,
    onSync: onSync,
    loading: loading,
    appUpdate: appUpdate,
    route: route,
    onBackHome: onBackHome,
    onDownload: onDownload,
    cardVariant: cardVariant,
    profile: profileObj,
    onNav: onNav
  });
}
window.HomeApp = HomeApp;
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/home/Home.jsx", error: String((e && e.message) || e) }); }

// features/home/HomeScreens.jsx
try { (() => {
// HomeScreens — presentational main screen for Pandora Spec, built ONLY from
// design-system Cmp* components (window.PandoraSpecDesignSystem_b8d6e1).
//
// The screen is a calm "installer work panel": top app bar + drawer + two equal
// primary action cards + a quiet grid of secondary tools. No visible section
// headings ("Монтаж"/"Инструменты"), no marketing hero, no data slop.
//
// Adaptivity — Material 3 Window Size Classes by container width (CLAUDE.md):
//   • compact  (<600)   — 1-col cards, tools as rows, modal drawer.
//   • medium   (600–839)— 2-col cards, 2-col tools, modal drawer.
//   • expanded (840–1199)— 2-col cards, 3-col tools, side-panel drawer.
//   • large    (≥1200)  — 2-col cards, 4-col tools, side-panel drawer.
//
// Everything takes explicit props/state so the same blocks render both the live
// interactive prototype (Home.jsx → HomeApp) and the parts/states gallery.

const HS_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar,
  CmpIconButton,
  CmpText,
  CmpIcon,
  CmpSurface,
  CmpDivider,
  CmpSwitch,
  CmpCircularProgressIndicator,
  CmpLinearProgressIndicator,
  CmpButton,
  CmpTextButton
} = HS_DS;

// ── Icon map (Material Icons filled ligatures; swap for project icons) ───────
const ICON = {
  quick: "bolt",
  extended: "tune",
  configurator: "settings",
  firmware: "archive",
  cars: "directions_car",
  requests: "assignment",
  feedback: "feedback",
  product: "info",
  menu: "menu",
  sync: "sync",
  settings: "settings",
  logout: "logout",
  themeDark: "dark_mode",
  themeLight: "light_mode",
  status: "check_circle",
  person: "person",
  support: "support_agent",
  appUpdate: "system_update",
  download: "download"
};

// ── Content data (icons = old-project SVGs via window.PS_MAIN_ICONS) ─────────
const MAIN_ACTIONS = [{
  key: "quick",
  svg: "fast_mount",
  title: "Быстрый монтаж",
  desc: "Пошаговая установка по готовым настройкам"
}, {
  key: "extended",
  svg: "adv_mount",
  title: "Расширенный монтаж",
  desc: "Детальная настройка и проверка системы"
}];
const TOOLS = [{
  key: "configurator",
  svg: "settings_configurator",
  title: "Конфигуратор",
  desc: "Создание и изменение настроек системы"
}, {
  key: "firmware",
  svg: "firmware_archive",
  title: "Архив прошивок",
  desc: "Поиск и загрузка прошивок"
}, {
  key: "cars",
  svg: "vehicle_support",
  title: "Автомобили",
  desc: "Поддерживаемые автомобили и функции"
}, {
  key: "requests",
  svg: "orders",
  title: "Заявки",
  desc: "Заявки и пользовательский опыт монтажа"
}, {
  key: "feedback",
  svg: "feedback",
  title: "Обратная связь",
  desc: "Отзыв о монтаже и улучшениях"
}, {
  key: "product",
  svg: "articles",
  title: "Информация о товаре",
  desc: "Поиск информации о продукте"
}];
const PROFILES = {
  full: {
    name: "Иван Петров",
    email: "ivan@pandora.pro"
  },
  partial: {
    name: "",
    email: "installer@pandora.pro"
  },
  noEmail: {
    name: "Иван Петров",
    email: ""
  }
};

// ── Container-width hook → Window Size Class ─────────────────────────────────
function useHomeMode() {
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
  if (w >= 1200) mode = "large";else if (w >= 840) mode = "expanded";else if (w >= 600) mode = "medium";
  return [ref, mode];
}

// ── Forced state layer (for the gallery: hover/focus/pressed) ────────────────
function stateOpacity(s) {
  return s === "pressed" ? 0.12 : s === "focus" ? 0.1 : s === "hover" ? 0.08 : 0;
}
function ForcedOverlay({
  state
}) {
  const o = stateOpacity(state);
  if (!o) return null;
  return /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--on-surface)",
      opacity: o,
      pointerEvents: "none",
      borderRadius: "inherit"
    }
  });
}

// ── Status chip in the top app bar ───────────────────────────────────────────
function StatusChip({
  compact = false,
  syncing = false
}) {
  if (syncing) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 8,
        padding: compact ? "8px" : "6px 14px 6px 10px",
        borderRadius: 999,
        background: compact ? "transparent" : "var(--surface-variant)"
      }
    }, /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 18,
      strokeWidth: 2.5
    }), !compact && /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      color: "var(--on-surface-variant)"
    }, "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435\u2026"));
  }
  if (compact) {
    return /*#__PURE__*/React.createElement("div", {
      title: "\u0411\u0430\u0437\u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0430",
      "aria-label": "\u0411\u0430\u0437\u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0430",
      style: {
        width: 40,
        height: 40,
        display: "grid",
        placeItems: "center",
        flex: "0 0 auto"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: ICON.status,
      size: 22,
      color: "var(--success)",
      title: "\u0411\u0430\u0437\u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0430"
    }));
  }
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      padding: "6px 14px 6px 10px",
      borderRadius: 999,
      background: "var(--surface-variant)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: ICON.status,
    size: 18,
    color: "var(--success)"
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelLarge",
    color: "var(--on-surface-variant)"
  }, "\u0411\u0430\u0437\u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0430"));
}

// ── Inline SVG icon (old-project asset; inlined so it inherits currentColor) ──
function PsSvgIcon({
  name,
  size = 32
}) {
  const html = window.PS_MAIN_ICONS && window.PS_MAIN_ICONS[name] || "";
  return /*#__PURE__*/React.createElement("span", {
    "aria-hidden": "true",
    style: {
      width: size,
      height: size,
      display: "inline-flex",
      flex: "0 0 auto"
    },
    dangerouslySetInnerHTML: {
      __html: html
    }
  });
}

// ── Icon tile ─────────────────────────────────────────────────────────────────
function IconTile({
  icon,
  svg,
  size = 64,
  iconSize = 32,
  tint = "primary",
  muted = false
}) {
  const bg = tint === "primary" ? muted ? "color-mix(in srgb, var(--on-surface) 8%, transparent)" : "var(--primary-container)" : muted ? "color-mix(in srgb, var(--on-surface) 8%, transparent)" : "var(--surface-variant)";
  const fg = tint === "primary" ? muted ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--on-primary-container)" : muted ? "color-mix(in srgb, var(--on-surface) 38%, transparent)" : "var(--on-surface-variant)";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      width: size,
      height: size,
      borderRadius: "var(--radius-m)",
      background: bg,
      color: fg,
      display: "grid",
      placeItems: "center",
      flex: "0 0 auto"
    }
  }, svg ? /*#__PURE__*/React.createElement(PsSvgIcon, {
    name: svg,
    size: iconSize
  }) : /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: iconSize,
    color: fg
  }));
}

// ── Primary action card (two equal, large) ───────────────────────────────────
// variant: "centered" (icon on top, centered) | "leading" (icon on the left)
function MainActionCard({
  item,
  variant = "centered",
  state = "default",
  onClick
}) {
  const disabled = state === "disabled";
  const clickable = !disabled && !!onClick;
  const focus = state === "focus";
  const centered = variant === "centered";
  const content = /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: centered ? "column" : "row",
      alignItems: "center",
      textAlign: centered ? "center" : "left",
      gap: centered ? 16 : 18,
      opacity: disabled ? 0.5 : 1,
      width: "100%"
    }
  }, /*#__PURE__*/React.createElement(IconTile, {
    svg: item.svg,
    icon: item.icon,
    size: 64,
    iconSize: 38,
    tint: "primary",
    muted: disabled
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 6,
      alignItems: centered ? "center" : "flex-start",
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleLarge",
    weight: 600,
    align: centered ? "center" : "left"
  }, item.title), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)",
    align: centered ? "center" : "left",
    style: {
      textWrap: "pretty"
    }
  }, item.desc)));
  return /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: clickable ? onClick : undefined,
    color: "var(--surface-container-low)",
    border: true,
    radius: 16,
    className: "ps-focusable",
    role: "button",
    tabIndex: clickable ? 0 : -1,
    "aria-disabled": disabled || undefined,
    style: {
      boxSizing: "border-box",
      padding: centered ? "28px 24px" : "24px",
      minHeight: 168,
      display: "flex",
      alignItems: "center",
      justifyContent: centered ? "center" : "flex-start",
      cursor: clickable ? "pointer" : "default",
      outline: focus ? "2px solid var(--primary)" : "none",
      outlineOffset: 2,
      transition: "box-shadow var(--motion-fast)"
    }
  }, content, /*#__PURE__*/React.createElement(ForcedOverlay, {
    state: state
  }));
}

// ── Secondary tool card (quieter than primary) ───────────────────────────────
// layout: "grid" (icon top, centered) | "row" (icon left, for compact lists)
function ToolCard({
  item,
  layout = "grid",
  state = "default",
  onClick
}) {
  const disabled = state === "disabled";
  const clickable = !disabled && !!onClick;
  const focus = state === "focus";
  const isRow = layout === "row";
  const content = /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: isRow ? "row" : "column",
      alignItems: "center",
      textAlign: isRow ? "left" : "center",
      gap: isRow ? 16 : 12,
      opacity: disabled ? 0.5 : 1,
      width: "100%"
    }
  }, /*#__PURE__*/React.createElement(IconTile, {
    svg: item.svg,
    icon: item.icon,
    size: 48,
    iconSize: 28,
    tint: "neutral",
    muted: disabled
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 4,
      alignItems: isRow ? "flex-start" : "center",
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium",
    align: isRow ? "left" : "center",
    style: {
      textWrap: "pretty"
    }
  }, item.title), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodySmall",
    color: "var(--on-surface-variant)",
    align: isRow ? "left" : "center",
    maxLines: 2,
    style: {
      textWrap: "pretty"
    }
  }, item.desc)));
  return /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: clickable ? onClick : undefined,
    color: "var(--surface)",
    border: true,
    radius: 12,
    className: "ps-focusable",
    role: "button",
    tabIndex: clickable ? 0 : -1,
    "aria-disabled": disabled || undefined,
    style: {
      boxSizing: "border-box",
      padding: isRow ? "14px 16px" : "20px 16px",
      minHeight: isRow ? 72 : 132,
      display: "flex",
      alignItems: "center",
      justifyContent: isRow ? "flex-start" : "center",
      cursor: clickable ? "pointer" : "default",
      outline: focus ? "2px solid var(--primary)" : "none",
      outlineOffset: 2
    }
  }, content, /*#__PURE__*/React.createElement(ForcedOverlay, {
    state: state
  }));
}

// ── Drawer: profile block ─────────────────────────────────────────────────────
function ProfileBlock({
  profile
}) {
  const name = profile && profile.name ? profile.name.trim() : "";
  const email = profile && profile.email ? profile.email.trim() : "";
  const initials = name ? name.split(/\s+/).slice(0, 2).map(p => p[0]).join("").toUpperCase() : "";
  const primary = name || email || "Профиль";
  const secondary = name ? email || "Email не указан" : name ? "" : "Имя не указано";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 14,
      padding: "8px 4px 16px"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 52,
      height: 52,
      borderRadius: 999,
      background: "var(--primary-container)",
      color: "var(--on-primary-container)",
      display: "grid",
      placeItems: "center",
      flex: "0 0 auto"
    }
  }, initials ? /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium",
    weight: 600,
    color: "var(--on-primary-container)"
  }, initials) : /*#__PURE__*/React.createElement(CmpIcon, {
    icon: ICON.person,
    size: 28,
    color: "var(--on-primary-container)"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0,
      display: "flex",
      flexDirection: "column",
      gap: 2
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium",
    maxLines: 1
  }, primary), secondary && /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)",
    maxLines: 1
  }, secondary)));
}

// ── Drawer: one row ───────────────────────────────────────────────────────────
function DrawerRow({
  icon,
  label,
  trailing,
  onClick,
  spin = false
}) {
  return /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: onClick,
    color: "transparent",
    radius: 12,
    className: "ps-focusable",
    role: "button",
    tabIndex: 0,
    style: {
      boxSizing: "border-box",
      minHeight: 52,
      display: "flex",
      alignItems: "center",
      gap: 16,
      padding: "0 12px",
      cursor: onClick ? "pointer" : "default"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 24,
    color: "var(--on-surface-variant)",
    style: spin ? {
      animation: "psSpin 1s linear infinite"
    } : undefined
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyLarge",
    maxLines: 1
  }, label)), trailing);
}

// ── Drawer ────────────────────────────────────────────────────────────────────
function HomeDrawer({
  variant = "modal",
  profile,
  dark,
  onDark,
  syncing,
  onSync,
  appUpdate = null,
  onNav = () => {}
}) {
  const panel = variant === "panel";
  return /*#__PURE__*/React.createElement("nav", {
    "aria-label": "\u041C\u0435\u043D\u044E",
    style: {
      width: panel ? 312 : "min(320px, 86vw)",
      maxWidth: panel ? 312 : 320,
      height: "100%",
      boxSizing: "border-box",
      display: "flex",
      flexDirection: "column",
      background: "var(--surface)",
      color: "var(--on-surface)",
      borderRight: panel ? "1px solid var(--outline-variant)" : "none",
      boxShadow: panel ? "none" : "var(--elevation-3)",
      padding: "20px 16px 16px"
    }
  }, /*#__PURE__*/React.createElement(ProfileBlock, {
    profile: profile
  }), /*#__PURE__*/React.createElement(CmpDivider, null), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 2,
      paddingTop: 8
    }
  }, /*#__PURE__*/React.createElement(DrawerRow, {
    icon: dark ? ICON.themeDark : ICON.themeLight,
    label: "\u0422\u0451\u043C\u043D\u0430\u044F \u0442\u0435\u043C\u0430",
    onClick: () => onDark && onDark(!dark),
    trailing: /*#__PURE__*/React.createElement(CmpSwitch, {
      checked: dark,
      onCheckedChange: onDark,
      thumbIcon: true
    })
  }), /*#__PURE__*/React.createElement(DrawerRow, {
    icon: ICON.sync,
    label: syncing ? "Обновление базы…" : "Обновить базу",
    onClick: syncing ? undefined : onSync,
    spin: syncing,
    trailing: syncing ? /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 20,
      strokeWidth: 2.5
    }) : null
  }), appUpdate && /*#__PURE__*/React.createElement(DrawerRow, {
    icon: ICON.appUpdate,
    label: appUpdate.state === "checking" ? "Проверка обновлений…" : "Проверить обновления",
    onClick: appUpdate.state === "checking" ? undefined : appUpdate.onCheck,
    trailing: appUpdate.state === "checking" ? /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 20,
      strokeWidth: 2.5
    }) : null
  }), /*#__PURE__*/React.createElement(DrawerRow, {
    icon: ICON.settings,
    label: "\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438",
    onClick: () => onNav("settings")
  }), /*#__PURE__*/React.createElement(DrawerRow, {
    icon: ICON.logout,
    label: "\u0412\u044B\u0445\u043E\u0434",
    onClick: () => onNav("logout")
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 10,
      paddingTop: 12
    }
  }, appUpdate && appUpdate.state === "available" && /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: appUpdate.onOpen,
    color: "var(--primary-container)",
    contentColor: "var(--on-primary-container)",
    radius: 12,
    className: "ps-focusable",
    role: "button",
    tabIndex: 0,
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12,
      padding: "12px 14px",
      cursor: "pointer"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: ICON.appUpdate,
    size: 24,
    color: "var(--on-primary-container)"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelLarge",
    weight: 600,
    color: "var(--on-primary-container)"
  }, "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \u043D\u043E\u0432\u0430\u044F \u0432\u0435\u0440\u0441\u0438\u044F"), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 1
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "color-mix(in srgb, var(--on-primary-container) 78%, transparent)"
  }, appUpdate.latest, " \xB7 \u043D\u0430\u0436\u043C\u0438\u0442\u0435, \u0447\u0442\u043E\u0431\u044B \u043E\u0431\u043D\u043E\u0432\u0438\u0442\u044C"))), /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "ChevronRight",
    size: 22,
    color: "var(--on-primary-container)"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "0 12px",
      display: "flex",
      alignItems: "center",
      gap: 6,
      flexWrap: "wrap"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--on-surface-variant)"
  }, "Pandora \u0421\u043F\u0435\u0446 \xB7 \u0432\u0435\u0440\u0441\u0438\u044F ", appUpdate && appUpdate.current || "2.4.1"), appUpdate && appUpdate.state === "uptodate" && /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: 4
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "Check",
    size: 14,
    color: "var(--success)"
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--success)"
  }, "\u043F\u043E\u0441\u043B\u0435\u0434\u043D\u044F\u044F")))));
}

// ── Top app bar ───────────────────────────────────────────────────────────────
function HomeTopBar({
  compactStatus = false,
  syncing = false,
  loading = false,
  onMenu,
  title = "Pandora Спец",
  back = false,
  onBack
}) {
  return /*#__PURE__*/React.createElement(CmpTopAppBar, {
    centered: true,
    title: title,
    style: {
      flex: "0 0 auto"
    },
    navigationIcon: back ? /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: "\u041D\u0430\u0437\u0430\u0434",
      onClick: onBack
    }) : /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: ICON.menu,
      contentDescription: "\u041E\u0442\u043A\u0440\u044B\u0442\u044C \u043C\u0435\u043D\u044E",
      onClick: onMenu,
      disabled: loading
    }),
    actions: loading || back ? null : /*#__PURE__*/React.createElement(StatusChip, {
      compact: compactStatus,
      syncing: syncing
    })
  });
}

// ── Screen body (cards + tools), no visible section headings ─────────────────
function HomeContent({
  mode,
  cardVariant = "centered",
  onNav = () => {}
}) {
  const compact = mode === "compact";
  const mainCols = compact ? 1 : 2;
  const toolCols = mode === "large" ? 4 : mode === "expanded" ? 3 : mode === "medium" ? 2 : 1;
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const maxW = mode === "large" || mode === "expanded" ? 1040 : "none";
  const toolLayout = compact ? "row" : "grid";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "auto"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: maxW,
      margin: "0 auto",
      padding: `${pad}px ${pad}px ${pad + 8}px`,
      display: "flex",
      flexDirection: "column",
      gap: mode === "compact" ? 16 : 24,
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `repeat(${mainCols}, 1fr)`,
      gap: mode === "compact" ? 12 : 16
    }
  }, MAIN_ACTIONS.map(item => /*#__PURE__*/React.createElement(MainActionCard, {
    key: item.key,
    item: item,
    variant: cardVariant,
    onClick: () => onNav(item.key)
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `repeat(${toolCols}, 1fr)`,
      gap: 12
    }
  }, TOOLS.map(item => /*#__PURE__*/React.createElement(ToolCard, {
    key: item.key,
    item: item,
    layout: toolLayout,
    onClick: () => onNav(item.key)
  })))));
}

// ── Loading skeletons (screen loading state) ─────────────────────────
function Skel({
  w,
  h,
  r = 8,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: "ps-skel",
    style: {
      width: w,
      height: h,
      borderRadius: r,
      flex: "0 0 auto",
      ...style
    }
  });
}
function MainCardSkeleton({
  variant = "centered"
}) {
  const centered = variant === "centered";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      boxSizing: "border-box",
      padding: centered ? "28px 24px" : "24px",
      minHeight: 168,
      borderRadius: 16,
      background: "var(--surface-container-low)",
      border: "1px solid var(--outline-variant)",
      display: "flex",
      alignItems: "center",
      justifyContent: centered ? "center" : "flex-start"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: centered ? "column" : "row",
      alignItems: "center",
      gap: centered ? 16 : 18,
      width: "100%"
    }
  }, /*#__PURE__*/React.createElement(Skel, {
    w: 64,
    h: 64,
    r: 16
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 10,
      alignItems: centered ? "center" : "flex-start",
      width: centered ? "auto" : "100%"
    }
  }, /*#__PURE__*/React.createElement(Skel, {
    w: centered ? 150 : 180,
    h: 18,
    r: 6
  }), /*#__PURE__*/React.createElement(Skel, {
    w: centered ? 200 : "80%",
    h: 13,
    r: 6
  }))));
}
function ToolCardSkeleton({
  layout = "grid"
}) {
  const isRow = layout === "row";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      boxSizing: "border-box",
      padding: isRow ? "14px 16px" : "20px 16px",
      minHeight: isRow ? 72 : 132,
      borderRadius: 12,
      background: "var(--surface)",
      border: "1px solid var(--outline-variant)",
      display: "flex",
      alignItems: "center",
      justifyContent: isRow ? "flex-start" : "center"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: isRow ? "row" : "column",
      alignItems: "center",
      gap: isRow ? 16 : 12,
      width: "100%"
    }
  }, /*#__PURE__*/React.createElement(Skel, {
    w: 48,
    h: 48,
    r: 12
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8,
      alignItems: isRow ? "flex-start" : "center",
      width: isRow ? "100%" : "auto"
    }
  }, /*#__PURE__*/React.createElement(Skel, {
    w: isRow ? 140 : 96,
    h: 15,
    r: 6
  }), /*#__PURE__*/React.createElement(Skel, {
    w: isRow ? "70%" : 120,
    h: 12,
    r: 6
  }))));
}

// Mirrors HomeContent layout so the skeleton reflows at every window-size class.
function HomeSkeleton({
  mode,
  cardVariant = "centered"
}) {
  const compact = mode === "compact";
  const mainCols = compact ? 1 : 2;
  const toolCols = mode === "large" ? 4 : mode === "expanded" ? 3 : mode === "medium" ? 2 : 1;
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const maxW = mode === "large" || mode === "expanded" ? 1040 : "none";
  const toolLayout = compact ? "row" : "grid";
  return /*#__PURE__*/React.createElement("div", {
    "aria-busy": "true",
    style: {
      flex: 1,
      minHeight: 0,
      overflow: "hidden"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: maxW,
      margin: "0 auto",
      padding: `${pad}px ${pad}px ${pad + 8}px`,
      display: "flex",
      flexDirection: "column",
      gap: compact ? 16 : 24,
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `repeat(${mainCols}, 1fr)`,
      gap: compact ? 12 : 16
    }
  }, MAIN_ACTIONS.map(it => /*#__PURE__*/React.createElement(MainCardSkeleton, {
    key: it.key,
    variant: cardVariant
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `repeat(${toolCols}, 1fr)`,
      gap: 12
    }
  }, TOOLS.map(it => /*#__PURE__*/React.createElement(ToolCardSkeleton, {
    key: it.key,
    layout: toolLayout
  })))));
}

// ── App-version update: the update screen now lives in its own feature
// (features/app-update/AppUpdateScreen.jsx). The home update route reuses
// window.AppUpdate.UpdateFlow so there is a single source of truth.

// ── Full screen (chrome + drawer + content) ──────────────────────────────────
function HomeScreen({
  dark = false,
  onDark = () => {},
  menuOpen = false,
  onMenu = () => {},
  syncing = false,
  onSync = () => {},
  loading = false,
  appUpdate = null,
  route = "home",
  onBackHome = () => {},
  onDownload = () => {},
  cardVariant = "centered",
  profile = PROFILES.full,
  onNav = () => {}
}) {
  const [rootRef, mode] = useHomeMode();
  const compact = mode === "compact";
  const sidePanel = mode === "expanded" || mode === "large";
  const showPanel = !loading && sidePanel && menuOpen;
  const showModal = !loading && !sidePanel && menuOpen;
  const inUpdate = route === "update";
  return /*#__PURE__*/React.createElement("div", {
    ref: rootRef,
    style: {
      position: "relative",
      height: "100%",
      width: "100%",
      display: "flex",
      flexDirection: "column",
      overflow: "hidden",
      background: "var(--background)",
      color: "var(--on-background)"
    }
  }, /*#__PURE__*/React.createElement("style", null, `
        @keyframes psSpin { to { transform: rotate(360deg); } }
        @keyframes psDrawerIn { from { transform: translateX(-100%); } to { transform: none; } }
        @keyframes psScrimIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes psShimmer { from { background-position: 100% 0; } to { background-position: 0 0; } }
        .ps-focusable:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
        .ps-skel { background: linear-gradient(90deg, var(--surface-variant) 25%, var(--surface-container-highest) 37%, var(--surface-variant) 63%); background-size: 400% 100%; animation: psShimmer 1.4s ease infinite; }
        @media (prefers-reduced-motion: reduce) { .ps-anim { animation: none !important; } .ps-skel { animation: none !important; } }
      `), /*#__PURE__*/React.createElement(HomeTopBar, {
    compactStatus: compact,
    syncing: syncing,
    loading: loading,
    onMenu: onMenu,
    title: inUpdate ? "Обновление приложения" : "Pandora Спец",
    back: inUpdate,
    onBack: onBackHome
  }), loading && /*#__PURE__*/React.createElement(CmpLinearProgressIndicator, null), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      display: "flex"
    }
  }, showPanel && /*#__PURE__*/React.createElement("div", {
    className: "ps-anim",
    style: {
      flex: "0 0 auto",
      animation: "psDrawerIn var(--motion-standard) both"
    }
  }, /*#__PURE__*/React.createElement(HomeDrawer, {
    variant: "panel",
    profile: profile,
    dark: dark,
    onDark: onDark,
    syncing: syncing,
    onSync: onSync,
    appUpdate: appUpdate,
    onNav: onNav
  })), loading ? /*#__PURE__*/React.createElement(HomeSkeleton, {
    mode: mode,
    cardVariant: cardVariant
  }) : inUpdate ? window.AppUpdate ? /*#__PURE__*/React.createElement(window.AppUpdate.UpdateFlow, {
    mode: mode,
    current: appUpdate ? appUpdate.current : "2.4.1",
    latest: appUpdate ? appUpdate.latest : "2.5.0",
    onLater: onBackHome
  }) : null : /*#__PURE__*/React.createElement(HomeContent, {
    mode: mode,
    cardVariant: cardVariant,
    onNav: onNav
  })), showModal && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      zIndex: 10,
      display: "flex"
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "ps-anim",
    onClick: onMenu,
    style: {
      position: "absolute",
      inset: 0,
      background: "color-mix(in srgb, var(--scrim) 42%, transparent)",
      animation: "psScrimIn var(--motion-fast) both"
    }
  }), /*#__PURE__*/React.createElement("div", {
    className: "ps-anim",
    style: {
      position: "relative",
      height: "100%",
      animation: "psDrawerIn var(--motion-standard) both"
    }
  }, /*#__PURE__*/React.createElement(HomeDrawer, {
    variant: "modal",
    profile: profile,
    dark: dark,
    onDark: onDark,
    syncing: syncing,
    onSync: onSync,
    appUpdate: appUpdate,
    onNav: onNav
  }))));
}
window.HomeScreens = {
  useHomeMode,
  HomeScreen,
  HomeTopBar,
  HomeContent,
  HomeDrawer,
  ProfileBlock,
  MainActionCard,
  ToolCard,
  StatusChip,
  IconTile,
  PsSvgIcon,
  HomeSkeleton,
  MainCardSkeleton,
  ToolCardSkeleton,
  ICON,
  MAIN_ACTIONS,
  TOOLS,
  PROFILES
};
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/home/HomeScreens.jsx", error: String((e && e.message) || e) }); }

// features/support-chat/SupportChat.jsx
try { (() => {
// SupportChat.jsx — Pandora Spec support-chat controller.
//   • SupportChatApp — live, interactive screen. Owns the message list, draft,
//     pending attachments, delivery statuses (sent → read), the "specialist is
//     typing…" simulation and the offline queue. Presentational blocks come
//     from SupportChatScreens.jsx.
//
// scenario prop (host toggle):
//   "dialog"  — full demo history, live sending.
//   "empty"   — no messages yet (empty state), first send starts the dialog.
//   "loading" — history skeleton ~1.8s, then the dialog appears.
//   "offline" — no-connection banner; sends queue as "pending" until Повторить.

const SCS = window.SupportChatScreens;
const {
  ChatScreen,
  SC_HISTORY,
  SC_AGENTS,
  SC_REPLIES,
  SC_MOCK_FILES,
  SC_MOCK_PHOTOS
} = SCS;
function scNow() {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}
function SupportChatApp({
  variant = "tonal",
  scenario = "dialog",
  platform = "mobile",
  loadMs = 1800
}) {
  const {
    useState,
    useRef,
    useEffect
  } = React;
  const [rows, setRows] = useState(scenario === "empty" ? [] : SC_HISTORY);
  const [loading, setLoading] = useState(scenario === "loading");
  const [offline, setOffline] = useState(scenario === "offline");
  const [typing, setTyping] = useState(null);
  const [draft, setDraft] = useState("");
  const [attachments, setAttachments] = useState([]);
  const seq = useRef(0);
  const replyIdx = useRef(0);
  const fileIdx = useRef(0);
  const photoIdx = useRef(0);
  const timers = useRef([]);
  const later = (fn, ms) => {
    const t = setTimeout(fn, ms);
    timers.current.push(t);
    return t;
  };
  useEffect(() => () => timers.current.forEach(clearTimeout), []);

  // "loading" — simulate history fetch, then reveal the dialog.
  useEffect(() => {
    if (scenario !== "loading") return;
    later(() => setLoading(false), loadMs);
  }, [scenario, loadMs]);
  const nextId = () => `m${++seq.current}`;
  const setStatus = (ids, status) => setRows(rs => rs.map(r => ids.includes(r.id) ? {
    ...r,
    status
  } : r));

  // Support side: typing indicator with the specialist's email, then a reply.
  const scheduleReply = () => {
    const author = SC_AGENTS.ivan;
    later(() => setTyping(author), 900);
    later(() => {
      setTyping(null);
      const text = SC_REPLIES[replyIdx.current++ % SC_REPLIES.length];
      setRows(rs => [...rs, {
        id: nextId(),
        from: "support",
        author,
        kind: "text",
        text,
        time: scNow()
      }]);
    }, 3200);
  };
  const deliver = ids => {
    later(() => setStatus(ids, "sent"), 500);
    later(() => setStatus(ids, "read"), 1700);
    scheduleReply();
  };
  const onSend = () => {
    const text = draft.trim();
    if (!text && attachments.length === 0) return;
    const time = scNow();
    const status = offline ? "pending" : undefined; // undefined → "sent" after delivery tick
    const out = [];
    attachments.forEach(att => {
      out.push(att.kind === "image" ? {
        id: nextId(),
        from: "me",
        kind: "image",
        image: {
          name: att.name
        },
        time,
        status: status || "sent"
      } : {
        id: nextId(),
        from: "me",
        kind: "doc",
        file: {
          name: att.name,
          size: att.size,
          ext: att.ext
        },
        time,
        status: status || "sent"
      });
    });
    if (text) out.push({
      id: nextId(),
      from: "me",
      kind: "text",
      text,
      time,
      status: status || "sent"
    });
    setRows(rs => [...rs, ...out]);
    setDraft("");
    setAttachments([]);
    if (!offline) deliver(out.map(m => m.id));
  };

  // Offline → Повторить: connection restored, queued messages get delivered.
  const onRetry = () => {
    setOffline(false);
    const ids = [];
    setRows(rs => {
      rs.forEach(r => {
        if (r.status === "pending") ids.push(r.id);
      });
      return rs;
    });
    later(() => {
      setRows(rs => {
        const pend = rs.filter(r => r.status === "pending").map(r => r.id);
        if (pend.length) {
          later(() => setStatus(pend, "read"), 1400);
          scheduleReply();
          return rs.map(r => r.status === "pending" ? {
            ...r,
            status: "sent"
          } : r);
        }
        return rs;
      });
    }, 600);
  };

  // Mock pickers (the real app opens system file / photo pickers).
  const onAttachFile = () => {
    const f = SC_MOCK_FILES[fileIdx.current++ % SC_MOCK_FILES.length];
    setAttachments(a => [...a, {
      id: `a${Date.now()}${a.length}`,
      kind: "doc",
      ...f
    }]);
  };
  const onAttachPhoto = () => {
    const p = SC_MOCK_PHOTOS[photoIdx.current++ % SC_MOCK_PHOTOS.length];
    setAttachments(a => [...a, {
      id: `a${Date.now()}${a.length}`,
      kind: "image",
      ...p
    }]);
  };
  const onRemoveAttachment = id => setAttachments(a => a.filter(x => x.id !== id));
  const onBack = () => {
    const key = platform === "desktop" ? "home-desktop" : platform === "tablet" ? "home-tablet" : "home";
    if (window.psHas && window.psHas(key)) window.psGo(key);
  };
  return /*#__PURE__*/React.createElement(ChatScreen, {
    variant: variant,
    rows: rows,
    typing: typing,
    loading: loading,
    offline: offline,
    onRetry: onRetry,
    onBack: onBack,
    draft: draft,
    onDraft: setDraft,
    onSend: onSend,
    attachments: attachments,
    onAttachFile: onAttachFile,
    onAttachPhoto: onAttachPhoto,
    onRemoveAttachment: onRemoveAttachment,
    isDesktop: platform === "desktop"
  });
}
window.SupportChatApp = SupportChatApp;
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/support-chat/SupportChat.jsx", error: String((e && e.message) || e) }); }

// features/support-chat/SupportChatScreens.jsx
try { (() => {
// SupportChatScreens — presentational support-chat screen for Pandora Spec,
// built ONLY from design-system Cmp* components (window.PandoraSpecDesignSystem_b8d6e1).
//
// The screen is a support conversation: top app bar + message history + composer.
// Incoming messages are labelled with the support specialist's EMAIL (specialists
// rotate, the email identifies who is answering right now). Outgoing messages
// carry delivery status: sent (✓) / read (✓✓) / pending (clock, offline queue).
//
// Bubble variants (host toggle): "tonal" (filled containers) | "flat" (outlined).
//
// Adaptivity — Window Size Classes by container width (CLAUDE.md):
//   • compact  (<600)   — full-width column, bubbles ≤78%.
//   • medium   (600–839)— column max-width 720, centered.
//   • expanded (≥840)   — column max-width 840, centered.
// Touch targets ≥48dp everywhere; correct in light and dark themes.

const SC_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar,
  CmpIconButton,
  CmpText,
  CmpIcon,
  CmpSurface,
  CmpLinearProgressIndicator
} = SC_DS;

// ── Icons (Material Icons filled ligatures / catalog names) ──────────────────
const SC_ICON = {
  back: "ArrowBack",
  attachFile: "attach_file",
  attachPhoto: "image",
  send: "Send",
  doc: "description",
  photo: "image",
  pending: "Schedule",
  sent: "Done",
  read: "done_all",
  offline: "wifi_off",
  empty: "forum",
  close: "Close",
  support: "support_agent"
};

// ── Demo data ─────────────────────────────────────────────────────────────────
// Support side of the dialog: specialists rotate; each message carries the
// email of the person who wrote it.
const SC_AGENTS = {
  elena: "elena.support@pandora.pro",
  ivan: "ivan.support@pandora.pro"
};

// Flat list; rows are either { kind: "date", label } or message objects:
// { id, from: "me"|"support", author?, kind: "text"|"doc"|"image",
//   text?, file?: {name,size,ext}, image?: {name, caption?}, time, status? }
const SC_HISTORY = [{
  kind: "date",
  label: "12 июля"
}, {
  id: "h1",
  from: "me",
  kind: "text",
  text: "Здравствуйте! Pandora DX-4GS, Kia Sportage 2024. После монтажа не читается CAN-шина — статус «нет связи».",
  time: "14:02",
  status: "read"
}, {
  id: "h2",
  from: "me",
  kind: "doc",
  file: {
    name: "монтажная_схема_sportage.pdf",
    size: "2,4 МБ",
    ext: "PDF"
  },
  time: "14:03",
  status: "read"
}, {
  id: "h3",
  from: "support",
  author: SC_AGENTS.elena,
  kind: "text",
  text: "Здравствуйте! Схему получила, посмотрю. Уточните, пожалуйста, куда подключали CAN-High — к разъёму OBD или к жгуту за блоком BCM?",
  time: "14:11"
}, {
  kind: "date",
  label: "Сегодня"
}, {
  id: "h4",
  from: "support",
  author: SC_AGENTS.ivan,
  kind: "text",
  text: "Добрый день, я продолжаю по вашей заявке. Подключение по схеме корректное. Пришлите, пожалуйста, фото точки подключения CAN-шины.",
  time: "09:40"
}, {
  id: "h5",
  from: "me",
  kind: "image",
  image: {
    name: "IMG_4312.jpg",
    caption: "Точка подключения за блоком BCM"
  },
  time: "09:52",
  status: "read"
}, {
  id: "h6",
  from: "me",
  kind: "text",
  text: "Прикрепил фото. Провод — коричневый с белой полосой, скрутка пропаяна.",
  time: "09:53",
  status: "sent"
}];

// Cycled mock replies for the live prototype.
const SC_REPLIES = ["Принято, проверяю по схеме подключения. Ответ дам в течение 10 минут.", "Спасибо, информации достаточно. Передал вопрос инженеру, он подключится к чату.", "Проверьте, пожалуйста, версию прошивки базового блока — обновление вышло вчера."];

// Cycled mock attachments (the real app opens a file/photo picker).
const SC_MOCK_FILES = [{
  name: "схема_подключения.pdf",
  size: "1,8 МБ",
  ext: "PDF"
}, {
  name: "лог_ошибок_can.txt",
  size: "64 КБ",
  ext: "TXT"
}, {
  name: "акт_установки.docx",
  size: "312 КБ",
  ext: "DOCX"
}];
const SC_MOCK_PHOTOS = [{
  name: "IMG_4313.jpg"
}, {
  name: "IMG_4314.jpg"
}, {
  name: "IMG_4315.jpg"
}];

// ── Container-width hook → Window Size Class ─────────────────────────────────
function useChatMode() {
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
  if (w >= 840) mode = "expanded";else if (w >= 600) mode = "medium";
  return [ref, mode];
}
function chatColumnMax(mode) {
  return mode === "expanded" ? 840 : mode === "medium" ? 720 : "none";
}

// ── Date divider ──────────────────────────────────────────────────────────────
function DateDivider({
  label
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "center",
      padding: "6px 0"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "6px 14px",
      borderRadius: 999,
      background: "var(--surface-variant)"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--on-surface-variant)"
  }, label)));
}

// ── Delivery status (outgoing only) ──────────────────────────────────────────
function MsgStatus({
  status,
  tone = "var(--on-surface-variant)"
}) {
  if (!status) return null;
  const icon = status === "read" ? SC_ICON.read : status === "pending" ? SC_ICON.pending : SC_ICON.sent;
  const color = status === "read" ? "var(--primary)" : tone;
  const label = status === "read" ? "Прочитано" : status === "pending" ? "Ожидает отправки" : "Отправлено";
  return /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 16,
    color: color,
    title: label
  });
}

// Time + status row, bottom-right inside a bubble.
function MetaRow({
  time,
  status,
  mine,
  tone
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "flex-end",
      gap: 4,
      marginTop: 2
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelSmall",
    color: tone
  }, time), mine && /*#__PURE__*/React.createElement(MsgStatus, {
    status: status,
    tone: tone
  }));
}

// ── Attachments ───────────────────────────────────────────────────────────────
function DocAttachment({
  file
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12,
      minWidth: 200
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 44,
      height: 44,
      borderRadius: "var(--radius-s)",
      background: "color-mix(in srgb, currentColor 12%, transparent)",
      display: "grid",
      placeItems: "center",
      flex: "0 0 auto"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: SC_ICON.doc,
    size: 24,
    color: "currentColor"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 2,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    weight: 500,
    maxLines: 1,
    color: "currentColor",
    style: {
      wordBreak: "break-all"
    }
  }, file.name), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelSmall",
    color: "color-mix(in srgb, currentColor 70%, transparent)"
  }, file.size, file.ext ? ` · ${file.ext}` : "")));
}

// Image placeholder (real app renders the photo; mockup uses a striped slot).
function ImageAttachment({
  image,
  width = 260,
  height = 170
}) {
  return /*#__PURE__*/React.createElement("div", {
    "aria-label": `Фото ${image.name}`,
    style: {
      width,
      maxWidth: "100%",
      height,
      borderRadius: "var(--radius-m)",
      background: "repeating-linear-gradient(45deg, color-mix(in srgb, var(--on-surface) 7%, transparent) 0 12px, transparent 12px 24px), var(--surface-variant)",
      border: "1px solid var(--outline-variant)",
      display: "grid",
      placeItems: "center",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 6,
      padding: "6px 12px",
      borderRadius: 999,
      background: "color-mix(in srgb, var(--surface) 82%, transparent)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: SC_ICON.photo,
    size: 16,
    color: "var(--on-surface-variant)"
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--on-surface-variant)",
    style: {
      fontFamily: "var(--font-mono)"
    }
  }, image.name)));
}

// ── Message bubble ────────────────────────────────────────────────────────────
// variant: "tonal" (filled) | "flat" (outlined, transparent)
function MessageBubble({
  msg,
  variant = "tonal"
}) {
  const mine = msg.from === "me";
  const tonal = variant === "tonal";
  const isImage = msg.kind === "image";
  const bg = !tonal ? "transparent" : mine ? "var(--primary-container)" : "var(--surface-container-low)";
  const fg = tonal && mine ? "var(--on-primary-container)" : "var(--on-surface)";
  const metaTone = tonal && mine ? "color-mix(in srgb, var(--on-primary-container) 70%, transparent)" : "var(--on-surface-variant)";
  const border = !tonal ? `1px solid ${mine ? "color-mix(in srgb, var(--primary) 45%, transparent)" : "var(--outline-variant)"}` : mine ? "none" : "1px solid var(--outline-variant)";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: mine ? "flex-end" : "flex-start",
      gap: 3
    }
  }, !mine && msg.author && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "0 6px"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--primary)"
  }, msg.author)), /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: "min(560px, 82%)",
      boxSizing: "border-box",
      padding: isImage ? "6px 6px 8px" : "10px 14px 8px",
      borderRadius: 16,
      borderBottomRightRadius: mine ? 4 : 16,
      borderBottomLeftRadius: mine ? 16 : 4,
      background: bg,
      color: fg,
      border
    }
  }, msg.kind === "text" && /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyLarge",
    color: "currentColor",
    style: {
      textWrap: "pretty",
      overflowWrap: "anywhere"
    }
  }, msg.text), msg.kind === "doc" && /*#__PURE__*/React.createElement(DocAttachment, {
    file: msg.file
  }), msg.kind === "image" && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 6
    }
  }, /*#__PURE__*/React.createElement(ImageAttachment, {
    image: msg.image
  }), msg.image.caption && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "0 8px"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "currentColor",
    style: {
      textWrap: "pretty"
    }
  }, msg.image.caption))), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: isImage ? "0 8px" : 0
    }
  }, /*#__PURE__*/React.createElement(MetaRow, {
    time: msg.time,
    status: msg.status,
    mine: mine,
    tone: metaTone
  }))));
}

// ── Typing indicator — “<email> печатает…” + animated dots ───────────────────
function TypingIndicator({
  author,
  variant = "tonal"
}) {
  const tonal = variant === "tonal";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "flex-start",
      gap: 3
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "0 6px"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--primary)"
  }, author, " \xB7 \u043F\u0435\u0447\u0430\u0442\u0430\u0435\u0442\u2026")), /*#__PURE__*/React.createElement("div", {
    "aria-label": `${author} печатает сообщение`,
    style: {
      padding: "14px 18px",
      borderRadius: 16,
      borderBottomLeftRadius: 4,
      background: tonal ? "var(--surface-container-low)" : "transparent",
      border: "1px solid var(--outline-variant)",
      display: "flex",
      gap: 5,
      alignItems: "center"
    }
  }, [0, 1, 2].map(i => /*#__PURE__*/React.createElement("span", {
    key: i,
    className: "sc-dot ps-anim",
    style: {
      width: 8,
      height: 8,
      borderRadius: 999,
      background: "var(--on-surface-variant)",
      animationDelay: `${i * 0.18}s`
    }
  }))));
}

// ── Empty state ───────────────────────────────────────────────────────────────
// Ghost mini-dialog: a quiet preview of what the conversation will look like.
function GhostBubble({
  mine = false,
  w = "62%",
  delay = 0
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: "sc-float ps-anim",
    style: {
      display: "flex",
      justifyContent: mine ? "flex-end" : "flex-start",
      animationDelay: `${delay}s`
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: w,
      height: 34,
      boxSizing: "border-box",
      borderRadius: 14,
      borderBottomRightRadius: mine ? 4 : 14,
      borderBottomLeftRadius: mine ? 14 : 4,
      background: mine ? "color-mix(in srgb, var(--primary) 12%, transparent)" : "color-mix(in srgb, var(--on-surface) 7%, transparent)",
      border: `1px dashed ${mine ? "color-mix(in srgb, var(--primary) 35%, transparent)" : "var(--outline-variant)"}`,
      display: "flex",
      alignItems: "center",
      padding: "0 12px",
      gap: 6,
      color: "var(--on-surface)"
    }
  }, [0.5, 0.3, 0.4].map((op, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    style: {
      height: 6,
      borderRadius: 999,
      flex: `${op} 1 0`,
      background: "currentColor",
      opacity: 0.18
    }
  }))));
}
function EmptyHintChip({
  icon,
  label
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      padding: "8px 14px",
      borderRadius: 999,
      background: "var(--surface-container-low)",
      border: "1px solid var(--outline-variant)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 18,
    color: "var(--primary)"
  }), /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelLarge",
    color: "var(--on-surface-variant)"
  }, label));
}
function EmptyChat() {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 22,
      padding: "40px 24px",
      textAlign: "center"
    }
  }, /*#__PURE__*/React.createElement("div", {
    "aria-hidden": "true",
    style: {
      width: "min(300px, 82%)",
      display: "flex",
      flexDirection: "column",
      gap: 8,
      position: "relative",
      paddingBottom: 6
    }
  }, /*#__PURE__*/React.createElement(GhostBubble, {
    mine: true,
    w: "58%",
    delay: 0
  }), /*#__PURE__*/React.createElement(GhostBubble, {
    w: "70%",
    delay: 0.35
  }), /*#__PURE__*/React.createElement(GhostBubble, {
    mine: true,
    w: "44%",
    delay: 0.7
  }), /*#__PURE__*/React.createElement("div", {
    className: "sc-float ps-anim",
    style: {
      position: "absolute",
      right: "8%",
      bottom: -14,
      animationDelay: "0.5s",
      width: 56,
      height: 56,
      borderRadius: 18,
      background: "var(--primary-container)",
      border: "1px solid color-mix(in srgb, var(--primary) 25%, transparent)",
      boxShadow: "var(--elevation-2)",
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: SC_ICON.support,
    size: 30,
    color: "var(--on-primary-container)"
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8,
      maxWidth: 340
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleLarge",
    weight: 600,
    align: "center"
  }, "\u0421\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0439 \u043F\u043E\u043A\u0430 \u043D\u0435\u0442"), /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--on-surface-variant)",
    align: "center",
    style: {
      textWrap: "pretty"
    }
  }, "\u041E\u043F\u0438\u0448\u0438\u0442\u0435 \u043F\u0440\u043E\u0431\u043B\u0435\u043C\u0443 \u2014 \u0441\u043F\u0435\u0446\u0438\u0430\u043B\u0438\u0441\u0442 \u0442\u0435\u0445\u043F\u043E\u0434\u0434\u0435\u0440\u0436\u043A\u0438 \u043E\u0442\u0432\u0435\u0442\u0438\u0442 \u0432 \u044D\u0442\u043E\u043C \u0447\u0430\u0442\u0435.")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexWrap: "wrap",
      justifyContent: "center",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(EmptyHintChip, {
    icon: SC_ICON.attachFile,
    label: "\u0414\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u044B"
  }), /*#__PURE__*/React.createElement(EmptyHintChip, {
    icon: SC_ICON.photo,
    label: "\u0424\u043E\u0442\u043E \u043C\u043E\u043D\u0442\u0430\u0436\u0430"
  })));
}

// ── History-loading skeleton ─────────────────────────────────────────────────
function BubbleSkeleton({
  mine = false,
  w = 240,
  h = 64
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: mine ? "flex-end" : "flex-start"
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "ps-skel",
    style: {
      width: w,
      maxWidth: "78%",
      height: h,
      borderRadius: 16,
      borderBottomRightRadius: mine ? 4 : 16,
      borderBottomLeftRadius: mine ? 16 : 4
    }
  }));
}
function ChatSkeleton() {
  return /*#__PURE__*/React.createElement("div", {
    "aria-busy": "true",
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 12,
      paddingTop: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "center"
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "ps-skel",
    style: {
      width: 96,
      height: 28,
      borderRadius: 999
    }
  })), /*#__PURE__*/React.createElement(BubbleSkeleton, {
    mine: true,
    w: 280,
    h: 72
  }), /*#__PURE__*/React.createElement(BubbleSkeleton, {
    mine: true,
    w: 220,
    h: 56
  }), /*#__PURE__*/React.createElement(BubbleSkeleton, {
    w: 320,
    h: 88
  }), /*#__PURE__*/React.createElement(BubbleSkeleton, {
    mine: true,
    w: 260,
    h: 180
  }), /*#__PURE__*/React.createElement(BubbleSkeleton, {
    w: 300,
    h: 72
  }));
}

// ── Offline banner ────────────────────────────────────────────────────────────
function OfflineBanner({
  onRetry
}) {
  return /*#__PURE__*/React.createElement("div", {
    role: "status",
    style: {
      flex: "0 0 auto",
      display: "flex",
      alignItems: "center",
      gap: 12,
      padding: "10px 16px",
      background: "var(--error-container)",
      color: "var(--on-error-container)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: SC_ICON.offline,
    size: 22,
    color: "currentColor"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    weight: 500,
    color: "currentColor",
    style: {
      textWrap: "pretty"
    }
  }, "\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F. \u0421\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u044F \u0431\u0443\u0434\u0443\u0442 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u044B \u043F\u0440\u0438 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0438.")), onRetry && /*#__PURE__*/React.createElement(CmpSurface, {
    onClick: onRetry,
    color: "color-mix(in srgb, currentColor 12%, transparent)",
    contentColor: "currentColor",
    radius: 999,
    className: "ps-focusable",
    role: "button",
    tabIndex: 0,
    style: {
      minHeight: 44,
      display: "flex",
      alignItems: "center",
      padding: "0 18px",
      cursor: "pointer",
      flex: "0 0 auto"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelLarge",
    weight: 600,
    color: "var(--on-error-container)"
  }, "\u041F\u043E\u0432\u0442\u043E\u0440\u0438\u0442\u044C")));
}

// ── Pending attachment chip (above the input row) ────────────────────────────
function PendingAttachment({
  att,
  onRemove
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 10,
      padding: "6px 6px 6px 12px",
      borderRadius: 999,
      background: "var(--surface-variant)",
      maxWidth: "100%",
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: att.kind === "image" ? SC_ICON.photo : SC_ICON.doc,
    size: 20,
    color: "var(--on-surface-variant)"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelLarge",
    maxLines: 1,
    style: {
      wordBreak: "break-all"
    }
  }, att.name)), att.size && /*#__PURE__*/React.createElement(CmpText, {
    variant: "labelMedium",
    color: "var(--on-surface-variant)",
    style: {
      flex: "0 0 auto"
    }
  }, att.size), /*#__PURE__*/React.createElement(CmpIconButton, {
    icon: SC_ICON.close,
    contentDescription: `Убрать ${att.name}`,
    onClick: onRemove,
    style: {
      width: 40,
      height: 40
    }
  }));
}

// ── Composer: photo + file buttons, auto-growing input, 48dp send ────────────
function Composer({
  value,
  onChange,
  onSend,
  onAttachFile,
  onAttachPhoto,
  attachments = [],
  onRemoveAttachment = () => {},
  disabled = false,
  maxW = "none",
  isDesktop = false
}) {
  const taRef = React.useRef(null);
  const canSend = !disabled && (value.trim().length > 0 || attachments.length > 0);
  const autoGrow = () => {
    const ta = taRef.current;
    if (!ta) return;
    ta.style.height = "auto";
    ta.style.height = Math.min(ta.scrollHeight, 120) + "px";
  };
  React.useEffect(autoGrow, [value]);
  const onKeyDown = e => {
    // Desktop: Enter sends, Shift+Enter — new line. Mobile keeps Enter as newline.
    if (isDesktop && e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      if (canSend) onSend();
    }
  };
  return /*#__PURE__*/React.createElement("div", {
    style: {
      flex: "0 0 auto",
      borderTop: "1px solid var(--outline-variant)",
      background: "var(--surface)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: maxW,
      margin: "0 auto",
      padding: "8px 8px 10px",
      boxSizing: "border-box",
      display: "flex",
      flexDirection: "column",
      gap: 8
    }
  }, attachments.length > 0 && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexWrap: "wrap",
      gap: 8,
      padding: "2px 4px 0"
    }
  }, attachments.map(att => /*#__PURE__*/React.createElement(PendingAttachment, {
    key: att.id,
    att: att,
    onRemove: () => onRemoveAttachment(att.id)
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "flex-end",
      gap: 2
    }
  }, /*#__PURE__*/React.createElement(CmpIconButton, {
    icon: SC_ICON.attachFile,
    contentDescription: "\u041F\u0440\u0438\u043A\u0440\u0435\u043F\u0438\u0442\u044C \u0434\u043E\u043A\u0443\u043C\u0435\u043D\u0442",
    onClick: onAttachFile,
    disabled: disabled
  }), /*#__PURE__*/React.createElement(CmpIconButton, {
    icon: SC_ICON.attachPhoto,
    contentDescription: "\u041F\u0440\u0438\u043A\u0440\u0435\u043F\u0438\u0442\u044C \u0444\u043E\u0442\u043E",
    onClick: onAttachPhoto,
    disabled: disabled
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      display: "flex",
      alignItems: "center",
      background: "var(--surface-container-highest)",
      borderRadius: 24,
      padding: "11px 16px",
      boxSizing: "border-box",
      minHeight: 48
    }
  }, /*#__PURE__*/React.createElement("textarea", {
    ref: taRef,
    rows: 1,
    value: value,
    disabled: disabled,
    placeholder: "\u0421\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435\u2026",
    "aria-label": "\u0422\u0435\u043A\u0441\u0442 \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u044F",
    onChange: e => onChange(e.target.value),
    onKeyDown: onKeyDown,
    style: {
      flex: 1,
      minWidth: 0,
      resize: "none",
      border: "none",
      outline: "none",
      background: "transparent",
      color: "var(--on-surface)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--body-large-size)",
      lineHeight: "var(--body-large-line)",
      padding: 0,
      maxHeight: 120
    }
  })), /*#__PURE__*/React.createElement("button", {
    type: "button",
    onClick: canSend ? onSend : undefined,
    disabled: !canSend,
    "aria-label": "\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435",
    className: "ps-focusable",
    style: {
      width: 48,
      height: 48,
      flex: "0 0 auto",
      marginLeft: 4,
      border: "none",
      borderRadius: 999,
      cursor: canSend ? "pointer" : "default",
      background: canSend ? "var(--primary)" : "color-mix(in srgb, var(--on-surface) 10%, transparent)",
      color: canSend ? "var(--on-primary)" : "color-mix(in srgb, var(--on-surface) 38%, transparent)",
      display: "grid",
      placeItems: "center",
      transition: "background var(--motion-fast)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: SC_ICON.send,
    size: 22,
    color: "currentColor"
  })))));
}

// ── Wallpaper tile: tiny keys scattered at random angles ─────────────────
// Used as a CSS mask, so the tint comes from theme tokens (color-mix below).
// One open-end wrench silhouette in the reference style (round C-head + long
// rounded handle), drawn as two thick strokes; reused 6× per tile.
const SC_KEYS_TILE = `url("data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><defs><g id='k' fill='none' stroke='black' stroke-linecap='round'><line x1='3' y1='10' x2='17' y2='10' stroke-width='5'/><path d='M27.2 6.5 A5.5 5.5 0 1 0 27.2 13.5' stroke-width='4.5'/></g></defs><use href='%23k' transform='translate(22 26) rotate(28)'/><use href='%23k' transform='translate(148 40) rotate(-38)'/><use href='%23k' transform='translate(66 106) rotate(82)'/><use href='%23k' transform='translate(188 128) rotate(-64)'/><use href='%23k' transform='translate(38 184) rotate(12)'/><use href='%23k' transform='translate(136 200) rotate(-18)'/></svg>")`;

// ── Full screen ───────────────────────────────────────────────────────────────
function ChatScreen({
  variant = "tonal",
  rows = [],
  // [{kind:"date"...} | message]
  typing = null,
  // email of the specialist typing, or null
  loading = false,
  offline = false,
  onRetry,
  onBack = () => {},
  // composer
  draft = "",
  onDraft = () => {},
  onSend = () => {},
  attachments = [],
  onAttachFile = () => {},
  onAttachPhoto = () => {},
  onRemoveAttachment = () => {},
  isDesktop = false
}) {
  const [rootRef, mode] = useChatMode();
  const listRef = React.useRef(null);
  const maxW = chatColumnMax(mode);
  const pad = mode === "compact" ? 12 : mode === "medium" ? 24 : 32;
  const empty = !loading && rows.length === 0;

  // Keep the newest message in view (plain scrollTop — never scrollIntoView).
  React.useEffect(() => {
    const el = listRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [rows.length, typing, loading]);
  return /*#__PURE__*/React.createElement("div", {
    ref: rootRef,
    "data-screen-label": "\u0427\u0430\u0442 \u0442\u0435\u0445\u043F\u043E\u0434\u0434\u0435\u0440\u0436\u043A\u0438",
    style: {
      position: "relative",
      height: "100%",
      width: "100%",
      display: "flex",
      flexDirection: "column",
      overflow: "hidden",
      background: "var(--background)",
      color: "var(--on-background)"
    }
  }, /*#__PURE__*/React.createElement("style", null, `
        @keyframes scTyping { 0%, 60%, 100% { opacity: .35; transform: translateY(0); } 30% { opacity: 1; transform: translateY(-3px); } }
        @keyframes psShimmer { from { background-position: 100% 0; } to { background-position: 0 0; } }
        .sc-dot { animation: scTyping 1.2s ease-in-out infinite; }
        @keyframes scFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } }
        .sc-float { animation: scFloat 4.5s ease-in-out infinite; }
        .ps-skel { background: linear-gradient(90deg, var(--surface-variant) 25%, var(--surface-container-highest) 37%, var(--surface-variant) 63%); background-size: 400% 100%; animation: psShimmer 1.4s ease infinite; }
        .ps-focusable:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
        @media (prefers-reduced-motion: reduce) { .ps-anim, .sc-dot, .ps-skel { animation: none !important; } }
      `), /*#__PURE__*/React.createElement(CmpTopAppBar, {
    centered: isDesktop,
    title: "\u0422\u0435\u0445\u043F\u043E\u0434\u0434\u0435\u0440\u0436\u043A\u0430",
    style: {
      flex: "0 0 auto"
    },
    navigationIcon: /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: SC_ICON.back,
      contentDescription: "\u041D\u0430\u0437\u0430\u0434",
      onClick: onBack
    })
  }), loading && /*#__PURE__*/React.createElement(CmpLinearProgressIndicator, null), offline && /*#__PURE__*/React.createElement(OfflineBanner, {
    onRetry: onRetry
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minHeight: 0,
      position: "relative"
    }
  }, /*#__PURE__*/React.createElement("div", {
    "aria-hidden": "true",
    style: {
      position: "absolute",
      inset: 0,
      pointerEvents: "none",
      background: "color-mix(in srgb, var(--on-surface) 60%, var(--primary))",
      opacity: 0.05,
      WebkitMaskImage: SC_KEYS_TILE,
      maskImage: SC_KEYS_TILE,
      WebkitMaskSize: "240px 240px",
      maskSize: "240px 240px"
    }
  }), /*#__PURE__*/React.createElement("div", {
    "aria-hidden": "true",
    style: {
      position: "absolute",
      inset: 0,
      pointerEvents: "none",
      background: "radial-gradient(120% 70% at 50% -20%, color-mix(in srgb, var(--primary) 6%, transparent), transparent 70%)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    ref: listRef,
    style: {
      position: "absolute",
      inset: 0,
      overflow: "auto",
      display: "flex",
      flexDirection: "column"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: maxW,
      width: "100%",
      margin: "0 auto",
      padding: `${pad}px ${pad}px 16px`,
      boxSizing: "border-box",
      display: "flex",
      flexDirection: "column",
      gap: 10,
      flex: empty ? 1 : "0 0 auto"
    }
  }, loading && /*#__PURE__*/React.createElement(ChatSkeleton, null), empty && /*#__PURE__*/React.createElement(EmptyChat, null), !loading && rows.map((row, i) => row.kind === "date" ? /*#__PURE__*/React.createElement(DateDivider, {
    key: `d${i}`,
    label: row.label
  }) : /*#__PURE__*/React.createElement(MessageBubble, {
    key: row.id,
    msg: row,
    variant: variant
  })), !loading && typing && /*#__PURE__*/React.createElement(TypingIndicator, {
    author: typing,
    variant: variant
  })))), /*#__PURE__*/React.createElement(Composer, {
    value: draft,
    onChange: onDraft,
    onSend: onSend,
    onAttachFile: onAttachFile,
    onAttachPhoto: onAttachPhoto,
    attachments: attachments,
    onRemoveAttachment: onRemoveAttachment,
    disabled: loading,
    maxW: maxW,
    isDesktop: isDesktop
  }));
}
window.SupportChatScreens = {
  useChatMode,
  ChatScreen,
  Composer,
  MessageBubble,
  TypingIndicator,
  DateDivider,
  MsgStatus,
  DocAttachment,
  ImageAttachment,
  EmptyChat,
  ChatSkeleton,
  OfflineBanner,
  PendingAttachment,
  SC_ICON,
  SC_AGENTS,
  SC_HISTORY,
  SC_REPLIES,
  SC_MOCK_FILES,
  SC_MOCK_PHOTOS
};
})(); } catch (e) { __ds_ns.__errors.push({ path: "features/support-chat/SupportChatScreens.jsx", error: String((e && e.message) || e) }); }

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

// res/icons/main-icons.js
try { (() => {
// Pandora Спец — main-screen icons (inlined for currentColor theming).
// Source: old-project SVGs (uploads/Архив/main_*). class refs → inline styles;
// monochrome icons inherit fill=currentColor; duotone (fast_mount, adv_mount) use
// opacity .3 for base layers. Render INLINE (not <img>) so currentColor applies.
(function () {
  window.PS_MAIN_ICONS = {
    "fast_mount": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n\n<g>\n&#x9;<rect x=\"4.3\" y=\"64.3\" style=\"fill:currentColor;opacity:.3\" width=\"119.4\" height=\"28.9\"></rect>\n&#x9;<path style=\"fill:currentColor;opacity:.3\" d=\"M4.3,105c0,9.4,7.7,17.1,17.1,17.1h85.3c9.4,0,17.1-7.7,17.1-17.1V93.2H4.3V105z\"></path>\n&#x9;<path style=\"fill:currentColor;opacity:.3\" d=\"M106.6,5.9H21.4c-1.7,0-3.3,0.3-4.8,0.7h94.9C109.9,6.2,108.3,5.9,106.6,5.9z\"></path>\n&#x9;<rect x=\"4.3\" y=\"35.5\" style=\"fill:currentColor;opacity:.3\" width=\"119.4\" height=\"28.9\"></rect>\n&#x9;<path style=\"fill:currentColor;opacity:.3\" d=\"M123.7,23c0-7.8-5.2-14.3-12.3-16.4H16.6C9.5,8.7,4.3,15.2,4.3,23v12.5h119.4C123.7,35.5,123.7,23,123.7,23z\"></path>\n&#x9;<g>\n&#x9;&#x9;<g>\n&#x9;&#x9;&#x9;<g>\n&#x9;&#x9;&#x9;&#x9;<g>\n&#x9;&#x9;&#x9;&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M103.1,87.2c0-0.1,0.1-0.1,0.1-0.2c0.2-0.3,0.3-0.6,0.5-0.9c0.2-0.5,0.5-1,0.7-1.5c0-0.1,0-0.1,0.1-0.2&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;c3.8-8.3,4.8-17.5,3.1-26.4v-0.1c-0.2-0.9-0.4-1.7-0.6-2.6c0-0.1,0-0.1-0.1-0.2c-0.2-0.9-0.5-1.7-0.8-2.6&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;c-1.1-3.4-2.6-6.5-4.4-9.5c-1.1-1.8-3.5-2.2-5.1-1l0,0c-1.5,1.1-1.8,3.2-0.8,4.7c6.6,10.5,7.6,24.1,1.7,36&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;c-2,3.9-4.6,7.4-7.6,10.2L89.8,93C79.1,103,63,106,49,99.4l2.1-4.3c0.6-1.2,0-1.9-1.3-1.6l-11.7,2.8c-1.3,0.3-1.9,1.6-1.4,2.8&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;l4.9,11c0.5,1.2,1.5,1.2,2.1,0l2.1-4.3c10.2,4.9,21.7,5.6,32.5,2c1.1-0.4,2.2-0.8,3.2-1.2l0,0c0,0,0.1,0,0.1-0.1&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;C90.9,102.8,98.4,96,103.1,87.2z\"></path>\n&#x9;&#x9;&#x9;&#x9;</g>\n&#x9;&#x9;&#x9;&#x9;<g>\n&#x9;&#x9;&#x9;&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M31.9,90.4c1.5-1.1,1.8-3.2,0.8-4.7c-6.6-10.5-7.7-24.1-1.7-36c2-3.9,4.6-7.4,7.6-10.2l0.1-0.1&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;c10.7-10,26.8-13,40.8-6.4l-2.1,4.3c-0.6,1.2,0,1.9,1.3,1.6L90.4,36c1.3-0.3,1.9-1.6,1.4-2.8l-4.9-11c-0.5-1.2-1.5-1.2-2.1,0&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;l-2.1,4.3c-10.2-4.9-21.7-5.6-32.5-2c-1.1,0.4-2.2,0.8-3.2,1.2l0,0c0,0-0.1,0-0.1,0.1c-9.2,3.9-16.7,10.7-21.4,19.5&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;c0,0.1-0.1,0.1-0.1,0.2c-0.2,0.3-0.3,0.6-0.5,0.9c-0.2,0.5-0.5,1-0.7,1.5c0,0.1,0,0.1-0.1,0.2c-3.8,8.3-4.8,17.5-3.1,26.4v0.1&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;c0.2,0.9,0.4,1.7,0.6,2.6c0,0.1,0,0.1,0.1,0.2c0.2,0.9,0.5,1.7,0.8,2.6c1.1,3.4,2.6,6.5,4.4,9.5C27.9,91.2,30.2,91.6,31.9,90.4&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;&#x9;L31.9,90.4z\"></path>\n&#x9;&#x9;&#x9;&#x9;</g>\n&#x9;&#x9;&#x9;</g>\n&#x9;&#x9;&#x9;<g>\n&#x9;&#x9;&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M79.3,61.7v1.7v0.4h-0.4H42.7V82c0,1.6,0.9,3.4,2.3,4.8c1.6,1.5,3.6,2.3,5.6,2.3h27.2&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;c2.1,0,4.1-0.8,5.6-2.3c1.4-1.4,2.3-3.2,2.3-4.8V63.4v-1.7c0-4.2-1.2-8.2-3.3-11.7C80.5,53.5,79.3,57.5,79.3,61.7z M67.9,78.5&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;c0,1.8-1.3,3.7-3.7,3.7c-2.4,0-3.7-1.9-3.7-3.7v-4c0-1.8,1.3-3.7,3.7-3.7c2.8,0,3.7,1.7,3.7,3.7V78.5z\"></path>\n&#x9;&#x9;&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M49.2,61.3c0.2-8.4,6.9-15.2,15-15.2c6.4,0,12.1,4.3,14.2,10.5l0.1-0.3l0.2-0.8l0.2-0.8l0.3-0.8l0.3-0.8&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;l0.3-0.8l0.3-0.8l0.4-0.8l0.4-0.7l0.4-0.7l0.4-0.7c-4.1-5.9-10.5-9.3-17.4-9.3c-11.7,0-21.3,9.9-21.5,22.1h6.4V61.3z\"></path>\n&#x9;&#x9;&#x9;</g>\n&#x9;&#x9;</g>\n&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M44.9,29.7L29.3,41.6l-5.3-7c-1-1.3-0.7-3.1,0.6-4.1l11-8.3c1.3-1,3.1-0.7,4.1,0.6L44.9,29.7z\"></path>\n&#x9;</g>\n</g>\n</svg>",
    "adv_mount": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n\n<g>\n&#x9;<path style=\"fill:currentColor\" d=\"M110.5,122.6c5.2,0,9.5-4.2,9.5-9.5V83.5V44.5V14.8c0-5.2-4.2-9.5-9.5-9.5H58.7v39.1v39.1v39.1H110.5z\"></path>\n&#x9;<g>\n&#x9;&#x9;<path style=\"fill:currentColor;opacity:.3\" d=\"M8,113.2c0,5.2,4.2,9.5,9.5,9.5h51.8V83.5H8V113.2z\"></path>\n&#x9;&#x9;<path style=\"fill:currentColor;opacity:.3\" d=\"M69.3,5.4H17.5C12.2,5.4,8,9.6,8,14.8v29.6h61.3V5.4z\"></path>\n&#x9;&#x9;<rect x=\"8\" y=\"44.5\" style=\"fill:currentColor;opacity:.3\" width=\"61.3\" height=\"39.1\"></rect>\n&#x9;</g>\n&#x9;<g>\n&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M48.7,64.1V65v0.2h-0.2H30.1v9.2c0,0.8,0.4,1.7,1.2,2.4c0.8,0.8,1.8,1.2,2.9,1.2h13.8c1.1,0,2.1-0.4,2.9-1.2&#xA;&#x9;&#x9;&#x9;c0.7-0.7,1.2-1.6,1.2-2.4V65v-0.9c0-2.1-0.6-4.2-1.7-6C49.2,59.9,48.7,62,48.7,64.1z M42.9,72.6c0,0.9-0.6,1.9-1.9,1.9&#xA;&#x9;&#x9;&#x9;c-1.2,0-1.9-0.9-1.9-1.9v-2.1c0-0.9,0.6-1.9,1.9-1.9c1.4,0,1.9,0.9,1.9,1.9V72.6z\"></path>\n&#x9;&#x9;<path style=\"fill:currentColor\" d=\"M33.4,63.9c0.1-4.3,3.5-7.7,7.6-7.7c3.3,0,6.1,2.2,7.2,5.4l0-0.2l0.1-0.4l0.1-0.4l0.1-0.4l0.1-0.4l0.2-0.4&#xA;&#x9;&#x9;&#x9;l0.2-0.4l0.2-0.4l0.2-0.4l0.2-0.4l0.2-0.3c-2.1-3-5.4-4.7-8.9-4.7c-6,0-10.8,5-10.9,11.2H33.4z\"></path>\n&#x9;</g>\n&#x9;<g>\n&#x9;&#x9;\n&#x9;&#x9;&#x9;<rect x=\"86.2\" y=\"22.2\" transform=\"matrix(0.7071 -0.7071 0.7071 0.7071 10.7543 77.9871)\" style=\"fill:currentColor\" width=\"26.6\" height=\"7.6\"></rect>\n&#x9;&#x9;\n&#x9;&#x9;&#x9;<rect x=\"81.6\" y=\"22.7\" transform=\"matrix(-0.7071 -0.7071 0.7071 -0.7071 129.0224 106.4977)\" style=\"fill:currentColor\" width=\"10\" height=\"7.6\"></rect>\n&#x9;</g>\n&#x9;<g>\n&#x9;&#x9;\n&#x9;&#x9;&#x9;<rect x=\"86.2\" y=\"58.3\" transform=\"matrix(0.7071 -0.7071 0.7071 0.7071 -14.7221 88.5398)\" style=\"fill:currentColor\" width=\"26.6\" height=\"7.6\"></rect>\n&#x9;&#x9;\n&#x9;&#x9;&#x9;<rect x=\"81.6\" y=\"58.8\" transform=\"matrix(-0.7071 -0.7071 0.7071 -0.7071 103.546 168.0033)\" style=\"fill:currentColor\" width=\"10\" height=\"7.6\"></rect>\n&#x9;</g>\n&#x9;<g>\n&#x9;&#x9;\n&#x9;&#x9;&#x9;<rect x=\"86.2\" y=\"94.3\" transform=\"matrix(0.7071 -0.7071 0.7071 0.7071 -40.1986 99.0925)\" style=\"fill:currentColor\" width=\"26.6\" height=\"7.6\"></rect>\n&#x9;&#x9;\n&#x9;&#x9;&#x9;<rect x=\"81.6\" y=\"94.8\" transform=\"matrix(-0.7071 -0.7071 0.7071 -0.7071 78.0695 229.509)\" style=\"fill:currentColor\" width=\"10\" height=\"7.6\"></rect>\n&#x9;</g>\n&#x9;<path style=\"fill:currentColor\" d=\"M59.9,44.2c0.2-0.7,2.4-17.5,2.5-18.2c0-0.1,0.3-1.2-0.8-1.8c-0.8-0.5-11.7-4.4-21.7-4.4&#xA;&#x9;&#x9;c-10,0-21,3.9-21.7,4.4c-1,0.6-0.8,1.6-0.8,1.8c0.1,0.7,2.3,17.5,2.5,18.2c0.1,0.6,0.6,0.9,1,0.9c0.6,0,1.6-0.2,2.7-0.3&#xA;&#x9;&#x9;c1.8-0.2,10.5-0.5,16.4-0.5c5.9,0.1,14.6,0.3,16.4,0.5c1.1,0.1,2,0.3,2.7,0.3C59.3,45.1,59.7,44.8,59.9,44.2z\"></path>\n&#x9;<path style=\"fill:currentColor\" d=\"M18.7,87.2c1,0.6,3.7,1.7,7.1,2.4c3.9,0.8,10.4,1.3,14.1,1.3c3.6,0,10.2-0.6,14.1-1.3c3.4-0.7,6.2-1.9,7.1-2.4&#xA;&#x9;&#x9;c0.3,5.1,0.5,10.6,0.4,17.2c0,0.2-0.1,0.4-0.3,0.5c-4.7,2.4-13,4.6-21.2,4.6c-8.2,0-16.5-2.2-21.2-4.6c-0.2-0.1-0.3-0.3-0.3-0.5&#xA;&#x9;&#x9;C18.2,97.8,18.4,92.3,18.7,87.2z\"></path>\n</g>\n</svg>",
    "settings_configurator": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n<g>\n&#x9;<path d=\"M66.1,22.5c-1.7,0-4.9,0-7.1,0.1c-2.3-1.4-4.8-2.5-7.4-3.4l-2.1-0.7l-1.1,1.9C47,23.3,44.1,25,41,25c-3.1,0-6-1.7-7.5-4.5&#xA;&#x9;&#x9;l-1-1.9l-2.1,0.7c-4.9,1.5-9.3,4.1-13.2,7.6l-1.6,1.5l1.1,1.9c1.6,2.7,1.6,6,0.1,8.7c-1.6,2.7-4.5,4.4-7.6,4.3l-2.2-0.1l-0.5,2.2&#xA;&#x9;&#x9;c-0.6,2.6-0.9,5.2-0.9,7.7c0,2.5,0.3,5,0.8,7.6l0.5,2.2l2.2-0.1c3.1-0.1,6,1.6,7.6,4.3c1.6,2.7,1.5,6-0.1,8.7l-1.1,1.9L17,79&#xA;&#x9;&#x9;c2.4,2.2,5,4,7.8,5.5v14.5c0,9,21.4,13.2,41.2,13.2c19.9,0,41.2-4.1,41.2-13.2V77.8V56.8V35.7C107.3,26.7,86,22.5,66.1,22.5z&#xA;&#x9;&#x9; M21.3,64.2c-2.1-3.7-5.9-6.2-10-6.8c-0.2-1.5-0.3-3-0.3-4.5c0-1.5,0.1-3,0.4-4.6c4.2-0.6,7.9-3.1,10.1-6.8&#xA;&#x9;&#x9;c2.1-3.7,2.4-8.2,0.9-12.1c2.4-1.9,5-3.4,7.8-4.5c2.6,3.3,6.6,5.3,10.9,5.3c4.3,0,8.3-2,10.9-5.3c2.8,1.1,5.4,2.6,7.8,4.6&#xA;&#x9;&#x9;c-1.6,3.9-1.3,8.4,0.9,12.1c2.1,3.7,5.9,6.2,10,6.8c0.2,1.5,0.3,3,0.3,4.5c0,1.5-0.1,3-0.4,4.6c-4.2,0.6-7.9,3.1-10.1,6.8&#xA;&#x9;&#x9;c-2.1,3.7-2.4,8.2-0.9,12.1c-2.4,1.9-5,3.4-7.8,4.5c-2.6-3.3-6.6-5.3-10.9-5.3c-4.3,0-8.3,2-10.9,5.3c-2.8-1.1-5.4-2.6-7.8-4.6&#xA;&#x9;&#x9;C23.7,72.4,23.5,67.9,21.3,64.2z M47.7,84.3c-4.1-0.6-7.7-1.4-10.7-2.4c1.2-0.6,2.5-1,3.9-1C43.6,80.9,46.1,82.2,47.7,84.3z&#xA;&#x9;&#x9; M66.1,106.7c-23.7,0-35.9-5.5-35.9-7.8V86.6c0,0,0,0,0,0l2.1,0.7l0.7-1.2c7.5,3,19.8,4.9,33.1,4.9c13.8,0,28.3-2,35.9-6.2v14.1&#xA;&#x9;&#x9;C102,101.2,89.8,106.7,66.1,106.7z M66.1,85.6c-3.7,0-7.3-0.2-10.7-0.4c3.4-1.5,6.5-3.6,9.3-6.2l1.6-1.5l-1.1-1.9&#xA;&#x9;&#x9;c-1.1-1.8-1.4-3.8-1.1-5.8c0.7,0,1.3,0,2,0c0.8,0,1.5,0,2.3,0c0.2,0,0.4,0,0.7,0c0.6,0,1.2,0,1.8,0c0.2,0,0.4,0,0.6,0&#xA;&#x9;&#x9;c0.8,0,1.5-0.1,2.3-0.1c0.2,0,0.3,0,0.5,0c0.6,0,1.2-0.1,1.9-0.1c0.2,0,0.4,0,0.7,0c0.7-0.1,1.4-0.1,2-0.2c0.1,0,0.2,0,0.2,0&#xA;&#x9;&#x9;c0.7-0.1,1.5-0.1,2.2-0.2c0.2,0,0.4,0,0.6-0.1c0.6-0.1,1.1-0.1,1.7-0.2c0.2,0,0.4-0.1,0.6-0.1c0.7-0.1,1.4-0.2,2.1-0.3&#xA;&#x9;&#x9;c0.1,0,0.2,0,0.3-0.1c0.6-0.1,1.2-0.2,1.7-0.3c0.2,0,0.4-0.1,0.6-0.1c0.6-0.1,1.1-0.2,1.7-0.3c0.1,0,0.2-0.1,0.4-0.1&#xA;&#x9;&#x9;c0.6-0.1,1.3-0.3,1.9-0.5c0.2,0,0.3-0.1,0.5-0.1c0.5-0.1,0.9-0.3,1.4-0.4c0.2,0,0.3-0.1,0.5-0.1c0.6-0.2,1.2-0.4,1.7-0.5&#xA;&#x9;&#x9;c0.1,0,0.1,0,0.1-0.1c0.5-0.2,1-0.4,1.5-0.5c0.2-0.1,0.3-0.1,0.4-0.2c0.4-0.2,0.8-0.3,1.2-0.5c0.1,0,0.2-0.1,0.3-0.1&#xA;&#x9;&#x9;c0.5-0.2,0.9-0.4,1.3-0.7c0,0,0.1,0,0.1,0v14.1C102,80.2,89.8,85.6,66.1,85.6z M67.3,64.5c1.5-1.2,3.5-1.8,5.5-1.8l2.2,0.1l0.5-2.2&#xA;&#x9;&#x9;c0.6-2.6,0.9-5.2,0.9-7.7c0-1.5-0.1-3-0.3-4.5c0.1,0,0.2,0,0.3,0c0.2,0,0.3,0,0.5,0c0.4,0,0.9-0.1,1.3-0.1c0.2,0,0.4,0,0.6,0&#xA;&#x9;&#x9;c0.5,0,1-0.1,1.5-0.1c0.1,0,0.2,0,0.3,0c0.6-0.1,1.2-0.1,1.8-0.2c0.2,0,0.3,0,0.5-0.1c0.4-0.1,0.9-0.1,1.3-0.2c0.2,0,0.4,0,0.5-0.1&#xA;&#x9;&#x9;c0.5-0.1,0.9-0.1,1.4-0.2c0.1,0,0.2,0,0.4-0.1c0.6-0.1,1.1-0.2,1.7-0.3c0.1,0,0.2,0,0.4-0.1c0.4-0.1,0.9-0.2,1.3-0.3&#xA;&#x9;&#x9;c0.2,0,0.3-0.1,0.5-0.1c0.4-0.1,0.8-0.2,1.2-0.3c0.1,0,0.3-0.1,0.4-0.1c0.5-0.1,1.1-0.2,1.6-0.4c0.1,0,0.1,0,0.2-0.1&#xA;&#x9;&#x9;c0.4-0.1,0.9-0.2,1.3-0.4c0.1,0,0.3-0.1,0.4-0.1c0.4-0.1,0.7-0.2,1.1-0.3c0.1,0,0.3-0.1,0.4-0.1c0.9-0.3,1.8-0.6,2.7-1&#xA;&#x9;&#x9;c0.1-0.1,0.2-0.1,0.4-0.2c0.3-0.1,0.6-0.3,0.9-0.4c0.1-0.1,0.2-0.1,0.4-0.2c0.3-0.2,0.6-0.3,1-0.5c0,0,0.1,0,0.1-0.1v14.1&#xA;&#x9;&#x9;C102,59.1,90.2,64.4,67.3,64.5z M74.2,43.3L74.2,43.3l-1.4,0c-3.1,0.1-6-1.6-7.6-4.3c-1.6-2.7-1.5-6,0.1-8.7l1.1-1.9l-0.6-0.6&#xA;&#x9;&#x9;c0.1,0,0.2,0,0.3,0c23.7,0,35.9,5.5,35.9,7.8C102,37.4,93.7,42.1,74.2,43.3z\"></path>\n</g>\n</svg>",
    "firmware_archive": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" id=\"Слой_1\" data-name=\"Слой 1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 128 128\"><title>archive-icon</title><path d=\"M108.8,40.32,93.72,17a1.32,1.32,0,0,0-1.13-.62H35.41a1.32,1.32,0,0,0-1.13.62L19.2,40.32a4.2,4.2,0,0,0-.67,2.29v65.68a3.37,3.37,0,0,0,3.37,3.38h84.2a3.37,3.37,0,0,0,3.37-3.38V42.61A4.2,4.2,0,0,0,108.8,40.32Zm-5,64.34a1.34,1.34,0,0,1-1.34,1.34H25.54a1.34,1.34,0,0,1-1.34-1.34V58.6a1.34,1.34,0,0,1,1.34-1.34h76.92a1.34,1.34,0,0,1,1.34,1.34Zm0-54.42a1.34,1.34,0,0,1-1.34,1.35H25.54a1.34,1.34,0,0,1-1.34-1.35v-6A4.2,4.2,0,0,1,24.87,42l12.5-19.37A1.32,1.32,0,0,1,38.49,22h51a1.32,1.32,0,0,1,1.12.62L103.13,42a4.2,4.2,0,0,1,.67,2.29Z\"></path><path d=\"M93.6,39.38H55a1.35,1.35,0,0,1-1.13-.61l-1.29-2a1.34,1.34,0,0,0-1.12-.61H33.63a1.34,1.34,0,0,0-1.34,1.34v8.76a2.83,2.83,0,0,0,2.83,2.84h0A2.83,2.83,0,0,0,38,46.28V43.19a1.35,1.35,0,0,1,1.35-1.34h9.05a1.34,1.34,0,0,1,1.12.61l1.29,2a1.35,1.35,0,0,0,1.13.61h36a1.34,1.34,0,0,1,1.34,1.34h0A2.73,2.73,0,0,0,92,49.12h.11a2.84,2.84,0,0,0,2.83-2.84V40.72A1.34,1.34,0,0,0,93.6,39.38Z\"></path><path d=\"M40.39,34.23h1.14a2.48,2.48,0,0,0,2.3-1.79,1.33,1.33,0,0,1,1.26-.89h6.4a1.33,1.33,0,0,1,1.14.63l1.17,1.89a1.35,1.35,0,0,0,1.14.64H84.69a2.5,2.5,0,0,0,2.5,2.5h.68a2.5,2.5,0,0,0,2.5-2.5V30.38A1.34,1.34,0,0,0,89,29H58.1A1.35,1.35,0,0,1,57,28.4l-1.17-1.89a1.35,1.35,0,0,0-1.14-.63H39.42a1.34,1.34,0,0,0-1.34,1.34v4.7A2.31,2.31,0,0,0,40.39,34.23Z\"></path><path d=\"M79.39,78.43a2.84,2.84,0,0,0-4.11-.11l-8.22,8.22V67.17a1.4,1.4,0,0,0-1.4-1.4H62.79a1.4,1.4,0,0,0-1.4,1.4V86.54l-8.21-8.22a2.84,2.84,0,0,0-4.37.44,2.9,2.9,0,0,0,.43,3.65l15,15L79.17,82.45A3,3,0,0,0,79.39,78.43Z\"></path></svg>",
    "vehicle_support": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n<g>\n&#x9;<path d=\"M44.9,96.8c4.1,2.4,6.6,6.8,6.5,11.5l-0.1,2.2l2.1,0.5c3.6,0.8,7.1,1.2,10.7,1.2c3.4,0,6.8-0.4,10.2-1.1l2.2-0.5l-0.1-2.2&#xA;&#x9;&#x9;c-0.1-4.7,2.4-9.2,6.5-11.5c4.1-2.4,9.2-2.3,13.3,0.2l1.9,1.2l1.5-1.6c4.7-5.1,8.2-11.2,10.4-18l0.7-2.1l-1.9-1.1&#xA;&#x9;&#x9;c-4.1-2.3-6.7-6.6-6.7-11.3c0-4.8,2.6-9.1,6.8-11.4l1.9-1.1l-0.7-2.1c-1.1-3.4-2.5-6.7-4.2-9.7c-1.7-2.9-3.8-5.8-6.1-8.4l-1.5-1.6&#xA;&#x9;&#x9;L96.3,31c-4,2.5-9.1,2.5-13.2,0.2c-4.1-2.4-6.6-6.8-6.5-11.5l0.1-2.2L74.5,17c-7-1.6-14-1.6-20.8-0.1l-2.2,0.5l0.1,2.2&#xA;&#x9;&#x9;c0.1,4.7-2.4,9.2-6.5,11.5c-4.1,2.4-9.2,2.3-13.3-0.2L30,29.7l-1.5,1.6c-4.7,5.1-8.2,11.2-10.4,18l-0.7,2.1l1.9,1.1&#xA;&#x9;&#x9;c4.1,2.3,6.7,6.6,6.7,11.3c0,4.8-2.6,9.1-6.8,11.4l-1.9,1.1l0.7,2.1c1.1,3.4,2.5,6.7,4.2,9.7c1.7,2.9,3.8,5.8,6.1,8.4l1.5,1.6&#xA;&#x9;&#x9;l1.9-1.1C35.8,94.5,40.8,94.4,44.9,96.8z M31,91.3c-1.6-1.9-2.9-3.8-4.1-5.9c-1.2-2.1-2.2-4.3-3.1-6.7c4.8-3.4,7.6-8.9,7.6-14.9&#xA;&#x9;&#x9;c0-5.9-2.9-11.4-7.6-14.8c1.7-4.6,4.1-8.9,7.2-12.6c5.3,2.4,11.6,2.2,16.7-0.8c5.1-3,8.5-8.2,9.1-14c4.8-0.8,9.6-0.8,14.5,0.1&#xA;&#x9;&#x9;c0.6,5.8,3.9,11.1,9.1,14c5.1,3,11.3,3.2,16.6,0.8c1.6,1.9,2.9,3.8,4.1,5.9c1.2,2.1,2.2,4.3,3.1,6.7c-4.8,3.4-7.6,8.9-7.6,14.9&#xA;&#x9;&#x9;c0,5.9,2.9,11.4,7.6,14.8c-1.7,4.6-4.1,8.9-7.2,12.6c-5.3-2.4-11.6-2.2-16.7,0.8c-5.1,3-8.5,8.2-9.1,14c-4.8,0.8-9.6,0.8-14.5-0.1&#xA;&#x9;&#x9;c-0.6-5.8-3.9-11.1-9.1-14C42.5,89.2,36.3,89,31,91.3z\"></path>\n&#x9;<path d=\"M79.3,61.2c-1,1-1.6,2.5-1.6,4c0,3,2.4,5.4,5.4,5.4c0,0,0,0,0,0c3,0,5.3-2.4,5.3-5.5c0-3.1-2.3-5.5-5.3-5.5&#xA;&#x9;&#x9;C81.7,59.6,80.4,60.2,79.3,61.2z M85.5,65.1c0,1.4-1,2.4-2.3,2.4c0,0,0,0,0,0c-1.3,0-2.3-1.1-2.3-2.4c0-0.7,0.2-1.3,0.7-1.8&#xA;&#x9;&#x9;c0.4-0.4,1-0.7,1.7-0.7c0,0,0,0,0,0C84.5,62.7,85.5,63.7,85.5,65.1z\"></path>\n&#x9;<path d=\"M44.8,70.6c3,0,5.3-2.4,5.4-5.4c0-3.1-2.2-5.5-5.3-5.5c-1.5,0-2.8,0.5-3.9,1.5c-1,1-1.6,2.5-1.6,4&#xA;&#x9;&#x9;C39.5,68.2,41.8,70.6,44.8,70.6C44.8,70.6,44.8,70.6,44.8,70.6z M42.5,65.1c0-0.7,0.2-1.4,0.7-1.8c0.4-0.4,1-0.6,1.6-0.6&#xA;&#x9;&#x9;c0,0,0,0,0,0c1.3,0,2.3,1.1,2.3,2.5v0c0,1.4-1,2.4-2.3,2.4c0,0,0,0,0,0C43.5,67.5,42.5,66.5,42.5,65.1z\"></path>\n&#x9;<path d=\"M42,88.3c0.8,0.5,1.7,0.7,2.7,0.7c1,0,2-0.3,2.9-0.8c1.9-1,2.9-2.8,2.9-5.1c0-1.1,0-2.1,0-3.2h27.1c0,1,0,2,0,2.9&#xA;&#x9;&#x9;c0,1.9,0.7,3.5,1.8,4.6c1.1,1,2.4,1.5,4,1.5c3.3-0.1,5.6-2.6,5.6-6.2c0-1,0-1.9,0-2.9h4.5V61.5l-0.1-0.2c-1-3.1-2.8-5-5.4-5.9&#xA;&#x9;&#x9;c0-0.1-0.1-0.2-0.1-0.3c-0.8-2-1.6-4.1-2.4-6.1l-1.7-4.3c-1.5-3.8-4-5.6-8-5.6c-7.6,0-15.4,0-23.8,0c-4,0-6.5,1.7-8,5.6&#xA;&#x9;&#x9;c-0.5,1.2-0.9,2.3-1.4,3.5c-0.9,2.3-1.7,4.5-2.7,6.7c-0.1,0.2-0.4,0.5-0.6,0.6c-2.4,1-4,2.9-4.9,5.8l-0.1,18.5H39c0,0,0,0,0,0&#xA;&#x9;&#x9;c0,1.1,0,2.3,0,3.5C39.1,85.5,40.2,87.3,42,88.3z M37.6,76.8V62c0.7-1.9,1.6-3.1,3.1-3.7c0.9-0.4,1.8-1.3,2.2-2.3&#xA;&#x9;&#x9;c0.9-2.3,1.8-4.5,2.7-6.8c0.4-1.2,0.9-2.3,1.3-3.5c1-2.7,2.4-3.6,5.2-3.6c8.4,0,16.1,0,23.8,0c2.7,0,4.1,1,5.2,3.6l1.7,4.3&#xA;&#x9;&#x9;c0.8,2,1.6,4.1,2.4,6.1c0.3,0.8,0.8,1.7,1.7,2c1.8,0.5,2.9,1.8,3.7,3.9v14.7H86l0,3c0,1,0,2,0,3c0,0.9-0.3,3.1-2.6,3.2&#xA;&#x9;&#x9;c-0.7,0-1.4-0.2-1.8-0.7c-0.6-0.5-0.9-1.4-0.9-2.5c0-1,0-2,0-3l0-3H47.4l0,3c0,1.1,0,2.1,0,3.2c0,1.2-0.4,2-1.3,2.5&#xA;&#x9;&#x9;c-0.8,0.5-1.8,0.5-2.6,0.1c-0.8-0.5-1.3-1.3-1.4-2.4c-0.1-1.1,0-2.2,0-3.3c0-0.5,0-1.1,0-1.6v-1.5H37.6z\"></path>\n&#x9;<path d=\"M82.5,53c-0.9-2.5-1.8-4.9-2.8-7.3c-0.4-1.1-1.4-2.3-3.9-2.3c0,0,0,0,0,0c-7.8,0-15.6,0-23.5,0c0,0,0,0,0,0&#xA;&#x9;&#x9;c-2.2,0-3.6,1-4.3,3.1c-0.4,1.1-0.8,2.2-1.2,3.3l-3,8.2h40.6L82.5,53z M48.1,54.9l1.5-4.1c0.4-1.1,0.8-2.2,1.2-3.4&#xA;&#x9;&#x9;c0.3-0.8,0.6-1,1.4-1c0,0,0,0,0,0c7.8,0,15.7,0,23.5,0c0,0,0,0,0,0c0.9,0,1,0.2,1.1,0.4c0.9,2.4,1.8,4.8,2.8,7.2l0.3,0.8H48.1z\"></path>\n</g>\n</svg>",
    "orders": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n<g>\n&#x9;<path d=\"M101.1,29.6H39.7v0.2l-1.8-3.7c-2.3-4.5-6.8-7.4-11.9-7.4h-4.9v6.1H26c2.7,0,5.2,1.5,6.4,4l16.9,34.1l-6.3,8.5&#xA;&#x9;&#x9;c-2.2,2.9-2.5,6.8-0.9,10.1c1.6,3.3,4.9,5.3,8.6,5.3h50.5v-6.1H50.8c-1.4,0-2.5-0.7-3.1-1.9C47,77.5,47.2,76.1,48,75l5.7-7.7h29.9&#xA;&#x9;&#x9;c4.1,0,7.9-2.2,9.8-5.8l12.8-23.3c1-1.8,1-4-0.1-5.7C105.1,30.7,103.2,29.6,101.1,29.6z M88,58.6c-0.9,1.6-2.6,2.6-4.5,2.6H55.3&#xA;&#x9;&#x9;L42.7,35.7h57.9L88,58.6z\"></path>\n&#x9;<path d=\"M50.6,88.8c-5.7,0-10.3,4.6-10.3,10.3s4.6,10.3,10.3,10.3c5.7,0,10.3-4.6,10.3-10.3S56.2,88.8,50.6,88.8z M50.6,103.2&#xA;&#x9;&#x9;c-2.3,0-4.2-1.9-4.2-4.2c0-2.3,1.9-4.2,4.2-4.2c2.3,0,4.2,1.9,4.2,4.2C54.7,101.3,52.9,103.2,50.6,103.2z\"></path>\n&#x9;<path d=\"M93.5,88.8c-5.7,0-10.3,4.6-10.3,10.3s4.6,10.3,10.3,10.3c5.7,0,10.3-4.6,10.3-10.3S99.1,88.8,93.5,88.8z M93.5,103.2&#xA;&#x9;&#x9;c-2.3,0-4.2-1.9-4.2-4.2c0-2.3,1.9-4.2,4.2-4.2s4.2,1.9,4.2,4.2C97.6,101.3,95.8,103.2,93.5,103.2z\"></path>\n</g>\n</svg>",
    "feedback": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n<g>\n&#x9;<path d=\"M100.6,23.1H29.1c-6.7,0-12.2,5.5-12.2,12.2v49.5c0,6.7,5.5,12.2,12.2,12.2h12.1v11.9c0,2.3,1.4,4.4,3.6,5.3&#xA;&#x9;&#x9;c0.7,0.3,1.5,0.4,2.2,0.4c1.5,0,3-0.6,4.1-1.7l16-16h33.6c6.7,0,12.2-5.5,12.2-12.2V35.3C112.9,28.6,107.4,23.1,100.6,23.1z&#xA;&#x9;&#x9; M107.5,84.8c0,3.8-3.1,6.9-6.9,6.9H64.9l-17.6,17.6c-0.1,0.1-0.2,0.2-0.5,0.1c-0.3-0.1-0.3-0.3-0.3-0.4V91.6H29.1&#xA;&#x9;&#x9;c-3.8,0-6.9-3.1-6.9-6.9V35.3c0-3.8,3.1-6.9,6.9-6.9h71.6c3.8,0,6.9,3.1,6.9,6.9V84.8z\"></path>\n&#x9;<path d=\"M80.5,57.6H44.2V70c0,8.5,6.9,15.3,15.3,15.3h10.3c8.5,0,15.3-6.9,15.3-15.3v-7.6C85.2,59.8,83.1,57.6,80.5,57.6z M79.9,70&#xA;&#x9;&#x9;c0,5.5-4.5,10-10,10H59.6c-5.5,0-10-4.5-10-10v-7h30.3V70z\"></path>\n&#x9;<path d=\"M64.6,54.8c5.9,0,10.7-4.8,10.7-10.7s-4.8-10.7-10.7-10.7s-10.7,4.8-10.7,10.7S58.7,54.8,64.6,54.8z M64.6,38.8&#xA;&#x9;&#x9;c3,0,5.4,2.4,5.4,5.4s-2.4,5.4-5.4,5.4s-5.4-2.4-5.4-5.4S61.7,38.8,64.6,38.8z\"></path>\n</g>\n</svg>",
    "articles": "<svg fill=\"currentColor\" width=\"100%\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" id=\"Слой_1\" xmlns=\"http://www.w3.org/2000/svg\" x=\"0px\" y=\"0px\" viewBox=\"0 0 128 128\" style=\"enable-background:new 0 0 128 128;\" xml:space=\"preserve\">\n<g>\n&#x9;<path d=\"M93.9,15.6H34.1c-5.2,0-9.4,4.2-9.4,9.4v40h5.3v-40c0-2.3,1.8-4.1,4.1-4.1h59.7c2.3,0,4.1,1.8,4.1,4.1v80.1&#xA;&#x9;&#x9;c0,2.3-1.8,4.1-4.1,4.1H34.1c-2.3,0-4.1-1.8-4.1-4.1V65.1h-5.3v40.1c0,5.2,4.2,9.4,9.4,9.4h59.7c5.2,0,9.4-4.2,9.4-9.4V25.1&#xA;&#x9;&#x9;C103.3,19.9,99.1,15.6,93.9,15.6z\"></path>\n&#x9;<rect x=\"38.5\" y=\"32.5\" width=\"50.9\" height=\"5.3\"></rect>\n&#x9;<rect x=\"38.5\" y=\"45\" width=\"50.9\" height=\"5.3\"></rect>\n&#x9;<rect x=\"38.5\" y=\"57.5\" width=\"50.9\" height=\"5.3\"></rect>\n&#x9;<rect x=\"38.5\" y=\"70\" width=\"50.9\" height=\"5.3\"></rect>\n&#x9;<rect x=\"38.5\" y=\"82.5\" width=\"50.9\" height=\"5.3\"></rect>\n</g>\n</svg>"
  };
})();
})(); } catch (e) { __ds_ns.__errors.push({ path: "res/icons/main-icons.js", error: String((e && e.message) || e) }); }

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
    home: 'features/home/Home.html',
    'home-tablet': 'features/home/HomeTablet.html',
    'home-desktop': 'features/home/HomeDesktop.html',
    'home-parts': 'features/home/HomeParts.html',
    'app-update': 'features/app-update/AppUpdate.html',
    index: 'index.html',
    'index-tablet': 'index-tablet.html',
    'index-desktop': 'index-desktop.html',
    'device-select': 'features/device-select/DeviceSelect.html',
    'device-select-tablet': 'features/device-select/DeviceSelectTablet.html',
    'device-select-desktop': 'features/device-select/DeviceSelectDesktop.html',
    'device-select-states': 'features/device-select/DeviceSelectStates.html',
    calculator: 'features/calculator/Calculator.html',
    'calculator-tablet': 'features/calculator/CalculatorTablet.html',
    'calculator-desktop': 'features/calculator/CalculatorDesktop.html',
    auth: 'features/auth/Auth.html',
    'auth-tablet': 'features/auth/AuthTablet.html',
    'auth-desktop': 'features/auth/AuthDesktop.html',
    'auth-states': 'features/auth/AuthStates.html',
    'auth-flow': 'features/auth/AuthFlow.html',
    'support-chat': 'features/support-chat/SupportChat.html',
    'support-chat-tablet': 'features/support-chat/SupportChatTablet.html',
    'support-chat-desktop': 'features/support-chat/SupportChatDesktop.html'
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
