package ru.alarmtrade.specmp.feature.main.presentation.model

enum class MainNavigationTarget {
    QuickMount,
    AdvancedMount,
    Configurator,
    FirmwareArchive,
    Cars,
    Feedback,
    ProductInfo,
    MountPassports,
    TechSupportChat,
    TechRequests,
    Warehouse1C,
    Settings,
    Appearance,

    /** Экран обновления с новой проверкой (пункт меню). */
    AppUpdate,

    /** Экран обновления с тем, что уже известно (плашка, блок меню, уведомление, значок «Нужно обновить»). */
    AppUpdatePrompt,

    /** Экран обновления и сразу установка скачанного («Установить» в боковом меню). */
    AppUpdateInstall,
}
