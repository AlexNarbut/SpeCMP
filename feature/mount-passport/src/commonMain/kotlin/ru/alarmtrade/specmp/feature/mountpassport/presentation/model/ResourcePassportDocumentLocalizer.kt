package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getStringArray
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource
import ru.alarmtrade.specmp.core.files.PdfFooter
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_months
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_can_code
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_check_summary
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_date
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_device_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_file_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_footer_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_footer_pages
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_hours
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_hours_minutes
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_mileage
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_minutes
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_format_thousands
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_accepted_by
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_address
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_author
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_bootloader
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_bypass
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_can
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_check_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_check_result
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_check_task
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_client_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_client_phone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_clone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_cloud_id
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_configuration_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_address
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_id
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_link
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_slot
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_device_type
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_duration
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_full_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_imei
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_installer_contact
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_installer_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_make_model
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_mileage
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_model
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_organization
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_plate
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_signature
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_signed_by
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_slave
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_vin
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_label_years
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_msg_manual_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_not_paired
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_online
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_paired
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_passed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_status_skipped
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_check
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_client
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_configuration
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_document
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_equipment
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_installer
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_photos
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_remarks
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_vehicle
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_title_works
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_bypass_clone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_bypass_trained
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_clone_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_language
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_settings_written
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_slave_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_bracelet
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_charger
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_di
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_door
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_fuel
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_hood_lock
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_immobilizer
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_keyfob
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_motion
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_navigation
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_nfc
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_relay
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_rf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_rhm
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_siren
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_tag
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_tpms
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_vs
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_document_value_type_watch
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDeviceKind
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentText

/** Тексты документа — из `core/localization` (`mountpassport_document_*`), язык — как у интерфейса. */
internal class ResourcePassportDocumentLocalizer(private val timeZones: TimeZoneSource) : PassportDocumentLocalizer {

    override suspend fun strings(): PassportDocumentStrings = PassportDocumentStrings(
        texts = PassportDocumentText.entries.associateWith { getString(it.resource()) },
        deviceTypes = PassportDeviceKind.entries.associateWith { getString(it.resource()) },
    )

    override suspend fun format(): PassportDocumentFormat = PassportDocumentFormat(
        language = getString(Strings.string.mountpassport_document_value_language),
        months = getStringArray(Strings.array.common_date_label_months),
        datePattern = getString(Strings.string.mountpassport_document_format_date),
        thousandsSeparator = getString(Strings.string.mountpassport_document_format_thousands),
        timeZoneId = timeZones.current().id,
    )

    override suspend fun footer(number: String): PdfFooter = PdfFooter(
        start = getString(Strings.string.mountpassport_document_format_footer_number, number),
        end = getString(Strings.string.mountpassport_document_format_footer_pages),
    )

    override suspend fun fileName(number: String, subject: String): String =
        getString(Strings.string.mountpassport_document_format_file_name, number, subject)
}

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun PassportDocumentText.resource(): StringResource = when (this) {
    PassportDocumentText.TITLE -> Strings.string.mountpassport_document_title_document
    PassportDocumentText.NUMBER_PREFIX -> Strings.string.mountpassport_document_label_number
    PassportDocumentText.VEHICLE -> Strings.string.mountpassport_document_title_vehicle
    PassportDocumentText.EQUIPMENT -> Strings.string.mountpassport_document_title_equipment
    PassportDocumentText.CONFIGURATION -> Strings.string.mountpassport_document_title_configuration
    PassportDocumentText.WORKS -> Strings.string.mountpassport_document_title_works
    PassportDocumentText.SYSTEM_CHECK -> Strings.string.mountpassport_document_title_check
    PassportDocumentText.DEVICES -> Strings.string.mountpassport_document_title_devices
    PassportDocumentText.REMARKS -> Strings.string.mountpassport_document_title_remarks
    PassportDocumentText.PHOTOS -> Strings.string.mountpassport_document_title_photos
    PassportDocumentText.INSTALLER -> Strings.string.mountpassport_document_title_installer
    PassportDocumentText.CLIENT -> Strings.string.mountpassport_document_title_client
    PassportDocumentText.VEHICLE_MAKE_MODEL -> Strings.string.mountpassport_document_label_make_model
    PassportDocumentText.VEHICLE_YEARS -> Strings.string.mountpassport_document_label_years
    PassportDocumentText.VEHICLE_PLATE -> Strings.string.mountpassport_document_label_plate
    PassportDocumentText.VEHICLE_VIN -> Strings.string.mountpassport_document_label_vin
    PassportDocumentText.VEHICLE_MILEAGE -> Strings.string.mountpassport_document_label_mileage
    PassportDocumentText.UNIT_MODEL -> Strings.string.mountpassport_document_label_model
    PassportDocumentText.UNIT_ID -> Strings.string.mountpassport_document_label_device_id
    PassportDocumentText.UNIT_FIRMWARE -> Strings.string.mountpassport_document_label_firmware
    PassportDocumentText.UNIT_BOOTLOADER -> Strings.string.mountpassport_document_label_bootloader
    PassportDocumentText.UNIT_CAN -> Strings.string.mountpassport_document_label_can
    PassportDocumentText.UNIT_CAN_CODE -> Strings.string.mountpassport_document_format_can_code
    PassportDocumentText.UNIT_BLUETOOTH -> Strings.string.mountpassport_document_label_bluetooth
    PassportDocumentText.UNIT_IMEI -> Strings.string.mountpassport_document_label_imei
    PassportDocumentText.MANUAL_UNIT_NOTE -> Strings.string.mountpassport_document_msg_manual_unit
    PassportDocumentText.CONFIGURATION_NAME -> Strings.string.mountpassport_document_label_configuration_name
    PassportDocumentText.CONFIGURATION_AUTHOR -> Strings.string.mountpassport_document_label_author
    PassportDocumentText.CONFIGURATION_CLOUD_ID -> Strings.string.mountpassport_document_label_cloud_id
    PassportDocumentText.WORK_SETTINGS -> Strings.string.mountpassport_document_label_settings
    PassportDocumentText.WORK_SETTINGS_WRITTEN -> Strings.string.mountpassport_document_value_settings_written
    PassportDocumentText.WORK_BYPASS -> Strings.string.mountpassport_document_label_bypass
    PassportDocumentText.WORK_BYPASS_TRAINED -> Strings.string.mountpassport_document_value_bypass_trained
    PassportDocumentText.WORK_BYPASS_CLONE_NEEDED -> Strings.string.mountpassport_document_value_bypass_clone
    PassportDocumentText.WORK_CLONE -> Strings.string.mountpassport_document_label_clone
    PassportDocumentText.WORK_CLONE_DONE -> Strings.string.mountpassport_document_value_clone_done
    PassportDocumentText.WORK_SLAVE -> Strings.string.mountpassport_document_label_slave
    PassportDocumentText.WORK_SLAVE_DONE -> Strings.string.mountpassport_document_value_slave_done
    PassportDocumentText.WORK_DURATION -> Strings.string.mountpassport_document_label_duration
    PassportDocumentText.CHECK_PASSED_OF_TOTAL -> Strings.string.mountpassport_document_format_check_summary
    PassportDocumentText.CHECK_NUMBER -> Strings.string.mountpassport_document_label_check_number
    PassportDocumentText.CHECK_TASK -> Strings.string.mountpassport_document_label_check_task
    PassportDocumentText.CHECK_RESULT -> Strings.string.mountpassport_document_label_check_result
    PassportDocumentText.CHECK_PASSED -> Strings.string.mountpassport_document_status_passed
    PassportDocumentText.CHECK_FAILED -> Strings.string.mountpassport_document_status_failed
    PassportDocumentText.CHECK_SKIPPED -> Strings.string.mountpassport_document_status_skipped
    PassportDocumentText.DEVICE_TYPE -> Strings.string.mountpassport_document_label_device_type
    PassportDocumentText.DEVICE_NUMBER -> Strings.string.mountpassport_document_label_device_number
    PassportDocumentText.DEVICE_ADDRESS -> Strings.string.mountpassport_document_label_device_address
    PassportDocumentText.DEVICE_LINK -> Strings.string.mountpassport_document_label_device_link
    PassportDocumentText.DEVICE_NAME -> Strings.string.mountpassport_document_label_device_name
    PassportDocumentText.DEVICE_SLOT -> Strings.string.mountpassport_document_label_device_slot
    PassportDocumentText.DEVICE_NUMBER_VALUE -> Strings.string.mountpassport_document_format_device_number
    PassportDocumentText.DEVICE_ONLINE -> Strings.string.mountpassport_document_status_online
    PassportDocumentText.DEVICE_OFFLINE -> Strings.string.mountpassport_document_status_offline
    PassportDocumentText.DEVICE_PAIRED -> Strings.string.mountpassport_document_status_paired
    PassportDocumentText.DEVICE_NOT_PAIRED -> Strings.string.mountpassport_document_status_not_paired
    PassportDocumentText.INSTALLER_NAME -> Strings.string.mountpassport_document_label_installer_name
    PassportDocumentText.INSTALLER_CONTACT -> Strings.string.mountpassport_document_label_installer_contact
    PassportDocumentText.INSTALLER_ORGANIZATION -> Strings.string.mountpassport_document_label_organization
    PassportDocumentText.INSTALLER_ADDRESS -> Strings.string.mountpassport_document_label_address
    PassportDocumentText.CLIENT_NAME -> Strings.string.mountpassport_document_label_client_name
    PassportDocumentText.CLIENT_PHONE -> Strings.string.mountpassport_document_label_client_phone
    PassportDocumentText.SIGNED_BY -> Strings.string.mountpassport_document_label_signed_by
    PassportDocumentText.ACCEPTED_BY -> Strings.string.mountpassport_document_label_accepted_by
    PassportDocumentText.SIGNATURE -> Strings.string.mountpassport_document_label_signature
    PassportDocumentText.FULL_NAME -> Strings.string.mountpassport_document_label_full_name
    PassportDocumentText.MILEAGE_VALUE -> Strings.string.mountpassport_document_format_mileage
    PassportDocumentText.DURATION_HOURS_MINUTES -> Strings.string.mountpassport_document_format_hours_minutes
    PassportDocumentText.DURATION_HOURS -> Strings.string.mountpassport_document_format_hours
    PassportDocumentText.DURATION_MINUTES -> Strings.string.mountpassport_document_format_minutes
}

@Suppress("CyclomaticComplexMethod")
private fun PassportDeviceKind.resource(): StringResource = when (this) {
    PassportDeviceKind.TAG -> Strings.string.mountpassport_document_value_type_tag
    PassportDeviceKind.KEYFOB -> Strings.string.mountpassport_document_value_type_keyfob
    PassportDeviceKind.WATCH -> Strings.string.mountpassport_document_value_type_watch
    PassportDeviceKind.BRACELET -> Strings.string.mountpassport_document_value_type_bracelet
    PassportDeviceKind.RELAY -> Strings.string.mountpassport_document_value_type_relay
    PassportDeviceKind.HOOD_LOCK -> Strings.string.mountpassport_document_value_type_hood_lock
    PassportDeviceKind.RHM -> Strings.string.mountpassport_document_value_type_rhm
    PassportDeviceKind.SIREN -> Strings.string.mountpassport_document_value_type_siren
    PassportDeviceKind.DOOR_MODULE -> Strings.string.mountpassport_document_value_type_door
    PassportDeviceKind.TPMS -> Strings.string.mountpassport_document_value_type_tpms
    PassportDeviceKind.FUEL_SENSOR -> Strings.string.mountpassport_document_value_type_fuel
    PassportDeviceKind.MOTION_SENSOR -> Strings.string.mountpassport_document_value_type_motion
    PassportDeviceKind.VS_MODULE -> Strings.string.mountpassport_document_value_type_vs
    PassportDeviceKind.NAVIGATION -> Strings.string.mountpassport_document_value_type_navigation
    PassportDeviceKind.DI_MODULE -> Strings.string.mountpassport_document_value_type_di
    PassportDeviceKind.RF_MODULE -> Strings.string.mountpassport_document_value_type_rf
    PassportDeviceKind.IMMOBILIZER -> Strings.string.mountpassport_document_value_type_immobilizer
    PassportDeviceKind.CHARGER -> Strings.string.mountpassport_document_value_type_charger
    PassportDeviceKind.NFC_READER -> Strings.string.mountpassport_document_value_type_nfc
}
