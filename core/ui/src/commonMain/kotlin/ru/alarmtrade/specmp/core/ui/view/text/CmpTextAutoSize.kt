package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private const val AUTO_SIZE_MIN_RATIO = 0.7f
private const val AUTO_SIZE_STEP_SP = 0.5f
private val AutoSizeReadableMin = 11.sp

@Composable
internal fun rememberCmpAutoSize(enabled: Boolean, style: TextStyle, maxLines: Int): TextAutoSize? {
    val size = style.fontSize
    return remember(enabled, size, maxLines) {
        if (!enabled || !size.isSpecified) return@remember null
        val lower = min(size.value, max(size.value * AUTO_SIZE_MIN_RATIO, AutoSizeReadableMin.value))
        if (lower >= size.value) null else CmpOneLineAutoSize(lower, size.value, ellipsizeAtMin = maxLines == 1)
    }
}

/**
 * Наибольший размер из [minSp]..[maxSp], при котором текст встаёт в одну строку. Не встаёт и на минимуме:
 * [ellipsizeAtMin] — минимум (обрежется многоточием), иначе размер стиля с обычным переносом.
 */
@Immutable
internal data class CmpOneLineAutoSize(
    private val minSp: Float,
    private val maxSp: Float,
    private val ellipsizeAtMin: Boolean,
) : TextAutoSize {

    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit {
        if (fitsOneLine(constraints, text, maxSp)) return maxSp.sp
        val steps = ceil((maxSp - minSp) / AUTO_SIZE_STEP_SP).toInt()
        var low = 1
        var high = steps
        var best = -1
        while (low <= high) {
            val mid = (low + high) / 2
            if (fitsOneLine(constraints, text, sizeAt(mid))) {
                best = mid
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return when {
            best > 0 -> sizeAt(best).sp
            ellipsizeAtMin -> minSp.sp
            else -> maxSp.sp
        }
    }

    private fun sizeAt(step: Int): Float = max(maxSp - step * AUTO_SIZE_STEP_SP, minSp)

    private fun TextAutoSizeLayoutScope.fitsOneLine(
        constraints: Constraints,
        text: AnnotatedString,
        sizeSp: Float,
    ): Boolean {
        val layout = performLayout(constraints, text, sizeSp.sp)
        return layout.lineCount <= 1 && !layout.hasVisualOverflow
    }
}
