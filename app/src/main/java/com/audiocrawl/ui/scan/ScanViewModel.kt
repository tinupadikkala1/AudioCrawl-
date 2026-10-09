package com.audiocrawl.ui.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import com.audiocrawl.model.ScanState
import com.audiocrawl.service.ScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val scanState: StateFlow<ScanState> = ScanService.scanState

    fun cancelScan() {
        ScanService.stopScan(context)
    }
}
