package com.y9vad9.minesweeper.ui

interface SettingsRepository {
    suspend fun load(): Settings
    suspend fun save(settings: Settings)
}
