package ru.alarmtrade.specmp.core.files

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageCompressionTest {

    @Test
    fun `landscape image is fitted by its width`() {
        assertEquals(ImageSize(800, 600), scaledToFit(4000, 3000))
    }

    @Test
    fun `portrait image is fitted by its height`() {
        assertEquals(ImageSize(450, 800), scaledToFit(2700, 4800))
    }

    @Test
    fun `small image is not upscaled`() {
        assertEquals(ImageSize(640, 480), scaledToFit(640, 480))
    }

    @Test
    fun `thin image keeps at least one pixel`() {
        assertEquals(ImageSize(800, 1), scaledToFit(10000, 2))
    }

    @Test
    fun `sample size keeps the long side not below the limit`() {
        assertEquals(4, sampleSizeFor(4000, 3000))
        assertEquals(1, sampleSizeFor(1500, 1000))
        assertEquals(2, sampleSizeFor(1600, 1200))
        assertEquals(1, sampleSizeFor(300, 200))
    }

    @Test
    fun `file name gets the jpg extension`() {
        assertEquals("IMG_1.jpg", jpegFileName("IMG_1.HEIC"))
        assertEquals("photo.jpg", jpegFileName("photo"))
        assertEquals("archive.tar.jpg", jpegFileName("archive.tar.png"))
    }

    @Test
    fun `missing or unsafe name falls back to a default`() {
        assertEquals("image.jpg", jpegFileName(null))
        assertEquals("image.jpg", jpegFileName("  "))
        assertEquals("my_photo.jpg", jpegFileName("../dir/my:photo.png"))
    }

    @Test
    fun `crop region is the whole image without a crop`() {
        assertEquals(ImageRegion(0, 0, 640, 480), cropRegion(null, 640, 480))
    }

    @Test
    fun `crop region of the same size is taken as is`() {
        val crop = ImageCropRect(left = 100, top = 50, width = 300, height = 300, imageWidth = 2048, imageHeight = 1536)

        assertEquals(ImageRegion(100, 50, 300, 300), cropRegion(crop, 2048, 1536))
    }

    @Test
    fun `crop region is scaled proportionally to another image size`() {
        val crop =
            ImageCropRect(left = 512, top = 256, width = 1024, height = 1024, imageWidth = 2048, imageHeight = 1536)

        assertEquals(ImageRegion(1000, 500, 2000, 2000), cropRegion(crop, 4000, 3000))
    }

    @Test
    fun `crop region stays inside the image`() {
        val crop =
            ImageCropRect(left = 1900, top = -10, width = 500, height = 500, imageWidth = 2000, imageHeight = 1000)

        assertEquals(ImageRegion(1900, 0, 100, 490), cropRegion(crop, 2000, 1000))
    }
}
