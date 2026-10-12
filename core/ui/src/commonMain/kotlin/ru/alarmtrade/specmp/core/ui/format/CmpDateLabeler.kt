package ru.alarmtrade.specmp.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import specmp.core.ui.generated.resources.Res
import specmp.core.ui.generated.resources.coreui_common_date_today
import specmp.core.ui.generated.resources.coreui_common_date_yesterday
import specmp.core.ui.generated.resources.coreui_common_month_april
import specmp.core.ui.generated.resources.coreui_common_month_august
import specmp.core.ui.generated.resources.coreui_common_month_december
import specmp.core.ui.generated.resources.coreui_common_month_february
import specmp.core.ui.generated.resources.coreui_common_month_january
import specmp.core.ui.generated.resources.coreui_common_month_july
import specmp.core.ui.generated.resources.coreui_common_month_june
import specmp.core.ui.generated.resources.coreui_common_month_march
import specmp.core.ui.generated.resources.coreui_common_month_may
import specmp.core.ui.generated.resources.coreui_common_month_november
import specmp.core.ui.generated.resources.coreui_common_month_october
import specmp.core.ui.generated.resources.coreui_common_month_september

/**
 * Общий лейблер дат (U-26): «Сегодня»/«Вчера»/«14 марта», год добавляется,
 * если он не текущий. Месяцы и примитивные даты — общие строки core/ui
 * (правило 2 docs/localization.md), live-смена языка инвалидирует лямбду через
 * recomposition (LocalAppLocale).
 *
 * Возвращает запомненную лямбду, чтобы фичи вызывали её из некомпозабельных
 * контекстов (например, из remember-блоков построения списков).
 */
@Composable
fun rememberCmpDateLabeler(): (LocalDate) -> String {
    val todayLabel = stringResource(Res.string.coreui_common_date_today)
    val yesterdayLabel = stringResource(Res.string.coreui_common_date_yesterday)
    val monthLabels = listOf(
        stringResource(Res.string.coreui_common_month_january),
        stringResource(Res.string.coreui_common_month_february),
        stringResource(Res.string.coreui_common_month_march),
        stringResource(Res.string.coreui_common_month_april),
        stringResource(Res.string.coreui_common_month_may),
        stringResource(Res.string.coreui_common_month_june),
        stringResource(Res.string.coreui_common_month_july),
        stringResource(Res.string.coreui_common_month_august),
        stringResource(Res.string.coreui_common_month_september),
        stringResource(Res.string.coreui_common_month_october),
        stringResource(Res.string.coreui_common_month_november),
        stringResource(Res.string.coreui_common_month_december),
    )
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = now.minus(DatePeriod(days = 1))

    return remember(todayLabel, yesterdayLabel, monthLabels, now) {
        { date ->
            when (date) {
                now -> todayLabel
                yesterday -> yesterdayLabel
                else -> formatLocalizedDate(date, monthLabels, now.year)
            }
        }
    }
}

private fun formatLocalizedDate(
    date: LocalDate,
    monthLabels: List<String>,
    currentYear: Int,
): String {
    val month = monthLabels.getOrNull(date.monthNumber - 1).orEmpty()
    return buildString {
        append(date.dayOfMonth)
        if (month.isNotBlank()) {
            append(' ')
            append(month)
        }
        if (date.year != currentYear) {
            append(' ')
            append(date.year)
        }
    }
}
