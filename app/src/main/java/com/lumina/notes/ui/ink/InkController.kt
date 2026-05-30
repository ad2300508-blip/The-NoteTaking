package com.lumina.notes.ui.ink

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.lumina.notes.data.ink.InkSerializer
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.data.ink.Stroke
import com.lumina.notes.data.ink.StrokePoint
import com.lumina.notes.data.ink.StrokeSmoothing
import kotlin.math.hypot

/**
 * Holds and mutates the ink for a single note. Lives in the editor's
 * ViewModel-scoped state so it survives recomposition. All mutations bump
 * [revision] so the persistence layer knows when to autosave.
 */
class InkController(initial: List<Stroke> = emptyList()) {

    val strokes: SnapshotStateList<Stroke> = mutableStateListOf<Stroke>().apply { addAll(initial) }

    // Snapshot-based history so undo/redo covers every mutation — drawing,
    // erasing, lasso delete and lasso move — not just the last stroke.
    private val undoStack = ArrayDeque<List<Stroke>>()
    private val redoStack = ArrayDeque<List<Stroke>>()
    private val maxHistory = 50

    /** Records the current strokes as a restore point before a mutation. */
    private fun pushHistory() {
        undoStack.addLast(strokes.toList())
        if (undoStack.size > maxHistory) undoStack.removeFirst()
        redoStack.clear()
    }

    var tool by mutableStateOf(PenTool.PEN)
        private set
    var color by mutableLongStateOf(0xFF111418L)
        private set
    var strokeWidth by mutableStateOf(4f)
        private set

    /** Increments on every committed change; used as an autosave trigger. */
    var revision by mutableLongStateOf(0L)
        private set

    /** Indices of strokes currently lasso-selected (highlighted, deletable). */
    val selected: SnapshotStateList<Int> = mutableStateListOf()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val hasSelection: Boolean get() = selected.isNotEmpty()

    fun selectTool(t: PenTool) { tool = t; clearSelection() }
    fun selectColor(c: Long) { color = c }
    fun setWidth(w: Float) { strokeWidth = w }

    /** Selects strokes enclosed by a lasso [polygon] (canvas-space points). */
    fun applyLasso(polygon: List<StrokePoint>) {
        val hits = com.lumina.notes.data.ink.LassoSelection.selectedIndices(strokes, polygon)
        selected.clear()
        selected.addAll(hits)
    }

    fun clearSelection() {
        if (selected.isNotEmpty()) selected.clear()
    }

    /**
     * Erases strokes overlapping a cross-out [scribble] path: any stroke with a
     * point within [radius] of the scribble is removed. One undo step.
     */
    fun eraseStrokesIn(scribble: List<StrokePoint>, radius: Float = 18f) {
        if (scribble.size < 2) return
        // Scribble bounds (with margin) for a cheap first reject.
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        for (p in scribble) {
            if (p.x < minX) minX = p.x; if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y; if (p.y > maxY) maxY = p.y
        }
        val toRemove = strokes.filter { s ->
            val b = s.bounds()
            if (b[2] < minX - radius || b[0] > maxX + radius ||
                b[3] < minY - radius || b[1] > maxY + radius
            ) return@filter false
            s.points.any { sp ->
                scribble.any { hypot(sp.x - it.x, sp.y - it.y) <= radius }
            }
        }
        if (toRemove.isEmpty()) return
        pushHistory()
        strokes.removeAll(toRemove)
        selected.clear()
        revision++
    }

    /** Removes the lasso-selected strokes. */
    fun deleteSelected() {
        if (selected.isEmpty()) return
        pushHistory()
        val toRemove = selected.toList().sortedDescending()
        for (i in toRemove) if (i in strokes.indices) strokes.removeAt(i)
        selected.clear()
        revision++
    }

    /**
     * Translates the selected strokes by ([dx], [dy]) in canvas space.
     * [record] should be true only on the first move of a drag gesture, so a
     * whole drag is one undo step rather than one per pointer sample.
     */
    fun moveSelected(dx: Float, dy: Float, record: Boolean = true) {
        if (selected.isEmpty() || (dx == 0f && dy == 0f)) return
        if (record) pushHistory()
        for (i in selected) {
            if (i !in strokes.indices) continue
            strokes[i] = strokes[i].translated(dx, dy)
        }
        revision++
    }

    fun commitStroke(points: List<StrokePoint>, withTool: PenTool, tilt: Float = 0f) {
        if (points.size < 2) {
            // A tap with the pen still leaves a dot.
            if (points.size == 1) {
                val p = points.first()
                addStroke(Stroke(listOf(p, p.copy(x = p.x + 0.5f)), color, strokeWidth, withTool, tilt))
            }
            return
        }
        val width = if (withTool == PenTool.HIGHLIGHTER) strokeWidth * 4f else strokeWidth
        // Smooth out S Pen sampling jitter for cleaner handwriting.
        val shaped = StrokeSmoothing.smooth(points)
        addStroke(Stroke(shaped, color, width, withTool, tilt))
    }

    private fun addStroke(stroke: Stroke) {
        pushHistory()
        strokes.add(stroke)
        revision++
    }

    /** Removes any stroke passing within [radius] of [x],[y] (stroke eraser). */
    fun eraseAt(x: Float, y: Float, radius: Float) {
        val toRemove = strokes.filter { strokeHit(it, x, y, radius) }
        if (toRemove.isEmpty()) return
        pushHistory()
        strokes.removeAll(toRemove)
        revision++
    }

    fun undo() {
        val prev = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(strokes.toList())
        strokes.clear()
        strokes.addAll(prev)
        selected.clear()
        revision++
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(strokes.toList())
        strokes.clear()
        strokes.addAll(next)
        selected.clear()
        revision++
    }

    fun clear() {
        if (strokes.isNotEmpty()) {
            pushHistory()
            strokes.clear()
            selected.clear()
            revision++
        }
    }

    fun encode(): String = InkSerializer.encode(strokes)

    /**
     * Replaces the most recent pen stroke with its recognized ideal shape
     * (line / rectangle / ellipse). Returns true if a shape was applied. One
     * undo step, so it's easy to revert if the guess is wrong.
     */
    fun straightenLastStroke(): Boolean {
        val last = strokes.lastOrNull() ?: return false
        if (last.tool != PenTool.PEN) return false
        val shape = com.lumina.notes.data.ink.ShapeRecognizer.recognize(last.points)
            ?: return false
        val newPoints = when (shape) {
            is com.lumina.notes.data.ink.RecognizedShape.Line ->
                listOf(StrokePoint(shape.x1, shape.y1), StrokePoint(shape.x2, shape.y2))
            is com.lumina.notes.data.ink.RecognizedShape.Rectangle ->
                com.lumina.notes.data.ink.ShapeRecognizer.rectToPoints(shape)
            is com.lumina.notes.data.ink.RecognizedShape.Ellipse ->
                com.lumina.notes.data.ink.ShapeRecognizer.ellipseToPoints(shape)
        }
        pushHistory()
        strokes[strokes.lastIndex] = last.copy(points = newPoints)
        revision++
        return true
    }

    /** Largest y-coordinate across all ink (0 when empty); drives page growth. */
    fun contentBottom(): Float {
        var maxY = 0f
        for (s in strokes) {
            for (p in s.points) if (p.y > maxY) maxY = p.y
        }
        return maxY
    }

    private fun strokeHit(s: Stroke, x: Float, y: Float, radius: Float): Boolean {
        val b = s.bounds()
        if (x < b[0] - radius || x > b[2] + radius || y < b[1] - radius || y > b[3] + radius) {
            return false
        }
        for (p in s.points) {
            if (hypot(p.x - x, p.y - y) <= radius) return true
        }
        return false
    }
}
