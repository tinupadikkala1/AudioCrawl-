package com.audiocrawl.scanner

import android.os.Build
import android.provider.MediaStore
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.model.ScanConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FilterEngine is the brain of AudioCrawl.
 * It applies multi-stage filtering to separate genuine music tracks
 * from voice notes, call recordings, app audio snippets, and system sounds.
 */
@Singleton
class FilterEngine @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    /**
     * Whitelist of music-grade audio container and codec file extensions.
     * Note: Speech-only codecs like AMR and 3GP are strictly omitted.
     */
    val audioExtensions: Set<String> = setOf(
        "mp3", "flac", "wav", "aac", "ogg", "oga",
        "m4a", "wma", "opus", "aiff", "aif", "alac",
        "ape", "dsf", "dff", "mka"
    )

    /**
     * Known voice-only codecs that should always be rejected.
     */
    val voiceCodecs: Set<String> = setOf("amr", "3gp", "3gpp", "awb")

    /**
     * Comprehensive database of filename regex patterns for voice recorders,
     * call recorders, messaging apps, and social apps across OEMs.
     */
    val excludedFilePatterns: List<Regex> = buildList {

        // ==========================================
        // 1. WhatsApp Voice Notes & Shared Audio
        // ==========================================
        // Voice notes: PTT-20261009-WA0003.opus
        add(Regex("""^PTT-\d{8}-WA\d+.*""", RegexOption.IGNORE_CASE))
        // Shared audio: AUD-20261009-WA0015.mp3 / AUD-20261009-WA0015.opus
        add(Regex("""^AUD-\d{8}-WA\d+.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 2. Telegram Voice Messages
        // ==========================================
        // Voice message: audio_2026-10-09_14-30-22.ogg
        add(Regex("""^audio_\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2}\.ogg$""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 3. Facebook Messenger & Instagram
        // ==========================================
        // FB Messenger: audioclip-1234567890-1234.m4a
        add(Regex("""^audioclip-\d+-\d+.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 4. Samsung Voice & Call Recorders
        // ==========================================
        // Samsung Voice Recorder: "Voice 001.m4a", "Voice 042.m4a"
        add(Regex("""^Voice\s+\d{3,}\.(m4a|mp3)$""", RegexOption.IGNORE_CASE))
        // Samsung Call Recording: "Call recording_John_202610091430.m4a", "Call recording_0123456789_..."
        add(Regex("""^Call\s+recording[_\s].*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 5. Xiaomi / MIUI / HyperOS Recorders
        // ==========================================
        // Sound Recorder: "sound_recorder_20261009_143022.mp3"
        add(Regex("""^sound_recorder[_-]\d+.*""", RegexOption.IGNORE_CASE))
        // Call Recording: "call_20261009_143022_9876543210.mp3"
        add(Regex("""^call_\d{8}_\d{6}.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 6. OnePlus / Oppo / Realme / ColorOS
        // ==========================================
        // OnePlus Recorder: "Record_2026-10-09-14-30-22.mp3"
        add(Regex("""^Record_\d{4}-\d{2}-\d{2}.*""", RegexOption.IGNORE_CASE))
        // ColorOS Recorder: "REC_20261009_143022.mp3"
        add(Regex("""^REC_\d{8}_\d{6}.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 7. Google Pixel & Stock AOSP
        // ==========================================
        // Pixel Recorder: "Recording_00042.m4a", "Recording_1.m4a"
        add(Regex("""^Recording_\d+.*""", RegexOption.IGNORE_CASE))
        // AOSP Voice Recorder: "New Recording 1.m4a", "New Recording 5.m4a"
        add(Regex("""^New\s+Recording\s+\d+.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 8. Huawei / Honor Recorders
        // ==========================================
        // Voice Recorder: "record_20261009_143022.m4a"
        add(Regex("""^record_\d{8}_\d{6}.*""", RegexOption.IGNORE_CASE))
        // Call Recording: "CallRecord-20261009_143022.mp3"
        add(Regex("""^CallRecord[_-]\d+.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 9. Vivo Recorders
        // ==========================================
        // Vivo Recorder: "Recording_20261009143022.amr" / ".mp3"
        add(Regex("""^Recording_\d{14}.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 10. Sony, Motorola, LG & Generic OEM
        // ==========================================
        // Motorola: "recording-20261009-143022.m4a"
        add(Regex("""^recording-\d{8}-\d{6}.*""", RegexOption.IGNORE_CASE))
        // LG: "Recorded_20261009.3gp"
        add(Regex("""^Recorded_\d+.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 11. Third-Party Call & Sound Recorders
        // ==========================================
        // ACR Call Recorder: "ACR_20261009_143022_9876.mp3"
        add(Regex("""^ACR[_-]\d+.*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 12. Generic Catch-All Recording Patterns
        // ==========================================
        // "VN001.mp3", "VN_20261009.m4a" (Voice Notes)
        add(Regex("""^VN[_-]?\d+.*""", RegexOption.IGNORE_CASE))
        // "voice_001.mp3", "voice-01.m4a", "voice 1.mp3"
        add(Regex("""^voice[_\s-]\d+.*""", RegexOption.IGNORE_CASE))
        // "call_001.mp3", "call-recording-01.mp3"
        add(Regex("""^call[_\s@-]recording.*""", RegexOption.IGNORE_CASE))
        add(Regex("""^call[_\s@-]\d+.*""", RegexOption.IGNORE_CASE))
        // "memo_001.m4a", "memo-01.mp3"
        add(Regex("""^memo[_\s-]\d+.*""", RegexOption.IGNORE_CASE))
        // "rec_001.mp3", "rec-01.wav"
        add(Regex("""^rec[_-]\d+.*""", RegexOption.IGNORE_CASE))
        // Raw timestamp filenames: "20261009_143022.mp3", "20261009-143022.m4a"
        add(Regex("""^\d{8}[_-]\d{6}\.(m4a|wav|mp3|ogg|aac|opus)$""", RegexOption.IGNORE_CASE))
    }

    /**
     * Builds the SQL selection query for MediaStore queries.
     * Evaluates server-side inside MediaStore SQLite database for ultra-fast queries.
     */
    fun buildMediaStoreSelection(): String {
        return buildString {
            append("${MediaStore.Audio.Media.IS_MUSIC} != 0")
            append(" AND ${MediaStore.Audio.Media.IS_ALARM} == 0")
            append(" AND ${MediaStore.Audio.Media.IS_NOTIFICATION} == 0")
            append(" AND ${MediaStore.Audio.Media.IS_RINGTONE} == 0")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                append(" AND ${MediaStore.Audio.Media.IS_PENDING} == 0")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                append(" AND ${MediaStore.Audio.AudioColumns.IS_RECORDING} == 0")
            }
        }
    }

    /**
     * In-memory secondary evaluation pipeline.
     * Evaluates whether an audio file is genuine music or should be filtered out.
     */
    suspend fun shouldInclude(
        filePath: String,
        fileName: String,
        durationMs: Long,
        mimeType: String? = null
    ): Boolean {
        val config = settingsRepository.getScanConfig()
        return evaluateFile(filePath, fileName, durationMs, config)
    }

    /**
     * Synchronous evaluation against a provided [ScanConfig] (useful for tests and bulk processing).
     */
    fun evaluateFile(
        filePath: String,
        fileName: String,
        durationMs: Long,
        config: ScanConfig
    ): Boolean {
        // 1. Extension check
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isEmpty() || extension !in audioExtensions) {
            return false
        }

        // 2. Reject known voice codecs (AMR, 3GP, etc.)
        if (extension in voiceCodecs) {
            return false
        }

        // 3. Excluded folder check
        val normalizedPath = filePath.replace('\\', '/').lowercase()
        for (excludedFolder in config.excludedFolders) {
            val normalizedExcluded = excludedFolder.replace('\\', '/').lowercase()
            if (normalizedPath.contains(normalizedExcluded)) {
                return false
            }
        }

        // 4. Minimum duration check (skip short notifications, clicks, voice snippets)
        // If duration is reported (>0) and below threshold, exclude
        if (durationMs in 1 until config.minDurationMs) {
            return false
        }

        // 5. Filename regex pattern check
        for (pattern in excludedFilePatterns) {
            if (pattern.containsMatchIn(fileName)) {
                return false
            }
        }

        return true
    }
}
