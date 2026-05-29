package com.lumina.notes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.notes.data.settings.AppSettings
import com.lumina.notes.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val state: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDynamicColor(value: Boolean) = viewModelScope.launch { repo.setDynamicColor(value) }
    fun setDarkTheme(value: Boolean?) = viewModelScope.launch { repo.setDarkTheme(value) }
    fun setPalmRejection(value: Boolean) = viewModelScope.launch { repo.setPalmRejection(value) }
}
