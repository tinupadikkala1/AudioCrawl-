package com.audiocrawl.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.audiocrawl.AudioCrawlApp
import com.audiocrawl.MainActivity
import com.audiocrawl.R
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.model.AudioFile
import com.audiocrawl.model.ScanState
import com.audiocrawl.scanner.FileSystemScanner
import com.audiocrawl.scanner.MediaStoreScanner
import com.audiocrawl.scanner.ScanResultItem
import com.audiocrawl.util.StorageUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScanService : LifecycleService() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_SCAN = "com.audiocrawl.action.START_SCAN"
        const val ACTION_STOP_SCAN = "com.audiocrawl.action.STOP_SCAN"

        private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
        val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

        private val _scannedFiles = MutableStateFlow<List<AudioFile>>(emptyList())
        val scannedFiles: StateFlow<List<AudioFile>> = _scannedFiles.asStateFlow()

        fun startScan(context: Context) {
            val intent = Intent(context, ScanService::class.java).apply {
                action = ACTION_START_SCAN
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopScan(context: Context) {
            val intent = Intent(context, ScanService::class.java).apply {
                action = ACTION_STOP_SCAN
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var mediaStoreScanner: MediaStoreScanner

    @Inject
    lateinit var fileSystemScanner: FileSystemScanner

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_SCAN -> {
                startForegroundNotification()
                performScan()
            }
            ACTION_STOP_SCAN -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val notification = buildNotification("Scanning for music…", "Analyzing storage")
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

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, AudioCrawlApp.CHANNEL_SCAN)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun performScan() {
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            val foundFiles = mutableListOf<AudioFile>()
            val foundPaths = mutableSetOf<String>()
            var filteredCount = 0

            _scanState.value = ScanState.Scanning(currentFolder = "MediaStore Database", filesFound = 0, filesFiltered = 0)

            try {
                // 1. Scan primary MediaStore
                mediaStoreScanner.scanAudioFiles().collect { result ->
                    when (result) {
                        is ScanResultItem.Found -> {
                            if (foundPaths.add(result.audioFile.filePath)) {
                                foundFiles.add(result.audioFile)
                                _scanState.value = ScanState.Scanning(
                                    currentFolder = result.audioFile.parentFolder,
                                    filesFound = foundFiles.size,
                                    filesFiltered = filteredCount
                                )
                            }
                        }
                        is ScanResultItem.Filtered -> {
                            filteredCount++
                            _scanState.value = ScanState.Scanning(
                                currentFolder = "Filtered non-music items",
                                filesFound = foundFiles.size,
                                filesFiltered = filteredCount
                            )
                        }
                    }
                }

                // 2. Check SD cards if enabled
                val config = settingsRepository.getScanConfig()
                if (config.includeSdCard) {
                    val sdCards = StorageUtils.getRemovableStorageDirectories(this@ScanService)
                    for (sdCard in sdCards) {
                        _scanState.value = ScanState.Scanning(
                            currentFolder = sdCard.name,
                            filesFound = foundFiles.size,
                            filesFiltered = filteredCount,
                            isSdCardScanning = true
                        )
                        fileSystemScanner.scanDirectory(sdCard).collect { result ->
                            when (result) {
                                is ScanResultItem.Found -> {
                                    if (foundPaths.add(result.audioFile.filePath)) {
                                        foundFiles.add(result.audioFile)
                                        _scanState.value = ScanState.Scanning(
                                            currentFolder = result.audioFile.parentFolder,
                                            filesFound = foundFiles.size,
                                            filesFiltered = filteredCount,
                                            isSdCardScanning = true
                                        )
                                    }
                                }
                                is ScanResultItem.Filtered -> {
                                    filteredCount++
                                }
                            }
                        }
                    }
                }

                _scannedFiles.value = foundFiles
                val duration = System.currentTimeMillis() - startTime
                _scanState.value = ScanState.Complete(
                    totalFound = foundFiles.size,
                    totalFiltered = filteredCount,
                    durationMs = duration
                )
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Scanning failed")
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }
}
