package ru.alarmtrade.specmp.core.files

import java.awt.GraphicsEnvironment
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import ru.alarmtrade.specmp.core.files.FileChooserDriver.chooseInDialog
import ru.alarmtrade.specmp.core.files.FileChooserDriver.composed
import ru.alarmtrade.specmp.core.files.FileChooserDriver.dispatchers

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
        val picker = composed { rememberImagePicker(dispatchers) }

        val result = chooseInDialog(image) { picker.pick(ImageSource.Gallery) }

        assertIs<ImagePickResult.Picked>(result, "chosen file ${image.name} was returned as $result")
    }
}
