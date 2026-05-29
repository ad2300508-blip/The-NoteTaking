package com.lumina.notes.di

import android.content.Context
import com.lumina.notes.data.local.LuminaDatabase
import com.lumina.notes.data.repository.NotesRepository
import com.lumina.notes.data.settings.SettingsRepository

/**
 * Tiny manual service locator. Keeps the project free of annotation-processor
 * DI frameworks while still giving ViewModels their dependencies in one place.
 */
class AppContainer(context: Context) {
    private val database by lazy { LuminaDatabase.get(context) }
    val notesRepository by lazy { NotesRepository(database.noteDao()) }
    val settingsRepository by lazy { SettingsRepository(context) }
}
