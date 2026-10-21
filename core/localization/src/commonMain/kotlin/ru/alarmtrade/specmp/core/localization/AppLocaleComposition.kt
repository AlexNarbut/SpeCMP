package ru.alarmtrade.specmp.core.localization

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-level локаль Compose-дерева. Провайдится один раз в composition root
 * (`App()`), фичи читают через `LocalAppLocale.current`.
 *
 * Именно static local: смена значения принудительно рекомпозирует всё поддерево
 * без его пересоздания, поэтому `stringResource` во всех модулях перечитывает
 * уже применённую через [applyAppLocale] платформенную локаль, а `remember` /
 * back stack экранов не сбрасываются (в отличие от `key(locale)`).
 */
val LocalAppLocale: ProvidableCompositionLocal<AppLocale> =
    staticCompositionLocalOf { currentAppLocale() }
