package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_error_unknown
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_format_date_time
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_format_megabytes
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_status_blank
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_status_draft
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_status_issued
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_photo
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_photo_limit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_photo_storage
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.error.messageText
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus

@Composable
internal fun PassportStatus.label(): String = stringResource(
    when (this) {
        PassportStatus.BLANK -> Strings.string.mountpassport_common_status_blank
        PassportStatus.DRAFT -> Strings.string.mountpassport_common_status_draft
        PassportStatus.ISSUED -> Strings.string.mountpassport_common_status_issued
    },
)

/** «Сегодня, 14:05» / «18 ноября, 17:20». */
@Composable
internal fun Instant.dateTimeLabel(): String {
    val local = toLocalDateTime(currentTimeZone())
    val date = rememberCmpDateLabeler()(local.date)
    return stringResource(Strings.string.mountpassport_common_format_date_time, date, timeLabel())
}

/** «14:05». */
@Composable
internal fun Instant.timeLabel(): String {
    val local = toLocalDateTime(currentTimeZone())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}

/** «19.11.2026». */
@Composable
internal fun Instant.numericDate(): String {
    val local = toLocalDateTime(currentTimeZone())
    val day = local.dayOfMonth.toString().padStart(2, '0')
    val month = local.monthNumber.toString().padStart(2, '0')
    return "$day.$month.${local.year}"
}

/** «19.11.2026 14:05». */
@Composable
internal fun Instant.numericDateTime(): String = "${numericDate()} ${timeLabel()}"

/** Объём с одним знаком после запятой в мегабайтах (минимум 0,1). */
@Composable
internal fun megabytesLabel(bytes: Long): String {
    val tenths = ((bytes + BYTES_IN_TENTH_MB - 1) / BYTES_IN_TENTH_MB).coerceAtLeast(1)
    val whole = tenths / TENTHS
    val fraction = tenths % TENTHS
    val number = if (fraction == 0L || whole >= WHOLE_FROM) "$whole" else "$whole,$fraction"
    return stringResource(Strings.string.mountpassport_common_format_megabytes, number)
}

@Composable
internal fun PassportError.text(): String = when (this) {
    is PassportError.Common -> error.message()
    PassportError.PhotoLimit -> stringResource(Strings.string.mountpassport_editor_error_photo_limit)
    PassportError.StorageFull -> stringResource(Strings.string.mountpassport_editor_error_photo_storage)
    PassportError.Unknown -> stringResource(Strings.string.mountpassport_common_error_unknown)
}

@Composable
internal fun PassportError.photoText(): String =
    if (this == PassportError.Unknown) stringResource(Strings.string.mountpassport_editor_error_photo) else text()

/** Текст с выделенным совпадением поиска. */
@Composable
internal fun highlighted(text: String, query: String): AnnotatedString {
    val range = highlightRange(text, query) ?: return AnnotatedString(text)
    val accent = SpanStyle(background = CmpTheme.colors.brandPrimaryHighlight, fontWeight = FontWeight.Bold)
    return buildAnnotatedString {
        append(text.substring(0, range.first))
        withStyle(accent) { append(text.substring(range.first, range.last + 1)) }
        append(text.substring(range.last + 1))
    }
}

private const val BYTES_IN_TENTH_MB = 100_000L
private const val WHOLE_FROM = 10L
private const val TENTHS = 10L

/** Текст ошибки вне композиции (снекбар). */
internal suspend fun PassportError.errorMessage(): String = when (this) {
    is PassportError.Common -> error.messageText()
    PassportError.PhotoLimit -> getString(Strings.string.mountpassport_editor_error_photo_limit)
    PassportError.StorageFull -> getString(Strings.string.mountpassport_editor_error_photo_storage)
    PassportError.Unknown -> getString(Strings.string.mountpassport_common_error_unknown)
}
