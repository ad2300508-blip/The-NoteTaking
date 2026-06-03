package com.lumina.notes.ui.ink

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class CanvasTransformTest {

    @Test fun identityMapsPointToItself() {
        val t = CanvasTransform()
        val p = Offset(12f, 34f)
        assertOffsetEquals(p, t.screenToCanvas(p))
        assertOffsetEquals(p, t.canvasToScreen(p))
    }

    @Test fun screenToCanvasInvertsCanvasToScreen() {
        val t = CanvasTransform(scale = 2f, offset = Offset(10f, 20f))
        val canvas = Offset(5f, 7f)
        val screen = t.canvasToScreen(canvas)
        assertOffsetEquals(canvas, t.screenToCanvas(screen))
    }

    @Test fun zoomKeepsCentroidPointStable() {
        val t = CanvasTransform()
        val centroid = Offset(100f, 100f)
        val logicalBefore = t.screenToCanvas(centroid)
        val zoomed = t.transform(centroid, zoom = 2f, pan = Offset.Zero)
        val logicalAfter = zoomed.screenToCanvas(centroid)
        assertOffsetEquals(logicalBefore, logicalAfter)
        assertEquals(2f, zoomed.scale, 0.0001f)
    }

    @Test fun scaleIsClamped() {
        val t = CanvasTransform()
        val huge = t.transform(Offset.Zero, zoom = 100f, pan = Offset.Zero)
        assertEquals(CanvasTransform.MAX_SCALE, huge.scale, 0.0001f)
        val tiny = t.transform(Offset.Zero, zoom = 0.001f, pan = Offset.Zero)
        assertEquals(CanvasTransform.MIN_SCALE, tiny.scale, 0.0001f)
    }

    @Test fun panShiftsOffset() {
        val t = CanvasTransform()
        val panned = t.transform(Offset.Zero, zoom = 1f, pan = Offset(15f, -5f))
        assertOffsetEquals(Offset(15f, -5f), panned.offset)
    }

    @Test fun clampVerticalStopsScrollingAboveTop() {
        // Pulled down (positive y) past the top -> clamped to 0.
        val t = CanvasTransform(scale = 1f, offset = Offset(0f, 50f))
        val c = t.clampVertical(viewportHeight = 1000f, sheetHeight = 3000f)
        assertEquals(0f, c.offset.y, 0.001f)
    }

    @Test fun clampVerticalStopsScrollingBelowLastPage() {
        // Scrolled up too far; min is viewport - scaledSheet = 1000 - 3000 = -2000.
        val t = CanvasTransform(scale = 1f, offset = Offset(0f, -5000f))
        val c = t.clampVertical(1000f, 3000f)
        assertEquals(-2000f, c.offset.y, 0.001f)
    }

    @Test fun clampVerticalNoOpWhenSheetShorterThanViewport() {
        val t = CanvasTransform(scale = 1f, offset = Offset(0f, -100f))
        val c = t.clampVertical(2000f, 1000f)
        assertEquals(0f, c.offset.y, 0.001f)
    }

    private fun assertOffsetEquals(expected: Offset, actual: Offset) {
        assertEquals(expected.x, actual.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)
    }
}
