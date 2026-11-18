package ru.alarmtrade.specmp.core.specialist

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.specmp.core.common.AppStore

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
}
