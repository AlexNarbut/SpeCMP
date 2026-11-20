package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_parameters
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_can_add
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_check_summary
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_photos_count
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_works_marked
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_check
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_client
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_configuration
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_equipment
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_installer
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_photos
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_remarks
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_vehicle
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_works
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportSection
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.needsReread
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.vehicleTitle

private val HeaderMinHeight = 72.dp
private val IconTile = 44.dp
private const val OPEN_ROTATION = 180f

/** Секция аккордеона: иконка, заголовок, итог одной строкой (или предупреждение), раскрытие по нажатию. */
@Composable
internal fun EditorSection(
    section: PassportSection,
    passport: MountPassport,
    state: PassportEditorState,
    open: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    val warning = section.warning(passport)
    val rotation by animateFloatAsState(if (open) OPEN_ROTATION else 0f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (open) colors.backgroundSurfaceCard else Color.Transparent, shape)
            .border(BorderStroke(1.dp, if (open) colors.borderHighlight else Color.Transparent), shape)
            .testTag(MountPassportTags.section(section.name)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = HeaderMinHeight)
                .cmpPressable(onClick = onToggle, shape = shape)
                .padding(start = CmpSpacing.space8, end = CmpSpacing.space4, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(IconTile)
                    .background(
                        if (warning != null) colors.warningHighlight else colors.backgroundSurfaceHighlight,
                        RoundedCornerShape(CmpRadius.cardSmall),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = if (warning != null) CmpIcons.Warning else section.icon(),
                    contentDescription = null,
                    tint = if (warning != null) colors.warningDefault else colors.foregroundOnDarkPrimaryDefault,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = CmpSpacing.space12)) {
                CmpText(text = section.title(), style = CmpTextStyle.Headline2)
                CmpText(
                    text = warning ?: section.summary(passport, state)
                        ?: stringResource(Strings.string.mountpassport_editor_hint_can_add),
                    style = CmpTextStyle.Paragraph3,
                    color = if (warning != null) colors.warningDefault else colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CmpIcon(
                icon = CmpIcons.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.padding(CmpSpacing.space12).rotate(rotation),
            )
        }
        AnimatedVisibility(visible = open) {
            Box(
                modifier = Modifier.padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space16,
                    bottom = CmpSpacing.space16,
                ),
            ) {
                content()
            }
        }
    }
}

private fun PassportSection.icon(): CmpIcons = when (this) {
    PassportSection.VEHICLE -> CmpIcons.DirectionsCar
    PassportSection.EQUIPMENT -> CmpIcons.Memory
    PassportSection.CONFIGURATION -> CmpIcons.Tune
    PassportSection.WORKS -> CmpIcons.Build
    PassportSection.CHECK -> CmpIcons.FactCheck
    PassportSection.DEVICES -> CmpIcons.Sensors
    PassportSection.PHOTOS -> CmpIcons.PhotoCamera
    PassportSection.REMARKS -> CmpIcons.Description
    PassportSection.CLIENT -> CmpIcons.Person
    PassportSection.INSTALLER -> CmpIcons.AccountCircle
}

@Composable
private fun PassportSection.title(): String = stringResource(
    when (this) {
        PassportSection.VEHICLE -> Strings.string.mountpassport_editor_title_vehicle
        PassportSection.EQUIPMENT -> Strings.string.mountpassport_editor_title_equipment
        PassportSection.CONFIGURATION -> Strings.string.mountpassport_editor_title_configuration
        PassportSection.WORKS -> Strings.string.mountpassport_editor_title_works
        PassportSection.CHECK -> Strings.string.mountpassport_editor_title_check
        PassportSection.DEVICES -> Strings.string.mountpassport_editor_title_devices
        PassportSection.PHOTOS -> Strings.string.mountpassport_editor_title_photos
        PassportSection.REMARKS -> Strings.string.mountpassport_editor_title_remarks
        PassportSection.CLIENT -> Strings.string.mountpassport_editor_title_client
        PassportSection.INSTALLER -> Strings.string.mountpassport_editor_title_installer
    },
)

@Composable
private fun PassportSection.warning(passport: MountPassport): String? {
    val notRead = when (this) {
        PassportSection.EQUIPMENT -> PassportReadStage.PARAMETERS
        PassportSection.CONFIGURATION -> PassportReadStage.SETTINGS
        PassportSection.DEVICES -> PassportReadStage.PERIPHERALS
        else -> null
    }?.takeIf { passport.needsReread(it) } ?: return null
    return stringResource(
        when (notRead) {
            PassportReadStage.SETTINGS -> Strings.string.mountpassport_editor_error_settings
            PassportReadStage.PERIPHERALS -> Strings.string.mountpassport_editor_error_devices
            else -> Strings.string.mountpassport_editor_error_parameters
        },
    )
}

@Composable
private fun PassportSection.summary(passport: MountPassport, state: PassportEditorState): String? {
    val form = state.form
    return when (this) {
        PassportSection.VEHICLE -> listOfNotNull(
            passport.vehicleTitle() ?: form.carTitle.takeIf { it.isNotBlank() },
            form.plate.takeIf { it.isNotBlank() },
        ).joinToString(SEPARATOR).takeIf { it.isNotEmpty() }

        PassportSection.EQUIPMENT -> listOfNotNull(
            passport.unit.systemName,
            "ID ${passport.unit.systemId}",
            passport.unit.firmwareVersion,
        ).joinToString(SEPARATOR)

        PassportSection.CONFIGURATION -> form.configurationName.takeIf { it.isNotBlank() }
            ?: passport.configuration?.name

        PassportSection.WORKS -> worksSummary(passport, state)

        PassportSection.CHECK -> stringResource(
            Strings.string.mountpassport_editor_label_check_summary,
            passport.systemCheck.count { it.result == PassportCheckResult.PASSED },
            passport.systemCheck.size,
        )

        PassportSection.DEVICES -> {
            val count = passport.peripherals.bluetooth.size + passport.peripherals.usb.size
            pluralStringResource(Strings.plurals.mountpassport_editor_label_devices, count, count)
        }

        PassportSection.PHOTOS -> passport.photos.size.takeIf { it > 0 }?.let {
            stringResource(Strings.string.mountpassport_editor_label_photos_count, it, MountPassport.MAX_PHOTOS)
        }

        PassportSection.REMARKS -> form.remarks.takeIf { it.isNotBlank() }?.lineSequence()?.first()

        PassportSection.CLIENT -> listOfNotNull(
            form.clientName.takeIf { it.isNotBlank() },
            form.clientPhone.takeIf { it.isNotEmpty() }?.let(PhoneNumbers::format),
        ).joinToString(SEPARATOR).takeIf { it.isNotEmpty() }

        PassportSection.INSTALLER -> listOfNotNull(
            passport.installer.name,
            form.organization.takeIf { it.isNotBlank() },
        ).joinToString(SEPARATOR).takeIf { it.isNotEmpty() }
    }
}

@Composable
private fun worksSummary(passport: MountPassport, state: PassportEditorState): String? {
    if (state.worksEditable) {
        val works = state.form.works
        val marked = listOf(works.settingsWritten, works.bypass, works.clone, works.slave).count { it }
        return if (marked > 0) stringResource(Strings.string.mountpassport_editor_label_works_marked, marked) else null
    }
    val works = passport.works
    val names = listOfNotNull(CLONE.takeIf { works.clone }, SLAVE.takeIf { works.slave })
    val duration = works.durationMinutes?.let { durationLabel(it) }
    return (names + listOfNotNull(duration)).joinToString(SEPARATOR).takeIf { it.isNotEmpty() }
}

private const val SEPARATOR = " · "
private const val CLONE = "Clone"
private const val SLAVE = "Slave"
