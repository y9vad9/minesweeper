package com.y9vad9.minesweeper.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameConfigTest {

    @Test fun `construct with valid dimensions succeeds`() {
        // GIVEN
        val rows = 9; val cols = 9; val mines = 10

        // WHEN
        val config = GameConfig(rows, cols, mines, seed = Seed(1))

        // THEN
        assertEquals(81, config.totalCells)
    }

    @Test fun `construct with rows below four fails require`() {
        // GIVEN / WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            GameConfig(rows = 3, cols = 9, mines = 10, seed = Seed(1))
        }
    }

    @Test fun `construct with cols below four fails require`() {
        // GIVEN / WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            GameConfig(rows = 9, cols = 3, mines = 10, seed = Seed(1))
        }
    }

    @Test fun `construct with zero mines fails require`() {
        // GIVEN / WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            GameConfig(rows = 9, cols = 9, mines = 0, seed = Seed(1))
        }
    }

    @Test fun `construct with mines filling safe zone fails require`() {
        // GIVEN: 9*9 - 9 = 72 is the exclusive upper bound
        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            GameConfig(rows = 9, cols = 9, mines = 72, seed = Seed(1))
        }
    }

    @Test fun `construct with mines just below safe zone bound succeeds`() {
        // GIVEN / WHEN
        val config = GameConfig(rows = 9, cols = 9, mines = 71, seed = Seed(1))

        // THEN
        assertEquals(71, config.mines)
    }
}
