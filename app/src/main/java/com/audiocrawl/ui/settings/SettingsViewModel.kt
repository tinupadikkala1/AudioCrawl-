package com.audiocrawl.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.model.ScanConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val scanConfig: StateFlow<ScanConfig> = settingsRepository.scanConfigFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScanConfig()
    )

    fun addExcludedFolder(folder: String) {
        viewModelScope.launch {
            settingsRepository.addExcludedFolder(folder)
        }
    }

    fun removeExcludedFolder(folder: String) {
        viewModelScope.launch {
            settingsRepository.removeExcludedFolder(folder)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetExcludedFoldersToDefault()
        }
    }
}
