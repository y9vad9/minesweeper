package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.logic.Difficulty
import com.y9vad9.minesweeper.logic.GameConfig
import com.y9vad9.minesweeper.logic.GameCodeParseResult
import com.y9vad9.minesweeper.logic.GameEngine
import com.y9vad9.minesweeper.logic.GameState
import com.y9vad9.minesweeper.logic.GameStateRepository
import com.y9vad9.minesweeper.logic.Seed
import com.y9vad9.minesweeper.logic.parseGameCode
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce

typealias GameStore = Store<GameScreenState, GameIntent, GameUiAction>
private typealias GameContext = PipelineContext<GameScreenState, GameIntent, GameUiAction>

fun gameStore(
    scope: CoroutineScope,
    engine: GameEngine = GameEngine(),
    initialConfig: GameConfig = Difficulty.Easy.toConfig(Seed.random()),
    persistence: GameStateRepository? = null,
): GameStore = store(
    initial = GameScreenState(
        game = engine.newGame(initialConfig),
        seedInput = "",
        lastConfig = initialConfig,
    ),
    scope = scope,
) {
    installGamePersistence(persistence)

    reduce { intent: GameIntent ->
        when (intent) {
            is GameIntent.CellRevealed -> applyIfActive { copy(game = engine.reveal(game, intent.pos)) }
            is GameIntent.CellFlagged -> applyIfActive { copy(game = engine.flag(game, intent.pos)) }
            is GameIntent.CellChorded -> applyIfActive { copy(game = engine.chord(game, intent.pos)) }

            GameIntent.NewGameRequested -> startNewGame(engine)

            is GameIntent.DifficultyPicked -> updateState {
                val config = intent.difficulty.toConfig(Seed.random())
                copy(game = engine.newGame(config), lastConfig = config, seedInput = "")
            }

            is GameIntent.CustomGameRequested -> updateState {
                val seed = if (intent.seedInput.isBlank()) {
                    Seed.random()
                } else {
                    intent.seedInput.trim().toULongOrNull(16)?.let { Seed(it.toLong()) }
                        ?: return@updateState this
                }
                val config = runCatching {
                    GameConfig(intent.rows, intent.cols, intent.mines, seed)
                }.getOrNull() ?: return@updateState this
                copy(game = engine.newGame(config), lastConfig = config, seedInput = "")
            }

            is GameIntent.SeedInputChanged -> updateState { copy(seedInput = intent.value) }

            GameIntent.FlagModeToggled -> updateState { copy(flagMode = !flagMode) }

            is GameIntent.ReplayRequested -> updateState {
                val config = GameConfig(intent.rows, intent.cols, intent.mines, intent.seed)
                copy(game = engine.newGame(config), lastConfig = config, seedInput = "")
            }
        }
        emitTerminalActionIfNeeded()
    }
}

private suspend inline fun GameContext.applyIfActive(
    crossinline transform: GameScreenState.() -> GameScreenState,
) {
    updateState {
        if (game is GameState.Playing || game is GameState.Idle) transform() else this
    }
}

private suspend fun GameContext.startNewGame(engine: GameEngine) {
    updateState {
        val config = when (val parsed = parseGameCode(seedInput)) {
            is GameCodeParseResult.Success -> parsed.config
            GameCodeParseResult.Blank -> lastConfig.copy(seed = Seed.random())
            GameCodeParseResult.InvalidFormat -> {
                action(GameUiAction.InvalidSeed(seedInput))
                return@updateState this
            }
        }
        copy(game = engine.newGame(config), lastConfig = config, seedInput = "")
    }
}

private suspend fun GameContext.emitTerminalActionIfNeeded() {
    withState {
        when (game) {
            is GameState.Won -> action(GameUiAction.GameWon)
            is GameState.Lost -> action(GameUiAction.GameLost)
            else -> Unit
        }
    }
}
