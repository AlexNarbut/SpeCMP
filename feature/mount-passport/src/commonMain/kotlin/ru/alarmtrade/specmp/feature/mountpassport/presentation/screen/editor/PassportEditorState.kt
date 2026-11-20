package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

@Immutable
internal data class PassportEditorArgs(val passportId: String)

/** Ручные поля предотчёта как их видит экран: телефон — цифры с кодом страны, пробег — текст как введён. */
@Immutable
internal data class PassportForm(
    val carTitle: String = "",
    val plate: String = "",
    val vin: String = "",
    val mileage: String = "",
    val configurationName: String = "",
    val remarks: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val organization: String = "",
    val address: String = "",
    val works: WorksForm = WorksForm(),
)

@Immutable
internal data class WorksForm(
    val settingsWritten: Boolean = false,
    val bypass: Boolean = false,
    val clone: Boolean = false,
    val slave: Boolean = false,
)

internal enum class PassportWork { SETTINGS_WRITTEN, BYPASS, CLONE, SLAVE }

internal enum class PassportField {
    CAR,
    PLATE,
    VIN,
    MILEAGE,
    CONFIGURATION,
    REMARKS,
    CLIENT_NAME,
    CLIENT_PHONE,
    ORGANIZATION,
    ADDRESS,
}

/** `PENDING` — правка ждёт записи (debounce) или пишется. */
internal enum class SaveStatus { SAVED, PENDING, FAILED }

internal enum class VinError { INVALID_CHARS, LENGTH }

internal enum class PhotoSource { CAMERA, GALLERY }

/** Лист выбора источника фото: выбор или «нет доступа к камере». */
@Immutable
internal sealed interface PhotoSheet {
    data object Pick : PhotoSheet

    data class CameraDenied(val canOpenSettings: Boolean) : PhotoSheet
}

/** Секции в порядке документа. */
internal enum class PassportSection {
    VEHICLE,
    EQUIPMENT,
    CONFIGURATION,
    WORKS,
    CHECK,
    DEVICES,
    PHOTOS,
    REMARKS,
    CLIENT,
    INSTALLER,
}

/** Состояние снимка настроек блока для строки «Настройки блока». */
internal enum class SettingsSnapshotStatus { SAVED, NOT_READ, FACTORY_CAN, UNAVAILABLE, NO_UNIT, MISSING }

/**
 * [connectedSystemId] — ID подключённого сейчас блока; [viewer] — индекс фото на весь экран; [photoToDelete] — открыт
 * диалог удаления фото; [canTakePhoto] — есть камера (desktop — сразу файл); [rereading] — идёт «Перечитать» этапа.
 */
@Immutable
internal data class PassportEditorState(
    val passportId: String,
    val passport: LCEState<MountPassport, PassportError> = LCEState.Loading,
    val form: PassportForm = PassportForm(),
    val save: SaveStatus = SaveStatus.SAVED,
    val connectedSystemId: String? = null,
    val canTakePhoto: Boolean = true,
    val photoSheet: PhotoSheet? = null,
    val addingPhoto: Boolean = false,
    val photoError: PassportError? = null,
    val viewer: Int? = null,
    val photoToDelete: String? = null,
    val rereading: PassportReadStage? = null,
) {
    private val loaded: MountPassport? get() = passport.content

    /** «Перечитать» — только при подключённом том же блоке и у считанного паспорта. */
    val rereadVisible: Boolean
        get() = loaded?.let { !it.withoutUnit && it.unit.systemId == connectedSystemId } == true

    /** Паспорт без блока, а блок с этим ID подключён — «Считать блок». */
    val readUnitVisible: Boolean
        get() = loaded?.let { it.withoutUnit && it.unit.systemId == connectedSystemId } == true

    /** Работы мастера быстрого монтажа — только чтение; в расширенном монтаже и без блока — галочками. */
    val worksEditable: Boolean get() = loaded?.origin != PassportOrigin.FAST_MOUNT

    val vinError: VinError? get() = vinError(form.vin)

    val mileageInvalid: Boolean get() = mileageInvalid(form.mileage)

    val phoneInvalid: Boolean get() = phoneInvalid(form.clientPhone)
}
