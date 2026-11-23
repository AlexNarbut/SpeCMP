package ru.alarmtrade.specmp.core.specialist

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.specmp.core.common.AppStore
import ru.alarmtrade.specmp.core.common.AppVersion

class DeviceInformationStoreTest {

    private val android = AppDeviceInformation(
        deviceId = "id",
        deviceType = DeviceType.ANDROID,
        deviceDescription = "Pixel",
    )

    @Test
    fun appGalleryBuildIsHuawei() {
        assertEquals(DeviceType.HUAWEI, android.forStore(AppStore.APP_GALLERY).deviceType)
    }

    @Test
    fun googlePlayBuildKeepsPlatformType() {
        assertEquals(android, android.forStore(AppStore.GOOGLE_PLAY))
    }

    @Test
    fun noStoreKeepsPlatformType() {
        assertEquals(android, android.forStore(null))
    }

    @Test
    fun appVersionEndsDescription() {
        val description = android.withAppVersion(AppVersion(name = "10.0.5", code = 1006)).deviceDescription
        assertEquals("Pixel 10.0.5(1006)", description)
    }

    @Test
    fun userAgentNamesPandoraSpecialist() {
        val device = android.copy(deviceName = "Pixel 8", osName = "Android", osVersion = "15")
        val userAgent = device.withAppVersion(AppVersion(name = "10.0.5", code = 1006)).userAgent
        assertEquals("Pixel 8 Android/15 PandoraSpecialist/v10.0.5", userAgent)
    }
}
