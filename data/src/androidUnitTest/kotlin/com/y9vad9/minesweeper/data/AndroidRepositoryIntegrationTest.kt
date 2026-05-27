package com.y9vad9.minesweeper.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.y9vad9.minesweeper.data.db.MinesweeperDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AndroidRepositoryIntegrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun history_round_trips_through_real_sqlite() = runTest {
        val driver = AndroidSqliteDriver(MinesweeperDatabase.Schema, context, name = null)
        try {
            RepositoryContract.verifyHistory { SqlDelightHistoryRepository(driver) }
        } finally {
            driver.close()
        }
    }

    @Test
    fun game_state_round_trips_through_a_real_file() = runTest {
        val file = File(context.cacheDir, "test-game.json").also { it.delete() }
        try {
            RepositoryContract.verifyGameState { FileGameStateBlobStore(file) }
        } finally {
            file.delete()
        }
    }
}
