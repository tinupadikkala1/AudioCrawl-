package com.audiocrawl.ui.copy

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiocrawl.model.AudioFile
import com.audiocrawl.model.CopyState
import com.audiocrawl.model.DeleteState
import com.audiocrawl.service.CopyService
import com.audiocrawl.util.DeletionRequestResult
import com.audiocrawl.util.StorageDeletionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CopyViewModel @Inject constructor(
    private val deletionHelper: StorageDeletionHelper
) : ViewModel() {

    val copyState: StateFlow<CopyState> = CopyService.copyState

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    private var pendingDeleteCount = 0
    private var pendingIsTrash = false

    fun requestDeleteOriginals(
        files: List<AudioFile>,
        onLaunchIntentSender: (IntentSender) -> Unit
    ) {
        if (files.isEmpty()) return

        _deleteState.value = DeleteState.InProgress
        pendingDeleteCount = files.size

        viewModelScope.launch {
            when (val result = deletionHelper.deleteOrTrashFiles(files)) {
                is DeletionRequestResult.PermissionRequired -> {
                    pendingIsTrash = result.isTrash
                    onLaunchIntentSender(result.intentSender)
                }
                is DeletionRequestResult.Completed -> {
                    _deleteState.value = DeleteState.Success(
                        deletedCount = result.count,
                        isTrashed = result.isTrash
                    )
                }
                is DeletionRequestResult.Failed -> {
                    _deleteState.value = DeleteState.Error(result.error)
                }
            }
        }
    }

    fun onIntentSenderResult(success: Boolean) {
        if (success) {
            _deleteState.value = DeleteState.Success(
                deletedCount = pendingDeleteCount,
                isTrashed = pendingIsTrash
            )
        } else {
            // User cancelled the system dialog
            _deleteState.value = DeleteState.Idle
        }
    }
}
