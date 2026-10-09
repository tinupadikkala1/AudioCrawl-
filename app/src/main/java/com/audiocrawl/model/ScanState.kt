package com.audiocrawl.model

/**
 * Lifecycle states during the filesystem & MediaStore scan process.
 */
sealed interface ScanState {
    data object Idle : ScanState

    data class Scanning(
        val currentFolder: String = "",
        val filesFound: Int = 0,
        val filesFiltered: Int = 0,
        val isSdCardScanning: Boolean = false
    ) : ScanState

    data class Complete(
        val totalFound: Int,
        val totalFiltered: Int,
        val durationMs: Long
    ) : ScanState

    data class Error(val message: String) : ScanState
}

/**
 * Lifecycle states during the batch copy process.
 */
sealed interface CopyState {
    data object Idle : CopyState

    data class Copying(
        val current: Int,
        val total: Int,
        val currentFileName: String,
        val progressPercentage: Int = if (total > 0) ((current * 100) / total) else 0
    ) : CopyState

    data class Complete(
        val copiedCount: Int,
        val targetPath: String
    ) : CopyState

    data class Error(val message: String) : CopyState
}
