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
    private val settingsRepository: SettingsRepository? = null
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

        // ==========================================
        // 13. System-Generated, Audio Test & Benchmark Audio
        // ==========================================
        // Test audio specifications: "test-44100Hz-be-1ch-4bytes", "test_48000Hz_2ch_16bit.wav"
        add(Regex("""^test[-_].*""", RegexOption.IGNORE_CASE))
        add(Regex(""".*[-_]test\.[a-z0-9]+$""", RegexOption.IGNORE_CASE))
        add(Regex("""\b\d{4,6}\s*(?:hz|khz)\b""", RegexOption.IGNORE_CASE))
        add(Regex("""\b(?:1ch|2ch|4ch|6ch|8ch|mono|stereo|surround|5\.1ch|7\.1ch)\b""", RegexOption.IGNORE_CASE))
        add(Regex("""\b(?:\d+bytes?|\d+bits?|[1-4]byte|16bit|24bit|32bit|64bit)\b""", RegexOption.IGNORE_CASE))
        add(Regex("""\b(?:be|le|endian|bigendian|littleendian|pcm|raw|s16le|s16be|s24le|s24be)\b""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:sine|sweep|pink[-_]noise|white[-_]noise|test[-_]tone|testtone|soundcheck|latency[-_]test|loopback).*""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:cts|gts|vts|benchmark|diag|diagnostics|dummy|silence|silent)[-_].*""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:audio[-_]test|test[-_]audio|sample[-_]audio).*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 14. UI, Sound Effects (SFX), System & Game Audio
        // ==========================================
        add(Regex("""^(?:sfx|snd|sound[-_]?effect|effect|fx)[-_].*""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:ui|btn|button|click|tap|touch|keypress|beep|boop|ding|chime|pop|shutter|camera_click|alert|prompt|guidance)[-_].*""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:tts|speech|vocalizer|voice[-_]assistant)[-_].*""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:cache|temp|tmp|preview|snippet|chunk|seg)[-_].*""", RegexOption.IGNORE_CASE))
        add(Regex("""^(?:ad|rewarded)[-_].*""", RegexOption.IGNORE_CASE))

        // ==========================================
        // 15. Hash / UUID Cache Filenames (e.g. ad caches, temp buffers)
        // ==========================================
        // UUID filename: 3fa85f64-5717-4562-b3fc-2c963f66afa6.mp3
        add(Regex("""^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.[a-z0-9]+$""", RegexOption.IGNORE_CASE))
        // MD5/SHA hex-only filename: d41d8cd98f00b204e9800998ecf8427e.mp3 (32+ hex characters)
        add(Regex("""^[0-9a-f]{32,64}\.[a-z0-9]+$""", RegexOption.IGNORE_CASE))
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
     * Set of disallowed keywords in audio tags (e.g. system tones, diagnostic suites).
     */
    val disallowedMetadataKeywords: Set<String> = setOf(
        "test", "test audio", "audio test", "sample", "sound effect", "sound effects",
        "sfx", "ringtone", "notification", "alarm", "system sound", "benchmark",
        "voice memo", "call recording", "touch sound", "shutter"
    )

    /**
     * In-memory secondary evaluation pipeline.
     * Evaluates whether an audio file is genuine music or should be filtered out.
     */
    suspend fun shouldInclude(
        filePath: String,
        fileName: String,
        durationMs: Long,
        mimeType: String? = null,
        sizeBytes: Long = 0L,
        title: String? = null,
        artist: String? = null,
        album: String? = null
    ): Boolean {
        val config = settingsRepository?.getScanConfig() ?: ScanConfig()
        return evaluateFile(
            filePath = filePath,
            fileName = fileName,
            durationMs = durationMs,
            config = config,
            sizeBytes = sizeBytes,
            title = title,
            artist = artist,
            album = album
        )
    }

    /**
     * Synchronous evaluation against a provided [ScanConfig] (useful for tests and bulk processing).
     */
    fun evaluateFile(
        filePath: String,
        fileName: String,
        durationMs: Long,
        config: ScanConfig,
        sizeBytes: Long = 0L,
        title: String? = null,
        artist: String? = null,
        album: String? = null
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

        // 3. Hidden file or hidden directory check
        if (fileName.startsWith(".")) {
            return false
        }
        val normalizedPath = filePath.replace('\\', '/').lowercase()
        val pathSegments = normalizedPath.split('/')
        if (pathSegments.any { it.startsWith(".") && it.isNotEmpty() && it != "." && it != ".." }) {
            return false
        }

        // 4. Excluded folder check
        for (excludedFolder in config.excludedFolders) {
            val normalizedExcluded = excludedFolder.replace('\\', '/').lowercase()
            if (normalizedPath.contains(normalizedExcluded)) {
                return false
            }
        }

        // 5. Strict Minimum duration check
        // Genuine songs must have a known duration of at least config.minDurationMs (default 30s)
        if (durationMs < config.minDurationMs) {
            return false
        }

        // 6. Minimum file size check (valid music tracks are not tiny sub-200KB stubs/tones)
        if (sizeBytes in 1 until 200_000L) {
            return false
        }

        // 7. Filename regex pattern check
        for (pattern in excludedFilePatterns) {
            if (pattern.containsMatchIn(fileName)) {
                return false
            }
        }

        // 8. Disallowed metadata check (filters out explicit test / system audio tags)
        val tagsToCheck = listOfNotNull(title, artist, album).map { it.trim().lowercase() }
        for (tag in tagsToCheck) {
            if (tag.isEmpty()) continue
            for (keyword in disallowedMetadataKeywords) {
                if (tag == keyword || tag.startsWith("$keyword ") || tag.startsWith("$keyword-") || tag.startsWith("${keyword}_")) {
                    return false
                }
            }
        }

        return true
    }
}
