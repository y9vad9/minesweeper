package com.y9vad9.minesweeper.logic

interface SettingsRepository {
    suspend fun load(): Settings
    suspend fun save(settings: Settings)
}
