package com.y9vad9.minesweeper.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.y9vad9.minesweeper.GameOutcome
import com.y9vad9.minesweeper.GameRecord
import com.y9vad9.minesweeper.HistoryRepository
import com.y9vad9.minesweeper.Seed
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeHistoryRepository(initial: List<GameRecord> = emptyList()) : HistoryRepository {
    private val records = ArrayDeque(initial)
    val addCalls = mutableListOf<GameRecord>()
    val trimCalls = mutableListOf<Int>()
    var clearCalls = 0

    override suspend fun loadAll(): List<GameRecord> = records.toList()

    override suspend fun add(record: GameRecord) {
        addCalls += record
        records.addFirst(record)
    }

    override suspend fun clear() {
        clearCalls++
        records.clear()
    }

    override suspend fun trimToCap(cap: Int) {
        trimCalls += cap
        while (records.size > cap) records.removeLast()
    }
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateStoreApi::class, InternalFlowMVIAPI::class)
class HistoryStoreTest {

    private fun record(seed: Long, outcome: GameOutcome = GameOutcome.Won, playedAt: Long = seed) = GameRecord(
        seed = Seed(seed),
        rows = 9,
        cols = 9,
        mines = 10,
        outcome = outcome,
        durationMillis = 1_000,
        playedAtEpochMillis = playedAt,
    )

    private fun storeTest(
        repository: FakeHistoryRepository = FakeHistoryRepository(),
        maxRecords: Int = 100,
        block: suspend TestScope.(HistoryStore, FakeHistoryRepository) -> Unit,
    ): TestResult = runTest {
        val store = historyStore(scope = this, repository = repository, maxRecords = maxRecords)
        advanceUntilIdle()
        try {
            block(store, repository)
        } finally {
            store.close()
        }
    }

    @Test fun `load with empty repository starts with empty records and loaded true`() =
        storeTest { store, _ ->
            val state = store.states.value
            assertEquals(emptyList(), state.records)
            assertTrue(state.loaded)
        }

    @Test fun `Add intent persists the record and reloads from the repository`() =
        storeTest { store, repo ->
            store.intent(HistoryIntent.Add(record(seed = 1)))
            advanceUntilIdle()

            assertEquals(listOf(Seed(1)), store.states.value.records.map { it.seed })
            assertEquals(1, repo.addCalls.size)
        }

    @Test fun `Add intent places the newest record at the head of the list`() =
        storeTest { store, _ ->
            store.intent(HistoryIntent.Add(record(seed = 1, playedAt = 1)))
            store.intent(HistoryIntent.Add(record(seed = 2, playedAt = 2)))
            advanceUntilIdle()

            assertEquals(listOf(Seed(2), Seed(1)), store.states.value.records.map { it.seed })
        }

    @Test fun `Add intent past maxRecords trims the oldest record via the repository`() =
        storeTest(maxRecords = 2) { store, repo ->
            store.intent(HistoryIntent.Add(record(seed = 1, playedAt = 1)))
            store.intent(HistoryIntent.Add(record(seed = 2, playedAt = 2)))
            store.intent(HistoryIntent.Add(record(seed = 3, playedAt = 3)))
            advanceUntilIdle()

            assertEquals(listOf(Seed(3), Seed(2)), store.states.value.records.map { it.seed })
            assertEquals(listOf(2, 2, 2), repo.trimCalls)
        }

    @Test fun `Clear intent empties the records and calls clear on the repository`() =
        storeTest { store, repo ->
            store.intent(HistoryIntent.Add(record(seed = 1)))
            advanceUntilIdle()

            store.intent(HistoryIntent.Clear)
            advanceUntilIdle()

            assertEquals(emptyList(), store.states.value.records)
            assertEquals(1, repo.clearCalls)
        }

    @Test fun `repository populated up front is reflected in the initial state`() =
        storeTest(repository = FakeHistoryRepository(initial = listOf(record(seed = 7)))) { store, _ ->
            assertEquals(listOf(Seed(7)), store.states.value.records.map { it.seed })
        }
}
