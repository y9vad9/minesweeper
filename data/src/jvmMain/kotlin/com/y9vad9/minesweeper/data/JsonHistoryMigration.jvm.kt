package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.HistoryRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

internal object JsonHistoryMigration {

    @Serializable
    private data class HistoryFile(val records: List<GameRecordDto>)

    private val json = Json { ignoreUnknownKeys = true }

    fun migrate(legacyJson: File, repo: HistoryRepository) {
        val raw = try {
            legacyJson.readText()
        } catch (_: IOException) {
            return
        }
        val records = try {
            json.decodeFromString(HistoryFile.serializer(), raw).records.map { it.toDomain() }
        } catch (_: SerializationException) {
            return
        }
        runBlocking {
            for (record in records) repo.add(record)
        }

        try {
            val bak = File(legacyJson.parentFile, legacyJson.name + ".bak")
            if (bak.exists()) bak.delete()
            legacyJson.renameTo(bak)
        } catch (_: SecurityException) {
        }
    }
}
