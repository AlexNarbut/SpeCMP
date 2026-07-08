import React from "react";

/**
 * Catalog of icon names (CmpIcons enum) → Material Icons ligatures.
 * Mirrors core/ui/.../icon/CmpIconsResolver.kt exactly.
 * Icons render from the "Material Icons" (Filled) webfont.
 */
export const CMP_ICONS = {
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
  Sun: "wb_sunny",
};

/**
 * CmpIcon — single catalog icon. Default 24dp, inherits text color.
 * `icon` is a CmpIcons catalog name (e.g. "ArrowBack") or any raw
 * Material Icons ligature. Never hand-roll icons outside the catalog.
 */
export function CmpIcon({
  icon,
  size = 24,
  color = "currentColor",
  title,
  style,
  ...rest
}) {
  const ligature = CMP_ICONS[icon] || icon;
  return (
    <span
      className="material-icons"
      role="img"
      aria-label={title || icon}
      aria-hidden={title ? undefined : true}
      style={{
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
        ...style,
      }}
      {...rest}
    >
      {ligature}
    </span>
  );
}
