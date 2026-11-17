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
/** Style scale (UI kit): name → [size var, line var, weight]. */
const SCALE = {
  accent1: ["--t-accent1-size", "--t-accent1-line", 700],
  accent2: ["--t-accent2-size", "--t-accent2-line", 700],
  accent3: ["--t-accent3-size", "--t-accent3-line", 700],
  title1: ["--t-title1-size", "--t-title1-line", 700],
  title2: ["--t-title2-size", "--t-title2-line", 700],
  headline1: ["--t-headline1-size", "--t-headline1-line", 700],
  headline2: ["--t-headline2-size", "--t-headline2-line", 700],
  headline3: ["--t-headline3-size", "--t-headline3-line", 700],
  paragraph1: ["--t-paragraph1-size", "--t-paragraph1-line", 500],
  paragraph2: ["--t-paragraph2-size", "--t-paragraph2-line", 500],
  paragraph3: ["--t-paragraph3-size", "--t-paragraph3-line", 500],
  additional0: ["--t-additional0-size", "--t-additional0-line", 700],
  additional1: ["--t-additional1-size", "--t-additional1-line", 700],
  additional2: ["--t-additional2-size", "--t-additional2-line", 700]
};

/** Legacy M3 role → scale style (kept so existing screens keep working). */
const LEGACY = {
  displayLarge: "accent2",
  displayMedium: "accent3",
  displaySmall: "title1",
  headlineLarge: "title1",
  headlineMedium: "title2",
  headlineSmall: "headline1",
  titleLarge: "headline1",
  titleMedium: "headline2",
  titleSmall: "headline3",
  bodyLarge: "paragraph1",
  bodyMedium: "paragraph2",
  bodySmall: "paragraph3",
  labelLarge: "headline3",
  labelMedium: "additional0",
  labelSmall: "additional1"
};

/**
 * CmpText — every piece of text. Pick a style from the 15-style scale
 * (`accent1…additional2`); legacy M3 names map onto it. Size/line/weight
 * are owned by the style — only color and align may change.
 */
function CmpText({
  children,
  variant = "paragraph2",
  color = "currentColor",
  weight,
  align,
  maxLines,
  uppercase = false,
  as: Tag = "span",
  style,
  ...rest
}) {
  const key = SCALE[variant] ? variant : LEGACY[variant] || "paragraph2";
  const [sizeVar, lineVar, defWeight] = SCALE[key];
  const clamp = maxLines === 1 ? {
    display: "block",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    minWidth: 0,
    maxWidth: "100%"
  } : maxLines ? {
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
      textTransform: uppercase ? "uppercase" : undefined,
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
const { useState } = React;
/**
 * Internal helper: interaction state for every pressable Cmp*.
 * Kit behaviour: press = scale 0.96 (springy) + 10% ripple overlay.
 * Returns handlers, overlay `opacity` (hover .06, focus .08, pressed .10),
 * `pressStyle` (transform + transition) and `focusRing` (box-shadow for
 * keyboard focus, 3px border-focus). Not a public component.
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
    onTouchStart: () => setActive(true),
    onTouchEnd: () => setActive(false),
    onFocus: e => setFocus(e?.target?.matches ? e.target.matches(":focus-visible") : true),
    onBlur: () => setFocus(false)
  };
  const opacity = active ? 0.1 : focus ? 0.08 : hover ? 0.06 : 0;
  const pressStyle = {
    transform: active ? "scale(var(--press-scale))" : "scale(1)",
    transition: "transform var(--motion-press), background var(--motion-fast), color var(--motion-fast), border-color var(--motion-fast)"
  };
  const focusRing = focus ? "0 0 0 var(--w-focus-width) var(--border-focus)" : undefined;
  return {
    hover,
    focus,
    active,
    opacity,
    handlers,
    pressStyle,
    focusRing
  };
}
Object.assign(__ds_scope, { useStateLayer });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/app-kit/useStateLayer.js", error: String((e && e.message) || e) }); }

// components/buttons/CmpButton.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const TYPES = {
  // Ink fill (navy in light, blue-white in dark) — main action.
  Primary: {
    bg: "var(--background-surface-primary)",
    fg: "var(--foreground-on-light-primary-default)",
    ripple: "var(--foreground-on-light-primary-default)"
  },
  // Blue accent — special emphasis (start, flash, connect).
  Accent: {
    bg: "var(--brand-primary-default)",
    fg: "var(--on-brand)",
    ripple: "#FFFFFF"
  },
  // Translucent ink step + 1px border.
  Secondary: {
    bg: "var(--background-surface-highlight)",
    fg: "var(--foreground-on-dark-primary-default)",
    border: "var(--border-light)",
    ripple: "var(--foreground-on-dark-primary-default)"
  },
  // No fill.
  Tertiary: {
    bg: "transparent",
    fg: "var(--foreground-on-dark-primary-default)",
    ripple: "var(--foreground-on-dark-primary-default)"
  },
  // Destructive.
  Danger: {
    bg: "var(--error-default)",
    fg: "#FFFFFF",
    ripple: "#FFFFFF"
  }
};
const SIZES = {
  S: {
    h: "var(--h-button-s)",
    fs: "var(--t-headline3-size)",
    pad: 20
  },
  M: {
    h: "var(--h-button-m)",
    fs: "var(--t-headline3-size)",
    pad: 24
  },
  L: {
    h: "var(--h-button-l)",
    fs: "var(--t-headline2-size)",
    pad: 24
  },
  XL: {
    h: "var(--h-button-xl)",
    fs: "var(--t-headline1-size)",
    pad: 32
  }
};

/**
 * CmpButton — the one button. type: Primary | Accent | Secondary | Tertiary | Danger;
 * size: S 48 · M 52 · L 56 · XL 64. Label UPPERCASE, style by size: S/M headline3 16 · L headline2 19 · XL headline1 24.
 * Press: scale .96 + 10% ripple. loading: label hidden (width kept) + spinner.
 */
function CmpButton({
  text,
  onClick,
  disabled = false,
  loading = false,
  icon,
  type = "Primary",
  size = "L",
  fullWidth = false,
  children,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const t = TYPES[type] || TYPES.Primary;
  const s = SIZES[size] || SIZES.L;
  const label = text ?? children;
  const off = disabled || loading;
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: off ? undefined : onClick,
    disabled: disabled
  }, handlers, {
    style: {
      position: "relative",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      gap: "var(--space-8)",
      height: s.h,
      minWidth: 96,
      width: fullWidth ? "100%" : undefined,
      padding: `0 ${s.pad}px`,
      border: loading ? "1px solid var(--border-highlight)" : t.border ? `1px solid ${disabled ? "transparent" : t.border}` : "none",
      borderRadius: "var(--r-buttons-radius)",
      background: loading ? "transparent" : disabled ? type === "Tertiary" ? "transparent" : "var(--background-surface-ultra-highlight)" : t.bg,
      color: disabled ? "var(--foreground-on-dark-primary-disabled)" : loading ? "var(--foreground-on-dark-primary-default)" : t.fg,
      fontFamily: "var(--font-sans)",
      fontSize: s.fs,
      lineHeight: 1,
      fontWeight: 700,
      textTransform: "uppercase",
      letterSpacing: "0.03em",
      cursor: off ? "default" : "pointer",
      overflow: "hidden",
      boxShadow: focusRing,
      ...(off ? {} : pressStyle),
      ...style
    }
  }, rest), !off && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: t.ripple,
      opacity,
      pointerEvents: "none"
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: "var(--space-8)",
      opacity: loading ? 0 : 1,
      whiteSpace: "nowrap",
      minWidth: 0,
      maxWidth: "100%",
      overflow: "hidden",
      textOverflow: "ellipsis"
    }
  }, icon && /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 24,
    color: "currentColor"
  }), label), loading && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      width: 26,
      height: 26,
      borderRadius: "50%",
      border: "3px solid var(--border-light)",
      borderTopColor: "var(--brand-primary-default)",
      animation: "cmp-spin 0.9s linear infinite"
    }
  }), loading && /*#__PURE__*/React.createElement("style", null, "@keyframes cmp-spin{to{transform:rotate(360deg)}}"));
}
Object.assign(__ds_scope, { CmpButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpFloatingActionButton.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const SIZES = {
  Small: [48, 24],
  Default: [56, 24],
  Large: [96, 36]
};

/** CmpFloatingActionButton — blue accent circle, no shadow, press scale .96. */
function CmpFloatingActionButton({
  icon = "add",
  onClick,
  size = "Default",
  contentDescription,
  children,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const [box, ic] = SIZES[size] || SIZES.Default;
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    "aria-label": contentDescription,
    onClick: onClick
  }, handlers, {
    style: {
      position: "relative",
      width: box,
      height: box,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      border: "none",
      borderRadius: "var(--r-round-radius)",
      background: "var(--brand-primary-default)",
      color: "var(--on-brand)",
      cursor: "pointer",
      overflow: "hidden",
      boxShadow: focusRing,
      ...pressStyle,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "#FFFFFF",
      opacity,
      pointerEvents: "none"
    }
  }), children ?? /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: ic,
    color: "currentColor"
  }));
}
Object.assign(__ds_scope, { CmpFloatingActionButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpFloatingActionButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpIconButton.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpIconButton — round icon action, 48 (Standard) or 56 (Glass, top bar).
 * Glass = translucent ink fill + 1px light border + backdrop blur (kit TopBarIcon).
 * Press: scale .96 + ripple.
 */
function CmpIconButton({
  icon,
  onClick,
  contentDescription,
  disabled = false,
  selected = false,
  variant = "Standard",
  tint,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const glass = variant === "Glass";
  const size = glass ? 56 : 48;
  const fg = disabled ? "var(--foreground-on-dark-primary-disabled)" : selected ? "var(--foreground-on-light-primary-default)" : tint ?? "var(--foreground-on-dark-primary-default)";
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    "aria-label": contentDescription,
    title: contentDescription,
    onClick: disabled ? undefined : onClick,
    disabled: disabled
  }, handlers, {
    style: {
      position: "relative",
      width: size,
      height: size,
      flexShrink: 0,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      borderRadius: "var(--r-icon-radius)",
      border: glass ? "var(--w-icon-width) solid var(--border-light)" : "none",
      background: selected ? "var(--background-surface-primary)" : glass ? "var(--background-glass)" : "transparent",
      backdropFilter: glass ? "blur(var(--blur-glass))" : undefined,
      WebkitBackdropFilter: glass ? "blur(var(--blur-glass))" : undefined,
      color: fg,
      cursor: disabled ? "default" : "pointer",
      overflow: "hidden",
      padding: 0,
      boxShadow: focusRing,
      ...(disabled ? {} : pressStyle),
      ...style
    }
  }, rest), !disabled && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "currentColor",
      opacity,
      pointerEvents: "none"
    }
  }), /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 24,
    color: "currentColor"
  }));
}
Object.assign(__ds_scope, { CmpIconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpIconButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpOutlinedButton.jsx
try { (() => {
const CmpButton = __ds_scope.CmpButton;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/** @deprecated → CmpButton type="Secondary". Kept for existing screens. */
function CmpOutlinedButton({
  size = "L",
  ...props
}) {
  return /*#__PURE__*/React.createElement(CmpButton, _extends({
    type: "Secondary",
    size: size
  }, props));
}
Object.assign(__ds_scope, { CmpOutlinedButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpOutlinedButton.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpTextButton.jsx
try { (() => {
const CmpButton = __ds_scope.CmpButton;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/** @deprecated → CmpButton type="Tertiary". Kept for existing screens. */
function CmpTextButton({
  size = "S",
  style,
  ...props
}) {
  return /*#__PURE__*/React.createElement(CmpButton, _extends({
    type: "Tertiary",
    size: size,
    style: {
      minWidth: 64,
      padding: "0 16px",
      ...style
    }
  }, props));
}
Object.assign(__ds_scope, { CmpTextButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpTextButton.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpBadge.jsx
try { (() => {

/** CmpBadge — pill counter (additional1), brand blue by default; dot when no count. */
function CmpBadge({
  count,
  containerColor = "var(--brand-primary-default)",
  contentColor = "var(--on-brand)",
  style
}) {
  const dot = count === undefined || count === null || count === "";
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      boxSizing: "border-box",
      minWidth: dot ? 8 : 20,
      height: dot ? 8 : 20,
      padding: dot ? 0 : "0 6px",
      borderRadius: 9999,
      background: containerColor,
      color: contentColor,
      fontFamily: "var(--font-sans)",
      fontSize: "var(--t-additional1-size)",
      fontWeight: 700,
      lineHeight: 1,
      ...style
    }
  }, dot ? null : count);
}
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
      top: 0,
      right: 0,
      transform: "translate(35%, -35%)"
    }
  }, badge));
}
Object.assign(__ds_scope, { CmpBadge, CmpBadgedBox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpBadge.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpBottomSheet.jsx
try { (() => {

/**
 * CmpBottomSheet — kit BottomSheet: frosted surface (blur) over the overlay,
 * top radius r-sheet, 1px top border, 40×4 handle. Max-width 640 on wide windows.
 */
function CmpBottomSheet({
  open = true,
  onDismissRequest,
  children,
  containerColor,
  style
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    onClick: onDismissRequest,
    style: {
      position: "absolute",
      inset: 0,
      zIndex: 50,
      background: "var(--background-overlay)",
      display: "flex",
      alignItems: "flex-end",
      justifyContent: "center"
    }
  }, /*#__PURE__*/React.createElement("div", {
    onClick: e => e.stopPropagation(),
    style: {
      width: "100%",
      maxWidth: 640,
      boxSizing: "border-box",
      maxHeight: "90%",
      overflowY: "auto",
      background: containerColor ?? "var(--surface-container-lowest)",
      color: "var(--foreground-on-dark-primary-default)",
      borderTop: "1px solid var(--border-light)",
      borderRadius: "var(--r-sheet-radius) var(--r-sheet-radius) 0 0",
      padding: "var(--space-12) var(--screen-padding-h) var(--space-32)",
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 40,
      height: 4,
      borderRadius: 2,
      background: "var(--background-surface-tertiary)",
      margin: "0 auto var(--space-20)"
    }
  }), children));
}
Object.assign(__ds_scope, { CmpBottomSheet });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpBottomSheet.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpDialog.jsx
try { (() => {
const CmpIcon = __ds_scope.CmpIcon;
const CmpText = __ds_scope.CmpText;
/**
 * CmpDialog — kit Modal: centered content over a blurred overlay.
 * Card surface, 1px border, radius r-dialog. Title = title2, text = headline2 @ 60%.
 * Buttons stack full-width (confirm on top) — big targets.
 * Overlay fills the nearest positioned ancestor.
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
      background: "var(--background-overlay)",
      backdropFilter: "blur(var(--blur-sheet))",
      WebkitBackdropFilter: "blur(var(--blur-sheet))",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "var(--space-24)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    onClick: e => e.stopPropagation(),
    style: {
      width: "100%",
      maxWidth: 440,
      boxSizing: "border-box",
      background: "var(--surface-container-lowest)",
      color: "var(--foreground-on-dark-primary-default)",
      border: "1px solid var(--border-highlight)",
      borderRadius: "var(--r-dialog-radius)",
      padding: "var(--space-32) var(--space-24) var(--space-24)",
      boxShadow: "var(--elevation-3)",
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-16)",
      alignItems: "center",
      textAlign: "center",
      ...style
    }
  }, icon && /*#__PURE__*/React.createElement("span", {
    style: {
      width: 64,
      height: 64,
      borderRadius: "50%",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      background: "var(--background-surface-highlight)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 32,
    color: "var(--foreground-on-dark-primary-default)"
  })), title && /*#__PURE__*/React.createElement(CmpText, {
    variant: "title2",
    as: "div"
  }, title), text && /*#__PURE__*/React.createElement(CmpText, {
    variant: "headline2",
    as: "div",
    color: "var(--foreground-on-dark-secondary-default)"
  }, text), (confirmButton || dismissButton) && /*#__PURE__*/React.createElement("div", {
    style: {
      alignSelf: "stretch",
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-8)",
      marginTop: "var(--space-8)"
    }
  }, confirmButton && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column"
    }
  }, confirmButton), dismissButton && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column"
    }
  }, dismissButton))));
}
Object.assign(__ds_scope, { CmpDialog });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpDialog.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpSnackbar.jsx
try { (() => {
const CmpIcon = __ds_scope.CmpIcon;
/**
 * CmpSnackbar — kit TopToast: ink-filled bar (on-light text, headline3),
 * radius r-snackbar, min-height 56. Place it at the TOP of the screen.
 * `tone` adds a leading status icon.
 */
const TONES = {
  success: ["check_circle", "var(--success-default)"],
  error: ["error", "var(--error-default)"],
  info: ["info", "var(--brand-secondary-default)"]
};
function CmpSnackbar({
  message,
  actionLabel,
  onActionClick,
  withDismissAction = false,
  onDismiss,
  tone,
  style
}) {
  const t = TONES[tone];
  return /*#__PURE__*/React.createElement("div", {
    role: "status",
    style: {
      display: "flex",
      alignItems: "center",
      gap: "var(--space-12)",
      minHeight: 56,
      boxSizing: "border-box",
      padding: "var(--space-8) var(--space-8) var(--space-8) var(--space-16)",
      background: "var(--background-surface-primary)",
      color: "var(--foreground-on-light-primary-default)",
      borderRadius: "var(--r-snackbar-radius)",
      boxShadow: "var(--elevation-3)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--t-headline3-size)",
      lineHeight: "var(--t-headline3-line)",
      fontWeight: 700,
      ...style
    }
  }, t && /*#__PURE__*/React.createElement(CmpIcon, {
    icon: t[0],
    size: 24,
    color: t[1]
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      minWidth: 0,
      padding: "var(--space-8) 0"
    }
  }, message), actionLabel && /*#__PURE__*/React.createElement("button", {
    type: "button",
    onClick: onActionClick,
    style: {
      minHeight: 48,
      padding: "0 var(--space-12)",
      border: "none",
      background: "transparent",
      color: "inherit",
      fontFamily: "inherit",
      fontSize: "inherit",
      fontWeight: 700,
      textTransform: "uppercase",
      textDecoration: "underline",
      textUnderlineOffset: 3,
      cursor: "pointer"
    }
  }, actionLabel), withDismissAction && /*#__PURE__*/React.createElement("button", {
    type: "button",
    "aria-label": "\u0417\u0430\u043A\u0440\u044B\u0442\u044C",
    onClick: onDismiss,
    style: {
      width: 48,
      height: 48,
      border: "none",
      background: "transparent",
      color: "inherit",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: "pointer"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "close",
    size: 24,
    color: "currentColor"
  })));
}
Object.assign(__ds_scope, { CmpSnackbar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpSnackbar.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpCheckbox.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpCheckbox — kit CheckBox: box 24, radius r-checkbox, hit area 48.
 * Off = 1.5px ink-30 border. On/indeterminate = ink fill + on-light check.
 */
function CmpCheckbox({
  checked = false,
  indeterminate = false,
  onCheckedChange,
  disabled = false,
  style,
  ...rest
}) {
  const {
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const on = checked || indeterminate;
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    role: "checkbox",
    "aria-checked": indeterminate ? "mixed" : checked,
    disabled: disabled,
    onClick: () => !disabled && onCheckedChange && onCheckedChange(!checked)
  }, handlers, {
    style: {
      width: 48,
      height: 48,
      padding: 0,
      border: "none",
      background: "transparent",
      flexShrink: 0,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.4 : 1,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      width: 24,
      height: 24,
      boxSizing: "border-box",
      borderRadius: "var(--r-checkbox-radius)",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      background: on ? "var(--background-surface-primary)" : "transparent",
      border: on ? "none" : "1.5px solid var(--border-tertiary)",
      color: "var(--foreground-on-light-primary-default)",
      boxShadow: focusRing,
      ...(disabled ? {} : pressStyle)
    }
  }, on && /*#__PURE__*/React.createElement(CmpIcon, {
    icon: indeterminate ? "remove" : "check",
    size: 18,
    color: "currentColor"
  })));
}
Object.assign(__ds_scope, { CmpCheckbox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpCheckbox.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpDropdown.jsx
try { (() => {
const { useState } = React;
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpDropdown — select in the InputMain shell (72, translucent fill, 1px border).
 * Menu = card surface with 1px border, rows 56 (≥48 touch).
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
  const {
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      width: "100%",
      opacity: disabled ? 0.4 : 1,
      ...rootStyle
    }
  }, /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    disabled: disabled,
    onClick: () => setOpen(o => !o)
  }, handlers, {
    style: {
      width: "100%",
      minHeight: "var(--h-input)",
      padding: "0 var(--space-16) 0 var(--space-20)",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-12)",
      boxSizing: "border-box",
      background: open ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
      border: `1px solid ${open ? "var(--border-focus)" : "var(--border-light)"}`,
      borderRadius: "var(--r-inputs-radius)",
      color: selectedValue ? "var(--foreground-on-dark-primary-default)" : "var(--foreground-on-dark-secondary-default)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--t-headline2-size)",
      fontWeight: 700,
      textAlign: "left",
      cursor: disabled ? "default" : "pointer",
      boxShadow: focusRing
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      minWidth: 0,
      overflow: "hidden",
      textOverflow: "ellipsis",
      whiteSpace: "nowrap"
    }
  }, selectedValue ?? placeholder), /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      transform: open ? "rotate(180deg)" : "none",
      transition: "transform var(--motion-standard)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "expand_more",
    size: 32,
    color: "var(--foreground-on-dark-primary-default)"
  }))), open && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 0,
      right: 0,
      top: "calc(100% + 8px)",
      zIndex: 40,
      padding: "var(--space-8)",
      background: "var(--surface-container-lowest)",
      border: "1px solid var(--border-light)",
      borderRadius: "var(--r-card-small-radius)",
      boxShadow: "var(--elevation-3)",
      maxHeight: 320,
      overflowY: "auto"
    }
  }, options.map((opt, i) => {
    const sel = opt === selectedValue;
    return /*#__PURE__*/React.createElement("button", {
      key: i,
      type: "button",
      onClick: () => {
        setOpen(false);
        onOptionSelected && onOptionSelected(i, opt);
      },
      style: {
        width: "100%",
        minHeight: 56,
        padding: "0 var(--space-16)",
        display: "flex",
        alignItems: "center",
        gap: "var(--space-12)",
        border: "none",
        borderRadius: "var(--radius-s)",
        textAlign: "left",
        cursor: "pointer",
        background: sel ? "var(--background-surface-highlight)" : "transparent",
        color: "var(--foreground-on-dark-primary-default)",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--t-paragraph1-size)",
        fontWeight: sel ? 700 : 500
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        flex: 1
      }
    }, opt), sel && /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "check",
      size: 24,
      color: "var(--brand-primary-default)"
    }));
  })));
}
Object.assign(__ds_scope, { CmpDropdown });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpDropdown.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpRadioButton.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/** CmpRadioButton — circle 24 in a 48 hit area. Selected = ink ring + ink dot. */
function CmpRadioButton({
  selected = false,
  onClick,
  disabled = false,
  style,
  ...rest
}) {
  const {
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    role: "radio",
    "aria-checked": selected,
    disabled: disabled,
    onClick: disabled ? undefined : onClick
  }, handlers, {
    style: {
      width: 48,
      height: 48,
      padding: 0,
      border: "none",
      background: "transparent",
      flexShrink: 0,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.4 : 1,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      width: 24,
      height: 24,
      boxSizing: "border-box",
      borderRadius: "50%",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      border: `2px solid ${selected ? "var(--foreground-on-dark-primary-default)" : "var(--border-tertiary)"}`,
      boxShadow: focusRing,
      ...(disabled ? {} : pressStyle)
    }
  }, selected && /*#__PURE__*/React.createElement("span", {
    style: {
      width: 12,
      height: 12,
      borderRadius: "50%",
      background: "var(--foreground-on-dark-primary-default)"
    }
  })));
}
Object.assign(__ds_scope, { CmpRadioButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpRadioButton.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpSlider.jsx
try { (() => {
const { useRef, useState } = React;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSlider — kit Slider: track 4 (ink 15%), active part ink 100%,
 * round thumb (24 here, for fingers) in a 48-high hit area. Drag or tap.
 */
function CmpSlider({
  value = 0,
  onValueChange,
  min = 0,
  max = 1,
  step,
  disabled = false,
  style,
  ...rest
}) {
  const ref = useRef(null);
  const [drag, setDrag] = useState(false);
  const pct = Math.max(0, Math.min(1, (value - min) / (max - min || 1))) * 100;
  const setFrom = clientX => {
    const r = ref.current.getBoundingClientRect();
    let v = min + Math.max(0, Math.min(1, (clientX - r.left) / r.width)) * (max - min);
    if (step) v = Math.round((v - min) / step) * step + min;
    onValueChange && onValueChange(v);
  };
  const down = e => {
    if (disabled) return;
    setDrag(true);
    setFrom(e.clientX);
    const move = ev => setFrom(ev.clientX);
    const up = () => {
      setDrag(false);
      window.removeEventListener("pointermove", move);
      window.removeEventListener("pointerup", up);
    };
    window.addEventListener("pointermove", move);
    window.addEventListener("pointerup", up);
  };
  return /*#__PURE__*/React.createElement("div", _extends({
    ref: ref,
    onPointerDown: down,
    style: {
      position: "relative",
      height: 48,
      width: "100%",
      touchAction: "none",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.4 : 1,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 0,
      right: 0,
      top: 22,
      height: 4,
      borderRadius: 2,
      background: "var(--background-surface-medium-highlight)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 0,
      width: `${pct}%`,
      top: 22,
      height: 4,
      borderRadius: 2,
      background: "var(--foreground-on-dark-primary-default)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: `${pct}%`,
      top: 24,
      width: 24,
      height: 24,
      borderRadius: "50%",
      background: "var(--foreground-on-dark-primary-default)",
      transform: `translate(-50%, -50%) scale(${drag ? 1.15 : 1})`,
      transition: "transform var(--motion-press)",
      boxShadow: "0 0 0 4px var(--background-page-primary)"
    }
  }));
}
Object.assign(__ds_scope, { CmpSlider });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpSlider.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpSwitch.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSwitch — kit Toggle, scaled for gloved fingers: track 52×32, thumb 24,
 * hit area 48. On = brand blue track + white thumb; off = ink 15% track + ink thumb.
 */
function CmpSwitch({
  checked = false,
  onCheckedChange,
  disabled = false,
  thumbIcon,
  style,
  ...rest
}) {
  const {
    handlers,
    active,
    focusRing
  } = useStateLayer();
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    role: "switch",
    "aria-checked": checked,
    disabled: disabled,
    onClick: () => !disabled && onCheckedChange && onCheckedChange(!checked)
  }, handlers, {
    style: {
      width: 64,
      height: 48,
      padding: 0,
      border: "none",
      background: "transparent",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.4 : 1,
      flexShrink: 0,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "relative",
      width: 52,
      height: 32,
      borderRadius: 9999,
      boxSizing: "border-box",
      background: checked ? "var(--brand-primary-default)" : "var(--background-surface-medium-highlight)",
      border: checked ? "none" : "1px solid var(--border-light)",
      boxShadow: focusRing,
      transition: "background var(--motion-standard)"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      top: "50%",
      left: checked ? 24 : 4,
      width: 24,
      height: 24,
      borderRadius: "50%",
      background: checked ? "#FFFFFF" : "var(--foreground-on-dark-primary-default)",
      transform: `translateY(-50%) scale(${active ? 0.9 : 1})`,
      transition: "left var(--motion-press), transform var(--motion-press)"
    }
  })));
}
Object.assign(__ds_scope, { CmpSwitch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpSwitch.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpTextField.jsx
try { (() => {
const { useState } = React;
const CmpText = __ds_scope.CmpText;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpTextField — kit InputMain. Height 72, translucent ink fill + 1px border,
 * radius r-inputs. Floating label (additional0) above value (headline2).
 * Focus: border-focus 2px. Error: error-default border + hint (red, our decision).
 * `style="Filled"` kept for API compat (same look).
 */
function CmpTextField({
  value = "",
  onChange,
  label,
  placeholder,
  style: _variant,
  disabled = false,
  readOnly = false,
  isError = false,
  singleLine = true,
  supportingText,
  leadingIcon,
  trailingIcon,
  type = "text",
  rootStyle,
  ...rest
}) {
  const [focus, setFocus] = useState(false);
  const floated = focus || value !== "" || !!placeholder;
  const borderColor = isError ? "var(--error-default)" : focus ? "var(--border-focus)" : "var(--border-light)";
  const labelColor = isError ? "var(--error-default)" : "var(--foreground-on-dark-secondary-default)";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%",
      opacity: disabled ? 0.4 : 1,
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-8)",
      ...rootStyle
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-12)",
      minHeight: "var(--h-input)",
      padding: "0 var(--space-20)",
      background: focus ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
      border: `var(--w-inputs-width) solid ${borderColor}`,
      boxShadow: focus || isError ? `inset 0 0 0 1px ${borderColor}` : "none",
      borderRadius: "var(--r-inputs-radius)",
      transition: "background var(--motion-fast), border-color var(--motion-fast)",
      boxSizing: "border-box"
    }
  }, leadingIcon && /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--foreground-on-dark-secondary-default)",
      display: "inline-flex"
    }
  }, leadingIcon), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      flex: 1,
      minWidth: 0,
      alignSelf: "stretch",
      display: "flex",
      flexDirection: "column",
      justifyContent: "center"
    }
  }, label && /*#__PURE__*/React.createElement("label", {
    style: {
      position: "absolute",
      left: 0,
      pointerEvents: "none",
      color: labelColor,
      fontFamily: "var(--font-sans)",
      fontWeight: floated ? 700 : 500,
      transition: "all var(--motion-fast)",
      top: floated ? 12 : "50%",
      transform: floated ? "none" : "translateY(-50%)",
      fontSize: floated ? "var(--t-additional0-size)" : "var(--t-paragraph1-size)",
      lineHeight: 1.2
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
      width: "100%",
      border: "none",
      outline: "none",
      background: "transparent",
      color: "var(--foreground-on-dark-primary-default)",
      caretColor: "var(--brand-primary-default)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--t-headline2-size)",
      fontWeight: 700,
      lineHeight: "var(--t-headline2-line)",
      padding: label ? "26px 0 8px" : 0
    }
  }, rest))), trailingIcon && /*#__PURE__*/React.createElement("span", {
    style: {
      color: isError ? "var(--error-default)" : "var(--foreground-on-dark-secondary-default)",
      display: "inline-flex"
    }
  }, trailingIcon)), supportingText && /*#__PURE__*/React.createElement(CmpText, {
    variant: "headline3",
    color: isError ? "var(--error-default)" : "var(--foreground-on-dark-secondary-default)",
    style: {
      padding: "0 var(--space-4)"
    }
  }, supportingText));
}
Object.assign(__ds_scope, { CmpTextField });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpTextField.jsx", error: String((e && e.message) || e) }); }

// components/navigation/CmpNavigationBar.jsx
try { (() => {
const CmpIcon = __ds_scope.CmpIcon;
const CmpBadge = __ds_scope.CmpBadge;
const CmpBadgedBox = __ds_scope.CmpBadgedBox;
/**
 * CmpNavigationBar — frosted bottom bar, 1px top border. Selected item =
 * ink pill behind icon + ink label; others 60%. Labels additional1.
 * (Not used in the app today — kept for completeness.)
 */
function CmpNavigationBar({
  items = [],
  style
}) {
  return /*#__PURE__*/React.createElement("nav", {
    style: {
      display: "flex",
      height: "var(--navbar-height)",
      boxSizing: "border-box",
      background: "var(--background-glass)",
      backdropFilter: "blur(var(--blur-glass))",
      WebkitBackdropFilter: "blur(var(--blur-glass))",
      borderTop: "1px solid var(--border-highlight)",
      ...style
    }
  }, items.map((it, i) => {
    const icon = /*#__PURE__*/React.createElement(CmpIcon, {
      icon: it.icon,
      size: 24,
      color: "currentColor"
    });
    return /*#__PURE__*/React.createElement("button", {
      key: i,
      type: "button",
      onClick: it.onClick,
      style: {
        flex: 1,
        border: "none",
        background: "transparent",
        cursor: "pointer",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: "var(--space-4)",
        color: it.selected ? "var(--foreground-on-dark-primary-default)" : "var(--foreground-on-dark-secondary-default)"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        width: 64,
        height: 32,
        borderRadius: 9999,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        background: it.selected ? "var(--background-surface-primary)" : "transparent",
        color: it.selected ? "var(--foreground-on-light-primary-default)" : "inherit",
        transition: "background var(--motion-standard)"
      }
    }, it.badge !== undefined ? /*#__PURE__*/React.createElement(CmpBadgedBox, {
      badge: /*#__PURE__*/React.createElement(CmpBadge, {
        count: it.badge
      })
    }, icon) : icon), it.label && /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-sans)",
        fontSize: "var(--t-additional1-size)",
        fontWeight: 700
      }
    }, it.label));
  }));
}
Object.assign(__ds_scope, { CmpNavigationBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/CmpNavigationBar.jsx", error: String((e && e.message) || e) }); }

// components/navigation/CmpTabRow.jsx
try { (() => {
const CmpIcon = __ds_scope.CmpIcon;
/**
 * CmpTabRow — kit SegmentedTabs: translucent ink track (radius r-controls),
 * selected segment = ink fill + on-light text. Tab height 48, label headline3 UPPERCASE.
 * `scrollable` → horizontal scroll with auto-width segments.
 * `inset` — horizontal margin so the track aligns with content (default 16; 0 = full width).
 */
function CmpTabRow({
  tabs = [],
  selectedTabIndex = 0,
  onTabSelected,
  scrollable = false,
  inset = 16,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    role: "tablist",
    style: {
      display: "flex",
      gap: "var(--space-4)",
      padding: "var(--space-4)",
      background: "var(--background-surface-ultra-highlight)",
      border: "1px solid var(--border-highlight)",
      borderRadius: "calc(var(--r-controls-radius) + 4px)",
      overflowX: scrollable ? "auto" : "hidden",
      margin: `var(--space-8) ${typeof inset === "number" ? inset + "px" : inset}`,
      flexShrink: 0,
      ...style
    }
  }, tabs.map((t, i) => {
    const sel = i === selectedTabIndex;
    return /*#__PURE__*/React.createElement("button", {
      key: i,
      type: "button",
      role: "tab",
      "aria-selected": sel,
      onClick: () => onTabSelected && onTabSelected(i),
      style: {
        flex: scrollable ? "0 0 auto" : "1 1 0",
        minWidth: 0,
        height: "var(--h-tab)",
        padding: "0 var(--space-16)",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "var(--space-8)",
        border: "none",
        borderRadius: "var(--r-controls-radius)",
        cursor: "pointer",
        background: sel ? "var(--background-surface-primary)" : "transparent",
        color: sel ? "var(--foreground-on-light-primary-default)" : "var(--foreground-on-dark-secondary-default)",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--t-headline3-size)",
        fontWeight: 700,
        textTransform: "uppercase",
        whiteSpace: "nowrap",
        transition: "background 600ms, color 600ms"
      }
    }, t.icon && /*#__PURE__*/React.createElement(CmpIcon, {
      icon: t.icon,
      size: 24,
      color: "currentColor"
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        overflow: "hidden",
        textOverflow: "ellipsis"
      }
    }, t.title));
  }));
}
Object.assign(__ds_scope, { CmpTabRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/CmpTabRow.jsx", error: String((e && e.message) || e) }); }

// components/navigation/CmpTopAppBar.jsx
try { (() => {
const CmpText = __ds_scope.CmpText;
/**
 * CmpTopAppBar — kit TopBar: transparent 64 bar over the page, title headline1,
 * title headline2 19 (large → headline1 24), optional subtitle (additional0 @ 60%). Use CmpIconButton variant="Glass" for
 * navigation/actions. Horizontal padding 16 (icons sit near edges).
 */
function CmpTopAppBar({
  title,
  subtitle,
  navigationIcon,
  actions,
  centered = false,
  large = false,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-12)",
      minHeight: "var(--h-topbar)",
      padding: "var(--space-8) var(--space-16)",
      boxSizing: "border-box",
      background: "transparent",
      color: "var(--foreground-on-dark-primary-default)",
      ...style
    }
  }, navigationIcon, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      display: "flex",
      flexDirection: "column",
      gap: 2,
      alignItems: centered ? "center" : "flex-start",
      textAlign: centered ? "center" : "left",
      paddingLeft: navigationIcon || centered ? 0 : "var(--space-8)"
    }
  }, title && /*#__PURE__*/React.createElement(CmpText, {
    variant: large ? "headline1" : "headline2",
    maxLines: 1
  }, title), subtitle && /*#__PURE__*/React.createElement(CmpText, {
    variant: "additional0",
    color: "var(--foreground-on-dark-secondary-default)",
    maxLines: 1
  }, subtitle)), actions && /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: "var(--space-8)"
    }
  }, actions), centered && !actions && navigationIcon && /*#__PURE__*/React.createElement("span", {
    style: {
      width: 56,
      flexShrink: 0
    }
  }));
}
Object.assign(__ds_scope, { CmpTopAppBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/CmpTopAppBar.jsx", error: String((e && e.message) || e) }); }

// components/progress/CmpCircularProgressIndicator.jsx
try { (() => {

/**
 * CmpCircularProgressIndicator — kit SpinningProgressBar: 30dp arc, ink track 15%,
 * brand-blue arc. progress null → spinning; 0..1 → determinate.
 */
function CmpCircularProgressIndicator({
  progress = null,
  size = 30,
  strokeWidth = 3,
  color = "var(--brand-primary-default)",
  trackColor = "var(--background-surface-medium-highlight)",
  style
}) {
  const r = (size - strokeWidth) / 2;
  const c = 2 * Math.PI * r;
  const det = typeof progress === "number";
  const dash = det ? c * Math.max(0, Math.min(1, progress)) : c * 0.28;
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      width: size,
      height: size,
      flexShrink: 0,
      ...style
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: size,
    height: size,
    viewBox: `0 0 ${size} ${size}`,
    style: {
      animation: det ? undefined : "cmp-spin 0.9s linear infinite",
      transform: det ? "rotate(-90deg)" : undefined
    }
  }, /*#__PURE__*/React.createElement("circle", {
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
    style: {
      transition: "stroke-dasharray var(--motion-standard)"
    }
  })), /*#__PURE__*/React.createElement("style", null, "@keyframes cmp-spin{to{transform:rotate(360deg)}}"));
}
Object.assign(__ds_scope, { CmpCircularProgressIndicator });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/CmpCircularProgressIndicator.jsx", error: String((e && e.message) || e) }); }

// components/progress/CmpLinearProgressIndicator.jsx
try { (() => {

/** CmpLinearProgressIndicator — 4dp track (ink 15%), brand-blue fill; null → sweeping. */
function CmpLinearProgressIndicator({
  progress = null,
  color = "var(--brand-primary-default)",
  trackColor = "var(--background-surface-medium-highlight)",
  height = 4,
  style
}) {
  const det = typeof progress === "number";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      width: "100%",
      height,
      borderRadius: height,
      background: trackColor,
      overflow: "hidden",
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      top: 0,
      bottom: 0,
      borderRadius: height,
      background: color,
      left: det ? 0 : undefined,
      width: det ? `${Math.max(0, Math.min(1, progress)) * 100}%` : "35%",
      animation: det ? undefined : "cmp-sweep 1.4s cubic-bezier(0.4,0,0.2,1) infinite",
      transition: "width var(--motion-standard)"
    }
  }), /*#__PURE__*/React.createElement("style", null, "@keyframes cmp-sweep{0%{left:-35%}100%{left:100%}}"));
}
Object.assign(__ds_scope, { CmpLinearProgressIndicator });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/CmpLinearProgressIndicator.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpCard.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpCard — card surface (white in light, ink 5% in dark) + 1px ink-10 border,
 * radius r-card-medium, no shadow. Clickable → press scale .96 + ripple.
 * `elevation` kept for API compat (≥2 = one step brighter fill).
 */
function CmpCard({
  children,
  onClick,
  elevation = 0,
  border = true,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const click = !!onClick;
  return /*#__PURE__*/React.createElement("div", _extends({
    onClick: onClick,
    role: click ? "button" : undefined,
    tabIndex: click ? 0 : undefined
  }, click ? handlers : {}, {
    style: {
      position: "relative",
      overflow: "hidden",
      boxSizing: "border-box",
      background: elevation >= 2 ? "var(--background-surface-highlight)" : "var(--background-surface-card)",
      color: "var(--foreground-on-dark-primary-default)",
      border: border ? "1px solid var(--border-highlight)" : "none",
      borderRadius: "var(--r-card-medium-radius)",
      cursor: click ? "pointer" : undefined,
      boxShadow: focusRing,
      ...(click ? pressStyle : {}),
      ...style
    }
  }, rest), click && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--foreground-on-dark-primary-default)",
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
/** CmpDivider — 1px ink-10 line (border-highlight). */
function CmpDivider({
  orientation = "horizontal",
  thickness = 1,
  color = "var(--border-highlight)",
  inset = 0,
  style,
  ...rest
}) {
  const h = orientation === "horizontal";
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "separator",
    style: {
      flexShrink: 0,
      background: color,
      width: h ? "auto" : thickness,
      height: h ? thickness : "auto",
      alignSelf: h ? undefined : "stretch",
      marginLeft: h ? inset : 0,
      ...style
    }
  }, rest));
}
Object.assign(__ds_scope, { CmpDivider });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpDivider.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpSelectableCard.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSelectableCard — kit SelectableListItem: min-height 80, radius r-list-item.
 * Off: ink 5% fill + 1px ink-10 border. On: 2px ink border + ink check circle.
 */
function CmpSelectableCard({
  selected = false,
  onClick,
  icon,
  children,
  style
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "button",
    tabIndex: 0,
    "aria-pressed": selected,
    onClick: onClick
  }, handlers, {
    style: {
      position: "relative",
      overflow: "hidden",
      boxSizing: "border-box",
      minHeight: "var(--h-list-item)",
      padding: "var(--space-16) var(--space-20)",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-16)",
      background: selected ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
      color: "var(--foreground-on-dark-primary-default)",
      border: selected ? "2px solid var(--border-primary)" : "1px solid var(--border-highlight)",
      borderRadius: "var(--r-list-item-radius)",
      cursor: "pointer",
      boxShadow: focusRing,
      ...pressStyle,
      ...style
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "var(--foreground-on-dark-primary-default)",
      opacity,
      pointerEvents: "none"
    }
  }), icon && /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      flexShrink: 0
    }
  }, icon), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, children), /*#__PURE__*/React.createElement("span", {
    style: {
      width: 28,
      height: 28,
      borderRadius: "50%",
      flexShrink: 0,
      boxSizing: "border-box",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      background: selected ? "var(--background-surface-primary)" : "transparent",
      border: selected ? "none" : "1.5px solid var(--border-tertiary)"
    }
  }, selected && /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "check",
    size: 20,
    color: "var(--foreground-on-light-primary-default)"
  })));
}
Object.assign(__ds_scope, { CmpSelectableCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpSelectableCard.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpSurface.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpSurface — generic container. Default fill = ink 5% step (kit
 * background-surface-ultra-highlight); pass `color` for another step.
 * `border` → 1px border-highlight. `shadowElevation` ignored (no shadows).
 */
function CmpSurface({
  children,
  onClick,
  color = "var(--background-surface-ultra-highlight)",
  contentColor = "var(--foreground-on-dark-primary-default)",
  radius,
  shadowElevation,
  border = false,
  style,
  ...rest
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const click = !!onClick;
  return /*#__PURE__*/React.createElement("div", _extends({
    onClick: onClick,
    role: click ? "button" : undefined,
    tabIndex: click ? 0 : undefined
  }, click ? handlers : {}, {
    style: {
      position: "relative",
      overflow: "hidden",
      boxSizing: "border-box",
      background: color,
      color: contentColor,
      borderRadius: radius ?? "var(--r-card-small-radius)",
      border: border ? "1px solid var(--border-highlight)" : "none",
      cursor: click ? "pointer" : undefined,
      boxShadow: focusRing,
      ...(click ? pressStyle : {}),
      ...style
    }
  }, rest), click && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: contentColor,
      opacity,
      pointerEvents: "none"
    }
  }), children);
}
Object.assign(__ds_scope, { CmpSurface });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpSurface.jsx", error: String((e && e.message) || e) }); }

// components/buttons/CmpHoldToConfirmButton.jsx
try { (() => {
const { useEffect, useRef, useState } = React;
/**
 * CmpHoldToConfirmButton — kit HoldToConfirm: for dangerous actions (reset, erase, flash).
 * Height 64, radius r-buttons, label UPPERCASE headline2. Press and hold `duration` ms:
 * a fill sweeps left→right; releasing early cancels. Fires onConfirm at 100%.
 */
function CmpHoldToConfirmButton({
  text = "Удерживайте",
  doneText = "Готово",
  onConfirm,
  duration = 1500,
  danger = true,
  disabled = false,
  style
}) {
  const [p, setP] = useState(0);
  const [done, setDone] = useState(false);
  const raf = useRef(0),
    t0 = useRef(0);
  const stop = () => {
    cancelAnimationFrame(raf.current);
    if (!done) setP(0);
  };
  const tick = () => {
    const v = Math.min(1, (performance.now() - t0.current) / duration);
    setP(v);
    if (v >= 1) {
      setDone(true);
      onConfirm && onConfirm();
      return;
    }
    raf.current = requestAnimationFrame(tick);
  };
  const start = () => {
    if (disabled || done) return;
    t0.current = performance.now();
    raf.current = requestAnimationFrame(tick);
  };
  useEffect(() => () => cancelAnimationFrame(raf.current), []);
  const fill = danger ? "var(--error-default)" : "var(--brand-primary-default)";
  return /*#__PURE__*/React.createElement("button", {
    type: "button",
    disabled: disabled,
    onPointerDown: start,
    onPointerUp: stop,
    onPointerLeave: stop,
    onKeyDown: e => {
      if ((e.key === " " || e.key === "Enter") && !e.repeat) start();
    },
    onKeyUp: stop,
    style: {
      position: "relative",
      overflow: "hidden",
      width: "100%",
      height: "var(--h-hold)",
      boxSizing: "border-box",
      border: `1px solid ${disabled ? "transparent" : fill}`,
      borderRadius: "var(--r-buttons-radius)",
      background: disabled ? "var(--background-surface-ultra-highlight)" : "var(--background-surface-ultra-highlight)",
      color: disabled ? "var(--foreground-on-dark-primary-disabled)" : p > 0.5 || done ? "#FFFFFF" : fill,
      fontFamily: "var(--font-sans)",
      fontSize: "var(--t-headline2-size)",
      fontWeight: 700,
      textTransform: "uppercase",
      letterSpacing: "0.03em",
      cursor: disabled ? "default" : "pointer",
      touchAction: "none",
      userSelect: "none",
      transform: p > 0 && !done ? "scale(var(--press-scale))" : "scale(1)",
      transition: "transform var(--motion-press), color var(--motion-fast)",
      ...style
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      width: `${(done ? 1 : p) * 100}%`,
      background: fill,
      transition: p === 0 ? "width var(--motion-standard)" : "none"
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "relative"
    }
  }, done ? doneText : text));
}
Object.assign(__ds_scope, { CmpHoldToConfirmButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/CmpHoldToConfirmButton.jsx", error: String((e && e.message) || e) }); }

// components/feedback/CmpContentError.jsx
try { (() => {
const CmpIcon = __ds_scope.CmpIcon;
const CmpText = __ds_scope.CmpText;
/**
 * CmpContentError — kit ContentError: centered state block for empty/error/offline.
 * 80 icon circle, title title2 (one line), text headline2 @ 60%, optional action slot.
 * tone: error (red) · empty (ink) · offline (warning).
 */
function CmpContentError({
  icon,
  title,
  text,
  action,
  tone = "error",
  style
}) {
  const t = {
    error: ["error", "var(--error-default)", "var(--error-highlight)"],
    empty: ["inbox", "var(--foreground-on-dark-primary-default)", "var(--background-surface-highlight)"],
    offline: ["wifi_off", "var(--warning-default)", "var(--warning-highlight)"]
  }[tone] || [];
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      textAlign: "center",
      gap: "var(--space-16)",
      padding: "var(--space-32) var(--space-24)",
      maxWidth: 480,
      margin: "0 auto",
      ...style
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      width: 80,
      height: 80,
      borderRadius: "var(--r-round-radius)",
      background: t[2],
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon ?? t[0],
    size: 40,
    color: t[1]
  })), /*#__PURE__*/React.createElement(CmpText, {
    variant: "title2",
    as: "div"
  }, title), text && /*#__PURE__*/React.createElement(CmpText, {
    variant: "headline2",
    as: "div",
    color: "var(--foreground-on-dark-secondary-default)",
    style: {
      textWrap: "pretty"
    }
  }, text), action && /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: "var(--space-8)",
      display: "flex",
      flexDirection: "column",
      alignSelf: "stretch",
      gap: "var(--space-8)"
    }
  }, action));
}
Object.assign(__ds_scope, { CmpContentError });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/CmpContentError.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpChip.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpChip — kit Chips: pill 48 high, label headline3. Off: ink 5% fill + vertical-gradient
 * 1px border. Selected: ink fill + on-light text + check. Optional leading icon / onRemove.
 */
function CmpChip({
  label,
  selected = false,
  icon,
  onClick,
  onRemove,
  disabled = false,
  style
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    "aria-pressed": selected,
    disabled: disabled,
    onClick: disabled ? undefined : onClick
  }, handlers, {
    style: {
      position: "relative",
      overflow: "hidden",
      height: "var(--h-chip)",
      boxSizing: "border-box",
      flexShrink: 0,
      display: "inline-flex",
      alignItems: "center",
      gap: "var(--space-8)",
      padding: `0 ${onRemove ? 8 : 20}px 0 ${selected || icon ? 14 : 20}px`,
      borderRadius: "var(--r-chips-radius)",
      border: "var(--w-chips-width) solid transparent",
      background: selected ? "linear-gradient(var(--background-surface-primary), var(--background-surface-primary)) padding-box, linear-gradient(var(--border-light), var(--border-ultra-highlight)) border-box" : "linear-gradient(var(--background-surface-ultra-highlight), var(--background-surface-ultra-highlight)) padding-box, linear-gradient(var(--border-light), var(--border-ultra-highlight)) border-box",
      color: selected ? "var(--foreground-on-light-primary-default)" : "var(--foreground-on-dark-primary-default)",
      fontFamily: "var(--font-sans)",
      fontSize: "var(--t-headline3-size)",
      fontWeight: 700,
      whiteSpace: "nowrap",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.4 : 1,
      boxShadow: focusRing,
      transition: "background 300ms, color 300ms",
      ...(disabled ? {} : pressStyle),
      ...style
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "currentColor",
      opacity,
      pointerEvents: "none"
    }
  }), selected ? /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "check",
    size: 20,
    color: "currentColor"
  }) : icon && /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 20,
    color: "currentColor"
  }), label, onRemove && /*#__PURE__*/React.createElement("span", {
    role: "button",
    "aria-label": "\u0423\u0431\u0440\u0430\u0442\u044C",
    onClick: e => {
      e.stopPropagation();
      onRemove();
    },
    style: {
      width: 32,
      height: 32,
      borderRadius: "50%",
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "close",
    size: 18,
    color: "currentColor"
  })));
}
Object.assign(__ds_scope, { CmpChip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpChip.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/CmpListItem.jsx
try { (() => {
const useStateLayer = __ds_scope.useStateLayer;
const CmpIcon = __ds_scope.CmpIcon;
const CmpText = __ds_scope.CmpText;
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * CmpListItem — kit ListItem: min-height 80, ink 5% fill + 1px border, radius r-list-item.
 * Leading icon in a 48 circle, title headline2, subtitle paragraph3 @ 60%,
 * trailing slot (default chevron when clickable). `status` adds a colored dot.
 */
function CmpListItem({
  icon,
  title,
  subtitle,
  trailing,
  onClick,
  status,
  disabled = false,
  style
}) {
  const {
    opacity,
    handlers,
    pressStyle,
    focusRing
  } = useStateLayer();
  const click = !!onClick && !disabled;
  const dot = {
    ok: "var(--success-default)",
    warn: "var(--warning-default)",
    error: "var(--error-default)",
    info: "var(--brand-primary-default)"
  }[status];
  return /*#__PURE__*/React.createElement("div", _extends({
    role: click ? "button" : undefined,
    tabIndex: click ? 0 : undefined,
    onClick: click ? onClick : undefined
  }, click ? handlers : {}, {
    style: {
      position: "relative",
      overflow: "hidden",
      boxSizing: "border-box",
      minHeight: "var(--h-list-item)",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-16)",
      padding: "var(--space-12) var(--space-16) var(--space-12) var(--space-16)",
      background: "var(--background-surface-ultra-highlight)",
      border: "var(--w-list-item-width) solid var(--border-highlight)",
      borderRadius: "var(--r-list-item-radius)",
      color: "var(--foreground-on-dark-primary-default)",
      cursor: click ? "pointer" : undefined,
      opacity: disabled ? 0.4 : 1,
      boxShadow: focusRing,
      ...(click ? pressStyle : {}),
      ...style
    }
  }), click && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      inset: 0,
      background: "currentColor",
      opacity,
      pointerEvents: "none"
    }
  }), icon && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "relative",
      width: 48,
      height: 48,
      flexShrink: 0,
      borderRadius: "var(--r-icon-radius)",
      background: "var(--background-surface-highlight)",
      display: "grid",
      placeItems: "center"
    }
  }, typeof icon === "string" ? /*#__PURE__*/React.createElement(CmpIcon, {
    icon: icon,
    size: 24,
    color: "currentColor"
  }) : icon, dot && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      right: 0,
      bottom: 0,
      width: 12,
      height: 12,
      borderRadius: "50%",
      background: dot,
      border: "2px solid var(--background-page-primary)"
    }
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-4)"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "headline2",
    maxLines: 1
  }, title), subtitle && /*#__PURE__*/React.createElement(CmpText, {
    variant: "paragraph3",
    color: "var(--foreground-on-dark-secondary-default)",
    maxLines: 2
  }, subtitle)), trailing ?? (click && /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "chevron_right",
    size: 32,
    color: "var(--foreground-on-dark-secondary-default)"
  })));
}

/**
 * CmpInfoRow — kit InfoRow: label (additional0 @ 60%) above value (headline2), min-height 88,
 * optional trailing action (e.g. copy). Bottom divider unless `last`.
 */
function CmpInfoRow({
  label,
  value,
  mono = false,
  accent = false,
  trailing,
  last = false,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      minHeight: "var(--h-info-row)",
      boxSizing: "border-box",
      display: "flex",
      alignItems: "center",
      gap: "var(--space-12)",
      padding: "var(--space-12) 0",
      borderBottom: last ? "none" : "1px solid var(--border-highlight)",
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      display: "flex",
      flexDirection: "column",
      gap: "var(--space-4)"
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "additional0",
    color: "var(--foreground-on-dark-secondary-default)"
  }, label), /*#__PURE__*/React.createElement(CmpText, {
    variant: "headline2",
    color: accent ? "var(--brand-primary-default)" : "var(--foreground-on-dark-primary-default)",
    style: {
      fontFamily: mono ? "var(--font-mono)" : undefined,
      overflowWrap: "anywhere"
    }
  }, value)), trailing);
}
Object.assign(__ds_scope, { CmpListItem, CmpInfoRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/CmpListItem.jsx", error: String((e && e.message) || e) }); }

// components/forms/CmpInputDigit.jsx
try { (() => {
const {
  useRef
} = React;

/**
 * CmpInputDigit — kit InputDigit (OTP / PIN): cells 56×72, digit title1, radius r-inputs.
 * One hidden input drives all cells (paste works). Focused cell: border-focus 2px.
 * Error: red border + shake-free (static) emphasis.
 */
function CmpInputDigit({
  value = "",
  onChange,
  length = 4,
  error = false,
  disabled = false,
  secure = false,
  autoFocus = false,
  style
}) {
  const ref = useRef(null);
  const [focus, setFocus] = React.useState(false);
  const cells = Array.from({
    length
  }, (_, i) => value[i] || "");
  const active = Math.min(value.length, length - 1);
  return /*#__PURE__*/React.createElement("div", {
    onClick: () => ref.current && ref.current.focus(),
    style: {
      position: "relative",
      display: "flex",
      gap: "var(--space-8)",
      justifyContent: "center",
      opacity: disabled ? 0.4 : 1,
      ...style
    }
  }, /*#__PURE__*/React.createElement("input", {
    ref: ref,
    value: value,
    disabled: disabled,
    autoFocus: autoFocus,
    inputMode: "numeric",
    autoComplete: "one-time-code",
    maxLength: length,
    onChange: e => onChange && onChange(e.target.value.replace(/\D/g, "").slice(0, length)),
    onFocus: () => setFocus(true),
    onBlur: () => setFocus(false),
    "aria-label": "\u041A\u043E\u0434",
    style: {
      position: "absolute",
      inset: 0,
      opacity: 0,
      border: "none",
      background: "transparent",
      color: "transparent",
      caretColor: "transparent"
    }
  }), cells.map((c, i) => {
    const on = focus && i === active;
    const bc = error ? "var(--error-default)" : on ? "var(--border-focus)" : c ? "var(--border-tertiary)" : "var(--border-light)";
    return /*#__PURE__*/React.createElement("span", {
      key: i,
      "aria-hidden": "true",
      style: {
        width: 56,
        height: 72,
        boxSizing: "border-box",
        borderRadius: "var(--r-inputs-radius)",
        border: `1px solid ${bc}`,
        boxShadow: on || error ? `inset 0 0 0 1px ${bc}` : "none",
        background: on ? "var(--background-surface-highlight)" : "var(--background-surface-ultra-highlight)",
        display: "grid",
        placeItems: "center",
        color: "var(--foreground-on-dark-primary-default)",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--t-title1-size)",
        fontWeight: 700,
        lineHeight: 1,
        transition: "border-color var(--motion-fast), background var(--motion-fast)"
      }
    }, c ? secure ? "•" : c : on ? /*#__PURE__*/React.createElement("span", {
      style: {
        width: 2,
        height: 32,
        background: "var(--brand-primary-default)"
      }
    }) : "");
  }));
}
Object.assign(__ds_scope, { CmpInputDigit });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/CmpInputDigit.jsx", error: String((e && e.message) || e) }); }

// features/app-update/AppUpdateScreen.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// AppUpdateScreen — «Обновление приложения».
// Desktop (Windows, macOS): проверка → загрузка → установка, каналы обновлений.
// Телефон/планшет (Android, iOS): описание новой версии и переход в магазин, каналов нет.
//
//   • useUpdateCtl(opts) — контроллер (состояние, канал, загрузка, диалоги). Им же пользуется главный экран,
//     поэтому загрузка идёт в фоне и видна в блоке бокового бара.
//   • UpdateBody / UpdateRoute — тело экрана (топбар даёт хост). UpdateDialogs — диалоги «Идёт работа с блоком», «Перейти на бету».
//   • AppUpdateApp — полный экран (топбар + тело + диалоги).
//   • Входы на главном: SideUpdateBlock (desktop, низ бокового бара), UpdateToast (desktop, при старте),
//     UpdateBanner (телефон/планшет, плашка на главном), MenuUpdateRow (телефон/планшет, пункт меню с точкой).
//   • Витрины: AppUpdateDeskDemo, AppUpdatePhoneDemo, AppUpdateStatesDemo.
// Всё экспортируется в window.AppUpdate.

// Ждём бандл дизайн-системы: в .dc.html он может догрузиться позже этого файла.
(function boot() {
  if (!window.PandoraSpecDesignSystem_b8d6e1 || !window.React) return void setTimeout(boot, 30);
  const DS = window.PandoraSpecDesignSystem_b8d6e1;
  const {
    CmpTopAppBar,
    CmpIconButton,
    CmpText,
    CmpIcon,
    CmpButton,
    CmpOutlinedButton,
    CmpTextButton,
    CmpCircularProgressIndicator,
    CmpLinearProgressIndicator,
    CmpRadioButton,
    CmpDialog,
    CmpSnackbar,
    CmpBadge
  } = DS;
  const {
    useState,
    useEffect,
    useRef
  } = React;
  if (!document.getElementById("au-css")) {
    const s = document.createElement("style");
    s.id = "au-css";
    s.textContent = `
@keyframes auIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}
.au-in{animation:auIn .24s cubic-bezier(.2,0,0,1) both}
.au-hov:hover{background-color:color-mix(in srgb, var(--on-surface) 8%, transparent)!important}
.au-hov:active{background-color:color-mix(in srgb, var(--on-surface) 12%, transparent)!important}
.au-hovp:hover{filter:brightness(.96)}
.au-f:focus-visible{outline:2px solid var(--primary);outline-offset:2px}
@media (prefers-reduced-motion:reduce){.au-in{animation:none!important}}`;
    document.head.appendChild(s);
  }

  // ── Данные ──────────────────────────────────────────────────────────────────
  const isDesk = p => p === "win" || p === "mac";
  const cmpV = (a, b) => {
    const x = String(a).split(".").map(Number),
      y = String(b).split(".").map(Number);
    for (let i = 0; i < 3; i++) {
      if ((x[i] || 0) !== (y[i] || 0)) return (x[i] || 0) - (y[i] || 0);
    }
    return 0;
  };
  const INST = {
    old: {
      v: "2.0.4",
      b: "204"
    },
    cur: {
      v: "2.1.0",
      b: "210"
    },
    beta: {
      v: "2.2.0",
      b: "220"
    }
  };
  const BASE = "s1234";
  const REL = {
    id: "rel",
    name: "Релиз",
    v: "2.1.0",
    d: "12.11.2026",
    def: true
  };
  const BETA = {
    id: "beta",
    name: "Бета",
    v: "2.2.0",
    d: "10.11.2026"
  };
  const CHANNELS = {
    one: [REL],
    three: [REL, BETA, {
      id: "test",
      name: "Тест",
      v: "2.2.1",
      d: "13.11.2026"
    }],
    many: [REL, BETA, {
      id: "test",
      name: "Тест — внутренние сборки отдела разработки, не для монтажа у клиентов",
      v: "2.2.1",
      d: "13.11.2026"
    }, {
      id: "pilot",
      name: "Пилот: дилерские центры Северо-Западного региона",
      v: "2.1.1",
      d: "11.11.2026"
    }, {
      id: "can",
      name: "Пилот CAN-модулей нового поколения",
      v: null
    }, {
      id: "w7",
      name: "Windows 7 и старые компьютеры",
      v: "1.9.8",
      d: "02.06.2026"
    }]
  };
  const MOBILE_CH = {
    id: "store",
    name: "",
    v: "2.1.0",
    d: "12.11.2026",
    def: true
  };
  const NOTES = {
    short: "- Ускорена запись прошивок и синхронизация базы\n- Новые автомобили и точки подключения\n- Исправлены сбои при работе по CAN/LIN",
    long: ["Монтаж", "- Быстрый монтаж: подсказки по точкам подключения для 140 новых моделей", "- Расширенный монтаж: поиск блока по ID и штрих-коду", "- Ассистент монтажа запоминает последние действия", "", "Прошивка", "- Запись прошивок по USB стала примерно вдвое быстрее", "- Проверка файла прошивки перед записью", "- Архив прошивок: фильтр по дате выпуска", "", "Настройки", "- Новые параметры автозапуска для дизельных двигателей", "- Сравнение настроек с заводскими", "- Экспорт настроек в файл", "", "Исправления", "- Сбой при обрыве Bluetooth во время записи настроек", "- Неверная дата в журнале событий", "- Зависание при обновлении базы на медленной сети", "- Обрезка длинных названий автомобилей", "- Мелкие исправления интерфейса"].join("\n"),
    empty: ""
  };
  const BETA_HINT = "Бета — новые функции раньше всех, но возможны ошибки";
  const SIZE = 180;

  // ── Контроллер ──────────────────────────────────────────────────────────────
  // initial: "auto" — проверка при открытии; иначе — стартовое состояние (для витрины и главного).
  function useUpdateCtl({
    platform = "win",
    inst0 = "old",
    chans = "three",
    ch0,
    sizeKnown = false,
    busy = false,
    initial = "auto",
    dialog0 = null
  } = {}) {
    const desk = isDesk(platform);
    const channels = desk ? CHANNELS[chans] || CHANNELS.three : [MOBILE_CH];
    const [st, set] = useState(() => ({
      s: initial === "auto" ? "checking" : initial,
      inst: INST[inst0] || INST.old,
      chId: desk && ch0 || channels[0].id,
      prog: initial === "downloading" ? 0.24 : initial === "downloaded" ? 1 : 0,
      dialog: dialog0,
      pend: dialog0 === "beta" ? channels[1] && channels[1].id : null
    }));
    const T = useRef({});
    const clear = () => {
      clearTimeout(T.current.t);
      clearInterval(T.current.i);
    };
    const chOf = id => channels.find(c => c.id === id) || channels[0];
    const resolve = (inst, c) => !c.v ? "err_empty" : cmpV(c.v, inst.v) > 0 ? "available" : cmpV(c.v, inst.v) === 0 ? "uptodate" : desk ? "older" : "uptodate";
    function check(id) {
      clear();
      set(s => ({
        ...s,
        s: "checking",
        chId: id || s.chId,
        prog: 0
      }));
      T.current.t = setTimeout(() => set(s => ({
        ...s,
        s: resolve(s.inst, chOf(s.chId))
      })), 1200);
    }
    useEffect(() => {
      if (initial === "auto") check();
      return clear;
    }, []);
    function update() {
      clear();
      set(s => ({
        ...s,
        s: "downloading",
        prog: 0
      }));
      T.current.i = setInterval(() => set(s => {
        if (s.s !== "downloading") return s;
        const p = s.prog + 0.02;
        if (p >= 1) {
          clearInterval(T.current.i);
          return {
            ...s,
            s: "downloaded",
            prog: 1
          };
        }
        return {
          ...s,
          prog: p
        };
      }), 110);
    }
    function doInstall() {
      clear();
      set(s => ({
        ...s,
        s: "installing",
        dialog: null
      }));
      T.current.t = setTimeout(() => set(s => {
        const c = chOf(s.chId);
        return {
          ...s,
          s: "uptodate",
          inst: {
            v: c.v,
            b: c.v.replace(/\./g, "")
          },
          prog: 0
        };
      }), 2600);
    }
    return {
      ...st,
      platform,
      channels,
      ch: chOf(st.chId),
      sizeMB: sizeKnown ? SIZE : null,
      dlMB: st.prog * SIZE,
      check: () => check(),
      update,
      cancel: () => {
        clear();
        set(s => ({
          ...s,
          s: "available",
          prog: 0
        }));
      },
      install: () => busy ? set(s => ({
        ...s,
        dialog: "busy"
      })) : doInstall(),
      pick: id => {
        if (id === st.chId) return;
        if (!chOf(id).def) set(s => ({
          ...s,
          dialog: "beta",
          pend: id
        }));else check(id);
      },
      confirm: () => {
        if (st.dialog === "busy") doInstall();else {
          const id = st.pend;
          set(s => ({
            ...s,
            dialog: null,
            pend: null
          }));
          check(id);
        }
      },
      dismiss: () => set(s => ({
        ...s,
        dialog: null,
        pend: null
      }))
    };
  }

  // ── Ширина контейнера → класс окна ──────────────────────────────────────────
  function useAuMode() {
    const ref = useRef(null);
    const [w, setW] = useState(0);
    useEffect(() => {
      const el = ref.current;
      if (!el) return;
      const m = () => setW(el.offsetWidth);
      const ro = new ResizeObserver(m);
      ro.observe(el);
      m();
      return () => ro.disconnect();
    }, []);
    return [ref, w >= 1200 ? "large" : w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact"];
  }

  // ── Примитивы ───────────────────────────────────────────────────────────────
  const TONE = {
    primary: {
      bg: "var(--brand-primary-highlight)",
      fg: "var(--brand-primary-default)"
    },
    neutral: {
      bg: "var(--background-surface-highlight)",
      fg: "var(--foreground-on-dark-secondary-default)"
    },
    success: {
      bg: "var(--success-highlight)",
      fg: "var(--success-default)"
    },
    error: {
      bg: "var(--error-highlight)",
      fg: "var(--error-default)"
    }
  };
  const B48 = {
    height: 48,
    whiteSpace: "nowrap"
  };
  function Card({
    children,
    gap = 20,
    style
  }) {
    return /*#__PURE__*/React.createElement("div", {
      className: "au-in",
      style: {
        boxSizing: "border-box",
        borderRadius: "var(--r-card-medium-radius)",
        background: "var(--background-surface-card)",
        border: "1px solid var(--border-highlight)",
        padding: 24,
        display: "flex",
        flexDirection: "column",
        gap,
        minWidth: 0,
        ...style
      }
    }, children);
  }
  function Head({
    icon,
    tone = "primary",
    title,
    sub,
    spin
  }) {
    const c = TONE[tone];
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 16
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        width: 56,
        height: 56,
        borderRadius: "var(--r-icon-radius)",
        background: c.bg,
        color: c.fg,
        display: "grid",
        placeItems: "center",
        flex: "0 0 auto"
      }
    }, spin ? /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 28,
      strokeWidth: 3
    }) : /*#__PURE__*/React.createElement(CmpIcon, {
      icon: icon,
      size: 30,
      color: "currentColor"
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        minWidth: 0,
        display: "flex",
        flexDirection: "column",
        gap: 2
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        textWrap: "balance",
        overflowWrap: "anywhere"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "headlineSmall"
    }, title)), sub && /*#__PURE__*/React.createElement("span", {
      style: {
        textWrap: "pretty"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, sub))));
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
        borderRadius: "var(--r-card-small-radius)",
        minWidth: 120,
        border: "1px solid var(--border-highlight)",
        background: accent ? "var(--brand-primary-highlight)" : "var(--background-surface-ultra-highlight)",
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
  function Pills({
    from,
    to
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        gap: 10,
        flexWrap: "wrap",
        alignItems: "center"
      }
    }, /*#__PURE__*/React.createElement(VerPill, {
      label: "\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0430",
      value: from
    }), /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "arrow_forward",
      size: 22,
      color: "var(--on-surface-variant)"
    }), /*#__PURE__*/React.createElement(VerPill, {
      label: "\u041D\u043E\u0432\u0430\u044F",
      value: to,
      accent: true
    }));
  }
  function Note({
    icon = "info",
    children
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        gap: 12,
        alignItems: "flex-start",
        padding: "12px 16px",
        borderRadius: "var(--r-card-small-radius)",
        background: "var(--background-surface-ultra-highlight)",
        border: "1px solid var(--border-highlight)"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        marginTop: 1,
        flex: "0 0 auto",
        color: "var(--on-surface-variant)"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: icon,
      size: 22,
      color: "currentColor"
    })), /*#__PURE__*/React.createElement("span", {
      style: {
        textWrap: "pretty"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface)"
    }, children)));
  }
  const Divider = () => /*#__PURE__*/React.createElement("div", {
    style: {
      height: 1,
      background: "var(--outline-variant)"
    }
  });

  // «Что нового»: простой текст, «-» — маркер, пустая строка — отступ, остальные строки — подзаголовки.
  // Длиннее 8 строк — свёрнут до 6 с «Показать полностью».
  function Notes({
    text,
    open,
    onToggle,
    fadeBg = "var(--background-surface-card)"
  }) {
    const lines = text.split("\n");
    const LIM = 6,
      long = lines.length > LIM + 2;
    const shown = long && !open ? lines.slice(0, LIM) : lines;
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleMedium"
    }, "\u0427\u0442\u043E \u043D\u043E\u0432\u043E\u0433\u043E"), /*#__PURE__*/React.createElement("div", {
      style: {
        position: "relative",
        display: "flex",
        flexDirection: "column",
        gap: 6
      }
    }, shown.map((l, i) => {
      if (!l.trim()) return /*#__PURE__*/React.createElement("div", {
        key: i,
        style: {
          height: 6
        }
      });
      const m = l.match(/^\s*[-•]\s+(.*)$/);
      return m ? /*#__PURE__*/React.createElement("div", {
        key: i,
        style: {
          display: "flex",
          gap: 12,
          alignItems: "baseline"
        }
      }, /*#__PURE__*/React.createElement("span", {
        style: {
          width: 6,
          height: 6,
          borderRadius: 999,
          background: "var(--primary)",
          flex: "0 0 auto",
          transform: "translateY(-3px)"
        }
      }), /*#__PURE__*/React.createElement("span", {
        style: {
          textWrap: "pretty"
        }
      }, /*#__PURE__*/React.createElement(CmpText, {
        variant: "bodyLarge"
      }, m[1]))) : /*#__PURE__*/React.createElement("div", {
        key: i,
        style: {
          paddingTop: i ? 2 : 0
        }
      }, /*#__PURE__*/React.createElement(CmpText, {
        variant: "titleSmall",
        color: "var(--on-surface-variant)"
      }, l));
    }), long && !open && /*#__PURE__*/React.createElement("div", {
      "aria-hidden": "true",
      style: {
        position: "absolute",
        left: 0,
        right: 0,
        bottom: 0,
        height: 48,
        background: `linear-gradient(to bottom, transparent, ${fadeBg})`
      }
    })), long && /*#__PURE__*/React.createElement(CmpTextButton, {
      text: open ? "Свернуть" : "Показать полностью",
      icon: open ? "expand_less" : "expand_more",
      onClick: onToggle,
      style: {
        ...B48,
        alignSelf: "flex-start",
        marginLeft: -12
      }
    }));
  }
  function Progress({
    sizeMB,
    dlMB,
    bg
  }) {
    const p = sizeMB ? dlMB / sizeMB : null;
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10
      }
    }, /*#__PURE__*/React.createElement(CmpLinearProgressIndicator, {
      progress: p,
      height: 8
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        fontVariantNumeric: "tabular-nums"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleMedium"
    }, p != null ? `${Math.round(dlMB)} из ${sizeMB} МБ · ${Math.round(p * 100)} %` : `${Math.round(dlMB)} МБ загружено`)), bg && /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium",
      color: "var(--on-surface-variant)"
    }, "\u041C\u043E\u0436\u043D\u043E \u0432\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F \u043D\u0430 \u0433\u043B\u0430\u0432\u043D\u044B\u0439 \u044D\u043A\u0440\u0430\u043D \u2014 \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u0441\u044F."));
  }

  // ── Тело экрана ─────────────────────────────────────────────────────────────
  function headOf(c, multi) {
    const ch = c.ch,
      n = ch.name,
      size = c.sizeMB ? ` · ${c.sizeMB} МБ` : "";
    switch (c.s) {
      case "checking":
        return {
          spin: 1,
          title: "Проверяем обновления",
          sub: multi ? `Канал «${n}»` : "Это займёт несколько секунд"
        };
      case "available":
        return {
          icon: "system_update",
          title: "Доступна новая версия",
          sub: `Версия ${ch.v} от ${ch.d}${isDesk(c.platform) ? size : ""}`
        };
      case "downloading":
        return {
          icon: "download",
          title: "Загружаем обновление",
          sub: `Версия ${ch.v}`
        };
      case "downloaded":
        return {
          icon: "download_done",
          title: "Обновление загружено",
          sub: `Версия ${ch.v} готова к установке`
        };
      case "installing":
        return {
          spin: 1,
          title: "Устанавливаем обновление…",
          sub: "Приложение закроется через несколько секунд"
        };
      case "uptodate":
        return {
          icon: "check_circle",
          tone: "success",
          title: "У вас последняя версия",
          sub: multi ? `Канал «${n}»` : null
        };
      case "older":
        return {
          icon: "check_circle",
          tone: "success",
          title: "Обновлений нет",
          sub: `Канал «${n}»`
        };
      case "err_net":
        return {
          icon: "wifi_off",
          tone: "neutral",
          title: "Нет подключения к интернету",
          sub: "Проверьте сеть и повторите"
        };
      case "err_server":
        return {
          icon: "cloud_off",
          tone: "neutral",
          title: "Не удалось проверить обновления",
          sub: "Сервер не ответил. Повторите через минуту"
        };
      case "err_empty":
        return {
          icon: "inbox",
          tone: "neutral",
          title: `В канале «${n}» пока нет версий`,
          sub: "Выберите другой канал или проверьте позже"
        };
      case "err_download":
        return {
          icon: "error",
          tone: "error",
          title: "Не удалось загрузить обновление",
          sub: "Загрузка прервалась. «Повторить» начнёт её заново"
        };
      case "err_file":
        return {
          icon: "file_download_off",
          tone: "neutral",
          title: "Файл обновления не подходит",
          sub: "Файл обновления не подходит для этого компьютера. Скачайте установщик с сайта"
        };
      default:
        return {
          icon: "system_update",
          title: ""
        };
    }
  }
  function CurrentRow({
    c,
    multi
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 12,
        flexWrap: "wrap",
        minHeight: 48
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        flex: "1 1 220px",
        minWidth: 0,
        display: "flex",
        flexDirection: "column"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      color: "var(--on-surface-variant)"
    }, "\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0430 \u0432\u0435\u0440\u0441\u0438\u044F"), /*#__PURE__*/React.createElement("span", {
      style: {
        display: "flex",
        alignItems: "baseline",
        columnGap: 12,
        flexWrap: "wrap"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleLarge"
    }, `${c.inst.v} (${c.inst.b})`), /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium",
      color: "var(--on-surface-variant)"
    }, `база настроек ${BASE}`))), multi && /*#__PURE__*/React.createElement("span", {
      style: {
        display: "inline-flex",
        alignItems: "center",
        gap: 8,
        minHeight: 36,
        padding: "6px 14px",
        boxSizing: "border-box",
        borderRadius: "var(--r-tags-radius)",
        border: "1px solid var(--border-highlight)",
        background: "var(--background-surface-highlight)",
        color: "var(--on-secondary-container)",
        maxWidth: "100%"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "layers",
      size: 20,
      color: "currentColor"
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        overflowWrap: "anywhere"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      color: "currentColor"
    }, `Канал: ${c.ch.name}`))));
  }
  function ChannelCard({
    c
  }) {
    const lock = c.s === "downloading" || c.s === "installing";
    return /*#__PURE__*/React.createElement(Card, {
      gap: 12,
      style: {
        padding: "20px 16px 16px"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 2,
        padding: "0 8px"
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleLarge"
    }, "\u041A\u0430\u043D\u0430\u043B \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0439"), /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium",
      color: "var(--on-surface-variant)"
    }, "\u041E\u0442\u043A\u0443\u0434\u0430 \u043F\u0440\u0438\u0445\u043E\u0434\u044F\u0442 \u043D\u043E\u0432\u044B\u0435 \u0432\u0435\u0440\u0441\u0438\u0438")), /*#__PURE__*/React.createElement("div", {
      role: "radiogroup",
      "aria-label": "\u041A\u0430\u043D\u0430\u043B \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0439",
      style: {
        display: "flex",
        flexDirection: "column"
      }
    }, c.channels.map(ch => {
      const on = ch.id === c.chId;
      const desc = !ch.v ? "Пока нет версий" : `Версия ${ch.v} · ${ch.d}${ch.v === c.inst.v ? " · установлена" : ""}`;
      return /*#__PURE__*/React.createElement("div", {
        key: ch.id,
        role: "radio",
        "aria-checked": on,
        "aria-disabled": lock || undefined,
        tabIndex: lock ? -1 : 0,
        className: lock ? "" : "au-hov au-f",
        onClick: lock ? undefined : () => c.pick(ch.id),
        onKeyDown: e => {
          if (!lock && (e.key === "Enter" || e.key === " ")) {
            e.preventDefault();
            c.pick(ch.id);
          }
        },
        style: {
          display: "flex",
          alignItems: "center",
          gap: 4,
          minHeight: 64,
          padding: "6px 12px 6px 0",
          borderRadius: 12,
          cursor: lock ? "default" : "pointer",
          opacity: lock && !on ? 0.5 : 1,
          background: on ? "color-mix(in srgb, var(--primary) 8%, transparent)" : "transparent"
        }
      }, /*#__PURE__*/React.createElement(CmpRadioButton, {
        selected: on,
        tabIndex: -1,
        "aria-hidden": "true",
        style: {
          pointerEvents: "none"
        }
      }), /*#__PURE__*/React.createElement("div", {
        style: {
          flex: 1,
          minWidth: 0,
          display: "flex",
          flexDirection: "column",
          gap: 2
        }
      }, /*#__PURE__*/React.createElement("span", {
        style: {
          overflowWrap: "anywhere"
        }
      }, /*#__PURE__*/React.createElement(CmpText, {
        variant: "titleMedium"
      }, ch.name)), /*#__PURE__*/React.createElement(CmpText, {
        variant: "bodyMedium",
        color: "var(--on-surface-variant)"
      }, desc)));
    })), /*#__PURE__*/React.createElement(Note, {
      icon: "science"
    }, BETA_HINT));
  }
  // c — контроллер (useUpdateCtl), from: menu | banner | toast | sidebar.
  function UpdateBody({
    c,
    mode = "expanded",
    from = "menu",
    notes = "long",
    layout = "two",
    bg = true,
    notesOpen0 = false,
    onLater = () => {}
  }) {
    const [open, setOpen] = useState(notesOpen0);
    const desk = isDesk(c.platform),
      multi = desk && c.channels.length > 1;
    const wide = mode === "expanded" || mode === "large";
    const two = multi && layout !== "one" && wide;
    const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
    const h = headOf(c, multi),
      s = c.s,
      nv = c.ch.v;
    const later = from !== "menu" ? /*#__PURE__*/React.createElement(CmpTextButton, {
      text: "\u041F\u043E\u0437\u0436\u0435",
      onClick: onLater,
      style: B48
    }) : null;
    const store = c.platform === "ios" ? "Открыть в App Store" : "Открыть в Google Play";
    const txt = NOTES[notes] != null ? NOTES[notes] : notes;
    let act = null;
    if (s === "available") act = desk ? /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C",
      icon: "system_update",
      onClick: c.update,
      style: B48
    }), /*#__PURE__*/React.createElement(CmpOutlinedButton, {
      text: "\u0421\u043A\u0430\u0447\u0430\u0442\u044C \u0444\u0430\u0439\u043B",
      icon: "download",
      style: B48
    }), later) : /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
      text: store,
      icon: "open_in_new",
      style: B48
    }), later);
    if (s === "downloading") act = /*#__PURE__*/React.createElement(CmpOutlinedButton, {
      text: "\u041E\u0442\u043C\u0435\u043D\u0438\u0442\u044C",
      icon: "close",
      onClick: c.cancel,
      style: B48
    });
    if (s === "downloaded") act = /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C \u0438 \u043F\u0435\u0440\u0435\u0437\u0430\u043F\u0443\u0441\u0442\u0438\u0442\u044C",
      icon: "restart_alt",
      onClick: c.install,
      style: B48
    }), /*#__PURE__*/React.createElement(CmpTextButton, {
      text: "\u041F\u043E\u0437\u0436\u0435",
      onClick: onLater,
      style: B48
    }));
    if (s === "uptodate" || s === "older" || s === "err_empty") act = /*#__PURE__*/React.createElement(CmpOutlinedButton, {
      text: "\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u0442\u044C \u0441\u043D\u043E\u0432\u0430",
      icon: "refresh",
      onClick: c.check,
      style: B48
    });
    if (s === "err_net" || s === "err_server") act = /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u041F\u043E\u0432\u0442\u043E\u0440\u0438\u0442\u044C",
      icon: "refresh",
      onClick: c.check,
      style: B48
    });
    if (s === "err_download") act = /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u041F\u043E\u0432\u0442\u043E\u0440\u0438\u0442\u044C",
      icon: "refresh",
      onClick: c.update,
      style: B48
    }), /*#__PURE__*/React.createElement(CmpOutlinedButton, {
      text: "\u0421\u043A\u0430\u0447\u0430\u0442\u044C \u0444\u0430\u0439\u043B",
      icon: "download",
      style: B48
    }));
    if (s === "err_file") act = /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u0421\u043A\u0430\u0447\u0430\u0442\u044C \u0444\u0430\u0439\u043B",
      icon: "download",
      style: B48
    });
    const main = /*#__PURE__*/React.createElement(Card, {
      key: s
    }, /*#__PURE__*/React.createElement(Head, h), (s === "available" || s === "downloaded") && /*#__PURE__*/React.createElement(Pills, {
      from: c.inst.v,
      to: nv
    }), s === "downloading" && /*#__PURE__*/React.createElement(Progress, {
      sizeMB: c.sizeMB,
      dlMB: c.dlMB,
      bg: bg
    }), s === "older" && /*#__PURE__*/React.createElement(Note, {
      icon: "info"
    }, `В канале «${c.ch.name}» сейчас ${nv} — она старше установленной. Обновление придёт, когда в канале выйдет версия новее.`), act && /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 12,
        flexWrap: "wrap"
      }
    }, act), s === "available" && !desk && /*#__PURE__*/React.createElement("span", {
      style: {
        marginTop: -8
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium",
      color: "var(--on-surface-variant)"
    }, "\u0415\u0441\u043B\u0438 \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0435\u0449\u0451 \u043D\u0435 \u043F\u043E\u044F\u0432\u0438\u043B\u043E\u0441\u044C \u0432 \u043C\u0430\u0433\u0430\u0437\u0438\u043D\u0435, \u043F\u043E\u043F\u0440\u043E\u0431\u0443\u0439\u0442\u0435 \u043F\u043E\u0437\u0436\u0435.")), s === "downloaded" && /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 12,
        marginTop: -4
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge",
      color: "var(--on-surface-variant)"
    }, "\u041F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u0437\u0430\u043A\u0440\u043E\u0435\u0442\u0441\u044F, \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442 \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0438 \u043E\u0442\u043A\u0440\u043E\u0435\u0442\u0441\u044F \u0441\u043D\u043E\u0432\u0430."), /*#__PURE__*/React.createElement(Note, {
      icon: c.platform === "mac" ? "laptop_mac" : "desktop_windows"
    }, c.platform === "mac" ? "macOS: установка пройдёт без окон — через несколько секунд приложение откроется само." : "Windows: откроется окно установщика — в нём нажмите «Далее», затем «Установить».")), s === "available" && txt && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(Divider, null), /*#__PURE__*/React.createElement(Notes, {
      text: txt,
      open: open,
      onToggle: () => setOpen(!open)
    })));
    const chans = multi ? /*#__PURE__*/React.createElement(ChannelCard, {
      c: c
    }) : null;
    return /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0,
        overflow: "auto"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        maxWidth: two ? 1040 : 720,
        margin: "0 auto",
        padding: `${pad}px ${pad}px ${pad + 16}px`,
        boxSizing: "border-box",
        display: "flex",
        flexDirection: "column",
        gap: 16
      }
    }, /*#__PURE__*/React.createElement(CurrentRow, {
      c: c,
      multi: multi
    }), two ? /*#__PURE__*/React.createElement("div", {
      style: {
        display: "grid",
        gridTemplateColumns: "minmax(0,1fr) minmax(300px,360px)",
        gap: 24,
        alignItems: "start"
      }
    }, main, chans) : /*#__PURE__*/React.createElement(React.Fragment, null, main, chans)));
  }
  function UpdateDialogs({
    c
  }) {
    if (c.dialog === "busy") return /*#__PURE__*/React.createElement(CmpDialog, {
      icon: "warning",
      title: "\u0418\u0434\u0451\u0442 \u0440\u0430\u0431\u043E\u0442\u0430 \u0441 \u0431\u043B\u043E\u043A\u043E\u043C",
      text: "\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C\u0441\u044F \u0441\u0435\u0439\u0447\u0430\u0441? \u0421\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u0435 \u0431\u0443\u0434\u0435\u0442 \u043F\u0440\u0435\u0440\u0432\u0430\u043D\u043E.",
      onDismissRequest: c.dismiss,
      dismissButton: /*#__PURE__*/React.createElement(CmpTextButton, {
        text: "\u041E\u0442\u043C\u0435\u043D\u0430",
        onClick: c.dismiss,
        style: B48
      }),
      confirmButton: /*#__PURE__*/React.createElement(CmpButton, {
        text: "\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C",
        onClick: c.confirm,
        style: B48
      })
    });
    if (c.dialog === "beta") {
      const ch = c.channels.find(x => x.id === c.pend) || c.channels[1];
      const rel = c.channels.find(x => x.def) || c.channels[0];
      return /*#__PURE__*/React.createElement(CmpDialog, {
        icon: "science",
        title: `Перейти на канал «${ch.name}»?`,
        text: `Новые функции появляются здесь раньше, но возможны ошибки. Вернуться на «${rel.name}» можно в любой момент.`,
        onDismissRequest: c.dismiss,
        dismissButton: /*#__PURE__*/React.createElement(CmpTextButton, {
          text: "\u041E\u0442\u043C\u0435\u043D\u0430",
          onClick: c.dismiss,
          style: B48
        }),
        confirmButton: /*#__PURE__*/React.createElement(CmpButton, {
          text: "\u041F\u0435\u0440\u0435\u0439\u0442\u0438",
          onClick: c.confirm,
          style: B48
        })
      });
    }
    return null;
  }
  // Для главного экрана: тело с контроллером главного (фоновая загрузка).
  function UpdateRoute({
    ctl,
    mode,
    from = "menu",
    onLater
  }) {
    return /*#__PURE__*/React.createElement(UpdateBody, {
      c: ctl,
      mode: mode,
      from: from,
      onLater: onLater,
      bg: true
    });
  }
  // Полный экран.
  function AppUpdateApp({
    platform = "win",
    initial = "auto",
    inst0 = "old",
    chans = "three",
    ch0,
    sizeKnown = false,
    busy = false,
    dialog0 = null,
    notes = "long",
    notesOpen0 = false,
    from = "menu",
    layout = "two",
    onBack
  }) {
    const [ref, mode] = useAuMode();
    const c = useUpdateCtl({
      platform,
      initial,
      inst0,
      chans,
      ch0,
      sizeKnown,
      busy,
      dialog0
    });
    return /*#__PURE__*/React.createElement("div", {
      ref: ref,
      style: {
        position: "relative",
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
        background: "var(--background)",
        color: "var(--on-background)",
        fontFamily: "var(--font-sans)"
      }
    }, /*#__PURE__*/React.createElement(CmpTopAppBar, {
      title: "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u044F",
      style: {
        flex: "0 0 auto"
      },
      navigationIcon: /*#__PURE__*/React.createElement(CmpIconButton, {
        icon: "ArrowBack",
        contentDescription: "\u041D\u0430\u0437\u0430\u0434",
        onClick: onBack
      })
    }), /*#__PURE__*/React.createElement(UpdateBody, {
      c: c,
      mode: mode,
      from: from,
      notes: notes,
      notesOpen0: notesOpen0,
      layout: layout,
      onLater: onBack,
      bg: true
    }), /*#__PURE__*/React.createElement(UpdateDialogs, {
      c: c
    }));
  }
  // Совместимость со старым API.
  function UpdateFlow({
    mode,
    onLater,
    platform = "win"
  }) {
    const c = useUpdateCtl({
      platform
    });
    return /*#__PURE__*/React.createElement(UpdateBody, {
      c: c,
      mode: mode,
      onLater: onLater
    });
  }

  // ── Входы на главном ────────────────────────────────────────────────────────
  // Desktop: блок внизу бокового бара — доступна / загрузка / загружено.
  function SideUpdateBlock({
    c,
    onOpen
  }) {
    if (!c) return null;
    const box = {
      boxSizing: "border-box",
      borderRadius: "var(--r-card-small-radius)",
      padding: "12px 14px",
      display: "flex",
      flexDirection: "column",
      gap: 10
    };
    const keys = fn => e => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        fn();
      }
    };
    if (c.s === "available") return /*#__PURE__*/React.createElement("div", {
      role: "button",
      tabIndex: 0,
      className: "au-hovp au-f au-in",
      onClick: onOpen,
      onKeyDown: keys(onOpen),
      style: {
        ...box,
        flexDirection: "row",
        alignItems: "center",
        gap: 12,
        minHeight: 64,
        background: "var(--brand-primary-highlight)",
        color: "var(--on-primary-container)",
        cursor: "pointer"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "system_update",
      size: 24,
      color: "currentColor"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minWidth: 0
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      weight: 600,
      color: "currentColor"
    }, "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \u043D\u043E\u0432\u0430\u044F \u0432\u0435\u0440\u0441\u0438\u044F"), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelMedium",
      color: "currentColor"
    }, `${c.ch.v} · нажмите, чтобы обновить`))), /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "chevron_right",
      size: 22,
      color: "currentColor"
    }));
    if (c.s === "downloading") {
      const p = c.sizeMB ? c.dlMB / c.sizeMB : null;
      return /*#__PURE__*/React.createElement("div", {
        role: "button",
        tabIndex: 0,
        className: "au-hov au-f au-in",
        onClick: onOpen,
        onKeyDown: keys(onOpen),
        style: {
          ...box,
          background: "var(--background-surface-ultra-highlight)",
          border: "1px solid var(--border-highlight)",
          color: "var(--on-surface)",
          cursor: "pointer"
        }
      }, /*#__PURE__*/React.createElement("div", {
        style: {
          display: "flex",
          alignItems: "center",
          gap: 12
        }
      }, /*#__PURE__*/React.createElement(CmpIcon, {
        icon: "download",
        size: 24,
        color: "var(--primary)"
      }), /*#__PURE__*/React.createElement("div", {
        style: {
          flex: 1,
          minWidth: 0
        }
      }, /*#__PURE__*/React.createElement(CmpText, {
        variant: "labelLarge",
        weight: 600
      }, `Загрузка ${c.ch.v}`), /*#__PURE__*/React.createElement("div", {
        style: {
          fontVariantNumeric: "tabular-nums"
        }
      }, /*#__PURE__*/React.createElement(CmpText, {
        variant: "labelMedium",
        color: "var(--on-surface-variant)"
      }, p != null ? `${Math.round(c.dlMB)} из ${c.sizeMB} МБ · ${Math.round(p * 100)} %` : `${Math.round(c.dlMB)} МБ загружено`)))), /*#__PURE__*/React.createElement(CmpLinearProgressIndicator, {
        progress: p,
        height: 6
      }));
    }
    if (c.s === "downloaded") return /*#__PURE__*/React.createElement("div", {
      className: "au-in",
      style: {
        ...box,
        background: "var(--brand-primary-highlight)",
        border: "1px solid var(--border-highlight)",
        color: "var(--foreground-on-dark-primary-default)"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 12
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "download_done",
      size: 24,
      color: "currentColor"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minWidth: 0
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      weight: 600,
      color: "currentColor"
    }, "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043D\u043E"), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelMedium",
      color: "currentColor"
    }, `Версия ${c.ch.v} готова к установке`)))), /*#__PURE__*/React.createElement(CmpButton, {
      text: "\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C",
      icon: "restart_alt",
      fullWidth: true,
      onClick: c.install,
      style: B48
    }));
    if (c.s === "installing") return /*#__PURE__*/React.createElement("div", {
      className: "au-in",
      style: {
        ...box,
        flexDirection: "row",
        alignItems: "center",
        gap: 12,
        minHeight: 64,
        background: "var(--background-surface-ultra-highlight)",
        border: "1px solid var(--border-highlight)"
      }
    }, /*#__PURE__*/React.createElement(CmpCircularProgressIndicator, {
      size: 22,
      strokeWidth: 2.5
    }), /*#__PURE__*/React.createElement(CmpText, {
      variant: "labelLarge",
      weight: 600
    }, "\u0423\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0435\u043C \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435\u2026"));
    return null;
  }
  // Desktop: уведомление при старте, один раз за запуск.
  function UpdateToast({
    latest,
    onUpdate,
    onClose
  }) {
    return /*#__PURE__*/React.createElement("div", {
      className: "au-in",
      style: {
        width: "100%",
        maxWidth: 480
      }
    }, /*#__PURE__*/React.createElement(CmpSnackbar, {
      message: `Доступна версия ${latest}`,
      actionLabel: "\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C",
      onActionClick: onUpdate,
      withDismissAction: true,
      onDismiss: onClose
    }));
  }
  // Телефон/планшет: закрываемая плашка на главном. Скрыта до следующей новой версии.
  function UpdateBanner({
    latest,
    onOpen,
    onClose
  }) {
    return /*#__PURE__*/React.createElement("div", {
      role: "button",
      tabIndex: 0,
      className: "au-hovp au-f au-in",
      onClick: onOpen,
      onKeyDown: e => {
        if (e.key === "Enter") onOpen();
      },
      style: {
        display: "flex",
        alignItems: "center",
        gap: 14,
        minHeight: 72,
        padding: "10px 4px 10px 16px",
        boxSizing: "border-box",
        borderRadius: "var(--r-card-small-radius)",
        border: "1px solid var(--border-highlight)",
        background: "var(--brand-primary-highlight)",
        color: "var(--on-primary-container)",
        cursor: "pointer"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "system_update",
      size: 28,
      color: "currentColor"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minWidth: 0,
        display: "flex",
        flexDirection: "column",
        gap: 2
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "titleMedium",
      color: "currentColor"
    }, `Доступна новая версия ${latest}`), /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyMedium",
      color: "currentColor"
    }, "\u0427\u0442\u043E \u043D\u043E\u0432\u043E\u0433\u043E \u0438 \u043A\u0430\u043A \u043E\u0431\u043D\u043E\u0432\u0438\u0442\u044C\u0441\u044F")), /*#__PURE__*/React.createElement("button", {
      type: "button",
      className: "au-f",
      "aria-label": "\u0421\u043A\u0440\u044B\u0442\u044C",
      title: "\u0421\u043A\u0440\u044B\u0442\u044C",
      onClick: e => {
        e.stopPropagation();
        onClose();
      },
      style: {
        width: 48,
        height: 48,
        border: "none",
        borderRadius: 999,
        background: "transparent",
        color: "currentColor",
        display: "grid",
        placeItems: "center",
        cursor: "pointer",
        flex: "0 0 auto"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "close",
      size: 24,
      color: "currentColor"
    })));
  }
  // Телефон/планшет: пункт бокового меню с точкой, когда есть новая версия.
  function MenuUpdateRow({
    dot,
    onClick
  }) {
    return /*#__PURE__*/React.createElement("div", {
      role: "button",
      tabIndex: 0,
      className: "au-hov au-f",
      onClick: onClick,
      onKeyDown: e => {
        if (e.key === "Enter") onClick && onClick();
      },
      style: {
        boxSizing: "border-box",
        minHeight: 52,
        display: "flex",
        alignItems: "center",
        gap: 16,
        padding: "0 12px",
        borderRadius: 12,
        cursor: "pointer",
        color: "var(--foreground-on-dark-primary-default)"
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "system_update",
      size: 24,
      color: "var(--on-surface-variant)"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minWidth: 0
      }
    }, /*#__PURE__*/React.createElement(CmpText, {
      variant: "bodyLarge"
    }, "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u044F")), dot && /*#__PURE__*/React.createElement("span", {
      "aria-label": "\u0415\u0441\u0442\u044C \u043D\u043E\u0432\u0430\u044F \u0432\u0435\u0440\u0441\u0438\u044F",
      style: {
        width: 10,
        height: 10,
        borderRadius: 999,
        background: "var(--brand-primary-default)",
        flex: "0 0 auto"
      }
    }));
  }
  // Топбар главного: значок «Нужно обновить приложение» (база настроек требует более новую версию).
  function RequiredUpdateButton({
    onClick
  }) {
    return /*#__PURE__*/React.createElement("span", {
      style: {
        position: "relative",
        display: "inline-flex"
      }
    }, /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "system_update",
      contentDescription: "\u041D\u0443\u0436\u043D\u043E \u043E\u0431\u043D\u043E\u0432\u0438\u0442\u044C \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435",
      onClick: onClick
    }), /*#__PURE__*/React.createElement("span", {
      "aria-hidden": "true",
      style: {
        position: "absolute",
        top: 8,
        right: 8,
        width: 10,
        height: 10,
        borderRadius: 999,
        background: "var(--brand-primary-default)",
        pointerEvents: "none"
      }
    }));
  }

  // ── Витрины ─────────────────────────────────────────────────────────────────
  const DF = "var(--font-sans)";
  function Seg({
    items,
    value,
    onChange
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "center",
        background: "#fff",
        borderRadius: 22,
        padding: 4,
        boxShadow: "0 1px 3px rgba(0,0,0,.18)"
      }
    }, items.map(([k, l]) => /*#__PURE__*/React.createElement("button", {
      key: k,
      onClick: () => onChange(k),
      style: {
        border: "none",
        background: value === k ? "#0A1A3A" : "transparent",
        color: value === k ? "#fff" : "#4A5874",
        padding: "8px 14px",
        borderRadius: 999,
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        fontWeight: 500,
        cursor: "pointer",
        whiteSpace: "nowrap"
      }
    }, l)));
  }
  function Fit({
    w,
    h,
    max = 1,
    children
  }) {
    const ref = useRef(null);
    const [s, setS] = useState(max);
    useEffect(() => {
      const el = ref.current;
      const m = () => setS(Math.min(max, el.offsetWidth / w));
      const ro = new ResizeObserver(m);
      ro.observe(el);
      m();
      return () => ro.disconnect();
    }, [w, max]);
    return /*#__PURE__*/React.createElement("div", {
      ref: ref,
      style: {
        width: "100%",
        display: "flex",
        justifyContent: "center"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: w * s,
        height: h * s,
        flex: "0 0 auto"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: w,
        height: h,
        transform: `scale(${s})`,
        transformOrigin: "top left"
      }
    }, children)));
  }
  function DeskWindow({
    w = 1280,
    h = 800,
    os = "win",
    dark,
    children
  }) {
    const bar = dark ? "#2a2e30" : "#e3e5e6",
      fg = dark ? "#e2e4e5" : "#262626";
    const dot = c => /*#__PURE__*/React.createElement("span", {
      style: {
        width: 12,
        height: 12,
        borderRadius: 999,
        background: c
      }
    });
    return /*#__PURE__*/React.createElement("div", {
      style: {
        width: w,
        height: h + 36,
        borderRadius: 10,
        overflow: "hidden",
        border: "1px solid rgba(0,0,0,.25)",
        boxShadow: "0 30px 80px rgba(0,0,0,.25)",
        display: "flex",
        flexDirection: "column",
        boxSizing: "border-box"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        height: 36,
        flex: "0 0 auto",
        background: bar,
        display: "flex",
        alignItems: "center",
        gap: 8,
        padding: "0 12px",
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        color: fg
      }
    }, os === "mac" && /*#__PURE__*/React.createElement("span", {
      style: {
        display: "flex",
        gap: 8,
        marginRight: 8
      }
    }, dot("#ff5f57"), dot("#febc2e"), dot("#28c840")), /*#__PURE__*/React.createElement("span", {
      style: {
        fontWeight: 600
      }
    }, "Pandora \u0421\u043F\u0435\u0446"), /*#__PURE__*/React.createElement("span", {
      style: {
        flex: 1
      }
    }), os === "win" && /*#__PURE__*/React.createElement("span", {
      style: {
        display: "flex",
        gap: 18,
        opacity: 0.7
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "remove",
      size: 16,
      color: fg
    }), /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "crop_square",
      size: 16,
      color: fg
    }), /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "close",
      size: 16,
      color: fg
    }))), /*#__PURE__*/React.createElement("div", {
      "data-theme": dark ? "dark" : undefined,
      style: {
        flex: 1,
        minHeight: 0,
        background: "var(--background)"
      }
    }, children));
  }
  function Phone({
    dark,
    w = 412,
    h = 840,
    children
  }) {
    const c = dark ? "#fff" : "#1A1C1C";
    return /*#__PURE__*/React.createElement("div", {
      style: {
        width: w,
        height: h,
        borderRadius: 22,
        overflow: "hidden",
        border: "8px solid rgba(116,119,117,.5)",
        boxShadow: "0 30px 80px rgba(0,0,0,.25)",
        display: "flex",
        flexDirection: "column",
        boxSizing: "border-box",
        background: "#000"
      }
    }, /*#__PURE__*/React.createElement("div", {
      "data-theme": dark ? "dark" : undefined,
      style: {
        flex: 1,
        minHeight: 0,
        display: "flex",
        flexDirection: "column",
        background: "var(--background)"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        height: 32,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "0 18px",
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        color: c,
        flex: "0 0 auto"
      }
    }, /*#__PURE__*/React.createElement("span", null, "9:30"), /*#__PURE__*/React.createElement("span", {
      style: {
        display: "flex",
        gap: 4
      }
    }, /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "signal_cellular_alt",
      size: 16,
      color: c
    }), /*#__PURE__*/React.createElement(CmpIcon, {
      icon: "battery_full",
      size: 16,
      color: c
    }))), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minHeight: 0
      }
    }, children), /*#__PURE__*/React.createElement("div", {
      style: {
        height: 20,
        display: "grid",
        placeItems: "center",
        flex: "0 0 auto"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: 108,
        height: 4,
        borderRadius: 2,
        background: c,
        opacity: 0.4
      }
    }))));
  }
  const DESK_STATES = [["checking", "Проверка"], ["available", "Доступна"], ["downloading", "Загрузка"], ["downloaded", "Загружено"], ["installing", "Установка"], ["uptodate", "Последняя версия"], ["older", "В канале старше"], ["err_net", "Нет сети"], ["err_server", "Ошибка сервера"], ["err_empty", "Канал пуст"], ["err_download", "Сбой загрузки"], ["err_file", "Файл не подходит"]];
  const MOB_STATES = [["checking", "Проверка"], ["available", "Доступна"], ["uptodate", "Последняя версия"], ["err_net", "Нет сети"], ["err_server", "Ошибка сервера"]];
  // Стартовые данные под закреплённое состояние.
  function preset(st, chans) {
    if (st === "uptodate") return {
      inst0: "cur"
    };
    if (st === "older") return {
      inst0: "beta",
      ch0: "rel"
    };
    if (st === "err_empty") return {
      chans: "many",
      ch0: "can"
    };
    return {
      chans
    };
  }
  const UPDATE_STATES = DESK_STATES.map(([k]) => k);
  function useDemoTheme() {
    const [dark, setDark] = useState(() => {
      try {
        return localStorage.getItem("ps-theme") === "dark";
      } catch (e) {
        return false;
      }
    });
    useEffect(() => {
      document.body.style.background = dark ? "#101314" : "#c9ccce";
      try {
        localStorage.setItem("ps-theme", dark ? "dark" : "light");
      } catch (e) {}
    }, [dark]);
    return [dark, setDark];
  }
  function AppUpdateDeskDemo() {
    const [dark, setDark] = useDemoTheme();
    const [c, setC] = useState({
      os: "win",
      run: "proto",
      st: "available",
      layout: "two",
      chans: "three",
      size: "unk",
      busy: "off",
      notes: "long",
      from: "menu",
      dlg: "none",
      win: "dk"
    });
    const set = k => v => setC(s => ({
      ...s,
      [k]: v
    }));
    const W = c.win === "dk" ? [1280, 800] : [800, 600];
    const pin = c.run === "states";
    const pr = pin ? preset(c.st, c.chans) : {
      chans: c.chans
    };
    const props = {
      platform: c.os,
      initial: pin ? c.st : "auto",
      chans: c.chans,
      ...pr,
      sizeKnown: c.size === "known",
      busy: c.busy === "on",
      dialog0: pin && c.dlg !== "none" ? c.dlg : null,
      notes: c.notes,
      from: c.from,
      layout: c.layout,
      onBack: () => window.psGo && window.psGo("home-desktop")
    };
    const key = JSON.stringify([c, pin]);
    const row = {
      display: "flex",
      gap: 10,
      flexWrap: "wrap",
      justifyContent: "center",
      maxWidth: 1240
    };
    return /*#__PURE__*/React.createElement("div", {
      style: {
        minHeight: "100vh",
        padding: "20px 24px 32px",
        boxSizing: "border-box",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 14
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: [["l", "Светлая"], ["d", "Тёмная"]],
      value: dark ? "d" : "l",
      onChange: v => setDark(v === "d")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["win", "Windows"], ["mac", "macOS"]],
      value: c.os,
      onChange: set("os")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["proto", "Прототип"], ["states", "Состояния"]],
      value: c.run,
      onChange: set("run")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["dk", "Окно 1280×800"], ["dmin", "Мин. 800×600"]],
      value: c.win,
      onChange: set("win")
    })), pin && /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: DESK_STATES,
      value: c.st,
      onChange: set("st")
    })), /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: [["one", "1 канал"], ["three", "3 канала"], ["many", "6 каналов, длинные"]],
      value: c.chans,
      onChange: set("chans")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["two", "Каналы справа"], ["one", "Каналы ниже"]],
      value: c.layout,
      onChange: set("layout")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["unk", "Размер неизвестен"], ["known", "Размер известен"]],
      value: c.size,
      onChange: set("size")
    })), /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: [["long", "«Что нового» длинное"], ["short", "короткое"], ["empty", "пустое"]],
      value: c.notes,
      onChange: set("notes")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["menu", "Открыт из меню"], ["toast", "из уведомления"]],
      value: c.from,
      onChange: set("from")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["off", "Блок не подключён"], ["on", "Идёт работа с блоком"]],
      value: c.busy,
      onChange: set("busy")
    }), pin && /*#__PURE__*/React.createElement(Seg, {
      items: [["none", "Без диалога"], ["busy", "Диалог: работа с блоком"], ["beta", "Диалог: переход на бету"]],
      value: c.dlg,
      onChange: set("dlg")
    }), /*#__PURE__*/React.createElement("button", {
      onClick: () => window.psGo && window.psGo("home-desktop"),
      style: {
        border: "none",
        background: "#fff",
        borderRadius: 999,
        padding: "8px 14px",
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        fontWeight: 500,
        color: "#1A3E7A",
        cursor: "pointer",
        boxShadow: "0 1px 3px rgba(0,0,0,.18)"
      }
    }, "\u0413\u043B\u0430\u0432\u043D\u044B\u0439 \u044D\u043A\u0440\u0430\u043D \u2192")), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        color: dark ? "#c7cccf" : "#444",
        maxWidth: 820,
        textAlign: "center"
      }
    }, "\u041F\u0440\u043E\u0442\u043E\u0442\u0438\u043F: \xAB\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C\xBB \u2192 \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \u2192 \xAB\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C \u0438 \u043F\u0435\u0440\u0435\u0437\u0430\u043F\u0443\u0441\u0442\u0438\u0442\u044C\xBB. \u0421\u043C\u0435\u043D\u0430 \u043A\u0430\u043D\u0430\u043B\u0430 \u0437\u0430\u043F\u0443\u0441\u043A\u0430\u0435\u0442 \u043F\u0440\u043E\u0432\u0435\u0440\u043A\u0443; \u043F\u0435\u0440\u0435\u0445\u043E\u0434 \u043D\u0430 \u043D\u0435-\u0440\u0435\u043B\u0438\u0437\u043D\u044B\u0439 \u043A\u0430\u043D\u0430\u043B \u2014 \u0447\u0435\u0440\u0435\u0437 \u0434\u0438\u0430\u043B\u043E\u0433. \xAB\u0418\u0434\u0451\u0442 \u0440\u0430\u0431\u043E\u0442\u0430 \u0441 \u0431\u043B\u043E\u043A\u043E\u043C\xBB \u0432\u043A\u043B\u044E\u0447\u0430\u0435\u0442 \u043F\u0440\u0435\u0434\u0443\u043F\u0440\u0435\u0436\u0434\u0435\u043D\u0438\u0435 \u043F\u0435\u0440\u0435\u0434 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u043E\u0439."), /*#__PURE__*/React.createElement(Fit, {
      w: W[0],
      h: W[1] + 36
    }, /*#__PURE__*/React.createElement(DeskWindow, {
      w: W[0],
      h: W[1],
      os: c.os,
      dark: dark
    }, /*#__PURE__*/React.createElement(AppUpdateApp, _extends({
      key: key
    }, props)))));
  }
  function AppUpdatePhoneDemo() {
    const [dark, setDark] = useDemoTheme();
    const [c, setC] = useState({
      os: "android",
      run: "proto",
      st: "available",
      notes: "long",
      from: "menu",
      dev: "phone"
    });
    const set = k => v => setC(s => ({
      ...s,
      [k]: v
    }));
    const pin = c.run === "states";
    const props = {
      platform: c.os,
      initial: pin ? c.st : "auto",
      ...(pin ? preset(c.st) : {}),
      notes: c.notes,
      from: c.from,
      onBack: () => window.psGo && window.psGo(c.dev === "tablet" ? "home-tablet" : "home")
    };
    const key = JSON.stringify([c]);
    const row = {
      display: "flex",
      gap: 10,
      flexWrap: "wrap",
      justifyContent: "center",
      maxWidth: 1000
    };
    const app = /*#__PURE__*/React.createElement(AppUpdateApp, _extends({
      key: key
    }, props));
    return /*#__PURE__*/React.createElement("div", {
      style: {
        minHeight: "100vh",
        padding: "20px 16px 32px",
        boxSizing: "border-box",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 14
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: [["l", "Светлая"], ["d", "Тёмная"]],
      value: dark ? "d" : "l",
      onChange: v => setDark(v === "d")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["android", "Android"], ["ios", "iOS"]],
      value: c.os,
      onChange: set("os")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["phone", "Телефон 412×840"], ["tablet", "Планшет 800×1280"]],
      value: c.dev,
      onChange: set("dev")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["proto", "Прототип"], ["states", "Состояния"]],
      value: c.run,
      onChange: set("run")
    })), pin && /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: MOB_STATES,
      value: c.st,
      onChange: set("st")
    })), /*#__PURE__*/React.createElement("div", {
      style: row
    }, /*#__PURE__*/React.createElement(Seg, {
      items: [["long", "«Что нового» длинное"], ["short", "короткое"], ["empty", "пустое"]],
      value: c.notes,
      onChange: set("notes")
    }), /*#__PURE__*/React.createElement(Seg, {
      items: [["menu", "Открыт из меню"], ["banner", "из плашки на главном"]],
      value: c.from,
      onChange: set("from")
    })), c.dev === "phone" ? /*#__PURE__*/React.createElement(Phone, {
      dark: dark
    }, app) : /*#__PURE__*/React.createElement(Fit, {
      w: 800,
      h: 1280,
      max: 0.7
    }, /*#__PURE__*/React.createElement(Phone, {
      dark: dark,
      w: 800,
      h: 1280
    }, app)));
  }
  // Холст всех состояний.
  function Cap({
    children
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        fontWeight: 600,
        color: "#262626",
        minHeight: 20
      }
    }, children);
  }
  function Group({
    title,
    note,
    children
  }) {
    return /*#__PURE__*/React.createElement("section", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 12,
        width: "100%",
        maxWidth: 1760
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: DF,
        fontSize: "var(--t-headline2-size)",
        fontWeight: 700,
        color: "#1A1C1C"
      }
    }, title), note && /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: DF,
        fontSize: "var(--t-additional0-size)",
        color: "#3b3f41",
        maxWidth: 900
      }
    }, note), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexWrap: "wrap",
        gap: 24,
        alignItems: "flex-start"
      }
    }, children));
  }
  function D({
    cap,
    dark,
    os = "win",
    w = 1280,
    h = 800,
    scale = 0.4,
    ...p
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 8,
        width: w * scale
      }
    }, /*#__PURE__*/React.createElement(Cap, null, cap), /*#__PURE__*/React.createElement("div", {
      style: {
        width: w * scale,
        height: (h + 36) * scale
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: w,
        height: h + 36,
        transform: `scale(${scale})`,
        transformOrigin: "top left"
      }
    }, /*#__PURE__*/React.createElement(DeskWindow, {
      w: w,
      h: h,
      os: os,
      dark: dark
    }, /*#__PURE__*/React.createElement(AppUpdateApp, _extends({
      platform: os
    }, p))))));
  }
  function M({
    cap,
    dark,
    scale = 0.55,
    w = 412,
    h = 840,
    ...p
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 8,
        width: w * scale
      }
    }, /*#__PURE__*/React.createElement(Cap, null, cap), /*#__PURE__*/React.createElement("div", {
      style: {
        width: w * scale,
        height: h * scale
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: w,
        height: h,
        transform: `scale(${scale})`,
        transformOrigin: "top left"
      }
    }, /*#__PURE__*/React.createElement(Phone, {
      dark: dark,
      w: w,
      h: h
    }, /*#__PURE__*/React.createElement(AppUpdateApp, p)))));
  }
  function Piece({
    cap,
    dark,
    w = 360,
    children
  }) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 8,
        width: w
      }
    }, /*#__PURE__*/React.createElement(Cap, null, cap), /*#__PURE__*/React.createElement("div", {
      "data-theme": dark ? "dark" : undefined,
      style: {
        padding: 16,
        borderRadius: 16,
        background: "var(--surface)",
        color: "var(--foreground-on-dark-primary-default)",
        border: "1px solid var(--border-highlight)",
        display: "flex",
        flexDirection: "column",
        gap: 10
      }
    }, children));
  }
  function PieceCtl({
    initial,
    sizeKnown,
    children
  }) {
    const c = useUpdateCtl({
      platform: "win",
      initial,
      sizeKnown
    });
    return children(c);
  }
  function AppUpdateStatesDemo() {
    const P = (st, x = {}) => ({
      initial: st,
      ...preset(st, "three"),
      ...x
    });
    return /*#__PURE__*/React.createElement("div", {
      style: {
        minHeight: "100vh",
        padding: "24px 24px 48px",
        boxSizing: "border-box",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 40,
        background: "#c9ccce"
      }
    }, /*#__PURE__*/React.createElement(Group, {
      title: "Desktop \xB7 \u0441\u043E\u0441\u0442\u043E\u044F\u043D\u0438\u044F \xB7 Windows, \u0441\u0432\u0435\u0442\u043B\u0430\u044F, 3 \u043A\u0430\u043D\u0430\u043B\u0430 (\u043A\u0430\u043D\u0430\u043B\u044B \u0441\u043F\u0440\u0430\u0432\u0430)"
    }, /*#__PURE__*/React.createElement(D, _extends({
      cap: "1 \u041F\u0440\u043E\u0432\u0435\u0440\u043A\u0430"
    }, P("checking"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "2 \u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \u043D\u043E\u0432\u0430\u044F \u0432\u0435\u0440\u0441\u0438\u044F \xB7 \xAB\u0427\u0442\u043E \u043D\u043E\u0432\u043E\u0433\u043E\xBB \u0441\u0432\u0451\u0440\u043D\u0443\u0442\u043E"
    }, P("available"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "2 \u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \xB7 \u0438\u0437 \u0443\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u044F (\u0435\u0441\u0442\u044C \xAB\u041F\u043E\u0437\u0436\u0435\xBB), \u0442\u0435\u043A\u0441\u0442 \u0440\u0430\u0441\u043A\u0440\u044B\u0442"
    }, P("available", {
      from: "toast",
      notesOpen0: true
    }), {
      h: 1100
    })), /*#__PURE__*/React.createElement(D, _extends({
      cap: "2 \u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \xB7 \xAB\u0427\u0442\u043E \u043D\u043E\u0432\u043E\u0433\u043E\xBB \u043F\u0443\u0441\u0442\u043E\u0435, \u0440\u0430\u0437\u043C\u0435\u0440 \u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043D"
    }, P("available", {
      notes: "empty",
      sizeKnown: true
    }))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "3 \u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \xB7 \u0440\u0430\u0437\u043C\u0435\u0440 \u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043D"
    }, P("downloading", {
      sizeKnown: true
    }))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "3 \u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \xB7 \u0440\u0430\u0437\u043C\u0435\u0440 \u043D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043D (\u0441\u0435\u0439\u0447\u0430\u0441 \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438)"
    }, P("downloading"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "4 \u0413\u043E\u0442\u043E\u0432\u043E \u043A \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0435 \xB7 Windows"
    }, P("downloaded"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "4 \u0413\u043E\u0442\u043E\u0432\u043E \u043A \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0435 \xB7 macOS",
      os: "mac"
    }, P("downloaded"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "5 \u0423\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0430 \u0437\u0430\u043F\u0443\u0449\u0435\u043D\u0430"
    }, P("installing"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "6 \u0423\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0430 \u043F\u043E\u0441\u043B\u0435\u0434\u043D\u044F\u044F \u0432\u0435\u0440\u0441\u0438\u044F"
    }, P("uptodate")))), /*#__PURE__*/React.createElement(Group, {
      title: "Desktop \xB7 \u043E\u0448\u0438\u0431\u043A\u0438"
    }, /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u041D\u0435\u0442 \u0441\u0435\u0442\u0438"
    }, P("err_net"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u041E\u0448\u0438\u0431\u043A\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0430"
    }, P("err_server"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u0412 \u043A\u0430\u043D\u0430\u043B\u0435 \u043D\u0435\u0442 \u0441\u0431\u043E\u0440\u043E\u043A (6 \u043A\u0430\u043D\u0430\u043B\u043E\u0432)"
    }, P("err_empty"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \u043F\u0440\u0435\u0440\u0432\u0430\u043B\u0430\u0441\u044C"
    }, P("err_download"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u0424\u0430\u0439\u043B \u043D\u0435 \u043F\u043E\u0434\u0445\u043E\u0434\u0438\u0442"
    }, P("err_file")))), /*#__PURE__*/React.createElement(Group, {
      title: "Desktop \xB7 \u0434\u0438\u0430\u043B\u043E\u0433\u0438"
    }, /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u0418\u0434\u0451\u0442 \u0440\u0430\u0431\u043E\u0442\u0430 \u0441 \u0431\u043B\u043E\u043A\u043E\u043C \u2192 \xAB\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C \u0438 \u043F\u0435\u0440\u0435\u0437\u0430\u043F\u0443\u0441\u0442\u0438\u0442\u044C\xBB"
    }, P("downloaded", {
      dialog0: "busy"
    }))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u041F\u0435\u0440\u0435\u0445\u043E\u0434 \u043D\u0430 \xAB\u0411\u0435\u0442\u0430\xBB"
    }, P("available", {
      dialog0: "beta"
    })))), /*#__PURE__*/React.createElement(Group, {
      title: "Desktop \xB7 \u043A\u0430\u043D\u0430\u043B\u044B",
      note: "\u041E\u0434\u0438\u043D \u043A\u0430\u043D\u0430\u043B \u2014 \u0441\u0435\u043A\u0446\u0438\u0438 \u043D\u0435\u0442. \u0421\u043C\u0435\u043D\u0430 \u043A\u0430\u043D\u0430\u043B\u0430 \u0441\u0440\u0430\u0437\u0443 \u0437\u0430\u043F\u0443\u0441\u043A\u0430\u0435\u0442 \u043F\u0440\u043E\u0432\u0435\u0440\u043A\u0443; \u043F\u0435\u0440\u0435\u0445\u043E\u0434 \u043D\u0430 \u043D\u0435-\u0440\u0435\u043B\u0438\u0437\u043D\u044B\u0439 \u043A\u0430\u043D\u0430\u043B \u2014 \u0447\u0435\u0440\u0435\u0437 \u043A\u043E\u0440\u043E\u0442\u043A\u0438\u0439 \u0434\u0438\u0430\u043B\u043E\u0433. \u041E\u0442\u043A\u0430\u0442\u0430 \u043D\u0435\u0442: \u0435\u0441\u043B\u0438 \u0432 \u043A\u0430\u043D\u0430\u043B\u0435 \u0432\u0435\u0440\u0441\u0438\u044F \u0441\u0442\u0430\u0440\u0448\u0435 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u043D\u043E\u0439, \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u043E\u0441\u0442\u0430\u0451\u0442\u0441\u044F \u043D\u0430 \u0442\u0435\u043A\u0443\u0449\u0435\u0439."
    }, /*#__PURE__*/React.createElement(D, _extends({
      cap: "1 \u043A\u0430\u043D\u0430\u043B \xB7 \u0441\u0435\u043A\u0446\u0438\u0438 \u043D\u0435\u0442"
    }, P("available", {
      chans: "one"
    }))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "6 \u043A\u0430\u043D\u0430\u043B\u043E\u0432, \u0434\u043B\u0438\u043D\u043D\u044B\u0435 \u043D\u0430\u0437\u0432\u0430\u043D\u0438\u044F"
    }, P("available", {
      chans: "many"
    }), {
      h: 900
    })), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u0412\u0435\u0440\u0441\u0438\u044F \u0432 \u043A\u0430\u043D\u0430\u043B\u0435 \u0441\u0442\u0430\u0440\u0448\u0435 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u043D\u043E\u0439 (\u0411\u0435\u0442\u0430 2.2.0 \u2192 \u0420\u0435\u043B\u0438\u0437 2.1.0)"
    }, P("older"))), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u0412\u0430\u0440\u0438\u0430\u043D\u0442 \xAB\u043A\u0430\u043D\u0430\u043B\u044B \u043D\u0438\u0436\u0435\xBB \xB7 \u043E\u0434\u043D\u0430 \u043A\u043E\u043B\u043E\u043D\u043A\u0430"
    }, P("available", {
      layout: "one",
      notes: "short"
    }), {
      h: 1060
    })), /*#__PURE__*/React.createElement(D, _extends({
      cap: "\u041C\u0438\u043D. \u043E\u043A\u043D\u043E 800\xD7600 \xB7 \u043A\u0430\u043D\u0430\u043B\u044B \u0432\u0441\u0435\u0433\u0434\u0430 \u043D\u0438\u0436\u0435",
      w: 800,
      h: 600,
      scale: 0.55
    }, P("available", {
      notes: "short"
    })))), /*#__PURE__*/React.createElement(Group, {
      title: "Desktop \xB7 \u0442\u0451\u043C\u043D\u0430\u044F \u0442\u0435\u043C\u0430"
    }, /*#__PURE__*/React.createElement(D, _extends({
      dark: true,
      cap: "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430"
    }, P("available"))), /*#__PURE__*/React.createElement(D, _extends({
      dark: true,
      cap: "\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \xB7 \u0440\u0430\u0437\u043C\u0435\u0440 \u043D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043D"
    }, P("downloading"))), /*#__PURE__*/React.createElement(D, _extends({
      dark: true,
      cap: "\u0412\u0435\u0440\u0441\u0438\u044F \u0432 \u043A\u0430\u043D\u0430\u043B\u0435 \u0441\u0442\u0430\u0440\u0448\u0435"
    }, P("older"))), /*#__PURE__*/React.createElement(D, _extends({
      dark: true,
      cap: "\u0414\u0438\u0430\u043B\u043E\u0433 \xAB\u0418\u0434\u0451\u0442 \u0440\u0430\u0431\u043E\u0442\u0430 \u0441 \u0431\u043B\u043E\u043A\u043E\u043C\xBB"
    }, P("downloaded", {
      dialog0: "busy"
    })))), /*#__PURE__*/React.createElement(Group, {
      title: "\u0422\u0435\u043B\u0435\u0444\u043E\u043D \xB7 Android \u0438 iOS",
      note: "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0442\u043E\u043B\u044C\u043A\u043E \u0447\u0435\u0440\u0435\u0437 \u043C\u0430\u0433\u0430\u0437\u0438\u043D, \u043A\u0430\u043D\u0430\u043B\u043E\u0432 \u0438 \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0438 \u043D\u0435\u0442."
    }, /*#__PURE__*/React.createElement(M, {
      cap: "\u041F\u0440\u043E\u0432\u0435\u0440\u043A\u0430",
      platform: "android",
      initial: "checking"
    }), /*#__PURE__*/React.createElement(M, {
      cap: "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \xB7 Android",
      platform: "android",
      initial: "available"
    }), /*#__PURE__*/React.createElement(M, {
      cap: "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \xB7 iOS \xB7 \u0438\u0437 \u043F\u043B\u0430\u0448\u043A\u0438 (\u0435\u0441\u0442\u044C \xAB\u041F\u043E\u0437\u0436\u0435\xBB)",
      platform: "ios",
      initial: "available",
      from: "banner",
      notes: "short"
    }), /*#__PURE__*/React.createElement(M, {
      cap: "\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u044F\u044F \u0432\u0435\u0440\u0441\u0438\u044F",
      platform: "android",
      initial: "uptodate",
      inst0: "cur"
    }), /*#__PURE__*/React.createElement(M, {
      cap: "\u041D\u0435\u0442 \u0441\u0435\u0442\u0438",
      platform: "android",
      initial: "err_net"
    }), /*#__PURE__*/React.createElement(M, {
      cap: "\u041E\u0448\u0438\u0431\u043A\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0430",
      platform: "ios",
      initial: "err_server"
    }), /*#__PURE__*/React.createElement(M, {
      dark: true,
      cap: "\u0422\u0451\u043C\u043D\u0430\u044F \xB7 Android",
      platform: "android",
      initial: "available"
    }), /*#__PURE__*/React.createElement(M, {
      dark: true,
      cap: "\u0422\u0451\u043C\u043D\u0430\u044F \xB7 iOS \xB7 \u043F\u043E\u0441\u043B\u0435\u0434\u043D\u044F\u044F \u0432\u0435\u0440\u0441\u0438\u044F",
      platform: "ios",
      initial: "uptodate",
      inst0: "cur"
    })), /*#__PURE__*/React.createElement(Group, {
      title: "\u041F\u043B\u0430\u043D\u0448\u0435\u0442 800\xD71280 \xB7 \u043A\u0430\u043A \u0442\u0435\u043B\u0435\u0444\u043E\u043D \u0441 \u0431\u043E\u043B\u044C\u0448\u0438\u043C\u0438 \u043E\u0442\u0441\u0442\u0443\u043F\u0430\u043C\u0438"
    }, /*#__PURE__*/React.createElement(M, {
      cap: "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \xB7 Android",
      platform: "android",
      initial: "available",
      w: 800,
      h: 1280,
      scale: 0.4
    }), /*#__PURE__*/React.createElement(M, {
      dark: true,
      cap: "\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \xB7 iOS, \u0442\u0451\u043C\u043D\u0430\u044F",
      platform: "ios",
      initial: "available",
      notes: "short",
      w: 800,
      h: 1280,
      scale: 0.4
    })), /*#__PURE__*/React.createElement(Group, {
      title: "\u0412\u0445\u043E\u0434\u044B \u043D\u0430 \u0433\u043B\u0430\u0432\u043D\u043E\u043C",
      note: "\u0416\u0438\u0432\u044C\u0451\u043C \u2014 \u0432 \u043C\u0430\u043A\u0435\u0442\u0430\u0445 \u0433\u043B\u0430\u0432\u043D\u043E\u0433\u043E \u044D\u043A\u0440\u0430\u043D\u0430 (\u0442\u0435\u043B\u0435\u0444\u043E\u043D, \u043F\u043B\u0430\u043D\u0448\u0435\u0442, Desktop)."
    }, /*#__PURE__*/React.createElement(Piece, {
      cap: "\u0422\u0435\u043B\u0435\u0444\u043E\u043D/\u043F\u043B\u0430\u043D\u0448\u0435\u0442 \xB7 \u043F\u043B\u0430\u0448\u043A\u0430 \u043D\u0430 \u0433\u043B\u0430\u0432\u043D\u043E\u043C (\u0437\u0430\u043A\u0440\u044B\u0432\u0430\u0435\u043C\u0430\u044F)"
    }, /*#__PURE__*/React.createElement(UpdateBanner, {
      latest: "2.1.0",
      onOpen: () => {},
      onClose: () => {}
    })), /*#__PURE__*/React.createElement(Piece, {
      cap: "\u0422\u0435\u043B\u0435\u0444\u043E\u043D/\u043F\u043B\u0430\u043D\u0448\u0435\u0442 \xB7 \u043F\u0443\u043D\u043A\u0442 \u043C\u0435\u043D\u044E \u0441 \u0442\u043E\u0447\u043A\u043E\u0439"
    }, /*#__PURE__*/React.createElement(MenuUpdateRow, {
      dot: true,
      onClick: () => {}
    })), /*#__PURE__*/React.createElement(Piece, {
      cap: "\u0422\u043E\u043F\u0431\u0430\u0440 \xB7 \xAB\u041D\u0443\u0436\u043D\u043E \u043E\u0431\u043D\u043E\u0432\u0438\u0442\u044C \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435\xBB"
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "flex-end"
      }
    }, /*#__PURE__*/React.createElement(RequiredUpdateButton, {
      onClick: () => {}
    }))), /*#__PURE__*/React.createElement(Piece, {
      cap: "Desktop \xB7 \u0431\u043B\u043E\u043A \u0431\u043E\u043A\u043E\u0432\u043E\u0433\u043E \u0431\u0430\u0440\u0430: \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u0430"
    }, /*#__PURE__*/React.createElement(PieceCtl, {
      initial: "available"
    }, c => /*#__PURE__*/React.createElement(SideUpdateBlock, {
      c: c,
      onOpen: () => {}
    }))), /*#__PURE__*/React.createElement(Piece, {
      cap: "Desktop \xB7 \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0430 (\u0440\u0430\u0437\u043C\u0435\u0440 \u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043D / \u043D\u0435\u0442)"
    }, /*#__PURE__*/React.createElement(PieceCtl, {
      initial: "downloading",
      sizeKnown: true
    }, c => /*#__PURE__*/React.createElement(SideUpdateBlock, {
      c: c,
      onOpen: () => {}
    })), /*#__PURE__*/React.createElement(PieceCtl, {
      initial: "downloading"
    }, c => /*#__PURE__*/React.createElement(SideUpdateBlock, {
      c: c,
      onOpen: () => {}
    }))), /*#__PURE__*/React.createElement(Piece, {
      cap: "Desktop \xB7 \u0433\u043E\u0442\u043E\u0432\u043E \u043A \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0435"
    }, /*#__PURE__*/React.createElement(PieceCtl, {
      initial: "downloaded"
    }, c => /*#__PURE__*/React.createElement(SideUpdateBlock, {
      c: c,
      onOpen: () => {}
    }))), /*#__PURE__*/React.createElement(Piece, {
      cap: "Desktop \xB7 \u0443\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u0435 \u043F\u0440\u0438 \u0441\u0442\u0430\u0440\u0442\u0435",
      w: 480
    }, /*#__PURE__*/React.createElement(UpdateToast, {
      latest: "2.1.0",
      onUpdate: () => {},
      onClose: () => {}
    })), /*#__PURE__*/React.createElement(Piece, {
      dark: true,
      cap: "\u0422\u0451\u043C\u043D\u0430\u044F \xB7 \u043F\u043B\u0430\u0448\u043A\u0430 \u0438 \u043F\u0443\u043D\u043A\u0442 \u043C\u0435\u043D\u044E"
    }, /*#__PURE__*/React.createElement(UpdateBanner, {
      latest: "2.1.0",
      onOpen: () => {},
      onClose: () => {}
    }), /*#__PURE__*/React.createElement(MenuUpdateRow, {
      dot: true,
      onClick: () => {}
    })), /*#__PURE__*/React.createElement(Piece, {
      dark: true,
      cap: "\u0422\u0451\u043C\u043D\u0430\u044F \xB7 \u0431\u043B\u043E\u043A \u0431\u043E\u043A\u043E\u0432\u043E\u0433\u043E \u0431\u0430\u0440\u0430"
    }, /*#__PURE__*/React.createElement(PieceCtl, {
      initial: "downloaded"
    }, c => /*#__PURE__*/React.createElement(SideUpdateBlock, {
      c: c,
      onOpen: () => {}
    })))));
  }
  window.AppUpdate = {
    useUpdateCtl,
    useAuMode,
    UpdateBody,
    UpdateRoute,
    UpdateDialogs,
    AppUpdateApp,
    UpdateFlow,
    UpdateScreen: UpdateBody,
    VerPill,
    SideUpdateBlock,
    UpdateToast,
    UpdateBanner,
    MenuUpdateRow,
    RequiredUpdateButton,
    AppUpdateDeskDemo,
    AppUpdatePhoneDemo,
    AppUpdateStatesDemo,
    UPDATE_STATES,
    CHANNELS,
    NOTES
  };
  // Для .dc.html: скомпилированный _ds_bundle.js содержит старый window.AppUpdate, поэтому витрины — отдельными глобалами.
  window.AuPhoneDemo = AppUpdatePhoneDemo;
  window.AuStatesDemo = AppUpdateStatesDemo;
})();

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
} = window.AuthScreens || {};

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
const BIG_BTN = {};
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
        fontSize: "var(--t-additional0-size)",
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
      background: "var(--error-highlight)",
      color: "var(--error-default)",
      border: "1px solid var(--error-default)"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "AlertCircle",
    color: "var(--error-default)"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodyMedium",
    color: "var(--foreground-on-dark-primary-default)"
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
      borderRadius: "var(--r-card-large-radius)",
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
      background: "var(--brand-primary-highlight)",
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: "CheckCircle",
    size: 40,
    color: "var(--brand-primary-default)"
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
      borderRadius: "var(--r-card-large-radius)",
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
  type: "Сигнализация",
  mode: "normal",
  phoneLink: true,
  bound: true,
  icon: "sensors"
}, {
  id: "dxl5000",
  name: "Pandora DXL-5000",
  transport: "ble",
  address: "C8:FD:19:22:8B:04",
  rssi: -67,
  type: "Сигнализация",
  mode: "pairing",
  phoneLink: false,
  bound: false,
  icon: "sensors"
}, {
  id: "rmd5",
  name: "Pandora RMD-5",
  transport: "usb",
  address: "HID · 0483:5750",
  systemId: "107131313131",
  vendor: "NPO Telemetry",
  vidPid: "0483:5750",
  firmware: "3.57",
  mode: "work",
  type: "Модуль",
  icon: "memory"
}, {
  id: "rmd4",
  name: "Pandora RMD-4",
  transport: "usb",
  address: "HID · 0483:5751",
  systemId: null,
  vendor: "NPO Telemetry",
  vidPid: "0483:5751",
  firmware: "3.42",
  mode: "bootloader",
  type: "Модуль",
  icon: "memory"
}, {
  id: "d032",
  name: "Pandora D-032",
  transport: "ble",
  address: "7A:11:0D:5E:C3:90",
  rssi: -75,
  type: "Метка",
  mode: "normal",
  phoneLink: false,
  bound: false,
  icon: "vpn_key"
}, {
  id: "vx4g",
  name: "Pandora VX-4G",
  transport: "ble",
  address: "3C:2E:FF:90:11:AA",
  rssi: -83,
  type: "Сигнализация",
  mode: "bootloader",
  phoneLink: true,
  bound: true,
  icon: "sensors"
}];

// Поля «до подключения» (значения из эфира / USB-дескриптора).
const MODE_LABEL = {
  normal: "Обычный",
  pairing: "Сопряжение",
  bootloader: "Загрузчик",
  work: "Рабочий"
};
function infoRows(d, platform) {
  const strong = d.mode === "pairing" || d.mode === "bootloader";
  if (d.transport === "usb") {
    return [{
      label: "ID блока (systemId)",
      value: d.systemId || "не определён",
      mono: true
    }, {
      label: "Производитель",
      value: d.vendor
    }, {
      label: "VID:PID",
      value: d.vidPid,
      mono: true
    }, {
      label: "Версия прошивки",
      value: d.firmware,
      mono: true
    }, {
      label: "Режим",
      value: MODE_LABEL[d.mode],
      strong
    }];
  }
  const rows = [{
    label: platform === "ios" ? "ID (UUID)" : "MAC-адрес",
    value: d.address,
    mono: true
  }, {
    label: "Тип устройства",
    value: d.type
  }, {
    label: "Уровень сигнала",
    value: `${d.rssi} dBm`,
    mono: true
  }, {
    label: "Режим",
    value: MODE_LABEL[d.mode],
    strong
  }];
  if (platform === "android") rows.push({
    label: "Связь с телефоном",
    value: d.phoneLink ? "есть" : "нет"
  });
  rows.push({
    label: "Связь у самого устройства",
    value: d.bound ? "есть" : "нет"
  });
  return rows;
}
const INFO_NOTE = {
  ble: "Версия прошивки и ID блока в эфире не передаются — они появятся после подключения.",
  usb: ""
};

// Список полей «ключ — значение».
function InfoRows({
  rows
}) {
  return /*#__PURE__*/React.createElement("div", {
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
      fontSize: "var(--t-additional0-size)",
      fontWeight: r.strong ? 700 : 500,
      color: r.strong ? "var(--primary)" : "var(--on-surface)",
      textAlign: "right"
    }
  }, r.value)))));
}
const TRANSPORT = {
  ble: {
    icon: "bluetooth",
    label: "Bluetooth",
    short: "BLE"
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
      border: "1px solid var(--border-highlight)",
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

// Правая метка карточки: RSSI (BLE) или systemId / «HID» (USB).
function MetaLabel({
  text,
  selected
}) {
  return /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-mono)",
      fontSize: "var(--t-additional0-size)",
      fontWeight: 500,
      whiteSpace: "nowrap",
      color: selected ? "inherit" : "var(--on-surface-variant)"
    }
  }, text);
}

// Чип транспорта (Bluetooth / USB).
function TransportChip({
  transport,
  short
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
      fontSize: "var(--t-additional1-size)",
      fontWeight: 500,
      flex: "0 0 auto"
    }
  }, /*#__PURE__*/React.createElement(CmpIcon, {
    icon: t.icon,
    size: 14
  }), short && t.short ? t.short : t.label);
}

// Строка устройства в списке.
function DeviceRow({
  device,
  selected,
  onClick,
  onInfo,
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
    color: selected ? "var(--primary-container)" : "var(--background-surface-card)",
    contentColor: selected ? "var(--on-primary-container)" : "var(--on-surface)",
    radius: "var(--r-card-medium-radius)",
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
      padding: "10px 14px",
      minHeight: 64,
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
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "baseline",
      justifyContent: "space-between",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(CmpText, {
    variant: "titleMedium",
    maxLines: 1
  }, device.name), device.transport === "ble" && /*#__PURE__*/React.createElement(MetaLabel, {
    text: `${device.rssi} dBm`,
    selected: selected
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      marginTop: 4,
      flexWrap: "wrap"
    }
  }, /*#__PURE__*/React.createElement(TransportChip, {
    transport: device.transport,
    short: true
  }), device.transport === "ble" ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-mono)",
      fontSize: "var(--t-additional1-size)",
      color: selected ? "inherit" : "var(--on-surface-variant)"
    }
  }, device.address) : /*#__PURE__*/React.createElement(MetaLabel, {
    text: device.systemId || device.address,
    selected: selected
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      position: "relative",
      flex: "0 0 auto",
      marginRight: onInfo ? -6 : 0
    }
  }, onInfo && /*#__PURE__*/React.createElement("div", {
    onClick: e => {
      e.stopPropagation();
      onInfo();
    },
    onKeyDown: e => e.stopPropagation()
  }, /*#__PURE__*/React.createElement(CmpIconButton, {
    icon: "MoreVert",
    contentDescription: "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u043E\u0431 \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0435"
  })), /*#__PURE__*/React.createElement(CmpIcon, {
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
    color: "var(--background-surface-card)",
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
      borderRadius: "var(--r-card-small-radius)",
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
  onBack = () => {},
  onConnected
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
  const [infoId, setInfoId] = useState(null);
  const scanTimer = useRef(null);

  // Сканирование: авто-переход в список (или «пусто») через задержку.
  useEffect(() => {
    if (step !== "scanning") return;
    clearTimeout(scanTimer.current);
    scanTimer.current = setTimeout(() => setStep(demoEmpty ? "empty" : "list"), 2200);
    return () => clearTimeout(scanTimer.current);
  }, [step, demoEmpty]);

  // Подключение (заглушка) → переход в «Расширенный монтаж» только после реального выбора устройства.
  useEffect(() => {
    if (step !== "connecting" || !connectingId || !onConnected) return;
    const t = setTimeout(() => onConnected(DEVICES.find(x => x.id === connectingId)), 1800);
    return () => clearTimeout(t);
  }, [step, connectingId]);
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
  const tileSize = mode === "compact" ? 44 : 48;

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
        fontSize: "var(--t-additional0-size)",
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
      platform: platform,
      tileSize: tileSize,
      selectedId: selectedId,
      onSelect: selectDevice,
      onConnect: connectSelected,
      onInfo: setInfoId
    });
  }
  const infoDev = DEVICES.find(x => x.id === infoId);
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
    title: "\u0412\u044B\u0431\u043E\u0440 \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0430",
    navigationIcon: showBack ? /*#__PURE__*/React.createElement(CmpIconButton, {
      icon: "ArrowBack",
      contentDescription: "\u041D\u0430\u0437\u0430\u0434",
      onClick: onBack
    }) : undefined
  }), body, infoDev && /*#__PURE__*/React.createElement(DS.CmpDialog, {
    open: true,
    onDismissRequest: () => setInfoId(null),
    title: infoDev.name,
    text: /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(InfoRows, {
      rows: infoRows(infoDev, platform)
    }), INFO_NOTE[infoDev.transport] && /*#__PURE__*/React.createElement("span", {
      style: {
        display: "block",
        marginTop: 8
      }
    }, INFO_NOTE[infoDev.transport])),
    dismissButton: /*#__PURE__*/React.createElement(CmpTextButtonSafe, {
      text: "\u0417\u0430\u043A\u0440\u044B\u0442\u044C",
      onClick: () => setInfoId(null)
    }),
    confirmButton: /*#__PURE__*/React.createElement(CmpTextButtonSafe, {
      text: "\u041F\u043E\u0434\u043A\u043B\u044E\u0447\u0438\u0442\u044C",
      onClick: () => {
        const id = infoDev.id;
        setInfoId(null);
        selectDevice(DEVICES.find(x => x.id === id));
      }
    })
  }));
}

// Подсказки «как починить» / «почему пусто».
function FixSteps({
  platform = "android",
  empty = false
}) {
  const items = empty ? ["Убедитесь, что на устройстве включено питание", "Поднесите блок ближе — в пределах 1–2 метров", "Проверьте, что Bluetooth включён"] : platform === "ios" ? ["Настройки → Pandora Spec → Bluetooth → включить"] : ["Настройки → Приложения → Pandora Spec → Разрешения", "Включите «Устройства поблизости» и «Bluetooth»"];
  return /*#__PURE__*/React.createElement(CmpSurface, {
    color: "var(--background-surface-card)",
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
  platform,
  tileSize,
  selectedId,
  onSelect,
  onConnect,
  onInfo
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
    onClick: () => onSelect(d),
    onInfo: mode === "split" ? undefined : () => onInfo(d.id)
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
      platform: platform,
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
  platform,
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
  const rows = infoRows(device, platform);
  return /*#__PURE__*/React.createElement(CmpSurface, {
    color: "var(--background-surface-card)",
    radius: "var(--r-card-medium-radius)",
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
    radius: "var(--r-card-medium-radius)",
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
  }))), /*#__PURE__*/React.createElement(InfoRows, {
    rows: rows
  }), INFO_NOTE[device.transport] && /*#__PURE__*/React.createElement(CmpText, {
    variant: "bodySmall",
    color: "var(--on-surface-variant)"
  }, INFO_NOTE[device.transport]), /*#__PURE__*/React.createElement(CmpButton, {
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
} = window.HomeScreens || {};

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
  initialUpdateState = "available",
  updateRequired = false,
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
  // Обновление приложения: один контроллер на главный и экран обновления (загрузка идёт в фоне).
  // Desktop — загрузка и установка; телефон/планшет — переход в магазин. Требует ../app-update/AppUpdateScreen.jsx.
  const AU = window.AppUpdate;
  const upd = AU ? AU.useUpdateCtl({
    platform: isDesktop ? "win" : "android",
    initial: initialUpdateState
  }) : null;
  const [updFrom, setUpdFrom] = useState("menu");
  const [bannerHidden, setBannerHidden] = useState(false);
  const [toast, setToast] = useState(false);
  const toastDone = useRef(false);
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

  // Desktop: уведомление «Доступна версия…» — один раз за запуск, после загрузки главного.
  useEffect(() => {
    if (!isDesktop || loading || !upd || upd.s !== "available" || toastDone.current) return;
    toastDone.current = true;
    setToast(true);
    clearTimeout(upTimer.current);
    upTimer.current = setTimeout(() => setToast(false), 8000);
  }, [loading, upd && upd.s]);

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
      // «Заявки на техпомощь» → экран заявок для этой платформы.
      const target = isDesktop ? "tech-requests-desktop" : "tech-requests";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "quick") {
      // «Быстрый монтаж» → мастер быстрого монтажа (поиск только сигнализаций — его первый шаг).
      const target = isDesktop ? "fast-mount-desktop" : "fast-mount";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "extended") {
      // «Расширенный монтаж» → экран поиска и выбора устройства.
      const target = isDesktop ? "device-select-desktop" : "device-select";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    if (key === "firmware") {
      // «Архив прошивок» без подключённого блока → вход Б: выбор модели → архив.
      const target = isDesktop ? "firmware-desktop" : "firmware";
      if (window.psHas && window.psHas(target)) window.psGo(target, "nounit");
    }
    if (key === "configurator") {
      const target = isDesktop ? "configurator-desktop" : "configurator";
      if (window.psHas && window.psHas(target)) window.psGo(target);
    }
    // «Информация о товаре» → экран с пустым полем.
    if (key === "product" && window.psProduct) window.psProduct("home");
  };

  // Открыть экран обновления. Из меню — с новой проверкой (если не идёт загрузка), из плашки/уведомления/блока — как есть.
  const openUpdate = (from = "menu") => {
    if (!isDesktop) setMenuOpen(false);
    setToast(false);
    setUpdFrom(from);
    setRoute("update");
    if (from === "menu" && upd && !/^(downloading|downloaded|installing)$/.test(upd.s)) upd.check();
  };
  const onBackHome = () => setRoute("home");
  const onDownload = () => {};
  const appUpdate = upd ? {
    platform: isDesktop ? "desktop" : "mobile",
    ctl: upd,
    state: upd.s,
    current: upd.inst.v,
    latest: upd.ch.v,
    from: updFrom,
    onCheck: () => openUpdate("menu"),
    onOpen: openUpdate,
    bannerHidden,
    onHideBanner: () => setBannerHidden(true),
    toast,
    onToastClose: () => setToast(false),
    onToastUpdate: () => {
      upd.update();
      openUpdate("toast");
    },
    required: updateRequired
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
  product: "manage_search",
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
  svg: "tech_requests",
  title: "Заявки на техпомощь",
  desc: "Обратный звонок специалиста и история заявок"
}, {
  key: "feedback",
  svg: "feedback",
  title: "Обратная связь",
  desc: "Отзыв о монтаже и улучшениях"
}, {
  key: "product",
  icon: "manage_search",
  title: "Информация о товаре",
  desc: "Найти информацию о товаре и комплектации"
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
  const html = window.PS_SECTION_ICONS && window.PS_SECTION_ICONS[name] || window.PS_MAIN_ICONS && window.PS_MAIN_ICONS[name] || "";
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
    color: "var(--background-surface-card)",
    border: true,
    radius: "var(--r-card-medium-radius)",
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
      borderRight: panel ? "1px solid var(--border-highlight)" : "none",
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
  }), appUpdate && appUpdate.platform === "desktop" && /*#__PURE__*/React.createElement(DrawerRow, {
    icon: ICON.appUpdate,
    label: "\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u0442\u044C \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F",
    onClick: appUpdate.onCheck
  }), appUpdate && appUpdate.platform !== "desktop" && window.AppUpdate && /*#__PURE__*/React.createElement(window.AppUpdate.MenuUpdateRow, {
    dot: appUpdate.state === "available",
    onClick: () => appUpdate.onOpen("menu")
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
  }, appUpdate && appUpdate.platform === "desktop" && window.AppUpdate && /*#__PURE__*/React.createElement(window.AppUpdate.SideUpdateBlock, {
    c: appUpdate.ctl,
    onOpen: () => appUpdate.onOpen("sidebar")
  }), /*#__PURE__*/React.createElement("div", {
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
  onBack,
  required = null
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
    actions: loading || back ? null : /*#__PURE__*/React.createElement("span", {
      style: {
        display: "inline-flex",
        alignItems: "center",
        gap: 4
      }
    }, required && window.AppUpdate && /*#__PURE__*/React.createElement(window.AppUpdate.RequiredUpdateButton, {
      onClick: required
    }), /*#__PURE__*/React.createElement(StatusChip, {
      compact: compactStatus,
      syncing: syncing
    }))
  });
}

// ── Screen body (cards + tools), no visible section headings ─────────────────
function HomeContent({
  mode,
  cardVariant = "centered",
  onNav = () => {},
  banner = null
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
  }, banner, /*#__PURE__*/React.createElement("div", {
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
      borderRadius: "var(--r-card-medium-radius)",
      background: "var(--background-surface-card)",
      border: "1px solid var(--border-highlight)",
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
      borderRadius: "var(--r-card-small-radius)",
      background: "var(--surface)",
      border: "1px solid var(--border-highlight)",
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
    onBack: onBackHome,
    required: appUpdate && appUpdate.required ? () => appUpdate.onOpen("required") : null
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
  }) : inUpdate ? window.AppUpdate && appUpdate ? /*#__PURE__*/React.createElement(window.AppUpdate.UpdateRoute, {
    ctl: appUpdate.ctl,
    mode: mode,
    from: appUpdate.from,
    onLater: onBackHome
  }) : null : /*#__PURE__*/React.createElement(HomeContent, {
    mode: mode,
    cardVariant: cardVariant,
    onNav: onNav,
    banner: appUpdate && appUpdate.platform !== "desktop" && appUpdate.state === "available" && !appUpdate.bannerHidden && window.AppUpdate ? /*#__PURE__*/React.createElement(window.AppUpdate.UpdateBanner, {
      latest: appUpdate.latest,
      onOpen: () => appUpdate.onOpen("banner"),
      onClose: appUpdate.onHideBanner
    }) : null
  })), appUpdate && appUpdate.toast && !inUpdate && window.AppUpdate && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 16,
      right: 16,
      bottom: 16,
      zIndex: 20,
      display: "flex",
      justifyContent: "center"
    }
  }, /*#__PURE__*/React.createElement(window.AppUpdate.UpdateToast, {
    latest: appUpdate.latest,
    onUpdate: appUpdate.onToastUpdate,
    onClose: appUpdate.onToastClose
  })), appUpdate && window.AppUpdate && /*#__PURE__*/React.createElement(window.AppUpdate.UpdateDialogs, {
    c: appUpdate.ctl
  }), showModal && /*#__PURE__*/React.createElement("div", {
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

const SCS = window.SupportChatScreens || {};
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
      border: "1px solid var(--border-highlight)",
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
  const bg = !tonal ? "transparent" : mine ? "var(--primary-container)" : "var(--background-surface-card)";
  const fg = tonal && mine ? "var(--on-primary-container)" : "var(--on-surface)";
  const metaTone = tonal && mine ? "color-mix(in srgb, var(--on-primary-container) 70%, transparent)" : "var(--on-surface-variant)";
  const border = !tonal ? `1px solid ${mine ? "color-mix(in srgb, var(--primary) 45%, transparent)" : "var(--outline-variant)"}` : mine ? "none" : "1px solid var(--border-highlight)";
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
      borderRadius: "var(--r-card-medium-radius)",
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
      borderRadius: "var(--r-card-medium-radius)",
      borderBottomLeftRadius: 4,
      background: tonal ? "var(--background-surface-card)" : "transparent",
      border: "1px solid var(--border-highlight)",
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
      borderRadius: "var(--r-card-small-radius)",
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
      background: "var(--background-surface-card)",
      border: "1px solid var(--border-highlight)"
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
      borderRadius: "var(--r-card-medium-radius)",
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
      borderRadius: "var(--r-card-medium-radius)",
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
      borderTop: "1px solid var(--border-highlight)",
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
      background: "var(--background-surface-highlight)",
      borderRadius: "var(--r-card-medium-radius)",
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

__ds_ns.CmpHoldToConfirmButton = __ds_scope.CmpHoldToConfirmButton;

__ds_ns.CmpContentError = __ds_scope.CmpContentError;

__ds_ns.CmpChip = __ds_scope.CmpChip;

__ds_ns.CmpListItem = __ds_scope.CmpListItem;

__ds_ns.CmpInfoRow = __ds_scope.CmpInfoRow;

__ds_ns.CmpInputDigit = __ds_scope.CmpInputDigit;

})();
