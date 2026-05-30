package com.lumina.notes.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lumina.notes.LuminaApplication
import com.lumina.notes.di.AppContainer
import com.lumina.notes.ui.editor.EditorViewModel
import com.lumina.notes.ui.notes.NotesListViewModel
import com.lumina.notes.ui.settings.SettingsViewModel

@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as LuminaApplication).container

@Composable
fun notesListViewModel(): NotesListViewModel {
    val container = appContainer()
    return viewModel(factory = viewModelFactory {
        initializer {
            NotesListViewModel(container.notesRepository, container.settingsRepository)
        }
    })
}

@Composable
fun editorViewModel(noteId: String): EditorViewModel {
    val container = appContainer()
    return viewModel(key = "editor-$noteId", factory = viewModelFactory {
        initializer { EditorViewModel(container.notesRepository, noteId) }
    })
}

@Composable
fun settingsViewModel(): SettingsViewModel {
    val container = appContainer()
    return viewModel(factory = viewModelFactory {
        initializer { SettingsViewModel(container.settingsRepository) }
    })
}

/** Marker to keep imports tidy for ViewModel subclasses. */
internal typealias AppViewModel = ViewModel
