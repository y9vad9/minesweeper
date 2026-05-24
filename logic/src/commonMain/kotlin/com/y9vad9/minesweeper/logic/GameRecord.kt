package com.y9vad9.minesweeper.logic

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

enum class GameOutcome { Won, Lost }

data class GameRecord(
    val seed: Seed,
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val outcome: GameOutcome,
    val durationMillis: Long,
    val playedAtEpochMillis: Long,
    val threeBV: Int? = null,
    val threeBVSolved: Int? = null,
) {
    val duration: Duration get() = durationMillis.milliseconds

    fun toConfig(): GameConfig = GameConfig(rows, cols, mines, seed)

    val threeBVPerSecond: Double?
        get() {
            val solved = threeBVSolved ?: return null
            if (durationMillis <= 0) return null
            return solved * 1000.0 / durationMillis
        }
}

fun gameRecordFromFinished(
    state: GameState,
    nowEpochMillis: Long,
    threeBV: Int? = null,
    threeBVSolved: Int? = null,
): GameRecord? = when (state) {
    is GameState.Won -> GameRecord(
        seed = state.config.seed,
        rows = state.config.rows,
        cols = state.config.cols,
        mines = state.config.mines,
        outcome = GameOutcome.Won,
        durationMillis = state.elapsed.inWholeMilliseconds,
        playedAtEpochMillis = nowEpochMillis,
        threeBV = threeBV,
        threeBVSolved = threeBVSolved ?: threeBV,
    )
    is GameState.Lost -> GameRecord(
        seed = state.config.seed,
        rows = state.config.rows,
        cols = state.config.cols,
        mines = state.config.mines,
        outcome = GameOutcome.Lost,
        durationMillis = state.elapsed.inWholeMilliseconds,
        playedAtEpochMillis = nowEpochMillis,
        threeBV = threeBV,
        threeBVSolved = threeBVSolved,
    )
    else -> null
}
