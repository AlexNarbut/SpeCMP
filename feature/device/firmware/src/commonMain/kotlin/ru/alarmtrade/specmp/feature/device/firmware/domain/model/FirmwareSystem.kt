package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/**
 * Система из базы устройств (комплект: сигнализация, метка, модуль…) и её подсистемы — у каждой подсистемы свой
 * архив прошивок. Архив без блока выбирает подсистему отсюда.
 */
data class FirmwareSystem(val id: Long, val name: String, val subsystems: List<FirmwareSubsystem>)

/** Подсистема системы: [systemName] — имя прошивки («DX4G»), [title] — название для показа, [kind] — тип. */
data class FirmwareSubsystem(val id: Long, val title: String, val systemName: String, val kind: FirmwareModuleKind)
