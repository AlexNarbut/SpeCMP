package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

internal sealed interface PassportTransferEffect {
    /** Архив собран (до отправки): список выходит из режима выбора, копия всех снимает напоминание. */
    data class ArchiveBuilt(val fullBackup: Boolean) : PassportTransferEffect

    data object Shared : PassportTransferEffect

    data class Saved(val name: String) : PassportTransferEffect

    data object SaveFailed : PassportTransferEffect

    data object ShareFailed : PassportTransferEffect

    data object PickFailed : PassportTransferEffect
}
