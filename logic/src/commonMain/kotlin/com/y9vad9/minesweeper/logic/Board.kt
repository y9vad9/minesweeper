package com.y9vad9.minesweeper.logic

@Suppress("TooManyFunctions")
class Board internal constructor(
    val rows: Int,
    val cols: Int,
    private val mines: Set<Position>,
    private val revealed: Set<Position>,
    private val flagged: Set<Position>,
    private val explodedAt: Position?,
) {
    val flagCount: Int get() = flagged.size
    val revealedCount: Int get() = revealed.size
    val mineCount: Int get() = mines.size

    val isCleared: Boolean
        get() = revealedCount == rows * cols - mineCount

    fun inBounds(pos: Position): Boolean =
        pos.row in 0 until rows && pos.col in 0 until cols

    fun isMine(pos: Position): Boolean = pos in mines

    fun isRevealed(pos: Position): Boolean = pos in revealed

    fun isFlagged(pos: Position): Boolean = pos in flagged

    fun cellAt(pos: Position): Cell {
        require(inBounds(pos)) { "position out of bounds: $pos" }
        return when {
            pos == explodedAt -> Cell.ExplodedMine
            pos in revealed && pos in mines -> Cell.RevealedMine
            pos in revealed -> Cell.Revealed(adjacentMineCount(pos))
            else -> Cell.Hidden(flagged = pos in flagged)
        }
    }

    fun adjacentMineCount(pos: Position): Int {
        var count = 0
        forEachNeighbor(pos) { if (it in mines) count++ }
        return count
    }

    internal inline fun forEachNeighbor(pos: Position, action: (Position) -> Unit) {
        for (dr in -1..1) for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val r = pos.row + dr
            val c = pos.col + dc
            if (r in 0 until rows && c in 0 until cols) action(Position(r, c))
        }
    }

    internal fun withFlagToggled(pos: Position): Board {
        val newFlagged = if (pos in flagged) flagged - pos else flagged + pos
        return copy(flagged = newFlagged)
    }

    internal fun withRevealed(positions: Set<Position>): Board =
        copy(revealed = revealed + positions, flagged = flagged - positions)

    internal fun withAllMinesRevealed(explodedAt: Position): Board =
        copy(revealed = revealed + mines, explodedAt = explodedAt)

    private fun copy(
        mines: Set<Position> = this.mines,
        revealed: Set<Position> = this.revealed,
        flagged: Set<Position> = this.flagged,
        explodedAt: Position? = this.explodedAt,
    ) = Board(rows, cols, mines, revealed, flagged, explodedAt)

    fun snapshot(): BoardSnapshot = BoardSnapshot(rows, cols, mines, revealed, flagged, explodedAt)

    companion object {
        fun restore(snapshot: BoardSnapshot): Board = Board(
            rows = snapshot.rows,
            cols = snapshot.cols,
            mines = snapshot.mines,
            revealed = snapshot.revealed,
            flagged = snapshot.flagged,
            explodedAt = snapshot.explodedAt,
        )
    }
}

data class BoardSnapshot(
    val rows: Int,
    val cols: Int,
    val mines: Set<Position>,
    val revealed: Set<Position>,
    val flagged: Set<Position>,
    val explodedAt: Position?,
)
