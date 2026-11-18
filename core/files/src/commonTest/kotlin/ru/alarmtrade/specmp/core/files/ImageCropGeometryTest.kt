package ru.alarmtrade.specmp.core.files

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageCropGeometryTest {

    private val landscape = ImageCropGeometry(imageWidth = 2000, imageHeight = 1000, cropSize = 500f)

    @Test
    fun `initial transform is centred and fills the crop area`() {
        assertEquals(ImageCropTransform(scale = 0.5f, offsetX = 0f, offsetY = 0f), landscape.initial())
        assertEquals(ImageCropRect(500, 0, 1000, 1000, 2000, 1000), landscape.sourceRect(landscape.initial()))
    }

    @Test
    fun `zoom is limited to fill the area and four times that`() {
        assertEquals(0.5f, landscape.transform(landscape.initial(), zoom = 0.1f).scale)
        assertEquals(2f, landscape.transform(landscape.initial(), zoom = 100f).scale)
    }

    @Test
    fun `pan cannot leave empty space inside the crop area`() {
        val moved = landscape.transform(landscape.initial(), panX = 10_000f, panY = 10_000f)

        // Ширина на экране 1000 px: центр сдвигается не больше чем на (1000 − 500) / 2; по высоте запаса нет.
        assertEquals(ImageCropTransform(0.5f, 250f, 0f), moved)
        assertEquals(ImageCropRect(0, 0, 1000, 1000, 2000, 1000), landscape.sourceRect(moved))
    }

    @Test
    fun `pan to the other edge crops the right part`() {
        val moved = landscape.transform(landscape.initial(), panX = -10_000f)

        assertEquals(ImageCropRect(1000, 0, 1000, 1000, 2000, 1000), landscape.sourceRect(moved))
    }

    @Test
    fun `zoom around the centre keeps the centre of the crop`() {
        val zoomed = landscape.transform(landscape.initial(), zoom = 2f)

        assertEquals(ImageCropRect(750, 250, 500, 500, 2000, 1000), landscape.sourceRect(zoomed))
    }

    @Test
    fun `zoom around an off-centre point keeps that point in place`() {
        val zoomed = landscape.transform(landscape.initial(), zoom = 2f, centroidX = 100f, centroidY = 0f)

        // Точка в 100 px правее центра (x = 1000 + 100 / 0.5 = 1200) остаётся на месте: левый край — 1200 − 350 / 1.
        assertEquals(ImageCropTransform(1f, -100f, 0f), zoomed)
        assertEquals(ImageCropRect(850, 250, 500, 500, 2000, 1000), landscape.sourceRect(zoomed))
    }

    @Test
    fun `portrait image fills by width`() {
        val portrait = ImageCropGeometry(imageWidth = 600, imageHeight = 1200, cropSize = 300f)

        assertEquals(0.5f, portrait.minScale)
        assertEquals(ImageCropRect(0, 300, 600, 600, 600, 1200), portrait.sourceRect(portrait.initial()))
        val top = portrait.transform(portrait.initial(), panY = 10_000f)
        assertEquals(ImageCropRect(0, 0, 600, 600, 600, 1200), portrait.sourceRect(top))
    }

    @Test
    fun `out of range transform is clamped when the crop area changes`() {
        val bigger = ImageCropGeometry(imageWidth = 2000, imageHeight = 1000, cropSize = 800f)

        assertEquals(ImageCropTransform(0.8f, 400f, 0f), bigger.clamp(ImageCropTransform(0.5f, 900f, 50f)))
    }
}
