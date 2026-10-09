package com.audiocrawl.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFileTest {

    @Test
    fun testFormattedDuration_minutesAndSeconds() {
        val audioFile = AudioFile(
            title = "Test Song",
            durationMs = 215_000L, // 3 minutes 35 seconds
            filePath = "/path/test.mp3",
            fileName = "test.mp3"
        )
        assertEquals("3:35", audioFile.formattedDuration)
    }

    @Test
    fun testFormattedDuration_hoursMinutesSeconds() {
        val audioFile = AudioFile(
            title = "Long Symphony",
            durationMs = 3_725_000L, // 1 hour 2 minutes 5 seconds
            filePath = "/path/symphony.mp3",
            fileName = "symphony.mp3"
        )
        assertEquals("1:02:05", audioFile.formattedDuration)
    }

    @Test
    fun testFormattedDuration_zero() {
        val audioFile = AudioFile(
            title = "Zero Duration",
            durationMs = 0L,
            filePath = "/path/test.mp3",
            fileName = "test.mp3"
        )
        assertEquals("0:00", audioFile.formattedDuration)
    }

    @Test
    fun testFormattedSize_megabytes() {
        val audioFile = AudioFile(
            title = "Test Song",
            sizeBytes = 5_242_880L, // 5.0 MB
            filePath = "/path/test.mp3",
            fileName = "test.mp3"
        )
        assertEquals("5.0 MB", audioFile.formattedSize)
    }

    @Test
    fun testFormattedSize_kilobytes() {
        val audioFile = AudioFile(
            title = "Small Audio",
            sizeBytes = 512_000L, // 500 KB
            filePath = "/path/test.mp3",
            fileName = "test.mp3"
        )
        assertEquals("500 KB", audioFile.formattedSize)
    }
}
