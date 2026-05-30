package com.lumina.notes.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.ink.PaperStyle
import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.ui.ink.InkCanvas
import com.lumina.notes.util.TextStatsCalculator

private enum class EditorMode { TEXT, INK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    palmRejection: Boolean,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier,
) {
    val title by viewModel.title.collectAsState()
    val body by viewModel.body.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    var mode by remember { mutableStateOf(EditorMode.TEXT) }
    var showColorPicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                BasicTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (title.isEmpty()) {
                            Text(
                                "Titolo",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    },
                )
            },
            navigationIcon = {
                if (showBackButton) {
                    IconButton(onClick = { viewModel.discardIfEmpty(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            },
            actions = {
                IconButton(onClick = viewModel::togglePin) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Fissa",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Preferito",
                        tint = if (isFavorite) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showColorPicker = !showColorPicker }) {
                    Icon(Icons.Filled.Palette, contentDescription = "Colore")
                }
                IconButton(
                    onClick = {
                        val text = (title + "\n\n" + body).trim()
                        if (text.isNotEmpty()) {
                            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                                putExtra(android.content.Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(send, "Condividi nota")
                            )
                        }
                    }
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Condividi")
                }
                IconButton(onClick = { viewModel.deleteNote(); onDeleted() }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        AnimatedVisibility(visible = showColorPicker) {
            ColorSeedRow(
                selected = viewModel.colorSeed.collectAsState().value,
                onSelect = viewModel::setColorSeed,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        // Mode switch
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = mode == EditorMode.TEXT,
                onClick = { mode = EditorMode.TEXT },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                icon = { Icon(Icons.Filled.Notes, contentDescription = null) },
            ) { Text("Testo") }
            SegmentedButton(
                selected = mode == EditorMode.INK,
                onClick = { mode = EditorMode.INK },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                icon = { Icon(Icons.Filled.Draw, contentDescription = null) },
            ) { Text("Inchiostro") }
        }

        Box(Modifier.fillMaxSize()) {
            when (mode) {
                EditorMode.TEXT -> TextBody(
                    body = body,
                    onBodyChange = viewModel::onBodyChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                )
                EditorMode.INK -> InkArea(
                    viewModel = viewModel,
                    palmRejection = palmRejection,
                )
            }
        }
    }
}

@Composable
private fun TextBody(
    body: String,
    onBodyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val stats = remember(body) { TextStatsCalculator.of(body) }
    Box(modifier) {
        Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
            BasicTextField(
                value = body,
                onValueChange = onBodyChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                decorationBox = { inner ->
                    if (body.isEmpty()) {
                        Text(
                            "Inizia a scrivere…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
        }
        if (body.isNotBlank()) {
            Text(
                stats.summary(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun InkArea(
    viewModel: EditorViewModel,
    palmRejection: Boolean,
) {
    val paperOrdinal by viewModel.paperStyle.collectAsState()
    val paper = PaperStyle.fromOrdinal(paperOrdinal)
    var menuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        PaperBackground(style = paper, modifier = Modifier.fillMaxSize())
        InkCanvas(
            controller = viewModel.ink,
            palmRejection = palmRejection,
            modifier = Modifier.fillMaxSize(),
        )

        // Paper-style picker (top-start overlay).
        Box(Modifier.align(Alignment.TopStart).padding(8.dp)) {
            androidx.compose.material3.FilledTonalButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.GridOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(paper.label)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                PaperStyle.entries.forEach { style ->
                    DropdownMenuItem(
                        text = { Text(style.label) },
                        onClick = {
                            viewModel.setPaperStyle(style.ordinal)
                            menuOpen = false
                        },
                    )
                }
            }
        }

        val ctx = androidx.compose.ui.platform.LocalContext.current
        InkToolbar(
            controller = viewModel.ink,
            onExportPng = {
                val bmp = com.lumina.notes.util.InkExporter.renderToBitmap(
                    viewModel.ink.strokes.toList()
                )
                com.lumina.notes.util.InkExporter.share(ctx, bmp)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
        )
    }
}
