package com.y9vad9.minesweeper.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.y9vad9.minesweeper.GameEngine
import com.y9vad9.minesweeper.Difficulty
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.Position
import com.y9vad9.minesweeper.Seed
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, DelicateStoreApi::class, InternalFlowMVIAPI::class)
class GameStoreTest {

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }

    private fun storeTest(block: suspend TestScope.(GameStore) -> Unit): TestResult = runTest {
        val store = gameStore(
            scope = this,
            engine = GameEngine(fixedClock),
            initialConfig = Difficulty.Easy.toConfig(Seed(1234)),
        )
        advanceUntilIdle()
        try {
            block(store)
        } finally {
            store.close()
        }
    }

    @Test fun `new store is Idle with the initial config`() = storeTest { store ->
        // WHEN
        val state = store.states.value

        // THEN
        assertIs<GameState.Idle>(state.game)
        assertEquals(Seed(1234), state.lastConfig.seed)
        assertEquals(Difficulty.Easy, state.difficulty)
    }

    @Test fun `CellRevealed intent at center transitions Idle to Playing`() = storeTest { store ->
        // WHEN
        store.intent(GameIntent.CellRevealed(Position(4, 4)))
        advanceUntilIdle()

        // THEN
        assertIs<GameState.Playing>(store.states.value.game)
    }

    @Test fun `CellFlagged intent on Idle state is a no-op`() = storeTest { store ->
        // GIVEN
        val before = store.states.value

        // WHEN
        store.intent(GameIntent.CellFlagged(Position(0, 0)))
        advanceUntilIdle()

        // THEN
        assertEquals(before, store.states.value)
    }

    @Test fun `SeedInputChanged updates seedInput without changing the game`() = storeTest { store ->
        // GIVEN
        val originalGame = store.states.value.game

        // WHEN
        store.intent(GameIntent.SeedInputChanged("42"))
        advanceUntilIdle()

        // THEN
        val state = store.states.value
        assertEquals("42", state.seedInput)
        assertEquals(originalGame, state.game)
    }

    @Test fun `NewGameRequested with a preset code uses its dimensions and seed`() = storeTest { store ->
        // GIVEN
        store.intent(GameIntent.SeedInputChanged("M-1E61"))

        // WHEN
        store.intent(GameIntent.NewGameRequested)
        advanceUntilIdle()

        // THEN
        val state = store.states.value
        assertEquals(Seed(0x1E61), state.lastConfig.seed)
        assertEquals(Difficulty.Medium, state.difficulty)
        assertEquals(16, state.game.config.rows)
    }

    @Test fun `NewGameRequested with an explicit-dimensions code builds a custom board`() = storeTest { store ->
        // GIVEN
        store.intent(GameIntent.SeedInputChanged("20x30x120-7A3F"))

        // WHEN
        store.intent(GameIntent.NewGameRequested)
        advanceUntilIdle()

        // THEN
        val config = store.states.value.game.config
        assertEquals(20, config.rows)
        assertEquals(30, config.cols)
        assertEquals(120, config.mines)
        assertEquals(Seed(0x7A3F), config.seed)
    }

    @Test fun `DifficultyPicked Medium resets the game to Medium dimensions`() = storeTest { store ->
        // WHEN
        store.intent(GameIntent.DifficultyPicked(Difficulty.Medium))
        advanceUntilIdle()

        // THEN
        val state = store.states.value
        assertEquals(Difficulty.Medium, state.difficulty)
        assertEquals(16, state.game.config.rows)
        assertEquals(16, state.game.config.cols)
        assertEquals(40, state.game.config.mines)
    }

    @Test fun `CustomGameRequested sets the requested dimensions`() = storeTest { store ->
        // WHEN
        store.intent(GameIntent.CustomGameRequested(rows = 12, cols = 18, mines = 50, seedInput = ""))
        advanceUntilIdle()

        // THEN
        val config = store.states.value.game.config
        assertEquals(12, config.rows)
        assertEquals(18, config.cols)
        assertEquals(50, config.mines)
        assertIs<GameState.Idle>(store.states.value.game)
    }

    @Test fun `CustomGameRequested with a hex seed uses that seed`() = storeTest { store ->
        // WHEN
        store.intent(GameIntent.CustomGameRequested(rows = 12, cols = 18, mines = 50, seedInput = "7A3F"))
        advanceUntilIdle()

        // THEN
        assertEquals(Seed(0x7A3F), store.states.value.lastConfig.seed)
    }

    @Test fun `CustomGameRequested with invalid dimensions is a no-op`() = storeTest { store ->
        // GIVEN
        val before = store.states.value

        // WHEN: 3x3 violates the 4x4 minimum
        store.intent(GameIntent.CustomGameRequested(rows = 3, cols = 3, mines = 1, seedInput = ""))
        advanceUntilIdle()

        // THEN
        assertEquals(before, store.states.value)
    }

    @Test fun `ReplayRequested rebuilds the board from the stored config`() = storeTest { store ->
        // GIVEN / WHEN
        store.intent(GameIntent.ReplayRequested(rows = 9, cols = 9, mines = 10, seed = Seed(99)))
        advanceUntilIdle()

        // THEN
        val state = store.states.value
        assertEquals(Seed(99), state.lastConfig.seed)
        assertEquals(Difficulty.Easy, state.difficulty)
        assertIs<GameState.Idle>(state.game)
    }
}
