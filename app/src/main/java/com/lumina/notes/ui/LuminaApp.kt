package com.lumina.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.settings.AppSettings
import com.lumina.notes.ui.editor.EditorScreen
import com.lumina.notes.ui.notes.NotesListScreen
import com.lumina.notes.ui.settings.SettingsScreen

private sealed interface Detail {
    data object None : Detail
    data class Note(val id: String) : Detail
    data object Settings : Detail
}

@Composable
fun LuminaApp(settings: AppSettings, quickNote: Boolean = false) {
    val notesVm = notesListViewModel()

    var detail by rememberSaveable(
        stateSaver = androidx.compose.runtime.saveable.Saver(
            save = {
                when (it) {
                    is Detail.Note -> "note:${it.id}"
                    Detail.Settings -> "settings"
                    Detail.None -> "none"
                }
            },
            restore = {
                when {
                    it == "settings" -> Detail.Settings
                    it.startsWith("note:") -> Detail.Note(it.removePrefix("note:"))
                    else -> Detail.None
                }
            },
        ),
    ) { mutableStateOf<Detail>(Detail.None) }

    // S Pen "Create note" shortcut: spin up a fresh note exactly once.
    var quickNoteHandled by rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(quickNote) {
        if (quickNote && !quickNoteHandled) {
            quickNoteHandled = true
            notesVm.createNote { detail = Detail.Note(it) }
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val twoPane = maxWidth >= 840.dp
            val selectedId = (detail as? Detail.Note)?.id

            if (twoPane) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.width(if (maxWidth >= 1240.dp) 420.dp else 360.dp)) {
                        NotesListScreen(
                            viewModel = notesVm,
                            selectedNoteId = selectedId,
                            onOpenNote = { detail = Detail.Note(it) },
                            onOpenSettings = { detail = Detail.Settings },
                            columns = 1,
                        )
                    }
                    VerticalDivider()
                    Box(Modifier.weight(1f)) {
                        DetailPane(
                            detail = detail,
                            settings = settings,
                            showBackButton = false,
                            onClose = { detail = Detail.None },
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = detail,
                    transitionSpec = {
                        if (targetState is Detail.None) {
                            (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                        } else {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it / 4 } + fadeOut())
                        }
                    },
                    label = "detail",
                ) { target ->
                    when (target) {
                        Detail.None -> NotesListScreen(
                            viewModel = notesVm,
                            selectedNoteId = null,
                            onOpenNote = { detail = Detail.Note(it) },
                            onOpenSettings = { detail = Detail.Settings },
                            columns = if (maxWidth >= 600.dp) 2 else 1,
                        )
                        else -> {
                            BackHandler { detail = Detail.None }
                            DetailPane(
                                detail = target,
                                settings = settings,
                                showBackButton = true,
                                onClose = { detail = Detail.None },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPane(
    detail: Detail,
    settings: AppSettings,
    showBackButton: Boolean,
    onClose: () -> Unit,
) {
    when (detail) {
        is Detail.Note -> EditorScreen(
            viewModel = editorViewModel(detail.id),
            palmRejection = settings.palmRejection,
            onBack = onClose,
            onDeleted = onClose,
            showBackButton = showBackButton,
        )
        Detail.Settings -> SettingsScreen(
            viewModel = settingsViewModel(),
            onBack = onClose,
            showBackButton = showBackButton,
        )
        Detail.None -> EmptyDetail()
    }
}

@Composable
private fun EmptyDetail() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Filled.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Seleziona una nota o creane una nuova",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
