package com.y9vad9.minesweeper.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class ThreeBVTest {

    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }
    private val engine = GameEngine(clock)

    @Test fun `threeBV is at least one and at most non-mine cells`() {
        val config = GameConfig(rows = 9, cols = 9, mines = 10, seed = Seed(1234))
        val playing = engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing
        val bv = playing.board.threeBV()

        assertTrue(bv >= 1, "expected 3BV >= 1, was $bv")
        assertTrue(bv <= 9 * 9 - 10, "expected 3BV <= non-mine cells, was $bv")
    }

    @Test fun `threeBV is deterministic for the same seed and first reveal`() {
        val config = GameConfig(rows = 9, cols = 9, mines = 10, seed = Seed(42))
        val a = (engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing).board
        val b = (engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing).board
        assertEquals(a.threeBV(), b.threeBV())
    }

    @Test fun `threeBV on a Hard-sized board is comfortably above ten`() {
        val config = GameConfig(
            rows = Difficulty.Hard.rows,
            cols = Difficulty.Hard.cols,
            mines = Difficulty.Hard.mines,
            seed = Seed(2024),
        )
        val playing = engine.reveal(engine.newGame(config), Position(8, 15)) as GameState.Playing
        assertTrue(playing.board.threeBV() > 10, "expected 3BV > 10 on Hard board")
    }
}
