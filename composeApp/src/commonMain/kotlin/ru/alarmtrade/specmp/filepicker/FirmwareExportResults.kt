package ru.alarmtrade.specmp.filepicker

import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult

/** Сохранение — тем же путём, что вложения чата («Загрузки/Pandora Spec»); [folder] — папка для показа. */
internal fun SupportChatFileSaveResult.toFirmware(folder: (String) -> String?): FirmwareSaveResult = when (this) {
    is SupportChatFileSaveResult.Saved -> FirmwareSaveResult.Saved(path, folder(path))
    SupportChatFileSaveResult.Cancelled -> FirmwareSaveResult.Cancelled
    SupportChatFileSaveResult.PermissionDenied -> FirmwareSaveResult.PermissionDenied
    SupportChatFileSaveResult.Failed -> FirmwareSaveResult.Failed
}
