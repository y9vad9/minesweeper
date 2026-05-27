package com.y9vad9.minesweeper.data

import kotlinx.browser.localStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WebRepositoryIntegrationTest {

    @Test
    fun history_round_trips_through_real_local_storage() = runTest {
        val key = "test:minesweeper:history"
        localStorage.removeItem(key)
        try {
            RepositoryContract.verifyHistory { JsonHistoryRepository(LocalStorageHistoryBlobStore(key)) }
        } finally {
            localStorage.removeItem(key)
        }
    }

    @Test
    fun game_state_round_trips_through_real_local_storage() = runTest {
        val key = "test:minesweeper:game"
        localStorage.removeItem(key)
        try {
            RepositoryContract.verifyGameState { LocalStorageGameBlobStore(key) }
        } finally {
            localStorage.removeItem(key)
        }
    }
}
