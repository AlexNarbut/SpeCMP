@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.feature.device.settings.data.files

import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

internal actual fun settingsScratchDirectory(): String {
    val directory = NSTemporaryDirectory().trimEnd('/') + "/" + SETTINGS_SCRATCH_DIR
    NSFileManager.defaultManager.createDirectoryAtPath(
        path = directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null
    )
    return directory
}

internal actual fun deleteSettingsScratchFile(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

private const val SETTINGS_SCRATCH_DIR = "settings-files"
