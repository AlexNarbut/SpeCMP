package ru.alarmtrade.specmp.core.specialist

import ru.alarmtrade.common.app.domain.AppDeviceInformation

fun interface SpecialistDeviceInformationProvider {
    fun create(): AppDeviceInformation
}

expect fun createSpecialistDeviceInformation(): AppDeviceInformation
