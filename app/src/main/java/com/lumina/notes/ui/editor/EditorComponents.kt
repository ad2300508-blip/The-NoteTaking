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
import com.lumina.notes.ui.theme.NoteAccents

/** A faint dotted grid that gives the ink canvas a tactile "paper" feel. */
@Composable
fun PaperBackground(modifier: Modifier = Modifier) {
    val dotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val surface = MaterialTheme.colorScheme.surface
    Canvas(modifier.background(surface)) {
        val step = 36.dp.toPx()
        val radius = 1.4.dp.toPx()
        var y = step
        while (y < size.height) {
            var x = step
            while (x < size.width) {
                drawCircle(color = dotColor, radius = radius, center = Offset(x, y))
                x += step
            }
            y += step
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
