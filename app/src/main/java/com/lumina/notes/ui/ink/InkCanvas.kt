package com.lumina.notes.ui.ink

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.data.ink.Stroke
import com.lumina.notes.data.ink.StrokePoint

/**
 * The freehand drawing surface. Reads true stylus [pressure] and pointer
 * [PointerType] from the input stream so the S Pen draws pressure-tapered
 * lines, the side button erases, and (with palm rejection) a resting hand is
 * ignored while the pen is in use.
 */
@Composable
fun InkCanvas(
    controller: InkController,
    palmRejection: Boolean,
    modifier: Modifier = Modifier,
) {
    var liveStroke by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }
    var liveTool by remember { mutableStateOf(PenTool.PEN) }
    val eraserRadius = with(LocalDensity.current) { 16.dp.toPx() }

    Canvas(
        modifier = modifier.pointerInput(controller, palmRejection) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)

                // Palm rejection: while drawing with the pen, ignore finger touches.
                if (palmRejection && down.type == PointerType.Touch) return@awaitEachGesture

                val erasing = controller.tool == PenTool.ERASER || down.type == PointerType.Eraser
                val drawTool = if (down.type == PointerType.Eraser) PenTool.ERASER else controller.tool
                val points = ArrayList<StrokePoint>()

                fun handle(change: PointerInputChange) {
                    if (erasing) {
                        // Use historical samples too so fast erases don't skip.
                        change.historical.forEach {
                            controller.eraseAt(it.position.x, it.position.y, eraserRadius)
                        }
                        controller.eraseAt(change.position.x, change.position.y, eraserRadius)
                    } else {
                        change.historical.forEach {
                            points.add(StrokePoint(it.position.x, it.position.y, sanitize(change.pressure)))
                        }
                        points.add(StrokePoint(change.position.x, change.position.y, sanitize(change.pressure)))
                        liveStroke = ArrayList(points)
                        liveTool = drawTool
                    }
                    change.consume()
                }

                handle(down)

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        change.consume()
                        break
                    }
                    handle(change)
                }

                if (!erasing) {
                    controller.commitStroke(points, drawTool)
                    liveStroke = emptyList()
                }
            }
        }
    ) {
        controller.strokes.forEach { drawInk(it) }
        if (liveStroke.size >= 2) {
            drawInk(
                Stroke(
                    points = liveStroke,
                    color = controller.color,
                    baseWidth = if (liveTool == PenTool.HIGHLIGHTER) controller.strokeWidth * 4f
                    else controller.strokeWidth,
                    tool = liveTool,
                )
            )
        }
    }
}

private fun sanitize(pressure: Float): Float =
    if (pressure.isNaN() || pressure <= 0f) 1f else pressure.coerceIn(0.05f, 1f)

private fun DrawScope.drawInk(stroke: Stroke) {
    val pts = stroke.points
    if (pts.size < 2) return
    val color = Color(stroke.color)

    when (stroke.tool) {
        PenTool.HIGHLIGHTER -> {
            val path = smoothPath(pts)
            drawPath(
                path = path,
                color = color.copy(alpha = 0.30f),
                style = StrokeStyle(
                    width = stroke.baseWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
                blendMode = BlendMode.Multiply,
            )
        }
        else -> {
            // Pressure-tapered: width follows pen pressure for a natural line.
            for (i in 1 until pts.size) {
                val a = pts[i - 1]
                val b = pts[i]
                val pressure = ((a.pressure + b.pressure) * 0.5f)
                val w = stroke.baseWidth * (0.35f + 0.65f * pressure)
                drawLine(
                    color = color,
                    start = Offset(a.x, a.y),
                    end = Offset(b.x, b.y),
                    strokeWidth = w,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

/** Quadratic-bezier smoothing through segment midpoints. */
private fun smoothPath(pts: List<StrokePoint>): Path {
    val path = Path()
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size - 1) {
        val mx = (pts[i].x + pts[i + 1].x) / 2f
        val my = (pts[i].y + pts[i + 1].y) / 2f
        path.quadraticTo(pts[i].x, pts[i].y, mx, my)
    }
    val last = pts.last()
    path.lineTo(last.x, last.y)
    return path
}
