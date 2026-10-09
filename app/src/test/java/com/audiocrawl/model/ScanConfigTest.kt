package com.audiocrawl.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanConfigTest {

    @Test
    fun testDefaultScanConfig() {
        val config = ScanConfig()
        assertTrue(config.includeSdCard)
        assertEquals("AudioCrawl_Songs", config.targetFolderName)
        assertEquals(30_000L, config.minDurationMs)
    }

    @Test
    fun testDefaultExcludedFolders_containsCriticalPaths() {
        val excluded = ScanConfig.DEFAULT_EXCLUDED_FOLDERS
        assertTrue(excluded.contains("WhatsApp/Media/WhatsApp Voice Notes"))
        assertTrue(excluded.contains("Recordings"))
        assertTrue(excluded.contains("Call"))
        assertTrue(excluded.contains("MIUI/sound_recorder"))
        assertTrue(excluded.contains("CubeACR"))
        assertTrue(excluded.contains("DCIM"))
        assertTrue(excluded.contains("Ringtones"))
    }
}
