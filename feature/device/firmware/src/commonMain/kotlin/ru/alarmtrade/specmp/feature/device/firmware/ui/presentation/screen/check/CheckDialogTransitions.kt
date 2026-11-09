package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import kotlin.time.Duration

internal fun CheckDialog?.withPinLock(lock: Duration?): CheckDialog? =
    if (this is CheckDialog.Pin) copy(lockedFor = lock) else this

internal fun CheckDialog?.sending(moduleId: Long, withPin: Boolean): CheckDialog = if (withPin) {
    val previous = this as? CheckDialog.Pin
    CheckDialog.Pin(moduleId, sending = true, wrongCount = previous?.wrongCount ?: 0)
} else {
    CheckDialog.ConfirmBoot(moduleId, sending = true)
}

internal fun CheckDialog?.isSending(): Boolean = (this as? CheckDialog.ConfirmBoot)?.sending == true ||
    (this as? CheckDialog.Pin)?.sending == true

internal fun CheckDialog?.nextPin(moduleId: Long, wrongPin: Boolean): CheckDialog.Pin {
    val previous = this as? CheckDialog.Pin
    val wrongCount = (previous?.wrongCount ?: 0) + if (wrongPin) 1 else 0
    return CheckDialog.Pin(moduleId, wrong = wrongPin, wrongCount = wrongCount)
}

/** [next] вместо текущего диалога; отсчёт блокировки идёт отдельным потоком — новый диалог PIN его сохраняет. */
internal fun CheckDialog?.replacedBy(next: CheckDialog?): CheckDialog? =
    next.withPinLock((this as? CheckDialog.Pin)?.lockedFor)
