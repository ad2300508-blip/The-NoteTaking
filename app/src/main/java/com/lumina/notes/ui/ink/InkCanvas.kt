package com.lumina.notes.ui.ink

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.ink.NibWidth
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.data.ink.Stroke
import com.lumina.notes.data.ink.StrokePoint

/**
 * The freehand drawing surface. Reads true stylus [pressure] and pointer
 * [PointerType] from the input stream so the S Pen draws pressure-tapered
 * lines, the side button erases, and (with palm rejection) a resting hand is
 * ignored while the pen is in use.
 *
 * Two-finger gestures pinch-zoom and pan the canvas; strokes are stored in the
 * canvas' logical space (see [CanvasTransform]) so the drawing stays crisp at
 * any zoom. Strokes are rendered as vectors under a draw-time transform rather
 * than a scaled bitmap, so they never blur when zoomed in.
 */
private enum class GestureMode { UNDECIDED, DRAW, TRANSFORM }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InkCanvas(
    controller: InkController,
    palmRejection: Boolean,
    modifier: Modifier = Modifier,
    resetZoomSignal: Int = 0,
    onZoomChange: (Float) -> Unit = {},
) {
    var liveStroke by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }
    var liveTool by remember { mutableStateOf(PenTool.PEN) }
    var transform by remember { mutableStateOf(CanvasTransform()) }
    // Where the S Pen is hovering (no contact yet), in screen space, or null.
    var hover by remember { mutableStateOf<Offset?>(null) }
    val eraserRadius = with(LocalDensity.current) { 16.dp.toPx() }
    val haptics = LocalHapticFeedback.current

    // External reset (button/double-tap) returns the canvas to 1:1.
    androidx.compose.runtime.LaunchedEffect(resetZoomSignal) {
        if (resetZoomSignal > 0) {
            transform = CanvasTransform()
            onZoomChange(1f)
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(controller, palmRejection) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pen = down.type == PointerType.Stylus || down.type == PointerType.Eraser
                    val erasing = controller.tool == PenTool.ERASER || down.type == PointerType.Eraser
                    val drawTool =
                        if (down.type == PointerType.Eraser) PenTool.ERASER else controller.tool
                    val points = ArrayList<StrokePoint>()

                    fun drawAt(change: PointerInputChange) {
                        if (erasing) {
                            val r = eraserRadius / transform.scale
                            change.historical.forEach {
                                val p = transform.screenToCanvas(it.position)
                                controller.eraseAt(p.x, p.y, r)
                            }
                            val p = transform.screenToCanvas(change.position)
                            controller.eraseAt(p.x, p.y, r)
                        } else {
                            change.historical.forEach {
                                val p = transform.screenToCanvas(it.position)
                                points.add(StrokePoint(p.x, p.y, sanitize(change.pressure)))
                            }
                            val p = transform.screenToCanvas(change.position)
                            points.add(StrokePoint(p.x, p.y, sanitize(change.pressure)))
                            liveStroke = ArrayList(points)
                            liveTool = drawTool
                        }
                    }

                    var mode = if (pen) GestureMode.DRAW else GestureMode.UNDECIDED
                    if (mode == GestureMode.DRAW) {
                        drawAt(down)
                        down.consume()
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }

                        if (mode == GestureMode.UNDECIDED) {
                            mode = when {
                                pressedCount >= 2 -> GestureMode.TRANSFORM
                                !palmRejection -> GestureMode.DRAW.also {
                                    event.changes.firstOrNull { it.id == down.id }?.let(::drawAt)
                                }
                                else -> GestureMode.UNDECIDED
                            }
                        }

                        when (mode) {
                            GestureMode.TRANSFORM -> {
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val centroid = event.calculateCentroid()
                                if (centroid != Offset.Unspecified) {
                                    transform = transform.transform(centroid, zoom, pan)
                                    onZoomChange(transform.scale)
                                }
                                event.changes.forEach { it.consume() }
                            }
                            GestureMode.DRAW -> {
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change != null && change.pressed) {
                                    drawAt(change)
                                    change.consume()
                                }
                            }
                            GestureMode.UNDECIDED -> Unit
                        }

                        if (event.changes.none { it.pressed }) break
                    }

                    if (mode == GestureMode.DRAW && !erasing) {
                        controller.commitStroke(points, drawTool)
                        liveStroke = emptyList()
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            }
            // S Pen hover: track the pen above the screen and clear on lift/exit.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        when (event.type) {
                            PointerEventType.Exit -> hover = null
                            PointerEventType.Enter,
                            PointerEventType.Move -> {
                                val c = event.changes.firstOrNull()
                                hover = if (c != null && !c.pressed &&
                                    (c.type == PointerType.Stylus || c.type == PointerType.Eraser)
                                ) c.position else null
                            }
                            else -> if (event.changes.any { it.pressed }) hover = null
                        }
                    }
                }
            }
    ) {
        // Vector rendering with a draw-time transform keeps ink crisp at any
        // zoom (scaling a rasterized cache would blur it). Reading
        // controller.revision and transform invalidates the draw when needed.
        Canvas(Modifier.fillMaxSize()) {
            @Suppress("UNUSED_EXPRESSION") controller.revision
            withTransform({
                translate(transform.offset.x, transform.offset.y)
                scale(transform.scale, transform.scale, pivot = Offset.Zero)
            }) {
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
            // Hover indicator (screen space): a ring at the pen tip, tinted with
            // the current ink color, so you can aim before touching down.
            hover?.let { h ->
                val isEraser = controller.tool == PenTool.ERASER
                val r = if (isEraser) eraserRadius
                else (controller.strokeWidth * transform.scale / 2f + 4.dp.toPx())
                drawCircle(
                    color = if (isEraser) Color(0xFF888888) else Color(controller.color),
                    radius = r,
                    center = h,
                    style = StrokeStyle(width = 1.5.dp.toPx()),
                )
            }
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
            // Fountain-pen nib: width follows both pressure and speed, so the
            // line swells on slow, firm strokes and tapers on quick ones.
            for (i in 1 until pts.size) {
                val a = pts[i - 1]
                val b = pts[i]
                val pressure = (a.pressure + b.pressure) * 0.5f
                val dist = NibWidth.distance(a.x, a.y, b.x, b.y)
                val w = NibWidth.of(stroke.baseWidth, pressure, dist)
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
