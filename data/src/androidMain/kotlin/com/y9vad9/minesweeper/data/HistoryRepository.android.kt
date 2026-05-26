package com.y9vad9.minesweeper.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import com.y9vad9.minesweeper.HistoryRepository

fun androidHistoryRepository(context: Context): HistoryRepository {
    val driver = AndroidSqliteDriver(
        schema = MinesweeperDatabase.Schema,
        context = context.applicationContext,
        name = "history.db",
    )
    return SqlDelightHistoryRepository(driver)
}
