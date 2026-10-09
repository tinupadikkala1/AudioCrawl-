package com.audiocrawl.ui.results

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.model.AudioFile
import com.audiocrawl.player.AudioPlayerManager
import com.audiocrawl.player.PlayerState
import com.audiocrawl.service.CopyService
import com.audiocrawl.service.ScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val playerManager: AudioPlayerManager,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _targetFolderName = MutableStateFlow("AudioCrawl_Songs")
    val targetFolderName: StateFlow<String> = _targetFolderName.asStateFlow()

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    val scannedFiles: StateFlow<List<AudioFile>> = ScanService.scannedFiles

    init {
        // By default, select all detected songs
        viewModelScope.launch {
            ScanService.scannedFiles.collect { files ->
                if (_selectedIds.value.isEmpty() && files.isNotEmpty()) {
                    _selectedIds.value = files.map { it.id }.toSet()
                }
            }
        }
        viewModelScope.launch {
            val config = settingsRepository.getScanConfig()
            _targetFolderName.value = config.targetFolderName
        }
    }

    val filteredFiles: StateFlow<List<AudioFile>> = combine(
        scannedFiles,
        _searchQuery
    ) { files, query ->
        if (query.isBlank()) {
            files
        } else {
            val lowerQuery = query.lowercase().trim()
            files.filter { file ->
                file.title.lowercase().contains(lowerQuery) ||
                file.fileName.lowercase().contains(lowerQuery) ||
                (file.artist?.lowercase()?.contains(lowerQuery) == true) ||
                (file.album?.lowercase()?.contains(lowerQuery) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTargetFolderName(name: String) {
        _targetFolderName.value = name
    }

    fun toggleSelection(audioFile: AudioFile) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(audioFile.id)) {
            current.remove(audioFile.id)
        } else {
            current.add(audioFile.id)
        }
        _selectedIds.value = current
    }

    fun selectAll() {
        _selectedIds.value = scannedFiles.value.map { it.id }.toSet()
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
    }

    fun playPreview(audioFile: AudioFile) {
        if (playerState.value.currentSong?.id == audioFile.id) {
            playerManager.togglePlayPause()
        } else {
            playerManager.play(audioFile)
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun stopPreview() {
        playerManager.stop()
    }

    fun copySelectedFiles(): List<AudioFile> {
        val selected = scannedFiles.value.filter { _selectedIds.value.contains(it.id) }
        if (selected.isNotEmpty()) {
            playerManager.stop()
            CopyService.startCopy(context, selected, _targetFolderName.value)
        }
        return selected
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.stop()
    }
}
