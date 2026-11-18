package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

@OptIn(ExperimentalTestApi::class)
class CmpButtonLayoutTest {

    @Test
    fun `content that fits keeps full padding`() {
        assertEquals(24, adaptivePadding(available = 200, content = 100, min = 12, full = 24))
    }

    @Test
    fun `tight content shrinks padding to what is left`() {
        assertEquals(17, adaptivePadding(available = 134, content = 100, min = 12, full = 24))
    }

    @Test
    fun `padding never goes below minimum`() {
        assertEquals(12, adaptivePadding(available = 100, content = 300, min = 12, full = 24))
    }

    @Test
    fun `unbounded width keeps full padding`() {
        assertEquals(24, adaptivePadding(available = Int.MAX_VALUE, content = 300, min = 12, full = 24))
    }

    @Test
    fun `minimum above full padding falls back to full`() {
        assertEquals(8, adaptivePadding(available = 50, content = 300, min = 12, full = 8))
    }

    @Test
    fun `wide button keeps full padding and icon`() {
        val (button, label) = bounds(width = null, text = SHORT)
        assertEquals(FULL_PADDING + ICON_AND_GAP, label.left - button.left)
    }

    @Test
    fun `narrow button with long label hides icon and uses minimal padding`() {
        val (button, label) = bounds(width = NARROW, text = LONG)
        assertEquals(MIN_PADDING, label.left - button.left)
        assertEquals(NARROW - MIN_PADDING * 2, label.width)
    }

    @Test
    fun `narrow button with short label keeps icon`() {
        val (button, label) = bounds(width = NARROW, text = SHORT)
        assertTrue(label.left - button.left >= MIN_PADDING + ICON_AND_GAP)
    }

    private fun bounds(width: Dp?, text: String): Pair<DpRect, DpRect> {
        lateinit var result: Pair<DpRect, DpRect>
        runComposeUiTest {
            setContent {
                CmpButton(
                    text = text,
                    onClick = {},
                    type = CmpButtonType.Secondary,
                    icon = CmpIcons.Download,
                    modifier = Modifier.testTag(BUTTON).then(width?.let { Modifier.width(it) } ?: Modifier),
                )
            }
            result = onNodeWithTag(BUTTON).getUnclippedBoundsInRoot() to
                onNodeWithText(text.uppercase(), useUnmergedTree = true).getUnclippedBoundsInRoot()
        }
        return result
    }

    private companion object {
        const val BUTTON = "button"
        const val SHORT = "Ок"
        const val LONG = "Экспортировать"
        val NARROW = 125.dp
        val FULL_PADDING = 24.dp
        val MIN_PADDING = 12.dp
        val ICON_AND_GAP = 32.dp
    }
}
