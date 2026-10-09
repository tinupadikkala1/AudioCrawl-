package com.audiocrawl

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AudioCrawlApp : Application() {

    companion object {
        const val CHANNEL_SCAN = "audiocrawl_scan_channel"
        const val CHANNEL_COPY = "audiocrawl_copy_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val scanChannel = NotificationChannel(
                CHANNEL_SCAN,
                "Audio Scanning",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress while scanning for songs across storage"
            }

            val copyChannel = NotificationChannel(
                CHANNEL_COPY,
                "Audio Copying",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress while copying songs to destination folder"
            }

            notificationManager?.createNotificationChannel(scanChannel)
            notificationManager?.createNotificationChannel(copyChannel)
        }
    }
}
