package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_btn_done
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_btn_send
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_btn_to_list
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_error_issue
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_error_name
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_error_phone
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_format_counter
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_hint_issue
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_hint_keyboard
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_hint_keyboard_esc
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_hint_phone
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_label_attach_device
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_label_attach_language
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_label_attach_settings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_label_issue
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_label_name
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_label_phone
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_loading_reading_device
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_loading_sending
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_msg_attach_note
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_msg_cancel_note
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_msg_origin
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_msg_sent
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_placeholder_issue
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_subtitle_callback
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_title_attach
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_title_screen
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_title_sent
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_value_attach_device
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_value_attach_settings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_value_attach_summary
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_value_language_en
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_value_language_ru
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpPhoneField
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestField
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestFormActions
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsTags
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.icon
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.submitText
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.AttachedDevice
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.SubmitStage
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestForm
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestSending

private const val COUNTER_FROM = 1700

@Composable
internal fun TechRequestFormPane(
    kind: FormKind,
    form: TechRequestForm,
    sending: TechRequestSending,
    device: AttachedDevice?,
    language: AppLocale,
    keyboardHints: Boolean,
    actions: TechRequestFormActions,
    modifier: Modifier = Modifier,
    origin: String? = null,
) {
    val colors = cmpThemeColors()
    val inProgress = sending is TechRequestSending.InProgress
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.Enter && (event.isCtrlPressed || event.isMetaPressed) -> {
                        val idle = sending is TechRequestSending.Idle || sending is TechRequestSending.Failed
                        if (idle) actions.onSubmit()
                        true
                    }

                    event.key == Key.Escape && kind != FormKind.Panel -> {
                        actions.onClose()
                        true
                    }

                    else -> false
                }
            },
    ) {
        FormHeader(kind, actions.onClose)
        if (sending is TechRequestSending.Sent) {
            SentContent(sending, actions.onSentDone, Modifier.weight(1f))
            return@Column
        }
        val horizontal = if (kind == FormKind.Page) 16.dp else 24.dp
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontal, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            origin?.let { OriginNote(it) }
            FormFields(form, enabled = !inProgress, actions = actions)
            AttachBlock(device, language, initiallyOpen = kind == FormKind.Panel)
        }
        FormBottom(sending, keyboardHints, kind, actions, horizontal)
    }
}

@Composable
private fun FormHeader(kind: FormKind, onClose: () -> Unit) {
    val colors = cmpThemeColors()
    val close = stringResource(Strings.string.techrequests_common_a11y_close)
    if (kind == FormKind.Page) {
        CmpTopAppBar(
            title = {
                CmpText(
                    text = stringResource(Strings.string.techrequests_form_title_screen),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            navigationIcon = { CmpIconButton(onClick = onClose, icon = CmpIcons.Close, contentDescription = close) },
        )
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(
                text = stringResource(Strings.string.techrequests_form_title_screen),
                style = CmpTextStyle.Headline1,
            )
            CmpText(
                text = stringResource(Strings.string.techrequests_form_subtitle_callback),
                style = CmpTextStyle.Paragraph2,
                color = colors.onSurfaceVariant,
            )
        }
        if (kind == FormKind.Dialog) {
            CmpIconButton(onClick = onClose, icon = CmpIcons.Close, contentDescription = close)
        }
    }
}

@Composable
private fun OriginNote(origin: String) {
    val colors = cmpThemeColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(icon = CmpIcons.Link, contentDescription = null, tint = colors.onSecondaryContainer)
        CmpText(
            text = stringResource(Strings.string.techrequests_form_msg_origin, origin),
            style = CmpTextStyle.Paragraph2,
            color = colors.onSecondaryContainer,
        )
    }
}

@Composable
private fun FormFields(form: TechRequestForm, enabled: Boolean, actions: TechRequestFormActions) {
    val errors = form.errors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        CmpTextField(
            value = form.name,
            onValueChange = actions.onNameChange,
            label = stringResource(Strings.string.techrequests_form_label_name),
            enabled = enabled,
            isError = TechRequestField.Name in errors,
            singleLine = true,
            leadingIcon = { CmpIcon(icon = CmpIcons.Person, contentDescription = null) },
            supportingText = fieldError(TechRequestField.Name in errors, Strings.string.techrequests_form_error_name),
            modifier = Modifier.fillMaxWidth().testTag(TechRequestsTags.FORM_NAME),
        )
        CmpPhoneField(
            digits = form.phoneDigits,
            onDigitsChange = actions.onPhoneChange,
            label = stringResource(Strings.string.techrequests_form_label_phone),
            placeholder = stringResource(Strings.string.techrequests_form_hint_phone),
            enabled = enabled,
            isError = TechRequestField.Phone in errors,
            supportingText = fieldError(TechRequestField.Phone in errors, Strings.string.techrequests_form_error_phone),
            modifier = Modifier.fillMaxWidth().testTag(TechRequestsTags.FORM_PHONE),
        )
        IssueField(form.issue, TechRequestField.Issue in errors, enabled, actions.onIssueChange)
    }
}

@Composable
private fun fieldError(
    visible: Boolean,
    message: org.jetbrains.compose.resources.StringResource,
): (@Composable () -> Unit)? = if (visible) {
    { CmpText(text = stringResource(message), style = CmpTextStyle.Paragraph2) }
} else {
    null
}

@Composable
private fun IssueField(value: String, isError: Boolean, enabled: Boolean, onChange: (String) -> Unit) {
    val colors = cmpThemeColors()
    val length = value.length
    CmpTextField(
        value = value,
        onValueChange = onChange,
        label = stringResource(Strings.string.techrequests_form_label_issue),
        placeholder = stringResource(Strings.string.techrequests_form_placeholder_issue),
        enabled = enabled,
        isError = isError,
        minLines = 5,
        maxLines = 12,
        supportingText = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CmpText(
                    text = stringResource(
                        if (isError) {
                            Strings.string.techrequests_form_error_issue
                        } else {
                            Strings.string.techrequests_form_hint_issue
                        },
                    ),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.weight(1f),
                )
                if (length >= COUNTER_FROM) {
                    CmpText(
                        text = stringResource(
                            Strings.string.techrequests_form_format_counter,
                            length,
                            TechRequestDraft.ISSUE_MAX_LENGTH,
                        ),
                        style = CmpTextStyle.Headline3,
                        color = if (length >= TechRequestDraft.ISSUE_MAX_LENGTH) {
                            colors.error
                        } else {
                            colors.onSurfaceVariant
                        },
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth().testTag(TechRequestsTags.FORM_ISSUE),
    )
}

/** Что уходит вместе с заявкой; отключить нельзя. Координаты SDK отправляет, но здесь не упоминаются (макет). */
@Composable
private fun AttachBlock(device: AttachedDevice?, language: AppLocale, initiallyOpen: Boolean) {
    val colors = cmpThemeColors()
    var open by rememberSaveable { mutableStateOf(initiallyOpen) }
    val shape = RoundedCornerShape(16.dp)
    val model = device?.model.orEmpty()
    val firmware = device?.firmware.orEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, shape)
            .border(1.dp, colors.outlineVariant, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable { open = !open }
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CmpIcon(icon = CmpIcons.Outbox, contentDescription = null, tint = colors.primary)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = stringResource(Strings.string.techrequests_form_title_attach),
                    style = CmpTextStyle.Headline3,
                )
                if (!open) {
                    CmpText(
                        text = stringResource(Strings.string.techrequests_form_value_attach_summary, model, firmware),
                        style = CmpTextStyle.Paragraph2,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            CmpIcon(
                icon = if (open) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = open) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AttachRow(
                    CmpIcons.Memory,
                    stringResource(Strings.string.techrequests_form_label_attach_device),
                    stringResource(Strings.string.techrequests_form_value_attach_device, model, firmware),
                )
                AttachRow(
                    CmpIcons.Tune,
                    stringResource(Strings.string.techrequests_form_label_attach_settings),
                    stringResource(Strings.string.techrequests_form_value_attach_settings),
                )
                AttachRow(
                    CmpIcons.Language,
                    stringResource(Strings.string.techrequests_form_label_attach_language),
                    stringResource(
                        when (language) {
                            AppLocale.Russian -> Strings.string.techrequests_form_value_language_ru
                            AppLocale.English -> Strings.string.techrequests_form_value_language_en
                        },
                    ),
                )
                CmpText(
                    text = stringResource(Strings.string.techrequests_form_msg_attach_note),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AttachRow(icon: CmpIcons, label: String, value: String) {
    val colors = cmpThemeColors()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.onSurfaceVariant)
        Column {
            CmpText(text = label, style = CmpTextStyle.Headline3)
            CmpText(text = value, style = CmpTextStyle.Paragraph2, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun FormBottom(
    sending: TechRequestSending,
    keyboardHints: Boolean,
    kind: FormKind,
    actions: TechRequestFormActions,
    horizontal: androidx.compose.ui.unit.Dp,
) {
    val colors = cmpThemeColors()
    Box(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
        Box(Modifier.fillMaxWidth().heightIn(min = 1.dp).background(colors.outlineVariant))
        if (sending is TechRequestSending.InProgress) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Column(
            modifier = Modifier.padding(horizontal = horizontal, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val failure = (sending as? TechRequestSending.Failed)?.error
            if (failure != null) {
                val onAction = if (failure == TechRequestsError.Limit) actions.onToList else actions.onSubmit
                ErrorBanner(failure, onAction = onAction)
            }
            when {
                sending is TechRequestSending.InProgress -> SendingStages(sending.stage, actions.onCancelSending)

                failure != TechRequestsError.Limit -> CmpButton(
                    onClick = actions.onSubmit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(TechRequestsTags.FORM_SUBMIT),
                ) {
                    CmpIcon(icon = CmpIcons.Send, contentDescription = null)
                    CmpText(
                        text = stringResource(Strings.string.techrequests_form_btn_send),
                        style = CmpTextStyle.Paragraph2,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            if (keyboardHints && sending !is TechRequestSending.InProgress) {
                CmpText(
                    text = stringResource(
                        if (kind == FormKind.Panel) {
                            Strings.string.techrequests_form_hint_keyboard
                        } else {
                            Strings.string.techrequests_form_hint_keyboard_esc
                        },
                    ),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(error: TechRequestsError, onAction: () -> Unit) {
    val colors = cmpThemeColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.errorContainer, RoundedCornerShape(16.dp))
            .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpIcon(icon = error.icon, contentDescription = null, tint = colors.onErrorContainer)
            CmpText(
                text = error.submitText(),
                style = CmpTextStyle.Paragraph1,
                color = colors.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
        }
        CmpTextButton(onClick = onAction, modifier = Modifier.align(Alignment.End).heightIn(min = 48.dp)) {
            CmpIcon(
                icon = if (error == TechRequestsError.Limit) CmpIcons.List else CmpIcons.Refresh,
                contentDescription = null,
                tint = colors.onErrorContainer,
            )
            CmpText(
                text = stringResource(
                    if (error == TechRequestsError.Limit) {
                        Strings.string.techrequests_form_btn_to_list
                    } else {
                        Strings.string.techrequests_common_btn_retry
                    },
                ),
                style = CmpTextStyle.Paragraph2,
                color = colors.onErrorContainer,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun SendingStages(stage: SubmitStage, onCancel: () -> Unit) {
    val colors = cmpThemeColors()
    val domain = cmpDomainColors()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SubmitStage.entries.forEach { item ->
            Row(
                modifier = Modifier.heightIn(min = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    when {
                        item.ordinal < stage.ordinal ->
                            CmpIcon(icon = CmpIcons.CheckCircle, contentDescription = null, tint = domain.success)

                        item == stage ->
                            CmpCircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 3.dp)

                        else ->
                            CmpIcon(
                                icon = CmpIcons.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = colors.outline,
                            )
                    }
                }
                CmpText(
                    text = stringResource(
                        when (item) {
                            SubmitStage.ReadingDevice -> Strings.string.techrequests_form_loading_reading_device
                            SubmitStage.Sending -> Strings.string.techrequests_form_loading_sending
                        },
                    ),
                    style = if (item == stage) CmpTextStyle.Headline2 else CmpTextStyle.Paragraph1,
                    color = if (item.ordinal > stage.ordinal) colors.onSurfaceVariant else colors.onSurface,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpText(
                text = stringResource(Strings.string.techrequests_form_msg_cancel_note),
                style = CmpTextStyle.Paragraph3,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            CmpOutlinedButton(
                text = stringResource(Strings.string.techrequests_form_btn_cancel),
                onClick = onCancel,
                modifier = Modifier.heightIn(min = 48.dp).testTag(TechRequestsTags.FORM_CANCEL),
            )
        }
    }
}

@Composable
private fun SentContent(sent: TechRequestSending.Sent, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    val domain = cmpDomainColors()
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier.size(104.dp).background(domain.success.copy(alpha = SUCCESS_TINT), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = domain.success,
            )
        }
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpText(
                text = stringResource(Strings.string.techrequests_form_title_sent, sent.id.toString()),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(
                    Strings.string.techrequests_form_msg_sent,
                    PhoneNumbers.format(sent.phoneDigits).unbreakable(),
                ),
                style = CmpTextStyle.Paragraph1,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        CmpButton(
            text = stringResource(Strings.string.techrequests_form_btn_done),
            onClick = onDone,
            modifier = Modifier.widthIn(min = 200.dp).heightIn(min = 56.dp).testTag(TechRequestsTags.SENT_OK),
        )
    }
}

private const val SUCCESS_TINT = 0.24f
private const val NO_BREAK_SPACE = '\u00A0'
private const val NO_BREAK_HYPHEN = '\u2011'

/** Номер переносится целиком: пробелы и дефисы маски — неразрывные. */
private fun String.unbreakable(): String = replace(' ', NO_BREAK_SPACE).replace('-', NO_BREAK_HYPHEN)
