package com.y9vad9.minesweeper

import kotlin.time.Duration
import kotlin.time.Instant

sealed interface GameState {
    val config: GameConfig

    data class Idle(override val config: GameConfig) : GameState

    data class Playing(
        override val config: GameConfig,
        val board: Board,
        val startedAt: Instant,
    ) : GameState

    data class Won(
        override val config: GameConfig,
        val board: Board,
        val elapsed: Duration,
    ) : GameState

    data class Lost(
        override val config: GameConfig,
        val board: Board,
        val triggeredAt: Position,
        val elapsed: Duration,
    ) : GameState
}

enum class GameStatus { Idle, Playing, Won, Lost }

val GameState.status: GameStatus
    get() = when (this) {
        is GameState.Idle -> GameStatus.Idle
        is GameState.Playing -> GameStatus.Playing
        is GameState.Won -> GameStatus.Won
        is GameState.Lost -> GameStatus.Lost
    }
