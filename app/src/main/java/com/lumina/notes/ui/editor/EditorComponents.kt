package com.lumina.notes.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.ink.PaperStyle
import com.lumina.notes.ui.theme.NoteAccents

/** A faint ruling that gives the ink canvas a tactile "paper" feel. */
@Composable
fun PaperBackground(
    style: PaperStyle,
    modifier: Modifier = Modifier,
    spacingDp: Float = 36f,
) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val marginColor = Color(0xFFE57373).copy(alpha = 0.5f)
    val surface = MaterialTheme.colorScheme.surface
    Canvas(modifier.background(surface)) {
        if (style == PaperStyle.PLAIN) return@Canvas
        val step = spacingDp.dp.toPx()
        when (style) {
            PaperStyle.DOTS -> {
                val radius = 1.4.dp.toPx()
                var y = step
                while (y < size.height) {
                    var x = step
                    while (x < size.width) {
                        drawCircle(color = lineColor, radius = radius, center = Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }
            PaperStyle.GRID -> {
                val w = 1.dp.toPx()
                var x = step
                while (x < size.width) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), w)
                    x += step
                }
                var y = step
                while (y < size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), w)
                    y += step
                }
            }
            PaperStyle.LINES -> {
                val w = 1.dp.toPx()
                var y = step
                while (y < size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), w)
                    y += step
                }
                // Classic red margin rule down the left side of the page.
                val marginX = 48.dp.toPx()
                drawLine(
                    color = marginColor,
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, size.height),
                    strokeWidth = 1.5.dp.toPx(),
                )
            }
            PaperStyle.PLAIN -> Unit
        }
    }
}

/** Horizontal row of accent colors for tinting a note. */
@Composable
fun ColorSeedRow(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NoteAccents.forEachIndexed { index, color ->
            val isSel = index == selected
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(34.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (isSel) 3.dp else 1.dp,
                        color = if (isSel) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSel) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
