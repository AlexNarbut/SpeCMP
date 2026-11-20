package ru.alarmtrade.specmp.core.files

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.awt.Container
import java.awt.GraphicsEnvironment
import java.awt.Window
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Executor
import javax.imageio.ImageIO
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import ru.alarmtrade.specmp.core.common.AppDispatchers

// Настоящий JFileChooser: нужен не-headless JVM (окно выбора открывается и подтверждается само).
// Main — EDT: в jvmTest нет kotlinx-coroutines-swing.
class DesktopImagePickerPickTest {

    private val directory: File = Files.createTempDirectory("image-picker-test").toFile()

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `file chosen in the dialog is picked`() {
        if (GraphicsEnvironment.isHeadless()) return
        val image = File(directory, "photo.png")
        ImageIO.write(BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "png", image)
        val edt = Executor { SwingUtilities.invokeLater(it) }.asCoroutineDispatcher()
        val picker = composedPicker(AppDispatchers(main = edt))

        var approved = false
        val driver = thread(isDaemon = true) { approved = approveWhenShown(image) }
        val result = runBlocking(Dispatchers.IO) { picker.pick(ImageSource.Gallery) }
        driver.join(DIALOG_WAIT_MS)

        assertTrue(approved, "file dialog was not shown/approved")
        assertIs<ImagePickResult.Picked>(result, "chosen file ${image.name} was returned as $result")
    }

    private fun composedPicker(dispatchers: AppDispatchers): ImagePicker {
        var picker: ImagePicker? = null
        val scene = ImageComposeScene(SCENE_SIDE, SCENE_SIDE, Density(1f)) {
            picker = rememberImagePicker(dispatchers)
        }
        scene.render()
        scene.close()
        return checkNotNull(picker)
    }

    private fun approveWhenShown(file: File): Boolean {
        val deadline = System.currentTimeMillis() + DIALOG_WAIT_MS
        while (System.currentTimeMillis() < deadline) {
            val chooser = Window.getWindows()
                .filter { it is JDialog && it.isShowing }
                .firstNotNullOfOrNull { findChooser(it) }
            if (chooser != null) {
                SwingUtilities.invokeAndWait {
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

    private companion object {
        const val SCENE_SIDE = 10
        const val DIALOG_WAIT_MS = 15_000L
        const val POLL_MS = 100L
    }
}
