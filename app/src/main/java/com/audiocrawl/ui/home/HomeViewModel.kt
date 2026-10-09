package com.audiocrawl.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.model.ScanConfig
import com.audiocrawl.service.ScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val scanConfig: StateFlow<ScanConfig> = settingsRepository.scanConfigFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScanConfig()
    )

    fun toggleIncludeSdCard(include: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateIncludeSdCard(include)
        }
    }

    fun updateMinDuration(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateMinDurationMs(seconds * 1000L)
        }
    }

    fun updateTargetFolderName(name: String) {
        viewModelScope.launch {
            settingsRepository.updateTargetFolderName(name)
        }
    }

    fun startScanning() {
        ScanService.startScan(context)
    }
}
