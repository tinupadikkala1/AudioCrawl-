package com.audiocrawl.ui.copy

import androidx.lifecycle.ViewModel
import com.audiocrawl.model.CopyState
import com.audiocrawl.service.CopyService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CopyViewModel @Inject constructor() : ViewModel() {
    val copyState: StateFlow<CopyState> = CopyService.copyState
}
