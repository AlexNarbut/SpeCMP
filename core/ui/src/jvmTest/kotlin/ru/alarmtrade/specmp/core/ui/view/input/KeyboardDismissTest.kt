package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

@OptIn(ExperimentalTestApi::class)
class KeyboardDismissTest {

    @Test
    fun `tap outside field clears focus and still clicks the button`() = runComposeUiTest {
        var clicks by mutableIntStateOf(0)
        setContent {
            CmpColumn(modifier = Modifier.fillMaxSize().clearFocusOnTapOutside()) {
                var text by remember { mutableStateOf("") }
                CmpTextField(value = text, onValueChange = { text = it }, modifier = Modifier.testTag(FIELD))
                CmpButton(text = "OK", onClick = { clicks++ }, modifier = Modifier.testTag(BUTTON))
            }
        }

        onNodeWithTag(FIELD).performClick()
        onNodeWithTag(FIELD).assertIsFocused()

        onNodeWithTag(BUTTON).performClick()

        onNodeWithTag(FIELD).assertIsNotFocused()
        assertEquals(1, clicks)
    }

    @Test
    fun `tap on non-focusable area clears focus and reaches its handler`() = runComposeUiTest {
        var taps by mutableIntStateOf(0)
        setContent {
            CmpColumn(modifier = Modifier.fillMaxSize().clearFocusOnTapOutside()) {
                var text by remember { mutableStateOf("") }
                CmpTextField(value = text, onValueChange = { text = it }, modifier = Modifier.testTag(FIELD))
                CmpBox(
                    modifier = Modifier
                        .size(96.dp)
                        .testTag(AREA)
                        .pointerInput(Unit) { detectTapGestures { taps++ } },
                )
            }
        }

        onNodeWithTag(FIELD).performClick()
        onNodeWithTag(FIELD).assertIsFocused()

        onNodeWithTag(AREA).performClick()

        onNodeWithTag(FIELD).assertIsNotFocused()
        assertEquals(1, taps)
    }

    @Test
    fun `tap on field keeps it focused`() = runComposeUiTest {
        setContent {
            CmpColumn(modifier = Modifier.fillMaxSize().clearFocusOnTapOutside()) {
                var text by remember { mutableStateOf("") }
                CmpTextField(value = text, onValueChange = { text = it }, modifier = Modifier.testTag(FIELD))
            }
        }

        onNodeWithTag(FIELD).performClick()
        onNodeWithTag(FIELD).performClick()

        onNodeWithTag(FIELD).assertIsFocused()
    }

    @Test
    fun `scrolling list clears focus`() = runComposeUiTest {
        setContent {
            CmpColumn(modifier = Modifier.fillMaxSize()) {
                var text by remember { mutableStateOf("") }
                CmpSearchField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "",
                    clearContentDescription = "",
                    modifier = Modifier.testTag(FIELD),
                )
                LazyColumn(modifier = Modifier.fillMaxSize().hideKeyboardOnScroll().testTag(LIST)) {
                    items(LIST_SIZE) { index ->
                        CmpText(text = "Item $index", modifier = Modifier.fillMaxWidth().height(48.dp))
                    }
                }
            }
        }

        onNodeWithTag(FIELD).performClick()
        onNodeWithTag(FIELD).assertIsFocused()

        onNodeWithTag(LIST).performTouchInput { swipeUp() }

        onNodeWithTag(FIELD).assertIsNotFocused()
    }

    @Test
    fun `focusing a field below the fold keeps focus while the container scrolls it into view`() = runComposeUiTest {
        setContent {
            CmpColumn(
                modifier = Modifier
                    .height(200.dp)
                    .hideKeyboardOnScroll()
                    .verticalScroll(rememberScrollState()),
            ) {
                CmpBox(modifier = Modifier.fillMaxWidth().height(400.dp))
                var text by remember { mutableStateOf("") }
                CmpTextField(value = text, onValueChange = { text = it }, modifier = Modifier.testTag(FIELD))
            }
        }

        onNodeWithTag(FIELD).requestFocus()

        onNodeWithTag(FIELD).assertIsFocused().assertIsDisplayed()
    }

    @Test
    fun `search ime action clears focus`() = runComposeUiTest {
        setContent {
            var text by remember { mutableStateOf("") }
            CmpSearchField(
                value = text,
                onValueChange = { text = it },
                placeholder = "",
                clearContentDescription = "",
                modifier = Modifier.testTag(FIELD),
            )
        }

        onNodeWithTag(FIELD).performClick()
        onNodeWithTag(FIELD).assertIsFocused()

        onNodeWithTag(FIELD).performImeAction()

        onNodeWithTag(FIELD).assertIsNotFocused()
    }

    private companion object {
        const val FIELD = "field"
        const val BUTTON = "button"
        const val AREA = "area"
        const val LIST = "list"
        const val LIST_SIZE = 100
    }
}
