package com.audiocrawl.service

import android.app.Notification
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.audiocrawl.AudioCrawlApp
import com.audiocrawl.MainActivity
import com.audiocrawl.R
import com.audiocrawl.model.AudioFile
import com.audiocrawl.model.CopyState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class CopyService : LifecycleService() {

    companion object {
        const val NOTIFICATION_ID = 1002
        const val ACTION_START_COPY = "com.audiocrawl.action.START_COPY"
        const val EXTRA_TARGET_FOLDER = "extra_target_folder"

        private val _copyState = MutableStateFlow<CopyState>(CopyState.Idle)
        val copyState: StateFlow<CopyState> = _copyState.asStateFlow()

        private var pendingFilesToCopy: List<AudioFile> = emptyList()

        fun startCopy(context: Context, filesToCopy: List<AudioFile>, targetFolder: String) {
            pendingFilesToCopy = filesToCopy
            val intent = Intent(context, CopyService::class.java).apply {
                action = ACTION_START_COPY
                putExtra(EXTRA_TARGET_FOLDER, targetFolder)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_START_COPY) {
            val targetFolder = intent.getStringExtra(EXTRA_TARGET_FOLDER) ?: "AudioCrawl_Songs"
            val files = pendingFilesToCopy
            startForegroundNotification(files.size)
            performCopy(files, targetFolder)
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification(total: Int) {
        val notification = buildNotification(0, total, "Preparing to copy songs…")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(current: Int, total: Int, fileName: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, AudioCrawlApp.CHANNEL_COPY)
            .setContentTitle("Copying Songs ($current/$total)")
            .setContentText(fileName)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(total, current, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(current: Int, total: Int, fileName: String) {
        val notification = buildNotification(current, total, fileName)
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission might be withheld on API 33+
        }
    }

    private fun performCopy(files: List<AudioFile>, targetFolderName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val total = files.size
            var copiedCount = 0

            _copyState.value = CopyState.Copying(0, total, "")

            try {
                for ((index, audioFile) in files.withIndex()) {
                    val current = index + 1
                    _copyState.value = CopyState.Copying(current, total, audioFile.fileName)
                    updateNotification(current, total, audioFile.fileName)

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            copyViaMediaStore(audioFile, targetFolderName)
                        } else {
                            copyViaFileSystem(audioFile, targetFolderName)
                        }
                        copiedCount++
                    } catch (e: Exception) {
                        // Log and continue with remaining files
                    }
                }

                val finalPath = "${Environment.DIRECTORY_MUSIC}/$targetFolderName"
                _copyState.value = CopyState.Complete(copiedCount, finalPath)
            } catch (e: Exception) {
                _copyState.value = CopyState.Error(e.message ?: "File copy failed")
            } finally {
                withContext(Dispatchers.Main) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun copyViaMediaStore(audioFile: AudioFile, folderName: String) {
        val relativePath = "${Environment.DIRECTORY_MUSIC}/$folderName/"
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, audioFile.fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, audioFile.mimeType)
            put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
            audioFile.artist?.let { put(MediaStore.Audio.Media.ARTIST, it) }
            audioFile.album?.let { put(MediaStore.Audio.Media.ALBUM, it) }
            put(MediaStore.Audio.Media.TITLE, audioFile.title)
        }

        val targetUri = contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IOException("Could not create MediaStore entry for ${audioFile.fileName}")

        try {
            val sourceUri = if (audioFile.id > 0) {
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioFile.id)
            } else {
                Uri.fromFile(File(audioFile.filePath))
            }

            contentResolver.openInputStream(sourceUri)?.use { input ->
                contentResolver.openOutputStream(targetUri)?.use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }

            // Mark write as complete so media scanner indexes it
            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            contentResolver.update(targetUri, contentValues, null, null)
        } catch (e: Exception) {
            contentResolver.delete(targetUri, null, null)
            throw e
        }
    }

    private fun copyViaFileSystem(audioFile: AudioFile, folderName: String) {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val targetDir = File(musicDir, folderName)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val sourceFile = File(audioFile.filePath)
        var targetFile = File(targetDir, audioFile.fileName)

        if (targetFile.exists()) {
            val baseName = audioFile.fileName.substringBeforeLast('.')
            val ext = audioFile.fileName.substringAfterLast('.', "")
            var counter = 1
            while (targetFile.exists()) {
                val newName = if (ext.isNotEmpty()) "${baseName}_($counter).$ext" else "${baseName}_($counter)"
                targetFile = File(targetDir, newName)
                counter++
            }
        }

        FileInputStream(sourceFile).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output, bufferSize = 64 * 1024)
            }
        }

        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(targetFile.absolutePath),
            arrayOf(audioFile.mimeType),
            null
        )
    }
}
