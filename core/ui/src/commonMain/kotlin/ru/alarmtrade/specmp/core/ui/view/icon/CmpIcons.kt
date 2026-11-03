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
    SwapHoriz,
    Visibility,
    VisibilityOff,
    WifiOff,
    HourglassTop,
    SyncLock,
    Block,
    Hearing,
    Replay,
    FactCheck,
    MenuBook,

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

    // ── Firmware / update ───────────────────────────────────
    SystemUpdate,
    History,
    FolderOpen,
    PowerSettingsNew,
    LinkOff,
    Sync,
    ArrowCircleUp,
    CloudOff,
    Inventory2,

    // ── Конфигурации ────────────────────────────────────────
    Cloud,
    Save,
    SaveAs,
    Upload,
    Factory,
    RecordVoiceOver,
    ErrorOutline,
    PriorityHigh,

    // ── Vehicles ────────────────────────────────────────────
    DirectionsCar,
    FilterList,
    FilterAlt,
    FilterAltOff,
    Pin,
    ContentCopy,
    Security,
    SettingsRemote,
    PictureAsPdf,
    Groups,
    OpenInNew,
    Cancel,
    HelpOutline,
    Cable,
    PhotoLibrary,

    // ── Vehicle state (проверка состояния) ─────────────────
    SensorDoor,
    Luggage,
    Key,
    PauseCircle,
    PanTool,
    LocalParking,
    Highlight,
    Vibration,
    DirectionsWalk,
    ScreenRotation,
    SyncProblem,
    LockOpen,
    GppGood,
    NotificationsActive,
    DeveloperBoard,
    EvStation,
    LocalFireDepartment,
    Videocam,
    VolumeUp,
    TaskAlt,
    Autorenew,
    Redo,

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
    Sun,

    // ── Быстрый монтаж ──────────────────────────────────────
    StarBorder,
    RestartAlt,
    CloudDone,
    CloudSync
}
