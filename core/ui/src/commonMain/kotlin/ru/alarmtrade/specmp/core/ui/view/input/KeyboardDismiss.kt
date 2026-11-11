package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.TraversableNode
import androidx.compose.ui.node.TraversableNode.Companion.TraverseDescendantsAction
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.node.traverseDescendants
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntSize

/**
 * Тап вне текстового поля (и вне областей [keepFocusOnTap]) снимает фокус и прячет клавиатуру.
 * Нажатия не потребляются: кнопки и строки списков под пальцем срабатывают как обычно.
 */
fun Modifier.clearFocusOnTapOutside(): Modifier = this then ClearFocusOnTapOutsideElement

/** Область, тап по которой не снимает фокус ([clearFocusOnTapOutside]): поля ввода, кнопка «Отправить» у поля. */
fun Modifier.keepFocusOnTap(): Modifier = this then KeepFocusOnTapElement

/**
 * Прокрутка пальцем/колесом вложенного списка снимает фокус и прячет клавиатуру; программная прокрутка, fling и
 * bringIntoView (фокус на поле ниже края) — нет.
 */
fun Modifier.hideKeyboardOnScroll(): Modifier = this then HideKeyboardOnScrollElement

private data object ClearFocusOnTapOutsideElement : ModifierNodeElement<ClearFocusOnTapOutsideNode>() {
    override fun create() = ClearFocusOnTapOutsideNode()

    override fun update(node: ClearFocusOnTapOutsideNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "clearFocusOnTapOutside"
    }
}

private data object KeepFocusOnTapElement : ModifierNodeElement<KeepFocusOnTapNode>() {
    override fun create() = KeepFocusOnTapNode()

    override fun update(node: KeepFocusOnTapNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "keepFocusOnTap"
    }
}

private data object HideKeyboardOnScrollElement : ModifierNodeElement<HideKeyboardOnScrollNode>() {
    override fun create() = HideKeyboardOnScrollNode().attachToNestedScroll()

    override fun update(node: HideKeyboardOnScrollNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "hideKeyboardOnScroll"
    }
}

private class KeepFocusOnTapNode :
    Modifier.Node(),
    TraversableNode {
    override val traverseKey: Any = KeepFocusOnTapKey
}

private object KeepFocusOnTapKey

// Фокус снимается на Initial-проходе отпускания — до onClick детей: клик, сам запросивший фокус, его сохранит.
private class ClearFocusOnTapOutsideNode :
    Modifier.Node(),
    PointerInputModifierNode,
    CompositionLocalConsumerModifierNode {

    private var downPosition: Offset? = null

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        if (pass != PointerEventPass.Initial) return
        val change = pointerEvent.changes.firstOrNull() ?: return
        when (pointerEvent.type) {
            PointerEventType.Press -> {
                downPosition = if (pointerEvent.changes.size == 1 && !isInsideFocusKeeper(change.position)) {
                    change.position
                } else {
                    null
                }
            }

            PointerEventType.Move -> {
                val start = downPosition ?: return
                if ((change.position - start).getDistance() > currentValueOf(LocalViewConfiguration).touchSlop) {
                    downPosition = null
                }
            }

            PointerEventType.Release -> {
                if (downPosition != null && pointerEvent.changes.none { it.pressed }) {
                    downPosition = null
                    dismissKeyboard()
                }
            }
        }
    }

    override fun onCancelPointerInput() {
        downPosition = null
    }

    private fun isInsideFocusKeeper(position: Offset): Boolean {
        val root = requireLayoutCoordinates()
        var inside = false
        traverseDescendants(KeepFocusOnTapKey) { keeper ->
            val coordinates = (keeper as DelegatableNode).requireLayoutCoordinates()
            if (coordinates.isAttached && root.localBoundingBoxOf(coordinates).contains(position)) {
                inside = true
                TraverseDescendantsAction.CancelTraversal
            } else {
                TraverseDescendantsAction.ContinueTraversal
            }
        }
        return inside
    }
}

// bringIntoView (фокус поля ниже края) тоже скроллит с источником UserInput: прячем клавиатуру, только пока
// палец/кнопка мыши прижаты к контейнеру или пришло колесо, иначе фокус на поле сам себя снимал бы.
private class HideKeyboardOnScrollNode :
    DelegatingNode(),
    NestedScrollConnection,
    PointerInputModifierNode,
    CompositionLocalConsumerModifierNode {

    private var isPointerDown = false

    fun attachToNestedScroll(): HideKeyboardOnScrollNode = apply { delegate(nestedScrollModifierNode(this, null)) }

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        if (pass != PointerEventPass.Initial) return
        if (pointerEvent.type == PointerEventType.Scroll) {
            dismissKeyboard()
        } else {
            isPointerDown = pointerEvent.changes.any { it.pressed }
        }
    }

    override fun onCancelPointerInput() {
        isPointerDown = false
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (isPointerDown && source == NestedScrollSource.UserInput && available != Offset.Zero) dismissKeyboard()
        return Offset.Zero
    }
}

private fun CompositionLocalConsumerModifierNode.dismissKeyboard() {
    currentValueOf(LocalFocusManager).clearFocus()
    currentValueOf(LocalSoftwareKeyboardController)?.hide()
}
