package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage

@Immutable
internal class PassportEditorActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onFieldChange: (PassportField, String) -> Unit,
    val onWorkToggle: (PassportWork, Boolean) -> Unit,
    val onReread: (PassportReadStage) -> Unit,
    val onReadUnit: () -> Unit,
    val onCreatePdf: () -> Unit,
    val photos: PassportPhotoActions,
    val onSaveSettingsFile: () -> Unit,
    val onSaveSettingsToConfigurations: () -> Unit,
)

@Immutable
internal class PassportPhotoActions(
    val onAdd: () -> Unit,
    val onSource: (PhotoSource) -> Unit,
    val onSheetDismiss: () -> Unit,
    val onOpenAppSettings: () -> Unit,
    val onErrorDismiss: () -> Unit,
    val onOpen: (Int) -> Unit,
    val onViewerIndex: (Int) -> Unit,
    val onViewerClose: () -> Unit,
    val onCaptionChange: (String, String) -> Unit,
    val onDeleteRequest: (String) -> Unit,
    val onDeleteConfirm: () -> Unit,
    val onDeleteDismiss: () -> Unit,
)
