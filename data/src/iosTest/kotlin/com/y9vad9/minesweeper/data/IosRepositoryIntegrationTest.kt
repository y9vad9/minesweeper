package com.y9vad9.minesweeper.data

import app.cash.sqldelight.driver.native.inMemoryDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSUserDefaults
import kotlin.test.Test

class IosRepositoryIntegrationTest {

    @Test
    fun history_round_trips_through_real_sqlite() = runTest {
        val driver = inMemoryDriver(MinesweeperDatabase.Schema)
        try {
            RepositoryContract.verifyHistory { SqlDelightHistoryRepository(driver) }
        } finally {
            driver.close()
        }
    }

    @Test
    fun game_state_round_trips_through_real_user_defaults() = runTest {
        val key = "test:minesweeper:game"
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        try {
            RepositoryContract.verifyGameState { UserDefaultsGameBlobStore(key) }
        } finally {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        }
    }
}
