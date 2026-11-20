package ru.alarmtrade.specmp.feature.mountpassport.domain.model

/**
 * Тексты документа, которые SDK подставляет в HTML. Шаблоны: `{passed}`/`{total}`, `{value}`, `{h}`/`{m}`, `{n}`,
 * `{code}`.
 */
enum class PassportDocumentText {
    TITLE,
    NUMBER_PREFIX,
    VEHICLE,
    EQUIPMENT,
    CONFIGURATION,
    WORKS,
    SYSTEM_CHECK,
    DEVICES,
    REMARKS,
    PHOTOS,
    INSTALLER,
    CLIENT,
    VEHICLE_MAKE_MODEL,
    VEHICLE_YEARS,
    VEHICLE_PLATE,
    VEHICLE_VIN,
    VEHICLE_MILEAGE,
    UNIT_MODEL,
    UNIT_ID,
    UNIT_FIRMWARE,
    UNIT_BOOTLOADER,
    UNIT_CAN,
    UNIT_CAN_CODE,
    UNIT_BLUETOOTH,
    UNIT_IMEI,
    MANUAL_UNIT_NOTE,
    CONFIGURATION_NAME,
    CONFIGURATION_AUTHOR,
    CONFIGURATION_CLOUD_ID,
    WORK_SETTINGS,
    WORK_SETTINGS_WRITTEN,
    WORK_BYPASS,
    WORK_BYPASS_TRAINED,
    WORK_BYPASS_CLONE_NEEDED,
    WORK_CLONE,
    WORK_CLONE_DONE,
    WORK_SLAVE,
    WORK_SLAVE_DONE,
    WORK_DURATION,
    CHECK_PASSED_OF_TOTAL,
    CHECK_NUMBER,
    CHECK_TASK,
    CHECK_RESULT,
    CHECK_PASSED,
    CHECK_FAILED,
    CHECK_SKIPPED,
    DEVICE_TYPE,
    DEVICE_NUMBER,
    DEVICE_ADDRESS,
    DEVICE_LINK,
    DEVICE_NAME,
    DEVICE_SLOT,
    DEVICE_NUMBER_VALUE,
    DEVICE_ONLINE,
    DEVICE_OFFLINE,
    DEVICE_PAIRED,
    DEVICE_NOT_PAIRED,
    INSTALLER_NAME,
    INSTALLER_CONTACT,
    INSTALLER_ORGANIZATION,
    INSTALLER_ADDRESS,
    CLIENT_NAME,
    CLIENT_PHONE,
    SIGNED_BY,
    ACCEPTED_BY,
    SIGNATURE,
    FULL_NAME,
    MILEAGE_VALUE,
    DURATION_HOURS_MINUTES,
    DURATION_HOURS,
    DURATION_MINUTES,
}

/** Вид устройства системы для документа: модификации одного устройства называются одинаково. */
enum class PassportDeviceKind {
    TAG,
    KEYFOB,
    WATCH,
    BRACELET,
    RELAY,
    HOOD_LOCK,
    RHM,
    SIREN,
    DOOR_MODULE,
    TPMS,
    FUEL_SENSOR,
    MOTION_SENSOR,
    VS_MODULE,
    NAVIGATION,
    DI_MODULE,
    RF_MODULE,
    IMMOBILIZER,
    CHARGER,
    NFC_READER,
}

/** [deviceTypes] — названия видов устройств; неизвестный SDK тип печатается как есть. */
data class PassportDocumentStrings(
    val texts: Map<PassportDocumentText, String>,
    val deviceTypes: Map<PassportDeviceKind, String> = emptyMap(),
) {
    fun text(key: PassportDocumentText): String = texts[key].orEmpty()
}

/**
 * [datePattern] — `{d}`, `{dd}`, `{MM}`, `{MMMM}` (из [months]), `{yyyy}`, `{HH}`, `{mm}`; [timeZoneId] — пояс даты
 * паспорта; [thousandsSeparator] — разделитель разрядов пробега.
 */
data class PassportDocumentFormat(
    val language: String,
    val months: List<String>,
    val datePattern: String,
    val thousandsSeparator: String,
    val timeZoneId: String?,
)
