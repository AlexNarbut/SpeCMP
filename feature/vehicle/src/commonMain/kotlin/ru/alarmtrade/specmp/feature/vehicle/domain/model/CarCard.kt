package ru.alarmtrade.specmp.feature.vehicle.domain.model

/** Списки уже отсортированы по `order` сервера. */
data class CarCard(
    val summary: CarSummary,
    /** `null` — функция неприменима к типу устройства, строку не показывают. */
    val engineBlockByCan: CarCapability?,
    val keylessBypass: CarCapability?,
    val notes: List<String>,
    val features: List<CarFeature>,
    val mountPoints: List<MountPoint>,
    val schemes: List<CarDocument>,
    val mountingMaps: List<CarDocument>,
    /** Первые народные карты (сервер отдаёт до 15); [hasUserManuals] — есть ли они вообще. */
    val userManuals: List<UserManualRef>,
    val hasUserManuals: Boolean,
)

sealed interface CarCapability {
    val note: String?

    data class Supported(override val note: String?) : CarCapability
    data class NotSupported(override val note: String?) : CarCapability
}

data class CarFeature(
    val name: String,
    val description: String?,
    val group: CarFeatureGroup,
    val reads: List<CarBus>,
    val controls: List<CarBus>,
)

/** Группа функций; неизвестный код сервера SDK приводит к [MAIN]. */
enum class CarFeatureGroup { MAIN, EXTENDED, ELECTRIC, TRAILER }

data class MountPoint(val title: String, val type: String, val description: String?, val photos: List<MountPhoto>)

data class MountPhoto(val name: String, val url: String)

data class CarDocument(val name: String, val description: String?, val url: String)

data class UserManualRef(val id: Int, val title: String, val description: String?)
