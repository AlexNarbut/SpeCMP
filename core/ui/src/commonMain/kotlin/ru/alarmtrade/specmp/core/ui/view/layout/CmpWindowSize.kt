package ru.alarmtrade.specmp.core.ui.view.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Границы раскладок проекта — M3 Window Size Classes плюс две свои (`design/CLAUDE.md` → «Размеры экранов»).
 * Меряется доступная экрану область (`BoxWithConstraints`), а не всё окно: экран в панели мастера или рядом с
 * колонкой шагов получает свой класс.
 */
object CmpBreakpoints {
    /** Compact — уже: телефон в портрете, раскладной сложен (344×882). */
    val Medium: Dp = 600.dp

    /** Expanded — шире: планшет-ландшафт, desktop; master-detail в две колонки. */
    val Expanded: Dp = 840.dp

    /** Large — шире: три колонки (планшет-ландшафт 1280×800, desktop). */
    val Large: Dp = 1100.dp

    /** Низкое окно — ниже при ширине ≥ [Medium]: телефон-ландшафт (915×412); статус в шапку, без вкладок. */
    val LowHeight: Dp = 520.dp
}

/** Класс ширины. Порядок — от узкого к широкому: `widthClass >= CmpWidthClass.Medium`. */
enum class CmpWidthClass {
    /** < 600: одна колонка, вкладки, нижняя навигация. */
    Compact,

    /** 600–839: раскладной разложен (690×829), планшет-портрет (800×1280), телефон-ландшафт. */
    Medium,

    /** 840–1099: master-detail в две колонки. */
    Expanded,

    /** ≥ 1100: три колонки. */
    Large,
}

/** Размер области экрана и его классы; получать — [cmpWindowSize] в `BoxWithConstraints` или [CmpAdaptiveBox]. */
@Immutable
data class CmpWindowSize(val width: Dp, val height: Dp) {
    val widthClass: CmpWidthClass = when {
        width < CmpBreakpoints.Medium -> CmpWidthClass.Compact
        width < CmpBreakpoints.Expanded -> CmpWidthClass.Medium
        width < CmpBreakpoints.Large -> CmpWidthClass.Expanded
        else -> CmpWidthClass.Large
    }

    /** Телефон-ландшафт: ширины хватает на две панели, высоты — нет. Для Compact не бывает. */
    val isLowHeight: Boolean = height < CmpBreakpoints.LowHeight && widthClass != CmpWidthClass.Compact

    val isCompact: Boolean get() = widthClass == CmpWidthClass.Compact

    fun atLeast(widthClass: CmpWidthClass): Boolean = this.widthClass >= widthClass
}

/** Размер области из `BoxWithConstraints`. */
val BoxWithConstraintsScope.cmpWindowSize: CmpWindowSize
    get() = CmpWindowSize(maxWidth, maxHeight)

/** `BoxWithConstraints`, который сразу отдаёт размер области с классами — основа адаптивного экрана. */
@Composable
fun CmpAdaptiveBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxWithConstraintsScope.(CmpWindowSize) -> Unit,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = contentAlignment) {
        content(cmpWindowSize)
    }
}
