package com.audiocrawl.scanner

import com.audiocrawl.model.ScanConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FilterEngineTest {

    private lateinit var filterEngine: FilterEngine
    private val testConfig = ScanConfig()

    @Before
    fun setUp() {
        // Mock-free instance: FilterEngine.evaluateFile only needs ScanConfig
        filterEngine = FilterEngine(
            settingsRepository = object : Any() {} as com.audiocrawl.data.SettingsRepository
        )
    }

    // ====================================================
    // Genuine Music Tracks Tests (Should all be INCLUDED)
    // ====================================================

    @Test
    fun shouldInclude_genuineMusicTracks() {
        val genuineSongs = listOf(
            Triple("/storage/emulated/0/Music/Queen - Bohemian Rhapsody.mp3", "Queen - Bohemian Rhapsody.mp3", 354_000L),
            Triple("/storage/emulated/0/Download/Coldplay - Yellow.flac", "Coldplay - Yellow.flac", 269_000L),
            Triple("/storage/emulated/0/Music/Taylor Swift/Blank Space.m4a", "Blank Space.m4a", 231_000L),
            Triple("/storage/emulated/0/Music/01 - Track 1.wav", "01 - Track 1.wav", 180_000L),
            Triple("/storage/emulated/0/Music/Albums/Imagine Dragons/Believer.aac", "Believer.aac", 204_000L),
            Triple("/storage/emulated/0/Music/Beethoven - Symphony 9.ogg", "Beethoven - Symphony 9.ogg", 1_200_000L),
            Triple("/storage/emulated/0/Music/Song 2.mp3", "Song 2.mp3", 122_000L),
            Triple("/storage/emulated/0/Music/01_intro_theme.mp3", "01_intro_theme.mp3", 95_000L)
        )

        for ((path, name, duration) in genuineSongs) {
            val included = filterEngine.evaluateFile(path, name, duration, testConfig)
            assertTrue("Expected genuine track to be included: $name", included)
        }
    }

    // ====================================================
    // Messaging Apps Recordings Tests (Should be FILTERED)
    // ====================================================

    @Test
    fun shouldExclude_whatsAppAudioAndVoiceNotes() {
        // WhatsApp PTT Voice Notes
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/WhatsApp/Media/WhatsApp Voice Notes/202640/PTT-20261009-WA0003.opus",
                "PTT-20261009-WA0003.opus",
                45_000L,
                testConfig
            )
        )

        // WhatsApp Shared Audio
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/WhatsApp/Media/WhatsApp Audio/AUD-20261009-WA0015.mp3",
                "AUD-20261009-WA0015.mp3",
                60_000L,
                testConfig
            )
        )

        // WhatsApp New Path on Android 11+
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/PTT-20261009-WA0001.opus",
                "PTT-20261009-WA0001.opus",
                15_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_telegramVoiceMessages() {
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Telegram/Telegram Audio/audio_2026-10-09_14-30-22.ogg",
                "audio_2026-10-09_14-30-22.ogg",
                55_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_facebookMessengerVoiceClips() {
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Android/media/com.facebook.orca/audioclip-1728482910-4821.m4a",
                "audioclip-1728482910-4821.m4a",
                32_000L,
                testConfig
            )
        )
    }

    // ====================================================
    // OEM Voice & Call Recorders Tests (Should be FILTERED)
    // ====================================================

    @Test
    fun shouldExclude_samsungVoiceAndCallRecordings() {
        // Samsung Voice Recorder
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Sounds/Voice Recorder/Voice 001.m4a",
                "Voice 001.m4a",
                65_000L,
                testConfig
            )
        )

        // Samsung Call Recording
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Recordings/Call/Call recording_0123456789_202610091430.m4a",
                "Call recording_0123456789_202610091430.m4a",
                120_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_xiaomiMiuiRecordings() {
        // Xiaomi Sound Recorder
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/MIUI/sound_recorder/sound_recorder_20261009_143022.mp3",
                "sound_recorder_20261009_143022.mp3",
                90_000L,
                testConfig
            )
        )

        // Xiaomi Call Recording
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/MIUI/sound_recorder/call_rec/call_20261009_143022_9876543210.mp3",
                "call_20261009_143022_9876543210.mp3",
                140_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_onePlusAndOppoRecordings() {
        // OnePlus
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Recordings/Record_2026-10-09-14-30-22.mp3",
                "Record_2026-10-09-14-30-22.mp3",
                80_000L,
                testConfig
            )
        )

        // Oppo / Realme
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Recordings/REC_20261009_143022.mp3",
                "REC_20261009_143022.mp3",
                75_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_googlePixelAndAospRecordings() {
        // Pixel
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Recordings/Recording_00042.m4a",
                "Recording_00042.m4a",
                60_000L,
                testConfig
            )
        )

        // AOSP
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Recordings/New Recording 1.m4a",
                "New Recording 1.m4a",
                50_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_thirdPartyRecordingApps() {
        // ACR
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/ACR/ACR_20261009_143022_9876.mp3",
                "ACR_20261009_143022_9876.mp3",
                95_000L,
                testConfig
            )
        )

        // Cube ACR folder check
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/CubeACR/John_Doe_call.mp3",
                "John_Doe_call.mp3",
                95_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_voiceCodecs() {
        // AMR voice files
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Audio/voice_memo.amr",
                "voice_memo.amr",
                120_000L,
                testConfig
            )
        )

        // 3GP voice files
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Audio/recording.3gp",
                "recording.3gp",
                120_000L,
                testConfig
            )
        )
    }

    @Test
    fun shouldExclude_shortClipsBelowDurationThreshold() {
        // Audio clip shorter than min duration (30,000ms)
        assertFalse(
            filterEngine.evaluateFile(
                "/storage/emulated/0/Music/Short_Chime.mp3",
                "Short_Chime.mp3",
                12_000L, // 12 seconds
                testConfig
            )
        )
    }
}
