// Сгенерировано scripts/ds-tokens/generate.py из design/design-system/tokens/typography.css — не править вручную.

package ru.alarmtrade.specmp.core.ui.theme.tokens

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Размер, межстрочный и вес стиля шкалы. Вес: `paragraph*` — Medium, остальные — Bold (typography.css). */
internal class CmpTypeSpec(val size: TextUnit, val lineHeight: TextUnit, val weight: FontWeight)

/** Шкала стилей текста — единственные допустимые размеры текста в приложении. */
internal object CmpTypeScale {
    val accent1 = CmpTypeSpec(88.sp, 88.sp, FontWeight.Bold)
    val accent2 = CmpTypeSpec(56.sp, 56.sp, FontWeight.Bold)
    val accent3 = CmpTypeSpec(40.sp, 40.sp, FontWeight.Bold)
    val title1 = CmpTypeSpec(32.sp, 34.sp, FontWeight.Bold)
    val title2 = CmpTypeSpec(28.sp, 30.sp, FontWeight.Bold)
    val headline1 = CmpTypeSpec(24.sp, 26.sp, FontWeight.Bold)
    val headline2 = CmpTypeSpec(19.sp, 22.sp, FontWeight.Bold)
    val headline3 = CmpTypeSpec(16.sp, 18.sp, FontWeight.Bold)
    val paragraph1 = CmpTypeSpec(19.sp, 25.sp, FontWeight.Medium)
    val paragraph2 = CmpTypeSpec(16.sp, 21.sp, FontWeight.Medium)
    val paragraph3 = CmpTypeSpec(14.sp, 18.sp, FontWeight.Medium)
    val additional0 = CmpTypeSpec(14.sp, 17.sp, FontWeight.Bold)
    val additional1 = CmpTypeSpec(12.sp, 15.sp, FontWeight.Bold)
    val additional2 = CmpTypeSpec(10.sp, 12.sp, FontWeight.Bold)
}

/** Роли Material3 → стиль шкалы (алиасы `--display-large-size` → `--t-*` в typography.css). */
internal val CmpM3TypeAliases: Map<String, CmpTypeSpec> = mapOf(
    "displayLarge" to CmpTypeScale.accent2,
    "displayMedium" to CmpTypeScale.accent3,
    "displaySmall" to CmpTypeScale.title1,
    "headlineLarge" to CmpTypeScale.title1,
    "headlineMedium" to CmpTypeScale.title2,
    "headlineSmall" to CmpTypeScale.headline1,
    "titleLarge" to CmpTypeScale.headline1,
    "titleMedium" to CmpTypeScale.headline2,
    "titleSmall" to CmpTypeScale.headline3,
    "bodyLarge" to CmpTypeScale.paragraph1,
    "bodyMedium" to CmpTypeScale.paragraph2,
    "bodySmall" to CmpTypeScale.paragraph3,
    "labelLarge" to CmpTypeScale.headline3,
    "labelMedium" to CmpTypeScale.additional0,
    "labelSmall" to CmpTypeScale.additional1,
)
