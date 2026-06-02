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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.ui.ink.InkController
import com.lumina.notes.ui.theme.InkPalette
import com.lumina.notes.util.ColorHsv

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun InkToolbar(
    controller: InkController,
    onExportPng: () -> Unit,
    onRecognizeText: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    fun tap() = haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    var hue by remember { mutableFloatStateOf(210f) }
    var confirmClear by remember { mutableStateOf(false) }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { androidx.compose.material3.Text("Cancellare il disegno?") },
            text = { androidx.compose.material3.Text("Tutti i tratti verranno rimossi. Puoi comunque annullare con la freccia indietro.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    controller.clear(); confirmClear = false
                }) { androidx.compose.material3.Text("Cancella") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmClear = false }) {
                    androidx.compose.material3.Text("Annulla")
                }
            },
        )
    }

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
                ToolButton(Icons.Filled.Gesture, "Seleziona", controller.tool == PenTool.LASSO) {
                    tap(); controller.selectTool(PenTool.LASSO)
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
                IconButton(
                    onClick = {
                        val recognized = controller.straightenLastStroke()
                        haptics.performHapticFeedback(
                            if (recognized != null) HapticFeedbackType.LongPress
                            else HapticFeedbackType.TextHandleMove
                        )
                    },
                    enabled = controller.strokes.isNotEmpty(),
                ) {
                    Icon(Icons.Filled.AutoFixHigh, contentDescription = "Raddrizza forma")
                }
                IconButton(onClick = { confirmClear = true }, enabled = controller.canUndo) {
                    Icon(Icons.Filled.Delete, contentDescription = "Cancella tutto")
                }
                if (controller.hasSelection) {
                    IconButton(onClick = { controller.deleteSelected() }) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Elimina selezione",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                IconButton(onClick = { tap(); onRecognizeText() }, enabled = controller.canUndo) {
                    Icon(Icons.Filled.TextFields, contentDescription = "Riconosci testo")
                }
                IconButton(onClick = onExportPng, enabled = controller.canUndo) {
                    Icon(Icons.Filled.Share, contentDescription = "Condividi disegno")
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
                Spacer(Modifier.width(12.dp))
                // Quick width presets for switching nib while writing.
                listOf("Fine" to 2.5f, "Medio" to 6f, "Largo" to 12f).forEach { (label, w) ->
                    val on = kotlin.math.abs(controller.strokeWidth - w) < 0.6f
                    androidx.compose.material3.FilterChip(
                        selected = on,
                        onClick = { tap(); controller.setWidth(w) },
                        label = { androidx.compose.material3.Text(label) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }

            // Custom color: a hue slider feeds a live preview swatch.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text2("Colore")
                Slider(
                    value = hue,
                    onValueChange = {
                        hue = it
                        val c = ColorHsv.toArgb(it, 0.9f, 0.9f)
                        controller.selectColor(c)
                        if (controller.tool == PenTool.ERASER) controller.selectTool(PenTool.PEN)
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(220.dp),
                )
                Box(
                    Modifier
                        .size(24.dp)
                        .background(Color(ColorHsv.toArgb(hue, 0.9f, 0.9f)), CircleShape)
                )
            }

            // Recently used pens, for one-tap re-selection.
            if (controller.recentColors.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text2("Recenti")
                    Spacer(Modifier.width(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        controller.recentColors.forEach { c ->
                            ColorSwatch(
                                color = Color(c),
                                selected = controller.color == c && controller.tool != PenTool.ERASER,
                                onClick = {
                                    tap()
                                    controller.selectColor(c)
                                    if (controller.tool == PenTool.ERASER) controller.selectTool(PenTool.PEN)
                                },
                            )
                        }
                    }
                }
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
