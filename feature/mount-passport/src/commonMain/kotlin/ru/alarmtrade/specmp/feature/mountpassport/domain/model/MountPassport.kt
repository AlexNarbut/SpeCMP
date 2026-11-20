package ru.alarmtrade.specmp.feature.mountpassport.domain.model

import kotlinx.datetime.Instant

/** Паспорт монтажа; пути фото — абсолютные. Источник правды и вся логика хранения — в SDK. */
data class MountPassport(
    val id: String,
    val number: String,
    val status: PassportStatus,
    val origin: PassportOrigin,
    val createdAt: Instant,
    val updatedAt: Instant,
    val vehicle: PassportVehicle,
    val unit: PassportUnit,
    val configuration: PassportConfiguration?,
    val works: PassportWorks,
    val systemCheck: List<PassportCheckTask>,
    val peripherals: PassportPeripherals,
    val photos: List<PassportPhoto>,
    val remarks: String?,
    val client: PassportClient,
    val installer: PassportInstaller,
    val settingsSnapshot: PassportSettingsSnapshot?,
    val readStages: Map<PassportReadStage, PassportReadStatus>,
) {
    val withoutUnit: Boolean get() = unit.manual

    companion object {
        const val MAX_PHOTOS = 8
        const val MAX_REMARKS_LENGTH = 1000
    }
}

/** `BLANK` — заготовка без считывания блока, `DRAFT` — предотчёт, `ISSUED` — документ сформирован. */
enum class PassportStatus { BLANK, DRAFT, ISSUED }

enum class PassportOrigin { FAST_MOUNT, ADVANCED_MOUNT, ARCHIVE }

/** [customTitle] — автомобиль текстом; иначе марка, модель и годы из каталога. */
data class PassportVehicle(
    val brand: String? = null,
    val model: String? = null,
    val years: String? = null,
    val catalogCarId: String? = null,
    val customTitle: String? = null,
    val vin: String? = null,
    val plate: String? = null,
    val mileageKm: Long? = null,
)

/** [manual] — данные блока внесены вручную, блок не считывался. */
data class PassportUnit(
    val systemName: String,
    val systemId: String,
    val firmwareVersion: String?,
    val can: PassportCan?,
    val imei: String?,
    val bluetoothModule: PassportBluetoothModule?,
    val transport: PassportTransport?,
    val manual: Boolean,
)

data class PassportCan(val version: String?, val code: String?, val factory: Boolean)

data class PassportBluetoothModule(val firmwareVersion: String?, val bootloaderVersion: String?)

enum class PassportTransport { BLUETOOTH, USB }

data class PassportConfiguration(val name: String?, val author: String?, val cloudId: String?)

/** [fromMaster] — работы записал мастер быстрого монтажа, иначе их отмечает монтажник. */
data class PassportWorks(
    val fromMaster: Boolean = false,
    val settingsWritten: Boolean = false,
    val bypass: PassportBypass? = null,
    val clone: Boolean = false,
    val slave: Boolean = false,
    val durationMinutes: Int? = null,
)

enum class PassportBypass { TRAINED, CLONE_NEEDED, NONE }

data class PassportCheckTask(val title: String, val result: PassportCheckResult)

enum class PassportCheckResult { PASSED, FAILED, SKIPPED }

data class PassportPeripherals(
    val bluetooth: List<PassportBluetoothPeripheral> = emptyList(),
    val usb: List<PassportUsbPeripheral> = emptyList(),
)

/** [type] — имя типа устройства, [title] — название для документа. */
data class PassportBluetoothPeripheral(
    val type: String,
    val title: String?,
    val number: Int?,
    val address: String?,
    val online: Boolean?,
)

data class PassportUsbPeripheral(
    val type: String,
    val name: String?,
    val slot: Int?,
    val paired: Boolean?,
    val online: Boolean?,
)

data class PassportPhoto(val id: String, val path: String, val thumbnailPath: String, val caption: String?)

data class PassportClient(val name: String? = null, val phone: String? = null)

data class PassportInstaller(
    val name: String? = null,
    val contact: String? = null,
    val organization: String? = null,
    val address: String? = null,
)

/** Снимок настроек блока (`.dxs`) — рабочая копия мастера, в документ не попадает. */
data class PassportSettingsSnapshot(val deviceModel: String, val takenAt: Instant, val canFactory: Boolean)

enum class PassportReadStage { UNIT, PARAMETERS, PERIPHERALS, SETTINGS }

/** `NOT_READ` — ошибка чтения (можно перечитать), `UNAVAILABLE` — блок этого не умеет, `SKIPPED` — пропущено. */
enum class PassportReadStatus { READ, NOT_READ, UNAVAILABLE, SKIPPED }
