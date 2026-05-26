package com.y9vad9.minesweeper.data

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import com.y9vad9.minesweeper.HistoryRepository

fun iosHistoryRepository(): HistoryRepository {
    val driver = NativeSqliteDriver(
        schema = MinesweeperDatabase.Schema,
        name = "minesweeper.db",
    )
    return SqlDelightHistoryRepository(driver)
}
