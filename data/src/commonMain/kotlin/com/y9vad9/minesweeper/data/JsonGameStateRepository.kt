package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.GameStateRepository
import com.y9vad9.minesweeper.logic.PersistedGame
import kotlinx.serialization.json.Json

interface GameStateBlobStore {
    suspend fun read(): String?
    suspend fun write(json: String)
    suspend fun clear()
}

class JsonGameStateRepository(private val store: GameStateBlobStore) : GameStateRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(): PersistedGame? {
        val raw = try {
            store.read()
        } catch (_: Exception) {
            return null
        } ?: return null
        return try {
            json.decodeFromString(PersistedGameDto.serializer(), raw).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun save(game: PersistedGame) {
        val encoded = json.encodeToString(PersistedGameDto.serializer(), game.toDto())
        try {
            store.write(encoded)
        } catch (_: Exception) {
        }
    }

    override suspend fun clear() {
        try {
            store.clear()
        } catch (_: Exception) {
        }
    }
}
