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
    ArrowBack: 'arrow_back', ArrowForward: 'arrow_forward', ArrowDropDown: 'arrow_drop_down',
    ChevronLeft: 'keyboard_arrow_left', ChevronRight: 'keyboard_arrow_right',
    KeyboardArrowDown: 'keyboard_arrow_down', KeyboardArrowUp: 'keyboard_arrow_up',
    Menu: 'menu', MoreVert: 'more_vert', Close: 'close', Home: 'home',
    // Actions
    Add: 'add', Remove: 'clear', Edit: 'edit', Create: 'create', Delete: 'delete',
    Search: 'search', Refresh: 'refresh', Share: 'share', Send: 'send', Done: 'done',
    Check: 'check', CheckCircle: 'check_circle', Clear: 'clear', PlayArrow: 'play_arrow',
    Pause: 'pause', Build: 'build',
    // Content
    Favorite: 'favorite', FavoriteBorder: 'favorite_border', Star: 'star',
    ThumbUp: 'thumb_up', List: 'list', Tag: 'sell',
    // Communication
    Email: 'email', Phone: 'phone', Call: 'call', Notifications: 'notifications', Mic: 'mic',
    // User
    Person: 'person', AccountCircle: 'account_circle', Face: 'face',
    // Status
    Info: 'info', Warning: 'warning', AlertTriangle: 'warning', AlertCircle: 'error',
    Lock: 'lock', Visibility: 'visibility', VisibilityOff: 'visibility_off',
    // Places
    LocationOn: 'location_on', Place: 'place',
    // Other
    Settings: 'settings', DateRange: 'date_range', Schedule: 'schedule',
    ShoppingCart: 'shopping_cart', ExitToApp: 'exit_to_app', DarkMode: 'dark_mode',
    LightMode: 'light_mode', NightsStay: 'nights_stay', Lightbulb: 'lightbulb',
    Moon: 'nights_stay', Sun: 'wb_sunny',
  };
  window.CMP_ICONS = CMP_ICONS;
  window.CmpIconLigature = function (name) { return CMP_ICONS[name] || name; };
})();
