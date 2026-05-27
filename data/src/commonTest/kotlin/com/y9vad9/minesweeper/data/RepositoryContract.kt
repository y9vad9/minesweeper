package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.GameConfig
import com.y9vad9.minesweeper.GameEngine
import com.y9vad9.minesweeper.GameOutcome
import com.y9vad9.minesweeper.GameRecord
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.HistoryRepository
import com.y9vad9.minesweeper.PersistedGame
import com.y9vad9.minesweeper.Position
import com.y9vad9.minesweeper.Seed
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Platform-agnostic behaviour every real persistence backend must satisfy.
 *
 * Each platform test supplies factories backed by the *actual* production store
 * (real SQLite driver, real file, real NSUserDefaults, real localStorage) pointed
 * at an isolated, initially-empty location. Factories must be re-invokable and
 * observe the same backing, so we can prove data survives a fresh repository.
 */
object RepositoryContract {

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(42_000)
    }
    private val engine = GameEngine(fixedClock)
    private val config = GameConfig(rows = 9, cols = 9, mines = 10, seed = Seed(1234))

    private fun record(seed: Long, outcome: GameOutcome = GameOutcome.Won): GameRecord =
        GameRecord(
            seed = Seed(seed),
            rows = 9,
            cols = 9,
            mines = 10,
            outcome = outcome,
            durationMillis = 1_000 + seed,
            playedAtEpochMillis = seed * 1_000,
            threeBV = 30,
            threeBVSolved = if (outcome == GameOutcome.Won) 30 else 12,
        )

    suspend fun verifyHistory(newRepository: () -> HistoryRepository) {
        assertEquals(emptyList(), newRepository().loadAll(), "backing must start empty")

        newRepository().apply {
            add(record(1))
            add(record(2, GameOutcome.Lost))
            add(record(3))
        }

        // Survives a fresh repository instance over the same backing.
        val reloaded = newRepository().loadAll()
        assertEquals(setOf(Seed(1), Seed(2), Seed(3)), reloaded.map { it.seed }.toSet())
        assertEquals(3, reloaded.size)

        // Every field round-trips through the real store.
        assertEquals(record(1), reloaded.first { it.seed == Seed(1) })
        assertEquals(record(2, GameOutcome.Lost), reloaded.first { it.seed == Seed(2) })

        // trimToCap keeps the most recent `cap` records (highest playedAt).
        newRepository().trimToCap(2)
        assertEquals(setOf(Seed(2), Seed(3)), newRepository().loadAll().map { it.seed }.toSet())

        newRepository().clear()
        assertEquals(emptyList(), newRepository().loadAll())
    }

    suspend fun verifyGameState(newStore: () -> GameStateBlobStore) {
        assertNull(JsonGameStateRepository(newStore()).load(), "backing must start empty")

        val playing = engine.reveal(engine.newGame(config), Position(4, 4))
        assertIs<GameState.Playing>(playing)
        JsonGameStateRepository(newStore()).save(PersistedGame(playing, flagMode = true))

        // Survives a fresh repository instance over the same backing.
        val restored = assertNotNull(JsonGameStateRepository(newStore()).load())
        val restoredGame = assertIs<GameState.Playing>(restored.game)
        assertEquals(playing.board.snapshot(), restoredGame.board.snapshot())
        assertEquals(playing.startedAt, restoredGame.startedAt)
        assertEquals(config, restoredGame.config)
        assertEquals(true, restored.flagMode)

        JsonGameStateRepository(newStore()).clear()
        assertNull(JsonGameStateRepository(newStore()).load())
    }
}
