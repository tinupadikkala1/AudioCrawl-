package com.audiocrawl.util

import java.io.File
import java.util.Locale

/**
 * Extension to safely retrieve file extension in lowercase without leading dot.
 */
val File.cleanExtension: String
    get() = extension.lowercase(Locale.ROOT)

/**
 * Extension to safely check if file is an audio file.
 */
fun File.hasAudioExtension(supportedExtensions: Set<String>): Boolean {
    return isFile && cleanExtension in supportedExtensions
}
