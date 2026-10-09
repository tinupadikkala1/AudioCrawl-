package com.audiocrawl.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import java.io.File

/**
 * Utility functions for device storage detection including internal shared storage and SD cards.
 */
object StorageUtils {

    /**
     * Returns the primary external storage root directory.
     */
    fun getPrimaryStorageDirectory(): File {
        return Environment.getExternalStorageDirectory()
    }

    /**
     * Detects external removable storage directories (SD card) if present and mounted.
     */
    fun getRemovableStorageDirectories(context: Context): List<File> {
        val removableDirs = mutableListOf<File>()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
                storageManager?.storageVolumes?.forEach { volume ->
                    if (volume.isRemovable && volume.state == Environment.MEDIA_MOUNTED) {
                        volume.directory?.let { dir ->
                            removableDirs.add(dir)
                        }
                    }
                }
            } else {
                val externalFilesDirs = context.getExternalFilesDirs(null)
                for (dir in externalFilesDirs) {
                    if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                        // Extract root directory of the SD card (up to Android/data)
                        val path = dir.absolutePath
                        val androidIndex = path.indexOf("/Android")
                        if (androidIndex > 0) {
                            val rootPath = path.substring(0, androidIndex)
                            val rootFile = File(rootPath)
                            if (rootFile.exists() && rootFile.canRead() && !removableDirs.contains(rootFile)) {
                                removableDirs.add(rootFile)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Defensive fallback
        }

        return removableDirs
    }

    /**
     * Checks whether a file path resides on a removable SD card rather than primary internal storage.
     */
    fun isPathOnSdCard(context: Context, filePath: String): Boolean {
        val primaryPath = getPrimaryStorageDirectory().absolutePath
        if (filePath.startsWith(primaryPath)) {
            return false
        }
        val sdCards = getRemovableStorageDirectories(context)
        return sdCards.any { filePath.startsWith(it.absolutePath) }
    }
}
