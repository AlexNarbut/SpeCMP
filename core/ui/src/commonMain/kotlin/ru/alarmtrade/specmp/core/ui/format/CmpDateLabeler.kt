package ru.alarmtrade.specmp.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_months
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_today
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_yesterday
import ru.alarmtrade.specmp.core.ui.time.currentToday

/** Лямбда запомнена — её можно вызывать и вне композиции (remember-блоки списков). */
@Composable
fun rememberCmpDateLabeler(): (LocalDate) -> String {
    val todayLabel = stringResource(Strings.string.common_date_label_today)
    val yesterdayLabel = stringResource(Strings.string.common_date_label_yesterday)
    val monthLabels = stringArrayResource(Strings.array.common_date_label_months)
    val now = currentToday()
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

private fun formatLocalizedDate(date: LocalDate, monthLabels: List<String>, currentYear: Int): String {
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
