package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_reread
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_a11y_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_format_device_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_format_device_slot
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_format_hours_minutes
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_format_minutes
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_item_save_configurations
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_item_save_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_author
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_bypass
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_clone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_configuration_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_duration
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_settings_written
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_slave
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_source_master
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_source_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_check_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_check_passed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_check_skipped
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_device_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_device_online
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_device_paired
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_factory
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_missing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_no_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_not_read
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_saved_factory
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_unavailable
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_value_bypass_clone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_value_bypass_none
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_value_bypass_trained
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_value_clone_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_value_slave_done
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportBypass
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.numericDateTime
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.timeLabel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportField
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportWork
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.SettingsSnapshotStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.configurationFromMaster
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.needsReread
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.settingsStatus

private const val MINUTES_IN_HOUR = 60

/** Снимок настроек: в документ не попадает; меню — файл и «Мои конфигурации». */
@Composable
internal fun ConfigurationBody(
    passport: MountPassport,
    state: PassportEditorState,
    onFieldChange: (PassportField, String) -> Unit,
    onReread: (PassportReadStage) -> Unit,
    onSaveFile: () -> Unit,
    onSaveConfigurations: () -> Unit,
) {
    SectionColumn {
        val configuration = passport.configuration
        if (passport.configurationFromMaster() && configuration != null) {
            AutoCard(source = stringResource(Strings.string.mountpassport_editor_label_source_master)) {
                KeyValue(
                    stringResource(Strings.string.mountpassport_editor_label_name),
                    configuration.name.orEmpty(),
                    last = configuration.author == null,
                )
                configuration.author?.let {
                    KeyValue(stringResource(Strings.string.mountpassport_editor_label_author), it, last = true)
                }
            }
        } else {
            OptionalField(
                label = stringResource(Strings.string.mountpassport_editor_label_configuration_name),
                value = state.form.configurationName,
                onValueChange = { onFieldChange(PassportField.CONFIGURATION, it) },
            )
        }
        SettingsCard(passport, state.rereadVisible, state.rereading, onReread, onSaveFile, onSaveConfigurations)
    }
}

@Composable
private fun SettingsCard(
    passport: MountPassport,
    rereadVisible: Boolean,
    rereading: PassportReadStage?,
    onReread: (PassportReadStage) -> Unit,
    onSaveFile: () -> Unit,
    onSaveConfigurations: () -> Unit,
) {
    val colors = CmpTheme.colors
    val status = passport.settingsStatus()
    CmpCard {
        Column(
            modifier = Modifier.padding(start = CmpSpacing.space16, end = CmpSpacing.space4, top = CmpSpacing.space8),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CmpIcon(icon = CmpIcons.Tune, contentDescription = null, tint = colors.foregroundOnDarkSecondaryDefault)
                Column(modifier = Modifier.weight(1f).padding(horizontal = CmpSpacing.space12)) {
                    CmpText(
                        text = stringResource(Strings.string.mountpassport_editor_title_settings),
                        style = CmpTextStyle.Headline3,
                    )
                    CmpText(
                        text = settingsStatusText(passport, status),
                        style = CmpTextStyle.Paragraph3,
                        color = if (status == SettingsSnapshotStatus.NOT_READ) {
                            colors.warningDefault
                        } else {
                            colors.foregroundOnDarkSecondaryDefault
                        },
                    )
                }
                when {
                    status == SettingsSnapshotStatus.SAVED -> SettingsMenu(onSaveFile, onSaveConfigurations)

                    status == SettingsSnapshotStatus.NOT_READ && rereadVisible -> CmpButton(
                        text = stringResource(Strings.string.mountpassport_common_btn_reread),
                        onClick = { onReread(PassportReadStage.SETTINGS) },
                        type = CmpButtonType.Tertiary,
                        size = CmpButtonSize.S,
                        icon = CmpIcons.Refresh,
                        uppercase = false,
                        enabled = rereading == null,
                        loading = rereading == PassportReadStage.SETTINGS,
                    )
                }
            }
            Note(
                icon = CmpIcons.VisibilityOff,
                text = stringResource(Strings.string.mountpassport_editor_hint_settings),
                modifier = Modifier.padding(vertical = CmpSpacing.space12),
            )
        }
    }
}

@Composable
private fun settingsStatusText(passport: MountPassport, status: SettingsSnapshotStatus): String {
    val snapshot = passport.settingsSnapshot
    return when (status) {
        SettingsSnapshotStatus.SAVED -> {
            val at = requireNotNull(snapshot).takenAt.numericDateTime()
            if (snapshot.canFactory) {
                stringResource(Strings.string.mountpassport_editor_status_settings_saved_factory, at)
            } else {
                stringResource(Strings.string.mountpassport_editor_status_settings_saved, at)
            }
        }

        SettingsSnapshotStatus.NOT_READ -> stringResource(Strings.string.mountpassport_editor_status_settings_not_read)

        SettingsSnapshotStatus.FACTORY_CAN -> stringResource(
            Strings.string.mountpassport_editor_status_settings_factory,
        )

        SettingsSnapshotStatus.UNAVAILABLE ->
            stringResource(Strings.string.mountpassport_editor_status_settings_unavailable)

        SettingsSnapshotStatus.NO_UNIT -> stringResource(Strings.string.mountpassport_editor_status_settings_no_unit)

        SettingsSnapshotStatus.MISSING -> stringResource(Strings.string.mountpassport_editor_status_settings_missing)
    }
}

@Composable
private fun SettingsMenu(onSaveFile: () -> Unit, onSaveConfigurations: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        CmpIconButton(
            onClick = { expanded = true },
            icon = CmpIcons.MoreVert,
            contentDescription = stringResource(Strings.string.mountpassport_editor_a11y_settings),
        )
        CmpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CmpDropdownMenuItem(
                text = stringResource(Strings.string.mountpassport_editor_item_save_file),
                icon = CmpIcons.Save,
                onClick = {
                    expanded = false
                    onSaveFile()
                },
            )
            CmpDropdownMenuItem(
                text = stringResource(Strings.string.mountpassport_editor_item_save_configurations),
                icon = CmpIcons.FolderOpen,
                onClick = {
                    expanded = false
                    onSaveConfigurations()
                },
            )
        }
    }
}

/** Мастер быстрого монтажа — только чтение; иначе работы отмечаются галочками. */
@Composable
internal fun WorksBody(
    passport: MountPassport,
    state: PassportEditorState,
    onWorkToggle: (PassportWork, Boolean) -> Unit,
) {
    if (state.worksEditable) {
        val works = state.form.works
        Column {
            WorkCheck(Strings.string.mountpassport_editor_label_settings_written, works.settingsWritten) {
                onWorkToggle(PassportWork.SETTINGS_WRITTEN, it)
            }
            WorkCheck(Strings.string.mountpassport_editor_label_bypass, works.bypass) {
                onWorkToggle(PassportWork.BYPASS, it)
            }
            WorkCheck(Strings.string.mountpassport_editor_label_clone, works.clone) {
                onWorkToggle(PassportWork.CLONE, it)
            }
            WorkCheck(Strings.string.mountpassport_editor_label_slave, works.slave) {
                onWorkToggle(PassportWork.SLAVE, it)
            }
        }
    } else {
        AutoCard(source = stringResource(Strings.string.mountpassport_editor_label_source_master)) {
            MasterWorks(passport)
        }
    }
}

@Composable
private fun WorkCheck(label: StringResource, checked: Boolean, onChange: (Boolean) -> Unit) {
    SourceRow {
        CmpCheckbox(checked = checked, onCheckedChange = onChange)
        CmpText(text = stringResource(label), style = CmpTextStyle.Headline3, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MasterWorks(passport: MountPassport) {
    val works = passport.works
    val rows = buildList {
        if (works.settingsWritten) {
            add(stringResource(Strings.string.mountpassport_editor_label_settings_written) to null)
        }
        works.bypass?.let { bypass ->
            val value = when (bypass) {
                PassportBypass.TRAINED -> Strings.string.mountpassport_editor_value_bypass_trained
                PassportBypass.CLONE_NEEDED -> Strings.string.mountpassport_editor_value_bypass_clone
                PassportBypass.NONE -> Strings.string.mountpassport_editor_value_bypass_none
            }
            add(stringResource(Strings.string.mountpassport_editor_label_bypass) to stringResource(value))
        }
        if (works.clone) {
            add(
                stringResource(Strings.string.mountpassport_editor_label_clone) to
                    stringResource(Strings.string.mountpassport_editor_value_clone_done),
            )
        }
        if (works.slave) {
            add(
                stringResource(Strings.string.mountpassport_editor_label_slave) to
                    stringResource(Strings.string.mountpassport_editor_value_slave_done),
            )
        }
    }
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    rows.forEach { (label, value) ->
        SourceRow {
            CmpIcon(icon = CmpIcons.Check, contentDescription = null, tint = CmpTheme.colors.successDefault)
            CmpText(text = label, style = CmpTextStyle.Headline3, modifier = Modifier.weight(1f))
            value?.let { CmpText(text = it, style = CmpTextStyle.Paragraph2, color = muted) }
        }
        CmpDivider()
    }
    works.durationMinutes?.let { minutes ->
        SourceRow {
            CmpIcon(icon = CmpIcons.Schedule, contentDescription = null, tint = muted)
            CmpText(
                text = stringResource(Strings.string.mountpassport_editor_label_duration),
                style = CmpTextStyle.Headline3,
                modifier = Modifier.weight(1f),
            )
            CmpText(text = durationLabel(minutes), style = CmpTextStyle.Paragraph2, color = muted)
        }
    }
}

@Composable
internal fun durationLabel(minutes: Int): String = if (minutes >= MINUTES_IN_HOUR) {
    stringResource(
        Strings.string.mountpassport_editor_format_hours_minutes,
        minutes / MINUTES_IN_HOUR,
        minutes % MINUTES_IN_HOUR,
    )
} else {
    stringResource(Strings.string.mountpassport_editor_format_minutes, minutes)
}

@Composable
internal fun CheckBody(passport: MountPassport) {
    val colors = CmpTheme.colors
    Column {
        passport.systemCheck.forEachIndexed { index, task ->
            val (icon, tint, word) = when (task.result) {
                PassportCheckResult.PASSED ->
                    Triple(
                        CmpIcons.CheckCircle,
                        colors.successDefault,
                        Strings.string.mountpassport_editor_status_check_passed,
                    )

                PassportCheckResult.FAILED ->
                    Triple(
                        CmpIcons.Cancel,
                        colors.errorDefault,
                        Strings.string.mountpassport_editor_status_check_failed,
                    )

                PassportCheckResult.SKIPPED -> Triple(
                    CmpIcons.Block,
                    colors.foregroundOnDarkSecondaryDefault,
                    Strings.string.mountpassport_editor_status_check_skipped,
                )
            }
            SourceRow {
                CmpIcon(icon = icon, contentDescription = null, tint = tint)
                CmpText(text = task.title, style = CmpTextStyle.Paragraph2, modifier = Modifier.weight(1f))
                CmpText(
                    text = stringResource(word),
                    style = CmpTextStyle.Additional0,
                    color = if (task.result ==
                        PassportCheckResult.FAILED
                    ) {
                        tint
                    } else {
                        colors.foregroundOnDarkSecondaryDefault
                    },
                )
            }
            if (index < passport.systemCheck.lastIndex) CmpDivider()
        }
    }
}

@Composable
internal fun DevicesBody(
    passport: MountPassport,
    rereadVisible: Boolean,
    rereading: PassportReadStage?,
    onReread: (PassportReadStage) -> Unit,
) {
    if (passport.needsReread(PassportReadStage.PERIPHERALS)) {
        RereadRow(
            text = stringResource(Strings.string.mountpassport_editor_error_devices),
            stage = PassportReadStage.PERIPHERALS,
            rereadVisible = rereadVisible,
            onReread = onReread,
            rereading = rereading,
        )
        return
    }
    val online = stringResource(Strings.string.mountpassport_editor_status_device_online)
    val offline = stringResource(Strings.string.mountpassport_editor_status_device_offline)
    val paired = stringResource(Strings.string.mountpassport_editor_status_device_paired)
    AutoCard(
        source = stringResource(Strings.string.mountpassport_editor_label_source_unit, passport.createdAt.timeLabel()),
    ) {
        passport.peripherals.bluetooth.forEach { device ->
            val number = device.number?.let {
                stringResource(Strings.string.mountpassport_editor_format_device_number, it)
            }
            val details = listOfNotNull(device.address, device.online?.let { if (it) online else offline })
            DeviceRow(listOfNotNull(device.title ?: device.type, number).joinToString(" "), details, mono = true)
        }
        passport.peripherals.usb.forEach { device ->
            val slot = device.slot?.let { stringResource(Strings.string.mountpassport_editor_format_device_slot, it) }
            val details = listOfNotNull(
                slot,
                paired.takeIf { device.paired == true },
                device.online?.let { if (it) online else offline },
            )
            DeviceRow(device.name ?: device.type, details, mono = false)
        }
    }
}

@Composable
private fun DeviceRow(title: String, details: List<String>, mono: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = CmpSpacing.space8)) {
        CmpText(text = title, style = CmpTextStyle.Headline3)
        if (details.isNotEmpty()) {
            CmpText(
                text = details.joinToString(" · "),
                style = if (mono) CmpTextStyle.Mono2 else CmpTextStyle.Additional1,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
    CmpDivider()
}
