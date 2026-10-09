package com.audiocrawl.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
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
class FileSystemScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filterEngine: FilterEngine,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Walks a filesystem directory tree recursively and yields genuine music tracks.
     * Useful for discovering files on removable SD cards or custom un-indexed folders.
     */
    fun scanDirectory(rootDir: File): Flow<ScanResultItem> = flow {
        if (!rootDir.exists() || !rootDir.canRead()) return@flow

        val config = settingsRepository.getScanConfig()
        val retriever = MediaMetadataRetriever()

        try {
            rootDir.walkTopDown()
                .onEnter { dir ->
                    // Fast skip excluded directories before descending into them
                    val normalizedPath = dir.absolutePath.replace('\\', '/').lowercase()
                    !config.excludedFolders.any { excluded ->
                        normalizedPath.contains(excluded.replace('\\', '/').lowercase())
                    }
                }
                .filter { it.isFile }
                .forEach { file ->
                    val ext = file.extension.lowercase()
                    if (ext !in filterEngine.audioExtensions || ext in filterEngine.voiceCodecs) {
                        return@forEach
                    }

                    var durationMs = 0L
                    var title: String? = null
                    var artist: String? = null
                    var album: String? = null
                    var mimeType = "audio/*"

                    try {
                        retriever.setDataSource(file.absolutePath)
                        durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                        mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "audio/*"
                    } catch (e: Exception) {
                        // File might be unreadable by retriever, fall back to basic file metadata
                    }

                    val isOnSdCard = StorageUtils.isPathOnSdCard(context, file.absolutePath)
                    if (!config.includeSdCard && isOnSdCard) {
                        emit(ScanResultItem.Filtered(file.name))
                        return@forEach
                    }

                    if (filterEngine.evaluateFile(file.absolutePath, file.name, durationMs, config)) {
                        val audioFile = AudioFile(
                            id = file.hashCode().toLong(),
                            title = title?.ifBlank { null } ?: file.nameWithoutExtension,
                            artist = artist?.takeIf { it != "<unknown>" },
                            album = album?.takeIf { it != "<unknown>" },
                            durationMs = durationMs,
                            sizeBytes = file.length(),
                            filePath = file.absolutePath,
                            fileName = file.name,
                            mimeType = mimeType,
                            parentFolder = file.parent ?: "",
                            isFromSdCard = isOnSdCard
                        )
                        emit(ScanResultItem.Found(audioFile))
                    } else {
                        emit(ScanResultItem.Filtered(file.name))
                    }
                }
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }.flowOn(Dispatchers.IO)
}
