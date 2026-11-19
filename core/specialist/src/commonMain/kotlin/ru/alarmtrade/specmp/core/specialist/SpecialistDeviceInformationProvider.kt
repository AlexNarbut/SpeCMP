package ru.alarmtrade.specmp.core.specialist

import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.specmp.core.common.AppStore

fun interface SpecialistDeviceInformationProvider {
    fun create(): AppDeviceInformation
}

expect fun createSpecialistDeviceInformation(): AppDeviceInformation

/** Сборка для AppGallery — `DeviceType.HUAWEI`: по нему сервер шлёт push через HMS, а не FCM. */
internal fun AppDeviceInformation.forStore(store: AppStore?): AppDeviceInformation =
    if (store == AppStore.APP_GALLERY) copy(deviceType = DeviceType.HUAWEI) else this
