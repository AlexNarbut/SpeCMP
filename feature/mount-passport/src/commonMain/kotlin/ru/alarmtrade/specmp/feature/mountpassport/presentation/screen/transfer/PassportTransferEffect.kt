package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportTransferEffect {
    /** Архив собран (до отправки): список выходит из режима выбора, копия всех снимает напоминание. */
    data class ArchiveBuilt(val fullBackup: Boolean) : PassportTransferEffect

    data object Shared : PassportTransferEffect

    data class Saved(val name: String) : PassportTransferEffect

    data object SaveFailed : PassportTransferEffect

    data object ShareFailed : PassportTransferEffect

    data object PickFailed : PassportTransferEffect

    data class PdfSaved(val name: String) : PassportTransferEffect

    data object PdfSaveFailed : PassportTransferEffect

    data object PdfShareFailed : PassportTransferEffect

    data class PdfFailed(val error: PassportError) : PassportTransferEffect
}
