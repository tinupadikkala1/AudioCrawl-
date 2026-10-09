package com.audiocrawl.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.model.AudioFile
import com.audiocrawl.util.StorageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filterEngine: FilterEngine,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Scans MediaStore for audio files, applying SQL pre-filtering and FilterEngine analysis.
     */
    fun scanAudioFiles(): Flow<ScanResultItem> = flow {
        val config = settingsRepository.getScanConfig()
        val contentUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
        }.toTypedArray()

        val selection = filterEngine.buildMediaStoreSelection()
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            contentUri,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val fileName = cursor.getString(nameCol) ?: ""
                val title = cursor.getString(titleCol) ?: fileName
                val artist = cursor.getString(artistCol).takeIf { it != "<unknown>" }
                val album = cursor.getString(albumCol).takeIf { it != "<unknown>" }
                val durationMs = cursor.getLong(durationCol)
                val sizeBytes = cursor.getLong(sizeCol)
                val mimeType = cursor.getString(mimeCol) ?: "audio/*"
                val filePath = cursor.getString(dataCol) ?: ""

                val parentFolder = if (filePath.isNotEmpty()) {
                    File(filePath).parent ?: ""
                } else ""

                val isOnSdCard = if (filePath.isNotEmpty()) {
                    StorageUtils.isPathOnSdCard(context, filePath)
                } else false

                // If user disabled SD card scanning and file is on SD card, skip it
                if (!config.includeSdCard && isOnSdCard) {
                    emit(ScanResultItem.Filtered(fileName))
                    continue
                }

                // Apply deep in-memory filter
                val isIncluded = filterEngine.evaluateFile(filePath, fileName, durationMs, config)
                if (isIncluded) {
                    val audioFile = AudioFile(
                        id = id,
                        title = title.ifBlank { fileName },
                        artist = artist,
                        album = album,
                        durationMs = durationMs,
                        sizeBytes = sizeBytes,
                        filePath = filePath,
                        fileName = fileName,
                        mimeType = mimeType,
                        parentFolder = parentFolder,
                        isFromSdCard = isOnSdCard
                    )
                    emit(ScanResultItem.Found(audioFile))
                } else {
                    emit(ScanResultItem.Filtered(fileName))
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}

sealed interface ScanResultItem {
    data class Found(val audioFile: AudioFile) : ScanResultItem
    data class Filtered(val fileName: String) : ScanResultItem
}
