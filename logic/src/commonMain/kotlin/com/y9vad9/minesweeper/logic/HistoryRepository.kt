package com.y9vad9.minesweeper.logic

interface HistoryRepository {
    suspend fun loadAll(): List<GameRecord>
    suspend fun add(record: GameRecord)
    suspend fun clear()
    suspend fun trimToCap(cap: Int)
}
