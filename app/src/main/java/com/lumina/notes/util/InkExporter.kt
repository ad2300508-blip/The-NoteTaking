package com.lumina.notes.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.data.ink.Stroke
import java.io.File
import java.io.FileOutputStream

/** Renders ink strokes to a PNG and shares it via a content URI. */
object InkExporter {

    private const val PADDING = 48f
    private const val MIN_SIZE = 256

    /** Computes the tight content bounds across all strokes, or null if empty. */
    fun contentBounds(strokes: List<Stroke>): FloatArray? {
        if (strokes.isEmpty()) return null
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var any = false
        for (s in strokes) {
            if (s.points.isEmpty()) continue
            any = true
            val b = s.bounds()
            if (b[0] < minX) minX = b[0]
            if (b[1] < minY) minY = b[1]
            if (b[2] > maxX) maxX = b[2]
            if (b[3] > maxY) maxY = b[3]
        }
        return if (any) floatArrayOf(minX, minY, maxX, maxY) else null
    }

    fun renderToBitmap(
        strokes: List<Stroke>,
        background: Int = Color.WHITE,
        backgroundImagePath: String? = null,
    ): Bitmap {
        val bounds = contentBounds(strokes)
        val width: Int
        val height: Int
        val offsetX: Float
        val offsetY: Float
        if (bounds == null) {
            width = MIN_SIZE; height = MIN_SIZE; offsetX = 0f; offsetY = 0f
        } else {
            width = ((bounds[2] - bounds[0]) + PADDING * 2).toInt().coerceAtLeast(MIN_SIZE)
            height = ((bounds[3] - bounds[1]) + PADDING * 2).toInt().coerceAtLeast(MIN_SIZE)
            offsetX = PADDING - bounds[0]
            offsetY = PADDING - bounds[1]
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(background)

        // Draw the annotated photo (fitted) under the ink, if any.
        if (!backgroundImagePath.isNullOrBlank()) {
            ImageDecoder.decodeSampled(backgroundImagePath, maxEdge = 3000)?.let { bg ->
                val scale = minOf(width.toFloat() / bg.width, height.toFloat() / bg.height)
                val dw = bg.width * scale
                val dh = bg.height * scale
                val left = (width - dw) / 2f
                val top = (height - dh) / 2f
                canvas.drawBitmap(
                    bg,
                    null,
                    android.graphics.RectF(left, top, left + dw, top + dh),
                    Paint(Paint.FILTER_BITMAP_FLAG),
                )
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (s in strokes) {
            if (s.points.size < 2) continue
            paint.color = colorFor(s)
            paint.strokeWidth = s.baseWidth
            for (i in 1 until s.points.size) {
                val a = s.points[i - 1]
                val b = s.points[i]
                canvas.drawLine(
                    a.x + offsetX, a.y + offsetY,
                    b.x + offsetX, b.y + offsetY,
                    paint,
                )
            }
        }
        return bitmap
    }

    private fun colorFor(stroke: Stroke): Int {
        val base = stroke.color.toInt()
        return if (stroke.tool == PenTool.HIGHLIGHTER) {
            (base and 0x00FFFFFF) or (0x4D shl 24) // ~30% alpha
        } else {
            base
        }
    }

    /** Saves [bitmap] to the share cache and launches a chooser. Returns false if it couldn't. */
    fun share(context: Context, bitmap: Bitmap, fileName: String = "lumina-nota.png"): Boolean {
        return runCatching {
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            shareFile(context, file, "image/png", "Condividi disegno")
            true
        }.getOrDefault(false)
    }

    /** Renders the strokes to a single-page PDF and shares it. */
    fun sharePdf(
        context: Context,
        strokes: List<Stroke>,
        fileName: String = "lumina-nota.pdf",
        backgroundImagePath: String? = null,
    ): Boolean {
        return runCatching {
            val bitmap = renderToBitmap(strokes, backgroundImagePath = backgroundImagePath)
            val pdf = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo
                .Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdf.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdf.finishPage(page)

            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out -> pdf.writeTo(out) }
            pdf.close()
            shareFile(context, file, "application/pdf", "Esporta PDF")
            true
        }.getOrDefault(false)
    }

    private fun shareFile(context: Context, file: File, mime: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
