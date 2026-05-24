package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.logic.GameRecord
import com.y9vad9.minesweeper.logic.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

data class HistoryState(
    val records: List<GameRecord>,
    val loaded: Boolean,
) : MVIState

sealed interface HistoryIntent : MVIIntent {
    data class Add(val record: GameRecord) : HistoryIntent
    data object Clear : HistoryIntent
    data class ReplayRequested(val record: GameRecord) : HistoryIntent
}

sealed interface HistoryAction : MVIAction {
    data class Replay(val record: GameRecord) : HistoryAction
}

typealias HistoryStore = Store<HistoryState, HistoryIntent, HistoryAction>

fun historyStore(
    scope: CoroutineScope,
    repository: HistoryRepository,
    maxRecords: Int = 100,
): HistoryStore = store(
    initial = HistoryState(records = emptyList(), loaded = false),
    scope = scope,
) {
    recover { null }

    init {
        val records = repository.loadAll()
        updateState { copy(records = records, loaded = true) }
    }

    reduce { intent: HistoryIntent ->
        when (intent) {
            is HistoryIntent.Add -> {
                repository.add(intent.record)
                repository.trimToCap(maxRecords)
                val next = repository.loadAll()
                updateState { copy(records = next) }
            }
            HistoryIntent.Clear -> {
                repository.clear()
                updateState { copy(records = emptyList()) }
            }
            is HistoryIntent.ReplayRequested -> action(HistoryAction.Replay(intent.record))
        }
    }
}
