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
    private val redoStack = ArrayDeque<Stroke>()

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

    val canUndo: Boolean get() = strokes.isNotEmpty()
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

    /** Removes the lasso-selected strokes. */
    fun deleteSelected() {
        if (selected.isEmpty()) return
        val toRemove = selected.toList().sortedDescending()
        for (i in toRemove) if (i in strokes.indices) strokes.removeAt(i)
        selected.clear()
        redoStack.clear()
        revision++
    }

    fun commitStroke(points: List<StrokePoint>, withTool: PenTool) {
        if (points.size < 2) {
            // A tap with the pen still leaves a dot.
            if (points.size == 1) {
                val p = points.first()
                addStroke(Stroke(listOf(p, p.copy(x = p.x + 0.5f)), color, strokeWidth, withTool))
            }
            return
        }
        val width = if (withTool == PenTool.HIGHLIGHTER) strokeWidth * 4f else strokeWidth
        // Smooth out S Pen sampling jitter for cleaner handwriting.
        val shaped = StrokeSmoothing.smooth(points)
        addStroke(Stroke(shaped, color, width, withTool))
    }

    private fun addStroke(stroke: Stroke) {
        strokes.add(stroke)
        redoStack.clear()
        revision++
    }

    /** Removes any stroke passing within [radius] of [x],[y] (stroke eraser). */
    fun eraseAt(x: Float, y: Float, radius: Float) {
        var changed = false
        val it = strokes.listIterator(strokes.size)
        while (it.hasPrevious()) {
            val s = it.previous()
            if (strokeHit(s, x, y, radius)) {
                strokes.remove(s)
                changed = true
            }
        }
        if (changed) {
            redoStack.clear()
            revision++
        }
    }

    fun undo() {
        if (strokes.isNotEmpty()) {
            redoStack.addLast(strokes.removeAt(strokes.lastIndex))
            revision++
        }
    }

    fun redo() {
        redoStack.removeLastOrNull()?.let {
            strokes.add(it)
            revision++
        }
    }

    fun clear() {
        if (strokes.isNotEmpty()) {
            strokes.clear()
            redoStack.clear()
            revision++
        }
    }

    fun encode(): String = InkSerializer.encode(strokes)

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
