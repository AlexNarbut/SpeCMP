package ru.alarmtrade.specmp.core.specialist

import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.specmp.core.common.AppStore
import ru.alarmtrade.specmp.core.common.AppVersion

fun interface SpecialistDeviceInformationProvider {
    fun create(): AppDeviceInformation
}

expect fun createSpecialistDeviceInformation(): AppDeviceInformation

/** Сборка для AppGallery — `DeviceType.HUAWEI`: по нему сервер шлёт push через HMS, а не FCM. */
internal fun AppDeviceInformation.forStore(store: AppStore?): AppDeviceInformation =
    if (store == AppStore.APP_GALLERY) copy(deviceType = DeviceType.HUAWEI) else this

// Как в старом приложении: SDK дописывает к описанию `-s<версия настроек>` (`10.0.5(1006)-s257`),
// а User-Agent собирает из полей — `MacBookAir10,1 macOS/26.1 PandoraSpecialist/v10.0.5`.
internal fun AppDeviceInformation.withAppVersion(version: AppVersion): AppDeviceInformation = copy(
    deviceDescription = "$deviceDescription ${version.name}(${version.code})",
    appName = USER_AGENT_APP_NAME,
    appVersion = "v${version.name}",
)

private const val USER_AGENT_APP_NAME = "PandoraSpecialist"
