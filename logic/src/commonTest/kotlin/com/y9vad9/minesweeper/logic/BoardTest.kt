package com.y9vad9.minesweeper.logic

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class BoardTest {

    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }
    private val engine = GameEngine(clock)
    private val config = GameConfig(rows = 9, cols = 9, mines = 10, seed = Seed(1234))

    private fun playing(): GameState.Playing =
        engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing

    @Test fun `cellAt with negative row throws IllegalArgumentException`() {
        // GIVEN
        val board = playing().board

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> { board.cellAt(Position(-1, 0)) }
    }

    @Test fun `cellAt with column past width throws IllegalArgumentException`() {
        // GIVEN
        val board = playing().board

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> { board.cellAt(Position(0, 9)) }
    }

    @Test fun `inBounds for top-left corner returns true`() {
        // GIVEN
        val board = playing().board

        // WHEN / THEN
        assertTrue(board.inBounds(Position(0, 0)))
    }

    @Test fun `inBounds for bottom-right corner returns true`() {
        // GIVEN
        val board = playing().board

        // WHEN / THEN
        assertTrue(board.inBounds(Position(8, 8)))
    }

    @Test fun `inBounds for row past height returns false`() {
        // GIVEN
        val board = playing().board

        // WHEN / THEN
        assertFalse(board.inBounds(Position(9, 0)))
    }

    @Test fun `inBounds for negative column returns false`() {
        // GIVEN
        val board = playing().board

        // WHEN / THEN
        assertFalse(board.inBounds(Position(0, -1)))
    }

    @Test fun `flag on hidden cell marks it flagged and increments flagCount`() {
        // GIVEN
        val start = playing()
        val target = Position(0, 0)

        // WHEN
        val afterFlag = engine.flag(start, target) as GameState.Playing

        // THEN
        val cell = assertIs<Cell.Hidden>(afterFlag.board.cellAt(target))
        assertTrue(cell.flagged)
        assertEquals(1, afterFlag.board.flagCount)
    }

    @Test fun `flag twice on the same hidden cell returns it to unflagged`() {
        // GIVEN
        val target = Position(0, 0)
        val flagged = engine.flag(playing(), target) as GameState.Playing

        // WHEN
        val unflagged = engine.flag(flagged, target) as GameState.Playing

        // THEN
        val cell = assertIs<Cell.Hidden>(unflagged.board.cellAt(target))
        assertFalse(cell.flagged)
        assertEquals(0, unflagged.board.flagCount)
    }

    @Test fun `adjacentMineCount for top-left corner stays within zero to three neighbors`() {
        // GIVEN
        val board = playing().board

        // WHEN
        val count = board.adjacentMineCount(Position(0, 0))

        // THEN
        assertContains(0..3, count)
    }

    @Test fun `isCleared on a freshly playing board returns false`() {
        // GIVEN / WHEN
        val board = playing().board

        // THEN
        assertFalse(board.isCleared)
    }
}
