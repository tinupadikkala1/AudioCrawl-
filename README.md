# AudioCrawl 🎵

**AudioCrawl** is a modern Android application built with **Kotlin** and **Jetpack Compose** that solves a widespread problem: your phone's music player is cluttered with thousands of voice recordings, WhatsApp voice notes, Instagram clips, call recordings, and app sound effects.

AudioCrawl scans your device's internal storage and external SD cards for genuine music files, intelligently filters out all recordings and noise using a **7-layer Filter Engine**, lets you preview songs right inside the app, and copies selected music into a clean, dedicated folder.

---

## ✨ Features

- 🔍 **High-Speed Storage Scanning**: Queries Android's MediaStore database for sub-second indexed search across thousands of tracks.
- 💾 **SD Card / Removable Memory Support**: Toggle to scan secondary external memory cards or restrict to internal storage.
- 🧠 **7-Layer Filter Engine**:
  1. **MediaStore SQL Classification**: Server-side filtering (`IS_MUSIC != 0`, `IS_ALARM == 0`, `IS_NOTIFICATION == 0`, `IS_RINGTONE == 0`, `IS_RECORDING == 0` on Android 12+).
  2. **Music-Grade Extension Whitelist**: Only accepts music formats (`.mp3`, `.flac`, `.wav`, `.aac`, `.ogg`, `.m4a`, `.wma`, `.opus`, `.alac`, `.aiff`, `.dsf`, `.dff`, `.ape`).
  3. **Voice Codec Exclusion**: Strictly blocks speech-only codecs (`.amr`, `.3gp`, `.3gpp`, `.awb`).
  4. **40+ Directory Blacklist**: Automatically excludes WhatsApp Voice Notes, Telegram Audio, Messenger, Instagram, DCIM, MIUI/sound_recorder, Sounds, CubeACR, CallRecordings, and app data folders.
  5. **25+ OEM & App Recording Filename Patterns**: Intelligently identifies and filters recording naming conventions across Samsung, Xiaomi/HyperOS, OnePlus, Google Pixel, Huawei, Oppo, Realme, Vivo, Sony, Motorola, LG, WhatsApp (`PTT-*`, `AUD-*`), Telegram, Facebook Messenger, ACR, and more.
  6. **Configurable Duration Threshold**: Filters out audio clips under a user-defined threshold (default: 30 seconds).
  7. **User-Customizable Exclusions**: Add or remove custom folders directly in the Settings screen with DataStore persistence.
- 🎧 **Built-in Audio Preview Player**: Powered by **AndroidX Media3 ExoPlayer** — listen to any song with play/pause and seek controls before deciding to copy.
- 📁 **Scoped Storage Safe Batch Copier**:
  - **Android 10+ (API 29+)**: Uses `MediaStore.insert()` with `IS_PENDING` atomic writes. Requires **zero write permissions** and automatically indexes new songs so they immediately show up in all music players.
  - **Android 8-9 (API 26-28)**: High-speed streaming file copy with automatic duplicate name resolution (`song_(1).mp3`) and `MediaScannerConnection` indexing.
- 🎨 **Modern Material 3 Design**: Fully responsive Jetpack Compose interface with dark/light themes and dynamic color support on Android 12+.

---

## 🏗️ Architecture & Tech Stack

```
com.audiocrawl
├── AudioCrawlApp.kt          # Hilt Application & Notification Channels
├── MainActivity.kt           # Single Activity with Compose Navigation
├── navigation/
│   ├── NavGraph.kt           # Compose NavHost
│   └── Screen.kt             # Navigation route definitions
├── model/
│   ├── AudioFile.kt          # Song metadata model
│   ├── ScanConfig.kt         # User scan settings & default blacklists
│   └── ScanState.kt          # Scan & Copy sealed UI states
├── scanner/
│   ├── FilterEngine.kt       # The brain: 25+ regexes, SQL builder & filters
│   ├── MediaStoreScanner.kt  # MediaStore queries (API 26-35)
│   └── FileSystemScanner.kt  # Recursive directory tree walker for SD cards
├── service/
│   ├── ScanService.kt        # Foreground service for scanning
│   └── CopyService.kt        # Foreground service for copying
├── player/
│   └── AudioPlayerManager.kt # ExoPlayer wrapper for preview playback
├── data/
│   └── SettingsRepository.kt # Preferences DataStore persistence
├── di/
│   └── AppModule.kt          # Hilt Dependency Injection module
├── ui/
│   ├── theme/                # Color, Theme, Typography
│   ├── components/           # PermissionHandler, TopAppBar, Badges
│   ├── home/                 # HomeScreen & HomeViewModel
│   ├── scan/                 # ScanScreen & ScanViewModel
│   ├── results/              # ResultsScreen, ResultsViewModel, AudioFileItem, MiniPlayer
│   ├── copy/                 # CopyScreen & CopyViewModel
│   └── settings/             # SettingsScreen & SettingsViewModel
└── util/
    ├── StorageUtils.kt       # SD card detection & storage paths
    └── Extensions.kt         # Extension helper functions
```

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose + Material 3 (Material You)
- **Architecture**: Modern Android Architecture (MVVM / MVI) with unidirectional data flow
- **Dependency Injection**: Hilt / Dagger
- **Audio Engine**: AndroidX Media3 ExoPlayer 1.5.0
- **Storage & State**: DataStore Preferences + Kotlin Coroutines & StateFlow
- **Minimum SDK**: API 26 (Android 8.0 Oreo) — covers ~95%+ of active devices
- **Target SDK**: API 35 (Android 15)

---

## 🚀 Building & Running

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer)
- **JDK 21**
- **Android SDK Platform 35** and Build Tools

### Open in Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the `AudioCrawl` directory.
3. Allow Gradle to sync.
4. Run on an Android emulator or physical device running Android 8.0 to Android 15+.

### Build from Command Line
```bash
# Build Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk

# Run Unit Tests
./gradlew testDebugUnitTest
```

---

## 🔒 Permission Model

AudioCrawl adheres strictly to Google Play storage privacy guidelines and does **not** request invasive `MANAGE_EXTERNAL_STORAGE` permissions:
- **Android 13+ (API 33+)**: Requests granular `READ_MEDIA_AUDIO` + `POST_NOTIFICATIONS`.
- **Android 10–12 (API 29–32)**: Requests `READ_EXTERNAL_STORAGE`. Writing to `Environment.DIRECTORY_MUSIC` requires zero permissions via `MediaStore`.
- **Android 8–9 (API 26–28)**: Requests `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE`.

---

## 🧪 Automated Testing

AudioCrawl includes unit test coverage for the filter engine and data layers:
- `FilterEngineTest`: Validates 25+ OEM and messaging recording regex patterns (WhatsApp, Samsung, Xiaomi, OnePlus, Pixel, Telegram, etc.) and ensures real music tracks are accurately identified and preserved.
- `AudioFileTest`: Tests duration (mm:ss, hh:mm:ss) and byte size (KB, MB) formatters.
- `ScanConfigTest`: Verifies default configurations and critical excluded folder lists.
