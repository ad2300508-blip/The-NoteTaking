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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
    fontScale: Float = 1f,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier,
) {
    val title by viewModel.title.collectAsState()
    val body by viewModel.body.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val lastMode by viewModel.lastMode.collectAsState()

    var mode by remember { mutableStateOf(EditorMode.TEXT) }
    // Restore the remembered editor mode once the note has loaded.
    androidx.compose.runtime.LaunchedEffect(lastMode) {
        mode = if (lastMode == 1) EditorMode.INK else EditorMode.TEXT
    }
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

        TagEditor(
            tags = viewModel.tags.collectAsState().value,
            onAdd = viewModel::addTag,
            onRemove = viewModel::removeTag,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        // Mode switch
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Handwriting-first: ink is the primary mode, typing is secondary.
            SegmentedButton(
                selected = mode == EditorMode.INK,
                onClick = { mode = EditorMode.INK; viewModel.setLastMode(1) },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                icon = { Icon(Icons.Filled.Draw, contentDescription = null) },
            ) { Text("Scrittura") }
            SegmentedButton(
                selected = mode == EditorMode.TEXT,
                onClick = { mode = EditorMode.TEXT; viewModel.setLastMode(0) },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                icon = { Icon(Icons.Filled.Notes, contentDescription = null) },
            ) { Text("Tastiera") }
        }

        Box(Modifier.fillMaxSize()) {
            when (mode) {
                EditorMode.TEXT -> TextBody(
                    body = body,
                    onBodyChange = viewModel::onBodyChange,
                    fontScale = fontScale,
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
    fontScale: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val stats = remember(body) { TextStatsCalculator.of(body) }
    val base = MaterialTheme.typography.bodyLarge
    val scaled = base.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = base.fontSize * fontScale,
        lineHeight = base.lineHeight * fontScale,
    )
    Box(modifier) {
        Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
            BasicTextField(
                value = body,
                onValueChange = onBodyChange,
                textStyle = scaled,
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
    var zoom by remember { mutableFloatStateOf(1f) }
    var resetSignal by remember { mutableIntStateOf(0) }
    var page by remember { mutableIntStateOf(1) }
    var pageCount by remember { mutableIntStateOf(1) }
    var recognizing by remember { mutableStateOf(false) }

    // On-device handwriting recognition (ML Kit), tied to this screen.
    val recognizer = remember { com.lumina.notes.spen.HandwritingRecognizer("it") }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { recognizer.close() } }

    // S Pen Air Actions: the side button toggles pen/eraser; a double click
    // switches to the highlighter. No-op on devices without the S Pen SDK.
    val appCtx = androidx.compose.ui.platform.LocalContext.current.applicationContext
    androidx.compose.runtime.DisposableEffect(Unit) {
        val air = com.lumina.notes.spen.SpenAirActions(appCtx)
        air.onButtonClick = {
            viewModel.ink.selectTool(
                if (viewModel.ink.tool == com.lumina.notes.data.ink.PenTool.ERASER)
                    com.lumina.notes.data.ink.PenTool.PEN
                else com.lumina.notes.data.ink.PenTool.ERASER
            )
        }
        air.onButtonDouble = {
            viewModel.ink.selectTool(com.lumina.notes.data.ink.PenTool.HIGHLIGHTER)
        }
        air.connect()
        onDispose { air.disconnect() }
    }

    Box(Modifier.fillMaxSize()) {
        PaperBackground(style = paper, modifier = Modifier.fillMaxSize())
        InkCanvas(
            controller = viewModel.ink,
            palmRejection = palmRejection,
            modifier = Modifier.fillMaxSize(),
            resetZoomSignal = resetSignal,
            onZoomChange = { zoom = it },
            onPageInfo = { current, total -> page = current; pageCount = total },
        )

        // Zoom badge + reset, shown only when the canvas is zoomed.
        if (kotlin.math.abs(zoom - 1f) > 0.02f) {
            androidx.compose.material3.AssistChip(
                onClick = { resetSignal++ },
                label = { Text("${(zoom * 100).toInt()}%  ·  1:1") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }

        // Page indicator, shown once the note spans more than one page.
        if (pageCount > 1) {
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 96.dp),
            ) {
                Text(
                    "Pag. $page/$pageCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

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
            onRecognizeText = {
                if (!recognizing) {
                    recognizing = true
                    recognizer.recognize(viewModel.ink.strokes.toList()) { text ->
                        recognizing = false
                        if (!text.isNullOrBlank()) viewModel.appendRecognizedText(text)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
        )

        if (recognizing) {
            androidx.compose.material3.AssistChip(
                onClick = {},
                label = { Text("Riconoscimento…") },
                modifier = Modifier.align(Alignment.TopCenter).padding(8.dp),
            )
        }
    }
}
