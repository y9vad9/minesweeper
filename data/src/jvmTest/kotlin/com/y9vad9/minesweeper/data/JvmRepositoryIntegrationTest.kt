package com.y9vad9.minesweeper.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test

class JvmRepositoryIntegrationTest {

    @Test
    fun history_round_trips_through_real_sqlite() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MinesweeperDatabase.Schema.create(driver)
        try {
            RepositoryContract.verifyHistory { SqlDelightHistoryRepository(driver) }
        } finally {
            driver.close()
        }
    }

    @Test
    fun game_state_round_trips_through_a_real_file() = runTest {
        val file = File.createTempFile("ms-game", ".json").also { it.delete() }
        try {
            RepositoryContract.verifyGameState { FileGameStateBlobStore(file) }
        } finally {
            file.delete()
        }
    }
}
