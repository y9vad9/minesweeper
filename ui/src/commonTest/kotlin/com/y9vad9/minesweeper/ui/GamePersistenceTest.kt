package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.Difficulty
import com.y9vad9.minesweeper.GameEngine
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.GameStateRepository
import com.y9vad9.minesweeper.PersistedGame
import com.y9vad9.minesweeper.Position
import com.y9vad9.minesweeper.Seed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, InternalFlowMVIAPI::class)
class GamePersistenceTest {

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }

    private class FakeRepository(var saved: PersistedGame? = null) : GameStateRepository {
        override suspend fun load(): PersistedGame? = saved
        override suspend fun save(game: PersistedGame) { saved = game }
        override suspend fun clear() { saved = null }
    }

    private fun TestScope.newStore(repo: GameStateRepository) = gameStore(
        scope = this,
        engine = GameEngine(fixedClock),
        initialConfig = Difficulty.Easy.toConfig(Seed(1234)),
        persistence = repo,
    )

    @Test
    fun saves_state_after_a_move() = runTest {
        val repo = FakeRepository()
        val store = newStore(repo)
        advanceUntilIdle()

        store.intent(GameIntent.CellRevealed(Position(4, 4)))
        advanceUntilIdle()

        val saved = assertNotNull(repo.saved)
        assertIs<GameState.Playing>(saved.game)
        store.close()
    }

    @Test
    fun restores_persisted_game_on_start() = runTest {
        val repo = FakeRepository()
        val first = newStore(repo)
        advanceUntilIdle()
        first.intent(GameIntent.CellRevealed(Position(4, 4)))
        advanceUntilIdle()
        first.close()
        val persisted = assertNotNull(repo.saved)

        val second = newStore(repo)
        advanceUntilIdle()

        val state = second.states.value
        val restored = assertIs<GameState.Playing>(state.game)
        assertEquals(
            assertIs<GameState.Playing>(persisted.game).board.snapshot(),
            restored.board.snapshot(),
        )
        assertEquals("", state.seedInput)
        second.close()
    }
}
