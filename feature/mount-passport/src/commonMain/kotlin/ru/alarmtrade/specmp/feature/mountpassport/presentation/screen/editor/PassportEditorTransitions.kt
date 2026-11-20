package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportBypass
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportClient
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportConfiguration
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStageNotReadException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitMismatchException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportWorks
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

private const val VIN_LENGTH = 17
private val VIN_CHARS = Regex("[A-HJ-NPR-Z0-9]*")

/** VIN — 17 символов: латинские буквы без I, O, Q и цифры; пустой — без ошибки (поле необязательное). */
internal fun vinError(vin: String): VinError? = when {
    vin.isEmpty() -> null
    !VIN_CHARS.matches(vin.uppercase()) -> VinError.INVALID_CHARS
    vin.length != VIN_LENGTH -> VinError.LENGTH
    else -> null
}

internal fun mileageInvalid(mileage: String): Boolean = mileage.isNotBlank() && mileageKm(mileage) == null

internal fun phoneInvalid(digits: String): Boolean = digits.isNotEmpty() && !PhoneNumbers.isValid(digits)

/** Автомобиль одной строкой: свой текст или марка, модель и годы; `null` — не указан. */
internal fun MountPassport.vehicleTitle(): String? = vehicle.customTitle?.takeIf { it.isNotBlank() }
    ?: listOfNotNull(vehicle.brand, vehicle.model, vehicle.years).filter { it.isNotBlank() }.joinToString(" ")
        .takeIf { it.isNotEmpty() }

internal fun MountPassport.vehicleFromCatalog(): Boolean = vehicle.brand != null || vehicle.model != null

internal fun MountPassport.configurationFromMaster(): Boolean =
    origin == PassportOrigin.FAST_MOUNT && configuration?.name != null

internal fun MountPassport.toForm(): PassportForm = PassportForm(
    carTitle = vehicle.customTitle.orEmpty(),
    plate = vehicle.plate.orEmpty(),
    vin = vehicle.vin.orEmpty(),
    mileage = vehicle.mileageKm?.toString().orEmpty(),
    configurationName = configuration?.name.orEmpty(),
    remarks = remarks.orEmpty(),
    clientName = client.name.orEmpty(),
    clientPhone = client.phone?.let(PhoneNumbers::fromRaw).orEmpty(),
    organization = installer.organization.orEmpty(),
    address = installer.address.orEmpty(),
    works = WorksForm(
        settingsWritten = works.settingsWritten,
        bypass = works.bypass == PassportBypass.TRAINED,
        clone = works.clone,
        slave = works.slave,
    ),
)

/** Неверный VIN, пробег и телефон не пишутся: остаётся прежнее значение паспорта. */
internal fun PassportForm.toEdit(passport: MountPassport): PassportEdit = PassportEdit(
    vehicle = passport.vehicle.copy(
        customTitle = if (passport.vehicleFromCatalog()) passport.vehicle.customTitle else carTitle.orNull(),
        plate = plate.orNull(),
        vin = if (vinError(vin) == null) vin.uppercase().orNull() else passport.vehicle.vin,
        mileageKm = if (mileageInvalid(mileage)) passport.vehicle.mileageKm else mileageKm(mileage),
    ),
    configuration = if (passport.configurationFromMaster()) {
        passport.configuration
    } else {
        configurationName.orNull()?.let { name ->
            passport.configuration?.copy(name = name)
                ?: PassportConfiguration(name = name, author = null, cloudId = null)
        }
    },
    works = if (passport.origin == PassportOrigin.FAST_MOUNT) passport.works else works.toWorks(passport.works),
    remarks = remarks.orNull()?.take(MountPassport.MAX_REMARKS_LENGTH),
    client = PassportClient(
        name = clientName.orNull(),
        phone = when {
            clientPhone.isEmpty() -> null
            phoneInvalid(clientPhone) -> passport.client.phone
            else -> PhoneNumbers.toE164(clientPhone)
        },
    ),
    installer = passport.installer.copy(organization = organization.orNull(), address = address.orNull()),
)

internal fun MountPassport.sections(): List<PassportSection> = PassportSection.entries.filter { section ->
    when (section) {
        PassportSection.CHECK -> systemCheck.isNotEmpty()
        PassportSection.DEVICES -> !withoutUnit
        else -> true
    }
}

internal fun MountPassport.settingsStatus(): SettingsSnapshotStatus = when {
    settingsSnapshot != null -> SettingsSnapshotStatus.SAVED

    withoutUnit -> SettingsSnapshotStatus.NO_UNIT

    else -> when (readStages[PassportReadStage.SETTINGS]) {
        PassportReadStatus.NOT_READ, PassportReadStatus.SKIPPED -> SettingsSnapshotStatus.NOT_READ

        PassportReadStatus.UNAVAILABLE ->
            if (unit.can?.factory == true) SettingsSnapshotStatus.FACTORY_CAN else SettingsSnapshotStatus.UNAVAILABLE

        PassportReadStatus.READ, null -> SettingsSnapshotStatus.MISSING
    }
}

private fun WorksForm.toWorks(previous: PassportWorks): PassportWorks = previous.copy(
    fromMaster = false,
    settingsWritten = settingsWritten,
    bypass = when {
        bypass -> PassportBypass.TRAINED
        previous.bypass == PassportBypass.TRAINED -> null
        else -> previous.bypass
    },
    clone = clone,
    slave = slave,
)

private fun mileageKm(text: String): Long? =
    text.filterNot(Char::isWhitespace).takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toLongOrNull()

private fun String.orNull(): String? = trim().takeIf { it.isNotEmpty() }

internal fun PassportForm.with(field: PassportField, value: String): PassportForm {
    val text = value.take(MAX_FIELD_LENGTH)
    return when (field) {
        PassportField.CAR -> copy(carTitle = text)
        PassportField.PLATE -> copy(plate = text)
        PassportField.VIN -> copy(vin = text.filterNot(Char::isWhitespace).uppercase().take(VIN_LENGTH + 1))
        PassportField.MILEAGE -> copy(mileage = text)
        PassportField.CONFIGURATION -> copy(configurationName = text)
        PassportField.REMARKS -> copy(remarks = value.take(MountPassport.MAX_REMARKS_LENGTH))
        PassportField.CLIENT_NAME -> copy(clientName = text)
        PassportField.CLIENT_PHONE -> copy(clientPhone = PhoneNumbers.fromInput(value))
        PassportField.ORGANIZATION -> copy(organization = text)
        PassportField.ADDRESS -> copy(address = text)
    }
}

internal fun WorksForm.with(work: PassportWork, checked: Boolean): WorksForm = when (work) {
    PassportWork.SETTINGS_WRITTEN -> copy(settingsWritten = checked)
    PassportWork.BYPASS -> copy(bypass = checked)
    PassportWork.CLONE -> copy(clone = checked)
    PassportWork.SLAVE -> copy(slave = checked)
}

internal fun LCEState<MountPassport, PassportError>.replacedWith(
    passport: MountPassport,
): LCEState<MountPassport, PassportError> = LCEState.Content(passport)

internal fun LCEState<MountPassport, PassportError>.withCaption(
    photoId: String,
    caption: String,
): LCEState<MountPassport, PassportError> {
    val passport = content ?: return this
    return LCEState.Content(
        passport.copy(photos = passport.photos.map { if (it.id == photoId) it.copy(caption = caption) else it }),
    )
}

private const val MAX_FIELD_LENGTH = 200

/** Этап не прочитан или пропущен при считывании — его можно перечитать. */
internal fun MountPassport.needsReread(stage: PassportReadStage): Boolean =
    readStages[stage] == PassportReadStatus.NOT_READ || readStages[stage] == PassportReadStatus.SKIPPED

internal fun Throwable.toRereadError(): RereadError = when (this) {
    is PassportUnitMismatchException -> RereadError.WrongUnit(actualSystemId)
    is PassportStageNotReadException -> RereadError.NotRead(connectionLost)
    else -> RereadError.Common(PassportError.from(this))
}
