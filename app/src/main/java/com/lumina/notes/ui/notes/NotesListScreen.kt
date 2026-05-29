package com.lumina.notes.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.local.NoteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    selectedNoteId: String?,
    onOpenNote: (String) -> Unit,
    onOpenSettings: () -> Unit,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Lumina",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Impostazioni")
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cerca tra le note…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancella")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )
            }

            if (!state.loading && state.notes.isEmpty()) {
                EmptyState(query = state.query, Modifier.fillMaxSize())
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columns),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            selected = note.id == selectedNoteId,
                            onClick = { onOpenNote(note.id) },
                            onLongClick = { viewModel.togglePin(note) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { viewModel.createNote(onOpenNote) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuova nota") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        )
    }
}

@Composable
private fun EmptyState(query: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(72.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (query.isBlank()) "Nessuna nota" else "Nessun risultato",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (query.isBlank()) "Tocca + o usa la S Pen per iniziare a scrivere"
                else "Prova con un altro termine di ricerca",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Convenience for callers that already hold a note instance. */
fun NoteEntity.matches(query: String): Boolean =
    query.isBlank() || title.contains(query, true) || body.contains(query, true)
