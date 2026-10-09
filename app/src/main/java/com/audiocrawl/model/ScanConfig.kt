package com.audiocrawl.model

/**
 * User-configurable settings for scanning audio files.
 */
data class ScanConfig(
    val includeSdCard: Boolean = true,
    val targetFolderName: String = DEFAULT_TARGET_FOLDER,
    val minDurationMs: Long = DEFAULT_MIN_DURATION_MS,
    val excludedFolders: Set<String> = DEFAULT_EXCLUDED_FOLDERS
) {
    companion object {
        const val DEFAULT_TARGET_FOLDER = "AudioCrawl_Songs"
        const val DEFAULT_MIN_DURATION_MS = 30_000L // 30 seconds

        /**
         * Smart default list of excluded directories across popular OEM recorders,
         * messaging apps, social platforms, and system sound directories.
         */
        val DEFAULT_EXCLUDED_FOLDERS: Set<String> = setOf(
            // Messaging apps
            "WhatsApp/Media/WhatsApp Voice Notes",
            "WhatsApp/Media/WhatsApp Audio",
            "WhatsApp Business/Media",
            "Android/media/com.whatsapp",
            "Telegram/Telegram Audio",
            "Android/data/org.telegram.messenger",
            "Android/media/com.facebook.orca",
            "Android/media/com.instagram.android",
            "Viber/media",
            "MicroMsg", // WeChat

            // Voice & call recording folders across OEMs
            "Recordings",
            "Recordings/Call",
            "Recordings/Voice Recorder",
            "Call",
            "CallRecordings",
            "Voice Recorder",
            "VoiceRecorder",
            "Sound Recorder",
            "Voice Memos",
            "PhoneRecord",
            "Record/PhoneRecord",
            "MIUI/sound_recorder",
            "MIUI/sound_recorder/call_rec",
            "Sounds",

            // Third-party call & voice recorder apps
            "ACR",
            "CubeACR",
            "Truecaller",
            "Easy Voice Recorder",
            "ASR",

            // System ringtones, alarms, notifications & camera audio
            "DCIM",
            "Ringtones",
            "Alarms",
            "Notifications",
            "Podcasts",
            "Audiobooks",

            // App private data & caches
            "Android/data",
            "Facebook",
            "Instagram",
            "TikTok",
            "Snapchat",
            ".thumbnails"
        )
    }
}
