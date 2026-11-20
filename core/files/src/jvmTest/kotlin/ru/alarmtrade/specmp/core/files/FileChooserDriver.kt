package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.awt.Container
import java.awt.Window
import java.io.File
import java.util.concurrent.Executor
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import ru.alarmtrade.specmp.core.common.AppDispatchers

// Настоящий JFileChooser: нужен не-headless JVM (окно выбора открывается и подтверждается само).
// Main — EDT: в jvmTest нет kotlinx-coroutines-swing.
internal object FileChooserDriver {

    val dispatchers = AppDispatchers(main = Executor { SwingUtilities.invokeLater(it) }.asCoroutineDispatcher())

    fun <T> composed(factory: @Composable () -> T): T {
        var value: T? = null
        val scene = ImageComposeScene(SCENE_SIDE, SCENE_SIDE, Density(1f)) { value = factory() }
        scene.render()
        scene.close()
        return checkNotNull(value)
    }

    fun <T> chooseInDialog(file: File, action: suspend () -> T): T {
        var approved = false
        val driver = thread(isDaemon = true) { approved = approveWhenShown(file) }
        val result = runBlocking(Dispatchers.IO) { action() }
        driver.join(DIALOG_WAIT_MS)
        assertTrue(approved, "file dialog was not shown/approved")
        return result
    }

    private fun approveWhenShown(file: File): Boolean {
        val deadline = System.currentTimeMillis() + DIALOG_WAIT_MS
        while (System.currentTimeMillis() < deadline) {
            val chooser = Window.getWindows()
                .filter { it is JDialog && it.isShowing }
                .firstNotNullOfOrNull { findChooser(it) }
            if (chooser != null) {
                SwingUtilities.invokeAndWait {
                    chooser.currentDirectory = file.parentFile
                    chooser.selectedFile = file
                    chooser.approveSelection()
                }
                return true
            }
            Thread.sleep(POLL_MS)
        }
        return false
    }

    private fun findChooser(container: Container): JFileChooser? {
        if (container is JFileChooser) return container
        return container.components.filterIsInstance<Container>().firstNotNullOfOrNull { findChooser(it) }
    }

    private const val SCENE_SIDE = 10
    private const val DIALOG_WAIT_MS = 15_000L
    private const val POLL_MS = 100L
}
