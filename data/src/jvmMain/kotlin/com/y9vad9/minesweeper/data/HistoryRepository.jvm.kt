package com.y9vad9.minesweeper.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import com.y9vad9.minesweeper.HistoryRepository
import java.io.File

fun jvmHistoryRepository(): HistoryRepository {
    val home = minesweeperHomeDir().also { it.mkdirs() }
    val dbFile = File(home, "history.db")
    val isFresh = !dbFile.exists()

    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (isFresh) {
        MinesweeperDatabase.Schema.create(driver)
    }

    val repo = SqlDelightHistoryRepository(driver)

    if (isFresh) {
        val legacyJson = File(home, "history.json")
        if (legacyJson.exists()) {
            JsonHistoryMigration.migrate(legacyJson, repo)
        }
    }

    return repo
}
