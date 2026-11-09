package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickKind
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.withoutSettingsExtension
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.capabilities
import ru.alarmtrade.specmp.feature.device.settings.domain.model.deviceNameOrNull

/*
 * Редактор настроек: стартовый state и переходы state без I/O (чистые функции; запросы — во VM).
 */

internal fun initialSettingsState(
    source: SettingsSource,
    options: SettingsEditorOptions,
    treeWidthDp: Int?,
): DeviceSettingsState = DeviceSettingsState(
    treeWidthDp = treeWidthDp,
    canResetToFactory = source.capabilities.canResetToFactory,
    source = source.header(),
    canWriteToDevice = options.canWriteToDevice,
    pendingLoad = options.loadOnOpen,
)

/**
 * Перед чтением меню: прежние правки, окна и сравнение устарели (прерванное обрывом связи осталось бы в Busy);
 * подписи автомобилей запрашиваются заново, «загружено» сбрасывается.
 */
internal fun DeviceSettingsState.withMenuLoading(): DeviceSettingsState = copy(
    menu = LCEState.Loading,
    changedIds = emptySet(),
    sectionChanges = emptyMap(),
    editing = null,
    dialog = null,
    diff = null,
    isWriting = false,
    isResetting = false,
    requestedCarIds = emptySet(),
    changes = changes.withoutLoaded(),
)

/** Новый черновик окна; другой CAN-код — найденные автомобили к нему не относятся. */
internal fun DeviceSettingsState.withDraft(draft: SettingValue): DeviceSettingsState {
    val editing = editing ?: return this
    val codeChanged = (draft as? SettingValue.Can)?.value?.code != (editing.draft as? SettingValue.Can)?.value?.code
    return copy(
        editing = editing.copy(draft = draft, carSearch = if (codeChanged) LCEState.None else editing.carSearch),
    )
}

/**
 * Подтверждение выхода с незаписанными изменениями (блок — «Записать и выйти», конфигурация — «Сохранить»);
 * `null` — изменений нет, выходим сразу.
 */
internal fun DeviceSettingsState.exitDialog(target: ExitTarget): SettingsDialog? = when {
    changedCount == 0 -> null
    source.kind == SourceKind.DEVICE -> SettingsDialog.ExitConfirm(target)
    else -> SettingsDialog.ExitSave
}

/** Куда выйти из открытого диалога выхода; `null` — диалога нет. */
internal fun SettingsDialog?.exitTarget(): ExitTarget? = when (this) {
    is SettingsDialog.ExitConfirm -> target
    SettingsDialog.ExitSave -> ExitTarget.BACK
    else -> null
}

internal fun DeviceSettingsState.withConfigurationPicker(
    kind: LoadSourceKind,
    deviceName: String,
): DeviceSettingsState = copy(
    configurationPicker = ConfigurationPickRequest(
        kind = if (kind == LoadSourceKind.MINE) ConfigurationPickKind.MINE else ConfigurationPickKind.CLOUD,
        deviceName = deviceName,
        firmwareCanCode = header?.singleCanCode,
    ),
)

/** Сравнение с [from] не удалось: файл — ошибка файла, иначе «Повторить» с тем же [closeAfter]. */
internal fun DeviceSettingsState.withPreviewFailed(from: SettingsSource, closeAfter: Boolean): DeviceSettingsState =
    if (from is SettingsSource.File) {
        copy(diff = null, dialog = SettingsDialog.LoadError(LoadErrorKind.FILE))
    } else {
        copy(diff = DiffState.Error(from.displayTitle, from, closeAfter))
    }

internal fun DeviceSettingsState.withSaveForm(name: String, description: String): DeviceSettingsState = copy(
    saveForm = saveForm?.copy(
        name = name.take(LocalConfiguration.NAME_MAX_LENGTH),
        description = description.take(LocalConfiguration.DESCRIPTION_MAX_LENGTH),
    ),
)

/** Имя новой конфигурации по умолчанию: имя файла — без расширения и с пробелами вместо `_`. */
internal fun SettingsSource.defaultConfigurationName(): String = when (this) {
    is SettingsSource.File -> name.withoutSettingsExtension().replace('_', ' ')
    else -> displayTitle
}.take(LocalConfiguration.NAME_MAX_LENGTH)

/** Часть имени экспортируемого файла после модели: у блока — его номер, у прочих — название источника. */
internal fun SettingsSource.exportTitle(header: SettingsHeader?): String? = when (this) {
    SettingsSource.Device -> header?.deviceId?.toString()
    else -> displayTitle.withoutSettingsExtension().ifEmpty { null }
}

/** Шапка источника; название заводских настроек и подписи — в UI. */
private fun SettingsSource.header(): SourceHeader {
    val kind = when (this) {
        SettingsSource.Device -> return SourceHeader.Device
        is SettingsSource.File -> SourceKind.FILE
        is SettingsSource.LocalConfiguration -> SourceKind.MINE
        is SettingsSource.RemoteConfiguration -> SourceKind.CLOUD
        is SettingsSource.Defaults -> SourceKind.DEFAULTS
    }
    val capabilities = capabilities
    return SourceHeader(kind, displayTitle, deviceNameOrNull.orEmpty(), capabilities.isReadOnly, capabilities.canSave)
}

/** Название источника: файл — имя файла, конфигурации — название; у блока и заводских — пусто (подпись в UI). */
internal val SettingsSource.displayTitle: String
    get() = when (this) {
        is SettingsSource.File -> name
        is SettingsSource.LocalConfiguration -> title
        is SettingsSource.RemoteConfiguration -> title
        SettingsSource.Device, is SettingsSource.Defaults -> ""
    }
