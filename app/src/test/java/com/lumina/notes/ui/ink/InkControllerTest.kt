package com.lumina.notes.ui.ink

import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.data.ink.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InkControllerTest {

    private fun line() = listOf(
        StrokePoint(0f, 0f),
        StrokePoint(10f, 10f),
        StrokePoint(20f, 20f),
    )

    @Test fun commitAddsStroke() {
        val c = InkController()
        assertFalse(c.canUndo)
        c.commitStroke(line(), PenTool.PEN)
        assertEquals(1, c.strokes.size)
        assertTrue(c.canUndo)
        assertTrue(c.revision > 0)
    }

    @Test fun singleTapLeavesADot() {
        val c = InkController()
        c.commitStroke(listOf(StrokePoint(5f, 5f)), PenTool.PEN)
        assertEquals(1, c.strokes.size)
    }

    @Test fun emptyCommitDoesNothing() {
        val c = InkController()
        c.commitStroke(emptyList(), PenTool.PEN)
        assertEquals(0, c.strokes.size)
    }

    @Test fun undoRedoRoundTrip() {
        val c = InkController()
        c.commitStroke(line(), PenTool.PEN)
        c.undo()
        assertEquals(0, c.strokes.size)
        assertTrue(c.canRedo)
        c.redo()
        assertEquals(1, c.strokes.size)
        assertFalse(c.canRedo)
    }

    @Test fun newStrokeClearsRedoStack() {
        val c = InkController()
        c.commitStroke(line(), PenTool.PEN)
        c.undo()
        assertTrue(c.canRedo)
        c.commitStroke(line(), PenTool.PEN)
        assertFalse(c.canRedo)
    }

    @Test fun clearRemovesEverything() {
        val c = InkController()
        c.commitStroke(line(), PenTool.PEN)
        c.commitStroke(line(), PenTool.PEN)
        c.clear()
        assertEquals(0, c.strokes.size)
        assertFalse(c.canUndo)
    }

    @Test fun eraseRemovesNearbyStroke() {
        val c = InkController()
        c.commitStroke(line(), PenTool.PEN)
        c.eraseAt(10f, 10f, radius = 5f)
        assertEquals(0, c.strokes.size)
    }

    @Test fun eraseMissesDistantStroke() {
        val c = InkController()
        c.commitStroke(line(), PenTool.PEN)
        c.eraseAt(500f, 500f, radius = 5f)
        assertEquals(1, c.strokes.size)
    }

    @Test fun highlighterWidthIsScaledOnCommit() {
        val c = InkController()
        c.setWidth(4f)
        c.commitStroke(line(), PenTool.HIGHLIGHTER)
        assertEquals(16f, c.strokes.first().baseWidth, 0.001f)
    }
}
