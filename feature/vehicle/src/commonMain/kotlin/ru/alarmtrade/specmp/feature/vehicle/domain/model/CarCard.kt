package ru.alarmtrade.specmp.feature.vehicle.domain.model

/** Карточка комплектации: всё, что сервер знает об автомобиле. Списки уже отсортированы по `order` сервера. */
data class CarCard(
    val summary: CarSummary,
    val engineBlockByCan: CarCapability,
    val keylessBypass: CarCapability,
    /** Примечания (HTML), пустые отброшены; 0–2. */
    val notes: List<String>,
    val features: List<CarFeature>,
    val mountPoints: List<MountPoint>,
    val schemes: List<CarDocument>,
    val mountingMaps: List<CarDocument>,
    /** Первые народные карты (сервер отдаёт до 15); [hasUserManuals] — есть ли они вообще. */
    val userManuals: List<UserManualRef>,
    val hasUserManuals: Boolean,
)

/** Возможность автомобиля (блокировка по CAN, бесключевой обход). [note] — примечание в HTML. */
sealed interface CarCapability {
    data object NoData : CarCapability
    data class Supported(val note: String?) : CarCapability
    data class NotSupported(val note: String?) : CarCapability
}

/** Функция автомобиля: по каким шинам блок читает статус ([reads]) и управляет ([controls]). */
data class CarFeature(
    val name: String,
    val description: String?,
    val group: CarFeatureGroup,
    val reads: List<CarBus>,
    val controls: List<CarBus>,
)

/** Группа функций; неизвестный код сервера SDK приводит к [MAIN]. */
enum class CarFeatureGroup { MAIN, EXTENDED, ELECTRIC, TRAILER }

/** Точка подключения: тип («Зажигание», «CAN»), название, описание и фото. */
data class MountPoint(val title: String, val type: String, val description: String?, val photos: List<MountPhoto>)

data class MountPhoto(val name: String, val url: String)

/** PDF-документ: схема или карта монтажа. */
data class CarDocument(val name: String, val description: String?, val url: String)

/** Народная карта (инструкция установщика) — ссылка на экран инструкции. */
data class UserManualRef(val id: Int, val title: String, val description: String?)
