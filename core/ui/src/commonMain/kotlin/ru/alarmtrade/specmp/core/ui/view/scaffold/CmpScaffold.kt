package ru.alarmtrade.specmp.core.ui.view.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.effect.CmpBlurEdge
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBlurSource
import ru.alarmtrade.specmp.core.ui.view.effect.cmpEdgeBlur
import ru.alarmtrade.specmp.core.ui.view.effect.cmpEdgeGradient
import ru.alarmtrade.specmp.core.ui.view.effect.rememberCmpBlurState

/** Дистанция прокрутки, за которую блюр под топбаром нарастает от 0 до полного. */
private val TopBlurRamp = 80.dp

/**
 * Каркас экрана. Топбар прозрачный: когда контент уезжает под него (контент рисуется под топбаром через
 * `contentPadding` списка), появляется лёгкий прогрессивный блюр и градиент цвета страницы — нарастает за [TopBlurRamp]
 * прокрутки, отслеживаемой через nested scroll, и исчезает в начале списка.
 */
@Composable
fun CmpScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    topBarBlur: CmpTopBarBlur = CmpTopBarBlur.OnScroll,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedContainerColor = if (containerColor != Color.Unspecified) {
        containerColor
    } else {
        colorScheme.background
    }
    val resolvedContentColor = if (contentColor != Color.Unspecified) {
        contentColor
    } else {
        colorScheme.onBackground
    }
    val blurState = rememberCmpBlurState()
    val scrollUnder = remember { ScrollUnderTracker() }
    val rampPx = with(LocalDensity.current) { TopBlurRamp.toPx() }

    Scaffold(
        modifier = modifier.nestedScroll(scrollUnder),
        topBar = {
            Box(
                modifier = Modifier
                    .then(
                        if (topBarBlur == CmpTopBarBlur.Off) {
                            Modifier
                        } else {
                            Modifier
                                .cmpEdgeBlur(blurState, CmpBlurEdge.Top) {
                                    topBarBlur.alpha(scrollUnder.offset, rampPx)
                                }
                                .cmpEdgeGradient(CmpBlurEdge.Top) { topBarBlur.alpha(scrollUnder.offset, rampPx) }
                        },
                    )
                    .windowInsetsPadding(
                        contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    ),
            ) { topBar() }
        },
        bottomBar = {
            Box(
                modifier = Modifier.windowInsetsPadding(
                    contentWindowInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                ),
            ) { bottomBar() }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = resolvedContainerColor,
        contentColor = resolvedContentColor,
        contentWindowInsets = contentWindowInsets,
        content = { padding -> Box(Modifier.cmpBlurSource(blurState)) { content(padding) } },
    )
}

/** Сколько контент прокручен от начала (≤ 0): сумма потреблённой вертикальной прокрутки вложенных списков. */
private class ScrollUnderTracker : NestedScrollConnection {
    var offset by mutableFloatStateOf(0f)
        private set

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        offset = (offset + consumed.y).coerceAtMost(0f)
        return Offset.Zero
    }
}

/** Полоса блюра под топбаром: по прокрутке (обычные списки), всегда (обратные списки — чат), выключена. */
enum class CmpTopBarBlur {
    OnScroll,
    Always,
    Off,
    ;

    internal fun alpha(offset: Float, rampPx: Float): Float = when (this) {
        OnScroll -> -offset / rampPx
        Always -> 1f
        Off -> 0f
    }
}
