package ru.alarmtrade.specmp.core.ui.view.icon

/**
 * Единый каталог иконок дизайн-системы.
 *
 * Enum хранит только имя. Для получения `ImageVector` используйте
 * `@Composable` расширение [imageVector] (см. `CmpIconsResolver.kt`):
 *
 * ```kotlin
 * CmpIcon(icon = CmpIcons.ArrowBack, contentDescription = "Назад")
 * // или напрямую:
 * val iv = CmpIcons.Lock.imageVector()
 * ```
 *
 * Все иконки резолвятся из material-icons (констант, без ресурсов).
 * Чтобы добавить новую — добавь элемент сюда и ветку в [imageVector].
 * Брендовые/кастомные иконки подключай через compose resources и
 * `CmpIcon(painter = ...)`.
 */
enum class CmpIcons {
    // ── Navigation ──────────────────────────────────────────
    ArrowBack,
    ArrowForward,
    ArrowDropDown,
    ChevronLeft,
    ChevronRight,
    KeyboardArrowDown,
    KeyboardArrowUp,
    Menu,
    MoreVert,
    Close,
    Home,

    // ── Actions ─────────────────────────────────────────────
    Add,
    Remove,
    Edit,
    Create,
    Delete,
    Search,
    Refresh,
    Share,
    Send,
    Done,
    Check,
    CheckCircle,
    Clear,
    PlayArrow,
    Pause,
    Build,

    // ── Content ─────────────────────────────────────────────
    Favorite,
    FavoriteBorder,
    Star,
    ThumbUp,
    List,
    Tag,

    // ── Communication ───────────────────────────────────────
    Email,
    Phone,
    Call,
    Notifications,
    Mic,
    Forum,
    SupportAgent,

    // ── User ────────────────────────────────────────────────
    Person,
    AccountCircle,
    Face,

    // ── Status ──────────────────────────────────────────────
    Info,
    Warning,
    AlertTriangle,
    AlertCircle,
    Lock,
    Visibility,
    VisibilityOff,
    WifiOff,

    // ── Places ──────────────────────────────────────────────
    LocationOn,
    Place,

    // ── Content / Files ─────────────────────────────────────
    AttachFile,
    Image,
    Description,
    DoneAll,
    Download,

    // ── Devices / Connectivity ──────────────────────────────
    Bluetooth,
    BluetoothSearching,
    BluetoothDisabled,
    Usb,
    Sensors,
    Memory,
    VpnKey,
    SearchOff,
    TouchApp,
    Link,

    // ── Other ───────────────────────────────────────────────
    Settings,
    DateRange,
    Schedule,
    ShoppingCart,
    ExitToApp,
    DarkMode,
    LightMode,
    NightsStay,
    Lightbulb,
    Moon,
    Sun
}
