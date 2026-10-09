package com.audiocrawl.model

import java.util.Locale

/**
 * Represents an audio file detected on the device storage.
 */
data class AudioFile(
    val id: Long = 0,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val filePath: String,
    val fileName: String,
    val mimeType: String = "audio/*",
    val parentFolder: String = "",
    val isFromSdCard: Boolean = false,
    val isSelected: Boolean = true
) {
    /**
     * Formats duration in mm:ss or hh:mm:ss format.
     */
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "0:00"
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
            }
        }

    /**
     * Formats file size in KB or MB with 1 decimal place.
     */
    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 B"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                String.format(Locale.getDefault(), "%.1f MB", mb)
            } else {
                String.format(Locale.getDefault(), "%.0f KB", kb)
            }
        }
}
