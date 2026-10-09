package com.audiocrawl.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.audiocrawl.model.ScanConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "audiocrawl_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val INCLUDE_SD_CARD = booleanPreferencesKey("include_sd_card")
        val TARGET_FOLDER_NAME = stringPreferencesKey("target_folder_name")
        val MIN_DURATION_MS = longPreferencesKey("min_duration_ms")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
    }

    val scanConfigFlow: Flow<ScanConfig> = context.dataStore.data.map { preferences ->
        ScanConfig(
            includeSdCard = preferences[PreferencesKeys.INCLUDE_SD_CARD] ?: true,
            targetFolderName = preferences[PreferencesKeys.TARGET_FOLDER_NAME] ?: ScanConfig.DEFAULT_TARGET_FOLDER,
            minDurationMs = preferences[PreferencesKeys.MIN_DURATION_MS] ?: ScanConfig.DEFAULT_MIN_DURATION_MS,
            excludedFolders = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: ScanConfig.DEFAULT_EXCLUDED_FOLDERS
        )
    }

    suspend fun getScanConfig(): ScanConfig {
        return scanConfigFlow.first()
    }

    suspend fun updateIncludeSdCard(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_SD_CARD] = include
        }
    }

    suspend fun updateTargetFolderName(name: String) {
        val sanitized = name.trim().ifEmpty { ScanConfig.DEFAULT_TARGET_FOLDER }
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_FOLDER_NAME] = sanitized
        }
    }

    suspend fun updateMinDurationMs(durationMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_DURATION_MS] = durationMs
        }
    }

    suspend fun addExcludedFolder(folder: String) {
        val trimmed = folder.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: ScanConfig.DEFAULT_EXCLUDED_FOLDERS
            preferences[PreferencesKeys.EXCLUDED_FOLDERS] = current + trimmed
        }
    }

    suspend fun removeExcludedFolder(folder: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: ScanConfig.DEFAULT_EXCLUDED_FOLDERS
            preferences[PreferencesKeys.EXCLUDED_FOLDERS] = current - folder
        }
    }

    suspend fun resetExcludedFoldersToDefault() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXCLUDED_FOLDERS] = ScanConfig.DEFAULT_EXCLUDED_FOLDERS
        }
    }
}
