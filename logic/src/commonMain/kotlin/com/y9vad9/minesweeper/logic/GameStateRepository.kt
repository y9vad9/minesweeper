package com.y9vad9.minesweeper.logic

data class PersistedGame(
    val game: GameState,
    val flagMode: Boolean,
)

interface GameStateRepository {
    suspend fun load(): PersistedGame?
    suspend fun save(game: PersistedGame)
    suspend fun clear()
}
