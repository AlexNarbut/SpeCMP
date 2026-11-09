package ru.alarmtrade.specmp.feature.device.firmware.domain.model

data class FirmwareSystem(val id: Long, val name: String, val subsystems: List<FirmwareSubsystem>)

data class FirmwareSubsystem(val id: Long, val title: String, val systemName: String, val kind: FirmwareModuleKind)
