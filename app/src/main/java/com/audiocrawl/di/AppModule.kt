package com.audiocrawl.di

import android.content.Context
import com.audiocrawl.data.SettingsRepository
import com.audiocrawl.player.AudioPlayerManager
import com.audiocrawl.scanner.FileSystemScanner
import com.audiocrawl.scanner.FilterEngine
import com.audiocrawl.scanner.MediaStoreScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideFilterEngine(
        settingsRepository: SettingsRepository
    ): FilterEngine {
        return FilterEngine(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideMediaStoreScanner(
        @ApplicationContext context: Context,
        filterEngine: FilterEngine,
        settingsRepository: SettingsRepository
    ): MediaStoreScanner {
        return MediaStoreScanner(context, filterEngine, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideFileSystemScanner(
        @ApplicationContext context: Context,
        filterEngine: FilterEngine,
        settingsRepository: SettingsRepository
    ): FileSystemScanner {
        return FileSystemScanner(context, filterEngine, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideAudioPlayerManager(
        @ApplicationContext context: Context
    ): AudioPlayerManager {
        return AudioPlayerManager(context)
    }
}
