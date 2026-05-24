package com.y9vad9.minesweeper.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.plugin

fun <S : MVIState, I : MVIIntent, A : MVIAction> persistStatePlugin(
    name: String,
    load: suspend () -> S?,
    save: suspend (S) -> Unit,
    shouldSave: (old: S, new: S) -> Boolean = { _, _ -> true },
): StorePlugin<S, I, A> = plugin {
    this.name = name
    val pending = Channel<S>(Channel.CONFLATED)
    onStart {
        load()?.let { restored -> updateState { restored } }
        launch {
            for (state in pending) runCatching { save(state) }
        }
    }
    onState { old, new ->
        if (shouldSave(old, new)) pending.trySend(new)
        new
    }
}
