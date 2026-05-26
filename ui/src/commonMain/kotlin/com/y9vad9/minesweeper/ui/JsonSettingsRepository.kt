package com.y9vad9.minesweeper.ui

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

interface SettingsBlobStore {
    suspend fun read(): String?
    suspend fun write(json: String)
}

class JsonSettingsRepository(private val store: SettingsBlobStore) : SettingsRepository {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    override suspend fun load(): Settings {
        val raw = try {
            store.read()
        } catch (_: Exception) {
            return Settings()
        } ?: return Settings()
        return try {
            json.decodeFromString(SettingsDto.serializer(), raw).toDomain()
        } catch (_: SerializationException) {
            Settings()
        }
    }

    override suspend fun save(settings: Settings) {
        val encoded = json.encodeToString(SettingsDto.serializer(), settings.toDto())
        try {
            store.write(encoded)
        } catch (_: Exception) {
        }
    }
}
