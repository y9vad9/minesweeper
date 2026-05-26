package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import com.y9vad9.minesweeper.GameState
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberElapsed(game: GameState, tickEveryMs: Long = 100L): State<Duration> =
    produceState(initialValue = startingValue(game), key1 = game) {
        value = startingValue(game)
        if (game is GameState.Playing) {
            while (true) {
                value = Clock.System.now() - game.startedAt
                delay(tickEveryMs)
            }
        }
    }

private fun startingValue(game: GameState): Duration = when (game) {
    is GameState.Playing -> Clock.System.now() - game.startedAt
    is GameState.Won -> game.elapsed
    is GameState.Lost -> game.elapsed
    is GameState.Idle -> 0.milliseconds
}

fun Duration.formatClock(): String {
    val total = inWholeSeconds
    val mm = total / 60
    val ss = total % 60
    return "${mm.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}"
}
