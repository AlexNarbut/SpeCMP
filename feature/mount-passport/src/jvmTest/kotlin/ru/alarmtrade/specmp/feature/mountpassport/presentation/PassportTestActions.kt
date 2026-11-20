package ru.alarmtrade.specmp.feature.mountpassport.presentation

import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportPhotoActions

internal fun editorActions(onCreatePdf: () -> Unit = {}, onExportPdf: () -> Unit = {}, onExportZip: () -> Unit = {}) =
    PassportEditorActions(
        onBack = {},
        onRetry = {},
        onFieldChange = { _, _ -> },
        onPickVehicle = {},
        onWorkToggle = { _, _ -> },
        onReread = {},
        onReadUnit = {},
        onCreatePdf = onCreatePdf,
        photos = PassportPhotoActions({}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {}, {}, {}, {}),
        onSaveSettingsFile = {},
        onSaveSettingsToConfigurations = {},
        onSectionToggle = {},
        onExportPdf = onExportPdf,
        onExportZip = onExportZip,
    )
