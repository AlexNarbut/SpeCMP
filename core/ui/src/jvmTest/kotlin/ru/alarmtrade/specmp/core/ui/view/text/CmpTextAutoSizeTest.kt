package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn

@OptIn(ExperimentalTestApi::class)
class CmpTextAutoSizeTest {

    @Test
    fun `text that fits keeps style size`() {
        val heights = measure(widthRatio = 1.2f, maxLines = Int.MAX_VALUE)
        assertEquals(heights.reference, heights.auto)
        assertEquals(heights.reference, heights.plain)
    }

    @Test
    fun `slightly too long text shrinks into one line instead of wrapping`() {
        val heights = measure(widthRatio = 0.85f, maxLines = Int.MAX_VALUE)
        assertEquals(heights.reference, heights.auto)
        assertTrue(heights.plain > heights.reference)
    }

    @Test
    fun `single line text below minimum stays on one line`() {
        val heights = measure(widthRatio = 0.3f, maxLines = 1)
        assertEquals(heights.reference, heights.auto)
    }

    @Test
    fun `multi line text below minimum wraps at style size`() {
        val heights = measure(widthRatio = 0.3f, maxLines = Int.MAX_VALUE)
        assertTrue(heights.auto > heights.reference)
        assertEquals(heights.plain, heights.auto)
    }

    private class Heights(val reference: Dp, val auto: Dp, val plain: Dp)

    private fun measure(widthRatio: Float, maxLines: Int): Heights {
        lateinit var result: Heights
        runComposeUiTest {
            var width by mutableStateOf<Dp?>(null)
            setContent {
                CmpColumn {
                    CmpText(text = TEXT, style = CmpTextStyle.Headline3, modifier = Modifier.testTag(REF))
                    width?.let { w ->
                        CmpText(
                            text = TEXT,
                            style = CmpTextStyle.Headline3,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis,
                            autoSize = true,
                            modifier = Modifier.width(w).testTag(AUTO),
                        )
                        CmpText(
                            text = TEXT,
                            style = CmpTextStyle.Headline3,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(w).testTag(PLAIN),
                        )
                    }
                }
            }
            val reference = onNodeWithTag(REF).getUnclippedBoundsInRoot()
            width = reference.width * widthRatio
            waitForIdle()
            result = Heights(
                reference = reference.height,
                auto = onNodeWithTag(AUTO).getUnclippedBoundsInRoot().height,
                plain = onNodeWithTag(PLAIN).getUnclippedBoundsInRoot().height,
            )
        }
        return result
    }

    private companion object {
        const val TEXT = "НЕ ПОДДЕРЖИВАЕТСЯ"
        const val REF = "ref"
        const val AUTO = "auto"
        const val PLAIN = "plain"
    }
}
