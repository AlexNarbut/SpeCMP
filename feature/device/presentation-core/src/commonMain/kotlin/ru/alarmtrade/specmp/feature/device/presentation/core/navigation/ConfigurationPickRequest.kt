package ru.alarmtrade.specmp.feature.device.presentation.core.navigation

/** Откуда загрузить конфигурацию в редактор блока: «Мои конфигурации» или облако. */
enum class ConfigurationPickKind { MINE, CLOUD }

/**
 * Запрос выбора конфигурации для загрузки в редактор подключённого блока (слот `configurationPicker`, экраны —
 * фича `configurator-ui`, подключается в `composeApp`).
 *
 * @property deviceName модель блока: «Мои» — фильтр по ней, облако — каталог под неё.
 * @property firmwareCanCode CAN-код единой CAN-прошивки блока: облачную конфигурацию с другим кодом загрузить нельзя.
 */
data class ConfigurationPickRequest(
    val kind: ConfigurationPickKind,
    val deviceName: String,
    val firmwareCanCode: String?,
)
