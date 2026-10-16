package com.audiocrawl.util

import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.audiocrawl.model.AudioFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface DeletionRequestResult {
    data class PermissionRequired(val intentSender: IntentSender, val isTrash: Boolean) : DeletionRequestResult
    data class Completed(val count: Int, val isTrash: Boolean) : DeletionRequestResult
    data class Failed(val error: String) : DeletionRequestResult
}

@Singleton
class StorageDeletionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Prepares and initiates a batch deletion/trash request for the given original audio files.
     * On Android 11+ (API 30+): tries MediaStore.createTrashRequest first (system Recently Deleted/Trash).
     * If trash is not supported by OEM, falls back to MediaStore.createDeleteRequest.
     * On Android <= 29 or direct filesystem paths: performs direct deletion or handles RecoverableSecurityException.
     */
    suspend fun deleteOrTrashFiles(files: List<AudioFile>): DeletionRequestResult = withContext(Dispatchers.IO) {
        if (files.isEmpty()) {
            return@withContext DeletionRequestResult.Completed(0, false)
        }

        val contentResolver = context.contentResolver
        val mediaStoreUris = mutableListOf<Uri>()
        val nonMediaStoreFiles = mutableListOf<File>()

        for (file in files) {
            if (file.id > 0) {
                mediaStoreUris.add(
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, file.id)
                )
            } else if (file.filePath.isNotEmpty()) {
                nonMediaStoreFiles.add(File(file.filePath))
            }
        }

        // Delete any non-indexed filesystem files directly
        var directDeletedCount = 0
        for (f in nonMediaStoreFiles) {
            try {
                if (f.exists() && f.delete()) {
                    directDeletedCount++
                    MediaScannerConnection.scanFile(context, arrayOf(f.absolutePath), null, null)
                }
            } catch (e: Exception) {
                // Ignore failure on individual file
            }
        }

        if (mediaStoreUris.isEmpty()) {
            return@withContext DeletionRequestResult.Completed(directDeletedCount, false)
        }

        // Android 11+ (API 30+): Scoped Storage Trash / Delete request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 1. Try MediaStore.createTrashRequest (moves to Trash / Recently Deleted)
            try {
                val trashPendingIntent: PendingIntent = MediaStore.createTrashRequest(
                    contentResolver,
                    mediaStoreUris,
                    true
                )
                return@withContext DeletionRequestResult.PermissionRequired(
                    intentSender = trashPendingIntent.intentSender,
                    isTrash = true
                )
            } catch (e: Exception) {
                // Some OEMs or storage volumes don't support Trash. Fallback to createDeleteRequest
                try {
                    val deletePendingIntent: PendingIntent = MediaStore.createDeleteRequest(
                        contentResolver,
                        mediaStoreUris
                    )
                    return@withContext DeletionRequestResult.PermissionRequired(
                        intentSender = deletePendingIntent.intentSender,
                        isTrash = false
                    )
                } catch (deleteEx: Exception) {
                    return@withContext DeletionRequestResult.Failed(deleteEx.message ?: "Failed to create delete request")
                }
            }
        }

        // Android 10 (API 29) or legacy (API <= 28)
        var deletedCount = directDeletedCount
        for (uri in mediaStoreUris) {
            try {
                val rows = contentResolver.delete(uri, null, null)
                if (rows > 0) deletedCount += rows
            } catch (securityEx: SecurityException) {
                // On Android 10, check for RecoverableSecurityException
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    securityEx is android.app.RecoverableSecurityException
                ) {
                    return@withContext DeletionRequestResult.PermissionRequired(
                        intentSender = securityEx.userAction.actionIntent.intentSender,
                        isTrash = false
                    )
                }
            } catch (e: Exception) {
                // fallback to direct file delete if accessible
                val path = files.find { it.id == ContentUris.parseId(uri) }?.filePath
                if (!path.isNullOrEmpty()) {
                    val f = File(path)
                    if (f.exists() && f.delete()) {
                        deletedCount++
                        MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
                    }
                }
            }
        }

        return@withContext DeletionRequestResult.Completed(deletedCount, false)
    }
}
