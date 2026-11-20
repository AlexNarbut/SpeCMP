package ru.alarmtrade.specmp.feature.mountpassport.data.mapper

import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportDocumentFormat
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportDocumentStrings
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDeviceKind
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentText as T

@Suppress("LongMethod")
internal fun PassportDocumentStrings.toSdk(): MountPassportDocumentStrings = MountPassportDocumentStrings(
    title = text(T.TITLE),
    numberPrefix = text(T.NUMBER_PREFIX),
    vehicle = text(T.VEHICLE),
    equipment = text(T.EQUIPMENT),
    configuration = text(T.CONFIGURATION),
    works = text(T.WORKS),
    systemCheck = text(T.SYSTEM_CHECK),
    devices = text(T.DEVICES),
    remarks = text(T.REMARKS),
    photos = text(T.PHOTOS),
    installer = text(T.INSTALLER),
    client = text(T.CLIENT),
    vehicleMakeModel = text(T.VEHICLE_MAKE_MODEL),
    vehicleYears = text(T.VEHICLE_YEARS),
    vehiclePlate = text(T.VEHICLE_PLATE),
    vehicleVin = text(T.VEHICLE_VIN),
    vehicleMileage = text(T.VEHICLE_MILEAGE),
    unitModel = text(T.UNIT_MODEL),
    unitId = text(T.UNIT_ID),
    unitFirmware = text(T.UNIT_FIRMWARE),
    unitBootloader = text(T.UNIT_BOOTLOADER),
    unitCan = text(T.UNIT_CAN),
    unitCanCode = text(T.UNIT_CAN_CODE),
    unitBluetooth = text(T.UNIT_BLUETOOTH),
    unitImei = text(T.UNIT_IMEI),
    manualUnitNote = text(T.MANUAL_UNIT_NOTE),
    configurationName = text(T.CONFIGURATION_NAME),
    configurationAuthor = text(T.CONFIGURATION_AUTHOR),
    configurationCloudId = text(T.CONFIGURATION_CLOUD_ID),
    workSettings = text(T.WORK_SETTINGS),
    workSettingsWritten = text(T.WORK_SETTINGS_WRITTEN),
    workBypass = text(T.WORK_BYPASS),
    workBypassTrained = text(T.WORK_BYPASS_TRAINED),
    workBypassCloneNeeded = text(T.WORK_BYPASS_CLONE_NEEDED),
    workClone = text(T.WORK_CLONE),
    workCloneDone = text(T.WORK_CLONE_DONE),
    workSlave = text(T.WORK_SLAVE),
    workSlaveDone = text(T.WORK_SLAVE_DONE),
    workDuration = text(T.WORK_DURATION),
    checkPassedOfTotal = text(T.CHECK_PASSED_OF_TOTAL),
    checkNumber = text(T.CHECK_NUMBER),
    checkTask = text(T.CHECK_TASK),
    checkResult = text(T.CHECK_RESULT),
    checkPassed = text(T.CHECK_PASSED),
    checkFailed = text(T.CHECK_FAILED),
    checkSkipped = text(T.CHECK_SKIPPED),
    deviceType = text(T.DEVICE_TYPE),
    deviceNumber = text(T.DEVICE_NUMBER),
    deviceAddress = text(T.DEVICE_ADDRESS),
    deviceLink = text(T.DEVICE_LINK),
    deviceName = text(T.DEVICE_NAME),
    deviceSlot = text(T.DEVICE_SLOT),
    deviceNumberValue = text(T.DEVICE_NUMBER_VALUE),
    deviceOnline = text(T.DEVICE_ONLINE),
    deviceOffline = text(T.DEVICE_OFFLINE),
    devicePaired = text(T.DEVICE_PAIRED),
    deviceNotPaired = text(T.DEVICE_NOT_PAIRED),
    deviceTypes = DeviceType.entries.mapNotNull { type ->
        type.kind()?.let(deviceTypes::get)?.let { type.name to it }
    }.toMap(),
    installerName = text(T.INSTALLER_NAME),
    installerContact = text(T.INSTALLER_CONTACT),
    installerOrganization = text(T.INSTALLER_ORGANIZATION),
    installerAddress = text(T.INSTALLER_ADDRESS),
    clientName = text(T.CLIENT_NAME),
    clientPhone = text(T.CLIENT_PHONE),
    signedBy = text(T.SIGNED_BY),
    acceptedBy = text(T.ACCEPTED_BY),
    signature = text(T.SIGNATURE),
    fullName = text(T.FULL_NAME),
    mileageValue = text(T.MILEAGE_VALUE),
    durationHoursMinutes = text(T.DURATION_HOURS_MINUTES),
    durationHours = text(T.DURATION_HOURS),
    durationMinutes = text(T.DURATION_MINUTES),
)

internal fun PassportDocumentFormat.toSdk(): MountPassportDocumentFormat =
    MountPassportDocumentFormat(language, months, datePattern, thousandsSeparator, timeZoneId)

@Suppress("CyclomaticComplexMethod")
private fun DeviceType.kind(): PassportDeviceKind? = when (this) {
    DeviceType.TAG_760, DeviceType.TAG_760_V3 -> PassportDeviceKind.TAG

    DeviceType.REMOTE_CONTROL,
    DeviceType.REMOTE_CONTROL_D030,
    DeviceType.REMOTE_CONTROL_D_035,
    -> PassportDeviceKind.KEYFOB

    DeviceType.WATCH -> PassportDeviceKind.WATCH

    DeviceType.BAND -> PassportDeviceKind.BRACELET

    DeviceType.RELAY_BTR_101, DeviceType.RELAY_BTR_101_V3, DeviceType.RELAY_BT_02 -> PassportDeviceKind.RELAY

    DeviceType.HOOD_LOCK -> PassportDeviceKind.HOOD_LOCK

    DeviceType.RHM, DeviceType.RHM_V2 -> PassportDeviceKind.RHM

    DeviceType.SIREN -> PassportDeviceKind.SIREN

    DeviceType.DOOR_MODULE_DMS, DeviceType.DOOR_MODULE_DMS_V4 -> PassportDeviceKind.DOOR_MODULE

    DeviceType.TPMS -> PassportDeviceKind.TPMS

    DeviceType.FUEL_SENSOR -> PassportDeviceKind.FUEL_SENSOR

    DeviceType.PIR_SENSOR -> PassportDeviceKind.MOTION_SENSOR

    DeviceType.VS_MODULE -> PassportDeviceKind.VS_MODULE

    DeviceType.GPS_NAV_03, DeviceType.NAV_08, DeviceType.NAV_09_10, DeviceType.NAV_11, DeviceType.NAV_12,
    DeviceType.NAV12_V5, DeviceType.NAV_13,
    -> PassportDeviceKind.NAVIGATION

    DeviceType.DI_04 -> PassportDeviceKind.DI_MODULE

    DeviceType.RF_MODULE -> PassportDeviceKind.RF_MODULE

    DeviceType.IMMOBILIZER -> PassportDeviceKind.IMMOBILIZER

    DeviceType.VEHICLE_CHARGER -> PassportDeviceKind.CHARGER

    DeviceType.NFC_READER -> PassportDeviceKind.NFC_READER

    else -> null
}
