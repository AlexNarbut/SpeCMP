package ru.alarmtrade.specmp.detekt

/**
 * Разделяемые сервисы по имени: держат состояние между потоками (SharedMutableStateInSingleton) и не берутся прямо в
 * composable (ServiceInComposable).
 */
internal fun String.isServiceName(): Boolean = SERVICE_NAME.matches(this)

private val SERVICE_NAME =
    Regex(".*(Repository|Manager|Controller|Starter|Initializer|Coordinator|Session|Notifier|Provider|Store|Source)")
