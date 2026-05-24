package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.Board
import com.y9vad9.minesweeper.logic.BoardSnapshot
import com.y9vad9.minesweeper.logic.GameConfig
import com.y9vad9.minesweeper.logic.GameState
import com.y9vad9.minesweeper.logic.PersistedGame
import com.y9vad9.minesweeper.logic.Position
import com.y9vad9.minesweeper.logic.Seed
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Serializable
internal data class PositionDto(val row: Int, val col: Int)

@Serializable
internal data class GameConfigDto(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val seed: Long,
)

@Serializable
internal data class BoardDto(
    val rows: Int,
    val cols: Int,
    val mines: List<PositionDto>,
    val revealed: List<PositionDto>,
    val flagged: List<PositionDto>,
    val explodedAt: PositionDto? = null,
)

@Serializable
internal data class PersistedGameDto(
    val status: String,
    val config: GameConfigDto,
    val board: BoardDto? = null,
    val startedAtEpochMillis: Long? = null,
    val elapsedMillis: Long? = null,
    val triggeredAt: PositionDto? = null,
    val flagMode: Boolean = false,
)

private fun Position.toDto() = PositionDto(row, col)
private fun PositionDto.toDomain() = Position(row, col)

private fun GameConfig.toDto() = GameConfigDto(rows, cols, mines, seed.value)
private fun GameConfigDto.toDomain() = GameConfig(rows, cols, mines, Seed(seed))

private fun BoardSnapshot.toDto() = BoardDto(
    rows = rows,
    cols = cols,
    mines = mines.map { it.toDto() },
    revealed = revealed.map { it.toDto() },
    flagged = flagged.map { it.toDto() },
    explodedAt = explodedAt?.toDto(),
)

private fun BoardDto.toDomain(): Board = Board.restore(
    BoardSnapshot(
        rows = rows,
        cols = cols,
        mines = mines.map { it.toDomain() }.toSet(),
        revealed = revealed.map { it.toDomain() }.toSet(),
        flagged = flagged.map { it.toDomain() }.toSet(),
        explodedAt = explodedAt?.toDomain(),
    )
)

internal fun PersistedGame.toDto(): PersistedGameDto {
    val configDto = game.config.toDto()
    return when (val g = game) {
        is GameState.Idle -> PersistedGameDto(
            status = "Idle",
            config = configDto,
            flagMode = flagMode,
        )
        is GameState.Playing -> PersistedGameDto(
            status = "Playing",
            config = configDto,
            board = g.board.snapshot().toDto(),
            startedAtEpochMillis = g.startedAt.toEpochMilliseconds(),
            flagMode = flagMode,
        )
        is GameState.Won -> PersistedGameDto(
            status = "Won",
            config = configDto,
            board = g.board.snapshot().toDto(),
            elapsedMillis = g.elapsed.inWholeMilliseconds,
            flagMode = flagMode,
        )
        is GameState.Lost -> PersistedGameDto(
            status = "Lost",
            config = configDto,
            board = g.board.snapshot().toDto(),
            elapsedMillis = g.elapsed.inWholeMilliseconds,
            triggeredAt = g.triggeredAt.toDto(),
            flagMode = flagMode,
        )
    }
}

internal fun PersistedGameDto.toDomain(): PersistedGame? {
    val config = config.toDomain()
    val game: GameState = when (status) {
        "Idle" -> GameState.Idle(config)
        "Playing" -> GameState.Playing(
            config = config,
            board = board?.toDomain() ?: return null,
            startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis ?: return null),
        )
        "Won" -> GameState.Won(
            config = config,
            board = board?.toDomain() ?: return null,
            elapsed = (elapsedMillis ?: return null).milliseconds,
        )
        "Lost" -> GameState.Lost(
            config = config,
            board = board?.toDomain() ?: return null,
            triggeredAt = triggeredAt?.toDomain() ?: return null,
            elapsed = (elapsedMillis ?: return null).milliseconds,
        )
        else -> return null
    }
    return PersistedGame(game = game, flagMode = flagMode)
}
