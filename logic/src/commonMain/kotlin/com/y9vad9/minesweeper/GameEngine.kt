package com.y9vad9.minesweeper

import kotlin.time.Clock
import kotlin.time.Instant

class GameContractViolation(message: String) : IllegalStateException(message)

class GameEngine(
    private val clock: Clock = Clock.System,
) {
    fun newGame(config: GameConfig): GameState.Idle = GameState.Idle(config)

    fun reveal(state: GameState, pos: Position): GameState = when (state) {
        is GameState.Idle -> revealFirst(state, pos)
        is GameState.Playing -> revealDuringPlay(state, pos)
        is GameState.Won, is GameState.Lost ->
            throw GameContractViolation("reveal() called on finished game (${state.status})")
    }

    fun flag(state: GameState, pos: Position): GameState = when (state) {
        is GameState.Idle -> state
        is GameState.Playing -> {
            requireInBounds(state.board, pos)
            if (state.board.isRevealed(pos)) state
            else state.copy(board = state.board.withFlagToggled(pos))
        }
        is GameState.Won, is GameState.Lost ->
            throw GameContractViolation("flag() called on finished game (${state.status})")
    }

    fun chord(state: GameState, pos: Position): GameState {
        if (state !is GameState.Playing) {
            throw GameContractViolation("chord() requires Playing, was ${state.status}")
        }
        requireInBounds(state.board, pos)
        val cell = state.board.cellAt(pos)
        if (cell !is Cell.Revealed || cell.adjacentMines == 0) return state

        val neighbors = neighborsOf(state.board, pos)
        val flaggedNeighbors = neighbors.count(state.board::isFlagged)
        if (flaggedNeighbors != cell.adjacentMines) return state

        var next: GameState = state
        for (n in neighbors) {
            if (state.board.isFlagged(n) || state.board.isRevealed(n)) continue
            next = reveal(next, n)
            if (next is GameState.Lost) return next
        }
        return next
    }

    private fun revealFirst(state: GameState.Idle, pos: Position): GameState {
        requireInBoundsOf(state.config, pos)
        val board = BoardGenerator.generate(state.config, pos)
        val playing = GameState.Playing(
            config = state.config,
            board = board,
            startedAt = clock.now(),
        )
        return revealDuringPlay(playing, pos)
    }

    private fun revealDuringPlay(state: GameState.Playing, pos: Position): GameState {
        requireInBounds(state.board, pos)
        val board = state.board
        if (board.isFlagged(pos) || board.isRevealed(pos)) return state

        if (board.isMine(pos)) {
            return GameState.Lost(
                config = state.config,
                board = board.withAllMinesRevealed(pos),
                triggeredAt = pos,
                elapsed = elapsedSince(state.startedAt),
            )
        }

        val cascade = floodFill(board, pos)
        val newBoard = board.withRevealed(cascade)

        return if (newBoard.isCleared) {
            GameState.Won(
                config = state.config,
                board = newBoard,
                elapsed = elapsedSince(state.startedAt),
            )
        } else {
            state.copy(board = newBoard)
        }
    }

    private fun floodFill(board: Board, start: Position): Set<Position> {
        val visited = mutableSetOf(start)
        if (board.adjacentMineCount(start) != 0) return visited
        val queue = ArrayDeque<Position>().apply { add(start) }
        while (queue.isNotEmpty()) {
            val pos = queue.removeFirst()
            board.forEachNeighbor(pos) { n ->
                if (n in visited || board.isRevealed(n) || board.isFlagged(n)) return@forEachNeighbor
                if (board.isMine(n)) return@forEachNeighbor
                visited += n
                if (board.adjacentMineCount(n) == 0) queue.addLast(n)
            }
        }
        return visited
    }

    private fun neighborsOf(board: Board, pos: Position): List<Position> =
        buildList(8) { board.forEachNeighbor(pos) { add(it) } }

    private fun elapsedSince(start: Instant) = clock.now() - start

    private fun requireInBounds(board: Board, pos: Position) {
        if (!board.inBounds(pos)) {
            throw GameContractViolation("position out of bounds: $pos on ${board.rows}x${board.cols} board")
        }
    }

    private fun requireInBoundsOf(config: GameConfig, pos: Position) {
        if (pos.row !in 0 until config.rows || pos.col !in 0 until config.cols) {
            throw GameContractViolation("position out of bounds: $pos on ${config.rows}x${config.cols} board")
        }
    }
}
