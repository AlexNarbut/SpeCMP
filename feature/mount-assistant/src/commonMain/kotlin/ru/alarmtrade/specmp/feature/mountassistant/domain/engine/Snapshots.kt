package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockSnapshot
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SnapshotChange

fun snapshotOf(facts: DeviceFacts, progress: MountProgress): BlockSnapshot = BlockSnapshot(
    firmwareVersion = facts.firmwareVersion,
    can = facts.can,
    autostart = progress.autostart,
    settingsChanged = progress.settingsChanged,
    changedSettings = progress.changedSettings,
)

/** Статус обхода, который не читали до или после, в сравнение не попадает. */
fun BlockSnapshot.diffTo(after: BlockSnapshot): List<SnapshotChange> = buildList {
    if (firmwareVersion != after.firmwareVersion) add(SnapshotChange.Firmware(firmwareVersion, after.firmwareVersion))
    if (can != after.can) add(SnapshotChange.Can(can, after.can))
    val was = autostart
    val now = after.autostart
    if (was != null && now != null) {
        if (was.bypassLearned != now.bypassLearned) add(SnapshotChange.Bypass(was.bypassLearned, now.bypassLearned))
        if (was.clone != now.clone) add(SnapshotChange.Clone(was.clone, now.clone))
        if (was.cloneAttempts != now.cloneAttempts) {
            add(SnapshotChange.CloneAttempts(was.cloneAttempts, now.cloneAttempts))
        }
    }
    if (settingsChanged != after.settingsChanged) {
        val names = after.changedSettings.drop(changedSettings.size)
        add(SnapshotChange.Settings(settingsChanged, after.settingsChanged, names))
    }
}
