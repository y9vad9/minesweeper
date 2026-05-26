package com.y9vad9.minesweeper.logic

import com.y9vad9.minesweeper.Board
import com.y9vad9.minesweeper.Cell
import com.y9vad9.minesweeper.GameConfig
import com.y9vad9.minesweeper.GameContractViolation
import com.y9vad9.minesweeper.GameEngine
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.GameStatus
import com.y9vad9.minesweeper.Position
import com.y9vad9.minesweeper.Seed
import com.y9vad9.minesweeper.status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.time.Clock
import kotlin.time.Instant

class GameEngineTest {

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }
    private val engine = GameEngine(fixedClock)
    private val config = GameConfig(rows = 9, cols = 9, mines = 10, seed = Seed(1234))

    @Test fun `newGame returns an Idle state with the given config`() {
        // GIVEN / WHEN
        val state = engine.newGame(config)

        // THEN
        assertEquals(GameStatus.Idle, state.status)
    }

    @Test fun `first reveal at center transitions to Playing`() {
        // GIVEN
        val idle = engine.newGame(config)

        // WHEN
        val state = engine.reveal(idle, Position(4, 4))

        // THEN
        assertIs<GameState.Playing>(state)
    }

    @Test fun `first reveal keeps the 3x3 zone around the click mine-free`() {
        // GIVEN
        val idle = engine.newGame(config)

        // WHEN
        val state = engine.reveal(idle, Position(4, 4)) as GameState.Playing

        // THEN
        for (dr in -1..1) for (dc in -1..1) {
            assertFalse(state.board.isMine(Position(4 + dr, 4 + dc)))
        }
    }

    @Test fun `same seed and same first click produce identical mine layouts`() {
        // GIVEN
        val firstClick = Position(0, 0)

        // WHEN
        val a = engine.reveal(engine.newGame(config), firstClick) as GameState.Playing
        val b = engine.reveal(engine.newGame(config), firstClick) as GameState.Playing

        // THEN
        for (r in 0 until config.rows) for (c in 0 until config.cols) {
            assertEquals(a.board.isMine(Position(r, c)), b.board.isMine(Position(r, c)))
        }
    }

    @Test fun `flag on hidden cell marks it flagged`() {
        // GIVEN
        val playing = engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing
        val hidden = findHidden(playing)

        // WHEN
        val flagged = engine.flag(playing, hidden) as GameState.Playing

        // THEN
        val cell = assertIs<Cell.Hidden>(flagged.board.cellAt(hidden))
        assertEquals(true, cell.flagged)
    }

    @Test fun `flag toggled twice on the same hidden cell returns it to unflagged`() {
        // GIVEN
        val playing = engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing
        val hidden = findHidden(playing)
        val flagged = engine.flag(playing, hidden) as GameState.Playing

        // WHEN
        val unflagged = engine.flag(flagged, hidden) as GameState.Playing

        // THEN
        val cell = assertIs<Cell.Hidden>(unflagged.board.cellAt(hidden))
        assertFalse(cell.flagged)
    }

    @Test fun `revealing a mine ends the game in Lost with that mine marked exploded`() {
        // GIVEN
        val playing = engine.reveal(engine.newGame(config), Position(0, 0)) as GameState.Playing
        val mine = findMine(playing)

        // WHEN
        val result = engine.reveal(playing, mine)

        // THEN
        val lost = assertIs<GameState.Lost>(result)
        assertEquals(mine, lost.triggeredAt)
        assertIs<Cell.ExplodedMine>(lost.board.cellAt(mine))
    }

    @Test fun `clearing every non-mine cell ends the game in Won`() {
        // GIVEN
        val playing = engine.reveal(engine.newGame(config), Position(4, 4)) as GameState.Playing

        // WHEN
        var current: GameState = playing
        for (r in 0 until config.rows) for (c in 0 until config.cols) {
            val pos = Position(r, c)
            if (playing.board.isMine(pos)) continue
            if (current is GameState.Playing && !(current as GameState.Playing).board.isRevealed(pos)) {
                current = engine.reveal(current, pos)
            }
        }

        // THEN
        assertIs<GameState.Won>(current)
    }

    @Test fun `revealing after the game is Lost throws GameContractViolation`() {
        // GIVEN
        val playing = engine.reveal(engine.newGame(config), Position(0, 0)) as GameState.Playing
        val lost = engine.reveal(playing, findMine(playing))

        // WHEN / THEN
        assertFailsWith<GameContractViolation> {
            engine.reveal(lost, Position(0, 0))
        }
    }

    @Test fun `chord on numbered cell with all mine-neighbors flagged changes state`() {
        // GIVEN
        val playing = engine.reveal(engine.newGame(config), Position(0, 0)) as GameState.Playing
        val (numbered, mineNeighbors) = findNumberedCellWithMineNeighbors(playing) ?: return
        var state: GameState = playing
        for (m in mineNeighbors) state = engine.flag(state, m)
        val before = state as GameState.Playing

        // WHEN
        val after = engine.chord(before, numbered)

        // THEN
        assertNotEquals(before, after)
    }

    private fun findHidden(state: GameState.Playing): Position {
        for (r in 0 until state.config.rows) for (c in 0 until state.config.cols) {
            val pos = Position(r, c)
            if (!state.board.isRevealed(pos) && !state.board.isMine(pos)) return pos
        }
        error("no hidden non-mine cell")
    }

    private fun findMine(state: GameState.Playing): Position {
        for (r in 0 until state.config.rows) for (c in 0 until state.config.cols) {
            val pos = Position(r, c)
            if (state.board.isMine(pos)) return pos
        }
        error("no mine on board")
    }

    private fun findNumberedCellWithMineNeighbors(state: GameState.Playing): Pair<Position, List<Position>>? {
        for (r in 0 until state.config.rows) for (c in 0 until state.config.cols) {
            val pos = Position(r, c)
            val cell = state.board.cellAt(pos)
            if (cell is Cell.Revealed && cell.adjacentMines > 0) {
                val mineNeighbors = buildList {
                    state.board.forEachNeighborPublic(pos) { n ->
                        if (state.board.isMine(n)) add(n)
                    }
                }
                if (mineNeighbors.size == cell.adjacentMines) return pos to mineNeighbors
            }
        }
        return null
    }
}

private inline fun Board.forEachNeighborPublic(pos: Position, action: (Position) -> Unit) {
    for (dr in -1..1) for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val r = pos.row + dr
        val c = pos.col + dc
        if (r in 0 until rows && c in 0 until cols) action(Position(r, c))
    }
}
