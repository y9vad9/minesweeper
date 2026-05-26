package com.y9vad9.minesweeper.data

import app.cash.sqldelight.db.SqlDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import com.y9vad9.minesweeper.Seed
import com.y9vad9.minesweeper.GameOutcome
import com.y9vad9.minesweeper.GameRecord
import com.y9vad9.minesweeper.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.y9vad9.minesweeper.data.db.GameRecord as DbGameRecord

class SqlDelightHistoryRepository(driver: SqlDriver) : HistoryRepository {
    private val db = MinesweeperDatabase(driver)
    private val queries = db.gameRecordQueries

    override suspend fun loadAll(): List<GameRecord> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toRecord() }
    }

    override suspend fun add(record: GameRecord): Unit = withContext(Dispatchers.Default) {
        queries.insert(
            played_at_epoch_millis = record.playedAtEpochMillis,
            seed = record.seed.value,
            rows = record.rows.toLong(),
            cols = record.cols.toLong(),
            mines = record.mines.toLong(),
            outcome = record.outcome.name,
            duration_millis = record.durationMillis,
            three_bv = record.threeBV?.toLong(),
            three_bv_solved = record.threeBVSolved?.toLong(),
        )
        Unit
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.Default) {
        queries.clear()
        Unit
    }

    override suspend fun trimToCap(cap: Int): Unit = withContext(Dispatchers.Default) {
        queries.trimToCap(cap.toLong())
        Unit
    }

    private fun DbGameRecord.toRecord(): GameRecord = GameRecord(
        seed = Seed(seed),
        rows = rows.toInt(),
        cols = cols.toInt(),
        mines = mines.toInt(),
        outcome = GameOutcome.valueOf(outcome),
        durationMillis = duration_millis,
        playedAtEpochMillis = played_at_epoch_millis,
        threeBV = three_bv?.toInt(),
        threeBVSolved = three_bv_solved?.toInt(),
    )
}
