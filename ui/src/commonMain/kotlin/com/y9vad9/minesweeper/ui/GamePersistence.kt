package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.logic.GameStateRepository
import com.y9vad9.minesweeper.logic.PersistedGame
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

fun gamePersistencePlugin(
    repository: GameStateRepository,
): StorePlugin<GameScreenState, GameIntent, GameUiAction> = persistStatePlugin(
    name = "GamePersistence",
    load = {
        repository.load()?.let { persisted ->
            GameScreenState(
                game = persisted.game,
                seedInput = "",
                lastConfig = persisted.game.config,
                flagMode = persisted.flagMode,
            )
        }
    },
    save = { state ->
        repository.save(PersistedGame(game = state.game, flagMode = state.flagMode))
    },
    shouldSave = { old, new -> old.game != new.game || old.flagMode != new.flagMode },
)

fun StoreBuilder<GameScreenState, GameIntent, GameUiAction>.installGamePersistence(
    repository: GameStateRepository?,
) {
    repository?.let { install(gamePersistencePlugin(it)) }
}
