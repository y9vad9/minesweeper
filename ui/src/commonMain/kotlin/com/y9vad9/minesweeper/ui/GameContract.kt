package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.logic.Difficulty
import com.y9vad9.minesweeper.logic.GameConfig
import com.y9vad9.minesweeper.logic.GameState
import com.y9vad9.minesweeper.logic.Position
import com.y9vad9.minesweeper.logic.Seed
import com.y9vad9.minesweeper.logic.toCode
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

data class GameScreenState(
    val game: GameState,
    val seedInput: String,
    val lastConfig: GameConfig,
    val flagMode: Boolean = false,
) : MVIState {
    val difficulty: Difficulty
        get() = Difficulty.matchOrClosest(lastConfig.rows, lastConfig.cols, lastConfig.mines)

    val currentCode: String get() = lastConfig.toCode()
}

sealed interface GameIntent : MVIIntent {
    data class CellRevealed(val pos: Position) : GameIntent
    data class CellFlagged(val pos: Position) : GameIntent
    data class CellChorded(val pos: Position) : GameIntent
    data object NewGameRequested : GameIntent
    data class DifficultyPicked(val difficulty: Difficulty) : GameIntent
    data class CustomGameRequested(
        val rows: Int,
        val cols: Int,
        val mines: Int,
        val seedInput: String,
    ) : GameIntent
    data class SeedInputChanged(val value: String) : GameIntent
    data object FlagModeToggled : GameIntent
    data class ReplayRequested(val rows: Int, val cols: Int, val mines: Int, val seed: Seed) : GameIntent
}

sealed interface GameUiAction : MVIAction {
    data class InvalidSeed(val raw: String) : GameUiAction
    data object GameWon : GameUiAction
    data object GameLost : GameUiAction
}
