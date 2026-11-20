package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_phone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_optional
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_remember
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_address
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_client_name
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_client_phone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_contact
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_installer
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_organization
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_remarks_counter
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_source_profile
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_placeholder_remarks
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.view.input.CmpPhoneField
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportField

private const val REMARKS_COUNTER_FROM = 800
private const val REMARKS_MIN_LINES = 3
private const val REMARKS_MAX_LINES = 8

@Composable
internal fun RemarksBody(remarks: String, onFieldChange: (PassportField, String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CmpTextField(
            value = remarks,
            onValueChange = { onFieldChange(PassportField.REMARKS, it) },
            placeholder = stringResource(Strings.string.mountpassport_editor_placeholder_remarks),
            minLines = REMARKS_MIN_LINES,
            maxLines = REMARKS_MAX_LINES,
            modifier = Modifier.fillMaxWidth(),
        )
        if (remarks.length >= REMARKS_COUNTER_FROM) {
            val full = remarks.length >= MountPassport.MAX_REMARKS_LENGTH
            CmpText(
                text = stringResource(
                    Strings.string.mountpassport_editor_label_remarks_counter,
                    remarks.length,
                    MountPassport.MAX_REMARKS_LENGTH,
                ),
                style = CmpTextStyle.Additional0,
                color = if (full) CmpTheme.colors.errorDefault else CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun ClientBody(state: PassportEditorState, onFieldChange: (PassportField, String) -> Unit) {
    SectionColumn {
        OptionalField(
            label = stringResource(Strings.string.mountpassport_editor_label_client_name),
            value = state.form.clientName,
            onValueChange = { onFieldChange(PassportField.CLIENT_NAME, it) },
        )
        val support = if (state.phoneInvalid) {
            stringResource(Strings.string.mountpassport_editor_error_phone)
        } else {
            stringResource(Strings.string.mountpassport_editor_hint_optional)
        }
        CmpPhoneField(
            digits = state.form.clientPhone,
            onDigitsChange = { onFieldChange(PassportField.CLIENT_PHONE, it) },
            label = stringResource(Strings.string.mountpassport_editor_label_client_phone),
            isError = state.phoneInvalid,
            supportingText = { CmpText(text = support, style = CmpTextStyle.Additional1) },
            modifier = Modifier.fillMaxWidth().testTag(MountPassportTags.FIELD_CUSTOMER_PHONE),
        )
    }
}

/** Исполнитель и контакт — из профиля; организация и адрес запоминаются SDK для следующих паспортов. */
@Composable
internal fun InstallerBody(
    passport: MountPassport,
    state: PassportEditorState,
    onFieldChange: (PassportField, String) -> Unit,
) {
    SectionColumn {
        val installer = passport.installer
        if (installer.name != null || installer.contact != null) {
            AutoCard(source = stringResource(Strings.string.mountpassport_editor_label_source_profile)) {
                installer.name?.let {
                    KeyValue(
                        stringResource(Strings.string.mountpassport_editor_label_installer),
                        it,
                        last = installer.contact == null,
                    )
                }
                installer.contact?.let {
                    KeyValue(stringResource(Strings.string.mountpassport_editor_label_contact), it, last = true)
                }
            }
        }
        val remember = stringResource(Strings.string.mountpassport_editor_hint_remember)
        OptionalField(
            label = stringResource(Strings.string.mountpassport_editor_label_organization),
            value = state.form.organization,
            onValueChange = { onFieldChange(PassportField.ORGANIZATION, it) },
            hint = remember,
        )
        OptionalField(
            label = stringResource(Strings.string.mountpassport_editor_label_address),
            value = state.form.address,
            onValueChange = { onFieldChange(PassportField.ADDRESS, it) },
            hint = remember,
        )
    }
}
