package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.GameRecord
import com.y9vad9.minesweeper.HistoryRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface HistoryBlobStore {
    suspend fun read(): String?
    suspend fun write(json: String)
}

class JsonHistoryRepository(private val store: HistoryBlobStore) : HistoryRepository {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val serializer = ListSerializer(GameRecordDto.serializer())
    private val mutex = Mutex()

    private var cache: List<GameRecord>? = null

    private suspend fun snapshot(): List<GameRecord> {
        cache?.let { return it }
        val raw = try {
            store.read()
        } catch (_: Exception) {
            return emptyList<GameRecord>().also { cache = it }
        } ?: return emptyList<GameRecord>().also { cache = it }
        val parsed = try {
            json.decodeFromString(serializer, raw).map { it.toDomain() }
        } catch (_: SerializationException) {
            emptyList()
        }
        cache = parsed
        return parsed
    }

    private suspend fun persist(records: List<GameRecord>) {
        cache = records
        try {
            store.write(json.encodeToString(serializer, records.map { it.toDto() }))
        } catch (_: Exception) {
        }
    }

    override suspend fun loadAll(): List<GameRecord> = mutex.withLock { snapshot() }

    override suspend fun add(record: GameRecord) = mutex.withLock {
        persist(snapshot() + record)
    }

    override suspend fun clear() = mutex.withLock { persist(emptyList()) }

    override suspend fun trimToCap(cap: Int) = mutex.withLock {
        val current = snapshot()
        if (current.size <= cap) return@withLock
        persist(current.takeLast(cap))
    }
}
