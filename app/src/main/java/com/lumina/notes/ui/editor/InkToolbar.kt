package com.lumina.notes.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.ui.ink.InkController
import com.lumina.notes.ui.theme.InkPalette

@Composable
fun InkToolbar(
    controller: InkController,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    fun tap() = haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToolButton(Icons.Filled.Edit, "Penna", controller.tool == PenTool.PEN) {
                    tap(); controller.selectTool(PenTool.PEN)
                }
                ToolButton(Icons.Filled.Highlight, "Evidenziatore", controller.tool == PenTool.HIGHLIGHTER) {
                    tap(); controller.selectTool(PenTool.HIGHLIGHTER)
                }
                ToolButton(Icons.Filled.Brush, "Gomma", controller.tool == PenTool.ERASER) {
                    tap(); controller.selectTool(PenTool.ERASER)
                }

                Spacer(Modifier.width(8.dp))

                // Color swatches
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InkPalette.forEach { c ->
                        ColorSwatch(
                            color = Color(c),
                            selected = controller.color == c && controller.tool != PenTool.ERASER,
                            onClick = {
                                controller.selectColor(c)
                                if (controller.tool == PenTool.ERASER) controller.selectTool(PenTool.PEN)
                            },
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                IconButton(onClick = controller::undo, enabled = controller.canUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Annulla")
                }
                IconButton(onClick = controller::redo, enabled = controller.canRedo) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Ripeti")
                }
                IconButton(onClick = controller::clear, enabled = controller.canUndo) {
                    Icon(Icons.Filled.Delete, contentDescription = "Cancella tutto")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text2("Spessore")
                Slider(
                    value = controller.strokeWidth,
                    onValueChange = controller::setWidth,
                    valueRange = 1.5f..18f,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(220.dp),
                )
                Box(
                    Modifier
                        .size((6 + controller.strokeWidth).dp)
                        .background(
                            if (controller.tool == PenTool.ERASER) MaterialTheme.colorScheme.outline
                            else Color(controller.color),
                            CircleShape,
                        )
                )
            }
        }
    }
}

@Composable
private fun Text2(text: String) {
    androidx.compose.material3.Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "toolbg",
    )
    Box(
        Modifier
            .padding(2.dp)
            .size(44.dp)
            .background(bg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(if (selected) 30.dp else 26.dp)
            .background(color, CircleShape)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}
