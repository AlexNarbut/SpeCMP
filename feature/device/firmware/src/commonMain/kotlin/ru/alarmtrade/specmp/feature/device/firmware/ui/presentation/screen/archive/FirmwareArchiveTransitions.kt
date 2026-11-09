package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionNumber
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

internal val FirmwareVersion.archiveKey: String get() = archiveRowKey(version, canCode, canVersion)

internal val FirmwareArchiveRequest.canFilterOn: Boolean get() = canFilterAllowed && profile == UpdateProfile.Main

internal fun FirmwareArchiveRequest.initialState(exporter: FirmwareFileExporter?): FirmwareArchiveState {
    val can = canCode ?: deviceCanCode
    return FirmwareArchiveState(
        channel = channel,
        installedVersion = installedVersion,
        deviceTitle = deviceTitle,
        profile = profile,
        viewOnly = viewOnly || noUnit,
        deviceName = deviceName,
        noUnit = noUnit,
        moduleName = moduleName,
        saveTarget = exporter?.target ?: FirmwareSaveTarget.Downloads,
        canShare = exporter?.canShare == true,
        canOpenFolder = exporter?.canOpenFolder == true,
    ).withSource(ArchiveSource(moduleId = moduleId, canInput = can.orEmpty(), canApplied = can), this)
}

/** [next] и посчитанные из него поля экрана; строки пересобираются только при новом списке версий. */
internal fun FirmwareArchiveState.withSource(
    next: ArchiveSource,
    request: FirmwareArchiveRequest,
): FirmwareArchiveState {
    val source = if (next.items === source.items) next else next.copy(rows = request.rowsOf(next.items))
    val rows = source.rows
    val content = when (val load = source.load) {
        LCEState.None, LCEState.Loading -> LCEState.Loading
        is LCEState.Error -> load
        is LCEState.Content -> LCEState.Content(rows, isUpdating = source.loadingMore, updateError = source.moreError)
    }
    val installed = request.installedVersion
    val rollbackItem = source.rollback?.let { key -> source.items.firstOrNull { it.archiveKey == key } }
    return copy(
        source = source,
        content = content,
        selected = source.selected ?: rows.firstOrNull()?.key,
        rollback = rollbackItem?.let { item -> installed?.let { RollbackUi(item.version, it) } },
        canFilter = if (request.canFilterOn) {
            CanFilterUi(
                input = source.canInput,
                applied = source.canApplied,
                carTitle = source.carTitle,
                deviceCode = request.deviceCanCode,
                deviceProfile = request.deviceCanProfile,
                deviceFactory = request.deviceCanFactory,
                noUnit = request.noUnit,
            )
        } else {
            null
        },
        noModelFirmware = request.noUnit && source.load is LCEState.Content &&
            (source.noModule || (rows.isEmpty() && source.canApplied == null)),
    )
}

/** Строки списка от новой версии к старой; номер каждой версии разбирается один раз. */
private fun FirmwareArchiveRequest.rowsOf(items: List<FirmwareVersion>): List<ArchiveRowUi> = items
    .map { FirmwareVersionNumber.parse(it.version) to it }
    .sortedByDescending { it.first }
    .map { (_, version) ->
        ArchiveRowUi(
            version = version.version,
            createdOn = version.createdOn,
            description = version.description,
            kind = kindOf(version),
            canCode = version.canCode,
            canVersion = version.canVersion,
            canMatchesDevice = deviceCanCode != null && version.canCode == deviceCanCode,
            canProfileDiffers = deviceCanCode != null && version.canCode == deviceCanCode &&
                version.canVersionCode != null && deviceCanVersion != null &&
                version.canVersionCode != deviceCanVersion,
        )
    }

/**
 * Отношение к установленной версии. «Установлена» у сборки под CAN — только под CAN-код и профиль блока: тот же
 * номер под другой CAN — другой файл, его установка не переустановка.
 */
internal fun FirmwareArchiveRequest.kindOf(version: FirmwareVersion): ArchiveRowKind {
    val installed = installedVersion ?: return ArchiveRowKind.New
    return when (version.version.relationTo(installed)) {
        VersionRelation.Newer -> ArchiveRowKind.New
        VersionRelation.Same -> if (isDeviceBuild(version)) ArchiveRowKind.Installed else ArchiveRowKind.New
        VersionRelation.Older -> ArchiveRowKind.Older
    }
}

/** Сборка без CAN — любая; под CAN — под код блока и (если обе известны) его версию профиля. */
private fun FirmwareArchiveRequest.isDeviceBuild(version: FirmwareVersion): Boolean {
    val code = version.canCode ?: return true
    val deviceCode = deviceCanCode ?: return !deviceCanFactory
    val profile = version.canVersionCode
    val deviceProfile = deviceCanVersion
    return code == deviceCode && (profile == null || deviceProfile == null || profile == deviceProfile)
}

/** Фильтр запроса: код из поля; версия CAN-профиля блока — только к коду блока (к другому не относится). */
internal fun FirmwareArchiveRequest.canFilterOf(code: String?): FirmwareCan? =
    code?.let { FirmwareCan(it, if (it == deviceCanCode) deviceCanVersion else null) }

/** `<имя подсистемы>_3.61[_CAN2154-1.03].<расширение файла с сервера>`. */
internal fun FirmwareArchiveRequest.fileNameOf(item: FirmwareVersion): String {
    val extension = item.url.substringBefore('?').substringAfterLast('/').substringAfterLast('.', "")
        .takeIf { it.isNotBlank() && it.length <= MAX_EXTENSION } ?: DEFAULT_EXTENSION
    val can = item.canCode?.let { code -> "_CAN$code" + item.canVersion?.let { "-$it" }.orEmpty() }.orEmpty()
    // Ревизии сервера — с пробелом («3.57 C»): в имени файла без пробелов.
    val version = item.version.filterNot(Char::isWhitespace)
    val base = (moduleName ?: deviceName.orEmpty()).filterNot(Char::isWhitespace)
    return "${base}_$version$can.$extension"
}

private const val MAX_EXTENSION = 5
private const val DEFAULT_EXTENSION = "bin"
